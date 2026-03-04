import React from 'react';
import {Utensils, Zap, Sparkles} from 'lucide-react';
import {MealType} from '../../types/pet';

interface PetActionsCardProps {
    stats: {
        fuel: number;
        motivation: number;
        fatigue: number;
        cleanliness: number;
        crystals: number;
    };
    actionLoading: boolean;
    onFeed: (mealType: MealType) => void;
    onMotivate: () => void;
    onBathe: () => void;
}

const PetActionsCard: React.FC<PetActionsCardProps> = ({
                                                           stats,
                                                           actionLoading,
                                                           onFeed,
                                                           onMotivate,
                                                           onBathe
                                                       }) => {
    const canAfford = (cost: number) => stats.crystals >= cost;

    const actions = [
        {
            id: 'feed',
            icon: Utensils,
            label: 'Feed',
            cost: 1,
            effect: '+15 Fuel',
            color: 'from-orange-400 to-red-500',
            textColor: 'text-orange-600',
            bgColor: 'bg-orange-50',
            hoverColor: 'hover:bg-orange-100',
            disabled: !canAfford(1) || stats.fuel >= 100,
            onClick: () => onFeed('SNACK'),
            disabledReason: stats.fuel >= 100 ? 'Fuel is full!' : 'Not enough crystals'
        },
        {
            id: 'motivate',
            icon: Zap,
            label: 'Motivate',
            cost: 3,
            effect: '+15 Energy',
            color: 'from-yellow-400 to-amber-500',
            textColor: 'text-yellow-600',
            bgColor: 'bg-yellow-50',
            hoverColor: 'hover:bg-yellow-100',
            disabled: !canAfford(3) || stats.motivation >= 100,
            onClick: onMotivate,
            disabledReason: stats.motivation >= 100 ? 'Motivation is full!' : 'Not enough crystals'
        },
        {
            id: 'bathe',
            icon: Sparkles,
            label: 'Bathe',
            cost: 2,
            effect: '+25 Clean',
            color: 'from-cyan-400 to-blue-500',
            textColor: 'text-cyan-600',
            bgColor: 'bg-cyan-50',
            hoverColor: 'hover:bg-cyan-100',
            disabled: !canAfford(2) || stats.cleanliness >= 100,
            onClick: onBathe,
            disabledReason: stats.cleanliness >= 100 ? 'Cleanliness is full!' : 'Not enough crystals'
        }
    ];

    return (
        <div className="pet-actions-card bg-white rounded-2xl shadow-lg border-2 border-purple-100 overflow-hidden">
            {/* Header */}
            <div className="bg-gradient-to-r from-purple-500 to-pink-500 px-4 py-3">
                <div className="flex items-center gap-2">
                    <span className="text-2xl">🎮</span>
                    <h3 className="text-lg font-bold text-white">Pet Care</h3>
                </div>
                <p className="text-purple-100 text-sm mt-1">
                    Keep your pet happy and healthy!
                </p>
            </div>

            {/* Action Buttons */}
            <div className="p-4">
                <div className="grid grid-cols-3 gap-3">
                    {actions.map((action) => {
                        const Icon = action.icon;
                        const isDisabled = action.disabled || actionLoading;

                        return (
                            <button
                                key={action.id}
                                onClick={action.onClick}
                                disabled={isDisabled}
                                className={`
                                    relative flex flex-col items-center gap-2 p-4 rounded-xl
                                    transition-all duration-200 border-2
                                    ${isDisabled
                                    ? 'bg-gray-50 border-gray-200 opacity-50 cursor-not-allowed'
                                    : `${action.bgColor} ${action.hoverColor} border-transparent hover:border-purple-300 hover:shadow-md active:scale-95`
                                }
                                `}
                                title={isDisabled ? action.disabledReason : `${action.label} your pet`}
                            >
                                {/* Icon with gradient background */}
                                <div className={`
                                    w-12 h-12 rounded-full flex items-center justify-center
                                    bg-gradient-to-br ${action.color} shadow-md
                                    ${!isDisabled && 'group-hover:scale-110 transition-transform'}
                                `}>
                                    <Icon className="w-6 h-6 text-white" strokeWidth={2.5}/>
                                </div>

                                {/* Label */}
                                <div className="text-center">
                                    <div
                                        className={`font-bold text-sm ${isDisabled ? 'text-gray-400' : action.textColor}`}>
                                        {action.label}
                                    </div>
                                    <div className="text-xs text-gray-500 mt-1">
                                        {action.effect}
                                    </div>
                                </div>

                                {/* Cost Badge */}
                                <div className={`
                                    flex items-center gap-1 px-2 py-1 rounded-full text-xs font-bold
                                    ${isDisabled
                                    ? 'bg-gray-200 text-gray-400'
                                    : 'bg-white shadow-sm text-amber-700'
                                }
                                `}>
                                    <span>💎</span>
                                    <span>{action.cost}</span>
                                </div>

                                {/* Loading Spinner Overlay */}
                                {actionLoading && (
                                    <div
                                        className="absolute inset-0 bg-white/80 rounded-xl flex items-center justify-center">
                                        <div
                                            className="w-5 h-5 border-2 border-purple-500 border-t-transparent rounded-full animate-spin"/>
                                    </div>
                                )}
                            </button>
                        );
                    })}
                </div>

                {/* Helpful Tip */}
                <div className="mt-4 p-3 bg-purple-50 rounded-lg border border-purple-200">
                    <div className="flex items-start gap-2">
                        <span className="text-lg">💡</span>
                        <div className="flex-1">
                            <p className="text-xs text-purple-700 leading-relaxed">
                                <strong>Tip:</strong> Complete workouts to earn crystals! Use them to keep your pet's
                                stats balanced and watch them thrive.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PetActionsCard;