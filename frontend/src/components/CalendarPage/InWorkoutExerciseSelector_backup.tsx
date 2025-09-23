// src/components/CalendarPage/InWorkoutExerciseSelector.tsx - Fixed
import React from 'react';
import {useWorkout} from '../../contexts/WorkoutContext';
import {useExerciseSelector} from '../../hooks/useExerciseSelector';
import {Exercise, formatTime} from '../../types/exercise';
import {
    XMarkIcon,
    MagnifyingGlassIcon,
    PlusIcon,
    StarIcon,
    ClockIcon,
    BoltIcon,
    FireIcon,
    HeartIcon,
    CheckIcon,
    ArrowRightIcon,
    BookmarkIcon,
    ExclamationTriangleIcon
} from '@heroicons/react/24/outline';

interface InWorkoutExerciseSelectorProps {
    open: boolean;
    onClose: () => void;
}

const InWorkoutExerciseSelector: React.FC<InWorkoutExerciseSelectorProps> = ({
                                                                                 open,
                                                                                 onClose
                                                                             }) => {
    const {addExerciseToCurrentWorkout, currentWorkout} = useWorkout();

    const selectorData = useExerciseSelector();

    // Handle exercise selection
    const handleExerciseSelect = (exercise: Exercise) => {
        selectorData.handleExerciseSelect(exercise);
    };

    // Handle adding exercise to workout
    const handleAddToWorkout = async () => {
        if (!selectorData.selectedExercise || !addExerciseToCurrentWorkout) return;

        selectorData.setAddingExercise(true);
        try {
            console.log('🏋️‍♂️ Adding exercise to current workout:', {
                exercise: selectorData.selectedExercise,
                config: selectorData.exerciseConfig
            });

            // Use your WorkoutContext function to add exercise
            await addExerciseToCurrentWorkout(selectorData.selectedExercise, selectorData.exerciseConfig);

            // Reset and close
            handleCloseConfig();
            onClose();

            console.log('✅ Exercise added to workout successfully');

        } catch (err) {
            console.error('❌ Failed to add exercise to workout:', err);
            selectorData.setError('Failed to add exercise. Please try again.');
        } finally {
            selectorData.setAddingExercise(false);
        }
    };

    // Handle closing configuration modal
    const handleCloseConfig = () => {
        selectorData.handleCloseConfig();
    };

    // Handle closing main modal
    const handleClose = () => {
        selectorData.setSearchTerm('');
        selectorData.setSelectedCategory('all');
        selectorData.setError(null);
        selectorData.handleCloseConfig();
        onClose();
    };

    // Get preset configurations based on exercise type
    const getPresetConfigs = () => {
        if (!selectorData.selectedExercise) return [];

        const presets = [];
        const exerciseType = selectorData.selectedExercise.exerciseType.toLowerCase();

        if (exerciseType === 'strength') {
            presets.push(
                {
                    name: 'Light',
                    targetSets: 3,
                    targetReps: 10,
                    restSeconds: 60,
                    targetRpe: 6
                },
                {
                    name: 'Moderate',
                    targetSets: 3,
                    targetReps: 8,
                    restSeconds: 90,
                    targetRpe: 7
                },
                {
                    name: 'Intense',
                    targetSets: 4,
                    targetReps: 6,
                    restSeconds: 120,
                    targetRpe: 8
                }
            );
        } else if (exerciseType === 'cardio') {
            presets.push(
                {
                    name: 'Short',
                    targetDurationMinutes: 10
                },
                {
                    name: 'Standard',
                    targetDurationMinutes: 20
                },
                {
                    name: 'Long',
                    targetDurationMinutes: 30
                }
            );
        } else if (exerciseType === 'flexibility') {
            presets.push(
                {
                    name: 'Quick',
                    targetSets: 2,
                    holdDurationSeconds: 30,
                    restSeconds: 15
                },
                {
                    name: 'Standard',
                    targetSets: 3,
                    holdDurationSeconds: 45,
                    restSeconds: 20
                }
            );
        }

        return presets;
    };

    // Don't render if not open
    if (!open) return null;

    // Configuration Modal
    if (selectorData.showConfig && selectorData.selectedExercise) {
        return (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] flex flex-col">
                    {/* Config Header */}
                    <div className="flex items-center justify-between p-4 border-b border-gray-200">
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900">Configure Exercise</h3>
                            <p className="text-sm text-gray-600">
                                {selectorData.selectedExercise.emoji} {selectorData.getExerciseName(selectorData.selectedExercise)}
                            </p>
                        </div>
                        <button
                            onClick={handleCloseConfig}
                            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                            disabled={selectorData.addingExercise}
                        >
                            <XMarkIcon className="w-5 h-5"/>
                        </button>
                    </div>

                    {/* Error Display */}
                    {selectorData.error && (
                        <div className="mx-4 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                            <div className="flex items-center">
                                <ExclamationTriangleIcon className="w-4 h-4 text-red-400 mr-2"/>
                                <span className="text-sm text-red-700">{selectorData.error}</span>
                                <button
                                    onClick={() => selectorData.setError(null)}
                                    className="ml-auto text-red-400 hover:text-red-600"
                                >
                                    <XMarkIcon className="w-4 h-4"/>
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Config Content */}
                    <div className="flex-1 overflow-y-auto p-4 space-y-4">
                        {/* Exercise Info */}
                        <div className="bg-gray-50 rounded-lg p-3">
                            <div className="flex items-center space-x-2 mb-2">
                                <span
                                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${selectorData.getExerciseTypeColor(selectorData.selectedExercise.exerciseType)}`}>
                                    {selectorData.selectedExercise.exerciseType}
                                </span>
                                <span
                                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${selectorData.getDifficultyColor(selectorData.selectedExercise.difficultyLevel)}`}>
                                    {selectorData.selectedExercise.difficultyLevel}
                                </span>
                                {selectorData.selectedExercise.createdByProfessional && (
                                    <span
                                        className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                                        <StarIcon className="w-3 h-3 mr-1"/>
                                        Pro
                                    </span>
                                )}
                            </div>
                            {selectorData.selectedExercise.description && (
                                <p className="text-sm text-gray-600 mb-2">{selectorData.selectedExercise.description}</p>
                            )}
                            {selectorData.selectedExercise.targetMuscleGroups && selectorData.selectedExercise.targetMuscleGroups.length > 0 && (
                                <div className="flex flex-wrap gap-1">
                                    {selectorData.selectedExercise.targetMuscleGroups.slice(0, 3).map((muscle, index) => (
                                        <span
                                            key={index}
                                            className="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-purple-100 text-purple-800"
                                        >
                                            {muscle}
                                        </span>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Quick Presets */}
                        {getPresetConfigs().length > 0 && (
                            <div>
                                <h4 className="text-sm font-medium text-gray-700 mb-2 flex items-center">
                                    <BoltIcon className="w-4 h-4 mr-1"/>
                                    Quick Presets
                                </h4>
                                <div className="grid grid-cols-3 gap-2">
                                    {getPresetConfigs().map((preset, index) => (
                                        <button
                                            key={index}
                                            onClick={() => {
                                                if (selectorData.exerciseConfig) {
                                                    if (selectorData.exerciseConfig.trackingMode === 'strength') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetSets: preset.targetSets || 3,
                                                            targetReps: preset.targetReps || 10,
                                                            restSeconds: preset.restSeconds || 90,
                                                            targetRpe: preset.targetRpe || 7
                                                        });
                                                    } else if (selectorData.exerciseConfig.trackingMode === 'cardio') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetDurationMinutes: preset.targetDurationMinutes || 20
                                                        });
                                                    } else if (selectorData.exerciseConfig.trackingMode === 'isometric') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetSets: preset.targetSets || 3,
                                                            holdDurationSeconds: preset.holdDurationSeconds || 30,
                                                            restSeconds: preset.restSeconds || 60
                                                        });
                                                    }
                                                }
                                            }}
                                            className="p-2 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 text-left transition-colors"
                                            disabled={selectorData.addingExercise}
                                        >
                                            <div className="text-sm font-medium">{preset.name}</div>
                                            <div className="text-xs text-gray-600">
                                                {preset.targetSets ? `${preset.targetSets} × ${preset.targetReps}` :
                                                    preset.targetDurationMinutes ? `${preset.targetDurationMinutes} min` :
                                                        preset.holdDurationSeconds ? `${preset.holdDurationSeconds}s hold` : ''}
                                            </div>
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Configuration Fields */}
                        <div className="space-y-3">
                            {/* Strength Configuration */}
                            {selectorData.exerciseConfig?.trackingMode === 'strength' && (
                                <>
                                    {/* Sets */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Sets
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="10"
                                            value={selectorData.exerciseConfig.targetSets || 1}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetSets: parseInt(e.target.value) || 1
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>

                                    {/* Reps */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Reps
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            value={selectorData.exerciseConfig.targetReps || 10}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetReps: parseInt(e.target.value) || 10
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>

                                    {/* Weight */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Weight (lbs) - Optional
                                        </label>
                                        <input
                                            type="number"
                                            step="0.5"
                                            value={selectorData.exerciseConfig.targetWeight || ''}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetWeight: parseFloat(e.target.value) || undefined
                                                    });
                                                }
                                            }}
                                            placeholder="Body weight, 135, etc."
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>
                                </>
                            )}

                            {/* Cardio Configuration */}
                            {selectorData.exerciseConfig?.trackingMode === 'cardio' && (
                                <>
                                    {/* Duration */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Duration (minutes)
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            value={selectorData.exerciseConfig.targetDurationMinutes || 20}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetDurationMinutes: parseInt(e.target.value) || 20
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>

                                    {/* Distance (Optional) */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Distance - Optional
                                        </label>
                                        <div className="flex space-x-2">
                                            <input
                                                type="number"
                                                step="0.1"
                                                value={selectorData.exerciseConfig.targetDistance || ''}
                                                onChange={(e) => {
                                                    if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetDistance: parseFloat(e.target.value) || undefined
                                                        });
                                                    }
                                                }}
                                                placeholder="e.g., 3.1"
                                                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                                disabled={selectorData.addingExercise}
                                            />
                                            <select
                                                value={selectorData.exerciseConfig.targetDistanceUnit}
                                                onChange={(e) => {
                                                    if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetDistanceUnit: e.target.value as 'km' | 'miles'
                                                        });
                                                    }
                                                }}
                                                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                                disabled={selectorData.addingExercise}
                                            >
                                                <option value="miles">miles</option>
                                                <option value="km">km</option>
                                            </select>
                                        </div>
                                    </div>
                                </>
                            )}

                            {/* Isometric Configuration */}
                            {selectorData.exerciseConfig?.trackingMode === 'isometric' && (
                                <>
                                    {/* Sets */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Sets
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="10"
                                            value={selectorData.exerciseConfig.targetSets || 1}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'isometric') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetSets: parseInt(e.target.value) || 1
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>

                                    {/* Hold Duration */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Hold Duration (seconds)
                                        </label>
                                        <input
                                            type="number"
                                            min="5"
                                            value={selectorData.exerciseConfig.holdDurationSeconds || 30}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'isometric') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        holdDurationSeconds: parseInt(e.target.value) || 30
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>
                                </>
                            )}

                            {/* Rest Time (for strength and isometric) */}
                            {(selectorData.exerciseConfig?.trackingMode === 'strength' || selectorData.exerciseConfig?.trackingMode === 'isometric') && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Rest Time: {formatTime(selectorData.exerciseConfig.restSeconds || 90)}
                                    </label>
                                    <input
                                        type="range"
                                        min="30"
                                        max="300"
                                        step="15"
                                        value={selectorData.exerciseConfig.restSeconds || 90}
                                        onChange={(e) => {
                                            if (selectorData.exerciseConfig) {
                                                selectorData.setExerciseConfig({
                                                    ...selectorData.exerciseConfig,
                                                    restSeconds: parseInt(e.target.value)
                                                });
                                            }
                                        }}
                                        className="w-full"
                                        disabled={selectorData.addingExercise}
                                    />
                                    <div className="flex justify-between text-xs text-gray-500 mt-1">
                                        <span>30s</span>
                                        <span>5min</span>
                                    </div>
                                </div>
                            )}

                            {/* Target RPE (for strength only) */}
                            {selectorData.exerciseConfig?.trackingMode === 'strength' && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Target RPE: {selectorData.exerciseConfig.targetRpe || 7}
                                    </label>
                                    <input
                                        type="range"
                                        min="1"
                                        max="10"
                                        value={selectorData.exerciseConfig.targetRpe || 7}
                                        onChange={(e) => {
                                            if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                selectorData.setExerciseConfig({
                                                    ...selectorData.exerciseConfig,
                                                    targetRpe: parseInt(e.target.value)
                                                });
                                            }
                                        }}
                                        className="w-full"
                                        disabled={selectorData.addingExercise}
                                    />
                                    <div className="flex justify-between text-xs text-gray-500 mt-1">
                                        <span>Easy</span>
                                        <span>Max</span>
                                    </div>
                                    <p className="text-xs text-gray-600 mt-1">
                                        {selectorData.getRpeDescription(selectorData.exerciseConfig.targetRpe || 7)}
                                    </p>
                                </div>
                            )}

                            {/* Notes (for all types) */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Notes - Optional
                                </label>
                                <textarea
                                    value={selectorData.exerciseConfig?.notes || ''}
                                    onChange={(e) => {
                                        if (selectorData.exerciseConfig) {
                                            selectorData.setExerciseConfig({
                                                ...selectorData.exerciseConfig,
                                                notes: e.target.value
                                            });
                                        }
                                    }}
                                    rows={2}
                                    placeholder="Form cues, modifications, or reminders..."
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                    disabled={selectorData.addingExercise}
                                />
                            </div>
                        </div>

                        {/* Estimated Time */}
                        <div className="bg-green-50 rounded-lg p-3">
                            <h4 className="text-sm font-medium text-green-800 mb-1 flex items-center">
                                <ClockIcon className="w-4 h-4 mr-1"/>
                                Estimated Time
                            </h4>
                            <div className="text-base font-semibold text-green-900">
                                {(() => {
                                    const exerciseTime = selectorData.selectedExercise.estimatedDurationMinutes || 2;
                                    let totalTime = exerciseTime;

                                    if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                        const restTime = ((selectorData.exerciseConfig.restSeconds || 90) * (selectorData.exerciseConfig.targetSets - 1)) / 60;
                                        totalTime = Math.ceil((exerciseTime * selectorData.exerciseConfig.targetSets) + restTime);
                                    } else if (selectorData.exerciseConfig?.trackingMode === 'isometric') {
                                        const restTime = ((selectorData.exerciseConfig.restSeconds || 60) * (selectorData.exerciseConfig.targetSets - 1)) / 60;
                                        totalTime = Math.ceil((exerciseTime * selectorData.exerciseConfig.targetSets) + restTime);
                                    } else if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                        totalTime = selectorData.exerciseConfig.targetDurationMinutes;
                                    }

                                    return `${totalTime} minutes`;
                                })()}
                            </div>
                            <p className="text-xs text-green-700">
                                Including rest periods between sets
                            </p>
                        </div>
                    </div>

                    {/* Config Footer */}
                    <div className="flex items-center justify-between p-4 border-t border-gray-200">
                        <button
                            onClick={handleCloseConfig}
                            className="px-4 py-2 text-gray-700 hover:text-gray-900 transition-colors"
                            disabled={selectorData.addingExercise}
                        >
                            Back
                        </button>
                        <button
                            onClick={handleAddToWorkout}
                            disabled={selectorData.addingExercise || !selectorData.exerciseConfig}
                            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center"
                        >
                            {selectorData.addingExercise ? (
                                <>
                                    <div
                                        className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                                    Adding...
                                </>
                            ) : (
                                <>
                                    <PlusIcon className="w-4 h-4 mr-2"/>
                                    Add to Workout
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    // Main Exercise Selection Modal
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-4 border-b border-gray-200">
                    <div>
                        <h2 className="text-lg font-semibold text-gray-900">Add Exercise</h2>
                        <p className="text-sm text-gray-600">
                            Add to current workout • {currentWorkout?.exercises.length || 0} exercises
                        </p>
                    </div>
                    <button
                        onClick={handleClose}
                        className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        <XMarkIcon className="w-5 h-5"/>
                    </button>
                </div>

                {/* Search */}
                <div className="p-4 border-b border-gray-200">
                    <div className="relative">
                        <MagnifyingGlassIcon
                            className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400"/>
                        <input
                            type="text"
                            placeholder="Search exercises..."
                            value={selectorData.searchTerm}
                            onChange={(e) => selectorData.setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                    </div>
                </div>

                {/* Error Display */}
                {selectorData.error && (
                    <div className="mx-4 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                        <div className="flex items-center">
                            <ExclamationTriangleIcon className="w-4 h-4 text-red-400 mr-2"/>
                            <span className="text-sm text-red-700 flex-1">{selectorData.error}</span>
                            <button
                                onClick={() => selectorData.setError(null)}
                                className="text-red-400 hover:text-red-600"
                            >
                                <XMarkIcon className="w-4 h-4"/>
                            </button>
                        </div>
                    </div>
                )}

                {/* Quick Categories */}
                <div className="p-4 border-b border-gray-200">
                    <div className="flex space-x-2 overflow-x-auto">
                        {selectorData.quickCategories.map((category) => (
                            <button
                                key={category.id}
                                onClick={() => selectorData.setSelectedCategory(category.id)}
                                className={`px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap transition-colors ${
                                    selectorData.selectedCategory === category.id
                                        ? 'bg-blue-600 text-white'
                                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                }`}
                            >
                                {category.emoji} {category.name}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Recent Exercises Section */}
                {!selectorData.searchTerm && selectorData.selectedCategory === 'all' && selectorData.recentExercises.length > 0 && (
                    <div className="p-4 border-b border-gray-200">
                        <h3 className="text-sm font-medium text-gray-700 mb-3 flex items-center">
                            <ClockIcon className="w-4 h-4 mr-1"/>
                            Recent Exercises
                        </h3>
                        <div className="space-y-2">
                            {selectorData.recentExercises.map((exercise) => (
                                <button
                                    key={`recent-${exercise.id}`}
                                    onClick={() => handleExerciseSelect(exercise)}
                                    className="w-full text-left p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                                >
                                    <div className="flex items-center">
                                        <span className="text-lg mr-3">{exercise.emoji}</span>
                                        <div className="flex-1 min-w-0">
                                            <div className="text-sm font-medium text-gray-900 truncate">
                                                {selectorData.getExerciseName(exercise)}
                                            </div>
                                            <div className="text-xs text-gray-600">
                                                {exercise.targetMuscleGroups?.slice(0, 2).join(', ') || exercise.exerciseType}
                                            </div>
                                        </div>
                                        <ArrowRightIcon className="w-4 h-4 text-gray-400"/>
                                    </div>
                                </button>
                            ))}
                        </div>
                    </div>
                )}

                {/* Exercise List */}
                <div className="flex-1 overflow-y-auto">
                    {selectorData.loading ? (
                        <div className="p-8 text-center">
                            <div
                                className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
                            <p className="text-gray-600">Loading exercises...</p>
                        </div>
                    ) : selectorData.filteredExercises.length > 0 ? (
                        <div className="p-4 space-y-3">
                            {selectorData.filteredExercises.map((exercise) => (
                                <button
                                    key={exercise.id}
                                    onClick={() => handleExerciseSelect(exercise)}
                                    className="w-full text-left p-3 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 transition-colors"
                                >
                                    <div className="flex items-start">
                                        <span className="text-xl mr-3 mt-0.5">{exercise.emoji || '🏋️‍♂️'}</span>
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center justify-between mb-1">
                                                <h3 className="text-sm font-medium text-gray-900 truncate">
                                                    {selectorData.getExerciseName(exercise)}
                                                </h3>
                                                <div className="flex items-center space-x-1">
                                                    {exercise.createdByProfessional && (
                                                        <StarIcon className="w-3 h-3 text-yellow-500 flex-shrink-0"/>
                                                    )}
                                                    {exercise.averageRating && (
                                                        <span className="text-xs text-gray-500">
                                                            {exercise.averageRating.toFixed(1)}
                                                        </span>
                                                    )}
                                                </div>
                                            </div>

                                            {exercise.description && (
                                                <p className="text-xs text-gray-600 mb-2 line-clamp-2">
                                                    {exercise.description.length > 80
                                                        ? `${exercise.description.substring(0, 80)}...`
                                                        : exercise.description
                                                    }
                                                </p>
                                            )}

                                            <div className="flex flex-wrap gap-1">
                                                <span
                                                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${selectorData.getExerciseTypeColor(exercise.exerciseType)}`}>
                                                    {exercise.exerciseType}
                                                </span>
                                                <span
                                                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${selectorData.getDifficultyColor(exercise.difficultyLevel)}`}>
                                                    {exercise.difficultyLevel}
                                                </span>
                                                {exercise.estimatedDurationMinutes && (
                                                    <span
                                                        className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                                                        <ClockIcon className="w-3 h-3 mr-1"/>
                                                        {exercise.estimatedDurationMinutes}min
                                                    </span>
                                                )}
                                                {exercise.averageRating && (
                                                    <span
                                                        className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                                                        <StarIcon className="w-3 h-3 mr-1"/>
                                                        {exercise.averageRating.toFixed(1)}
                                                    </span>
                                                )}
                                            </div>

                                            {/* Target Muscle Groups */}
                                            {exercise.targetMuscleGroups && exercise.targetMuscleGroups.length > 0 && (
                                                <div className="mt-2">
                                                    <div className="flex flex-wrap gap-1">
                                                        {exercise.targetMuscleGroups.slice(0, 3).map((muscle, index) => (
                                                            <span
                                                                key={index}
                                                                className="inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-purple-100 text-purple-700"
                                                            >
                                                                {muscle}
                                                            </span>
                                                        ))}
                                                        {exercise.targetMuscleGroups.length > 3 && (
                                                            <span className="text-xs text-gray-500">
                                                                +{exercise.targetMuscleGroups.length - 3} more
                                                            </span>
                                                        )}
                                                    </div>
                                                </div>
                                            )}

                                            {/* Equipment Required */}
                                            {exercise.equipmentRequired && exercise.equipmentRequired.length > 0 && (
                                                <div className="mt-1">
                                                    <span className="text-xs text-gray-500">
                                                        Equipment: {exercise.equipmentRequired.slice(0, 2).join(', ')}
                                                        {exercise.equipmentRequired.length > 2 && ` +${exercise.equipmentRequired.length - 2} more`}
                                                    </span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </button>
                            ))}
                        </div>
                    ) : (
                        <div className="p-8 text-center">
                            <div className="text-gray-400 text-4xl mb-4">
                                {selectorData.searchTerm ? '🔍' : '💪'}
                            </div>
                            <h3 className="text-lg font-medium text-gray-900 mb-2">
                                {selectorData.searchTerm ? 'No exercises found' : 'No exercises available'}
                            </h3>
                            <p className="text-gray-500">
                                {selectorData.searchTerm
                                    ? 'Try adjusting your search or category filter'
                                    : 'Loading exercises...'
                                }
                            </p>
                            {selectorData.searchTerm && (
                                <button
                                    onClick={() => selectorData.setSearchTerm('')}
                                    className="mt-3 px-4 py-2 text-blue-600 hover:text-blue-700 text-sm font-medium"
                                >
                                    Clear search
                                </button>
                            )}
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="p-4 border-t border-gray-200 bg-gray-50">
                    <div className="flex items-center justify-between">
                        <p className="text-xs text-gray-500">
                            💡 Tap any exercise to configure sets, reps, and intensity
                        </p>
                        <button
                            onClick={handleClose}
                            className="px-4 py-2 text-gray-700 hover:text-gray-900 text-sm font-medium transition-colors"
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default InWorkoutExerciseSelector;