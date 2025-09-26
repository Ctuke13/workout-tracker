import {useState, useEffect, useMemo} from 'react';
import {useWorkoutEventListener} from './useWorkoutEventListener';
import {toast} from 'react-hot-toast';
import {calendarApi} from '../services/calendarApi';
import {exerciseApi} from '../services/exerciseApi';
import {transformScheduledWorkoutsToCalendarData} from '../services/transformers';
import {ScheduledExercise, WorkoutStats, WorkoutResults} from '../types/exercise';

interface UseCalendarDataReturn {
    scheduledWorkouts: ScheduledExercise[];
    stats: WorkoutStats | null;
    userFavoriteIds: Set<number>;
    workoutResults: Record<string, WorkoutResults>;
    loading: boolean;
    viewingDateString: string;
    viewingDateExercises: ScheduledExercise[];
    loadDayData: (forceCacheBust?: boolean) => Promise<void>;
    refreshCalendarData: (showToast?: boolean, forceCacheBust?: boolean) => Promise<void>;
    setScheduledWorkouts: React.Dispatch<React.SetStateAction<ScheduledExercise[]>>;
    setUserFavoriteIds: React.Dispatch<React.SetStateAction<Set<number>>>;
}

export const useCalendarData = (viewingDate: Date): UseCalendarDataReturn => {
    const [scheduledWorkouts, setScheduledWorkouts] = useState<ScheduledExercise[]>([]);
    const [stats, setStats] = useState<WorkoutStats | null>(null);
    const [loading, setLoading] = useState(false);
    const [userFavoriteIds, setUserFavoriteIds] = useState<Set<number>>(new Set());
    const [workoutResults, setWorkoutResults] = useState<Record<string, WorkoutResults>>({});

    const viewingDateString = useMemo(() => {
        return viewingDate.toISOString().split('T')[0];
    }, [viewingDate]);

    const viewingDateExercises = useMemo(() => {
        const dateString = viewingDate.toISOString().split('T')[0];
        return scheduledWorkouts.filter(workout => workout.scheduledDate === dateString);
    }, [viewingDate, scheduledWorkouts]);

    const loadDayData = async (forceCacheBust = false) => {
        if (loading && !forceCacheBust) return;

        try {
            setLoading(true);

            const startDate = new Date(viewingDate);
            startDate.setDate(viewingDate.getDate() - 3);
            const endDate = new Date(viewingDate);
            endDate.setDate(viewingDate.getDate() + 3);

            const startDateStr = startDate.toISOString().split('T')[0];
            const endDateStr = endDate.toISOString().split('T')[0];

            const [apiResponse, favoriteIds] = await Promise.all([
                calendarApi.getScheduledExercises(startDateStr, endDateStr),
                exerciseApi.getFavoriteExerciseIds().catch(() => new Set<number>())
            ]);

            const transformedWorkouts = transformScheduledWorkoutsToCalendarData(apiResponse);

            const workoutsWithFavorites = transformedWorkouts.map(workout => ({
                ...workout,
                exercise: {
                    ...workout.exercise,
                    isFavorite: favoriteIds.has(workout.exercise.id)
                }
            }));

            const sortedWorkouts = workoutsWithFavorites.sort((a, b) => {
                if (a.completed !== b.completed) {
                    return a.completed ? 1 : -1;
                }
                if (!a.completed && !b.completed) {
                    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
                }
                return 0;
            });

            setScheduledWorkouts(sortedWorkouts);
            setUserFavoriteIds(favoriteIds);

            // Load workout results for completed exercises
            const completedExercises = sortedWorkouts.filter(ex => ex.completed);
            if (completedExercises.length > 0) {
                try {
                    const exerciseIds = completedExercises.map(ex => ex.id);
                    const results = await calendarApi.getBatchWorkoutResults(exerciseIds);
                    setWorkoutResults(results);
                } catch (error) {
                    console.error('Failed to load workout results:', error);
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
            const apiStats = await calendarApi.getWorkoutStats();
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

    const refreshCalendarData = async (showToast = false, forceCacheBust = false) => {
        try {
            const [workoutResult, statsResult] = await Promise.allSettled([
                loadDayData(forceCacheBust),
                loadWorkoutStats(forceCacheBust)
            ]);

            if (showToast && workoutResult.status === 'fulfilled') {
                toast.success('Calendar updated successfully!');
            }
        } catch (error) {
            console.error('Failed to refresh calendar data:', error);
            if (showToast) {
                toast.error('Failed to refresh calendar data');
            }
        }
    };

    // Add workout completion event listener
    useWorkoutEventListener((detail) => {
        console.log('Calendar refreshing due to workout completion:', detail);

        // Force refresh calendar data
        refreshCalendarData(true, true); // showToast=true, forceCacheBust=true

        // If the completed workout is for the currently viewed date, reload that day's data
        if (detail.date === viewingDateString) {
            loadDayData(true);
        }
    });

    useEffect(() => {
        loadDayData();
        loadWorkoutStats();
    }, [viewingDateString]);

    return {
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
    };
};