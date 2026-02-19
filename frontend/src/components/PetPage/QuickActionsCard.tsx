import React from 'react';
import {Calendar, ChevronRight, TrendingUp} from 'lucide-react';

interface QuickActionsCardProps {
    onPlanWorkout: () => void;
    workoutsThisWeek?: number;
}

const QuickActionsCard: React.FC<QuickActionsCardProps> = ({
                                                               onPlanWorkout,
                                                               workoutsThisWeek = 0,
                                                           }) => {
    return (
        <div
            className="quick-actions-card bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl p-4 shadow-lg border border-blue-200">
            <div className="flex items-center gap-3 mb-3">
                <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
                    <Calendar className="w-5 h-5 text-white"/>
                </div>
                <h3 className="text-lg font-bold text-gray-900">Workout Planning</h3>
            </div>

            {/* Info Section */}
            <div className="mb-4">
                <p className="text-gray-700 mb-2 flex items-center gap-2">
                    <TrendingUp className="w-4 h-4 text-blue-600"/>
                    <span className="text-sm">
                        {workoutsThisWeek === 0
                            ? 'No workouts planned yet this week'
                            : `${workoutsThisWeek} workout${workoutsThisWeek !== 1 ? 's' : ''} this week`}
                    </span>
                </p>
                <p className="text-sm text-gray-600">
                    View your calendar and schedule your next session
                </p>
            </div>

            {/* CTA Button */}
            <button
                onClick={onPlanWorkout}
                className="w-full flex items-center justify-between px-4 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold rounded-xl transition-all shadow-md hover:shadow-lg transform hover:scale-[1.02] active:scale-[0.98]"
            >
                <div className="flex items-center gap-2">
                    <Calendar className="w-5 h-5"/>
                    <span>Open Calendar</span>
                </div>
                <ChevronRight className="w-5 h-5"/>
            </button>

            {/* Quick Stats (if workouts exist) */}
            {workoutsThisWeek > 0 && (
                <div className="mt-3 pt-3 border-t border-blue-200 grid grid-cols-2 gap-2">
                    <div className="text-center p-2 bg-white/60 rounded-lg">
                        <div className="text-xs text-gray-600">Completed</div>
                        <div className="text-sm font-bold text-blue-600">{workoutsThisWeek}</div>
                    </div>
                    <div className="text-center p-2 bg-white/60 rounded-lg">
                        <div className="text-xs text-gray-600">This Week</div>
                        <div className="text-sm font-bold text-blue-600">📅</div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default QuickActionsCard;