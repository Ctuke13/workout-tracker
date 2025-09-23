import React, {useState, useEffect, useCallback, useMemo, useRef} from 'react';
import {useAuth} from '../../contexts/AuthContext';
import {Exercise, Goal, CategoryWithDescription} from '../../types/exercise';
import ExerciseCard from '../cards/ExerciseCard';
import WorkoutPlanCard from '../cards/WorkoutPlanCard';
import WorkoutPlanGrid from '../tabs/WorkoutPlanGrid';
import ExerciseGrid from '../tabs/ExerciseGrid';
import FavoritesGrid from '../tabs/FavoritesGrid';
import CategoryGrid from '../tabs/CategoryGrid';
import PopularGrid from '../tabs/PopularGrid';
import {WorkoutPlanInfo} from '../../types/api';
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
    HeartIcon,
    UserGroupIcon,
    PlayIcon,
    LockClosedIcon,
    CheckCircleIcon,
    CalendarIcon,
    CogIcon,
    BoltIcon,
    TrophyIcon,
    StarIcon as StarIconSolid
} from '@heroicons/react/24/outline';
import {CalendarDay} from '../../types/calendar';
import {useExerciseSelector} from '../../hooks/useExerciseSelector';

// ==================== INTERFACES ====================

interface EnhancedExerciseSelectorProps {
    open: boolean;
    onClose: () => void;
    onExerciseSelect: (exercise: Exercise) => void;
    onWorkoutPlanSelect: (workoutPlan: WorkoutPlanInfo) => void;
    onWorkoutPlanConfigure: (workoutPlan: WorkoutPlanInfo) => void; // NEW: Configuration callback
    onDragStart?: (exercise: Exercise) => void;
    selectedDate?: string | null;
    canAddToSelectedDate?: boolean;
    title?: string;
    calendarDays?: CalendarDay[];
    onDateChange?: (date: string) => void;
    initialTab?: number;
}

// ==================== MAIN COMPONENT ====================

const ExerciseSelector: React.FC<EnhancedExerciseSelectorProps> = ({
                                                                       open,
                                                                       onClose,
                                                                       onExerciseSelect,
                                                                       onWorkoutPlanSelect,
                                                                       onWorkoutPlanConfigure,
                                                                       onDragStart,
                                                                       selectedDate,
                                                                       canAddToSelectedDate = true,
                                                                       title = "Choose Exercise or Workout Plan",
                                                                       calendarDays = [],
                                                                       onDateChange,
                                                                       initialTab = 0
                                                                   }) => {
    const {user} = useAuth();

    // ==================== STATE MANAGEMENT ====================

    const selectorData = useExerciseSelector();

    // ==================== COMPUTED VALUES ====================

    // Check user subscription level
    const userTier = user?.subscriptionTier || 'FREE';
    const canAccessPaidPlans = userTier === 'PLUS' || userTier === 'PRO';

    // Enhanced tab definitions with workout plan focus
    const tabs = [
        {id: 0, name: 'Exercises', icon: FireIcon, description: 'Individual exercises'},
        {id: 1, name: 'Favorites', icon: StarIcon, description: 'Your saved exercises', highlight: true},
        {id: 2, name: 'Workout Plans', icon: UserGroupIcon, description: 'Complete routines', highlight: true},
        {id: 3, name: 'Categories', icon: Bars3Icon, description: 'Browse by goal'},
        {id: 4, name: 'Popular', icon: TrophyIcon, description: 'Trending choices'},
    ];

    // ==================== DATE NAVIGATION FUNCTIONS ====================

    const getCurrentDateIndex = () => {
        if (!selectedDate || calendarDays.length === 0) return -1;
        return calendarDays.findIndex(day => day.dateString === selectedDate);
    };

    const canNavigatePrevious = () => {
        const currentIndex = getCurrentDateIndex();
        return currentIndex > 0;
    };

    const canNavigateNext = () => {
        const currentIndex = getCurrentDateIndex();
        return currentIndex >= 0 && currentIndex < calendarDays.length - 1;
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
            dayName: selectedDay.date.toLocaleDateString('en-US', {weekday: 'long'}),
            date: selectedDay.date.toLocaleDateString('en-US', {month: 'short', day: 'numeric'}),
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

    // ==================== EVENT HANDLERS ====================

    const handleClearSearch = () => {
        selectorData.handleClearSearch();
    };

    const handleExerciseSelect = (exercise: Exercise) => {
        onExerciseSelect(exercise);
        onClose();
    };

    const handleWorkoutPlanSelect = (plan: WorkoutPlanInfo) => {
        if (onWorkoutPlanConfigure) {
            onWorkoutPlanConfigure(plan);
        } else {
            onWorkoutPlanSelect(plan);
        }
        onClose();
    };

    const canAccessPlan = (plan: WorkoutPlanInfo): boolean => {
        const requiredTier = plan.subscriptionTierRequired || 'FREE';
        if (requiredTier === 'FREE') return true;
        return canAccessPaidPlans;
    };

    const handleCategorySelect = async (categoryId: string) => {
        selectorData.handleCategorySelect(categoryId);
    };

    const handlePlanCategoryFilter = (categoryId: string) => {
        selectorData.handlePlanCategoryFilter(categoryId);
    };

    // ==================== FILTERED DATA ====================

    const filteredExercises = useMemo(() => {
        return selectorData.exercises.filter(exercise => {
            const matchesSearch = !selectorData.searchTerm ||
                exercise.name.toLowerCase().includes(selectorData.searchTerm.toLowerCase()) ||
                exercise.description?.toLowerCase().includes(selectorData.searchTerm.toLowerCase());
            return matchesSearch;
        });
    }, [selectorData.exercises, selectorData.searchTerm]);

    const filteredWorkoutPlans = useMemo(() => {
        let filtered = selectorData.workoutPlans.filter(plan => {
            if (!plan.name) return false;
            const matchesSearch = !selectorData.searchTerm ||
                plan.name.toLowerCase().includes(selectorData.searchTerm.toLowerCase()) ||
                (plan.description && plan.description.toLowerCase().includes(selectorData.searchTerm.toLowerCase()));
            return matchesSearch;
        });

        // Apply category filter
        if (selectorData.selectedPlanCategory !== 'all') {
            filtered = filtered.filter(plan => {
                const planCategory = plan.category?.toLowerCase() || 'other';
                return planCategory.includes(selectorData.selectedPlanCategory) ||
                    plan.difficulty?.toLowerCase() === selectorData.selectedPlanCategory ||
                    (selectorData.selectedPlanCategory === 'hiit' && plan.workoutType?.toLowerCase().includes('hiit'));
            });
        }

        return filtered;
    }, [selectorData.workoutPlans, selectorData.searchTerm, selectorData.selectedPlanCategory]);

    // ==================== EFFECTS ====================

    useEffect(() => {
        if (open) {
            selectorData.setSelectedTab(initialTab);
        }
    }, [open, initialTab, selectorData.setSelectedTab]);

    useEffect(() => {
        if (open && !selectorData.hasInitialized) {
            selectorData.initializeData();
        }
    }, [open, selectorData.hasInitialized, selectorData.initializeData]);

    useEffect(() => {
        if (!open) {
            selectorData.resetState();
        }
    }, [open, selectorData.resetState]);


// Update your filteredExercises to include favorite status
    const filteredExercisesWithFavorites = useMemo(() => {
        return filteredExercises.map(exercise => ({
            ...exercise,
            isFavorite: selectorData.userFavoriteIds.has(exercise.id)
        }));
    }, [filteredExercises, selectorData.userFavoriteIds]);

// Add filtered favorites
    const filteredFavoriteExercises = useMemo(() => {
        return selectorData.favoriteExercises
            .map(exercise => ({
                ...exercise,
                isFavorite: true // 🌟 ENSURE this is always true in favorites tab
            }))
            .filter(exercise => {
                const matchesSearch = !selectorData.searchTerm ||
                    exercise.name.toLowerCase().includes(selectorData.searchTerm.toLowerCase()) ||
                    exercise.description?.toLowerCase().includes(selectorData.searchTerm.toLowerCase());
                return matchesSearch;
            });
    }, [selectorData.favoriteExercises, selectorData.searchTerm]);

// Update your useEffect for initialization to include favorites

    // ==================== RENDER FUNCTIONS ====================

    const renderTabContent = () => {
        switch (selectorData.selectedTab) {
            case 0: // Exercises Tab
                return (
                    <ExerciseGrid
                        loading={selectorData.loading}
                        filteredExercisesWithFavorites={filteredExercisesWithFavorites}
                        searchTerm={selectorData.searchTerm}
                        canAddToTargetDate={canAddToTargetDate}
                        user={user}
                        onExerciseSelect={handleExerciseSelect}
                        onDragStart={onDragStart}
                        onToggleFavorite={selectorData.handleToggleFavorite}
                    />
                );

            case 1: // Favorite Tab
                return (
                    <FavoritesGrid
                        user={user}
                        loadingFavorites={selectorData.loadingFavorites}
                        filteredFavoriteExercises={filteredFavoriteExercises}
                        searchTerm={selectorData.searchTerm}
                        canAddToTargetDate={canAddToTargetDate}
                        onExerciseSelect={handleExerciseSelect}
                        onDragStart={onDragStart}
                        onToggleFavorite={selectorData.handleToggleFavorite}
                        onSwitchToExercisesTab={() => selectorData.setSelectedTab(0)}
                    />
                );

            case 2: // Workout Plans (moved to index 2)
                return (
                    <WorkoutPlanGrid
                        loading={selectorData.loading}
                        filteredWorkoutPlans={filteredWorkoutPlans}
                        searchTerm={selectorData.searchTerm}
                        workoutPlanCategories={selectorData.workoutPlanCategories}
                        selectedPlanCategory={selectorData.selectedPlanCategory}
                        planView={selectorData.planView}
                        userTier={userTier}
                        canAddToTargetDate={canAddToTargetDate}
                        onPlanCategoryFilter={handlePlanCategoryFilter}
                        onPlanViewChange={selectorData.setPlanView}
                        onWorkoutPlanSelect={handleWorkoutPlanSelect}
                        canAccessPlan={canAccessPlan}
                    />
                );

            case 3: // Categories
                return (
                    <CategoryGrid
                        categories={selectorData.categories}
                        onCategorySelect={handleCategorySelect}
                    />
                );

            case 4: // Popular
                return (
                    <PopularGrid
                        loading={selectorData.loading}
                        popularExercises={selectorData.popularExercises}
                        userFavoriteIds={selectorData.userFavoriteIds}
                        canAddToTargetDate={canAddToTargetDate}
                        user={user}
                        onExerciseSelect={handleExerciseSelect}
                        onDragStart={onDragStart}
                        onToggleFavorite={selectorData.handleToggleFavorite}
                    />
                );

            default:
                return null;
        }
    };

    // ==================== MAIN RENDER ====================

    if (!open) return null;

    const dateInfo = getSelectedDateInfo();
    const canAddToDate = canAddToTargetDate();

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-2 sm:p-4">
            <div
                className="bg-white rounded-3xl max-w-4xl w-full max-h-[95vh] sm:max-h-[90vh] flex flex-col shadow-2xl border border-gray-200">
                {/* Enhanced Header */}
                <div
                    className="relative bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 rounded-t-3xl p-4 sm:p-6">
                    <div className="absolute inset-0 bg-white/10 rounded-t-3xl backdrop-blur-sm"></div>
                    <div className="relative z-10">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-xl sm:text-2xl font-bold text-white truncate">{title}</h2>
                            <button
                                onClick={onClose}
                                className="p-2 hover:bg-white/20 rounded-xl transition-colors active:scale-95 text-white"
                            >
                                <XMarkIcon className="w-6 h-6"/>
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
                                    <ChevronLeftIcon className="w-5 h-5"/>
                                </button>

                                <div
                                    className="flex items-center space-x-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-xl border border-white/30">
                                    <CalendarIcon className="w-4 h-4 text-white"/>
                                    <span className="text-sm font-medium text-white">
                                        <span className="sm:hidden">{dateInfo.date}</span>
                                        <span className="hidden sm:inline">{dateInfo.dayName}, {dateInfo.date}</span>
                                    </span>
                                    {dateInfo.isToday && (
                                        <span
                                            className="px-2 py-1 bg-yellow-400 text-yellow-900 text-xs font-bold rounded-full">
                                            Today
                                        </span>
                                    )}
                                    {dateInfo.exerciseCount > 0 && (
                                        <span
                                            className="px-2 py-1 bg-white/30 text-white text-xs font-medium rounded-full">
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
                                    <ChevronRightIcon className="w-5 h-5"/>
                                </button>
                            </div>
                        )}

                        {/* Day Full Warning */}
                        {!canAddToDate && (
                            <div className="flex items-center justify-center">
                                <div
                                    className="inline-flex items-center px-3 py-2 rounded-xl bg-yellow-400/20 backdrop-blur-sm border border-yellow-400/30">
                                    <ExclamationTriangleIcon className="w-4 h-4 mr-2 text-yellow-200"/>
                                    <span className="text-sm font-medium text-yellow-100">
                                        <span className="sm:hidden">Day Full</span>
                                        <span className="hidden sm:inline">Day Full (Free Limit: 4 exercises)</span>
                                    </span>
                                </div>
                            </div>
                        )}

                        {/* Subscription Info */}
                        <div className="flex items-center justify-center mt-2">
                            <div
                                className="inline-flex items-center px-3 py-1 rounded-xl bg-white/20 backdrop-blur-sm border border-white/30">
                                <span className={`w-2 h-2 rounded-full mr-2 ${
                                    canAccessPaidPlans ? 'bg-green-400' : 'bg-yellow-400'
                                }`}></span>
                                <span className="text-xs font-medium text-white">
                                    {userTier} • {canAccessPaidPlans ? 'Full Access' : 'Free Plans Only'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Enhanced Search Bar */}
                <div className="px-3 sm:px-4 py-3 bg-gray-50 border-b border-gray-200">
                    <div className="relative w-full max-w-full">
                        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none z-10">
                            <MagnifyingGlassIcon className="h-4 w-4 text-gray-400"/>
                        </div>
                        <input
                            type="text"
                            placeholder={`Search ${selectorData.selectedTab === 2 ? 'workout plans' : 'exercises'}...`}
                            value={selectorData.searchTerm}
                            onChange={(e) => selectorData.setSearchTerm(e.target.value)}
                            className="w-full pl-10 pr-10 py-2.5 bg-white border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm shadow-sm transition-all duration-200 min-w-0"
                        />
                        {selectorData.searchTerm && (
                            <button
                                onClick={handleClearSearch}
                                className="absolute inset-y-0 right-0 pr-3 flex items-center active:scale-95 z-10"
                            >
                                <XMarkIcon className="h-4 w-4 text-gray-400 hover:text-gray-600"/>
                            </button>
                        )}
                    </div>
                    <p className="text-xs text-gray-500 mt-2 px-1">
                        {selectorData.selectedTab === 2 ? 'Try "HIIT", "strength", "beginner"' : 'Try "cardio", "isometric", "planks"'}
                    </p>
                </div>

                {/* Error Display */}
                {selectorData.error && (
                    <div className="mx-4 sm:mx-6 mt-4 p-4 bg-red-50 border border-red-200 rounded-2xl">
                        <div className="flex items-center">
                            <ExclamationTriangleIcon className="w-5 h-5 text-red-400 mr-3 flex-shrink-0"/>
                            <span className="text-red-700 flex-1">{selectorData.error}</span>
                            <button
                                onClick={() => {
                                }}
                                className="ml-auto text-red-400 hover:text-red-600 flex-shrink-0 active:scale-95"
                            >
                                <XMarkIcon className="w-5 h-5"/>
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
                        {tabs.map((tab) => {
                            const IconComponent = tab.icon;
                            const isActive = selectorData.selectedTab === tab.id;

                            let count = 0;
                            if (tab.id === 0) count = filteredExercises.length;
                            else if (tab.id === 1) count = filteredFavoriteExercises.length;
                            else if (tab.id === 2) count = filteredWorkoutPlans.length;
                            else if (tab.id === 3) count = selectorData.categories.length;
                            else if (tab.id === 4) count = selectorData.popularExercises.length;

                            return (
                                <button
                                    key={tab.id}
                                    onClick={() => selectorData.setSelectedTab(tab.id)}
                                    className={`flex items-center px-3 py-2 rounded-lg font-medium text-sm whitespace-nowrap transition-all duration-200 flex-shrink-0 relative ${
                                        isActive
                                            ? tab.id === 1
                                                ? 'bg-purple-100 text-purple-700 shadow-sm'
                                                : 'bg-blue-100 text-blue-700 shadow-sm'
                                            : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                                    }`}
                                >
                                    <IconComponent className="w-4 h-4 mr-1.5"/>
                                    {tab.name} ({count})
                                    {tab.highlight && (
                                        <span
                                            className="absolute -top-1 -right-1 w-2 h-2 bg-purple-500 rounded-full animate-pulse"></span>
                                    )}
                                </button>
                            );
                        })}
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
                            {selectorData.selectedTab === 0 && (
                                <>
                                    <div className="flex items-center">
                                        <HeartIcon className="w-3 h-3 mr-1 text-red-500"/>
                                        Cardio
                                    </div>
                                    <div className="flex items-center">
                                        <SparklesIcon className="w-3 h-3 mr-1 text-purple-500"/>
                                        Isometric
                                    </div>
                                    <div className="flex items-center">
                                        <FireIcon className="w-3 h-3 mr-1 text-blue-500"/>
                                        Strength
                                    </div>
                                </>
                            )}
                            {selectorData.selectedTab === 1 && (
                                <>
                                    <div className="flex items-center">
                                        <StarIcon className="w-3 h-3 mr-1 text-yellow-500"/>
                                        Your Favorites
                                    </div>
                                </>
                            )}
                            {selectorData.selectedTab === 2 && (
                                <>
                                    <div className="flex items-center">
                                        <CheckCircleIcon className="w-3 h-3 mr-1 text-green-500"/>
                                        Free Plans
                                    </div>
                                    <div className="flex items-center">
                                        <StarIcon className="w-3 h-3 mr-1 text-yellow-500"/>
                                        Premium
                                    </div>
                                    <div className="flex items-center">
                                        <LockClosedIcon className="w-3 h-3 mr-1 text-gray-500"/>
                                        {canAccessPaidPlans ? 'Unlocked' : 'Upgrade for More'}
                                    </div>
                                </>
                            )}
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

export default ExerciseSelector;