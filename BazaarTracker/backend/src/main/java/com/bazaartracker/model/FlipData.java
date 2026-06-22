package com.bazaartracker.model;

public class FlipData {

    private String itemId;
    private double buyPrice;
    private double sellPrice;
    private double profit;
    private double profitPercent;
    private double sellsPerHour;
    private double profitPerHour;

    public FlipData(
            String itemId,
            double buyPrice,
            double sellPrice,
            double profit,
            double profitPercent
    ) {
        this(
                itemId,
                buyPrice,
                sellPrice,
                profit,
                profitPercent,
                0,
                0
        );
    }

    public FlipData(
            String itemId,
            double buyPrice,
            double sellPrice,
            double profit,
            double profitPercent,
            double sellsPerHour,
            double profitPerHour
    ) {
        this.itemId = itemId;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.profit = profit;
        this.profitPercent = profitPercent;
        this.sellsPerHour = sellsPerHour;
        this.profitPerHour = profitPerHour;
    }

    public String getItemId() {
        return itemId;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getProfit() {
        return profit;
    }

    public double getProfitPercent() {
        return profitPercent;
    }

    public double getSellsPerHour() {
        return sellsPerHour;
    }

    public double getProfitPerHour() {
        return profitPerHour;
    }
}
