import React from 'react';
import {CheckCircle, XCircle} from 'lucide-react';
import {ScheduledExercise, WorkoutResults} from '../../types/exercise';

interface SetBySetViewProps {
    exercise: ScheduledExercise;
    workoutResults: WorkoutResults;
}

const SetBySetView: React.FC<SetBySetViewProps> = ({exercise, workoutResults}) => {
    workoutResults.sets.forEach(set => {
        console.log(`Set ${set.setNumber} - actualRestSeconds:`, set.actualRestSeconds);
    });
    // 🔧 FIXED: Helper function to get the correct value for each exercise type
    const getActualValue = (set: any) => {
        if (exercise.exercise.isCardio) {
            return set.actualDurationMinutes || set.targetReps || 0;
        } else if (exercise.exercise.isIsometric) {
            // Use the same fallback logic we've been applying
            let holdTime = 0;

            if (set.isometricData?.holdDurationSeconds) {
                holdTime = set.isometricData.holdDurationSeconds;
            } else if ((set as any).actualHoldSeconds) {
                holdTime = (set as any).actualHoldSeconds;
            } else if ((set as any).holdTime) {
                holdTime = (set as any).holdTime;
            } else if (set.actualReps) {
                // Legacy fallback
                holdTime = set.actualReps;
            }

            console.log(`🔍 SET-BY-SET: Set ${set.setNumber} hold time: ${holdTime}s`);
            return holdTime;
        } else {
            return set.actualReps || 0;
        }
    };

    // 🔧 FIXED: Helper function to get the target value for comparison
    const getTargetValue = (set: any) => {
        if (exercise.exercise.isCardio) {
            return exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes || 20;
        } else if (exercise.exercise.isIsometric) {
            return exercise.holdDurationSeconds || 30;
        } else {
            return set.targetReps || 0;
        }
    };

    // 🔧 FIXED: Helper function to determine if target was met
    const isTargetMet = (set: any) => {
        const actual = getActualValue(set);
        const target = getTargetValue(set);
        return actual >= target;
    };

    return (
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
                                <span className="text-gray-600">
                                    {exercise.exercise.isCardio ? 'Duration:' :
                                        exercise.exercise.isIsometric ? 'Hold Time:' : 'Reps:'}
                                </span>
                                <span className={`ml-2 font-medium ${
                                    isTargetMet(set) ? 'text-green-600' : 'text-red-600'
                                }`}>
                                    {getActualValue(set)}/{getTargetValue(set)}
                                    {exercise.exercise.isCardio ? ' min' :
                                        exercise.exercise.isIsometric ? 's' : ''}
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

                            {set.setNumber > 1 && (set.actualRestSeconds || set.restSeconds) && (
                                <div>
                                    <span className="text-gray-600">Rest before:</span>
                                    <span className="ml-2 font-medium">
                                        {set.actualRestSeconds ? (
                                            <>
                                                {Math.floor(set.actualRestSeconds / 60) > 0 && `${Math.floor(set.actualRestSeconds / 60)}m `}
                                                {set.actualRestSeconds % 60}s
                                                {set.restSeconds && (
                                                    <span className="text-xs text-gray-500 ml-1">
                                                        / {Math.floor((set.restSeconds || 0) / 60) > 0 && `${Math.floor((set.restSeconds || 0) / 60)}m `}
                                                        {(set.restSeconds || 0) % 60}s
                                                    </span>
                                                )}
                                            </>
                                        ) : (
                                            <>
                                                {Math.floor((set.restSeconds || 0) / 60) > 0 && `${Math.floor((set.restSeconds || 0) / 60)}m `}
                                                {(set.restSeconds || 0) % 60}s target
                                            </>
                                        )}
                                    </span>
                                    {set.actualRestSeconds && set.restSeconds && Math.abs(set.actualRestSeconds - set.restSeconds) > 5 && (
                                        <span className={`ml-2 text-xs font-medium ${
                                            set.actualRestSeconds <= set.restSeconds
                                                ? 'text-green-600'
                                                : 'text-orange-600'
                                        }`}>
                                            {set.actualRestSeconds <= set.restSeconds
                                                ? `↓${set.restSeconds - set.actualRestSeconds}s`
                                                : `↑${set.actualRestSeconds - set.restSeconds}s`}
                                        </span>
                                    )}
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
    );
};

export default SetBySetView;