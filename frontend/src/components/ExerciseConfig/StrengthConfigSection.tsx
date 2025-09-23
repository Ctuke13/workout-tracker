import React from 'react';
import {WeightUnit, formatTime, convertWeight, formatWeight} from '../../types/exercise';

interface StrengthConfigSectionProps {
    targetSets: number;
    targetReps: number;
    targetWeight?: number;
    targetWeightUnit: WeightUnit;
    targetRpe?: number;
    restSeconds: number;
    tempo?: string;
    onSetsChange: (sets: number) => void;
    onRepsChange: (reps: number) => void;
    onWeightChange: (weight?: number) => void;
    onWeightUnitToggle: () => void;
    onRpeChange: (rpe?: number) => void;
    onRestChange: (seconds: number) => void;
    onTempoChange: (tempo?: string) => void;
    onWeightPresetClick: (weight: number) => void;
    onRpePresetClick: (rpe: number) => void;
    getWeightPresets: () => number[];
    getRpePresets: () => number[];
}

export const StrengthConfigSection: React.FC<StrengthConfigSectionProps> = ({
                                                                                targetSets,
                                                                                targetReps,
                                                                                targetWeight,
                                                                                targetWeightUnit,
                                                                                targetRpe,
                                                                                restSeconds,
                                                                                tempo,
                                                                                onSetsChange,
                                                                                onRepsChange,
                                                                                onWeightChange,
                                                                                onWeightUnitToggle,
                                                                                onRpeChange,
                                                                                onRestChange,
                                                                                onTempoChange,
                                                                                onWeightPresetClick,
                                                                                onRpePresetClick,
                                                                                getWeightPresets,
                                                                                getRpePresets
                                                                            }) => {
    return (
        <div className="space-y-6">
            {/* Sets and Reps */}
            <div className="grid grid-cols-2 gap-4 text-center">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Target Sets
                    </label>
                    <input
                        type="number"
                        min="1"
                        max="20"
                        value={targetSets}
                        onChange={(e) => onSetsChange(parseInt(e.target.value) || 1)}
                        className="w-10 p-3 text-lg font-medium border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        inputMode="numeric"
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
                        onChange={(e) => onRepsChange(parseInt(e.target.value) || 1)}
                        className="w-10 p-3 text-lg font-medium border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        inputMode="numeric"
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
                        onClick={onWeightUnitToggle}
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
                    onChange={(e) => onWeightChange(e.target.value ? parseFloat(e.target.value) : undefined)}
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
                    <div className="text-xs text-gray-600 mb-1">Quick weights ({targetWeightUnit}):</div>
                    <div className="flex flex-wrap gap-1">
                        {getWeightPresets().map((weight) => (
                            <button
                                key={weight}
                                type="button"
                                onClick={() => onWeightPresetClick(weight)}
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
                    onChange={(e) => onRpeChange(e.target.value ? parseFloat(e.target.value) : undefined)}
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
                                onClick={() => onRpePresetClick(rpe)}
                                className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                            >
                                {rpe}
                            </button>
                        ))}
                    </div>
                </div>

                {/* RPE Guide */}
                <div className="mt-2 text-xs text-gray-500">
                    <strong>RPE Guide:</strong> 6-7 = Easy, 8 = Challenging, 9 = Hard, 10 = Maximum effort
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
                    onChange={(e) => onTempoChange(e.target.value || undefined)}
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
                    onChange={(e) => onRestChange(parseInt(e.target.value) || 0)}
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
                                onClick={() => onRestChange(seconds)}
                                className="text-xs bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded transition-colors"
                            >
                                {formatTime(seconds)}
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};