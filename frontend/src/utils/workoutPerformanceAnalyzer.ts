import {ScheduledExercise, WorkoutResults} from '../types/exercise';
import {
    CriterionDetail,
    PerformanceEvaluation,
    WorkoutAnalysisResult,
    WorkoutQuickStats,
    WorkoutSummary
} from '../types/workoutAnalysis';

/**
 * Enhanced performance evaluation that checks ALL targets across different exercise types
 */
export const evaluateTargetAchievement = (
    exercise: ScheduledExercise,
    results: WorkoutResults
): PerformanceEvaluation => {
    const criteria: CriterionDetail[] = [];
    let totalWeight = 0;
    let achievedWeight = 0;

    // 1. Sets criterion (always applicable)
    if (exercise.targetSets) {
        const completedSets = results.sets.filter(s => s.completed).length;
        const setsStatus = completedSets >= exercise.targetSets ? 'MET' :
            completedSets >= exercise.targetSets * 0.8 ? 'PARTIAL' : 'BELOW_TARGET';

        criteria.push({
            name: 'Sets',
            target: exercise.targetSets,
            actual: completedSets,
            status: setsStatus,
            weight: 0.25,
            displayText: `${completedSets}/${exercise.targetSets} sets`,
            percentage: (completedSets / exercise.targetSets) * 100
        });

        totalWeight += 0.25;
        if (setsStatus === 'MET') achievedWeight += 0.25;
        else if (setsStatus === 'PARTIAL') achievedWeight += 0.15;
    }

    // 2. Exercise type specific criteria
    if (exercise.exercise.isCardio) {
        analyzeCardioPerformance(exercise, results, criteria, totalWeight, achievedWeight);
    } else if (exercise.exercise.isIsometric) {
        analyzeIsometricPerformance(exercise, results, criteria, totalWeight, achievedWeight);
    } else {
        analyzeStrengthPerformance(exercise, results, criteria, totalWeight, achievedWeight);
    }

    // Calculate overall achievement
    const achievementScore = totalWeight > 0 ? (achievedWeight / totalWeight) * 100 : 0;

    let overall: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET';
    if (achievementScore >= 100) overall = 'EXCEEDED';
    else if (achievementScore >= 90) overall = 'MET';
    else if (achievementScore >= 70) overall = 'PARTIAL';
    else overall = 'BELOW_TARGET';

    return {overall, criteria, achievementScore};
};

/**
 * Analyze cardio exercise performance
 */
const analyzeCardioPerformance = (
    exercise: ScheduledExercise,
    results: WorkoutResults,
    criteria: CriterionDetail[],
    totalWeight: number,
    achievedWeight: number
) => {
    // Duration criterion for cardio
    if (exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes) {
        const targetDuration = exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes!;
        const actualDuration = results.actualDurationMinutes || results.totalDurationMinutes;

        const durationStatus = actualDuration >= targetDuration ?
            (actualDuration >= targetDuration * 1.1 ? 'EXCEEDED' : 'MET') :
            (actualDuration >= targetDuration * 0.8 ? 'PARTIAL' : 'BELOW_TARGET');

        criteria.push({
            name: 'Duration',
            target: targetDuration,
            actual: actualDuration,
            status: durationStatus,
            weight: 0.4,
            displayText: `${actualDuration}/${targetDuration} min`,
            unit: 'minutes',
            percentage: (actualDuration / targetDuration) * 100
        });

        totalWeight += 0.4;
        if (durationStatus === 'EXCEEDED') achievedWeight += 0.44;
        else if (durationStatus === 'MET') achievedWeight += 0.4;
        else if (durationStatus === 'PARTIAL') achievedWeight += 0.25;
    }

    // Distance criterion (if set)
    if (exercise.targetDistance || exercise.targetDistanceKm) {
        const targetDistance = exercise.targetDistance ||
            (exercise.targetDistanceKm ? exercise.targetDistanceKm * 0.621371 : 0);
        const actualDistance = results.actualDistanceKm ?
            results.actualDistanceKm * 0.621371 : 0;

        const distanceStatus = actualDistance >= targetDistance ?
            (actualDistance >= targetDistance * 1.05 ? 'EXCEEDED' : 'MET') :
            (actualDistance >= targetDistance * 0.9 ? 'PARTIAL' : 'BELOW_TARGET');

        criteria.push({
            name: 'Distance',
            target: targetDistance,
            actual: actualDistance,
            status: distanceStatus,
            weight: 0.25,
            displayText: `${actualDistance.toFixed(1)}/${targetDistance.toFixed(1)} mi`,
            unit: 'miles',
            percentage: (actualDistance / targetDistance) * 100
        });

        totalWeight += 0.25;
        if (distanceStatus === 'EXCEEDED') achievedWeight += 0.275;
        else if (distanceStatus === 'MET') achievedWeight += 0.25;
        else if (distanceStatus === 'PARTIAL') achievedWeight += 0.15;
    }

    // Pace criterion (if set) - lower is better
    if (exercise.targetPace) {
        const actualPace = results.actualPace || 0;
        const paceStatus = actualPace <= exercise.targetPace ?
            (actualPace <= exercise.targetPace * 0.95 ? 'EXCEEDED' : 'MET') :
            (actualPace <= exercise.targetPace * 1.1 ? 'PARTIAL' : 'BELOW_TARGET');

        criteria.push({
            name: 'Pace',
            target: exercise.targetPace,
            actual: actualPace,
            status: paceStatus,
            weight: 0.1,
            displayText: `${formatPace(actualPace)}/${formatPace(exercise.targetPace)} /mi`,
            unit: 'min/mile',
            percentage: (exercise.targetPace / actualPace) * 100 // Inverted for pace
        });

        totalWeight += 0.1;
        if (paceStatus === 'EXCEEDED') achievedWeight += 0.11;
        else if (paceStatus === 'MET') achievedWeight += 0.1;
        else if (paceStatus === 'PARTIAL') achievedWeight += 0.05;
    }
};

/**
 * Analyze isometric exercise performance
 */
const analyzeIsometricPerformance = (
    exercise: ScheduledExercise,
    results: WorkoutResults,
    criteria: CriterionDetail[],
    totalWeight: number,
    achievedWeight: number
) => {
    if (exercise.holdDurationSeconds) {
        // For isometric exercises, actualReps contains the hold time in seconds
        const totalActualHold = results.sets.reduce((sum, set) => sum + (set.actualReps || 0), 0);
        const expectedTotalHold = exercise.holdDurationSeconds * (exercise.targetSets || 1);

        const holdStatus = totalActualHold >= expectedTotalHold ?
            (totalActualHold >= expectedTotalHold * 1.1 ? 'EXCEEDED' : 'MET') :
            (totalActualHold >= expectedTotalHold * 0.8 ? 'PARTIAL' : 'BELOW_TARGET');

        criteria.push({
            name: 'Hold Time',
            target: expectedTotalHold,
            actual: totalActualHold,
            status: holdStatus,
            weight: 0.5,
            displayText: `${totalActualHold}/${expectedTotalHold}s total`,
            unit: 'seconds',
            percentage: (totalActualHold / expectedTotalHold) * 100
        });

        totalWeight += 0.5;
        if (holdStatus === 'EXCEEDED') achievedWeight += 0.55;
        else if (holdStatus === 'MET') achievedWeight += 0.5;
        else if (holdStatus === 'PARTIAL') achievedWeight += 0.3;

        // Average Hold Analysis
        if (results.sets.length > 0) {
            const avgActualHold = totalActualHold / results.sets.length;
            const avgPercentage = (avgActualHold / exercise.holdDurationSeconds) * 100;

            criteria.push({
                name: 'Average Hold Time',
                target: exercise.holdDurationSeconds,
                actual: Math.round(avgActualHold),
                status: avgActualHold >= exercise.holdDurationSeconds ?
                    (avgActualHold >= exercise.holdDurationSeconds * 1.1 ? 'EXCEEDED' : 'MET') :
                    (avgActualHold >= exercise.holdDurationSeconds * 0.8 ? 'PARTIAL' : 'BELOW_TARGET'),
                weight: 0.25,
                displayText: `${Math.round(avgActualHold)}s of ${exercise.holdDurationSeconds}s`,
                unit: 'seconds per set',
                percentage: avgPercentage
            });
        }
    }
};

/**
 * Analyze strength exercise performance
 */
const analyzeStrengthPerformance = (
    exercise: ScheduledExercise,
    results: WorkoutResults,
    criteria: CriterionDetail[],
    totalWeight: number,
    achievedWeight: number
) => {
    // Reps criterion
    if (exercise.targetReps) {
        const targetReps = typeof exercise.targetReps === 'number' ?
            exercise.targetReps : parseInt(String(exercise.targetReps), 10);

        const totalActualReps = results.sets.reduce((sum, set) => sum + set.actualReps, 0);
        const expectedTotalReps = targetReps * results.sets.length;
        const repsRatio = totalActualReps / expectedTotalReps;

        const repsStatus = repsRatio >= 1.0 ?
            (repsRatio >= 1.1 ? 'EXCEEDED' : 'MET') :
            (repsRatio >= 0.8 ? 'PARTIAL' : 'BELOW_TARGET');

        criteria.push({
            name: 'Total Reps',
            target: expectedTotalReps,
            actual: totalActualReps,
            status: repsStatus,
            weight: 0.4,
            displayText: `${totalActualReps}/${expectedTotalReps} total reps`,
            unit: 'reps',
            percentage: repsRatio * 100
        });

        totalWeight += 0.4;
        if (repsStatus === 'EXCEEDED') achievedWeight += 0.44;
        else if (repsStatus === 'MET') achievedWeight += 0.4;
        else if (repsStatus === 'PARTIAL') achievedWeight += 0.25;

        // Rep Consistency Analysis
        const setConsistency = results.sets.filter(set => set.actualReps >= targetReps).length;
        const consistencyPercentage = (setConsistency / results.sets.length) * 100;

        criteria.push({
            name: 'Rep Consistency',
            target: results.sets.length,
            actual: setConsistency,
            status: setConsistency === results.sets.length ? 'MET' :
                setConsistency >= results.sets.length * 0.8 ? 'PARTIAL' : 'BELOW_TARGET',
            weight: 0.1,
            displayText: `${setConsistency}/${results.sets.length} sets`,
            unit: 'sets at target',
            percentage: consistencyPercentage
        });
    }

    // Weight criterion (if set)
    if (exercise.targetWeight && exercise.targetWeight > 0) {
        const maxActualWeight = Math.max(...results.sets.map(set => set.actualWeight || 0));
        const percentage = (maxActualWeight / exercise.targetWeight) * 100;

        const weightStatus = maxActualWeight >= exercise.targetWeight ?
            (maxActualWeight > exercise.targetWeight * 1.05 ? 'EXCEEDED' : 'MET') :
            (maxActualWeight >= exercise.targetWeight * 0.9 ? 'PARTIAL' : 'BELOW_TARGET');

        criteria.push({
            name: 'Max Weight',
            target: exercise.targetWeight,
            actual: maxActualWeight,
            status: weightStatus,
            weight: 0.3,
            displayText: `${maxActualWeight}/${exercise.targetWeight} ${exercise.targetWeightUnit || 'lbs'}`,
            unit: exercise.targetWeightUnit || 'lbs',
            percentage
        });

        totalWeight += 0.3;
        if (weightStatus === 'EXCEEDED') achievedWeight += 0.33;
        else if (weightStatus === 'MET') achievedWeight += 0.3;
        else if (weightStatus === 'PARTIAL') achievedWeight += 0.18;
    }

    // RPE criterion (if set) - lower is better for same performance
    if (exercise.targetRpe) {
        const rpeValues = results.sets
            .filter(set => set.rpe && set.rpe > 0)
            .map(set => set.rpe!);

        if (rpeValues.length > 0) {
            const avgRpe = rpeValues.reduce((sum, rpe) => sum + rpe, 0) / rpeValues.length;
            const percentage = (exercise.targetRpe / avgRpe) * 100; // Inverted for RPE

            const rpeStatus = avgRpe <= exercise.targetRpe ?
                (avgRpe <= exercise.targetRpe - 1 ? 'EXCEEDED' : 'MET') :
                (avgRpe <= exercise.targetRpe + 1 ? 'PARTIAL' : 'BELOW_TARGET');

            criteria.push({
                name: 'Average RPE',
                target: exercise.targetRpe,
                actual: Math.round(avgRpe * 10) / 10,
                status: rpeStatus,
                weight: 0.05,
                displayText: `${(Math.round(avgRpe * 10) / 10)}/${exercise.targetRpe} effort`,
                unit: 'effort level',
                percentage
            });

            totalWeight += 0.05;
            if (rpeStatus === 'EXCEEDED') achievedWeight += 0.055;
            else if (rpeStatus === 'MET') achievedWeight += 0.05;
            else if (rpeStatus === 'PARTIAL') achievedWeight += 0.025;
        }
    }
};

/**
 * Generate quick stats summary for display
 */
export const generateQuickStats = (exercise: ScheduledExercise, results: WorkoutResults): WorkoutQuickStats => {
    const completedSets = results.sets.filter(s => s.completed).length;

    const stats: WorkoutQuickStats = {
        completedSets,
        totalDuration: results.totalDurationMinutes
    };

    if (exercise.exercise.isCardio) {
        stats.totalDistance = results.actualDistanceKm ? results.actualDistanceKm * 0.621371 : undefined;
        stats.distanceUnit = 'miles';
        stats.averagePace = results.actualPace;
        stats.caloriesBurned = results.caloriesBurned;
    } else if (exercise.exercise.isIsometric) {
        stats.totalHoldTime = results.sets.reduce((sum, set) => sum + (set.actualReps || 0), 0);
        stats.averageHoldTime = stats.totalHoldTime && results.sets.length > 0 ?
            Math.round(stats.totalHoldTime / results.sets.length) : undefined;
    } else {
        // Strength exercise
        stats.totalReps = results.sets.reduce((sum, set) => sum + set.actualReps, 0);
        if (results.sets.some(set => set.actualWeight && set.actualWeight > 0)) {
            stats.maxWeight = Math.max(...results.sets.map(set => set.actualWeight || 0));
            stats.weightUnit = results.sets[0]?.targetWeightUnit || 'lbs';
        }
    }

    return stats;
};

/**
 * Complete workout analysis combining all aspects
 */
export const analyzeWorkout = (exercise: ScheduledExercise, results: WorkoutResults): WorkoutAnalysisResult => {
    const performance = evaluateTargetAchievement(exercise, results);
    const quickStats = generateQuickStats(exercise, results);

    const summary: WorkoutSummary = {
        exerciseName: exercise.exercise.name || exercise.exercise.exerciseName || 'Unknown Exercise',
        completedAt: results.completedAt,
        exerciseType: exercise.exercise.isCardio ? 'cardio' :
            exercise.exercise.isIsometric ? 'isometric' : 'strength',
        totalDuration: results.totalDurationMinutes,
        performanceRating: performance.overall,
        achievementPercentage: Math.round(performance.achievementScore),
        hasPersonalRecords: (results.personalRecords?.length || 0) > 0,
        hasImprovements: (results.improvements?.length || 0) > 0
    };

    return {
        performance,
        quickStats,
        summary
    };
};

/**
 * Helper function to format pace for display
 */
export const formatPace = (pace: number): string => {
    const minutes = Math.floor(pace);
    const seconds = Math.round((pace - minutes) * 60);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
};

/**
 * Fallback analysis when sets data is missing or incomplete
 */
export const createFallbackAnalysis = (exercise: ScheduledExercise, results: WorkoutResults): WorkoutAnalysisResult => {
    console.log('🔄 Creating fallback analysis for:', exercise.exercise.name);

    const criteria: CriterionDetail[] = [];

    // For fallback, we focus on high-level data that's available
    if (exercise.exercise.isCardio) {
        // Cardio fallback - use total duration
        if (exercise.targetDurationMinutes) {
            const actualDuration = results.actualDurationMinutes || results.totalDurationMinutes;
            const percentage = (actualDuration / exercise.targetDurationMinutes) * 100;
            const status = actualDuration >= exercise.targetDurationMinutes ? 'MET' :
                actualDuration >= exercise.targetDurationMinutes * 0.8 ? 'PARTIAL' : 'BELOW_TARGET';

            criteria.push({
                name: 'Duration',
                target: exercise.targetDurationMinutes,
                actual: actualDuration,
                status,
                weight: 1.0,
                displayText: `${actualDuration}/${exercise.targetDurationMinutes} min`,
                unit: 'minutes',
                percentage
            });
        }
    } else if (exercise.exercise.isIsometric) {
        // Isometric fallback - use duration as hold time
        const actualHoldTime = results.totalDurationMinutes * 60; // Convert to seconds
        if (exercise.holdDurationSeconds) {
            const percentage = (actualHoldTime / exercise.holdDurationSeconds) * 100;
            const status = actualHoldTime >= exercise.holdDurationSeconds ? 'MET' :
                actualHoldTime >= exercise.holdDurationSeconds * 0.8 ? 'PARTIAL' : 'BELOW_TARGET';

            criteria.push({
                name: 'Hold Time',
                target: exercise.holdDurationSeconds,
                actual: actualHoldTime,
                status,
                weight: 1.0,
                displayText: `${actualHoldTime}/${exercise.holdDurationSeconds} seconds`,
                unit: 'seconds',
                percentage
            });
        }
    } else {
        // Strength fallback - assume workout was completed as planned
        criteria.push({
            name: 'Workout Completion',
            target: 1,
            actual: 1,
            status: 'MET',
            weight: 1.0,
            displayText: 'Workout completed',
            unit: 'completion',
            percentage: 100
        });
    }

    // Overall performance based on available criteria
    const achievementScore = criteria.length > 0 ?
        criteria.reduce((sum, c) => sum + (c.percentage || 0), 0) / criteria.length : 100;

    let overall: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET';
    if (achievementScore >= 100) overall = 'MET'; // Conservative for fallback
    else if (achievementScore >= 80) overall = 'PARTIAL';
    else overall = 'BELOW_TARGET';

    const performance: PerformanceEvaluation = {
        overall,
        criteria,
        achievementScore
    };

    // Generate quick stats for fallback
    const quickStats: WorkoutQuickStats = {
        completedSets: 1, // Assume 1 set was completed
        totalDuration: results.totalDurationMinutes
    };

    if (exercise.exercise.isCardio) {
        quickStats.caloriesBurned = results.caloriesBurned;
    } else if (exercise.exercise.isIsometric) {
        quickStats.totalHoldTime = results.totalDurationMinutes * 60;
        quickStats.averageHoldTime = quickStats.totalHoldTime;
    } else {
        quickStats.totalReps = exercise.targetReps || 0;
    }

    const summary: WorkoutSummary = {
        exerciseName: exercise.exercise.name || exercise.exercise.exerciseName || 'Unknown Exercise',
        completedAt: results.completedAt,
        exerciseType: exercise.exercise.isCardio ? 'cardio' :
            exercise.exercise.isIsometric ? 'isometric' : 'strength',
        totalDuration: results.totalDurationMinutes,
        performanceRating: overall,
        achievementPercentage: Math.round(achievementScore),
        hasPersonalRecords: (results.personalRecords?.length || 0) > 0,
        hasImprovements: (results.improvements?.length || 0) > 0
    };

    console.log('✅ Fallback analysis created:', {performance, quickStats, summary});

    return {
        performance,
        quickStats,
        summary
    };
};