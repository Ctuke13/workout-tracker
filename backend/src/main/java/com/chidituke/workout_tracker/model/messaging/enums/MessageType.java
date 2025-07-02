package com.chidituke.workout_tracker.model.messaging.enums;

/**
 * Enumeration for different types of messages that can be sent
 * Supports text, media, and fitness-specific content sharing
 */
public enum MessageType {

    /**
     * Standard text message
     */
    TEXT("Text Message", "💬", false, false),

    /**
     * Image attachment
     */
    IMAGE("Image", "📷", true, false),

    /**
     * Video attachment
     */
    VIDEO("Video", "🎥", true, false),

    /**
     * Audio attachment (voice notes, music, etc.)
     */
    AUDIO("Audio", "🎵", true, false),

    /**
     * File attachment (documents, PDFs, etc.)
     */
    FILE("File", "📎", true, false),

    /**
     * External link sharing
     */
    LINK("Link", "🔗", false, true),

    /**
     * Shared workout session (fitness-specific)
     */
    WORKOUT("Workout Shared", "🏋️", false, true),

    /**
     * Shared workout plan/program (fitness-specific)
     */
    WORKOUT_PLAN("Workout Plan", "📋", false, true),

    /**
     * System-generated message (notifications, etc.)
     */
    SYSTEM("System Message", "⚙️", false, false);

    private final String displayName;
    private final String emoji;
    private final boolean requiresMedia;
    private final boolean requiresMetadata;

    MessageType(String displayName, String emoji, boolean requiresMedia, boolean requiresMetadata) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.requiresMedia = requiresMedia;
        this.requiresMetadata = requiresMetadata;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayWithEmoji() {
        return emoji + " " + displayName;
    }

    public boolean requiresMedia() {
        return requiresMedia;
    }

    public boolean requiresMetadata() {
        return requiresMetadata;
    }

    /**
     * Check if this message type represents media content
     */
    public boolean isMediaType() {
        return this == IMAGE || this == VIDEO || this == AUDIO || this == FILE;
    }

    /**
     * Check if this message type represents fitness content
     */
    public boolean isFitnessContent() {
        return this == WORKOUT || this == WORKOUT_PLAN;
    }

    /**
     * Check if this message type can be shared externally
     */
    public boolean canShareExternally() {
        return this == TEXT || this == IMAGE || this == VIDEO || this == AUDIO || this == FILE || this == WORKOUT;
    }

    /**
     * Check if this message type supports rich preview
     */
    public boolean supportsRichPreview() {
        return this == LINK || this == WORKOUT || this == WORKOUT_PLAN;
    }

    /**
     * Get expected file extensions for media types
     */
    public String[] getSupportedExtensions() {
        return switch (this) {
            case IMAGE -> new String[]{".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".tiff"};
            case VIDEO -> new String[]{".mp4", ".mov", ".avi", ".mkv", ".webm", ".flv", ".wmv"};
            case AUDIO -> new String[]{".mp3", ".wav", ".aac", ".ogg", ".flac", ".m4a", ".wma"};
            case FILE -> new String[]{".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
                    ".txt", ".rtf", ".zip", ".rar", ".csv", ".json"};
            default -> new String[]{};
        };
    }

    /**
     * Get maximum file size in bytes for this message type
     */
    public long getMaxFileSizeBytes() {
        return switch (this) {
            case IMAGE -> 10L * 1024 * 1024;      // 10MB for images
            case VIDEO -> 25L * 1024 * 1024;      // 25MB for videos
            case AUDIO -> 5L * 1024 * 1024;       // 5MB for audio files
            case FILE -> 15L * 1024 * 1024;       // 15MB for general files
            default -> 0L;                         // No file size limit for non-media
        };
    }

    /**
     * Check if content should be filtered for inappropriate material
     */
    public boolean requiresContentFiltering() {
        return this == TEXT || this == IMAGE || this == VIDEO || this == AUDIO;
    }

    /**
     * Get user-friendly file size description
     */
    public String getFileSizeDescription() {
        long sizeBytes = getMaxFileSizeBytes();
        if (sizeBytes == 0) return "No file limit";

        long sizeMB = sizeBytes / (1024 * 1024);
        return sizeMB + "MB max";
    }

    /**
     * Get MIME type patterns for file validation
     */
    public String[] getMimeTypePatterns() {
        return switch (this) {
            case IMAGE -> new String[]{"image/*"};
            case VIDEO -> new String[]{"video/*"};
            case AUDIO -> new String[]{"audio/*"};
            case FILE -> new String[]{"application/*", "text/*"};
            default -> new String[]{};
        };
    }

    /**
     * Check if this message type is suitable for fitness content
     */
    public boolean isFitnessCompatible() {
        return switch (this) {
            case TEXT, IMAGE, VIDEO, AUDIO, WORKOUT, WORKOUT_PLAN -> true;
            case FILE -> true; // For workout PDFs, nutrition guides, etc.
            case LINK -> true;  // For sharing fitness videos, articles
            case SYSTEM -> false; // System messages aren't user-generated fitness content
        };
    }

    /**
     * Get priority for message ordering (lower = higher priority)
     */
    public int getDisplayPriority() {
        return switch (this) {
            case SYSTEM -> 1;           // Highest priority
            case WORKOUT, WORKOUT_PLAN -> 2; // Fitness content priority
            case TEXT -> 3;             // Standard text
            case IMAGE, VIDEO -> 4;     // Media content
            case AUDIO, FILE -> 5;      // Other attachments
            case LINK -> 6;             // External links
        };
    }

    /**
     * Check if message type supports replies/threading
     */
    public boolean supportsReplies() {
        return this != SYSTEM; // All message types except system messages can be replied to
    }

    /**
     * Get appropriate notification sound type
     */
    public String getNotificationSoundType() {
        return switch (this) {
            case WORKOUT, WORKOUT_PLAN -> "fitness";
            case IMAGE, VIDEO -> "media";
            case AUDIO -> "voice";
            case FILE -> "document";
            case SYSTEM -> "alert";
            default -> "message";
        };
    }

    /**
     * Get the database value for storage
     */
    public String getDatabaseValue() {
        return this.name();
    }

    /**
     * Create from database value
     */
    public static MessageType fromDatabaseValue(String value) {
        try {
            return MessageType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return TEXT; // Default fallback
        }
    }

    /**
     * Get all media types as array
     */
    public static MessageType[] getMediaTypes() {
        return new MessageType[]{IMAGE, VIDEO, AUDIO, FILE};
    }

    /**
     * Get all fitness-related types as array
     */
    public static MessageType[] getFitnessTypes() {
        return new MessageType[]{WORKOUT, WORKOUT_PLAN};
    }

    /**
     * Check if file extension is supported for this message type
     */
    public boolean isExtensionSupported(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }

        String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        String[] supportedExtensions = getSupportedExtensions();

        for (String supportedExt : supportedExtensions) {
            if (supportedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate file size against message type limits
     */
    public boolean isFileSizeValid(long fileSizeBytes) {
        long maxSize = getMaxFileSizeBytes();
        return maxSize == 0 || fileSizeBytes <= maxSize;
    }
}