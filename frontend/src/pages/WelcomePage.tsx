import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useLocation } from 'react-router-dom';

interface DashboardStats {
    totalWorkouts: number;
    currentStreak: number;
    totalVolume: number;
    recentPRs: number;
}

const WelcomePage: React.FC = () => {
    const { user } = useAuth();
    const location = useLocation();
    const [stats] = useState<DashboardStats>({
        totalWorkouts: 0,
        currentStreak: 0,
        totalVolume: 0,
        recentPRs: 0
    });

    // Type-safe way to access location state
    const isFromRegistration = (location as any).state?.fromRegistration || false;
    const isNewUser = stats.totalWorkouts === 0;

    return (
        <div className="p-4 pb-20"> {/* Add bottom padding for bottom nav */}
            {/* Welcome Header */}
            <div className="mb-6">
                <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">
                    {isFromRegistration
                        ? `Welcome to WorkoutTracker, ${user?.firstName || 'Athlete'}! 💪`
                        : `Welcome back, ${user?.firstName || 'Athlete'}! 💪`
                    }
                </h1>
                <p className="text-gray-600">
                    {isFromRegistration
                        ? "Thanks for joining! Let's get you started with your first workout."
                        : isNewUser
                            ? "Ready to start your fitness journey? Let's log your first workout!"
                            : "Ready to crush another workout?"
                    }
                </p>
            </div>

            {isNewUser ? <NewUserDashboard /> : <ReturningUserDashboard stats={stats} />}
        </div>
    );
};

// Component for new users
const NewUserDashboard: React.FC = () => (
    <div className="space-y-6">
        {/* Quick Start Section */}
        <div className="bg-white rounded-xl shadow-lg p-4 sm:p-6">
            <h2 className="text-lg sm:text-xl font-bold text-gray-900 mb-4">🚀 Quick Start</h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4">
                <button className="p-3 sm:p-4 border-2 border-blue-500 bg-blue-50 rounded-lg text-center hover:bg-blue-100 transition-colors">
                    <div className="text-xl sm:text-2xl mb-2">🏋️‍♂️</div>
                    <div className="font-semibold text-blue-700 text-sm sm:text-base">Log First Workout</div>
                    <div className="text-xs sm:text-sm text-blue-600">Start tracking your progress</div>
                </button>

                <button className="p-3 sm:p-4 border-2 border-gray-300 rounded-lg text-center hover:bg-gray-50 transition-colors">
                    <div className="text-xl sm:text-2xl mb-2">📚</div>
                    <div className="font-semibold text-gray-700 text-sm sm:text-base">Browse Exercises</div>
                    <div className="text-xs sm:text-sm text-gray-600">Explore our exercise library</div>
                </button>

                <button className="p-3 sm:p-4 border-2 border-gray-300 rounded-lg text-center hover:bg-gray-50 transition-colors">
                    <div className="text-xl sm:text-2xl mb-2">📊</div>
                    <div className="font-semibold text-gray-700 text-sm sm:text-base">Set Goals</div>
                    <div className="text-xs sm:text-sm text-gray-600">Define your fitness targets</div>
                </button>
            </div>
        </div>

        {/* Getting Started Guide */}
        <div className="bg-white rounded-xl shadow-lg p-4 sm:p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Getting Started Guide</h3>
            <div className="space-y-3">
                <div className="flex items-center">
                    <div className="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold mr-3 flex-shrink-0">1</div>
                    <span className="text-sm sm:text-base">Log your first workout to start tracking progress</span>
                </div>
                <div className="flex items-center">
                    <div className="w-6 h-6 bg-gray-300 text-white rounded-full flex items-center justify-center text-sm font-bold mr-3 flex-shrink-0">2</div>
                    <span className="text-gray-500 text-sm sm:text-base">Complete 3 workouts to unlock analytics</span>
                </div>
                <div className="flex items-center">
                    <div className="w-6 h-6 bg-gray-300 text-white rounded-full flex items-center justify-center text-sm font-bold mr-3 flex-shrink-0">3</div>
                    <span className="text-gray-500 text-sm sm:text-base">Build a 7-day streak for bonus features</span>
                </div>
            </div>
        </div>
    </div>
);

// Component for returning users
const ReturningUserDashboard: React.FC<{ stats: DashboardStats }> = ({ stats }) => (
    <div className="space-y-6">
        {/* Stats Cards */}
        <div className="grid grid-cols-2 gap-3 sm:gap-4">
            <div className="bg-white rounded-lg shadow-lg p-3 sm:p-4 text-center">
                <div className="text-xl sm:text-2xl font-bold text-blue-600">{stats.totalWorkouts}</div>
                <div className="text-xs sm:text-sm text-gray-600">Total Workouts</div>
            </div>
            <div className="bg-white rounded-lg shadow-lg p-3 sm:p-4 text-center">
                <div className="text-xl sm:text-2xl font-bold text-green-600">{stats.currentStreak}</div>
                <div className="text-xs sm:text-sm text-gray-600">Day Streak</div>
            </div>
            <div className="bg-white rounded-lg shadow-lg p-3 sm:p-4 text-center">
                <div className="text-xl sm:text-2xl font-bold text-purple-600">{stats.totalVolume.toLocaleString()}</div>
                <div className="text-xs sm:text-sm text-gray-600">Total Volume (lbs)</div>
            </div>
            <div className="bg-white rounded-lg shadow-lg p-3 sm:p-4 text-center">
                <div className="text-xl sm:text-2xl font-bold text-orange-600">{stats.recentPRs}</div>
                <div className="text-xs sm:text-sm text-gray-600">Recent PRs</div>
            </div>
        </div>

        {/* Quick Actions */}
        <div className="bg-white rounded-xl shadow-lg p-4 sm:p-6">
            <h2 className="text-lg sm:text-xl font-bold text-gray-900 mb-4">Quick Actions</h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4">
                <button className="p-3 sm:p-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                    <div className="text-lg sm:text-xl mb-2">🏋️‍♂️</div>
                    <div className="font-semibold text-sm sm:text-base">Log Workout</div>
                </button>
                <button className="p-3 sm:p-4 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                    <div className="text-lg sm:text-xl mb-2">📊</div>
                    <div className="font-semibold text-sm sm:text-base">View Progress</div>
                </button>
                <button className="p-3 sm:p-4 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors">
                    <div className="text-lg sm:text-xl mb-2">📚</div>
                    <div className="font-semibold text-sm sm:text-base">Browse Exercises</div>
                </button>
            </div>
        </div>
    </div>
);

export default WelcomePage;