import React, {useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../contexts/AuthContext';
import {usePet} from '../contexts/PetContext';
import {PetRoom, PetStatBars, PetActionButtons} from '../components/PetPage';
import {MealType} from '../types/pet';

const PetHomePage: React.FC = () => {
    const navigate = useNavigate();
    const {user} = useAuth();
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
        sleepPet,
        wakePet,
        clearLastAction,
    } = usePet();

    // Clear action message after 3 seconds
    useEffect(() => {
        if (lastAction) {
            const timer = setTimeout(clearLastAction, 3000);
            return () => clearTimeout(timer);
        }
    }, [lastAction, clearLastAction]);

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

    const handleSleep = async () => {
        try {
            await sleepPet();
        } catch (err) {
            console.error('Sleep failed:', err);
        }
    };

    const handleWake = async () => {
        try {
            await wakePet();
        } catch (err) {
            console.error('Wake failed:', err);
        }
    };

    // Loading state
    if (loading) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-amber-50 via-orange-50 to-yellow-50 flex items-center justify-center">
                <div className="text-center">
                    <div
                        className="w-16 h-16 border-4 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"/>
                    <p className="text-amber-700 font-medium">Loading your pet...</p>
                </div>
            </div>
        );
    }

    // Error state
    if (error && !stats) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-amber-50 via-orange-50 to-yellow-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-2xl shadow-xl p-6 max-w-md w-full text-center">
                    <div className="text-4xl mb-4">😢</div>
                    <h2 className="text-xl font-bold text-gray-900 mb-2">Couldn't Load Pet</h2>
                    <p className="text-gray-600 mb-4">{error}</p>
                    <button
                        onClick={refreshStats}
                        className="px-6 py-2 bg-amber-500 text-white rounded-lg font-medium hover:bg-amber-600 transition-colors"
                    >
                        Try Again
                    </button>
                </div>
            </div>
        );
    }

    // No stats (shouldn't happen if onboarding is complete)
    if (!stats) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-amber-50 via-orange-50 to-yellow-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-2xl shadow-xl p-6 max-w-md w-full text-center">
                    <div className="text-4xl mb-4">🐺</div>
                    <h2 className="text-xl font-bold text-gray-900 mb-2">No Pet Found</h2>
                    <p className="text-gray-600 mb-4">Complete onboarding to get your pet!</p>
                    <button
                        onClick={() => navigate('/onboarding/nickname')}
                        className="px-6 py-2 bg-amber-500 text-white rounded-lg font-medium hover:bg-amber-600 transition-colors"
                    >
                        Start Onboarding
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-gradient-to-br from-amber-50 via-orange-50 to-yellow-50 min-h-full">
            {/* Header */}
            <div className="bg-white/80 backdrop-blur-md border-b border-amber-200">
                <div className="max-w-lg mx-auto px-4 py-3 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <span className="text-2xl">🐺</span>
                        <div>
                            <h1 className="font-bold text-gray-900">
                                {user?.petName || 'Your Pet'}
                            </h1>
                            <p className="text-xs text-gray-500">
                                {user?.nickname ? `@${user.nickname}` : user?.firstName}'s companion
                            </p>
                        </div>
                    </div>

                    {/* Crystal Counter */}
                    <div
                        className="flex items-center gap-2 bg-gradient-to-r from-purple-100 to-pink-100 px-3 py-1.5 rounded-full">
                        <span className="text-lg">💎</span>
                        <span className="font-bold text-purple-700">{stats.crystals ?? 0}</span>
                        <span className="text-xs text-purple-500">/ {stats.maxCrystals ?? 100}</span>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-lg mx-auto px-4 py-4 space-y-4">
                {/* Action Feedback Toast */}
                {lastAction && (
                    <div className="fixed top-20 left-1/2 -translate-x-1/2 z-50 animate-bounce">
                        <div className="bg-green-500 text-white px-4 py-2 rounded-full shadow-lg font-medium text-sm">
                            {lastAction}
                        </div>
                    </div>
                )}

                {/* Error Toast */}
                {error && (
                    <div className="bg-red-50 border border-red-200 rounded-xl p-3 text-red-700 text-sm">
                        {error}
                    </div>
                )}

                {/* Pet Room */}
                <PetRoom/>

                {/* Stats */}
                <PetStatBars stats={stats}/>

                {/* Actions */}
                <PetActionButtons
                    stats={stats}
                    actionLoading={actionLoading}
                    onFeed={handleFeed}
                    onMotivate={handleMotivate}
                    onBathe={handleBathe}
                    onSleep={handleSleep}
                    onWake={handleWake}
                />

                {/* Quick Tips */}
                <div className="bg-white/70 backdrop-blur-sm rounded-xl p-4 text-sm text-gray-600">
                    <p className="font-medium text-gray-800 mb-2">💡 Tips:</p>
                    <ul className="space-y-1 text-xs">
                        <li>• Complete workouts to earn 💎 crystals</li>
                        <li>• Feed your pet to keep fuel high</li>
                        <li>• Motivate daily for best results (30min cooldown)</li>
                        <li>• Bathe requires 40%+ motivation</li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default PetHomePage;