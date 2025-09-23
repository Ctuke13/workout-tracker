import {useState, useEffect, useCallback, useMemo, useRef} from 'react';
import {useAuth} from '../contexts/AuthContext';
import {Exercise, ExerciseConfiguration, DEFAULT_STRENGTH_CONFIG, getDefaultConfigForExercise} from '../types/exercise';
import {WorkoutPlanInfo} from '../types/api';
import {exerciseApi} from '../services/exerciseApi';
import {workoutPlanApi} from '../services/workoutPlanApi';
import {Goal, CategoryWithDescription} from '../types/exercise';

export const useExerciseSelector = () => {
    const {user} = useAuth();

    // Exercise state
    const [searchTerm, setSearchTerm] = useState('');
    const [exercises, setExercises] = useState<Exercise[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [categories, setCategories] = useState<Goal[]>([]);
    const [popularExercises, setPopularExercises] = useState<Exercise[]>([]);

    // Enhanced workout plan state
    const [workoutPlans, setWorkoutPlans] = useState<WorkoutPlanInfo[]>([]);
    const [freePlans, setFreePlans] = useState<WorkoutPlanInfo[]>([]);
    const [featuredPlans, setFeaturedPlans] = useState<WorkoutPlanInfo[]>([]);
    const [workoutPlanCategories, setWorkoutPlanCategories] = useState<CategoryWithDescription[]>([]);
    const [selectedPlanCategory, setSelectedPlanCategory] = useState<string>('all');

    // UI state
    const [selectedTab, setSelectedTab] = useState(0);
    const [hasInitialized, setHasInitialized] = useState(false);
    const [planView, setPlanView] = useState<'grid' | 'list'>('grid');

    // In-workout specific state
    const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
    const [showConfig, setShowConfig] = useState(false);
    const [exerciseConfig, setExerciseConfig] = useState<ExerciseConfiguration | null>(null);
    const [addingExercise, setAddingExercise] = useState(false);
    const [recentExercises, setRecentExercises] = useState<Exercise[]>([]);
    const [selectedCategory, setSelectedCategory] = useState('all');

    // Favorite state
    const [favoriteExercises, setFavoriteExercises] = useState<Exercise[]>([]);
    const [userFavoriteIds, setUserFavoriteIds] = useState<Set<number>>(new Set());
    const [loadingFavorites, setLoadingFavorites] = useState(false);

    // Refs to prevent duplicate API calls
    const searchTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);
    const lastSearchTermRef = useRef<string>('');
    const isLoadingRef = useRef<boolean>(false);

    // Check user subscription level
    const userTier = user?.subscriptionTier || 'FREE';
    const canAccessPaidPlans = userTier === 'PLUS' || userTier === 'PRO';

    // Enhanced tab definitions
    const tabs = [
        {id: 0, name: 'Exercises', icon: 'FireIcon', description: 'Individual exercises'},
        {id: 1, name: 'Favorites', icon: 'StarIcon', description: 'Your saved exercises', highlight: true},
        {id: 2, name: 'Workout Plans', icon: 'UserGroupIcon', description: 'Complete routines', highlight: true},
        {id: 3, name: 'Categories', icon: 'Bars3Icon', description: 'Browse by goal'},
        {id: 4, name: 'Popular', icon: 'TrophyIcon', description: 'Trending choices'},
    ];

    const QUICK_CATEGORIES = [
        {id: 'all', name: 'All', emoji: '🎯'},
        {id: 'STRENGTH', name: 'Strength', emoji: '💪'},
        {id: 'CARDIO', name: 'Cardio', emoji: '❤️'},
        {id: 'FLEXIBILITY', name: 'Flexibility', emoji: '🧘‍♀️'},
        {id: 'CORE', name: 'Core', emoji: '🔥'}
    ];

    // Enhanced workout plan categories
    const enhancedWorkoutPlanCategories: CategoryWithDescription[] = [
        {
            id: 'all',
            name: 'All Plans',
            emoji: '🎯',
            count: workoutPlans.length,
            description: 'All available workout plans'
        },
        {id: 'strength', name: 'Strength', emoji: '💪', count: 0, description: 'Build muscle and power'},
        {id: 'cardio', name: 'Cardio', emoji: '❤️', count: 0, description: 'Improve cardiovascular fitness'},
        {id: 'hiit', name: 'HIIT', emoji: '⚡', count: 0, description: 'High-intensity interval training'},
        {id: 'beginner', name: 'Beginner', emoji: '🌱', count: 0, description: 'Perfect for getting started'},
        {id: 'advanced', name: 'Advanced', emoji: '🏆', count: 0, description: 'Challenge your limits'}
    ];

    // Add these handlers and return them:
    const handleExerciseSelect = (exercise: Exercise) => {
        setSelectedExercise(exercise);
        setExerciseConfig(getDefaultConfigForExercise(exercise)); // Use existing function
        setShowConfig(true);
    };

    const handleCloseConfig = () => {
        setSelectedExercise(null);
        setShowConfig(false);
        setExerciseConfig(null);
        setError(null);
    };

    // Fetch exercises function (following useCalendarActions pattern)
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

    // Fetch categories and popular exercises
    const fetchCategories = useCallback(async () => {
        if (categories.length > 0) return;
        try {
            const goalsData = await exerciseApi.getGoals();
            const categoryList: Goal[] = goalsData.map((goal: any) => ({
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

    // Fetch favorite exercises (following useCalendarActions pattern)
    const fetchFavoriteExercises = useCallback(async () => {
        if (!user || favoriteExercises.length > 0) return;

        try {
            setLoadingFavorites(true);
            console.log('🌟 Loading user favorite exercises');

            const [favorites, favoriteIds] = await Promise.all([
                exerciseApi.getFavoriteExercises(),
                exerciseApi.getFavoriteExerciseIds()
            ]);

            const favoritesWithCorrectFlag = favorites.map(exercise => ({
                ...exercise,
                isFavorite: true
            }));

            setFavoriteExercises(favoritesWithCorrectFlag);
            setUserFavoriteIds(favoriteIds);

            console.log(`✅ Loaded ${favoritesWithCorrectFlag.length} favorite exercises`);
        } catch (err) {
            console.error('❌ Failed to fetch favorite exercises:', err);
        } finally {
            setLoadingFavorites(false);
        }
    }, [user, favoriteExercises.length]);

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

    // Helper functions
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

    // Toggle favorite handler (following useCalendarActions pattern)
    const handleToggleFavorite = async (exercise: Exercise, event?: React.MouseEvent) => {
        if (event) {
            event.stopPropagation();
        }

        if (!user) {
            console.log('User not authenticated, cannot toggle favorite');
            return;
        }

        try {
            console.log(`🌟 Toggling favorite for exercise: ${exercise.name}`);

            // Optimistic update
            const wasFavorited = userFavoriteIds.has(exercise.id);
            const newFavoriteIds = new Set(userFavoriteIds);

            if (wasFavorited) {
                newFavoriteIds.delete(exercise.id);
                setFavoriteExercises(prev => prev.filter(fav => fav.id !== exercise.id));
            } else {
                newFavoriteIds.add(exercise.id);
                setFavoriteExercises(prev => [...prev, exercise]);
            }

            setUserFavoriteIds(newFavoriteIds);

            // Update exercise object
            setExercises(prev => prev.map(ex =>
                ex.id === exercise.id ? {...ex, isFavorite: !wasFavorited} : ex
            ));
            setPopularExercises(prev => prev.map(ex =>
                ex.id === exercise.id ? {...ex, isFavorite: !wasFavorited} : ex
            ));

            // Make API call
            const result = await exerciseApi.toggleFavorite(exercise.id);
            console.log(`✅ ${result.isFavorite ? 'Added to' : 'Removed from'} favorites: ${exercise.name}`);

        } catch (error) {
            console.error('❌ Failed to toggle favorite:', error);

            // Revert optimistic update on error
            const originalFavoriteIds = new Set(userFavoriteIds);
            if (originalFavoriteIds.has(exercise.id)) {
                originalFavoriteIds.delete(exercise.id);
                setFavoriteExercises(prev => prev.filter(fav => fav.id !== exercise.id));
            } else {
                originalFavoriteIds.add(exercise.id);
                setFavoriteExercises(prev => [...prev, exercise]);
            }
            setUserFavoriteIds(originalFavoriteIds);
            exercise.isFavorite = !exercise.isFavorite;
        }
    };

    // Filtered data
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

    const filteredExercisesWithFavorites = useMemo(() => {
        return filteredExercises.map(exercise => ({
            ...exercise,
            isFavorite: userFavoriteIds.has(exercise.id)
        }));
    }, [filteredExercises, userFavoriteIds]);

    const filteredFavoriteExercises = useMemo(() => {
        return favoriteExercises
            .map(exercise => ({
                ...exercise,
                isFavorite: true
            }))
            .filter(exercise => {
                const matchesSearch = !searchTerm ||
                    exercise.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    exercise.description?.toLowerCase().includes(searchTerm.toLowerCase());
                return matchesSearch;
            });
    }, [favoriteExercises, searchTerm]);

    // Event handlers
    const handleClearSearch = () => {
        setSearchTerm('');
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

    // Initialize data
    const initializeData = useCallback(async () => {
        console.log('🚀 Initializing Enhanced ExerciseSelector...');
        await Promise.all([
            fetchExercises('', true),
            fetchCategories(),
            fetchPopularExercises(),
            fetchWorkoutPlans(),
            loadRecentExercises(),
            user ? fetchFavoriteExercises() : Promise.resolve()
        ]);
        setHasInitialized(true);
    }, [fetchExercises, fetchCategories, fetchPopularExercises, fetchWorkoutPlans, fetchFavoriteExercises, user]);

    // Reset state
    const resetState = () => {
        setSearchTerm('');
        setSelectedTab(0);
        setError(null);
        setHasInitialized(false);
        setSelectedPlanCategory('all');
        lastSearchTermRef.current = '';
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }
    };

    return {
        // State
        searchTerm,
        exercises,
        loading,
        error,
        categories,
        popularExercises,
        workoutPlans,
        freePlans,
        featuredPlans,
        workoutPlanCategories,
        selectedPlanCategory,
        selectedTab,
        hasInitialized,
        planView,
        favoriteExercises,
        userFavoriteIds,
        loadingFavorites,
        userTier,
        canAccessPaidPlans,
        tabs,

        // Computed
        filteredExercises,
        filteredWorkoutPlans,
        filteredExercisesWithFavorites,
        filteredFavoriteExercises,

        // Setters
        setSearchTerm,
        setSelectedTab,
        setPlanView,
        setError,

        // Handlers
        handleClearSearch,
        handleCategorySelect,
        handlePlanCategoryFilter,
        handleToggleFavorite,
        initializeData,
        resetState,

        // Search timeout management
        searchTimeoutRef,

        // In-workout specific state
        selectedCategory,
        setSelectedCategory,
        selectedExercise,
        showConfig,
        exerciseConfig,
        addingExercise,
        recentExercises,

        // In-workout specific setters
        setSelectedExercise,
        setShowConfig,
        setExerciseConfig,
        setAddingExercise,
        setRecentExercises,

        // In-workout specific handlers
        handleExerciseSelect,
        handleCloseConfig,

        quickCategories: QUICK_CATEGORIES,

        getDifficultyColor: (difficulty: string) => {
            switch (difficulty.toLowerCase()) {
                case 'beginner':
                    return 'bg-green-100 text-green-800';
                case 'intermediate':
                    return 'bg-yellow-100 text-yellow-800';
                case 'advanced':
                    return 'bg-red-100 text-red-800';
                default:
                    return 'bg-gray-100 text-gray-800';
            }
        },

        getExerciseTypeColor: (type: string) => {
            switch (type.toLowerCase()) {
                case 'strength':
                    return 'bg-blue-100 text-blue-800';
                case 'cardio':
                    return 'bg-red-100 text-red-800';
                case 'flexibility':
                    return 'bg-green-100 text-green-800';
                default:
                    return 'bg-gray-100 text-gray-800';
            }
        },

        getRpeDescription: (rpe: number) => {
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
        },

        getExerciseName: (exercise: Exercise) => {
            return exercise.exerciseName || exercise.name || 'Unknown Exercise';
        }
    };
};