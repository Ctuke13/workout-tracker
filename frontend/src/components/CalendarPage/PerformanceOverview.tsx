import React from 'react';
import {Target} from 'lucide-react';
import {WorkoutResults} from '../../types/exercise';
import {getStatusColor} from '../../utils/workoutDisplayHelpers';

interface PerformanceOverviewProps {
    workoutResults: WorkoutResults;
}

const PerformanceOverview: React.FC<PerformanceOverviewProps> = ({workoutResults}) => {
    return (
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
    );
};

export default PerformanceOverview;