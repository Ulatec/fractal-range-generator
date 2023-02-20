package com.example.fractalrangecalculator.Threads;


import com.example.fractalrangecalculator.Libraries.TreasuryRateLibrary;
import com.example.fractalrangecalculator.Model.Bar;
import com.example.fractalrangecalculator.Model.OptionContract;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.fractalrangecalculator.Util.DateStringFormatter.getFormattedDateString;

public class IVSolverThread extends Thread{

    private List<Bar> barList;

    private final IVSolverMonitor ivSolverMonitor;

    private final String ticker;

    private final int daysVol;

    private final String apiKey;
    private final int threadNum;
    private List<OptionContract> filteredContracts = new ArrayList<>();
    //Store closest contracts to current price.
    private OptionContract closestContract = null;
    private OptionContract secondClosestContract = null;

    public IVSolverThread( int threadNum, List<Bar> barList, IVSolverMonitor ivSolverMonitor, String ticker, int daysVol, String apiKey){
        this.barList = barList;
        this.ivSolverMonitor = ivSolverMonitor;
        this.ticker = ticker;
        this.daysVol = daysVol;
        this.apiKey = apiKey;
        this.threadNum = threadNum;
    }


    public void run(){
        //JSONArray jsonArray = getTreasuryRate();
        for(Bar bar : barList){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            bar.setPut_implied_vol(calculatePutImpliedVol(bar, ticker, daysVol));

        }
        ivSolverMonitor.threadFinished(threadNum, barList);
    }



    public double calculatePutImpliedVol(Bar bar, String ticker, int dayCalc){
        Date date = bar.getDate();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate thirtyDays = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(dayCalc);
        LocalDate fortyFiveDayForward = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(dayCalc + 15);
        LocalDate fifteenDayForward = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(dayCalc).minusDays(15);
        LocalDate fiveDaysBack = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(6);
        String dateString = getFormattedDateString(localDate);
        String fortyFiveDayString = getFormattedDateString(fortyFiveDayForward);
        String fifteenDayString = getFormattedDateString(fifteenDayForward);
        String fiveDaysBackString = getFormattedDateString(fiveDaysBack);
        try {
            String url3 = "https://api.polygon.io/v3/reference/options/contracts?underlying_ticker=" +
                    ticker +
                    "&contract_type=put" +
                    "&as_of=" + dateString +
                    "&expired=false" +
                    "&expiration_date.lte=" + fortyFiveDayString +
                    "&expiration_date.gte=" + fifteenDayString +
                    "&limit=1000" +
                    "&sort=expiration_date" +
                    "&apiKey=rcJCxVUqKDfgcSgLSkDkQpnfVn0rk9Ne";

            Connection.Response response3 = Jsoup.connect
                            (url3)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

            JSONObject jsonObject3 = new JSONObject(response3.body());
            JSONArray resultArray3 = jsonObject3.getJSONArray("results");

            List<OptionContract> optionContracts = new ArrayList<>();
            for(Object object : resultArray3){
                JSONObject jsonObject = (JSONObject) object;
                OptionContract optionContract = new OptionContract();
                optionContract.setTicker(jsonObject.getString("ticker"));
                optionContract.setUnderlyingTicker(jsonObject.getString("underlying_ticker"));
                optionContract.setStrike(jsonObject.getDouble("strike_price"));
                String expDateString = jsonObject.getString("expiration_date");
                String[] split = expDateString.split("-");
                LocalDate expDate = LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]),Integer.parseInt(split[2]));
                optionContract.setExpirationDate(expDate);
                optionContracts.add(optionContract);
            }

            int closestDays = 10000;
            for(OptionContract optionContract : optionContracts){
                Period period = Period.between(thirtyDays, optionContract.getExpirationDate());
                if(Math.abs(period.getDays()) < closestDays){
                    secondClosestContract = closestContract;
                    closestContract = optionContract;
                    closestDays = Math.abs(period.getDays());
                }
            }
            assembledFilteredContracts(optionContracts, bar);
            filteredContracts.sort(Comparator.comparing(OptionContract::getStrike).reversed());

            List<Double> ivList = new ArrayList<>();
            int completedIVs = 0;
            int i = 0;
            //attempt to get 2 implied volatility calculation numbers
            while(completedIVs<2 && i<filteredContracts.size()){
                try {
                String contractQuery = "https://api.polygon.io/v2/aggs/ticker/" + filteredContracts.get(i).getTicker() + "/range/1/day/" + fiveDaysBackString + "/" + dateString + "?adjusted=true&sort=asc&limit=120&apiKey=" + apiKey;
                Connection.Response contractResponse = Jsoup.connect
                                (contractQuery)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .execute();

                JSONObject contractJsonObject = new JSONObject(contractResponse.body());
                    if(contractJsonObject.getInt("resultsCount")>0) {
                    JSONArray contractArray = contractJsonObject.getJSONArray("results");
                        double price;
                        price = contractArray.getJSONObject(contractArray.length() - 1).getDouble("c");
                        filteredContracts.get(i).setPrice(price);

                        long timeDiff = Math.abs(Date.from(filteredContracts.get(i).getExpirationDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()).getTime()
                                - Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()).getTime());
                        long daysDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

                        double rate = findTreasuryRate(localDate);

                        double iv = getImpliedVolatility(price, false, bar.getClose(), filteredContracts.get(i).getStrike(), rate/100, (double) daysDiff / 365);
                        if(!Double.isNaN(iv)){
                            iv = iv * ((double)daysVol/(double)daysDiff);
                            ivList.add(iv);
                            completedIVs = completedIVs + 1;
                        }else{
                            //Retry calculation with split adjustment factor. This is due to Polygon's Dataset not being consistent.
                            iv = getImpliedVolatility(price, false, bar.getClose() * bar.getSplitAdjustFactor(),
                                    filteredContracts.get(i).getStrike(), rate/100, (double) daysDiff /365);
                            if(!Double.isNaN(iv)) {
                                iv = iv * ((double) daysVol / (double) daysDiff);
                                ivList.add(iv);
                                completedIVs = completedIVs + 1;
                            }
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                i = i + 1;
            }
            DoubleSummaryStatistics doubleSummaryStatistics = ivList.stream().mapToDouble(x -> x).summaryStatistics();
            return doubleSummaryStatistics.getAverage();
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0.0;
    }

    public double findTreasuryRate(LocalDate localDate){
        long time = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()).getTime();
        for (int x = 0; x < TreasuryRateLibrary.dateArray.length; x++) {
            if (TreasuryRateLibrary.dateArray[x] < time) {
                return TreasuryRateLibrary.dataArray[x];
            }
        }
        return 0.0;
    }

    public void assembledFilteredContracts(List<OptionContract> optionContracts, Bar bar){

        filteredContracts = new ArrayList<>();
        for(OptionContract optionContract : optionContracts){
            try {
                if(closestContract != null){
                    if(optionContract.getExpirationDate().equals(closestContract.getExpirationDate()) && (int)(optionContract.getStrike()/ bar.getSplitAdjustFactor()) < bar.getClose()){
                        filteredContracts.add(optionContract);
                    }
                }
                if(secondClosestContract != null){
                    if(optionContract.getExpirationDate().equals(secondClosestContract.getExpirationDate()) && (int)(optionContract.getStrike()/ bar.getSplitAdjustFactor()) < bar.getClose()){
                        filteredContracts.add(optionContract);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public double getImpliedVolatility(double target, boolean callFlag, double stockPrice, double strike, double riskFreeRate, double expirationTime){
        int MAX_ITERATIONS = 100;
        double PRECISION = 1E-5;

        double sigma =0.5;
        for(int i = 0; i < MAX_ITERATIONS; i++){
            double price = getBsPrice(callFlag, stockPrice, strike, riskFreeRate, expirationTime, sigma);
            double vega = getBsVega(callFlag, stockPrice, strike, riskFreeRate, expirationTime,sigma);

            double diff = target - price;

            if(Math.abs(diff) < PRECISION){
                return sigma;
            }
            sigma = sigma + diff/vega;
        }

        return sigma;
    }
    public double getBsPrice(boolean callFlag, double stockPrice, double strike, double riskFreeRate, double expirationTime, double sigma){
        double q =0.0;
        double d1 = ((Math.log(stockPrice/strike)) + (riskFreeRate+sigma*sigma/2.)*expirationTime)/(sigma*Math.sqrt(expirationTime));
        double d2 = d1-sigma*Math.sqrt(expirationTime);
        double price;
        NormalDistribution normalDistribution = new NormalDistribution();
        if(callFlag){
            price = stockPrice*Math.exp(-q * expirationTime)*normalDistribution.cumulativeProbability(d1)-strike*Math.exp(-riskFreeRate*expirationTime)*normalDistribution.cumulativeProbability(d2);
        }else{
            price = strike*Math.exp(-riskFreeRate * expirationTime)*normalDistribution.cumulativeProbability(-d2)-stockPrice*Math.exp(-q*expirationTime)*normalDistribution.cumulativeProbability(-d1);
        }
        return price;
    }
    public double getBsVega(boolean callFlag, double stockPrice, double strike, double riskFreeRate, double expirationTime, double sigma){
        double d1 = (Math.log(stockPrice/strike) + (riskFreeRate+sigma*sigma/2.)*expirationTime)/(sigma*Math.sqrt(expirationTime));
        NormalDistribution normalDistribution = new NormalDistribution();
        return stockPrice * Math.sqrt(expirationTime)*normalDistribution.density(d1);
    }



}
