// src/components/CalendarPage/ExerciseSelector.tsx - Mobile-First with Compact Search
import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import {
    MagnifyingGlassIcon,
    XMarkIcon,
    Bars3Icon,
    StarIcon,
    ClockIcon,
    ExclamationTriangleIcon,
    ChevronLeftIcon,
    ChevronRightIcon
} from '@heroicons/react/24/outline';

// Import mock data services
import { calendarMockApi, CalendarDay } from '../../services/calendarMockData';

// Types based on your backend
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
    createdByProfessional?: boolean;
}

interface ExerciseCategory {
    id: string;
    name: string;
    emoji: string;
    count: number;
}

interface ExerciseSelectorProps {
    open: boolean;
    onClose: () => void;
    onExerciseSelect: (exercise: Exercise) => void;
    onDragStart?: (exercise: Exercise) => void;
    selectedDate?: string | null;
    canAddToSelectedDate?: boolean;
    title?: string;
    calendarDays?: CalendarDay[];
    onDateChange?: (date: string) => void;
}

const ExerciseSelector: React.FC<ExerciseSelectorProps> = ({
                                                               open,
                                                               onClose,
                                                               onExerciseSelect,
                                                               onDragStart,
                                                               selectedDate,
                                                               canAddToSelectedDate = true,
                                                               title = "Choose Exercise",
                                                               calendarDays = [],
                                                               onDateChange
                                                           }) => {
    const { user } = useAuth();

    // State management
    const [searchTerm, setSearchTerm] = useState('');
    const [exercises, setExercises] = useState<Exercise[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [selectedTab, setSelectedTab] = useState(0);
    const [categories, setCategories] = useState<ExerciseCategory[]>([]);
    const [popularExercises, setPopularExercises] = useState<Exercise[]>([]);

    // Date navigation functions
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

    // Check if target date can accept exercises
    const canAddToTargetDate = () => {
        if (!selectedDate) return true;

        const targetDay = calendarDays.find(day => day.dateString === selectedDate);
        if (!targetDay) return true;

        // Free tier limit: 4 exercises per day
        if (user?.userType === 'REGULAR' && !user?.isProfessional) {
            return targetDay.exercises.length < 4;
        }

        return true;
    };

    // Fetch exercises using mock API
    const fetchExercises = useCallback(async (query: string = '') => {
        setLoading(true);
        setError(null);

        try {
            console.log('🔍 Searching exercises:', query);

            // Use mock API to search exercises
            const results = await calendarMockApi.searchExercises(query, {
                // Add any filters here if needed
            });

            setExercises(results);
            console.log('✅ Exercises loaded:', results.length);

        } catch (err) {
            console.error('❌ Exercise search failed:', err);
            setError(err instanceof Error ? err.message : 'Failed to load exercises');
        } finally {
            setLoading(false);
        }
    }, []);

    // Fetch exercise categories/goals using mock API
    const fetchCategories = useCallback(async () => {
        try {
            console.log('🎯 Loading exercise goals');

            const goalsData = await calendarMockApi.getExerciseGoals();
            const categoryList: ExerciseCategory[] = goalsData.map((goal: any) => ({
                id: goal.goal,
                name: formatGoalName(goal.goal),
                emoji: getGoalEmoji(goal.goal),
                count: goal.count
            }));

            setCategories(categoryList);
            console.log('✅ Categories loaded:', categoryList.length);

        } catch (err) {
            console.error('❌ Failed to fetch categories:', err);
        }
    }, []);

    // Fetch popular exercises using mock API
    const fetchPopularExercises = useCallback(async () => {
        try {
            console.log('⭐ Loading popular exercises');

            const popular = await calendarMockApi.getPopularExercises(10);
            setPopularExercises(popular);
            console.log('✅ Popular exercises loaded:', popular.length);

        } catch (err) {
            console.error('❌ Failed to fetch popular exercises:', err);
        }
    }, []);

    // Initial data loading
    useEffect(() => {
        if (open) {
            fetchExercises();
            fetchCategories();
            fetchPopularExercises();
        }
    }, [open, fetchExercises, fetchCategories, fetchPopularExercises]);

    // Debounced search
    useEffect(() => {
        if (!open) return;

        const timeoutId = setTimeout(() => {
            fetchExercises(searchTerm);
        }, 300);

        return () => clearTimeout(timeoutId);
    }, [searchTerm, fetchExercises, open]);

    // Helper functions
    const formatGoalName = (goal: string): string => {
        const goalMap: Record<string, string> = {
            'fat-burn': 'Fat Burn',
            'muscle-building': 'Muscle Building',
            'endurance': 'Endurance',
            'flexibility': 'Flexibility',
            'sport-specific': 'Sport-Specific',
            'recovery': 'Recovery'
        };
        return goalMap[goal.toLowerCase()] || goal;
    };

    const getGoalEmoji = (goal: string): string => {
        const emojiMap: Record<string, string> = {
            'fat-burn': '🔥',
            'muscle-building': '💪',
            'endurance': '⚡',
            'flexibility': '🧘‍♀️',
            'sport-specific': '🎯',
            'recovery': '🛡️'
        };
        return emojiMap[goal.toLowerCase()] || '🎯';
    };

    // Handle search clear
    const handleClearSearch = () => {
        setSearchTerm('');
        fetchExercises();
    };

    // Handle drag start with proper typing
    const handleDragStart = (exercise: Exercise) => {
        if (onDragStart) {
            onDragStart(exercise);
        }
    };

    // Tab content renderer
    const renderTabContent = () => {
        switch (selectedTab) {
            case 0: // All/Search Results
                return (
                    <div className="space-y-3 sm:space-y-4">
                        {loading ? (
                            <div className="space-y-3 sm:space-y-4">
                                {[1, 2, 3, 4].map((i) => (
                                    <div key={i} className="animate-pulse">
                                        <div className="bg-gray-200 h-24 sm:h-32 rounded-lg"></div>
                                    </div>
                                ))}
                            </div>
                        ) : exercises.length > 0 ? (
                            <div className="space-y-3 sm:space-y-4">
                                {exercises.map((exercise) => (
                                    <ExerciseCard
                                        key={exercise.id}
                                        exercise={exercise}
                                        onSelect={() => onExerciseSelect(exercise)}
                                        onDragStart={() => handleDragStart(exercise)}
                                        disabled={!canAddToTargetDate()}
                                    />
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-8 sm:py-12">
                                <div className="text-gray-400 text-3xl sm:text-4xl mb-3 sm:mb-4">🔍</div>
                                <h3 className="text-base sm:text-lg font-medium text-gray-900 mb-2">
                                    {searchTerm ? 'No exercises found' : 'No exercises available'}
                                </h3>
                                <p className="text-sm sm:text-base text-gray-500">
                                    {searchTerm ? 'Try a different search term' : 'Loading exercises...'}
                                </p>
                            </div>
                        )}
                    </div>
                );

            case 1: // Categories
                return (
                    <div className="space-y-2 sm:space-y-3">
                        {categories.map((category) => (
                            <div key={category.id} className="bg-white rounded-lg border border-gray-200 p-3 sm:p-4 hover:shadow-md transition-shadow cursor-pointer active:scale-[0.98]">
                                <div className="flex items-center">
                                    <div className="text-2xl sm:text-3xl mr-3 sm:mr-4">
                                        {category.emoji}
                                    </div>
                                    <div className="flex-1">
                                        <h3 className="font-semibold text-gray-900 text-sm sm:text-base">
                                            {category.name}
                                        </h3>
                                        <p className="text-xs sm:text-sm text-gray-500">
                                            {category.count} exercises
                                        </p>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                );

            case 2: // Popular
                return (
                    <div className="space-y-3 sm:space-y-4">
                        {popularExercises.length > 0 ? (
                            popularExercises.map((exercise) => (
                                <ExerciseCard
                                    key={exercise.id}
                                    exercise={exercise}
                                    onSelect={() => onExerciseSelect(exercise)}
                                    onDragStart={() => handleDragStart(exercise)}
                                    disabled={!canAddToTargetDate()}
                                    showPopularBadge
                                />
                            ))
                        ) : (
                            <div className="text-center py-8 sm:py-12">
                                <div className="text-gray-400 text-3xl sm:text-4xl mb-3 sm:mb-4">⭐</div>
                                <h3 className="text-base sm:text-lg font-medium text-gray-900 mb-2">
                                    Loading popular exercises...
                                </h3>
                                <p className="text-sm sm:text-base text-gray-500">
                                    Popular exercises will appear here
                                </p>
                            </div>
                        )}
                    </div>
                );

            default:
                return null;
        }
    };

    if (!open) return null;

    const dateInfo = getSelectedDateInfo();
    const canAddToDate = canAddToTargetDate();

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-2 sm:p-4">
            <div className="bg-white rounded-xl max-w-4xl w-full max-h-[95vh] sm:max-h-[90vh] flex flex-col">
                {/* Header - Mobile Optimized */}
                <div className="flex items-center justify-between p-3 sm:p-4 md:p-6 border-b border-gray-200">
                    <div className="flex-1 min-w-0">
                        <h2 className="text-lg sm:text-xl font-semibold text-gray-900 truncate">{title}</h2>

                        {/* Date Navigation - Enhanced with better visual feedback */}
                        {selectedDate && dateInfo && (
                            <div className="mt-2 flex items-center space-x-2 sm:space-x-3">
                                <button
                                    onClick={navigateToPreviousDay}
                                    disabled={!canNavigatePrevious()}
                                    className={`p-1.5 rounded-md transition-all ${
                                        canNavigatePrevious()
                                            ? 'text-gray-700 hover:text-gray-900 hover:bg-gray-100 active:scale-95 border border-gray-200 hover:border-gray-300'
                                            : 'text-gray-300 cursor-not-allowed border border-gray-100'
                                    }`}
                                    title={canNavigatePrevious() ? "Previous day" : "No previous day"}
                                >
                                    <ChevronLeftIcon className="w-4 h-4" />
                                </button>

                                <div className="flex items-center space-x-1 sm:space-x-2 min-w-0 px-2 py-1 bg-gray-50 rounded-md border">
                                    <span className="text-xs sm:text-sm font-medium text-gray-900 truncate">
                                        <span className="sm:hidden">{dateInfo.date}</span>
                                        <span className="hidden sm:inline">{dateInfo.dayName}, {dateInfo.date}</span>
                                    </span>
                                    {dateInfo.isToday && (
                                        <span className="px-1.5 py-0.5 bg-blue-100 text-blue-800 text-xs font-medium rounded-full">
                                            Today
                                        </span>
                                    )}
                                    {dateInfo.exerciseCount > 0 && (
                                        <span className="px-1.5 py-0.5 bg-gray-100 text-gray-700 text-xs font-medium rounded-full">
                                            {dateInfo.exerciseCount}
                                        </span>
                                    )}
                                </div>

                                <button
                                    onClick={navigateToNextDay}
                                    disabled={!canNavigateNext()}
                                    className={`p-1.5 rounded-md transition-all ${
                                        canNavigateNext()
                                            ? 'text-gray-700 hover:text-gray-900 hover:bg-gray-100 active:scale-95 border border-gray-200 hover:border-gray-300'
                                            : 'text-gray-300 cursor-not-allowed border border-gray-100'
                                    }`}
                                    title={canNavigateNext() ? "Next day" : "No next day"}
                                >
                                    <ChevronRightIcon className="w-4 h-4" />
                                </button>
                            </div>
                        )}

                        {/* Day Full Warning - Compact */}
                        {!canAddToDate && (
                            <div className="mt-2">
                                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                                    <ExclamationTriangleIcon className="w-3 h-3 mr-1" />
                                    <span className="sm:hidden">Day Full</span>
                                    <span className="hidden sm:inline">Day Full (Free Limit: 4 exercises)</span>
                                </span>
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

                {/* Fixed Search Bar - Proper Container */}
                <div className="px-3 sm:px-4 md:px-6 py-3 sm:py-4 border-b border-gray-200">
                    <div className="relative max-w-full">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                            <MagnifyingGlassIcon className="h-4 w-4 sm:h-5 sm:w-5 text-gray-400" />
                        </div>
                        <input
                            type="text"
                            placeholder="Search exercises..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="block w-full pl-9 sm:pl-10 pr-8 sm:pr-10 py-2 sm:py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm sm:text-base"
                        />
                        {searchTerm && (
                            <button
                                onClick={handleClearSearch}
                                className="absolute inset-y-0 right-0 pr-3 flex items-center active:scale-95"
                            >
                                <XMarkIcon className="h-4 w-4 sm:h-5 sm:w-5 text-gray-400 hover:text-gray-600" />
                            </button>
                        )}
                    </div>
                </div>

                {/* Error Display - Compact */}
                {error && (
                    <div className="mx-3 sm:mx-4 mt-3 sm:mt-4 p-3 sm:p-4 bg-red-50 border border-red-200 rounded-lg">
                        <div className="flex items-center">
                            <ExclamationTriangleIcon className="w-4 h-4 sm:w-5 sm:h-5 text-red-400 mr-2 flex-shrink-0" />
                            <span className="text-red-700 flex-1 text-sm sm:text-base">{error}</span>
                            <button
                                onClick={() => setError(null)}
                                className="ml-auto text-red-400 hover:text-red-600 flex-shrink-0 active:scale-95"
                            >
                                <XMarkIcon className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                )}

                {/* Compact Tabs - Mobile optimized */}
                <div className="px-3 sm:px-4 border-b border-gray-200">
                    <nav className="flex space-x-4 sm:space-x-8 overflow-x-auto">
                        <button
                            onClick={() => setSelectedTab(0)}
                            className={`py-3 px-1 border-b-2 font-medium text-sm whitespace-nowrap transition-colors ${
                                selectedTab === 0
                                    ? 'border-blue-500 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            }`}
                        >
                            <div className="flex items-center">
                                <MagnifyingGlassIcon className="w-4 h-4 mr-1 sm:mr-2" />
                                All
                            </div>
                        </button>
                        <button
                            onClick={() => setSelectedTab(1)}
                            className={`py-3 px-1 border-b-2 font-medium text-sm whitespace-nowrap transition-colors ${
                                selectedTab === 1
                                    ? 'border-blue-500 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            }`}
                        >
                            <div className="flex items-center">
                                <Bars3Icon className="w-4 h-4 mr-1 sm:mr-2" />
                                Categories
                            </div>
                        </button>
                        <button
                            onClick={() => setSelectedTab(2)}
                            className={`py-3 px-1 border-b-2 font-medium text-sm whitespace-nowrap transition-colors ${
                                selectedTab === 2
                                    ? 'border-blue-500 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            }`}
                        >
                            <div className="flex items-center">
                                <StarIcon className="w-4 h-4 mr-1 sm:mr-2" />
                                Popular
                            </div>
                        </button>
                    </nav>
                </div>

                {/* Tab Content - Mobile optimized */}
                <div className="flex-1 overflow-y-auto p-3 sm:p-4 md:p-6">
                    {renderTabContent()}
                </div>

                {/* Compact Footer - Mobile optimized */}
                <div className="border-t border-gray-200 px-3 sm:px-4 md:px-6 py-3 sm:py-4">
                    <div className="flex flex-col sm:flex-row items-center justify-between space-y-2 sm:space-y-0">
                        <p className="text-xs sm:text-sm text-gray-500 text-center sm:text-left">
                            💡 Tip: Tap exercises to configure sets, reps, and weight
                        </p>
                        <button
                            onClick={onClose}
                            className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors active:scale-95 text-sm"
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

// Mobile-Optimized Exercise Card Component
interface ExerciseCardProps {
    exercise: Exercise;
    onSelect: () => void;
    onDragStart?: () => void;
    disabled?: boolean;
    showPopularBadge?: boolean;
}

const ExerciseCard: React.FC<ExerciseCardProps> = ({
                                                       exercise,
                                                       onSelect,
                                                       onDragStart,
                                                       disabled = false,
                                                       showPopularBadge = false
                                                   }) => {
    const exerciseName = exercise.exerciseName || exercise.name || 'Unknown Exercise';
    const isMobile = window.innerWidth < 768;

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

    return (
        <div
            className={`
                bg-white rounded-lg border border-gray-200 p-3 sm:p-4 transition-all duration-200
                ${disabled
                ? 'opacity-50 cursor-not-allowed'
                : 'hover:shadow-md cursor-pointer active:scale-[0.98]'
            }
            `}
            draggable={!disabled && !isMobile}
            onDragStart={disabled || isMobile ? undefined : onDragStart}
            onClick={disabled ? undefined : onSelect}
        >
            <div className="flex items-start">
                {/* Desktop Drag Handle - Hidden on mobile */}
                {!isMobile && (
                    <div className="mr-2 sm:mr-3 text-gray-400 pt-1">
                        <Bars3Icon className="w-4 h-4 sm:w-5 sm:h-5" />
                    </div>
                )}

                {/* Exercise Info */}
                <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between mb-2">
                        <div className="flex items-center min-w-0 flex-1">
                            <h3 className="font-semibold text-gray-900 mr-2 text-sm sm:text-base truncate">
                                {exercise.emoji || '💪'} {exerciseName}
                            </h3>
                            {showPopularBadge && (
                                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 flex-shrink-0">
                                    <StarIcon className="w-3 h-3 mr-1" />
                                    Popular
                                </span>
                            )}
                        </div>
                    </div>

                    {exercise.description && (
                        <p className="text-xs sm:text-sm text-gray-600 mb-2 sm:mb-3 line-clamp-2">
                            {exercise.description.length > 80
                                ? `${exercise.description.substring(0, 80)}...`
                                : exercise.description
                            }
                        </p>
                    )}

                    {/* Exercise Tags - Compact for mobile */}
                    <div className="flex flex-wrap gap-1 sm:gap-2">
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
                </div>
            </div>
        </div>
    );
};

export default ExerciseSelector;