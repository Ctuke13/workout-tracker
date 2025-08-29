import React from 'react';
import {
    X,
    Target,
    TrendingUp,
    TrendingDown,
    CheckCircle,
    XCircle,
    Minus,
    Clock,
    Weight,
    Zap,
    Heart
} from 'lucide-react';
import {ScheduledExercise, WorkoutResults} from '../../types/exercise';

interface WorkoutDetailsModalProps {
    isOpen: boolean;
    onClose: () => void;
    exercise: ScheduledExercise;
    workoutResults?: WorkoutResults;
}

interface CriterionDetail {
    name: string;
    target: any;
    actual: any;
    status: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET' | 'NOT_SET';
    displayText: string;
    unit?: string;
    percentage?: number;
}

const WorkoutDetailsModal: React.FC<WorkoutDetailsModalProps> = ({
                                                                     isOpen,
                                                                     onClose,
                                                                     exercise,
                                                                     workoutResults
                                                                 }) => {
    if (!isOpen || !workoutResults) return null;

    // Calculate detailed target vs actual analysis
    const getDetailedAnalysis = (): CriterionDetail[] => {
        const details: CriterionDetail[] = [];

        // Sets Analysis
        if (exercise.targetSets) {
            const completedSets = workoutResults.sets.filter(s => s.completed).length;
            const percentage = (completedSets / exercise.targetSets) * 100;

            details.push({
                name: 'Sets Completed',
                target: exercise.targetSets,
                actual: completedSets,
                status: completedSets >= exercise.targetSets ? 'MET' :
                    completedSets >= exercise.targetSets * 0.8 ? 'PARTIAL' : 'BELOW_TARGET',
                displayText: `${completedSets} of ${exercise.targetSets}`,
                unit: 'sets',
                percentage
            });
        }

        if (exercise.exercise.isCardio) {
            // Duration Analysis
            const targetDuration = exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes;
            if (targetDuration) {
                const actualDuration = workoutResults.actualDurationMinutes || workoutResults.totalDurationMinutes;
                const percentage = (actualDuration / targetDuration) * 100;

                details.push({
                    name: 'Duration',
                    target: targetDuration,
                    actual: actualDuration,
                    status: actualDuration >= targetDuration ?
                        (actualDuration >= targetDuration * 1.1 ? 'EXCEEDED' : 'MET') :
                        (actualDuration >= targetDuration * 0.8 ? 'PARTIAL' : 'BELOW_TARGET'),
                    displayText: `${actualDuration} of ${targetDuration}`,
                    unit: 'minutes',
                    percentage
                });
            }

            // Distance Analysis
            if (exercise.targetDistance || exercise.targetDistanceKm) {
                const targetDistance = exercise.targetDistance ||
                    (exercise.targetDistanceKm ? exercise.targetDistanceKm * 0.621371 : 0);
                const actualDistance = workoutResults.actualDistanceKm ?
                    workoutResults.actualDistanceKm * 0.621371 : 0;
                const percentage = targetDistance > 0 ? (actualDistance / targetDistance) * 100 : 0;

                details.push({
                    name: 'Distance',
                    target: targetDistance,
                    actual: actualDistance,
                    status: actualDistance >= targetDistance ?
                        (actualDistance >= targetDistance * 1.05 ? 'EXCEEDED' : 'MET') :
                        (actualDistance >= targetDistance * 0.9 ? 'PARTIAL' : 'BELOW_TARGET'),
                    displayText: `${actualDistance.toFixed(1)} of ${targetDistance.toFixed(1)}`,
                    unit: 'miles',
                    percentage
                });
            }

            // Pace Analysis (lower is better)
            if (exercise.targetPace && workoutResults.actualPace) {
                const percentage = (exercise.targetPace / workoutResults.actualPace) * 100; // Inverted for pace

                details.push({
                    name: 'Pace',
                    target: exercise.targetPace,
                    actual: workoutResults.actualPace,
                    status: workoutResults.actualPace <= exercise.targetPace ?
                        (workoutResults.actualPace <= exercise.targetPace * 0.95 ? 'EXCEEDED' : 'MET') :
                        (workoutResults.actualPace <= exercise.targetPace * 1.1 ? 'PARTIAL' : 'BELOW_TARGET'),
                    displayText: `${formatPace(workoutResults.actualPace)} vs ${formatPace(exercise.targetPace)}`,
                    unit: 'min/mile',
                    percentage
                });
            }

        } else if (exercise.exercise.isIsometric) {
            // Hold Duration Analysis
            if (exercise.holdDurationSeconds) {
                // Try multiple data sources for hold times
                let totalActualHold = 0;
                let actualHoldArray: number[] = [];

                if (workoutResults.actualHoldDurations && workoutResults.actualHoldDurations.length > 0) {
                    // Primary: Use actualHoldDurations array
                    totalActualHold = workoutResults.actualHoldDurations.reduce((sum, hold) => sum + hold, 0);
                    actualHoldArray = workoutResults.actualHoldDurations;
                } else if (workoutResults.sets.some(set => set.isometricData?.holdDurationSeconds)) {
                    // Fallback: Use set-level isometric data
                    totalActualHold = workoutResults.sets.reduce((sum, set) =>
                        sum + (set.isometricData?.holdDurationSeconds || 0), 0);
                    actualHoldArray = workoutResults.sets
                        .map(set => set.isometricData?.holdDurationSeconds || 0)
                        .filter(hold => hold > 0);
                } else {
                    // Last resort: Use actualReps as hold time (for isometric, reps often represent seconds)
                    totalActualHold = workoutResults.sets.reduce((sum, set) => sum + (set.actualReps || 0), 0);
                    actualHoldArray = workoutResults.sets.map(set => set.actualReps || 0);
                }

                const expectedTotalHold = exercise.holdDurationSeconds * (exercise.targetSets || 3);
                const percentage = (totalActualHold / expectedTotalHold) * 100;

                details.push({
                    name: 'Total Hold Time',
                    target: expectedTotalHold,
                    actual: totalActualHold,
                    status: totalActualHold >= expectedTotalHold ?
                        (totalActualHold >= expectedTotalHold * 1.1 ? 'EXCEEDED' : 'MET') :
                        (totalActualHold >= expectedTotalHold * 0.8 ? 'PARTIAL' : 'BELOW_TARGET'),
                    displayText: `${totalActualHold}s of ${expectedTotalHold}s`,
                    unit: 'seconds',
                    percentage
                });

                // Average Hold Analysis
                if (actualHoldArray.length > 0) {
                    const avgActualHold = totalActualHold / actualHoldArray.length;
                    const avgPercentage = (avgActualHold / exercise.holdDurationSeconds) * 100;

                    details.push({
                        name: 'Average Hold Time',
                        target: exercise.holdDurationSeconds,
                        actual: Math.round(avgActualHold),
                        status: avgActualHold >= exercise.holdDurationSeconds ?
                            (avgActualHold >= exercise.holdDurationSeconds * 1.1 ? 'EXCEEDED' : 'MET') :
                            (avgActualHold >= exercise.holdDurationSeconds * 0.8 ? 'PARTIAL' : 'BELOW_TARGET'),
                        displayText: `${Math.round(avgActualHold)}s of ${exercise.holdDurationSeconds}s`,
                        unit: 'seconds per set',
                        percentage: avgPercentage
                    });
                }
            }

        } else {
            // Strength Exercise Analysis

            // Total Reps Analysis
            if (exercise.targetReps) {
                const targetReps = typeof exercise.targetReps === 'number' ?
                    exercise.targetReps : parseInt(String(exercise.targetReps), 10);
                const totalActualReps = workoutResults.sets.reduce((sum, set) => sum + set.actualReps, 0);
                const expectedTotalReps = targetReps * workoutResults.sets.length;
                const percentage = (totalActualReps / expectedTotalReps) * 100;

                details.push({
                    name: 'Total Reps',
                    target: expectedTotalReps,
                    actual: totalActualReps,
                    status: totalActualReps >= expectedTotalReps ?
                        (totalActualReps >= expectedTotalReps * 1.1 ? 'EXCEEDED' : 'MET') :
                        (totalActualReps >= expectedTotalReps * 0.8 ? 'PARTIAL' : 'BELOW_TARGET'),
                    displayText: `${totalActualReps} of ${expectedTotalReps}`,
                    unit: 'reps',
                    percentage
                });

                // Rep Consistency Analysis
                const setConsistency = workoutResults.sets.filter(set =>
                    set.actualReps >= targetReps).length;
                const consistencyPercentage = (setConsistency / workoutResults.sets.length) * 100;

                details.push({
                    name: 'Rep Consistency',
                    target: workoutResults.sets.length,
                    actual: setConsistency,
                    status: setConsistency === workoutResults.sets.length ? 'MET' :
                        setConsistency >= workoutResults.sets.length * 0.8 ? 'PARTIAL' : 'BELOW_TARGET',
                    displayText: `${setConsistency} of ${workoutResults.sets.length} sets`,
                    unit: 'sets at target',
                    percentage: consistencyPercentage
                });
            }

            // Weight Analysis
            if (exercise.targetWeight && exercise.targetWeight > 0) {
                const maxActualWeight = Math.max(...workoutResults.sets.map(set => set.actualWeight || 0));
                const percentage = (maxActualWeight / exercise.targetWeight) * 100;

                details.push({
                    name: 'Max Weight',
                    target: exercise.targetWeight,
                    actual: maxActualWeight,
                    status: maxActualWeight >= exercise.targetWeight ?
                        (maxActualWeight > exercise.targetWeight * 1.05 ? 'EXCEEDED' : 'MET') :
                        (maxActualWeight >= exercise.targetWeight * 0.9 ? 'PARTIAL' : 'BELOW_TARGET'),
                    displayText: `${maxActualWeight} of ${exercise.targetWeight}`,
                    unit: exercise.targetWeightUnit || 'lbs',
                    percentage
                });

                // Weight Consistency
                const weightConsistency = workoutResults.sets.filter(set => {
                    const targetWeight = exercise.targetWeight;
                    if (targetWeight === undefined) return false;
                    return (set.actualWeight || 0) >= targetWeight;
                }).length;
                const weightConsistencyPercentage = (weightConsistency / workoutResults.sets.length) * 100;

                details.push({
                    name: 'Weight Consistency',
                    target: workoutResults.sets.length,
                    actual: weightConsistency,
                    status: weightConsistency === workoutResults.sets.length ? 'MET' :
                        weightConsistency >= workoutResults.sets.length * 0.8 ? 'PARTIAL' : 'BELOW_TARGET',
                    displayText: `${weightConsistency} of ${workoutResults.sets.length} sets`,
                    unit: 'sets at target weight',
                    percentage: weightConsistencyPercentage
                });
            }

            // RPE Analysis
            if (exercise.targetRpe) {
                const rpeValues = workoutResults.sets
                    .filter(set => set.rpe && set.rpe > 0)
                    .map(set => set.rpe!);

                if (rpeValues.length > 0) {
                    const avgRpe = rpeValues.reduce((sum, rpe) => sum + rpe, 0) / rpeValues.length;
                    // For RPE, lower is better (less perceived exertion for same work)
                    const percentage = (exercise.targetRpe / avgRpe) * 100;

                    details.push({
                        name: 'Average RPE',
                        target: exercise.targetRpe,
                        actual: Math.round(avgRpe * 10) / 10,
                        status: avgRpe <= exercise.targetRpe ?
                            (avgRpe <= exercise.targetRpe - 1 ? 'EXCEEDED' : 'MET') :
                            (avgRpe <= exercise.targetRpe + 1 ? 'PARTIAL' : 'BELOW_TARGET'),
                        displayText: `${(Math.round(avgRpe * 10) / 10)} vs ${exercise.targetRpe}`,
                        unit: 'effort level',
                        percentage
                    });
                }
            }
        }

        return details;
    };

    const formatPace = (pace: number): string => {
        const mins = Math.floor(pace);
        const secs = Math.round((pace - mins) * 60);
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const getStatusIcon = (status: string) => {
        switch (status) {
            case 'EXCEEDED':
                return <TrendingUp className="w-4 h-4 text-green-600"/>;
            case 'MET':
                return <CheckCircle className="w-4 h-4 text-blue-600"/>;
            case 'PARTIAL':
                return <Minus className="w-4 h-4 text-yellow-600"/>;
            case 'BELOW_TARGET':
                return <TrendingDown className="w-4 h-4 text-red-600"/>;
            default:
                return <XCircle className="w-4 h-4 text-gray-400"/>;
        }
    };

    const getStatusColor = (status: string) => {
        switch (status) {
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

    const analysisDetails = getDetailedAnalysis();

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="sticky top-0 bg-white border-b border-gray-200 p-6 flex items-center justify-between">
                    <div>
                        <h2 className="text-2xl font-bold text-gray-900">
                            {exercise.exercise.name || exercise.exercise.exerciseName}
                        </h2>
                        <p className="text-sm text-gray-600">
                            Completed on {new Date(workoutResults.completedAt).toLocaleDateString()} at{' '}
                            {new Date(workoutResults.completedAt).toLocaleTimeString([], {
                                hour: '2-digit',
                                minute: '2-digit'
                            })}
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                    >
                        <X className="w-6 h-6 text-gray-600"/>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 space-y-6">
                    {/* Performance Overview */}
                    <div className="bg-gray-50 rounded-lg p-4">
                        <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
                            <Target className="w-5 h-5"/>
                            Performance Overview
                        </h3>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                            <div className="text-center p-3 bg-white rounded-lg border">
                                <div className="text-2xl font-bold text-blue-600">
                                    {workoutResults.sets.filter(s => s.completed).length}
                                </div>
                                <div className="text-sm text-gray-600">Sets Completed</div>
                            </div>

                            <div className="text-center p-3 bg-white rounded-lg border">
                                <div className="text-2xl font-bold text-green-600">
                                    {workoutResults.totalDurationMinutes}m
                                </div>
                                <div className="text-sm text-gray-600">Total Duration</div>
                            </div>

                            <div className="text-center p-3 bg-white rounded-lg border">
                                <div
                                    className={`text-2xl font-bold px-3 py-1 rounded-full text-xs border inline-block ${
                                        getStatusColor(workoutResults.performanceRating)
                                    }`}>
                                    {workoutResults.performanceRating.replace('_', ' ')}
                                </div>
                                <div className="text-sm text-gray-600 mt-1">Overall Rating</div>
                            </div>
                        </div>
                    </div>

                    {/* Target vs Actual Analysis */}
                    <div>
                        <h3 className="text-lg font-semibold mb-4">Target vs Actual Performance</h3>
                        <div className="space-y-4">
                            {analysisDetails.map((detail, index) => (
                                <div key={index} className="border border-gray-200 rounded-lg p-4">
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center gap-2">
                                            {getStatusIcon(detail.status)}
                                            <span className="font-medium text-gray-900">{detail.name}</span>
                                        </div>
                                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                            getStatusColor(detail.status)
                                        }`}>
                                            {detail.status.replace('_', ' ')}
                                        </span>
                                    </div>

                                    <div className="flex items-center justify-between mb-2">
                                        <span className="text-sm text-gray-600">{detail.displayText}</span>
                                        {detail.unit && (
                                            <span className="text-xs text-gray-500">{detail.unit}</span>
                                        )}
                                    </div>

                                    {detail.percentage && (
                                        <div className="w-full bg-gray-200 rounded-full h-2">
                                            <div
                                                className={`h-2 rounded-full transition-all duration-300 ${
                                                    detail.status === 'EXCEEDED' ? 'bg-green-500' :
                                                        detail.status === 'MET' ? 'bg-blue-500' :
                                                            detail.status === 'PARTIAL' ? 'bg-yellow-500' :
                                                                'bg-red-500'
                                                }`}
                                                style={{width: `${Math.min(detail.percentage, 100)}%`}}
                                            />
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Set-by-Set Breakdown */}
                    <div>
                        <h3 className="text-lg font-semibold mb-4">Set-by-Set Breakdown</h3>
                        <div className="space-y-3">
                            {workoutResults.sets.map((set, index) => (
                                <div key={index} className={`border rounded-lg p-4 ${
                                    set.completed ? 'border-green-200 bg-green-50' : 'border-gray-200 bg-gray-50'
                                }`}>
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center gap-2">
                                            {set.completed ? (
                                                <CheckCircle className="w-4 h-4 text-green-600"/>
                                            ) : (
                                                <XCircle className="w-4 h-4 text-gray-400"/>
                                            )}
                                            <span className="font-medium">Set {set.setNumber}</span>
                                        </div>
                                        {set.completed && (
                                            <span className="text-xs text-green-600 font-medium">Completed</span>
                                        )}
                                    </div>

                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                                        <div>
                                            <span className="text-gray-600">Reps:</span>
                                            <span className={`ml-2 font-medium ${
                                                set.actualReps >= set.targetReps ? 'text-green-600' : 'text-red-600'
                                            }`}>
                                                {set.actualReps}/{set.targetReps}
                                            </span>
                                        </div>

                                        {set.actualWeight && (
                                            <div>
                                                <span className="text-gray-600">Weight:</span>
                                                <span className="ml-2 font-medium">
                                                    {set.actualWeight}{set.targetWeightUnit}
                                                </span>
                                            </div>
                                        )}

                                        {set.rpe && (
                                            <div>
                                                <span className="text-gray-600">RPE:</span>
                                                <span className="ml-2 font-medium">{set.rpe}/10</span>
                                            </div>
                                        )}

                                        {set.restSeconds && (
                                            <div>
                                                <span className="text-gray-600">Rest:</span>
                                                <span className="ml-2 font-medium">{set.restSeconds}s</span>
                                            </div>
                                        )}
                                    </div>

                                    {set.notes && (
                                        <div className="mt-2 p-2 bg-white rounded border">
                                            <span className="text-xs font-medium text-gray-600">Notes:</span>
                                            <span className="text-xs text-gray-700 ml-2">{set.notes}</span>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Additional Metrics */}
                    {(workoutResults.personalRecords.length > 0 || workoutResults.improvements.length > 0) && (
                        <div>
                            <h3 className="text-lg font-semibold mb-4">Achievements & Improvements</h3>

                            {workoutResults.personalRecords.length > 0 && (
                                <div className="mb-4">
                                    <h4 className="font-medium text-green-600 mb-2 flex items-center gap-2">
                                        <TrendingUp className="w-4 h-4"/>
                                        Personal Records
                                    </h4>
                                    <div className="space-y-2">
                                        {workoutResults.personalRecords.map((pr, index) => (
                                            <div key={index}
                                                 className="bg-green-50 border border-green-200 rounded-lg p-3">
                                                <div
                                                    className="font-medium text-green-800">{pr.type.replace('_', ' ')}</div>
                                                <div className="text-sm text-green-700">
                                                    New: {pr.newValue} {pr.unit}
                                                    {pr.previousValue && (
                                                        <span className="ml-2 text-green-600">
                                                            (Previous: {pr.previousValue} {pr.unit})
                                                        </span>
                                                    )}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {workoutResults.improvements.length > 0 && (
                                <div>
                                    <h4 className="font-medium text-blue-600 mb-2 flex items-center gap-2">
                                        <TrendingUp className="w-4 h-4"/>
                                        Improvements
                                    </h4>
                                    <div className="space-y-2">
                                        {workoutResults.improvements.map((improvement, index) => (
                                            <div key={index}
                                                 className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                                                <div className="font-medium text-blue-800">
                                                    {improvement.metric.replace('_', ' ')}
                                                </div>
                                                <div className="text-sm text-blue-700">
                                                    +{improvement.improvementPercentage.toFixed(1)}% improvement
                                                    <span className="ml-2">
                                                        ({improvement.previousValue} → {improvement.currentValue})
                                                    </span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {/* Workout Notes */}
                    {(workoutResults.notes || workoutResults.workoutNotes) && (
                        <div>
                            <h3 className="text-lg font-semibold mb-4">Notes</h3>
                            <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                                {workoutResults.notes && (
                                    <div className="mb-2">
                                        <span className="font-medium text-gray-700">Exercise Notes:</span>
                                        <p className="text-gray-600 mt-1">{workoutResults.notes}</p>
                                    </div>
                                )}
                                {workoutResults.workoutNotes && (
                                    <div>
                                        <span className="font-medium text-gray-700">Workout Notes:</span>
                                        <p className="text-gray-600 mt-1">{workoutResults.workoutNotes}</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="border-t border-gray-200 p-6 bg-gray-50">
                    <button
                        onClick={onClose}
                        className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
                    >
                        Close Details
                    </button>
                </div>
            </div>
        </div>
    );
};

export default WorkoutDetailsModal;