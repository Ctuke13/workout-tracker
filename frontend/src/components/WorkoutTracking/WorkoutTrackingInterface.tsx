import React, { useState, useEffect } from 'react';
import {
    PlayIcon,
    PauseIcon,
    StopIcon,
    CheckIcon,
    ClockIcon,
    HeartIcon,
    EyeIcon, // Using EyeIcon instead of TargetIcon
    ArrowUpIcon,
    ArrowDownIcon
} from '@heroicons/react/24/outline';
import {
    CardioConfiguration,
    Exercise,
    ExerciseConfiguration,
    getWorkoutTrackingType,
    IsometricConfiguration,
    StrengthConfiguration,
    UnifiedWorkoutData
} from "../../types";

interface WorkoutTrackingInterfaceProps {
    exercise: Exercise;
    configuration: ExerciseConfiguration;
    onComplete: (workoutData: UnifiedWorkoutData) => void;
    onCancel: () => void;
}

export const WorkoutTrackingInterface: React.FC<WorkoutTrackingInterfaceProps> = ({
                                                                                      exercise,
                                                                                      configuration,
                                                                                      onComplete,
                                                                                      onCancel
                                                                                  }) => {
    const [isActive, setIsActive] = useState(false);
    const [isPaused, setIsPaused] = useState(false);
    const [startTime, setStartTime] = useState<Date | null>(null);
    const [currentTime, setCurrentTime] = useState(0); // seconds
    const [currentSet, setCurrentSet] = useState(1);
    const [workoutData, setWorkoutData] = useState<UnifiedWorkoutData | null>(null);

    const trackingMode = getWorkoutTrackingType(exercise);

    // Timer effect
    useEffect(() => {
        let interval: NodeJS.Timeout | null = null;

        if (isActive && !isPaused) {
            interval = setInterval(() => {
                setCurrentTime(prev => prev + 1);
            }, 1000);
        } else if (!isActive) {
            setCurrentTime(0);
        }

        return () => {
            if (interval) clearInterval(interval);
        };
    }, [isActive, isPaused]);

    // Start workout
    const handleStart = () => {
        const now = new Date();
        setStartTime(now);
        setIsActive(true);
        setIsPaused(false);

        // Initialize workout data based on tracking mode
        const baseData: UnifiedWorkoutData = {
            exerciseId: exercise.id,
            exercise,
            trackingMode,
            startTime: now,
            completed: false,
        };

        if (trackingMode === 'cardio') {
            baseData.cardioData = {
                exerciseId: exercise.id,
                configuration: configuration as CardioConfiguration,
                startTime: now,
                completed: false,
            };
        } else if (trackingMode === 'isometric') {
            const config = configuration as IsometricConfiguration;
            baseData.isometricData = {
                exerciseId: exercise.id,
                configuration: config,
                startTime: now,
                completedSets: [],
                completed: false,
            };
        } else {
            const config = configuration as StrengthConfiguration;
            baseData.strengthData = {
                exerciseId: exercise.id,
                configuration: config,
                startTime: now,
                completedSets: [],
                completed: false,
            };
        }

        setWorkoutData(baseData);
    };

    // Pause/Resume workout
    const handlePauseResume = () => {
        setIsPaused(!isPaused);
    };

    // Complete workout
    const handleComplete = () => {
        if (!workoutData || !startTime) return;

        const endTime = new Date();
        const completedWorkoutData: UnifiedWorkoutData = {
            ...workoutData,
            endTime,
            completed: true,
        };

        // Update mode-specific data
        if (trackingMode === 'cardio' && completedWorkoutData.cardioData) {
            completedWorkoutData.cardioData.endTime = endTime;
            completedWorkoutData.cardioData.actualDurationMinutes = Math.round(currentTime / 60);
            completedWorkoutData.cardioData.completed = true;
        } else if (trackingMode === 'isometric' && completedWorkoutData.isometricData) {
            completedWorkoutData.isometricData.endTime = endTime;
            completedWorkoutData.isometricData.completed = true;
        } else if (trackingMode === 'strength' && completedWorkoutData.strengthData) {
            completedWorkoutData.strengthData.endTime = endTime;
            completedWorkoutData.strengthData.completed = true;
        }

        onComplete(completedWorkoutData);
    };

    // Format time display
    const formatTime = (seconds: number): string => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
    };

    // Render cardio tracking interface
    const renderCardioInterface = () => {
        const config = configuration as CardioConfiguration;
        const targetSeconds = config.targetDurationMinutes * 60;
        const progress = Math.min(currentTime / targetSeconds, 1) * 100;

        return (
            <div className="space-y-6">
                {/* Timer Display */}
                <div className="text-center">
                    <div className="text-6xl font-mono font-bold text-red-600 mb-2">
                        {formatTime(currentTime)}
                    </div>
                    <div className="text-lg text-gray-600">
                        Target: {config.targetDurationMinutes} minutes
                    </div>

                    {/* Progress Bar */}
                    <div className="w-full bg-gray-200 rounded-full h-4 mt-4">
                        <div
                            className="bg-red-500 h-4 rounded-full transition-all duration-1000"
                            style={{ width: `${progress}%` }}
                        />
                    </div>
                    <div className="text-sm text-gray-500 mt-1">
                        {Math.round(progress)}% complete
                    </div>
                </div>

                {/* Distance Tracking (if configured) */}
                {config.targetDistanceKm && (
                    <div className="bg-red-50 p-4 rounded-lg">
                        <div className="flex items-center justify-between">
                            <span className="text-red-800 font-medium">Target Distance:</span>
                            <span className="text-red-900 font-bold">{config.targetDistanceKm} km</span>
                        </div>
                        {config.targetPace && (
                            <div className="flex items-center justify-between mt-2">
                                <span className="text-red-700">Target Pace:</span>
                                <span className="text-red-800">{config.targetPace.toFixed(1)} min/km</span>
                            </div>
                        )}
                    </div>
                )}

                {/* Heart Rate Zone (placeholder) */}
                <div className="bg-orange-50 p-4 rounded-lg">
                    <div className="flex items-center gap-2 mb-2">
                        <HeartIcon className="w-5 h-5 text-orange-600" />
                        <span className="font-medium text-orange-800">Heart Rate Zone</span>
                    </div>
                    <div className="text-sm text-orange-700">
                        Stay in your target heart rate zone for optimal cardio benefits
                    </div>
                </div>
            </div>
        );
    };

    // Render isometric tracking interface
    const renderIsometricInterface = () => {
        const config = configuration as IsometricConfiguration;
        const isInHold = isActive && !isPaused;
        const holdProgress = Math.min(currentTime / config.holdDurationSeconds, 1) * 100;

        return (
            <div className="space-y-6">
                {/* Current Set Display */}
                <div className="text-center">
                    <div className="text-2xl font-bold text-purple-600 mb-2">
                        Set {currentSet} of {config.targetSets || 3} {/* ✅ FIXED: Use targetSets */}
                    </div>

                    {/* Hold Timer */}
                    <div className="text-5xl font-mono font-bold text-purple-700 mb-2">
                        {formatTime(currentTime)}
                    </div>

                    <div className="text-lg text-gray-600">
                        Target Hold: {config.holdDurationSeconds}s
                    </div>

                    {/* Hold Progress Circle */}
                    <div className="relative w-48 h-48 mx-auto mt-6">
                        <svg className="w-48 h-48 transform -rotate-90">
                            <circle
                                cx="96"
                                cy="96"
                                r="88"
                                stroke="rgb(221, 214, 254)"
                                strokeWidth="8"
                                fill="transparent"
                            />
                            <circle
                                cx="96"
                                cy="96"
                                r="88"
                                stroke="rgb(147, 51, 234)"
                                strokeWidth="8"
                                fill="transparent"
                                strokeDasharray={553.36} // 2 * π * 88
                                strokeDashoffset={553.36 * (1 - holdProgress / 100)}
                                strokeLinecap="round"
                                className="transition-all duration-1000"
                            />
                        </svg>
                        <div className="absolute inset-0 flex items-center justify-center">
                            <div className="text-center">
                                <div className="text-2xl font-bold text-purple-700">
                                    {Math.round(holdProgress)}%
                                </div>
                                <div className="text-sm text-purple-600">Hold</div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Set Information */}
                <div className="bg-purple-50 p-4 rounded-lg">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <div className="text-purple-800 font-medium">Hold Duration</div>
                            <div className="text-purple-900 font-bold">{config.holdDurationSeconds}s</div>
                        </div>
                        <div>
                            <div className="text-purple-800 font-medium">Rest Time</div>
                            <div className="text-purple-900 font-bold">{config.restSeconds || 60}s</div>
                        </div>
                    </div>
                </div>

                {/* Form Cues */}
                <div className="bg-yellow-50 p-4 rounded-lg">
                    <div className="flex items-center gap-2 mb-2">
                        <EyeIcon className="w-5 h-5 text-yellow-600" />
                        <span className="font-medium text-yellow-800">Focus Points</span>
                    </div>
                    <ul className="text-sm text-yellow-700 space-y-1">
                        <li>• Maintain proper form throughout the hold</li>
                        <li>• Breathe steadily and controlled</li>
                        <li>• Engage target muscles consistently</li>
                        <li>• Stop if you feel pain or excessive strain</li>
                    </ul>
                </div>
            </div>
        );
    };

    // Render strength tracking interface
    const renderStrengthInterface = () => {
        const config = configuration as StrengthConfiguration;

        return (
            <div className="space-y-6">
                {/* Current Set Display */}
                <div className="text-center">
                    <div className="text-2xl font-bold text-blue-600 mb-4">
                        Set {currentSet} of {config.targetSets || 3} {/* ✅ FIXED: Use targetSets */}
                    </div>

                    {/* Rep Counter */}
                    <div className="bg-blue-50 p-6 rounded-lg">
                        <div className="text-lg text-blue-800 mb-2">Target Reps</div>
                        <div className="text-4xl font-bold text-blue-900">{config.targetReps || 10}</div> {/* ✅ FIXED: Use targetReps */}

                        {config.targetWeight && ( /* ✅ FIXED: Use targetWeight */
                            <div className="mt-4">
                                <div className="text-lg text-blue-800">Weight</div>
                                <div className="text-2xl font-bold text-blue-900">
                                    {config.targetWeight} {config.targetWeightUnit || 'lbs'} {/* ✅ FIXED: Use targetWeight and targetWeightUnit */}
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Set Configuration */}
                <div className="bg-blue-50 p-4 rounded-lg">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <div className="text-blue-800 font-medium">Rest Time</div>
                            <div className="text-blue-900 font-bold">
                                {Math.floor((config.restSeconds || 60) / 60)}:{((config.restSeconds || 60) % 60).toString().padStart(2, '0')}
                            </div>
                        </div>
                        <div>
                            <div className="text-blue-800 font-medium">Target RPE</div>
                            <div className="text-blue-900 font-bold">{config.targetRpe || 7}/10</div>
                        </div>
                    </div>
                </div>

                {/* Rest Timer (when between sets) */}
                {currentSet > 1 && (
                    <div className="bg-orange-50 p-4 rounded-lg">
                        <div className="text-center">
                            <div className="text-orange-800 font-medium mb-2">Rest Timer</div>
                            <div className="text-3xl font-mono font-bold text-orange-600">
                                {formatTime(Math.max(0, (config.restSeconds || 60) - currentTime))}
                            </div>
                        </div>
                    </div>
                )}

                {/* Form Tips */}
                <div className="bg-green-50 p-4 rounded-lg">
                    <div className="flex items-center gap-2 mb-2">
                        <CheckIcon className="w-5 h-5 text-green-600" />
                        <span className="font-medium text-green-800">Form Reminders</span>
                    </div>
                    <ul className="text-sm text-green-700 space-y-1">
                        <li>• Control both lifting and lowering phases</li>
                        <li>• Maintain proper breathing pattern</li>
                        <li>• Focus on target muscle engagement</li>
                        <li>• Use full range of motion</li>
                    </ul>
                </div>
            </div>
        );
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-4 border-b border-gray-200">
                    <div>
                        <h2 className="text-lg font-semibold text-gray-900">
                            {exercise.emoji} {exercise.name}
                        </h2>
                        <p className="text-sm text-gray-600">
                            {trackingMode === 'cardio' ? '❤️ Cardio Workout' :
                                trackingMode === 'isometric' ? '🛡️ Isometric Hold' :
                                    '💪 Strength Training'}
                        </p>
                    </div>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-4">
                    {trackingMode === 'cardio' && renderCardioInterface()}
                    {trackingMode === 'isometric' && renderIsometricInterface()}
                    {trackingMode === 'strength' && renderStrengthInterface()}
                </div>

                {/* Controls */}
                <div className="p-4 border-t border-gray-200 bg-gray-50">
                    <div className="flex items-center justify-center gap-4">
                        {!isActive ? (
                            <button
                                onClick={handleStart}
                                className="flex items-center gap-2 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                            >
                                <PlayIcon className="w-5 h-5" />
                                Start Workout
                            </button>
                        ) : (
                            <>
                                <button
                                    onClick={handlePauseResume}
                                    className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-colors ${
                                        isPaused
                                            ? 'bg-green-600 text-white hover:bg-green-700'
                                            : 'bg-yellow-600 text-white hover:bg-yellow-700'
                                    }`}
                                >
                                    {isPaused ? <PlayIcon className="w-4 h-4" /> : <PauseIcon className="w-4 h-4" />}
                                    {isPaused ? 'Resume' : 'Pause'}
                                </button>

                                <button
                                    onClick={handleComplete}
                                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                                >
                                    <CheckIcon className="w-4 h-4" />
                                    Complete
                                </button>
                            </>
                        )}

                        <button
                            onClick={onCancel}
                            className="flex items-center gap-2 px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
                        >
                            <StopIcon className="w-4 h-4" />
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};