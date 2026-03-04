import React, {useEffect, useState} from 'react';
import {useNavigate, useLocation} from 'react-router-dom';
import {WorkoutCompletionResponse} from '../types/workoutCompletionResponse';
import {Trophy, Zap, Target, Clock, Dumbbell, TrendingUp, Flame, Award, ChevronRight} from 'lucide-react';
import {usePet} from '../contexts/PetContext';

interface WorkoutStats {
    durationMinutes: number;
    setsCompleted: number;
    exerciseCount: number;
    volumeLifted?: number;
    distanceKm?: number;
    holdSeconds?: number;
    workoutType: 'STRENGTH' | 'CARDIO' | 'ISOMETRIC';
}

const WorkoutCompletePage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const {stats: petStats} = usePet();
    const [showConfetti, setShowConfetti] = useState(true);

    // Get data from navigation state
    const progressResponse = location.state?.progressResponse as WorkoutCompletionResponse;
    const workoutStats = location.state?.workoutStats as WorkoutStats;

    useEffect(() => {
        // Hide confetti after 3 seconds
        const timer = setTimeout(() => setShowConfetti(false), 3000);
        return () => clearTimeout(timer);
    }, []);

    // If no data, redirect to home
    useEffect(() => {
        if (!progressResponse || !workoutStats) {
            navigate('/');
        }
    }, [progressResponse, workoutStats, navigate]);

    if (!progressResponse || !workoutStats) {
        return null;
    }

    const handleContinue = () => {
        navigate('/pet');
    };

    return (
        <div className="min-h-screen bg-gradient-to-b from-purple-50 to-blue-50 pb-20">
            {/* Confetti Effect */}
            {showConfetti && (
                <div className="fixed inset-0 pointer-events-none z-50">
                    <div className="absolute inset-0 overflow-hidden">
                        {[...Array(50)].map((_, i) => (
                            <div
                                key={i}
                                className="absolute animate-confetti"
                                style={{
                                    left: `${Math.random() * 100}%`,
                                    top: '-10%',
                                    animationDelay: `${Math.random() * 3}s`,
                                    animationDuration: `${3 + Math.random() * 2}s`
                                }}
                            >
                                {['🎉', '⭐', '💪', '🔥', '✨'][Math.floor(Math.random() * 5)]}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Header */}
            <div className="bg-gradient-to-r from-purple-600 to-blue-600 text-white p-6 text-center">
                <h1 className="text-3xl font-bold mb-2">🎉 Workout Complete!</h1>
                <p className="text-purple-100">Amazing effort! Here's your summary</p>
            </div>

            <div className="max-w-2xl mx-auto px-4 py-6 space-y-4">
                {/* Pet Reward Card */}
                {progressResponse.petUpdate && (
                    <div className="bg-white rounded-2xl shadow-lg p-6 border-2 border-purple-200">
                        <div className="text-center">
                            {/* Pet Image/Animation Placeholder */}
                            <div className="w-32 h-32 mx-auto mb-4 bg-gradient-to-br from-purple-100 to-blue-100 rounded-full flex items-center justify-center text-6xl">
                                🐺
                            </div>

                            <h2 className="text-2xl font-bold text-gray-900 mb-2">
                                {petStats?.petName || 'Your Pet'}
                            </h2>

                            {/* Crystals Earned - Big Highlight */}
                            <div className="bg-gradient-to-r from-amber-100 to-orange-100 rounded-2xl p-6 mb-4 border-2 border-amber-300">
                                <div className="flex items-center justify-center gap-3">
                                    <span className="text-5xl">💎</span>
                                    <div className="text-left">
                                        <div className="text-4xl font-bold text-amber-700">
                                            +{progressResponse.petUpdate.crystalsEarned}
                                        </div>
                                        <div className="text-sm text-amber-600">Crystals Earned!</div>
                                    </div>
                                </div>
                                <div className="text-xs text-amber-600 mt-2">
                                    Balance: {progressResponse.petUpdate.newCrystalBalance} 💎
                                </div>
                            </div>

                            {/* Pet Message */}
                            {progressResponse.petUpdate.message && (
                                <p className="text-gray-600 text-sm italic">
                                    "{progressResponse.petUpdate.message}"
                                </p>
                            )}

                            {/* Fatigue Warning */}
                            {progressResponse.petUpdate.isSleeping && (
                                <div className="mt-4 bg-red-50 border-2 border-red-200 rounded-xl p-4">
                                    <div className="flex items-center gap-2 text-red-700">
                                        <span className="text-2xl">💤</span>
                                        <div className="text-left">
                                            <div className="font-bold">Pet is Exhausted!</div>
                                            <div className="text-sm">Needs 24 hours of rest</div>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Wasted Crystals Warning */}
                            {progressResponse.petUpdate.wastedCrystals > 0 && (
                                <div className="mt-2 bg-yellow-50 border border-yellow-200 rounded-lg p-3">
                                    <div className="text-yellow-700 text-sm">
                                        ⚠️ {progressResponse.petUpdate.wastedCrystals} crystals wasted (cap reached)
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* XP & Rank Card */}
                <div className="bg-white rounded-2xl shadow-lg p-6">
                    <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                        <Zap className="w-5 h-5 text-yellow-500"/>
                        Progression Rewards
                    </h3>

                    {/* XP Gained */}
                    <div className="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4 mb-3">
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                                <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                                    <Zap className="w-6 h-6 text-white"/>
                                </div>
                                <div>
                                    <div className="text-2xl font-bold text-blue-600">
                                        +{progressResponse.xpGained} XP
                                    </div>
                                    <div className="text-xs text-gray-600">
                                        Total: {progressResponse.newLifetimeXp} XP
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Current Rank */}
                    <div className="bg-purple-50 rounded-xl p-4">
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                                <Trophy className="w-8 h-8 text-purple-600"/>
                                <div>
                                    <div className="text-sm text-gray-600">Current Rank</div>
                                    <div className="text-xl font-bold text-purple-600">
                                        {progressResponse.seasonalRank}
                                    </div>
                                </div>
                            </div>
                            {progressResponse.rankedUp && (
                                <div className="bg-yellow-100 text-yellow-700 px-3 py-1 rounded-full text-xs font-bold border-2 border-yellow-300">
                                    RANK UP! 🎉
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Streak Info */}
                    {progressResponse.currentStreak > 0 && (
                        <div className="mt-3 bg-orange-50 rounded-xl p-4">
                            <div className="flex items-center gap-3">
                                <Flame className="w-8 h-8 text-orange-500"/>
                                <div>
                                    <div className="text-sm text-gray-600">Workout Streak</div>
                                    <div className="text-xl font-bold text-orange-600">
                                        {progressResponse.currentStreak} {progressResponse.currentStreak === 1 ? 'day' : 'days'}
                                    </div>
                                    {progressResponse.streakMilestone && (
                                        <div className="text-xs text-orange-600 font-semibold">
                                            🔥 {progressResponse.streakMessage}
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* Achievements */}
                {progressResponse.achievementsUnlocked && progressResponse.achievementsUnlocked.length > 0 && (
                    <div className="bg-white rounded-2xl shadow-lg p-6">
                        <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                            <Award className="w-5 h-5 text-yellow-500"/>
                            Achievements Unlocked!
                        </h3>
                        <div className="space-y-3">
                            {progressResponse.achievementsUnlocked.map((achievement) => (
                                <div
                                    key={achievement.achievementId}
                                    className="bg-gradient-to-r from-yellow-50 to-amber-50 rounded-xl p-4 border-2 border-yellow-200"
                                >
                                    <div className="flex items-start gap-3">
                                        <div className="text-3xl">{achievement.icon}</div>
                                        <div className="flex-1">
                                            <div className="font-bold text-gray-900">{achievement.name}</div>
                                            <div className="text-sm text-gray-600">{achievement.description}</div>
                                            <div className="mt-1 flex items-center gap-2">
                                                <span className="text-xs bg-yellow-200 text-yellow-800 px-2 py-0.5 rounded-full font-semibold">
                                                    {achievement.rarity}
                                                </span>
                                                <span className="text-xs text-green-600 font-semibold">
                                                    +{achievement.bonusXp} XP
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Workout Stats */}
                <div className="bg-white rounded-2xl shadow-lg p-6">
                    <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                        <Target className="w-5 h-5 text-blue-500"/>
                        Workout Summary
                    </h3>

                    <div className="grid grid-cols-2 gap-3">
                        {/* Duration */}
                        <div className="bg-blue-50 rounded-xl p-4 text-center">
                            <Clock className="w-6 h-6 text-blue-600 mx-auto mb-2"/>
                            <div className="text-2xl font-bold text-blue-600">
                                {workoutStats.durationMinutes}
                            </div>
                            <div className="text-xs text-gray-600">Minutes</div>
                        </div>

                        {/* Sets */}
                        <div className="bg-green-50 rounded-xl p-4 text-center">
                            <Dumbbell className="w-6 h-6 text-green-600 mx-auto mb-2"/>
                            <div className="text-2xl font-bold text-green-600">
                                {workoutStats.setsCompleted}
                            </div>
                            <div className="text-xs text-gray-600">Sets</div>
                        </div>

                        {/* Exercises */}
                        <div className="bg-purple-50 rounded-xl p-4 text-center">
                            <TrendingUp className="w-6 h-6 text-purple-600 mx-auto mb-2"/>
                            <div className="text-2xl font-bold text-purple-600">
                                {workoutStats.exerciseCount}
                            </div>
                            <div className="text-xs text-gray-600">Exercises</div>
                        </div>

                        {/* Type-specific stat */}
                        {workoutStats.workoutType === 'STRENGTH' && workoutStats.volumeLifted && workoutStats.volumeLifted > 0 && (
                            <div className="bg-orange-50 rounded-xl p-4 text-center">
                                <span className="text-2xl mb-2 block">💪</span>
                                <div className="text-2xl font-bold text-orange-600">
                                    {Math.round(workoutStats.volumeLifted)}
                                </div>
                                <div className="text-xs text-gray-600">Volume (lbs)</div>
                            </div>
                        )}

                        {workoutStats.workoutType === 'CARDIO' && workoutStats.distanceKm && workoutStats.distanceKm > 0 && (
                            <div className="bg-red-50 rounded-xl p-4 text-center">
                                <span className="text-2xl mb-2 block">🏃</span>
                                <div className="text-2xl font-bold text-red-600">
                                    {workoutStats.distanceKm.toFixed(2)}
                                </div>
                                <div className="text-xs text-gray-600">Kilometers</div>
                            </div>
                        )}

                        {workoutStats.workoutType === 'ISOMETRIC' && workoutStats.holdSeconds && workoutStats.holdSeconds > 0 && (
                            <div className="bg-indigo-50 rounded-xl p-4 text-center">
                                <span className="text-2xl mb-2 block">⏱️</span>
                                <div className="text-2xl font-bold text-indigo-600">
                                    {workoutStats.holdSeconds}
                                </div>
                                <div className="text-xs text-gray-600">Hold Time (s)</div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Continue Button */}
                <button
                    onClick={handleContinue}
                    className="w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-4 rounded-2xl shadow-lg transition-all transform hover:scale-105 flex items-center justify-center gap-2"
                >
                    Continue to Home
                    <ChevronRight className="w-5 h-5"/>
                </button>
            </div>

            {/* Confetti Animation CSS */}
            <style>{`
                @keyframes confetti {
                    0% {
                        transform: translateY(0) rotate(0deg);
                        opacity: 1;
                    }
                    100% {
                        transform: translateY(100vh) rotate(720deg);
                        opacity: 0;
                    }
                }
                .animate-confetti {
                    animation: confetti linear forwards;
                }
            `}</style>
        </div>
    );
};

export default WorkoutCompletePage;