package com.bazaartracker.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bazaartracker.model.PriceData;
import com.bazaartracker.service.DatabaseService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class HistoryController {

    private final DatabaseService database =
            new DatabaseService();

    @GetMapping("/history/{itemId}")
    public List<PriceData> getHistory(
            @PathVariable String itemId
    ) {

        return database.getHistory(itemId);
    }
}