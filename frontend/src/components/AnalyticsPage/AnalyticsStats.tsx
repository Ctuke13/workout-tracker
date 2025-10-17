import React, {useEffect, useState} from 'react';
import {Dumbbell, Clock, Flame} from 'lucide-react';
import {analyticsApi} from '../../services/analyticsApi';
import {useSeason} from '../../contexts/SeasonContext';
import {TimePeriod, mapPeriodToBackend} from '../../types/analytics';


interface AnalyticsStatsProps {
    period: TimePeriod;
}

interface StatsData {
    workouts: number;
    minutes: number;
    volume: number;
    workoutChange: number;
    minutesChange: number;
    volumeChange: number;
}

export const AnalyticsStats: React.FC<AnalyticsStatsProps> = ({period}) => {
    const {theme} = useSeason();
    const [stats, setStats] = useState<StatsData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadStats();
    }, [period]);

    const loadStats = async () => {
        try {
            setLoading(true);
            setError(null);

            const backendPeriod = mapPeriodToBackend(period);
            let data;

            switch (backendPeriod) {
                case 'WEEK':
                    data = await analyticsApi.getWeeklySummary();
                    break;
                case 'MONTH':
                    data = await analyticsApi.getMonthlySummary();
                    break;
                case 'YEAR':
                    data = await analyticsApi.getYearlySummary();
                    break;
                default:
                    // This should never happen, but TypeScript needs it
                    throw new Error(`Unsupported period: ${backendPeriod}`);
            }

            setStats(data); // ✅ TypeScript now knows data is defined
        } catch (err) {
            console.error('Failed to load stats:', err);
            setError('Failed to load statistics');
        } finally {
            setLoading(false);
        }
    }

    if (loading) {
        return (
            <div className="grid grid-cols-3 gap-3">
                {[1, 2, 3].map((i) => (
                    <div
                        key={i}
                        className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-3 border ${theme.border} animate-pulse`}
                    >
                        <div className={`h-4 ${theme.accentLight} rounded w-16 mb-2`}></div>
                        <div className={`h-6 ${theme.accentLight} rounded w-12 mb-1`}></div>
                        <div className={`h-3 ${theme.accentLight} rounded w-14`}></div>
                    </div>
                ))}
            </div>
        );
    }

    if (error || !stats) {
        return (
            <div className="bg-red-50 border border-red-300 rounded-lg p-4 text-center">
                <p className="text-red-700 text-sm font-semibold">⚠️ {error || 'No data available'}</p>
            </div>
        );
    }

    const getPeriodLabel = () => {
        switch (period) {
            case 'WEEK':
                return 'This Week';
            case 'MONTH':
                return 'This Month';
            case 'SEASON':
                return 'This Season';
            case 'YEAR':
                return 'All Year';
        }
    };

    // Estimate calories (roughly 5 calories per minute of exercise)
    const estimatedCalories = stats.minutes * 5;

    const statCards = [
        {
            icon: <Dumbbell className="w-4 h-4 text-white"/>,
            label: 'Workouts',
            value: stats.workouts.toString(),
            subtitle: getPeriodLabel(),
            change: stats.workoutChange,
            color: 'from-blue-500 to-blue-600'
        },
        {
            icon: <Clock className="w-4 h-4 text-white"/>,
            label: 'Time',
            value: `${stats.minutes}`,
            unit: 'min',
            subtitle: 'Duration',
            change: stats.minutesChange,
            color: 'from-purple-500 to-purple-600'
        },
        {
            icon: <Flame className="w-4 h-4 text-white"/>,
            label: 'Calories',
            value: estimatedCalories >= 1000 ? `${(estimatedCalories / 1000).toFixed(1)}K` : estimatedCalories.toString(),
            subtitle: 'Burned',
            change: stats.minutesChange, // Use same change as minutes for now
            color: 'from-orange-500 to-orange-600'
        }
    ];

    return (
        <div className="grid grid-cols-3 gap-3 md:gap-4">
            {statCards.map((card, index) => (
                <div
                    key={index}
                    className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-3 md:p-4 border ${theme.border} shadow-md hover:shadow-lg transition-all duration-200`}
                >
                    {/* Icon */}
                    <div className={`inline-flex p-2 bg-gradient-to-br ${card.color} rounded-lg shadow-md mb-2`}>
                        {card.icon}
                    </div>

                    {/* Label */}
                    <div
                        className={`text-[10px] md:text-xs font-bold ${theme.textSecondary} uppercase tracking-wide mb-1`}>
                        {card.label}
                    </div>

                    {/* Value */}
                    <div
                        className={`text-xl md:text-2xl font-black ${theme.textPrimary} tabular-nums leading-none mb-1`}>
                        {card.value}
                        {card.unit && <span className="text-sm md:text-base ml-1">{card.unit}</span>}
                    </div>

                    {/* Subtitle and Change */}
                    <div className="flex items-center justify-between gap-1">
            <span className={`text-[10px] md:text-xs ${theme.textSecondary} font-semibold truncate`}>
              {card.subtitle}
            </span>

                        {/* Change Indicator (only if not ALL_TIME) */}
                        {period !== 'YEAR' && card.change !== 0 && (
                            <span
                                className={`text-[10px] md:text-xs font-bold flex items-center gap-0.5 flex-shrink-0 ${
                                    card.change > 0 ? 'text-green-600' : 'text-red-600'
                                }`}
                            >
                {card.change > 0 ? '↑' : '↓'}{Math.abs(card.change)}%
              </span>
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
};