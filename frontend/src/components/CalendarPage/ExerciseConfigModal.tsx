import React, { useState, useEffect } from 'react';
import { Calendar, Clock, Target, Zap, Weight, Repeat, Timer, MapPin, Gauge, Sparkles, Heart, Flame, AlertTriangle } from 'lucide-react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Badge } from '../ui/badge';

// Import your types (these should match your actual imports)
import {
    Exercise,
    ExerciseConfiguration,
    StrengthConfiguration,
    CardioConfiguration,
    IsometricConfiguration,
    getDefaultConfigForExercise
} from '../../types/exercise';

// Import WorkoutPlanInfo from api types
import { WorkoutPlanInfo } from '../../types/api';

interface ExerciseConfigModalProps {
    isOpen: boolean;
    onClose: () => void;
    exercise?: Exercise;
    config: ExerciseConfiguration | null;
    onConfigChange: (config: ExerciseConfiguration) => void;
    onSave: () => Promise<void>;
    selectedDate: Date;
    loading?: boolean;
    mode: 'exercise' | 'workout-plan';
    onModeChange: (mode: 'exercise' | 'workout-plan') => void;
    onWorkoutPlanSelect: (plan: WorkoutPlanInfo | null) => void;
    selectedWorkoutPlan: WorkoutPlanInfo | null;
    // New props for edit mode
    isEditMode?: boolean;
    editingExercise?: any; // ScheduledExercise type
}

const ExerciseConfigModal: React.FC<ExerciseConfigModalProps> = ({
                                                                     isOpen,
                                                                     onClose,
                                                                     exercise,
                                                                     config,
                                                                     onConfigChange,
                                                                     onSave,
                                                                     selectedDate,
                                                                     loading = false,
                                                                     mode,
                                                                     onModeChange,
                                                                     onWorkoutPlanSelect,
                                                                     selectedWorkoutPlan,
                                                                     isEditMode = false,
                                                                     editingExercise
                                                                 }) => {
    // ==================== STATE ====================
    const [localConfig, setLocalConfig] = useState<ExerciseConfiguration | null>(null);
    const [currentMode, setCurrentMode] = useState<'exercise' | 'workout-plan'>(mode);

    // ==================== DEBUG STATE ====================
    const [debugInfo, setDebugInfo] = useState({
        exerciseReceived: false,
        configCreated: false,
        trackingMode: 'unknown',
        renderAttempted: false
    });

    // ==================== EFFECTS ====================

    // Sync mode changes from parent
    useEffect(() => {
        console.log('🔄 Mode changed from parent:', mode);
        setCurrentMode(mode);
    }, [mode]);

    // Create default config when exercise changes
    useEffect(() => {
        if (exercise && currentMode === 'exercise') {
            console.log('🔧 Creating default config for exercise:', exercise.name, {
                isCardio: exercise.isCardio,
                isIsometric: exercise.isIsometric,
                exerciseType: exercise.exerciseType
            });

            try {
                const defaultConfig = getDefaultConfigForExercise(exercise);
                console.log('🔧 Default config created:', defaultConfig);

                setLocalConfig(defaultConfig);
                onConfigChange(defaultConfig);

                setDebugInfo(prev => ({
                    ...prev,
                    exerciseReceived: true,
                    configCreated: true,
                    trackingMode: defaultConfig.trackingMode
                }));
            } catch (error) {
                console.error('❌ Error creating default config:', error);

                // Fallback config creation
                const fallbackConfig = createFallbackConfig(exercise);
                setLocalConfig(fallbackConfig);
                onConfigChange(fallbackConfig);
            }
        } else if (currentMode === 'workout-plan') {
            setLocalConfig(null);
        }
    }, [exercise, currentMode]); // Remove onConfigChange from dependencies

    // Sync external config changes - only when external config prop changes
    useEffect(() => {
        if (config) {
            console.log('🔄 Syncing external config change:', config);
            setLocalConfig(config);
        }
    }, [config]);

    // ==================== HELPER FUNCTIONS ====================

    const createFallbackConfig = (exercise: Exercise): ExerciseConfiguration => {
        console.log('🚨 Creating fallback config for exercise:', exercise.name);

        // Determine tracking mode based on exercise properties
        if (exercise.isCardio) {
            console.log('🎯 Determined tracking mode: cardio');
            const config: CardioConfiguration = {
                trackingMode: 'cardio',
                targetDurationMinutes: exercise.estimatedDurationMinutes || 20,
                targetDistanceKm: undefined,
                targetPace: undefined,
                notes: ''
            };
            return config;
        } else if (exercise.isIsometric) {
            console.log('🎯 Determined tracking mode: isometric');
            const config: IsometricConfiguration = {
                trackingMode: 'isometric',
                sets: 3,
                holdDurationSeconds: 30,
                restSeconds: 60,
                notes: ''
            };
            return config;
        } else {
            console.log('🎯 Determined tracking mode: strength');
            const config: StrengthConfiguration = {
                trackingMode: 'strength',
                sets: 3,
                reps: '8-12',
                weight: undefined,
                restSeconds: 90,
                targetRpe: 7,
                tempo: undefined,
                notes: ''
            };
            return config;
        }
    };

    // ==================== HANDLERS ====================

    const handleConfigUpdate = (updates: Partial<ExerciseConfiguration>) => {
        if (!localConfig) {
            console.warn('⚠️ Trying to update config but localConfig is null');
            return;
        }

        let updatedConfig: ExerciseConfiguration;

        // Type-safe updates based on current tracking mode
        if (localConfig.trackingMode === 'strength') {
            updatedConfig = {
                ...localConfig as StrengthConfiguration,
                ...updates
            } as StrengthConfiguration;
        } else if (localConfig.trackingMode === 'cardio') {
            updatedConfig = {
                ...localConfig as CardioConfiguration,
                ...updates
            } as CardioConfiguration;
        } else if (localConfig.trackingMode === 'isometric') {
            updatedConfig = {
                ...localConfig as IsometricConfiguration,
                ...updates
            } as IsometricConfiguration;
        } else {
            console.error('❌ Unknown tracking mode:', (localConfig as any).trackingMode);
            return;
        }

        console.log('🔄 Updating config:', { current: localConfig, updates, result: updatedConfig });

        setLocalConfig(updatedConfig);
        onConfigChange(updatedConfig);
    };

    const handleSave = async () => {
        console.log('💾 Save clicked - Current state:', {
            mode: currentMode,
            exercise: exercise?.name,
            config: localConfig,
            workoutPlan: selectedWorkoutPlan?.name
        });

        try {
            await onSave();
            console.log('✅ Save completed successfully');
        } catch (error) {
            console.error('❌ Save failed:', error);
        }
    };

    const handleClose = () => {
        console.log('🚪 Modal closing');
        onClose();
    };

    // ==================== VALIDATION ====================

    const validateConfiguration = (): boolean => {
        console.log('🔍 Validating configuration:', { currentMode, localConfig });

        if (currentMode === 'workout-plan') {
            return selectedWorkoutPlan !== null;
        }

        if (!exercise || !localConfig) {
            console.log('❌ Validation failed: missing exercise or config');
            return false;
        }

        // Type guard functions
        const isStrengthConfig = (config: ExerciseConfiguration): config is StrengthConfiguration => {
            return config.trackingMode === 'strength';
        };

        const isCardioConfig = (config: ExerciseConfiguration): config is CardioConfiguration => {
            return config.trackingMode === 'cardio';
        };

        const isIsometricConfig = (config: ExerciseConfiguration): config is IsometricConfiguration => {
            return config.trackingMode === 'isometric';
        };

        if (isStrengthConfig(localConfig)) {
            const isValid = localConfig.sets > 0 && localConfig.reps.length > 0;
            console.log('🏋️ Strength validation:', { sets: localConfig.sets, reps: localConfig.reps, valid: isValid });
            return isValid;
        } else if (isCardioConfig(localConfig)) {
            const cardioValid = localConfig.targetDurationMinutes > 0;
            console.log('❤️ Cardio validation:', { duration: localConfig.targetDurationMinutes, valid: cardioValid });
            return cardioValid;
        } else if (isIsometricConfig(localConfig)) {
            const isometricValid = localConfig.sets > 0 && localConfig.holdDurationSeconds > 0;
            console.log('✨ Isometric validation:', { sets: localConfig.sets, hold: localConfig.holdDurationSeconds, valid: isometricValid });
            return isometricValid;
        } else {
            console.log('❌ Unknown tracking mode:', (localConfig as any).trackingMode);
            return false;
        }
    };

    // ==================== RENDER CONFIGURATION FORMS ====================

    const renderConfigurationForm = () => {
        console.log('🎯 renderConfigurationForm called', {
            currentMode,
            exercise: exercise?.name,
            localConfig: localConfig ? (localConfig as any).trackingMode : 'null',
            configExists: !!localConfig
        });

        if (currentMode === 'workout-plan') {
            return renderWorkoutPlanSelector();
        }

        if (!exercise || !localConfig) {
            console.log('❌ Cannot render config form: missing exercise or localConfig');
            return (
                <div className="p-6 text-center">
                    <AlertTriangle className="w-12 h-12 text-yellow-500 mx-auto mb-4" />
                    <p className="text-gray-600">Configuration not available</p>
                    <p className="text-sm text-gray-500 mt-2">
                        Exercise: {exercise ? '✅' : '❌'} | Config: {localConfig ? '✅' : '❌'}
                    </p>
                </div>
            );
        }

        // Don't update state during render - this causes infinite loops
        // setDebugInfo(prev => ({ ...prev, renderAttempted: true }));

        // Type guard functions
        const isStrengthConfig = (config: ExerciseConfiguration): config is StrengthConfiguration => {
            return config.trackingMode === 'strength';
        };

        const isCardioConfig = (config: ExerciseConfiguration): config is CardioConfiguration => {
            return config.trackingMode === 'cardio';
        };

        const isIsometricConfig = (config: ExerciseConfiguration): config is IsometricConfiguration => {
            return config.trackingMode === 'isometric';
        };

        if (isStrengthConfig(localConfig)) {
            console.log('🏋️ Rendering strength config:', localConfig);
            return renderStrengthConfiguration(localConfig);
        } else if (isCardioConfig(localConfig)) {
            console.log('❤️ Rendering cardio config:', localConfig);
            return renderCardioConfiguration(localConfig);
        } else if (isIsometricConfig(localConfig)) {
            console.log('✨ Rendering isometric config:', localConfig);
            return renderIsometricConfiguration(localConfig);
        } else {
            console.log('❌ Unknown tracking mode:', (localConfig as any).trackingMode);
            return (
                <div className="p-6 text-center">
                    <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                    <p className="text-gray-600">Unknown configuration type: {(localConfig as any).trackingMode}</p>
                </div>
            );
        }
    };

    const renderStrengthConfiguration = (config: StrengthConfiguration) => (
        <div className="space-y-6 p-6">
            <div className="text-center mb-6">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Weight className="w-8 h-8 text-blue-600" />
                </div>
                <h3 className="text-xl font-bold text-gray-900">Strength Configuration</h3>
                <p className="text-sm text-gray-600 mt-1">Configure sets, reps, and weight</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Sets */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-blue-600 flex items-center gap-2">
                        <Repeat className="w-4 h-4" />
                        Sets
                    </Label>
                    <Input
                        type="number"
                        value={config.sets}
                        onChange={(e) => handleConfigUpdate({ sets: parseInt(e.target.value) || 1 })}
                        min="1"
                        max="10"
                        className="border-blue-200 focus:border-blue-500"
                        placeholder="3"
                    />
                </div>

                {/* Reps */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-blue-600 flex items-center gap-2">
                        <Target className="w-4 h-4" />
                        Reps
                    </Label>
                    <Input
                        type="text"
                        value={config.reps}
                        onChange={(e) => handleConfigUpdate({ reps: e.target.value })}
                        className="border-blue-200 focus:border-blue-500"
                        placeholder="8-12"
                    />
                </div>

                {/* Weight */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-green-600 flex items-center gap-2">
                        <Weight className="w-4 h-4" />
                        Weight (kg)
                    </Label>
                    <Input
                        type="number"
                        value={config.weight || ''}
                        onChange={(e) => handleConfigUpdate({ weight: parseFloat(e.target.value) || undefined })}
                        step="0.5"
                        min="0"
                        className="border-green-200 focus:border-green-500"
                        placeholder="Optional"
                    />
                </div>

                {/* Rest */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-green-600 flex items-center gap-2">
                        <Timer className="w-4 h-4" />
                        Rest (seconds)
                    </Label>
                    <Input
                        type="number"
                        value={config.restSeconds}
                        onChange={(e) => handleConfigUpdate({ restSeconds: parseInt(e.target.value) || 60 })}
                        min="15"
                        max="300"
                        className="border-green-200 focus:border-green-500"
                        placeholder="90"
                    />
                </div>

                {/* RPE */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-purple-600 flex items-center gap-2">
                        <Gauge className="w-4 h-4" />
                        Target RPE (1-10)
                    </Label>
                    <Input
                        type="number"
                        value={config.targetRpe || ''}
                        onChange={(e) => handleConfigUpdate({ targetRpe: parseInt(e.target.value) || undefined })}
                        min="1"
                        max="10"
                        className="border-purple-200 focus:border-purple-500"
                        placeholder="7"
                    />
                </div>

                {/* Tempo */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-purple-600 flex items-center gap-2">
                        <Clock className="w-4 h-4" />
                        Tempo
                    </Label>
                    <Input
                        type="text"
                        value={config.tempo || ''}
                        onChange={(e) => handleConfigUpdate({ tempo: e.target.value || undefined })}
                        className="border-purple-200 focus:border-purple-500"
                        placeholder="3-1-2-1"
                    />
                </div>
            </div>

            {/* Notes */}
            <div className="space-y-2">
                <Label className="text-sm font-medium text-gray-600">Notes</Label>
                <Textarea
                    value={config.notes || ''}
                    onChange={(e) => handleConfigUpdate({ notes: e.target.value })}
                    className="min-h-[80px]"
                    placeholder="Any additional notes or modifications..."
                />
            </div>
        </div>
    );

    const renderCardioConfiguration = (config: CardioConfiguration) => (
        <div className="space-y-6 p-6">
            <div className="text-center mb-6">
                <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Heart className="w-8 h-8 text-red-600" />
                </div>
                <h3 className="text-xl font-bold text-gray-900">Cardio Configuration</h3>
                <p className="text-sm text-gray-600 mt-1">Configure duration, distance, and pace</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Duration */}
                <div className="space-y-2 sm:col-span-2">
                    <Label className="text-sm font-medium text-red-600 flex items-center gap-2">
                        <Clock className="w-4 h-4" />
                        Target Duration (minutes)
                    </Label>
                    <Input
                        type="number"
                        value={config.targetDurationMinutes}
                        onChange={(e) => handleConfigUpdate({ targetDurationMinutes: parseInt(e.target.value) || 1 })}
                        min="1"
                        max="180"
                        className="border-red-200 focus:border-red-500"
                        placeholder="20"
                    />
                </div>

                {/* Distance */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-orange-600 flex items-center gap-2">
                        <MapPin className="w-4 h-4" />
                        Distance (km)
                    </Label>
                    <Input
                        type="number"
                        value={config.targetDistanceKm || ''}
                        onChange={(e) => handleConfigUpdate({ targetDistanceKm: parseFloat(e.target.value) || undefined })}
                        step="0.1"
                        min="0"
                        className="border-orange-200 focus:border-orange-500"
                        placeholder="Optional"
                    />
                </div>

                {/* Pace */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-orange-600 flex items-center gap-2">
                        <Zap className="w-4 h-4" />
                        Target Pace (min/km)
                    </Label>
                    <Input
                        type="number"
                        value={config.targetPace || ''}
                        onChange={(e) => handleConfigUpdate({ targetPace: parseFloat(e.target.value) || undefined })}
                        step="0.1"
                        min="0"
                        className="border-orange-200 focus:border-orange-500"
                        placeholder="Optional"
                    />
                </div>
            </div>

            {/* Notes */}
            <div className="space-y-2">
                <Label className="text-sm font-medium text-gray-600">Notes</Label>
                <Textarea
                    value={config.notes || ''}
                    onChange={(e) => handleConfigUpdate({ notes: e.target.value })}
                    className="min-h-[80px]"
                    placeholder="Intensity level, terrain, or other notes..."
                />
            </div>
        </div>
    );

    const renderIsometricConfiguration = (config: IsometricConfiguration) => (
        <div className="space-y-6 p-6">
            <div className="text-center mb-6">
                <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Sparkles className="w-8 h-8 text-purple-600" />
                </div>
                <h3 className="text-xl font-bold text-gray-900">Isometric Configuration</h3>
                <p className="text-sm text-gray-600 mt-1">Configure holds, sets, and rest</p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Sets */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-purple-600 flex items-center gap-2">
                        <Repeat className="w-4 h-4" />
                        Sets
                    </Label>
                    <Input
                        type="number"
                        value={config.sets}
                        onChange={(e) => handleConfigUpdate({ sets: parseInt(e.target.value) || 1 })}
                        min="1"
                        max="10"
                        className="border-purple-200 focus:border-purple-500"
                        placeholder="3"
                    />
                </div>

                {/* Hold Duration */}
                <div className="space-y-2">
                    <Label className="text-sm font-medium text-purple-600 flex items-center gap-2">
                        <Timer className="w-4 h-4" />
                        Hold Duration (seconds)
                    </Label>
                    <Input
                        type="number"
                        value={config.holdDurationSeconds}
                        onChange={(e) => handleConfigUpdate({ holdDurationSeconds: parseInt(e.target.value) || 1 })}
                        min="1"
                        max="300"
                        className="border-purple-200 focus:border-purple-500"
                        placeholder="30"
                    />
                </div>

                {/* Rest */}
                <div className="space-y-2 sm:col-span-2">
                    <Label className="text-sm font-medium text-indigo-600 flex items-center gap-2">
                        <Clock className="w-4 h-4" />
                        Rest Between Sets (seconds)
                    </Label>
                    <Input
                        type="number"
                        value={config.restSeconds}
                        onChange={(e) => handleConfigUpdate({ restSeconds: parseInt(e.target.value) || 60 })}
                        min="15"
                        max="300"
                        className="border-indigo-200 focus:border-indigo-500"
                        placeholder="60"
                    />
                </div>
            </div>

            {/* Total Duration Calculator */}
            <div className="bg-purple-50 p-4 rounded-lg">
                <p className="text-sm font-medium text-purple-700 mb-2">Estimated Total Duration</p>
                <p className="text-2xl font-bold text-purple-600">
                    {Math.round(((config.holdDurationSeconds * config.sets) + (config.restSeconds * Math.max(0, config.sets - 1))) / 60 * 10) / 10} minutes
                </p>
                <p className="text-xs text-purple-600 mt-1">
                    {config.sets} × {config.holdDurationSeconds}s holds + {Math.max(0, config.sets - 1)} × {config.restSeconds}s rest
                </p>
            </div>

            {/* Notes */}
            <div className="space-y-2">
                <Label className="text-sm font-medium text-gray-600">Notes</Label>
                <Textarea
                    value={config.notes || ''}
                    onChange={(e) => handleConfigUpdate({ notes: e.target.value })}
                    className="min-h-[80px]"
                    placeholder="Form cues, modifications, or intensity notes..."
                />
            </div>
        </div>
    );

    const renderWorkoutPlanSelector = () => (
        <div className="p-6 text-center">
            <Calendar className="w-12 h-12 text-blue-500 mx-auto mb-4" />
            <h3 className="text-xl font-bold text-gray-900 mb-2">Workout Plan Mode</h3>
            <p className="text-gray-600">Workout plan selection would go here</p>
            <p className="text-sm text-gray-500 mt-2">
                Selected: {selectedWorkoutPlan?.name || 'None'}
            </p>
        </div>
    );

    // ==================== RENDER MODAL ====================

    if (!isOpen) return null;

    const isConfigValid = validateConfiguration();
    const dateDisplay = selectedDate.toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="bg-gradient-to-r from-blue-500 to-purple-600 text-white p-6 rounded-t-xl">
                    <div className="flex items-center justify-between">
                        <div>
                            <h2 className="text-xl font-bold">
                                {isEditMode ? 'Edit Exercise Configuration' :
                                    currentMode === 'exercise' ? 'Configure Exercise' : 'Select Workout Plan'}
                            </h2>
                            <p className="text-blue-100 text-sm mt-1">
                                {currentMode === 'exercise' && exercise ?
                                    `${exercise.name} • ${dateDisplay}` :
                                    `Scheduling for • ${dateDisplay}`}
                                {isEditMode && editingExercise && (
                                    <span className="ml-2 bg-blue-600 px-2 py-1 rounded text-xs">
                                        Editing
                                    </span>
                                )}
                            </p>
                        </div>
                        <button
                            onClick={handleClose}
                            className="text-white hover:text-blue-200 text-2xl font-bold"
                        >
                            ×
                        </button>
                    </div>
                </div>

                {/* Debug Panel (Development only) */}
                {process.env.NODE_ENV === 'development' && (
                    <div className="bg-yellow-50 border-b border-yellow-200 p-3">
                        <p className="text-xs font-medium text-yellow-800 mb-1">Debug Info:</p>
                        <div className="text-xs text-yellow-700 space-y-1">
                            <p>Exercise: {exercise?.name || 'None'} | isCardio: {exercise?.isCardio ? 'Yes' : 'No'} | isIsometric: {exercise?.isIsometric ? 'Yes' : 'No'}</p>
                            <p>Local Config: {localConfig ? (localConfig as any).trackingMode : 'None'} | Valid: {isConfigValid ? 'Yes' : 'No'}</p>
                            <p>Mode: {currentMode} | Render Attempted: {debugInfo.renderAttempted ? 'Yes' : 'No'}</p>
                        </div>
                    </div>
                )}

                {/* Mode Selector - Only show when not in edit mode */}
                {!isEditMode && (
                    <div className="border-b border-gray-200 p-4">
                        <div className="flex gap-2">
                            <button
                                onClick={() => {
                                    setCurrentMode('exercise');
                                    onModeChange('exercise');
                                }}
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                    currentMode === 'exercise'
                                        ? 'bg-blue-500 text-white'
                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                }`}
                            >
                                Single Exercise
                            </button>
                            <button
                                onClick={() => {
                                    setCurrentMode('workout-plan');
                                    onModeChange('workout-plan');
                                }}
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                    currentMode === 'workout-plan'
                                        ? 'bg-blue-500 text-white'
                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                }`}
                            >
                                Workout Plan
                            </button>
                        </div>
                    </div>
                )}

                {/* Content */}
                <div className="flex-1 overflow-y-auto">
                    {renderConfigurationForm()}
                </div>

                {/* Footer */}
                <div className="border-t border-gray-200 p-4 flex gap-3 justify-end">
                    <Button
                        variant="outline"
                        onClick={handleClose}
                        disabled={loading}
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={handleSave}
                        disabled={!isConfigValid || loading}
                        className="bg-blue-600 hover:bg-blue-700"
                    >
                        {loading ? 'Saving...' : isEditMode ? 'Update Exercise' : 'Schedule Exercise'}
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default ExerciseConfigModal;