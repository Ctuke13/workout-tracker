import {useState, useEffect, useRef} from 'react';
import {useNavigate} from 'react-router-dom';
import {useWorkout} from '../contexts/WorkoutContext';
import {Exercise, ExerciseConfiguration} from '../types/exercise';
import {toast} from 'react-hot-toast'

export interface SetData {
    // Common fields
    actualReps?: number;
    notes?: string;
    actualRestSeconds?: number;

    // Strength-specific
    actualWeight?: number;
    actualRpe?: number;


    // Cardio-specific
    actualDurationMinutes?: number;
    actualDistance?: number;
    actualPace?: number;
    averageHeartRate?: number;
    caloriesBurned?: number;

    // Isometric-specific
    actualHoldSeconds?: number;
}

export const useWorkoutMode = () => {

    const navigate = useNavigate();
    const {
        currentWorkout,
        getCurrentExercise,
        getCurrentSet,
        goToNextExercise,
        goToPreviousExercise,
        completeSet,
        skipSet,
        addSet,
        removeSet,
        completeWorkout,
        pauseWorkout,
        resumeWorkout,
        cancelWorkout,
        getTotalDuration,
        getCompletionPercentage,
        isWorkoutActive,
        isPaused,
        canGoNext,
        canGoPrevious,
        addExerciseToCurrentWorkout,
        setRestTimeForNextSet,
        getRestTimeForNextSet,
        clearRestTimeForNextSet
    } = useWorkout();

    // Current exercise and set tracking
    const currentExercise = getCurrentExercise();
    const currentSet = getCurrentSet();

    // Refs for timer management
    const timerRef = useRef<NodeJS.Timeout | null>(null);
    const restTimerRef = useRef<NodeJS.Timeout | null>(null);

    // Set input tracking for different exercise types
    const [setData, setSetData] = useState<SetData>({});

    // Timer states for different exercise types
    const [isTimerRunning, setIsTimerRunning] = useState(false);
    const [timerSeconds, setTimerSeconds] = useState(0);
    const [isRestTimer, setIsRestTimer] = useState(false);
    const [restTimeRemaining, setRestTimeRemaining] = useState(0);

    // UI states
    const [showSetDialog, setShowSetDialog] = useState(false);
    const [showConfetti, setShowConfetti] = useState(false);

    const [restStartTime, setRestStartTime] = useState<Date | null>(null);
    const [currentRestSeconds, setCurrentRestSeconds] = useState(0);

    const [workoutStartTime, setWorkoutStartTime] = useState<number | null>(null);
    const [workoutDuration, setWorkoutDuration] = useState(0);

    // Redirect if no workout is active - Enhanced with better error handling
    useEffect(() => {
        if (!isWorkoutActive) {
            navigate('/calendar');
            return;
        }
    }, [isWorkoutActive, navigate]);

    // Cleanup timers on unmount
    useEffect(() => {
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
            if (restTimerRef.current) clearInterval(restTimerRef.current);
        };
    }, []);

// ✅ ADD THIS ENTIRE useEffect:
// Track total workout duration
    useEffect(() => {
        console.log('🔍 Timer effect running:', {
            isWorkoutActive,
            workoutStartTime,
            currentDuration: workoutDuration,
            hasStartTime: workoutStartTime !== null
        });

        // Start timer when workout becomes active
        if (isWorkoutActive && workoutStartTime === null) {
            const startTime = Date.now();
            setWorkoutStartTime(startTime);
            console.log('✅ TIMER STARTED at:', new Date(startTime).toLocaleTimeString(), 'timestamp:', startTime);
        }

        // Stop timer when workout is no longer active
        if (!isWorkoutActive && workoutStartTime !== null) {
            console.log('⏹️ TIMER STOPPED. Final duration:', workoutDuration, 'seconds');
            setWorkoutStartTime(null);
            setWorkoutDuration(0);
        }

        // Update duration every second while workout is active
        if (isWorkoutActive && workoutStartTime !== null) {
            console.log('⏱️ Starting interval timer. Current duration:', workoutDuration);

            const interval = setInterval(() => {
                const elapsed = Math.floor((Date.now() - workoutStartTime) / 1000);
                setWorkoutDuration(elapsed);
                console.log('⏱️ TICK:', elapsed, 'seconds'); // Will log every second
            }, 1000);

            return () => {
                console.log('🧹 Cleaning up timer interval');
                clearInterval(interval);
            };
        }
    }, [isWorkoutActive, workoutStartTime]);

// Initialize set data when current set changes - Improved with better defaults
    useEffect(() => {
        if (currentSet && currentExercise) {
            const exercise = currentExercise.scheduledExercise.exercise;

            // Initialize with appropriate defaults based on exercise type
            const initialData: SetData = {};

            if (exercise.isCardio) {
                initialData.actualDurationMinutes = currentExercise.scheduledExercise.targetDurationMinutes || 20;
                initialData.actualDistance = currentExercise.scheduledExercise.targetDistance || currentExercise.scheduledExercise.targetDistanceKm;
                initialData.actualPace = currentExercise.scheduledExercise.targetPace;
            } else if (exercise.isIsometric) {
                initialData.actualHoldSeconds = currentExercise.scheduledExercise.holdDurationSeconds || 30;
            } else {
                // Strength exercise
                initialData.actualReps = currentSet.targetReps;
                initialData.actualWeight = currentSet.targetWeight;
                initialData.actualRpe = currentSet.targetRpe;
            }

            setSetData(initialData);

            // Reset timers when switching sets
            setTimerSeconds(0);
            setIsTimerRunning(false);
            setIsRestTimer(false);

            // Auto-start timer for isometric exercises
            if (exercise.isIsometric && !currentSet.completed) {
                setTimerSeconds(0);
            }
        }
    }, [currentSet?.id, currentExercise?.id]);

    useEffect(() => {
        let interval: NodeJS.Timeout | null = null;

        if (restStartTime && !isRestTimer) {
            interval = setInterval(() => {
                const elapsed = Math.floor((Date.now() - restStartTime.getTime()) / 1000);
                setCurrentRestSeconds(elapsed);
            }, 1000);
        }

        return () => {
            if (interval) clearInterval(interval);
        };
    }, [restStartTime, isRestTimer]);

// Main timer effect - Enhanced with better cleanup
    useEffect(() => {
        if (isTimerRunning && timerRef.current === null) {
            timerRef.current = setInterval(() => {
                setTimerSeconds(prev => prev + 1);
            }, 1000);
        } else if (!isTimerRunning && timerRef.current) {
            clearInterval(timerRef.current);
            timerRef.current = null;
        }

        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
                timerRef.current = null;
            }
        };
    }, [isTimerRunning]);

// Rest timer effect - Improved with better notifications
    useEffect(() => {
        if (isRestTimer && restTimeRemaining > 0 && restTimerRef.current === null) {
            restTimerRef.current = setInterval(() => {
                setRestTimeRemaining(prev => {
                    if (prev <= 1) {
                        setIsRestTimer(false);
                        toast.success('Rest period complete! Ready for next set.');
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);
        } else if ((!isRestTimer || restTimeRemaining <= 0) && restTimerRef.current) {
            clearInterval(restTimerRef.current);
            restTimerRef.current = null;
        }

        return () => {
            if (restTimerRef.current) {
                clearInterval(restTimerRef.current);
                restTimerRef.current = null;
            }
        };
    }, [isRestTimer, restTimeRemaining]);

    return {
        // Context data
        navigate,
        currentWorkout,
        currentExercise,
        currentSet,
        goToNextExercise,
        goToPreviousExercise,
        completeSet,
        skipSet,
        addSet,
        removeSet,
        completeWorkout,
        pauseWorkout,
        resumeWorkout,
        cancelWorkout,
        getTotalDuration,
        getCompletionPercentage,
        isWorkoutActive,
        isPaused,
        canGoNext,
        canGoPrevious,
        addExerciseToCurrentWorkout,
        setRestTimeForNextSet,
        getRestTimeForNextSet,
        clearRestTimeForNextSet,

        // Existing state
        setData,
        setSetData,
        isTimerRunning,
        setIsTimerRunning,
        timerSeconds,
        setTimerSeconds,
        isRestTimer,
        setIsRestTimer,
        restTimeRemaining,
        setRestTimeRemaining,
        showSetDialog,
        setShowSetDialog,
        showConfetti,
        setShowConfetti,

        restStartTime,
        setRestStartTime,
        currentRestSeconds,
        setCurrentRestSeconds,

        workoutStartTime,
        workoutDuration,
    };
};