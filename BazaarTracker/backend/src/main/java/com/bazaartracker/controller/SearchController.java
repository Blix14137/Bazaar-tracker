package com.bazaartracker.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bazaartracker.model.CraftRecipeData;
import com.bazaartracker.model.FlipData;
import com.bazaartracker.service.DatabaseService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SearchController {

    private final DatabaseService database =
            new DatabaseService();

    @GetMapping("/items")
    public List<String> getItems() {

        return database.getAllItems();
    }

    @GetMapping("/search")
    public List<String> searchItems(
            @RequestParam String query
    ) {

        return database.searchItems(query);
    }

    @GetMapping("/flips")
    public List<FlipData> getCraftFlips() {

        return database.getCraftFlips();
    }

    @GetMapping("/bazaar-flips")
    public List<FlipData> getBazaarFlips() {

        return database.getBazaarFlips();
    }

    @GetMapping("/craft/{itemId}")
    public CraftRecipeData getCraftRecipe(
            @PathVariable String itemId
    ) {

        return database.getCraftRecipe(itemId);
    }

    @GetMapping("/search/{itemId}")
    public String searchItem(
            @PathVariable String itemId
    ) {

        return database.searchItem(itemId);
    }
}
