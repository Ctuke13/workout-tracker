import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {TrendingUp, Trophy, Dumbbell, Clock, Target} from 'lucide-react';
import {analyticsApi, TimePeriodSummary, PersonalRecord, TopExercise} from '../../services/analyticsApi';
import {useSeason} from '../../contexts/SeasonContext';

export const AnalyticsPreview: React.FC = () => {
    const navigate = useNavigate();
    
    const {theme, loading: seasonLoading} = useSeason();
    const [currentSlide, setCurrentSlide] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Data states
    const [weeklySummary, setWeeklySummary] = useState<TimePeriodSummary | null>(null);
    const [recentPRs, setRecentPRs] = useState<PersonalRecord[]>([]);
    const [topExercises, setTopExercises] = useState<TopExercise[]>([]);

    // Fetch data
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);

                const [summary, prs, exercises] = await Promise.all([
                    analyticsApi.getWeeklySummary(),
                    analyticsApi.getRecentPersonalRecords(30),
                    analyticsApi.getTopExercises('WEEK', 1)
                ]);

                setWeeklySummary(summary);
                setRecentPRs(prs);
                setTopExercises(exercises);
                setLoading(false);
            } catch (err) {
                console.error('Failed to fetch analytics:', err);
                setError('Failed to load analytics');
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    // Auto-rotate slides every 5 seconds
    useEffect(() => {
        if (loading || !weeklySummary) return;

        const interval = setInterval(() => {
            setCurrentSlide((prev) => (prev + 1) % 5);
        }, 5000);

        return () => clearInterval(interval);
    }, [loading, weeklySummary]);

    if (loading || seasonLoading) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-xl p-4 sm:p-6 border-2 ${theme.border} animate-pulse`}>
                <div className={`h-8 ${theme.accentLight} rounded w-48 mb-4`}></div>
                <div className={`h-24 ${theme.accentLight} rounded mb-4`}></div>
            </div>
        );
    }

    if (error || !weeklySummary) {
        return (
            <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-4 sm:p-6 border-2 border-red-300">
                <div className="text-center text-red-700">
                    <p className="text-base sm:text-lg font-semibold mb-2">⚠️ {error || 'No data available'}</p>
                </div>
            </div>
        );
    }

    // Build slides based on available data
    const slides = buildSlides(weeklySummary, recentPRs, topExercises);

    if (slides.length === 0) {
        return (
            <div className={`bg-gradient-to-br ${theme.gradient} rounded-xl p-4 sm:p-6 border-2 ${theme.border}`}>
                <div className="text-center py-8">
                    <p className={`text-base sm:text-lg font-bold ${theme.textPrimary} mb-2`}>
                        📊 Start Your Journey!
                    </p>
                    <p className={`text-xs sm:text-sm ${theme.textSecondary}`}>
                        Complete a workout to see your analytics here
                    </p>
                </div>
            </div>
        );
    }

    const currentSlideData = slides[currentSlide % slides.length];

    return (
        <div
            className={`relative overflow-hidden bg-gradient-to-br ${theme.gradient} rounded-xl border-2 ${theme.border} shadow-xl hover:shadow-2xl transition-shadow`}>
            {/* Animated background effects - Analytics themed */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-20">
                {/* Ambient orbs */}
                <div
                    className={`absolute top-0 right-0 w-32 h-32 sm:w-48 sm:h-48 ${theme.accentLight} rounded-full blur-3xl animate-pulse`}></div>
                <div
                    className={`absolute bottom-0 left-0 w-32 h-32 sm:w-48 sm:h-48 ${theme.accentLight} rounded-full blur-3xl animate-pulse`}
                    style={{animationDelay: '1s'}}
                ></div>

                {/* Analytics-themed floating particles */}
                <AnalyticsParticles theme={theme}/>
            </div>

            <div className="relative p-4 sm:p-6 space-y-4">
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div
                            className={`w-10 h-10 sm:w-12 sm:h-12 ${theme.accentGradient} rounded-xl flex items-center justify-center shadow-lg`}>
                            <TrendingUp className="w-5 h-5 sm:w-6 sm:h-6 text-white"/>
                        </div>
                        <div>
                            <h2 className={`text-lg sm:text-2xl font-black ${theme.textPrimary}`}>
                                This Week's Highlights
                            </h2>
                            <p className={`text-xs sm:text-sm ${theme.textSecondary} font-semibold`}>
                                Your progress at a glance
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={() => navigate('/analytics')}
                        className={`px-3 py-1.5 sm:px-4 sm:py-2 bg-gradient-to-r ${theme.buttonGradient} text-white text-xs sm:text-sm font-bold rounded-lg transition-all duration-200 transform hover:scale-105 shadow-lg`}>
                        View All →
                    </button>
                </div>

                {/* Rotating Slide Content */}
                <div className="min-h-[120px] flex items-center justify-center">
                    <div
                        key={currentSlide}
                        className="w-full animate-fadeIn"
                    >
                        <SlideContent slide={currentSlideData} theme={theme}/>
                    </div>
                </div>

                {/* Slide Indicators */}
                <div className="flex justify-center gap-2">
                    {slides.map((_, index) => (
                        <button
                            key={index}
                            onClick={() => setCurrentSlide(index)}
                            className={`w-2 h-2 rounded-full transition-all duration-300 ${
                                index === currentSlide % slides.length
                                    ? `${theme.accentBg} w-6`
                                    : 'bg-gray-300'
                            }`}
                            aria-label={`Go to slide ${index + 1}`}
                        />
                    ))}
                </div>
            </div>

            {/* CSS for fade animation */}
            <style>{`
                @keyframes fadeIn {
                    from {
                        opacity: 0;
                        transform: translateY(10px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
                .animate-fadeIn {
                    animation: fadeIn 0.5s ease-out;
                }
            `}</style>
        </div>
    );
};

// ==================== HELPER COMPONENTS ====================

interface SlideData {
    icon: React.ReactNode;
    title: string;
    value: string;
    subtitle: string;
    trend?: {
        direction: 'up' | 'down' | 'same';
        value: string;
    };
}

interface SlideContentProps {
    slide: SlideData;
    theme: any;
}

const SlideContent: React.FC<SlideContentProps> = ({slide, theme}) => {
    return (
        <div className="text-center space-y-2">
            {/* Icon */}
            <div className="flex justify-center mb-2">
                <div
                    className={`w-14 h-14 sm:w-16 sm:h-16 ${theme.accentGradient} rounded-2xl flex items-center justify-center shadow-lg`}>
                    {slide.icon}
                </div>
            </div>

            {/* Title */}
            <h3 className={`text-xl sm:text-2xl font-black ${theme.textPrimary}`}>
                {slide.title}
            </h3>

            {/* Value */}
            <p className={`text-3xl sm:text-4xl font-black ${theme.textPrimary} tabular-nums`}>
                {slide.value}
            </p>

            {/* Subtitle with optional trend */}
            <div className="flex items-center justify-center gap-2">
                <p className={`text-sm sm:text-base ${theme.textSecondary} font-semibold`}>
                    {slide.subtitle}
                </p>
                {slide.trend && slide.trend.direction !== 'same' && (
                    <span className={`text-xs sm:text-sm font-bold ${
                        slide.trend.direction === 'up' ? 'text-green-600' : 'text-red-600'
                    }`}>
                        {slide.trend.direction === 'up' ? '↑' : '↓'} {slide.trend.value}
                    </span>
                )}
            </div>
        </div>
    );
};

// ==================== SLIDE BUILDER ====================

function buildSlides(
    summary: TimePeriodSummary,
    prs: PersonalRecord[],
    topExercises: TopExercise[]
): SlideData[] {
    const slides: SlideData[] = [];

    // Slide 1: Workouts This Week
    if (summary.workouts > 0 || true) { // Always show this slide
        slides.push({
            icon: <Dumbbell className="w-7 h-7 sm:w-8 sm:h-8 text-white"/>,
            title: 'Workouts',
            value: summary.workouts.toString(),
            subtitle: 'completed this week',
            trend: summary.workoutChange !== 0 ? {
                direction: summary.workoutChange > 0 ? 'up' : 'down',
                value: `${Math.abs(summary.workoutChange)}%`
            } : undefined
        });
    }

    // Slide 2: Minutes Trained
    if (summary.minutes > 0) {
        slides.push({
            icon: <Clock className="w-7 h-7 sm:w-8 sm:h-8 text-white"/>,
            title: 'Time Invested',
            value: `${summary.minutes}min`,
            subtitle: 'trained this week',
            trend: summary.minutesChange !== 0 ? {
                direction: summary.minutesChange > 0 ? 'up' : 'down',
                value: `${Math.abs(summary.minutesChange)}%`
            } : undefined
        });
    }

    // Slide 3: Volume Lifted
    if (summary.volume > 0) {
        slides.push({
            icon: <Target className="w-7 h-7 sm:w-8 sm:h-8 text-white"/>,
            title: 'Total Volume',
            value: `${(summary.volume / 1000).toFixed(1)}K`,
            subtitle: 'lbs lifted this week',
            trend: summary.volumeChange !== 0 ? {
                direction: summary.volumeChange > 0 ? 'up' : 'down',
                value: `${Math.abs(summary.volumeChange)}%`
            } : undefined
        });
    }

    // Slide 4: Recent PR
    if (prs.length > 0) {
        const latestPR = prs[0];
        slides.push({
            icon: <Trophy className="w-7 h-7 sm:w-8 sm:h-8 text-white"/>,
            title: 'New PR!',
            value: `${latestPR.value} ${latestPR.unit}`,
            subtitle: latestPR.exerciseName
        });
    }

    // Slide 5: Top Exercise
    if (topExercises.length > 0) {
        const topExercise = topExercises[0];
        slides.push({
            icon: <TrendingUp className="w-7 h-7 sm:w-8 sm:h-8 text-white"/>,
            title: 'Top Exercise',
            value: topExercise.exerciseName,
            subtitle: `${topExercise.count} sessions this week`
        });
    }

    return slides;
}

// ==================== ANALYTICS PARTICLES ====================

interface AnalyticsParticlesProps {
    theme: any;
}

const AnalyticsParticles: React.FC<AnalyticsParticlesProps> = ({theme}) => {
    const particles = [
        {icon: '📈', delay: 0, duration: 8, x: '15%'},
        {icon: '📊', delay: 1.5, duration: 9, x: '35%'},
        {icon: '💹', delay: 3, duration: 7.5, x: '55%'},
        {icon: '📉', delay: 4.5, duration: 8.5, x: '75%'},
        {icon: '🎯', delay: 2, duration: 8, x: '25%'},
        {icon: '⚡', delay: 3.5, duration: 7, x: '65%'},
    ];

    return (
        <>
            {particles.map((particle, i) => (
                <div
                    key={i}
                    className="absolute text-3xl sm:text-4xl"
                    style={{
                        left: particle.x,
                        bottom: '-5%',
                        animation: `floatAnalytics ${particle.duration}s ease-in-out infinite`,
                        animationDelay: `${particle.delay}s`,
                        filter: 'drop-shadow(0 0 8px rgba(251, 191, 36, 0.6))',
                    }}
                >
                    {particle.icon}
                </div>
            ))}
            <style>{`
                @keyframes floatAnalytics {
                    0%, 100% {
                        transform: translateY(0) translateX(0) scale(1) rotate(0deg);
                        opacity: 0.6;
                    }
                    25% {
                        transform: translateY(-20vh) translateX(5px) scale(1.15) rotate(5deg);
                        opacity: 0.8;
                    }
                    50% {
                        transform: translateY(-40vh) translateX(-5px) scale(1.1) rotate(-5deg);
                        opacity: 0.7;
                    }
                    75% {
                        transform: translateY(-60vh) translateX(8px) scale(1.2) rotate(3deg);
                        opacity: 0.75;
                    }
                }
            `}</style>
        </>
    );
};