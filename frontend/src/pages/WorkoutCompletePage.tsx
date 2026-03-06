import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useWorkoutMode, SetData} from '../hooks/useWorkoutMode';
import {useModalState} from '../hooks/useModalState';
import ExerciseSelector from '../components/CalendarPage/ExerciseSelector';
import {
    CardioConfiguration,
    Exercise,
    ExerciseConfiguration,
    IsometricConfiguration,
    StrengthConfiguration
} from '../types/exercise';
import {Button} from '../components/ui/button';
import {Card, CardContent} from '../components/ui/card';
import {Badge} from '../components/ui/badge';
import toast from 'react-hot-toast';
import {progressApi, WorkoutCompletionRequest} from '../services/progressApi';

// Extracted Components
import {WorkoutHeader} from '../components/WorkoutModePage/WorkoutHeader';
import {ExerciseNavigation} from '../components/WorkoutModePage/ExerciseNavigation';
import {SetCompletionDialog} from '../components/WorkoutModePage/SetCompletionDialog';
import {ConfettiEffect} from '../components/WorkoutModePage/ConfettiEffect';
import {ExerciseTracker} from '../components/WorkoutModePage/ExerciseTracker';
import {WorkoutSet} from "@/types";

const WorkoutModePage: React.FC = () => {
    const navigate = useNavigate();
    const workoutMode = useWorkoutMode();
    const modalState = useModalState();
    const [showSetCompletionDialog, setShowSetCompletionDialog] = useState(false);
    const [completedLastSet, setCompletedLastSet] = useState(false);

    useEffect(() => {
        if (!workoutMode.isWorkoutActive) {
            navigate('/calendar');
        }
    }, [workoutMode.isWorkoutActive, navigate]);

    // Get current exercise and workout state
    const currentExercise = workoutMode.currentExercise;
    const currentWorkout = workoutMode.currentWorkout;

    if (!currentWorkout || !currentExercise) {
        return null;
    }

    const isLastExercise = workoutMode.currentWorkout?.exercises.length === 1 || !workoutMode.canGoNext;
    const isLastSet = currentExercise?.sets.filter(set => !set.completed).length === 1;

    // Exercise type detection - using actual exercise properties
    const exerciseType = currentExercise.scheduledExercise?.exercise?.isCardio ? 'cardio' :
        currentExercise.scheduledExercise?.exercise?.isIsometric ? 'isometric' : 'strength';
    const isCardio = exerciseType === 'cardio';
    const isIsometric = exerciseType === 'isometric';

    // Type-based styling
    const getTypeStyle = () => {
        if (isCardio) {
            return {
                bg: 'from-red-900/20 to-pink-900/20',
                border: 'border-red-500/30',
                button: 'bg-red-600 hover:bg-red-700',
                text: 'text-red-400',
                gradient: 'from-red-600 to-pink-600'
            };
        } else if (isIsometric) {
            return {
                bg: 'from-purple-900/20 to-indigo-900/20',
                border: 'border-purple-500/30',
                button: 'bg-purple-600 hover:bg-purple-700',
                text: 'text-purple-400',
                gradient: 'from-purple-600 to-indigo-600'
            };
        } else {
            return {
                bg: 'from-blue-900/20 to-cyan-900/20',
                border: 'border-blue-500/30',
                button: 'bg-blue-600 hover:bg-blue-700',
                text: 'text-blue-400',
                gradient: 'from-blue-600 to-cyan-600'
            };
        }
    };

    const typeStyle = getTypeStyle();

    // Helper function to get current exercise index safely
    const getCurrentExerciseIndex = () => {
        if (!workoutMode.currentWorkout || !currentExercise) return 0;
        const index = workoutMode.currentWorkout.exercises.findIndex(ex => ex.id === currentExercise.id);
        return index >= 0 ? index : 0;
    };

    // Helper function to get current set number safely
    const getCurrentSetNumber = () => {
        if (!currentExercise || !workoutMode.currentSet) return 1;
        const index = currentExercise.sets.findIndex(set => set.id === workoutMode.currentSet?.id);
        return index >= 0 ? index + 1 : 1;
    };

    // Exercise selector handlers
    const handleAddExerciseToWorkout = async (exercise: Exercise, config: ExerciseConfiguration) => {
        try {
            modalState.closeExerciseSelector();
            toast.loading(`Adding ${exercise.name}...`);

            await workoutMode.addExerciseToCurrentWorkout(exercise, config);

            toast.dismiss();
            toast.success(`${exercise.name} added to workout!`);
        } catch (error) {
            toast.dismiss();
            console.error('Failed to add exercise to workout:', error);
            toast.error(`Failed to add ${exercise.name}. Please try again.`);
            modalState.openExerciseSelector();
        }
    };

    // Set completion handlers
    const handleSetComplete = (setData: SetData) => {
        const currentSetId = workoutMode.currentSet?.id;

        if (!currentSetId) return;

        // Calculate if this is the last set BEFORE completing
        const remainingUncompletedSets = currentExercise?.sets.filter(set =>
            !set.completed && set.id !== currentSetId
        ).length || 0;

        const wasLastSet = remainingUncompletedSets === 0;

        // ✅ Get the rest time that was stored for THIS set
        const restTimeForThisSet = workoutMode.getRestTimeForNextSet();
        console.log('🔥 RETRIEVING rest time for this set:', restTimeForThisSet);

        // Complete the set
        const workoutSetData: Partial<WorkoutSet> = {
            actualReps: setData.actualReps,
            actualWeight: setData.actualWeight,
            actualRpe: setData.actualRpe,
            actualDurationMinutes: setData.actualDurationMinutes,
            actualHoldSeconds: setData.actualHoldSeconds,
            actualRestSeconds: restTimeForThisSet, // ✅ Use stored rest time
            notes: setData.notes
        };

        workoutMode.completeSet(currentSetId, workoutSetData);

        // ✅ Clear the stored rest time after using it
        workoutMode.clearRestTimeForNextSet();

        if (wasLastSet) {
            setCompletedLastSet(true);
            setTimeout(() => {
                setShowSetCompletionDialog(true);
            }, 100);
        }
    };

    const handleUpdateSetData = (setData: SetData) => {
        // Update current set data in real-time
        workoutMode.setSetData(setData);
    };

    // Navigation handlers
    const handlePreviousExercise = () => {
        workoutMode.goToPreviousExercise();
    };

    const handleNextExercise = () => {
        workoutMode.goToNextExercise();
        setShowSetCompletionDialog(false);
    };

    const handleAddSet = () => {
        // The addSet function from context expects a string (exerciseId)
        const currentExerciseId = currentExercise?.id;
        if (currentExerciseId && typeof currentExerciseId === 'string') {
            workoutMode.addSet(currentExerciseId);
        }
        setShowSetCompletionDialog(false);
    };

    const handleCompleteWorkout = async () => {
        workoutMode.setShowConfetti(true);
        setShowSetCompletionDialog(false);

        try {
            const workoutData = await workoutMode.completeWorkout();

            if (workoutData) {
                console.log('✅ Completed workout:', workoutData);

                // 🆕 USE workoutMode.workoutDuration (it's already being tracked!)
                const durationMinutes = Math.round(workoutMode.workoutDuration / 60) || 30;

                console.log('⏱️ Workout duration:', {
                    durationSeconds: workoutMode.workoutDuration,
                    durationMinutes: durationMinutes
                });

                // Calculate statistics
                const totalSets = workoutData.exercises?.reduce((sum, ex) =>
                    sum + (ex.sets?.length || 0), 0
                ) || 0;

                const totalVolume = workoutData.exercises?.reduce((sum, ex) =>
                        sum + (ex.sets?.reduce((setSum: number, set: any) =>
                            setSum + ((set.actualWeight || 0) * (set.actualReps || 0)), 0
                        ) || 0), 0
                ) || 0;

                const totalDistance = workoutData.exercises?.reduce((sum, ex) =>
                        sum + (ex.sets?.reduce((setSum: number, set: any) =>
                            setSum + (set.actualDistanceKm || 0), 0
                        ) || 0), 0
                ) || 0;

                const totalHoldTime = workoutData.exercises?.reduce((sum, ex) =>
                        sum + (ex.sets?.reduce((setSum: number, set: any) =>
                            setSum + (set.actualHoldSeconds || 0), 0
                        ) || 0), 0
                ) || 0;

                const uniqueExercises = new Set(
                    workoutData.exercises?.map(ex => ex.scheduledExercise?.exercise?.exerciseId)
                ).size;

                const exerciseCount = workoutData.exercises?.length || 1;

                // Determine workout type
                const hasCardio = workoutData.exercises?.some(ex =>
                    ex.scheduledExercise?.exercise?.isCardio
                );
                const hasIsometric = workoutData.exercises?.some(ex =>
                    ex.scheduledExercise?.exercise?.isIsometric
                );

                const workoutType: 'CARDIO' | 'ISOMETRIC' | 'STRENGTH' = hasCardio
                    ? 'CARDIO'
                    : hasIsometric
                        ? 'ISOMETRIC'
                        : 'STRENGTH';

                console.log('📊 Workout stats:', {
                    duration: durationMinutes,
                    sets: totalSets,
                    volume: totalVolume,
                    exerciseCount,
                    workoutType
                });

                // Consistency bonus: reward honest, realistic workouts with +15% XP.
                // Thresholds are intentionally lenient — rewarding effort, not punishing short sessions.
                const isTimeBased = workoutType === 'CARDIO' || workoutType === 'ISOMETRIC';
                const consistencyBonus = isTimeBased
                    ? durationMinutes >= 10                        // cardio/isometric: 10+ min
                    : durationMinutes >= 20 && totalSets >= 3;     // strength: 20+ min AND 3+ sets

                console.log('💪 Consistency bonus:', consistencyBonus, { durationMinutes, totalSets, workoutType });

                // Submit to progression API
                const progressResponse = await progressApi.completeWorkout({
                    durationMinutes,
                    setsCompleted: totalSets,
                    volumeLifted: totalVolume,
                    distanceKm: totalDistance > 0 ? totalDistance : undefined,
                    holdSeconds: totalHoldTime > 0 ? totalHoldTime : undefined,
                    uniqueExercisesCount: uniqueExercises,
                    workoutType,
                    exerciseCount,
                    consistencyBonus
                });

                console.log('🎉 Progression response:', progressResponse);

                                // Navigate immediately to workout complete page
                                navigate('/workout-complete', {
                                    state: {
                                        progressResponse,
                                        workoutStats: {
                                            durationMinutes,
                                            setsCompleted: totalSets,
                                            exerciseCount,
                                            volumeLifted: totalVolume > 0 ? totalVolume : undefined,
                                            distanceKm: totalDistance > 0 ? totalDistance : undefined,
                                            holdSeconds: totalHoldTime > 0 ? totalHoldTime : undefined,
                                            workoutType,
                                            consistencyBonus
                                        }
                                    }
                                });
                            }

          } catch (error) {
              console.error('Error completing workout:', error);
              toast.error('Error saving workout. Please try again.');
              setTimeout(() => {
                  navigate('/calendar');
              }, 3000);
          }
      };



    // Workout control handlers
    const handlePauseWorkout = () => {
        workoutMode.pauseWorkout();
        navigate('/');
    };

    const handleCancelWorkout = () => {
        if (window.confirm('Are you sure you want to cancel this workout? All progress will be lost.')) {
            workoutMode.cancelWorkout();
            navigate('/');
        }
    };

    // Current set data
    const currentSetData = workoutMode.currentSet || workoutMode.setData || {};

    return (
        <div className="min-h-screen bg-gray-900 text-white">
            {/* Confetti Effect */}
            <ConfettiEffect
                show={workoutMode.showConfetti}
                onComplete={() => workoutMode.setShowConfetti(false)}
                mode="workout-complete"
            />

            {/* Set Completion Dialog */}
            <SetCompletionDialog
                show={showSetCompletionDialog}
                isLastSet={completedLastSet}
                isLastExercise={isLastExercise}
                exerciseName={currentExercise.scheduledExercise?.exercise?.name || 'Exercise'}
                onNextExercise={handleNextExercise}
                onAddSet={handleAddSet}
                onCompleteWorkout={handleCompleteWorkout}
                onClose={() => {
                    setShowSetCompletionDialog(false);
                    setCompletedLastSet(false);
                }}
            />

            {/* Exercise Selector Modal */}
            {modalState.showExerciseSelector && (
                <div className="fixed inset-0 z-30 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
                    <div className="w-full max-w-4xl max-h-[90vh] overflow-hidden">
                        <ExerciseSelector
                            open={modalState.showExerciseSelector}
                            onExerciseSelect={(exercise: Exercise) => {
                                let defaultConfig: ExerciseConfiguration;

                                if (exercise.isCardio) {
                                    defaultConfig = {
                                        trackingMode: 'cardio',
                                        targetDurationMinutes: exercise.estimatedDurationMinutes || 20,
                                        targetDistanceUnit: 'miles',
                                        sessionType: 'single_session'
                                    } as CardioConfiguration;
                                } else if (exercise.isIsometric) {
                                    defaultConfig = {
                                        trackingMode: 'isometric',
                                        targetSets: 3,
                                        holdDurationSeconds: 30,
                                        restSeconds: 60
                                    } as IsometricConfiguration;
                                } else {
                                    defaultConfig = {
                                        trackingMode: 'strength',
                                        targetSets: 3,
                                        targetReps: 10,
                                        targetWeightUnit: 'lbs',
                                        restSeconds: 90,
                                        targetRpe: 7
                                    } as StrengthConfiguration;
                                }

                                handleAddExerciseToWorkout(exercise, defaultConfig);
                            }}
                            onWorkoutPlanSelect={() => {
                            }} // Not used in workout mode
                            onWorkoutPlanConfigure={() => {
                            }} // Not used in workout mode
                            onClose={modalState.closeExerciseSelector}
                            mode="workout"
                        />
                    </div>
                </div>
            )}

            <div className="container mx-auto p-4 space-y-6">
                {/* Workout Header */}
                <WorkoutHeader
                    exerciseName={currentExercise.scheduledExercise?.exercise?.name || 'Exercise'}
                    exerciseIcon={currentExercise.scheduledExercise?.exercise?.emoji || '💪'}
                    currentExerciseIndex={getCurrentExerciseIndex()}
                    totalExercises={workoutMode.currentWorkout?.exercises.length || 1}
                    completionPercentage={workoutMode.getCompletionPercentage()}
                    gradientClass={typeStyle.gradient}
                    workoutDuration={workoutMode.workoutDuration}
                />

                {/* Exercise Navigation */}
                <ExerciseNavigation
                    currentSetNumber={getCurrentSetNumber()}
                    totalSets={currentExercise?.sets.length || 1}
                    canGoPrevious={workoutMode.canGoPrevious}
                    canGoNext={workoutMode.canGoNext}
                    onPreviousExercise={handlePreviousExercise}
                    onNextExercise={handleNextExercise}
                />

                {/* Exercise Tracker */}
                <ExerciseTracker
                    exerciseType={exerciseType}
                    isActive={workoutMode.isWorkoutActive}
                    currentSetData={currentSetData}
                    isLastSet={isLastSet}
                    onSetComplete={handleSetComplete}
                    onUpdateSetData={handleUpdateSetData}
                    typeStyle={typeStyle}
                />

                {/* Workout Controls */}
                <Card className="bg-gray-800 border-gray-700">
                    <CardContent className="p-6">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <Button
                                onClick={handlePauseWorkout}
                                variant="outline"
                                className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                            >
                                Pause Workout
                            </Button>

                            <Button
                                onClick={() => modalState.openExerciseSelector()}
                                variant="outline"
                                className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                            >
                                Add Exercise
                            </Button>

                            <Button
                                onClick={handleCancelWorkout}
                                variant="outline"
                                className="bg-red-600/20 border-red-500/30 hover:bg-red-600/30 text-red-300"
                            >
                                Cancel Workout
                            </Button>
                        </div>
                    </CardContent>
                </Card>

                {/* Workout Overview */}
                <Card className="bg-gray-800 border-gray-700">
                    <CardContent className="p-6">
                        <h3 className="text-xl font-bold mb-4">Workout Overview</h3>
                        <div className="space-y-3">
                            {workoutMode.currentWorkout?.exercises.map((exercise, index) => (
                                <div
                                    key={exercise.id || index}
                                    className={`flex items-center justify-between p-3 rounded-lg border ${
                                        exercise.id === currentExercise?.id
                                            ? 'bg-blue-900/20 border-blue-500/30'
                                            : 'bg-gray-700/50 border-gray-600/50'
                                    }`}
                                >
                                    <div className="flex items-center gap-3">
                                        <span
                                            className="text-2xl">{exercise.scheduledExercise?.exercise?.emoji || '💪'}</span>
                                        <span
                                            className="font-medium">{exercise.scheduledExercise?.exercise?.name || 'Exercise'}</span>
                                        {exercise.id === currentExercise?.id && (
                                            <Badge variant="secondary" className="bg-blue-600 text-white">
                                                Current
                                            </Badge>
                                        )}
                                    </div>
                                    <div className="text-sm text-gray-400">
                                        {exercise.sets?.length || 0} sets
                                    </div>
                                </div>
                            ))}
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default WorkoutModePage;