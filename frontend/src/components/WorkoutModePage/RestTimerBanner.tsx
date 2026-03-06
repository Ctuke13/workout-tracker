import React, {useState, useEffect} from 'react';
import {Clock, FastForward} from 'lucide-react';
import {Button} from '../ui/button';

// Minimum seconds before the Skip button unlocks
const MIN_REST_BEFORE_SKIP = 10;

interface RestTimerBannerProps {
    isResting: boolean;
    currentRestSeconds: number;
    targetRestSeconds: number;
    onEndRest: () => void;
    onSkipRest: () => void;
    exerciseType: 'strength' | 'isometric' | 'cardio';
}

export const RestTimerBanner: React.FC<RestTimerBannerProps> = ({
                                                                    isResting,
                                                                    currentRestSeconds,
                                                                    targetRestSeconds,
                                                                    onEndRest,
                                                                    onSkipRest,
                                                                    exerciseType
                                                                }) => {
    // Track how long this rest period has been open so Skip can be locked
    // independently of currentRestSeconds (which is managed by the parent).
    const [restOpenSeconds, setRestOpenSeconds] = useState(0);

    // Reset and start counting whenever a new rest period begins
    useEffect(() => {
        if (!isResting) {
            setRestOpenSeconds(0);
            return;
        }
        setRestOpenSeconds(0);
        const interval = setInterval(() => {
            setRestOpenSeconds(prev => prev + 1);
        }, 1000);
        return () => clearInterval(interval);
    }, [isResting]);

    if (!isResting) return null;

    const canSkip = restOpenSeconds >= MIN_REST_BEFORE_SKIP;
    const skipSecondsRemaining = Math.max(0, MIN_REST_BEFORE_SKIP - restOpenSeconds);

    const progressPercentage = Math.min(100, (currentRestSeconds / targetRestSeconds) * 100);
    const isOverTarget = currentRestSeconds > targetRestSeconds;

    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    };

    return (
        <div className="fixed top-0 left-0 right-0 z-50 bg-gradient-to-r from-yellow-500 to-orange-500 shadow-lg animate-slide-down">
            {/* Progress bar */}
            <div className="absolute bottom-0 left-0 right-0 h-1 bg-yellow-900/30">
                <div
                    className={`h-1 transition-all duration-1000 ${isOverTarget ? 'bg-red-400' : 'bg-white'}`}
                    style={{width: `${progressPercentage}%`}}
                />
            </div>

            <div className="container mx-auto px-4 py-3">
                <div className="flex items-center justify-between gap-4">
                    {/* Timer Display */}
                    <div className="flex items-center gap-3">
                        <div className="bg-white/20 rounded-full p-2">
                            <Clock className="w-5 h-5 text-white"/>
                        </div>
                        <div>
                            <div className="text-white font-bold text-lg">
                                {formatTime(currentRestSeconds)}
                                {isOverTarget && (
                                    <span className="ml-2 text-sm font-normal opacity-90">
                                        (+{formatTime(currentRestSeconds - targetRestSeconds)} over)
                                    </span>
                                )}
                            </div>
                            <div className="text-white/80 text-xs">
                                Target: {formatTime(targetRestSeconds)}
                            </div>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex items-center gap-2">
                        <Button
                            onClick={onEndRest}
                            size="sm"
                            className="bg-green-600 hover:bg-green-700 text-white font-semibold px-4 py-2 shadow-md"
                        >
                            <span className="hidden sm:inline">Ready - </span>Start Next Set
                        </Button>

                        <Button
                            onClick={canSkip ? onSkipRest : undefined}
                            size="sm"
                            variant="outline"
                            className={`border-white/40 text-white backdrop-blur-sm transition-opacity ${
                                canSkip
                                    ? 'bg-white/20 hover:bg-white/30 cursor-pointer'
                                    : 'bg-white/10 opacity-50 cursor-not-allowed'
                            }`}
                            disabled={!canSkip}
                        >
                            <FastForward className="w-4 h-4 sm:mr-1"/>
                            <span className="hidden sm:inline">
                                {canSkip ? 'Skip' : `Skip (${skipSecondsRemaining}s)`}
                            </span>
                            <span className="sm:hidden">
                                {canSkip ? '' : `${skipSecondsRemaining}s`}
                            </span>
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};