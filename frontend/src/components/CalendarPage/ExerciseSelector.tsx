import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { Exercise } from '../../types/exercise';
import { WorkoutPlanInfo } from '../../types/api';
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
    TrophyIcon
} from '@heroicons/react/24/outline';
import { exerciseApi } from '../../services/exerciseApi';
import { workoutPlanApi } from '../../services/workoutPlanApi';

// ==================== INTERFACES ====================

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

interface WorkoutPlanCategory {
    id: string;
    name: string;
    emoji: string;
    count: number;
    description: string;
}

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
    const { user } = useAuth();

    // ==================== STATE MANAGEMENT ====================

    // Exercise state
    const [searchTerm, setSearchTerm] = useState('');
    const [exercises, setExercises] = useState<Exercise[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [categories, setCategories] = useState<ExerciseCategory[]>([]);
    const [popularExercises, setPopularExercises] = useState<Exercise[]>([]);

    // Enhanced workout plan state
    const [workoutPlans, setWorkoutPlans] = useState<WorkoutPlanInfo[]>([]);
    const [freePlans, setFreePlans] = useState<WorkoutPlanInfo[]>([]);
    const [featuredPlans, setFeaturedPlans] = useState<WorkoutPlanInfo[]>([]);
    const [workoutPlanCategories, setWorkoutPlanCategories] = useState<WorkoutPlanCategory[]>([]);
    const [selectedPlanCategory, setSelectedPlanCategory] = useState<string>('all');

    // UI state
    const [selectedTab, setSelectedTab] = useState(initialTab);
    const [hasInitialized, setHasInitialized] = useState(false);
    const [planView, setPlanView] = useState<'grid' | 'list'>('grid');

    // Refs to prevent duplicate API calls
    const searchTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);
    const lastSearchTermRef = useRef<string>('');
    const isLoadingRef = useRef<boolean>(false);

    // ==================== COMPUTED VALUES ====================

    // Check user subscription level
    const userTier = user?.subscriptionTier || 'FREE';
    const canAccessPaidPlans = userTier === 'PLUS' || userTier === 'PRO';

    // Enhanced tab definitions with workout plan focus
    const tabs = [
        { id: 0, name: 'Exercises', icon: FireIcon, description: 'Individual exercises' },
        { id: 1, name: 'Workout Plans', icon: UserGroupIcon, description: 'Complete routines', highlight: true },
        { id: 2, name: 'Categories', icon: Bars3Icon, description: 'Browse by goal' },
        { id: 3, name: 'Popular', icon: StarIcon, description: 'Trending choices' },
    ];

    // Enhanced workout plan categories
    const enhancedWorkoutPlanCategories: WorkoutPlanCategory[] = [
        { id: 'all', name: 'All Plans', emoji: '🎯', count: workoutPlans.length, description: 'All available workout plans' },
        { id: 'strength', name: 'Strength', emoji: '💪', count: 0, description: 'Build muscle and power' },
        { id: 'cardio', name: 'Cardio', emoji: '❤️', count: 0, description: 'Improve cardiovascular fitness' },
        { id: 'hiit', name: 'HIIT', emoji: '⚡', count: 0, description: 'High-intensity interval training' },
        { id: 'beginner', name: 'Beginner', emoji: '🌱', count: 0, description: 'Perfect for getting started' },
        { id: 'advanced', name: 'Advanced', emoji: '🏆', count: 0, description: 'Challenge your limits' }
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

    // ==================== API FUNCTIONS ====================

    // Fetch exercises function (existing)
    const fetchExercises = useCallback(async (query: string = '', skipLoading: boolean = false) => {
        if (isLoadingRef.current && !skipLoading) {
            console.log('⏳ API call already in progress, skipping duplicate');
            return;
        }

        if (query === lastSearchTermRef.current && exercises.length > 0) {
            console.log('🔄 Same search term, using cached results');
            return;
        }

        isLoadingRef.current = true;
        if (!skipLoading) setLoading(true);
        setError(null);

        try {
            console.log('🔍 Searching exercises with real API:', query);
            const results = query
                ? await exerciseApi.searchExercises(query)
                : await exerciseApi.getPublicExercises();

            setExercises(results);
            lastSearchTermRef.current = query;
            console.log('✅ Real exercises loaded:', results.length);
        } catch (err) {
            console.error('❌ Exercise search failed:', err);
            setError(err instanceof Error ? err.message : 'Failed to load exercises from backend');
        } finally {
            isLoadingRef.current = false;
            if (!skipLoading) setLoading(false);
        }
    }, [exercises.length]);

    // Enhanced workout plan fetching
    const fetchWorkoutPlans = useCallback(async () => {
        if (workoutPlans.length > 0) {
            console.log('📋 Workout plans already loaded, skipping');
            return;
        }

        try {
            console.log('📋 Loading enhanced workout plans from API');
            const data = await workoutPlanApi.getInitialWorkoutPlanData();

            setWorkoutPlans(data.allPlans);
            setFreePlans(data.freePlans);
            setFeaturedPlans(data.popularPlans || data.trendingPlans || []);

            // Enhanced categories with real counts
            const categoryCounts = data.allPlans.reduce((acc, plan) => {
                const category = plan.category?.toLowerCase() || 'other';
                acc[category] = (acc[category] || 0) + 1;
                return acc;
            }, {} as Record<string, number>);

            const updatedCategories = enhancedWorkoutPlanCategories.map(cat => ({
                ...cat,
                count: cat.id === 'all' ? data.allPlans.length : (categoryCounts[cat.id] || 0)
            }));

            setWorkoutPlanCategories(updatedCategories);
            console.log('✅ Enhanced workout plans loaded:', data.allPlans.length);
        } catch (err) {
            console.error('❌ Failed to fetch workout plans:', err);
            setWorkoutPlanCategories(enhancedWorkoutPlanCategories);
        }
    }, [workoutPlans.length]);

    // Fetch categories and popular exercises (existing functions)
    const fetchCategories = useCallback(async () => {
        if (categories.length > 0) return;
        try {
            const goalsData = await exerciseApi.getGoals();
            const categoryList: ExerciseCategory[] = goalsData.map((goal: any) => ({
                id: goal.goal,
                name: formatGoalName(goal.goal),
                emoji: getGoalEmoji(goal.goal),
                count: goal.count
            }));
            setCategories(categoryList);
        } catch (err) {
            console.error('❌ Failed to fetch categories:', err);
        }
    }, [categories.length]);

    const fetchPopularExercises = useCallback(async () => {
        if (popularExercises.length > 0) return;
        try {
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
        } catch (err) {
            console.error('❌ Failed to fetch popular exercises:', err);
        }
    }, [popularExercises.length]);

    // ==================== HELPER FUNCTIONS ====================

    const formatGoalName = (goal: string): string => {
        const goalMap: Record<string, string> = {
            'fat-burn': 'Fat Burn',
            'muscle-building': 'Muscle Building',
            'endurance': 'Endurance',
            'strength': 'Strength',
            'flexibility': 'Flexibility',
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
        };
        return emojiMap[goal.toLowerCase()] || '🎯';
    };

    // ==================== EVENT HANDLERS ====================

    const handleClearSearch = () => {
        setSearchTerm('');
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
        try {
            setLoading(true);
            const results = await exerciseApi.searchExercises(categoryId);
            setExercises(results);
            setSelectedTab(0);
            lastSearchTermRef.current = categoryId;
        } catch (err) {
            console.error('❌ Category filter failed:', err);
            setError('Failed to filter by category');
        } finally {
            setLoading(false);
        }
    };

    const handlePlanCategoryFilter = (categoryId: string) => {
        setSelectedPlanCategory(categoryId);
    };

    // ==================== FILTERED DATA ====================

    const filteredExercises = useMemo(() => {
        return exercises.filter(exercise => {
            const matchesSearch = !searchTerm ||
                exercise.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                exercise.description?.toLowerCase().includes(searchTerm.toLowerCase());
            return matchesSearch;
        });
    }, [exercises, searchTerm]);

    const filteredWorkoutPlans = useMemo(() => {
        let filtered = workoutPlans.filter(plan => {
            if (!plan.name) return false;
            const matchesSearch = !searchTerm ||
                plan.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                (plan.description && plan.description.toLowerCase().includes(searchTerm.toLowerCase()));
            return matchesSearch;
        });

        // Apply category filter
        if (selectedPlanCategory !== 'all') {
            filtered = filtered.filter(plan => {
                const planCategory = plan.category?.toLowerCase() || 'other';
                return planCategory.includes(selectedPlanCategory) ||
                    plan.difficulty?.toLowerCase() === selectedPlanCategory ||
                    (selectedPlanCategory === 'hiit' && plan.workoutType?.toLowerCase().includes('hiit'));
            });
        }

        return filtered;
    }, [workoutPlans, searchTerm, selectedPlanCategory]);

    // ==================== EFFECTS ====================

    useEffect(() => {
        if (open) {
            setSelectedTab(initialTab);
        }
    }, [open, initialTab]);

    useEffect(() => {
        if (open && !hasInitialized) {
            const initializeData = async () => {
                console.log('🚀 Initializing Enhanced ExerciseSelector...');
                await Promise.all([
                    fetchExercises('', true),
                    fetchCategories(),
                    fetchPopularExercises(),
                    fetchWorkoutPlans()
                ]);
                setHasInitialized(true);
            };
            initializeData();
        }
    }, [open, hasInitialized, fetchExercises, fetchCategories, fetchPopularExercises, fetchWorkoutPlans]);

    useEffect(() => {
        if (!hasInitialized) return;

        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }

        searchTimeoutRef.current = setTimeout(() => {
            if (searchTerm !== lastSearchTermRef.current) {
                if (selectedTab === 0) {
                    fetchExercises(searchTerm);
                }
            }
        }, 300);

        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, [searchTerm, hasInitialized, fetchExercises, selectedTab]);

    useEffect(() => {
        if (!open) {
            setSearchTerm('');
            setSelectedTab(0);
            setError(null);
            setHasInitialized(false);
            setSelectedPlanCategory('all');
            lastSearchTermRef.current = '';
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        }
    }, [open]);

    // ==================== RENDER FUNCTIONS ====================

    const renderTabContent = () => {
        switch (selectedTab) {
            case 0: // Exercises (existing implementation)
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
                        ) : filteredExercises.length > 0 ? (
                            <div className="space-y-2">
                                {filteredExercises.map((exercise) => (
                                    <ExerciseCard
                                        key={exercise.id}
                                        exercise={exercise}
                                        onSelect={() => handleExerciseSelect(exercise)}
                                        onDragStart={() => onDragStart?.(exercise)}
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
                                    {searchTerm ? 'Try a different search term' : 'Loading exercises...'}
                                </p>
                            </div>
                        )}
                    </div>
                );

            case 1: // Enhanced Workout Plans
                return (
                    <div className="space-y-4">
                        {/* Category Filter Pills */}
                        <div className="flex gap-2 pb-4 border-b border-gray-200 overflow-x-auto">
                            {workoutPlanCategories.map((category) => (
                                <button
                                    key={category.id}
                                    onClick={() => handlePlanCategoryFilter(category.id)}
                                    className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
                                        selectedPlanCategory === category.id
                                            ? 'bg-purple-100 text-purple-700 border-2 border-purple-300'
                                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                    }`}
                                >
                                    <span>{category.emoji}</span>
                                    <span>{category.name}</span>
                                    <span className="bg-white px-2 py-0.5 rounded-full text-xs">
                                        {category.count}
                                    </span>
                                </button>
                            ))}
                        </div>

                        {/* View Toggle */}
                        <div className="flex items-center justify-between">
                            <p className="text-sm text-gray-600">
                                {filteredWorkoutPlans.length} plan{filteredWorkoutPlans.length !== 1 ? 's' : ''} found
                            </p>
                            <div className="flex bg-gray-100 rounded-lg p-1">
                                <button
                                    onClick={() => setPlanView('grid')}
                                    className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                                        planView === 'grid'
                                            ? 'bg-white text-gray-900 shadow-sm'
                                            : 'text-gray-600'
                                    }`}
                                >
                                    Grid
                                </button>
                                <button
                                    onClick={() => setPlanView('list')}
                                    className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                                        planView === 'list'
                                            ? 'bg-white text-gray-900 shadow-sm'
                                            : 'text-gray-600'
                                    }`}
                                >
                                    List
                                </button>
                            </div>
                        </div>

                        {/* Workout Plans Display */}
                        {loading ? (
                            <div className={`grid gap-4 ${planView === 'grid' ? 'grid-cols-1 md:grid-cols-2' : 'grid-cols-1'}`}>
                                {[1, 2, 3, 4].map((i) => (
                                    <div key={i} className="animate-pulse">
                                        <div className="bg-gradient-to-r from-gray-200 to-gray-300 h-32 rounded-2xl"></div>
                                    </div>
                                ))}
                            </div>
                        ) : filteredWorkoutPlans.length > 0 ? (
                            <div className={`grid gap-4 ${planView === 'grid' ? 'grid-cols-1 md:grid-cols-2' : 'grid-cols-1'}`}>
                                {filteredWorkoutPlans.map((plan) => (
                                    <WorkoutPlanCard
                                        key={plan.id}
                                        plan={plan}
                                        onSelect={() => handleWorkoutPlanSelect(plan)}
                                        canAccess={canAccessPlan(plan)}
                                        userTier={userTier}
                                        disabled={!canAddToTargetDate()}
                                        viewMode={planView}
                                    />
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-12">
                                <div className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-purple-100 to-pink-100 rounded-full flex items-center justify-center">
                                    <UserGroupIcon className="w-10 h-10 text-gray-400" />
                                </div>
                                <h3 className="text-xl font-bold text-gray-900 mb-2">
                                    {searchTerm ? 'No workout plans found' : 'No workout plans available'}
                                </h3>
                                <p className="text-gray-500 max-w-sm mx-auto">
                                    {searchTerm ? 'Try a different search term' : 'Loading workout plans...'}
                                </p>
                            </div>
                        )}
                    </div>
                );

            case 2: // Categories (existing implementation)
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

            case 3: // Popular (existing implementation)
                return (
                    <div className="space-y-2">
                        {popularExercises.length > 0 ? (
                            popularExercises.map((exercise) => (
                                <ExerciseCard
                                    key={exercise.id}
                                    exercise={exercise}
                                    onSelect={() => handleExerciseSelect(exercise)}
                                    onDragStart={() => onDragStart?.(exercise)}
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

    // ==================== MAIN RENDER ====================

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
                                    <CalendarIcon className="w-4 h-4 text-white" />
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

                        {/* Subscription Info */}
                        <div className="flex items-center justify-center mt-2">
                            <div className="inline-flex items-center px-3 py-1 rounded-xl bg-white/20 backdrop-blur-sm border border-white/30">
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
                            <MagnifyingGlassIcon className="h-4 w-4 text-gray-400" />
                        </div>
                        <input
                            type="text"
                            placeholder={`Search ${selectedTab === 1 ? 'workout plans' : 'exercises'}...`}
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
                        {selectedTab === 1 ? 'Try "HIIT", "strength", "beginner"' : 'Try "cardio", "isometric", "planks"'}
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
                        {tabs.map((tab) => {
                            const IconComponent = tab.icon;
                            const isActive = selectedTab === tab.id;

                            let count = 0;
                            if (tab.id === 0) count = filteredExercises.length;
                            else if (tab.id === 1) count = filteredWorkoutPlans.length;
                            else if (tab.id === 2) count = categories.length;
                            else if (tab.id === 3) count = popularExercises.length;

                            return (
                                <button
                                    key={tab.id}
                                    onClick={() => setSelectedTab(tab.id)}
                                    className={`flex items-center px-3 py-2 rounded-lg font-medium text-sm whitespace-nowrap transition-all duration-200 flex-shrink-0 relative ${
                                        isActive
                                            ? tab.id === 1
                                                ? 'bg-purple-100 text-purple-700 shadow-sm'
                                                : 'bg-blue-100 text-blue-700 shadow-sm'
                                            : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                                    }`}
                                >
                                    <IconComponent className="w-4 h-4 mr-1.5" />
                                    {tab.name} ({count})
                                    {tab.highlight && (
                                        <span className="absolute -top-1 -right-1 w-2 h-2 bg-purple-500 rounded-full animate-pulse"></span>
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
                            {selectedTab === 0 && (
                                <>
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
                                </>
                            )}
                            {selectedTab === 1 && (
                                <>
                                    <div className="flex items-center">
                                        <CheckCircleIcon className="w-3 h-3 mr-1 text-green-500" />
                                        Free Plans
                                    </div>
                                    <div className="flex items-center">
                                        <StarIcon className="w-3 h-3 mr-1 text-yellow-500" />
                                        Premium
                                    </div>
                                    <div className="flex items-center">
                                        <LockClosedIcon className="w-3 h-3 mr-1 text-gray-500" />
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

// ==================== EXERCISE CARD COMPONENT ====================

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

    const getDifficultyColor = (difficulty: string | undefined) => {
        const difficultyLevel = (difficulty || 'INTERMEDIATE').toLowerCase();
        switch (difficultyLevel) {
            case 'beginner': return 'bg-green-100 text-green-700 border-green-200';
            case 'intermediate': return 'bg-yellow-100 text-yellow-700 border-yellow-200';
            case 'advanced': return 'bg-red-100 text-red-700 border-red-200';
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

                    <div className="flex flex-wrap gap-2 mb-3">
                        {getWorkoutTrackingBadge()}
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getDifficultyColor(exercise.difficultyLevel)}`}>
                            {exercise.difficultyLevel || 'INTERMEDIATE'}
                        </span>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3 text-xs text-gray-500">
                            {exercise.estimatedDurationMinutes && (
                                <div className="flex items-center">
                                    <ClockIcon className="w-3 h-3 mr-1" />
                                    {exercise.estimatedDurationMinutes}min
                                </div>
                            )}
                            {exercise.averageRating && exercise.averageRating > 0 && (
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

// ==================== ENHANCED WORKOUT PLAN CARD COMPONENT ====================

interface WorkoutPlanCardProps {
    plan: WorkoutPlanInfo;
    onSelect: () => void;
    canAccess: boolean;
    userTier: string;
    disabled?: boolean;
    viewMode?: 'grid' | 'list';
}

const WorkoutPlanCard: React.FC<WorkoutPlanCardProps> = ({
                                                             plan,
                                                             onSelect,
                                                             canAccess,
                                                             userTier,
                                                             disabled = false,
                                                             viewMode = 'grid'
                                                         }) => {
    const isLocked = !canAccess;
    const isDisabled = disabled || isLocked;

    const getDifficultyColor = (difficulty: string | undefined) => {
        const difficultyLevel = (difficulty || 'INTERMEDIATE').toLowerCase();
        switch (difficultyLevel) {
            case 'beginner': return 'bg-green-100 text-green-700 border-green-200';
            case 'intermediate': return 'bg-yellow-100 text-yellow-700 border-yellow-200';
            case 'advanced': return 'bg-red-100 text-red-700 border-red-200';
            default: return 'bg-gray-100 text-gray-700 border-gray-200';
        }
    };

    const getTierColor = (tier: string | undefined) => {
        const tierLevel = tier || 'FREE';
        switch (tierLevel) {
            case 'FREE': return 'bg-green-100 text-green-700 border-green-200';
            case 'PLUS': return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'PRO': return 'bg-purple-100 text-purple-700 border-purple-200';
            default: return 'bg-gray-100 text-gray-700 border-gray-200';
        }
    };

    const getCardLayout = () => {
        if (viewMode === 'list') {
            return 'flex items-center p-4 space-x-4';
        }
        return 'flex flex-col p-6 space-y-4';
    };

    return (
        <div
            className={`
                group bg-white rounded-2xl border border-gray-200 transition-all duration-300
                ${isDisabled
                ? 'opacity-50 cursor-not-allowed'
                : 'hover:shadow-lg hover:border-purple-300 cursor-pointer active:scale-[0.98] hover:-translate-y-1'
            }
                ${isLocked ? 'bg-gray-50' : ''}
            `}
            onClick={isDisabled ? undefined : onSelect}
        >
            <div className={getCardLayout()}>
                {/* Plan Icon/Image */}
                <div className={`
                    flex-shrink-0 rounded-xl flex items-center justify-center text-2xl font-bold
                    ${viewMode === 'list' ? 'w-16 h-16' : 'w-20 h-20 mx-auto'}
                    ${isLocked ? 'bg-gray-200 text-gray-500' : 'bg-gradient-to-br from-purple-100 to-blue-100 text-purple-600'}
                `}>
                    {isLocked ? <LockClosedIcon className="w-8 h-8" /> : '📋'}
                </div>

                {/* Plan Content */}
                <div className={`${viewMode === 'list' ? 'flex-1' : ''}`}>
                    <div className={`${viewMode === 'list' ? 'flex items-start justify-between' : 'text-center'}`}>
                        <div className={`${viewMode === 'list' ? 'flex-1' : ''}`}>
                            <h3 className={`font-bold text-gray-900 group-hover:text-purple-900 transition-colors
                                ${viewMode === 'list' ? 'text-lg mb-1' : 'text-xl mb-2'}
                            `}>
                                {plan.name || plan.workoutName || 'Unnamed Workout Plan'}
                            </h3>

                            {plan.description && (
                                <p className={`text-gray-600 group-hover:text-gray-700 transition-colors
                                    ${viewMode === 'list' ? 'text-sm line-clamp-2' : 'text-sm mb-4 line-clamp-3'}
                                `}>
                                    {viewMode === 'list' && plan.description.length > 100
                                        ? `${plan.description.substring(0, 100)}...`
                                        : plan.description
                                    }
                                </p>
                            )}
                        </div>

                        {/* Quick Stats for List View */}
                        {viewMode === 'list' && (
                            <div className="flex items-center space-x-4 text-sm text-gray-500 ml-4">
                                <div className="flex items-center">
                                    <ClockIcon className="w-4 h-4 mr-1" />
                                    {plan.estimatedDurationMinutes || 0}min
                                </div>
                                <div className="flex items-center">
                                    <UserGroupIcon className="w-4 h-4 mr-1" />
                                    {plan.exerciseCount || 0}
                                </div>
                                {plan.averageRating > 0 && (
                                    <div className="flex items-center">
                                        <StarIcon className="w-4 h-4 mr-1 text-yellow-500" />
                                        {plan.averageRating.toFixed(1)}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Plan Tags */}
                    <div className={`flex flex-wrap gap-2 ${viewMode === 'list' ? 'mt-2' : 'mb-4'}`}>
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getTierColor(plan.subscriptionTierRequired)}`}>
                            {plan.subscriptionTierRequired || 'FREE'}
                        </span>
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getDifficultyColor(plan.difficulty)}`}>
                            {plan.difficulty || plan.difficultyLevel || 'INTERMEDIATE'}
                        </span>
                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-700 border border-gray-200">
                            <UserGroupIcon className="w-3 h-3 mr-1" />
                            {plan.exerciseCount || 0} exercises
                        </span>
                    </div>

                    {/* Grid View Stats */}
                    {viewMode === 'grid' && (
                        <div className="flex items-center justify-center space-x-4 text-sm text-gray-500">
                            <div className="flex items-center">
                                <ClockIcon className="w-4 h-4 mr-1" />
                                {plan.estimatedDurationMinutes || 0}min
                            </div>
                            {plan.averageRating && plan.averageRating > 0 && (
                                <div className="flex items-center">
                                    <StarIcon className="w-4 h-4 mr-1 text-yellow-500" />
                                    {plan.averageRating.toFixed(1)}
                                </div>
                            )}
                        </div>
                    )}

                    {/* Action Hint */}
                    <div className={`${viewMode === 'list' ? 'text-right mt-2' : 'text-center mt-4'}`}>
                        {isLocked ? (
                            <span className="text-xs font-medium text-gray-500">
                                Upgrade to access →
                            </span>
                        ) : (
                            <div className="flex items-center justify-center gap-2">
                                <CogIcon className="w-4 h-4 text-purple-600" />
                                <span className="text-xs font-medium text-gray-900 group-hover:text-purple-600 transition-colors">
                                    Configure & Schedule →
                                </span>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ExerciseSelector;