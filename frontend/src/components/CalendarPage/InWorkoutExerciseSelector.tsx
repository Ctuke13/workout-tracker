// src/components/CalendarPage/InWorkoutExerciseSelector.tsx
import React, { useState, useEffect, useCallback } from 'react';
import { useWorkout } from '../../contexts/WorkoutContext';
import { Exercise } from '../../types/exercise';
import { calendarMockApi } from '../../services/calendarMockData';
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


interface ExerciseConfiguration {
    sets: number;
    reps: string;
    weight?: string;
    restSeconds: number;
    targetRpe: number;
    notes: string;
}

interface InWorkoutExerciseSelectorProps {
    open: boolean;
    onClose: () => void;
}

const DEFAULT_CONFIG: ExerciseConfiguration = {
    sets: 3,
    reps: '10',
    weight: '',
    restSeconds: 90,
    targetRpe: 7,
    notes: ''
};

// Quick categories for filtering
const QUICK_CATEGORIES = [
    { id: 'all', name: 'All', emoji: '🎯' },
    { id: 'strength', name: 'Strength', emoji: '💪' },
    { id: 'cardio', name: 'Cardio', emoji: '❤️' },
    { id: 'flexibility', name: 'Flexibility', emoji: '🧘‍♀️' },
    { id: 'core', name: 'Core', emoji: '🔥' }
];

const InWorkoutExerciseSelector: React.FC<InWorkoutExerciseSelectorProps> = ({
                                                                                 open,
                                                                                 onClose
                                                                             }) => {
    const { addExerciseToCurrentWorkout, currentWorkout } = useWorkout();

    // State management
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [exercises, setExercises] = useState<Exercise[]>([]);
    const [filteredExercises, setFilteredExercises] = useState<Exercise[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Configuration modal state
    const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
    const [showConfig, setShowConfig] = useState(false);
    const [exerciseConfig, setExerciseConfig] = useState<ExerciseConfiguration>(DEFAULT_CONFIG);
    const [addingExercise, setAddingExercise] = useState(false);

    // Recent exercises (from workout history or context)
    const [recentExercises, setRecentExercises] = useState<Exercise[]>([]);

    // Fetch exercises using your real API
    const fetchExercises = useCallback(async (query: string = '') => {
        setLoading(true);
        setError(null);

        try {
            console.log('🔍 Fetching exercises for workout mode:', query);

            // Use your existing calendar mock API
            const results = await calendarMockApi.searchExercises(query, {
                exerciseType: selectedCategory !== 'all' ? selectedCategory : undefined
            });

            setExercises(results);
            console.log('✅ Exercises loaded for workout mode:', results.length);

        } catch (err) {
            console.error('❌ Failed to fetch exercises:', err);
            setError('Failed to load exercises. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [selectedCategory]);

    // Load recent exercises from workout history
    const loadRecentExercises = useCallback(async () => {
        try {
            // Get recent exercises from workout history stored in localStorage
            const history = JSON.parse(localStorage.getItem('workoutHistory') || '[]');
            const recentExerciseIds = new Set();
            const recent: Exercise[] = [];

            // Extract unique exercises from recent workouts
            history.slice(0, 3).forEach((workout: any) => {
                workout.exercises?.forEach((exercise: any) => {
                    const exerciseData = exercise.scheduledExercise?.exercise;
                    if (exerciseData && !recentExerciseIds.has(exerciseData.id)) {
                        recentExerciseIds.add(exerciseData.id);
                        recent.push(exerciseData);
                    }
                });
            });

            setRecentExercises(recent.slice(0, 3)); // Limit to 3 most recent
        } catch (error) {
            console.warn('Could not load recent exercises:', error);
        }
    }, []);

    // Filter exercises based on search and category
    useEffect(() => {
        let filtered = exercises;

        // Apply search filter
        if (searchTerm.trim()) {
            const searchLower = searchTerm.toLowerCase();
            filtered = filtered.filter(exercise => {
                const name = exercise.exerciseName || exercise.name || '';
                const description = exercise.description || '';
                const muscleGroups = exercise.targetMuscleGroups || [];

                return (
                    name.toLowerCase().includes(searchLower) ||
                    description.toLowerCase().includes(searchLower) ||
                    muscleGroups.some(muscle => muscle.toLowerCase().includes(searchLower)) ||
                    exercise.exerciseType.toLowerCase().includes(searchLower)
                );
            });
        }

        // Apply category filter
        if (selectedCategory !== 'all') {
            filtered = filtered.filter(exercise => {
                const exerciseType = exercise.exerciseType.toLowerCase();
                const muscleGroups = exercise.targetMuscleGroups?.map(m => m.toLowerCase()) || [];

                return (
                    exerciseType === selectedCategory ||
                    exerciseType.includes(selectedCategory) ||
                    muscleGroups.some(muscle => muscle.includes(selectedCategory))
                );
            });
        }

        setFilteredExercises(filtered);
    }, [searchTerm, selectedCategory, exercises]);

    // Load data when modal opens
    useEffect(() => {
        if (open) {
            fetchExercises();
            loadRecentExercises();
        }
    }, [open, fetchExercises, loadRecentExercises]);

    // Debounced search
    useEffect(() => {
        if (!open) return;

        const timeoutId = setTimeout(() => {
            fetchExercises(searchTerm);
        }, 300);

        return () => clearTimeout(timeoutId);
    }, [searchTerm, fetchExercises, open]);

    // Handle exercise selection
    const handleExerciseSelect = (exercise: Exercise) => {
        setSelectedExercise(exercise);
        setExerciseConfig(DEFAULT_CONFIG);
        setShowConfig(true);
    };

    // Handle adding exercise to workout
    const handleAddToWorkout = async () => {
        if (!selectedExercise || !addExerciseToCurrentWorkout) return;

        setAddingExercise(true);
        try {
            console.log('🏋️‍♂️ Adding exercise to current workout:', {
                exercise: selectedExercise,
                config: exerciseConfig
            });

            // Use your WorkoutContext function to add exercise
            await addExerciseToCurrentWorkout(selectedExercise, exerciseConfig);

            // Reset and close
            handleCloseConfig();
            onClose();

            console.log('✅ Exercise added to workout successfully');

        } catch (err) {
            console.error('❌ Failed to add exercise to workout:', err);
            setError('Failed to add exercise. Please try again.');
        } finally {
            setAddingExercise(false);
        }
    };

    // Handle closing configuration modal
    const handleCloseConfig = () => {
        setSelectedExercise(null);
        setShowConfig(false);
        setExerciseConfig(DEFAULT_CONFIG);
        setError(null);
    };

    // Handle closing main modal
    const handleClose = () => {
        setSearchTerm('');
        setSelectedCategory('all');
        setError(null);
        handleCloseConfig();
        onClose();
    };

    // Get preset configurations based on exercise type
    const getPresetConfigs = () => {
        if (!selectedExercise) return [];

        const presets = [];
        const exerciseType = selectedExercise.exerciseType.toLowerCase();
        const difficulty = selectedExercise.difficultyLevel.toLowerCase();

        if (exerciseType === 'strength') {
            presets.push(
                {
                    name: 'Light',
                    sets: 3,
                    reps: '10-12',
                    restSeconds: 60,
                    targetRpe: 6
                },
                {
                    name: 'Moderate',
                    sets: 3,
                    reps: '8-10',
                    restSeconds: 90,
                    targetRpe: 7
                },
                {
                    name: 'Intense',
                    sets: 4,
                    reps: '6-8',
                    restSeconds: 120,
                    targetRpe: 8
                }
            );
        } else if (exerciseType === 'cardio') {
            presets.push(
                {
                    name: 'Short',
                    sets: 1,
                    reps: '10 minutes',
                    restSeconds: 0,
                    targetRpe: 6
                },
                {
                    name: 'Standard',
                    sets: 1,
                    reps: '20 minutes',
                    restSeconds: 0,
                    targetRpe: 7
                },
                {
                    name: 'Long',
                    sets: 1,
                    reps: '30 minutes',
                    restSeconds: 0,
                    targetRpe: 6
                }
            );
        } else if (exerciseType === 'flexibility') {
            presets.push(
                {
                    name: 'Quick',
                    sets: 2,
                    reps: '30 seconds',
                    restSeconds: 15,
                    targetRpe: 4
                },
                {
                    name: 'Standard',
                    sets: 3,
                    reps: '45 seconds',
                    restSeconds: 20,
                    targetRpe: 5
                }
            );
        }

        return presets;
    };

    // Helper functions
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

    const formatTime = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    };

    const getRpeDescription = (rpe: number) => {
        const descriptions: Record<number, string> = {
            1: 'Very easy - warm up pace',
            2: 'Easy - could do this all day',
            3: 'Moderate - comfortable effort',
            4: 'Somewhat hard - breathing harder',
            5: 'Hard - challenging but sustainable',
            6: 'Hard+ - difficult to maintain',
            7: 'Very hard - can speak a few words',
            8: 'Very hard+ - can barely speak',
            9: 'Extremely hard - maximal effort',
            10: 'Maximum - cannot continue'
        };
        return descriptions[rpe] || 'Unknown intensity';
    };

    const getExerciseName = (exercise: Exercise) => {
        return exercise.exerciseName || exercise.name || 'Unknown Exercise';
    };

    // Don't render if not open
    if (!open) return null;

    // Configuration Modal
    if (showConfig && selectedExercise) {
        return (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] flex flex-col">
                    {/* Config Header */}
                    <div className="flex items-center justify-between p-4 border-b border-gray-200">
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900">Configure Exercise</h3>
                            <p className="text-sm text-gray-600">
                                {selectedExercise.emoji} {getExerciseName(selectedExercise)}
                            </p>
                        </div>
                        <button
                            onClick={handleCloseConfig}
                            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                            disabled={addingExercise}
                        >
                            <XMarkIcon className="w-5 h-5" />
                        </button>
                    </div>

                    {/* Error Display */}
                    {error && (
                        <div className="mx-4 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                            <div className="flex items-center">
                                <ExclamationTriangleIcon className="w-4 h-4 text-red-400 mr-2" />
                                <span className="text-sm text-red-700">{error}</span>
                                <button
                                    onClick={() => setError(null)}
                                    className="ml-auto text-red-400 hover:text-red-600"
                                >
                                    <XMarkIcon className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Config Content */}
                    <div className="flex-1 overflow-y-auto p-4 space-y-4">
                        {/* Exercise Info */}
                        <div className="bg-gray-50 rounded-lg p-3">
                            <div className="flex items-center space-x-2 mb-2">
                                <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getExerciseTypeColor(selectedExercise.exerciseType)}`}>
                                    {selectedExercise.exerciseType}
                                </span>
                                <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getDifficultyColor(selectedExercise.difficultyLevel)}`}>
                                    {selectedExercise.difficultyLevel}
                                </span>
                                {selectedExercise.createdByProfessional && (
                                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                                        <StarIcon className="w-3 h-3 mr-1" />
                                        Pro
                                    </span>
                                )}
                            </div>
                            {selectedExercise.description && (
                                <p className="text-sm text-gray-600 mb-2">{selectedExercise.description}</p>
                            )}
                            {selectedExercise.targetMuscleGroups && selectedExercise.targetMuscleGroups.length > 0 && (
                                <div className="flex flex-wrap gap-1">
                                    {selectedExercise.targetMuscleGroups.slice(0, 3).map((muscle, index) => (
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
                                    <BoltIcon className="w-4 h-4 mr-1" />
                                    Quick Presets
                                </h4>
                                <div className="grid grid-cols-3 gap-2">
                                    {getPresetConfigs().map((preset, index) => (
                                        <button
                                            key={index}
                                            onClick={() => setExerciseConfig({
                                                ...exerciseConfig,
                                                sets: preset.sets,
                                                reps: preset.reps,
                                                restSeconds: preset.restSeconds,
                                                targetRpe: preset.targetRpe
                                            })}
                                            className="p-2 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 text-left transition-colors"
                                            disabled={addingExercise}
                                        >
                                            <div className="text-sm font-medium">{preset.name}</div>
                                            <div className="text-xs text-gray-600">{preset.sets} × {preset.reps}</div>
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Configuration Fields */}
                        <div className="space-y-3">
                            {/* Sets */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Sets
                                </label>
                                <input
                                    type="number"
                                    min="1"
                                    max="10"
                                    value={exerciseConfig.sets}
                                    onChange={(e) => setExerciseConfig({
                                        ...exerciseConfig,
                                        sets: parseInt(e.target.value) || 1
                                    })}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    disabled={addingExercise}
                                />
                            </div>

                            {/* Reps */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Target Reps/Duration
                                </label>
                                <input
                                    type="text"
                                    value={exerciseConfig.reps}
                                    onChange={(e) => setExerciseConfig({
                                        ...exerciseConfig,
                                        reps: e.target.value
                                    })}
                                    placeholder="e.g., 8-12, 30 seconds, 1 mile"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    disabled={addingExercise}
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
                                    value={exerciseConfig.weight}
                                    onChange={(e) => setExerciseConfig({
                                        ...exerciseConfig,
                                        weight: e.target.value
                                    })}
                                    placeholder="Body weight, 135, etc."
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    disabled={addingExercise}
                                />
                            </div>

                            {/* Rest Time */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Rest Time: {formatTime(exerciseConfig.restSeconds)}
                                </label>
                                <input
                                    type="range"
                                    min="30"
                                    max="300"
                                    step="15"
                                    value={exerciseConfig.restSeconds}
                                    onChange={(e) => setExerciseConfig({
                                        ...exerciseConfig,
                                        restSeconds: parseInt(e.target.value)
                                    })}
                                    className="w-full"
                                    disabled={addingExercise}
                                />
                                <div className="flex justify-between text-xs text-gray-500 mt-1">
                                    <span>30s</span>
                                    <span>5min</span>
                                </div>
                            </div>

                            {/* Target RPE */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Target RPE: {exerciseConfig.targetRpe}
                                </label>
                                <input
                                    type="range"
                                    min="1"
                                    max="10"
                                    value={exerciseConfig.targetRpe}
                                    onChange={(e) => setExerciseConfig({
                                        ...exerciseConfig,
                                        targetRpe: parseInt(e.target.value)
                                    })}
                                    className="w-full"
                                    disabled={addingExercise}
                                />
                                <div className="flex justify-between text-xs text-gray-500 mt-1">
                                    <span>Easy</span>
                                    <span>Max</span>
                                </div>
                                <p className="text-xs text-gray-600 mt-1">
                                    {getRpeDescription(exerciseConfig.targetRpe)}
                                </p>
                            </div>

                            {/* Notes */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Notes - Optional
                                </label>
                                <textarea
                                    value={exerciseConfig.notes}
                                    onChange={(e) => setExerciseConfig({
                                        ...exerciseConfig,
                                        notes: e.target.value
                                    })}
                                    rows={2}
                                    placeholder="Form cues, modifications, or reminders..."
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                    disabled={addingExercise}
                                />
                            </div>
                        </div>

                        {/* Estimated Time */}
                        <div className="bg-green-50 rounded-lg p-3">
                            <h4 className="text-sm font-medium text-green-800 mb-1 flex items-center">
                                <ClockIcon className="w-4 h-4 mr-1" />
                                Estimated Time
                            </h4>
                            <div className="text-base font-semibold text-green-900">
                                {(() => {
                                    const exerciseTime = selectedExercise.estimatedDurationMinutes || 2;
                                    const restTime = ((exerciseConfig.restSeconds) * (exerciseConfig.sets - 1)) / 60;
                                    const totalTime = Math.ceil((exerciseTime * exerciseConfig.sets) + restTime);
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
                            disabled={addingExercise}
                        >
                            Back
                        </button>
                        <button
                            onClick={handleAddToWorkout}
                            disabled={addingExercise || !exerciseConfig.reps.trim()}
                            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center"
                        >
                            {addingExercise ? (
                                <>
                                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                                    Adding...
                                </>
                            ) : (
                                <>
                                    <PlusIcon className="w-4 h-4 mr-2" />
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
                        <XMarkIcon className="w-5 h-5" />
                    </button>
                </div>

                {/* Search */}
                <div className="p-4 border-b border-gray-200">
                    <div className="relative">
                        <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search exercises..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        />
                    </div>
                </div>

                {/* Error Display */}
                {error && (
                    <div className="mx-4 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                        <div className="flex items-center">
                            <ExclamationTriangleIcon className="w-4 h-4 text-red-400 mr-2" />
                            <span className="text-sm text-red-700 flex-1">{error}</span>
                            <button
                                onClick={() => setError(null)}
                                className="text-red-400 hover:text-red-600"
                            >
                                <XMarkIcon className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                )}

                {/* Quick Categories */}
                <div className="p-4 border-b border-gray-200">
                    <div className="flex space-x-2 overflow-x-auto">
                        {QUICK_CATEGORIES.map((category) => (
                            <button
                                key={category.id}
                                onClick={() => setSelectedCategory(category.id)}
                                className={`px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap transition-colors ${
                                    selectedCategory === category.id
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
                {!searchTerm && selectedCategory === 'all' && recentExercises.length > 0 && (
                    <div className="p-4 border-b border-gray-200">
                        <h3 className="text-sm font-medium text-gray-700 mb-3 flex items-center">
                            <ClockIcon className="w-4 h-4 mr-1" />
                            Recent Exercises
                        </h3>
                        <div className="space-y-2">
                            {recentExercises.map((exercise) => (
                                <button
                                    key={`recent-${exercise.id}`}
                                    onClick={() => handleExerciseSelect(exercise)}
                                    className="w-full text-left p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                                >
                                    <div className="flex items-center">
                                        <span className="text-lg mr-3">{exercise.emoji}</span>
                                        <div className="flex-1 min-w-0">
                                            <div className="text-sm font-medium text-gray-900 truncate">
                                                {getExerciseName(exercise)}
                                            </div>
                                            <div className="text-xs text-gray-600">
                                                {exercise.targetMuscleGroups?.slice(0, 2).join(', ') || exercise.exerciseType}
                                            </div>
                                        </div>
                                        <ArrowRightIcon className="w-4 h-4 text-gray-400" />
                                    </div>
                                </button>
                            ))}
                        </div>
                    </div>
                )}

                {/* Exercise List */}
                <div className="flex-1 overflow-y-auto">
                    {loading ? (
                        <div className="p-8 text-center">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
                            <p className="text-gray-600">Loading exercises...</p>
                        </div>
                    ) : filteredExercises.length > 0 ? (
                        <div className="p-4 space-y-3">
                            {filteredExercises.map((exercise) => (
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
                                                    {getExerciseName(exercise)}
                                                </h3>
                                                <div className="flex items-center space-x-1">
                                                    {exercise.createdByProfessional && (
                                                        <StarIcon className="w-3 h-3 text-yellow-500 flex-shrink-0" />
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
                                                <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getExerciseTypeColor(exercise.exerciseType)}`}>
                                                    {exercise.exerciseType}
                                                </span>
                                                <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${getDifficultyColor(exercise.difficultyLevel)}`}>
                                                    {exercise.difficultyLevel}
                                                </span>
                                                {exercise.estimatedDurationMinutes && (
                                                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                                                        <ClockIcon className="w-3 h-3 mr-1" />
                                                        {exercise.estimatedDurationMinutes}min
                                                    </span>
                                                )}
                                                {exercise.averageRating && (
                                                    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                                                        <StarIcon className="w-3 h-3 mr-1" />
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
                                {searchTerm ? '🔍' : '💪'}
                            </div>
                            <h3 className="text-lg font-medium text-gray-900 mb-2">
                                {searchTerm ? 'No exercises found' : 'No exercises available'}
                            </h3>
                            <p className="text-gray-500">
                                {searchTerm
                                    ? 'Try adjusting your search or category filter'
                                    : 'Loading exercises...'
                                }
                            </p>
                            {searchTerm && (
                                <button
                                    onClick={() => setSearchTerm('')}
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