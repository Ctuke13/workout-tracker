import React from 'react';
import {Clock, Eye, Target, TrendingUp, TrendingDown, Minus, CheckCircle, XCircle} from 'lucide-react';
import {ScheduledExercise, WorkoutResults} from '../../types/exercise';
import {analyzeWorkout} from '../../utils/workoutPerformanceAnalyzer';
import {
    getPerformanceColor,
    getPerformanceIcon,
    getPerformanceMessage,
    getStatusIcon
} from '../../utils/workoutDisplayHelpers';

interface CompletedWorkoutDisplayProps {
    exercise: ScheduledExercise;
    workoutResults?: WorkoutResults;
    onViewDetails: () => void;
}

const CompletedWorkoutDisplay: React.FC<CompletedWorkoutDisplayProps> = ({
                                                                             exercise,
                                                                             workoutResults,
                                                                             onViewDetails
                                                                         }) => {

    console.log('🔥 Workout Results Data:', {
        totalCaloriesCalculated: workoutResults?.totalCaloriesCalculated,
        caloriesBurned: workoutResults?.caloriesBurned,
        allFields: workoutResults ? Object.keys(workoutResults) : []
    });


    // Enhanced performance evaluation
    const analysis = workoutResults ?
        analyzeWorkout(exercise, workoutResults) : null;

    const getCriteriaIcon = (status: string) => {
        const IconComponent = getStatusIcon(status);
        return <IconComponent className="w-3 h-3"/>;
    };

    // 🔧 FIXED: Better isometric hold time calculation using correct field names
    const calculateIsometricHoldTimes = () => {
        if (!workoutResults?.sets) return {totalHold: 0, avgHold: 0};

        console.log('🔍 DEBUG: Calculating hold times from sets:', workoutResults.sets);

        let totalHold = 0;
        let completedSets = 0;

        workoutResults.sets.forEach((set, index) => {
            console.log(`🔍 DEBUG: Set ${index + 1}:`, {
                completed: set.completed,
                isometricData: set.isometricData,
                allFields: Object.keys(set)
            });

            if (set.completed) {
                completedSets++;

                // Use only fields that exist in the type definition
                let holdTime = 0;

                if (set.isometricData?.holdDurationSeconds) {
                    holdTime = set.isometricData.holdDurationSeconds;
                } else if ((set as any).actualHoldSeconds) {
                    // Fallback to any field that might exist but isn't typed
                    holdTime = (set as any).actualHoldSeconds;
                } else if ((set as any).holdDurationSeconds) {
                    holdTime = (set as any).holdDurationSeconds;
                }

                console.log(`🔍 DEBUG: Set ${index + 1} hold time: ${holdTime}s (from ${set.isometricData?.holdDurationSeconds ? 'isometricData.holdDurationSeconds' : 'fallback'})`);
                totalHold += holdTime;
            }
        });

        // Fallback: Check top-level actualHoldDurations array
        if (totalHold === 0 && workoutResults.actualHoldDurations?.length) {
            console.log('🔍 DEBUG: Using fallback actualHoldDurations:', workoutResults.actualHoldDurations);
            totalHold = workoutResults.actualHoldDurations.reduce((sum, hold) => sum + (hold || 0), 0);
            completedSets = workoutResults.actualHoldDurations.filter(hold => hold && hold > 0).length;
        }

        const avgHold = completedSets > 0 ? Math.round(totalHold / completedSets) : 0;

        console.log('🔍 DEBUG: Final calculation:', {
            totalHold,
            completedSets,
            avgHold
        });

        return {totalHold, avgHold};
    };

    const renderEnhancedResults = () => {
        if (!workoutResults || !analysis) return null;

        return (
            <div className="space-y-4">
                {/* Achievement Score */}
                <div className="bg-white rounded-lg p-3 border">
                    <div className="flex items-center justify-between mb-2">
                        <span className="text-sm font-medium text-gray-700">Target Achievement</span>
                        <span className="text-lg font-bold text-gray-900">
                            {Math.round(analysis.performance.achievementScore)}%
                        </span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                        <div
                            className={`h-2 rounded-full transition-all duration-300 ${
                                analysis.performance.achievementScore >= 100 ? 'bg-green-500' :
                                    analysis.performance.achievementScore >= 90 ? 'bg-blue-500' :
                                        analysis.performance.achievementScore >= 70 ? 'bg-yellow-500' :
                                            'bg-red-500'
                            }`}
                            style={{width: `${Math.min(analysis.performance.achievementScore, 100)}%`}}
                        />
                    </div>
                </div>

                {/* Criteria Breakdown */}
                <div className="space-y-2">
                    <div className="text-sm font-medium text-gray-700">Target Breakdown:</div>
                    <div className="space-y-1">
                        {analysis.performance.criteria.map((criterion, index) => (
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

                {/* Estimated Calories Section*/}
                {workoutResults?.caloriesBurned && (
                    <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                        <div className="flex items-center gap-3">
                            <div className="text-2xl">🔥</div>
                            <div className="flex-1">
                                <div className="text-sm text-gray-600">Estimated Calories Burned</div>
                                <div className="text-xl font-bold text-gray-900">
                                    {workoutResults.caloriesBurned} cal
                                </div>
                            </div>
                        </div>
                    </div>
                )}


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
                            {workoutResults.totalCaloriesCalculated && (
                                <div className="text-center p-2 bg-orange-50 rounded-lg">
                                    <div className="text-lg font-bold text-orange-600">
                                        {workoutResults.totalCaloriesCalculated}
                                    </div>
                                    <div className="text-xs text-orange-700">Calories</div>
                                </div>
                            )}
                        </>
                    ) : exercise.exercise.isIsometric ? (
                        <>
                            <div className="text-center p-2 bg-purple-50 rounded-lg">
                                <div className="text-lg font-bold text-purple-600">
                                    {calculateIsometricHoldTimes().totalHold}s
                                </div>
                                <div className="text-xs text-purple-700">Total Hold</div>
                            </div>
                            <div className="text-center p-2 bg-indigo-50 rounded-lg">
                                <div className="text-lg font-bold text-indigo-600">
                                    {calculateIsometricHoldTimes().avgHold}s
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
                            {workoutResults.totalCaloriesCalculated && (
                                <div className="text-center p-2 bg-orange-50 rounded-lg">
                                    <div className="text-lg font-bold text-orange-600">
                                        {workoutResults.totalCaloriesCalculated}
                                    </div>
                                    <div className="text-xs text-orange-700">Calories</div>
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
                {workoutResults && analysis && (
                    <div className={`px-3 py-1 rounded-full text-xs font-medium border flex items-center gap-1 ${
                        getPerformanceColor(analysis.performance.overall)
                    }`}>
                        {(() => {
                            const IconComponent = getPerformanceIcon(analysis.performance.overall);
                            return <IconComponent className="w-4 h-4"/>;
                        })()}
                        <span>{getPerformanceMessage(analysis.performance.overall)}</span>
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