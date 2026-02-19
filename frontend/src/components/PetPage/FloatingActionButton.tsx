import React, {useState} from 'react';
import {Sparkles, X} from 'lucide-react';
import {PetStats, MealType, MEAL_COSTS} from '../../types/pet';

interface FloatingActionButtonProps {
    stats: PetStats;
    actionLoading: boolean;
    onFeed: (mealType: MealType) => Promise<void>;
    onMotivate: () => Promise<void>;
    onBathe: () => Promise<void>;
}

const FloatingActionButton: React.FC<FloatingActionButtonProps> = ({
                                                                       stats,
                                                                       actionLoading,
                                                                       onFeed,
                                                                       onMotivate,
                                                                       onBathe,
                                                                   }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [showFeedMenu, setShowFeedMenu] = useState(false);

    // Check if actions are available
    const canFeed = (mealType: MealType) => stats.crystals >= MEAL_COSTS[mealType].crystals && stats.fuel < 100;
    const canMotivate = stats.motivation < 100;
    const canBathe = stats.motivation >= 40 && stats.cleanliness < 100;

    const handleFeedClick = () => {
        setShowFeedMenu(true);
        setIsOpen(false);
    };

    const handleFeed = async (mealType: MealType) => {
        setShowFeedMenu(false);
        await onFeed(mealType);
    };

    const handleMotivate = async () => {
        setIsOpen(false);
        await onMotivate();
    };

    const handleBathe = async () => {
        setIsOpen(false);
        await onBathe();
    };

    return (
        <>
            {/* Overlay when menu is open */}
            {(isOpen || showFeedMenu) && (
                <div
                    className="fixed inset-0 bg-black/20 backdrop-blur-sm z-40"
                    onClick={() => {
                        setIsOpen(false);
                        setShowFeedMenu(false);
                    }}
                />
            )}

            {/* Action Menu */}
            {isOpen && (
                <div className="fixed bottom-[88px] right-4 z-50 space-y-3 animate-slideUp">
                    {/* Feed */}
                    <button
                        onClick={handleFeedClick}
                        disabled={actionLoading || stats.crystals < 1}
                        className={`flex items-center gap-3 px-4 py-3 rounded-full shadow-lg transition-all ${
                            stats.crystals >= 1
                                ? 'bg-gradient-to-r from-amber-500 to-orange-500 text-white hover:scale-105'
                                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                        }`}
                    >
                        <span className="text-xl">🍖</span>
                        <span className="font-semibold text-sm">Feed</span>
                    </button>

                    {/* Motivate */}
                    <button
                        onClick={handleMotivate}
                        disabled={actionLoading || !canMotivate}
                        className={`flex items-center gap-3 px-4 py-3 rounded-full shadow-lg transition-all ${
                            canMotivate
                                ? 'bg-gradient-to-r from-pink-500 to-rose-500 text-white hover:scale-105'
                                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                        }`}
                    >
                        <span className="text-xl">💪</span>
                        <span className="font-semibold text-sm">Motivate</span>
                    </button>

                    {/* Bathe */}
                    <button
                        onClick={handleBathe}
                        disabled={actionLoading || !canBathe}
                        className={`flex items-center gap-3 px-4 py-3 rounded-full shadow-lg transition-all ${
                            canBathe
                                ? 'bg-gradient-to-r from-cyan-500 to-teal-500 text-white hover:scale-105'
                                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                        }`}
                    >
                        <span className="text-xl">🛁</span>
                        <span className="font-semibold text-sm">Bathe</span>
                    </button>
                </div>
            )}

            {/* Feed Menu */}
            {showFeedMenu && (
                <div
                    className="fixed bottom-[88px] right-4 bg-white rounded-2xl shadow-2xl p-3 z-50 min-w-[200px] animate-slideUp">
                    <div className="space-y-2">
                        {/* Snack */}
                        <button
                            onClick={() => handleFeed('SNACK')}
                            disabled={!canFeed('SNACK') || actionLoading}
                            className={`w-full p-3 rounded-xl text-left transition-colors ${
                                canFeed('SNACK')
                                    ? 'hover:bg-amber-50 active:bg-amber-100'
                                    : 'opacity-50 cursor-not-allowed'
                            }`}
                        >
                            <div className="flex justify-between items-center mb-1">
                                <span className="font-semibold">🥜 Snack</span>
                                <span className="text-xs text-gray-500">+15 fuel</span>
                            </div>
                            <div className="text-xs text-amber-600 font-medium">💎 1 crystal</div>
                        </button>

                        {/* Meal */}
                        <button
                            onClick={() => handleFeed('MEAL')}
                            disabled={!canFeed('MEAL') || actionLoading}
                            className={`w-full p-3 rounded-xl text-left transition-colors ${
                                canFeed('MEAL')
                                    ? 'hover:bg-amber-50 active:bg-amber-100'
                                    : 'opacity-50 cursor-not-allowed'
                            }`}
                        >
                            <div className="flex justify-between items-center mb-1">
                                <span className="font-semibold">🍖 Meal</span>
                                <span className="text-xs text-gray-500">+40 fuel</span>
                            </div>
                            <div className="text-xs text-amber-600 font-medium">💎 3 crystals</div>
                        </button>

                        {/* Feast */}
                        <button
                            onClick={() => handleFeed('FEAST')}
                            disabled={!canFeed('FEAST') || actionLoading}
                            className={`w-full p-3 rounded-xl text-left transition-colors ${
                                canFeed('FEAST')
                                    ? 'hover:bg-amber-50 active:bg-amber-100'
                                    : 'opacity-50 cursor-not-allowed'
                            }`}
                        >
                            <div className="flex justify-between items-center mb-1">
                                <span className="font-semibold">🍗 Feast</span>
                                <span className="text-xs text-gray-500">+80 fuel</span>
                            </div>
                            <div className="text-xs text-amber-600 font-medium">💎 6 crystals</div>
                        </button>
                    </div>
                </div>
            )}

            {/* Main FAB Button */}
            <button
                onClick={() => setIsOpen(!isOpen)}
                disabled={actionLoading}
                className={`fixed bottom-20 right-4 w-14 h-14 rounded-full shadow-2xl z-50 flex items-center justify-center transition-all duration-300 ${
                    isOpen
                        ? 'bg-gray-500 rotate-45'
                        : 'bg-gradient-to-br from-purple-500 to-pink-500 hover:scale-110'
                } ${actionLoading ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
                {isOpen ? (
                    <X className="w-7 h-7 text-white"/>
                ) : (
                    <Sparkles className="w-7 h-7 text-white"/>
                )}
            </button>

            <style dangerouslySetInnerHTML={{
                __html: `
                    @keyframes slideUp {
                        from {
                            opacity: 0;
                            transform: translateY(20px);
                        }
                        to {
                            opacity: 1;
                            transform: translateY(0);
                        }
                    }
                    .animate-slideUp {
                        animation: slideUp 0.3s ease-out;
                    }
                `
            }}/>
        </>
    );
};

export default FloatingActionButton;