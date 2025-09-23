import React from 'react';
import {
    DistanceUnit,
    formatTime,
    convertDistance,
    formatDistance,
    getDistancePresets,
    getCardioSessionType,
    Exercise
} from '../../types/exercise';

interface CardioConfigSectionProps {
    exercise: Exercise;
    targetDurationMinutes: number;
    targetDistance?: number;
    targetDistanceUnit: DistanceUnit;
    targetPace?: number;
    targetSets: number;
    isometricRestSeconds: number;
    onDurationChange: (minutes: number) => void;
    onDistanceChange: (distance?: number) => void;
    onDistanceUnitToggle: () => void;
    onPaceChange: (pace?: number) => void;
    onSetsChange: (sets: number) => void;
    onRestChange: (seconds: number) => void;
    onDistancePresetClick: (distance: number) => void;
}

export const CardioConfigSection: React.FC<CardioConfigSectionProps> = ({
                                                                            exercise,
                                                                            targetDurationMinutes,
                                                                            targetDistance,
                                                                            targetDistanceUnit,
                                                                            targetPace,
                                                                            targetSets,
                                                                            isometricRestSeconds,
                                                                            onDurationChange,
                                                                            onDistanceChange,
                                                                            onDistanceUnitToggle,
                                                                            onPaceChange,
                                                                            onSetsChange,
                                                                            onRestChange,
                                                                            onDistancePresetClick
                                                                        }) => {
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

    const sessionType = getCardioSessionType(exercise);

    return (
        <div className="space-y-6">
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
                        onChange={(e) => onSetsChange(parseInt(e.target.value) || 1)}
                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />

                    {/* Sets Presets for HIIT */}
                    <div className="mt-2">
                        <div className="text-xs text-gray-600 mb-1">Common set counts:</div>
                        <div className="flex gap-1">
                            {[3, 4, 5, 6, 8, 10].map((sets) => (
                                <button
                                    key={sets}
                                    type="button"
                                    onClick={() => onSetsChange(sets)}
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
                    onChange={(e) => onDurationChange(parseInt(e.target.value) || 1)}
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
                                    onClick={() => onDurationChange(minutes)}
                                    className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
                                >
                                    {minutes}m
                                </button>
                            ))
                            : [15, 20, 30, 45, 60, 75, 90].map((minutes) => (
                                <button
                                    key={minutes}
                                    type="button"
                                    onClick={() => onDurationChange(minutes)}
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
                    <label className="text-sm font-medium text-gray-700 flex items-center gap-1">
                        📍 Target Distance ({targetDistanceUnit})
                        <span className="text-xs text-gray-500">(optional)</span>
                    </label>
                    <button
                        type="button"
                        onClick={onDistanceUnitToggle}
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
                    onChange={(e) => onDistanceChange(e.target.value ? parseFloat(e.target.value) : undefined)}
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
                                onClick={() => onDistancePresetClick(distance)}
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
                    onChange={(e) => onPaceChange(e.target.value ? parseFloat(e.target.value) : undefined)}
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
                                onClick={() => onPaceChange(pace)}
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
                        onChange={(e) => onRestChange(parseInt(e.target.value) || 0)}
                        className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />

                    {/* Rest Time Presets */}
                    <div className="mt-2">
                        <div className="text-xs text-gray-600 mb-1">Quick rest times:</div>
                        <div className="flex gap-1">
                            {[15, 30, 45, 60, 90, 120].map((seconds) => (
                                <button
                                    key={seconds}
                                    type="button"
                                    onClick={() => onRestChange(seconds)}
                                    className="text-xs bg-red-100 hover:bg-red-200 text-red-700 px-2 py-1 rounded transition-colors"
                                >
                                    {formatTime(seconds)}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};