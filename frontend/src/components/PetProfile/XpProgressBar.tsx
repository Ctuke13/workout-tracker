import React from 'react';

interface XpProgressBarProps {
    currentXp: number;
    xpToNextLevel: number;
    level: number;
    className?: string;
}

const XpProgressBar: React.FC<XpProgressBarProps> = ({
                                                         currentXp,
                                                         xpToNextLevel,
                                                         level,
                                                         className = '',
                                                     }) => {
    const percentage = Math.min(100, Math.max(0, (currentXp / xpToNextLevel) * 100));

    return (
        <div className={`space-y-2 ${className}`}>
            {/* Level & XP Text */}
            <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                    <span className="text-2xl font-bold text-purple-700">Level {level}</span>
                </div>
                <span className="text-sm font-semibold text-gray-600">
                    {currentXp} / {xpToNextLevel} XP
                </span>
            </div>

            {/* Progress Bar */}
            <div
                className="relative h-8 bg-gradient-to-r from-purple-100 to-pink-100 rounded-full overflow-hidden shadow-inner border-2 border-purple-200">
                {/* Fill */}
                <div
                    className="absolute inset-0 bg-gradient-to-r from-purple-500 via-pink-500 to-purple-600 transition-all duration-500 ease-out flex items-center justify-center"
                    style={{width: `${percentage}%`}}
                >
                    {percentage > 15 && (
                        <span className="text-xs font-bold text-white drop-shadow-md">
                            {Math.round(percentage)}%
                        </span>
                    )}
                </div>

                {/* Percentage (if bar is too small) */}
                {percentage <= 15 && percentage > 0 && (
                    <div className="absolute inset-0 flex items-center justify-center">
                        <span className="text-xs font-bold text-purple-600">
                            {Math.round(percentage)}%
                        </span>
                    </div>
                )}

                {/* Shimmer effect */}
                <div
                    className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent"
                    style={{
                        animation: 'shimmer 2s infinite',
                    }}
                />
            </div>

            {/* Next Level Indicator */}
            <div className="text-xs text-gray-500 text-center">
                {xpToNextLevel - currentXp} XP until Level {level + 1}
            </div>

            {/* Inline keyframes animation */}
            <style dangerouslySetInnerHTML={{
                __html: `
                    @keyframes shimmer {
                        0% { transform: translateX(-100%); }
                        100% { transform: translateX(100%); }
                    }
                `
            }}/>
        </div>
    );
};

export default XpProgressBar;