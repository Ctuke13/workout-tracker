import React, {useState, useRef} from 'react';
import {Play, Plus, Settings} from 'lucide-react';
import {Button} from '../components/ui/button';
import {Card, CardContent, CardHeader, CardTitle} from '../components/ui/card';
import {Badge} from '../components/ui/badge';
import {toast} from 'react-hot-toast';
import {useWorkout} from '../contexts/WorkoutContext';
import {useNavigate, useLocation} from 'react-router-dom';

// Import extracted components
import {useCalendarData} from '../hooks/useCalendarData';
import {DateHeader} from '../components/CalendarPage/DateHeader';
import {WeekCalendar} from '../components/CalendarPage/WeekCalendar';
import {ExerciseCard} from '../components/CalendarPage/ExerciseCard';

// Import existing components
import FloatingActionButton from '../components/layout/FloatingActionButton';
import {ExerciseConfigModal} from '../components/CalendarPage/index';
import EnhancedExerciseSelector from '../components/CalendarPage/ExerciseSelector';
import WorkoutPlanConfigModal from '../components/CalendarPage/WorkoutPlanConfigModal';
import WorkoutDetailsModal from '../components/CalendarPage/WorkoutDetailsModal';

// Import types and services
import {WorkoutPlanInfo, WorkoutPlanScheduleRequest} from '../types/api';
import {
    Exercise,
    ExerciseConfiguration,
    ScheduledExercise,
    getDefaultConfigForExercise,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    WorkoutResults
} from '../types/exercise';
import {calendarApi} from '../services/calendarApi';
import {exerciseApi} from '../services/exerciseApi';

// Interfaces for workout plan configuration
interface WorkoutPlanConfiguration {
    workoutPlanId: number;
    scheduledDate: string;
    exerciseConfigs: WorkoutPlanExerciseConfig[];
    planNotes: string;
    estimatedDuration: number;
    reminderEnabled: boolean;
    reminderTime: string;
}

interface WorkoutPlanExerciseConfig {
    exerciseId: number;
    configuration: ExerciseConfiguration;
    skip: boolean;
    substitute?: boolean;
    notes?: string;
}

const CalendarPage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const previousLocation = useRef(location.pathname);
    const {startWorkout} = useWorkout();

    // Core state
    const [viewingDate, setViewingDate] = useState(new Date());

    // Use the extracted data management hook
    const {
        scheduledWorkouts,
        stats,
        userFavoriteIds,
        workoutResults,
        loading,
        viewingDateString,
        viewingDateExercises,
        loadDayData,
        refreshCalendarData,
        setScheduledWorkouts,
        setUserFavoriteIds
    } = useCalendarData(viewingDate);

    // Exercise scheduling state
    const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
    const [exerciseConfig, setExerciseConfig] = useState<ExerciseConfiguration | null>(null);

    // Workout plan state
    const [selectedWorkoutPlan, setSelectedWorkoutPlan] = useState<WorkoutPlanInfo | null>(null);
    const [showWorkoutPlanConfigModal, setShowWorkoutPlanConfigModal] = useState(false);

    // UI state
    const [showExerciseSelector, setShowExerciseSelector] = useState(false);
    const [showConfigModal, setShowConfigModal] = useState(false);
    const [schedulingMode, setSchedulingMode] = useState<'exercise' | 'workout-plan'>('exercise');

    // Editing state
    const [editingExercise, setEditingExercise] = useState<ScheduledExercise | null>(null);
    const [isEditMode, setIsEditMode] = useState(false);

    // Workout details modal
    const [showWorkoutDetailsModal, setShowWorkoutDetailsModal] = useState(false);
    const [selectedExerciseForDetails, setSelectedExerciseForDetails] = useState<ScheduledExercise | null>(null);
    const [selectedWorkoutResults, setSelectedWorkoutResults] = useState<WorkoutResults | null>(null);

    // Navigation handlers
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

    // Exercise scheduling handlers
    const handleExerciseSelect = (exercise: Exercise) => {
        setSelectedExercise(exercise);
        const defaultConfig = getDefaultConfigForExercise(exercise);
        setExerciseConfig(defaultConfig);
        setSchedulingMode('exercise');
        setShowExerciseSelector(false);
        setShowConfigModal(true);
    };

    const handleConfigChange = (config: ExerciseConfiguration) => {
        setExerciseConfig(config);
    };

    const handleSaveExercise = async () => {
        if (isEditMode) {
            return handleSaveEditedExercise();
        }

        if (!selectedExercise || !exerciseConfig) {
            toast.error('Please select an exercise and configure it properly');
            return;
        }

        try {
            const scheduleData: any = {
                exerciseId: selectedExercise.id,
                scheduledDate: viewingDate.toISOString().split('T')[0],
                notes: exerciseConfig.notes || `${selectedExercise.name || selectedExercise.exerciseName}`
            };

            // Add configuration based on tracking mode
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

            await calendarApi.scheduleIndividualExercise(scheduleData);
            toast.success('Exercise scheduled successfully!');

            resetSchedulingState();
            await loadDayData(true);
        } catch (error) {
            console.error('Error scheduling exercise:', error);
            toast.error('Failed to schedule exercise. Please check your configuration.');
        }
    };

    // Workout plan handlers
    const handleWorkoutPlanSelect = async (workoutPlan: WorkoutPlanInfo) => {
        try {
            const scheduleRequest: WorkoutPlanScheduleRequest = {
                workoutPlanId: workoutPlan.id,
                scheduledDate: viewingDateString,
                customNotes: `${workoutPlan.name || 'Workout Plan'} - ${workoutPlan.description || ''}`
            };

            await calendarApi.scheduleWorkout(scheduleRequest);
            setShowExerciseSelector(false);
            await loadDayData();
            toast.success('Workout plan scheduled successfully!');
        } catch (error) {
            console.error('Error scheduling workout plan:', error);
            toast.error('Failed to schedule workout plan');
        }
    };

    const handleWorkoutPlanConfigure = (workoutPlan: WorkoutPlanInfo) => {
        setSelectedWorkoutPlan(workoutPlan);
        setShowExerciseSelector(false);
        setShowWorkoutPlanConfigModal(true);
    };

    const handleWorkoutPlanConfigSave = (config: WorkoutPlanConfiguration): void => {
        const saveAsync = async () => {
            try {
                const exerciseSchedulePromises = config.exerciseConfigs
                    .filter(exerciseConfig => !exerciseConfig.skip)
                    .map(async (exerciseConfig) => {
                        const scheduleData: any = {
                            exerciseId: exerciseConfig.exerciseId,
                            scheduledDate: config.scheduledDate,
                            notes: exerciseConfig.notes || config.planNotes
                        };

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

                        return calendarApi.scheduleIndividualExercise(scheduleData);
                    });

                await Promise.all(exerciseSchedulePromises);

                setSelectedWorkoutPlan(null);
                setShowWorkoutPlanConfigModal(false);
                await refreshCalendarData();
                toast.success(`Workout "${selectedWorkoutPlan?.name}" added to ${viewingDate.toDateString()}!`);
            } catch (error) {
                console.error('Error scheduling workout session:', error);
                toast.error('Failed to schedule workout session');
            }
        };

        saveAsync();
    };

    // Edit handlers
    const handleEditExercise = (scheduledExercise: ScheduledExercise) => {
        setEditingExercise(scheduledExercise);
        setSelectedExercise(scheduledExercise.exercise);

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

        try {
            const updateData: any = {
                exerciseId: selectedExercise.id,
                scheduledDate: viewingDate.toISOString().split('T')[0],
                notes: exerciseConfig.notes || `${selectedExercise.name || selectedExercise.exerciseName}`
            };

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

            await calendarApi.updateScheduledExercise(editingExercise.id, updateData);
            toast.success('Exercise updated successfully!');

            resetEditingState();
            await loadDayData();
        } catch (error) {
            console.error('Error updating exercise:', error);
            toast.error('Failed to update exercise. Please try again.');
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

    // Favorite toggle handler
    const handleFavoriteToggle = async (exercise: Exercise) => {
        try {
            const wasFavorited = exercise.isFavorite;
            const newFavoriteStatus = !wasFavorited;

            // Optimistic update
            exercise.isFavorite = newFavoriteStatus;
            setScheduledWorkouts(prev => prev.map(workout =>
                workout.exercise.id === exercise.id
                    ? {...workout, exercise: {...workout.exercise, isFavorite: newFavoriteStatus}}
                    : workout
            ));

            const newFavoriteIds = new Set(userFavoriteIds);
            if (newFavoriteStatus) {
                newFavoriteIds.add(exercise.id);
            } else {
                newFavoriteIds.delete(exercise.id);
            }
            setUserFavoriteIds(newFavoriteIds);

            // API call
            const result = await exerciseApi.toggleFavorite(exercise.id);

            // Verify optimistic update
            if (result.isFavorite !== newFavoriteStatus) {
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
            console.error('Failed to toggle favorite:', error);
            // Revert optimistic update
            exercise.isFavorite = !exercise.isFavorite;
            setScheduledWorkouts(prev => prev.map(workout =>
                workout.exercise.id === exercise.id
                    ? {...workout, exercise: {...workout.exercise, isFavorite: !exercise.isFavorite}}
                    : workout
            ));
            toast.error('Failed to update favorites');
        }
    };

    // Workout handlers
    const handleStartWorkout = async (exerciseId: string) => {
        try {
            const targetExercise = viewingDateExercises.find(ex => ex.id === exerciseId);
            if (!targetExercise) {
                toast.error('Exercise not found');
                return;
            }

            const compatibleExercise = {
                ...targetExercise,
                exercise: {
                    ...targetExercise.exercise,
                    name: targetExercise.exercise.name || targetExercise.exercise.exerciseName || 'Unknown Exercise',
                    exerciseName: targetExercise.exercise.exerciseName || targetExercise.exercise.name || 'Unknown Exercise'
                },
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

            startWorkout([compatibleExercise], viewingDateString);
            navigate('/workout');
            toast.success(`Started workout with ${compatibleExercise.exercise.name}!`);
        } catch (error) {
            console.error('Failed to start workout:', error);
            toast.error('Failed to start workout');
        }
    };

    const handleStartFullWorkout = async () => {
        try {
            if (viewingDateExercises.length === 0) {
                toast.error('No exercises scheduled for today');
                return;
            }

            const compatibleExercises = viewingDateExercises.map(scheduledExercise => ({
                ...scheduledExercise,
                exercise: {
                    ...scheduledExercise.exercise,
                    name: scheduledExercise.exercise.name || scheduledExercise.exercise.exerciseName || 'Unknown Exercise',
                    exerciseName: scheduledExercise.exercise.exerciseName || scheduledExercise.exercise.name || 'Unknown Exercise'
                },
                targetSets: scheduledExercise.targetSets || 3,
                targetReps: scheduledExercise.targetReps || 10,
                targetWeight: scheduledExercise.targetWeight || undefined,
                targetWeightUnit: scheduledExercise.targetWeightUnit || 'lbs',
                restSeconds: scheduledExercise.restSeconds || 90,
                targetRpe: scheduledExercise.targetRpe || 7,
                completed: scheduledExercise.completed || false,
                createdAt: scheduledExercise.createdAt || new Date().toISOString(),
                userId: scheduledExercise.userId || 'current_user'
            }));

            startWorkout(compatibleExercises, viewingDateString);
            navigate('/workout');
            toast.success(`Started workout with ${compatibleExercises.length} exercises!`);
        } catch (error) {
            console.error('Failed to start full workout:', error);
            toast.error('Failed to start workout');
        }
    };

    const handleDeleteWorkout = async (workoutId: string) => {
        const workout = viewingDateExercises.find(ex => ex.id === workoutId);

        if (workout?.completed) {
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

        if (!window.confirm('Are you sure you want to delete this workout?')) {
            return;
        }

        try {
            await calendarApi.deleteWorkout(workoutId);
            toast.success('Workout deleted successfully');
            await loadDayData();
        } catch (error: any) {
            console.error('Error deleting workout:', error);

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
            } else {
                toast.error('Failed to delete workout');
            }
        }
    };

    const handleViewWorkoutDetails = (exerciseId: string) => {
        const exercise = viewingDateExercises.find(ex => ex.id === exerciseId);
        const results = workoutResults[exerciseId];

        if (exercise && results) {
            setSelectedExerciseForDetails(exercise);
            setSelectedWorkoutResults(results);
            setShowWorkoutDetailsModal(true);
        } else {
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

    // Utility functions
    const resetSchedulingState = () => {
        setSelectedExercise(null);
        setExerciseConfig(null);
        setSelectedWorkoutPlan(null);
        setSchedulingMode('exercise');
        setShowConfigModal(false);
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

    const handleModeChange = (mode: 'exercise' | 'workout-plan') => {
        setSchedulingMode(mode);
        if (mode === 'exercise') {
            setSelectedWorkoutPlan(null);
        } else {
            setSelectedExercise(null);
            setExerciseConfig(null);
        }
    };

    const isToday = () => {
        const today = new Date();
        return viewingDate.toDateString() === today.toDateString();
    };

    return (
        <div className="w-full min-h-screen bg-gray-50 pb-20">
            <div className="px-3 sm:px-4 lg:px-6 py-3 sm:py-4 lg:py-6 space-y-4 sm:space-y-6 max-w-4xl mx-auto">

                {/* Date Header Component */}
                <DateHeader
                    viewingDate={viewingDate}
                    loading={loading}
                    viewingDateExercises={viewingDateExercises}
                    onNavigateDay={navigateDay}
                    onGoToToday={goToToday}
                    onManualRefresh={() => refreshCalendarData(true)}
                />

                {/* Start Workout Button */}
                {isToday() && viewingDateExercises.length > 0 && (
                    <Button
                        className="w-full bg-green-600 hover:bg-green-700 text-white py-3 sm:py-4 lg:py-6 text-base sm:text-lg lg:text-xl font-bold flex items-center justify-center gap-2 sm:gap-3 shadow-lg rounded-xl sm:rounded-2xl"
                        onClick={handleStartFullWorkout}
                    >
                        <Play className="w-5 h-5 sm:w-6 sm:h-6 lg:w-8 lg:h-8"/>
                        Start Today's Workout ({viewingDateExercises.length} exercises)
                    </Button>
                )}

                {/* Week Calendar Component */}
                <WeekCalendar
                    viewingDate={viewingDate}
                    scheduledWorkouts={scheduledWorkouts}
                    onDateSelect={setViewingDate}
                />

                {/* Exercises List */}
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
                                {viewingDateExercises.map((exercise, index) => (
                                    <ExerciseCard
                                        key={exercise.id}
                                        exercise={exercise}
                                        index={index}
                                        workoutResults={workoutResults[exercise.id]}
                                        onStartWorkout={handleStartWorkout}
                                        onEditExercise={handleEditExercise}
                                        onDeleteWorkout={handleDeleteWorkout}
                                        onViewDetails={handleViewWorkoutDetails}
                                        onFavoriteToggle={handleFavoriteToggle}
                                    />
                                ))}
                            </div>
                        </CardContent>
                    </Card>
                ) : (
                    /* Empty State */
                    <Card className="shadow-sm border-dashed border-2 border-gray-300">
                        <CardContent className="py-8 sm:py-12 lg:py-16 text-center">
                            <div className="text-4xl sm:text-5xl lg:text-6xl mb-3 sm:mb-4 lg:mb-6">
                                {isToday() ? '🎯' : '📅'}
                            </div>
                            <h3 className="text-base sm:text-lg lg:text-xl font-bold text-gray-900 mb-2">
                                {isToday() ? 'No workouts planned for today' : 'No workouts planned'}
                            </h3>
                            <p className="text-sm sm:text-base text-gray-600 mb-4 sm:mb-6">
                                {isToday() ? 'Ready to start your fitness journey?' : 'Plan ahead for a successful workout'}
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
                                    Add Exercise
                                </Button>
                                <Button
                                    onClick={() => {
                                        setSchedulingMode('workout-plan');
                                        setShowExerciseSelector(true);
                                    }}
                                    className="bg-purple-600 hover:bg-purple-700 text-white px-4 sm:px-6 py-2 sm:py-3 text-sm sm:text-base font-semibold rounded-lg sm:rounded-xl flex items-center justify-center"
                                >
                                    <Settings className="w-4 h-4 sm:w-5 sm:h-5 mr-2"/>
                                    Add Workout Plan
                                </Button>
                            </div>
                        </CardContent>
                    </Card>
                )}

                {/* Quick Stats */}
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

            {/* Modals */}
            {showExerciseSelector && (
                <EnhancedExerciseSelector
                    open={showExerciseSelector}
                    onClose={() => setShowExerciseSelector(false)}
                    onExerciseSelect={handleExerciseSelect}
                    onWorkoutPlanSelect={handleWorkoutPlanSelect}
                    onWorkoutPlanConfigure={handleWorkoutPlanConfigure}
                    selectedDate={viewingDateString}
                    calendarDays={[]}
                    onDateChange={(dateString) => {
                        const newDate = new Date(dateString);
                        setViewingDate(newDate);
                    }}
                    title={`Add to ${isToday() ? 'Today' : 'Selected Date'}`}
                    initialTab={schedulingMode === 'workout-plan' ? 1 : 0}
                />
            )}

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

            {showWorkoutPlanConfigModal && selectedWorkoutPlan && (
                <WorkoutPlanConfigModal
                    isOpen={showWorkoutPlanConfigModal}
                    onClose={() => {
                        setShowWorkoutPlanConfigModal(false);
                        setSelectedWorkoutPlan(null);
                    }}
                    workoutPlan={selectedWorkoutPlan}
                    selectedDate={viewingDate}
                    onSchedule={handleWorkoutPlanConfigSave}
                    loading={loading}
                />
            )}

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