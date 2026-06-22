package com.bazaartracker.model;

public record CraftIngredientData(
        String itemId,
        int amount,
        double unitPrice,
        double totalPrice
) {
}
