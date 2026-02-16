import React from 'react';
import {Award, Dumbbell, Gem} from 'lucide-react';

interface PetStatsCardProps {
    level: number;
    workoutsCompleted: number;
    crystals: number;
    maxCrystals?: number;
    petName: string | null;
    evolutionStageDisplay: string;
    className?: string;
}

const PetStatsCard: React.FC<PetStatsCardProps> = ({
                                                       level,
                                                       workoutsCompleted,
                                                       crystals,
                                                       maxCrystals = 15,
                                                       petName,
                                                       evolutionStageDisplay,
                                                       className = '',
                                                   }) => {
    return (
        <div className={`bg-white rounded-2xl p-6 shadow-lg border border-gray-200 ${className}`}>
            {/* Pet Name & Stage */}
            <div className="text-center mb-6">
                <div className="text-5xl mb-3">🐺</div>
                <h2 className="text-2xl font-bold text-gray-900">
                    {petName || 'Your Pet'}
                </h2>
                <p className="text-sm text-gray-600 mt-1">{evolutionStageDisplay}</p>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-3 gap-4">
                {/* Level */}
                <div
                    className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-xl p-4 text-center border border-purple-200">
                    <div className="flex justify-center mb-2">
                        <div className="w-10 h-10 bg-purple-500 rounded-full flex items-center justify-center">
                            <Award className="w-5 h-5 text-white"/>
                        </div>
                    </div>
                    <p className="text-2xl font-bold text-purple-700">{level}</p>
                    <p className="text-xs text-gray-600 mt-1">Level</p>
                </div>

                {/* Workouts */}
                <div
                    className="bg-gradient-to-br from-blue-50 to-cyan-50 rounded-xl p-4 text-center border border-blue-200">
                    <div className="flex justify-center mb-2">
                        <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
                            <Dumbbell className="w-5 h-5 text-white"/>
                        </div>
                    </div>
                    <p className="text-2xl font-bold text-blue-700">{workoutsCompleted}</p>
                    <p className="text-xs text-gray-600 mt-1">Workouts</p>
                </div>

                {/* Crystals */}
                <div
                    className="bg-gradient-to-br from-amber-50 to-orange-50 rounded-xl p-4 text-center border border-amber-200">
                    <div className="flex justify-center mb-2">
                        <div className="w-10 h-10 bg-amber-500 rounded-full flex items-center justify-center">
                            <Gem className="w-5 h-5 text-white"/>
                        </div>
                    </div>
                    <p className="text-2xl font-bold text-amber-700">{crystals}</p>
                    <p className="text-xs text-gray-600 mt-1">
                        Crystals
                    </p>
                </div>
            </div>

            {/* Crystal Progress */}
            <div className="mt-4 bg-gray-50 rounded-lg p-3">
                <div className="flex justify-between items-center mb-1">
                    <span className="text-xs text-gray-600">Crystal Storage</span>
                    <span className="text-xs font-semibold text-gray-700">
                        {crystals}/{maxCrystals}
                    </span>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div
                        className="h-full bg-gradient-to-r from-amber-400 to-orange-500 transition-all duration-500"
                        style={{width: `${Math.min(100, (crystals / maxCrystals) * 100)}%`}}
                    />
                </div>
            </div>
        </div>
    );
};

export default PetStatsCard;