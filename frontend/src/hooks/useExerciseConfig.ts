import {useState, useEffect, useRef, useCallback} from 'react';
import {
    Exercise,
    ExerciseConfiguration,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    WeightUnit,
    DistanceUnit,
    convertWeight,
    convertDistance,
    getCardioSessionType
} from '../types/exercise';
import {exerciseApi} from '../services/exerciseApi';
import {toast} from 'react-hot-toast';

interface UseExerciseConfigProps {
    exercise: Exercise | null;
    config: ExerciseConfiguration | null;
    onConfigChange: (config: ExerciseConfiguration) => void;
    onFavoriteToggle?: (exercise: Exercise) => void;
}

export const useExerciseConfig = ({
                                      exercise,
                                      config,
                                      onConfigChange,
                                      onFavoriteToggle
                                  }: UseExerciseConfigProps) => {
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
    const getAutoTrackingMode = useCallback((exercise: Exercise): 'strength' | 'cardio' | 'isometric' => {
        if (exercise.isCardio) return 'cardio';
        if (exercise.isIsometric) return 'isometric';
        return 'strength';
    }, []);

    // Weight preset handlers for American users
    const getWeightPresets = useCallback((): number[] => {
        if (targetWeightUnit === 'lbs') {
            return [45, 65, 95, 135, 185, 225, 275, 315]; // Common barbell weights
        } else {
            return [20, 30, 40, 60, 80, 100, 120, 140]; // Common kg weights
        }
    }, [targetWeightUnit]);

    // RPE preset handlers
    const getRpePresets = useCallback((): number[] => {
        return [6, 7, 8, 9, 10]; // Common RPE values
    }, []);

    // Pace helpers
    const formatPaceDisplay = useCallback((pace: number, unit: DistanceUnit): string => {
        const minutes = Math.floor(pace);
        const seconds = Math.round((pace - minutes) * 60);
        return `${minutes}:${seconds.toString().padStart(2, '0')} min/${unit === 'miles' ? 'mi' : 'km'}`;
    }, []);

    const getPacePresets = useCallback((unit: DistanceUnit): number[] => {
        if (unit === 'miles') {
            return [6, 7, 8, 9, 10, 11, 12]; // Common min/mile paces
        } else {
            return [3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7]; // Common min/km paces
        }
    }, []);

    // =============================================================================
    // EVENT HANDLERS
    // =============================================================================

    // Weight unit conversion handlers
    const handleWeightUnitToggle = useCallback(() => {
        const newUnit: WeightUnit = targetWeightUnit === 'lbs' ? 'kg' : 'lbs';

        if (targetWeight !== undefined) {
            const convertedWeight = convertWeight(targetWeight, targetWeightUnit, newUnit);
            setTargetWeight(Math.round(convertedWeight * 10) / 10); // Round to 1 decimal
        }

        setTargetWeightUnit(newUnit);
    }, [targetWeight, targetWeightUnit]);

    // Distance unit conversion handlers
    const handleDistanceUnitToggle = useCallback(() => {
        const newUnit: DistanceUnit = targetDistanceUnit === 'miles' ? 'km' : 'miles';

        if (targetDistance !== undefined) {
            const convertedDistance = convertDistance(targetDistance, targetDistanceUnit, newUnit);
            setTargetDistance(Math.round(convertedDistance * 10) / 10); // Round to 1 decimal
        }

        setTargetDistanceUnit(newUnit);
    }, [targetDistance, targetDistanceUnit]);

    // Preset click handlers
    const handleDistancePresetClick = useCallback((distance: number) => {
        setTargetDistance(distance);
    }, []);

    const handleWeightPresetClick = useCallback((weight: number) => {
        setTargetWeight(weight);
    }, []);

    const handleRpePresetClick = useCallback((rpe: number) => {
        setTargetRpe(rpe);
    }, []);

    const handleToggleFavorite = useCallback(async () => {
        if (!exercise) return;

        try {
            console.log(`🌟 Hook: Toggling favorite for ${exercise.name}, current status: ${exercise.isFavorite}`);

            // 🚀 OPTIMISTIC UPDATE
            const newFavoriteStatus = !exercise.isFavorite;
            setIsFavorited(newFavoriteStatus);
            exercise.isFavorite = newFavoriteStatus;

            // 🌐 API CALL
            const result = await exerciseApi.toggleFavorite(exercise.id);

            // ✅ SYNC: Ensure state matches API response
            setIsFavorited(result.isFavorite);
            exercise.isFavorite = result.isFavorite;

            // 📢 NOTIFY PARENT: Let parent component know about the change
            if (onFavoriteToggle) {
                onFavoriteToggle(exercise);
            }

            toast.success(result.isFavorite ? 'Added to favorites' : 'Removed from favorites');

        } catch (error) {
            console.error('❌ Hook: Failed to toggle favorite:', error);

            // 🔄 REVERT on error
            setIsFavorited(exercise.isFavorite || false);
            toast.error('Failed to update favorites');
        }
    }, [exercise, onFavoriteToggle]);

    // Use a ref to store the latest onConfigChange to avoid dependency issues
    const onConfigChangeRef = useRef(onConfigChange);
    useEffect(() => {
        onConfigChangeRef.current = onConfigChange;
    }, [onConfigChange]);

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
    }, [config, exercise, getAutoTrackingMode]);

    // Add useEffect to sync favorite status
    useEffect(() => {
        if (exercise) {
            const favoriteStatus = exercise.isFavorite || false;
            setIsFavorited(favoriteStatus);
            console.log(`📄 Hook: Syncing favorite status for ${exercise.name}: ${favoriteStatus}`);
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
    // RETURN INTERFACE
    // =============================================================================

    return {
        // State
        trackingMode,
        notes,
        isFavorited,

        // Strength state
        targetSets,
        targetReps,
        targetWeight,
        targetWeightUnit,
        targetRpe,
        restSeconds,
        tempo,

        // Cardio state
        targetDurationMinutes,
        targetDistance,
        targetDistanceUnit,
        targetPace,

        // Isometric state
        targetHoldSeconds,
        isometricSets,
        isometricRestSeconds,

        // Setters
        setTrackingMode,
        setNotes,
        setTargetSets,
        setTargetReps,
        setTargetWeight,
        setTargetWeightUnit,
        setTargetRpe,
        setRestSeconds,
        setTempo,
        setTargetDurationMinutes,
        setTargetDistance,
        setTargetDistanceUnit,
        setTargetPace,
        setTargetHoldSeconds,
        setIsometricSets,
        setIsometricRestSeconds,

        // Event handlers
        handleWeightUnitToggle,
        handleDistanceUnitToggle,
        handleDistancePresetClick,
        handleWeightPresetClick,
        handleRpePresetClick,
        handleToggleFavorite,

        // Helper functions
        getWeightPresets,
        getRpePresets,
        formatPaceDisplay,
        getPacePresets,
        getAutoTrackingMode,
    };
};