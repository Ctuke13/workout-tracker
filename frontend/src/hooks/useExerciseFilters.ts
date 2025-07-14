import { useState, useMemo } from 'react';
import { Exercise, ExerciseFilters, SortOption, FilterType, ActiveFilter, Goal, ExerciseTypeOption } from '../types/exercise';
import { formatEquipmentName } from '../utils/exerciseFormatters';

export const useExerciseFilters = (
    exercises: Exercise[],
    goals: Goal[],
    exerciseTypeOptions: ExerciseTypeOption[]
) => {
    // Filter state
    const [filters, setFilters] = useState<ExerciseFilters>({
        activeGoal: 'all',
        searchTerm: '',
        selectedEquipment: 'all',
        selectedDifficulty: 'all',
        selectedExerciseType: 'all',
        minRating: 0,
        maxDuration: 480,
        sortBy: 'relevance',
        showProfessionalOnly: false,
    });

    // Filtered and sorted exercises
    const filteredAndSortedExercises = useMemo(() => {
        let filtered = exercises.filter((exercise: Exercise) => {
            // Search filter
            const matchesSearch = filters.searchTerm === '' ||
                exercise.name.toLowerCase().includes(filters.searchTerm.toLowerCase()) ||
                exercise.description.toLowerCase().includes(filters.searchTerm.toLowerCase()) ||
                exercise.benefits.some((benefit: string) => benefit.toLowerCase().includes(filters.searchTerm.toLowerCase()));

            // Basic filters
            const matchesEquipment = filters.selectedEquipment === 'all' ||
                exercise.equipmentRequired.some((eq: string) => formatEquipmentName(eq) === filters.selectedEquipment);
            const matchesDifficulty = filters.selectedDifficulty === 'all' ||
                exercise.difficultyLevel.toLowerCase() === filters.selectedDifficulty.toLowerCase();
            const matchesExerciseType = filters.selectedExerciseType === 'all' ||
                exercise.exerciseType === filters.selectedExerciseType;
            const matchesRating = exercise.averageRating >= filters.minRating;
            const matchesDuration = exercise.estimatedDurationMinutes <= filters.maxDuration;
            const matchesProfessional = !filters.showProfessionalOnly || exercise.createdByProfessional;

            return matchesSearch && matchesEquipment && matchesDifficulty && matchesExerciseType &&
                matchesRating && matchesDuration && matchesProfessional;
        });

        // Enhanced sorting
        filtered.sort((a: Exercise, b: Exercise) => {
            switch (filters.sortBy) {
                case 'rating':
                    return b.averageRating - a.averageRating;
                case 'popularity':
                    return b.usageCount - a.usageCount;
                case 'duration':
                    return a.estimatedDurationMinutes - b.estimatedDurationMinutes;
                case 'calories':
                    return b.estimatedCalories - a.estimatedCalories;
                case 'newest':
                    return b.id - a.id;
                default: // relevance
                    const aScore = (a.averageRating * 0.3) + (a.usageCount / 1000 * 0.3) + (a.createdByProfessional ? 0.4 : 0);
                    const bScore = (b.averageRating * 0.3) + (b.usageCount / 1000 * 0.3) + (b.createdByProfessional ? 0.4 : 0);
                    return bScore - aScore;
            }
        });

        return filtered;
    }, [exercises, filters]);

    // Get active filters for chips
    const getActiveFilters = (): ActiveFilter[] => {
        const activeFilters: ActiveFilter[] = [];

        if (filters.activeGoal !== 'all') {
            const goal = goals.find((g: Goal) => g.id === filters.activeGoal);
            if (goal) activeFilters.push({ type: 'goal', value: goal.name, emoji: goal.emoji });
        }

        if (filters.selectedDifficulty !== 'all') {
            activeFilters.push({ type: 'difficulty', value: filters.selectedDifficulty });
        }

        if (filters.selectedEquipment !== 'all') {
            activeFilters.push({ type: 'equipment', value: filters.selectedEquipment });
        }

        if (filters.selectedExerciseType !== 'all') {
            const exerciseType = exerciseTypeOptions.find((t: ExerciseTypeOption) => t.value === filters.selectedExerciseType);
            if (exerciseType) activeFilters.push({ type: 'exerciseType', value: exerciseType.display, emoji: exerciseType.emoji });
        }

        if (filters.minRating > 0) {
            activeFilters.push({ type: 'rating', value: `${filters.minRating}+ stars` });
        }

        if (filters.maxDuration < 480) {
            activeFilters.push({ type: 'duration', value: `Under ${filters.maxDuration}min` });
        }

        if (filters.showProfessionalOnly) {
            activeFilters.push({ type: 'professional', value: 'Professional Only' });
        }

        return activeFilters;
    };

    // Generate results summary
    const generateResultsSummary = (): string => {
        const count = filteredAndSortedExercises.length;
        const parts: string[] = [];

        // Base text - ONLY the number is bigger now
        parts.push(`Showing <span class="text-3xl font-bold text-blue-600">${count}</span>`);

        // Add exercise type if selected
        if (filters.selectedExerciseType !== 'all') {
            const exerciseType = exerciseTypeOptions.find((t: ExerciseTypeOption) => t.value === filters.selectedExerciseType);
            if (exerciseType) {
                parts.push(`<span class="text-purple-600 font-semibold">${exerciseType.display}</span>`);
            }
        }

        // Add difficulty if selected
        if (filters.selectedDifficulty !== 'all') {
            parts.push(`<span class="text-orange-600 font-semibold">${filters.selectedDifficulty}</span>`);
        }

        // Add "exercises"
        parts.push(`exercise${count !== 1 ? 's' : ''}`);

        // Add equipment if selected
        if (filters.selectedEquipment !== 'all') {
            parts.push(`using <span class="text-green-600 font-semibold">${filters.selectedEquipment}</span>`);
        }

        // Add goal if selected
        if (filters.activeGoal !== 'all') {
            const goal = goals.find((g: Goal) => g.id === filters.activeGoal);
            if (goal) {
                parts.push(`for <span class="text-red-600 font-semibold">${goal.name}</span>`);
            }
        }

        // Add search term if present
        if (filters.searchTerm.trim()) {
            parts.push(`matching <span class="text-green-600 font-semibold">"${filters.searchTerm.trim()}"</span>`);
        }

        return parts.join(' ');
    };

    // Update individual filter
    const updateFilter = <K extends keyof ExerciseFilters>(key: K, value: ExerciseFilters[K]) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    // Remove specific filter
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
            case 'rating':
                updateFilter('minRating', 0);
                break;
            case 'duration':
                updateFilter('maxDuration', 480);
                break;
            case 'professional':
                updateFilter('showProfessionalOnly', false);
                break;
        }
    };

    // Clear all filters
    const clearFilters = (): void => {
        setFilters({
            activeGoal: 'all',
            searchTerm: '',
            selectedEquipment: 'all',
            selectedDifficulty: 'all',
            selectedExerciseType: 'all',
            minRating: 0,
            maxDuration: 480,
            sortBy: 'relevance',
            showProfessionalOnly: false,
        });
    };

    return {
        filters,
        updateFilter,
        removeFilter,
        clearFilters,
        filteredAndSortedExercises,
        getActiveFilters,
        generateResultsSummary,
    };
};