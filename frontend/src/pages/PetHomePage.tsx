import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../contexts/AuthContext';
import {usePet} from '../contexts/PetContext';
import {PetRoom} from '../components/PetPage';
import CompactStats from '../components/PetPage/CompactStats';
import TodaysActivity from '../components/PetPage/TodaysActivity';
import PetActionsCard from '../components/PetPage/PetActionsCard';
import QuickActionsCard from '../components/PetPage/QuickActionsCard';
import GuidedTour from '../components/tutorial/GuidedTour';
import PostTutorialPrompt from '../components/tutorial/PostTutorialPrompt';
import SkipTutorialModal from '../components/tutorial/SkipTutorialModal';
import {MealType} from '../types/pet';
import petApi, {WeeklyStatsResponse} from '../services/petApi';
import {PET_TUTORIAL_STEPS} from '../config/tutorialSteps';
import userApi from '../services/userApi';

const PetHomePage: React.FC = () => {
    const navigate = useNavigate();
    const {user, refreshUser} = useAuth();
    const {
        stats,
        loading,
        error,
        actionLoading,
        lastAction,
        refreshStats,
        feedPet,
        motivatePet,
        bathePet,
        clearLastAction,
    } = usePet();

    // Weekly stats state
    const [weeklyStats, setWeeklyStats] = useState<WeeklyStatsResponse | null>(null);
    const [weeklyStatsLoading, setWeeklyStatsLoading] = useState(true);

    // Tutorial state
    const [showTutorial, setShowTutorial] = useState(false);
    const [showPostTutorialPrompt, setShowPostTutorialPrompt] = useState(false);
    const [showSkipModal, setShowSkipModal] = useState(false);

    // Fetch weekly stats on mount
    useEffect(() => {
        const fetchWeeklyStats = async () => {
            try {
                const data = await petApi.getWeeklyStats();
                setWeeklyStats(data);
            } catch (err) {
                console.error('Failed to fetch weekly stats:', err);
            } finally {
                setWeeklyStatsLoading(false);
            }
        };

        fetchWeeklyStats();
    }, []);

    // Tutorial trigger effect
    useEffect(() => {
        // Show tutorial ONLY if user hasn't completed it AND all data is loaded
        if (user && !user.petTutorialCompleted && stats && !loading && !weeklyStatsLoading && weeklyStats) {
            // Small delay for smooth UX
            const timer = setTimeout(() => {
                setShowTutorial(true);
            }, 1000);
            return () => clearTimeout(timer);
        }
    }, [user, stats, loading, weeklyStatsLoading, weeklyStats]);

    // Clear action message after 3 seconds
    useEffect(() => {
        if (lastAction) {
            const timer = setTimeout(clearLastAction, 3000);
            return () => clearTimeout(timer);
        }
    }, [lastAction, clearLastAction]);

    // Tutorial handlers
    const handleTutorialComplete = async () => {
        setShowTutorial(false);
        try {
            await userApi.completePetTutorial();
            await refreshUser();
            // Show post-tutorial prompt
            setShowPostTutorialPrompt(true);
        } catch (error) {
            console.error('Failed to mark pet tutorial as complete:', error);
        }
    };

    const handleTutorialSkip = () => {
        setShowSkipModal(true);
    };

    const handleConfirmSkip = async () => {
        setShowSkipModal(false);
        setShowTutorial(false);
        try {
            await userApi.completePetTutorial();
            await refreshUser();
        } catch (error) {
            console.error('Failed to skip tutorial:', error);
        }
    };

    const handlePlanWorkout = () => {
        setShowPostTutorialPrompt(false);
        navigate('/calendar');
    };

    const handleDismissPrompt = () => {
        setShowPostTutorialPrompt(false);
    };

    // Action handlers
    const handleFeed = async (mealType: MealType) => {
        try {
            await feedPet(mealType);
        } catch (err) {
            console.error('Feed failed:', err);
        }
    };

    const handleMotivate = async () => {
        try {
            await motivatePet();
        } catch (err) {
            console.error('Motivate failed:', err);
        }
    };

    const handleBathe = async () => {
        try {
            await bathePet();
        } catch (err) {
            console.error('Bathe failed:', err);
        }
    };

    // Loading state
    if (loading) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 flex items-center justify-center">
                <div className="text-center">
                    <div
                        className="w-16 h-16 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"/>
                    <p className="text-purple-700 font-medium">Loading your pet...</p>
                </div>
            </div>
        );
    }

    // Error state
    if (error && !stats) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-2xl shadow-xl p-6 max-w-md w-full text-center">
                    <div className="text-4xl mb-4">😢</div>
                    <h2 className="text-xl font-bold text-gray-900 mb-2">Couldn't Load Pet</h2>
                    <p className="text-gray-600 mb-4">{error}</p>
                    <button
                        onClick={refreshStats}
                        className="px-6 py-2 bg-purple-500 text-white rounded-lg font-medium hover:bg-purple-600 transition-colors"
                    >
                        Try Again
                    </button>
                </div>
            </div>
        );
    }

    // No stats
    if (!stats) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-2xl shadow-xl p-6 max-w-md w-full text-center">
                    <div className="text-4xl mb-4">🐺</div>
                    <h2 className="text-xl font-bold text-gray-900 mb-2">No Pet Found</h2>
                    <p className="text-gray-600 mb-4">Complete onboarding to get your pet!</p>
                    <button
                        onClick={() => navigate('/onboarding/nickname')}
                        className="px-6 py-2 bg-purple-500 text-white rounded-lg font-medium hover:bg-purple-600 transition-colors"
                    >
                        Start Onboarding
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 min-h-screen pb-24">
            {/* Main Content */}
            <div className="max-w-lg mx-auto px-4 py-4 space-y-4">
                {/* Action Feedback Toast */}
                {lastAction && (
                    <div className="fixed top-20 left-1/2 -translate-x-1/2 z-50 animate-bounce">
                        <div className="bg-green-500 text-white px-6 py-3 rounded-full shadow-lg font-medium text-sm">
                            {lastAction}
                        </div>
                    </div>
                )}

                {/* Error Toast */}
                {error && (
                    <div className="bg-red-50 border-2 border-red-300 rounded-xl p-3 text-red-700 text-sm shadow-lg">
                        {error}
                    </div>
                )}

                {/* ✨ NEW LOGICAL ORDER ✨ */}

                {/* 1. Pet Room */}
                <div className="pet-room-container relative">
                    <PetRoom/>
                </div>

                {/* 2. Compact Stats - RIGHT AFTER PET! */}
                <div className="compact-stats-container">
                    <CompactStats stats={stats}/>
                </div>

                {/* 3. Pet Actions - Always visible! */}
                <div className="pet-actions-card">
                    <PetActionsCard
                        stats={stats}
                        actionLoading={actionLoading}
                        onFeed={handleFeed}
                        onMotivate={handleMotivate}
                        onBathe={handleBathe}
                    />
                </div>

                {/* 4. Today's Activity */}
                {weeklyStatsLoading ? (
                    <div
                        className="bg-gradient-to-br from-blue-50 to-cyan-50 rounded-2xl p-4 shadow-lg border border-blue-200">
                        <div className="flex items-center justify-center py-8">
                            <div
                                className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"/>
                        </div>
                    </div>
                ) : weeklyStats ? (
                    <div className="todays-activity-card">
                        <TodaysActivity
                            workoutsThisWeek={weeklyStats.workoutsThisWeek}
                            workoutsGoal={weeklyStats.weeklyGoal}
                            currentStreak={weeklyStats.currentStreak}
                            xpThisWeek={weeklyStats.xpThisWeek}
                            goalProgress={weeklyStats.goalProgress}
                            goalAchieved={weeklyStats.goalAchieved}
                        />
                    </div>
                ) : null}

                {/* 5. Quick Actions Card */}
                <QuickActionsCard
                    onPlanWorkout={() => navigate('/calendar')}
                    workoutsThisWeek={weeklyStats?.workoutsThisWeek}
                />
            </div>

            {/* Guided Tour */}
            {showTutorial && (
                <GuidedTour
                    steps={PET_TUTORIAL_STEPS}
                    onComplete={handleTutorialComplete}
                    onSkip={handleTutorialSkip}
                    petName={stats.petName || user?.petName || 'your pet'}
                />
            )}

            {/* Post-Tutorial Prompt */}
            {showPostTutorialPrompt && (
                <PostTutorialPrompt
                    onPlanWorkout={handlePlanWorkout}
                    onDismiss={handleDismissPrompt}
                    petName={stats.petName || user?.petName || 'your pet'}
                />
            )}

            {/* Skip Tutorial Modal */}
            <SkipTutorialModal
                isOpen={showSkipModal}
                onConfirm={handleConfirmSkip}
                onCancel={() => setShowSkipModal(false)}
                tutorialName="Pet"
            />
        </div>
    );
};

export default PetHomePage;