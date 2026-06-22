package com.bazaartracker.model;

import java.util.List;

public record CraftRecipeData(
        String outputItemId,
        int outputCount,
        List<CraftIngredientData> ingredients,
        double craftCost,
        double sellPrice,
        double profit
) {
}
