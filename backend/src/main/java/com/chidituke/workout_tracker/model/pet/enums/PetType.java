package com.chidituke.workout_tracker.model.pet.enums;

/**
 * Enum for pet types available in EvoPet.
 * <p>
 * DESIGN PHILOSOPHY:
 * Start with just WOLF for MVP. Add new pet types as enum values when you're ready to implement them.
 * Each new pet type should have its own animations, colors, and evolution stages prepared before adding.
 * <p>
 * Future additions will be added like:
 * CAT("Cat", "Agile and independent companion"),
 * DRAGON("Dragon", "Mythical and powerful companion"),
 * etc.
 */
public enum PetType {
    WOLF("Wolf", "Loyal and strong companion that grows with your fitness journey");

    // ADD MORE PET TYPES HERE WHEN READY:
    // TIGER("Cat", "Agile and independent companion"),
    // RABBIT("Dragon", "Mythical and powerful companion"),
    // FOX("Fox", "Clever and swift companion"),
    // BEAR("Bear", "Powerful and protective companion"),
    // etc.

    private final String displayName;
    private final String description;

    PetType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * For future use: mark certain pets as "coming soon" in UI
     * For now, only WOLF exists so all existing pets are available
     */
    public boolean isAvailable() {
        return true; // All existing enum values are available
    }

    /**
     * Get all available pet types
     * Returns all enum values (for now just WOLF)
     */
    public static PetType[] getAvailableTypes() {
        return values(); // Returns all enum values
    }
}