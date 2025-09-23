import React from 'react';
import {CheckCircle, XCircle} from 'lucide-react';
import {ScheduledExercise, WorkoutResults} from '../../types/exercise';

interface SetBySetViewProps {
    exercise: ScheduledExercise;
    workoutResults: WorkoutResults;
}

const SetBySetView: React.FC<SetBySetViewProps> = ({exercise, workoutResults}) => {
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
                                    set.actualReps >= set.targetReps ? 'text-green-600' : 'text-red-600'
                                }`}>
                  {set.actualReps}/{exercise.exercise.isIsometric ? exercise.holdDurationSeconds : set.targetReps}
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
    );
};

export default SetBySetView;