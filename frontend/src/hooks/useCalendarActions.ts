import {useState} from 'react';
import {toast} from 'react-hot-toast';
import {useNavigate} from 'react-router-dom';
import {useWorkout} from '../contexts/WorkoutContext';
import {calendarApi} from '../services/calendarApi';
import {exerciseApi} from '../services/exerciseApi';
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
import {WorkoutPlanInfo} from '../types/api';
import {WorkoutPlanConfiguration, WorkoutPlanExerciseConfig} from '../types/calendar';


export const useCalendarActions = (
    viewingDate: Date,
    viewingDateString: string,
    viewingDateExercises: ScheduledExercise[],
    workoutResults: Record<string, WorkoutResults>,
    loadDayData: (forceRefresh?: boolean) => Promise<void>,
    refreshCalendarData: (forceRefresh?: boolean) => Promise<void>,
    setScheduledWorkouts: React.Dispatch<React.SetStateAction<ScheduledExercise[]>>,
    setUserFavoriteIds: React.Dispatch<React.SetStateAction<Set<number>>>,
    userFavoriteIds: Set<number>
) => {
    const navigate = useNavigate();
    const {startWorkout} = useWorkout();

    // Exercise scheduling state
    const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
    const [exerciseConfig, setExerciseConfig] = useState<ExerciseConfiguration | null>(null);

    // Workout plan state
    const [selectedWorkoutPlan, setSelectedWorkoutPlan] = useState<WorkoutPlanInfo | null>(null);

    // Editing state
    const [editingExercise, setEditingExercise] = useState<ScheduledExercise | null>(null);
    const [isEditMode, setIsEditMode] = useState(false);

    // Workout details modal state
    const [selectedExerciseForDetails, setSelectedExerciseForDetails] = useState<ScheduledExercise | null>(null);
    const [selectedWorkoutResults, setSelectedWorkoutResults] = useState<WorkoutResults | null>(null);

    // Exercise scheduling handlers
    const handleExerciseSelect = (exercise: Exercise) => {
        setSelectedExercise(exercise);
        const defaultConfig = getDefaultConfigForExercise(exercise);
        setExerciseConfig(defaultConfig);
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
            const scheduleRequest = {
                workoutPlanId: workoutPlan.id,
                scheduledDate: viewingDateString,
                customNotes: `${workoutPlan.name || 'Workout Plan'} - ${workoutPlan.description || ''}`
            };

            await calendarApi.scheduleWorkout(scheduleRequest);
            await loadDayData();
            toast.success('Workout plan scheduled successfully!');
        } catch (error) {
            console.error('Error scheduling workout plan:', error);
            toast.error('Failed to schedule workout plan');
        }
    };

    const handleWorkoutPlanConfigure = (workoutPlan: WorkoutPlanInfo) => {
        setSelectedWorkoutPlan(workoutPlan);
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
        setIsEditMode(true);
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
            // Filter out completed exercises FIRST
            const uncompletedExercises = viewingDateExercises.filter(ex => !ex.completed);

            if (uncompletedExercises.length === 0) {
                toast.success('All exercises completed! Great job! 🎉');
                return;
            }

            const compatibleExercises = uncompletedExercises.map(scheduledExercise => ({
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

            const exercisesInOrder = [...compatibleExercises].reverse();

            startWorkout(exercisesInOrder, viewingDateString);
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
            return true; // ✅ Signal that data is ready
        } else {
            const fetchResults = async () => {
                try {
                    const fetchedResults = await calendarApi.getWorkoutResults(exerciseId);
                    if (fetchedResults && exercise) {
                        setSelectedExerciseForDetails(exercise);
                        setSelectedWorkoutResults(fetchedResults);
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
            return false; // ✅ Data not immediately available
        }
    };

    // Utility functions
    const resetSchedulingState = () => {
        setSelectedExercise(null);
        setExerciseConfig(null);
        setSelectedWorkoutPlan(null);
    };

    const resetEditingState = () => {
        setEditingExercise(null);
        setIsEditMode(false);
        setSelectedExercise(null);
        setExerciseConfig(null);
        setSelectedWorkoutPlan(null);
    };

    return {
        // State
        selectedExercise,
        exerciseConfig,
        selectedWorkoutPlan,
        editingExercise,
        isEditMode,
        selectedExerciseForDetails,
        selectedWorkoutResults,

        // Setters
        setSelectedExercise,
        setExerciseConfig,
        setSelectedWorkoutPlan,
        setEditingExercise,
        setIsEditMode,
        setSelectedExerciseForDetails,
        setSelectedWorkoutResults,

        // Handlers
        handleExerciseSelect,
        handleConfigChange,
        handleSaveExercise,
        handleWorkoutPlanSelect,
        handleWorkoutPlanConfigure,
        handleWorkoutPlanConfigSave,
        handleEditExercise,
        handleSaveEditedExercise,
        handleFavoriteToggle,
        handleStartWorkout,
        handleStartFullWorkout,
        handleDeleteWorkout,
        handleViewWorkoutDetails,
        resetSchedulingState,
        resetEditingState
    };
};