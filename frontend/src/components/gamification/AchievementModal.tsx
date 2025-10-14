// components/gamification/AchievementModal.tsx
import React, {useState, useEffect} from 'react';
import {Trophy, Sparkles, X} from 'lucide-react';

interface Achievement {
    achievementId: number;
    name: string;
    description: string;
    icon: string;
    rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
    bonusXp: number;
}

interface AchievementModalProps {
    isOpen: boolean;
    achievement: Achievement;
    onDismiss: () => void;
    autoAdvanceSeconds?: number; // Auto-dismiss after X seconds (default 5)
}

export const AchievementModal: React.FC<AchievementModalProps> = ({
                                                                      isOpen,
                                                                      achievement,
                                                                      onDismiss,
                                                                      autoAdvanceSeconds = 10
                                                                  }) => {
    const [showContent, setShowContent] = useState(false);
    const [countdown, setCountdown] = useState(autoAdvanceSeconds);

    useEffect(() => {
        if (isOpen) {
            // Reset states
            setShowContent(false);
            setCountdown(autoAdvanceSeconds);

            // Trigger entrance animation
            const contentTimer = setTimeout(() => setShowContent(true), 100);

            // Start countdown
            const countdownInterval = setInterval(() => {
                setCountdown(prev => {
                    if (prev <= 1) {
                        clearInterval(countdownInterval);
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);

            return () => {
                clearTimeout(contentTimer);
                clearInterval(countdownInterval);
            };
        }
    }, [isOpen, autoAdvanceSeconds]);

    // 🆕 SEPARATE useEffect to handle auto-dismiss
    useEffect(() => {
        if (isOpen && countdown === 0) {
            onDismiss();
        }
    }, [countdown, isOpen, onDismiss]);

    if (!isOpen) return null;

    // Rarity-based colors
    const getRarityConfig = () => {
        const configs = {
            COMMON: {
                gradient: 'from-gray-600 to-gray-500',
                glow: 'shadow-gray-500/50',
                text: 'text-gray-300',
                border: 'border-gray-500/50',
                bg: 'bg-gray-900/95'
            },
            UNCOMMON: {
                gradient: 'from-green-600 to-emerald-500',
                glow: 'shadow-green-500/50',
                text: 'text-green-300',
                border: 'border-green-500/50',
                bg: 'bg-green-900/20'
            },
            RARE: {
                gradient: 'from-blue-600 to-cyan-500',
                glow: 'shadow-blue-500/50',
                text: 'text-blue-300',
                border: 'border-blue-500/50',
                bg: 'bg-blue-900/20'
            },
            EPIC: {
                gradient: 'from-purple-600 to-pink-500',
                glow: 'shadow-purple-500/50',
                text: 'text-purple-300',
                border: 'border-purple-500/50',
                bg: 'bg-purple-900/20'
            },
            LEGENDARY: {
                gradient: 'from-yellow-500 via-orange-500 to-red-500',
                glow: 'shadow-yellow-500/70',
                text: 'text-yellow-300',
                border: 'border-yellow-500/50',
                bg: 'bg-gradient-to-br from-yellow-900/30 to-orange-900/30'
            }
        };
        return configs[achievement.rarity] || configs.COMMON;
    };

    const rarityConfig = getRarityConfig();

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
            {/* Animated particles */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {[...Array(20)].map((_, i) => (
                    <Sparkles
                        key={i}
                        className={`absolute text-yellow-400 animate-ping opacity-60`}
                        style={{
                            left: `${Math.random() * 100}%`,
                            top: `${Math.random() * 100}%`,
                            animationDelay: `${Math.random() * 2}s`,
                            animationDuration: `${2 + Math.random() * 2}s`
                        }}
                        size={12 + Math.random() * 12}
                    />
                ))}
            </div>

            {/* Modal */}
            <div
                className={`
          relative w-full max-w-lg overflow-hidden rounded-3xl
          border-2 ${rarityConfig.border} ${rarityConfig.bg}
          backdrop-blur-xl shadow-2xl ${rarityConfig.glow}
          transform transition-all duration-700
          ${showContent ? 'scale-100 opacity-100 rotate-0' : 'scale-75 opacity-0 rotate-12'}
        `}
            >
                {/* Gradient header bar */}
                <div className={`h-2 bg-gradient-to-r ${rarityConfig.gradient}`}/>

                {/* Close button */}
                <button
                    onClick={onDismiss}
                    className="absolute top-4 right-4 p-2 rounded-full bg-slate-900/80 hover:bg-slate-800
                   transition-colors z-10 group"
                    aria-label="Close"
                >
                    <X className="w-5 h-5 text-slate-400 group-hover:text-white transition-colors"/>
                </button>

                <div className="relative p-8 text-center">
                    {/* Achievement Icon */}
                    <div className={`
            mx-auto mb-6 relative
            transform transition-all duration-700 delay-200
            ${showContent ? 'scale-100 rotate-0' : 'scale-0 rotate-180'}
          `}>
                        {/* Glow effect */}
                        <div className={`
              absolute inset-0 bg-gradient-to-r ${rarityConfig.gradient} 
              blur-3xl opacity-50 animate-pulse
            `}/>

                        {/* Icon container */}
                        <div className={`
              relative w-32 h-32 mx-auto rounded-full 
              bg-gradient-to-br ${rarityConfig.gradient}
              flex items-center justify-center
              border-4 border-white/20
              shadow-xl
            `}>
              <span className="text-6xl drop-shadow-lg">
                {achievement.icon}
              </span>
                        </div>
                    </div>

                    {/* Achievement unlocked text */}
                    <div className={`
            mb-6 transform transition-all duration-700 delay-300
            ${showContent ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}
          `}>
                        <p className={`text-sm font-semibold uppercase tracking-wider mb-2 ${rarityConfig.text}`}>
                            Achievement Unlocked
                        </p>
                        <h2 className="text-3xl font-bold text-white mb-3 leading-tight">
                            {achievement.name}
                        </h2>
                        <p className="text-slate-300 text-base leading-relaxed max-w-md mx-auto">
                            {achievement.description}
                        </p>
                    </div>

                    {/* Rarity Badge */}
                    <div className={`
            inline-block px-4 py-2 rounded-full mb-6
            bg-gradient-to-r ${rarityConfig.gradient}
            transform transition-all duration-700 delay-400
            ${showContent ? 'scale-100 opacity-100' : 'scale-75 opacity-0'}
          `}>
            <span className="text-white font-bold text-sm uppercase tracking-wide">
              {achievement.rarity}
            </span>
                    </div>

                    {/* Bonus XP */}
                    <div className={`
            flex items-center justify-center gap-2 mb-6
            transform transition-all duration-700 delay-500
            ${showContent ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}
          `}>
                        <Sparkles className="w-5 h-5 text-yellow-400"/>
                        <span className="text-2xl font-bold text-yellow-400">
              +{achievement.bonusXp} XP
            </span>
                    </div>

                    {/* Continue button with countdown */}
                    <button
                        onClick={onDismiss}
                        className={`
              w-full py-4 rounded-xl font-semibold text-white
              bg-gradient-to-r ${rarityConfig.gradient}
              hover:shadow-2xl transform hover:scale-105
              transition-all duration-300
              ${showContent ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}
            `}
                        style={{transitionDelay: '600ms'}}
                    >
                        {countdown > 0 ? `Continue (${countdown}s)` : 'Continue'}
                    </button>

                    {/* Skip hint */}
                    <p className="mt-3 text-xs text-slate-500">
                        Click anywhere or wait {countdown}s to continue
                    </p>
                </div>
            </div>
        </div>
    );
};