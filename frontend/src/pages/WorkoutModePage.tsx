// src/pages/WorkoutModePage.tsx - Complete with Exercise Completion UX
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWorkout } from '../contexts/WorkoutContext';
import InWorkoutExerciseSelector from '../components/CalendarPage/InWorkoutExerciseSelector';
import {
    XMarkIcon,
    PlayIcon,
    PauseIcon,
    CheckIcon,
    ClockIcon,
    ChevronLeftIcon,
    ChevronRightIcon,
    PlusIcon,
    MinusIcon,
    ForwardIcon,
    BackwardIcon,
    FireIcon,
    BoltIcon,
    HeartIcon,
    EyeSlashIcon,
    HomeIcon,
    CalendarDaysIcon,
    InformationCircleIcon,
    SparklesIcon,
    TrophyIcon
} from '@heroicons/react/24/outline';

const WorkoutModePage: React.FC = () => {
    const navigate = useNavigate();
    const {
        currentWorkout,
        pauseWorkout,
        resumeWorkout,
        completeWorkout,
        cancelWorkout,
        goToNextExercise,
        goToPreviousExercise,
        goToExercise,
        completeSet,
        skipSet,
        addSet,
        removeSet,
        skipExercise,
        getCurrentExercise,
        getCurrentSet,
        getCompletionPercentage,
        getTotalDuration,
        isWorkoutActive,
        isPaused,
        canGoNext,
        canGoPrevious,
    } = useWorkout();

    // Local component state
    const [showCompleteDialog, setShowCompleteDialog] = useState(false);
    const [showCancelDialog, setShowCancelDialog] = useState(false);
    const [currentSetInputs, setCurrentSetInputs] = useState<{
        reps: string;
        weight: string;
        rpe: string;
    }>({
        reps: '',
        weight: '',
        rpe: '',
    });
    const [restTimer, setRestTimer] = useState<number>(0);
    const [isResting, setIsResting] = useState(false);
    const [showExerciseOverview, setShowExerciseOverview] = useState(false);
    const [showRpeInfo, setShowRpeInfo] = useState(false);
    const [showInWorkoutSelector, setShowInWorkoutSelector] = useState(false);

    // Auto-fill inputs when current set changes
    useEffect(() => {
        const currentSet = getCurrentSet();
        if (currentSet) {
            setCurrentSetInputs({
                reps: currentSet.actualReps?.toString() || '',
                weight: currentSet.actualWeight?.toString() || currentSet.targetWeight?.toString() || '',
                rpe: currentSet.actualRpe?.toString() || currentSet.targetRpe?.toString() || '',
            });
        }
    }, [getCurrentSet]);

    // Rest timer effect
    useEffect(() => {
        if (!isResting || restTimer <= 0) return;

        const interval = setInterval(() => {
            setRestTimer(prev => {
                if (prev <= 1) {
                    setIsResting(false);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(interval);
    }, [isResting, restTimer]);

    // Redirect if no active workout
    useEffect(() => {
        if (!isWorkoutActive) {
            navigate('/calendar');
        }
    }, [isWorkoutActive, navigate]);

    if (!currentWorkout) {
        return (
            <div className="min-h-screen bg-gray-900 flex items-center justify-center">
                <div className="text-center text-white">
                    <p className="text-lg mb-4">No active workout</p>
                    <button
                        onClick={() => navigate('/calendar')}
                        className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Back to Calendar
                    </button>
                </div>
            </div>
        );
    }

    const currentExercise = getCurrentExercise();
    const currentSet = getCurrentSet();
    const completionPercentage = getCompletionPercentage();
    const totalDuration = getTotalDuration();

    // NEW: Check if current exercise is completed
    const isCurrentExerciseCompleted = currentExercise && currentExercise.completed;

    // NEW: Check if there's a next exercise available
    const hasNextExercise = currentWorkout.currentExerciseIndex < currentWorkout.exercises.length - 1;

    // Check if workout is complete
    const isWorkoutComplete = currentWorkout.exercises.every(ex => ex.completed || ex.skipped);

    // Event handlers
    const handleCompleteSet = () => {
        if (!currentSet) return;

        const actualData = {
            actualReps: parseInt(currentSetInputs.reps) || 0,
            actualWeight: parseFloat(currentSetInputs.weight) || undefined,
            actualRpe: parseInt(currentSetInputs.rpe) || undefined,
        };

        completeSet(currentSet.id, actualData);

        // Start rest timer if configured
        if (currentSet.restSeconds && currentSet.restSeconds > 0) {
            setRestTimer(currentSet.restSeconds);
            setIsResting(true);
        }

        // Clear inputs for next set
        setCurrentSetInputs({ reps: '', weight: '', rpe: '' });
    };

    const handleSkipSet = () => {
        if (!currentSet) return;
        skipSet(currentSet.id);
        setCurrentSetInputs({ reps: '', weight: '', rpe: '' });
    };

    const handleWorkoutComplete = () => {
        completeWorkout();
        setShowCompleteDialog(false);
        navigate('/calendar', {
            state: {
                workoutCompleted: true,
                completionData: {
                    duration: totalDuration,
                    exerciseCount: currentWorkout.exercises.length,
                    date: currentWorkout.date
                }
            }
        });
    };

    const handleWorkoutCancel = () => {
        cancelWorkout();
        setShowCancelDialog(false);
        navigate('/calendar');
    };

    const handleAddExercise = () => {
        console.log('🎯 Opening InWorkout Exercise Selector');
        setShowInWorkoutSelector(true);
    };

    // NEW: Handler for proceeding to next exercise
    const handleProceedToNext = () => {
        if (hasNextExercise) {
            goToNextExercise();
        } else if (isWorkoutComplete) {
            setShowCompleteDialog(true);
        }
    };

    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const formatDuration = (minutes: number): string => {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        if (hours > 0) {
            return `${hours}h ${mins}m`;
        }
        return `${mins}m`;
    };

    const getRpeDescription = (rpe: number): string => {
        const descriptions: Record<number, string> = {
            1: 'Very easy',
            2: 'Easy',
            3: 'Moderate',
            4: 'Somewhat hard',
            5: 'Hard',
            6: 'Hard+',
            7: 'Very hard',
            8: 'Very hard+',
            9: 'Extremely hard',
            10: 'Maximum effort'
        };
        return descriptions[rpe] || 'Unknown';
    };

    const getRpeColorClass = (rpe: number): string => {
        if (rpe >= 1 && rpe <= 3) {
            return 'bg-green-900 bg-opacity-30 border border-green-600 text-green-300';
        } else if (rpe >= 4 && rpe <= 6) {
            return 'bg-yellow-900 bg-opacity-30 border border-yellow-600 text-yellow-300';
        } else if (rpe >= 7 && rpe <= 8) {
            return 'bg-orange-900 bg-opacity-30 border border-orange-600 text-orange-300';
        } else if (rpe >= 9 && rpe <= 10) {
            return 'bg-red-900 bg-opacity-30 border border-red-600 text-red-300';
        }
        return 'bg-gray-700 text-gray-300';
    };

    return (
        <div className="min-h-screen bg-gray-900 text-white relative overflow-hidden">
            {/* Header */}
            <div className="bg-gray-800 border-b border-gray-700">
                <div className="max-w-4xl mx-auto px-4 py-3 sm:py-4">
                    <div className="flex items-center justify-between">
                        {/* Left: Back/Cancel */}
                        <button
                            onClick={() => setShowCancelDialog(true)}
                            className="p-2 hover:bg-gray-700 rounded-lg transition-colors active:scale-95"
                        >
                            <XMarkIcon className="w-6 h-6" />
                        </button>

                        {/* Center: Progress */}
                        <div className="flex-1 mx-4">
                            <div className="text-center">
                                <p className="text-sm text-gray-400">Workout Progress</p>
                                <p className="text-lg font-bold">{completionPercentage}%</p>
                            </div>
                            <div className="w-full bg-gray-700 rounded-full h-2 mt-2">
                                <div
                                    className="bg-green-500 h-2 rounded-full transition-all duration-500"
                                    style={{ width: `${completionPercentage}%` }}
                                />
                            </div>
                        </div>

                        {/* Right: Timer and Controls */}
                        <div className="flex items-center space-x-3">
                            {/* Pause/Resume Button */}
                            <button
                                onClick={isPaused ? resumeWorkout : pauseWorkout}
                                className={`p-2 rounded-lg transition-all active:scale-95 ${
                                    isPaused
                                        ? 'bg-green-600 hover:bg-green-700 text-white'
                                        : 'bg-yellow-600 hover:bg-yellow-700 text-white'
                                }`}
                                title={isPaused ? "Resume Workout" : "Pause Workout"}
                            >
                                {isPaused ? (
                                    <PlayIcon className="w-5 h-5" />
                                ) : (
                                    <PauseIcon className="w-5 h-5" />
                                )}
                            </button>

                            {/* Complete Workout Button */}
                            {isWorkoutComplete && (
                                <button
                                    onClick={() => setShowCompleteDialog(true)}
                                    className="p-2 bg-green-600 hover:bg-green-700 rounded-lg transition-all active:scale-95 text-white"
                                    title="Complete Workout"
                                >
                                    <CheckIcon className="w-5 h-5" />
                                </button>
                            )}

                            {/* Timer Display */}
                            <div className="text-right">
                                <div className="flex items-center text-sm text-gray-400">
                                    <ClockIcon className="w-4 h-4 mr-1" />
                                    <span>{formatDuration(totalDuration)}</span>
                                </div>
                                {isPaused && (
                                    <span className="text-xs text-yellow-400">PAUSED</span>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Rest Timer Overlay */}
            {isResting && (
                <div className="absolute inset-0 bg-black bg-opacity-80 flex items-center justify-center z-50">
                    <div className="bg-gray-800 rounded-xl p-8 text-center max-w-sm mx-4">
                        <div className="text-6xl font-bold text-blue-400 mb-4">
                            {formatTime(restTimer)}
                        </div>
                        <p className="text-gray-300 mb-6">Rest Time</p>
                        <div className="space-y-3">
                            <button
                                onClick={() => setIsResting(false)}
                                className="w-full px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                            >
                                Skip Rest
                            </button>
                            <button
                                onClick={() => {
                                    setRestTimer(restTimer + 30);
                                }}
                                className="w-full px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
                            >
                                +30 seconds
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Main Content */}
            <div className="flex-1 overflow-y-auto pb-64">
                {/* Exercise Header */}
                {currentExercise && (
                    <div className="bg-gray-800 border-b border-gray-700">
                        <div className="max-w-4xl mx-auto px-4 py-4 sm:py-6">
                            <div className="flex items-center justify-between mb-4">
                                <div className="flex items-center">
                                    <span className="text-3xl mr-3">
                                        {currentExercise.scheduledExercise.exercise.emoji || '💪'}
                                    </span>
                                    <div>
                                        <h1 className="text-xl sm:text-2xl font-bold">
                                            {currentExercise.scheduledExercise.exercise.exerciseName ||
                                                currentExercise.scheduledExercise.exercise.name}
                                        </h1>
                                        <p className="text-gray-400 text-sm">
                                            Exercise {currentWorkout.currentExerciseIndex + 1} of {currentWorkout.exercises.length}
                                        </p>
                                    </div>
                                </div>

                                {/* Exercise Navigation */}
                                <div className="flex items-center space-x-2">
                                    <button
                                        onClick={goToPreviousExercise}
                                        disabled={!canGoPrevious}
                                        className={`p-2 rounded-lg transition-colors ${
                                            canGoPrevious
                                                ? 'hover:bg-gray-700 text-white'
                                                : 'text-gray-500 cursor-not-allowed'
                                        }`}
                                    >
                                        <ChevronLeftIcon className="w-6 h-6" />
                                    </button>
                                    <button
                                        onClick={goToNextExercise}
                                        disabled={!canGoNext}
                                        className={`p-2 rounded-lg transition-colors ${
                                            canGoNext
                                                ? 'hover:bg-gray-700 text-white'
                                                : 'text-gray-500 cursor-not-allowed'
                                        }`}
                                    >
                                        <ChevronRightIcon className="w-6 h-6" />
                                    </button>
                                </div>
                            </div>

                            {/* Exercise Info */}
                            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
                                <div className="bg-gray-700 rounded-lg p-3">
                                    <div className="text-2xl font-bold text-blue-400">
                                        {currentExercise.sets.length}
                                    </div>
                                    <div className="text-xs text-gray-300">Sets</div>
                                </div>
                                <div className="bg-gray-700 rounded-lg p-3">
                                    <div className="text-2xl font-bold text-green-400">
                                        {currentExercise.scheduledExercise.reps}
                                    </div>
                                    <div className="text-xs text-gray-300">Target Reps</div>
                                </div>
                                {currentExercise.scheduledExercise.weight && (
                                    <div className="bg-gray-700 rounded-lg p-3">
                                        <div className="text-2xl font-bold text-purple-400">
                                            {currentExercise.scheduledExercise.weight}
                                        </div>
                                        <div className="text-xs text-gray-300">Weight (lbs)</div>
                                    </div>
                                )}
                                {currentExercise.scheduledExercise.targetRpe && (
                                    <div className="bg-gray-700 rounded-lg p-3">
                                        <div className="text-2xl font-bold text-orange-400">
                                            {currentExercise.scheduledExercise.targetRpe}
                                        </div>
                                        <div className="text-xs text-gray-300">Target RPE</div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}

                {/* Sets List */}
                {currentExercise && (
                    <div className="max-w-4xl mx-auto px-4 py-6">
                        <h2 className="text-lg font-semibold mb-4 flex items-center">
                            <BoltIcon className="w-5 h-5 mr-2" />
                            Sets
                        </h2>

                        <div className="space-y-3">
                            {currentExercise.sets.map((set, index) => (
                                <div
                                    key={set.id}
                                    className={`border rounded-lg p-4 transition-all duration-200 ${
                                        index === currentWorkout.currentSetIndex
                                            ? 'border-blue-500 bg-blue-900 bg-opacity-20'
                                            : set.completed
                                                ? 'border-green-500 bg-green-900 bg-opacity-20'
                                                : 'border-gray-600 bg-gray-800'
                                    }`}
                                >
                                    <div className="flex items-center justify-between mb-3">
                                        <div className="flex items-center">
                                            <span className="text-lg font-bold mr-3">
                                                Set {set.setNumber}
                                            </span>
                                            {set.completed && (
                                                <CheckIcon className="w-5 h-5 text-green-400" />
                                            )}
                                            {index === currentWorkout.currentSetIndex && !set.completed && (
                                                <span className="px-2 py-1 bg-blue-600 text-white text-xs rounded-full">
                                                    Current
                                                </span>
                                            )}
                                        </div>

                                        {/* Set Controls */}
                                        {index === currentWorkout.currentSetIndex && !set.completed && (
                                            <div className="flex items-center space-x-2">
                                                <button
                                                    onClick={() => addSet(currentExercise.id)}
                                                    className="p-1 hover:bg-gray-700 rounded text-gray-400 hover:text-white transition-colors"
                                                    title="Add set"
                                                >
                                                    <PlusIcon className="w-4 h-4" />
                                                </button>
                                                {currentExercise.sets.length > 1 && (
                                                    <button
                                                        onClick={() => {
                                                            const lastSet = currentExercise.sets[currentExercise.sets.length - 1];
                                                            removeSet(lastSet.id);
                                                        }}
                                                        className="p-1 hover:bg-gray-700 rounded text-gray-400 hover:text-white transition-colors"
                                                        title="Remove last set"
                                                    >
                                                        <MinusIcon className="w-4 h-4" />
                                                    </button>
                                                )}
                                            </div>
                                        )}
                                    </div>

                                    {/* Set Data */}
                                    <div className="grid grid-cols-3 gap-4 text-sm">
                                        <div>
                                            <label className="block text-gray-400 mb-1">Target</label>
                                            <div className="text-white">
                                                {set.targetReps} reps
                                                {set.targetWeight && ` @ ${set.targetWeight}lbs`}
                                            </div>
                                        </div>
                                        <div>
                                            <label className="block text-gray-400 mb-1">Actual</label>
                                            <div className="text-white">
                                                {set.completed ? (
                                                    <>
                                                        {set.actualReps} reps
                                                        {set.actualWeight && ` @ ${set.actualWeight}lbs`}
                                                    </>
                                                ) : (
                                                    <span className="text-gray-500">-</span>
                                                )}
                                            </div>
                                        </div>
                                        <div>
                                            <label className="block text-gray-400 mb-1">RPE</label>
                                            <div className="text-white">
                                                {set.completed && set.actualRpe ? (
                                                    <span className="text-orange-400">{set.actualRpe}</span>
                                                ) : set.targetRpe ? (
                                                    <span className="text-gray-400">Target: {set.targetRpe}</span>
                                                ) : (
                                                    <span className="text-gray-500">-</span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Exercise Overview */}
                <div className="max-w-4xl mx-auto px-4 py-6">
                    <button
                        onClick={() => setShowExerciseOverview(!showExerciseOverview)}
                        className="w-full flex items-center justify-between mb-4 p-3 bg-gray-800 rounded-lg hover:bg-gray-700 transition-colors"
                    >
                        <h2 className="text-lg font-semibold flex items-center">
                            <FireIcon className="w-5 h-5 mr-2" />
                            All Exercises ({currentWorkout.exercises.length})
                        </h2>
                        <ChevronRightIcon
                            className={`w-5 h-5 transition-transform ${showExerciseOverview ? 'rotate-90' : ''}`}
                        />
                    </button>

                    {showExerciseOverview && (
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                            {currentWorkout.exercises.map((exercise, index) => (
                                <button
                                    key={exercise.id}
                                    onClick={() => goToExercise(index)}
                                    className={`text-left p-4 rounded-lg border transition-all duration-200 ${
                                        index === currentWorkout.currentExerciseIndex
                                            ? 'border-blue-500 bg-blue-900 bg-opacity-20'
                                            : exercise.completed
                                                ? 'border-green-500 bg-green-900 bg-opacity-20'
                                                : exercise.skipped
                                                    ? 'border-yellow-500 bg-yellow-900 bg-opacity-20'
                                                    : 'border-gray-600 bg-gray-800 hover:bg-gray-700'
                                    }`}
                                >
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center">
                                            <span className="text-lg mr-2">
                                                {exercise.scheduledExercise.exercise.emoji || '💪'}
                                            </span>
                                            <span className="font-medium truncate">
                                                {exercise.scheduledExercise.exercise.exerciseName ||
                                                    exercise.scheduledExercise.exercise.name}
                                            </span>
                                        </div>
                                        <div className="flex items-center space-x-1">
                                            {exercise.completed && (
                                                <CheckIcon className="w-4 h-4 text-green-400" />
                                            )}
                                            {exercise.skipped && (
                                                <EyeSlashIcon className="w-4 h-4 text-yellow-400" />
                                            )}
                                            {index === currentWorkout.currentExerciseIndex && (
                                                <span className="w-2 h-2 bg-blue-400 rounded-full"></span>
                                            )}
                                        </div>
                                    </div>
                                    <div className="text-sm text-gray-400">
                                        {exercise.sets.length} sets × {exercise.scheduledExercise.reps}
                                        {' • '}
                                        {exercise.sets.filter(set => set.completed).length}/{exercise.sets.length} completed
                                    </div>
                                </button>
                            ))}
                        </div>
                    )}

                    {/* Quick Add Exercise Button */}
                    <button
                        onClick={handleAddExercise}
                        className="w-full mt-4 p-4 border-2 border-dashed border-gray-600 text-gray-400 rounded-lg hover:border-blue-500 hover:text-blue-400 transition-colors flex items-center justify-center"
                    >
                        <PlusIcon className="w-5 h-5 mr-2" />
                        Add Exercise to Workout
                    </button>
                </div>
            </div>

            {/* NEW: Exercise Completion Panel */}
            {isCurrentExerciseCompleted && (
                <div className="fixed bottom-0 left-0 right-0 bg-gradient-to-r from-green-800 to-green-600 border-t border-green-500 p-4 safe-area-bottom z-40">
                    <div className="max-w-4xl mx-auto">
                        <div className="text-center mb-4">
                            <div className="flex items-center justify-center mb-2">
                                <SparklesIcon className="w-6 h-6 text-yellow-300 mr-2" />
                                <h3 className="text-xl font-bold text-white">Exercise Complete!</h3>
                                <SparklesIcon className="w-6 h-6 text-yellow-300 ml-2" />
                            </div>
                            <p className="text-green-100 text-sm">
                                Great work on {currentExercise?.scheduledExercise.exercise.exerciseName || currentExercise?.scheduledExercise.exercise.name}!
                            </p>
                        </div>

                        <div className="flex space-x-3">
                            {hasNextExercise ? (
                                <button
                                    onClick={handleProceedToNext}
                                    className="flex-1 px-6 py-4 bg-white text-green-700 rounded-lg font-bold hover:bg-green-50 transition-all active:scale-[0.98] flex items-center justify-center text-lg shadow-lg"
                                >
                                    <ForwardIcon className="w-5 h-5 mr-2" />
                                    Next Exercise
                                </button>
                            ) : (
                                <button
                                    onClick={() => setShowCompleteDialog(true)}
                                    className="flex-1 px-6 py-4 bg-yellow-500 text-yellow-900 rounded-lg font-bold hover:bg-yellow-400 transition-all active:scale-[0.98] flex items-center justify-center text-lg shadow-lg"
                                >
                                    <TrophyIcon className="w-5 h-5 mr-2" />
                                    Complete Workout
                                </button>
                            )}

                            <button
                                onClick={handleAddExercise}
                                className="px-4 py-4 bg-green-700 text-white rounded-lg font-medium hover:bg-green-800 transition-all active:scale-[0.98] flex items-center justify-center shadow-lg"
                                title="Add Another Exercise"
                            >
                                <PlusIcon className="w-5 h-5" />
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Current Set Input Panel */}
            {currentSet && !currentSet.completed && !isCurrentExerciseCompleted && (
                <div className="fixed bottom-0 left-0 right-0 bg-gray-800 border-t border-gray-700 p-4 safe-area-bottom z-40">
                    <div className="max-w-4xl mx-auto">
                        <div className="mb-4">
                            <h3 className="text-lg font-semibold mb-2">
                                Set {currentSet.setNumber} - Enter Your Results
                            </h3>
                        </div>

                        <div className="grid grid-cols-3 gap-4 mb-3">
                            {/* Reps Input */}
                            <div>
                                <label className="block text-xs text-gray-400 mb-1 h-4">Reps</label>
                                <input
                                    type="number"
                                    value={currentSetInputs.reps}
                                    onChange={(e) => setCurrentSetInputs(prev => ({ ...prev, reps: e.target.value }))}
                                    placeholder={currentSet.targetReps}
                                    className="w-full px-1 py-1 bg-gray-700 border border-gray-600 rounded text-white placeholder-gray-400 focus:border-blue-500 focus:outline-none text-center text-xs"
                                />
                            </div>

                            {/* Weight Input */}
                            <div>
                                <label className="block text-xs text-gray-400 mb-1 h-4">Weight</label>
                                <input
                                    type="number"
                                    step="0.5"
                                    value={currentSetInputs.weight}
                                    placeholder={currentSet.targetWeight?.toString() || '0'}
                                    className="w-full px-1 py-1 bg-gray-700 border border-gray-600 rounded text-white placeholder-gray-400 focus:border-blue-500 focus:outline-none text-center text-xs"
                                />
                            </div>

                            {/* RPE Input */}
                            <div>
                                <div className="flex items-center justify-between mb-1 h-4">
                                    <label className="block text-xs text-gray-400">RPE</label>
                                    <button
                                        onClick={() => setShowRpeInfo(!showRpeInfo)}
                                        className="bg-transparent border-none outline-none text-blue-400 hover:text-blue-300 transition-colors"
                                        title="What is RPE?"
                                    >
                                        <InformationCircleIcon className="w-3 h-3" />
                                    </button>
                                </div>
                                <input
                                    type="number"
                                    min="1"
                                    max="10"
                                    value={currentSetInputs.rpe}
                                    onChange={(e) => setCurrentSetInputs(prev => ({ ...prev, rpe: e.target.value }))}
                                    placeholder={currentSet.targetRpe?.toString() || '7'}
                                    className="w-full px-1 py-1 bg-gray-700 border border-gray-600 rounded text-white placeholder-gray-400 focus:border-blue-500 focus:outline-none text-center text-xs"
                                />
                            </div>
                        </div>

                        {/* RPE Info Tooltip */}
                        {showRpeInfo && (
                            <div className="mb-3 p-3 border border-blue-500 rounded-lg bg-gray-800">
                                <div className="flex items-start">
                                    <InformationCircleIcon className="w-4 h-4 text-blue-400 mr-2 mt-0.5 flex-shrink-0" />
                                    <div>
                                        <h4 className="text-sm font-medium text-blue-400 mb-1">Rate of Perceived Exertion (RPE)</h4>
                                        <p className="text-xs text-gray-300 mb-2">
                                            How hard did this set feel on a scale of 1-10?
                                        </p>
                                        <div className="grid grid-cols-2 gap-2 text-xs">
                                            <div className="space-y-1">
                                                <div><span className="text-green-400">1-3:</span> Easy</div>
                                                <div><span className="text-yellow-400">4-6:</span> Moderate</div>
                                                <div><span className="text-orange-400">7-8:</span> Hard</div>
                                                <div><span className="text-red-400">9-10:</span> Maximum</div>
                                            </div>
                                            <div className="text-gray-300">
                                                <div className="text-xs">• 7 = Could do 3 more reps</div>
                                                <div className="text-xs">• 8 = Could do 2 more reps</div>
                                                <div className="text-xs">• 10 = Couldn't do another rep</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Color-Coded RPE Description */}
                        {currentSetInputs.rpe && parseInt(currentSetInputs.rpe) >= 1 && parseInt(currentSetInputs.rpe) <= 10 && (
                            <div className={`text-xs mb-3 text-center rounded px-2 py-1 ${getRpeColorClass(parseInt(currentSetInputs.rpe))}`}>
                                Difficulty: {getRpeDescription(parseInt(currentSetInputs.rpe))}
                            </div>
                        )}

                        {/* Action Buttons */}
                        <div className="flex space-x-2">
                            <button
                                onClick={handleCompleteSet}
                                disabled={!currentSetInputs.reps}
                                className="flex-1 px-3 py-2.5 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors active:scale-[0.98] flex items-center justify-center text-sm"
                            >
                                <CheckIcon className="w-4 h-4 mr-1" />
                                Complete
                            </button>
                            <button
                                onClick={handleSkipSet}
                                className="px-3 py-2.5 bg-gray-600 text-white rounded-lg font-medium hover:bg-gray-700 transition-colors active:scale-[0.98] flex items-center justify-center text-sm"
                            >
                                <ForwardIcon className="w-4 h-4 mr-1" />
                                Skip
                            </button>

                            {/* Add Exercise Button */}
                            <button
                                onClick={handleAddExercise}
                                className="px-3 py-2.5 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors active:scale-[0.98] flex items-center justify-center text-sm"
                                title="Add Exercise"
                            >
                                <PlusIcon className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* InWorkout Exercise Selector */}
            <InWorkoutExerciseSelector
                open={showInWorkoutSelector}
                onClose={() => setShowInWorkoutSelector(false)}
            />

            {/* Workout Complete Floating Button */}
            {isWorkoutComplete && (!currentSet || currentSet.completed) && !isCurrentExerciseCompleted && (
                <div className="fixed bottom-6 right-6">
                    <button
                        onClick={() => setShowCompleteDialog(true)}
                        className="p-4 bg-green-600 hover:bg-green-700 rounded-full shadow-lg transition-all active:scale-95 text-white"
                        title="Complete Workout"
                    >
                        <CheckIcon className="w-6 h-6" />
                    </button>
                </div>
            )}

            {/* Complete Workout Dialog */}
            {showCompleteDialog && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-gray-800 rounded-xl max-w-md w-full p-6">
                        <div className="text-center mb-6">
                            <div className="text-6xl mb-4">🎉</div>
                            <h2 className="text-2xl font-bold mb-2">Workout Complete!</h2>
                            <p className="text-gray-400">
                                Great job! You completed your workout in {formatDuration(totalDuration)}.
                            </p>
                        </div>

                        <div className="grid grid-cols-2 gap-4 mb-6 text-center">
                            <div className="bg-gray-700 rounded-lg p-3">
                                <div className="text-2xl font-bold text-green-400">
                                    {currentWorkout.exercises.length}
                                </div>
                                <div className="text-xs text-gray-300">Exercises</div>
                            </div>
                            <div className="bg-gray-700 rounded-lg p-3">
                                <div className="text-2xl font-bold text-blue-400">
                                    {currentWorkout.exercises.reduce((total, ex) => total + ex.sets.length, 0)}
                                </div>
                                <div className="text-xs text-gray-300">Total Sets</div>
                            </div>
                        </div>

                        <div className="flex space-x-3">
                            <button
                                onClick={() => setShowCompleteDialog(false)}
                                className="flex-1 px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
                            >
                                Continue
                            </button>
                            <button
                                onClick={handleWorkoutComplete}
                                className="flex-1 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                            >
                                Finish
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Cancel Workout Dialog */}
            {showCancelDialog && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-gray-800 rounded-xl max-w-md w-full p-6">
                        <h2 className="text-xl font-bold mb-4">Cancel Workout?</h2>
                        <p className="text-gray-400 mb-6">
                            Are you sure you want to cancel this workout? Your progress will not be saved.
                        </p>
                        <div className="flex space-x-3">
                            <button
                                onClick={() => setShowCancelDialog(false)}
                                className="flex-1 px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
                            >
                                Keep Going
                            </button>
                            <button
                                onClick={handleWorkoutCancel}
                                className="flex-1 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                            >
                                Cancel Workout
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default WorkoutModePage;