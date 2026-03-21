import React, { useState } from 'react';
import { WeightUnit, formatTime, convertWeight, formatWeight } from '../../types/exercise';

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
            <div className="flex items-center gap-0 bg-gray-100 rounded-2xl overflow-hidden border border-gray-200">
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
                        const parsed = parseFloat(e.target.value);
                        if (!isNaN(parsed) && parsed >= min) onChange(parsed);
                    }}
                    onBlur={() => {
                        const parsed = parseFloat(inputVal);
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

export const StrengthConfigSection: React.FC<StrengthConfigSectionProps> = ({
    targetSets, targetReps, targetWeight, targetWeightUnit, targetRpe,
    restSeconds, tempo, onSetsChange, onRepsChange, onWeightChange,
    onWeightUnitToggle, onRpeChange, onRestChange, onTempoChange,
    onWeightPresetClick, onRpePresetClick, getWeightPresets, getRpePresets
}) => {
    const [weightInput, setWeightInput] = useState<string>(targetWeight ? String(targetWeight) : '');

    return (
        <div className="space-y-5 pb-safe pb-6">

            {/* Sets & Reps - Stepper Row */}
            <div className="bg-gray-50 rounded-2xl p-4">
                <div className="flex justify-around">
                    <StepperInput
                        value={targetSets} min={1} max={20}
                        onChange={onSetsChange} label="Sets"
                    />
                    <div className="w-px bg-gray-200 self-stretch mx-2" />
                    <StepperInput
                        value={targetReps} min={1} max={100}
                        onChange={onRepsChange} label="Reps"
                    />
                </div>
            </div>

            {/* Weight */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold text-gray-700">Target Weight</span>
                    <button
                        type="button"
                        onClick={onWeightUnitToggle}
                        className="text-xs bg-white border border-gray-300 text-gray-600 px-3 py-1 rounded-full font-medium hover:bg-gray-100 transition-colors"
                    >
                        {targetWeightUnit} → {targetWeightUnit === 'lbs' ? 'kg' : 'lbs'}
                    </button>
                </div>
                <input
                    type="number"
                    min="0"
                    step="0.5"
                    value={weightInput}
                    onChange={(e) => {
                        setWeightInput(e.target.value);
                        onWeightChange(e.target.value ? parseFloat(e.target.value) : undefined);
                    }}
                    placeholder={`e.g. 135 ${targetWeightUnit}`}
                    className="w-full p-3 text-base border border-gray-200 rounded-xl bg-white focus:ring-2 focus:ring-purple-400 focus:border-purple-400 outline-none transition"
                />
                {targetWeight && (
                    <p className="text-xs text-gray-400 pl-1">
                        ≈ {formatWeight(convertWeight(targetWeight, targetWeightUnit, targetWeightUnit === 'lbs' ? 'kg' : 'lbs'), targetWeightUnit === 'lbs' ? 'kg' : 'lbs')}
                    </p>
                )}
                <div className="flex flex-wrap gap-2 pt-1">
                    {getWeightPresets().map((w) => (
                        <button
                            key={w}
                            type="button"
                            onClick={() => { setWeightInput(String(w)); onWeightPresetClick(w); }}
                            className="text-xs bg-white border border-gray-200 text-gray-600 px-3 py-1.5 rounded-full font-medium hover:border-purple-400 hover:text-purple-600 transition-colors"
                        >
                            {w}
                        </button>
                    ))}
                </div>
            </div>

            {/* Rest Time */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold text-gray-700">Rest Between Sets</span>
                    <span className="text-sm font-bold text-purple-600">{formatTime(restSeconds)}</span>
                </div>
                <div className="flex flex-wrap gap-2">
                    {[30, 60, 90, 120, 180, 300].map((s) => (
                        <button
                            key={s}
                            type="button"
                            onClick={() => onRestChange(s)}
                            className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                restSeconds === s
                                    ? 'bg-purple-100 border-purple-400 text-purple-700'
                                    : 'bg-white border-gray-200 text-gray-600 hover:border-purple-300'
                            }`}
                        >
                            {formatTime(s)}
                        </button>
                    ))}
                </div>
            </div>

            {/* RPE */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                <span className="text-sm font-semibold text-gray-700">RPE <span className="font-normal text-gray-400 text-xs">(optional)</span></span>
                <div className="flex flex-wrap gap-2 pt-1">
                    {getRpePresets().map((rpe) => (
                        <button
                            key={rpe}
                            type="button"
                            onClick={() => onRpeChange(rpe)}
                            className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                targetRpe === rpe
                                    ? 'bg-orange-100 border-orange-400 text-orange-700'
                                    : 'bg-white border-gray-200 text-gray-600 hover:border-orange-300'
                            }`}
                        >
                            {rpe}
                        </button>
                    ))}
                    {targetRpe && (
                        <button
                            type="button"
                            onClick={() => onRpeChange(undefined)}
                            className="text-xs px-3 py-1.5 rounded-full border border-red-200 text-red-500 hover:bg-red-50 transition-colors"
                        >
                            Clear
                        </button>
                    )}
                </div>
                <p className="text-xs text-gray-400">6–7 Easy · 8 Challenging · 9 Hard · 10 Max</p>
            </div>

            {/* Tempo */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-2">
                <span className="text-sm font-semibold text-gray-700">Tempo <span className="font-normal text-gray-400 text-xs">(optional)</span></span>
                <input
                    type="text"
                    value={tempo || ''}
                    onChange={(e) => onTempoChange(e.target.value || undefined)}
                    placeholder="e.g. 3-1-2-1"
                    className="w-full p-3 text-base border border-gray-200 rounded-xl bg-white focus:ring-2 focus:ring-purple-400 focus:border-purple-400 outline-none transition"
                />
                <p className="text-xs text-gray-400">eccentric · pause · concentric · pause</p>
            </div>

        </div>
    );
};