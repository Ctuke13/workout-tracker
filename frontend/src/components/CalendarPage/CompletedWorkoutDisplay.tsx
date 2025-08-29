import React from 'react';
import {Clock, Eye, Target, TrendingUp, TrendingDown, Minus, CheckCircle, XCircle} from 'lucide-react';
import {ScheduledExercise, WorkoutResults} from '../../types/exercise';


interface CompletedWorkoutDisplayProps {
    exercise: ScheduledExercise;
    workoutResults?: WorkoutResults;
    onViewDetails: () => void;
}

// Enhanced performance evaluation that checks ALL targets
const evaluateTargetAchievement = (exercise: ScheduledExercise, results: WorkoutResults): {
    overall: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET';
    criteria: Array<{
        name: string;
        target: any;
        actual: any;
        status: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET' | 'NOT_SET';
        weight: number;
        displayText: string;
    }>;
    achievementScore: number;
} => {
    const criteria: Array<{
        name: string;
        target: any;
        actual: any;
        status: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET' | 'NOT_SET';
        weight: number;
        displayText: string;
    }> = [];

    let totalWeight = 0;
    let achievedWeight = 0;

    // 1. Sets criterion (always applicable)
    if (exercise.targetSets) {
        const completedSets = results.sets.filter(s => s.completed).length;
        const setsStatus = completedSets >= exercise.targetSets ? 'MET' :
            completedSets >= exercise.targetSets * 0.8 ? 'PARTIAL' : 'BELOW_TARGET';

        criteria.push({
            name: 'Sets',
            target: exercise.targetSets,
            actual: completedSets,
            status: setsStatus,
            weight: 0.25,
            displayText: `${completedSets}/${exercise.targetSets} sets`
        });

        totalWeight += 0.25;
        if (setsStatus === 'MET') achievedWeight += 0.25;
        else if (setsStatus === 'PARTIAL') achievedWeight += 0.15;
    }

    // 2. Exercise type specific criteria
    if (exercise.exercise.isCardio) {
        // Duration criterion for cardio
        if (exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes) {
            const targetDuration = exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes!;
            const actualDuration = results.actualDurationMinutes || results.totalDurationMinutes;

            const durationStatus = actualDuration >= targetDuration ?
                (actualDuration >= targetDuration * 1.1 ? 'EXCEEDED' : 'MET') :
                (actualDuration >= targetDuration * 0.8 ? 'PARTIAL' : 'BELOW_TARGET');

            criteria.push({
                name: 'Duration',
                target: targetDuration,
                actual: actualDuration,
                status: durationStatus,
                weight: 0.4,
                displayText: `${actualDuration}/${targetDuration} min`
            });

            totalWeight += 0.4;
            if (durationStatus === 'EXCEEDED') achievedWeight += 0.44; // 110% credit for exceeding
            else if (durationStatus === 'MET') achievedWeight += 0.4;
            else if (durationStatus === 'PARTIAL') achievedWeight += 0.25;
        }

        // Distance criterion (if set)
        if (exercise.targetDistance || exercise.targetDistanceKm) {
            const targetDistance = exercise.targetDistance ||
                (exercise.targetDistanceKm ? exercise.targetDistanceKm * 0.621371 : 0);
            const actualDistance = results.actualDistanceKm ?
                results.actualDistanceKm * 0.621371 : 0;

            const distanceStatus = actualDistance >= targetDistance ?
                (actualDistance >= targetDistance * 1.05 ? 'EXCEEDED' : 'MET') :
                (actualDistance >= targetDistance * 0.9 ? 'PARTIAL' : 'BELOW_TARGET');

            criteria.push({
                name: 'Distance',
                target: targetDistance,
                actual: actualDistance,
                status: distanceStatus,
                weight: 0.25,
                displayText: `${actualDistance.toFixed(1)}/${targetDistance.toFixed(1)} mi`
            });

            totalWeight += 0.25;
            if (distanceStatus === 'EXCEEDED') achievedWeight += 0.275;
            else if (distanceStatus === 'MET') achievedWeight += 0.25;
            else if (distanceStatus === 'PARTIAL') achievedWeight += 0.15;
        }

        // Pace criterion (if set)
        if (exercise.targetPace) {
            const actualPace = results.actualPace || 0;
            // For pace, lower is better
            const paceStatus = actualPace <= exercise.targetPace ?
                (actualPace <= exercise.targetPace * 0.95 ? 'EXCEEDED' : 'MET') :
                (actualPace <= exercise.targetPace * 1.1 ? 'PARTIAL' : 'BELOW_TARGET');

            const formatPace = (pace: number) => {
                const mins = Math.floor(pace);
                const secs = Math.round((pace - mins) * 60);
                return `${mins}:${secs.toString().padStart(2, '0')}`;
            };

            criteria.push({
                name: 'Pace',
                target: exercise.targetPace,
                actual: actualPace,
                status: paceStatus,
                weight: 0.1,
                displayText: `${formatPace(actualPace)}/${formatPace(exercise.targetPace)} /mi`
            });

            totalWeight += 0.1;
            if (paceStatus === 'EXCEEDED') achievedWeight += 0.11;
            else if (paceStatus === 'MET') achievedWeight += 0.1;
            else if (paceStatus === 'PARTIAL') achievedWeight += 0.05;
        }

    } else if (exercise.exercise.isIsometric) {
        // Hold duration criterion
        if (exercise.holdDurationSeconds) {
            // For isometric exercises, actualReps contains the hold time in seconds
            const totalActualHold = results.sets.reduce((sum, set) => sum + (set.actualReps || 0), 0);
            const expectedTotalHold = exercise.holdDurationSeconds * (exercise.targetSets || 3);

            const holdStatus = totalActualHold >= expectedTotalHold ?
                (totalActualHold >= expectedTotalHold * 1.1 ? 'EXCEEDED' : 'MET') :
                (totalActualHold >= expectedTotalHold * 0.8 ? 'PARTIAL' : 'BELOW_TARGET');

            criteria.push({
                name: 'Hold Time',
                target: expectedTotalHold,
                actual: totalActualHold,
                status: holdStatus,
                weight: 0.5,
                displayText: `${totalActualHold}/${expectedTotalHold}s total`
            });

            totalWeight += 0.5;
            if (holdStatus === 'EXCEEDED') achievedWeight += 0.55;
            else if (holdStatus === 'MET') achievedWeight += 0.5;
            else if (holdStatus === 'PARTIAL') achievedWeight += 0.3;
        }

    } else {
        // Strength exercise criteria

        // Reps criterion
        if (exercise.targetReps) {
            const targetReps = typeof exercise.targetReps === 'number' ?
                exercise.targetReps : parseInt(String(exercise.targetReps), 10);

            const repsAchieved = results.sets.every(set => set.actualReps >= targetReps);
            const totalActualReps = results.sets.reduce((sum, set) => sum + set.actualReps, 0);
            const expectedTotalReps = targetReps * results.sets.length;

            const repsRatio = totalActualReps / expectedTotalReps;
            const repsStatus = repsRatio >= 1.0 ?
                (repsRatio >= 1.1 ? 'EXCEEDED' : 'MET') :
                (repsRatio >= 0.8 ? 'PARTIAL' : 'BELOW_TARGET');

            criteria.push({
                name: 'Reps',
                target: expectedTotalReps,
                actual: totalActualReps,
                status: repsStatus,
                weight: 0.4,
                displayText: `${totalActualReps}/${expectedTotalReps} total reps`
            });

            totalWeight += 0.4;
            if (repsStatus === 'EXCEEDED') achievedWeight += 0.44;
            else if (repsStatus === 'MET') achievedWeight += 0.4;
            else if (repsStatus === 'PARTIAL') achievedWeight += 0.25;
        }

        // Weight criterion (if set)
        if (exercise.targetWeight && exercise.targetWeight > 0) {
            const weightAchieved = results.sets.every(set =>
                (set.actualWeight || 0) >= exercise.targetWeight!);
            const maxActualWeight = Math.max(...results.sets.map(set => set.actualWeight || 0));

            const weightStatus = weightAchieved ?
                (maxActualWeight > exercise.targetWeight * 1.05 ? 'EXCEEDED' : 'MET') :
                (maxActualWeight >= exercise.targetWeight * 0.9 ? 'PARTIAL' : 'BELOW_TARGET');

            criteria.push({
                name: 'Weight',
                target: exercise.targetWeight,
                actual: maxActualWeight,
                status: weightStatus,
                weight: 0.3,
                displayText: `${maxActualWeight}/${exercise.targetWeight} ${exercise.targetWeightUnit || 'lbs'}`
            });

            totalWeight += 0.3;
            if (weightStatus === 'EXCEEDED') achievedWeight += 0.33;
            else if (weightStatus === 'MET') achievedWeight += 0.3;
            else if (weightStatus === 'PARTIAL') achievedWeight += 0.18;
        }

        // RPE criterion (if set) - lower is better for same performance
        if (exercise.targetRpe) {
            const avgActualRpe = results.sets
                .filter(set => set.rpe)
                .reduce((sum, set, _, arr) => sum + (set.rpe || 0) / arr.length, 0);

            if (avgActualRpe > 0) {
                const rpeStatus = avgActualRpe <= exercise.targetRpe ?
                    (avgActualRpe <= exercise.targetRpe - 1 ? 'EXCEEDED' : 'MET') :
                    (avgActualRpe <= exercise.targetRpe + 1 ? 'PARTIAL' : 'BELOW_TARGET');

                criteria.push({
                    name: 'RPE',
                    target: exercise.targetRpe,
                    actual: avgActualRpe,
                    status: rpeStatus,
                    weight: 0.05,
                    displayText: `${avgActualRpe.toFixed(1)}/${exercise.targetRpe} effort`
                });

                totalWeight += 0.05;
                if (rpeStatus === 'EXCEEDED') achievedWeight += 0.055;
                else if (rpeStatus === 'MET') achievedWeight += 0.05;
                else if (rpeStatus === 'PARTIAL') achievedWeight += 0.025;
            }
        }
    }

    // Calculate overall achievement
    const achievementScore = totalWeight > 0 ? (achievedWeight / totalWeight) * 100 : 0;

    let overall: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET';
    if (achievementScore >= 100) overall = 'EXCEEDED';
    else if (achievementScore >= 90) overall = 'MET';
    else if (achievementScore >= 70) overall = 'PARTIAL';
    else overall = 'BELOW_TARGET';

    return {overall, criteria, achievementScore};
};

const CompletedWorkoutDisplay: React.FC<CompletedWorkoutDisplayProps> = ({
                                                                             exercise,
                                                                             workoutResults,
                                                                             onViewDetails
                                                                         }) => {
    // Enhanced performance evaluation
    const performanceEval = workoutResults ?
        evaluateTargetAchievement(exercise, workoutResults) : null;

    React.useEffect(() => {
        if (workoutResults && exercise.exercise.isIsometric) {
            console.log('HOLD TIME DEBUG:', {
                exerciseName: exercise.exercise.name,
                actualHoldDurations: workoutResults.actualHoldDurations,
                sets: workoutResults.sets,
                setsWithIsometricData: workoutResults.sets?.map(set => ({
                    setNumber: set.setNumber,
                    actualReps: set.actualReps,
                    actualHoldSeconds: set.isometricData?.holdDurationSeconds,
                    isometricData: set.isometricData
                }))
            });
        }
    }, [workoutResults, exercise]);

    const getPerformanceColor = (rating: WorkoutResults['performanceRating']) => {
        switch (rating) {
            case 'EXCEEDED':
                return 'text-green-600 bg-green-50 border-green-200';
            case 'MET':
                return 'text-blue-600 bg-blue-50 border-blue-200';
            case 'PARTIAL':
                return 'text-yellow-600 bg-yellow-50 border-yellow-200';
            case 'BELOW_TARGET':
                return 'text-red-600 bg-red-50 border-red-200';
            default:
                return 'text-gray-600 bg-gray-50 border-gray-200';
        }
    };

    const getPerformanceIcon = (rating: WorkoutResults['performanceRating']) => {
        switch (rating) {
            case 'EXCEEDED':
                return <TrendingUp className="w-4 h-4"/>;
            case 'MET':
                return <Target className="w-4 h-4"/>;
            case 'PARTIAL':
                return <Minus className="w-4 h-4"/>;
            case 'BELOW_TARGET':
                return <TrendingDown className="w-4 h-4"/>;
            default:
                return <CheckCircle className="w-4 h-4"/>;
        }
    };

    const getPerformanceMessage = (rating: WorkoutResults['performanceRating']) => {
        switch (rating) {
            case 'EXCEEDED':
                return 'Exceeded targets!';
            case 'MET':
                return 'All targets achieved';
            case 'PARTIAL':
                return 'Most targets achieved';
            case 'BELOW_TARGET':
                return 'Some targets missed';
            default:
                return 'Completed';
        }
    };

    const getCriteriaIcon = (status: string) => {
        switch (status) {
            case 'EXCEEDED':
                return <TrendingUp className="w-3 h-3 text-green-600"/>;
            case 'MET':
                return <CheckCircle className="w-3 h-3 text-blue-600"/>;
            case 'PARTIAL':
                return <Minus className="w-3 h-3 text-yellow-600"/>;
            case 'BELOW_TARGET':
                return <XCircle className="w-3 h-3 text-red-600"/>;
            default:
                return <Minus className="w-3 h-3 text-gray-400"/>;
        }
    };

    const renderEnhancedResults = () => {
        if (!workoutResults || !performanceEval) return null;

        return (
            <div className="space-y-4">
                {/* Achievement Score */}
                <div className="bg-white rounded-lg p-3 border">
                    <div className="flex items-center justify-between mb-2">
                        <span className="text-sm font-medium text-gray-700">Target Achievement</span>
                        <span className="text-lg font-bold text-gray-900">
                            {Math.round(performanceEval.achievementScore)}%
                        </span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                        <div
                            className={`h-2 rounded-full transition-all duration-300 ${
                                performanceEval.achievementScore >= 100 ? 'bg-green-500' :
                                    performanceEval.achievementScore >= 90 ? 'bg-blue-500' :
                                        performanceEval.achievementScore >= 70 ? 'bg-yellow-500' :
                                            'bg-red-500'
                            }`}
                            style={{width: `${Math.min(performanceEval.achievementScore, 100)}%`}}
                        />
                    </div>
                </div>

                {/* Criteria Breakdown */}
                <div className="space-y-2">
                    <div className="text-sm font-medium text-gray-700">Target Breakdown:</div>
                    <div className="space-y-1">
                        {performanceEval.criteria.map((criterion, index) => (
                            <div key={index}
                                 className="flex items-center justify-between text-xs bg-gray-50 p-2 rounded">
                                <div className="flex items-center gap-2">
                                    {getCriteriaIcon(criterion.status)}
                                    <span className="font-medium">{criterion.name}</span>
                                </div>
                                <span className={`font-medium ${
                                    criterion.status === 'EXCEEDED' ? 'text-green-600' :
                                        criterion.status === 'MET' ? 'text-blue-600' :
                                            criterion.status === 'PARTIAL' ? 'text-yellow-600' :
                                                'text-red-600'
                                }`}>
                                    {criterion.displayText}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Quick Stats Grid */}
                <div className="grid grid-cols-3 gap-3">
                    <div className="text-center p-2 bg-blue-50 rounded-lg">
                        <div className="text-lg font-bold text-blue-600">
                            {workoutResults.sets.filter(s => s.completed).length}
                        </div>
                        <div className="text-xs text-blue-700">Sets Done</div>
                    </div>

                    {exercise.exercise.isCardio ? (
                        <>
                            <div className="text-center p-2 bg-red-50 rounded-lg">
                                <div className="text-lg font-bold text-red-600">
                                    {workoutResults.actualDurationMinutes || workoutResults.totalDurationMinutes}m
                                </div>
                                <div className="text-xs text-red-700">Duration</div>
                            </div>
                            {workoutResults.caloriesBurned && (
                                <div className="text-center p-2 bg-orange-50 rounded-lg">
                                    <div className="text-lg font-bold text-orange-600">
                                        {workoutResults.caloriesBurned}
                                    </div>
                                    <div className="text-xs text-orange-700">Calories</div>
                                </div>
                            )}
                        </>
                    ) : exercise.exercise.isIsometric ? (
                        <>
                            <div className="text-center p-2 bg-purple-50 rounded-lg">
                                <div className="text-lg font-bold text-purple-600">
                                    {(() => {
                                        let totalHold = 0;

                                        // For isometric exercises, actualReps contains the hold time in seconds
                                        if (workoutResults.sets && workoutResults.sets.length > 0) {
                                            totalHold = workoutResults.sets.reduce((sum, set) => {
                                                return sum + (set.actualReps || 0);
                                            }, 0);
                                        }

                                        // Fallback to actualHoldDurations if available
                                        if (totalHold === 0 && workoutResults.actualHoldDurations) {
                                            totalHold = workoutResults.actualHoldDurations.reduce((sum, hold) => sum + (hold || 0), 0);
                                        }

                                        return totalHold;
                                    })()}s
                                </div>
                                <div className="text-xs text-purple-700">Total Hold</div>
                            </div>
                            <div className="text-center p-2 bg-indigo-50 rounded-lg">
                                <div className="text-lg font-bold text-indigo-600">
                                    {(() => {
                                        let totalHold = 0;
                                        let setCount = 0;

                                        if (workoutResults.sets && workoutResults.sets.length > 0) {
                                            setCount = workoutResults.sets.length;
                                            totalHold = workoutResults.sets.reduce((sum, set) => {
                                                return sum + (set.actualReps || 0);
                                            }, 0);
                                        }

                                        return setCount > 0 ? Math.round(totalHold / setCount) : 0;
                                    })()}s
                                </div>
                                <div className="text-xs text-indigo-700">Avg Hold</div>
                            </div>
                        </>
                    ) : (
                        <>
                            <div className="text-center p-2 bg-green-50 rounded-lg">
                                <div className="text-lg font-bold text-green-600">
                                    {workoutResults.sets.reduce((sum, set) => sum + set.actualReps, 0)}
                                </div>
                                <div className="text-xs text-green-700">Total Reps</div>
                            </div>
                            {workoutResults.sets.some(set => set.actualWeight && set.actualWeight > 0) && (
                                <div className="text-center p-2 bg-purple-50 rounded-lg">
                                    <div className="text-lg font-bold text-purple-600">
                                        {Math.max(...workoutResults.sets.map(set => set.actualWeight || 0))}
                                        {workoutResults.sets[0]?.targetWeightUnit || 'lbs'}
                                    </div>
                                    <div className="text-xs text-purple-700">Max Weight</div>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        );
    };

    return (
        <div className="border-2 border-green-200 bg-green-50 rounded-xl p-4 relative">
            {/* Completed Badge */}
            <div
                className="absolute -top-2 -right-2 bg-green-500 text-white text-xs font-bold px-2 py-1 rounded-full shadow-md">
                DONE
            </div>

            {/* Header */}
            <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                    <div
                        className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center text-white font-bold">
                        <CheckCircle className="w-5 h-5"/>
                    </div>
                    <div>
                        <h3 className="font-bold text-gray-900">
                            {exercise.exercise.name || exercise.exercise.exerciseName}
                        </h3>
                        <div className="text-xs text-gray-600">
                            Completed at {new Date(workoutResults?.completedAt || '').toLocaleTimeString([], {
                            hour: '2-digit',
                            minute: '2-digit'
                        })}
                        </div>
                    </div>
                </div>

                {/* Enhanced Performance Badge */}
                {workoutResults && performanceEval && (
                    <div className={`px-3 py-1 rounded-full text-xs font-medium border flex items-center gap-1 ${
                        getPerformanceColor(performanceEval.overall)
                    }`}>
                        {getPerformanceIcon(performanceEval.overall)}
                        <span>{getPerformanceMessage(performanceEval.overall)}</span>
                    </div>
                )}
            </div>

            {/* Enhanced Results Display */}
            {workoutResults && (
                <div className="mb-4">
                    {renderEnhancedResults()}
                </div>
            )}

            {/* Duration and Notes */}
            <div className="space-y-2">
                {workoutResults?.totalDurationMinutes && (
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                        <Clock className="w-4 h-4"/>
                        <span>Total time: {workoutResults.totalDurationMinutes} minutes</span>
                    </div>
                )}

                {workoutResults?.notes && (
                    <div className="text-sm text-gray-700 bg-white p-2 rounded border">
                        <span className="font-medium">Notes:</span> {workoutResults.notes}
                    </div>
                )}
            </div>

            {/* Action Button */}
            <button
                onClick={onViewDetails}
                className="w-full mt-3 px-4 py-2 bg-green-600 hover:bg-green-700 text-white text-sm font-medium rounded-lg transition-colors flex items-center justify-center gap-2"
            >
                <Eye className="w-4 h-4"/>
                View Full Details
            </button>
        </div>
    );
};

export default CompletedWorkoutDisplay;