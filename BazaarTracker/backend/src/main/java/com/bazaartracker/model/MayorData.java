package com.bazaartracker.model;

import java.util.List;

public record MayorData(
        String name,
        List<PerkData> perks,
        MinisterData minister,
        int currentYear,
        List<SpecialMayorData> specialMayors,
        long lastUpdated
) {

    public record PerkData(
            String name,
            String description
    ) {
    }

    public record MinisterData(
            String name,
            PerkData perk
    ) {
    }

    public record SpecialMayorData(
            String name,
            int nextYear,
            int estimatedDays
    ) {
    }
}
