import React, {useState, useEffect} from 'react';
import {
    ScheduledExercise,
    ExerciseType,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    Exercise,
    WeightUnit,
    DistanceUnit,
    ExerciseConfiguration,
    formatTime,
    convertWeight,
    formatWeight,
    convertDistance,
    formatDistance,
    getDistancePresets,
    getCardioSessionType
} from '../../types/exercise';
import {calendarApi} from '../../services/calendarApi';
import {exerciseApi} from '../../services/exerciseApi';
import {toast} from 'react-hot-toast';
import {WorkoutPlanInfo} from '../../types/api';
import {StarIcon} from "@heroicons/react/24/outline";
import {StarIcon as StarIconSolid} from "@heroicons/react/24/solid";
// =============================================================================
// COMPONENT PROPS INTERFACE
// =============================================================================

interface ExerciseConfigModalProps {
    isOpen: boolean;
    onClose: () => void;
    exercise: Exercise | null;
    config: ExerciseConfiguration | null;
    onConfigChange: (config: ExerciseConfiguration) => void;
    onSave: () => void;
    selectedDate: Date;
    loading: boolean;
    mode: 'exercise' | 'workout-plan';
    onModeChange: (mode: 'exercise' | 'workout-plan') => void;
    onWorkoutPlanSelect: (workoutPlan: WorkoutPlanInfo | null) => void;
    selectedWorkoutPlan: WorkoutPlanInfo | null;
    isEditMode: boolean;
    editingExercise: ScheduledExercise | null;
    onFavoriteToggle?: (exercise: Exercise) => void;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

const ExerciseConfigModal: React.FC<ExerciseConfigModalProps> = ({
                                                                     isOpen,
                                                                     onClose,
                                                                     exercise,
                                                                     config,
                                                                     onConfigChange,
                                                                     onSave,
                                                                     selectedDate,
                                                                     loading,
                                                                     mode,
                                                                     onModeChange,
                                                                     onWorkoutPlanSelect,
                                                                     selectedWorkoutPlan,
                                                                     isEditMode,
                                                                     editingExercise,
                                                                     onFavoriteToggle
                                                                 }) => {
    // =============================================================================
    // STATE MANAGEMENT
    // =============================================================================

    // Common state
    const [trackingMode, setTrackingMode] = useState<'strength' | 'cardio' | 'isometric'>('strength');
    const [notes, setNotes] = useState('');
    const [isFavorited, setIsFavorited] = useState(false);

    // Strength configuration state
    const [targetSets, setTargetSets] = useState(3);
    const [targetReps, setTargetReps] = useState(10);
    const [targetWeight, setTargetWeight] = useState<number | undefined>(undefined);
    const [targetWeightUnit, setTargetWeightUnit] = useState<WeightUnit>('lbs');
    const [targetRpe, setTargetRpe] = useState<number | undefined>(undefined);
    const [restSeconds, setRestSeconds] = useState(90);
    const [tempo, setTempo] = useState<string | undefined>(undefined);

    // Cardio configuration state
    const [targetDurationMinutes, setTargetDurationMinutes] = useState(30);
    const [targetDistance, setTargetDistance] = useState<number | undefined>(undefined);
    const [targetDistanceUnit, setTargetDistanceUnit] = useState<DistanceUnit>('miles');
    const [targetPace, setTargetPace] = useState<number | undefined>(undefined);

    // Isometric configuration state
    const [targetHoldSeconds, setTargetHoldSeconds] = useState(30);
    const [isometricSets, setIsometricSets] = useState(3);
    const [isometricRestSeconds, setIsometricRestSeconds] = useState(60);

    // =============================================================================
    // HELPER FUNCTIONS
    // =============================================================================

    // Helper function to determine default tracking mode based on exercise type
    const getAutoTrackingMode = (exercise: Exercise): 'strength' | 'cardio' | 'isometric' => {
        if (exercise.isCardio) return 'cardio';
        if (exercise.isIsometric) return 'isometric';
        return 'strength';
    };

    // Weight preset handlers for American users
    const getWeightPresets = (): number[] => {
        if (targetWeightUnit === 'lbs') {
            return [45, 65, 95, 135, 185, 225, 275, 315]; // Common barbell weights
        } else {
            return [20, 30, 40, 60, 80, 100, 120, 140]; // Common kg weights
        }
    };

    // RPE preset handlers
    const getRpePresets = (): number[] => {
        return [6, 7, 8, 9, 10]; // Common RPE values
    };

    // Pace helpers
    const formatPaceDisplay = (pace: number, unit: DistanceUnit): string => {
        const minutes = Math.floor(pace);
        const seconds = Math.round((pace - minutes) * 60);
        return `${minutes}:${seconds.toString().padStart(2, '0')} min/${unit === 'miles' ? 'mi' : 'km'}`;
    };

    const getPacePresets = (unit: DistanceUnit): number[] => {
        if (unit === 'miles') {
            return [6, 7, 8, 9, 10, 11, 12]; // Common min/mile paces
        } else {
            return [3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7]; // Common min/km paces
        }
    };

    // =============================================================================
    // EVENT HANDLERS
    // =============================================================================

    // Weight unit conversion handlers
    const handleWeightUnitToggle = () => {
        const newUnit: WeightUnit = targetWeightUnit === 'lbs' ? 'kg' : 'lbs';

        if (targetWeight !== undefined) {
            const convertedWeight = convertWeight(targetWeight, targetWeightUnit, newUnit);
            setTargetWeight(Math.round(convertedWeight * 10) / 10); // Round to 1 decimal
        }

        setTargetWeightUnit(newUnit);
    };

    // Distance unit conversion handlers
    const handleDistanceUnitToggle = () => {
        const newUnit: DistanceUnit = targetDistanceUnit === 'miles' ? 'km' : 'miles';

        if (targetDistance !== undefined) {
            const convertedDistance = convertDistance(targetDistance, targetDistanceUnit, newUnit);
            setTargetDistance(Math.round(convertedDistance * 10) / 10); // Round to 1 decimal
        }

        setTargetDistanceUnit(newUnit);
    };

    // Preset click handlers
    const handleDistancePresetClick = (distance: number) => {
        setTargetDistance(distance);
    };

    const handleWeightPresetClick = (weight: number) => {
        setTargetWeight(weight);
    };

    const handleRpePresetClick = (rpe: number) => {
        setTargetRpe(rpe);
    };

    const handleToggleFavorite = async () => {
        if (!exercise) return;

        try {
            console.log(`🌟 Modal: Toggling favorite for ${exercise.name}, current status: ${exercise.isFavorite}`);

            // 🚀 OPTIMISTIC UPDATE
            const newFavoriteStatus = !exercise.isFavorite;
            setIsFavorited(newFavoriteStatus);
            exercise.isFavorite = newFavoriteStatus;

            // 🌐 API CALL
            const result = await exerciseApi.toggleFavorite(exercise.id);

            // ✅ SYNC: Ensure state matches API response
            setIsFavorited(result.isFavorite);
            exercise.isFavorite = result.isFavorite;

            // 📢 NOTIFY PARENT: Let CalendarPage know about the change
            if (onFavoriteToggle) {
                onFavoriteToggle(exercise);
            }

            toast.success(result.isFavorite ? 'Added to favorites' : 'Removed from favorites');

        } catch (error) {
            console.error('❌ Modal: Failed to toggle favorite:', error);

            // 🔄 REVERT on error
            setIsFavorited(exercise.isFavorite || false);
            toast.error('Failed to update favorites');
        }
    };

    // Use a ref to store the latest onConfigChange to avoid dependency issues
    const onConfigChangeRef = React.useRef(onConfigChange);
    React.useEffect(() => {
        onConfigChangeRef.current = onConfigChange;
    }, [onConfigChange]);

    // Update configuration when state changes - memoized to prevent infinite loops


    // =============================================================================
    // INITIALIZATION EFFECT
    // =============================================================================

    // Initialize state from existing configuration or exercise
    useEffect(() => {
        if (config) {
            setTrackingMode(config.trackingMode);
            setNotes(config.notes || '');

            if (config.trackingMode === 'strength') {
                const strengthConfig = config as StrengthConfiguration;
                setTargetSets(strengthConfig.targetSets || 3);
                setTargetReps(strengthConfig.targetReps || 10);
                setTargetWeight(strengthConfig.targetWeight);
                setTargetWeightUnit(strengthConfig.targetWeightUnit || 'lbs');
                setTargetRpe(strengthConfig.targetRpe);
                setRestSeconds(strengthConfig.restSeconds || 90);
                setTempo(strengthConfig.tempo);
            } else if (config.trackingMode === 'cardio') {
                const cardioConfig = config as CardioConfiguration;
                setTargetDurationMinutes(cardioConfig.targetDurationMinutes || 30);
                setTargetDistance(cardioConfig.targetDistance);
                setTargetDistanceUnit(cardioConfig.targetDistanceUnit || 'miles');
                setTargetPace(cardioConfig.targetPace);
                setTargetSets(cardioConfig.targetSets || 1);
                setIsometricRestSeconds(cardioConfig.restSeconds || 60);
            } else if (config.trackingMode === 'isometric') {
                const isometricConfig = config as IsometricConfiguration;
                setTargetHoldSeconds(isometricConfig.holdDurationSeconds || 30);
                setIsometricSets(isometricConfig.targetSets || 3);
                setIsometricRestSeconds(isometricConfig.restSeconds || 60);
            }
        } else if (exercise) {
            // Set defaults for new exercises
            const autoMode = getAutoTrackingMode(exercise);
            setTrackingMode(autoMode);
            setNotes('');

            // American user defaults
            setTargetWeightUnit('lbs');
            setTargetDistanceUnit('miles');

            if (autoMode === 'cardio') {
                const sessionType = getCardioSessionType(exercise);
                setTargetDurationMinutes(exercise.estimatedDurationMinutes || 20);
                setTargetSets(sessionType.defaultSets);
                if (sessionType.type === 'interval_sets') {
                    setIsometricRestSeconds(60); // Rest between intervals
                }
            }
        }
    }, [config, exercise]);

    // Add useEffect to sync favorite status
    useEffect(() => {
        if (exercise) {
            const favoriteStatus = exercise.isFavorite || false;
            setIsFavorited(favoriteStatus);
            console.log(`🔄 Modal: Syncing favorite status for ${exercise.name}: ${favoriteStatus}`);
        }
    }, [exercise, exercise?.isFavorite]);

    // Update configuration when state changes - properly debounced
    useEffect(() => {
        if (!exercise) return;

        const timeoutId = setTimeout(() => {
            let newConfig: ExerciseConfiguration;

            if (trackingMode === 'strength') {
                newConfig = {
                    trackingMode: 'strength',
                    targetSets,
                    targetReps,
                    targetWeight,
                    targetWeightUnit,
                    restSeconds,
                    targetRpe: targetRpe || undefined,
                    tempo: tempo || undefined,
                };
            } else if (trackingMode === 'cardio') {
                newConfig = {
                    trackingMode: 'cardio',
                    targetDurationMinutes,
                    targetDistance: targetDistance || undefined,
                    targetDistanceUnit,
                    targetPace: targetPace || undefined,
                };
            } else {
                newConfig = {
                    trackingMode: 'isometric',
                    targetSets: isometricSets,
                    holdDurationSeconds: targetHoldSeconds,
                    restSeconds: isometricRestSeconds,
                };
            }

            // Always add notes
            if (notes) {
                newConfig.notes = notes;
            }

            onConfigChangeRef.current(newConfig);
        }, 300); // 300ms debounce

        return () => clearTimeout(timeoutId);
    }, [
        exercise,
        trackingMode,
        targetSets,
        targetReps,
        targetWeight,
        targetWeightUnit,
        restSeconds,
        targetRpe,
        tempo,
        targetDurationMinutes,
        targetDistance,
        targetDistanceUnit,
        targetPace,
        targetHoldSeconds,
        isometricSets,
        isometricRestSeconds,
        notes
    ]);

    // =============================================================================
    // RENDER GUARD
    // =============================================================================

    if (!isOpen) return null;

    // =============================================================================
    // RENDER COMPONENT
    // =============================================================================

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
                {/* =============================================================================
                    HEADER
                    ============================================================================= */}
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold text-gray-900">
                        {isEditMode ? 'Edit Exercise' : mode === 'workout-plan' ? 'Schedule Workout Plan' : 'Configure Exercise'}
                    </h2>
                    <div className="flex items-center gap-3">
                        {/* ✅ NEW: Favorite Button in Header */}
                        {exercise && mode === 'exercise' && (
                            <button
                                onClick={handleToggleFavorite}
                                className={`
            p-2 rounded-full transition-all duration-200
            active:scale-95 shadow-sm hover:shadow-md border
            ${isFavorited
                                    ? 'text-yellow-500 bg-yellow-100 hover:bg-yellow-200 border-yellow-300'
                                    : 'text-gray-400 bg-gray-100 hover:bg-yellow-100 border-gray-300'
                                }
        `}
                                title={isFavorited ? 'Remove from favorites' : 'Add to favorites'}
                            >
                                {isFavorited ? (
                                    <StarIconSolid className="w-5 h-5 text-yellow-500"/> // ✅ Use solid star when favorited
                                ) : (
                                    <StarIcon className="w-5 h-5"/> // ✅ Use outline star when not favorited
                                )}
                            </button>
                        )}
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-600 text-2xl font-bold"
                            aria-label="Close modal"
                        >
                            ×
                        </button>
                    </div>
                </div>

                {/* =============================================================================
                    MODE SELECTION
                    ============================================================================= */}
                <div className="mb-6">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Scheduling Mode
                    </label>
                    <div className="grid grid-cols-2 gap-2">
                        <button
                            type="button"
                            onClick={() => onModeChange('exercise')}
                            className={`p-3 text-sm font-medium rounded-lg border transition-colors ${
                                mode === 'exercise'
                                    ? 'bg-blue-600 text-white border-blue-600'
                                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                            }`}
                        >
                            💪 Individual Exercise
                        </button>
                        <button
                            type="button"
                            onClick={() => onModeChange('workout-plan')}
                            className={`p-3 text-sm font-medium rounded-lg border transition-colors ${
                                mode === 'workout-plan'
                                    ? 'bg-blue-600 text-white border-blue-600'
                                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                            }`}
                        >
                            📋 Workout Plan
                        </button>
                    </div>
                </div>

                {/* =============================================================================
                    WORKOUT PLAN MODE
                    ============================================================================= */}
                {mode === 'workout-plan' && (
                    <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                        <h3 className="text-lg font-semibold text-gray-900 mb-4">Select Workout Plan</h3>
                        <div className="space-y-3">
                            {selectedWorkoutPlan ? (
                                <div className="p-3 bg-white rounded-lg border border-blue-200">
                                    <h4 className="font-semibold text-blue-900">{selectedWorkoutPlan.name}</h4>
                                    <p className="text-sm text-gray-600">{selectedWorkoutPlan.description}</p>
                                    <div className="flex gap-2 mt-2">
                                        <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                                            {selectedWorkoutPlan.difficulty}
                                        </span>
                                        <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">
                                            {selectedWorkoutPlan.exerciseCount} exercises
                                        </span>
                                        <span className="text-xs bg-orange-100 text-orange-800 px-2 py-1 rounded">
                                            {selectedWorkoutPlan.estimatedDurationMinutes} min
                                        </span>
                                    </div>
                                </div>
                            ) : (
                                <div className="text-center py-8">
                                    <p className="text-gray-500 mb-4">No workout plan selected</p>
                                    <button
                                        type="button"
                                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                                        onClick={() => {
                                            // This would typically open a workout plan selector
                                            console.log('Open workout plan selector');
                                        }}
                                    >
                                        Browse Workout Plans
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* =============================================================================
                    EXERCISE MODE
                    ============================================================================= */}
                {mode === 'exercise' && exercise && (
                    <>
                        {/* Exercise Info */}
                        <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                            <h3 className="text-lg font-semibold text-gray-900 mb-2">{exercise.name}</h3>
                            <div className="flex gap-4 text-sm text-gray-600">
                                <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded">
                                    {exercise.exerciseType}
                                </span>
                                <span className="bg-green-100 text-green-800 px-2 py-1 rounded">
                                    {exercise.targetMuscleGroups?.[0] || 'General'}
                                </span>
                            </div>
                        </div>


                        {/* =============================================================================
                            STRENGTH CONFIGURATION
                            ============================================================================= */}
                        {trackingMode === 'strength' && (
                            <div className="space-y-6">
                                {/* Sets and Reps */}
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Sets
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="20"
                                            value={targetSets}
                                            onChange={(e) => setTargetSets(parseInt(e.target.value) || 1)}
                                            className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Target Reps
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="100"
                                            value={targetReps}
                                            onChange={(e) => setTargetReps(parseInt(e.target.value) || 1)}
                                            className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        />
                                    </div>
                                </div>

                                {/* Weight Configuration */}
                                <div>
                                    <div className="flex items-center justify-between mb-2">
                                        <label className="text-sm font-medium text-gray-700">
                                            Target Weight ({targetWeightUnit})
                                        </label>
                                        <button
                                            type="button"
                                            onClick={handleWeightUnitToggle}
                                            className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                                            title="Toggle between lbs and kg"
                                        >
                                            Switch to {targetWeightUnit === 'lbs' ? 'kg' : 'lbs'}
                                        </button>
                                    </div>
                                    <input
                                        type="number"
                                        min="0"
                                        step="0.5"
                                        value={targetWeight || ''}
                                        onChange={(e) => setTargetWeight(e.target.value ? parseFloat(e.target.value) : undefined)}
                                        placeholder={`Enter weight in ${targetWeightUnit}`}
                                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    />

                                    {/* Weight Conversion Hint */}
                                    {targetWeight && (
                                        <div className="mt-1 text-xs text-gray-500">
                                            ≈ {formatWeight(convertWeight(targetWeight, targetWeightUnit, targetWeightUnit === 'lbs' ? 'kg' : 'lbs'), targetWeightUnit === 'lbs' ? 'kg' : 'lbs')}
                                        </div>
                                    )}

                                    {/* Weight Presets */}
                                    <div className="mt-2">
                                        <div className="text-xs text-gray-600 mb-1">Quick weights
                                            ({targetWeightUnit}):
                                        </div>
                                        <div className="flex flex-wrap gap-1">
                                            {getWeightPresets().map((weight) => (
                                                <button
                                                    key={weight}
                                                    type="button"
                                                    onClick={() => handleWeightPresetClick(weight)}
                                                    className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                                                >
                                                    {weight}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>

                                {/* RPE Configuration */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Target RPE (Rate of Perceived Exertion)
                                    </label>
                                    <input
                                        type="number"
                                        min="1"
                                        max="10"
                                        step="0.5"
                                        value={targetRpe || ''}
                                        onChange={(e) => setTargetRpe(e.target.value ? parseFloat(e.target.value) : undefined)}
                                        placeholder="1-10 scale (optional)"
                                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    />

                                    {/* RPE Presets */}
                                    <div className="mt-2">
                                        <div className="text-xs text-gray-600 mb-1">Quick RPE:</div>
                                        <div className="flex gap-1">
                                            {getRpePresets().map((rpe) => (
                                                <button
                                                    key={rpe}
                                                    type="button"
                                                    onClick={() => handleRpePresetClick(rpe)}
                                                    className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                                                >
                                                    {rpe}
                                                </button>
                                            ))}
                                        </div>
                                    </div>

                                    {/* RPE Guide */}
                                    <div className="mt-2 text-xs text-gray-500">
                                        <strong>RPE Guide:</strong> 6-7 = Easy, 8 = Challenging, 9 = Hard, 10 = Maximum
                                        effort
                                    </div>
                                </div>

                                {/* Tempo Configuration */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Tempo (Optional)
                                    </label>
                                    <input
                                        type="text"
                                        value={tempo || ''}
                                        onChange={(e) => setTempo(e.target.value || undefined)}
                                        placeholder="e.g., 3-1-2-1 (eccentric-pause-concentric-pause)"
                                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    />
                                    <div className="mt-1 text-xs text-gray-500">
                                        Format: eccentric-pause-concentric-pause (e.g., 3-1-2-1)
                                    </div>
                                </div>

                                {/* Rest Time */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Rest Between Sets (seconds)
                                    </label>
                                    <input
                                        type="number"
                                        min="0"
                                        max="600"
                                        step="15"
                                        value={restSeconds}
                                        onChange={(e) => setRestSeconds(parseInt(e.target.value) || 0)}
                                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    />

                                    {/* Rest Time Presets */}
                                    <div className="mt-2">
                                        <div className="text-xs text-gray-600 mb-1">Quick rest times:</div>
                                        <div className="flex gap-1">
                                            {[30, 60, 90, 120, 180, 300].map((seconds) => (
                                                <button
                                                    key={seconds}
                                                    type="button"
                                                    onClick={() => setRestSeconds(seconds)}
                                                    className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                                                >
                                                    {formatTime(seconds)}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* =============================================================================
                            CARDIO CONFIGURATION
                            ============================================================================= */}
                        {trackingMode === 'cardio' && (
                            <div className="space-y-6">
                                {/* ✅ NEW: Session Type Detection & Display */}
                                {(() => {
                                    const sessionType = getCardioSessionType(exercise!);
                                    return (
                                        <>
                                            {sessionType.type === 'interval_sets' && (
                                                <div className="bg-orange-50 border border-orange-200 rounded-lg p-3">
                                                    <div className="flex items-center gap-2 mb-2">
                                                        <span className="text-orange-600">⚡</span>
                                                        <span className="text-sm font-medium text-orange-800">
                                    Interval Training Detected
                                </span>
                                                    </div>
                                                    <p className="text-xs text-orange-700">
                                                        {sessionType.description}
                                                    </p>
                                                </div>
                                            )}

                                            {/* Sets Configuration - Only for interval cardio */}
                                            {sessionType.showSetsInConfig && (
                                                <div>
                                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                                        Number of Sets/Rounds
                                                    </label>
                                                    <input
                                                        type="number"
                                                        min="1"
                                                        max="20"
                                                        value={targetSets}
                                                        onChange={(e) => setTargetSets(parseInt(e.target.value) || 1)}
                                                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                                    />

                                                    {/* Sets Presets for HIIT */}
                                                    <div className="mt-2">
                                                        <div className="text-xs text-gray-600 mb-1">Common set counts:
                                                        </div>
                                                        <div className="flex gap-1">
                                                            {[3, 4, 5, 6, 8, 10].map((sets) => (
                                                                <button
                                                                    key={sets}
                                                                    type="button"
                                                                    onClick={() => setTargetSets(sets)}
                                                                    className="text-xs bg-orange-100 hover:bg-orange-200 text-orange-700 px-2 py-1 rounded transition-colors"
                                                                >
                                                                    {sets}
                                                                </button>
                                                            ))}
                                                        </div>
                                                    </div>
                                                </div>
                                            )}

                                            {/* Duration - Primary Focus */}
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-1">
                            <span className="flex items-center gap-1">
                                ⏱️ Target Duration (minutes)
                                <span className="text-red-600">*</span>
                            </span>
                                                </label>
                                                <input
                                                    type="number"
                                                    min="1"
                                                    max="300"
                                                    value={targetDurationMinutes}
                                                    onChange={(e) => setTargetDurationMinutes(parseInt(e.target.value) || 1)}
                                                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                                    placeholder="Required"
                                                />

                                                {/* Duration Presets - Cardio Focused */}
                                                <div className="mt-2">
                                                    <div className="text-xs text-gray-600 mb-1">Popular durations:</div>
                                                    <div className="flex gap-1 flex-wrap">
                                                        {sessionType.type === 'interval_sets'
                                                            ? [5, 10, 15, 20, 30, 45].map((minutes) => (
                                                                <button
                                                                    key={minutes}
                                                                    type="button"
                                                                    onClick={() => setTargetDurationMinutes(minutes)}
                                                                    className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
                                                                >
                                                                    {minutes}m
                                                                </button>
                                                            ))
                                                            : [15, 20, 30, 45, 60, 75, 90].map((minutes) => (
                                                                <button
                                                                    key={minutes}
                                                                    type="button"
                                                                    onClick={() => setTargetDurationMinutes(minutes)}
                                                                    className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
                                                                >
                                                                    {minutes}m
                                                                </button>
                                                            ))
                                                        }
                                                    </div>
                                                </div>
                                            </div>

                                            {/* Distance Configuration - High Priority */}
                                            <div>
                                                <div className="flex items-center justify-between mb-2">
                                                    <label
                                                        className="text-sm font-medium text-gray-700 flex items-center gap-1">
                                                        📍 Target Distance ({targetDistanceUnit})
                                                        <span className="text-xs text-gray-500">(optional)</span>
                                                    </label>
                                                    <button
                                                        type="button"
                                                        onClick={handleDistanceUnitToggle}
                                                        className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                                                        title="Toggle between miles and km"
                                                    >
                                                        Switch to {targetDistanceUnit === 'miles' ? 'km' : 'miles'}
                                                    </button>
                                                </div>

                                                <input
                                                    type="number"
                                                    min="0"
                                                    step="0.1"
                                                    value={targetDistance || ''}
                                                    onChange={(e) => setTargetDistance(e.target.value ? parseFloat(e.target.value) : undefined)}
                                                    placeholder={`Enter distance in ${targetDistanceUnit}`}
                                                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                                />

                                                {/* Distance Conversion Hint */}
                                                {targetDistance && (
                                                    <div className="mt-1 text-xs text-gray-500">
                                                        ≈ {formatDistance(convertDistance(targetDistance, targetDistanceUnit, targetDistanceUnit === 'miles' ? 'km' : 'miles'), targetDistanceUnit === 'miles' ? 'km' : 'miles')}
                                                    </div>
                                                )}

                                                {/* Distance Presets - Race Distances */}
                                                <div className="mt-2">
                                                    <div className="text-xs text-gray-600 mb-1">Popular distances:</div>
                                                    <div className="flex flex-wrap gap-1">
                                                        {getDistancePresets(targetDistanceUnit).map((distance) => (
                                                            <button
                                                                key={distance}
                                                                type="button"
                                                                onClick={() => handleDistancePresetClick(distance)}
                                                                className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
                                                            >
                                                                {distance === 3.1 && targetDistanceUnit === 'miles' ? '5K' :
                                                                    distance === 6.2 && targetDistanceUnit === 'miles' ? '10K' :
                                                                        distance === 13.1 && targetDistanceUnit === 'miles' ? 'Half' :
                                                                            distance === 26.2 && targetDistanceUnit === 'miles' ? 'Marathon' :
                                                                                distance === 5 && targetDistanceUnit === 'km' ? '5K' :
                                                                                    distance === 10 && targetDistanceUnit === 'km' ? '10K' :
                                                                                        distance === 21.1 && targetDistanceUnit === 'km' ? 'Half' :
                                                                                            distance === 42.2 && targetDistanceUnit === 'km' ? 'Marathon' :
                                                                                                distance}
                                                                {targetDistanceUnit === 'miles' ? '' : distance > 10 ? '' : targetDistanceUnit}
                                                            </button>
                                                        ))}
                                                    </div>
                                                </div>
                                            </div>

                                            {/* Pace Configuration */}
                                            <div>
                                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                                    ⚡ Target Pace (min/{targetDistanceUnit === 'miles' ? 'mile' : 'km'})
                                                </label>
                                                <input
                                                    type="number"
                                                    min="3"
                                                    max="20"
                                                    step="0.1"
                                                    value={targetPace || ''}
                                                    onChange={(e) => setTargetPace(e.target.value ? parseFloat(e.target.value) : undefined)}
                                                    placeholder={`Pace in min/${targetDistanceUnit === 'miles' ? 'mile' : 'km'} (optional)`}
                                                    className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                                />

                                                {/* Pace Display */}
                                                {targetPace && (
                                                    <div className="mt-1 text-xs text-gray-500">
                                                        Pace: {formatPaceDisplay(targetPace, targetDistanceUnit)}
                                                    </div>
                                                )}

                                                {/* Pace Presets */}
                                                <div className="mt-2">
                                                    <div className="text-xs text-gray-600 mb-1">Common paces:</div>
                                                    <div className="flex flex-wrap gap-1">
                                                        {getPacePresets(targetDistanceUnit).map((pace) => (
                                                            <button
                                                                key={pace}
                                                                type="button"
                                                                onClick={() => setTargetPace(pace)}
                                                                className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
                                                            >
                                                                {Math.floor(pace)}:{((pace % 1) * 60).toFixed(0).padStart(2, '0')}
                                                            </button>
                                                        ))}
                                                    </div>
                                                </div>
                                            </div>

                                            {/* Rest Time - Only for Interval Cardio */}
                                            {sessionType.showSetsInConfig && (
                                                <div>
                                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                                        💤 Rest Between Sets (seconds)
                                                    </label>
                                                    <input
                                                        type="number"
                                                        min="0"
                                                        max="600"
                                                        step="15"
                                                        value={isometricRestSeconds}
                                                        onChange={(e) => setIsometricRestSeconds(parseInt(e.target.value) || 0)}
                                                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                                                    />

                                                    {/* Rest Time Presets */}
                                                    <div className="mt-2">
                                                        <div className="text-xs text-gray-600 mb-1">Quick rest times:
                                                        </div>
                                                        <div className="flex gap-1">
                                                            {[15, 30, 45, 60, 90, 120].map((seconds) => (
                                                                <button
                                                                    key={seconds}
                                                                    type="button"
                                                                    onClick={() => setIsometricRestSeconds(seconds)}
                                                                    className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
                                                                >
                                                                    {formatTime(seconds)}
                                                                </button>
                                                            ))}
                                                        </div>
                                                    </div>
                                                </div>
                                            )}
                                        </>
                                    );
                                })()}
                            </div>
                        )}

                        {/* =============================================================================
                            ISOMETRIC CONFIGURATION
                            ============================================================================= */}
                        {trackingMode === 'isometric' && (
                            <div className="space-y-6">
                                {/* Hold Time and Sets */}
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Hold Time (seconds)
                                        </label>
                                        <input
                                            type="number"
                                            min="5"
                                            max="300"
                                            step="5"
                                            value={targetHoldSeconds}
                                            onChange={(e) => setTargetHoldSeconds(parseInt(e.target.value) || 5)}
                                            className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        />

                                        {/* Hold Time Presets */}
                                        <div className="mt-2">
                                            <div className="text-xs text-gray-600 mb-1">Quick times:</div>
                                            <div className="flex gap-1">
                                                {[15, 30, 45, 60, 90, 120].map((seconds) => (
                                                    <button
                                                        key={seconds}
                                                        type="button"
                                                        onClick={() => setTargetHoldSeconds(seconds)}
                                                        className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                                                    >
                                                        {seconds}s
                                                    </button>
                                                ))}
                                            </div>
                                        </div>
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Number of Sets
                                        </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="10"
                                            value={isometricSets}
                                            onChange={(e) => setIsometricSets(parseInt(e.target.value) || 1)}
                                            className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        />
                                    </div>
                                </div>

                                {/* Rest Time */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Rest Between Sets (seconds)
                                    </label>
                                    <input
                                        type="number"
                                        min="0"
                                        max="600"
                                        step="15"
                                        value={isometricRestSeconds}
                                        onChange={(e) => setIsometricRestSeconds(parseInt(e.target.value) || 0)}
                                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    />

                                    {/* Rest Time Presets */}
                                    <div className="mt-2">
                                        <div className="text-xs text-gray-600 mb-1">Quick rest times:</div>
                                        <div className="flex gap-1">
                                            {[30, 60, 90, 120, 180].map((seconds) => (
                                                <button
                                                    key={seconds}
                                                    type="button"
                                                    onClick={() => setIsometricRestSeconds(seconds)}
                                                    className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                                                >
                                                    {formatTime(seconds)}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* =============================================================================
                            NOTES SECTION
                            ============================================================================= */}
                        <div className="mt-6">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Notes (Optional)
                            </label>
                            <textarea
                                value={notes}
                                onChange={(e) => setNotes(e.target.value)}
                                placeholder="Add any notes about this exercise..."
                                rows={3}
                                className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                            />
                        </div>
                    </>
                )}

                {/* =============================================================================
                    ACTION BUTTONS
                    ============================================================================= */}
                <div className="flex justify-end gap-3 mt-8 pt-6 border-t border-gray-200">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                        disabled={loading}
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        onClick={onSave}
                        disabled={loading || (mode === 'exercise' && !exercise) || (mode === 'workout-plan' && !selectedWorkoutPlan)}
                        className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? (
                            <>
                                <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white inline"
                                     xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                            strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor"
                                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Saving...
                            </>
                        ) : (
                            <>
                                {isEditMode ? 'Update Exercise' :
                                    mode === 'workout-plan' ? 'Schedule Workout Plan' :
                                        'Add Exercise'}
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ExerciseConfigModal;