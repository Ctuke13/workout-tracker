import React from 'react';
import {Dumbbell, Flame, Star} from 'lucide-react';

interface TodaysActivityProps {
    workoutsThisWeek: number;
    workoutsGoal?: number | null;
    currentStreak: number;
    xpThisWeek: number;
    goalProgress?: number | null;
    goalAchieved?: boolean | null;
}

const TodaysActivity: React.FC<TodaysActivityProps> = ({
                                                           workoutsThisWeek,
                                                           workoutsGoal,
                                                           currentStreak,
                                                           xpThisWeek,
                                                           goalProgress,
                                                           goalAchieved,
                                                       }) => {
    const actualGoal = workoutsGoal || 1; // Default to 1 if no goal set
    const progress = goalProgress !== null && goalProgress !== undefined
        ? Math.min(100, goalProgress * 100)
        : Math.min(100, (workoutsThisWeek / actualGoal) * 100);

    return (
        <div className="bg-gradient-to-br from-blue-50 to-cyan-50 rounded-2xl p-4 shadow-lg border border-blue-200">
            {/* Header */}
            <h3 className="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                <span className="text-lg">📊</span>
                This Week's Progress
            </h3>

            {/* Stats Grid */}
            <div className="space-y-3">
                {/* Workouts */}
                <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3">
                    <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                            <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                                <Dumbbell className="w-4 h-4 text-white"/>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600">Workouts</p>
                                <p className="text-sm font-bold text-gray-900">
                                    {workoutsThisWeek}/{actualGoal} {workoutsGoal ? 'completed' : 'this week'}
                                </p>
                            </div>
                        </div>
                        <span className={`text-lg font-bold ${
                            goalAchieved ? 'text-green-600' : 'text-gray-400'
                        }`}>
                            {goalAchieved ? '✓' : '○'}
                        </span>
                    </div>

                    {/* Progress Bar */}
                    <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                        <div
                            className="h-full bg-gradient-to-r from-blue-500 to-cyan-500 rounded-full transition-all duration-500"
                            style={{width: `${progress}%`}}
                        />
                    </div>
                </div>

                {/* Streak & XP Row */}
                <div className="grid grid-cols-2 gap-3">
                    {/* Streak */}
                    <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3">
                        <div className="flex items-center gap-2 mb-1">
                            <div className="w-7 h-7 bg-orange-500 rounded-full flex items-center justify-center">
                                <Flame className="w-4 h-4 text-white"/>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600">Streak</p>
                                <p className="text-lg font-bold text-orange-600">{currentStreak}</p>
                            </div>
                        </div>
                        <p className="text-[10px] text-gray-500">days in a row</p>
                    </div>

                    {/* XP Earned This Week */}
                    <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3">
                        <div className="flex items-center gap-2 mb-1">
                            <div className="w-7 h-7 bg-purple-500 rounded-full flex items-center justify-center">
                                <Star className="w-4 h-4 text-white"/>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600">XP</p>
                                <p className="text-lg font-bold text-purple-600">{xpThisWeek}</p>
                            </div>
                        </div>
                        <p className="text-[10px] text-gray-500">earned this week</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TodaysActivity;