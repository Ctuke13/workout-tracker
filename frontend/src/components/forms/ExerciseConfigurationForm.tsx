import React from 'react';
import {
    Exercise,
    ExerciseConfiguration,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    getWorkoutTrackingType
} from '../../types/exercise';

// =============================================================================
// INTERFACES
// =============================================================================

interface ExerciseConfigurationFormProps {
    exercise: Exercise;
    configuration: ExerciseConfiguration;
    onConfigurationChange: (config: ExerciseConfiguration) => void;
    prescribedValues?: {
        sets?: number;
        reps?: number;
        weight?: number;
        duration?: number;
        rest?: number;
    };
    instructions?: string;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

const ExerciseConfigurationForm: React.FC<ExerciseConfigurationFormProps> = ({
                                                                                 exercise,
                                                                                 configuration,
                                                                                 onConfigurationChange,
                                                                                 prescribedValues,
                                                                                 instructions
                                                                             }) => {
    const trackingType = getWorkoutTrackingType(exercise);

    // Type-safe update functions for each configuration type
    const updateStrengthConfig = (updates: Partial<StrengthConfiguration>) => {
        const currentConfig = configuration as StrengthConfiguration;
        onConfigurationChange({...currentConfig, ...updates} as StrengthConfiguration);
    };

    const updateCardioConfig = (updates: Partial<CardioConfiguration>) => {
        const currentConfig = configuration as CardioConfiguration;
        onConfigurationChange({...currentConfig, ...updates} as CardioConfiguration);
    };

    const updateIsometricConfig = (updates: Partial<IsometricConfiguration>) => {
        const currentConfig = configuration as IsometricConfiguration;
        onConfigurationChange({...currentConfig, ...updates} as IsometricConfiguration);
    };

    // =============================================================================
    // STRENGTH CONFIGURATION
    // =============================================================================

    if (trackingType === 'strength') {
        const config = configuration as StrengthConfiguration;

        return (
            <div className="space-y-4">
                {instructions && (
                    <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                        <p className="text-sm text-blue-700">{instructions}</p>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Sets {prescribedValues?.sets &&
                            <span className="text-gray-500">(recommended: {prescribedValues.sets})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="20"
                            value={config.targetSets}
                            onChange={(e) => updateStrengthConfig({targetSets: parseInt(e.target.value) || 1})}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Reps {prescribedValues?.reps &&
                            <span className="text-gray-500">(recommended: {prescribedValues.reps})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="100"
                            value={config.targetReps}
                            onChange={(e) => updateStrengthConfig({targetReps: parseInt(e.target.value) || 1})}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Weight ({config.targetWeightUnit}) {prescribedValues?.weight &&
                            <span className="text-gray-500">(recommended: {prescribedValues.weight})</span>}
                        </label>
                        <input
                            type="number"
                            min="0"
                            step="0.5"
                            value={config.targetWeight || ''}
                            onChange={(e) => updateStrengthConfig({targetWeight: e.target.value ? parseFloat(e.target.value) : undefined})}
                            placeholder="Optional"
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Rest (seconds) {prescribedValues?.rest &&
                            <span className="text-gray-500">(recommended: {prescribedValues.rest})</span>}
                        </label>
                        <input
                            type="number"
                            min="0"
                            max="600"
                            step="15"
                            value={config.restSeconds}
                            onChange={(e) => updateStrengthConfig({restSeconds: parseInt(e.target.value) || 0})}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                </div>

                {/* Quick Presets */}
                <div className="grid grid-cols-3 gap-2">
                    <button
                        type="button"
                        onClick={() => updateStrengthConfig({targetWeight: 45})}
                        className="px-3 py-2 text-xs bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                    >
                        45 lbs
                    </button>
                    <button
                        type="button"
                        onClick={() => updateStrengthConfig({targetWeight: 135})}
                        className="px-3 py-2 text-xs bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                    >
                        135 lbs
                    </button>
                    <button
                        type="button"
                        onClick={() => updateStrengthConfig({restSeconds: 90})}
                        className="px-3 py-2 text-xs bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                    >
                        90s rest
                    </button>
                </div>
            </div>
        );
    }

    // =============================================================================
    // CARDIO CONFIGURATION
    // =============================================================================

    if (trackingType === 'cardio') {
        const config = configuration as CardioConfiguration;

        return (
            <div className="space-y-4">
                {instructions && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-sm text-red-700">{instructions}</p>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Duration (minutes) {prescribedValues?.duration &&
                            <span className="text-gray-500">(recommended: {prescribedValues.duration})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="300"
                            value={config.targetDurationMinutes}
                            onChange={(e) => updateCardioConfig({targetDurationMinutes: parseInt(e.target.value) || 1})}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Distance ({config.targetDistanceUnit})
                        </label>
                        <input
                            type="number"
                            min="0"
                            step="0.1"
                            value={config.targetDistance || ''}
                            onChange={(e) => updateCardioConfig({targetDistance: e.target.value ? parseFloat(e.target.value) : undefined})}
                            placeholder="Optional"
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                        />
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Target Pace (min/{config.targetDistanceUnit === 'miles' ? 'mile' : 'km'})
                    </label>
                    <input
                        type="number"
                        min="3"
                        max="20"
                        step="0.1"
                        value={config.targetPace || ''}
                        onChange={(e) => updateCardioConfig({targetPace: e.target.value ? parseFloat(e.target.value) : undefined})}
                        placeholder="Optional"
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />
                </div>

                {/* Quick Presets */}
                <div className="grid grid-cols-4 gap-2">
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({targetDurationMinutes: 20})}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        20 min
                    </button>
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({targetDurationMinutes: 30})}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        30 min
                    </button>
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({targetDistance: 3.1})}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        5K
                    </button>
                    <button
                        type="button"
                        onClick={() => updateCardioConfig({targetPace: 8})}
                        className="px-3 py-2 text-xs bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors"
                    >
                        8:00 pace
                    </button>
                </div>
            </div>
        );
    }

    // =============================================================================
    // ISOMETRIC CONFIGURATION
    // =============================================================================

    if (trackingType === 'isometric') {
        const config = configuration as IsometricConfiguration;

        return (
            <div className="space-y-4">
                {instructions && (
                    <div className="p-3 bg-purple-50 border border-purple-200 rounded-lg">
                        <p className="text-sm text-purple-700">{instructions}</p>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Sets {prescribedValues?.sets &&
                            <span className="text-gray-500">(recommended: {prescribedValues.sets})</span>}
                        </label>
                        <input
                            type="number"
                            min="1"
                            max="10"
                            value={config.targetSets}
                            onChange={(e) => updateIsometricConfig({targetSets: parseInt(e.target.value) || 1})}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Hold Time (seconds) {prescribedValues?.duration &&
                            <span className="text-gray-500">(recommended: {prescribedValues.duration})</span>}
                        </label>
                        <input
                            type="number"
                            min="5"
                            max="300"
                            step="5"
                            value={config.holdDurationSeconds}
                            onChange={(e) => updateIsometricConfig({holdDurationSeconds: parseInt(e.target.value) || 5})}
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                        />
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Rest Between Sets (seconds) {prescribedValues?.rest &&
                        <span className="text-gray-500">(recommended: {prescribedValues.rest})</span>}
                    </label>
                    <input
                        type="number"
                        min="0"
                        max="600"
                        step="15"
                        value={config.restSeconds}
                        onChange={(e) => updateIsometricConfig({restSeconds: parseInt(e.target.value) || 0})}
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                    />
                </div>

                {/* Quick Presets */}
                <div className="grid grid-cols-4 gap-2">
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({holdDurationSeconds: 30})}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        30s hold
                    </button>
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({holdDurationSeconds: 60})}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        60s hold
                    </button>
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({targetSets: 3})}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        3 sets
                    </button>
                    <button
                        type="button"
                        onClick={() => updateIsometricConfig({restSeconds: 60})}
                        className="px-3 py-2 text-xs bg-purple-100 hover:bg-purple-200 text-purple-700 rounded-lg transition-colors"
                    >
                        60s rest
                    </button>
                </div>
            </div>
        );
    }

    return null;
};

export default ExerciseConfigurationForm;