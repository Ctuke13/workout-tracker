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
import {analyzeWorkout} from '../../utils/workoutPerformanceAnalyzer';
import {
    getPerformanceColor,
    getPerformanceIcon,
    getPerformanceMessage,
    getStatusIcon,
    getStatusColor
} from '../../utils/workoutDisplayHelpers';
import {useWorkoutAnalysis} from '../../hooks/useWorkoutAnalysis';
import PerformanceOverview from './PerformanceOverview';
import CriteriaBreakdown from './CriteriaBreakdown';
import SetBySetView from './SetBySetView';

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

    const analysis = useWorkoutAnalysis(exercise, workoutResults);

    if (!isOpen || !workoutResults) return null;

    const formatPace = (pace: number): string => {
        const mins = Math.floor(pace);
        const secs = Math.round((pace - mins) * 60);
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    const analysisDetails = analysis ? analysis.performance.criteria : [];

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
                    <PerformanceOverview workoutResults={workoutResults}/>

                    {/* Target vs Actual Analysis */}
                    <CriteriaBreakdown analysis={analysis}/>

                    {/* Set-by-Set Breakdown */}
                    <SetBySetView exercise={exercise} workoutResults={workoutResults}/>

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