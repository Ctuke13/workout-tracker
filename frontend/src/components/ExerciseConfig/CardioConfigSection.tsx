import React, { useState } from 'react';
import {
    DistanceUnit, formatTime, convertDistance, formatDistance,
    getDistancePresets, getCardioSessionType, Exercise
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

export const CardioConfigSection: React.FC<CardioConfigSectionProps> = ({
    exercise, targetDurationMinutes, targetDistance, targetDistanceUnit,
    targetPace, targetSets, isometricRestSeconds, onDurationChange,
    onDistanceChange, onDistanceUnitToggle, onPaceChange, onSetsChange,
    onRestChange, onDistancePresetClick
}) => {
    const [setsInput, setSetsInput] = useState<string>(String(targetSets));
    const [distanceInput, setDistanceInput] = useState<string>(targetDistance ? String(targetDistance) : '');
    const [paceInput, setPaceInput] = useState<string>(targetPace ? String(targetPace) : '');

    const sessionType = getCardioSessionType(exercise);

    const formatPaceDisplay = (pace: number, unit: DistanceUnit): string => {
        const minutes = Math.floor(pace);
        const seconds = Math.round((pace - minutes) * 60);
        return `${minutes}:${seconds.toString().padStart(2, '0')} min/${unit === 'miles' ? 'mi' : 'km'}`;
    };

    const getPacePresets = (unit: DistanceUnit): number[] =>
        unit === 'miles' ? [6, 7, 8, 9, 10, 11, 12] : [3.5, 4, 4.5, 5, 5.5, 6, 6.5, 7];

    const getDistanceLabel = (distance: number): string => {
        const labels: Record<string, string> = {
            '3.1-miles': '5K', '6.2-miles': '10K', '13.1-miles': 'Half', '26.2-miles': 'Marathon',
            '5-km': '5K', '10-km': '10K', '21.1-km': 'Half', '42.2-km': 'Marathon',
        };
        return labels[`${distance}-${targetDistanceUnit}`] || String(distance);
    };

    return (
        <div className="space-y-5 pb-safe pb-6">

            {/* Interval Alert */}
            {sessionType.type === 'interval_sets' && (
                <div className="bg-orange-50 border border-orange-200 rounded-2xl p-3 flex items-start gap-2">
                    <span className="text-orange-500 text-lg">⚡</span>
                    <div>
                        <p className="text-sm font-semibold text-orange-800">Interval Training</p>
                        <p className="text-xs text-orange-600 mt-0.5">{sessionType.description}</p>
                    </div>
                </div>
            )}

            {/* Duration — Primary */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold text-gray-700">⏱ Duration <span className="text-red-500">*</span></span>
                    <span className="text-sm font-bold text-red-500">{targetDurationMinutes} min</span>
                </div>
                <div className="flex flex-wrap gap-2">
                    {(sessionType.type === 'interval_sets'
                        ? [5, 10, 15, 20, 30, 45]
                        : [15, 20, 30, 45, 60, 75, 90]
                    ).map((m) => (
                        <button
                            key={m}
                            type="button"
                            onClick={() => onDurationChange(m)}
                            className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                targetDurationMinutes === m
                                    ? 'bg-red-100 border-red-400 text-red-700'
                                    : 'bg-white border-gray-200 text-gray-600 hover:border-red-300'
                            }`}
                        >
                            {m}m
                        </button>
                    ))}
                </div>
                <input
                    type="number"
                    min="1"
                    max="300"
                    value={targetDurationMinutes}
                    onChange={(e) => onDurationChange(parseInt(e.target.value) || 1)}
                    placeholder="Custom minutes"
                    className="w-full p-3 text-base border border-gray-200 rounded-xl bg-white focus:ring-2 focus:ring-red-400 focus:border-red-400 outline-none transition"
                />
            </div>

            {/* Interval Sets */}
            {sessionType.showSetsInConfig && (
                <div className="bg-gray-50 rounded-2xl p-4">
                    <div className="flex justify-center">
                        <StepperInput
                            value={targetSets} min={1} max={20}
                            onChange={onSetsChange} label="Sets / Rounds"
                        />
                    </div>
                    <div className="mt-3 pt-3 border-t border-gray-200 flex justify-center flex-wrap gap-2">
                        {[3, 4, 5, 6, 8, 10].map((s) => (
                            <button
                                key={s}
                                type="button"
                                onClick={() => { setSetsInput(String(s)); onSetsChange(s); }}
                                className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                    targetSets === s
                                        ? 'bg-orange-100 border-orange-400 text-orange-700'
                                        : 'bg-white border-gray-200 text-gray-600 hover:border-orange-300'
                                }`}
                            >
                                {s}
                            </button>
                        ))}
                    </div>
                </div>
            )}

            {/* Distance */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                <div className="flex items-center justify-between">
                    <span className="text-sm font-semibold text-gray-700">📍 Distance <span className="text-xs font-normal text-gray-400">(optional)</span></span>
                    <button
                        type="button"
                        onClick={onDistanceUnitToggle}
                        className="text-xs bg-white border border-gray-300 text-gray-600 px-3 py-1 rounded-full font-medium hover:bg-gray-100 transition-colors"
                    >
                        {targetDistanceUnit} → {targetDistanceUnit === 'miles' ? 'km' : 'miles'}
                    </button>
                </div>
                <input
                    type="number"
                    min="0"
                    step="0.1"
                    value={distanceInput}
                    onChange={(e) => {
                        setDistanceInput(e.target.value);
                        onDistanceChange(e.target.value ? parseFloat(e.target.value) : undefined);
                    }}
                    placeholder={`Enter ${targetDistanceUnit}`}
                    className="w-full p-3 text-base border border-gray-200 rounded-xl bg-white focus:ring-2 focus:ring-red-400 focus:border-red-400 outline-none transition"
                />
                {targetDistance && (
                    <p className="text-xs text-gray-400 pl-1">
                        ≈ {formatDistance(convertDistance(targetDistance, targetDistanceUnit, targetDistanceUnit === 'miles' ? 'km' : 'miles'), targetDistanceUnit === 'miles' ? 'km' : 'miles')}
                    </p>
                )}
                <div className="flex flex-wrap gap-2">
                    {getDistancePresets(targetDistanceUnit).map((d) => (
                        <button
                            key={d}
                            type="button"
                            onClick={() => { setDistanceInput(String(d)); onDistancePresetClick(d); }}
                            className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                targetDistance === d
                                    ? 'bg-red-100 border-red-400 text-red-700'
                                    : 'bg-white border-gray-200 text-gray-600 hover:border-red-300'
                            }`}
                        >
                            {getDistanceLabel(d)}
                        </button>
                    ))}
                </div>
            </div>

            {/* Pace */}
            <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                <span className="text-sm font-semibold text-gray-700">⚡ Pace <span className="text-xs font-normal text-gray-400">(optional)</span></span>
                <input
                    type="number"
                    min="3"
                    max="20"
                    step="0.1"
                    value={paceInput}
                    onChange={(e) => {
                        setPaceInput(e.target.value);
                        onPaceChange(e.target.value ? parseFloat(e.target.value) : undefined);
                    }}
                    placeholder={`min/${targetDistanceUnit === 'miles' ? 'mile' : 'km'}`}
                    className="w-full p-3 text-base border border-gray-200 rounded-xl bg-white focus:ring-2 focus:ring-red-400 focus:border-red-400 outline-none transition"
                />
                {targetPace && (
                    <p className="text-xs text-gray-400 pl-1">{formatPaceDisplay(targetPace, targetDistanceUnit)}</p>
                )}
                <div className="flex flex-wrap gap-2">
                    {getPacePresets(targetDistanceUnit).map((pace) => (
                        <button
                            key={pace}
                            type="button"
                            onClick={() => { setPaceInput(String(pace)); onPaceChange(pace); }}
                            className={`text-xs px-3 py-1.5 rounded-full font-medium transition-colors border ${
                                targetPace === pace
                                    ? 'bg-red-100 border-red-400 text-red-700'
                                    : 'bg-white border-gray-200 text-gray-600 hover:border-red-300'
                            }`}
                        >
                            {Math.floor(pace)}:{((pace % 1) * 60).toFixed(0).padStart(2, '0')}
                        </button>
                    ))}
                </div>
            </div>

            {/* Rest - Interval only */}
            {sessionType.showSetsInConfig && (
                <div className="bg-gray-50 rounded-2xl p-4 space-y-3">
                    <div className="flex items-center justify-between">
                        <span className="text-sm font-semibold text-gray-700">💤 Rest Between Sets</span>
                        <span className="text-sm font-bold text-purple-600">{formatTime(isometricRestSeconds)}</span>
                    </div>
                    <div className="flex flex-wrap gap-2">
                        {[15, 30, 45, 60, 90, 120].map((s) => (
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
            )}

        </div>
    );
};