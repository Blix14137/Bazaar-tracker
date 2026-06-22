package com.bazaartracker.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bazaartracker.model.MayorData;
import com.bazaartracker.model.MayorData.MinisterData;
import com.bazaartracker.model.MayorData.PerkData;
import com.bazaartracker.model.MayorData.SpecialMayorData;

public class MayorService {

    private static final URI ELECTION_URI =
            URI.create(
                    "https://api.hypixel.net/v2/resources/skyblock/election"
            );

    private static final Duration CACHE_DURATION =
            Duration.ofMinutes(10);

    private final HttpClient client =
            HttpClient.newHttpClient();

    private MayorData cachedMayor;
    private Instant cacheTime =
            Instant.EPOCH;

    public synchronized MayorData getCurrentMayor() {

        if (
                cachedMayor != null &&
                Instant.now().isBefore(
                        cacheTime.plus(CACHE_DURATION)
                )
        ) {

            return cachedMayor;
        }

        try {

            HttpRequest request =
                    HttpRequest.newBuilder(ELECTION_URI)
                            .timeout(Duration.ofSeconds(10))
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                throw new IllegalStateException(
                        "Hypixel returned " +
                        response.statusCode()
                );
            }

            JSONObject root =
                    new JSONObject(response.body());

            JSONObject mayor =
                    root.getJSONObject("mayor");

            JSONArray perksJson =
                    mayor.getJSONArray("perks");

            List<PerkData> perks =
                    new ArrayList<>();

            for (int i = 0; i < perksJson.length(); i++) {

                JSONObject perk =
                        perksJson.getJSONObject(i);

                perks.add(
                        new PerkData(
                                perk.getString("name"),
                                perk.getString("description")
                        )
                );
            }

            int currentYear =
                    root.getJSONObject("current")
                            .getInt("year");

            MinisterData minister =
                    getMinister(mayor);

            cachedMayor =
                    new MayorData(
                            mayor.getString("name"),
                            List.copyOf(perks),
                            minister,
                            currentYear,
                            getSpecialMayorSchedule(currentYear),
                            root.getLong("lastUpdated")
                    );

            cacheTime =
                    Instant.now();

            return cachedMayor;

        } catch (Exception e) {

            if (cachedMayor != null) {

                return cachedMayor;
            }

            throw new IllegalStateException(
                    "Failed to load the current mayor",
                    e
            );
        }
    }

    private MinisterData getMinister(JSONObject mayor) {

        if (isSpecialMayor(mayor.getString("name"))) {

            return null;
        }

        JSONObject election =
                mayor.optJSONObject("election");

        if (election == null) {

            return null;
        }

        JSONArray candidates =
                election.optJSONArray("candidates");

        if (candidates == null || candidates.length() < 2) {

            return null;
        }

        List<JSONObject> rankedCandidates =
                new ArrayList<>();

        for (int i = 0; i < candidates.length(); i++) {

            rankedCandidates.add(
                    candidates.getJSONObject(i)
            );
        }

        rankedCandidates.sort(
                (left, right) ->
                        Integer.compare(
                                right.optInt("votes"),
                                left.optInt("votes")
                        )
        );

        JSONObject minister =
                rankedCandidates.get(1);

        JSONArray perks =
                minister.optJSONArray("perks");

        if (perks == null) {

            return null;
        }

        for (int i = 0; i < perks.length(); i++) {

            JSONObject perk =
                    perks.getJSONObject(i);

            if (perk.optBoolean("minister")) {

                return new MinisterData(
                        minister.getString("name"),
                        new PerkData(
                                perk.getString("name"),
                                perk.getString("description")
                        )
                );
            }
        }

        return null;
    }

    private List<SpecialMayorData> getSpecialMayorSchedule(
            int currentYear
    ) {

        return List.of(
                createSpecialMayor("Scorpius", 504, currentYear),
                createSpecialMayor("Derpy", 512, currentYear),
                createSpecialMayor("Jerry", 520, currentYear)
        );
    }

    private SpecialMayorData createSpecialMayor(
            String name,
            int anchorYear,
            int currentYear
    ) {

        int nextYear =
                anchorYear;

        while (nextYear < currentYear) {

            nextYear += 24;
        }

        int estimatedDays =
                Math.max(
                        0,
                        (int) Math.round(
                                (nextYear - currentYear) *
                                124.0 / 24.0
                        )
                );

        return new SpecialMayorData(
                name,
                nextYear,
                estimatedDays
        );
    }

    private boolean isSpecialMayor(String name) {

        return name.equals("Jerry") ||
               name.equals("Derpy") ||
               name.equals("Scorpius");
    }
}
