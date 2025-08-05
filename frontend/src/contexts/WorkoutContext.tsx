import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { ScheduledExercise } from '../types/exercise';

// ==================== TYPES ====================

export interface WorkoutSet {
    id: string;
    setNumber: number;
    targetReps: string;
    actualReps?: number;
    targetWeight?: number;
    actualWeight?: number;
    targetRpe?: number;
    actualRpe?: number;
    restSeconds?: number;
    completed: boolean;
    notes?: string;
    completedAt?: Date;
}

export interface WorkoutExercise {
    id: string;
    scheduledExercise: ScheduledExercise;
    sets: WorkoutSet[];
    completed: boolean;
    skipped: boolean;
    startedAt?: Date;
    completedAt?: Date;
    notes?: string;
}

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

// ==================== HELPER FUNCTIONS ====================

/**
 * ✅ NEW: Determines appropriate number of sets based on exercise type and scheduled data
 */
const getExerciseSetsCount = (scheduledExercise: ScheduledExercise): number => {
    const exercise = scheduledExercise.exercise;

    // If sets are explicitly defined in scheduled exercise, use that
    if (scheduledExercise.sets && scheduledExercise.sets > 0) {
        return scheduledExercise.sets;
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
 * ✅ NEW: Gets appropriate target reps based on exercise type
 */
const getExerciseTargetReps = (scheduledExercise: ScheduledExercise): string => {
    const exercise = scheduledExercise.exercise;

    // If reps are explicitly defined, use them
    if (scheduledExercise.reps) {
        return scheduledExercise.reps;
    }

    // Otherwise, provide sensible defaults based on exercise type
    if (exercise.isCardio) {
        return `${exercise.estimatedDurationMinutes || 20} min`; // Show duration for cardio
    } else if (exercise.isIsometric) {
        return `${scheduledExercise.holdDurationSeconds || 30}s hold`; // Show hold duration
    } else {
        return '8-12'; // Standard rep range for strength exercises
    }
};

/**
 * ✅ NEW: Gets appropriate rest seconds based on exercise type
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

/**
 * ✅ NEW: Validates exercise configuration and logs warnings for missing data
 */
const validateScheduledExercise = (scheduledExercise: ScheduledExercise): void => {
    const exercise = scheduledExercise.exercise;
    const exerciseName = exercise.name || exercise.exerciseName || 'Unknown Exercise';

    // Log missing configuration data (helps with debugging)
    if (exercise.isCardio && !scheduledExercise.targetDurationMinutes && !exercise.estimatedDurationMinutes) {
        console.warn(`⚠️ Cardio exercise "${exerciseName}" missing duration information`);
    }

    if (exercise.isIsometric && !scheduledExercise.holdDurationSeconds) {
        console.warn(`⚠️ Isometric exercise "${exerciseName}" missing hold duration`);
    }

    if (!exercise.isCardio && !exercise.isIsometric && !scheduledExercise.sets) {
        console.warn(`⚠️ Strength exercise "${exerciseName}" missing sets configuration`);
    }

    // Log what we're using for the workout
    console.log(`🏋️‍♂️ Exercise "${exerciseName}" workout config:`, {
        type: exercise.isCardio ? 'cardio' : exercise.isIsometric ? 'isometric' : 'strength',
        sets: getExerciseSetsCount(scheduledExercise),
        reps: getExerciseTargetReps(scheduledExercise),
        rest: getExerciseRestSeconds(scheduledExercise),
        weight: scheduledExercise.weight,
        notes: scheduledExercise.notes
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
            const { exercises, date } = action.payload;

            console.log('🏋️‍♂️ Starting workout with', exercises.length, 'exercises');

            // ✅ FIXED: Robust exercise conversion with proper type handling
            const workoutExercises: WorkoutExercise[] = exercises.map((scheduledExercise, index) => {
                // Validate and log exercise configuration
                validateScheduledExercise(scheduledExercise);

                // Get appropriate configuration for this exercise type
                const setsCount = getExerciseSetsCount(scheduledExercise);
                const targetReps = getExerciseTargetReps(scheduledExercise);
                const restSeconds = getExerciseRestSeconds(scheduledExercise);

                // Create workout sets based on exercise type and configuration
                const sets: WorkoutSet[] = Array.from({ length: setsCount }, (_, setIndex) => ({
                    id: `${scheduledExercise.id}-set-${setIndex + 1}`,
                    setNumber: setIndex + 1,
                    targetReps,
                    targetWeight: scheduledExercise.weight,
                    targetRpe: scheduledExercise.targetRpe,
                    restSeconds,
                    completed: false,
                }));

                console.log(`✅ Created ${sets.length} sets for "${scheduledExercise.exercise.name || scheduledExercise.exercise.exerciseName}"`);

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

            console.log('🎯 Workout session created:', {
                id: newWorkout.id,
                exerciseCount: workoutExercises.length,
                totalSets: workoutExercises.reduce((total, ex) => total + ex.sets.length, 0)
            });

            return { currentWorkout: newWorkout };
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
            if (!state.currentWorkout) return state;

            const completedWorkout = {
                ...state.currentWorkout,
                status: 'completed' as const,
                completedAt: new Date(),
                totalDurationMinutes: state.currentWorkout.startedAt
                    ? Math.round((Date.now() - state.currentWorkout.startedAt.getTime()) / 1000 / 60)
                    : 0,
            };

            // Save to local storage for history
            saveWorkoutToHistory(completedWorkout);

            return { currentWorkout: null };
        }

        case 'CANCEL_WORKOUT': {
            return { currentWorkout: null };
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

            const { setId, actualData } = action.payload;

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
                                ? { ...set, completed: true, actualReps: 0 }
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

            // ✅ FIXED: Create new set with proper defaults
            const newSet: WorkoutSet = {
                id: `${exerciseId}-set-${newSetNumber}`,
                setNumber: newSetNumber,
                targetReps: lastSet?.targetReps || getExerciseTargetReps(exercise.scheduledExercise),
                targetWeight: lastSet?.targetWeight || exercise.scheduledExercise.weight,
                targetRpe: lastSet?.targetRpe || exercise.scheduledExercise.targetRpe,
                restSeconds: lastSet?.restSeconds || getExerciseRestSeconds(exercise.scheduledExercise),
                completed: false,
            };

            const updatedExercises = state.currentWorkout.exercises.map((ex, index) =>
                index === exerciseIndex
                    ? { ...ex, sets: [...ex.sets, newSet] }
                    : ex
            );

            console.log('➕ Added new set to exercise:', {
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
                        sets: exercise.sets.map(set => ({ ...set, completed: true })),
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
                        sets: exercise.sets.map(set => ({ ...set, completed: true })),
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

            const { exercise, config } = action.payload;

            console.log('🏋️‍♂️ Adding exercise to current workout:', exercise.name || exercise.exerciseName, config);

            // ✅ FIXED: Robust configuration handling with defaults
            const setsCount = config.sets || (exercise.isCardio ? 1 : exercise.isIsometric ? 3 : 3);
            const targetReps = config.reps || (exercise.isCardio ? `${config.targetDurationMinutes || 20} min` :
                exercise.isIsometric ? `${config.holdDurationSeconds || 30}s hold` : '8-12');
            const restSeconds = config.restSeconds || (exercise.isCardio ? 0 : exercise.isIsometric ? 60 : 90);

            // Create sets based on configuration
            const sets: WorkoutSet[] = Array.from({ length: setsCount }, (_, setIndex) => ({
                id: `added-exercise-${Date.now()}-set-${setIndex + 1}`,
                setNumber: setIndex + 1,
                targetReps,
                targetWeight: config.weight ? parseFloat(config.weight) : undefined,
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
                sets: setsCount,
                reps: targetReps,
                weight: config.weight ? parseFloat(config.weight) : undefined,
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

            console.log('✅ Created workout exercise with', sets.length, 'sets');

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
        console.log('✅ Workout saved to history:', workout.id);
    } catch (error) {
        console.error('❌ Failed to save workout to history:', error);
    }
}

// ==================== CONTEXT ====================

const WorkoutContext = createContext<WorkoutContextType | undefined>(undefined);

interface WorkoutProviderProps {
    children: ReactNode;
}

export function WorkoutProvider({ children }: WorkoutProviderProps) {
    const [state, dispatch] = useReducer(workoutReducer, initialState);

    // ==================== WORKOUT CONTROLS ====================

    const startWorkout = (exercises: ScheduledExercise[], date: string) => {
        console.log('🏋️‍♂️ Starting workout with', exercises.length, 'exercises');

        // ✅ NEW: Validate exercises before starting workout
        const validExercises = exercises.filter(ex => {
            if (!ex.exercise) {
                console.error('❌ Exercise missing exercise data:', ex);
                return false;
            }
            return true;
        });

        if (validExercises.length === 0) {
            console.error('❌ No valid exercises found for workout');
            return;
        }

        if (validExercises.length !== exercises.length) {
            console.warn(`⚠️ Filtered out ${exercises.length - validExercises.length} invalid exercises`);
        }

        dispatch({ type: 'START_WORKOUT', payload: { exercises: validExercises, date } });
    };

    const pauseWorkout = () => {
        console.log('⏸️ Pausing workout');
        dispatch({ type: 'PAUSE_WORKOUT' });
    };

    const resumeWorkout = () => {
        console.log('▶️ Resuming workout');
        dispatch({ type: 'RESUME_WORKOUT' });
    };

    const completeWorkout = () => {
        console.log('✅ Completing workout');
        dispatch({ type: 'COMPLETE_WORKOUT' });
    };

    const cancelWorkout = () => {
        console.log('❌ Cancelling workout');
        dispatch({ type: 'CANCEL_WORKOUT' });
    };

    // ==================== NAVIGATION ====================

    const goToNextExercise = () => {
        dispatch({ type: 'NEXT_EXERCISE' });
    };

    const goToPreviousExercise = () => {
        dispatch({ type: 'PREVIOUS_EXERCISE' });
    };

    const goToExercise = (exerciseIndex: number) => {
        dispatch({ type: 'GO_TO_EXERCISE', payload: exerciseIndex });
    };

    // ==================== SET MANAGEMENT ====================

    const completeSet = (setId: string, actualData: Partial<WorkoutSet>) => {
        console.log('✅ Completing set:', setId, actualData);
        dispatch({ type: 'COMPLETE_SET', payload: { setId, actualData } });
    };

    const skipSet = (setId: string) => {
        console.log('⏭️ Skipping set:', setId);
        dispatch({ type: 'SKIP_SET', payload: setId });
    };

    const addSet = (exerciseId: string) => {
        console.log('➕ Adding set to exercise:', exerciseId);
        dispatch({ type: 'ADD_SET', payload: exerciseId });
    };

    const removeSet = (setId: string) => {
        console.log('🗑️ Removing set:', setId);
        dispatch({ type: 'REMOVE_SET', payload: setId });
    };

    // ==================== EXERCISE MANAGEMENT ====================

    const skipExercise = (exerciseId: string) => {
        console.log('⏭️ Skipping exercise:', exerciseId);
        dispatch({ type: 'SKIP_EXERCISE', payload: exerciseId });
    };

    const completeExercise = (exerciseId: string) => {
        console.log('✅ Completing exercise:', exerciseId);
        dispatch({ type: 'COMPLETE_EXERCISE', payload: exerciseId });
    };

    const addExerciseToCurrentWorkout = (exercise: any, config: any) => {
        console.log('🏋️‍♂️ Adding exercise to current workout:', exercise.name || exercise.exerciseName, config);

        // ✅ NEW: Validate exercise and config before adding
        if (!exercise || !config) {
            console.error('❌ Invalid exercise or config provided:', { exercise, config });
            return;
        }

        dispatch({ type: 'ADD_EXERCISE_TO_WORKOUT', payload: { exercise, config } });
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
            dispatch({ type: 'UPDATE_DURATION' });
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

export default WorkoutContext;