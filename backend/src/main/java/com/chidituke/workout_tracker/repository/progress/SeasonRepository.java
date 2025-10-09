package com.chidituke.workout_tracker.repository.progress;

import com.chidituke.workout_tracker.model.progress.Season;
import com.chidituke.workout_tracker.model.progress.enums.SeasonType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Integer> {

    Optional<Season> findByIsActiveTrue();

    @Query("SELECT s FROM Season s WHERE s.seasonType = :seasonType AND YEAR(s.startDate) = :year")
    Optional<Season> findBySeasonTypeAndYear(
            @Param("seasonType") SeasonType seasonType,
            @Param("year") int year
    );

    @Query("SELECT s FROM Season s WHERE :date BETWEEN s.startDate AND s.endDate")
    Optional<Season> findSeasonContainingDate(@Param("date") LocalDate date);

    List<Season> findAllByOrderByStartDateDesc();

    @Query("SELECT s FROM Season s WHERE s.startDate > :today ORDER BY s.startDate ASC")
    List<Season> findUpcomingSeasons(@Param("today") LocalDate today);

    @Query("SELECT s FROM Season s WHERE s.endDate < :today ORDER BY s.startDate DESC")
    List<Season> findPastSeasons(@Param("today") LocalDate today);

    boolean existsBySeasonName(String seasonName);
}