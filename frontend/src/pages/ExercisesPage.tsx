import React, { useState, useEffect } from 'react';
import { Search, Filter, X, Award, ChevronDown } from 'lucide-react';
import {
    Exercise,
    Goal,
    ExerciseTypeOption,
    SortOption,
    FilterType,
    WorkoutTrackingType,
    ExerciseConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    StrengthConfiguration,
    UnifiedWorkoutData,
    getWorkoutTrackingType,
    getDefaultConfigForExercise
} from '../types/exercise';
import { ExerciseCard } from '../components/ExercisePage/ExerciseCard';
import { MobileFilterDrawer } from '../components/ExercisePage/MobileFilterDrawerProps';
import { WorkoutTrackingInterface } from '../components/WorkoutTracking/WorkoutTrackingInterface';
import { useExerciseFilters } from '../hooks/useExerciseFilters';
import { useNavigate } from 'react-router-dom';
import { exerciseApi } from '../services/exerciseApi';

const defaultDifficultyOptions: string[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
const defaultEquipmentOptions: string[] = ['No Equipment', 'Dumbbells', 'Yoga Mat', 'Jump Rope', 'Foam Roller', 'Plyo Box', 'Tennis Racket'];

// Helper functions for goal formatting
const formatGoalName = (goal: string): string => {
    const goalMap: Record<string, string> = {
        'fat-burn': 'Fat Burn',
        'muscle-building': 'Muscle Building',
        'endurance': 'Endurance',
        'strength': 'Strength',
        'flexibility': 'Flexibility',
        'weight-loss': 'Weight Loss',
        'general-fitness': 'General Fitness'
    };
    return goalMap[goal] || goal.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase());
};

const getGoalEmoji = (goal: string): string => {
    const emojiMap: Record<string, string> = {
        'fat-burn': '🔥',
        'muscle-building': '💪',
        'endurance': '🏃‍♂️',
        'strength': '🏋️‍♀️',
        'flexibility': '🤸‍♀️',
        'weight-loss': '⚖️',
        'general-fitness': '✨'
    };
    return emojiMap[goal] || '🎯';
};

// Enhanced exercise type options
const getExerciseTypeOptions = (): ExerciseTypeOption[] => [
    { value: 'STRENGTH', display: 'Strength Training', emoji: '💪' },
    { value: 'CARDIO', display: 'Cardiovascular', emoji: '❤️' },
    { value: 'FLEXIBILITY', display: 'Flexibility', emoji: '🤸‍♀️' },
    { value: 'BALANCE', display: 'Balance', emoji: '⚖️' },
    { value: 'PLYOMETRIC', display: 'Plyometric', emoji: '⚡' },
    { value: 'REHABILITATION', display: 'Rehabilitation', emoji: '🛡️' },
    { value: 'SPORTS_SPECIFIC', display: 'Sports Specific', emoji: '🏆' }
];

// ✅ FIXED: Enhanced Exercise Card Component with proper props
interface EnhancedExerciseCardProps {
    exercise: Exercise;
    index: number;
    isExpanded: boolean;
    isFavorite: boolean;
    onToggleExpand: (index: number) => void;
    onToggleFavorite: (exerciseId: number) => void;
    onTrackWorkout: (exerciseId: number) => void;
}

const EnhancedExerciseCard: React.FC<EnhancedExerciseCardProps> = ({
                                                                       exercise,
                                                                       index,
                                                                       isExpanded,
                                                                       isFavorite,
                                                                       onToggleExpand,
                                                                       onToggleFavorite,
                                                                       onTrackWorkout
                                                                   }) => {
    const trackingMode = getWorkoutTrackingType(exercise);

    // Get tracking mode display info
    const trackingModeInfo = {
        cardio: { emoji: '❤️', label: 'Cardio Timer', color: 'text-red-600 bg-red-50' },
        isometric: { emoji: '🛡️', label: 'Hold Duration', color: 'text-purple-600 bg-purple-50' },
        strength: { emoji: '💪', label: 'Rep Counter', color: 'text-blue-600 bg-blue-50' }
    };

    const modeInfo = trackingModeInfo[trackingMode];

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200 overflow-hidden">
            {/* Card Header */}
            <div className="p-4 sm:p-6">
                <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center min-w-0 flex-1">
                        <span className="text-2xl mr-3 flex-shrink-0">
                            {exercise.emoji || '💪'}
                        </span>
                        <div className="min-w-0 flex-1">
                            <h3 className="text-lg font-semibold text-gray-900 truncate">
                                {exercise.name || exercise.exerciseName}
                            </h3>
                            <div className="flex items-center gap-2 mt-1">
                                <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${modeInfo.color}`}>
                                    {modeInfo.emoji} {modeInfo.label}
                                </span>
                                <span className="text-xs text-gray-500">
                                    {exercise.difficultyLevel}
                                </span>
                            </div>
                        </div>
                    </div>

                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onToggleFavorite(exercise.id);
                        }}
                        className={`p-2 rounded-full transition-colors ${
                            isFavorite
                                ? 'text-red-500 hover:text-red-600'
                                : 'text-gray-400 hover:text-red-500'
                        }`}
                    >
                        <span className="text-lg">
                            {isFavorite ? '❤️' : '🤍'}
                        </span>
                    </button>
                </div>

                {/* Description */}
                <p className="text-gray-600 text-sm mb-4 line-clamp-2">
                    {exercise.description}
                </p>

                {/* Exercise Stats */}
                <div className="flex items-center gap-4 text-sm text-gray-600 mb-4">
                    <div className="flex items-center gap-1">
                        <span>⏱️</span>
                        <span>{exercise.estimatedDurationMinutes || 0} min</span>
                    </div>
                    <div className="flex items-center gap-1">
                        <span>🔥</span>
                        <span>{exercise.estimatedCalories || 0} cal</span>
                    </div>
                    {exercise.averageRating > 0 && (
                        <div className="flex items-center gap-1">
                            <span>⭐</span>
                            <span>{exercise.averageRating.toFixed(1)}</span>
                        </div>
                    )}
                </div>

                {/* Exercise Tags */}
                <div className="flex flex-wrap gap-2 mb-4">
                    <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded-full">
                        {exercise.exerciseType}
                    </span>
                    {exercise.canDoAtHome && (
                        <span className="px-2 py-1 bg-green-100 text-green-700 text-xs rounded-full">
                            🏠 At Home
                        </span>
                    )}
                    {exercise.isCardio && (
                        <span className="px-2 py-1 bg-red-100 text-red-700 text-xs rounded-full">
                            ❤️ Cardio
                        </span>
                    )}
                    {exercise.isIsometric && (
                        <span className="px-2 py-1 bg-purple-100 text-purple-700 text-xs rounded-full">
                            🛡️ Isometric
                        </span>
                    )}
                </div>

                {/* Expanded Content */}
                {isExpanded && (
                    <div className="border-t border-gray-100 pt-4 mt-4">
                        <div className="space-y-3">
                            {/* Show target muscle groups if available */}
                            {exercise.targetMuscleGroups && exercise.targetMuscleGroups.length > 0 && (
                                <div>
                                    <h4 className="text-sm font-medium text-gray-900 mb-1">Primary Muscles</h4>
                                    <div className="flex flex-wrap gap-1">
                                        {exercise.targetMuscleGroups.map((muscle: string, idx: number) => (
                                            <span key={idx} className="px-2 py-1 bg-blue-100 text-blue-700 text-xs rounded">
                                                {muscle}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Show required equipment if available */}
                            {exercise.equipmentRequired && exercise.equipmentRequired.length > 0 && (
                                <div>
                                    <h4 className="text-sm font-medium text-gray-900 mb-1">Equipment</h4>
                                    <div className="flex flex-wrap gap-1">
                                        {exercise.equipmentRequired.map((item: string, idx: number) => (
                                            <span key={idx} className="px-2 py-1 bg-orange-100 text-orange-700 text-xs rounded">
                                                {item}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Show tips as instructions */}
                            {exercise.tips && exercise.tips.length > 0 && (
                                <div>
                                    <h4 className="text-sm font-medium text-gray-900 mb-1">Tips & Instructions</h4>
                                    <ol className="text-xs text-gray-600 space-y-1">
                                        {exercise.tips.slice(0, 3).map((tip: string, idx: number) => (
                                            <li key={idx} className="flex">
                                                <span className="mr-2 font-medium">{idx + 1}.</span>
                                                <span>{tip}</span>
                                            </li>
                                        ))}
                                        {exercise.tips.length > 3 && (
                                            <li className="text-gray-500 italic">
                                                +{exercise.tips.length - 3} more tips...
                                            </li>
                                        )}
                                    </ol>
                                </div>
                            )}

                            {/* Show benefits if available */}
                            {exercise.benefits && exercise.benefits.length > 0 && (
                                <div>
                                    <h4 className="text-sm font-medium text-gray-900 mb-1">Benefits</h4>
                                    <div className="flex flex-wrap gap-1">
                                        {exercise.benefits.slice(0, 3).map((benefit: string, idx: number) => (
                                            <span key={idx} className="px-2 py-1 bg-green-100 text-green-700 text-xs rounded">
                                                {benefit}
                                            </span>
                                        ))}
                                        {exercise.benefits.length > 3 && (
                                            <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded italic">
                                                +{exercise.benefits.length - 3} more
                                            </span>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Exercise metadata */}
                            <div className="pt-2 border-t border-gray-100">
                                <div className="text-xs text-gray-500 space-y-1">
                                    <div>Type: {exercise.exerciseType}</div>
                                    <div>Difficulty: {exercise.difficultyLevel}</div>
                                    {exercise.averageRating > 0 && (
                                        <div>Rating: ⭐ {exercise.averageRating.toFixed(1)} ({exercise.totalRatings} reviews)</div>
                                    )}
                                    {exercise.createdByProfessional && (
                                        <div className="flex items-center gap-1">
                                            <span className="text-blue-600">🏆</span>
                                            <span className="text-blue-600 font-medium">Professional Content</span>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Action Buttons */}
                <div className="flex gap-2 mt-4">
                    <button
                        onClick={() => onTrackWorkout(exercise.id)}
                        className="flex-1 bg-gradient-to-r from-blue-600 to-purple-600 text-white px-4 py-2 rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 text-sm font-medium flex items-center justify-center gap-2"
                    >
                        {modeInfo.emoji}
                        <span>Start {modeInfo.label}</span>
                    </button>

                    <button
                        onClick={() => onToggleExpand(index)}
                        className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
                    >
                        {isExpanded ? 'Less' : 'More'}
                    </button>
                </div>
            </div>
        </div>
    );
};

// ✅ FIXED: Main component export
const ExercisesPage: React.FC = () => {
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

    // Enhanced: Workout tracking state
    const [selectedExerciseForTracking, setSelectedExerciseForTracking] = useState<Exercise | null>(null);
    const [showWorkoutInterface, setShowWorkoutInterface] = useState(false);
    const [workoutConfiguration, setWorkoutConfiguration] = useState<ExerciseConfiguration | null>(null);

    // Enhanced hook for filtering with workout tracking mode support
    const {
        filters,
        updateFilter,
        removeFilter,
        clearFilters,
        filteredAndSortedExercises,
        getActiveFilters,
        generateResultsSummary,
        getTrackingModeDistribution,
    } = useExerciseFilters(exercises, goals, exerciseTypeOptions);

    // Load data on component mount - Enhanced with real API integration
    useEffect(() => {
        const loadData = async (): Promise<void> => {
            try {
                setLoading(true);
                console.log('🔄 Loading enhanced exercise data from backend...');

                const initialData = await exerciseApi.getInitialData();

                console.log('✅ Enhanced exercise data loaded:', {
                    exerciseCount: initialData.exercises.length,
                    goalsCount: initialData.goals.length,
                    hasCardioField: initialData.exercises.some(ex => ex.hasOwnProperty('isCardio')),
                    hasIsometricField: initialData.exercises.some(ex => ex.hasOwnProperty('isIsometric'))
                });

                setExercises(initialData.exercises);

                // Transform goals data to match Goal interface
                const goalsData: Goal[] = initialData.goals.map(g => ({
                    id: g.goal,
                    name: formatGoalName(g.goal),
                    emoji: getGoalEmoji(g.goal),
                    count: g.count
                }));
                setGoals(goalsData);

                // Generate enhanced exercise type options
                const exerciseTypesData: ExerciseTypeOption[] = getExerciseTypeOptions();
                setExerciseTypeOptions(exerciseTypesData);

                // Enhanced: Log workout tracking mode distribution
                const trackingDistribution = {
                    cardio: initialData.exercises.filter(ex => ex.isCardio).length,
                    isometric: initialData.exercises.filter(ex => ex.isIsometric).length,
                    strength: initialData.exercises.filter(ex => !ex.isCardio && !ex.isIsometric).length
                };

                console.log('📊 Enhanced workout tracking mode distribution:', trackingDistribution);

                setError(null);
            } catch (err) {
                setError('Failed to load exercises. Please check your backend connection.');
                console.error('❌ Error loading enhanced exercise data:', err);
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

    // Enhanced: Handle workout tracking with full three-mode support
    const handleTrackWorkout = (exerciseId: number): void => {
        const exercise = exercises.find(ex => ex.id === exerciseId);
        if (exercise) {
            const trackingMode = getWorkoutTrackingType(exercise);
            console.log(`🏃‍♂️ Starting ${trackingMode} workout tracking for: ${exercise.name}`);

            // Get default configuration for this exercise type
            const defaultConfig = getDefaultConfigForExercise(exercise);

            setSelectedExerciseForTracking(exercise);
            setWorkoutConfiguration(defaultConfig);
            setShowWorkoutInterface(true);
        }
    };

    // Enhanced: Handle workout completion
    const handleWorkoutComplete = (workoutData: UnifiedWorkoutData): void => {
        console.log('✅ Workout completed successfully:', workoutData);

        // Here you would save the workout data to your backend
        // await workoutApi.saveWorkout(workoutData);

        // Show success message or navigate to workout summary
        alert(`${workoutData.trackingMode.charAt(0).toUpperCase() + workoutData.trackingMode.slice(1)} workout completed! 🎉`);

        // Close the workout interface
        setShowWorkoutInterface(false);
        setSelectedExerciseForTracking(null);
        setWorkoutConfiguration(null);
    };

    // Enhanced: Handle workout cancellation
    const handleWorkoutCancel = (): void => {
        console.log('❌ Workout cancelled by user');
        setShowWorkoutInterface(false);
        setSelectedExerciseForTracking(null);
        setWorkoutConfiguration(null);
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

    // Enhanced: Handle filter removal with tracking type support
    const handleRemoveFilter = (filterType: string): void => {
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
            'trackingType': 'trackingType', // Enhanced: Support tracking type removal
            'search': 'search'
        };

        const mappedFilterType = filterTypeMap[filterType];
        if (mappedFilterType) {
            removeFilter(mappedFilterType);
        }
    };

    // Enhanced loading state
    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Loading enhanced exercises from backend...</p>
                    <p className="text-sm text-gray-500 mt-2">Connecting to real API with workout tracking modes...</p>
                </div>
            </div>
        );
    }

    // Enhanced error state
    if (error) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
                <div className="text-center max-w-md mx-auto">
                    <div className="text-red-500 text-xl mb-4">⚠️</div>
                    <p className="text-gray-600 mb-4">{error}</p>
                    <div className="space-y-2 mb-4">
                        <button
                            onClick={() => window.location.reload()}
                            className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                        >
                            Try Again
                        </button>
                        <p className="text-xs text-gray-500">
                            Make sure your backend server is running with workout tracking support
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    const activeFilters = getActiveFilters();
    const trackingDistribution = getTrackingModeDistribution();

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Enhanced Navigation Header */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-b border-gray-200 shadow-sm">
                <div className="px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-14 sm:h-16">
                        <div className="flex items-center min-w-0">
                            <span onClick={handleBackToHome} className="text-lg sm:text-xl font-bold text-blue-600 hover:text-blue-700 transition-colors cursor-pointer truncate">
                                💪 WorkoutTracker
                            </span>
                            <span className="ml-2 px-2 py-0.5 text-xs bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-full font-medium">
                                ENHANCED
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
                {/* Enhanced Hero Section */}
                <section className="px-4 py-6 sm:py-8 lg:py-12 bg-gradient-to-br from-blue-50 to-green-50">
                    <div className="max-w-7xl mx-auto text-center">
                        <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-4 sm:mb-6">
                            <span className="text-transparent bg-gradient-to-r from-blue-600 to-green-500 bg-clip-text">
                                Enhanced Exercise Library
                            </span>
                        </h1>
                        <p className="text-base sm:text-lg text-gray-600 max-w-2xl mx-auto mb-6 sm:mb-8">
                            Discover professional workouts with intelligent tracking: ❤️ Cardio Timer, 🛡️ Hold Duration, 💪 Rep Counter
                        </p>

                        {/* Enhanced: Tracking Mode Distribution Display */}
                        <div className="flex justify-center gap-4 sm:gap-8 mb-6">
                            <div className="text-center">
                                <div className="text-2xl font-bold text-red-600">{trackingDistribution.cardio}</div>
                                <div className="text-sm text-red-700">❤️ Cardio</div>
                            </div>
                            <div className="text-center">
                                <div className="text-2xl font-bold text-purple-600">{trackingDistribution.isometric}</div>
                                <div className="text-sm text-purple-700">🛡️ Isometric</div>
                            </div>
                            <div className="text-center">
                                <div className="text-2xl font-bold text-blue-600">{trackingDistribution.strength}</div>
                                <div className="text-sm text-blue-700">💪 Strength</div>
                            </div>
                        </div>
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

                    {/* Enhanced Search and Filter Section */}
                    <div className="mb-6">
                        {/* Enhanced Search Bar */}
                        <div className="relative mb-4">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                            <input
                                type="text"
                                placeholder="Search exercises... try 'cardio timer', 'isometric holds', 'strength reps'..."
                                value={filters.searchTerm}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleSearch(e.target.value)}
                                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-base"
                            />
                        </div>

                        {/* Enhanced Filter Controls */}
                        <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
                            {/* Mobile Filter Button */}
                            <button
                                onClick={() => setShowMobileFilters(true)}
                                className="sm:hidden flex items-center justify-center gap-2 px-4 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 bg-white"
                            >
                                <Filter className="w-5 h-5" />
                                <span>Enhanced Filters</span>
                                {activeFilters.length > 0 && (
                                    <span className="bg-blue-600 text-white text-xs rounded-full px-2 py-1 min-w-[20px]">
                                        {activeFilters.length}
                                    </span>
                                )}
                            </button>

                            {/* Enhanced Desktop Quick Filters */}
                            <div className="hidden sm:flex flex-wrap gap-2 flex-1">
                                {/* Enhanced: Workout Tracking Mode Filter */}
                                <select
                                    value={filters.trackingType || 'all'}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('trackingType', e.target.value as WorkoutTrackingType | 'all')}
                                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm min-w-[160px]"
                                >
                                    <option value="all">All Tracking Modes</option>
                                    <option value="cardio">❤️ Cardio Timer</option>
                                    <option value="isometric">🛡️ Hold Duration</option>
                                    <option value="strength">💪 Rep Counter</option>
                                </select>

                                <select
                                    value={filters.selectedExerciseType}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('selectedExerciseType', e.target.value)}
                                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm min-w-[120px]"
                                >
                                    <option value="all">All Types</option>
                                    {exerciseTypeOptions.map((type) => (
                                        <option key={type.value} value={type.value}>
                                            {type.emoji} {type.display}
                                        </option>
                                    ))}
                                </select>

                                <select
                                    value={filters.selectedDifficulty}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('selectedDifficulty', e.target.value)}
                                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm min-w-[120px]"
                                >
                                    <option value="all">All Levels</option>
                                    <option value="beginner">🟢 Beginner</option>
                                    <option value="intermediate">🟡 Intermediate</option>
                                    <option value="advanced">🔴 Advanced</option>
                                </select>

                                <select
                                    value={filters.selectedEquipment}
                                    onChange={(e: React.ChangeEvent<HTMLSelectElement>) => updateFilter('selectedEquipment', e.target.value)}
                                    className="px-3 py-2 border border-gray-300 rounded-lg bg-white text-gray-900 focus:border-blue-500 focus:outline-none text-sm min-w-[120px]"
                                >
                                    <option value="all">All Equipment</option>
                                    <option value="No Equipment">🏠 No Equipment</option>
                                    <option value="Dumbbells">🏋️ Dumbbells</option>
                                    <option value="Yoga Mat">🧘 Yoga Mat</option>
                                    <option value="Jump Rope">🪢 Jump Rope</option>
                                    <option value="Foam Roller">🔄 Foam Roller</option>
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

                    {/* Enhanced Exercises Grid */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
                        {filteredAndSortedExercises.map((exercise, index) => (
                            <EnhancedExerciseCard
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

                    {/* Enhanced No Results State */}
                    {filteredAndSortedExercises.length === 0 && (
                        <div className="text-center py-12 px-4">
                            <div className="text-gray-400 text-5xl sm:text-6xl mb-4">🔍</div>
                            <h3 className="text-xl font-semibold text-gray-900 mb-2">
                                No exercises found
                            </h3>
                            <p className="text-gray-600 mb-6 max-w-md mx-auto">
                                Try adjusting your filters or search terms. Make sure to explore different tracking modes!
                            </p>
                            <div className="space-y-3">
                                <button
                                    onClick={clearFilters}
                                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                                >
                                    Clear all filters
                                </button>
                                <div className="flex justify-center gap-2 text-sm">
                                    <button
                                        onClick={() => updateFilter('trackingType', 'cardio')}
                                        className="px-3 py-1 bg-red-100 text-red-700 rounded-md hover:bg-red-200"
                                    >
                                        ❤️ Try Cardio
                                    </button>
                                    <button
                                        onClick={() => updateFilter('trackingType', 'isometric')}
                                        className="px-3 py-1 bg-purple-100 text-purple-700 rounded-md hover:bg-purple-200"
                                    >
                                        🛡️ Try Isometric
                                    </button>
                                    <button
                                        onClick={() => updateFilter('trackingType', 'strength')}
                                        className="px-3 py-1 bg-blue-100 text-blue-700 rounded-md hover:bg-blue-200"
                                    >
                                        💪 Try Strength
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Enhanced Mobile Filter Drawer */}
            <MobileFilterDrawer
                isOpen={showMobileFilters}
                onClose={() => setShowMobileFilters(false)}
                filters={filters}
                exerciseTypeOptions={exerciseTypeOptions}
                onFilterChange={updateFilter}
            />

            {/* Enhanced: Workout Tracking Interface */}
            {showWorkoutInterface && selectedExerciseForTracking && workoutConfiguration && (
                <WorkoutTrackingInterface
                    exercise={selectedExerciseForTracking}
                    configuration={workoutConfiguration}
                    onComplete={handleWorkoutComplete}
                    onCancel={handleWorkoutCancel}
                />
            )}
        </div>
    );
};

export default ExercisesPage;