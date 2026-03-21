import React, { useState } from 'react';
import { formatTime } from '../../types/exercise';

interface IsometricConfigSectionProps {
    targetHoldSeconds: number;
    isometricSets: number;
    isometricRestSeconds: number;
    onHoldSecondsChange: (seconds: number) => void;
    onSetsChange: (sets: number) => void;
    onRestChange: (seconds: number) => void;
}

const StepperInput: React.FC<{
    value: number;
    min: number;
    max: number;
    step?: number;
    onChange: (val: number) => void;
    label: string;
    unit?: string;
}> = ({ value, min, max, step = 1, onChange, label, unit }) => {
    const [inputVal, setInputVal] = useState<string>(String(value));

    const handleDecrement = () => {
        const next = Math.max(min, value - step);
        setInputVal(String(next));
        onChange(next);
    };
    const handleIncrement = () => {
        const next = Math.min(max, value + step);
        setInputVal(String(next));
        onChange(next);
    };

    return (
        <div className="flex flex-col items-center gap-2">
            <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">{label}</span>
            <div className="flex items-center bg-gray-100 rounded-2xl overflow-hidden border border-gray-200">
                <button
                    type="button"
                    onClick={handleDecrement}
                    className="w-11 h-11 flex items-center justify-center text-gray-600 hover:bg-gray-200 active:bg-gray-300 transition-colors text-xl font-light select-none"
                >
                    −
                </button>
                <input
                    type="number"
                    value={inputVal}
                    onChange={(e) => {
                        setInputVal(e.target.value);
                        const parsed = parseInt(e.target.value);
                        if (!isNaN(parsed) && parsed >= min) onChange(parsed);
                    }}
                    onBlur={() => {
                        const parsed = parseInt(inputVal);
                        if (isNaN(parsed) || parsed < min) {
                            setInputVal(String(value));
                        }
                    }}
                    className="w-14 h-11 text-center text-lg font-bold text-gray-800 bg-transparent border-none outline-none [-moz-appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
                    inputMode="numeric"
                />
                <button
                    type="button"
                    onClick={handleIncrement}
                    className="w-11 h-11 flex items-center justify-center text-gray-600 hover:bg-gray-200 active:bg-gray-300 transition-colors text-xl font-light select-none"
                >
                    +
                </button>
            </div>
            {unit && <span className="text-xs text-gray-400">{unit}</span>}
        </div>
    );
};

export const IsometricConfigSection: React.FC<IsometricConfigSectionProps> = ({
    targetHoldSeconds,
    isometricSets,
    isometricRestSeconds,
    onHoldSecondsChange,
    onSetsChange,
    onRestChange
}) => {
    return (
        <div className="space-y-5 pb-safe pb-6">

            {/* Hold Time & Sets - Stepper Row */}
            <div className="bg-gray-50 rounded-2xl p-4">
                <div className="flex justify-around">
                    <StepperInput
                        value={targetHoldSeconds} min={5} max={300} step={5}
                        onChange={onHoldSecondsChange} label="Hold Time" unit="seconds"
                    />
                    <div className="w-px bg-gray-200 self-stretch mx-2" />
                    <StepperInput
                        value={isometricSets} min={1} max={10}
                        onChange={onSetsChange} label="Sets"
                    />
                </div>

                {/* Hold Time Quick Presets */}
                <div className="mt-4 pt-3 border-t border-gray-200">
                    <p className="text-xs text-gray-400 mb-2 text-center">Quick hold times</p>
                    <div className="flex justify-center flex-wrap gap-2">
                        {[15, 30, 45, 60, 90, 120].map((s) => (
                            <button
                                key={s}
                                type="button"
                                onClick={() => onHoldSecondsChange(s)}
                                className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                    targetHoldSeconds === s
                                        ? 'bg-blue-100 border-blue-400 text-blue-700'
                                        : 'bg-white border-gray-200 text-gray-600 hover:border-blue-300'
                                }`}
                            >
                                {s}s
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* Rest Time */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold text-gray-700">Rest Between Sets</span>
                    <span className="text-sm font-bold text-purple-600">{formatTime(isometricRestSeconds)}</span>
                </div>
                <div className="flex flex-wrap gap-2">
                    {[30, 60, 90, 120, 180].map((s) => (
                        <button
                            key={s}
                            type="button"
                            onClick={() => onRestChange(s)}
                            className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                isometricRestSeconds === s
                                    ? 'bg-purple-100 border-purple-400 text-purple-700'
                                    : 'bg-white border-gray-200 text-gray-600 hover:border-purple-300'
                            }`}
                        >
                            {formatTime(s)}
                        </button>
                    ))}
                </div>
            </div>

        </div>
    );
};