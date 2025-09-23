import React from 'react';
import {formatTime} from '../../types/exercise';

interface IsometricConfigSectionProps {
    targetHoldSeconds: number;
    isometricSets: number;
    isometricRestSeconds: number;
    onHoldSecondsChange: (seconds: number) => void;
    onSetsChange: (sets: number) => void;
    onRestChange: (seconds: number) => void;
}

export const IsometricConfigSection: React.FC<IsometricConfigSectionProps> = ({
                                                                                  targetHoldSeconds,
                                                                                  isometricSets,
                                                                                  isometricRestSeconds,
                                                                                  onHoldSecondsChange,
                                                                                  onSetsChange,
                                                                                  onRestChange
                                                                              }) => {
    return (
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
                        onChange={(e) => onHoldSecondsChange(parseInt(e.target.value) || 5)}
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
                                    onClick={() => onHoldSecondsChange(seconds)}
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
                        onChange={(e) => onSetsChange(parseInt(e.target.value) || 1)}
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
                    onChange={(e) => onRestChange(parseInt(e.target.value) || 0)}
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