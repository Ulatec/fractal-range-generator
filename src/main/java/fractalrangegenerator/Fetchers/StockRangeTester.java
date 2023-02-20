package com.example.fractalrangecalculator.Fetchers;


import com.example.fractalrangecalculator.Model.*;
import com.example.fractalrangecalculator.Threads.IVSolverMonitor;
import com.example.fractalrangecalculator.Threads.IVSolverThread;
import com.example.fractalrangecalculator.Util.ListSplitter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import static com.example.fractalrangecalculator.Libraries.StockCalculationLibrary.*;
import static com.example.fractalrangecalculator.Util.DateStringFormatter.getFormattedDateString;


public class StockRangeTester {

    public String apiKey;
    public static String baseURL = "https://api.polygon.io/v2/aggs/ticker/";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public List<Bar> run(String ticker) throws Exception {
        int threads = 16;
        int daysVol = 40;
        boolean rangeAdjustmentFlip = false;
        boolean RVAdjustmentFlip = false;
        boolean volumeADjustmentFlip = false;
        boolean impliedVolFlip = false;
        double ivWeighting = 1;
        double discountWeighting = 1;
        double realizedVolWeighting = 1;
        double volumeWeighting = 1;
        int ivTrendType = 1;
        int trendLengthToTest = 125;
        int tradeLengthToTest = 35;

        LocalDate today = LocalDate.now();

        String url = buildAPIExecutionString(ticker,today.minusYears(1),today,365);

        Connection.Response response3 = Jsoup.connect(url)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        JSONObject jsonObject3 = new JSONObject(response3.body());
        JSONArray resultArray3 = jsonObject3.getJSONArray("results");
        List<Bar> barListSafe = new ArrayList<>();
        for (Object object : resultArray3) {
            barListSafe.add(buildBarFromJsonData(object));
        }
        String url4 = "https://api.polygon.io/v3/reference/splits?ticker=" + ticker + "&apiKey=" + apiKey;
        Connection.Response response4 = Jsoup.connect
                        (url4)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .execute();
        JSONObject jsonObject4 = new JSONObject(response4.body());
        adjustSeriesForStockSplits(jsonObject4.getJSONArray("results"), barListSafe);
        //init monitor to listen for when all solver threads are finished.
        IVSolverMonitor ivSolverMonitor = new IVSolverMonitor(threads, barListSafe.size());
        List<List<Bar>> listOfLists = ListSplitter.splitBar(barListSafe, threads);
        //remove any empty lists.
        listOfLists.removeIf(list -> list.size() == 0);
        for (int x = 0; x < listOfLists.size(); x++) {
            IVSolverThread ivSolverThread = new IVSolverThread(x, listOfLists.get(x), ivSolverMonitor, ticker, (int) daysVol, apiKey);
            ivSolverThread.start();
        }
        //wait until all threads are finished.
        while (ivSolverMonitor.getImpliedVolailityCalculationResults() == null) {}

        barListSafe = ivSolverMonitor.getImpliedVolailityCalculationResults();
        barListSafe.sort(Comparator.comparing(Bar::getDate));
        List<Bar> barList = new ArrayList<>(barListSafe);
        Collections.reverse(barList);
                    int i = 0;
                    for (Bar bar : barList) {
                        bar.setMovingTrendLength((int) (trendLengthToTest * bar.getPut_implied_vol()));
                        bar.setMovingTradeLength((int) (tradeLengthToTest * bar.getPut_implied_vol()));
                        bar.setWma(weightedMovingAverage(barList, i, bar.getMovingTrendLength() + 1));
                        bar.setTrendWMA(weightedMovingAverage(barList, i, bar.getMovingTrendLength() + 1));
                        bar.setTradeVol(getLogVariance(barList, i, bar.getMovingTradeLength() + 1));
                        bar.setTrendVol(getLogVariance(barList, i, bar.getMovingTrendLength() + 1));
                        i++;
                    }
                    i = 0;
                    for (Bar bar : barList) {
                        bar.setSlope(getSlope(barList, i, bar.getMovingTradeLength()));
                        i++;
                    }
                    i = 0;
                    for (Bar bar : barList) {
                        bar.setMinDiff(min_diff(barList, i, bar.getSlope(), bar.getMovingTradeLength()));
                        bar.setMaxDiff(max_diff(barList, i, bar.getSlope(), bar.getMovingTradeLength()));
                        i++;
                    }
                    i = 0;
                    for (Bar bar : barList) {
                        bar.setStdDev(stdDev(barList, i, bar.getMovingTradeLength() + 1));
                        bar.setAverageTrueRange(averageTrueRange(barList, i, bar.getMovingTradeLength() + 1));
                        bar.setRma(getRMA(barList, i, bar.getMovingTradeLength() + 1));
                        i++;
                    }
                    i = 0;
                    for (Bar bar : barList) {
                        bar.setHurst(getHurst(barList, i, bar.getMovingTradeLength() + 1));
                        bar.setBb_bottom(getBridgeBottom(bar));
                        bar.setBb_top(getBridgeTop(bar));
                        bar.setIvDiscount(bar.getPut_implied_vol() - getLogVariance(barList, i, daysVol + 1));
                        i++;
                    }

                    for (int k = 0; k < barList.size(); k++) {
                        int movingTrendLength = barList.get(k).getMovingTrendLength();
                        if (k + movingTrendLength + 1 < barList.size()) {
                            double IVDiscountAdjustment = calculateFirstDerivativeChangeInImpliedVolatilityDiscount(barList,k,movingTrendLength);
                            double IVAdjustment = calculateFirstDerivativeChangeInImpliedVolatility(barList,k,movingTrendLength);
                            double rangeAdjustment = calculateFirstDerivativeChangeInVariables(barList, k, movingTrendLength);
                            double volumeAdjustment = calculateFirstDerivativeChangeInVolume(barList, k, movingTrendLength);
                            double volAdjustment = calculateFirstDerivativeChangeInRealizedVolatility(barList,k,movingTrendLength);
                            double wmaAdjustment = calculateFirstDerivativeChangeInWeightedMovingAverage(barList,k,movingTrendLength);


                            barList.get(k).setTrend( applySeriesAdjustments(rangeAdjustmentFlip, RVAdjustmentFlip,
                                    volumeADjustmentFlip, impliedVolFlip, ivWeighting, discountWeighting, realizedVolWeighting,
                                    volumeWeighting, ivTrendType, barList, k, IVDiscountAdjustment, IVAdjustment, rangeAdjustment,
                                    volumeAdjustment, volAdjustment, wmaAdjustment));
                        }
                    }
                    for (int k = 0; k < barList.size(); k++) {
                        int movingTradeLength = barList.get(k).getMovingTrendLength();
                        if (k + movingTradeLength + 1 < barList.size()) {
                            double rangeAdjustment = calculateFirstDerivativeChangeInVariables(barList, k, movingTradeLength);
                            double volumeAdjustment = calculateFirstDerivativeChangeInVolume(barList,k,movingTradeLength);
                            double volAdjustment = calculateFirstDerivativeChangeInVolume(barList,k,movingTradeLength);
                            double wmaAdjustment = calculateFirstDerivativeChangeInWeightedMovingAverage(barList,k,movingTradeLength);
                            double IVDiscountAdjustment = calculateFirstDerivativeChangeInImpliedVolatilityDiscount(barList,k,movingTradeLength);
                            double IVAdjustment = calculateFirstDerivativeChangeInImpliedVolatility(barList,k,movingTradeLength);

                            barList.get(k).setTrade(applySeriesAdjustments(RVAdjustmentFlip, RVAdjustmentFlip, RVAdjustmentFlip,
                                    impliedVolFlip, ivWeighting, discountWeighting, realizedVolWeighting,
                                    volumeWeighting, ivTrendType, barList, k, IVDiscountAdjustment, IVAdjustment, rangeAdjustment,
                                    volumeAdjustment, volAdjustment, wmaAdjustment));
                        }
                    }
            return barList;
    }

    private double applySeriesAdjustments(boolean rangeAdjustmentFlip, boolean RVAdjustmentFlip, boolean volumeADjustmentFlip, boolean impliedVolFlip, double ivWeighting, double discountWeighting, double realizedVolWeighting, double volumeWeighting, int ivTrendType, List<Bar> barList, int k, double IVDiscountAdjustment, double IVAdjustment, double rangeAdjustment, double volumeAdjustment, double volAdjustment, double wmaAdjustment) {
        double trend = barList.get(k + 1).getWma();
        if (!rangeAdjustmentFlip) {
            if (rangeAdjustment > 0) {
                trend = trend * (1 - rangeAdjustment);
            } else {
                trend = trend * (1 + rangeAdjustment);
            }
        } else {
            if (rangeAdjustment > 0) {
                trend = trend * (1 + rangeAdjustment);
            } else {
                trend = trend * (1 - rangeAdjustment);
            }
        }
        IVAdjustment = IVAdjustment * (ivWeighting);
        volAdjustment = volAdjustment * (realizedVolWeighting);
        IVDiscountAdjustment = IVDiscountAdjustment * (discountWeighting);
        volumeAdjustment = volumeAdjustment * (volumeWeighting);

        if (impliedVolFlip) {
            if (ivTrendType == 1) {
                if (IVAdjustment > 0) {
                    trend = trend * (1 - IVAdjustment);
                } else {
                    trend = trend * (1 + IVAdjustment);
                }
            } else if (ivTrendType == 2) {
                trend = trend * (1 - IVAdjustment);
            }
        } else {
            if (ivTrendType == 1) {
                if (IVAdjustment > 0) {
                    trend = trend * (1 + IVAdjustment);
                } else {
                    trend = trend * (1 - IVAdjustment);
                }
            } else if (ivTrendType == 2) {
                trend = trend * (1 + IVAdjustment);
            }
        }
        if (impliedVolFlip) {
            if (ivTrendType == 1) {
                if (IVDiscountAdjustment > 0) {
                    trend = trend * (1 - IVDiscountAdjustment);
                } else {
                    trend = trend * (1 + IVDiscountAdjustment);
                }
            } else if (ivTrendType == 2) {
                trend = trend * (1 - IVDiscountAdjustment);
            }
        } else {
            if (ivTrendType == 1) {
                if (IVDiscountAdjustment > 0) {
                    trend = trend * (1 + IVDiscountAdjustment);
                } else {
                    trend = trend * (1 - IVDiscountAdjustment);
                }
            } else if (ivTrendType == 2) {
                trend = trend * (1 + IVDiscountAdjustment);
            }
        }

        if (RVAdjustmentFlip) {
            trend = trend * (1 - volAdjustment);
        } else {
            trend = trend * (1 + volAdjustment);
        }
        if (volumeADjustmentFlip) {
            trend = trend * (1 - volumeAdjustment);
        } else {
            trend = trend * (1 + volumeAdjustment);
        }
        trend = trend * (1 + wmaAdjustment);
        return trend;
    }

    private double calculateFirstDerivativeChangeInVariables(List<Bar> barList, int k, int movingLookbackLength) {
        double rangeMid = (barList.get(k).getBb_top() + barList.get(k).getBb_bottom()) / 2;
        double prevSum = 0.0;
        double last = 0.0;
        for (int j = 1; j < movingLookbackLength + 1; j++) {
            prevSum = prevSum + ((barList.get(j + k).getBb_bottom() + barList.get(j + k).getBb_top()) / 2);
            last = (barList.get(j + k).getBb_bottom() + barList.get(j + k).getBb_top()) / 2;
        }
        double prevAvg = prevSum / (movingLookbackLength + 1);
        double newAvg = (prevSum - last + rangeMid) / (movingLookbackLength + 1);
        double rangeadjustment = 0.0;
        double abs1 = Math.abs((newAvg - prevAvg) / prevAvg);
        if (abs1 > 0) {
            rangeadjustment = abs1 * -1;
        } else {
            rangeadjustment = abs1;
        }
        return rangeadjustment;
    }


    private double calculateFirstDerivativeChangeInVolume(List<Bar> barList, int k, int movingLookBackLength){
        double volume = barList.get(k).getVolume();
        double prevVolumeSum = 0.0;
        double lastVolume = 0.0;
        for (int j = 1; j < movingLookBackLength + 1; j++) {
            prevVolumeSum = prevVolumeSum + ((barList.get(j + k).getVolume()));
            lastVolume = ((barList.get(j + k).getVolume()));
        }
        double prevVolumeAvg = prevVolumeSum / (movingLookBackLength + 1);
        double newVolumeAvg = (prevVolumeSum - lastVolume + volume) / (movingLookBackLength + 1);
        double volumeAdjustment = 0.0;
        double abs2 = Math.abs((newVolumeAvg - prevVolumeAvg) / prevVolumeAvg);
        if ((newVolumeAvg - prevVolumeAvg) / prevVolumeAvg < 0) {
            volumeAdjustment = abs2 * -1;
        } else {
            volumeAdjustment = abs2;
        }
        return volumeAdjustment;
    }
    private double calculateFirstDerivativeChangeInRealizedVolatility(List<Bar> barList, int k, int movingLookBackLength){
        double realizedVol = barList.get(k).getTrendVol();
        double prevVolSum = 0.0;
        double lastVol = 0.0;
        for (int j = 1; j < movingLookBackLength + 1; j++) {
            prevVolSum = prevVolSum + ((barList.get(j + k).getTrendVol()));
            lastVol = ((barList.get(j + k).getTrendVol()));
        }
        double prevVolAvg = prevVolSum / (movingLookBackLength + 1);
        double newVolAvg = (prevVolSum - lastVol + realizedVol) / (movingLookBackLength + 1);
        double volAdjustment = 0.0;
        double abs = Math.abs((newVolAvg - prevVolAvg) / prevVolAvg);
        if ((newVolAvg - prevVolAvg) / prevVolAvg < 0) {
            volAdjustment = abs * -1;
        } else {
            volAdjustment = abs;
        }
        return volAdjustment;
    }
    private double calculateFirstDerivativeChangeInWeightedMovingAverage(List<Bar> barList, int k, int movingLookBackLength){
        double wma = barList.get(k).getWma();
        double prevWMAsum = 0.0;
        double lastWMA = 0.0;
        for (int j = 1; j < movingLookBackLength + 1; j++) {
            prevWMAsum = prevWMAsum + ((barList.get(j + k).getWma()));
            lastWMA = ((barList.get(j + k).getWma()));
        }
        double prevWMAavg = prevWMAsum / (movingLookBackLength + 1);
        double newWMAavg = (prevWMAsum - lastWMA + wma) / (movingLookBackLength + 1);
        double wmaAdjustment = 0.0;
        double abs3 = Math.abs((newWMAavg - prevWMAavg) / prevWMAavg);

        if ((newWMAavg - prevWMAavg) / prevWMAavg < 0) {
            wmaAdjustment = abs3 * -1;
        } else {
            wmaAdjustment = abs3;
        }
        return wmaAdjustment;
    }
    private double calculateFirstDerivativeChangeInImpliedVolatilityDiscount(List<Bar> barList, int k, int movingLookBackLength){
        double IVDiscountAdjustment = 0.0;
        double putIVDiscount = barList.get(k).getIvDiscount();
        double prevIVDiscountSum = 0.0;
        double lastIVDiscount = 0.0;
        for (int j = 1; j < movingLookBackLength + 1; j++) {
            prevIVDiscountSum = prevIVDiscountSum + barList.get(j+k).getIvDiscount();
            lastIVDiscount = barList.get(j+k).getIvDiscount();
        }
        double prevIVDiscountAvg = prevIVDiscountSum / (movingLookBackLength + 1);
        double newIVDiscountAvg = (prevIVDiscountSum - lastIVDiscount + putIVDiscount) / (movingLookBackLength + 1);
        double abs4 = Math.abs((newIVDiscountAvg - prevIVDiscountAvg) / prevIVDiscountAvg);
        if ((newIVDiscountAvg - prevIVDiscountAvg) / prevIVDiscountAvg < 0) {
            IVDiscountAdjustment = abs4 * -1;
        } else {
            IVDiscountAdjustment = abs4;
        }
        return IVDiscountAdjustment;
    }
    private double calculateFirstDerivativeChangeInImpliedVolatility(List<Bar> barList, int k, int movingLookBackLength){
        double IVAdjustment = 0.0;
        double putIV = barList.get(k).getPut_implied_vol();
        double prevIVSum = 0.0;
        double lastIV = 0.0;
        for (int j = 1; j < movingLookBackLength + 1; j++) {
            prevIVSum = prevIVSum + ((barList.get(j + k).getPut_implied_vol()));
            lastIV = ((barList.get(j + k).getPut_implied_vol()));
        }
        double prevIVAvg = prevIVSum / (movingLookBackLength + 1);
        double newIVAvg = (prevIVSum - lastIV + putIV) / (movingLookBackLength + 1);
        double abs4 = Math.abs((newIVAvg - prevIVAvg) / prevIVAvg);
        if ((newIVAvg - prevIVAvg) / prevIVAvg < 0) {
            IVAdjustment = abs4 * -1;
        } else {
            IVAdjustment = abs4;
        }
        return IVAdjustment;
    }

    public Bar buildBarFromJsonData(Object object){
        JSONObject resultObject = (JSONObject) object;
        Bar bar = new Bar();
        bar.setClose(resultObject.getDouble("c"));
        bar.setOpen(resultObject.getDouble("o"));
        bar.setHigh(resultObject.getDouble("h"));
        bar.setLow(resultObject.getDouble("l"));
        bar.setVolume(resultObject.getDouble("v"));
        bar.setDate(new Date(resultObject.getLong("t") + (60000 * 1440)));
        bar.setSplitAdjustFactor(1);
        return bar;
    }


    public String buildAPIExecutionString(String ticker, LocalDate pastBoundary, LocalDate today, int dataPoints) {
        String url3 = baseURL + ticker + "/range/1/day/" + getFormattedDateString(pastBoundary) +
                "/" + getFormattedDateString(today) +"?adjusted=true&sort=asc&limit=" + dataPoints +
                 "&apiKey=" + apiKey;
        return url3;
    }
    public void adjustSeriesForStockSplits(JSONArray jsonArray, List<Bar> barListSafe) throws Exception{
        for (Object object : jsonArray) {
            if (!jsonArray.isEmpty()) {
                JSONObject resultObject = (JSONObject) object;
                System.out.println(resultObject);
                String dateString = resultObject.getString("execution_date");
                Date date = sdf.parse(dateString);
                double multiple = (double) resultObject.getInt("split_to") / resultObject.getInt("split_from");
                for (Bar bar : barListSafe) {
                    if (bar.getDate() == date || bar.getDate().before(date)) {
                        bar.setSplitAdjustFactor(bar.getSplitAdjustFactor() * multiple);
                    }
                }
            }
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
