import React, {useState, useEffect} from 'react';
import {
    ScheduledExercise,
    Exercise,
    ExerciseConfiguration,
} from '../../types/exercise';
import {StrengthConfigSection} from '../ExerciseConfig/StrengthConfigSection';
import {CardioConfigSection} from '../ExerciseConfig/CardioConfigSection';
import {IsometricConfigSection} from '../ExerciseConfig/IsometricConfigSection';
import {useExerciseConfig} from '../../hooks/useExerciseConfig';
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

    const configData = useExerciseConfig({
        exercise,
        config,
        onConfigChange,
        onFavoriteToggle
    });

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
                                onClick={configData.handleToggleFavorite}
                                className={`
            p-2 rounded-full transition-all duration-200
            active:scale-95 shadow-sm hover:shadow-md border
            ${configData.isFavorited
                                    ? 'text-yellow-500 bg-yellow-100 hover:bg-yellow-200 border-yellow-300'
                                    : 'text-gray-400 bg-gray-100 hover:bg-yellow-100 border-gray-300'
                                }
        `}
                                title={configData.isFavorited ? 'Remove from favorites' : 'Add to favorites'}
                            >
                                {configData.isFavorited ? (
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

                        {/* =============================================================================
                            CARDIO CONFIGURATION
                            ============================================================================= */}
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

                        {/* =============================================================================
                            ISOMETRIC CONFIGURATION
                            ============================================================================= */}
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

                        {/* =============================================================================
                            NOTES SECTION
                            ============================================================================= */}
                        <div className="mt-6">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Notes (Optional)
                            </label>
                            <textarea
                                value={configData.notes}
                                onChange={(e) => configData.setNotes(e.target.value)}
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