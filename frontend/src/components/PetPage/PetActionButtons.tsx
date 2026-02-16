import React, {useState} from 'react';
import {PetStats, MealType, MEAL_COSTS} from '../../types/pet';

interface PetActionButtonsProps {
    stats: PetStats;
    actionLoading: boolean;
    onFeed: (mealType: MealType) => Promise<void>;
    onMotivate: () => Promise<void>;
    onBathe: () => Promise<void>;
    onSleep: () => Promise<void>;
    onWake: () => Promise<void>;
}

const PetActionButtons: React.FC<PetActionButtonsProps> = ({
                                                               stats,
                                                               actionLoading,
                                                               onFeed,
                                                               onMotivate,
                                                               onBathe,
                                                               onSleep,
                                                               onWake,
                                                           }) => {
    const [showFeedMenu, setShowFeedMenu] = useState(false);

    // Check if actions are available
    const canFeed = (mealType: MealType) => stats.crystals >= MEAL_COSTS[mealType].crystals && stats.fuel < 100;
    const canMotivate = stats.motivation < 100; // Cooldown handled by backend
    const canBathe = stats.motivation >= 40 && stats.cleanliness < 100;
    const canSleep = !stats.isSleeping; // Can only sleep if not already sleeping
    const canWake = stats.isSleeping; // Can only wake if currently sleeping

    const handleFeed = async (mealType: MealType) => {
        setShowFeedMenu(false);
        await onFeed(mealType);
    };

    return (
        <div className="bg-white/90 backdrop-blur-sm rounded-2xl p-4 shadow-lg">
            <div className="grid grid-cols-2 gap-3">
                {/* Feed Button */}
                <div className="relative">
                    <button
                        onClick={() => setShowFeedMenu(!showFeedMenu)}
                        disabled={actionLoading || stats.crystals < 1}
                        className={`w-full py-3 px-4 rounded-xl font-semibold text-sm transition-all duration-200 flex items-center justify-center gap-2 ${
                            stats.crystals >= 1
                                ? 'bg-gradient-to-r from-amber-500 to-orange-500 text-white hover:from-amber-600 hover:to-orange-600 active:scale-95'
                                : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        }`}
                    >
                        <span className="text-lg">🍖</span>
                        <span>Feed</span>
                    </button>

                    {/* Feed Menu Dropdown */}
                    {showFeedMenu && (
                        <>
                            <div
                                className="fixed inset-0 z-10"
                                onClick={() => setShowFeedMenu(false)}
                            />
                            <div
                                className="absolute bottom-full left-0 right-0 mb-2 bg-white rounded-xl shadow-xl border border-gray-200 overflow-hidden z-20">
                                <div className="p-2 space-y-1">
                                    {/* Snack */}
                                    <button
                                        onClick={() => handleFeed('SNACK')}
                                        disabled={!canFeed('SNACK') || actionLoading}
                                        className={`w-full p-2 rounded-lg text-left transition-colors ${
                                            canFeed('SNACK')
                                                ? 'hover:bg-amber-50 active:bg-amber-100'
                                                : 'opacity-50 cursor-not-allowed'
                                        }`}
                                    >
                                        <div className="flex justify-between items-center">
                                            <span className="font-medium">🥜 Snack</span>
                                            <span className="text-xs text-gray-500">+15 fuel</span>
                                        </div>
                                        <div className="text-xs text-amber-600">💎 1 crystal</div>
                                    </button>

                                    {/* Meal */}
                                    <button
                                        onClick={() => handleFeed('MEAL')}
                                        disabled={!canFeed('MEAL') || actionLoading}
                                        className={`w-full p-2 rounded-lg text-left transition-colors ${
                                            canFeed('MEAL')
                                                ? 'hover:bg-amber-50 active:bg-amber-100'
                                                : 'opacity-50 cursor-not-allowed'
                                        }`}
                                    >
                                        <div className="flex justify-between items-center">
                                            <span className="font-medium">🍖 Meal</span>
                                            <span className="text-xs text-gray-500">+40 fuel</span>
                                        </div>
                                        <div className="text-xs text-amber-600">💎 3 crystals</div>
                                    </button>

                                    {/* Feast */}
                                    <button
                                        onClick={() => handleFeed('FEAST')}
                                        disabled={!canFeed('FEAST') || actionLoading}
                                        className={`w-full p-2 rounded-lg text-left transition-colors ${
                                            canFeed('FEAST')
                                                ? 'hover:bg-amber-50 active:bg-amber-100'
                                                : 'opacity-50 cursor-not-allowed'
                                        }`}
                                    >
                                        <div className="flex justify-between items-center">
                                            <span className="font-medium">🍗 Feast</span>
                                            <span className="text-xs text-gray-500">+80 fuel</span>
                                        </div>
                                        <div className="text-xs text-amber-600">💎 6 crystals</div>
                                    </button>
                                </div>
                            </div>
                        </>
                    )}
                </div>

                {/* Motivate Button */}
                <button
                    onClick={onMotivate}
                    disabled={actionLoading || !canMotivate}
                    className={`py-3 px-4 rounded-xl font-semibold text-sm transition-all duration-200 flex items-center justify-center gap-2 ${
                        canMotivate
                            ? 'bg-gradient-to-r from-pink-500 to-rose-500 text-white hover:from-pink-600 hover:to-rose-600 active:scale-95'
                            : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                    }`}
                >
                    <span className="text-lg">💪</span>
                    <span>Motivate</span>
                </button>

                {/* Bathe Button */}
                <button
                    onClick={onBathe}
                    disabled={actionLoading || !canBathe}
                    className={`py-3 px-4 rounded-xl font-semibold text-sm transition-all duration-200 flex items-center justify-center gap-2 ${
                        canBathe
                            ? 'bg-gradient-to-r from-cyan-500 to-teal-500 text-white hover:from-cyan-600 hover:to-teal-600 active:scale-95'
                            : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                    }`}
                >
                    <span className="text-lg">🛁</span>
                    <span>Bathe</span>
                </button>

                {/* Sleep/Wake Button (conditional) */}
                {stats.isSleeping ? (
                    // Wake Button (only when sleeping)
                    <button
                        onClick={onWake}
                        disabled={actionLoading}
                        className="py-3 px-4 rounded-xl font-semibold text-sm transition-all duration-200 flex items-center justify-center gap-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white hover:from-purple-600 hover:to-pink-600 active:scale-95"
                    >
                        <span className="text-lg">⏰</span>
                        <span>Wake Up</span>
                    </button>
                ) : (
                    // Sleep Button (only when awake)
                    <button
                        onClick={onSleep}
                        disabled={actionLoading}
                        className="py-3 px-4 rounded-xl font-semibold text-sm transition-all duration-200 flex items-center justify-center gap-2 bg-gradient-to-r from-blue-500 to-indigo-500 text-white hover:from-blue-600 hover:to-indigo-600 active:scale-95"
                    >
                        <span className="text-lg">😴</span>
                        <span>Sleep</span>
                    </button>
                )}
            </div>

            {/* Bathe Requirement Note */}
            {!canBathe && stats.motivation < 40 && (
                <p className="text-xs text-gray-500 text-center mt-2">
                    🛁 Bathing requires at least 40% motivation
                </p>
            )}
        </div>
    );
};

export default PetActionButtons;