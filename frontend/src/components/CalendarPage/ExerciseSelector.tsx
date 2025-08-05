// src/components/CalendarPage/ExerciseSelector.tsx - Enhanced Mobile-First Design
import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { Exercise } from '../../types/exercise';
import {
    MagnifyingGlassIcon,
    XMarkIcon,
    Bars3Icon,
    StarIcon,
    ClockIcon,
    ExclamationTriangleIcon,
    ChevronLeftIcon,
    ChevronRightIcon,
    SparklesIcon,
    FireIcon,
    HeartIcon
} from '@heroicons/react/24/outline';
import { exerciseApi } from '../../services/exerciseApi';

interface ExerciseCategory {
    id: string;
    name: string;
    emoji: string;
    count: number;
}

interface CalendarDay {
    dateString: string;
    date: Date;
    isToday: boolean;
    exercises: any[];
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
        return true;
    };

    const canNavigateNext = () => {
        const currentIndex = getCurrentDateIndex();
        if (currentIndex < 0 || currentIndex >= calendarDays.length - 1) return false;
        return true;
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

    const canAddToTargetDate = () => {
        if (!selectedDate) return true;
        const targetDay = calendarDays.find(day => day.dateString === selectedDate);
        if (!targetDay) return true;
        if (user?.userType === 'REGULAR' && !user?.isProfessional) {
            return targetDay.exercises.length < 4;
        }
        return true;
    };

    // API calls
    const fetchExercises = useCallback(async (query: string = '') => {
        setLoading(true);
        setError(null);
        try {
            console.log('🔍 Searching exercises with real API:', query);
            const results = query
                ? await exerciseApi.searchExercises(query)
                : await exerciseApi.getPublicExercises();
            setExercises(results);
            console.log('✅ Real exercises loaded:', results.length);
        } catch (err) {
            console.error('❌ Exercise search failed:', err);
            setError(err instanceof Error ? err.message : 'Failed to load exercises from backend');
        } finally {
            setLoading(false);
        }
    }, []);

    const fetchCategories = useCallback(async () => {
        try {
            console.log('🎯 Loading exercise goals from real API');
            const goalsData = await exerciseApi.getGoals();
            const categoryList: ExerciseCategory[] = goalsData.map((goal: any) => ({
                id: goal.goal,
                name: formatGoalName(goal.goal),
                emoji: getGoalEmoji(goal.goal),
                count: goal.count
            }));
            setCategories(categoryList);
            console.log('✅ Real categories loaded:', categoryList.length);
        } catch (err) {
            console.error('❌ Failed to fetch categories from backend:', err);
        }
    }, []);

    const fetchPopularExercises = useCallback(async () => {
        try {
            console.log('⭐ Loading popular exercises from real API');
            const allExercises = await exerciseApi.getPublicExercises();
            const popular = allExercises
                .filter(ex => ex.isPopular || ex.usageCount > 100)
                .sort((a, b) => {
                    const aScore = (a.usageCount * 0.7) + (a.averageRating * 30);
                    const bScore = (b.usageCount * 0.7) + (b.averageRating * 30);
                    return bScore - aScore;
                })
                .slice(0, 10);
            setPopularExercises(popular);
            console.log('✅ Real popular exercises loaded:', popular.length);
        } catch (err) {
            console.error('❌ Failed to fetch popular exercises from backend:', err);
        }
    }, []);

    // Effects
    useEffect(() => {
        if (open) {
            fetchExercises();
            fetchCategories();
            fetchPopularExercises();
        }
    }, [open, fetchExercises, fetchCategories, fetchPopularExercises]);

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
            'strength': 'Strength',
            'flexibility': 'Flexibility',
            'weight-loss': 'Weight Loss',
            'general-fitness': 'General Fitness',
            'sport-specific': 'Sport-Specific',
            'recovery': 'Recovery'
        };
        return goalMap[goal.toLowerCase()] || goal.replace('-', ' ').replace(/\b\w/g, l => l.toUpperCase());
    };

    const getGoalEmoji = (goal: string): string => {
        const emojiMap: Record<string, string> = {
            'fat-burn': '🔥',
            'muscle-building': '💪',
            'endurance': '🏃‍♂️',
            'strength': '🏋️‍♀️',
            'flexibility': '🤸‍♀️',
            'weight-loss': '⚖️',
            'general-fitness': '✨',
            'sport-specific': '🎯',
            'recovery': '🛡️'
        };
        return emojiMap[goal.toLowerCase()] || '🎯';
    };

    const handleClearSearch = () => {
        setSearchTerm('');
        fetchExercises();
    };

    const handleDragStart = (exercise: Exercise) => {
        if (onDragStart) {
            onDragStart(exercise);
        }
    };

    const handleCategorySelect = async (categoryId: string) => {
        try {
            setLoading(true);
            console.log('🎯 Filtering by category:', categoryId);
            const results = await exerciseApi.searchExercises(categoryId);
            setExercises(results);
            setSelectedTab(0);
        } catch (err) {
            console.error('❌ Category filter failed:', err);
            setError('Failed to filter by category');
        } finally {
            setLoading(false);
        }
    };

    // Tab content renderer
    const renderTabContent = () => {
        switch (selectedTab) {
            case 0:
                return (
                    <div className="space-y-2">
                        {loading ? (
                            <div className="space-y-3">
                                {[1, 2, 3, 4].map((i) => (
                                    <div key={i} className="animate-pulse">
                                        <div className="bg-gradient-to-r from-gray-200 to-gray-300 h-24 rounded-2xl"></div>
                                    </div>
                                ))}
                            </div>
                        ) : exercises.length > 0 ? (
                            <div className="space-y-2">
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
                            <div className="text-center py-12">
                                <div className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-blue-100 to-purple-100 rounded-full flex items-center justify-center">
                                    <MagnifyingGlassIcon className="w-10 h-10 text-gray-400" />
                                </div>
                                <h3 className="text-xl font-bold text-gray-900 mb-2">
                                    {searchTerm ? 'No exercises found' : 'No exercises available'}
                                </h3>
                                <p className="text-gray-500 max-w-sm mx-auto">
                                    {searchTerm ? 'Try a different search term or check your backend connection' : 'Loading exercises from backend...'}
                                </p>
                            </div>
                        )}
                    </div>
                );

            case 1:
                return (
                    <div className="grid grid-cols-1 gap-3">
                        {categories.map((category) => (
                            <div
                                key={category.id}
                                className="group bg-gradient-to-r from-white to-gray-50 hover:from-blue-50 hover:to-purple-50 rounded-2xl border border-gray-200 hover:border-blue-300 p-4 hover:shadow-lg transition-all duration-300 cursor-pointer active:scale-[0.98]"
                                onClick={() => handleCategorySelect(category.id)}
                            >
                                <div className="flex items-center">
                                    <div className="w-12 h-12 bg-gradient-to-br from-blue-100 to-purple-100 rounded-xl flex items-center justify-center text-2xl mr-4 group-hover:scale-110 transition-transform duration-300">
                                        {category.emoji}
                                    </div>
                                    <div className="flex-1">
                                        <h3 className="font-bold text-gray-900 text-lg group-hover:text-blue-900 transition-colors">
                                            {category.name}
                                        </h3>
                                        <p className="text-sm text-gray-500 group-hover:text-blue-600 transition-colors">
                                            {category.count} exercises available
                                        </p>
                                    </div>
                                    <ChevronRightIcon className="w-5 h-5 text-gray-400 group-hover:text-blue-600 group-hover:translate-x-1 transition-all duration-300" />
                                </div>
                            </div>
                        ))}
                    </div>
                );

            case 2:
                return (
                    <div className="space-y-2">
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
                            <div className="text-center py-12">
                                <div className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-yellow-100 to-orange-100 rounded-full flex items-center justify-center">
                                    <StarIcon className="w-10 h-10 text-yellow-500" />
                                </div>
                                <h3 className="text-xl font-bold text-gray-900 mb-2">
                                    {loading ? 'Loading popular exercises...' : 'No popular exercises found'}
                                </h3>
                                <p className="text-gray-500 max-w-sm mx-auto">
                                    {loading ? 'Fetching from backend...' : 'Popular exercises will appear here when available'}
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
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-2 sm:p-4">
            <div className="bg-white rounded-3xl max-w-4xl w-full max-h-[95vh] sm:max-h-[90vh] flex flex-col shadow-2xl border border-gray-200">
                {/* Enhanced Header */}
                <div className="relative bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 rounded-t-3xl p-4 sm:p-6">
                    <div className="absolute inset-0 bg-white/10 rounded-t-3xl backdrop-blur-sm"></div>
                    <div className="relative z-10">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-xl sm:text-2xl font-bold text-white truncate">{title}</h2>
                            <button
                                onClick={onClose}
                                className="p-2 hover:bg-white/20 rounded-xl transition-colors active:scale-95 text-white"
                            >
                                <XMarkIcon className="w-6 h-6" />
                            </button>
                        </div>

                        {/* Enhanced Date Navigation */}
                        {selectedDate && dateInfo && (
                            <div className="flex items-center justify-center space-x-3 mb-4">
                                <button
                                    onClick={navigateToPreviousDay}
                                    disabled={!canNavigatePrevious()}
                                    className={`p-2 rounded-xl transition-all ${
                                        canNavigatePrevious()
                                            ? 'text-white hover:bg-white/20 active:scale-95'
                                            : 'text-white/40 cursor-not-allowed'
                                    }`}
                                >
                                    <ChevronLeftIcon className="w-5 h-5" />
                                </button>

                                <div className="flex items-center space-x-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-xl border border-white/30">
                                    <span className="text-sm font-medium text-white">
                                        <span className="sm:hidden">{dateInfo.date}</span>
                                        <span className="hidden sm:inline">{dateInfo.dayName}, {dateInfo.date}</span>
                                    </span>
                                    {dateInfo.isToday && (
                                        <span className="px-2 py-1 bg-yellow-400 text-yellow-900 text-xs font-bold rounded-full">
                                            Today
                                        </span>
                                    )}
                                    {dateInfo.exerciseCount > 0 && (
                                        <span className="px-2 py-1 bg-white/30 text-white text-xs font-medium rounded-full">
                                            {dateInfo.exerciseCount}
                                        </span>
                                    )}
                                </div>

                                <button
                                    onClick={navigateToNextDay}
                                    disabled={!canNavigateNext()}
                                    className={`p-2 rounded-xl transition-all ${
                                        canNavigateNext()
                                            ? 'text-white hover:bg-white/20 active:scale-95'
                                            : 'text-white/40 cursor-not-allowed'
                                    }`}
                                >
                                    <ChevronRightIcon className="w-5 h-5" />
                                </button>
                            </div>
                        )}

                        {/* Day Full Warning */}
                        {!canAddToDate && (
                            <div className="flex items-center justify-center">
                                <div className="inline-flex items-center px-3 py-2 rounded-xl bg-yellow-400/20 backdrop-blur-sm border border-yellow-400/30">
                                    <ExclamationTriangleIcon className="w-4 h-4 mr-2 text-yellow-200" />
                                    <span className="text-sm font-medium text-yellow-100">
                                        <span className="sm:hidden">Day Full</span>
                                        <span className="hidden sm:inline">Day Full (Free Limit: 4 exercises)</span>
                                    </span>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Enhanced Search Bar */}
                <div className="px-3 sm:px-4 py-3 bg-gray-50 border-b border-gray-200">
                    <div className="relative w-full max-w-full">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none z-10">
                            <MagnifyingGlassIcon className="h-4 w-4 text-gray-400" />
                        </div>
                        <input
                            type="text"
                            placeholder="Search exercises..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-10 py-2.5 bg-white border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm shadow-sm transition-all duration-200 min-w-0"
                        />
                        {searchTerm && (
                            <button
                                onClick={handleClearSearch}
                                className="absolute inset-y-0 right-0 pr-3 flex items-center active:scale-95 z-10"
                            >
                                <XMarkIcon className="h-4 w-4 text-gray-400 hover:text-gray-600" />
                            </button>
                        )}
                    </div>
                    <p className="text-xs text-gray-500 mt-2 px-1">
                        Try 'cardio', 'isometric', 'planks'
                    </p>
                </div>

                {/* Error Display */}
                {error && (
                    <div className="mx-4 sm:mx-6 mt-4 p-4 bg-red-50 border border-red-200 rounded-2xl">
                        <div className="flex items-center">
                            <ExclamationTriangleIcon className="w-5 h-5 text-red-400 mr-3 flex-shrink-0" />
                            <span className="text-red-700 flex-1">{error}</span>
                            <button
                                onClick={() => setError(null)}
                                className="ml-auto text-red-400 hover:text-red-600 flex-shrink-0 active:scale-95"
                            >
                                <XMarkIcon className="w-5 h-5" />
                            </button>
                        </div>
                        <p className="text-sm text-red-600 mt-2 ml-8">
                            Check that your backend server is running and accessible
                        </p>
                    </div>
                )}

                {/* Enhanced Tabs */}
                <div className="px-3 sm:px-4 bg-white border-b border-gray-200">
                    <nav className="flex space-x-1 overflow-x-auto py-2 scrollbar-hide">
                        <button
                            onClick={() => setSelectedTab(0)}
                            className={`flex items-center px-3 py-2 rounded-lg font-medium text-sm whitespace-nowrap transition-all duration-200 flex-shrink-0 ${
                                selectedTab === 0
                                    ? 'bg-blue-100 text-blue-700 shadow-sm'
                                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                            }`}
                        >
                            <MagnifyingGlassIcon className="w-4 h-4 mr-1.5" />
                            All ({exercises.length})
                        </button>
                        <button
                            onClick={() => setSelectedTab(1)}
                            className={`flex items-center px-3 py-2 rounded-lg font-medium text-sm whitespace-nowrap transition-all duration-200 flex-shrink-0 ${
                                selectedTab === 1
                                    ? 'bg-purple-100 text-purple-700 shadow-sm'
                                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                            }`}
                        >
                            <Bars3Icon className="w-4 h-4 mr-1.5" />
                            Categories ({categories.length})
                        </button>
                        <button
                            onClick={() => setSelectedTab(2)}
                            className={`flex items-center px-3 py-2 rounded-lg font-medium text-sm whitespace-nowrap transition-all duration-200 flex-shrink-0 ${
                                selectedTab === 2
                                    ? 'bg-yellow-100 text-yellow-700 shadow-sm'
                                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                            }`}
                        >
                            <StarIcon className="w-4 h-4 mr-1.5" />
                            Popular ({popularExercises.length})
                        </button>
                    </nav>
                </div>

                {/* Tab Content */}
                <div className="flex-1 overflow-y-auto p-3 sm:p-4 bg-gray-50 min-h-0">
                    {renderTabContent()}
                </div>

                {/* Enhanced Footer */}
                <div className="border-t border-gray-200 bg-white rounded-b-3xl px-3 sm:px-4 py-3">
                    <div className="flex flex-col sm:flex-row items-center justify-between space-y-2 sm:space-y-0">
                        <div className="flex items-center space-x-3 text-xs text-gray-500">
                            <div className="flex items-center">
                                <HeartIcon className="w-3 h-3 mr-1 text-red-500" />
                                Cardio
                            </div>
                            <div className="flex items-center">
                                <SparklesIcon className="w-3 h-3 mr-1 text-purple-500" />
                                Isometric
                            </div>
                            <div className="flex items-center">
                                <FireIcon className="w-3 h-3 mr-1 text-blue-500" />
                                Strength
                            </div>
                        </div>
                        <button
                            onClick={onClose}
                            className="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors active:scale-95 font-medium text-sm"
                        >
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

// Enhanced Exercise Card Component
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
            case 'beginner': return 'bg-green-100 text-green-700 border-green-200';
            case 'intermediate': return 'bg-yellow-100 text-yellow-700 border-yellow-200';
            case 'advanced': return 'bg-red-100 text-red-700 border-red-200';
            default: return 'bg-gray-100 text-gray-700 border-gray-200';
        }
    };

    const getExerciseTypeColor = (type: string) => {
        switch (type.toLowerCase()) {
            case 'strength': return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'cardio': return 'bg-red-100 text-red-700 border-red-200';
            case 'flexibility': return 'bg-green-100 text-green-700 border-green-200';
            default: return 'bg-gray-100 text-gray-700 border-gray-200';
        }
    };

    const getWorkoutTrackingBadge = () => {
        if (exercise.isCardio) {
            return (
                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-700 border border-red-200">
                    <HeartIcon className="w-3 h-3 mr-1" />
                    Cardio
                </span>
            );
        }
        if (exercise.isIsometric) {
            return (
                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-700 border border-purple-200">
                    <SparklesIcon className="w-3 h-3 mr-1" />
                    Hold
                </span>
            );
        }
        return (
            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700 border border-blue-200">
                <FireIcon className="w-3 h-3 mr-1" />
                Reps
            </span>
        );
    };

    return (
        <div
            className={`
                group bg-white rounded-2xl border border-gray-200 p-4 transition-all duration-300
                ${disabled
                ? 'opacity-50 cursor-not-allowed'
                : 'hover:shadow-lg hover:border-blue-300 cursor-pointer active:scale-[0.98] hover:-translate-y-1'
            }
            `}
            draggable={!disabled && !isMobile}
            onDragStart={disabled || isMobile ? undefined : onDragStart}
            onClick={disabled ? undefined : onSelect}
        >
            <div className="flex items-start">
                {!isMobile && (
                    <div className="mr-3 text-gray-400 pt-1 group-hover:text-blue-500 transition-colors">
                        <Bars3Icon className="w-5 h-5" />
                    </div>
                )}

                <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between mb-3">
                        <div className="flex items-center min-w-0 flex-1">
                            <h3 className="font-bold text-gray-900 mr-2 text-base group-hover:text-blue-900 transition-colors truncate">
                                {exercise.emoji || '💪'} {exerciseName}
                            </h3>
                            {showPopularBadge && (
                                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gradient-to-r from-yellow-100 to-orange-100 text-yellow-800 border border-yellow-200 flex-shrink-0">
                                    <StarIcon className="w-3 h-3 mr-1" />
                                    Popular
                                </span>
                            )}
                        </div>
                    </div>

                    {exercise.description && (
                        <p className="text-sm text-gray-600 mb-3 line-clamp-2 group-hover:text-gray-700 transition-colors">
                            {exercise.description.length > 80
                                ? `${exercise.description.substring(0, 80)}...`
                                : exercise.description
                            }
                        </p>
                    )}

                    {/* Enhanced Exercise Tags */}
                    <div className="flex flex-wrap gap-2 mb-3">
                        {getWorkoutTrackingBadge()}
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getExerciseTypeColor(exercise.exerciseType)}`}>
                            {exercise.exerciseType}
                        </span>
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getDifficultyColor(exercise.difficultyLevel)}`}>
                            {exercise.difficultyLevel}
                        </span>
                    </div>

                    {/* Enhanced Stats Row */}
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3 text-xs text-gray-500">
                            {exercise.estimatedDurationMinutes && (
                                <div className="flex items-center">
                                    <ClockIcon className="w-3 h-3 mr-1" />
                                    {exercise.estimatedDurationMinutes}min
                                </div>
                            )}
                            {exercise.averageRating && (
                                <div className="flex items-center">
                                    <StarIcon className="w-3 h-3 mr-1 text-yellow-500" />
                                    {exercise.averageRating.toFixed(1)}
                                </div>
                            )}
                        </div>
                        <div className="text-right">
                            <span className="text-xs font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
                                Tap to add →
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ExerciseSelector;