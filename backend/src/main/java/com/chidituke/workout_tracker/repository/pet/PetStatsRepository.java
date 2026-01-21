package com.chidituke.workout_tracker.repository.pet;

import com.chidituke.workout_tracker.model.pet.PetStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PetStats entity
 * Provides database access for pet statistics
 */
@Repository
public interface PetStatsRepository extends JpaRepository<PetStats, Long> {

    // ============================================
    // BASIC CRUD QUERIES
    // ============================================

    /**
     * Find pet stats by user ID
     *
     * @param userId The user's ID
     * @return Optional containing pet stats if found
     */
    Optional<PetStats> findByUserId(Long userId);

    /**
     * Check if pet stats exist for a user
     *
     * @param userId The user's ID
     * @return true if pet exists
     */
    boolean existsByUserId(Long userId);

    /**
     * Delete pet stats by user ID
     *
     * @param userId The user's ID
     */
    void deleteByUserId(Long userId);

    // ============================================
    // STAT-BASED QUERIES
    // ============================================

    /**
     * Find pets with low fuel
     *
     * @param threshold Fuel threshold (e.g., 30)
     * @return List of pets with fuel below threshold
     */
    List<PetStats> findByFuelLessThan(int threshold);

    /**
     * Find pets with low motivation
     *
     * @param threshold Motivation threshold (e.g., 40)
     * @return List of pets with motivation below threshold
     */
    List<PetStats> findByMotivationLessThan(int threshold);

    /**
     * Find pets with high fatigue
     *
     * @param threshold Fatigue threshold (e.g., 70)
     * @return List of pets with fatigue above threshold
     */
    List<PetStats> findByFatigueGreaterThan(int threshold);

    /**
     * Find pets with low cleanliness (dirty)
     *
     * @param threshold Cleanliness threshold (e.g., 40)
     * @return List of pets with cleanliness below threshold
     */
    List<PetStats> findByCleanlinessLessThan(int threshold);

    // ============================================
    // SLEEP & NEGLECT QUERIES
    // ============================================

    /**
     * Find all sleeping pets
     *
     * @return List of pets that are currently sleeping
     */
    @Query("SELECT p FROM PetStats p WHERE p.isSleeping = true")
    List<PetStats> findSleepingPets();

    /**
     * Find all neglected pets
     *
     * @return List of pets that are currently neglected
     */
    @Query("SELECT p FROM PetStats p WHERE p.isNeglected = true")
    List<PetStats> findNeglectedPets();

    /**
     * Find pets with high fatigue (≥70) for admin monitoring
     *
     * @return List of pets nearing exhaustion
     */
    @Query("SELECT p FROM PetStats p WHERE p.fatigue >= 70")
    List<PetStats> findHighFatiguePets();

    // ============================================
    // ANALYTICS QUERIES
    // ============================================

    /**
     * Get total count of all pets
     *
     * @return Total number of pets
     */
    @Query("SELECT COUNT(p) FROM PetStats p")
    Long getTotalPetCount();

    /**
     * Get average fuel across all pets
     *
     * @return Average fuel value
     */
    @Query("SELECT AVG(p.fuel) FROM PetStats p")
    Double getAverageFuel();

    /**
     * Get average motivation across all pets
     *
     * @return Average motivation value
     */
    @Query("SELECT AVG(p.motivation) FROM PetStats p")
    Double getAverageMotivation();

    /**
     * Get average fatigue across all pets
     *
     * @return Average fatigue value
     */
    @Query("SELECT AVG(p.fatigue) FROM PetStats p")
    Double getAverageFatigue();

    /**
     * Get average cleanliness across all pets
     *
     * @return Average cleanliness value
     */
    @Query("SELECT AVG(p.cleanliness) FROM PetStats p")
    Double getAverageCleanliness();

    /**
     * Count healthy pets (all stats above minimum thresholds)
     * Healthy = fuel ≥ 40, motivation ≥ 40, fatigue ≤ 70, cleanliness ≥ 40
     *
     * @return Count of healthy pets
     */
    @Query("SELECT COUNT(p) FROM PetStats p WHERE p.fuel >= 40 AND p.motivation >= 40 AND p.fatigue <= 70 AND p.cleanliness >= 40 AND p.isSleeping = false AND p.isNeglected = false")
    Long countHealthyPets();

    /**
     * Count sleeping pets
     *
     * @return Count of pets currently sleeping
     */
    @Query("SELECT COUNT(p) FROM PetStats p WHERE p.isSleeping = true")
    Long countSleepingPets();

    /**
     * Count neglected pets
     *
     * @return Count of pets currently neglected
     */
    @Query("SELECT COUNT(p) FROM PetStats p WHERE p.isNeglected = true")
    Long countNeglectedPets();

    // ============================================
    // CRYSTAL & ECONOMY QUERIES
    // ============================================

    /**
     * Find pets with no crystals (can't feed)
     *
     * @return List of pets with 0 crystals
     */
    @Query("SELECT p FROM PetStats p WHERE p.crystals = 0")
    List<PetStats> findPetsWithNoCrystals();

    /**
     * Find pets at crystal cap (15 crystals)
     *
     * @return List of pets with max crystals
     */
    @Query("SELECT p FROM PetStats p WHERE p.crystals = 15")
    List<PetStats> findPetsAtCrystalCap();

    /**
     * Get average crystals across all pets
     *
     * @return Average crystal count
     */
    @Query("SELECT AVG(p.crystals) FROM PetStats p")
    Double getAverageCrystals();

    // ============================================
    // HOME SELECTION QUERIES
    // ============================================

    /**
     * Find pets by selected home
     *
     * @param homeType Home type (GYM, NATURE, COZY, etc.)
     * @return List of pets with that home selected
     */
    List<PetStats> findBySelectedHome(String homeType);

    /**
     * Count pets per home type
     *
     * @param homeType Home type
     * @return Count of pets with that home
     */
    @Query("SELECT COUNT(p) FROM PetStats p WHERE p.selectedHome = :homeType")
    Long countByHomeType(String homeType);

    // ============================================
    // TIME-BASED QUERIES
    // ============================================

    /**
     * Find pets that haven't been fed recently (for neglect tracking)
     *
     * @param cutoffTime Time threshold (e.g., 4 days ago)
     * @return List of pets not fed since cutoff
     */
    @Query("SELECT p FROM PetStats p WHERE p.lastFedTime IS NULL OR p.lastFedTime < :cutoffTime")
    List<PetStats> findPetsNotFedSince(LocalDateTime cutoffTime);

    /**
     * Find pets that haven't worked out recently (for efficiency tracking)
     *
     * @param cutoffTime Time threshold (e.g., 5 days ago)
     * @return List of pets without recent workouts
     */
    @Query("SELECT p FROM PetStats p WHERE p.lastWorkoutTime IS NULL OR p.lastWorkoutTime < :cutoffTime")
    List<PetStats> findPetsWithoutRecentWorkout(LocalDateTime cutoffTime);

    /**
     * Find pets with sleep ending soon (within next hour)
     *
     * @param now            Current time
     * @param oneHourFromNow One hour from now
     * @return List of pets waking up soon
     */
    @Query("SELECT p FROM PetStats p WHERE p.isSleeping = true AND p.sleepEndTime BETWEEN :now AND :oneHourFromNow")
    List<PetStats> findPetsWakingSoon(LocalDateTime now, LocalDateTime oneHourFromNow);

    // ============================================
    // ADMIN & MONITORING QUERIES
    // ============================================

    /**
     * Find pets that need attention (low stats or special states)
     * This is a convenience method combining multiple conditions
     *
     * @return List of pets needing user attention
     */
    @Query("SELECT p FROM PetStats p WHERE p.fuel < 30 OR p.motivation < 40 OR p.cleanliness < 40 OR p.fatigue > 70 OR p.isNeglected = true")
    List<PetStats> findPetsNeedingAttention();

    /**
     * Find all pets for daily maintenance job
     * (This is just findAll() but more explicit for the scheduled job)
     *
     * @return All pets in the system
     */
    @Query("SELECT p FROM PetStats p")
    List<PetStats> findAllForMaintenance();

    /**
     * Find pets by last updated time (for detecting stale data)
     *
     * @param cutoffTime Time threshold
     * @return List of pets not updated since cutoff
     */
    @Query("SELECT p FROM PetStats p WHERE p.lastUpdated < :cutoffTime")
    List<PetStats> findStaleData(LocalDateTime cutoffTime);
}