import React, {useState, useEffect} from 'react';
import {
    Calendar,
    Clock,
    Target,
    Weight,
    Timer,
    RotateCcw,
    Activity,
    ChevronDown,
    ChevronUp,
    X,
    Save,
    Play,
    AlertCircle,
    CheckCircle,
    Settings,
    SkipForward,
    Loader
} from 'lucide-react';
import {WorkoutPlanInfo} from '../../types/api';
import {
    Exercise,
    ExerciseConfiguration,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    getWorkoutTrackingType,
} from '../../types/exercise';
import {useAuth} from '../../contexts/AuthContext';
import {useWorkoutPlanConfig, WorkoutPlanConfiguration} from '../../hooks/useWorkoutPlanConfig';
import ExerciseConfigurationForm from '../forms/ExerciseConfigurationForm';

// =============================================================================
// INTERFACES
// =============================================================================

interface WorkoutPlanConfigModalProps {
    isOpen: boolean;
    onClose: () => void;
    workoutPlan: WorkoutPlanInfo | null;
    selectedDate: Date;
    onSchedule: (config: WorkoutPlanConfiguration) => void;
    loading: boolean;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

const WorkoutPlanConfigModal: React.FC<WorkoutPlanConfigModalProps> = ({
                                                                           isOpen,
                                                                           onClose,
                                                                           workoutPlan,
                                                                           selectedDate,
                                                                           onSchedule,
                                                                           loading
                                                                       }) => {
    const {user} = useAuth();

    const configData = useWorkoutPlanConfig({
        isOpen,
        workoutPlan,
        selectedDate
    });

    const handleSchedule = () => {
        const finalConfig = configData.buildConfiguration();
        if (finalConfig) {
            onSchedule(finalConfig);
        }
    };

    // =============================================================================
    // COMPUTED VALUES
    // =============================================================================

    const accessibleExercises = configData.accessibleExercises;
    const totalEstimatedDuration = configData.totalEstimatedDuration;
    const lockedExercises = configData.exercisesInPlan.filter(ex => !ex.isAccessible);

    // =============================================================================
    // RENDER GUARDS
    // =============================================================================

    if (!isOpen || !workoutPlan) return null;

    const canSchedule = configData.configuredCount >= accessibleExercises.length;

    // =============================================================================
    // RENDER COMPONENT
    // =============================================================================

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-3xl max-w-4xl w-full max-h-[95vh] flex flex-col shadow-2xl">

                {/* Header */}
                <div className="bg-gradient-to-r from-purple-600 to-blue-600 rounded-t-3xl p-6 text-white">
                    <div className="flex items-center justify-between mb-4">
                        <h2 className="text-2xl font-bold">Configure Your Workout</h2>
                        <button
                            onClick={onClose}
                            className="p-2 hover:bg-white/20 rounded-xl transition-colors"
                        >
                            <X className="w-6 h-6"/>
                        </button>
                    </div>

                    <div className="flex items-center gap-4 mb-4">
                        <div className="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center text-2xl">
                            📋
                        </div>
                        <div>
                            <h3 className="text-xl font-semibold">
                                {workoutPlan.name || workoutPlan.workoutName}
                            </h3>
                            <p className="text-white/80 text-sm">
                                Configure your targets for each exercise
                            </p>
                        </div>
                    </div>

                    {/* Progress Bar */}
                    <div className="bg-white/20 rounded-full h-2 mb-2">
                        <div
                            className="bg-white rounded-full h-2 transition-all duration-300"
                            style={{width: `${(configData.configuredCount / accessibleExercises.length) * 100}%`}}
                        />
                    </div>
                    <div className="flex justify-between text-sm text-white/80">
                        <span>{configData.configuredCount} of {accessibleExercises.length} exercises configured</span>
                        <span>{totalEstimatedDuration} min total</span>
                    </div>
                </div>

                {/* Exercise Configuration Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    {/* Loading State */}
                    {configData.loadingExercises && (
                        <div className="flex items-center justify-center py-12">
                            <div className="text-center">
                                <Loader className="w-8 h-8 animate-spin text-purple-600 mx-auto mb-4"/>
                                <p className="text-gray-600">Loading workout exercises...</p>
                            </div>
                        </div>
                    )}

                    {/* Error State */}
                    {configData.exerciseError && (
                        <div className="p-4 bg-red-50 border border-red-200 rounded-xl mb-6">
                            <div className="flex items-center gap-2 text-red-700">
                                <AlertCircle className="w-5 h-5"/>
                                <span className="font-medium">Error Loading Exercises</span>
                            </div>
                            <p className="text-sm text-red-600 mt-1">{configData.exerciseError}</p>
                            <button
                                onClick={() => window.location.reload()}
                                className="mt-2 px-3 py-1 bg-red-100 hover:bg-red-200 text-red-700 text-sm rounded-lg transition-colors"
                            >
                                Retry
                            </button>
                        </div>
                    )}

                    {/* Exercises List */}
                    {!configData.loadingExercises && !configData.exerciseError && configData.exercisesInPlan.length > 0 && (
                        <div className="space-y-4">
                            {configData.exercisesInPlan.map((exerciseInPlan, index) => {
                                const exercise = exerciseInPlan.exercise;
                                const config = configData.exerciseConfigs[exerciseInPlan.id];
                                const isExpanded = configData.expandedExercises.has(exerciseInPlan.id);
                                const isConfigured = !!config && !config.skip;
                                const trackingType = getWorkoutTrackingType(exercise);

                                return (
                                    <div
                                        key={exerciseInPlan.id}
                                        className={`border-2 rounded-2xl transition-all duration-200 ${
                                            !exerciseInPlan.isAccessible
                                                ? 'border-gray-200 bg-gray-50 opacity-60'
                                                : isConfigured
                                                    ? 'border-green-200 bg-green-50'
                                                    : isExpanded
                                                        ? 'border-purple-300 bg-white shadow-lg'
                                                        : 'border-gray-200 bg-white hover:border-gray-300'
                                        }`}
                                    >
                                        {/* Exercise Header */}
                                        <div
                                            className="p-4 cursor-pointer"
                                            onClick={() => exerciseInPlan.isAccessible && configData.toggleExercise(exerciseInPlan.id)}
                                        >
                                            <div className="flex items-center justify-between">
                                                <div className="flex items-center gap-4">
                                                    <div
                                                        className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-white ${
                                                            isConfigured ? 'bg-green-500' : 'bg-purple-500'
                                                        }`}>
                                                        {isConfigured ? <CheckCircle className="w-5 h-5"/> : index + 1}
                                                    </div>

                                                    <div>
                                                        <div className="flex items-center gap-2 mb-1">
                                                            <h4 className="font-bold text-lg text-gray-900">
                                                                {exercise.emoji} {exercise.name}
                                                            </h4>
                                                            {!exerciseInPlan.isAccessible && (
                                                                <span
                                                                    className="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full">
                                                                    Premium
                                                                </span>
                                                            )}
                                                        </div>
                                                        <div className="flex items-center gap-3 text-sm text-gray-600">
                                                            <span
                                                                className={`px-2 py-1 rounded-full text-xs font-medium ${
                                                                    trackingType === 'cardio' ? 'bg-red-100 text-red-700' :
                                                                        trackingType === 'isometric' ? 'bg-purple-100 text-purple-700' :
                                                                            'bg-blue-100 text-blue-700'
                                                                }`}>
                                                                {trackingType === 'cardio' ? '❤️ Cardio' :
                                                                    trackingType === 'isometric' ? '🛡️ Hold' :
                                                                        '💪 Strength'}
                                                            </span>
                                                            <span>{exercise.difficultyLevel}</span>
                                                            <span>{exercise.estimatedDurationMinutes} min</span>
                                                        </div>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-2">
                                                    {exerciseInPlan.isAccessible && (
                                                        <label className="flex items-center gap-2 text-sm">
                                                            <input
                                                                type="checkbox"
                                                                checked={config?.skip || false}
                                                                onChange={(e) => configData.handleSkipExercise(exerciseInPlan.id, e.target.checked)}
                                                                className="rounded"
                                                            />
                                                            Skip
                                                        </label>
                                                    )}
                                                    {isExpanded ?
                                                        <ChevronUp className="w-5 h-5 text-gray-400"/> :
                                                        <ChevronDown className="w-5 h-5 text-gray-400"/>
                                                    }
                                                </div>
                                            </div>
                                        </div>

                                        {/* Exercise Configuration */}
                                        {isExpanded && exerciseInPlan.isAccessible && config && !config.skip && (
                                            <div className="border-t border-gray-200 p-6 bg-gray-50">
                                                <ExerciseConfigurationForm
                                                    exercise={exercise}
                                                    configuration={config.configuration}
                                                    onConfigurationChange={(newConfig) => configData.updateExerciseConfiguration(exerciseInPlan.id, newConfig)}
                                                    prescribedValues={{
                                                        sets: exerciseInPlan.prescribedSets,
                                                        reps: exerciseInPlan.prescribedReps,
                                                        weight: exerciseInPlan.prescribedWeight,
                                                        duration: exerciseInPlan.prescribedDuration,
                                                        rest: exerciseInPlan.prescribedRest
                                                    }}
                                                    instructions={exerciseInPlan.instructions}
                                                />

                                                {/* Exercise Notes */}
                                                <div className="mt-4">
                                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                                        Personal Notes
                                                    </label>
                                                    <textarea
                                                        value={config.notes || ''}
                                                        onChange={(e) => configData.updateExerciseConfig(exerciseInPlan.id, {notes: e.target.value})}
                                                        placeholder="Add any personal notes for this exercise..."
                                                        rows={2}
                                                        className="w-full p-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500 resize-none"
                                                    />
                                                </div>
                                            </div>
                                        )}

                                        {/* Skipped Exercise Display */}
                                        {config?.skip && (
                                            <div className="border-t border-gray-200 p-4 bg-yellow-50">
                                                <div className="flex items-center gap-2 text-yellow-700">
                                                    <AlertCircle className="w-4 h-4"/>
                                                    <span
                                                        className="text-sm font-medium">This exercise will be skipped</span>
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {/* Empty State */}
                    {!configData.loadingExercises && !configData.exerciseError && configData.exercisesInPlan.length === 0 && (
                        <div className="text-center py-12">
                            <div
                                className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-gray-100 to-gray-200 rounded-full flex items-center justify-center">
                                <AlertCircle className="w-10 h-10 text-gray-400"/>
                            </div>
                            <h3 className="text-xl font-bold text-gray-900 mb-2">No Exercises Found</h3>
                            <p className="text-gray-500 max-w-sm mx-auto">
                                This workout plan doesn't have any exercises configured yet.
                            </p>
                        </div>
                    )}

                    {/* ✅ FIXED: Plan Notes - Only show if exercises are loaded */}
                    {!configData.loadingExercises && configData.exercisesInPlan.length > 0 && (
                        <>
                            <div className="mt-6 p-4 bg-gray-50 rounded-2xl">
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Workout Plan Notes
                                </label>
                                <textarea
                                    value={configData.planNotes}
                                    onChange={(e) => configData.setPlanNotes(e.target.value)}
                                    placeholder="Add any overall notes about this workout plan..."
                                    rows={3}
                                    className="w-full p-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500 resize-none"
                                />
                            </div>

                            {/* ✅ FIXED: Reminder Settings with proper JSX structure */}
                            <div className="mt-4 p-4 bg-gray-50 rounded-2xl">
                                <div className="flex items-center justify-between mb-3">
                                    <label className="text-sm font-medium text-gray-700">
                                        Workout Reminder
                                    </label>
                                    <label className="relative inline-flex items-center cursor-pointer">
                                        <input
                                            type="checkbox"
                                            checked={configData.reminderEnabled}
                                            onChange={(e) => configData.setReminderEnabled(e.target.checked)}
                                            className="sr-only peer"
                                        />
                                        <div
                                            className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-purple-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-600"></div>
                                    </label>
                                </div>
                                {configData.reminderEnabled && (
                                    <input
                                        type="time"
                                        value={configData.reminderTime}
                                        onChange={(e) => configData.setReminderTime(e.target.value)}
                                        className="w-full p-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                                    />
                                )}
                            </div>
                        </>
                    )}
                </div>

                {/* ✅ FIXED: Footer with proper JSX structure */}
                <div className="border-t border-gray-200 p-6 bg-gray-50 rounded-b-3xl">
                    <div className="flex items-center justify-between mb-4">
                        <div className="text-sm text-gray-600">
                            <div className="font-medium">Ready to schedule:</div>
                            <div>{configData.configuredCount} exercises • {totalEstimatedDuration} minutes</div>
                        </div>
                        <div className="text-sm text-gray-600">
                            {selectedDate.toLocaleDateString('en-US', {
                                weekday: 'long',
                                month: 'long',
                                day: 'numeric'
                            })}
                        </div>
                    </div>

                    <div className="flex gap-3">
                        <button
                            onClick={onClose}
                            className="flex-1 px-6 py-3 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-xl hover:bg-gray-50 transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleSchedule}
                            disabled={loading || !canSchedule || configData.loadingExercises}
                            className="flex-1 px-6 py-3 text-sm font-medium text-white bg-purple-600 border border-transparent rounded-xl hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
                        >
                            {loading ? (
                                <>
                                    <div
                                        className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                    Scheduling...
                                </>
                            ) : configData.loadingExercises ? (
                                <>
                                    <Loader className="w-4 h-4 animate-spin"/>
                                    Loading...
                                </>
                            ) : (
                                <>
                                    <Play className="w-4 h-4"/>
                                    Schedule Workout
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};


export default WorkoutPlanConfigModal;