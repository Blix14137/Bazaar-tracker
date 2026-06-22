package com.bazaartracker.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bazaartracker.model.CraftIngredientData;
import com.bazaartracker.model.CraftRecipeData;
import com.bazaartracker.model.FlipData;
import com.bazaartracker.model.PriceData;

public class DatabaseService {

    private static final String DB_URL =
            "jdbc:sqlite:data/bazaar.db";

    private static final List<CraftingRecipe> CRAFTING_RECIPES =
            loadCraftingRecipes();

    public DatabaseService() {

        createTable();
    }

    private void createTable() {

        try (
            Connection conn =
                    DriverManager.getConnection(DB_URL);

            Statement stmt =
                    conn.createStatement()
        ) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bazaar_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id TEXT,
                    buy_price REAL,
                    sell_price REAL,
                    buy_volume REAL,
                    sell_volume REAL,
                    buy_moving_week REAL DEFAULT 0,
                    sell_moving_week REAL DEFAULT 0,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            addColumnIfMissing(
                    conn,
                    "buy_moving_week",
                    "REAL DEFAULT 0"
            );

            addColumnIfMissing(
                    conn,
                    "sell_moving_week",
                    "REAL DEFAULT 0"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void addColumnIfMissing(
            Connection conn,
            String columnName,
            String definition
    ) {

        try (
            Statement stmt =
                    conn.createStatement()
        ) {

            stmt.execute(
                    "ALTER TABLE bazaar_history ADD COLUMN " +
                    columnName + " " + definition
            );

        } catch (Exception ignored) {

            // The column already exists.
        }
    }

    public void saveItem(
            String itemId,
            double buyPrice,
            double sellPrice,
            double buyVolume,
            double sellVolume,
            double buyMovingWeek,
            double sellMovingWeek
    ) {

        try (
            Connection conn =
                    DriverManager.getConnection(DB_URL)
        ) {

            String sql = """
                INSERT INTO bazaar_history
                (
                    item_id,
                    buy_price,
                    sell_price,
                    buy_volume,
                    sell_volume,
                    buy_moving_week,
                    sell_moving_week
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setString(1, itemId);
            ps.setDouble(2, buyPrice);
            ps.setDouble(3, sellPrice);
            ps.setDouble(4, buyVolume);
            ps.setDouble(5, sellVolume);
            ps.setDouble(6, buyMovingWeek);
            ps.setDouble(7, sellMovingWeek);

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public String searchItem(String itemId) {

        StringBuilder result =
                new StringBuilder();

        try (
            Connection conn =
                    DriverManager.getConnection(DB_URL);

            PreparedStatement ps =
                    conn.prepareStatement(
                            """
                            SELECT *
                            FROM bazaar_history
                            WHERE item_id = ?
                            ORDER BY timestamp DESC
                            LIMIT 1
                            """
                    )
        ) {

            ps.setString(1, itemId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                result.append("Item: ");
                result.append(rs.getString("item_id"));

                result.append(" | Buy: ");
                result.append(rs.getDouble("buy_price"));

                result.append(" | Sell: ");
                result.append(rs.getDouble("sell_price"));

                result.append(" | Buy Volume: ");
                result.append(rs.getDouble("buy_volume"));

                result.append(" | Sell Volume: ");
                result.append(rs.getDouble("sell_volume"));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return result.toString();
    }

   public List<PriceData> getHistory(String itemId) {

    List<PriceData> history =
            new ArrayList<>();

    try (
        Connection conn =
                DriverManager.getConnection(DB_URL);

        PreparedStatement ps =
                conn.prepareStatement(
                        """
                        SELECT *
                        FROM bazaar_history
                        WHERE item_id = ?
                        ORDER BY timestamp
                        """
                )
    ) {

        ps.setString(1, itemId);

        ResultSet rs =
                ps.executeQuery();

        while (rs.next()) {

            history.add(
                    new PriceData(
                            rs.getString("timestamp"),
                            rs.getDouble("buy_price"),
                            rs.getDouble("sell_price")
                    )
            );
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return history;
}
    
    
public List<String> getAllItems() {

    List<String> items =
            new ArrayList<>();

    try (
        Connection conn =
                DriverManager.getConnection(DB_URL);

        Statement stmt =
                conn.createStatement()
    ) {

        ResultSet rs =
                stmt.executeQuery(
                        """
                        SELECT DISTINCT item_id
                        FROM bazaar_history
                        ORDER BY item_id
                        """
                );

        while (rs.next()) {

            items.add(
                    rs.getString("item_id")
            );
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return items;
}
public List<String> searchItems(String query) {

    List<String> results = new ArrayList<>();
    String normalizedQuery =
            query == null
                    ? ""
                    : query.trim().toLowerCase();

    if (normalizedQuery.isEmpty()) {

        return results;
    }

    try (
        Connection conn =
                DriverManager.getConnection(DB_URL);

        PreparedStatement ps =
                conn.prepareStatement(
                        """
                        SELECT DISTINCT item_id
                        FROM bazaar_history
                        WHERE LOWER(item_id) LIKE ?
                           OR LOWER(REPLACE(item_id, '_', ' ')) LIKE ?
                        ORDER BY item_id
                        LIMIT 10
                        """
                )
    ) {

        ps.setString(
                1,
                "%" + normalizedQuery.replace(" ", "_") + "%"
        );

        ps.setString(
                2,
                "%" + normalizedQuery.replace("_", " ") + "%"
        );

        ResultSet rs =
                ps.executeQuery();

        while (rs.next()) {

            results.add(
                    rs.getString("item_id")
            );
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return results;
}

public List<FlipData> getCraftFlips() {

    Map<String, FlipData> bestFlipByItem =
            new HashMap<>();

    Map<String, double[]> latestPrices =
            getLatestPrices();

    for (CraftingRecipe recipe : CRAFTING_RECIPES) {

        addCraftFlip(
                bestFlipByItem,
                latestPrices,
                recipe
        );
    }

    List<FlipData> flips =
            new ArrayList<>(bestFlipByItem.values());

    flips.sort(
            Comparator.comparingDouble(FlipData::getProfit)
                    .reversed()
    );

    return flips;
}

public List<FlipData> getBazaarFlips() {

    List<FlipData> flips =
            new ArrayList<>();

    Map<String, double[]> latestPrices =
            getLatestPrices();

    for (Map.Entry<String, double[]> entry :
            latestPrices.entrySet()) {

        double buyOrderPrice =
                entry.getValue()[1];

        double sellOrderPrice =
                entry.getValue()[0];

        if (
                buyOrderPrice <= 0 ||
                sellOrderPrice <= buyOrderPrice
        ) {

            continue;
        }

        double profit =
                sellOrderPrice - buyOrderPrice;

        double sellsPerHour =
                Math.min(
                        entry.getValue()[2],
                        entry.getValue()[3]
                ) / 168.0;

        double netProfit =
                profit - sellOrderPrice * 0.0125;

        double profitPerHour =
                Math.max(0, netProfit) *
                sellsPerHour;

        if (netProfit <= 0 || sellsPerHour <= 0) {

            continue;
        }

        flips.add(
                new FlipData(
                        entry.getKey(),
                        buyOrderPrice,
                        sellOrderPrice,
                        profit,
                        profit / buyOrderPrice * 100,
                        sellsPerHour,
                        profitPerHour
                )
        );
    }

    flips.sort(
            Comparator.comparingDouble(FlipData::getProfitPerHour)
                    .reversed()
    );

    return flips;
}

public CraftRecipeData getCraftRecipe(String itemId) {

    Map<String, double[]> latestPrices =
            getLatestPrices();

    CraftRecipeData bestRecipe = null;

    for (CraftingRecipe recipe : CRAFTING_RECIPES) {

        if (!recipe.outputItemId().equals(itemId)) {

            continue;
        }

        CraftRecipeData pricedRecipe =
                priceCraftRecipe(recipe, latestPrices);

        if (
                pricedRecipe != null &&
                (
                    bestRecipe == null ||
                    pricedRecipe.profit() > bestRecipe.profit()
                )
        ) {

            bestRecipe = pricedRecipe;
        }
    }

    return bestRecipe;
}

private Map<String, double[]> getLatestPrices() {

    Map<String, double[]> prices =
            new HashMap<>();

    try (
        Connection conn =
                DriverManager.getConnection(DB_URL);

        PreparedStatement ps =
                conn.prepareStatement(
                        """
                        SELECT
                            latest.item_id,
                            latest.buy_price,
                            latest.sell_price,
                            latest.buy_moving_week,
                            latest.sell_moving_week
                        FROM bazaar_history latest
                        INNER JOIN (
                            SELECT item_id, MAX(id) AS latest_id
                            FROM bazaar_history
                            GROUP BY item_id
                        ) newest
                            ON latest.id = newest.latest_id
                        """
                )
    ) {

        ResultSet rs =
                ps.executeQuery();

        while (rs.next()) {

            prices.put(
                    rs.getString("item_id"),
                    new double[] {
                            rs.getDouble("buy_price"),
                            rs.getDouble("sell_price"),
                            rs.getDouble("buy_moving_week"),
                            rs.getDouble("sell_moving_week")
                    }
            );
        }

    } catch (Exception e) {

        e.printStackTrace();
    }

    return prices;
}

private void addCraftFlip(
        Map<String, FlipData> bestFlipByItem,
        Map<String, double[]> latestPrices,
        CraftingRecipe recipe
) {

    CraftRecipeData pricedRecipe =
            priceCraftRecipe(recipe, latestPrices);

    if (
            pricedRecipe == null ||
            pricedRecipe.profit() <= 0
    ) {

        return;
    }

    FlipData flip =
            new FlipData(
                    recipe.outputItemId(),
                    pricedRecipe.craftCost(),
                    pricedRecipe.sellPrice(),
                    pricedRecipe.profit(),
                    pricedRecipe.profit() /
                            pricedRecipe.craftCost() * 100
            );

    bestFlipByItem.merge(
            recipe.outputItemId(),
            flip,
            (current, candidate) ->
                    candidate.getProfit() > current.getProfit()
                            ? candidate
                            : current
    );
}

private CraftRecipeData priceCraftRecipe(
        CraftingRecipe recipe,
        Map<String, double[]> latestPrices
) {

    double[] outputPrices =
            latestPrices.get(recipe.outputItemId());

    if (outputPrices == null) {

        return null;
    }

    List<CraftIngredientData> ingredients =
            new ArrayList<>();

    double craftCost = 0;

    List<Map.Entry<String, Integer>> recipeIngredients =
            new ArrayList<>(recipe.ingredients().entrySet());

    recipeIngredients.sort(Map.Entry.comparingByKey());

    for (Map.Entry<String, Integer> ingredient :
            recipeIngredients) {

        double[] ingredientPrices =
                latestPrices.get(ingredient.getKey());

        if (ingredientPrices == null) {

            return null;
        }

        double unitPrice =
                ingredientPrices[1];

        double totalPrice =
                unitPrice * ingredient.getValue();

        craftCost += totalPrice;

        ingredients.add(
                new CraftIngredientData(
                        ingredient.getKey(),
                        ingredient.getValue(),
                        unitPrice,
                        totalPrice
                )
        );
    }

    double sellPrice =
            outputPrices[0] *
            recipe.outputCount();

    if (craftCost <= 0 || sellPrice <= 0) {

        return null;
    }

    return new CraftRecipeData(
            recipe.outputItemId(),
            recipe.outputCount(),
            ingredients,
            craftCost,
            sellPrice,
            sellPrice - craftCost
    );
}

private static List<CraftingRecipe> loadCraftingRecipes() {

    try (
        InputStream input =
                DatabaseService.class.getResourceAsStream(
                        "/crafting-recipes.json"
                )
    ) {

        if (input == null) {

            throw new IllegalStateException(
                    "Missing crafting-recipes.json"
            );
        }

        String json =
                new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );

        JSONArray recipesJson =
                new JSONArray(json);

        List<CraftingRecipe> recipes =
                new ArrayList<>();

        for (int i = 0; i < recipesJson.length(); i++) {

            JSONObject recipeJson =
                    recipesJson.getJSONObject(i);

            JSONObject ingredientsJson =
                    recipeJson.getJSONObject("ingredients");

            Map<String, Integer> ingredients =
                    new HashMap<>();

            for (String itemId : ingredientsJson.keySet()) {

                ingredients.put(
                        itemId,
                        ingredientsJson.getInt(itemId)
                );
            }

            recipes.add(
                    new CraftingRecipe(
                            recipeJson.getString("output"),
                            recipeJson.getInt("outputCount"),
                            ingredients
                    )
            );
        }

        return List.copyOf(recipes);

    } catch (Exception e) {

        throw new IllegalStateException(
                "Failed to load crafting recipes",
                e
        );
    }
}

private record CraftingRecipe(
        String outputItemId,
        int outputCount,
        Map<String, Integer> ingredients
) {
}


}
