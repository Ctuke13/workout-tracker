// components/gamification/RankPromotionModal.tsx
import React, {useState, useEffect} from 'react';
import {Crown, TrendingUp, ArrowRight, Sparkles, X} from 'lucide-react';

interface RankPromotionModalProps {
    isOpen: boolean;
    oldRank: string;
    oldTier: number;
    newRank: string;
    newTier: number;
    onDismiss: () => void;
}

export const RankPromotionModal: React.FC<RankPromotionModalProps> = ({
                                                                          isOpen,
                                                                          oldRank,
                                                                          oldTier,
                                                                          newRank,
                                                                          newTier,
                                                                          onDismiss,
                                                                      }) => {
    const [showContent, setShowContent] = useState(false);
    const [showRanks, setShowRanks] = useState(false);

    useEffect(() => {
        if (isOpen) {
            // Reset states
            setShowContent(false);
            setShowRanks(false);

            // Staggered animations
            const contentTimer = setTimeout(() => setShowContent(true), 100);
            const ranksTimer = setTimeout(() => setShowRanks(true), 800);

            return () => {
                clearTimeout(contentTimer);
                clearTimeout(ranksTimer);
            };
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const getTierDisplay = (tier: number) => {
        const tierMap: { [key: number]: string } = {
            3: 'III',
            2: 'II',
            1: 'I'
        };
        return tierMap[tier] || 'III';
    };

    // Check if this is a full rank change (NOVICE -> APPRENTICE) or just tier change
    const isFullRankChange = oldRank !== newRank;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-lg">
            {/* Epic particle effects */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {[...Array(40)].map((_, i) => (
                    <div
                        key={i}
                        className="absolute animate-float"
                        style={{
                            left: `${Math.random() * 100}%`,
                            top: `${Math.random() * 100}%`,
                            animationDelay: `${Math.random() * 3}s`,
                            animationDuration: `${3 + Math.random() * 4}s`
                        }}
                    >
                        <Sparkles
                            className="text-yellow-400 opacity-70"
                            size={8 + Math.random() * 16}
                        />
                    </div>
                ))}
            </div>

            {/* Modal */}
            <div
                className={`
          relative w-full max-w-2xl overflow-hidden rounded-3xl
          bg-gradient-to-br from-yellow-900/30 via-orange-900/30 to-amber-900/30
          backdrop-blur-xl border-2 border-yellow-500/50
          shadow-2xl shadow-yellow-500/50
          transform transition-all duration-1000
          ${showContent ? 'scale-100 opacity-100 rotate-0' : 'scale-90 opacity-0 rotate-6'}
        `}
            >
                {/* Golden gradient bar */}
                <div className="h-3 bg-gradient-to-r from-yellow-500 via-orange-500 to-yellow-500 animate-pulse"/>

                {/* Close button */}
                <button
                    onClick={onDismiss}
                    className="absolute top-4 right-4 p-2 rounded-full bg-slate-900/80 hover:bg-slate-800
                   transition-colors z-10 group"
                    aria-label="Close"
                >
                    <X className="w-5 h-5 text-slate-400 group-hover:text-white transition-colors"/>
                </button>

                <div className="relative p-12 text-center">
                    {/* Crown Icon */}
                    <div className={`
            mx-auto mb-8 relative
            transform transition-all duration-1000 delay-300
            ${showContent ? 'scale-100 rotate-0' : 'scale-0 rotate-180'}
          `}>
                        {/* Epic glow */}
                        <div className="absolute inset-0 bg-gradient-to-r from-yellow-500 via-orange-500 to-yellow-500
                          blur-3xl opacity-60 animate-pulse"/>

                        {/* Crown container */}
                        <div className="relative w-40 h-40 mx-auto rounded-full
                          bg-gradient-to-br from-yellow-500 via-orange-500 to-yellow-600
                          flex items-center justify-center
                          border-4 border-yellow-300/30
                          shadow-2xl animate-pulse">
                            <Crown className="w-20 h-20 text-white drop-shadow-2xl" strokeWidth={1.5}/>
                        </div>
                    </div>

                    {/* Promotion announcement */}
                    <div className={`
            mb-8 transform transition-all duration-1000 delay-500
            ${showContent ? 'translate-y-0 opacity-100' : 'translate-y-8 opacity-0'}
          `}>
                        <p className="text-yellow-400 text-sm font-bold uppercase tracking-widest mb-3 animate-pulse">
                            🎉 Rank Promotion! 🎉
                        </p>
                        <h1 className="text-5xl font-black text-white mb-4 leading-tight">
                            {isFullRankChange ? 'PROMOTED!' : 'TIER UP!'}
                        </h1>
                        <p className="text-xl text-slate-300">
                            You've reached a new {isFullRankChange ? 'rank' : 'tier'}!
                        </p>
                    </div>

                    {/* Rank comparison */}
                    <div className={`
            flex items-center justify-center gap-6 mb-10
            transform transition-all duration-1000 delay-700
            ${showRanks ? 'translate-y-0 opacity-100' : 'translate-y-8 opacity-0'}
          `}>
                        {/* Old rank */}
                        <div className="flex flex-col items-center">
                            <div className="px-6 py-3 rounded-2xl bg-slate-800/80 border border-slate-600 mb-2">
                                <p className="text-sm text-slate-400 uppercase tracking-wide mb-1">From</p>
                                <p className="text-2xl font-bold text-slate-300">
                                    {oldRank} {getTierDisplay(oldTier)}
                                </p>
                            </div>
                        </div>

                        {/* Arrow */}
                        <div className="animate-pulse">
                            <ArrowRight className="w-10 h-10 text-yellow-400" strokeWidth={3}/>
                        </div>

                        {/* New rank */}
                        <div className="flex flex-col items-center">
                            <div className="px-6 py-3 rounded-2xl bg-gradient-to-br from-yellow-600 to-orange-600
                            border-2 border-yellow-400 mb-2 shadow-lg shadow-yellow-500/50
                            animate-pulse">
                                <p className="text-sm text-yellow-200 uppercase tracking-wide mb-1 font-bold">To</p>
                                <p className="text-3xl font-black text-white">
                                    {newRank} {getTierDisplay(newTier)}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Motivational message */}
                    <div className={`
            mb-8 transform transition-all duration-1000 delay-900
            ${showRanks ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}
          `}>
                        <div className="inline-flex items-center gap-2 px-6 py-3 rounded-full
                          bg-gradient-to-r from-yellow-600/20 to-orange-600/20
                          border border-yellow-500/30">
                            <TrendingUp className="w-5 h-5 text-yellow-400"/>
                            <span className="text-yellow-300 font-semibold">
                {isFullRankChange
                    ? 'Outstanding achievement! Keep pushing forward!'
                    : 'Great progress! You\'re climbing the ranks!'}
              </span>
                        </div>
                    </div>

                    {/* Continue button */}
                    <button
                        onClick={onDismiss}
                        className="w-full py-5 rounded-2xl font-bold text-xl text-white
                 bg-gradient-to-r from-yellow-600 via-orange-600 to-yellow-600
                 hover:shadow-2xl hover:shadow-yellow-500/50
                 transform hover:scale-105 transition-all duration-300
                 border-2 border-yellow-400/30"
                    >
                        Awesome!
                    </button>

                    {/* Skip hint - REMOVE OR UPDATE */}
                    <p className="mt-4 text-sm text-slate-500">
                        Click to continue
                    </p>
                </div>
            </div>

            <style>{`
        @keyframes float {
          0%, 100% {
            transform: translateY(0px) translateX(0px);
            opacity: 0;
          }
          10% {
            opacity: 1;
          }
          90% {
            opacity: 1;
          }
          100% {
            transform: translateY(-100vh) translateX(${Math.random() * 100 - 50}px);
            opacity: 0;
          }
        }
        .animate-float {
          animation: float linear infinite;
        }
      `}</style>
        </div>
    );
};