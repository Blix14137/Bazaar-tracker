package com.bazaartracker.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

public class BazaarCollector {

    private final DatabaseService database =
            new DatabaseService();

    public void start() {

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(
                this::collectData,
                0,
                5,
                TimeUnit.MINUTES
        );

        System.out.println("Bazaar Collector started. Collecting every 5 minutes...");
    }

    private void collectData() {

        try {

            HttpClient client =
                    HttpClient.newHttpClient();

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "https://api.hypixel.net/v2/skyblock/bazaar"))
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            JSONObject json =
                    new JSONObject(response.body());

            JSONObject products =
                    json.getJSONObject("products");

            int count = 0;

            for (String itemId : products.keySet()) {

                JSONObject item =
                        products.getJSONObject(itemId);

                JSONObject quickStatus =
                        item.getJSONObject("quick_status");

                database.saveItem(
                        itemId,
                        quickStatus.getDouble("buyPrice"),
                        quickStatus.getDouble("sellPrice"),
                        quickStatus.getDouble("buyVolume"),
                        quickStatus.getDouble("sellVolume"),
                        quickStatus.getDouble("buyMovingWeek"),
                        quickStatus.getDouble("sellMovingWeek")
                );

                count++;
            }

            System.out.println(
                    "Saved " + count +
                    " items at " +
                    java.time.LocalDateTime.now()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
