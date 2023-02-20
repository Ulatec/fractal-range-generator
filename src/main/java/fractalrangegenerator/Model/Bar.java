package com.example.fractalrangecalculator.Model;

import java.util.Date;

public class Bar {

    private Date date;

    private double low;

    private double high;

    private double close;

    private double open;

    private double wma;

    private double slope;

    private double minDiff;

    private double maxDiff;

    private double stdDev;

    private double averageTrueRange;

    private double hurst;

    private double bb_bottom;

    private double bb_top;

    private double rma;

    private double trendVol;

    private double tradeVol;
    private double trend;

    private double volume;

    private double trendWMA;

    private double trade;

    private double put_implied_vol;

    private int movingTrendLength;

    private int movingTradeLength;

    private int trendVolCalcDays;

    private double splitAdjustFactor;

    private double ivDiscount;

    private double tradeIvDiscount;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    @Override
    public String toString() {
        return "Bar{" +
                "date=" + date +
                ", low=" + low +
                ", high=" + high +
                ", close=" + close +
                ", open=" + open +
                ", wma=" + wma +
                ", slope=" + slope +
                ", minDiff=" + minDiff +
                ", maxDiff=" + maxDiff +
                ", stdDev=" + stdDev +
                ", averageTrueRange=" + averageTrueRange +
                ", hurst=" + hurst +
                ", bb_bottom=" + bb_bottom +
                ", bb_top=" + bb_top +
                ", rma=" + rma +
                ", trendVol=" + trendVol +
                ", tradeVol=" + tradeVol +
                ", trend=" + trend +
                ", volume=" + volume +
                ", trendWMA=" + trendWMA +
                ", trade=" + trade +
                ", put_implied_vol=" + put_implied_vol +
                ", movingTrendLength=" + movingTrendLength +
                ", movingTradeLength=" + movingTradeLength +
                ", trendVolCalcDays=" + trendVolCalcDays +
                ", splitAdjustFactor=" + splitAdjustFactor +
                ", ivDiscount=" + ivDiscount +
                ", tradeIvDiscount=" + tradeIvDiscount +
                '}';
    }

    public double getWma() {
        return wma;
    }

    public void setWma(double wma) {
        this.wma = wma;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public double getMinDiff() {
        return minDiff;
    }

    public void setMinDiff(double minDiff) {
        this.minDiff = minDiff;
    }

    public double getMaxDiff() {
        return maxDiff;
    }

    public void setMaxDiff(double maxDiff) {
        this.maxDiff = maxDiff;
    }

    public double getStdDev() {
        return stdDev;
    }

    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    public double getAverageTrueRange() {
        return averageTrueRange;
    }

    public void setAverageTrueRange(double averageTrueRange) {
        this.averageTrueRange = averageTrueRange;
    }

    public double getHurst() {
        return hurst;
    }

    public void setHurst(double hurst) {
        this.hurst = hurst;
    }

    public double getBb_bottom() {
        return bb_bottom;
    }

    public void setBb_bottom(double bb_bottom) {
        this.bb_bottom = bb_bottom;
    }

    public double getBb_top() {
        return bb_top;
    }

    public void setBb_top(double bb_top) {
        this.bb_top = bb_top;
    }

    public double getRma() {
        return rma;
    }

    public void setRma(double rma) {
        this.rma = rma;
    }

    public double getTrendVol() {
        return trendVol;
    }

    public void setTrendVol(double realVol) {
        this.trendVol = realVol;
    }

    public double getTrend() {
        return trend;
    }

    public void setTrend(double trend) {
        this.trend = trend;
    }


    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getTrendWMA() {
        return trendWMA;
    }

    public void setTrendWMA(double trendWMA) {
        this.trendWMA = trendWMA;
    }

    public double getTrade() {
        return trade;
    }

    public void setTrade(double trade) {
        this.trade = trade;
    }

    public double getTradeVol() {
        return tradeVol;
    }

    public void setTradeVol(double tradeVol) {
        this.tradeVol = tradeVol;
    }

    public double getPut_implied_vol() {
        return put_implied_vol;
    }

    public void setPut_implied_vol(double put_implied_vol) {
        this.put_implied_vol = put_implied_vol;
    }

    public int getMovingTrendLength() {
        return movingTrendLength;
    }

    public void setMovingTrendLength(int movingTrendLength) {
        this.movingTrendLength = movingTrendLength;
    }

    public int getMovingTradeLength() {
        return movingTradeLength;
    }

    public void setMovingTradeLength(int movingTradeLength) {
        this.movingTradeLength = movingTradeLength;
    }

    public int getTrendVolCalcDays() {
        return trendVolCalcDays;
    }

    public void setTrendVolCalcDays(int trendVolCalcDays) {
        this.trendVolCalcDays = trendVolCalcDays;
    }

    public double getSplitAdjustFactor() {
        return splitAdjustFactor;
    }

    public void setSplitAdjustFactor(double splitAdjustFactor) {
        this.splitAdjustFactor = splitAdjustFactor;
    }

    public double getIvDiscount() {
        return ivDiscount;
    }

    public void setIvDiscount(double ivDiscount) {
        this.ivDiscount = ivDiscount;
    }

    public double getTradeIvDiscount() {
        return tradeIvDiscount;
    }

    public void setTradeIvDiscount(double tradeIvDiscount) {
        this.tradeIvDiscount = tradeIvDiscount;
    }
}
