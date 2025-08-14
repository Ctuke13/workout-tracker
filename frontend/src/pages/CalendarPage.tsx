import React, { useState, useEffect, useMemo } from 'react';
import { Calendar, ChevronLeft, ChevronRight, Play, Clock, CheckCircle, Target, Plus, Weight, Settings } from 'lucide-react';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { toast } from 'react-hot-toast';

// Import your existing components
import FloatingActionButton from '../components/layout/FloatingActionButton';
import { ExerciseConfigModal } from '../components/CalendarPage/index';
import EnhancedExerciseSelector from '../components/CalendarPage/ExerciseSelector';
import WorkoutPlanConfigModal from '../components/CalendarPage/WorkoutPlanConfigModal';

// Import types and services
import { ScheduledWorkoutResponse, WorkoutPlanInfo, WorkoutPlanScheduleRequest } from '../types/api';
import {
    Exercise,
    ExerciseConfiguration,
    CalendarDay,
    ScheduledExercise,
    WorkoutStats,
    getDefaultConfigForExercise,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration
} from '../types/exercise';
import { calendarApi } from '../services/calendarApi';
import { transformScheduledWorkoutsToCalendarData } from '../services/transformers';

// ==================== INTERFACES ====================

/**
 * Configuration for scheduling a single workout session
 * This EXACTLY matches the interface expected by WorkoutPlanConfigModal
 */
interface WorkoutPlanConfiguration {
    workoutPlanId: number;
    scheduledDate: string;
    exerciseConfigs: WorkoutPlanExerciseConfig[];
    planNotes: string;
    estimatedDuration: number;
    reminderEnabled: boolean;
    reminderTime: string;
}

/**
 * Individual exercise configuration within a workout plan
 * This matches the interface from WorkoutPlanConfigModal
 */
interface WorkoutPlanExerciseConfig {
    exerciseId: number;
    configuration: ExerciseConfiguration;
    skip: boolean;
    substitute?: boolean;
    notes?: string;
}

/**
 * Workout statistics response from API
 */
interface WorkoutStatsResponse {
    exercisesScheduledToday: number;
    exercisesCompletedToday: number;
    minutesWorkedOutToday: number;
    exercisesScheduledThisWeek: number;
    exercisesCompletedThisWeek: number;
    minutesWorkedOutThisWeek: number;
    exercisesScheduledThisMonth: number;
    exercisesCompletedThisMonth: number;
    minutesWorkedOutThisMonth: number;
    currentStreak: number;
    longestStreak: number;
    completionRateThisWeek: number;
    completionRateThisMonth: number;
    lastWorkoutDate: string;
    lastWorkoutType: string;
    totalWorkoutsCompleted: number;
    totalMinutesWorkedOut: number;
    favoriteExerciseType: string;
}

/**
 * Scheduled workout session data for API
 */
interface WorkoutSessionData {
    date: string; // ISO date string
    workoutPlanId: number;
    workoutPlanName: string;
    exerciseConfigurations: ExerciseConfiguration[];
    notes?: string;
    reminderEnabled?: boolean;
    reminderTime?: string;
    estimatedDuration?: number;
    createdAt: string;
}

// ==================== MAIN COMPONENT ====================

const CalendarPage: React.FC = () => {
    // ==================== CORE STATE ====================

    const [viewingDate, setViewingDate] = useState(new Date());
    const [scheduledWorkouts, setScheduledWorkouts] = useState<ScheduledExercise[]>([]);
    const [stats, setStats] = useState<WorkoutStats | null>(null);
    const [loading, setLoading] = useState(false);

    // ==================== EXERCISE SCHEDULING STATE ====================

    const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
    const [exerciseConfig, setExerciseConfig] = useState<ExerciseConfiguration | null>(null);

    // ==================== WORKOUT PLAN STATE ====================

    const [selectedWorkoutPlan, setSelectedWorkoutPlan] = useState<WorkoutPlanInfo | null>(null);
    const [showWorkoutPlanConfigModal, setShowWorkoutPlanConfigModal] = useState(false);

    // ==================== UI STATE ====================

    const [showExerciseSelector, setShowExerciseSelector] = useState(false);
    const [showConfigModal, setShowConfigModal] = useState(false);
    const [schedulingMode, setSchedulingMode] = useState<'exercise' | 'workout-plan'>('exercise');

    // ==================== EDITING STATE ====================

    const [editingExercise, setEditingExercise] = useState<ScheduledExercise | null>(null);
    const [isEditMode, setIsEditMode] = useState(false);

    // ==================== COMPUTED VALUES ====================

    const viewingDateString = useMemo(() => {
        return viewingDate.toISOString().split('T')[0];
    }, [viewingDate]);

    // Get exercises for the viewing date
    const viewingDateExercises = useMemo(() => {
        const dateString = viewingDate.toISOString().split('T')[0];
        return scheduledWorkouts.filter(workout => workout.scheduledDate === dateString);
    }, [viewingDate, scheduledWorkouts]);

    // ==================== DATA LOADING EFFECTS ====================

    // Load data when viewing date changes
    useEffect(() => {
        loadDayData();
        loadWorkoutStats();
    }, [viewingDateString]);

    const loadDayData = async () => {
        if (loading) {
            console.log('⏳ Already loading, skipping duplicate call');
            return;
        }

        try {
            setLoading(true);
            console.log('📅 Loading data for:', viewingDateString);

            // Load a week of data around the viewing date for context
            const startDate = new Date(viewingDate);
            startDate.setDate(viewingDate.getDate() - 3);
            const endDate = new Date(viewingDate);
            endDate.setDate(viewingDate.getDate() + 3);

            const startDateStr = startDate.toISOString().split('T')[0];
            const endDateStr = endDate.toISOString().split('T')[0];

            const apiResponse: ScheduledWorkoutResponse[] = await calendarApi.getScheduledWorkouts(startDateStr, endDateStr);
            console.log('📊 Raw API response (first 2 items):', apiResponse.slice(0, 2));

            const transformedWorkouts = transformScheduledWorkoutsToCalendarData(apiResponse);
            console.log('🔄 Transformed workouts (first 2):', transformedWorkouts.slice(0, 2).map(ex => ({
                id: ex.id,
                name: ex.exercise.name || ex.exercise.exerciseName,
                completed: ex.completed,
                status: (ex as any).status
            })));

            setScheduledWorkouts(transformedWorkouts);
            console.log(`✅ Loaded ${transformedWorkouts.length} scheduled workouts around ${viewingDate.toDateString()}`);
        } catch (error) {
            console.error('Error loading day data:', error);
            toast.error('Failed to load scheduled workouts');
            setScheduledWorkouts([]);
        } finally {
            setLoading(false);
        }
    };

    const loadWorkoutStats = async () => {
        try {
            const apiStats: WorkoutStatsResponse = await calendarApi.getWorkoutStats();
            const transformedStats: WorkoutStats = {
                totalWorkouts: apiStats.exercisesScheduledThisMonth || 0,
                completedWorkouts: apiStats.exercisesCompletedThisMonth || 0,
                completionRate: apiStats.completionRateThisMonth || 0,
                weeklyGoal: 5,
                currentStreak: apiStats.currentStreak || 0,
                bestStreak: apiStats.longestStreak || 0,
                totalExercisesCompleted: apiStats.totalWorkoutsCompleted || 0,
                averageWorkoutDuration: apiStats.totalMinutesWorkedOut ?
                    Math.round(apiStats.totalMinutesWorkedOut / Math.max(apiStats.totalWorkoutsCompleted, 1)) : 0
            };

            setStats(transformedStats);
        } catch (error) {
            console.error('Error loading workout stats:', error);
            setStats({
                totalWorkouts: 0,
                completedWorkouts: 0,
                completionRate: 0,
                weeklyGoal: 5,
                currentStreak: 0,
                bestStreak: 0,
                totalExercisesCompleted: 0,
                averageWorkoutDuration: 0
            });
        }
    };

    const refreshCalendarData = async () => {
        await loadDayData();
        await loadWorkoutStats();
    };

    // ==================== NAVIGATION HANDLERS ====================

    const navigateDay = (direction: 'prev' | 'next') => {
        setViewingDate(prev => {
            const newDate = new Date(prev);
            if (direction === 'prev') {
                newDate.setDate(prev.getDate() - 1);
            } else {
                newDate.setDate(prev.getDate() + 1);
            }
            return newDate;
        });
    };

    const goToToday = () => {
        setViewingDate(new Date());
    };

    // ==================== EXERCISE SCHEDULING HANDLERS ====================

    const handleExerciseSelect = (exercise: Exercise) => {
        setSelectedExercise(exercise);
        const defaultConfig = getDefaultConfigForExercise(exercise);
        setExerciseConfig(defaultConfig);
        setSchedulingMode('exercise');
        setShowExerciseSelector(false);
        setShowConfigModal(true);
    };

    // ==================== WORKOUT PLAN HANDLERS ====================

    const handleWorkoutPlanSelect = async (workoutPlan: WorkoutPlanInfo) => {
        try {
            setLoading(true);

            // Direct scheduling without configuration (simple approach)
            const scheduleRequest: WorkoutPlanScheduleRequest = {
                workoutPlanId: workoutPlan.id,
                scheduledDate: viewingDateString,
                customNotes: `${workoutPlan.name || 'Workout Plan'} - ${workoutPlan.description || ''}`
            };

            console.log('📋 Scheduling workout plan directly:', scheduleRequest);
            // await calendarApi.scheduleWorkout(scheduleRequest); // Uncomment when API is ready

            setShowExerciseSelector(false);
            await loadDayData();
            toast.success('Workout plan scheduled successfully!');

        } catch (error) {
            console.error('Error scheduling workout plan:', error);
            toast.error('Failed to schedule workout plan');
        } finally {
            setLoading(false);
        }
    };

    const handleWorkoutPlanConfigure = (workoutPlan: WorkoutPlanInfo) => {
        console.log('🔧 Configuring workout plan:', workoutPlan.name);
        setSelectedWorkoutPlan(workoutPlan);
        setShowExerciseSelector(false);
        setShowWorkoutPlanConfigModal(true);
    };

    /**
     * FIXED: Handle saving a configured workout plan as a single session
     * This now matches the WorkoutPlanConfigModal's expected interface
     */
    const handleWorkoutPlanConfigSaveAsync = async (config: WorkoutPlanConfiguration): Promise<void> => {
        try {
            setLoading(true);

            console.log('💾 Saving single workout session:', config);

            // Create schedule data for individual exercises from the workout plan
            const exerciseSchedulePromises = config.exerciseConfigs
                .filter(exerciseConfig => !exerciseConfig.skip) // Only schedule non-skipped exercises
                .map(async (exerciseConfig, index) => {
                    const scheduleData: any = {
                        exerciseId: exerciseConfig.exerciseId,
                        scheduledDate: config.scheduledDate,
                        notes: exerciseConfig.notes || config.planNotes || `${selectedWorkoutPlan?.name || 'Workout Plan'} - Exercise ${index + 1}`
                    };

                    // Add configuration based on tracking mode
                    const exerciseConfiguration = exerciseConfig.configuration;
                    if (exerciseConfiguration.trackingMode === 'strength') {
                        const strengthConfig = exerciseConfiguration as StrengthConfiguration;
                        scheduleData.sets = strengthConfig.targetSets;
                        scheduleData.reps = strengthConfig.targetReps?.toString();
                        scheduleData.weight = strengthConfig.targetWeight;
                        scheduleData.restSeconds = strengthConfig.restSeconds;
                        scheduleData.targetRpe = strengthConfig.targetRpe;
                        scheduleData.tempo = strengthConfig.tempo;
                    } else if (exerciseConfiguration.trackingMode === 'cardio') {
                        const cardioConfig = exerciseConfiguration as CardioConfiguration;
                        scheduleData.targetDurationMinutes = cardioConfig.targetDurationMinutes;
                        scheduleData.targetDistanceKm = cardioConfig.targetDistanceKm;
                        scheduleData.targetPace = cardioConfig.targetPace;
                    } else if (exerciseConfiguration.trackingMode === 'isometric') {
                        const isometricConfig = exerciseConfiguration as IsometricConfiguration;
                        scheduleData.sets = isometricConfig.targetSets;
                        scheduleData.holdDurationSeconds = isometricConfig.holdDurationSeconds;
                        scheduleData.restSeconds = isometricConfig.restSeconds;
                    }

                    console.log(`📡 Scheduling exercise ${exerciseConfig.exerciseId}:`, scheduleData);
                    return calendarApi.scheduleIndividualExercise(scheduleData);
                });

            // Wait for all exercises to be scheduled
            const results = await Promise.all(exerciseSchedulePromises);
            const successCount = results.filter(result => result !== null).length;

            console.log(`✅ Scheduled ${successCount} exercises from workout plan`);

            // Fallback: Use the existing scheduleWorkout method with workout plan if no individual exercises were scheduled
            if (successCount === 0 && config.exerciseConfigs.length > 0) {
                const fallbackScheduleData = {
                    workoutPlanId: config.workoutPlanId,
                    scheduledDate: config.scheduledDate,
                    customNotes: config.planNotes || `${selectedWorkoutPlan?.name || 'Workout Plan'}`
                };

                await calendarApi.scheduleWorkout(fallbackScheduleData);
                console.log('✅ Scheduled workout plan using fallback method');
            }

            // Close the modal and refresh the calendar
            setSelectedWorkoutPlan(null);
            setShowWorkoutPlanConfigModal(false);

            // Refresh calendar data to show the new scheduled workout
            await refreshCalendarData();

            toast.success(`Workout "${selectedWorkoutPlan?.name}" added to ${viewingDate.toDateString()}!`);

        } catch (error) {
            console.error('❌ Error scheduling workout session:', error);
            toast.error('Failed to schedule workout session');
            throw error; // Re-throw to let the modal handle loading state
        } finally {
            setLoading(false);
        }
    };

    /**
     * Wrapper function to match the expected non-async signature from WorkoutPlanConfigModal
     */
    const handleWorkoutPlanConfigSave = (config: WorkoutPlanConfiguration): void => {
        // Call the async version and handle any errors
        handleWorkoutPlanConfigSaveAsync(config).catch((error) => {
            console.error('Error in workout plan config save:', error);
            // Error handling is already done in the async function
        });
    };

    const resetWorkoutPlanState = () => {
        setSelectedWorkoutPlan(null);
        setShowWorkoutPlanConfigModal(false);
    };

    // ==================== INDIVIDUAL EXERCISE HANDLERS ====================

    const handleConfigChange = (config: ExerciseConfiguration) => {
        setExerciseConfig(config);
    };

    const handleSaveExercise = async () => {
        if (isEditMode) {
            return handleSaveEditedExercise();
        }

        if (schedulingMode === 'workout-plan') {
            return handleSaveWorkoutPlan();
        }

        if (!selectedExercise || !exerciseConfig) {
            toast.error('Please select an exercise and configure it properly');
            return;
        }

        setLoading(true);
        try {
            console.log('💾 Saving individual exercise:', {
                exercise: selectedExercise,
                config: exerciseConfig,
                date: viewingDate.toISOString().split('T')[0]
            });

            // Create proper schedule data with exercise configuration
            const scheduleData: any = {
                exerciseId: selectedExercise.id,
                scheduledDate: viewingDate.toISOString().split('T')[0],
                notes: exerciseConfig.notes || `${selectedExercise.name || selectedExercise.exerciseName}`
            };

            // Add configuration based on tracking mode with correct field names
            if (exerciseConfig.trackingMode === 'strength') {
                const strengthConfig = exerciseConfig as StrengthConfiguration;
                scheduleData.sets = strengthConfig.targetSets;
                scheduleData.reps = strengthConfig.targetReps?.toString();
                scheduleData.weight = strengthConfig.targetWeight;
                scheduleData.restSeconds = strengthConfig.restSeconds;
                scheduleData.targetRpe = strengthConfig.targetRpe;
                scheduleData.tempo = strengthConfig.tempo;
            } else if (exerciseConfig.trackingMode === 'cardio') {
                const cardioConfig = exerciseConfig as CardioConfiguration;
                scheduleData.targetDurationMinutes = cardioConfig.targetDurationMinutes;
                scheduleData.targetDistanceKm = cardioConfig.targetDistanceKm;
                scheduleData.targetPace = cardioConfig.targetPace;
            } else if (exerciseConfig.trackingMode === 'isometric') {
                const isometricConfig = exerciseConfig as IsometricConfiguration;
                scheduleData.sets = isometricConfig.targetSets;
                scheduleData.holdDurationSeconds = isometricConfig.holdDurationSeconds;
                scheduleData.restSeconds = isometricConfig.restSeconds;
            }

            console.log('📡 Sending individual exercise schedule request:', scheduleData);

            const response = await calendarApi.scheduleIndividualExercise(scheduleData);

            toast.success('Exercise scheduled successfully!');
            console.log('✅ Individual exercise scheduled:', response);

            await loadDayData();
            resetSchedulingState();
        } catch (error) {
            console.error('❌ Error scheduling individual exercise:', error);
            toast.error('Failed to schedule exercise. Please check your configuration.');
        } finally {
            setLoading(false);
        }
    };

    const handleSaveWorkoutPlan = async () => {
        if (!selectedWorkoutPlan) {
            toast.error('Please select a workout plan');
            return;
        }

        setLoading(true);
        try {
            console.log('📋 Saving workout plan:', {
                workoutPlan: selectedWorkoutPlan,
                date: viewingDate.toISOString().split('T')[0]
            });

            const scheduleData = {
                workoutPlanId: selectedWorkoutPlan.id,
                scheduledDate: viewingDate.toISOString().split('T')[0],
                customNotes: `Workout Plan: ${selectedWorkoutPlan.name}`
            };

            console.log('📡 Sending workout plan schedule request:', scheduleData);

            const response = await calendarApi.scheduleWorkout(scheduleData);

            toast.success('Workout plan scheduled successfully!');
            console.log('✅ Workout plan scheduled:', response);

            await loadDayData();
            resetSchedulingState();
        } catch (error) {
            console.error('❌ Error scheduling workout plan:', error);
            toast.error('Failed to schedule workout plan.');
        } finally {
            setLoading(false);
        }
    };

    // ==================== EDITING HANDLERS ====================

    const handleEditExercise = (scheduledExercise: ScheduledExercise) => {
        console.log('🔧 Editing exercise:', scheduledExercise);

        setEditingExercise(scheduledExercise);
        setSelectedExercise(scheduledExercise.exercise);

        // Convert scheduled exercise back to configuration
        const editConfig = convertScheduledExerciseToConfig(scheduledExercise);
        setExerciseConfig(editConfig);

        setSchedulingMode('exercise');
        setIsEditMode(true);
        setShowConfigModal(true);
    };

    const handleSaveEditedExercise = async () => {
        if (!editingExercise || !selectedExercise || !exerciseConfig) {
            toast.error('Missing exercise or configuration data');
            return;
        }

        setLoading(true);
        try {
            console.log('💾 Updating exercise:', {
                exerciseId: editingExercise.id,
                exercise: selectedExercise,
                config: exerciseConfig
            });

            const updateData: any = {
                exerciseId: selectedExercise.id,
                scheduledDate: viewingDate.toISOString().split('T')[0],
                notes: exerciseConfig.notes || `${selectedExercise.name || selectedExercise.exerciseName}`
            };

            // Add configuration based on tracking mode with correct field names
            if (exerciseConfig.trackingMode === 'strength') {
                const strengthConfig = exerciseConfig as StrengthConfiguration;
                updateData.sets = strengthConfig.targetSets;
                updateData.reps = strengthConfig.targetReps?.toString();
                updateData.weight = strengthConfig.targetWeight;
                updateData.restSeconds = strengthConfig.restSeconds;
                updateData.targetRpe = strengthConfig.targetRpe;
                updateData.tempo = strengthConfig.tempo;
            } else if (exerciseConfig.trackingMode === 'cardio') {
                const cardioConfig = exerciseConfig as CardioConfiguration;
                updateData.targetDurationMinutes = cardioConfig.targetDurationMinutes;
                updateData.targetDistanceKm = cardioConfig.targetDistanceKm;
                updateData.targetPace = cardioConfig.targetPace;
            } else if (exerciseConfig.trackingMode === 'isometric') {
                const isometricConfig = exerciseConfig as IsometricConfiguration;
                updateData.sets = isometricConfig.targetSets;
                updateData.holdDurationSeconds = isometricConfig.holdDurationSeconds;
                updateData.restSeconds = isometricConfig.restSeconds;
            }

            console.log('📡 Sending exercise update request:', updateData);

            await calendarApi.updateScheduledExercise(editingExercise.id, updateData);

            toast.success('Exercise updated successfully!');
            await loadDayData();
            resetEditingState();
        } catch (error) {
            console.error('❌ Error updating exercise:', error);
            toast.error('Failed to update exercise. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const convertScheduledExerciseToConfig = (scheduledExercise: ScheduledExercise): ExerciseConfiguration => {
        const exercise = scheduledExercise.exercise;

        if (exercise.isCardio) {
            return {
                trackingMode: 'cardio',
                targetDurationMinutes: scheduledExercise.targetDurationMinutes || exercise.estimatedDurationMinutes || 20,
                targetDistanceKm: scheduledExercise.targetDistanceKm,
                targetPace: scheduledExercise.targetPace,
                notes: scheduledExercise.notes || ''
            } as CardioConfiguration;
        } else if (exercise.isIsometric) {
            return {
                trackingMode: 'isometric',
                targetSets: scheduledExercise.targetSets || 3,
                holdDurationSeconds: scheduledExercise.holdDurationSeconds || 30,
                restSeconds: scheduledExercise.restSeconds || 60,
                notes: scheduledExercise.notes || ''
            } as IsometricConfiguration;
        } else {
            return {
                trackingMode: 'strength',
                targetSets: scheduledExercise.targetSets || 3,
                targetReps: scheduledExercise.targetReps || 10,
                targetWeight: scheduledExercise.targetWeight,
                targetWeightUnit: scheduledExercise.targetWeightUnit || 'lbs',
                restSeconds: scheduledExercise.restSeconds || 90,
                targetRpe: scheduledExercise.targetRpe,
                tempo: scheduledExercise.tempo,
                notes: scheduledExercise.notes || ''
            } as StrengthConfiguration;
        }
    };

    const resetEditingState = () => {
        setEditingExercise(null);
        setIsEditMode(false);
        setSelectedExercise(null);
        setExerciseConfig(null);
        setSelectedWorkoutPlan(null);
        setSchedulingMode('exercise');
        setShowConfigModal(false);
    };

    // ==================== SCHEDULING MODE HANDLERS ====================

    const handleModeChange = (mode: 'exercise' | 'workout-plan') => {
        setSchedulingMode(mode);

        // Clear opposite mode selections
        if (mode === 'exercise') {
            setSelectedWorkoutPlan(null);
        } else {
            setSelectedExercise(null);
            setExerciseConfig(null);
        }
    };

    const resetSchedulingState = () => {
        setSelectedExercise(null);
        setExerciseConfig(null);
        setSelectedWorkoutPlan(null);
        setSchedulingMode('exercise');
        setShowConfigModal(false);
    };

    // ==================== WORKOUT MANAGEMENT HANDLERS ====================

    const handleCompleteWorkout = async (workoutId: string) => {
        try {
            await calendarApi.startWorkout(workoutId);
            toast.success('Workout completed!');
            await loadDayData();
            await loadWorkoutStats();
        } catch (error) {
            console.error('Error completing workout:', error);
            toast.error('Failed to complete workout');
        }
    };

    const handleDeleteWorkout = async (workoutId: string) => {
        console.log('🗑️ DELETE CLICKED - Workout ID:', workoutId);

        // Find the workout in current data to check its status
        const workout = viewingDateExercises.find(ex => ex.id === workoutId);
        console.log('🔍 Found workout:', workout);

        if (workout) {
            console.log('🔍 Workout status check:', {
                completed: workout.completed,
                status: (workout as any).status
            });

            // Only block deletion for COMPLETED workouts
            if (workout.completed) {
                console.log('❌ Workout is completed, cannot delete');
                toast('Cannot delete completed workout', {
                    icon: 'ℹ️',
                    style: {
                        borderRadius: '10px',
                        background: '#3b82f6',
                        color: '#fff',
                    },
                });
                return;
            }
        }

        console.log('✅ Proceeding with delete confirmation');

        // Show confirmation dialog
        if (!window.confirm('Are you sure you want to delete this workout?')) {
            console.log('❌ User cancelled delete confirmation');
            return;
        }

        console.log('🚀 Starting API delete call');

        try {
            await calendarApi.deleteWorkout(workoutId);
            toast.success('Workout deleted successfully');
            await loadDayData();
        } catch (error: any) {
            console.error('Error deleting workout:', error);

            // Handle specific errors gracefully
            if (error.message?.includes('current state: CANCELLED') ||
                error.message?.includes('Cannot cancel workout')) {

                toast('This workout was already cancelled', {
                    icon: 'ℹ️',
                    style: {
                        borderRadius: '10px',
                        background: '#3b82f6',
                        color: '#fff',
                    },
                });
                await loadDayData();

            } else if (error.message?.includes('current state: COMPLETED')) {
                toast('Cannot delete completed workout', {
                    icon: 'ℹ️',
                    style: {
                        borderRadius: '10px',
                        background: '#3b82f6',
                        color: '#fff',
                    },
                });
                await loadDayData();

            } else if (error.message?.includes('409') || error.message?.includes('Conflict')) {
                toast('Cannot delete workout in its current state', {
                    icon: '⚠️',
                    style: {
                        borderRadius: '10px',
                        background: '#f59e0b',
                        color: '#fff',
                    },
                });
                await loadDayData();

            } else {
                toast.error('Failed to delete workout');
            }
        }
    };

    const startWorkoutMode = () => {
        toast.success('Starting workout mode!');
        // You can integrate with your WorkoutModeOverlay here
    };

    // ==================== DATE INFORMATION HELPERS ====================

    const isToday = () => {
        const today = new Date();
        return viewingDate.toDateString() === today.toDateString();
    };

    const isPast = () => {
        const today = new Date();
        return viewingDate < today;
    };

    const isFuture = () => {
        const today = new Date();
        return viewingDate > today;
    };

    const getDateDisplayInfo = () => {
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(today.getDate() - 1);
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);

        if (viewingDate.toDateString() === today.toDateString()) {
            return {
                title: 'Today',
                subtitle: viewingDate.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' }),
                emoji: '🎯',
                bgColor: 'from-blue-500 to-green-500',
                textColor: 'text-white'
            };
        } else if (viewingDate.toDateString() === yesterday.toDateString()) {
            return {
                title: 'Yesterday',
                subtitle: viewingDate.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' }),
                emoji: '📅',
                bgColor: 'from-gray-400 to-gray-500',
                textColor: 'text-white'
            };
        } else if (viewingDate.toDateString() === tomorrow.toDateString()) {
            return {
                title: 'Tomorrow',
                subtitle: viewingDate.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' }),
                emoji: '✨',
                bgColor: 'from-purple-500 to-pink-500',
                textColor: 'text-white'
            };
        } else {
            return {
                title: viewingDate.toLocaleDateString('en-US', { weekday: 'long' }),
                subtitle: viewingDate.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' }),
                emoji: '📆',
                bgColor: 'from-gray-600 to-gray-700',
                textColor: 'text-white'
            };
        }
    };

    const dateInfo = getDateDisplayInfo();

    // Generate week context for mini calendar
    const getWeekContext = () => {
        const startOfWeek = new Date(viewingDate);
        const day = startOfWeek.getDay();
        startOfWeek.setDate(viewingDate.getDate() - day);

        const weekDays = [];
        for (let i = 0; i < 7; i++) {
            const date = new Date(startOfWeek);
            date.setDate(startOfWeek.getDate() + i);
            const dateString = date.toISOString().split('T')[0];
            const dayExercises = scheduledWorkouts.filter(workout => workout.scheduledDate === dateString);

            weekDays.push({
                date,
                dateString,
                isViewing: date.toDateString() === viewingDate.toDateString(),
                isToday: date.toDateString() === new Date().toDateString(),
                exerciseCount: dayExercises.length,
                completedCount: dayExercises.filter(ex => ex.completed).length
            });
        }
        return weekDays;
    };

    // ==================== UTILITY FUNCTIONS ====================

    const getConfigurationDisplay = (exercise: ScheduledExercise) => {
        const exerciseType = exercise.exercise;

        if (exerciseType.isCardio) {
            const details = [];

            // Priority 1: Duration (most important for cardio)
            const duration = exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes;
            if (duration) {
                details.push(`⏱️ ${duration} min`);
            }

            // Priority 2: Distance (if available)
            if (exercise.targetDistance) {
                const unit = exercise.targetDistanceUnit === 'km' ? 'km' : 'mi';
                details.push(`📍 ${exercise.targetDistance}${unit}`);
            } else if (exercise.targetDistanceKm) {
                details.push(`📍 ${exercise.targetDistanceKm}km`);
            }

            // Priority 3: Pace (if available)
            if (exercise.targetPace) {
                const paceUnit = exercise.targetDistanceUnit === 'km' ? '/km' : '/mi';
                const minutes = Math.floor(exercise.targetPace);
                const seconds = Math.round((exercise.targetPace - minutes) * 60);
                const paceDisplay = `${minutes}:${seconds.toString().padStart(2, '0')}`;
                details.push(`⚡ ${paceDisplay}${paceUnit}`);
            }

            // Priority 4: Sets/Rounds (only if interval cardio)
            if (exercise.targetSets && exercise.targetSets > 1) {
                details.push(`🔄 ${exercise.targetSets} rounds`);

                if (exercise.restSeconds && exercise.restSeconds > 0) {
                    details.push(`💤 ${exercise.restSeconds}s rest`);
                }
            }

            return {
                text: details.length > 0 ? details.join(' • ') : '❤️ Cardio workout',
                bgColor: 'bg-red-50',
                textColor: 'text-red-700',
                borderColor: 'border-red-200',
                iconColor: 'text-red-600'
            };
        } else if (exerciseType.isIsometric) {
            const details = [];
            if (exercise.targetSets) details.push(`🔄 ${exercise.targetSets} sets`);
            if (exercise.holdDurationSeconds) details.push(`⏱️ ${exercise.holdDurationSeconds}s hold`);
            if (exercise.restSeconds) details.push(`💤 ${exercise.restSeconds}s rest`);

            return {
                text: details.length > 0 ? details.join(' • ') : '🛡️ Isometric holds',
                bgColor: 'bg-purple-50',
                textColor: 'text-purple-700',
                borderColor: 'border-purple-200',
                iconColor: 'text-purple-600'
            };
        } else {
            // Strength exercises
            const details = [];
            if (exercise.targetSets) details.push(`🔄 ${exercise.targetSets} sets`);
            if (exercise.targetReps) details.push(`🎯 ${exercise.targetReps} reps`);

            if (exercise.targetWeight) {
                const unit = exercise.targetWeightUnit || 'lbs';
                details.push(`⚖️ ${exercise.targetWeight}${unit}`);
            }

            if (exercise.restSeconds) details.push(`⏱️ ${exercise.restSeconds}s rest`);
            if (exercise.targetRpe) details.push(`💪 RPE ${exercise.targetRpe}`);
            if (exercise.tempo) details.push(`🎵 ${exercise.tempo}`);

            return {
                text: details.length > 0 ? details.join(' • ') : '💪 Strength training',
                bgColor: 'bg-blue-50',
                textColor: 'text-blue-700',
                borderColor: 'border-blue-200',
                iconColor: 'text-blue-600'
            };
        }
    };

    const getStatusIcon = (exercise: ScheduledExercise) => {
        if (exercise.completed) {
            return <CheckCircle className="w-4 h-4 sm:w-5 sm:h-5 text-green-500" />;
        }
        return <Clock className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500" />;
    };

    // ==================== RENDER ====================

    return (
        <div className="w-full min-h-screen bg-gray-50 pb-20">
            <div className="px-3 sm:px-4 lg:px-6 py-3 sm:py-4 lg:py-6 space-y-4 sm:space-y-6 max-w-4xl mx-auto">

                {/* Today-Focused Hero Header - Mobile Optimized */}
                <div className={`bg-gradient-to-r ${dateInfo.bgColor} rounded-xl sm:rounded-2xl lg:rounded-3xl p-4 sm:p-6 lg:p-8 ${dateInfo.textColor} shadow-lg`}>
                    <div className="text-center space-y-2 sm:space-y-3">
                        <div className="text-3xl sm:text-4xl lg:text-6xl">{dateInfo.emoji}</div>
                        <div>
                            <h1 className="text-xl sm:text-2xl lg:text-4xl font-bold">{dateInfo.title}</h1>
                            <p className="text-xs sm:text-sm lg:text-lg opacity-90 mt-1">{dateInfo.subtitle}</p>
                        </div>

                        {/* Day Navigation - Mobile Optimized */}
                        <div className="flex items-center justify-center gap-2 sm:gap-3 lg:gap-4 mt-4 sm:mt-6">
                            <Button
                                variant="secondary"
                                size="sm"
                                onClick={() => navigateDay('prev')}
                                className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-2 sm:px-3 lg:px-4"
                            >
                                <ChevronLeft className="w-4 h-4" />
                                <span className="hidden sm:inline ml-1">Yesterday</span>
                            </Button>

                            {!isToday() && (
                                <Button
                                    variant="secondary"
                                    size="sm"
                                    onClick={goToToday}
                                    className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-3 sm:px-4 lg:px-6"
                                >
                                    Today
                                </Button>
                            )}

                            <Button
                                variant="secondary"
                                size="sm"
                                onClick={() => navigateDay('next')}
                                className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-2 sm:px-3 lg:px-4"
                            >
                                <span className="hidden sm:inline mr-1">Tomorrow</span>
                                <ChevronRight className="w-4 h-4" />
                            </Button>
                        </div>

                        {/* Exercise Count Summary - Mobile Grid */}
                        <div className="grid grid-cols-3 gap-3 sm:gap-4 lg:gap-6 mt-4 sm:mt-6 max-w-sm mx-auto">
                            <div className="text-center">
                                <div className="text-lg sm:text-xl lg:text-2xl font-bold">{viewingDateExercises.length}</div>
                                <div className="text-xs sm:text-sm opacity-80">Planned</div>
                            </div>
                            <div className="text-center">
                                <div className="text-lg sm:text-xl lg:text-2xl font-bold">{viewingDateExercises.filter(ex => ex.completed).length}</div>
                                <div className="text-xs sm:text-sm opacity-80">Done</div>
                            </div>
                            <div className="text-center">
                                <div className="text-lg sm:text-xl lg:text-2xl font-bold">
                                    {viewingDateExercises.reduce((total, ex) => total + (ex.exercise.estimatedDurationMinutes || 0), 0)}
                                </div>
                                <div className="text-xs sm:text-sm opacity-80">Minutes</div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Start Workout Button - Mobile Optimized */}
                {isToday() && viewingDateExercises.length > 0 && (
                    <Button
                        className="w-full bg-green-600 hover:bg-green-700 text-white py-3 sm:py-4 lg:py-6 text-base sm:text-lg lg:text-xl font-bold flex items-center justify-center gap-2 sm:gap-3 shadow-lg rounded-xl sm:rounded-2xl"
                        onClick={startWorkoutMode}
                    >
                        <Play className="w-5 h-5 sm:w-6 sm:h-6 lg:w-8 lg:h-8" />
                        Start Today's Workout
                    </Button>
                )}

                {/* Week Context Mini Calendar - Mobile Optimized */}
                <Card className="shadow-sm">
                    <CardHeader className="pb-2 sm:pb-3">
                        <CardTitle className="text-sm sm:text-base text-gray-600 flex items-center gap-2">
                            <Calendar className="w-4 h-4" />
                            Week Overview
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-0">
                        <div className="grid grid-cols-7 gap-1 sm:gap-2">
                            {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((day, index) => (
                                <div key={day + index} className="text-center text-xs sm:text-sm font-medium text-gray-500 pb-2">
                                    <span className="sm:hidden">{day}</span>
                                    <span className="hidden sm:inline">
                                        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][index]}
                                    </span>
                                </div>
                            ))}
                            {getWeekContext().map((day, index) => (
                                <div
                                    key={index}
                                    className={`
                                        p-2 sm:p-3 rounded-lg cursor-pointer transition-all duration-200 text-center min-h-[50px] sm:min-h-[60px] flex flex-col justify-center
                                        ${day.isViewing ? 'bg-blue-500 text-white shadow-md scale-105' : 'bg-white hover:bg-gray-100'}
                                        ${day.isToday && !day.isViewing ? 'border-2 border-blue-300' : 'border border-gray-200'}
                                    `}
                                    onClick={() => setViewingDate(day.date)}
                                >
                                    <div className={`text-sm sm:text-base font-bold ${day.isViewing ? 'text-white' : day.isToday ? 'text-blue-600' : 'text-gray-900'}`}>
                                        {day.date.getDate()}
                                    </div>
                                    {day.exerciseCount > 0 && (
                                        <div className="flex justify-center gap-0.5 mt-1">
                                            {Array.from({ length: Math.min(day.exerciseCount, 4) }).map((_, idx) => (
                                                <div
                                                    key={idx}
                                                    className={`w-1 h-1 sm:w-1.5 sm:h-1.5 rounded-full ${
                                                        day.isViewing
                                                            ? 'bg-white'
                                                            : idx < day.completedCount
                                                                ? 'bg-green-500'
                                                                : 'bg-blue-500'
                                                    }`}
                                                />
                                            ))}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </CardContent>
                </Card>

                {/* Day's Exercises - Enhanced Display */}
                {viewingDateExercises.length > 0 ? (
                    <Card className="shadow-sm">
                        <CardHeader className="pb-3 sm:pb-4">
                            <CardTitle className="text-base sm:text-lg lg:text-xl flex items-center justify-between">
                                <span>Scheduled Exercises</span>
                                <Badge className={`text-xs sm:text-sm ${isToday() ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>
                                    {viewingDateExercises.filter(ex => ex.completed).length} / {viewingDateExercises.length} Complete
                                </Badge>
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="pt-0">
                            <div className="space-y-3 sm:space-y-4">
                                {viewingDateExercises.map((exercise, index) => (
                                    <div
                                        key={exercise.id}
                                        className={`
                                            border-2 rounded-lg sm:rounded-xl p-3 sm:p-4 lg:p-6 transition-all duration-200
                                            ${exercise.completed
                                            ? 'border-green-200 bg-green-50 hover:bg-green-100'
                                            : 'border-gray-200 bg-white hover:bg-gray-50 hover:border-blue-300'
                                        }
                                        `}
                                    >
                                        <div className="flex items-start justify-between mb-3">
                                            <div className="flex items-start gap-3 min-w-0 flex-1">
                                                <div className="flex-shrink-0 mt-0.5">
                                                    <div className={`
                                                        w-6 h-6 sm:w-8 sm:h-8 lg:w-10 lg:h-10 rounded-full flex items-center justify-center font-bold text-xs sm:text-sm lg:text-base
                                                        ${exercise.completed ? 'bg-green-500 text-white' : 'bg-blue-500 text-white'}
                                                    `}>
                                                        {index + 1}
                                                    </div>
                                                </div>
                                                <div className="min-w-0 flex-1">
                                                    <div className="flex items-center gap-2 mb-2">
                                                        {getStatusIcon(exercise)}
                                                        <h3 className="font-bold text-sm sm:text-base lg:text-lg text-gray-900 truncate">
                                                            {exercise.exercise.name || exercise.exercise.exerciseName}
                                                        </h3>
                                                    </div>

                                                    {/* Enhanced Configuration Details */}
                                                    <div className={`${getConfigurationDisplay(exercise).bgColor} rounded-lg p-3 mb-3 border ${getConfigurationDisplay(exercise).borderColor}`}>
                                                        <div className="flex items-center gap-2 mb-2">
                                                            <Weight className={`w-4 h-4 ${getConfigurationDisplay(exercise).iconColor}`} />
                                                            <span className={`text-sm font-medium ${getConfigurationDisplay(exercise).textColor}`}>
                                                                Configuration
                                                            </span>
                                                        </div>
                                                        <p className={`text-sm ${getConfigurationDisplay(exercise).textColor} font-medium`}>
                                                            {getConfigurationDisplay(exercise).text}
                                                        </p>
                                                        {exercise.notes && (
                                                            <p className={`text-xs ${getConfigurationDisplay(exercise).textColor} mt-1 italic opacity-80`}>
                                                                "{exercise.notes}"
                                                            </p>
                                                        )}
                                                    </div>

                                                    {exercise.exercise.description && (
                                                        <p className="text-xs sm:text-sm text-gray-600 mb-3 line-clamp-2">
                                                            {exercise.exercise.description}
                                                        </p>
                                                    )}

                                                    <div className="flex flex-wrap gap-1 sm:gap-2 mb-3">
                                                        <Badge variant="secondary" className="text-xs">
                                                            {exercise.exercise.exerciseType}
                                                        </Badge>
                                                        <Badge variant="outline" className="text-xs">
                                                            {exercise.exercise.difficultyLevel}
                                                        </Badge>
                                                        {exercise.exercise.isCardio && (
                                                            <Badge variant="outline" className="text-xs text-red-700 bg-red-50">
                                                                ❤️ Cardio
                                                            </Badge>
                                                        )}
                                                        {exercise.exercise.isIsometric && (
                                                            <Badge variant="outline" className="text-xs text-purple-700 bg-purple-50">
                                                                🛡️ Hold
                                                            </Badge>
                                                        )}
                                                    </div>

                                                    <div className="flex items-center gap-3 sm:gap-4 text-xs sm:text-sm text-gray-600">
                                                        <div className="flex items-center gap-1">
                                                            <Clock className="w-3 h-3 sm:w-4 sm:h-4" />
                                                            <span>{exercise.exercise.estimatedDurationMinutes} min</span>
                                                        </div>
                                                        <div className="flex items-center gap-1">
                                                            <Target className="w-3 h-3 sm:w-4 sm:h-4" />
                                                            <span>{exercise.exercise.estimatedCalories} cal</span>
                                                        </div>
                                                        {exercise.exercise.averageRating > 0 && (
                                                            <div className="flex items-center gap-1">
                                                                <span>⭐</span>
                                                                <span>{exercise.exercise.averageRating.toFixed(1)}</span>
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                            </div>

                                            {/* Action Buttons - Mobile Stacked */}
                                            <div className="flex flex-col gap-2 flex-shrink-0 ml-2 sm:ml-3">
                                                {!exercise.completed && (
                                                    <>
                                                        <Button
                                                            size="sm"
                                                            onClick={() => handleCompleteWorkout(exercise.id)}
                                                            className="bg-green-600 hover:bg-green-700 text-white px-2 sm:px-3 py-1 sm:py-2 text-xs sm:text-sm"
                                                        >
                                                            <CheckCircle className="w-3 h-3 sm:w-4 sm:h-4 sm:mr-1" />
                                                            <span className="hidden sm:inline">Complete</span>
                                                        </Button>
                                                        <Button
                                                            size="sm"
                                                            variant="outline"
                                                            onClick={() => handleEditExercise(exercise)}
                                                            className="text-blue-600 hover:bg-blue-50 border-blue-200 px-2 sm:px-3 py-1 sm:py-2 text-xs sm:text-sm"
                                                        >
                                                            <span className="sm:hidden">✏️</span>
                                                            <span className="hidden sm:inline">Edit</span>
                                                        </Button>
                                                    </>
                                                )}
                                                <Button
                                                    size="sm"
                                                    variant="outline"
                                                    onClick={() => handleDeleteWorkout(exercise.id)}
                                                    className="text-red-600 hover:bg-red-50 border-red-200 px-2 sm:px-3 py-1 sm:py-2 text-xs sm:text-sm"
                                                >
                                                    <span className="sm:hidden">✗</span>
                                                    <span className="hidden sm:inline">Delete</span>
                                                </Button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </CardContent>
                    </Card>
                ) : (
                    /* Enhanced Empty State */
                    <Card className="shadow-sm border-dashed border-2 border-gray-300">
                        <CardContent className="py-8 sm:py-12 lg:py-16 text-center">
                            <div className="text-4xl sm:text-5xl lg:text-6xl mb-3 sm:mb-4 lg:mb-6">
                                {isToday() ? '🎯' : isPast() ? '📅' : '✨'}
                            </div>
                            <h3 className="text-base sm:text-lg lg:text-xl font-bold text-gray-900 mb-2">
                                {isToday() ? 'No workouts planned for today' :
                                    isPast() ? 'No workouts were scheduled' :
                                        'No workouts planned'}
                            </h3>
                            <p className="text-sm sm:text-base text-gray-600 mb-4 sm:mb-6">
                                {isToday() ? 'Ready to start your fitness journey?' :
                                    isPast() ? 'You can still log a workout you did' :
                                        'Plan ahead for a successful workout'}
                            </p>
                            <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
                                <Button
                                    onClick={() => {
                                        setSchedulingMode('exercise');
                                        setShowExerciseSelector(true);
                                    }}
                                    className="bg-blue-600 hover:bg-blue-700 text-white px-4 sm:px-6 py-2 sm:py-3 text-sm sm:text-base font-semibold rounded-lg sm:rounded-xl flex items-center justify-center"
                                >
                                    <Plus className="w-4 h-4 sm:w-5 sm:h-5 mr-2" />
                                    💪 Add Exercise
                                </Button>
                                <Button
                                    onClick={() => {
                                        setSchedulingMode('workout-plan');
                                        setShowExerciseSelector(true);
                                    }}
                                    className="bg-purple-600 hover:bg-purple-700 text-white px-4 sm:px-6 py-2 sm:py-3 text-sm sm:text-base font-semibold rounded-lg sm:rounded-xl flex items-center justify-center"
                                >
                                    <Settings className="w-4 h-4 sm:w-5 sm:h-5 mr-2" />
                                    📋 Add Workout Plan
                                </Button>
                            </div>
                        </CardContent>
                    </Card>
                )}

                {/* Quick Stats - Mobile Grid */}
                {(viewingDateExercises.length > 0 || isToday()) && stats && (
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 sm:gap-3 lg:gap-4">
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div className="text-base sm:text-lg lg:text-xl font-bold text-blue-600">{stats.currentStreak}</div>
                                <div className="text-xs sm:text-sm text-gray-500">Day Streak</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div className="text-base sm:text-lg lg:text-xl font-bold text-green-600">{stats.completedWorkouts}</div>
                                <div className="text-xs sm:text-sm text-gray-500">This Month</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div className="text-base sm:text-lg lg:text-xl font-bold text-purple-600">{Math.round(stats.completionRate || 0)}%</div>
                                <div className="text-xs sm:text-sm text-gray-500">Success</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div className="text-base sm:text-lg lg:text-xl font-bold text-orange-600">{stats.averageWorkoutDuration}</div>
                                <div className="text-xs sm:text-sm text-gray-500">Avg Min</div>
                            </CardContent>
                        </Card>
                    </div>
                )}
            </div>

            {/* Floating Action Button */}
            <FloatingActionButton
                onClick={() => {
                    setSchedulingMode('exercise');
                    setShowExerciseSelector(true);
                }}
                isWorkoutMode={false}
            />

            {/* Enhanced Exercise Selector Modal with Workout Plan Support */}
            {showExerciseSelector && (
                <EnhancedExerciseSelector
                    open={showExerciseSelector}
                    onClose={() => setShowExerciseSelector(false)}
                    onExerciseSelect={handleExerciseSelect}
                    onWorkoutPlanSelect={handleWorkoutPlanSelect}
                    onWorkoutPlanConfigure={handleWorkoutPlanConfigure}
                    selectedDate={viewingDateString}
                    calendarDays={getWeekContext().map(day => ({
                        date: day.date,
                        dateString: day.dateString,
                        isToday: day.isToday,
                        isPast: day.date < new Date(),
                        exercises: scheduledWorkouts.filter(workout => workout.scheduledDate === day.dateString)
                    }))}
                    onDateChange={(dateString) => {
                        const newDate = new Date(dateString);
                        setViewingDate(newDate);
                    }}
                    title={`Add to ${isToday() ? 'Today' : dateInfo.title}`}
                    initialTab={schedulingMode === 'workout-plan' ? 1 : 0}
                />
            )}

            {/* Exercise Configuration Modal */}
            {showConfigModal && selectedExercise && exerciseConfig && (
                <ExerciseConfigModal
                    isOpen={showConfigModal}
                    onClose={() => {
                        setShowConfigModal(false);
                        if (!isEditMode) {
                            resetSchedulingState();
                        } else {
                            resetEditingState();
                        }
                    }}
                    exercise={selectedExercise}
                    config={exerciseConfig}
                    onConfigChange={handleConfigChange}
                    onSave={handleSaveExercise}
                    selectedDate={viewingDate}
                    loading={loading}
                    mode={schedulingMode}
                    onModeChange={handleModeChange}
                    onWorkoutPlanSelect={setSelectedWorkoutPlan}
                    selectedWorkoutPlan={selectedWorkoutPlan}
                    isEditMode={isEditMode}
                    editingExercise={editingExercise}
                />
            )}

            {/* FIXED: Workout Plan Configuration Modal */}
            {showWorkoutPlanConfigModal && selectedWorkoutPlan && (
                <WorkoutPlanConfigModal
                    isOpen={showWorkoutPlanConfigModal}
                    onClose={() => {
                        setShowWorkoutPlanConfigModal(false);
                        resetWorkoutPlanState();
                    }}
                    workoutPlan={selectedWorkoutPlan}
                    selectedDate={viewingDate}
                    onSchedule={handleWorkoutPlanConfigSave}
                    loading={loading}
                />
            )}
        </div>
    );
};

export default CalendarPage;