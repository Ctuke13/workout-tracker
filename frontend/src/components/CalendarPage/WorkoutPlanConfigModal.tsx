import React, { useState, useEffect } from 'react';
import {
    Calendar,
    Clock,
    Target,
    Weight,
    Timer,
    RotateCcw,
    Activity,
    ChevronDown,
    ChevronUp,
    X,
    Save,
    Play,
    AlertCircle,
    CheckCircle,
    Settings,
    SkipForward,
    Loader
} from 'lucide-react';
import { WorkoutPlanInfo } from '../../types/api';
import {
    Exercise,
    ExerciseConfiguration,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    getWorkoutTrackingType,
    getDefaultConfigForExercise
} from '../../types/exercise';
import { useAuth } from '../../contexts/AuthContext';
import { workoutPlanApi } from '../../services/workoutPlanApi';
import { exerciseApi } from '../../services/exerciseApi';
import { transformBackendExerciseToFrontend } from '../../services/transformers';

// =============================================================================
// INTERFACES
// =============================================================================

interface ExerciseInPlan {
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

interface WorkoutPlanExerciseConfig {
    exerciseId: number;
    configuration: ExerciseConfiguration;
    skip: boolean;
    substitute?: boolean;
    notes?: string;
}

interface WorkoutPlanConfiguration {
    workoutPlanId: number;
    scheduledDate: string;
    exerciseConfigs: WorkoutPlanExerciseConfig[];
    planNotes: string;
    estimatedDuration: number;
    reminderEnabled: boolean;
    reminderTime: string;
}

interface WorkoutPlanConfigModalProps {
    isOpen: boolean;
    onClose: () => void;
    workoutPlan: WorkoutPlanInfo | null;
    selectedDate: Date;
    onSchedule: (config: WorkoutPlanConfiguration) => void;
    loading: boolean;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

const WorkoutPlanConfigModal: React.FC<WorkoutPlanConfigModalProps> = ({
                                                                           isOpen,
                                                                           onClose,
                                                                           workoutPlan,
                                                                           selectedDate,
                                                                           onSchedule,
                                                                           loading
                                                                       }) => {
    const { user } = useAuth();

    // =============================================================================
    // STATE MANAGEMENT
    // =============================================================================

    // Real exercises from backend
    const [exercisesInPlan, setExercisesInPlan] = useState<ExerciseInPlan[]>([]);
    const [loadingExercises, setLoadingExercises] = useState(false);
    const [exerciseError, setExerciseError] = useState<string | null>(null);

    const [exerciseConfigs, setExerciseConfigs] = useState<Record<number, WorkoutPlanExerciseConfig>>({});
    const [expandedExercises, setExpandedExercises] = useState<Set<number>>(new Set([1])); // Start with first exercise expanded
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

    // =============================================================================
    // BACKEND DATA FETCHING
    // =============================================================================

    // Fetch exercises for the workout plan from backend
    useEffect(() => {
        const fetchWorkoutPlanExercises = async () => {
            if (!isOpen || !workoutPlan?.id) return;

            setLoadingExercises(true);
            setExerciseError(null);

            try {
                console.log('🏋️ Fetching exercises for workout plan:', workoutPlan.id);

                // Get the workout plan details with exercises
                const planDetails = await workoutPlanApi.getWorkoutPlanById(workoutPlan.id);

                // If your backend returns exercise IDs, fetch the full exercise details
                let planExercises: ExerciseInPlan[] = [];

                // ✅ FIXED: Type-safe access to exerciseIds
                const exerciseIds = (planDetails as any).exerciseIds as number[] | undefined;
                if (exerciseIds && exerciseIds.length > 0) {
                    // Fetch full exercise details for each exercise in the plan
                    const exercisePromises = exerciseIds.map(async (exerciseId: number, index: number) => {
                        try {
                            const exercise = await exerciseApi.getExerciseById(exerciseId);

                            // Check if user can access this exercise based on subscription
                            const isAccessible = exercise.requiresEquipment ?
                                (user?.subscriptionTier === 'PLUS' || user?.subscriptionTier === 'PRO') :
                                true; // Free exercises are always accessible

                            return {
                                id: exerciseId,
                                exercise: exercise,
                                orderInWorkout: index + 1,
                                // You may have prescribed values in your workout plan
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

                    // ✅ FIXED: Type-safe filter with explicit type annotation
                    const exerciseResults = await Promise.all(exercisePromises);
                    planExercises = exerciseResults.filter((ex): ex is ExerciseInPlan => ex !== null);
                }

                // Alternative: If your backend has a direct endpoint for workout plan exercises
                else {
                    try {
                        // Try a direct endpoint for workout plan exercises
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
                                isAccessible: backendEx.isAccessible !== false // Default to accessible unless explicitly false
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
        };

        fetchWorkoutPlanExercises();
    }, [isOpen, workoutPlan?.id, user?.subscriptionTier]);

    // =============================================================================
    // INITIALIZATION
    // =============================================================================

    useEffect(() => {
        if (isOpen && exercisesInPlan.length > 0) {
            // Initialize configurations for all accessible exercises
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

    const toggleExercise = (exerciseId: number) => {
        const newExpanded = new Set(expandedExercises);
        if (newExpanded.has(exerciseId)) {
            newExpanded.delete(exerciseId);
        } else {
            newExpanded.add(exerciseId);
        }
        setExpandedExercises(newExpanded);
    };

    const updateExerciseConfig = (exerciseId: number, updates: Partial<WorkoutPlanExerciseConfig>) => {
        setExerciseConfigs(prev => ({
            ...prev,
            [exerciseId]: {
                ...prev[exerciseId],
                ...updates
            }
        }));
    };

    const updateExerciseConfiguration = (exerciseId: number, newConfig: ExerciseConfiguration) => {
        updateExerciseConfig(exerciseId, { configuration: newConfig });
    };

    const handleSkipExercise = (exerciseId: number, skip: boolean) => {
        updateExerciseConfig(exerciseId, { skip });
    };

    const handleSchedule = () => {
        if (!workoutPlan) return;

        const finalConfig: WorkoutPlanConfiguration = {
            workoutPlanId: workoutPlan.id,
            scheduledDate: selectedDate.toISOString().split('T')[0],
            exerciseConfigs: Object.values(exerciseConfigs).filter(config => !config.skip),
            planNotes,
            estimatedDuration: totalEstimatedDuration,
            reminderEnabled,
            reminderTime
        };

        onSchedule(finalConfig);
    };

    const goToNextExercise = () => {
        const nextIndex = Math.min(currentStep + 1, exercisesInPlan.length - 1);
        setCurrentStep(nextIndex);
        setExpandedExercises(new Set([exercisesInPlan[nextIndex].id]));
    };

    const goToPreviousExercise = () => {
        const prevIndex = Math.max(currentStep - 1, 0);
        setCurrentStep(prevIndex);
        setExpandedExercises(new Set([exercisesInPlan[prevIndex].id]));
    };

    // =============================================================================
    // RENDER GUARDS
    // =============================================================================

    if (!isOpen || !workoutPlan) return null;

    const canSchedule = configuredCount >= accessibleExercises.length;

    // =============================================================================
    // RENDER COMPONENT
    // =============================================================================

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-3xl max-w-4xl w-full max-h-[95vh] flex flex-col shadow-2xl">

                {/* Header */}
                <div className="bg-gradient-to-r from-purple-600 to-blue-600 rounded-t-3xl p-6 text-white">
                    <div className="flex items-center justify-between mb-4">
                        <h2 className="text-2xl font-bold">Configure Your Workout</h2>
                        <button
                            onClick={onClose}
                            className="p-2 hover:bg-white/20 rounded-xl transition-colors"
                        >
                            <X className="w-6 h-6" />
                        </button>
                    </div>

                    <div className="flex items-center gap-4 mb-4">
                        <div className="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center text-2xl">
                            📋
                        </div>
                        <div>
                            <h3 className="text-xl font-semibold">
                                {workoutPlan.name || workoutPlan.workoutName}
                            </h3>
                            <p className="text-white/80 text-sm">
                                Configure your targets for each exercise
                            </p>
                        </div>
                    </div>

                    {/* Progress Bar */}
                    <div className="bg-white/20 rounded-full h-2 mb-2">
                        <div
                            className="bg-white rounded-full h-2 transition-all duration-300"
                            style={{ width: `${(configuredCount / accessibleExercises.length) * 100}%` }}
                        />
                    </div>
                    <div className="flex justify-between text-sm text-white/80">
                        <span>{configuredCount} of {accessibleExercises.length} exercises configured</span>
                        <span>{totalEstimatedDuration} min total</span>
                    </div>
                </div>

                {/* Exercise Configuration Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    {/* Loading State */}
                    {loadingExercises && (
                        <div className="flex items-center justify-center py-12">
                            <div className="text-center">
                                <Loader className="w-8 h-8 animate-spin text-purple-600 mx-auto mb-4" />
                                <p className="text-gray-600">Loading workout exercises...</p>
                            </div>
                        </div>
                    )}

                    {/* Error State */}
                    {exerciseError && (
                        <div className="p-4 bg-red-50 border border-red-200 rounded-xl mb-6">
                            <div className="flex items-center gap-2 text-red-700">
                                <AlertCircle className="w-5 h-5" />
                                <span className="font-medium">Error Loading Exercises</span>
                            </div>
                            <p className="text-sm text-red-600 mt-1">{exerciseError}</p>
                            <button
                                onClick={() => window.location.reload()}
                                className="mt-2 px-3 py-1 bg-red-100 hover:bg-red-200 text-red-700 text-sm rounded-lg transition-colors"
                            >
                                Retry
                            </button>
                        </div>
                    )}

                    {/* Exercises List */}
                    {!loadingExercises && !exerciseError && exercisesInPlan.length > 0 && (
                        <div className="space-y-4">
                            {exercisesInPlan.map((exerciseInPlan, index) => {
                                const exercise = exerciseInPlan.exercise;
                                const config = exerciseConfigs[exerciseInPlan.id];
                                const isExpanded = expandedExercises.has(exerciseInPlan.id);
                                const isConfigured = !!config && !config.skip;
                                const trackingType = getWorkoutTrackingType(exercise);

                                return (
                                    <div
                                        key={exerciseInPlan.id}
                                        className={`border-2 rounded-2xl transition-all duration-200 ${
                                            !exerciseInPlan.isAccessible
                                                ? 'border-gray-200 bg-gray-50 opacity-60'
                                                : isConfigured
                                                    ? 'border-green-200 bg-green-50'
                                                    : isExpanded
                                                        ? 'border-purple-300 bg-white shadow-lg'
                                                        : 'border-gray-200 bg-white hover:border-gray-300'
                                        }`}
                                    >
                                        {/* Exercise Header */}
                                        <div
                                            className="p-4 cursor-pointer"
                                            onClick={() => exerciseInPlan.isAccessible && toggleExercise(exerciseInPlan.id)}
                                        >
                                            <div className="flex items-center justify-between">
                                                <div className="flex items-center gap-4">
                                                    <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-white ${
                                                        isConfigured ? 'bg-green-500' : 'bg-purple-500'
                                                    }`}>
                                                        {isConfigured ? <CheckCircle className="w-5 h-5" /> : index + 1}
                                                    </div>

                                                    <div>
                                                        <div className="flex items-center gap-2 mb-1">
                                                            <h4 className="font-bold text-lg text-gray-900">
                                                                {exercise.emoji} {exercise.name}
                                                            </h4>
                                                            {!exerciseInPlan.isAccessible && (
                                                                <span className="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full">
                                                                    Premium
                                                                </span>
                                                            )}
                                                        </div>
                                                        <div className="flex items-center gap-3 text-sm text-gray-600">
                                                            <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                                                trackingType === 'cardio' ? 'bg-red-100 text-red-700' :
                                                                    trackingType === 'isometric' ? 'bg-purple-100 text-purple-700' :
                                                                        'bg-blue-100 text-blue-700'
                                                            }`}>
                                                                {trackingType === 'cardio' ? '❤️ Cardio' :
                                                                    trackingType === 'isometric' ? '🛡️ Hold' :
                                                                        '💪 Strength'}
                                                            </span>
                                                            <span>{exercise.difficultyLevel}</span>
                                                            <span>{exercise.estimatedDurationMinutes} min</span>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-2">
                                                    {exerciseInPlan.isAccessible && (
                                                        <label className="flex items-center gap-2 text-sm">
                                                            <input
                                                                type="checkbox"
                                                                checked={config?.skip || false}
                                                                onChange={(e) => handleSkipExercise(exerciseInPlan.id, e.target.checked)}
                                                                className="rounded"
                                                            />
                                                            Skip
                                                        </label>
                                                    )}
                                                    {isExpanded ?
                                                        <ChevronUp className="w-5 h-5 text-gray-400" /> :
                                                        <ChevronDown className="w-5 h-5 text-gray-400" />
                                                    }
                                                </div>
                                            </div>
                                        </div>

                                        {/* Exercise Configuration */}
                                        {isExpanded && exerciseInPlan.isAccessible && config && !config.skip && (
                                            <div className="border-t border-gray-200 p-6 bg-gray-50">
                                                <ExerciseConfigurationForm
                                                    exercise={exercise}
                                                    configuration={config.configuration}
                                                    onConfigurationChange={(newConfig) => updateExerciseConfiguration(exerciseInPlan.id, newConfig)}
                                                    prescribedValues={{
                                                        sets: exerciseInPlan.prescribedSets,
                                                        reps: exerciseInPlan.prescribedReps,
                                                        weight: exerciseInPlan.prescribedWeight,
                                                        duration: exerciseInPlan.prescribedDuration,
                                                        rest: exerciseInPlan.prescribedRest
                                                    }}
                                                    instructions={exerciseInPlan.instructions}
                                                />

                                                {/* Exercise Notes */}
                                                <div className="mt-4">
                                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                                        Personal Notes
                                                    </label>
                                                    <textarea
                                                        value={config.notes || ''}
                                                        onChange={(e) => updateExerciseConfig(exerciseInPlan.id, { notes: e.target.value })}
                                                        placeholder="Add any personal notes for this exercise..."
                                                        rows={2}
                                                        className="w-full p-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500 resize-none"
                                                    />
                                                </div>
                                            </div>
                                        )}

                                        {/* Skipped Exercise Display */}
                                        {config?.skip && (
                                            <div className="border-t border-gray-200 p-4 bg-yellow-50">
                                                <div className="flex items-center gap-2 text-yellow-700">
                                                    <AlertCircle className="w-4 h-4" />
                                                    <span className="text-sm font-medium">This exercise will be skipped</span>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {/* Empty State */}
                    {!loadingExercises && !exerciseError && exercisesInPlan.length === 0 && (
                        <div className="text-center py-12">
                            <div className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-gray-100 to-gray-200 rounded-full flex items-center justify-center">
                                <AlertCircle className="w-10 h-10 text-gray-400" />
                            </div>
                            <h3 className="text-xl font-bold text-gray-900 mb-2">No Exercises Found</h3>
                            <p className="text-gray-500 max-w-sm mx-auto">
                                This workout plan doesn't have any exercises configured yet.
                            </p>
                        </div>
                    )}

                    {/* ✅ FIXED: Plan Notes - Only show if exercises are loaded */}
                    {!loadingExercises && exercisesInPlan.length > 0 && (
                        <>
                            <div className="mt-6 p-4 bg-gray-50 rounded-2xl">
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Workout Plan Notes
                                </label>
                                <textarea
                                    value={planNotes}
                                    onChange={(e) => setPlanNotes(e.target.value)}
                                    placeholder="Add any overall notes about this workout plan..."
                                    rows={3}
                                    className="w-full p-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500 resize-none"
                                />
                            </div>

                            {/* ✅ FIXED: Reminder Settings with proper JSX structure */}
                            <div className="mt-4 p-4 bg-gray-50 rounded-2xl">
                                <div className="flex items-center justify-between mb-3">
                                    <label className="text-sm font-medium text-gray-700">
                                        Workout Reminder
                                    </label>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={reminderEnabled}
                                            onChange={(e) => setReminderEnabled(e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-purple-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-600"></div>
                                    </label>
                                </div>
                                {reminderEnabled && (
                                    <input
                                        type="time"
                                        value={reminderTime}
                                        onChange={(e) => setReminderTime(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                                    />
                                )}
                            </div>
                        </>
                    )}
                </div>

                {/* ✅ FIXED: Footer with proper JSX structure */}
                <div className="border-t border-gray-200 p-6 bg-gray-50 rounded-b-3xl">
                    <div className="flex items-center justify-between mb-4">
                        <div className="text-sm text-gray-600">
                            <div className="font-medium">Ready to schedule:</div>
                            <div>{configuredCount} exercises • {totalEstimatedDuration} minutes</div>
                        </div>
                        <div className="text-sm text-gray-600">
                            {selectedDate.toLocaleDateString('en-US', {
                                weekday: 'long',
                                month: 'long',
                                day: 'numeric'
                            })}
                        </div>
                    </div>

                    <div className="flex gap-3">
                        <button
                            onClick={onClose}
                            className="flex-1 px-6 py-3 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-xl hover:bg-gray-50 transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleSchedule}
                            disabled={loading || !canSchedule || loadingExercises}
                            className="flex-1 px-6 py-3 text-sm font-medium text-white bg-purple-600 border border-transparent rounded-xl hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
                        >
                            {loading ? (
                                <>
                                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                    Scheduling...
                                </>
                            ) : loadingExercises ? (
                                <>
                                    <Loader className="w-4 h-4 animate-spin" />
                                    Loading...
                                </>
                            ) : (
                                <>
                                    <Play className="w-4 h-4" />
                                    Schedule Workout
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

// =============================================================================
// EXERCISE CONFIGURATION FORM COMPONENT
// =============================================================================

interface ExerciseConfigurationFormProps {
    exercise: Exercise;
    configuration: ExerciseConfiguration;
    onConfigurationChange: (config: ExerciseConfiguration) => void;
    prescribedValues?: {
        sets?: number;
        reps?: number;
        weight?: number;
        duration?: number;
        rest?: number;
    };
    instructions?: string;
}

const ExerciseConfigurationForm: React.FC<ExerciseConfigurationFormProps> = ({
                                                                                 exercise,
                                                                                 configuration,
                                                                                 onConfigurationChange,
                                                                                 prescribedValues,
                                                                                 instructions
                                                                             }) => {
    const trackingType = getWorkoutTrackingType(exercise);

    // ✅ FIXED: Type-safe update functions for each configuration type
    const updateStrengthConfig = (updates: Partial<StrengthConfiguration>) => {
        const currentConfig = configuration as StrengthConfiguration;
        onConfigurationChange({ ...currentConfig, ...updates } as StrengthConfiguration);
    };

    const updateCardioConfig = (updates: Partial<CardioConfiguration>) => {
        const currentConfig = configuration as CardioConfiguration;
        onConfigurationChange({ ...currentConfig, ...updates } as CardioConfiguration);
    };

    const updateIsometricConfig = (updates: Partial<IsometricConfiguration>) => {
        const currentConfig = configuration as IsometricConfiguration;
        onConfigurationChange({ ...currentConfig, ...updates } as IsometricConfiguration);
    };

    if (trackingType === 'strength') {
        const config = configuration as StrengthConfiguration;

        return (
            <div className="space-y-4">
                {instructions && (
                    <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                        <p className="text-sm text-blue-700">{instructions}</p>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Sets {prescribedValues?.sets && <span className="text-gray-500">(recommended: {prescribedValues.sets})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="20"
                            value={config.targetSets}
                            onChange={(e) => updateStrengthConfig({ targetSets: parseInt(e.target.value) || 1 })}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Reps {prescribedValues?.reps && <span className="text-gray-500">(recommended: {prescribedValues.reps})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="100"
                            value={config.targetReps}
                            onChange={(e) => updateStrengthConfig({ targetReps: parseInt(e.target.value) || 1 })}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Weight ({config.targetWeightUnit}) {prescribedValues?.weight && <span className="text-gray-500">(recommended: {prescribedValues.weight})</span>}
                        </label>
                        <input
                            type="number"
                            min="0"
                            step="0.5"
                            value={config.targetWeight || ''}
                            onChange={(e) => updateStrengthConfig({ targetWeight: e.target.value ? parseFloat(e.target.value) : undefined })}
                            placeholder="Optional"
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Rest (seconds) {prescribedValues?.rest && <span className="text-gray-500">(recommended: {prescribedValues.rest})</span>}
                        </label>
                        <input
                            type="number"
                            min="0"
                            max="600"
                            step="15"
                            value={config.restSeconds}
                            onChange={(e) => updateStrengthConfig({ restSeconds: parseInt(e.target.value) || 0 })}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                </div>

                {/* Quick Presets */}
                <div className="grid grid-cols-3 gap-2">
                    <button
                        type="button"
                        onClick={() => updateStrengthConfig({ targetWeight: 45 })}
                        className="px-3 py-2 text-xs bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                    >
                        45 lbs
                    </button>
                    <button
                        type="button"
                        onClick={() => updateStrengthConfig({ targetWeight: 135 })}
                        className="px-3 py-2 text-xs bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                    >
                        135 lbs
                    </button>
                    <button
                        type="button"
                        onClick={() => updateStrengthConfig({ restSeconds: 90 })}
                        className="px-3 py-2 text-xs bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                    >
                        90s rest
                    </button>
                </div>
            </div>
        );
    }

    if (trackingType === 'cardio') {
        const config = configuration as CardioConfiguration;

        return (
            <div className="space-y-4">
                {instructions && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-700">{instructions}</p>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Duration (minutes) {prescribedValues?.duration && <span className="text-gray-500">(recommended: {prescribedValues.duration})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="300"
                            value={config.targetDurationMinutes}
                            onChange={(e) => updateCardioConfig({ targetDurationMinutes: parseInt(e.target.value) || 1 })}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Distance ({config.targetDistanceUnit})
                        </label>
                        <input
                            type="number"
                            min="0"
                            step="0.1"
                            value={config.targetDistance || ''}
                            onChange={(e) => updateCardioConfig({ targetDistance: e.target.value ? parseFloat(e.target.value) : undefined })}
                            placeholder="Optional"
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                        />
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Target Pace (min/{config.targetDistanceUnit === 'miles' ? 'mile' : 'km'})
                    </label>
                    <input
                        type="number"
                        min="3"
                        max="20"
                        step="0.1"
                        value={config.targetPace || ''}
                        onChange={(e) => updateCardioConfig({ targetPace: e.target.value ? parseFloat(e.target.value) : undefined })}
                        placeholder="Optional"
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />
                </div>

                {/* Quick Presets */}
                <div className="grid grid-cols-4 gap-2">
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({ targetDurationMinutes: 20 })}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        20 min
                    </button>
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({ targetDurationMinutes: 30 })}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        30 min
                    </button>
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({ targetDistance: 3.1 })}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        5K
                    </button>
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({ targetPace: 8 })}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        8:00 pace
                    </button>
                </div>
            </div>
        );
    }

    if (trackingType === 'isometric') {
        const config = configuration as IsometricConfiguration;

        return (
            <div className="space-y-4">
                {instructions && (
                    <div className="p-3 bg-purple-50 border border-purple-200 rounded-lg">
                        <p className="text-sm text-purple-700">{instructions}</p>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Sets {prescribedValues?.sets && <span className="text-gray-500">(recommended: {prescribedValues.sets})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="10"
                            value={config.targetSets}
                            onChange={(e) => updateIsometricConfig({ targetSets: parseInt(e.target.value) || 1 })}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Hold Time (seconds) {prescribedValues?.duration && <span className="text-gray-500">(recommended: {prescribedValues.duration})</span>}
                        </label>
                        <input
                            type="number"
                            min="5"
                            max="300"
                            step="5"
                            value={config.holdDurationSeconds}
                            onChange={(e) => updateIsometricConfig({ holdDurationSeconds: parseInt(e.target.value) || 5 })}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Rest Between Sets (seconds) {prescribedValues?.rest && <span className="text-gray-500">(recommended: {prescribedValues.rest})</span>}
                    </label>
                    <input
                        type="number"
                        min="0"
                        max="600"
                        step="15"
                        value={config.restSeconds}
                        onChange={(e) => updateIsometricConfig({ restSeconds: parseInt(e.target.value) || 0 })}
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                    />
                </div>

                {/* Quick Presets */}
                <div className="grid grid-cols-4 gap-2">
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({ holdDurationSeconds: 30 })}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        30s hold
                    </button>
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({ holdDurationSeconds: 60 })}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        60s hold
                    </button>
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({ targetSets: 3 })}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        3 sets
                    </button>
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({ restSeconds: 60 })}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        60s rest
                    </button>
                </div>
            </div>
        );
    }

    return null;
};

export default WorkoutPlanConfigModal;