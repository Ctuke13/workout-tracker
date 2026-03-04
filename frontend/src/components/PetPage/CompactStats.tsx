import React from 'react';
import {PetStats} from '../../types/pet';

interface CompactStatsProps {
    stats: PetStats;
}

const CompactStats: React.FC<CompactStatsProps> = ({stats}) => {
    const getStatColor = (value: number, inverse = false): string => {
        const effectiveValue = inverse ? 100 - value : value;
        if (effectiveValue >= 70) return 'bg-green-500';
        if (effectiveValue >= 40) return 'bg-yellow-500';
        if (effectiveValue >= 20) return 'bg-orange-500';
        return 'bg-red-500';
    };

    const statItems = [
        {
            icon: '🍖',
            label: 'Fuel',
            value: stats.fuel,
            color: getStatColor(stats.fuel),
        },
        {
            icon: '⚡',
            label: 'Motivation',
            value: stats.motivation,
            color: getStatColor(stats.motivation),
        },
        {
            icon: '😴',
            label: 'Fatigue',
            value: stats.fatigue,
            color: getStatColor(stats.fatigue, true),
            inverse: true,
        },
        {
            icon: '✨',
            label: 'Cleanliness',
            value: stats.cleanliness,
            color: getStatColor(stats.cleanliness),
        },
    ];

    const level = stats.level ?? 1;
    const currentXp = stats.xp ?? 0;
    const xpToNextLevel = stats.xpToNextLevel ?? 100;
    const xpPercentage = Math.min(100, (currentXp / xpToNextLevel) * 100);

    return (
        <div className="bg-white rounded-2xl shadow-lg border-2 border-purple-100 overflow-hidden">
            {/* Header with Level & XP Bar */}
            <div className="px-4 py-3 bg-gradient-to-r from-purple-50 to-pink-50">
                {/* Title & Level */}
                <div className="flex items-center gap-2 mb-2">
                    <span className="text-sm font-bold text-gray-900">Pet Stats</span>
                    <span className="text-gray-400">·</span>
                    <span className="text-sm font-semibold text-purple-600">Level {level}</span>
                </div>

                {/* XP Progress Bar */}
                <div className="flex items-center gap-2">
                    <div className="flex-1 bg-white/50 rounded-full h-2 overflow-hidden">
                        <div
                            className="h-full bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transition-all duration-500"
                            style={{width: `${xpPercentage}%`}}
                        />
                    </div>
                    <span className="text-xs font-medium text-gray-600 whitespace-nowrap">
                        {currentXp}/{xpToNextLevel} XP
                    </span>
                </div>
            </div>

            {/* Always Visible Stats */}
            <div className="px-4 py-4 space-y-3">
                {statItems.map((stat) => (
                    <div key={stat.label} className="space-y-1">
                        <div className="flex items-center justify-between text-sm">
                            <div className="flex items-center gap-2">
                                <span className="text-base">{stat.icon}</span>
                                <span className="font-medium text-gray-700">{stat.label}</span>
                            </div>
                            <span className="font-bold text-gray-900">{stat.value}/100</span>
                        </div>
                        <div className="bg-gray-200 rounded-full h-2 overflow-hidden">
                            <div
                                className={`h-full ${stat.color} rounded-full transition-all duration-300`}
                                style={{width: `${stat.value}%`}}
                            />
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CompactStats;