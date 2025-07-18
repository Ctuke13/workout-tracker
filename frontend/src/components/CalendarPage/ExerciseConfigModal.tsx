// src/components/CalendarPage/ExerciseConfigModal.tsx - With Day Navigation
import React from 'react';
import {
    XMarkIcon,
    BookmarkIcon,
    ClockIcon,
    BoltIcon,
    HeartIcon,
    InformationCircleIcon,
    LightBulbIcon,
    PlayIcon,
    ChevronLeftIcon,
    ChevronRightIcon
} from '@heroicons/react/24/outline';

// Import calendar day type for navigation
import { CalendarDay } from '../../services/calendarMockData';

// Types
interface Exercise {
    id: number;
    exerciseName?: string;
    name?: string;
    emoji?: string;
    description?: string;
    exerciseType: string;
    difficultyLevel: string;
    estimatedDurationMinutes?: number;
    estimatedCalories?: number;
    targetMuscleGroups?: string[];
    equipmentRequired?: string[];
    benefits?: string[];
    tips?: string[];
    videoUrl?: string;
    averageRating?: number;
    totalRatings?: number;
    usageCount?: number;
}

interface ExerciseConfiguration {
    sets: number;
    reps: string;
    weight?: number;
    restSeconds?: number;
    tempo?: string;
    targetRpe?: number;
    notes?: string;
}

interface ExerciseConfigModalProps {
    open: boolean;
    onClose: () => void;
    exercise: Exercise | null;
    config: ExerciseConfiguration;
    onConfigChange: (config: ExerciseConfiguration) => void;
    onSave: () => void;
    selectedDate?: string | null;
    loading?: boolean;
    isEditing?: boolean;
    calendarDays?: CalendarDay[];
    onDateChange?: (date: string) => void;
}

const ExerciseConfigModal: React.FC<ExerciseConfigModalProps> = ({
                                                                     open,
                                                                     onClose,
                                                                     exercise,
                                                                     config,
                                                                     onConfigChange,
                                                                     onSave,
                                                                     selectedDate,
                                                                     loading = false,
                                                                     isEditing = false,
                                                                     calendarDays = [],
                                                                     onDateChange
                                                                 }) => {
    if (!exercise || !open) return null;

    const exerciseName = exercise.exerciseName || exercise.name || 'Unknown Exercise';

    // Day navigation functions
    const getCurrentDateIndex = () => {
        if (!selectedDate || calendarDays.length === 0) return -1;
        return calendarDays.findIndex(day => day.dateString === selectedDate);
    };

    const canNavigatePrevious = () => {
        const currentIndex = getCurrentDateIndex();
        if (currentIndex <= 0) return false;
        return true; // Allow navigation to any previous day in the week
    };

    const canNavigateNext = () => {
        const currentIndex = getCurrentDateIndex();
        if (currentIndex < 0 || currentIndex >= calendarDays.length - 1) return false;
        return true; // Allow navigation to any next day in the week
    };

    const navigateToPreviousDay = () => {
        if (!canNavigatePrevious() || !onDateChange) return;

        const currentIndex = getCurrentDateIndex();
        const previousDay = calendarDays[currentIndex - 1];
        if (previousDay) {
            onDateChange(previousDay.dateString);
        }
    };

    const navigateToNextDay = () => {
        if (!canNavigateNext() || !onDateChange) return;

        const currentIndex = getCurrentDateIndex();
        const nextDay = calendarDays[currentIndex + 1];
        if (nextDay) {
            onDateChange(nextDay.dateString);
        }
    };

    const getSelectedDateInfo = () => {
        if (!selectedDate) return null;

        const selectedDay = calendarDays.find(day => day.dateString === selectedDate);
        if (!selectedDay) return null;

        return {
            dayName: selectedDay.date.toLocaleDateString('en-US', { weekday: 'long' }),
            date: selectedDay.date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
            isToday: selectedDay.isToday,
            exerciseCount: selectedDay.exercises.length
        };
    };

    // Preset configurations based on exercise type and difficulty
    const getPresetConfigs = () => {
        const presets = [];

        // Strength training presets
        if (exercise.exerciseType?.toLowerCase() === 'strength') {
            if (exercise.difficultyLevel?.toLowerCase() === 'beginner') {
                presets.push({
                    name: 'Beginner Strength',
                    sets: 2,
                    reps: '8-10',
                    restSeconds: 60,
                    targetRpe: 6
                });
            } else if (exercise.difficultyLevel?.toLowerCase() === 'intermediate') {
                presets.push({
                    name: 'Intermediate Strength',
                    sets: 3,
                    reps: '8-12',
                    restSeconds: 90,
                    targetRpe: 7
                });
            } else {
                presets.push({
                    name: 'Advanced Strength',
                    sets: 4,
                    reps: '6-8',
                    restSeconds: 120,
                    targetRpe: 8
                });
            }
        }

        // Cardio presets
        if (exercise.exerciseType?.toLowerCase() === 'cardio') {
            presets.push({
                name: 'Cardio Session',
                sets: 1,
                reps: '20 minutes',
                restSeconds: 0,
                targetRpe: 6
            });
        }

        // Flexibility presets
        if (exercise.exerciseType?.toLowerCase() === 'flexibility') {
            presets.push({
                name: 'Flexibility Session',
                sets: 3,
                reps: '30 seconds',
                restSeconds: 15,
                targetRpe: 4
            });
        }

        return presets;
    };

    const handlePresetSelect = (preset: any) => {
        onConfigChange({
            ...config,
            sets: preset.sets,
            reps: preset.reps,
            restSeconds: preset.restSeconds,
            targetRpe: preset.targetRpe
        });
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            weekday: 'long',
            month: 'long',
            day: 'numeric'
        });
    };

    const getRpeDescription = (rpe: number) => {
        const descriptions: Record<number, string> = {
            1: 'Very easy - warm up pace',
            2: 'Easy - could do this all day',
            3: 'Moderate - comfortable effort',
            4: 'Somewhat hard - breathing harder',
            5: 'Hard - challenging but sustainable',
            6: 'Hard - difficult to maintain',
            7: 'Very hard - can speak a few words',
            8: 'Very hard - can barely speak',
            9: 'Extremely hard - maximal effort',
            10: 'Maximum - cannot continue'
        };
        return descriptions[rpe] || 'Unknown intensity';
    };

    const formatTime = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    };

    const getDifficultyColor = (difficulty: string) => {
        switch (difficulty.toLowerCase()) {
            case 'beginner': return 'bg-green-100 text-green-800';
            case 'intermediate': return 'bg-yellow-100 text-yellow-800';
            case 'advanced': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getExerciseTypeColor = (type: string) => {
        switch (type.toLowerCase()) {
            case 'strength': return 'bg-blue-100 text-blue-800';
            case 'cardio': return 'bg-red-100 text-red-800';
            case 'flexibility': return 'bg-green-100 text-green-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const dateInfo = getSelectedDateInfo();

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-2 sm:p-4">
            <div className="bg-white rounded-xl max-w-4xl w-full max-h-[95vh] sm:max-h-[90vh] flex flex-col">
                {/* Header with Day Navigation */}
                <div className="flex items-center justify-between p-3 sm:p-4 md:p-6 border-b border-gray-200">
                    <div className="flex-1 min-w-0">
                        <h2 className="text-lg sm:text-xl font-semibold text-gray-900">
                            {isEditing ? 'Edit Exercise' : 'Configure Exercise'}
                        </h2>
                        <p className="text-sm text-gray-600 mt-1">
                            {exercise.emoji} {exerciseName}
                        </p>

                        {/* Date Navigation with Arrows */}
                        {selectedDate && dateInfo && onDateChange && (
                            <div className="mt-2 flex items-center justify-center space-x-3">
                                <button
                                    onClick={navigateToPreviousDay}
                                    disabled={!canNavigatePrevious()}
                                    className={`p-2 rounded-full transition-all ${
                                        canNavigatePrevious()
                                            ? 'text-gray-700 hover:text-gray-900 hover:bg-gray-100 active:scale-95 border border-gray-200 hover:border-gray-300'
                                            : 'text-gray-300 cursor-not-allowed border border-gray-100'
                                    }`}
                                    title={canNavigatePrevious() ? "Previous day" : "First day of week"}
                                >
                                    <ChevronLeftIcon className="w-5 h-5" />
                                </button>

                                <div className="flex items-center space-x-2 min-w-0 px-3 py-2 bg-green-50 rounded-lg border border-green-200">
                                    <span className="text-sm font-semibold text-green-900 truncate">
                                        <span className="sm:hidden">{dateInfo.date}</span>
                                        <span className="hidden sm:inline">{dateInfo.dayName}, {dateInfo.date}</span>
                                    </span>
                                    {dateInfo.isToday && (
                                        <span className="px-2 py-0.5 bg-green-600 text-white text-xs font-medium rounded-full">
                                            Today
                                        </span>
                                    )}
                                    {dateInfo.exerciseCount > 0 && (
                                        <span className="px-2 py-0.5 bg-white text-green-700 text-xs font-medium rounded-full border border-green-200">
                                            {dateInfo.exerciseCount} exercises
                                        </span>
                                    )}
                                </div>

                                <button
                                    onClick={navigateToNextDay}
                                    disabled={!canNavigateNext()}
                                    className={`p-2 rounded-full transition-all ${
                                        canNavigateNext()
                                            ? 'text-gray-700 hover:text-gray-900 hover:bg-gray-100 active:scale-95 border border-gray-200 hover:border-gray-300'
                                            : 'text-gray-300 cursor-not-allowed border border-gray-100'
                                    }`}
                                    title={canNavigateNext() ? "Next day" : "Last day of week"}
                                >
                                    <ChevronRightIcon className="w-5 h-5" />
                                </button>
                            </div>
                        )}
                    </div>

                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-gray-100 rounded-lg transition-colors active:scale-95 ml-2"
                    >
                        <XMarkIcon className="w-5 h-5" />
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-3 sm:p-4 md:p-6">
                    {/* Exercise Info Card */}
                    <div className="bg-gray-50 rounded-lg p-3 sm:p-4 mb-4 sm:mb-6">
                        <div className="flex items-center mb-3">
                            <div className="w-8 h-8 sm:w-10 sm:h-10 bg-blue-600 rounded-full flex items-center justify-center text-white mr-3">
                                <span className="text-sm sm:text-base">{exercise.emoji || '🏋️‍♂️'}</span>
                            </div>
                            <div>
                                <h3 className="font-semibold text-gray-900 text-sm sm:text-base">{exerciseName}</h3>
                                <div className="flex gap-2 mt-1">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getDifficultyColor(exercise.difficultyLevel)}`}>
                                        {exercise.difficultyLevel}
                                    </span>
                                </div>
                            </div>
                        </div>
                        {exercise.description && (
                            <p className="text-xs sm:text-sm text-gray-600 mb-3">{exercise.description}</p>
                        )}
                        {exercise.targetMuscleGroups && exercise.targetMuscleGroups.length > 0 && (
                            <div className="flex flex-wrap gap-1">
                                {exercise.targetMuscleGroups.map((muscle, index) => (
                                    <span
                                        key={index}
                                        className="inline-flex items-center px-2 py-1 rounded-md text-xs font-medium bg-purple-100 text-purple-800"
                                    >
                                        {muscle}
                                    </span>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Quick Presets */}
                    {getPresetConfigs().length > 0 && (
                        <div className="mb-4 sm:mb-6">
                            <h4 className="text-sm font-medium text-gray-700 mb-3 flex items-center">
                                <BoltIcon className="w-4 h-4 mr-1" />
                                Quick Presets
                            </h4>
                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                                {getPresetConfigs().map((preset, index) => (
                                    <button
                                        key={index}
                                        onClick={() => handlePresetSelect(preset)}
                                        className="p-3 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition-colors text-left active:scale-[0.98]"
                                    >
                                        <div className="text-sm font-medium text-gray-900">{preset.name}</div>
                                        <div className="text-xs text-gray-600 mt-1">
                                            {preset.sets} sets × {preset.reps}
                                        </div>
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6">
                        {/* Basic Configuration */}
                        <div className="space-y-3 sm:space-y-4">
                            <h4 className="text-sm font-medium text-gray-700 flex items-center">
                                <BookmarkIcon className="w-4 h-4 mr-1" />
                                Basic Configuration
                            </h4>

                            {/* Sets */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Sets
                                </label>
                                <input
                                    type="number"
                                    min="1"
                                    max="10"
                                    value={config.sets}
                                    onChange={(e) => onConfigChange({
                                        ...config,
                                        sets: parseInt(e.target.value) || 1
                                    })}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm sm:text-base"
                                />
                            </div>

                            {/* Reps */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Reps/Duration
                                </label>
                                <input
                                    type="text"
                                    value={config.reps}
                                    onChange={(e) => onConfigChange({
                                        ...config,
                                        reps: e.target.value
                                    })}
                                    placeholder="e.g., 8-12, 30 seconds, 1 mile"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm sm:text-base"
                                />
                            </div>

                            {/* Weight */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Weight (lbs) - Optional
                                </label>
                                <input
                                    type="number"
                                    min="0"
                                    step="0.5"
                                    value={config.weight || ''}
                                    onChange={(e) => onConfigChange({
                                        ...config,
                                        weight: e.target.value ? parseFloat(e.target.value) : undefined
                                    })}
                                    placeholder="Body weight, 135, etc."
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm sm:text-base"
                                />
                            </div>
                        </div>

                        {/* Advanced Configuration */}
                        <div className="space-y-3 sm:space-y-4">
                            <h4 className="text-sm font-medium text-gray-700 flex items-center">
                                <LightBulbIcon className="w-4 h-4 mr-1" />
                                Advanced Options
                            </h4>

                            {/* Rest Time */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Rest Time: {config.restSeconds ? formatTime(config.restSeconds) : '0:00'}
                                </label>
                                <input
                                    type="range"
                                    min="0"
                                    max="300"
                                    step="15"
                                    value={config.restSeconds || 60}
                                    onChange={(e) => onConfigChange({
                                        ...config,
                                        restSeconds: parseInt(e.target.value)
                                    })}
                                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer slider"
                                />
                                <div className="flex justify-between text-xs text-gray-500 mt-1">
                                    <span>0s</span>
                                    <span>5min</span>
                                </div>
                            </div>

                            {/* RPE */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Target RPE: {config.targetRpe || 7}
                                </label>
                                <input
                                    type="range"
                                    min="1"
                                    max="10"
                                    value={config.targetRpe || 7}
                                    onChange={(e) => onConfigChange({
                                        ...config,
                                        targetRpe: parseInt(e.target.value)
                                    })}
                                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer slider"
                                />
                                <div className="flex justify-between text-xs text-gray-500 mt-1">
                                    <span>Easy</span>
                                    <span>Max</span>
                                </div>
                                <p className="text-xs text-gray-600 mt-1">
                                    {getRpeDescription(config.targetRpe || 7)}
                                </p>
                            </div>

                            {/* Tempo */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Tempo - Optional
                                </label>
                                <input
                                    type="text"
                                    value={config.tempo || ''}
                                    onChange={(e) => onConfigChange({
                                        ...config,
                                        tempo: e.target.value
                                    })}
                                    placeholder="e.g., 3-1-2-1"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm sm:text-base"
                                />
                                <p className="text-xs text-gray-500 mt-1">
                                    Format: eccentric-pause-concentric-pause
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Notes */}
                    <div className="mt-4 sm:mt-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Notes - Optional
                        </label>
                        <textarea
                            value={config.notes || ''}
                            onChange={(e) => onConfigChange({
                                ...config,
                                notes: e.target.value
                            })}
                            rows={3}
                            placeholder="Form cues, modifications, or personal reminders..."
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none text-sm sm:text-base"
                        />
                    </div>

                    {/* Exercise Tips */}
                    {exercise.tips && exercise.tips.length > 0 && (
                        <div className="mt-4 sm:mt-6">
                            <h4 className="text-sm font-medium text-gray-700 mb-3 flex items-center">
                                <InformationCircleIcon className="w-4 h-4 mr-1" />
                                Exercise Tips
                            </h4>
                            <div className="bg-blue-50 rounded-lg p-3 sm:p-4">
                                <ul className="space-y-2">
                                    {exercise.tips.slice(0, 3).map((tip, index) => (
                                        <li key={index} className="text-xs sm:text-sm text-blue-800 flex items-start">
                                            <span className="text-blue-600 mr-2">•</span>
                                            {tip}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    )}

                    {/* Estimated Workout Time */}
                    <div className="mt-4 sm:mt-6 bg-green-50 rounded-lg p-3 sm:p-4">
                        <h4 className="text-sm font-medium text-green-800 mb-2 flex items-center">
                            <ClockIcon className="w-4 h-4 mr-1" />
                            Estimated Workout Time
                        </h4>
                        <div className="text-base sm:text-lg font-semibold text-green-900">
                            {(() => {
                                const exerciseTime = exercise.estimatedDurationMinutes || 2;
                                const restTime = ((config.restSeconds || 60) * (config.sets - 1)) / 60;
                                const totalTime = Math.ceil((exerciseTime * config.sets) + restTime);
                                return `${totalTime} minutes`;
                            })()}
                        </div>
                        <p className="text-xs text-green-700 mt-1">
                            Including rest periods between sets
                        </p>
                    </div>
                </div>

                {/* Footer */}
                <div className="flex items-center justify-between p-3 sm:p-4 md:p-6 border-t border-gray-200 bg-gray-50">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-gray-700 hover:text-gray-900 transition-colors active:scale-95"
                        disabled={loading}
                    >
                        Cancel
                    </button>
                    <button
                        onClick={onSave}
                        disabled={loading}
                        className="px-4 sm:px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center active:scale-95"
                    >
                        {loading ? (
                            <>
                                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                                <span className="text-sm sm:text-base">Adding...</span>
                            </>
                        ) : (
                            <>
                                <PlayIcon className="w-4 h-4 mr-2" />
                                <span className="text-sm sm:text-base">
                                    {isEditing ? 'Update Exercise' : 'Add Exercise'}
                                </span>
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ExerciseConfigModal;