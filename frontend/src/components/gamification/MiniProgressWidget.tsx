// components/gamification/MiniProgressWidget.tsx
import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Badge} from '../ui/badge';
import {ProgressTooltip} from './ProgressTooltip';
import {useUserProgression} from '../../hooks/useUserProgression';
import {getRankInfo, getNextRank, getTierDisplay} from '../../types/gamification';
import {useAuth} from '../../contexts/AuthContext';

interface MiniProgressWidgetProps {
    className?: string;
}

export const MiniProgressWidget: React.FC<MiniProgressWidgetProps> = ({className = ''}) => {
    const navigate = useNavigate();
    const {isAuthenticated} = useAuth();
    const {progression, loading} = useUserProgression();
    const [animatedXP, setAnimatedXP] = useState<number>(0);
    const [animatedProgress, setAnimatedProgress] = useState<number>(0);
    const [showTierGlow, setShowTierGlow] = useState(false);
    const [prevTier, setPrevTier] = useState<number | null>(null);
    const [showTierChange, setShowTierChange] = useState(false);
    const [oldTierDisplay, setOldTierDisplay] = useState<string>('');
    const [newTierDisplay, setNewTierDisplay] = useState<string>('');


    // Animate XP changes
    useEffect(() => {
        if (progression?.seasonalXp !== undefined) {
            const start = animatedXP;
            const end = progression.seasonalXp;
            const duration = 1000;
            const startTime = Date.now();

            const animate = () => {
                const elapsed = Date.now() - startTime;
                const progress = Math.min(elapsed / duration, 1);

                const easeOut = 1 - Math.pow(1 - progress, 3);
                const current = Math.floor(start + (end - start) * easeOut);

                setAnimatedXP(current);

                if (progress < 1) {
                    requestAnimationFrame(animate);
                }
            };

            animate();
        }
    }, [progression?.seasonalXp]);

    // Animate progress bar
    useEffect(() => {
        if (progression) {
            const currentRankInfo = getRankInfo(progression.seasonalRank);
            const nextRankInfo = getNextRank(progression.seasonalRank);

            let targetProgress = 0;
            if (nextRankInfo) {
                // Calculate progress within current rank (0-100%)
                const rankXpRange = currentRankInfo.maxXp - currentRankInfo.minXp;
                const xpIntoRank = progression.seasonalXp - currentRankInfo.minXp;
                targetProgress = Math.min(Math.max((xpIntoRank / rankXpRange) * 100, 0), 100);
            } else {
                // Max rank - show 100%
                targetProgress = 100;
            }

            const start = animatedProgress;
            const end = targetProgress;
            const duration = 1000;
            const startTime = Date.now();

            const animate = () => {
                const elapsed = Date.now() - startTime;
                const progress = Math.min(elapsed / duration, 1);

                const easeOut = 1 - Math.pow(1 - progress, 3);
                const current = start + (end - start) * easeOut;

                setAnimatedProgress(current);

                if (progress < 1) {
                    requestAnimationFrame(animate);
                }
            };

            animate();
        }
    }, [progression?.seasonalXp, progression?.seasonalRank]);

    // Detect tier changes
    useEffect(() => {
        const checkTierChange = () => {
            if (!progression?.seasonalTier) return;

            // Check for recent tier change from sessionStorage
            const recentTierChange = sessionStorage.getItem('recentTierChange');

            console.log('🔍 MiniProgressWidget checking tier change:', {
                hasSessionData: !!recentTierChange,
                currentTier: progression.seasonalTier,
                prevTier
            });

            if (recentTierChange) {
                try {
                    const tierData = JSON.parse(recentTierChange);
                    const timeSinceChange = Date.now() - tierData.timestamp;

                    console.log('📦 Tier change data found:', tierData, 'Time since:', timeSinceChange);

                    // If tier change happened in last 10 seconds, show dramatic animation
                    if (timeSinceChange < 10000 && tierData.newTier === progression.seasonalTier) {
                        console.log('🎊 Tier change detected! Showing DRAMATIC animation');

                        // Set old and new tier for animation
                        setOldTierDisplay(getTierDisplay(tierData.oldTier));
                        setNewTierDisplay(getTierDisplay(tierData.newTier));

                        // Show the change animation
                        setShowTierChange(true);
                        setShowTierGlow(true);

                        // Hide after 5 seconds
                        setTimeout(() => {
                            setShowTierChange(false);
                            setShowTierGlow(false);
                            sessionStorage.removeItem('recentTierChange');
                        }, 5000);
                    } else if (timeSinceChange >= 10000) {
                        // Clean up old data
                        sessionStorage.removeItem('recentTierChange');
                    }
                } catch (e) {
                    console.error('Error parsing tier change data:', e);
                    sessionStorage.removeItem('recentTierChange');
                }
            }

            // Also detect natural tier changes (from API polling/refetch)
            if (prevTier !== null && progression.seasonalTier < prevTier) {
                console.log('🎊 Natural tier change detected:', prevTier, '→', progression.seasonalTier);
                setOldTierDisplay(getTierDisplay(prevTier));
                setNewTierDisplay(getTierDisplay(progression.seasonalTier));
                setShowTierChange(true);
                setShowTierGlow(true);
                setTimeout(() => {
                    setShowTierChange(false);
                    setShowTierGlow(false);
                }, 5000);
            }

            setPrevTier(progression.seasonalTier);
        };

        // Check immediately when progression changes
        checkTierChange();

        // 🆕 Listen for tier change event from CalendarPage
        const handleTierChangeEvent = () => {
            console.log('🎯 Received tierChanged event!');
            // Small delay to ensure sessionStorage is written
            setTimeout(checkTierChange, 100);
        };

        window.addEventListener('tierChanged', handleTierChangeEvent);

        return () => {
            window.removeEventListener('tierChanged', handleTierChangeEvent);
        };
    }, [progression?.seasonalTier, prevTier]);

    // Don't show widget if not authenticated
    if (!isAuthenticated) {
        return null;
    }

    // Show loading state
    if (loading) {
        return (
            <div className={`flex items-center gap-2 ${className}`}>
                <div className="h-7 w-20 bg-gray-200 animate-pulse rounded"/>
                <div className="h-7 w-24 bg-gray-200 animate-pulse rounded"/>
                <div className="h-7 w-16 bg-gray-200 animate-pulse rounded"/>
            </div>
        );
    }

    // Don't show if no progression data
    if (!progression) {
        return null;
    }

    const currentRankInfo = getRankInfo(progression.seasonalRank);
    const nextRankInfo = getNextRank(progression.seasonalRank);
    const xpToNext = nextRankInfo ? (nextRankInfo.minXp - progression.seasonalXp) : 0;

    return (
        <div
            className={`flex items-center gap-2 cursor-pointer ${className}`}
            onClick={() => navigate('/progress')}
        >
            {/* XP Badge with Progress Bar */}
            <ProgressTooltip content={
                nextRankInfo
                    ? `${xpToNext.toLocaleString()} XP to ${nextRankInfo.name} • ${Math.round(animatedProgress)}% through ${currentRankInfo.name}`
                    : 'Max Rank Achieved! 🎉'
            }>
                <Badge
                    className="relative overflow-hidden bg-blue-600 hover:bg-blue-700 transition-colors px-3 py-1 text-sm font-medium">
                    {/* Progress Bar Background */}
                    <div
                        className="absolute inset-0 bg-blue-400 transition-all duration-1000 ease-out"
                        style={{width: `${animatedProgress}%`}}
                    />

                    {/* Content */}
                    <span className="relative flex items-center gap-1.5 z-10">
                        <span className="text-yellow-300">⚡</span>
                        <span className="tabular-nums">{animatedXP.toLocaleString()}</span>
                        <span className="text-blue-200 text-xs">XP</span>
                    </span>
                </Badge>
            </ProgressTooltip>

            {/* Rank Badge */}
            <ProgressTooltip content={`Tier ${getTierDisplay(progression.seasonalTier)} • Seasonal Rank`}>
                <div className="relative">
                    {/* Main Badge */}
                    <Badge className={`
            bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 
            transition-all px-3 py-1 text-sm font-medium relative overflow-visible
            ${showTierGlow ? 'animate-tier-glow-intense ring-4 ring-blue-400/70 scale-110' : ''}
        `}>
                        {/* Sparkle explosion on tier change */}
                        {showTierGlow && (
                            <>
                    <span className="absolute -top-2 -right-2 text-yellow-400 animate-ping text-xl">
                        ✨
                    </span>
                                <span className="absolute -top-2 -left-2 text-yellow-400 animate-ping text-xl"
                                      style={{animationDelay: '0.2s'}}>
                        ⭐
                    </span>
                                <span className="absolute -bottom-1 right-0 text-blue-400 animate-ping text-xl"
                                      style={{animationDelay: '0.4s'}}>
                        💫
                    </span>
                            </>
                        )}

                        <span className="flex items-center gap-1.5 relative z-10">
                <span>{currentRankInfo.icon}</span>
                <span className="uppercase tracking-wide">
                    {progression.seasonalRank}
                </span>

                            {/* ANIMATED TIER CHANGE */}
                            {showTierChange ? (
                                <span className="inline-flex items-center gap-1 ml-1">
                        {/* Old tier flying out */}
                                    <span className="absolute animate-tier-fly-out text-red-300 font-bold">
                            {oldTierDisplay}
                        </span>
                                    {/* New tier flying in */}
                                    <span className="animate-tier-fly-in text-green-300 font-bold">
                            {newTierDisplay}
                        </span>
                    </span>
                            ) : (
                                <span className="ml-1 font-bold">
                        {getTierDisplay(progression.seasonalTier)}
                    </span>
                            )}
            </span>
                    </Badge>

                    {/* Floating "TIER UP!" text */}
                    {showTierChange && (
                        <div className="absolute -top-8 left-1/2 -translate-x-1/2 whitespace-nowrap">
                            <div
                                className="animate-float-up bg-gradient-to-r from-blue-500 to-cyan-500 text-white px-3 py-1 rounded-full text-xs font-bold shadow-lg">
                                🎉 TIER UP!
                            </div>
                        </div>
                    )}
                </div>
            </ProgressTooltip>
        </div>
    );
};

const tierGlowStyles = `
@keyframes tier-glow {
  0%, 100% {
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.5);
  }
  50% {
    box-shadow: 0 0 40px rgba(59, 130, 246, 0.8);
  }
}

@keyframes tier-glow-intense {
  0%, 100% {
    box-shadow: 0 0 30px rgba(59, 130, 246, 0.8),
                0 0 60px rgba(59, 130, 246, 0.4);
    transform: scale(1.1);
  }
  50% {
    box-shadow: 0 0 50px rgba(59, 130, 246, 1),
                0 0 100px rgba(59, 130, 246, 0.6);
    transform: scale(1.15);
  }
}

@keyframes tier-fly-out {
  0% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateX(-20px) translateY(-10px) scale(0.5) rotate(-45deg);
  }
}

@keyframes tier-fly-in {
  0% {
    opacity: 0;
    transform: translateX(20px) translateY(10px) scale(1.5) rotate(45deg);
  }
  50% {
    opacity: 1;
    transform: translateX(0) scale(1.2);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

@keyframes float-up {
  0% {
    opacity: 0;
    transform: translateY(10px) scale(0.8);
  }
  20% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  80% {
    opacity: 1;
    transform: translateY(-5px) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-20px) scale(0.9);
  }
}

.animate-tier-glow {
  animation: tier-glow 1.5s ease-in-out infinite;
}

.animate-tier-glow-intense {
  animation: tier-glow-intense 1s ease-in-out infinite;
}

.animate-tier-fly-out {
  animation: tier-fly-out 0.6s ease-out forwards;
}

.animate-tier-fly-in {
  animation: tier-fly-in 0.8s ease-out forwards;
}

.animate-float-up {
  animation: float-up 3s ease-out forwards;
}
`;

if (typeof document !== 'undefined') {
    const styleEl = document.createElement('style');
    styleEl.innerHTML = tierGlowStyles;
    if (!document.getElementById('tier-glow-styles')) {
        styleEl.id = 'tier-glow-styles';
        document.head.appendChild(styleEl);
    }
}