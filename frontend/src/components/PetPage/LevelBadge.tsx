import React from 'react';
import {Award} from 'lucide-react';
import {useNavigate} from 'react-router-dom';

interface LevelBadgeProps {
    level: number;
    currentXp: number;
    xpToNextLevel: number;
    className?: string;
}

const LevelBadge: React.FC<LevelBadgeProps> = ({
                                                   level,
                                                   currentXp,
                                                   xpToNextLevel,
                                                   className = '',
                                               }) => {
    const navigate = useNavigate();
    const percentage = Math.min(100, (currentXp / xpToNextLevel) * 100);

    return (
        <button
            onClick={() => navigate('/pet/profile')}
            className={`group bg-gradient-to-br from-purple-500 to-pink-500 rounded-2xl p-3 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105 ${className}`}
        >
            {/* Level Number */}
            <div className="flex items-center gap-2 mb-2">
                <Award className="w-5 h-5 text-white"/>
                <span className="text-white font-bold text-lg">Level {level}</span>
            </div>

            {/* Mini XP Bar */}
            <div className="bg-white/30 rounded-full h-2 overflow-hidden backdrop-blur-sm">
                <div
                    className="h-full bg-white rounded-full transition-all duration-500"
                    style={{width: `${percentage}%`}}
                />
            </div>

            {/* XP Text */}
            <p className="text-white/90 text-xs mt-1.5 font-medium">
                {currentXp}/{xpToNextLevel} XP
            </p>

            {/* Hover hint */}
            <p className="text-white/70 text-[10px] mt-1 opacity-0 group-hover:opacity-100 transition-opacity">
                Tap for profile →
            </p>
        </button>
    );
};

export default LevelBadge;