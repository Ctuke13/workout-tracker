import {useState, useEffect, useCallback} from 'react';
import {WorkoutPlanInfo} from '../types/api';
import {
    Exercise,
    ExerciseConfiguration,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    getDefaultConfigForExercise
} from '../types/exercise';
import {useAuth} from '../contexts/AuthContext';
import {workoutPlanApi} from '../services/workoutPlanApi';
import {exerciseApi} from '../services/exerciseApi';
import {transformBackendExerciseToFrontend} from '../services/transformers';

// =============================================================================
// INTERFACES
// =============================================================================

export interface ExerciseInPlan {
    id: number;
    exercise: Exercise;
    orderInWorkout: number;
    prescribedSets?: number;
    prescribedReps?: number;
    prescribedWeight?: number;
    prescribedDuration?: number;
    prescribedRest?: number;
    instructions?: string;
    isOptional?: boolean;
    isAccessible: boolean;
}

export interface WorkoutPlanExerciseConfig {
    exerciseId: number;
    configuration: ExerciseConfiguration;
    skip: boolean;
    substitute?: boolean;
    notes?: string;
}

export interface WorkoutPlanConfiguration {
    workoutPlanId: number;
    scheduledDate: string;
    exerciseConfigs: WorkoutPlanExerciseConfig[];
    planNotes: string;
    estimatedDuration: number;
    reminderEnabled: boolean;
    reminderTime: string;
}

interface UseWorkoutPlanConfigProps {
    isOpen: boolean;
    workoutPlan: WorkoutPlanInfo | null;
    selectedDate: Date;
}

export const useWorkoutPlanConfig = ({isOpen, workoutPlan, selectedDate}: UseWorkoutPlanConfigProps) => {
    const {user} = useAuth();

    // =============================================================================
    // STATE MANAGEMENT
    // =============================================================================

    // Exercise data state
    const [exercisesInPlan, setExercisesInPlan] = useState<ExerciseInPlan[]>([]);
    const [loadingExercises, setLoadingExercises] = useState(false);
    const [exerciseError, setExerciseError] = useState<string | null>(null);

    // Configuration state
    const [exerciseConfigs, setExerciseConfigs] = useState<Record<number, WorkoutPlanExerciseConfig>>({});
    const [expandedExercises, setExpandedExercises] = useState<Set<number>>(new Set([1]));
    const [planNotes, setPlanNotes] = useState('');
    const [reminderEnabled, setReminderEnabled] = useState(true);
    const [reminderTime, setReminderTime] = useState('08:00');
    const [currentStep, setCurrentStep] = useState(0);

    // =============================================================================
    // COMPUTED VALUES
    // =============================================================================

    const accessibleExercises = exercisesInPlan.filter(ex => ex.isAccessible);
    const lockedExercises = exercisesInPlan.filter(ex => !ex.isAccessible);
    const configuredCount = Object.keys(exerciseConfigs).length;
    const totalEstimatedDuration = exercisesInPlan.reduce((total, ex) => {
        const config = exerciseConfigs[ex.id];
        if (config?.skip) return total;

        const exercise = ex.exercise;
        if (exercise.isCardio) {
            const cardioConfig = config?.configuration as CardioConfiguration;
            return total + (cardioConfig?.targetDurationMinutes || ex.prescribedDuration || 20);
        }
        return total + (exercise.estimatedDurationMinutes || 5);
    }, 0);

    const canSchedule = configuredCount >= accessibleExercises.length;

    // =============================================================================
    // DATA FETCHING
    // =============================================================================

    const fetchWorkoutPlanExercises = useCallback(async () => {
        if (!isOpen || !workoutPlan?.id) return;

        setLoadingExercises(true);
        setExerciseError(null);

        try {
            console.log('🏋️ Fetching exercises for workout plan:', workoutPlan.id);

            // Get the workout plan details with exercises
            const planDetails = await workoutPlanApi.getWorkoutPlanById(workoutPlan.id);

            let planExercises: ExerciseInPlan[] = [];

            // Type-safe access to exerciseIds
            const exerciseIds = (planDetails as any).exerciseIds as number[] | undefined;
            if (exerciseIds && exerciseIds.length > 0) {
                // Fetch full exercise details for each exercise in the plan
                const exercisePromises = exerciseIds.map(async (exerciseId: number, index: number) => {
                    try {
                        const exercise = await exerciseApi.getExerciseById(exerciseId);

                        // Check if user can access this exercise based on subscription
                        const isAccessible = exercise.requiresEquipment ?
                            (user?.subscriptionTier === 'PLUS' || user?.subscriptionTier === 'PRO') :
                            true;

                        return {
                            id: exerciseId,
                            exercise: exercise,
                            orderInWorkout: index + 1,
                            prescribedSets: (planDetails as any).prescribedSets?.[index],
                            prescribedReps: (planDetails as any).prescribedReps?.[index],
                            prescribedWeight: (planDetails as any).prescribedWeights?.[index],
                            prescribedDuration: (planDetails as any).prescribedDurations?.[index],
                            prescribedRest: (planDetails as any).prescribedRestTimes?.[index],
                            instructions: (planDetails as any).exerciseInstructions?.[index],
                            isOptional: (planDetails as any).optionalExercises?.includes(exerciseId) || false,
                            isAccessible: isAccessible
                        } as ExerciseInPlan;
                    } catch (error) {
                        console.error(`Failed to fetch exercise ${exerciseId}:`, error);
                        return null;
                    }
                });

                const exerciseResults = await Promise.all(exercisePromises);
                planExercises = exerciseResults.filter((ex): ex is ExerciseInPlan => ex !== null);
            }

            // Alternative: Direct endpoint for workout plan exercises
            else {
                try {
                    const response = await fetch(`/api/workout-plans/${workoutPlan.id}/exercises`);
                    if (response.ok) {
                        const backendExercises = await response.json();
                        planExercises = backendExercises.map((backendEx: any, index: number) => ({
                            id: backendEx.exerciseId || backendEx.id,
                            exercise: transformBackendExerciseToFrontend(backendEx.exercise || backendEx),
                            orderInWorkout: backendEx.orderInWorkout || index + 1,
                            prescribedSets: backendEx.prescribedSets,
                            prescribedReps: backendEx.prescribedReps,
                            prescribedWeight: backendEx.prescribedWeight,
                            prescribedDuration: backendEx.prescribedDuration,
                            prescribedRest: backendEx.prescribedRest,
                            instructions: backendEx.instructions,
                            isOptional: backendEx.isOptional || false,
                            isAccessible: backendEx.isAccessible !== false
                        }));
                    }
                } catch (fallbackError) {
                    console.warn('Direct workout plan exercises endpoint not available, using fallback');
                }
            }

            // Final fallback: Create sample exercises if none found
            if (planExercises.length === 0) {
                console.log('No exercises found, creating fallback exercises');
                const sampleExercises = await exerciseApi.getPublicExercises();
                planExercises = sampleExercises.slice(0, 3).map((exercise, index) => ({
                    id: exercise.id,
                    exercise: exercise,
                    orderInWorkout: index + 1,
                    prescribedSets: exercise.isCardio ? 1 : 3,
                    prescribedReps: exercise.isCardio ? undefined : 10,
                    prescribedDuration: exercise.isCardio ? 20 : undefined,
                    prescribedRest: 60,
                    instructions: `Perform ${exercise.name} with proper form`,
                    isOptional: false,
                    isAccessible: true
                }));
            }

            console.log(`✅ Successfully loaded ${planExercises.length} exercises for workout plan`);
            setExercisesInPlan(planExercises);

        } catch (error) {
            console.error('❌ Failed to fetch workout plan exercises:', error);
            setExerciseError('Failed to load workout plan exercises. Please try again.');
        } finally {
            setLoadingExercises(false);
        }
    }, [isOpen, workoutPlan?.id, user?.subscriptionTier]);

    // =============================================================================
    // EFFECTS
    // =============================================================================

    // Fetch exercises when modal opens
    useEffect(() => {
        fetchWorkoutPlanExercises();
    }, [fetchWorkoutPlanExercises]);

    // Initialize configurations when exercises are loaded
    useEffect(() => {
        if (isOpen && exercisesInPlan.length > 0) {
            const initialConfigs: Record<number, WorkoutPlanExerciseConfig> = {};

            exercisesInPlan.forEach(exerciseInPlan => {
                if (exerciseInPlan.isAccessible) {
                    const defaultConfig = getDefaultConfigForExercise(exerciseInPlan.exercise);

                    // Override defaults with prescribed values from the plan
                    if (defaultConfig.trackingMode === 'strength') {
                        const strengthConfig = defaultConfig as StrengthConfiguration;
                        strengthConfig.targetSets = exerciseInPlan.prescribedSets || strengthConfig.targetSets;
                        strengthConfig.targetReps = exerciseInPlan.prescribedReps || strengthConfig.targetReps;
                        strengthConfig.targetWeight = exerciseInPlan.prescribedWeight || strengthConfig.targetWeight;
                        strengthConfig.restSeconds = exerciseInPlan.prescribedRest || strengthConfig.restSeconds;
                    } else if (defaultConfig.trackingMode === 'cardio') {
                        const cardioConfig = defaultConfig as CardioConfiguration;
                        cardioConfig.targetDurationMinutes = exerciseInPlan.prescribedDuration || cardioConfig.targetDurationMinutes;
                    } else if (defaultConfig.trackingMode === 'isometric') {
                        const isometricConfig = defaultConfig as IsometricConfiguration;
                        isometricConfig.targetSets = exerciseInPlan.prescribedSets || isometricConfig.targetSets;
                        isometricConfig.holdDurationSeconds = exerciseInPlan.prescribedDuration || isometricConfig.holdDurationSeconds;
                        isometricConfig.restSeconds = exerciseInPlan.prescribedRest || isometricConfig.restSeconds;
                    }

                    initialConfigs[exerciseInPlan.id] = {
                        exerciseId: exerciseInPlan.id,
                        configuration: defaultConfig,
                        skip: false,
                        notes: exerciseInPlan.instructions || ''
                    };
                }
            });

            setExerciseConfigs(initialConfigs);
        }
    }, [isOpen, exercisesInPlan]);

    // =============================================================================
    // EVENT HANDLERS
    // =============================================================================

    const toggleExercise = useCallback((exerciseId: number) => {
        setExpandedExercises(prev => {
            const newExpanded = new Set(prev);
            if (newExpanded.has(exerciseId)) {
                newExpanded.delete(exerciseId);
            } else {
                newExpanded.add(exerciseId);
            }
            return newExpanded;
        });
    }, []);

    const updateExerciseConfig = useCallback((exerciseId: number, updates: Partial<WorkoutPlanExerciseConfig>) => {
        setExerciseConfigs(prev => ({
            ...prev,
            [exerciseId]: {
                ...prev[exerciseId],
                ...updates
            }
        }));
    }, []);

    const updateExerciseConfiguration = useCallback((exerciseId: number, newConfig: ExerciseConfiguration) => {
        updateExerciseConfig(exerciseId, {configuration: newConfig});
    }, [updateExerciseConfig]);

    const handleSkipExercise = useCallback((exerciseId: number, skip: boolean) => {
        updateExerciseConfig(exerciseId, {skip});
    }, [updateExerciseConfig]);

    const goToNextExercise = useCallback(() => {
        const nextIndex = Math.min(currentStep + 1, exercisesInPlan.length - 1);
        setCurrentStep(nextIndex);
        setExpandedExercises(new Set([exercisesInPlan[nextIndex].id]));
    }, [currentStep, exercisesInPlan]);

    const goToPreviousExercise = useCallback(() => {
        const prevIndex = Math.max(currentStep - 1, 0);
        setCurrentStep(prevIndex);
        setExpandedExercises(new Set([exercisesInPlan[prevIndex].id]));
    }, [currentStep, exercisesInPlan]);

    const buildConfiguration = useCallback((): WorkoutPlanConfiguration | null => {
        if (!workoutPlan) return null;

        return {
            workoutPlanId: workoutPlan.id,
            scheduledDate: selectedDate.toISOString().split('T')[0],
            exerciseConfigs: Object.values(exerciseConfigs).filter(config => !config.skip),
            planNotes,
            estimatedDuration: totalEstimatedDuration,
            reminderEnabled,
            reminderTime
        };
    }, [workoutPlan, selectedDate, exerciseConfigs, planNotes, totalEstimatedDuration, reminderEnabled, reminderTime]);

    // =============================================================================
    // RETURN INTERFACE
    // =============================================================================

    return {
        // Data state
        exercisesInPlan,
        loadingExercises,
        exerciseError,

        // Configuration state
        exerciseConfigs,
        expandedExercises,
        planNotes,
        reminderEnabled,
        reminderTime,
        currentStep,

        // Computed values
        accessibleExercises,
        lockedExercises,
        configuredCount,
        totalEstimatedDuration,
        canSchedule,

        // Setters
        setPlanNotes,
        setReminderEnabled,
        setReminderTime,

        // Event handlers
        toggleExercise,
        updateExerciseConfig,
        updateExerciseConfiguration,
        handleSkipExercise,
        goToNextExercise,
        goToPreviousExercise,
        buildConfiguration,

        // Utilities
        fetchWorkoutPlanExercises
    };
};