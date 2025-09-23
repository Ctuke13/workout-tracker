import {useMemo} from 'react';
import {ScheduledExercise, WorkoutResults} from '../types/exercise';
import {analyzeWorkout} from '../utils/workoutPerformanceAnalyzer';

export const useWorkoutAnalysis = (exercise: ScheduledExercise, workoutResults?: WorkoutResults) => {
    return useMemo(() => {
        if (!workoutResults) return null;
        return analyzeWorkout(exercise, workoutResults);
    }, [exercise, workoutResults]);
};