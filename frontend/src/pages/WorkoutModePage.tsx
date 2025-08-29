import React, {useState, useEffect, useRef} from 'react';
import {useNavigate} from 'react-router-dom';
import {useWorkout} from '../contexts/WorkoutContext';
import {
    Play,
    Pause,
    SkipForward,
    SkipBack,
    CheckCircle,
    Clock,
    Plus,
    Minus,
    Target,
    Weight,
    Timer,
    Heart,
    RotateCcw,
    Home,
    Trophy,
    Activity,
    Zap,
    ArrowRight
} from 'lucide-react';
import {Button} from '../components/ui/button';
import {Card, CardContent, CardHeader, CardTitle} from '../components/ui/card';
import {Badge} from '../components/ui/badge';
import {Input} from '../components/ui/input';
import {toast} from 'react-hot-toast';

// Enhanced interface for set tracking that handles all exercise types
interface EnhancedSetData {
    // Common fields
    actualReps?: number;
    notes?: string;

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

// Confetti component for workout completion
const ConfettiEffect: React.FC<{ show: boolean; onComplete: () => void }> = ({show, onComplete}) => {
    useEffect(() => {
        if (show) {
            const timer = setTimeout(() => {
                onComplete();
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, onComplete]);

    if (!show) return null;

    return (
        <div className="fixed inset-0 z-50 pointer-events-none overflow-hidden">
            <div className="absolute inset-0 flex items-center justify-center">
                <div className="text-8xl animate-bounce">🎉</div>
            </div>
            {/* Animated confetti pieces */}
            {Array.from({length: 50}).map((_, i) => (
                <div
                    key={i}
                    className="absolute animate-pulse"
                    style={{
                        left: `${Math.random() * 100}%`,
                        top: `${Math.random() * 100}%`,
                        animationDelay: `${Math.random() * 3}s`,
                        fontSize: `${Math.random() * 20 + 10}px`,
                    }}
                >
                    {['🎊', '✨', '🌟', '💪', '🏆', '🎉'][Math.floor(Math.random() * 6)]}
                </div>
            ))}
        </div>
    );
};

// Set completion dialog component
const SetCompletionDialog: React.FC<{
    show: boolean;
    isLastSet: boolean;
    isLastExercise: boolean;
    exerciseName: string;
    onNextExercise: () => void;
    onAddSet: () => void;
    onCompleteWorkout: () => void;
    onClose: () => void;
}> = ({
          show,
          isLastSet,
          isLastExercise,
          exerciseName,
          onNextExercise,
          onAddSet,
          onCompleteWorkout,
          onClose
      }) => {
    if (!show) return null;

    return (
        <div className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
            <Card className="w-full max-w-md bg-gray-800 border-gray-700 text-white">
                <CardHeader className="pb-3">
                    <CardTitle className="text-center">
                        {isLastSet ? (
                            <div className="space-y-2">
                                <div className="text-2xl">🎯</div>
                                <div>Set Complete!</div>
                            </div>
                        ) : (
                            <div className="space-y-2">
                                <div className="text-2xl">✅</div>
                                <div>Great job!</div>
                            </div>
                        )}
                    </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    {isLastSet ? (
                        <div className="space-y-4">
                            <p className="text-center text-gray-300">
                                You've completed all sets for <span
                                className="font-bold text-white">{exerciseName}</span>
                            </p>

                            <div className="space-y-3">
                                <Button
                                    onClick={onAddSet}
                                    variant="outline"
                                    className="w-full bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                >
                                    <Plus className="w-4 h-4 mr-2"/>
                                    Add Another Set
                                </Button>

                                {isLastExercise ? (
                                    <Button
                                        onClick={onCompleteWorkout}
                                        className="w-full bg-green-600 hover:bg-green-700"
                                    >
                                        <Trophy className="w-4 h-4 mr-2"/>
                                        Complete Workout!
                                    </Button>
                                ) : (
                                    <Button
                                        onClick={onNextExercise}
                                        className="w-full bg-blue-600 hover:bg-blue-700"
                                    >
                                        <ArrowRight className="w-4 h-4 mr-2"/>
                                        Next Exercise
                                    </Button>
                                )}

                                <Button
                                    onClick={onClose}
                                    variant="outline"
                                    className="w-full bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                >
                                    Continue Here
                                </Button>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center">
                            <p className="text-gray-300 mb-4">Set completed successfully!</p>
                            <Button
                                onClick={onClose}
                                className="w-full bg-blue-600 hover:bg-blue-700"
                            >
                                Continue
                            </Button>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};

const WorkoutModePage: React.FC = () => {
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
        canGoPrevious
    } = useWorkout();

    // Current exercise and set tracking
    const currentExercise = getCurrentExercise();
    const currentSet = getCurrentSet();

    // Set input tracking for different exercise types
    const [setData, setSetData] = useState<EnhancedSetData>({});

    // Timer states for different exercise types
    const [isTimerRunning, setIsTimerRunning] = useState(false);
    const [timerSeconds, setTimerSeconds] = useState(0);
    const [isRestTimer, setIsRestTimer] = useState(false);
    const [restTimeRemaining, setRestTimeRemaining] = useState(0);

    // UI states
    const [showSetDialog, setShowSetDialog] = useState(false);
    const [showConfetti, setShowConfetti] = useState(false);

    // Refs for timer management
    const timerRef = useRef<NodeJS.Timeout | null>(null);
    const restTimerRef = useRef<NodeJS.Timeout | null>(null);

    // Redirect if no workout is active
    useEffect(() => {
        if (!isWorkoutActive) {
            navigate('/calendar');
            return;
        }
    }, [isWorkoutActive, navigate]);

    // Initialize set data when current set changes
    useEffect(() => {
        if (currentSet && currentExercise) {
            const exercise = currentExercise.scheduledExercise.exercise;

            // Initialize with appropriate defaults based on exercise type
            const initialData: EnhancedSetData = {};

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

    // Timer effect for exercise and rest
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

    // Rest timer effect
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

    // Cleanup timers on unmount
    useEffect(() => {
        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
            if (restTimerRef.current) clearInterval(restTimerRef.current);
        };
    }, []);

    if (!currentWorkout || !currentExercise || !currentSet) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-900">
                <Card className="w-full max-w-md mx-4 bg-gray-800 border-gray-700">
                    <CardContent className="p-8 text-center">
                        <div className="text-6xl mb-4">🏋️‍♂️</div>
                        <h2 className="text-xl font-bold text-white mb-2">No Active Workout</h2>
                        <p className="text-gray-400 mb-6">Start a workout from your calendar to begin tracking.</p>
                        <Button
                            onClick={() => navigate('/calendar')}
                            className="w-full bg-blue-600 hover:bg-blue-700"
                        >
                            <Home className="w-4 h-4 mr-2"/>
                            Go to Calendar
                        </Button>
                    </CardContent>
                </Card>
            </div>
        );
    }

    const exercise = currentExercise.scheduledExercise.exercise;
    const isCardio = exercise.isCardio;
    const isIsometric = exercise.isIsometric;
    const isStrength = !isCardio && !isIsometric;

    // Check if this is the last set of the current exercise
    const isLastSetOfExercise = currentSet.setNumber === currentExercise.sets.length;

    // Check if this is the last exercise of the workout
    const isLastExerciseOfWorkout = currentWorkout.currentExerciseIndex === currentWorkout.exercises.length - 1;

    // Timer utilities
    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const startTimer = () => {
        setIsTimerRunning(true);
        if (isIsometric && timerSeconds === 0) {
            toast.success('Hold timer started!');
        }
    };

    const stopTimer = () => {
        setIsTimerRunning(false);
        if (isIsometric) {
            setSetData(prev => ({...prev, actualHoldSeconds: timerSeconds}));
        } else if (isCardio) {
            setSetData(prev => ({...prev, actualDurationMinutes: Math.round(timerSeconds / 60 * 10) / 10}));
        }
    };

    const resetTimer = () => {
        setIsTimerRunning(false);
        setTimerSeconds(0);
        if (isIsometric) {
            setSetData(prev => ({...prev, actualHoldSeconds: undefined}));
        }
    };

    const startRestTimer = () => {
        const restSeconds = currentSet.restSeconds || 90;
        setRestTimeRemaining(restSeconds);
        setIsRestTimer(true);
        toast.success(`Rest timer started: ${formatTime(restSeconds)}`);
    };

    // Set completion handlers
    const handleCompleteSet = () => {
        if (!currentSet) return;

        // Validate required fields based on exercise type
        let isValid = true;
        let errorMessage = '';

        if (isCardio) {
            if (!setData.actualDurationMinutes || setData.actualDurationMinutes <= 0) {
                isValid = false;
                errorMessage = 'Please enter a valid duration';
            }
        } else if (isIsometric) {
            if (!setData.actualHoldSeconds || setData.actualHoldSeconds <= 0) {
                isValid = false;
                errorMessage = 'Please enter a valid hold time';
            }
        } else if (isStrength) {
            if (!setData.actualReps || setData.actualReps <= 0) {
                isValid = false;
                errorMessage = 'Please enter valid reps';
            }
        }

        if (!isValid) {
            toast.error(errorMessage);
            return;
        }

        // Prepare completion data based on exercise type
        const completionData: any = {...setData};

        // For cardio, convert duration to reps (since the backend expects actualReps)
        if (isCardio) {
            completionData.actualReps = setData.actualDurationMinutes;
        }
        // For isometric, convert hold time to reps
        else if (isIsometric) {
            completionData.actualReps = setData.actualHoldSeconds;
        }

        completeSet(currentSet.id, completionData);

        // Stop any running timers
        setIsTimerRunning(false);
        setTimerSeconds(0);

        // Check if this was the last set and show appropriate dialog
        if (isLastSetOfExercise) {
            setShowSetDialog(true);
        } else {
            // Auto-start rest timer if there are more sets and rest time is configured
            if (currentSet.restSeconds && currentSet.restSeconds > 0) {
                setTimeout(() => startRestTimer(), 500);
            }
        }

        // Reset set data for next set
        setSetData({});

        toast.success(`Set ${currentSet.setNumber} completed!`);
    };

    const handleSkipSet = () => {
        if (!currentSet) return;

        skipSet(currentSet.id);
        setIsTimerRunning(false);
        setTimerSeconds(0);
        setSetData({});

        // Check if this was the last set
        if (isLastSetOfExercise) {
            setShowSetDialog(true);
        }

        toast.success(`Set ${currentSet.setNumber} skipped`);
    };

    const handleAddSet = () => {
        if (!currentExercise) return;
        addSet(currentExercise.id);
        setShowSetDialog(false);
        toast.success('Added new set');
    };

    const handleRemoveSet = () => {
        if (!currentSet || currentExercise.sets.length <= 1) return;
        removeSet(currentSet.id);
        toast.success('Removed set');
    };

    // Navigation handlers
    const handlePreviousExercise = () => {
        if (canGoPrevious) {
            goToPreviousExercise();
            setIsTimerRunning(false);
            setTimerSeconds(0);
            setIsRestTimer(false);
            setShowSetDialog(false);
        }
    };

    const handleNextExercise = () => {
        if (canGoNext) {
            goToNextExercise();
            setIsTimerRunning(false);
            setTimerSeconds(0);
            setIsRestTimer(false);
            setShowSetDialog(false);
        }
    };

    const handleCompleteWorkout = () => {
        if (isLastExerciseOfWorkout && isLastSetOfExercise) {
            setShowConfetti(true);
            setTimeout(() => {
                completeWorkout();
                toast.success('Workout completed! Great job! 🎉');
                navigate('/calendar');
            }, 3000);
        } else {
            if (window.confirm('Are you sure you want to complete this workout?')) {
                completeWorkout();
                toast.success('Workout completed! Great job! 🎉');
                navigate('/calendar');
            }
        }
        setShowSetDialog(false);
    };

    const handleCancelWorkout = () => {
        if (window.confirm('Are you sure you want to cancel this workout? All progress will be lost.')) {
            cancelWorkout();
            toast.error('Workout cancelled');
            navigate('/calendar');
        }
    };

    // Get exercise type styling
    const getExerciseTypeStyle = () => {
        if (isCardio) {
            return {
                bg: 'bg-red-900/20',
                border: 'border-red-700',
                text: 'text-red-300',
                icon: '❤️',
                gradient: 'from-red-600 to-pink-600'
            };
        } else if (isIsometric) {
            return {
                bg: 'bg-purple-900/20',
                border: 'border-purple-700',
                text: 'text-purple-300',
                icon: '🛡️',
                gradient: 'from-purple-600 to-indigo-600'
            };
        } else {
            return {
                bg: 'bg-blue-900/20',
                border: 'border-blue-700',
                text: 'text-blue-300',
                icon: '💪',
                gradient: 'from-blue-600 to-cyan-600'
            };
        }
    };

    const typeStyle = getExerciseTypeStyle();

    return (
        <div className="min-h-screen bg-gray-900 text-white pb-6">
            {/* Confetti Effect */}
            <ConfettiEffect
                show={showConfetti}
                onComplete={() => setShowConfetti(false)}
            />

            {/* Set Completion Dialog */}
            <SetCompletionDialog
                show={showSetDialog}
                isLastSet={isLastSetOfExercise}
                isLastExercise={isLastExerciseOfWorkout}
                exerciseName={exercise.name || exercise.exerciseName || 'Exercise'}
                onNextExercise={() => {
                    setShowSetDialog(false);
                    handleNextExercise();
                }}
                onAddSet={handleAddSet}
                onCompleteWorkout={handleCompleteWorkout}
                onClose={() => setShowSetDialog(false)}
            />

            {/* Header with workout progress */}
            <div className={`bg-gradient-to-r ${typeStyle.gradient} text-white p-4 shadow-lg`}>
                <div className="max-w-4xl mx-auto">
                    <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-3">
                            <div className="text-2xl">{typeStyle.icon}</div>
                            <div>
                                <h1 className="text-xl font-bold">
                                    {exercise.name || exercise.exerciseName}
                                </h1>
                                <p className="text-sm opacity-90">
                                    Exercise {currentWorkout.currentExerciseIndex + 1} of {currentWorkout.exercises.length}
                                </p>
                            </div>
                        </div>
                        <div className="text-right">
                            <div className="text-lg font-bold">{getCompletionPercentage()}%</div>
                            <div className="text-sm opacity-90">Complete</div>
                        </div>
                    </div>

                    {/* Progress bar */}
                    <div className="w-full bg-white/20 rounded-full h-3">
                        <div
                            className="bg-white rounded-full h-3 transition-all duration-300"
                            style={{width: `${getCompletionPercentage()}%`}}
                        />
                    </div>
                </div>
            </div>

            <div className="max-w-4xl mx-auto p-4 space-y-6">
                {/* Exercise Navigation */}
                <Card className="bg-gray-800 border-gray-700">
                    <CardContent className="p-4">
                        <div className="flex items-center justify-between">
                            <Button
                                variant="outline"
                                onClick={handlePreviousExercise}
                                disabled={!canGoPrevious}
                                className="flex-1 mr-2 bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                            >
                                <SkipBack className="w-4 h-4 mr-2"/>
                                Previous
                            </Button>

                            <div className="flex-1 text-center">
                                <Badge variant="secondary" className="text-sm bg-gray-700 text-white">
                                    Set {currentSet.setNumber} of {currentExercise.sets.length}
                                </Badge>
                            </div>

                            <Button
                                variant="outline"
                                onClick={handleNextExercise}
                                disabled={!canGoNext}
                                className="flex-1 ml-2 bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                            >
                                Next
                                <SkipForward className="w-4 h-4 ml-2"/>
                            </Button>
                        </div>
                    </CardContent>
                </Card>

                {/* Rest Timer Display */}
                {isRestTimer && (
                    <Card className="border-orange-700 bg-orange-900/20">
                        <CardContent className="p-4 text-center">
                            <div className="text-2xl mb-2">⏰</div>
                            <h3 className="text-lg font-bold text-orange-300 mb-2">Rest Time</h3>
                            <div className="text-3xl font-bold text-orange-400 mb-2">
                                {formatTime(restTimeRemaining)}
                            </div>
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => {
                                    setIsRestTimer(false);
                                    setRestTimeRemaining(0);
                                }}
                                className="text-orange-400 border-orange-600 bg-gray-800 hover:bg-gray-700"
                            >
                                Skip Rest
                            </Button>
                        </CardContent>
                    </Card>
                )}
                
                {/* Exercise-Specific Tracking */}
                {isCardio ? (
                    /* Cardio Tracking Interface */
                    <Card className={`${typeStyle.bg} ${typeStyle.border} bg-gray-800`}>
                        <CardHeader className="pb-3">
                            <CardTitle className={`${typeStyle.text} flex items-center gap-2`}>
                                <Heart className="w-5 h-5"/>
                                Cardio Session
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {/* Timer Display */}
                            <div className="text-center py-6">
                                <div className="text-4xl font-bold text-white mb-2">
                                    {formatTime(timerSeconds)}
                                </div>
                                <div className="flex justify-center gap-2">
                                    <Button
                                        onClick={isTimerRunning ? stopTimer : startTimer}
                                        className={isTimerRunning ? "bg-red-600 hover:bg-red-700" : "bg-green-600 hover:bg-green-700"}
                                    >
                                        {isTimerRunning ? <Pause className="w-4 h-4 mr-2"/> :
                                            <Play className="w-4 h-4 mr-2"/>}
                                        {isTimerRunning ? 'Stop Timer' : 'Start Timer'}
                                    </Button>
                                    <Button variant="outline" onClick={resetTimer}
                                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white">
                                        <RotateCcw className="w-4 h-4 mr-2"/>
                                        Reset
                                    </Button>
                                </div>
                            </div>

                            {/* Cardio Duration Input */}
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Duration (minutes) *
                                </label>
                                <Input
                                    type="number"
                                    step="0.1"
                                    value={setData.actualDurationMinutes || ''}
                                    onChange={(e) => setSetData(prev => ({
                                        ...prev,
                                        actualDurationMinutes: parseFloat(e.target.value) || undefined
                                    }))}
                                    placeholder="Enter duration in minutes"
                                    className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                />
                            </div>

                            {/* Optional Distance Input */}
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Distance (optional)
                                </label>
                                <Input
                                    type="number"
                                    step="0.1"
                                    value={setData.actualDistance || ''}
                                    onChange={(e) => setSetData(prev => ({
                                        ...prev,
                                        actualDistance: parseFloat(e.target.value) || undefined
                                    }))}
                                    placeholder="Enter distance"
                                    className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Notes
                                </label>
                                <Input
                                    value={setData.notes || ''}
                                    onChange={(e) => setSetData(prev => ({...prev, notes: e.target.value}))}
                                    placeholder="How did the cardio session feel?"
                                    className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                />
                            </div>
                        </CardContent>
                    </Card>
                ) : isIsometric ? (
                    /* Isometric Tracking Interface */
                    <Card className={`${typeStyle.bg} ${typeStyle.border} bg-gray-800`}>
                        <CardHeader className="pb-3">
                            <CardTitle className={`${typeStyle.text} flex items-center gap-2`}>
                                <Timer className="w-5 h-5"/>
                                Isometric Hold
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {/* Timer Display */}
                            <div className="text-center py-6">
                                <div className="text-4xl font-bold text-white mb-2">
                                    {formatTime(timerSeconds)}
                                </div>
                                <div className="flex justify-center gap-2">
                                    <Button
                                        onClick={isTimerRunning ? stopTimer : startTimer}
                                        className={isTimerRunning ? "bg-red-600 hover:bg-red-700" : "bg-green-600 hover:bg-green-700"}
                                    >
                                        {isTimerRunning ? <Pause className="w-4 h-4 mr-2"/> :
                                            <Play className="w-4 h-4 mr-2"/>}
                                        {isTimerRunning ? 'Stop Hold' : 'Start Hold'}
                                    </Button>
                                    <Button variant="outline" onClick={resetTimer}
                                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white">
                                        <RotateCcw className="w-4 h-4 mr-2"/>
                                        Reset
                                    </Button>
                                </div>
                            </div>

                            {/* Hold Time Input */}
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Hold Time (seconds) *
                                </label>
                                <Input
                                    type="number"
                                    value={setData.actualHoldSeconds || timerSeconds || ''}
                                    onChange={(e) => setSetData(prev => ({
                                        ...prev,
                                        actualHoldSeconds: parseInt(e.target.value) || undefined
                                    }))}
                                    placeholder="Enter hold time in seconds"
                                    className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Notes
                                </label>
                                <Input
                                    value={setData.notes || ''}
                                    onChange={(e) => setSetData(prev => ({...prev, notes: e.target.value}))}
                                    placeholder="How challenging was this hold?"
                                    className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                />
                            </div>
                        </CardContent>
                    </Card>
                ) : (
                    /* Strength Training Interface */
                    <Card className={`${typeStyle.bg} ${typeStyle.border} bg-gray-800`}>
                        <CardHeader className="pb-3">
                            <CardTitle className={`${typeStyle.text} flex items-center gap-2`}>
                                <Weight className="w-5 h-5"/>
                                Strength Set {currentSet.setNumber}
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-1">
                                        Reps Completed *
                                    </label>
                                    <div className="flex items-center gap-2">
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setSetData(prev => ({
                                                ...prev,
                                                actualReps: Math.max(0, (prev.actualReps || 0) - 1)
                                            }))}
                                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                        >
                                            <Minus className="w-4 h-4"/>
                                        </Button>
                                        <Input
                                            type="number"
                                            value={setData.actualReps || ''}
                                            onChange={(e) => setSetData(prev => ({
                                                ...prev,
                                                actualReps: parseInt(e.target.value) || undefined
                                            }))}
                                            placeholder={`Target: ${currentSet.targetReps}`}
                                            className="text-center font-bold bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                        />
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setSetData(prev => ({
                                                ...prev,
                                                actualReps: (prev.actualReps || 0) + 1
                                            }))}
                                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                        >
                                            <Plus className="w-4 h-4"/>
                                        </Button>
                                    </div>
                                    <p className="text-xs text-gray-400 mt-1">Target: {currentSet.targetReps} reps</p>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-1">
                                        Weight ({currentSet.targetWeightUnit || 'lbs'})
                                    </label>
                                    <Input
                                        type="number"
                                        step="0.1"
                                        value={setData.actualWeight || ''}
                                        onChange={(e) => setSetData(prev => ({
                                            ...prev,
                                            actualWeight: parseFloat(e.target.value) || undefined
                                        }))}
                                        placeholder={currentSet.targetWeight ? `Target: ${currentSet.targetWeight}` : 'Enter weight'}
                                        className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-1">
                                        RPE (1-10)
                                    </label>
                                    <Input
                                        type="number"
                                        min="1"
                                        max="10"
                                        value={setData.actualRpe || ''}
                                        onChange={(e) => setSetData(prev => ({
                                            ...prev,
                                            actualRpe: parseInt(e.target.value) || undefined
                                        }))}
                                        placeholder={currentSet.targetRpe ? `Target: ${currentSet.targetRpe}` : 'Rate effort'}
                                        className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-1">
                                        Notes
                                    </label>
                                    <Input
                                        value={setData.notes || ''}
                                        onChange={(e) => setSetData(prev => ({...prev, notes: e.target.value}))}
                                        placeholder="Form notes, feeling, etc."
                                        className="w-full bg-gray-700 border-gray-600 text-white placeholder-gray-400"
                                    />
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                )}

                {/* Set Actions */}
                <Card className="bg-gray-800 border-gray-700">
                    <CardContent className="p-4">
                        <div className="flex gap-3">
                            <Button
                                onClick={handleCompleteSet}
                                disabled={currentSet.completed}
                                className="flex-1 bg-green-600 hover:bg-green-700 text-white"
                            >
                                <CheckCircle className="w-4 h-4 mr-2"/>
                                Complete Set
                            </Button>

                            <Button
                                onClick={handleSkipSet}
                                disabled={currentSet.completed}
                                variant="outline"
                                className="px-6 bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                            >
                                Skip
                            </Button>
                        </div>

                        {/* Set Management */}
                        <div className="flex justify-center gap-2 mt-4">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={handleAddSet}
                                className="text-blue-400 border-blue-600 bg-gray-800 hover:bg-gray-700"
                            >
                                <Plus className="w-4 h-4 mr-1"/>
                                Add Set
                            </Button>

                            {currentExercise.sets.length > 1 && (
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={handleRemoveSet}
                                    className="text-red-400 border-red-600 bg-gray-800 hover:bg-gray-700"
                                >
                                    <Minus className="w-4 h-4 mr-1"/>
                                    Remove Set
                                </Button>
                            )}
                        </div>
                    </CardContent>
                </Card>

                {/* Workout Controls */}
                <Card className="bg-gray-800 border-gray-700">
                    <CardContent className="p-4">
                        <div className="flex gap-3">
                            <Button
                                onClick={isPaused ? resumeWorkout : pauseWorkout}
                                variant="outline"
                                className="flex-1 bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                            >
                                {isPaused ? <Play className="w-4 h-4 mr-2"/> : <Pause className="w-4 h-4 mr-2"/>}
                                {isPaused ? 'Resume' : 'Pause'} Workout
                            </Button>

                            <Button
                                onClick={handleCompleteWorkout}
                                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white"
                            >
                                <Trophy className="w-4 h-4 mr-2"/>
                                Complete Workout
                            </Button>

                            <Button
                                onClick={handleCancelWorkout}
                                variant="outline"
                                className="px-6 text-red-400 border-red-600 bg-gray-800 hover:bg-gray-700"
                            >
                                Cancel
                            </Button>
                        </div>
                    </CardContent>
                </Card>

                {/* Exercise Overview */}
                <Card className="bg-gray-800 border-gray-700">
                    <CardHeader className="pb-3">
                        <CardTitle className="text-lg flex items-center gap-2 text-white">
                            <Activity className="w-5 h-5"/>
                            Exercise Overview
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {/* Exercise Details */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                            <div className="text-center p-3 bg-gray-700 rounded-lg">
                                <div className="font-bold text-white">{exercise.exerciseType}</div>
                                <div className="text-gray-400">Type</div>
                            </div>
                            <div className="text-center p-3 bg-gray-700 rounded-lg">
                                <div className="font-bold text-white">{exercise.difficultyLevel}</div>
                                <div className="text-gray-400">Difficulty</div>
                            </div>
                            <div className="text-center p-3 bg-gray-700 rounded-lg">
                                <div className="font-bold text-white">{getTotalDuration()} min</div>
                                <div className="text-gray-400">Duration</div>
                            </div>
                        </div>

                        {/* Set Progress */}
                        <div className="space-y-2">
                            <h4 className="font-medium text-white">Set Progress</h4>
                            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2">
                                {currentExercise.sets.map((set, index) => (
                                    <div
                                        key={set.id}
                                        className={`
                      p-3 rounded-lg text-center text-sm border-2 transition-all duration-200 cursor-pointer
                      ${index === currentWorkout.currentSetIndex
                                            ? 'border-blue-500 bg-blue-900/50 shadow-md scale-105'
                                            : set.completed
                                                ? 'border-green-600 bg-green-900/30'
                                                : 'border-gray-600 bg-gray-800 hover:border-gray-500'
                                        }
                    `}
                                    >
                                        <div className="font-bold text-white">Set {set.setNumber}</div>
                                        {set.completed ? (
                                            <div className="text-green-400 text-xs">
                                                {isCardio ? `${set.actualReps || 0}min` :
                                                    isIsometric ? `${set.actualReps || 0}s` :
                                                        `${set.actualReps || 0}/${set.targetReps}`}
                                            </div>
                                        ) : (
                                            <div className="text-gray-400 text-xs">
                                                {isCardio ? `${currentExercise.scheduledExercise.targetDurationMinutes || 20}min` :
                                                    isIsometric ? `${currentExercise.scheduledExercise.holdDurationSeconds || 30}s` :
                                                        `${set.targetReps} reps`}
                                            </div>
                                        )}
                                        {index === currentWorkout.currentSetIndex && (
                                            <div className="text-blue-400 text-xs font-medium mt-1">Current</div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Exercise Description */}
                        {exercise.description && (
                            <div className="p-3 bg-gray-700 rounded-lg">
                                <h4 className="font-medium text-white mb-2">Exercise Description</h4>
                                <p className="text-sm text-gray-300">{exercise.description}</p>
                            </div>
                        )}

                        {/* Exercise Tips */}
                        {exercise.tips && exercise.tips.length > 0 && (
                            <div className="p-3 bg-blue-900/20 border border-blue-700 rounded-lg">
                                <h4 className="font-medium text-blue-300 mb-2 flex items-center gap-2">
                                    <Zap className="w-4 h-4"/>
                                    Tips for Success
                                </h4>
                                <ul className="text-sm text-blue-200 space-y-1">
                                    {exercise.tips.slice(0, 3).map((tip, index) => (
                                        <li key={index} className="flex items-start gap-2">
                                            <span className="text-blue-400 mt-0.5">•</span>
                                            <span>{tip}</span>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}
                    </CardContent>
                </Card>

                {/* Workout Summary */}
                <Card className="bg-gradient-to-r from-gray-800 to-gray-700 border-gray-600">
                    <CardHeader className="pb-3">
                        <CardTitle className="text-lg text-white">Workout Summary</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                            <div>
                                <div className="text-2xl font-bold text-blue-400">
                                    {currentWorkout.exercises.filter(ex => ex.completed).length}
                                </div>
                                <div className="text-sm text-gray-400">Exercises Done</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-green-400">
                                    {currentWorkout.exercises.reduce((total, ex) =>
                                        total + ex.sets.filter(set => set.completed).length, 0
                                    )}
                                </div>
                                <div className="text-sm text-gray-400">Sets Completed</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-purple-400">
                                    {getTotalDuration()}
                                </div>
                                <div className="text-sm text-gray-400">Minutes Active</div>
                            </div>
                            <div>
                                <div className="text-2xl font-bold text-orange-400">
                                    {currentWorkout.exercises.reduce((total, ex) =>
                                        total + (ex.scheduledExercise.exercise.estimatedCalories || 0), 0
                                    )}
                                </div>
                                <div className="text-sm text-gray-400">Est. Calories</div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default WorkoutModePage;