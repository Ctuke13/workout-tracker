package com.chidituke.workout_tracker.service.progress;

import com.chidituke.workout_tracker.model.progress.Season;
import com.chidituke.workout_tracker.model.progress.enums.SeasonType;
import com.chidituke.workout_tracker.repository.progress.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonService {

    private final SeasonRepository seasonRepository;

    @Transactional(readOnly = true)
    public Season getActiveSeason() {
        return seasonRepository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException(
                        "No active season found. Please activate a season first."
                ));
    }

    @Transactional(readOnly = true)
    public Optional<Season> findActiveSeason() {
        return seasonRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Season> getSeasonById(Integer seasonId) {
        return seasonRepository.findById(seasonId);
    }

    @Transactional
    public Season createSeason(String seasonName, SeasonType seasonType,
                               LocalDate startDate, LocalDate endDate) {
        validateSeasonDates(startDate, endDate);

        if (seasonRepository.existsBySeasonName(seasonName)) {
            throw new IllegalArgumentException("Season name already exists: " + seasonName);
        }

        Season season = new Season();
        season.setSeasonName(seasonName);
        season.setSeasonType(seasonType);
        season.setStartDate(startDate);
        season.setEndDate(endDate);
        season.setIsActive(false);

        Season savedSeason = seasonRepository.save(season);
        log.info("Created new season: {} ({} to {})", seasonName, startDate, endDate);

        return savedSeason;
    }

    @Transactional
    public Season activateSeason(Integer seasonId) {
        Season seasonToActivate = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Season not found with ID: " + seasonId
                ));

        seasonRepository.findByIsActiveTrue().ifPresent(activeSeason -> {
            activeSeason.setIsActive(false);
            seasonRepository.save(activeSeason);
            log.info("Deactivated season: {}", activeSeason.getSeasonName());
        });

        seasonToActivate.setIsActive(true);
        Season activated = seasonRepository.save(seasonToActivate);
        log.info("Activated season: {}", activated.getSeasonName());

        return activated;
    }

    @Transactional(readOnly = true)
    public List<Season> getAllSeasons() {
        return seasonRepository.findAllByOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public List<Season> getUpcomingSeasons() {
        return seasonRepository.findUpcomingSeasons(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Season> getPastSeasons() {
        return seasonRepository.findPastSeasons(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Optional<Season> findSeasonForDate(LocalDate date) {
        return seasonRepository.findSeasonContainingDate(date);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSeason() {
        return seasonRepository.findByIsActiveTrue().isPresent();
    }

    private void validateSeasonDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        long durationDays = startDate.until(endDate).getDays();
        if (durationDays < 85 || durationDays > 95) {
            log.warn("Season duration is {} days (expected ~90 days)", durationDays);
        }
    }
}