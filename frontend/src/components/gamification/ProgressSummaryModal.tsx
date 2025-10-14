// components/gamification/ProgressSummaryModal.tsx
import React, {useState, useEffect} from 'react';
import {X, Zap, Flame, Target, TrendingUp} from 'lucide-react';

interface ProgressSummaryModalProps {
    isOpen: boolean;
    onClose: () => void;

    // XP data
    xpGained: number;
    previousXP: number;
    newTotalXP: number;

    // Rank data
    currentRank: string;
    currentTier: number;
    rankProgress: number; // Percentage through current rank (0-100)

    // Streak data
    streakDays: number;
    streakActive: boolean;
    streakBonusXP?: number;

    // Weekly data
    weeklyWorkouts: number;
    weeklyGoal?: number;

    // Tier change indicator (if tier changed but not rank)
    tieredUp?: boolean;
    previousTier?: number;
}

export const ProgressSummaryModal: React.FC<ProgressSummaryModalProps> = ({
                                                                              isOpen,
                                                                              onClose,
                                                                              xpGained,
                                                                              previousXP,
                                                                              newTotalXP,
                                                                              currentRank,
                                                                              currentTier,
                                                                              rankProgress,
                                                                              streakDays,
                                                                              streakActive,
                                                                              streakBonusXP = 0,
                                                                              weeklyWorkouts,
                                                                              weeklyGoal = 7,
                                                                              tieredUp = false,
                                                                              previousTier
                                                                          }) => {
    const [animatedXP, setAnimatedXP] = useState(0);
    const [animatedProgress, setAnimatedProgress] = useState(0);
    const [showContent, setShowContent] = useState(false);

    useEffect(() => {
        if (isOpen) {
            // Reset animations
            setAnimatedXP(0);
            setAnimatedProgress(0);
            setShowContent(false);

            // Start animations after a short delay
            const contentTimer = setTimeout(() => setShowContent(true), 100);

            // Animate XP counter
            const xpTimer = setTimeout(() => {
                const duration = 2000; // 2 seconds
                const steps = 60;
                const increment = xpGained / steps;
                let currentStep = 0;

                const xpInterval = setInterval(() => {
                    currentStep++;
                    setAnimatedXP(prev => {
                        const next = Math.min(prev + increment, xpGained);
                        return Math.round(next);
                    });

                    if (currentStep >= steps) {
                        clearInterval(xpInterval);
                        setAnimatedXP(xpGained);
                    }
                }, duration / steps);

                return () => clearInterval(xpInterval);
            }, 300);

            // Animate progress bar
            const progressTimer = setTimeout(() => {
                setAnimatedProgress(rankProgress);
            }, 800);

            return () => {
                clearTimeout(contentTimer);
                clearTimeout(xpTimer);
                clearTimeout(progressTimer);
            };
        }
    }, [isOpen, xpGained, rankProgress]);

    if (!isOpen) return null;

    const getTierDisplay = (tier: number) => {
        const tierMap: { [key: number]: string } = {
            3: 'III',
            2: 'II',
            1: 'I'
        };
        return tierMap[tier] || 'III';
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
            <div
                className={`
          relative w-full max-w-md bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900
          rounded-2xl shadow-2xl border border-slate-700/50 overflow-hidden
          transform transition-all duration-500
          ${showContent ? 'scale-100 opacity-100' : 'scale-95 opacity-0'}
        `}
            >
                {/* Decorative background elements */}
                <div
                    className="absolute inset-0 bg-gradient-to-br from-blue-500/5 via-transparent to-purple-500/5 pointer-events-none"/>
                <div
                    className="absolute top-0 right-0 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl pointer-events-none"/>

                {/* Close button */}
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 p-2 rounded-lg bg-slate-800/80 hover:bg-slate-700
                   transition-colors z-10 group"
                    aria-label="Close"
                >
                    <X className="w-5 h-5 text-slate-400 group-hover:text-white transition-colors"/>
                </button>

                <div className="relative p-8">
                    {/* Header */}
                    <div className="text-center mb-8">
                        <h2 className="text-3xl font-bold text-white mb-2">
                            🎉 Workout Complete!
                        </h2>
                        <p className="text-slate-400">Great job! Here's your progress</p>
                    </div>

                    {/* Tier Up Banner (if applicable) */}
                    {tieredUp && previousTier && (
                        <div className="mb-6 p-5 bg-gradient-to-r from-blue-600/30 via-cyan-600/30 to-blue-600/30
    rounded-xl border-2 border-blue-400/50 relative overflow-hidden
    animate-pulse-slow">
                            {/* Animated background shimmer */}
                            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
      animate-shimmer pointer-events-none"/>

                            <div className="relative flex items-center justify-center gap-3">
                                <div className="flex items-center gap-2 text-blue-300">
                                    <TrendingUp className="w-6 h-6 animate-bounce"/>
                                    <span
                                        className="text-xs uppercase tracking-wider font-medium">Tier Advancement</span>
                                </div>
                            </div>

                            <div className="relative mt-3 flex items-center justify-center gap-4">
                                {/* Old tier */}
                                <div className="px-4 py-2 bg-slate-800/60 rounded-lg border border-slate-600">
                                    <span className="text-slate-400 text-sm">{currentRank}</span>
                                    <span
                                        className="text-white font-bold text-lg ml-2">{getTierDisplay(previousTier)}</span>
                                </div>

                                {/* Arrow */}
                                <div className="text-blue-400 animate-pulse">
                                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3}
                                              d="M13 7l5 5m0 0l-5 5m5-5H6"/>
                                    </svg>
                                </div>

                                {/* New tier */}
                                <div
                                    className="px-4 py-2 bg-gradient-to-r from-blue-600 to-cyan-600 rounded-lg border-2 border-blue-400 shadow-lg shadow-blue-500/50">
                                    <span className="text-blue-200 text-sm">{currentRank}</span>
                                    <span
                                        className="text-white font-bold text-lg ml-2">{getTierDisplay(currentTier)}</span>
                                </div>
                            </div>

                            <div className="relative mt-3 text-center">
                                <p className="text-blue-300 text-sm font-medium">
                                    You're climbing the ranks! Keep pushing! 💪
                                </p>
                            </div>
                        </div>
                    )}

                    {/* XP Gained Section */}
                    <div className="mb-6 p-6 bg-gradient-to-br from-blue-500/10 to-purple-500/10
                        rounded-xl border border-blue-500/30">
                        <div className="flex items-center justify-between mb-4">
                            <div className="flex items-center gap-2 text-blue-400">
                                <Zap className="w-5 h-5"/>
                                <span className="font-semibold">XP Earned</span>
                            </div>
                            <div className="text-right">
                                <div className="text-3xl font-bold text-white tabular-nums">
                                    +{animatedXP.toLocaleString()}
                                </div>
                                {streakBonusXP > 0 && (
                                    <div className="text-xs text-green-400 mt-1">
                                        (+{streakBonusXP} streak bonus)
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Rank Progress Bar */}
                        <div className="space-y-2">
                            <div className="flex items-center justify-between text-sm">
                                {/* ANIMATED TIER TEXT CHANGE */}
                                {tieredUp && previousTier ? (
                                    <div className="relative inline-flex items-center gap-2">
                                        <span className="text-slate-400">{currentRank}</span>
                                        <div className="relative inline-block w-10">
                                            {/* Old tier flying out */}
                                            <span
                                                className="absolute left-0 animate-tier-slide-out text-red-400 font-bold">
                        {getTierDisplay(previousTier)}
                    </span>
                                            {/* New tier flying in */}
                                            <span
                                                className="absolute left-0 animate-tier-slide-in text-green-400 font-bold">
                        {getTierDisplay(currentTier)}
                    </span>
                                        </div>
                                    </div>
                                ) : (
                                    <span className="text-slate-400">
                {currentRank} {getTierDisplay(currentTier)}
            </span>
                                )}

                                <span className="text-slate-400 tabular-nums">
            {Math.round(animatedProgress)}%
        </span>
                            </div>
                            <div className="relative h-3 bg-slate-700/50 rounded-full overflow-hidden">
                                <div
                                    className="absolute inset-y-0 left-0 bg-gradient-to-r from-blue-500 to-blue-400
                rounded-full transition-all duration-1000 ease-out"
                                    style={{width: `${animatedProgress}%`}}
                                >
                                    <div className="absolute inset-0 bg-white/20 animate-pulse"/>
                                </div>
                            </div>
                            <div className="flex justify-between text-xs text-slate-500 tabular-nums">
                                <span>{previousXP.toLocaleString()} XP</span>
                                <span>{newTotalXP.toLocaleString()} XP</span>
                            </div>
                        </div>
                    </div>

                    {/* Stats Grid */}
                    <div className="grid grid-cols-2 gap-4 mb-6">
                        {/* Streak */}
                        <div className={`
              p-4 rounded-xl border transition-all
              ${streakActive
                            ? 'bg-green-500/10 border-green-500/30'
                            : 'bg-slate-800/50 border-slate-700/50'}
            `}>
                            <div className="flex items-center gap-2 mb-2">
                                {streakDays >= 2 && (
                                    <Flame
                                        className={`w-4 h-4 ${streakActive ? 'text-orange-400 animate-pulse' : 'text-slate-500'}`}/>
                                )}
                                <span className="text-xs font-medium text-slate-400">Streak</span>
                            </div>
                            <div className={`text-2xl font-bold ${streakActive ? 'text-green-400' : 'text-slate-400'}`}>
                                {streakDays}
                            </div>
                            <div className="text-xs text-slate-500 mt-1">day streak</div>
                        </div>

                        {/* Weekly Workouts */}
                        <div className="p-4 bg-slate-800/50 rounded-xl border border-slate-700/50">
                            <div className="flex items-center gap-2 mb-2">
                                <Target className="w-4 h-4 text-slate-500"/>
                                <span className="text-xs font-medium text-slate-400">This Week</span>
                            </div>
                            <div className="text-2xl font-bold text-white">
                                {weeklyWorkouts}/{weeklyGoal}
                            </div>
                            <div className="text-xs text-slate-500 mt-1">workouts</div>
                        </div>
                    </div>

                    {/* Continue Button */}
                    <button
                        onClick={onClose}
                        className="w-full py-4 bg-gradient-to-r from-blue-600 to-blue-500 hover:from-blue-500
                     hover:to-blue-400 text-white font-semibold rounded-xl transition-all
                     transform hover:scale-[1.02] active:scale-[0.98] shadow-lg
                     shadow-blue-500/25"
                    >
                        Continue
                    </button>

                    {/* Hint text */}
                    <p className="text-center text-xs text-slate-500 mt-4">
                        Keep up the great work! 💪
                    </p>
                </div>
            </div>
            <style>{`
  @keyframes shimmer {
    0% { transform: translateX(-100%); }
    100% { transform: translateX(100%); }
  }
  .animate-shimmer {
    animation: shimmer 2s infinite;
  }
  @keyframes pulse-slow {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.9; }
  }
  .animate-pulse-slow {
    animation: pulse-slow 2s ease-in-out infinite;
  }
  
  /* 🆕 TIER TEXT ANIMATIONS */
  @keyframes tier-slide-out {
    0% {
      opacity: 1;
      transform: translateX(0) scale(1);
    }
    100% {
      opacity: 0;
      transform: translateX(-30px) scale(0.5);
      color: rgba(248, 113, 113, 0);
    }
  }
  
  @keyframes tier-slide-in {
    0% {
      opacity: 0;
      transform: translateX(30px) scale(1.5);
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
  
  .animate-tier-slide-out {
    animation: tier-slide-out 0.8s ease-out forwards;
  }
  
  .animate-tier-slide-in {
    animation: tier-slide-in 1s ease-out forwards;
  }
`}</style>
        </div>
    );
};