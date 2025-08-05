// src/hooks/useExerciseFilters.ts - Fixed with complete interface
import { useState, useMemo } from 'react';
import { Exercise, ExerciseFilters, SortOption, FilterType, ActiveFilter, Goal, ExerciseTypeOption, WorkoutTrackingType } from '../types/exercise';
import { formatEquipmentName } from '../utils/exerciseFormatters';

export const useExerciseFilters = (
    exercises: Exercise[],
    goals: Goal[],
    exerciseTypeOptions: ExerciseTypeOption[]
) => {
    // Enhanced filter state with workout tracking mode support and ALL required fields
    const [filters, setFilters] = useState<ExerciseFilters>({
        // Core filters
        activeGoal: 'all',
        searchTerm: '',
        selectedEquipment: 'all',
        selectedDifficulty: 'all',
        selectedExerciseType: 'all',

        // Range filters
        minRating: 0,
        maxDuration: 480,

        // Sort and display options
        sortBy: 'relevance',
        showProfessionalOnly: false,

        // ✅ NEW: Required fields that were missing
        onlyFavorites: false,
        includeCompleted: true,
        muscleGroups: [],
        availableEquipment: [],
        fitnessLevel: 'all',

        // Enhanced tracking mode filter
        trackingType: 'all',

        // Optional backward compatibility fields
        exerciseType: undefined,
        difficulty: undefined,
        equipment: undefined,
        professionalOnly: undefined
    });

    // Enhanced filtered and sorted exercises with workout tracking mode support
    const filteredAndSortedExercises = useMemo(() => {
        let filtered = exercises.filter((exercise: Exercise) => {
            // Enhanced search filter - includes exercise type and tracking mode keywords
            const searchTerm = filters.searchTerm.toLowerCase();
            const matchesSearch = filters.searchTerm === '' ||
                exercise.name.toLowerCase().includes(searchTerm) ||
                exercise.description.toLowerCase().includes(searchTerm) ||
                exercise.benefits.some((benefit: string) => benefit.toLowerCase().includes(searchTerm)) ||
                exercise.exerciseTypeDisplay.toLowerCase().includes(searchTerm) ||
                exercise.targetMuscleGroups.some((muscle: string) => muscle.toLowerCase().includes(searchTerm)) ||
                // Enhanced tracking mode search terms
                (exercise.isCardio && ['cardio', 'time', 'distance', 'running', 'cycling', 'heart rate'].some(term => term.includes(searchTerm))) ||
                (exercise.isIsometric && ['isometric', 'hold', 'static', 'plank', 'wall sit', 'pose'].some(term => term.includes(searchTerm))) ||
                (!exercise.isCardio && !exercise.isIsometric && ['strength', 'reps', 'sets', 'weight', 'lifting', 'resistance'].some(term => term.includes(searchTerm)));

            // Equipment filter with proper formatting
            const matchesEquipment = filters.selectedEquipment === 'all' ||
                exercise.equipmentRequired.some((eq: string) => formatEquipmentName(eq) === filters.selectedEquipment) ||
                (filters.selectedEquipment === 'No Equipment' && exercise.equipmentRequired.length === 0);

            // Difficulty filter with case-insensitive matching
            const matchesDifficulty = filters.selectedDifficulty === 'all' ||
                exercise.difficultyLevel.toLowerCase() === filters.selectedDifficulty.toLowerCase();

            // Exercise type filter
            const matchesExerciseType = filters.selectedExerciseType === 'all' ||
                exercise.exerciseType === filters.selectedExerciseType;

            // Rating filter
            const matchesRating = exercise.averageRating >= filters.minRating;

            // Duration filter
            const matchesDuration = exercise.estimatedDurationMinutes <= filters.maxDuration;

            // Professional filter
            const matchesProfessional = !filters.showProfessionalOnly || exercise.createdByProfessional;

            // ✅ NEW: Favorites filter
            const matchesFavorites = !filters.onlyFavorites || exercise.isFavorite;

            // ✅ NEW: Muscle groups filter
            const matchesMuscleGroups = filters.muscleGroups.length === 0 ||
                filters.muscleGroups.some(targetMuscle =>
                    exercise.targetMuscleGroups.some(exerciseMuscle =>
                        exerciseMuscle.toLowerCase().includes(targetMuscle.toLowerCase())
                    )
                );

            // ✅ NEW: Available equipment filter
            const matchesAvailableEquipment = filters.availableEquipment.length === 0 ||
                exercise.equipmentRequired.every(required =>
                    filters.availableEquipment.includes(required) || required === 'none'
                );

            // ✅ NEW: Fitness level filter
            const matchesFitnessLevel = filters.fitnessLevel === 'all' ||
                exercise.difficultyLevel.toLowerCase() === filters.fitnessLevel.toLowerCase();

            // Enhanced workout tracking mode filter
            const matchesTrackingType = !filters.trackingType || filters.trackingType === 'all' ||
                (filters.trackingType === 'cardio' && exercise.isCardio) ||
                (filters.trackingType === 'isometric' && exercise.isIsometric) ||
                (filters.trackingType === 'strength' && !exercise.isCardio && !exercise.isIsometric);

            // Return true only if ALL filters match
            return matchesSearch &&
                matchesEquipment &&
                matchesDifficulty &&
                matchesExerciseType &&
                matchesRating &&
                matchesDuration &&
                matchesProfessional &&
                matchesFavorites &&
                matchesMuscleGroups &&
                matchesAvailableEquipment &&
                matchesFitnessLevel &&
                matchesTrackingType;
        });

        // Enhanced sorting with workout tracking mode consideration
        filtered.sort((a: Exercise, b: Exercise) => {
            switch (filters.sortBy) {
                case 'rating':
                    if (b.averageRating !== a.averageRating) {
                        return b.averageRating - a.averageRating;
                    }
                    return b.totalRatings - a.totalRatings;

                case 'popularity':
                    if (b.usageCount !== a.usageCount) {
                        return b.usageCount - a.usageCount;
                    }
                    return b.averageRating - a.averageRating;

                case 'duration':
                    if (a.estimatedDurationMinutes !== b.estimatedDurationMinutes) {
                        return a.estimatedDurationMinutes - b.estimatedDurationMinutes;
                    }
                    return b.averageRating - a.averageRating;

                case 'calories':
                    if (b.estimatedCalories !== a.estimatedCalories) {
                        return b.estimatedCalories - a.estimatedCalories;
                    }
                    return b.averageRating - a.averageRating;

                case 'newest':
                    return b.id - a.id;

                default: // 'relevance'
                    const calculateRelevanceScore = (exercise: Exercise): number => {
                        let score = 0;

                        score += exercise.averageRating * 0.2;
                        score += (exercise.usageCount / 1000) * 0.2;
                        score += exercise.createdByProfessional ? 0.25 : 0;

                        if (exercise.averageRating >= 4.5) {
                            score += 0.15;
                        } else if (exercise.averageRating >= 4.0) {
                            score += 0.1;
                        }

                        if (exercise.usageCount >= 2000) {
                            score += 0.15;
                        } else if (exercise.usageCount >= 1000) {
                            score += 0.1;
                        }

                        if (exercise.canDoAtHome) {
                            score += 0.05;
                        }

                        if (!exercise.requiresEquipment) {
                            score += 0.05;
                        }

                        if (exercise.videoUrl) {
                            score += 0.03;
                        }

                        // Enhanced tracking mode preference boost
                        if (filters.trackingType && filters.trackingType !== 'all') {
                            if ((filters.trackingType === 'cardio' && exercise.isCardio) ||
                                (filters.trackingType === 'isometric' && exercise.isIsometric) ||
                                (filters.trackingType === 'strength' && !exercise.isCardio && !exercise.isIsometric)) {
                                score += 0.2;
                            }
                        }

                        if (filters.searchTerm.trim()) {
                            const searchTerm = filters.searchTerm.toLowerCase();
                            if (exercise.name.toLowerCase().includes(searchTerm)) {
                                score += 0.1;
                            }
                            if (exercise.targetMuscleGroups.some(muscle => muscle.toLowerCase().includes(searchTerm))) {
                                score += 0.05;
                            }
                        }

                        if (filters.activeGoal !== 'all') {
                            if (filters.activeGoal === 'fat-burn' && exercise.isCardio) {
                                score += 0.1;
                            } else if (filters.activeGoal === 'muscle-building' && !exercise.isCardio) {
                                score += 0.1;
                            } else if (filters.activeGoal === 'flexibility' && exercise.exerciseType === 'FLEXIBILITY') {
                                score += 0.1;
                            }
                        }

                        return score;
                    };

                    const aScore = calculateRelevanceScore(a);
                    const bScore = calculateRelevanceScore(b);

                    if (bScore !== aScore) {
                        return bScore - aScore;
                    }

                    return a.name.localeCompare(b.name);
            }
        });

        return filtered;
    }, [exercises, filters]);

    // Enhanced active filters with workout tracking mode support
    const getActiveFilters = (): ActiveFilter[] => {
        const activeFilters: ActiveFilter[] = [];

        if (filters.activeGoal !== 'all') {
            const goal = goals.find((g: Goal) => g.id === filters.activeGoal);
            if (goal) {
                activeFilters.push({
                    type: 'goal',
                    value: goal.name,
                    emoji: goal.emoji
                });
            }
        }

        if (filters.selectedDifficulty !== 'all') {
            const difficultyDisplay = {
                'beginner': { name: 'Beginner', emoji: '🟢' },
                'intermediate': { name: 'Intermediate', emoji: '🟡' },
                'advanced': { name: 'Advanced', emoji: '🔴' }
            };
            const difficulty = difficultyDisplay[filters.selectedDifficulty.toLowerCase() as keyof typeof difficultyDisplay];
            activeFilters.push({
                type: 'difficulty',
                value: difficulty?.name || filters.selectedDifficulty,
                emoji: difficulty?.emoji
            });
        }

        if (filters.selectedEquipment !== 'all') {
            const equipmentEmojis: Record<string, string> = {
                'No Equipment': '🏠',
                'Dumbbells': '🏋️',
                'Yoga Mat': '🧘',
                'Jump Rope': '🪢',
                'Foam Roller': '🔄',
                'Plyo Box': '📦',
                'Tennis Racket': '🎾',
                'Barbell': '🏋️‍♂️',
                'Kettlebell': '⚫',
                'Resistance Bands': '🔗',
                'Pull-up Bar': '🏗️',
                'Medicine Ball': '⚽'
            };
            activeFilters.push({
                type: 'equipment',
                value: filters.selectedEquipment,
                emoji: equipmentEmojis[filters.selectedEquipment] || '🔧'
            });
        }

        if (filters.selectedExerciseType !== 'all') {
            const exerciseType = exerciseTypeOptions.find((t: ExerciseTypeOption) => t.value === filters.selectedExerciseType);
            if (exerciseType) {
                activeFilters.push({
                    type: 'exerciseType',
                    value: exerciseType.display,
                    emoji: exerciseType.emoji
                });
            }
        }

        if (filters.trackingType && filters.trackingType !== 'all') {
            const trackingTypeDisplay = {
                'cardio': {
                    name: 'Cardio Tracking',
                    emoji: '❤️',
                    description: 'Time & distance based tracking'
                },
                'isometric': {
                    name: 'Hold Tracking',
                    emoji: '🛡️',
                    description: 'Duration holds & static positions'
                },
                'strength': {
                    name: 'Rep Tracking',
                    emoji: '💪',
                    description: 'Sets, reps & weight progression'
                }
            };
            const display = trackingTypeDisplay[filters.trackingType as keyof typeof trackingTypeDisplay];
            if (display) {
                activeFilters.push({
                    type: 'trackingType' as FilterType,
                    value: display.name,
                    emoji: display.emoji
                });
            }
        }

        if (filters.minRating > 0) {
            const ratingEmoji = filters.minRating >= 4.5 ? '⭐' :
                filters.minRating >= 4.0 ? '🌟' :
                    filters.minRating >= 3.0 ? '✨' : '⭐';
            activeFilters.push({
                type: 'rating',
                value: `${filters.minRating}+ stars`,
                emoji: ratingEmoji
            });
        }

        if (filters.maxDuration < 480) {
            const durationEmoji = filters.maxDuration <= 15 ? '⚡' :
                filters.maxDuration <= 30 ? '🕐' :
                    filters.maxDuration <= 60 ? '🕑' : '🕒';
            const durationText = filters.maxDuration === 480 ? '8+ hours' : `Under ${filters.maxDuration}min`;
            activeFilters.push({
                type: 'duration',
                value: durationText,
                emoji: durationEmoji
            });
        }

        if (filters.showProfessionalOnly) {
            activeFilters.push({
                type: 'professional',
                value: 'Professional Only',
                emoji: '🏆'
            });
        }

        if (filters.onlyFavorites) {
            activeFilters.push({
                type: 'favorites' as FilterType,
                value: 'Favorites Only',
                emoji: '❤️'
            });
        }

        if (filters.muscleGroups.length > 0) {
            activeFilters.push({
                type: 'muscleGroups' as FilterType,
                value: `${filters.muscleGroups.length} muscle group${filters.muscleGroups.length > 1 ? 's' : ''}`,
                emoji: '💪'
            });
        }

        if (filters.searchTerm.trim()) {
            activeFilters.push({
                type: 'search' as FilterType,
                value: `"${filters.searchTerm.trim()}"`,
                emoji: '🔍'
            });
        }

        return activeFilters;
    };

    // Enhanced results summary with workout tracking mode awareness
    const generateResultsSummary = (): string => {
        const count = filteredAndSortedExercises.length;
        const parts: string[] = [];

        parts.push(`Showing <span class="text-3xl font-bold text-blue-600">${count.toLocaleString()}</span>`);

        if (filters.trackingType && filters.trackingType !== 'all') {
            const trackingModeDisplay = {
                'cardio': '<span class="text-red-600 font-semibold">Cardio</span>',
                'isometric': '<span class="text-purple-600 font-semibold">Isometric</span>',
                'strength': '<span class="text-blue-600 font-semibold">Strength</span>'
            };
            const modeText = trackingModeDisplay[filters.trackingType as keyof typeof trackingModeDisplay];
            if (modeText) {
                parts.push(modeText);
            }
        }

        if (filters.selectedExerciseType !== 'all') {
            const exerciseType = exerciseTypeOptions.find((t: ExerciseTypeOption) => t.value === filters.selectedExerciseType);
            if (exerciseType) {
                parts.push(`<span class="text-purple-600 font-semibold">${exerciseType.display}</span>`);
            }
        }

        if (filters.selectedDifficulty !== 'all') {
            const difficultyColors = {
                'beginner': 'text-green-600',
                'intermediate': 'text-yellow-600',
                'advanced': 'text-red-600'
            };
            const colorClass = difficultyColors[filters.selectedDifficulty.toLowerCase() as keyof typeof difficultyColors] || 'text-orange-600';
            parts.push(`<span class="${colorClass} font-semibold">${filters.selectedDifficulty.charAt(0).toUpperCase() + filters.selectedDifficulty.slice(1)}</span>`);
        }

        parts.push(`exercise${count !== 1 ? 's' : ''}`);

        if (filters.selectedEquipment !== 'all') {
            parts.push(`using <span class="text-green-600 font-semibold">${filters.selectedEquipment}</span>`);
        }

        if (filters.activeGoal !== 'all') {
            const goal = goals.find((g: Goal) => g.id === filters.activeGoal);
            if (goal) {
                parts.push(`for <span class="text-indigo-600 font-semibold">${goal.name}</span>`);
            }
        }

        if (filters.searchTerm.trim()) {
            parts.push(`matching <span class="text-emerald-600 font-semibold">"${filters.searchTerm.trim()}"</span>`);
        }

        if (filters.minRating > 0) {
            parts.push(`rated <span class="text-yellow-600 font-semibold">${filters.minRating}+ stars</span>`);
        }

        return parts.join(' ');
    };

    // Update individual filter
    const updateFilter = <K extends keyof ExerciseFilters>(key: K, value: ExerciseFilters[K]) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    // Enhanced remove filter with workout tracking mode support
    const removeFilter = (filterType: FilterType): void => {
        switch (filterType) {
            case 'goal':
                updateFilter('activeGoal', 'all');
                break;
            case 'difficulty':
                updateFilter('selectedDifficulty', 'all');
                break;
            case 'equipment':
                updateFilter('selectedEquipment', 'all');
                break;
            case 'exerciseType':
                updateFilter('selectedExerciseType', 'all');
                break;
            case 'trackingType':
                updateFilter('trackingType', 'all');
                break;
            case 'rating':
                updateFilter('minRating', 0);
                break;
            case 'duration':
                updateFilter('maxDuration', 480);
                break;
            case 'professional':
                updateFilter('showProfessionalOnly', false);
                break;
            case 'favorites':
                updateFilter('onlyFavorites', false);
                break;
            case 'muscleGroups':
                updateFilter('muscleGroups', []);
                break;
            case 'search':
                updateFilter('searchTerm', '');
                break;
        }
    };

    // Enhanced clear all filters with workout tracking mode support
    const clearFilters = (): void => {
        setFilters({
            // Core filters
            activeGoal: 'all',
            searchTerm: '',
            selectedEquipment: 'all',
            selectedDifficulty: 'all',
            selectedExerciseType: 'all',

            // Range filters
            minRating: 0,
            maxDuration: 480,

            // Sort and display options
            sortBy: 'relevance',
            showProfessionalOnly: false,

            // ✅ Required fields
            onlyFavorites: false,
            includeCompleted: true,
            muscleGroups: [],
            availableEquipment: [],
            fitnessLevel: 'all',

            // Enhanced: Workout tracking mode filter
            trackingType: 'all',

            // Optional backward compatibility fields
            exerciseType: undefined,
            difficulty: undefined,
            equipment: undefined,
            professionalOnly: undefined
        });
    };

    // Enhanced helper function to get filter count for UI badges
    const getActiveFilterCount = (): number => {
        let count = 0;

        if (filters.activeGoal !== 'all') count++;
        if (filters.selectedDifficulty !== 'all') count++;
        if (filters.selectedEquipment !== 'all') count++;
        if (filters.selectedExerciseType !== 'all') count++;
        if (filters.trackingType && filters.trackingType !== 'all') count++;
        if (filters.minRating > 0) count++;
        if (filters.maxDuration < 480) count++;
        if (filters.showProfessionalOnly) count++;
        if (filters.onlyFavorites) count++;
        if (filters.muscleGroups.length > 0) count++;
        if (filters.searchTerm.trim()) count++;

        return count;
    };

    // Helper function to check if any filters are active
    const hasActiveFilters = (): boolean => {
        return getActiveFilterCount() > 0;
    };

    // Helper function to get filter summary text
    const getFilterSummary = (): string => {
        const activeCount = getActiveFilterCount();
        if (activeCount === 0) return 'No filters applied';
        if (activeCount === 1) return '1 filter applied';
        return `${activeCount} filters applied`;
    };

    // Enhanced helper function to get tracking mode distribution
    const getTrackingModeDistribution = () => {
        const cardioCount = filteredAndSortedExercises.filter(ex => ex.isCardio).length;
        const isometricCount = filteredAndSortedExercises.filter(ex => ex.isIsometric).length;
        const strengthCount = filteredAndSortedExercises.filter(ex => !ex.isCardio && !ex.isIsometric).length;

        return {
            cardio: cardioCount,
            isometric: isometricCount,
            strength: strengthCount,
            total: filteredAndSortedExercises.length
        };
    };

    // Enhanced helper function to get recommended exercises based on current filters
    const getRecommendedExercises = (limit: number = 5): Exercise[] => {
        return filteredAndSortedExercises
            .filter(ex => ex.averageRating >= 4.0 && ex.usageCount >= 500)
            .slice(0, limit);
    };

    // Enhanced helper function to check if a specific tracking mode is available
    const hasExercisesForTrackingMode = (mode: WorkoutTrackingType): boolean => {
        return exercises.some(exercise => {
            if (mode === 'cardio') return exercise.isCardio;
            if (mode === 'isometric') return exercise.isIsometric;
            return !exercise.isCardio && !exercise.isIsometric;
        });
    };

    return {
        // Core state and functions
        filters,
        updateFilter,
        removeFilter,
        clearFilters,
        filteredAndSortedExercises,

        // Display functions
        getActiveFilters,
        generateResultsSummary,

        // Helper functions
        getActiveFilterCount,
        hasActiveFilters,
        getFilterSummary,

        // Enhanced functions for workout tracking
        getTrackingModeDistribution,
        getRecommendedExercises,
        hasExercisesForTrackingMode,
    };
};