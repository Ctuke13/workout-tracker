package com.chidituke.workout_tracker.service.user;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.chidituke.workout_tracker.model.pet.PetStats;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.pet.PetStatsRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Handles profile photo uploads to Cloudinary.
 * <p>
 * Crystal reward policy:
 * - User earns 2 crystals the FIRST time they upload a photo (profileImageUrl was null before)
 * - Subsequent updates/replacements do not award crystals
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfilePhotoService {

    private static final int FIRST_UPLOAD_CRYSTAL_REWARD = 2;
    private static final String UPLOAD_FOLDER = "evopet/profile_photos";

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;
    private final PetStatsRepository petStatsRepository;

    /**
     * Upload a profile photo for the given user.
     *
     * @param userId The authenticated user's ID
     * @param file   The image file from the multipart request
     * @return Result containing the new photo URL and crystals awarded
     */
    @Transactional
    public ProfilePhotoResult uploadProfilePhoto(Long userId, MultipartFile file) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Determine if this is the user's first ever upload (before we overwrite the value)
        boolean isFirstUpload = user.getProfileImageUrl() == null
                || user.getProfileImageUrl().isBlank();

        // Upload to Cloudinary — use userId as public_id so re-uploads overwrite cleanly
        String publicId = UPLOAD_FOLDER + "/user_" + userId;

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "overwrite", true,
                        "resource_type", "image",
                        "transformation", ObjectUtils.asMap(
                                "width", 400,
                                "height", 400,
                                "crop", "fill",
                                "gravity", "face"   // centres crop on the face if detected
                        )
                )
        );

        String imageUrl = (String) uploadResult.get("secure_url");
        log.info("✅ Uploaded profile photo for user {}: {}", userId, imageUrl);

        // Save URL to user record
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        // Award 2 crystals on first upload only
        int crystalsAwarded = 0;
        if (isFirstUpload) {
            crystalsAwarded = awardCrystals(userId);
            log.info("💎 Awarded {} crystals to user {} for first profile photo upload", crystalsAwarded, userId);
        }

        return new ProfilePhotoResult(imageUrl, crystalsAwarded, isFirstUpload);
    }

    private int awardCrystals(Long userId) {
        return petStatsRepository.findByUserId(userId).map(petStats -> {
            petStats.setCrystals(petStats.getCrystals() + FIRST_UPLOAD_CRYSTAL_REWARD);
            petStatsRepository.save(petStats);
            return FIRST_UPLOAD_CRYSTAL_REWARD;
        }).orElse(0);
    }

    // ── Result record ──────────────────────────────────────────────────────────

    public record ProfilePhotoResult(
            String imageUrl,
            int crystalsAwarded,
            boolean firstUpload
    ) {
    }
}