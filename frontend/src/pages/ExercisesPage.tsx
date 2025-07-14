import React, { useState, useEffect } from 'react';
import { Search, Filter, X, Award, ChevronDown } from 'lucide-react';
import { Exercise, Goal, ExerciseTypeOption, SortOption, FilterType } from '../types/exercise';
import { ExerciseCard } from '../components/ExercisePage/ExerciseCard';
import { MobileFilterDrawer } from '../components/ExercisePage/MobileFilterDrawerProps';
import { useExerciseFilters } from '../hooks/useExerciseFilters';
import { useNavigate } from 'react-router-dom';
import {
    getMockGoals,
    getMockExerciseTypes,
    fetchExercises
} from '../services/mockData';

const defaultDifficultyOptions: string[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
const defaultEquipmentOptions: string[] = ['No Equipment', 'Dumbbells', 'Yoga Mat', 'Jump Rope', 'Foam Roller', 'Plyo Box', 'Tennis Racket'];

export const ExercisesPage: React.FC = () => {
    const navigate = useNavigate();

    // State for exercises and filter options
    const [exercises, setExercises] = useState<Exercise[]>([]);
    const [goals, setGoals] = useState<Goal[]>([]);
    const [exerciseTypeOptions, setExerciseTypeOptions] = useState<ExerciseTypeOption[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showAdvancedFilters, setShowAdvancedFilters] = useState(false);
    const [difficultyOptions] = useState<string[]>(defaultDifficultyOptions);
    const [equipmentOptions] = useState<string[]>(defaultEquipmentOptions);

    // UI State
    const [expandedCards, setExpandedCards] = useState<Set<number>>(new Set());
    const [favorites, setFavorites] = useState<Set<number>>(new Set());
    const [showMobileFilters, setShowMobileFilters] = useState(false);

    // Custom hook for filtering
    const {
        filters,
        updateFilter,
        removeFilter,
        clearFilters,
        filteredAndSortedExercises,
        getActiveFilters,
        generateResultsSummary,
    } = useExerciseFilters(exercises, goals, exerciseTypeOptions);

    // Load data on component mount
    useEffect(() => {
        const loadData = async (): Promise<void> => {
            try {
                setLoading(true);

                // Fetch exercises
                const exercisesData: Exercise[] = await fetchExercises();
                setExercises(exercisesData);

                // Generate goals with correct exercise count
                const goalsData: Goal[] = getMockGoals(exercisesData.length);
                setGoals(goalsData);

                // Generate exercise type options
                const exerciseTypesData: ExerciseTypeOption[] = getMockExerciseTypes();
                setExerciseTypeOptions(exerciseTypesData);

                setError(null);
            } catch (err) {
                setError('Failed to load exercises. Please try again.');
                console.error('Error loading exercises:', err);
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, []);

    // Handle card expansion
    const handleToggleExpand = (index: number): void => {
        const newExpanded = new Set(expandedCards);
        if (newExpanded.has(index)) {
            newExpanded.delete(index);
        } else {
            newExpanded.add(index);
        }
        setExpandedCards(newExpanded);
    };

    // Handle favorites
    const handleToggleFavorite = (exerciseId: number): void => {
        const newFavorites = new Set(favorites);
        if (newFavorites.has(exerciseId)) {
            newFavorites.delete(exerciseId);
        } else {
            newFavorites.add(exerciseId);
        }
        setFavorites(newFavorites);
    };

    // Handle workout tracking
    const handleTrackWorkout = (exerciseId: number): void => {
        const exercise = exercises.find(ex => ex.id === exerciseId);
        if (exercise) {
            console.log(`Tracking workout for: ${exercise.name}`);
            // Implement workout tracking logic here
        }
    };

    // Handle goal selection
    const handleGoalSelect = (goalId: string): void => {
        updateFilter('activeGoal', goalId);
    };

    // Handle search
    const handleSearch = (searchTerm: string): void => {
        updateFilter('searchTerm', searchTerm);
    };

    // Navigation handlers
    const handleBackToHome = (): void => {
        navigate('/');
    };

    // Handle filter removal
    const handleRemoveFilter = (filterType: string): void => {
        // Map string to FilterType
        const filterTypeMap: Record<string, FilterType> = {
            'goal': 'goal',
            'activeGoal': 'goal',
            'difficulty': 'difficulty',
            'selectedDifficulty': 'difficulty',
            'equipment': 'equipment',
            'selectedEquipment': 'equipment',
            'exerciseType': 'exerciseType',
            'selectedExerciseType': 'exerciseType',
            'rating': 'rating',
            'minRating': 'rating',
            'duration': 'duration',
            'maxDuration': 'duration',
            'professional': 'professional',
            'showProfessionalOnly': 'professional',
        };

        const mappedFilterType = filterTypeMap[filterType];
        if (mappedFilterType) {
            removeFilter(mappedFilterType);
        }
    };

    // Loading state
    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Loading exercises...</p>
                </div>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
                <div className="text-center max-w-md mx-auto">
                    <div className="text-red-500 text-xl mb-4">⚠️</div>
                    <p className="text-gray-600 mb-4">{error}</p>
                    <button
                        onClick={() => window.location.reload()}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        Try Again
                    </button>
                </div>
            </div>
        );
    }

    const activeFilters = getActiveFilters();

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Mobile-First Navigation Header */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-b border-gray-200 shadow-sm">
                <div className="px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-14 sm:h-16">
                        <div className="flex items-center min-w-0">
                            <span onClick={handleBackToHome} className="text-lg sm:text-xl font-bold text-blue-600 hover:text-blue-700 transition-colors cursor-pointer truncate">
                                💪 WorkoutTracker
                            </span>
                            <span className="ml-2 px-2 py-0.5 text-xs bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full font-medium">
                                BETA
                            </span>
                        </div>

                        <div className="flex items-center space-x-2 sm:space-x-4">
                            <button onClick={handleBackToHome} className="hidden sm:block text-gray-600 hover:text-gray-900 transition-colors text-sm">
                                ← Back to Home
                            </button>
                            <button className="bg-gradient-to-r from-orange-500 to-red-500 text-white px-3 sm:px-6 py-2 sm:py-2.5 rounded-lg font-medium sm:font-semibold shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300 text-sm sm:text-base">
                                Start FREE
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Main Content */}
            <div className="pt-14 sm:pt-16">
                {/* Hero Section */}
                <section className="px-4 py-6 sm:py-8 lg:py-12 bg-gradient-to-br from-blue-50 to-green-50">
                    <div className="max-w-7xl mx-auto text-center">
                        <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-4 sm:mb-6">
                            <span className="text-transparent bg-gradient-to-r from-blue-600 to-green-500 bg-clip-text">
                                Exercise Library
                            </span>
                        </h1>
                        <p className="text-base sm:text-lg text-gray-600 max-w-2xl mx-auto mb-6 sm:mb-8">
                            Discover professional-grade workouts with detailed instructions, ratings, and progress tracking.
                        </p>
                    </div>
                </section>

                {/* Error Banner */}
                {error && (
                    <div className="px-4 py-2">
                        <div className="max-w-7xl mx-auto">
                            <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 flex items-center justify-between">
                                <span className="text-sm">{error}</span>
                                <button
                                    onClick={() => setError(null)}
                                    className="text-red-600 hover:text-red-800 ml-2"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Main Container */}
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
                    {/* Goals Section */}
                    <div className="mb-6 sm:mb-8">
                        <h2 className="text-lg sm:text-xl font-semibold text-gray-900 mb-4">
                            What's your goal?
                        </h2>
                        <div className="flex flex-wrap gap-2 sm:gap-3">
                            {goals.map((goal) => (
                                <button
                                    key={goal.id}
                                    onClick={() => handleGoalSelect(goal.id)}
                                    className={`flex items-center gap-2 px-3 sm:px-4 py-2 rounded-full border transition-all text-sm sm:text-base ${
                                        filters.activeGoal === goal.id
                                            ? 'bg-blue-600 text-white border-blue-600'
                                            : 'bg-white text-gray-700 border-gray-300 hover:border-blue-300'
                                    }`}
                                >
                                    <span>{goal.emoji}</span>
                                    <span className="font-medium">{goal.name}</span>
                                    <span className="text-xs sm:text-sm opacity-75">({goal.count})</span>
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Search and Filter Section */}
                    <div className="mb-6">
                        {/* Search Bar */}
                        <div className="relative mb-4">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <input
                                type="text"
                                placeholder="Search exercises..."
                                value={filters.searchTerm}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleSearch(e.target.value)}
                                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-base"
                            />
                        </div>

                        {/* Filter Controls */}
                        <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
                            {/* Mobile Filter Button */}
                            <button
                                onClick={() => setShowMobileFilters(true)}
                                className="sm:hidden flex items-center justify-center gap-2 px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 bg-white"
                            >
                                <Filter className="w-5 h-5" />
                                <span>Filters</span>
                                {activeFilters.length > 0 && (
                                    <span className="bg-blue-600 text-white text-xs rounded-full px-2 py-1 min-w-[20px]">
                                        {activeFilters.length}
                                    </span>
                                )}
                            </button>

                            {/* Desktop Quick Filters */}
                            <div className="hidden sm:flex flex-wrap gap-2 flex-1">
                                <select
                                    value={filters.selectedExerciseType}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('selectedExerciseType', e.target.value)}
                                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm min-w-[120px]"
                                >
                                    <option value="all">All Types</option>
                                    {exerciseTypeOptions.map((type) => (
                                        <option key={type.value} value={type.value}>
                                            {type.display}
                                        </option>
                                    ))}
                                </select>

                                <select
                                    value={filters.selectedDifficulty}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('selectedDifficulty', e.target.value)}
                                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm min-w-[120px]"
                                >
                                    <option value="all">All Levels</option>
                                    {difficultyOptions.map((difficulty) => (
                                        <option key={difficulty} value={difficulty}>
                                            {difficulty}
                                        </option>
                                    ))}
                                </select>

                                <select
                                    value={filters.selectedEquipment}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('selectedEquipment', e.target.value)}
                                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm min-w-[120px]"
                                >
                                    <option value="all">All Equipment</option>
                                    {equipmentOptions.map((equipment) => (
                                        <option key={equipment} value={equipment}>
                                            {equipment}
                                        </option>
                                    ))}
                                </select>

                                <button
                                    onClick={() => setShowAdvancedFilters(!showAdvancedFilters)}
                                    className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg bg-white text-gray-700 hover:bg-gray-50 text-sm whitespace-nowrap"
                                >
                                    <span>More Filters</span>
                                    <ChevronDown className={`w-4 h-4 transition-transform ${showAdvancedFilters ? 'rotate-180' : ''}`} />
                                </button>
                            </div>
                        </div>

                        {/* Advanced Filters */}
                        {showAdvancedFilters && (
                            <div className="mt-4 bg-white border border-gray-200 rounded-lg p-4 sm:p-6">
                                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Sort By</label>
                                        <select
                                            value={filters.sortBy}
                                            onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('sortBy', e.target.value as SortOption)}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm"
                                        >
                                            <option value="relevance">Best Match</option>
                                            <option value="rating">Highest Rated</option>
                                            <option value="popularity">Most Popular</option>
                                            <option value="calories">Most Calories</option>
                                            <option value="duration">Shortest Duration</option>
                                            <option value="newest">Newest First</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Minimum Rating</label>
                                        <select
                                            value={filters.minRating}
                                            onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('minRating', Number(e.target.value))}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm"
                                        >
                                            <option value={0}>Any Rating</option>
                                            <option value={3}>3+ Stars</option>
                                            <option value={4}>4+ Stars</option>
                                            <option value={4.5}>4.5+ Stars</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            Max Duration: {filters.maxDuration === 480 ? '8+ hours' : `${filters.maxDuration} min`}
                                        </label>
                                        <input
                                            type="range"
                                            min="5"
                                            max="480"
                                            value={filters.maxDuration}
                                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('maxDuration', Number(e.target.value))}
                                            className="w-full accent-blue-600 h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Content Type</label>
                                        <label className="flex items-center cursor-pointer">
                                            <input
                                                type="checkbox"
                                                checked={filters.showProfessionalOnly}
                                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => updateFilter('showProfessionalOnly', e.target.checked)}
                                                className="mr-2 w-4 h-4 text-blue-600 focus:ring-blue-500 rounded"
                                            />
                                            <div className="flex items-center">
                                                <Award className="w-4 h-4 text-blue-600 mr-1" />
                                                <span className="text-sm">Professional Only</span>
                                            </div>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Active Filters */}
                        {activeFilters.length > 0 && (
                            <div className="mt-4 flex flex-wrap gap-2">
                                {activeFilters.map((filter, index) => (
                                    <div
                                        key={index}
                                        className="flex items-center gap-2 px-3 py-1.5 bg-blue-100 text-blue-800 rounded-full text-sm"
                                    >
                                        {filter.emoji && <span>{filter.emoji}</span>}
                                        <span>{filter.value}</span>
                                        <button
                                            onClick={() => handleRemoveFilter(filter.type)}
                                            className="hover:bg-blue-200 rounded-full p-0.5 ml-1"
                                        >
                                            <X className="w-3 h-3" />
                                        </button>
                                    </div>
                                ))}
                                <button
                                    onClick={clearFilters}
                                    className="px-3 py-1.5 text-gray-600 hover:text-gray-800 text-sm underline"
                                >
                                    Clear all
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Results Summary */}
                    <div className="mb-6">
                        <div
                            className="text-base sm:text-lg text-gray-700"
                            dangerouslySetInnerHTML={{ __html: generateResultsSummary() }}
                        />
                    </div>

                    {/* Exercises Grid */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
                        {filteredAndSortedExercises.map((exercise, index) => (
                            <ExerciseCard
                                key={exercise.id}
                                exercise={exercise}
                                index={index}
                                isExpanded={expandedCards.has(index)}
                                isFavorite={favorites.has(exercise.id)}
                                onToggleExpand={handleToggleExpand}
                                onToggleFavorite={handleToggleFavorite}
                                onTrackWorkout={handleTrackWorkout}
                            />
                        ))}
                    </div>

                    {/* No Results State */}
                    {filteredAndSortedExercises.length === 0 && (
                        <div className="text-center py-12 px-4">
                            <div className="text-gray-400 text-5xl sm:text-6xl mb-4">🔍</div>
                            <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                No exercises found
                            </h3>
                            <p className="text-gray-600 mb-6 max-w-md mx-auto">
                                Try adjusting your filters or search terms to find what you're looking for
                            </p>
                            <button
                                onClick={clearFilters}
                                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                            >
                                Clear all filters
                            </button>
                        </div>
                    )}
                </div>
            </div>

            {/* Mobile Filter Drawer */}
            <MobileFilterDrawer
                isOpen={showMobileFilters}
                onClose={() => setShowMobileFilters(false)}
                filters={filters}
                exerciseTypeOptions={exerciseTypeOptions}
                onFilterChange={updateFilter}
            />
        </div>
    );
};