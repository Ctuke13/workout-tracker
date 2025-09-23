import React, {useEffect, useMemo} from 'react';
import {useAuth} from '../../contexts/AuthContext';
import {Exercise, ExerciseConfiguration, formatTime} from '../../types/exercise';
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
    StarIcon as StarIconSolid, PlusIcon
} from '@heroicons/react/24/outline';
import {CalendarDay} from '../../types/calendar';
import {useExerciseSelector} from '../../hooks/useExerciseSelector';
import {useWorkout} from '../../contexts/WorkoutContext';

// ==================== INTERFACES ====================

interface EnhancedExerciseSelectorProps {
    mode?: 'calendar' | 'workout';
    onAddToWorkout?: (exercise: Exercise, config: ExerciseConfiguration) => Promise<void>;
    currentWorkoutExerciseCount?: number;
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
                                                                       mode = 'calendar',
                                                                       onAddToWorkout,
                                                                       currentWorkoutExerciseCount,
                                                                       open,
                                                                       onClose,
                                                                       onExerciseSelect,
                                                                       onWorkoutPlanSelect,
                                                                       onWorkoutPlanConfigure,
                                                                       onDragStart,
                                                                       selectedDate,
                                                                       canAddToSelectedDate = true,
                                                                       title = mode === 'workout' ? "Add Exercise to Workout" : "Choose Exercise or Workout Plan",
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
    const allTabs = [
        {id: 0, name: 'Exercises', icon: FireIcon, description: 'Individual exercises'},
        {id: 1, name: 'Favorites', icon: StarIcon, description: 'Your saved exercises', highlight: true},
        {id: 2, name: 'Workout Plans', icon: UserGroupIcon, description: 'Complete routines', highlight: true},
        {id: 3, name: 'Categories', icon: Bars3Icon, description: 'Browse by goal'},
        {id: 4, name: 'Popular', icon: TrophyIcon, description: 'Trending choices'},
    ];

    const tabs = mode === 'workout'
        ? allTabs.filter(tab => tab.id !== 2) // Remove workout plans in workout mode
        : allTabs;

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
        if (mode === 'workout') {
            selectorData.handleExerciseSelect(exercise);
        } else {
            onExerciseSelect(exercise);
            onClose();
        }
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

    const handleAddToWorkout = async () => {
        if (!selectorData.selectedExercise || !selectorData.exerciseConfig || !onAddToWorkout) return;

        selectorData.setAddingExercise(true);
        try {
            await onAddToWorkout(selectorData.selectedExercise, selectorData.exerciseConfig);
            selectorData.handleCloseConfig();
            onClose();
        } catch (err) {
            console.error('Failed to add exercise to workout:', err);
            selectorData.setError('Failed to add exercise. Please try again.');
        } finally {
            selectorData.setAddingExercise(false);
        }
    };

    const handleCloseConfig = () => {
        selectorData.handleCloseConfig();
    };

    const getPresetConfigs = () => {
        if (!selectorData.selectedExercise) return [];

        const presets = [];
        const exerciseType = selectorData.selectedExercise.exerciseType.toLowerCase();

        if (exerciseType === 'strength') {
            presets.push(
                {
                    name: 'Light',
                    targetSets: 3,
                    targetReps: 10,
                    restSeconds: 60,
                    targetRpe: 6
                },
                {
                    name: 'Moderate',
                    targetSets: 3,
                    targetReps: 8,
                    restSeconds: 90,
                    targetRpe: 7
                },
                {
                    name: 'Intense',
                    targetSets: 4,
                    targetReps: 6,
                    restSeconds: 120,
                    targetRpe: 8
                }
            );
        } else if (exerciseType === 'cardio') {
            presets.push(
                {
                    name: 'Short',
                    targetDurationMinutes: 10
                },
                {
                    name: 'Standard',
                    targetDurationMinutes: 20
                },
                {
                    name: 'Long',
                    targetDurationMinutes: 30
                }
            );
        } else if (exerciseType === 'flexibility') {
            presets.push(
                {
                    name: 'Quick',
                    targetSets: 2,
                    holdDurationSeconds: 30,
                    restSeconds: 15
                },
                {
                    name: 'Standard',
                    targetSets: 3,
                    holdDurationSeconds: 45,
                    restSeconds: 20
                }
            );
        }

        return presets;
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

    if (selectorData.showConfig && selectorData.selectedExercise) {
        return (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] flex flex-col">
                    {/* Config Header */}
                    <div className="flex items-center justify-between p-4 border-b border-gray-200">
                        <div>
                            <h3 className="text-lg font-semibold text-gray-900">Configure Exercise</h3>
                            <p className="text-sm text-gray-600">
                                {selectorData.selectedExercise.emoji} {selectorData.getExerciseName(selectorData.selectedExercise)}
                            </p>
                        </div>
                        <button
                            onClick={handleCloseConfig}
                            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                            disabled={selectorData.addingExercise}
                        >
                            <XMarkIcon className="w-5 h-5"/>
                        </button>
                    </div>

                    {/* Error Display */}
                    {selectorData.error && (
                        <div className="mx-4 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                            <div className="flex items-center">
                                <ExclamationTriangleIcon className="w-4 h-4 text-red-400 mr-2"/>
                                <span className="text-sm text-red-700">{selectorData.error}</span>
                                <button
                                    onClick={() => selectorData.setError(null)}
                                    className="ml-auto text-red-400 hover:text-red-600"
                                >
                                    <XMarkIcon className="w-4 h-4"/>
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Config Content */}
                    <div className="flex-1 overflow-y-auto p-4 space-y-4">
                        {/* Exercise Info */}
                        <div className="bg-gray-50 rounded-lg p-3">
                            <div className="flex items-center space-x-2 mb-2">
                                <span
                                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${selectorData.getExerciseTypeColor(selectorData.selectedExercise.exerciseType)}`}>
                                    {selectorData.selectedExercise.exerciseType}
                                </span>
                                <span
                                    className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${selectorData.getDifficultyColor(selectorData.selectedExercise.difficultyLevel)}`}>
                                    {selectorData.selectedExercise.difficultyLevel}
                                </span>
                                {selectorData.selectedExercise.createdByProfessional && (
                                    <span
                                        className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">
                                        <StarIcon className="w-3 h-3 mr-1"/>
                                        Pro
                                    </span>
                                )}
                            </div>
                            {selectorData.selectedExercise.description && (
                                <p className="text-sm text-gray-600 mb-2">{selectorData.selectedExercise.description}</p>
                            )}
                            {selectorData.selectedExercise.targetMuscleGroups && selectorData.selectedExercise.targetMuscleGroups.length > 0 && (
                                <div className="flex flex-wrap gap-1">
                                    {selectorData.selectedExercise.targetMuscleGroups.slice(0, 3).map((muscle, index) => (
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
                                    <BoltIcon className="w-4 h-4 mr-1"/>
                                    Quick Presets
                                </h4>
                                <div className="grid grid-cols-3 gap-2">
                                    {getPresetConfigs().map((preset, index) => (
                                        <button
                                            key={index}
                                            onClick={() => {
                                                if (selectorData.exerciseConfig) {
                                                    if (selectorData.exerciseConfig.trackingMode === 'strength') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetSets: preset.targetSets || 3,
                                                            targetReps: preset.targetReps || 10,
                                                            restSeconds: preset.restSeconds || 90,
                                                            targetRpe: preset.targetRpe || 7
                                                        });
                                                    } else if (selectorData.exerciseConfig.trackingMode === 'cardio') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetDurationMinutes: preset.targetDurationMinutes || 20
                                                        });
                                                    } else if (selectorData.exerciseConfig.trackingMode === 'isometric') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetSets: preset.targetSets || 3,
                                                            holdDurationSeconds: preset.holdDurationSeconds || 30,
                                                            restSeconds: preset.restSeconds || 60
                                                        });
                                                    }
                                                }
                                            }}
                                            className="p-2 border border-gray-200 rounded-lg hover:border-blue-300 hover:bg-blue-50 text-left transition-colors"
                                            disabled={selectorData.addingExercise}
                                        >
                                            <div className="text-sm font-medium">{preset.name}</div>
                                            <div className="text-xs text-gray-600">
                                                {preset.targetSets ? `${preset.targetSets} × ${preset.targetReps}` :
                                                    preset.targetDurationMinutes ? `${preset.targetDurationMinutes} min` :
                                                        preset.holdDurationSeconds ? `${preset.holdDurationSeconds}s hold` : ''}
                                            </div>
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Configuration Fields */}
                        <div className="space-y-3">
                            {/* Strength Configuration */}
                            {selectorData.exerciseConfig?.trackingMode === 'strength' && (
                                <>
                                    {/* Sets */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Sets
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="10"
                                            value={selectorData.exerciseConfig.targetSets || 1}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetSets: parseInt(e.target.value) || 1
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>

                                    {/* Reps */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Reps
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            value={selectorData.exerciseConfig.targetReps || 10}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetReps: parseInt(e.target.value) || 10
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
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
                                            value={selectorData.exerciseConfig.targetWeight || ''}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetWeight: parseFloat(e.target.value) || undefined
                                                    });
                                                }
                                            }}
                                            placeholder="Body weight, 135, etc."
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>
                                </>
                            )}

                            {/* Cardio Configuration */}
                            {selectorData.exerciseConfig?.trackingMode === 'cardio' && (
                                <>
                                    {/* Duration */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Duration (minutes)
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            value={selectorData.exerciseConfig.targetDurationMinutes || 20}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetDurationMinutes: parseInt(e.target.value) || 20
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>

                                    {/* Distance (Optional) */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Distance - Optional
                                        </label>
                                        <div className="flex space-x-2">
                                            <input
                                                type="number"
                                                step="0.1"
                                                value={selectorData.exerciseConfig.targetDistance || ''}
                                                onChange={(e) => {
                                                    if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetDistance: parseFloat(e.target.value) || undefined
                                                        });
                                                    }
                                                }}
                                                placeholder="e.g., 3.1"
                                                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                                disabled={selectorData.addingExercise}
                                            />
                                            <select
                                                value={selectorData.exerciseConfig.targetDistanceUnit}
                                                onChange={(e) => {
                                                    if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                                        selectorData.setExerciseConfig({
                                                            ...selectorData.exerciseConfig,
                                                            targetDistanceUnit: e.target.value as 'km' | 'miles'
                                                        });
                                                    }
                                                }}
                                                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                                disabled={selectorData.addingExercise}
                                            >
                                                <option value="miles">miles</option>
                                                <option value="km">km</option>
                                            </select>
                                        </div>
                                    </div>
                                </>
                            )}

                            {/* Isometric Configuration */}
                            {selectorData.exerciseConfig?.trackingMode === 'isometric' && (
                                <>
                                    {/* Sets */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Sets
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="10"
                                            value={selectorData.exerciseConfig.targetSets || 1}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'isometric') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        targetSets: parseInt(e.target.value) || 1
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>

                                    {/* Hold Duration */}
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Hold Duration (seconds)
                                        </label>
                                        <input
                                            type="number"
                                            min="5"
                                            value={selectorData.exerciseConfig.holdDurationSeconds || 30}
                                            onChange={(e) => {
                                                if (selectorData.exerciseConfig?.trackingMode === 'isometric') {
                                                    selectorData.setExerciseConfig({
                                                        ...selectorData.exerciseConfig,
                                                        holdDurationSeconds: parseInt(e.target.value) || 30
                                                    });
                                                }
                                            }}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                            disabled={selectorData.addingExercise}
                                        />
                                    </div>
                                </>
                            )}

                            {/* Rest Time (for strength and isometric) */}
                            {(selectorData.exerciseConfig?.trackingMode === 'strength' || selectorData.exerciseConfig?.trackingMode === 'isometric') && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Rest Time: {formatTime(selectorData.exerciseConfig.restSeconds || 90)}
                                    </label>
                                    <input
                                        type="range"
                                        min="30"
                                        max="300"
                                        step="15"
                                        value={selectorData.exerciseConfig.restSeconds || 90}
                                        onChange={(e) => {
                                            if (selectorData.exerciseConfig) {
                                                selectorData.setExerciseConfig({
                                                    ...selectorData.exerciseConfig,
                                                    restSeconds: parseInt(e.target.value)
                                                });
                                            }
                                        }}
                                        className="w-full"
                                        disabled={selectorData.addingExercise}
                                    />
                                    <div className="flex justify-between text-xs text-gray-500 mt-1">
                                        <span>30s</span>
                                        <span>5min</span>
                                    </div>
                                </div>
                            )}

                            {/* Target RPE (for strength only) */}
                            {selectorData.exerciseConfig?.trackingMode === 'strength' && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Target RPE: {selectorData.exerciseConfig.targetRpe || 7}
                                    </label>
                                    <input
                                        type="range"
                                        min="1"
                                        max="10"
                                        value={selectorData.exerciseConfig.targetRpe || 7}
                                        onChange={(e) => {
                                            if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                                selectorData.setExerciseConfig({
                                                    ...selectorData.exerciseConfig,
                                                    targetRpe: parseInt(e.target.value)
                                                });
                                            }
                                        }}
                                        className="w-full"
                                        disabled={selectorData.addingExercise}
                                    />
                                    <div className="flex justify-between text-xs text-gray-500 mt-1">
                                        <span>Easy</span>
                                        <span>Max</span>
                                    </div>
                                    <p className="text-xs text-gray-600 mt-1">
                                        {selectorData.getRpeDescription(selectorData.exerciseConfig.targetRpe || 7)}
                                    </p>
                                </div>
                            )}

                            {/* Notes (for all types) */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Notes - Optional
                                </label>
                                <textarea
                                    value={selectorData.exerciseConfig?.notes || ''}
                                    onChange={(e) => {
                                        if (selectorData.exerciseConfig) {
                                            selectorData.setExerciseConfig({
                                                ...selectorData.exerciseConfig,
                                                notes: e.target.value
                                            });
                                        }
                                    }}
                                    rows={2}
                                    placeholder="Form cues, modifications, or reminders..."
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                                    disabled={selectorData.addingExercise}
                                />
                            </div>
                        </div>

                        {/* Estimated Time */}
                        <div className="bg-green-50 rounded-lg p-3">
                            <h4 className="text-sm font-medium text-green-800 mb-1 flex items-center">
                                <ClockIcon className="w-4 h-4 mr-1"/>
                                Estimated Time
                            </h4>
                            <div className="text-base font-semibold text-green-900">
                                {(() => {
                                    const exerciseTime = selectorData.selectedExercise.estimatedDurationMinutes || 2;
                                    let totalTime = exerciseTime;

                                    if (selectorData.exerciseConfig?.trackingMode === 'strength') {
                                        const restTime = ((selectorData.exerciseConfig.restSeconds || 90) * (selectorData.exerciseConfig.targetSets - 1)) / 60;
                                        totalTime = Math.ceil((exerciseTime * selectorData.exerciseConfig.targetSets) + restTime);
                                    } else if (selectorData.exerciseConfig?.trackingMode === 'isometric') {
                                        const restTime = ((selectorData.exerciseConfig.restSeconds || 60) * (selectorData.exerciseConfig.targetSets - 1)) / 60;
                                        totalTime = Math.ceil((exerciseTime * selectorData.exerciseConfig.targetSets) + restTime);
                                    } else if (selectorData.exerciseConfig?.trackingMode === 'cardio') {
                                        totalTime = selectorData.exerciseConfig.targetDurationMinutes;
                                    }

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
                            disabled={selectorData.addingExercise}
                        >
                            Back
                        </button>
                        <button
                            onClick={handleAddToWorkout}
                            disabled={selectorData.addingExercise || !selectorData.exerciseConfig}
                            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center"
                        >
                            {selectorData.addingExercise ? (
                                <>
                                    <div
                                        className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                                    Adding...
                                </>
                            ) : (
                                <>
                                    <PlusIcon className="w-4 h-4 mr-2"/>
                                    Add to Workout
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        );
    }

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