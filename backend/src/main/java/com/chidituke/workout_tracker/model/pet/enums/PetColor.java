package com.chidituke.workout_tracker.model.pet.enums;

/**
 * Pet color system for EvoPet.
 * <p>
 * DESIGN PHILOSOPHY:
 * Colors are currently specific to WOLF. When you add new pets (Cat, Dragon, etc.),
 * you have TWO options:
 * <p>
 * OPTION A: Keep this enum, add cat-specific colors as new enum values
 * Example: WOLF_GREY, WOLF_BROWN, CAT_ORANGE, CAT_TABBY, DRAGON_RED, etc.
 * <p>
 * OPTION B: Create separate color enums per pet type
 * Example: WolfColor enum, CatColor enum, DragonColor enum
 * (Requires more refactoring but cleaner separation)
 * <p>
 * For MVP, these are WOLF colors only. Decide on approach when adding second pet.
 */
public enum PetColor {
    // FREE WOLF COLORS (Available to all users)
    GREY("Classic Grey", "Classic grey wolf coloring", true, false),

    // ACHIEVEMENT-UNLOCKED WOLF COLORS
    BROWN("Earthy Brown", "Warm brown wolf - Unlock by completing 10 workouts", false, false),
    WHITE("Arctic White", "Pure white wolf - Unlock by maintaining a 7-day streak", false, false),
    BLACK("Midnight Black", "Deep black wolf - Unlock by reaching Devotee rank", false, false),

    // PREMIUM WOLF COLORS (Requires paid tier or special achievement)
    GOLDEN("Golden", "Radiant golden wolf - Premium or special achievement", false, true),
    RED("Crimson", "Bold red wolf - Premium", false, true),
    BLUE("Sapphire", "Cool blue wolf - Premium", false, true),
    GREEN("Emerald", "Vibrant green wolf - Premium", false, true),
    PURPLE("Royal Purple", "Majestic purple wolf - Premium", false, true),
    PINK("Soft Pink", "Gentle pink wolf - Premium", false, true);

    private final String displayName;
    private final String description;
    private final boolean isFree;
    private final boolean isPremium;

    PetColor(String displayName, String description, boolean isFree, boolean isPremium) {
        this.displayName = displayName;
        this.description = description;
        this.isFree = isFree;
        this.isPremium = isPremium;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFree() {
        return isFree;
    }

    public boolean isPremium() {
        return isPremium;
    }

    /**
     * Get colors available for free tier users
     */
    public static PetColor[] getFreeColors() {
        return new PetColor[]{GREY};
    }

    /**
     * Get colors that can be unlocked through achievements
     */
    public static PetColor[] getAchievementColors() {
        return new PetColor[]{BROWN, WHITE, BLACK};
    }

    /**
     * Get premium-only colors
     */
    public static PetColor[] getPremiumColors() {
        return new PetColor[]{GOLDEN, RED, BLUE, GREEN, PURPLE, PINK};
    }

    /**
     * Check if user can select this color
     *
     * @param hasPremium     Whether user has premium subscription
     * @param unlockedColors Set of colors user has unlocked through achievements
     * @return true if user can use this color
     */
    public boolean canUserSelect(boolean hasPremium, java.util.Set<PetColor> unlockedColors) {
        if (isFree) return true;
        if (isPremium && hasPremium) return true;
        return unlockedColors != null && unlockedColors.contains(this);
    }

    /**
     * TODO: When adding new pet types, decide on color strategy:
     *
     * Option A - Expand this enum:
     * Add new values like CAT_ORANGE, CAT_TABBY, DRAGON_RED
     * Pro: Simple, one enum for all colors
     * Con: Gets messy with many pets
     *
     * Option B - Separate enums per pet:
     * Create WolfColor, CatColor, DragonColor enums
     * Refactor User.petColor to be a String or create ColorChoice interface
     * Pro: Clean separation, each pet has its own colors
     * Con: More refactoring needed
     *
     * Option C - Database-driven colors:
     * Store colors in a database table instead of enum
     * Pro: Most flexible, colors can be added without code changes
     * Con: More complex, loses type safety
     */
}