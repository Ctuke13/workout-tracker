import React, {useState} from 'react';
import {ChevronDown, ChevronUp} from 'lucide-react';
import {PetStats} from '../../types/pet';
import PetStatBars from './PetStatBars';

interface CompactStatsProps {
    stats: PetStats;
}

const CompactStats: React.FC<CompactStatsProps> = ({stats}) => {
    const [isExpanded, setIsExpanded] = useState(false);

    // Helper to get stat color
    const getStatColor = (value: number, inverse = false) => {
        const effectiveValue = inverse ? 100 - value : value;
        if (effectiveValue >= 70) return 'text-green-600';
        if (effectiveValue >= 40) return 'text-yellow-600';
        if (effectiveValue >= 20) return 'text-orange-600';
        return 'text-red-600';
    };

    // Check if any stat needs attention (below 40%)
    const needsAttention =
        stats.fuel < 40 ||
        stats.motivation < 40 ||
        stats.fatigue > 60 ||
        stats.cleanliness < 40;

    return (
        <div className="bg-white/90 backdrop-blur-sm rounded-2xl shadow-lg overflow-hidden">
            {/* Compact View */}
            <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full p-4 flex items-center justify-between hover:bg-purple-50/50 transition-colors"
            >
                {/* Stats Grid */}
                <div className="flex items-center gap-4 flex-1">
                    {/* Fuel */}
                    <div className="flex items-center gap-1.5">
                        <span className="text-xl">🍖</span>
                        <span className={`font-bold text-sm ${getStatColor(stats.fuel)}`}>
                            {Math.round(stats.fuel)}
                        </span>
                    </div>

                    {/* Motivation */}
                    <div className="flex items-center gap-1.5">
                        <span className="text-xl">💪</span>
                        <span className={`font-bold text-sm ${getStatColor(stats.motivation)}`}>
                            {Math.round(stats.motivation)}
                        </span>
                    </div>

                    {/* Fatigue */}
                    <div className="flex items-center gap-1.5">
                        <span className="text-xl">😴</span>
                        <span className={`font-bold text-sm ${getStatColor(stats.fatigue, true)}`}>
                            {Math.round(stats.fatigue)}
                        </span>
                    </div>

                    {/* Cleanliness */}
                    <div className="flex items-center gap-1.5">
                        <span className="text-xl">✨</span>
                        <span className={`font-bold text-sm ${getStatColor(stats.cleanliness)}`}>
                            {Math.round(stats.cleanliness)}
                        </span>
                    </div>
                </div>

                {/* Expand/Collapse Button */}
                <div className="flex items-center gap-2">
                    {needsAttention && (
                        <div className="flex items-center gap-1">
                            <div className="w-2 h-2 bg-red-500 rounded-full animate-pulse"/>
                            <span className="text-xs text-red-600 font-medium">Needs care</span>
                        </div>
                    )}
                    {isExpanded ? (
                        <ChevronUp className="w-5 h-5 text-gray-500"/>
                    ) : (
                        <ChevronDown className="w-5 h-5 text-gray-500"/>
                    )}
                </div>
            </button>

            {/* Expanded View - Full Stat Bars */}
            {isExpanded && (
                <div className="border-t border-gray-200 p-4 pt-3 animate-fadeIn">
                    <PetStatBars stats={stats}/>
                </div>
            )}

            <style dangerouslySetInnerHTML={{
                __html: `
                    @keyframes fadeIn {
                        from { opacity: 0; transform: translateY(-10px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                    .animate-fadeIn {
                        animation: fadeIn 0.3s ease-out;
                    }
                `
            }}/>
        </div>
    );
};

export default CompactStats;