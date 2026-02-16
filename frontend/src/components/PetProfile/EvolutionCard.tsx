import React from 'react';
import {Sparkles, TrendingUp, Lock} from 'lucide-react';

interface EvolutionCardProps {
    currentStage: string;
    currentStageDisplay: string;
    nextStage: string | null;
    nextStageDisplay: string | null;
    currentLevel: number;
    levelRequired: number | null;
    levelsRemaining: number | null;
    canEvolve: boolean;
    message: string;
    onEvolveClick?: () => void;
    className?: string;
}

const EvolutionCard: React.FC<EvolutionCardProps> = ({
                                                         currentStage,
                                                         currentStageDisplay,
                                                         nextStage,
                                                         nextStageDisplay,
                                                         currentLevel,
                                                         levelRequired,
                                                         levelsRemaining,
                                                         canEvolve,
                                                         message,
                                                         onEvolveClick,
                                                         className = '',
                                                     }) => {
    return (
        <div
            className={`bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 rounded-2xl p-6 shadow-lg border-2 border-purple-200 ${className}`}>
            {/* Header */}
            <div className="flex items-center gap-2 mb-4">
                <Sparkles className="w-6 h-6 text-purple-600"/>
                <h3 className="text-xl font-bold text-purple-900">Evolution Status</h3>
            </div>

            {/* Current Stage */}
            <div className="bg-white/80 backdrop-blur-sm rounded-xl p-4 mb-4">
                <div className="flex items-center justify-between">
                    <div>
                        <p className="text-sm text-gray-600 mb-1">Current Stage</p>
                        <p className="text-2xl font-bold text-purple-700">{currentStageDisplay}</p>
                        <p className="text-xs text-gray-500 mt-1">Level {currentLevel}</p>
                    </div>
                    <div className="text-4xl">🐺</div>
                </div>
            </div>

            {/* Evolution Path */}
            {nextStage && (
                <>
                    {/* Arrow */}
                    <div className="flex justify-center my-3">
                        <TrendingUp className="w-6 h-6 text-purple-400"/>
                    </div>

                    {/* Next Stage */}
                    <div className={`rounded-xl p-4 mb-4 border-2 ${
                        canEvolve
                            ? 'bg-gradient-to-r from-green-50 to-emerald-50 border-green-300'
                            : 'bg-gray-50 border-gray-300'
                    }`}>
                        <div className="flex items-center justify-between">
                            <div>
                                <p className="text-sm text-gray-600 mb-1">Next Stage</p>
                                <p className={`text-xl font-bold ${canEvolve ? 'text-green-700' : 'text-gray-600'}`}>
                                    {nextStageDisplay}
                                </p>
                                {levelRequired && (
                                    <p className="text-xs text-gray-500 mt-1">
                                        Requires Level {levelRequired}
                                    </p>
                                )}
                            </div>
                            {canEvolve ? (
                                <Sparkles className="w-8 h-8 text-green-500 animate-pulse"/>
                            ) : (
                                <Lock className="w-8 h-8 text-gray-400"/>
                            )}
                        </div>
                    </div>

                    {/* Progress/Message */}
                    <div className="bg-white/60 rounded-lg p-3 text-sm text-gray-700 text-center">
                        {message}
                    </div>

                    {/* Evolve Button */}
                    {canEvolve && onEvolveClick && (
                        <button
                            onClick={onEvolveClick}
                            className="w-full mt-4 py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-bold rounded-xl shadow-lg hover:from-purple-600 hover:to-pink-600 active:scale-95 transition-all duration-200 flex items-center justify-center gap-2"
                        >
                            <Sparkles className="w-5 h-5"/>
                            Evolve Now!
                        </button>
                    )}

                    {/* Levels Remaining */}
                    {!canEvolve && levelsRemaining !== null && levelsRemaining > 0 && (
                        <div className="mt-4 text-center">
                            <div className="inline-flex items-center gap-2 bg-purple-100 px-4 py-2 rounded-full">
                                <span className="text-sm font-semibold text-purple-700">
                                    {levelsRemaining} level{levelsRemaining !== 1 ? 's' : ''} to go!
                                </span>
                            </div>
                        </div>
                    )}
                </>
            )}

            {/* Max Evolution */}
            {!nextStage && (
                <div className="bg-gradient-to-r from-yellow-50 to-amber-50 rounded-xl p-4 border-2 border-yellow-300">
                    <div className="text-center">
                        <p className="text-2xl mb-2">👑</p>
                        <p className="font-bold text-amber-700">Maximum Evolution Reached!</p>
                        <p className="text-sm text-amber-600 mt-1">Your pet is fully evolved</p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default EvolutionCard;