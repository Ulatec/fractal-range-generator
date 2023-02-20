package com.example.fractalrangecalculator.Libraries;

import com.example.fractalrangecalculator.Model.Bar;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class StockCalculationLibrary {

    public static double calculateStockBeta(){
        return 0.0;
    }

    public static double averageTrueRange(List<Bar> bars, int dayOffset, int length){
        //trueRange = na(security_high[1])? security_high-security_low : math.max(math.max(security_high - security_low, math.abs(security_high - security_close[1])), math.abs(security_low - security_close[1]))

        //trueRange = na(high[1])? high-low : math.max(math.max(high - low, math.abs(high - close[1])), math.abs(low - close[1]))
        if(dayOffset + length < bars.size()) {
            double max = -10000000.0;
            double min = 10000000.0;
            //dayOffset = dayOffset + 1;
            double rangeSum = 0.0;
            for(int i = dayOffset; i < dayOffset + length -1; i++){
                if(bars.get(i + 1).getHigh() > max){
                    max = bars.get(i + 1).getHigh();
                }
                if(bars.get(i).getLow() < min){
                    min = bars.get(i + 1).getLow();
                }
                double num1 = Math.max(bars.get(i + 1).getHigh() - bars.get(i + 1).getLow(), (Math.abs(bars.get(i + 1).getHigh() - bars.get(i + 2).getClose())));
                double num2 = Math.abs(bars.get(i + 1).getLow() - bars.get(i + 2).getClose());
                rangeSum = rangeSum + Math.max(num1,num2);
            }

            double atr = rangeSum/length;


            return atr;

        }else{
            return 0.0;
        }
    }
    public static double getRMA(List<Bar> bars, int dayOffset, int length){
        if(dayOffset + length < bars.size()) {
            if (bars.get(dayOffset + 1).getRma() == 0.0) {
                double sum = 0.0;
                for (int i = dayOffset; i < dayOffset + length; i++) {
                    sum = sum + bars.get(dayOffset).getAverageTrueRange();
                }
                return sum / length;
            } else {
                double alpha = 1 / (double)length;
                double sum = 0.0;
                sum = sum + (alpha * bars.get(dayOffset).getAverageTrueRange() + (1 - alpha) * bars.get(dayOffset + 1).getRma());
                return sum;
            }
        }else{
            return 0.0;
        }
    }

    public static double getLogVariance(List<Bar> barList, int dayOffset, int length){
        if(dayOffset + length < barList.size()) {
            List<Double> returnsBetweenBars = new ArrayList<>();
            for (int i = 0; i < length - 1; i++) {
                double a = barList.get(dayOffset + i).getClose();

                double b = barList.get(dayOffset + i + 1).getClose();

                double d = Math.log(a / b);


                returnsBetweenBars.add(d);

            }
            DoubleSummaryStatistics doubleSummaryStatistics = returnsBetweenBars.stream().mapToDouble(x -> x).summaryStatistics();
            double variance = 0;
            for (int i = 0; i < returnsBetweenBars.size(); i++) {
                variance += Math.pow(returnsBetweenBars.get(i) - doubleSummaryStatistics.getAverage(), 2);
            }
            variance /= returnsBetweenBars.size();
            //System.out.println("variance: " + (variance * (252/length)/12) + " RV: " + (Math.sqrt(variance) * Math.sqrt(365)));
            if(Double.isNaN(100* (Math.sqrt(variance) * Math.sqrt(365)))){
                //System.out.println("stop!");
            }
            return 100* (Math.sqrt(variance) * Math.sqrt(365));
        }
        return 0.0;
    }
    public static double min_diff(List<Bar> bars, int dayOffset, double slope, int lengthMinus1){
        if(dayOffset + (lengthMinus1 * 2) < bars.size()) {
            double m = 100000000;
            List<Bar> subList = bars.subList(dayOffset, dayOffset + lengthMinus1 + 1);
            for (int i = 0; i < (lengthMinus1); i++) {

                double a = subList.get((lengthMinus1) - i).getClose() - (subList.get(lengthMinus1).getClose() + (slope * i));
                m = Math.min(m, a);
            }
            return m;
        }else{
            return 0.0;
        }
    }

    public static double max_diff(List<Bar> bars, int dayOffset, double slope, int lengthMinus1){
        if(dayOffset + (lengthMinus1 * 2) < bars.size()) {
            double m = -100000000;
            List<Bar> subList = bars.subList(dayOffset, dayOffset + lengthMinus1 + 1);
            for (int i = 0; i < (lengthMinus1); i++) {
                double a = subList.get((lengthMinus1) - i).getClose() - (subList.get(lengthMinus1).getClose() + (slope * i));
                m = Math.max(m, a);
            }
            return m;
        }else{
            return 0.0;
        }
    }

    public static double weightedMovingAverage(List<Bar> bars, int dayOffset, int length){
        if(dayOffset + length < bars.size()) {
            double norm = 0.0;
            double sum = 0.0;
            for(int i = 0; i < length; i++){
                double weight = (length - i) * length;
                norm = norm + weight;
                sum = sum + bars.get(i + dayOffset).getClose() * weight;
            }
            return sum / norm;
        }else{
            return 0.0;
        }
    }

    public static double getSlope(List<Bar> bars, int dayOffset, int lengthMinus1){
        if(dayOffset + lengthMinus1 < bars.size()) {
            double slope = (bars.get(dayOffset).getClose() - bars.get(dayOffset + lengthMinus1).getClose()) / lengthMinus1;
            return slope;
        }else{
            return 0.0;
        }
    }

    public static double getHurst(List<Bar> bars, int dayOffset, int length){
        if(dayOffset + length < bars.size()) {
            double high = -1000000.0;
            double low = 1000000.0;
            for(int i = dayOffset; i < dayOffset + length; i++){
                if(bars.get(i).getHigh() > high){
                    high = bars.get(i).getHigh();
                }
                if(bars.get(i).getLow() < low){
                    low = bars.get(i).getLow();
                }
            }
            double a = Math.log(high - low);
            double b = Math.log(bars.get(dayOffset).getAverageTrueRange());

            return (a -  b )/ Math.log(length);
        }else{
            return 0.0;
        }
    }



    public static double getBridgeBottom(Bar bar){
        return (bar.getWma() - (bar.getStdDev() * 2)) + (((bar.getClose() + bar.getMinDiff()) - (bar.getWma() - (bar.getStdDev() * 2))) * Math.abs((bar.getHurst() * 2) -1));
    }
    public static double getBridgeTop(Bar bar){
        return (bar.getWma() + (bar.getStdDev() * 2)) - (((bar.getWma() + (bar.getStdDev() * 2)) - ((bar.getClose() + bar.getMaxDiff()))) * Math.abs((bar.getHurst() * 2) -1));
    }
    public static double stdDev(List<Bar> bars, int dayOffset, int length){
        if(dayOffset + length < bars.size()) {
            List<Double> prices = new ArrayList<>();

            for (int i = dayOffset; i < length + dayOffset; i++) {
                prices.add(bars.get(i).getClose());
            }
            DoubleSummaryStatistics doubleSummaryStatistics = prices.stream().mapToDouble(x -> x).summaryStatistics();
            double variance = 0;
            for (int i = 0; i < prices.size(); i++) {
                variance += Math.pow(prices.get(i) - doubleSummaryStatistics.getAverage(), 2);
            }
            variance /= prices.size();
            //System.out.println("Std Dev Per Game For " + player.getFirstName() + " " + player.getLastName() + ": " + Math.sqrt(variance));
            return Math.sqrt(variance);
        }else{
            return 0.0;
        }
    }
}
