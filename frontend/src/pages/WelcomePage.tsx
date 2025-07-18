// src/pages/WelcomePage.tsx - Tailwind CSS Version
import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useLocation, useNavigate } from 'react-router-dom';
import {
    PlayIcon,
    BookOpenIcon,
    ChartBarIcon,
    CheckCircleIcon,
    ClockIcon
} from '@heroicons/react/24/outline';

interface DashboardStats {
    totalWorkouts: number;
    currentStreak: number;
    totalVolume: number;
    recentPRs: number;
}

const WelcomePage: React.FC = () => {
    const { user } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const [stats] = useState<DashboardStats>({
        totalWorkouts: 0,
        currentStreak: 0,
        totalVolume: 0,
        recentPRs: 0
    });

    // Type-safe way to access location state
    const isFromRegistration = (location as any).state?.fromRegistration || false;
    const isNewUser = stats.totalWorkouts === 0;

    const handleLogFirstWorkout = () => {
        // Navigate to calendar with special state to open exercise selector
        navigate('/calendar', {
            state: {
                openExerciseSelector: true,
                selectedDate: new Date().toISOString().split('T')[0] // Today's date
            }
        });
    };

    const handleBrowseExercises = () => {
        navigate('/exercises');
    };

    const handleViewProgress = () => {
        navigate('/progress');
    };

    const handleSetGoals = () => {
        // TODO: Navigate to goals page when implemented
        console.log('Set Goals clicked');
    };

    return (
        <div className="max-w-7xl mx-auto px-4 py-6 pb-20">
            {/* Welcome Header */}
            <div className="mb-8 animate-fade-in">
                <h1 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
                    {isFromRegistration
                        ? `Welcome to WorkoutTracker, ${user?.firstName || 'Athlete'}! 💪`
                        : `Welcome back, ${user?.firstName || 'Athlete'}! 💪`
                    }
                </h1>
                <p className="text-lg text-gray-600">
                    {isFromRegistration
                        ? "Thanks for joining! Let's get you started with your first workout."
                        : isNewUser
                            ? "Ready to start your fitness journey? Let's log your first workout!"
                            : "Ready to crush another workout?"
                    }
                </p>
            </div>

            {isNewUser ? (
                <NewUserDashboard
                    onLogFirstWorkout={handleLogFirstWorkout}
                    onBrowseExercises={handleBrowseExercises}
                    onSetGoals={handleSetGoals}
                />
            ) : (
                <ReturningUserDashboard
                    stats={stats}
                    onLogWorkout={handleLogFirstWorkout}
                    onViewProgress={handleViewProgress}
                    onBrowseExercises={handleBrowseExercises}
                />
            )}
        </div>
    );
};

// Component for new users - Tailwind version
interface NewUserDashboardProps {
    onLogFirstWorkout: () => void;
    onBrowseExercises: () => void;
    onSetGoals: () => void;
}

const NewUserDashboard: React.FC<NewUserDashboardProps> = ({
                                                               onLogFirstWorkout,
                                                               onBrowseExercises,
                                                               onSetGoals
                                                           }) => (
    <div className="space-y-8">
        {/* Quick Start Section */}
        <div className="bg-white rounded-xl shadow-lg p-6 animate-slide-up">
            <div className="flex items-center mb-6">
                <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center mr-3">
                    <PlayIcon className="w-6 h-6 text-white" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900">🚀 Quick Start</h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <button
                    onClick={onLogFirstWorkout}
                    className="group p-6 border-2 border-blue-500 bg-blue-50 rounded-xl text-center hover:bg-blue-100 transition-all duration-300 hover:scale-105 hover:shadow-lg"
                >
                    <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                        🏋️‍♂️
                    </div>
                    <h3 className="font-semibold text-blue-700 text-lg mb-2">Log First Workout</h3>
                    <p className="text-sm text-blue-600">Start tracking your progress</p>
                </button>

                <button
                    onClick={onBrowseExercises}
                    className="group p-6 border-2 border-gray-300 rounded-xl text-center hover:bg-gray-50 transition-all duration-300 hover:scale-105 hover:shadow-lg"
                >
                    <div className="w-12 h-12 bg-gray-600 rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                        <BookOpenIcon className="w-6 h-6 text-white" />
                    </div>
                    <h3 className="font-semibold text-gray-700 text-lg mb-2">Browse Exercises</h3>
                    <p className="text-sm text-gray-600">Explore our exercise library</p>
                </button>

                <button
                    onClick={onSetGoals}
                    className="group p-6 border-2 border-gray-300 rounded-xl text-center hover:bg-gray-50 transition-all duration-300 hover:scale-105 hover:shadow-lg"
                >
                    <div className="w-12 h-12 bg-gray-600 rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                        <ChartBarIcon className="w-6 h-6 text-white" />
                    </div>
                    <h3 className="font-semibold text-gray-700 text-lg mb-2">Set Goals</h3>
                    <p className="text-sm text-gray-600">Define your fitness targets</p>
                </button>
            </div>
        </div>

        {/* Getting Started Guide */}
        <div className="bg-white rounded-xl shadow-lg p-6 animate-slide-up-delay">
            <div className="flex items-center mb-6">
                <div className="w-10 h-10 bg-green-600 rounded-full flex items-center justify-center mr-3">
                    <CheckCircleIcon className="w-6 h-6 text-white" />
                </div>
                <h3 className="text-2xl font-bold text-gray-900">Getting Started Guide</h3>
            </div>

            <div className="space-y-4">
                <div className="flex items-center p-4 bg-blue-50 rounded-lg">
                    <div className="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-bold mr-4 flex-shrink-0">
                        1
                    </div>
                    <div className="flex-1">
                        <p className="font-medium text-gray-900">Log your first workout to start tracking progress</p>
                        <p className="text-sm text-gray-600 mt-1">
                            Click "Log First Workout" to add exercises to your calendar and begin your fitness journey.
                        </p>
                        <button
                            onClick={onLogFirstWorkout}
                            className="mt-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                        >
                            Start Now
                        </button>
                    </div>
                </div>

                <div className="flex items-center p-4 bg-gray-50 rounded-lg opacity-60">
                    <div className="w-8 h-8 bg-gray-400 text-white rounded-full flex items-center justify-center text-sm font-bold mr-4 flex-shrink-0">
                        2
                    </div>
                    <div>
                        <p className="text-gray-500">Complete 3 workouts to unlock analytics</p>
                        <p className="text-sm text-gray-400 mt-1">
                            After 3 completed workouts, you'll gain access to progress charts and insights.
                        </p>
                    </div>
                </div>

                <div className="flex items-center p-4 bg-gray-50 rounded-lg opacity-60">
                    <div className="w-8 h-8 bg-gray-400 text-white rounded-full flex items-center justify-center text-sm font-bold mr-4 flex-shrink-0">
                        3
                    </div>
                    <div>
                        <p className="text-gray-500">Build a 7-day streak for bonus features</p>
                        <p className="text-sm text-gray-400 mt-1">
                            Maintain consistency for a week to unlock achievement badges and streak rewards.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
);

// Component for returning users - Tailwind version
interface ReturningUserDashboardProps {
    stats: DashboardStats;
    onLogWorkout: () => void;
    onViewProgress: () => void;
    onBrowseExercises: () => void;
}

const ReturningUserDashboard: React.FC<ReturningUserDashboardProps> = ({
                                                                           stats,
                                                                           onLogWorkout,
                                                                           onViewProgress,
                                                                           onBrowseExercises
                                                                       }) => (
    <div className="space-y-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 animate-fade-in">
            <div className="bg-white rounded-xl shadow-lg p-4 text-center hover:shadow-xl transition-shadow">
                <div className="w-12 h-12 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-3">
                    🏋️‍♂️
                </div>
                <div className="text-2xl font-bold text-blue-600 mb-1">
                    {stats.totalWorkouts}
                </div>
                <div className="text-sm text-gray-600">Total Workouts</div>
            </div>

            <div className="bg-white rounded-xl shadow-lg p-4 text-center hover:shadow-xl transition-shadow">
                <div className="w-12 h-12 bg-green-600 rounded-full flex items-center justify-center mx-auto mb-3">
                    🔥
                </div>
                <div className="text-2xl font-bold text-green-600 mb-1">
                    {stats.currentStreak}
                </div>
                <div className="text-sm text-gray-600">Day Streak</div>
            </div>

            <div className="bg-white rounded-xl shadow-lg p-4 text-center hover:shadow-xl transition-shadow">
                <div className="w-12 h-12 bg-purple-600 rounded-full flex items-center justify-center mx-auto mb-3">
                    💪
                </div>
                <div className="text-2xl font-bold text-purple-600 mb-1">
                    {stats.totalVolume.toLocaleString()}
                </div>
                <div className="text-sm text-gray-600">Total Volume (lbs)</div>
            </div>

            <div className="bg-white rounded-xl shadow-lg p-4 text-center hover:shadow-xl transition-shadow">
                <div className="w-12 h-12 bg-orange-600 rounded-full flex items-center justify-center mx-auto mb-3">
                    🏆
                </div>
                <div className="text-2xl font-bold text-orange-600 mb-1">
                    {stats.recentPRs}
                </div>
                <div className="text-sm text-gray-600">Recent PRs</div>
            </div>
        </div>

        {/* Quick Actions */}
        <div className="bg-white rounded-xl shadow-lg p-6 animate-slide-up">
            <div className="flex items-center mb-6">
                <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center mr-3">
                    <PlayIcon className="w-6 h-6 text-white" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900">Quick Actions</h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <button
                    onClick={onLogWorkout}
                    className="group p-6 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all duration-300 hover:scale-105 hover:shadow-lg"
                >
                    <div className="text-3xl mb-3 group-hover:scale-110 transition-transform">🏋️‍♂️</div>
                    <h3 className="font-semibold text-lg">Log Workout</h3>
                </button>

                <button
                    onClick={onViewProgress}
                    className="group p-6 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-all duration-300 hover:scale-105 hover:shadow-lg"
                >
                    <div className="text-3xl mb-3 group-hover:scale-110 transition-transform">📊</div>
                    <h3 className="font-semibold text-lg">View Progress</h3>
                </button>

                <button
                    onClick={onBrowseExercises}
                    className="group p-6 bg-purple-600 text-white rounded-xl hover:bg-purple-700 transition-all duration-300 hover:scale-105 hover:shadow-lg"
                >
                    <div className="text-3xl mb-3 group-hover:scale-110 transition-transform">📚</div>
                    <h3 className="font-semibold text-lg">Browse Exercises</h3>
                </button>
            </div>
        </div>
    </div>
);

export default WelcomePage;