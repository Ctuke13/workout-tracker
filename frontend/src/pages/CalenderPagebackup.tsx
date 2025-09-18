import React, {useState, useEffect, useMemo, useRef} from 'react';
import {
    Calendar,
    ChevronLeft,
    ChevronRight,
    Play,
    Clock,
    CheckCircle,
    Target,
    Plus,
    Weight,
    Settings, RefreshCw
} from 'lucide-react';
import {Button} from '../components/ui/button';
import {Card, CardContent, CardHeader, CardTitle} from '../components/ui/card';
import {Badge} from '../components/ui/badge';
import {toast} from 'react-hot-toast';
import {useWorkout} from '../contexts/WorkoutContext';
import {useNavigate, useLocation} from 'react-router-dom';

// Import your existing components
import FloatingActionButton from '../components/layout/FloatingActionButton';
import {ExerciseConfigModal} from '../components/CalendarPage/index';
import EnhancedExerciseSelector from '../components/CalendarPage/ExerciseSelector';
import WorkoutPlanConfigModal from '../components/CalendarPage/WorkoutPlanConfigModal';
import CompletedWorkoutDisplay from '../components/CalendarPage/CompletedWorkoutDisplay';
import WorkoutDetailsModal from '../components/CalendarPage/WorkoutDetailsModal';

// Import types and services
import {ScheduledWorkoutResponse, WorkoutPlanInfo, WorkoutPlanScheduleRequest} from '../types/api';
import {
    Exercise,
    ExerciseConfiguration,
    CalendarDay,
    ScheduledExercise,
    WorkoutStats,
    getDefaultConfigForExercise,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    WorkoutResults
} from '../types/exercise';
import {calendarApi} from '../services/calendarApi';
import {transformScheduledWorkoutsToCalendarData} from '../services/transformers';
import {StarIcon, StarIcon as StarIconSolid} from "@heroicons/react/24/outline";
import {exerciseApi} from "../services/exerciseApi";

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
    const navigate = useNavigate();
    const location = useLocation();
    const previousLocation = useRef(location.pathname);

    // Use correct method name from WorkoutContext
    const {startWorkout, isWorkoutActive} = useWorkout();

    // ==================== CORE STATE ====================

    const [viewingDate, setViewingDate] = useState(new Date());
    const [scheduledWorkouts, setScheduledWorkouts] = useState<ScheduledExercise[]>([]);
    const [stats, setStats] = useState<WorkoutStats | null>(null);
    const [loading, setLoading] = useState(false);

    // ==================== EXERCISE SCHEDULING STATE ====================

    const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
    const [exerciseConfig, setExerciseConfig] = useState<ExerciseConfiguration | null>(null);
    const [userFavoriteIds, setUserFavoriteIds] = useState<Set<number>>(new Set());

    // ==================== WORKOUT PLAN STATE ====================

    const [selectedWorkoutPlan, setSelectedWorkoutPlan] = useState<WorkoutPlanInfo | null>(null);
    const [showWorkoutPlanConfigModal, setShowWorkoutPlanConfigModal] = useState(false);

    // ==================== UI STATE ====================

    const [showExerciseSelector, setShowExerciseSelector] = useState(false);
    const [showConfigModal, setShowConfigModal] = useState(false);
    const [schedulingMode, setSchedulingMode] = useState<'exercise' | 'workout-plan'>('exercise');
    const [workoutResults, setWorkoutResults] = useState<Record<string, WorkoutResults>>({});

    const [showWorkoutDetailsModal, setShowWorkoutDetailsModal] = useState(false);
    const [selectedExerciseForDetails, setSelectedExerciseForDetails] = useState<ScheduledExercise | null>(null);
    const [selectedWorkoutResults, setSelectedWorkoutResults] = useState<WorkoutResults | null>(null);

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

    const loadDayData = async (forceCacheBust = false) => {
        if (loading && !forceCacheBust) {
            console.log('⏳ Already loading, skipping duplicate call');
            return;
        }

        try {
            setLoading(true);
            console.log('📅 Loading data for:', viewingDateString, forceCacheBust ? '(cache bust)' : '');

            // Load a week of data around the viewing date for context
            const startDate = new Date(viewingDate);
            startDate.setDate(viewingDate.getDate() - 3);
            const endDate = new Date(viewingDate);
            endDate.setDate(viewingDate.getDate() + 3);

            const startDateStr = startDate.toISOString().split('T')[0];
            const endDateStr = endDate.toISOString().split('T')[0];

            // Add cache busting parameter if needed
            const apiParams = forceCacheBust ? {
                startDate: startDateStr,
                endDate: endDateStr,
                _cacheBust: Date.now()
            } : {startDate: startDateStr, endDate: endDateStr};

            // 🔥 PARALLEL LOADING: Load both workouts and favorites
            const [apiResponse, favoriteIds] = await Promise.all([
                calendarApi.getScheduledExercises(startDateStr, endDateStr),
                exerciseApi.getFavoriteExerciseIds().catch(() => new Set<number>()) // Don't fail if this errors
            ]);

            console.log('📊 Raw API response (first 2 items):', apiResponse.slice(0, 2));

            const transformedWorkouts = transformScheduledWorkoutsToCalendarData(apiResponse);

            // 🌟 SYNC FAVORITES: Update exercises with favorite status
            const workoutsWithFavorites = transformedWorkouts.map(workout => ({
                ...workout,
                exercise: {
                    ...workout.exercise,
                    isFavorite: favoriteIds.has(workout.exercise.id)
                }
            }));

            // ✅ ENHANCED: Sort by completion status and priority
            const sortedWorkouts = workoutsWithFavorites.sort((a, b) => {
                // Completed exercises go to the end
                if (a.completed !== b.completed) {
                    return a.completed ? 1 : -1;
                }
                // Within same completion status, sort by creation time (newer first for pending)
                if (!a.completed && !b.completed) {
                    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
                }
                // For completed exercises, sort by completion time (most recent first)
                return 0;
            });

            console.log('🔄 Transformed workouts (first 2):', sortedWorkouts.slice(0, 2).map(ex => ({
                id: ex.id,
                name: ex.exercise.name || ex.exercise.exerciseName,
                completed: ex.completed,
                isFavorite: ex.exercise.isFavorite,
                status: (ex as any).status
            })));

            setScheduledWorkouts(sortedWorkouts);
            setUserFavoriteIds(favoriteIds);

            console.log(`✅ Loaded ${sortedWorkouts.length} scheduled workouts with favorite status`);
            console.log(`📈 Completion status: ${sortedWorkouts.filter(w => w.completed).length} completed, ${sortedWorkouts.filter(w => !w.completed).length} pending`);

            // If we have completed exercises, load their results
            const completedExercises = sortedWorkouts.filter(ex => ex.completed);
            if (completedExercises.length > 0) {
                console.log('🏋️‍♂️ Loading results for completed exercises...');
                try {
                    const exerciseIds = completedExercises.map(ex => ex.id);
                    const results = await calendarApi.getBatchWorkoutResults(exerciseIds);
                    setWorkoutResults(results);
                    console.log(`✅ Loaded results for ${Object.keys(results).length} exercises`);
                } catch (error) {
                    console.error('Failed to load workout results:', error);
                    // Don't fail the entire refresh for this
                    setWorkoutResults({});
                }
            }

        } catch (error) {
            console.error('Error loading day data:', error);
            toast.error('Failed to load scheduled workouts');
            setScheduledWorkouts([]);
        } finally {
            setLoading(false);
        }
    };

    const loadWorkoutStats = async (forceCacheBust = false) => {
        try {
            const cacheParam = forceCacheBust ? {_cacheBust: Date.now()} : {};
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

    useEffect(() => {
        const loadWorkoutResults = async () => {
            const completedExercises = viewingDateExercises.filter(ex => ex.completed);
            if (completedExercises.length > 0) {
                try {
                    const exerciseIds = completedExercises.map(ex => ex.id);
                    const results = await calendarApi.getBatchWorkoutResults(exerciseIds);
                    setWorkoutResults(results);
                } catch (error) {
                    console.error('Failed to load workout results:', error);
                }
            }
        };

        if (viewingDateExercises.length > 0) {
            loadWorkoutResults();
        }
    }, [viewingDateExercises]);

    // Real-time sync check (optional - for multi-device sync)
    useEffect(() => {
        // Set up periodic sync check every 30 seconds when page is visible
        let syncInterval: NodeJS.Timeout;

        const handleVisibilityChange = () => {
            if (document.visibilityState === 'visible') {
                // Page became visible - check for updates
                syncInterval = setInterval(async () => {
                    if (isToday()) {
                        // Only sync today's data to avoid unnecessary requests
                        await refreshCalendarData();
                    }
                }, 30000); // 30 seconds
            } else {
                // Page hidden - stop syncing
                if (syncInterval) {
                    clearInterval(syncInterval);
                }
            }
        };

        document.addEventListener('visibilitychange', handleVisibilityChange);
        handleVisibilityChange(); // Set up initial state

        return () => {
            document.removeEventListener('visibilitychange', handleVisibilityChange);
            if (syncInterval) {
                clearInterval(syncInterval);
            }
        };
    }, [viewingDateString]);

    useEffect(() => {
        // Check if we're returning from workout mode
        if (previousLocation.current === '/workout' && location.pathname === '/calendar') {
            console.log('🔄 Returning from workout mode - refreshing calendar data...');
            handleWorkoutReturn();
        }
        previousLocation.current = location.pathname;
    }, [location.pathname]);

    const handleWorkoutReturn = async () => {
        try {
            setLoading(true);

            console.log('Handling workout return with enhanced refresh...');

            // Add a small delay to ensure backend processing completes
            await new Promise(resolve => setTimeout(resolve, 500));

            // Check session storage flags
            const workoutCompleted = sessionStorage.getItem('workoutJustCompleted') === 'true';

            if (workoutCompleted) {
                console.log('Detected completed workout, forcing cache bust...');

                // Clear the flags
                sessionStorage.removeItem('workoutJustCompleted');
                sessionStorage.removeItem('completedWorkoutDate');

                // Force complete refresh with cache bust
                await refreshCalendarData(false, true);
            } else {
                // Regular refresh
                await refreshCalendarData(false, false);
            }

            console.log('Calendar refresh completed');

        } catch (error) {
            console.error('Failed to refresh calendar after workout return:', error);
            toast.error('Failed to refresh calendar data');
        } finally {
            setLoading(false);
        }
    };

    // ✅ NEW: Check for workout achievements
    const checkForWorkoutAchievements = () => {
        const completedToday = viewingDateExercises.filter(ex => ex.completed).length;
        const totalToday = viewingDateExercises.length;

        if (completedToday === totalToday && totalToday > 0) {
            // All exercises completed for today
            setTimeout(() => {
                toast.success('🎉 Outstanding! You completed all your exercises for today!', {
                    duration: 5000,
                    style: {
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        color: 'white',
                        fontSize: '16px',
                        padding: '16px',
                    },
                });
            }, 1500);
        } else if (completedToday > 0) {
            // Some exercises completed
            setTimeout(() => {
                toast.success(`Great progress! You completed ${completedToday} of ${totalToday} exercises.`, {
                    duration: 3000,
                });
            }, 800);
        }

        // Check for streaks and milestones
        if (stats) {
            if (stats.currentStreak >= 7) {
                setTimeout(() => {
                    toast.success(`🔥 Amazing! ${stats.currentStreak} day streak!`, {
                        duration: 4000,
                        style: {
                            background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
                            color: 'white',
                        },
                    });
                }, 2500);
            }

            if (stats.completedWorkouts > 0 && stats.completedWorkouts % 10 === 0) {
                setTimeout(() => {
                    toast.success(`🏆 Milestone reached: ${stats.completedWorkouts} completed workouts!`, {
                        duration: 4000,
                        style: {
                            background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
                            color: 'white',
                        },
                    });
                }, 3500);
            }
        }
    };


    const refreshCalendarData = async (showToast = false, forceCacheBust = false) => {
        try {
            console.log(`🔄 Refreshing calendar data... (cacheBust: ${forceCacheBust})`);

            // Add timestamp to force cache busting if needed
            const cacheParam = forceCacheBust ? `?_t=${Date.now()}` : '';

            // Use Promise.allSettled to handle partial failures gracefully
            const [workoutResult, statsResult] = await Promise.allSettled([
                loadDayData(forceCacheBust),
                loadWorkoutStats(forceCacheBust)
            ]);

            // Log results
            if (workoutResult.status === 'rejected') {
                console.error('Failed to load workout data:', workoutResult.reason);
            }
            if (statsResult.status === 'rejected') {
                console.error('Failed to load stats:', statsResult.reason);
            }

            // Show success message if requested and at least workouts succeeded
            if (showToast && workoutResult.status === 'fulfilled') {
                toast.success('Calendar updated successfully!');
            }

            console.log('✅ Calendar data refresh completed');

        } catch (error) {
            console.error('❌ Failed to refresh calendar data:', error);
            if (showToast) {
                toast.error('Failed to refresh calendar data');
            }
        }
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

    const handleFavoriteToggle = async (exercise: Exercise) => {
        try {
            console.log(`🌟 Toggling favorite for exercise: ${exercise.name}`);

            // 🚀 OPTIMISTIC UPDATE: Update UI immediately
            const wasFavorited = exercise.isFavorite;
            const newFavoriteStatus = !wasFavorited;

            // Update the exercise object immediately
            exercise.isFavorite = newFavoriteStatus;

            // Update scheduled workouts state
            setScheduledWorkouts(prev => prev.map(workout =>
                workout.exercise.id === exercise.id
                    ? {...workout, exercise: {...workout.exercise, isFavorite: newFavoriteStatus}}
                    : workout
            ));

            // Update favorite IDs set
            const newFavoriteIds = new Set(userFavoriteIds);
            if (newFavoriteStatus) {
                newFavoriteIds.add(exercise.id);
            } else {
                newFavoriteIds.delete(exercise.id);
            }
            setUserFavoriteIds(newFavoriteIds);

            // 🌐 API CALL: Sync with backend
            const result = await exerciseApi.toggleFavorite(exercise.id);

            // ✅ VERIFY: Ensure optimistic update was correct
            if (result.isFavorite !== newFavoriteStatus) {
                console.warn('⚠️ Optimistic update mismatch, correcting...');
                exercise.isFavorite = result.isFavorite;

                setScheduledWorkouts(prev => prev.map(workout =>
                    workout.exercise.id === exercise.id
                        ? {...workout, exercise: {...workout.exercise, isFavorite: result.isFavorite}}
                        : workout
                ));

                const correctedFavoriteIds = new Set(userFavoriteIds);
                if (result.isFavorite) {
                    correctedFavoriteIds.add(exercise.id);
                } else {
                    correctedFavoriteIds.delete(exercise.id);
                }
                setUserFavoriteIds(correctedFavoriteIds);
            }

            toast.success(result.isFavorite ? 'Added to favorites' : 'Removed from favorites');

        } catch (error) {
            console.error('❌ Failed to toggle favorite:', error);

            // 🔄 REVERT: Undo optimistic update on error
            exercise.isFavorite = !exercise.isFavorite;
            setScheduledWorkouts(prev => prev.map(workout =>
                workout.exercise.id === exercise.id
                    ? {...workout, exercise: {...workout.exercise, isFavorite: !exercise.isFavorite}}
                    : workout
            ));

            toast.error('Failed to update favorites');
        }
    };

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

    const handleManualRefresh = () => {
        refreshCalendarData(true);
    };

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

    const handleStartWorkout = async (exerciseId: string) => {
        try {
            console.log('🎯 Starting workout with exercise:', exerciseId);

            // Find the exercise in current scheduled workouts
            const targetExercise = viewingDateExercises.find(ex => ex.id === exerciseId);

            if (!targetExercise) {
                toast.error('Exercise not found');
                return;
            }

            // ✅ FIXED: Transform single exercise to match WorkoutContext expectations
            const compatibleExercise = {
                ...targetExercise,
                exercise: {
                    ...targetExercise.exercise,
                    name: targetExercise.exercise.name || targetExercise.exercise.exerciseName || 'Unknown Exercise',
                    exerciseName: targetExercise.exercise.exerciseName || targetExercise.exercise.name || 'Unknown Exercise'
                },
                // Ensure all required fields are present
                targetSets: targetExercise.targetSets || (targetExercise.exercise.isCardio ? 1 : targetExercise.exercise.isIsometric ? 3 : 3),
                targetReps: targetExercise.targetReps || (targetExercise.exercise.isCardio ? targetExercise.exercise.estimatedDurationMinutes || 20 : targetExercise.exercise.isIsometric ? targetExercise.holdDurationSeconds || 30 : 10),
                targetWeight: targetExercise.targetWeight || undefined,
                targetWeightUnit: targetExercise.targetWeightUnit || 'lbs',
                restSeconds: targetExercise.restSeconds || (targetExercise.exercise.isCardio ? 0 : targetExercise.exercise.isIsometric ? 60 : 90),
                targetRpe: targetExercise.targetRpe || 7,
                completed: false,
                createdAt: targetExercise.createdAt || new Date().toISOString(),
                userId: targetExercise.userId || 'current_user'
            };

            // Start workout with single exercise
            startWorkout([compatibleExercise], viewingDateString);

            // Navigate to workout mode
            navigate('/workout');

            toast.success(`Started workout with ${compatibleExercise.exercise.name}!`);

        } catch (error) {
            console.error('❌ Failed to start workout:', error);
            toast.error('Failed to start workout');
        }
    };

    const handleStartFullWorkout = async () => {
        try {
            console.log('🎯 Starting workout with all today\'s exercises');

            if (viewingDateExercises.length === 0) {
                toast.error('No exercises scheduled for today');
                return;
            }

            // ✅ FIXED: Transform exercises to match WorkoutContext expectations
            const compatibleExercises = viewingDateExercises.map(scheduledExercise => {
                // Ensure the exercise has the required properties
                const exercise = {
                    ...scheduledExercise.exercise,
                    // Add any missing properties that WorkoutContext expects
                    name: scheduledExercise.exercise.name || scheduledExercise.exercise.exerciseName || 'Unknown Exercise',
                    exerciseName: scheduledExercise.exercise.exerciseName || scheduledExercise.exercise.name || 'Unknown Exercise'
                };

                // Return ScheduledExercise with proper structure
                return {
                    ...scheduledExercise,
                    exercise: exercise,
                    // Ensure all required fields are present with proper types
                    targetSets: scheduledExercise.targetSets || (exercise.isCardio ? 1 : exercise.isIsometric ? 3 : 3),
                    targetReps: scheduledExercise.targetReps || (exercise.isCardio ? exercise.estimatedDurationMinutes || 20 : exercise.isIsometric ? scheduledExercise.holdDurationSeconds || 30 : 10),
                    targetWeight: scheduledExercise.targetWeight || undefined,
                    targetWeightUnit: scheduledExercise.targetWeightUnit || 'lbs',
                    restSeconds: scheduledExercise.restSeconds || (exercise.isCardio ? 0 : exercise.isIsometric ? 60 : 90),
                    targetRpe: scheduledExercise.targetRpe || 7,
                    holdDurationSeconds: scheduledExercise.holdDurationSeconds || (exercise.isIsometric ? 30 : undefined),
                    notes: scheduledExercise.notes || '',
                    completed: scheduledExercise.completed || false,
                    createdAt: scheduledExercise.createdAt || new Date().toISOString(),
                    userId: scheduledExercise.userId || 'current_user'
                };
            });

            console.log('✅ Transformed exercises for workout:', compatibleExercises.map(ex => ({
                id: ex.id,
                name: ex.exercise.name,
                targetSets: ex.targetSets,
                targetReps: ex.targetReps,
                isCardio: ex.exercise.isCardio,
                isIsometric: ex.exercise.isIsometric
            })));

            // Start the workout with compatible data
            startWorkout(compatibleExercises, viewingDateString);

            // Navigate to workout mode
            navigate('/workout');

            toast.success(`Started workout with ${compatibleExercises.length} exercises!`);

        } catch (error) {
            console.error('❌ Failed to start full workout:', error);
            toast.error('Failed to start workout');
        }
    };

    const debugWorkoutData = () => {
        console.log('🔍 DEBUGGING CALENDAR WORKOUT DATA');
        console.log('===================================');

        console.log('Current viewing date:', viewingDateString);
        console.log('Total scheduled workouts:', scheduledWorkouts.length);
        console.log('Exercises for viewing date:', viewingDateExercises.length);

        viewingDateExercises.forEach((exercise, index) => {
            console.log(`Exercise ${index + 1}:`, {
                id: exercise.id,
                exerciseId: exercise.exerciseId,
                exerciseName: exercise.exercise?.name || exercise.exercise?.exerciseName || 'MISSING NAME',
                hasExerciseObject: !!exercise.exercise,
                exerciseType: exercise.exercise?.exerciseType || 'UNKNOWN',
                isCardio: exercise.exercise?.isCardio,
                isIsometric: exercise.exercise?.isIsometric,
                targetSets: exercise.targetSets,
                targetReps: exercise.targetReps,
                targetWeight: exercise.targetWeight,
                targetWeightUnit: exercise.targetWeightUnit,
                restSeconds: exercise.restSeconds,
                holdDurationSeconds: exercise.holdDurationSeconds,
                scheduledDate: exercise.scheduledDate,
                completed: exercise.completed
            });
        });

        console.log('===================================');
    };

    if (typeof window !== 'undefined') {
        (window as any).debugWorkoutData = debugWorkoutData;
    }

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
        if (viewingDateExercises.length === 0) {
            toast.error('No exercises scheduled for today');
            return;
        }

        handleStartFullWorkout();
    };

    const getWorkoutResultsForExercise = (exerciseId: string): WorkoutResults | undefined => {
        return workoutResults[exerciseId];
    };

    const handleViewWorkoutDetails = (exerciseId: string) => {
        // Find the exercise and its results
        const exercise = viewingDateExercises.find(ex => ex.id === exerciseId);
        const results = workoutResults[exerciseId];

        if (exercise && results) {
            setSelectedExerciseForDetails(exercise);
            setSelectedWorkoutResults(results);
            setShowWorkoutDetailsModal(true);
        } else {
            // Fallback: try to fetch results if not already loaded
            const fetchResults = async () => {
                try {
                    const fetchedResults = await calendarApi.getWorkoutResults(exerciseId);
                    if (fetchedResults && exercise) {
                        setSelectedExerciseForDetails(exercise);
                        setSelectedWorkoutResults(fetchedResults);
                        setShowWorkoutDetailsModal(true);
                    } else {
                        toast.error('Workout details not available yet. Results may still be processing.');
                    }
                } catch (error) {
                    console.error('Failed to fetch workout details:', error);
                    toast.error('Unable to load workout details');
                }
            };

            if (exercise) {
                fetchResults();
            } else {
                toast.error('Exercise not found');
            }
        }
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
                subtitle: viewingDate.toLocaleDateString('en-US', {weekday: 'long', month: 'long', day: 'numeric'}),
                emoji: '🎯',
                bgColor: 'from-blue-500 to-green-500',
                textColor: 'text-white'
            };
        } else if (viewingDate.toDateString() === yesterday.toDateString()) {
            return {
                title: 'Yesterday',
                subtitle: viewingDate.toLocaleDateString('en-US', {weekday: 'long', month: 'long', day: 'numeric'}),
                emoji: '📅',
                bgColor: 'from-gray-400 to-gray-500',
                textColor: 'text-white'
            };
        } else if (viewingDate.toDateString() === tomorrow.toDateString()) {
            return {
                title: 'Tomorrow',
                subtitle: viewingDate.toLocaleDateString('en-US', {weekday: 'long', month: 'long', day: 'numeric'}),
                emoji: '✨',
                bgColor: 'from-purple-500 to-pink-500',
                textColor: 'text-white'
            };
        } else {
            return {
                title: viewingDate.toLocaleDateString('en-US', {weekday: 'long'}),
                subtitle: viewingDate.toLocaleDateString('en-US', {month: 'long', day: 'numeric', year: 'numeric'}),
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
        console.log('🔍 RAW EXERCISE CONFIG DEBUG:', {
            exerciseId: exercise.id,
            exerciseName: exercise.exercise.name,
            exerciseType: exercise.exercise.exerciseType,
            isCardio: exercise.exercise.isCardio,
            isIsometric: exercise.exercise.isIsometric,

            // Strength fields
            targetSets: exercise.targetSets,
            targetReps: exercise.targetReps,
            targetWeight: exercise.targetWeight,
            targetWeightUnit: exercise.targetWeightUnit,
            restSeconds: exercise.restSeconds,
            targetRpe: exercise.targetRpe,
            tempo: exercise.tempo,

            // Cardio fields
            targetDurationMinutes: exercise.targetDurationMinutes,
            targetDistance: exercise.targetDistance,
            targetDistanceKm: exercise.targetDistanceKm,
            targetDistanceUnit: exercise.targetDistanceUnit,
            targetPace: exercise.targetPace,

            // Isometric fields
            holdDurationSeconds: exercise.holdDurationSeconds,

            // Notes
            notes: exercise.notes
        });
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
            return <CheckCircle className="w-4 h-4 sm:w-5 sm:h-5 text-green-500"/>;
        }
        return <Clock className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500"/>;
    };

    // ==================== RENDER ====================

    const renderDayCompletionSummary = () => {
        const completedExercises = viewingDateExercises.filter(ex => ex.completed);
        if (completedExercises.length === 0 || completedExercises.length !== viewingDateExercises.length) {
            return null;
        }

        const totalDuration = Object.values(workoutResults).reduce(
            (sum, result) => sum + result.totalDurationMinutes, 0
        );

        const totalCalories = Object.values(workoutResults).reduce(
            (sum, result) => sum + (result.caloriesBurned || 0), 0
        );

        return (
            <div className="bg-gradient-to-r from-green-500 to-emerald-600 rounded-xl p-6 text-white mb-6">
                <div className="flex items-center gap-3 mb-4">
                    <div className="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center text-2xl">
                        🏆
                    </div>
                    <div>
                        <h3 className="text-xl font-bold">Workout Complete!</h3>
                        <p className="text-green-100">Great job finishing today's exercises</p>
                    </div>
                </div>

                <div className="grid grid-cols-3 gap-4">
                    <div className="text-center">
                        <div className="text-2xl font-bold">{completedExercises.length}</div>
                        <div className="text-sm text-green-100">Exercises</div>
                    </div>
                    <div className="text-center">
                        <div className="text-2xl font-bold">{totalDuration}m</div>
                        <div className="text-sm text-green-100">Total Time</div>
                    </div>
                    {totalCalories > 0 && (
                        <div className="text-center">
                            <div className="text-2xl font-bold">{totalCalories}</div>
                            <div className="text-sm text-green-100">Calories</div>
                        </div>
                    )}
                </div>
            </div>
        );
    };

    return (
        <div className="w-full min-h-screen bg-gray-50 pb-20">
            <div className="px-3 sm:px-4 lg:px-6 py-3 sm:py-4 lg:py-6 space-y-4 sm:space-y-6 max-w-4xl mx-auto">

                {/* Today-Focused Hero Header - Mobile Optimized */}
                <div
                    className={`bg-gradient-to-r ${dateInfo.bgColor} rounded-xl sm:rounded-2xl lg:rounded-3xl p-4 sm:p-6 lg:p-8 ${dateInfo.textColor} shadow-lg`}>
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
                                <ChevronLeft className="w-4 h-4"/>
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

                            {/* Manual Refresh Button */}
                            <Button
                                variant="secondary"
                                size="sm"
                                onClick={handleManualRefresh}
                                disabled={loading}
                                className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-2 sm:px-3 lg:px-4"
                                title="Refresh calendar data"
                            >
                                {loading ? (
                                    <div
                                        className="w-4 h-4 animate-spin rounded-full border-2 border-white border-t-transparent"/>
                                ) : (
                                    <>
                                        <RefreshCw className="w-4 h-4"/>
                                        <span className="hidden sm:inline ml-1">Refresh</span>
                                    </>
                                )}
                            </Button>

                            <Button
                                variant="secondary"
                                size="sm"
                                onClick={() => navigateDay('next')}
                                className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-2 sm:px-3 lg:px-4"
                            >
                                <span className="hidden sm:inline mr-1">Tomorrow</span>
                                <ChevronRight className="w-4 h-4"/>
                            </Button>
                        </div>

                        {/* Exercise Count Summary - Mobile Grid */}
                        <div className="grid grid-cols-3 gap-3 sm:gap-4 lg:gap-6 mt-4 sm:mt-6 max-w-sm mx-auto">
                            <div className="text-center">
                                <div
                                    className="text-lg sm:text-xl lg:text-2xl font-bold">{viewingDateExercises.length}</div>
                                <div className="text-xs sm:text-sm opacity-80">Planned</div>
                            </div>
                            <div className="text-center">
                                <div
                                    className="text-lg sm:text-xl lg:text-2xl font-bold">{viewingDateExercises.filter(ex => ex.completed).length}</div>
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
                        onClick={() => {
                            console.log('🎯 Start Today\'s Workout button clicked');
                            debugWorkoutData(); // Debug the data first
                            startWorkoutMode(); // Then start the workout
                        }}
                    >
                        <Play className="w-5 h-5 sm:w-6 sm:h-6 lg:w-8 lg:h-8"/>
                        Start Today's Workout ({viewingDateExercises.length} exercises)
                    </Button>
                )}

                {/* Week Context Mini Calendar - Mobile Optimized */}
                <Card className="shadow-sm">
                    <CardHeader className="pb-2 sm:pb-3">
                        <CardTitle className="text-sm sm:text-base text-gray-600 flex items-center gap-2">
                            <Calendar className="w-4 h-4"/>
                            Week Overview
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-0">
                        <div className="grid grid-cols-7 gap-1 sm:gap-2">
                            {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((day, index) => (
                                <div key={day + index}
                                     className="text-center text-xs sm:text-sm font-medium text-gray-500 pb-2">
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
                                    <div
                                        className={`text-sm sm:text-base font-bold ${day.isViewing ? 'text-white' : day.isToday ? 'text-blue-600' : 'text-gray-900'}`}>
                                        {day.date.getDate()}
                                    </div>
                                    {day.exerciseCount > 0 && (
                                        <div className="flex justify-center gap-0.5 mt-1">
                                            {Array.from({length: Math.min(day.exerciseCount, 4)}).map((_, idx) => (
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
                                <Badge
                                    className={`text-xs sm:text-sm ${isToday() ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>
                                    {viewingDateExercises.filter(ex => ex.completed).length} / {viewingDateExercises.length} Complete
                                </Badge>
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="pt-0">
                            <div className="space-y-3 sm:space-y-4">
                                {viewingDateExercises.map((exercise, index) => {
                                    // Check if this exercise has been completed and has workout results
                                    const exerciseWorkoutResults = getWorkoutResultsForExercise(exercise.id);

                                    if (exercise.completed && exerciseWorkoutResults) {
                                        // Show completed workout display with beautiful results
                                        return (
                                            <CompletedWorkoutDisplay
                                                key={exercise.id}
                                                exercise={exercise}
                                                workoutResults={exerciseWorkoutResults}
                                                onViewDetails={() => handleViewWorkoutDetails(exercise.id)}
                                            />
                                        );
                                    } else if (exercise.completed && !exerciseWorkoutResults) {
                                        // Show completed but no results available
                                        return (
                                            <div
                                                key={exercise.id}
                                                className="border-2 border-yellow-200 bg-yellow-50 rounded-lg sm:rounded-xl p-3 sm:p-4 lg:p-6 transition-all duration-200"
                                            >
                                                <div className="flex items-start gap-3">
                                                    <div className="flex-shrink-0 mt-0.5">
                                                        <div
                                                            className="w-6 h-6 sm:w-8 sm:h-8 lg:w-10 lg:h-10 rounded-full flex items-center justify-center font-bold text-xs sm:text-sm lg:text-base bg-yellow-500 text-white">
                                                            ⚠️
                                                        </div>
                                                    </div>
                                                    <div className="flex-1 min-w-0">
                                                        <div className="flex items-center gap-2 mb-2">
                                                            <CheckCircle
                                                                className="w-4 h-4 sm:w-5 sm:h-5 text-yellow-500"/>
                                                            <h3 className="font-bold text-sm sm:text-base lg:text-lg text-gray-900 truncate">
                                                                {exercise.exercise.name || exercise.exercise.exerciseName}
                                                            </h3>
                                                        </div>
                                                        <p className="text-sm text-yellow-700 mb-3">
                                                            Workout completed but detailed results are not available
                                                            yet.
                                                        </p>
                                                        <div
                                                            className="flex items-center text-yellow-600 text-sm font-medium">
                                                            <CheckCircle className="w-4 h-4 mr-1"/>
                                                            <span>Completed</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    } else {
                                        // Show regular scheduled exercise card (your existing code)
                                        return (
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
                                                {/* ✅ NEW: Mobile-First Layout */}
                                                <div className="flex items-start gap-3">
                                                    <div className="flex-shrink-0 mt-0.5">
                                                        <div className={`
                                    w-6 h-6 sm:w-8 sm:h-8 lg:w-10 lg:h-10 rounded-full flex items-center justify-center font-bold text-xs sm:text-sm lg:text-base
                                    ${exercise.completed ? 'bg-green-500 text-white' : 'bg-blue-500 text-white'}
                                `}>
                                                            {index + 1}
                                                        </div>
                                                    </div>

                                                    <div className="flex-1 min-w-0">
                                                        {/* ✅ NEW: Header with Exercise Title and Favorite Star */}
                                                        <div className="flex items-center justify-between mb-2">
                                                            <div className="flex items-center gap-2 flex-1 min-w-0">
                                                                {getStatusIcon(exercise)}
                                                                <h3 className="font-bold text-sm sm:text-base lg:text-lg text-gray-900 truncate">
                                                                    {exercise.exercise.name || exercise.exercise.exerciseName}
                                                                </h3>
                                                            </div>

                                                            {/* ✅ MOVED: Favorite star to header - clean and accessible */}
                                                            <button
                                                                onClick={async (e) => {
                                                                    e.stopPropagation();
                                                                    try {
                                                                        const result = await exerciseApi.toggleFavorite(exercise.exercise.id);
                                                                        exercise.exercise.isFavorite = result.isFavorite;
                                                                        setScheduledWorkouts(prev => prev.map(w =>
                                                                            w.id === exercise.id
                                                                                ? {
                                                                                    ...w,
                                                                                    exercise: {
                                                                                        ...w.exercise,
                                                                                        isFavorite: result.isFavorite
                                                                                    }
                                                                                }
                                                                                : w
                                                                        ));
                                                                        toast.success(result.isFavorite ? 'Added to favorites' : 'Removed from favorites');
                                                                    } catch (error) {
                                                                        toast.error('Failed to update favorites');
                                                                    }
                                                                }}
                                                                className={`
                                            ml-2 p-1.5 rounded-full transition-all duration-200 flex-shrink-0
                                            active:scale-95 shadow-sm hover:shadow-md border
                                            ${exercise.exercise.isFavorite
                                                                    ? 'text-yellow-500 bg-yellow-100 hover:bg-yellow-200 border-yellow-300'
                                                                    : 'text-gray-400 bg-gray-50 hover:bg-yellow-100 border-gray-200'
                                                                }
                                        `}
                                                                title={exercise.exercise.isFavorite ? 'Remove from favorites' : 'Add to favorites'}
                                                            >
                                                                {/* ✅ FIXED: Use solid star when favorited, outline when not */}
                                                                {exercise.exercise.isFavorite ? (
                                                                    <StarIconSolid className="w-4 h-4 text-yellow-500"/>
                                                                ) : (
                                                                    <StarIcon className="w-4 h-4"/>
                                                                )}
                                                            </button>
                                                        </div>

                                                        {/* Enhanced Configuration Details */}
                                                        <div
                                                            className={`${getConfigurationDisplay(exercise).bgColor} rounded-lg p-3 mb-3 border ${getConfigurationDisplay(exercise).borderColor}`}>
                                                            <div className="flex items-center gap-2 mb-2">
                                                                <Weight
                                                                    className={`w-4 h-4 ${getConfigurationDisplay(exercise).iconColor}`}/>
                                                                <span
                                                                    className={`text-sm font-medium ${getConfigurationDisplay(exercise).textColor}`}>
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
                                                                <Badge variant="outline"
                                                                       className="text-xs text-red-700 bg-red-50">
                                                                    ❤️ Cardio
                                                                </Badge>
                                                            )}
                                                            {exercise.exercise.isIsometric && (
                                                                <Badge variant="outline"
                                                                       className="text-xs text-purple-700 bg-purple-50">
                                                                    🛡️ Hold
                                                                </Badge>
                                                            )}
                                                        </div>

                                                        <div
                                                            className="flex items-center gap-3 sm:gap-4 text-xs sm:text-sm text-gray-600 mb-4">
                                                            <div className="flex items-center gap-1">
                                                                <Clock className="w-3 h-3 sm:w-4 sm:h-4"/>
                                                                <span>{exercise.exercise.estimatedDurationMinutes} min</span>
                                                            </div>
                                                            <div className="flex items-center gap-1">
                                                                <Target className="w-3 h-3 sm:w-4 sm:h-4"/>
                                                                <span>{exercise.exercise.estimatedCalories} cal</span>
                                                            </div>
                                                            {exercise.exercise.averageRating > 0 && (
                                                                <div className="flex items-center gap-1">
                                                                    <span>⭐</span>
                                                                    <span>{exercise.exercise.averageRating.toFixed(1)}</span>
                                                                </div>
                                                            )}
                                                        </div>

                                                        {/* ✅ IMPROVED: Action Buttons with Natural Spacing */}
                                                        <div
                                                            className="flex flex-wrap gap-2 pt-2 border-t border-gray-100">
                                                            {exercise.completed ? (
                                                                <div
                                                                    className="flex items-center text-green-600 text-sm font-medium">
                                                                    <CheckCircle className="w-4 h-4 mr-1"/>
                                                                    <span className="hidden sm:inline">Completed</span>
                                                                    <span className="sm:hidden">✓ Done</span>
                                                                </div>
                                                            ) : (
                                                                <>
                                                                    {/* 🎯 PRIMARY ACTION: Start Workout */}
                                                                    <Button
                                                                        size="sm"
                                                                        onClick={() => handleStartWorkout(exercise.id)}
                                                                        className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 text-xs sm:text-sm font-medium"
                                                                    >
                                                                        <Play className="w-4 h-4 mr-1"/>
                                                                        <span
                                                                            className="hidden sm:inline">Start Workout</span>
                                                                        <span className="sm:hidden">▶️</span>
                                                                    </Button>

                                                                    {/* 🛠️ SECONDARY: Edit Configuration */}
                                                                    <Button
                                                                        size="sm"
                                                                        variant="outline"
                                                                        onClick={() => handleEditExercise(exercise)}
                                                                        className="text-blue-600 hover:bg-blue-50 border-blue-200 px-3 py-2 text-xs sm:text-sm"
                                                                    >
                                                                        <Settings className="w-4 h-4 mr-1"/>
                                                                        <span className="hidden sm:inline">Edit</span>
                                                                        <span className="sm:hidden">⚙️</span>
                                                                    </Button>
                                                                </>
                                                            )}

                                                            <Button
                                                                size="sm"
                                                                variant="outline"
                                                                onClick={() => handleDeleteWorkout(exercise.id)}
                                                                className="text-red-600 hover:bg-red-50 border-red-200 px-3 py-2 text-xs sm:text-sm"
                                                            >
                                                                <span className="hidden sm:inline mr-1">🗑️</span>
                                                                <span className="hidden sm:inline">Delete</span>
                                                                <span className="sm:hidden">✗</span>
                                                            </Button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        );
                                    }
                                })}
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
                            <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center">
                                <Button
                                    onClick={() => {
                                        setSchedulingMode('exercise');
                                        setShowExerciseSelector(true);
                                    }}
                                    className="bg-blue-600 hover:bg-blue-700 text-white px-4 sm:px-6 py-2 sm:py-3 text-sm sm:text-base font-semibold rounded-lg sm:rounded-xl flex items-center justify-center"
                                >
                                    <Plus className="w-4 h-4 sm:w-5 sm:h-5 mr-2"/>
                                    💪 Add Exercise
                                </Button>
                                <Button
                                    onClick={() => {
                                        setSchedulingMode('workout-plan');
                                        setShowExerciseSelector(true);
                                    }}
                                    className="bg-purple-600 hover:bg-purple-700 text-white px-4 sm:px-6 py-2 sm:py-3 text-sm sm:text-base font-semibold rounded-lg sm:rounded-xl flex items-center justify-center"
                                >
                                    <Settings className="w-4 h-4 sm:w-5 sm:h-5 mr-2"/>
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
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-blue-600">{stats.currentStreak}</div>
                                <div className="text-xs sm:text-sm text-gray-500">Day Streak</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-green-600">{stats.completedWorkouts}</div>
                                <div className="text-xs sm:text-sm text-gray-500">This Month</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-purple-600">{Math.round(stats.completionRate || 0)}%
                                </div>
                                <div className="text-xs sm:text-sm text-gray-500">Success</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-orange-600">{stats.averageWorkoutDuration}</div>
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
                    onFavoriteToggle={handleFavoriteToggle}
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

            {/* Workout Details Modal */}
            {showWorkoutDetailsModal && selectedExerciseForDetails && selectedWorkoutResults && (
                <WorkoutDetailsModal
                    isOpen={showWorkoutDetailsModal}
                    onClose={() => {
                        setShowWorkoutDetailsModal(false);
                        setSelectedExerciseForDetails(null);
                        setSelectedWorkoutResults(null);
                    }}
                    exercise={selectedExerciseForDetails}
                    workoutResults={selectedWorkoutResults}
                />
            )}
        </div>
    );
};

export default CalendarPage;