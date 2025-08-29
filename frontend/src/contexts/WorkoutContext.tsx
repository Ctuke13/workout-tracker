import React, {createContext, useContext, useReducer, useEffect, ReactNode, useState, useCallback} from 'react';
import {ScheduledExercise, WorkoutResults} from '../types/exercise';
import {WorkoutSet, WorkoutExercise, PersonalRecord, PerformanceImprovement} from '../types/api';
import {calendarApi} from '../services/calendarApi';

// ==================== TYPES ====================

export interface WorkoutSession {
    id: string;
    date: string;
    exercises: WorkoutExercise[];
    status: 'not_started' | 'in_progress' | 'paused' | 'completed' | 'cancelled';
    startedAt?: Date;
    completedAt?: Date;
    pausedAt?: Date;
    totalDurationMinutes?: number;
    currentExerciseIndex: number;
    currentSetIndex: number;
    notes?: string;
}

export interface WorkoutContextType {
    // Current workout session
    currentWorkout: WorkoutSession | null;

    // Workout controls
    startWorkout: (exercises: ScheduledExercise[], date: string) => void;
    pauseWorkout: () => void;
    resumeWorkout: () => void;
    completeWorkout: () => void;
    cancelWorkout: () => void;

    // Exercise navigation
    goToNextExercise: () => void;
    goToPreviousExercise: () => void;
    goToExercise: (exerciseIndex: number) => void;

    // Set management
    completeSet: (setId: string, actualData: Partial<WorkoutSet>) => void;
    skipSet: (setId: string) => void;
    addSet: (exerciseId: string) => void;
    removeSet: (setId: string) => void;

    // Exercise management
    skipExercise: (exerciseId: string) => void;
    completeExercise: (exerciseId: string) => void;
    addExerciseToCurrentWorkout: (exercise: any, config: any) => void;

    // Session data
    getTotalDuration: () => number;
    getCurrentExercise: () => WorkoutExercise | null;
    getCurrentSet: () => WorkoutSet | null;
    getCompletionPercentage: () => number;

    // State queries
    isWorkoutActive: boolean;
    isPaused: boolean;
    canGoNext: boolean;
    canGoPrevious: boolean;
}

interface WorkoutCompletionData {
    exerciseId: string;
    scheduledExerciseId: string;
    completedAt: Date;
    totalDurationMinutes: number;
    sets: CompletedSetData[];
    notes?: string;
    performanceRating: 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL';
    personalRecords: PersonalRecord[];
    improvements: PerformanceImprovement[];
}

interface CompletedSetData {
    setNumber: number;
    targetReps: number;
    actualReps: number;
    targetWeight?: number;
    actualWeight?: number;
    targetWeightUnit: 'kg' | 'lbs';
    rpe?: number;
    restSeconds?: number;
    completed: boolean;
    actualDurationMinutes?: number; // For cardio
    actualHoldSeconds?: number; // For isometric
}

// ==================== HELPER FUNCTIONS ====================

/**
 * Determines appropriate number of sets based on exercise type and scheduled data
 */
const getExerciseSetsCount = (scheduledExercise: ScheduledExercise): number => {
    const exercise = scheduledExercise.exercise;

    if (scheduledExercise.targetSets && scheduledExercise.targetSets > 0) {
        return scheduledExercise.targetSets;
    }

    // Otherwise, determine based on exercise type
    if (exercise.isCardio) {
        return 1; // Cardio typically has 1 "set" representing the entire duration
    } else if (exercise.isIsometric) {
        return 3; // Isometric exercises typically have 3 holds
    } else {
        return 3; // Strength exercises typically have 3 sets
    }
};

/**
 * Gets appropriate target reps based on exercise type
 */
const getExerciseTargetReps = (scheduledExercise: ScheduledExercise): number => {
    const exercise = scheduledExercise.exercise;

    // First priority: explicit targetReps with safe type conversion
    if (scheduledExercise.targetReps !== undefined && scheduledExercise.targetReps !== null) {
        // Handle different possible types safely
        let reps: number;

        if (typeof scheduledExercise.targetReps === 'number') {
            reps = scheduledExercise.targetReps;
        } else if (typeof scheduledExercise.targetReps === 'string') {
            reps = parseInt(scheduledExercise.targetReps, 10);
        } else {
            // Handle any other type by converting to string first
            reps = parseInt(String(scheduledExercise.targetReps), 10);
        }

        if (!isNaN(reps) && reps > 0) {
            return Math.min(reps, 100); // Cap at 100 for sanity
        }
    }

    // Second priority: exercise type specific defaults
    if (exercise.isCardio) {
        // For cardio, "reps" represents duration in minutes
        return exercise.estimatedDurationMinutes || scheduledExercise.targetDurationMinutes || 20;
    } else if (exercise.isIsometric) {
        // For isometric, "reps" represents hold duration in seconds
        return scheduledExercise.holdDurationSeconds || 30;
    } else {
        // Standard reps for strength exercises
        return 10;
    }
};

/**
 * Gets appropriate rest seconds based on exercise type
 */
const getExerciseRestSeconds = (scheduledExercise: ScheduledExercise): number => {
    const exercise = scheduledExercise.exercise;

    // If rest is explicitly defined, use it
    if (scheduledExercise.restSeconds && scheduledExercise.restSeconds > 0) {
        return scheduledExercise.restSeconds;
    }

    // Otherwise, provide sensible defaults
    if (exercise.isCardio) {
        return 0; // Cardio typically doesn't have rest between "sets"
    } else if (exercise.isIsometric) {
        return 60; // Shorter rest for isometric holds
    } else {
        return 90; // Standard rest for strength exercises
    }
};

const getExerciseWeightUnit = (scheduledExercise: ScheduledExercise): 'kg' | 'lbs' => {
    return scheduledExercise.targetWeightUnit || 'lbs'; // Default to lbs for American users
};

/**
 * Validates exercise configuration and logs warnings for missing data
 */
const validateScheduledExercise = (scheduledExercise: ScheduledExercise): void => {
    const exercise = scheduledExercise.exercise;
    const exerciseName = exercise.name || exercise.exerciseName || 'Unknown Exercise';

    // Ensure exercise has a name
    if (!exercise.name && !exercise.exerciseName) {
        console.warn(`Warning: Exercise missing name, using fallback`);
        exercise.name = 'Unknown Exercise';
    }

    // Validate tracking mode compatibility
    if (exercise.isCardio && !scheduledExercise.targetDurationMinutes && !exercise.estimatedDurationMinutes) {
        console.warn(`Warning: Cardio exercise "${exerciseName}" missing duration, using default 20 minutes`);
    }

    if (exercise.isIsometric && !scheduledExercise.holdDurationSeconds) {
        console.warn(`Warning: Isometric exercise "${exerciseName}" missing hold duration, using default 30 seconds`);
    }

    if (!exercise.isCardio && !exercise.isIsometric && !scheduledExercise.targetSets) {
        console.warn(`Warning: Strength exercise "${exerciseName}" missing sets, using default 3 sets`);
    }

    // Log successful validation
    console.log(`Exercise "${exerciseName}" validated successfully:`, {
        type: exercise.isCardio ? 'cardio' : exercise.isIsometric ? 'isometric' : 'strength',
        sets: getExerciseSetsCount(scheduledExercise),
        reps: getExerciseTargetReps(scheduledExercise),
        rest: getExerciseRestSeconds(scheduledExercise),
        weight: scheduledExercise.targetWeight,
        weightUnit: getExerciseWeightUnit(scheduledExercise),
        notes: scheduledExercise.notes,
        isValid: true
    });
};

// ==================== WORKOUT REDUCER ====================

type WorkoutAction =
    | { type: 'START_WORKOUT'; payload: { exercises: ScheduledExercise[]; date: string } }
    | { type: 'PAUSE_WORKOUT' }
    | { type: 'RESUME_WORKOUT' }
    | { type: 'COMPLETE_WORKOUT' }
    | { type: 'CANCEL_WORKOUT' }
    | { type: 'GO_TO_EXERCISE'; payload: number }
    | { type: 'NEXT_EXERCISE' }
    | { type: 'PREVIOUS_EXERCISE' }
    | { type: 'COMPLETE_SET'; payload: { setId: string; actualData: Partial<WorkoutSet> } }
    | { type: 'SKIP_SET'; payload: string }
    | { type: 'ADD_SET'; payload: string }
    | { type: 'REMOVE_SET'; payload: string }
    | { type: 'SKIP_EXERCISE'; payload: string }
    | { type: 'COMPLETE_EXERCISE'; payload: string }
    | { type: 'UPDATE_DURATION' }
    | { type: 'ADD_EXERCISE_TO_WORKOUT'; payload: { exercise: any; config: any } };

const initialState: { currentWorkout: WorkoutSession | null } = {
    currentWorkout: null,
};

function workoutReducer(
    state: { currentWorkout: WorkoutSession | null },
    action: WorkoutAction
): { currentWorkout: WorkoutSession | null } {
    switch (action.type) {
        case 'START_WORKOUT': {
            const {exercises, date} = action.payload;

            console.log('Starting workout with', exercises.length, 'exercises');

            // Robust exercise conversion with proper type handling
            const workoutExercises: WorkoutExercise[] = exercises.map((scheduledExercise, index) => {
                // Validate and log exercise configuration
                validateScheduledExercise(scheduledExercise);

                // Get appropriate configuration for this exercise type
                const setsCount = getExerciseSetsCount(scheduledExercise);
                const targetReps = getExerciseTargetReps(scheduledExercise);
                const restSeconds = getExerciseRestSeconds(scheduledExercise);
                const weightUnit = getExerciseWeightUnit(scheduledExercise);

                // Create workout sets based on exercise type and configuration
                const sets: WorkoutSet[] = Array.from({length: setsCount}, (_, setIndex) => ({
                    id: `${scheduledExercise.id}-set-${setIndex + 1}`,
                    setNumber: setIndex + 1,
                    targetReps,
                    targetWeight: scheduledExercise.targetWeight,
                    targetWeightUnit: weightUnit,
                    targetRpe: scheduledExercise.targetRpe,
                    restSeconds,
                    completed: false,
                }));

                console.log(`Created ${sets.length} sets for "${scheduledExercise.exercise.name || scheduledExercise.exercise.exerciseName}"`);

                return {
                    id: scheduledExercise.id,
                    scheduledExercise,
                    sets,
                    completed: false,
                    skipped: false,
                };
            });

            const newWorkout: WorkoutSession = {
                id: `workout-${Date.now()}`,
                date,
                exercises: workoutExercises,
                status: 'in_progress',
                startedAt: new Date(),
                currentExerciseIndex: 0,
                currentSetIndex: 0,
            };

            console.log('Workout session created:', {
                id: newWorkout.id,
                exerciseCount: workoutExercises.length,
                totalSets: workoutExercises.reduce((total, ex) => total + ex.sets.length, 0)
            });

            return {currentWorkout: newWorkout};
        }

        case 'PAUSE_WORKOUT': {
            if (!state.currentWorkout) return state;

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    status: 'paused',
                    pausedAt: new Date(),
                },
            };
        }

        case 'RESUME_WORKOUT': {
            if (!state.currentWorkout) return state;

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    status: 'in_progress',
                    pausedAt: undefined,
                },
            };
        }

        case 'COMPLETE_WORKOUT': {
            return {currentWorkout: null};
        }

        case 'CANCEL_WORKOUT': {
            return {currentWorkout: null};
        }

        case 'GO_TO_EXERCISE': {
            if (!state.currentWorkout) return state;

            const exerciseIndex = action.payload;
            if (exerciseIndex < 0 || exerciseIndex >= state.currentWorkout.exercises.length) {
                return state;
            }

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    currentExerciseIndex: exerciseIndex,
                    currentSetIndex: 0,
                },
            };
        }

        case 'NEXT_EXERCISE': {
            if (!state.currentWorkout) return state;

            const nextIndex = state.currentWorkout.currentExerciseIndex + 1;
            if (nextIndex >= state.currentWorkout.exercises.length) {
                return state;
            }

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    currentExerciseIndex: nextIndex,
                    currentSetIndex: 0,
                },
            };
        }

        case 'PREVIOUS_EXERCISE': {
            if (!state.currentWorkout) return state;

            const prevIndex = state.currentWorkout.currentExerciseIndex - 1;
            if (prevIndex < 0) {
                return state;
            }

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    currentExerciseIndex: prevIndex,
                    currentSetIndex: 0,
                },
            };
        }

        case 'COMPLETE_SET': {
            if (!state.currentWorkout) return state;

            const {setId, actualData} = action.payload;

            const updatedExercises = state.currentWorkout.exercises.map(exercise => ({
                ...exercise,
                sets: exercise.sets.map(set =>
                    set.id === setId
                        ? {
                            ...set,
                            ...actualData,
                            completed: true,
                            completedAt: new Date(),
                        }
                        : set
                ),
            }));

            // Check if current exercise is completed
            const currentExercise = updatedExercises[state.currentWorkout.currentExerciseIndex];
            const allSetsCompleted = currentExercise.sets.every(set => set.completed);

            if (allSetsCompleted) {
                updatedExercises[state.currentWorkout.currentExerciseIndex].completed = true;
                updatedExercises[state.currentWorkout.currentExerciseIndex].completedAt = new Date();
            }

            // Auto-advance to next set
            const currentSet = currentExercise.sets[state.currentWorkout.currentSetIndex];
            let newSetIndex = state.currentWorkout.currentSetIndex;

            if (currentSet.id === setId && !allSetsCompleted) {
                newSetIndex = state.currentWorkout.currentSetIndex + 1;
            }

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    exercises: updatedExercises,
                    currentSetIndex: newSetIndex,
                },
            };
        }

        case 'SKIP_SET': {
            if (!state.currentWorkout) return state;

            const setId = action.payload;
            const currentExercise = state.currentWorkout.exercises[state.currentWorkout.currentExerciseIndex];
            const setIndex = currentExercise.sets.findIndex(set => set.id === setId);

            if (setIndex === -1) return state;

            const updatedExercises = state.currentWorkout.exercises.map((exercise, exerciseIndex) =>
                exerciseIndex === state.currentWorkout!.currentExerciseIndex
                    ? {
                        ...exercise,
                        sets: exercise.sets.map(set =>
                            set.id === setId
                                ? {...set, completed: true, actualReps: 0}
                                : set
                        ),
                    }
                    : exercise
            );

            // Auto-advance to next set
            const newSetIndex = Math.min(
                state.currentWorkout.currentSetIndex + 1,
                currentExercise.sets.length - 1
            );

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    exercises: updatedExercises,
                    currentSetIndex: newSetIndex,
                },
            };
        }

        case 'ADD_SET': {
            if (!state.currentWorkout) return state;

            const exerciseId = action.payload;
            const exerciseIndex = state.currentWorkout.exercises.findIndex(ex => ex.id === exerciseId);

            if (exerciseIndex === -1) return state;

            const exercise = state.currentWorkout.exercises[exerciseIndex];
            const newSetNumber = exercise.sets.length + 1;
            const lastSet = exercise.sets[exercise.sets.length - 1];

            // Create new set with proper defaults and weight unit
            const newSet: WorkoutSet = {
                id: `${exerciseId}-set-${newSetNumber}`,
                setNumber: newSetNumber,
                targetReps: lastSet?.targetReps || getExerciseTargetReps(exercise.scheduledExercise),
                targetWeight: lastSet?.targetWeight || exercise.scheduledExercise.targetWeight,
                targetWeightUnit: lastSet?.targetWeightUnit || getExerciseWeightUnit(exercise.scheduledExercise),
                targetRpe: lastSet?.targetRpe || exercise.scheduledExercise.targetRpe,
                restSeconds: lastSet?.restSeconds || getExerciseRestSeconds(exercise.scheduledExercise),
                completed: false,
            };

            const updatedExercises = state.currentWorkout.exercises.map((ex, index) =>
                index === exerciseIndex
                    ? {...ex, sets: [...ex.sets, newSet]}
                    : ex
            );

            console.log('Added new set to exercise:', {
                exerciseId,
                setNumber: newSetNumber,
                targetReps: newSet.targetReps
            });

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    exercises: updatedExercises,
                },
            };
        }

        case 'REMOVE_SET': {
            if (!state.currentWorkout) return state;

            const setId = action.payload;

            const updatedExercises = state.currentWorkout.exercises.map(exercise => {
                const filteredSets = exercise.sets.filter(set => set.id !== setId);

                // Re-number sets to maintain sequential order
                const renumberedSets = filteredSets.map((set, index) => ({
                    ...set,
                    setNumber: index + 1,
                    id: set.id.includes('-set-')
                        ? set.id.replace(/-set-\d+$/, `-set-${index + 1}`)
                        : `${exercise.id}-set-${index + 1}`
                }));

                return {
                    ...exercise,
                    sets: renumberedSets
                };
            });

            // Adjust current set index if needed
            const currentExercise = updatedExercises[state.currentWorkout.currentExerciseIndex];
            const newSetIndex = Math.min(
                state.currentWorkout.currentSetIndex,
                currentExercise.sets.length - 1
            );

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    exercises: updatedExercises,
                    currentSetIndex: Math.max(0, newSetIndex),
                },
            };
        }

        case 'SKIP_EXERCISE': {
            if (!state.currentWorkout) return state;

            const exerciseId = action.payload;

            const updatedExercises = state.currentWorkout.exercises.map(exercise =>
                exercise.id === exerciseId
                    ? {
                        ...exercise,
                        skipped: true,
                        completed: true,
                        completedAt: new Date(),
                        sets: exercise.sets.map(set => ({...set, completed: true})),
                    }
                    : exercise
            );

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    exercises: updatedExercises,
                },
            };
        }

        case 'COMPLETE_EXERCISE': {
            if (!state.currentWorkout) return state;

            const exerciseId = action.payload;

            const updatedExercises = state.currentWorkout.exercises.map(exercise =>
                exercise.id === exerciseId
                    ? {
                        ...exercise,
                        completed: true,
                        completedAt: new Date(),
                        sets: exercise.sets.map(set => ({...set, completed: true})),
                    }
                    : exercise
            );

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    exercises: updatedExercises,
                },
            };
        }

        case 'ADD_EXERCISE_TO_WORKOUT': {
            if (!state.currentWorkout) return state;

            const {exercise, config} = action.payload;

            console.log('Adding exercise to current workout:', exercise.name || exercise.exerciseName, config);

            // Robust configuration handling with defaults and weight units
            const setsCount = config.targetSets || (exercise.isCardio ? 1 : exercise.isIsometric ? 3 : 3);
            const targetReps = config.targetReps || (exercise.isCardio ? config.targetDurationMinutes || 20 :
                exercise.isIsometric ? config.holdDurationSeconds || 30 : 10);
            const restSeconds = config.restSeconds || (exercise.isCardio ? 0 : exercise.isIsometric ? 60 : 90);
            const weightUnit = config.targetWeightUnit || 'lbs';

            // Create sets based on configuration
            const sets: WorkoutSet[] = Array.from({length: setsCount}, (_, setIndex) => ({
                id: `added-exercise-${Date.now()}-set-${setIndex + 1}`,
                setNumber: setIndex + 1,
                targetReps,
                targetWeight: config.targetWeight ? parseFloat(config.targetWeight) : undefined,
                targetWeightUnit: weightUnit,
                targetRpe: config.targetRpe,
                restSeconds,
                completed: false,
            }));

            // Create a ScheduledExercise object to match your existing structure
            const scheduledExercise: ScheduledExercise = {
                id: `scheduled-${Date.now()}`,
                exerciseId: exercise.id,
                exercise: exercise,
                scheduledDate: state.currentWorkout.date,
                targetSets: setsCount,
                targetReps: targetReps,
                targetWeight: config.targetWeight ? parseFloat(config.targetWeight) : undefined,
                targetWeightUnit: weightUnit,
                restSeconds,
                targetRpe: config.targetRpe,
                notes: config.notes,
                completed: false,
                createdAt: new Date().toISOString(),
                userId: 'current_user'
            };

            // Create the workout exercise
            const workoutExercise: WorkoutExercise = {
                id: `workout-exercise-${Date.now()}`,
                scheduledExercise,
                sets,
                completed: false,
                skipped: false,
            };

            console.log('Created workout exercise with', sets.length, 'sets');

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    exercises: [...state.currentWorkout.exercises, workoutExercise],
                },
            };
        }

        case 'UPDATE_DURATION': {
            if (!state.currentWorkout || !state.currentWorkout.startedAt) return state;

            return {
                currentWorkout: {
                    ...state.currentWorkout,
                    totalDurationMinutes: Math.round(
                        (Date.now() - state.currentWorkout.startedAt.getTime()) / 1000 / 60
                    ),
                },
            };
        }

        default:
            return state;
    }
}

// ==================== HELPER FUNCTIONS ====================

/**
 * Calculate exercise duration in minutes
 */
function calculateExerciseDuration(exercise: any): number {
    if (exercise.actualDurationMinutes) {
        return exercise.actualDurationMinutes;
    }

    // Calculate from sets if available
    const totalSetTime = exercise.sets
        .filter((set: any) => set.completed)
        .reduce((total: number, set: any) => {
            const setDuration = set.actualDurationMinutes || set.targetDurationMinutes || 0;
            const restTime = set.restSeconds ? Math.round(set.restSeconds / 60) : 0;
            return total + setDuration + restTime;
        }, 0);

    return totalSetTime > 0 ? totalSetTime : 30; // Default 30 minutes
}

/**
 * Enhanced performance rating calculation with multi-criteria evaluation
 */
function calculateEnhancedPerformanceRating(exercise: WorkoutExercise): 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL' {
    const completedSets = exercise.sets.filter(set => set.completed);
    const scheduledExercise = exercise.scheduledExercise;

    if (completedSets.length === 0) return 'PARTIAL';

    // Multi-criteria evaluation
    const criteria: { name: string; achieved: boolean; weight: number }[] = [];

    // Check set completion (required criterion)
    criteria.push({
        name: 'sets',
        achieved: completedSets.length >= (scheduledExercise.targetSets || 1),
        weight: 0.3
    });

    // Check reps/duration achievement based on exercise type
    if (scheduledExercise.exercise.isCardio) {
        // For cardio, check duration
        const targetDuration = scheduledExercise.targetDurationMinutes || scheduledExercise.exercise.estimatedDurationMinutes || 20;
        const actualDuration = completedSets.reduce((total, set) =>
            total + (set.actualDurationMinutes || targetDuration), 0);

        criteria.push({
            name: 'duration',
            achieved: actualDuration >= targetDuration,
            weight: 0.4
        });

        // Check distance if specified
        if (scheduledExercise.targetDistance || scheduledExercise.targetDistanceKm) {
            const targetDistance = scheduledExercise.targetDistance ||
                (scheduledExercise.targetDistanceKm ? scheduledExercise.targetDistanceKm * 0.621371 : 0);
            // For now, assume distance was met if duration was met (would need actual tracking)
            criteria.push({
                name: 'distance',
                achieved: actualDuration >= targetDuration, // Proxy for distance achievement
                weight: 0.2
            });
        }
    } else if (scheduledExercise.exercise.isIsometric) {
        // For isometric, check hold duration
        const targetHold = scheduledExercise.holdDurationSeconds || 30;
        const actualHolds = completedSets.reduce((total, set) =>
            total + (set.actualHoldSeconds || targetHold), 0);
        const expectedHolds = completedSets.length * targetHold;

        criteria.push({
            name: 'hold_duration',
            achieved: actualHolds >= expectedHolds,
            weight: 0.4
        });
    } else {
        // For strength, check reps
        const targetReps = scheduledExercise.targetReps || 10;
        const repsAchieved = completedSets.every(set =>
            (set.actualReps || 0) >= targetReps);

        criteria.push({
            name: 'reps',
            achieved: repsAchieved,
            weight: 0.4
        });

        // Check weight if specified
        if (scheduledExercise.targetWeight) {
            const weightAchieved = completedSets.every(set =>
                (set.actualWeight || 0) >= scheduledExercise.targetWeight!);

            criteria.push({
                name: 'weight',
                achieved: weightAchieved,
                weight: 0.2
            });
        }
    }

    // Calculate weighted achievement score
    const totalWeight = criteria.reduce((sum, c) => sum + c.weight, 0);
    const achievedWeight = criteria
        .filter(c => c.achieved)
        .reduce((sum, c) => sum + c.weight, 0);

    const achievementRatio = achievedWeight / totalWeight;

    // Determine rating based on achievement ratio
    if (achievementRatio >= 1.0) {
        // Check if exceeded any targets
        const exceeded = completedSets.some(set => {
            if (scheduledExercise.exercise.isCardio) {
                const targetDuration = scheduledExercise.targetDurationMinutes || 20;
                return (set.actualDurationMinutes || 0) > targetDuration * 1.1;
            } else if (scheduledExercise.exercise.isIsometric) {
                const targetHold = scheduledExercise.holdDurationSeconds || 30;
                return (set.actualHoldSeconds || 0) > targetHold * 1.1;
            } else {
                const targetReps = typeof scheduledExercise.targetReps === 'number'
                    ? scheduledExercise.targetReps
                    : (typeof scheduledExercise.targetReps === 'string'
                        ? parseInt(scheduledExercise.targetReps, 10)
                        : 10);
                const targetWeight = scheduledExercise.targetWeight || 0;
                return (set.actualReps || 0) > targetReps * 1.1 ||
                    (targetWeight > 0 && (set.actualWeight || 0) > targetWeight * 1.05);
            }
        });
        return exceeded ? 'EXCEEDED' : 'MET';
    } else if (achievementRatio >= 0.8) {
        return 'BELOW_TARGET';
    } else {
        return 'PARTIAL';
    }
}

/**
 * Calculate overall effort for enhanced tracking
 */
function calculateOverallEffort(exercise: WorkoutExercise): number {
    const completedSets = exercise.sets.filter(set => set.completed);
    if (completedSets.length === 0) return 5;

    // Average RPE if available
    const rpeSets = completedSets.filter(set => set.actualRpe || set.targetRpe);
    if (rpeSets.length > 0) {
        const avgRpe = rpeSets.reduce((sum, set) =>
            sum + (set.actualRpe || set.targetRpe || 7), 0) / rpeSets.length;
        return Math.round(avgRpe * 10) / 10; // Round to 1 decimal
    }

    return 7.0; // Default moderate effort
}

/**
 * Generate enhanced performance summary
 */
function generateEnhancedPerformanceSummary(exercise: WorkoutExercise): string {
    const completedSets = exercise.sets.filter(set => set.completed);
    const exerciseName = exercise.scheduledExercise.exercise.name;
    const scheduledExercise = exercise.scheduledExercise;

    if (completedSets.length === 0) {
        return `Started ${exerciseName} but did not complete any sets`;
    }

    const summary: string[] = [];
    summary.push(`${completedSets.length}/${exercise.sets.length} sets completed`);

    if (scheduledExercise.exercise.isCardio) {
        const totalDuration = completedSets.reduce((total, set) =>
            total + (set.actualDurationMinutes || 0), 0);
        if (totalDuration > 0) {
            summary.push(`${totalDuration} minutes total`);
        }
    } else if (scheduledExercise.exercise.isIsometric) {
        const totalHoldTime = completedSets.reduce((total, set) =>
            total + (set.actualHoldSeconds || 0), 0);
        if (totalHoldTime > 0) {
            summary.push(`${totalHoldTime}s total hold time`);
        }
    } else {
        const totalReps = completedSets.reduce((total, set) =>
            total + (set.actualReps || 0), 0);
        summary.push(`${totalReps} total reps`);

        const weights = completedSets
            .map(set => set.actualWeight || set.targetWeight)
            .filter((w): w is number => w !== undefined && w > 0);
        if (weights.length > 0) {
            const maxWeight = Math.max(...weights);
            summary.push(`${maxWeight} lbs max weight`);
        }
    }

    return `${exerciseName}: ${summary.join(', ')}`;
}

/**
 * Calculate estimated calories
 */
function calculateEstimatedCalories(exercise: any): number {
    const duration = calculateExerciseDuration(exercise);
    const exerciseType = exercise.scheduledExercise.exercise;

    // Simple calorie estimation based on exercise type and duration
    let caloriesPerMinute = 5; // Default moderate activity

    if (exerciseType.isCardio) {
        caloriesPerMinute = 8; // Higher for cardio
    } else if (exerciseType.isIsometric) {
        caloriesPerMinute = 4; // Lower for isometric
    } else {
        caloriesPerMinute = 6; // Strength training
    }

    return Math.round(duration * caloriesPerMinute);
}

/**
 * Save workout results to backend with enhanced performance tracking
 */
async function saveWorkoutResultsToBackend(completedWorkout: WorkoutSession): Promise<boolean> {
    try {
        console.log('Saving workout results to backend with full performance tracking...');

        // Process each completed exercise
        const savePromises = completedWorkout.exercises
            .filter(exercise => exercise.completed || exercise.sets.some(set => set.completed))
            .map(async (exercise) => {
                try {
                    // Enhanced completion data with comprehensive performance evaluation
                    const completionData = {
                        exerciseId: exercise.scheduledExercise.id,
                        scheduledExerciseId: exercise.scheduledExercise.id,
                        completedAt: (exercise.completedAt || completedWorkout.completedAt || new Date()).toISOString(),
                        totalDurationMinutes: calculateExerciseDuration(exercise),
                        notes: exercise.notes || completedWorkout.notes || '',
                        performanceRating: calculateEnhancedPerformanceRating(exercise),

                        // Include all completed sets with detailed data
                        sets: exercise.sets
                            .filter(set => set.completed)
                            .map(set => ({
                                setNumber: set.setNumber,
                                targetReps: set.targetReps || 0,
                                actualReps: set.actualReps || 0,
                                targetWeight: set.targetWeight,
                                actualWeight: set.actualWeight || set.targetWeight,
                                targetWeightUnit: set.targetWeightUnit || 'lbs',
                                rpe: set.actualRpe || set.targetRpe || 7,
                                restSeconds: set.restSeconds || 90,
                                completed: set.completed,
                                actualDurationMinutes: set.actualDurationMinutes,
                                actualHoldSeconds: set.actualHoldSeconds,
                                notes: set.notes || ''
                            })),

                        // Enhanced performance tracking
                        difficultyRating: 5,
                        overallEffort: calculateOverallEffort(exercise),
                        mood: 'FOCUSED',
                        location: 'HOME',
                        workoutFeedback: '',
                        performanceSummary: generateEnhancedPerformanceSummary(exercise),

                        // Cardio-specific data if applicable
                        distanceKm: exercise.scheduledExercise.targetDistanceKm,
                        caloriesBurned: calculateEstimatedCalories(exercise),

                        // Always provide arrays (backend will populate with actual data)
                        personalRecords: [],
                        improvements: []
                    };

                    // Use the enhanced completion endpoint
                    await calendarApi.markExerciseCompletedWithPerformance(
                        exercise.scheduledExercise.id,
                        completionData
                    );

                    console.log(`Successfully saved exercise ${exercise.scheduledExercise.exercise.name}`);
                    return {success: true, exerciseId: exercise.id};

                } catch (error) {
                    console.error(`Failed to save results for exercise ${exercise.id}:`, error);

                    // Store failed save for retry
                    storeFailedSaveForRetry(exercise, completedWorkout);

                    return {success: false, exerciseId: exercise.id, error};
                }
            });

        // Wait for all saves to complete
        const results = await Promise.allSettled(savePromises);

        const successful = results.filter(result =>
            result.status === 'fulfilled' && result.value.success
        ).length;

        const failed = results.filter(result =>
            result.status === 'rejected' ||
            (result.status === 'fulfilled' && !result.value.success)
        ).length;

        if (failed > 0) {
            console.warn(`Some exercises failed to save: ${failed} failed, ${successful} succeeded`);
            // Still return true if at least some succeeded
            return successful > 0;
        }

        console.log(`Successfully saved all ${successful} completed exercises`);
        return true;

    } catch (error) {
        console.error('Failed to save workout results to backend:', error);

        // Store entire workout for retry
        storeWorkoutForRetry(completedWorkout);

        return false;
    }
}

/**
 * Store failed save for retry
 */
function storeFailedSaveForRetry(exercise: WorkoutExercise, workoutSession: WorkoutSession): void {
    try {
        const pendingSaves = JSON.parse(localStorage.getItem('pendingWorkoutSaves') || '[]');
        pendingSaves.push({
            exerciseId: exercise.scheduledExercise.id,
            exercise: exercise,
            workoutSession: workoutSession,
            timestamp: new Date().toISOString(),
            retryCount: 0
        });
        localStorage.setItem('pendingWorkoutSaves', JSON.stringify(pendingSaves));
        console.log('Stored failed exercise save for retry');
    } catch (error) {
        console.error('Failed to store failed save for retry:', error);
    }
}

/**
 * Store entire workout for retry
 */
function storeWorkoutForRetry(completedWorkout: WorkoutSession): void {
    try {
        const pendingSaves = JSON.parse(localStorage.getItem('pendingWorkoutSaves') || '[]');
        pendingSaves.push({
            workout: completedWorkout,
            timestamp: new Date().toISOString(),
            retryCount: 0
        });
        localStorage.setItem('pendingWorkoutSaves', JSON.stringify(pendingSaves));
        console.log('Stored entire workout for retry');
    } catch (error) {
        console.error('Failed to store workout for retry:', error);
    }
}

/**
 * Save workout to local history
 */
function saveWorkoutToHistory(workout: WorkoutSession) {
    try {
        const history = JSON.parse(localStorage.getItem('workoutHistory') || '[]');
        history.push({
            ...workout,
            // Serialize dates for storage
            startedAt: workout.startedAt?.toISOString(),
            completedAt: workout.completedAt?.toISOString(),
            exercises: workout.exercises.map(exercise => ({
                ...exercise,
                startedAt: exercise.startedAt?.toISOString(),
                completedAt: exercise.completedAt?.toISOString(),
                sets: exercise.sets.map(set => ({
                    ...set,
                    completedAt: set.completedAt?.toISOString(),
                })),
            })),
        });
        localStorage.setItem('workoutHistory', JSON.stringify(history));
        console.log('Workout saved to history:', workout.id);
    } catch (error) {
        console.error('Failed to save workout to history:', error);
    }
}

// ==================== CONTEXT ====================

const WorkoutContext = createContext<WorkoutContextType | undefined>(undefined);

interface WorkoutProviderProps {
    children: ReactNode;
}

export function WorkoutProvider({children}: WorkoutProviderProps) {
    const [state, dispatch] = useReducer(workoutReducer, initialState);
    const [completingExercises, setCompletingExercises] = useState<Set<string>>(new Set());

    // ==================== WORKOUT CONTROLS ====================

    const startWorkout = (exercises: ScheduledExercise[], date: string) => {
        console.log('Starting workout with', exercises.length, 'exercises');

        // Enhanced validation with detailed error reporting
        const validationResults = exercises.map((ex, index) => {
            const issues = [];

            if (!ex.exercise) {
                issues.push('Missing exercise object');
            } else {
                if (!ex.exercise.id) issues.push('Missing exercise ID');
                if (!ex.exercise.name && !ex.exercise.exerciseName) issues.push('Missing exercise name');
                if (!ex.exercise.exerciseType) issues.push('Missing exercise type');
            }

            if (!ex.id) issues.push('Missing scheduled exercise ID');
            if (!ex.scheduledDate) issues.push('Missing scheduled date');

            return {
                index,
                exercise: ex,
                issues,
                isValid: issues.length === 0
            };
        });

        const validExercises = validationResults
            .filter(result => result.isValid)
            .map(result => result.exercise);

        const invalidExercises = validationResults.filter(result => !result.isValid);

        // Log validation results
        if (invalidExercises.length > 0) {
            console.warn('Found invalid exercises:', invalidExercises.map(invalid => ({
                index: invalid.index + 1,
                issues: invalid.issues,
                exerciseName: invalid.exercise.exercise?.name || 'Unknown'
            })));
        }

        if (validExercises.length === 0) {
            console.error('No valid exercises found for workout');
            throw new Error('No valid exercises available for workout. Please check your scheduled exercises.');
        }

        if (validExercises.length !== exercises.length) {
            console.warn(`Starting workout with ${validExercises.length} of ${exercises.length} exercises (${invalidExercises.length} were invalid)`);
        }

        // Log successful exercises
        console.log('Valid exercises for workout:', validExercises.map((ex, index) => ({
            index: index + 1,
            name: ex.exercise.name || ex.exercise.exerciseName,
            type: ex.exercise.isCardio ? 'cardio' : ex.exercise.isIsometric ? 'isometric' : 'strength',
            sets: ex.targetSets || 'auto',
            reps: ex.targetReps || 'auto'
        })));

        // Start the workout
        dispatch({type: 'START_WORKOUT', payload: {exercises: validExercises, date}});
    };

    const pauseWorkout = () => {
        console.log('Pausing workout');
        dispatch({type: 'PAUSE_WORKOUT'});
    };

    const resumeWorkout = () => {
        console.log('Resuming workout');
        dispatch({type: 'RESUME_WORKOUT'});
    };

    const completeWorkout = useCallback(async () => {
        if (!state.currentWorkout) return;

        const workoutId = state.currentWorkout.id;

        // Prevent duplicate completion attempts
        if (completingExercises.has(workoutId)) {
            console.log('Workout completion already in progress:', workoutId);
            return;
        }

        setCompletingExercises(prev => new Set(prev).add(workoutId));

        try {
            console.log('Starting workout completion process...');

            const completedWorkout = {
                ...state.currentWorkout,
                status: 'completed' as const,
                completedAt: new Date(),
                totalDurationMinutes: state.currentWorkout.startedAt
                    ? Math.round((Date.now() - state.currentWorkout.startedAt.getTime()) / 1000 / 60)
                    : 0,
            };

            // Wait for backend save to complete
            const saveSuccessful = await saveWorkoutResultsToBackend(completedWorkout);

            if (saveSuccessful) {
                console.log('Workout results saved successfully');

                // Set session storage flags AFTER successful save
                sessionStorage.setItem('workoutJustCompleted', 'true');
                sessionStorage.setItem('completedWorkoutDate', completedWorkout.date);
            } else {
                console.warn('Some workout data may not have saved properly');
                sessionStorage.setItem('workoutNeedsRetry', 'true');
            }

            // Save to local storage for history regardless of backend success
            saveWorkoutToHistory(completedWorkout);

            // Clear the workout state
            dispatch({type: 'COMPLETE_WORKOUT'});

        } catch (error) {
            console.error('Error in workout completion process:', error);
            sessionStorage.setItem('workoutNeedsRetry', 'true');

            // Still allow completion but flag for retry
            dispatch({type: 'COMPLETE_WORKOUT'});
        } finally {
            setCompletingExercises(prev => {
                const newSet = new Set(prev);
                newSet.delete(workoutId);
                return newSet;
            });
        }
    }, [state.currentWorkout, completingExercises]);

    const cancelWorkout = () => {
        console.log('Cancelling workout');
        dispatch({type: 'CANCEL_WORKOUT'});
    };

    // ==================== NAVIGATION ====================

    const goToNextExercise = () => {
        dispatch({type: 'NEXT_EXERCISE'});
    };

    const goToPreviousExercise = () => {
        dispatch({type: 'PREVIOUS_EXERCISE'});
    };

    const goToExercise = (exerciseIndex: number) => {
        dispatch({type: 'GO_TO_EXERCISE', payload: exerciseIndex});
    };

    // ==================== SET MANAGEMENT ====================

    const completeSet = (setId: string, actualData: Partial<WorkoutSet>) => {
        console.log('Completing set:', setId, actualData);
        dispatch({type: 'COMPLETE_SET', payload: {setId, actualData}});
    };

    const skipSet = (setId: string) => {
        console.log('Skipping set:', setId);
        dispatch({type: 'SKIP_SET', payload: setId});
    };

    const addSet = (exerciseId: string) => {
        console.log('Adding set to exercise:', exerciseId);
        dispatch({type: 'ADD_SET', payload: exerciseId});
    };

    const removeSet = (setId: string) => {
        console.log('Removing set:', setId);
        dispatch({type: 'REMOVE_SET', payload: setId});
    };

    // ==================== EXERCISE MANAGEMENT ====================

    const skipExercise = (exerciseId: string) => {
        console.log('Skipping exercise:', exerciseId);
        dispatch({type: 'SKIP_EXERCISE', payload: exerciseId});
    };

    const completeExercise = (exerciseId: string) => {
        console.log('Completing exercise:', exerciseId);
        dispatch({type: 'COMPLETE_EXERCISE', payload: exerciseId});
    };

    const addExerciseToCurrentWorkout = (exercise: any, config: any) => {
        console.log('Adding exercise to current workout:', exercise.name || exercise.exerciseName, config);

        // Validate exercise and config before adding
        if (!exercise || !config) {
            console.error('Invalid exercise or config provided:', {exercise, config});
            return;
        }

        dispatch({type: 'ADD_EXERCISE_TO_WORKOUT', payload: {exercise, config}});
    };

    // ==================== SESSION DATA ====================

    const getTotalDuration = (): number => {
        if (!state.currentWorkout?.startedAt) return 0;
        return Math.round((Date.now() - state.currentWorkout.startedAt.getTime()) / 1000 / 60);
    };

    const getCurrentExercise = (): WorkoutExercise | null => {
        if (!state.currentWorkout) return null;
        return state.currentWorkout.exercises[state.currentWorkout.currentExerciseIndex] || null;
    };

    const getCurrentSet = (): WorkoutSet | null => {
        const currentExercise = getCurrentExercise();
        if (!currentExercise || !state.currentWorkout) return null;
        return currentExercise.sets[state.currentWorkout.currentSetIndex] || null;
    };

    const getCompletionPercentage = (): number => {
        if (!state.currentWorkout) return 0;

        const totalSets = state.currentWorkout.exercises.reduce((total, exercise) =>
            total + exercise.sets.length, 0
        );

        const completedSets = state.currentWorkout.exercises.reduce((total, exercise) =>
            total + exercise.sets.filter(set => set.completed).length, 0
        );

        return totalSets > 0 ? Math.round((completedSets / totalSets) * 100) : 0;
    };

    // ==================== STATE QUERIES ====================

    const isWorkoutActive = state.currentWorkout !== null;
    const isPaused = state.currentWorkout?.status === 'paused';

    const canGoNext = state.currentWorkout
        ? state.currentWorkout.currentExerciseIndex < state.currentWorkout.exercises.length - 1
        : false;

    const canGoPrevious = state.currentWorkout
        ? state.currentWorkout.currentExerciseIndex > 0
        : false;

    // ==================== DURATION TIMER ====================

    useEffect(() => {
        if (!isWorkoutActive || isPaused) return;

        const interval = setInterval(() => {
            dispatch({type: 'UPDATE_DURATION'});
        }, 60000); // Update every minute

        return () => clearInterval(interval);
    }, [isWorkoutActive, isPaused]);

    // ==================== CONTEXT VALUE ====================

    const contextValue: WorkoutContextType = {
        currentWorkout: state.currentWorkout,
        startWorkout,
        pauseWorkout,
        resumeWorkout,
        completeWorkout,
        cancelWorkout,
        goToNextExercise,
        goToPreviousExercise,
        goToExercise,
        completeSet,
        skipSet,
        addSet,
        removeSet,
        skipExercise,
        completeExercise,
        addExerciseToCurrentWorkout,
        getTotalDuration,
        getCurrentExercise,
        getCurrentSet,
        getCompletionPercentage,
        isWorkoutActive,
        isPaused,
        canGoNext,
        canGoPrevious,
    };

    return (
        <WorkoutContext.Provider value={contextValue}>
            {children}
        </WorkoutContext.Provider>
    );
}

// ==================== HOOK ====================

export function useWorkout(): WorkoutContextType {
    const context = useContext(WorkoutContext);
    if (context === undefined) {
        throw new Error('useWorkout must be used within a WorkoutProvider');
    }
    return context;
}