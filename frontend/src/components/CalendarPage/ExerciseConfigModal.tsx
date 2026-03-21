import React, { useState, useEffect } from 'react';
import {
    ScheduledExercise,
    Exercise,
    ExerciseConfiguration,
} from '../../types/exercise';
import { StrengthConfigSection } from '../ExerciseConfig/StrengthConfigSection';
import { CardioConfigSection } from '../ExerciseConfig/CardioConfigSection';
import { IsometricConfigSection } from '../ExerciseConfig/IsometricConfigSection';
import { useExerciseConfig } from '../../hooks/useExerciseConfig';
import { WorkoutPlanInfo } from '../../types/api';
import { StarIcon } from "@heroicons/react/24/outline";
import { StarIcon as StarIconSolid } from "@heroicons/react/24/solid";

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

const trackingModeColor = {
    strength: 'from-violet-600 to-purple-600',
    cardio: 'from-rose-500 to-orange-500',
    isometric: 'from-blue-500 to-cyan-500',
};

const ExerciseConfigModal: React.FC<ExerciseConfigModalProps> = ({
    isOpen, onClose, exercise, config, onConfigChange, onSave,
    selectedDate, loading, mode, onModeChange, onWorkoutPlanSelect,
    selectedWorkoutPlan, isEditMode, editingExercise, onFavoriteToggle
}) => {
    const [visible, setVisible] = useState(false);

    const configData = useExerciseConfig({
        exercise, config, onConfigChange, onFavoriteToggle
    });

    useEffect(() => {
        if (isOpen) {
            requestAnimationFrame(() => setVisible(true));
        } else {
            setVisible(false);
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const trackingMode = configData.trackingMode as 'strength' | 'cardio' | 'isometric';
    const gradientClass = trackingModeColor[trackingMode] || 'from-violet-600 to-purple-600';

    const saveLabel = loading
        ? 'Saving...'
        : isEditMode
            ? 'Update Exercise'
            : mode === 'workout-plan'
                ? 'Schedule Plan'
                : 'Add Exercise';

    return (
        <div
            className={`fixed inset-0 z-50 flex items-end sm:items-center justify-center transition-all duration-300 ${
                visible ? 'bg-black/60 backdrop-blur-sm' : 'bg-black/0'
            }`}
            onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
        >
            <div
                className={`
                    bg-white w-full sm:max-w-lg flex flex-col
                    rounded-t-3xl sm:rounded-3xl
                    shadow-2xl
                    transition-transform duration-300 ease-out
                    ${visible ? 'translate-y-0' : 'translate-y-full sm:translate-y-4'}
                `}
                style={{ maxHeight: 'calc(92vh - env(safe-area-inset-bottom, 0px))' }}
            >
                {/* ── HEADER ── */}
                <div className={`bg-gradient-to-r ${gradientClass} rounded-t-3xl sm:rounded-t-3xl px-5 pt-5 pb-4 flex-shrink-0`}>
                    {/* Drag handle — mobile only */}
                    <div className="flex justify-center mb-3 sm:hidden">
                        <div className="w-10 h-1 bg-white/40 rounded-full" />
                    </div>

                    <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0">
                            <p className="text-white/70 text-xs font-medium uppercase tracking-widest mb-0.5">
                                {isEditMode ? 'Editing' : 'Configure'}
                            </p>
                            <h2 className="text-white text-xl font-bold leading-tight truncate">
                                {exercise?.name ?? (mode === 'workout-plan' ? 'Workout Plan' : 'Exercise')}
                            </h2>
                            {exercise && (
                                <div className="flex gap-2 mt-2 flex-wrap">
                                    <span className="bg-white/20 text-white text-xs px-2.5 py-1 rounded-full font-medium">
                                        {exercise.exerciseType}
                                    </span>
                                    {exercise.targetMuscleGroups?.[0] && (
                                        <span className="bg-white/20 text-white text-xs px-2.5 py-1 rounded-full font-medium">
                                            {exercise.targetMuscleGroups[0]}
                                        </span>
                                    )}
                                </div>
                            )}
                        </div>

                        <div className="flex items-center gap-2 flex-shrink-0">
                            {exercise && mode === 'exercise' && (
                                <button
                                    onClick={configData.handleToggleFavorite}
                                    className="w-9 h-9 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors"
                                    title={configData.isFavorited ? 'Remove from favorites' : 'Add to favorites'}
                                >
                                    {configData.isFavorited
                                        ? <StarIconSolid className="w-5 h-5 text-yellow-300" />
                                        : <StarIcon className="w-5 h-5 text-white" />
                                    }
                                </button>
                            )}
                            <button
                                onClick={onClose}
                                className="w-9 h-9 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center text-white transition-colors text-lg font-bold"
                                aria-label="Close"
                            >
                                ×
                            </button>
                        </div>
                    </div>
                </div>

                {/* ── MODE TABS ── */}
                <div className="flex-shrink-0 px-5 pt-4 pb-2">
                    <div className="flex bg-gray-100 rounded-2xl p-1 gap-1">
                        {(['exercise', 'workout-plan'] as const).map((m) => (
                            <button
                                key={m}
                                type="button"
                                onClick={() => onModeChange(m)}
                                className={`flex-1 py-2 text-sm font-semibold rounded-xl transition-all duration-200 ${
                                    mode === m
                                        ? 'bg-white text-gray-900 shadow-sm'
                                        : 'text-gray-500 hover:text-gray-700'
                                }`}
                            >
                                {m === 'exercise' ? '💪 Exercise' : '📋 Workout Plan'}
                            </button>
                        ))}
                    </div>
                </div>

                {/* ── SCROLLABLE BODY ── */}
                <div className="flex-1 overflow-y-auto px-5 pb-2 overscroll-contain">

                    {/* Workout Plan Mode */}
                    {mode === 'workout-plan' && (
                        <div className="py-4">
                            {selectedWorkoutPlan ? (
                                <div className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl p-4 border border-blue-100">
                                    <h4 className="font-bold text-blue-900 text-base">{selectedWorkoutPlan.name}</h4>
                                    <p className="text-sm text-gray-600 mt-1">{selectedWorkoutPlan.description}</p>
                                    <div className="flex flex-wrap gap-2 mt-3">
                                        <span className="text-xs bg-blue-100 text-blue-800 px-2.5 py-1 rounded-full font-medium">
                                            {selectedWorkoutPlan.difficulty}
                                        </span>
                                        <span className="text-xs bg-green-100 text-green-800 px-2.5 py-1 rounded-full font-medium">
                                            {selectedWorkoutPlan.exerciseCount} exercises
                                        </span>
                                        <span className="text-xs bg-orange-100 text-orange-800 px-2.5 py-1 rounded-full font-medium">
                                            {selectedWorkoutPlan.estimatedDurationMinutes} min
                                        </span>
                                    </div>
                                </div>
                            ) : (
                                <div className="text-center py-12">
                                    <div className="text-5xl mb-4">📋</div>
                                    <p className="text-gray-500 text-sm mb-4">No workout plan selected yet</p>
                                    <button
                                        type="button"
                                        className="px-5 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-semibold hover:bg-blue-700 transition-colors"
                                        onClick={() => console.log('Open workout plan selector')}
                                    >
                                        Browse Workout Plans
                                    </button>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Exercise Mode */}
                    {mode === 'exercise' && exercise && (
                        <>
                            {configData.trackingMode === 'strength' && (
                                <StrengthConfigSection
                                    targetSets={configData.targetSets}
                                    targetReps={configData.targetReps}
                                    targetWeight={configData.targetWeight}
                                    targetWeightUnit={configData.targetWeightUnit}
                                    targetRpe={configData.targetRpe}
                                    restSeconds={configData.restSeconds}
                                    tempo={configData.tempo}
                                    onSetsChange={configData.setTargetSets}
                                    onRepsChange={configData.setTargetReps}
                                    onWeightChange={configData.setTargetWeight}
                                    onWeightUnitToggle={configData.handleWeightUnitToggle}
                                    onRpeChange={configData.setTargetRpe}
                                    onRestChange={configData.setRestSeconds}
                                    onTempoChange={configData.setTempo}
                                    onWeightPresetClick={configData.handleWeightPresetClick}
                                    onRpePresetClick={configData.handleRpePresetClick}
                                    getWeightPresets={configData.getWeightPresets}
                                    getRpePresets={configData.getRpePresets}
                                />
                            )}

                            {configData.trackingMode === 'cardio' && (
                                <CardioConfigSection
                                    exercise={exercise}
                                    targetDurationMinutes={configData.targetDurationMinutes}
                                    targetDistance={configData.targetDistance}
                                    targetDistanceUnit={configData.targetDistanceUnit}
                                    targetPace={configData.targetPace}
                                    targetSets={configData.targetSets}
                                    isometricRestSeconds={configData.isometricRestSeconds}
                                    onDurationChange={configData.setTargetDurationMinutes}
                                    onDistanceChange={configData.setTargetDistance}
                                    onDistanceUnitToggle={configData.handleDistanceUnitToggle}
                                    onPaceChange={configData.setTargetPace}
                                    onSetsChange={configData.setTargetSets}
                                    onRestChange={configData.setIsometricRestSeconds}
                                    onDistancePresetClick={configData.handleDistancePresetClick}
                                />
                            )}

                            {configData.trackingMode === 'isometric' && (
                                <IsometricConfigSection
                                    targetHoldSeconds={configData.targetHoldSeconds}
                                    isometricSets={configData.isometricSets}
                                    isometricRestSeconds={configData.isometricRestSeconds}
                                    onHoldSecondsChange={configData.setTargetHoldSeconds}
                                    onSetsChange={configData.setIsometricSets}
                                    onRestChange={configData.setIsometricRestSeconds}
                                />
                            )}

                            {/* Notes */}
                            <div className="mt-2 mb-4">
                                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
                                    Notes <span className="normal-case font-normal text-gray-400">(optional)</span>
                                </label>
                                <textarea
                                    value={configData.notes}
                                    onChange={(e) => configData.setNotes(e.target.value)}
                                    placeholder="Any notes about this exercise..."
                                    rows={2}
                                    className="w-full p-3 text-sm border border-gray-200 rounded-xl bg-gray-50 focus:ring-2 focus:ring-purple-400 focus:border-purple-400 outline-none transition resize-none"
                                />
                            </div>
                        </>
                    )}
                </div>

                {/* ── STICKY FOOTER ── */}
                <div
                    className="flex-shrink-0 px-5 pt-3 pb-5 bg-white border-t border-gray-100 rounded-b-3xl sm:rounded-b-3xl"
                    style={{ paddingBottom: 'max(1.25rem, env(safe-area-inset-bottom, 1.25rem))' }}
                >
                    <div className="flex gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={loading}
                            className="flex-1 py-3.5 text-sm font-semibold text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-2xl transition-colors disabled:opacity-50"
                        >
                            Cancel
                        </button>
                        <button
                            type="button"
                            onClick={onSave}
                            disabled={loading || (mode === 'exercise' && !exercise) || (mode === 'workout-plan' && !selectedWorkoutPlan)}
                            className={`flex-2 flex-grow py-3.5 text-sm font-bold text-white bg-gradient-to-r ${gradientClass} rounded-2xl shadow-lg hover:opacity-90 active:scale-[0.98] transition-all disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2`}
                        >
                            {loading && (
                                <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                                </svg>
                            )}
                            {saveLabel}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ExerciseConfigModal;