package com.bazaartracker.model;

public class PriceData {

    private String timestamp;
    private double buyPrice;
    private double sellPrice;

    public PriceData(
            String timestamp,
            double buyPrice,
            double sellPrice
    ) {
        this.timestamp = timestamp;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }
}