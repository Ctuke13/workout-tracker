import React, {useState, useEffect} from 'react';
import {TrendingUp, BarChart3, Activity, ChevronDown, ChevronLeft, ChevronRight, Sparkles, Zap} from 'lucide-react';
import {
    LineChart,
    Line,
    BarChart,
    Bar,
    Cell,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer
} from 'recharts';
import {useSeason} from '../../contexts/SeasonContext';
import {useUserPreferences} from '../../contexts/UserPreferencesContext';
import {TimePeriod, ChartMetric} from '../../types/analytics';
import {analyticsApi} from '../../services/analyticsApi';
import {mapPeriodToBackend} from '../../types/analytics';

// ==================== TYPES ====================

type ChartMode = 'EXERCISE' | 'WORKOUT';
type ChartType = 'LINE' | 'BAR';

interface MetricOption {
    value: string;
    label: string;
    unit: string;
    icon: string;
}

interface ChartDataPoint {
    date: string;
    value: number;
    displayValue: number;
    label: string;
    workoutCount: number;
    isCurrentPeriod?: boolean;
}

interface Summary {
    average: number;
    peak: number;
    low: number;
    trend: 'UP' | 'DOWN' | 'STABLE';
    trendPercentage: number;
}

interface PerformanceTrackerChartProps {
    period?: TimePeriod;
    onPeriodChange?: (period: TimePeriod) => void;
}

// ==================== HELPERS ====================

const isIntegerMetric = (metric: string): boolean => {
    const integerMetrics = [
        'TOTAL_REPS', 'WORKOUT_COUNT', 'CALORIES',
        'MAX_HOLD', 'TOTAL_HOLD', 'AVG_HOLD',
        'TOTAL_DURATION', 'WORKOUT_DURATION', 'REPS', 'SETS'
    ];
    return integerMetrics.includes(metric);
};

const formatMetricValue = (value: number, metric: string): string => {
    if (isIntegerMetric(metric)) {
        return Math.round(value).toString();
    }
    return value.toFixed(1);
};

const getDateRange = (period: TimePeriod, offset: number) => {
    const today = new Date();

    if (period === 'WEEK') {
        const targetWeek = new Date(today);
        targetWeek.setDate(today.getDate() + (offset * 7));

        const dayOfWeek = targetWeek.getDay();
        const diff = targetWeek.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1);

        const startDate = new Date(targetWeek.setDate(diff));
        const endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 6);

        return {startDate, endDate};
    }

    if (period === 'MONTH') {
        const targetMonth = new Date(today.getFullYear(), today.getMonth() + offset, 1);
        const startDate = new Date(targetMonth.getFullYear(), targetMonth.getMonth(), 1);
        const endDate = new Date(targetMonth.getFullYear(), targetMonth.getMonth() + 1, 0);

        return {startDate, endDate};
    }

    if (period === 'SEASON') {
        const seasonStart = new Date();
        const seasonEnd = new Date();
        return {startDate: seasonStart, endDate: seasonEnd};
    }

    if (period === 'YEAR') {
        const targetYear = new Date(today.getFullYear() + offset, 0, 1);
        const startDate = new Date(targetYear.getFullYear(), 0, 1);
        const endDate = new Date(targetYear.getFullYear(), 11, 31);

        return {startDate, endDate};
    }

    return {startDate: today, endDate: today};
};

const formatDateRange = (startDate: Date, endDate: Date): string => {
    const start = startDate.toLocaleDateString('en-US', {month: 'short', day: 'numeric'});
    const end = endDate.toLocaleDateString('en-US', {month: 'short', day: 'numeric'});
    return `${start} - ${end}`;
};

const getPeriodLabel = (period: TimePeriod, offset: number, seasonName: string, seasonEmoji: string): string => {
    if (offset === 0) {
        if (period === 'WEEK') return 'This Week';
        if (period === 'MONTH') {
            return new Date().toLocaleDateString('en-US', {month: 'long', year: 'numeric'});
        }
        if (period === 'SEASON') return `${seasonEmoji} ${seasonName}`;
        if (period === 'YEAR') return new Date().getFullYear().toString();
    }

    if (offset === -1) {
        if (period === 'WEEK') return 'Last Week';
        if (period === 'MONTH') {
            const lastMonth = new Date();
            lastMonth.setMonth(lastMonth.getMonth() - 1);
            return lastMonth.toLocaleDateString('en-US', {month: 'long', year: 'numeric'});
        }
        if (period === 'SEASON') return 'Last Season';
        if (period === 'YEAR') return (new Date().getFullYear() - 1).toString();
    }

    if (period === 'WEEK') return `${Math.abs(offset)} Weeks Ago`;
    if (period === 'MONTH') {
        const targetMonth = new Date();
        targetMonth.setMonth(targetMonth.getMonth() + offset);
        return targetMonth.toLocaleDateString('en-US', {month: 'long', year: 'numeric'});
    }
    if (period === 'SEASON') return `${Math.abs(offset)} Seasons Ago`;
    if (period === 'YEAR') return (new Date().getFullYear() + offset).toString();

    return 'Unknown';
};

const formatChartDate = (dateStr: string, period: TimePeriod): string => {
    const date = new Date(dateStr);

    switch (period) {
        case 'WEEK':
            return date.toLocaleDateString('en-US', {weekday: 'short'});
        case 'MONTH':
            return date.toLocaleDateString('en-US', {month: 'short', day: 'numeric'});
        case 'SEASON':
            return date.toLocaleDateString('en-US', {month: 'short', day: 'numeric'});
        case 'YEAR':
            return date.toLocaleDateString('en-US', {month: 'short'});
        default:
            return dateStr;
    }
};

// ==================== MAIN COMPONENT ====================

export default function PerformanceTrackerChart({
                                                    period: externalPeriod,
                                                    onPeriodChange
                                                }: PerformanceTrackerChartProps) {
    const {theme, season} = useSeason();
    const {distanceUnit, weightUnit} = useUserPreferences();

    const [mode, setMode] = useState<ChartMode>('EXERCISE');
    const [availableExercises, setAvailableExercises] = useState<{
        id: number;
        name: string;
        trackingMode: 'REP_BASED' | 'TIME_BASED' | 'HOLD_BASED';
    }[]>([]);
    const [selectedExercise, setSelectedExercise] = useState<number | null>(null);
    const [selectedMetric, setSelectedMetric] = useState<string>('VOLUME');
    const [chartType, setChartType] = useState<ChartType>('LINE');
    const [internalPeriod, setInternalPeriod] = useState<TimePeriod>('MONTH');
    const [offset, setOffset] = useState(0);
    const [error, setError] = useState<string | null>(null);

    const [showMetricDropdown, setShowMetricDropdown] = useState(false);
    const [loading, setLoading] = useState(false);

    const [dataPoints, setDataPoints] = useState<ChartDataPoint[]>([]);
    const [summary, setSummary] = useState<Summary | null>(null);

    const period = externalPeriod || internalPeriod;

    useEffect(() => {
        const loadTopExercises = async () => {
            try {
                const exercises = await analyticsApi.getTopExercises('MONTH', 10);
                setAvailableExercises(exercises.map(ex => ({
                    id: ex.exerciseId,
                    name: ex.exerciseName,
                    trackingMode: ex.trackingMode
                })));
            } catch (err) {
                console.error('Failed to load exercises:', err);
                setAvailableExercises([]);
            }
        };

        loadTopExercises();
    }, []);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            const target = event.target as HTMLElement;
            if (!target.closest('.dropdown-container')) {
                setShowMetricDropdown(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const getAvailableMetrics = (): MetricOption[] => {
        if (mode === 'WORKOUT') {
            return [
                {value: 'VOLUME', label: 'Total Volume', unit: weightUnit, icon: '📊'},
                {value: 'REPS', label: 'Total Reps', unit: 'reps', icon: '🔢'},
                {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'},
                {value: 'CALORIES', label: 'Calories', unit: 'cal', icon: '🔥'},
                {value: 'DISTANCE', label: 'Distance', unit: distanceUnit, icon: '🏃'}
            ];
        }

        const selectedExerciseData = availableExercises.find(e => e.id === selectedExercise);

        if (!selectedExercise || !selectedExerciseData) {
            return [
                {value: 'WEIGHT', label: 'Max Weight', unit: weightUnit, icon: '🏋️'},
                {value: 'VOLUME', label: 'Total Volume', unit: weightUnit, icon: '📊'},
                {value: 'REPS', label: 'Total Reps', unit: 'reps', icon: '🔢'},
                {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'},
                {value: 'DISTANCE', label: 'Distance', unit: distanceUnit, icon: '🏃'},
                {value: 'PACE', label: 'Pace', unit: `min/${distanceUnit}`, icon: '⏱️'},
                {value: 'SPEED', label: 'Speed', unit: `${distanceUnit}/hr`, icon: '⚡'},
                {value: 'CALORIES', label: 'Calories', unit: 'cal', icon: '🔥'}
            ];
        }

        const trackingMode = selectedExerciseData.trackingMode;

        if (trackingMode === 'REP_BASED') {
            return [
                {value: 'WEIGHT', label: 'Max Weight', unit: weightUnit, icon: '🏋️'},
                {value: 'VOLUME', label: 'Total Volume', unit: weightUnit, icon: '📊'},
                {value: 'REPS', label: 'Total Reps', unit: 'reps', icon: '🔢'},
                {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'}
            ];
        }

        if (trackingMode === 'TIME_BASED') {
            return [
                {value: 'DISTANCE', label: 'Distance', unit: distanceUnit, icon: '🏃'},
                {value: 'PACE', label: 'Pace', unit: `min/${distanceUnit}`, icon: '⏱️'},
                {value: 'SPEED', label: 'Speed', unit: `${distanceUnit}/hr`, icon: '⚡'},
                {value: 'CALORIES', label: 'Calories', unit: 'cal', icon: '🔥'}
            ];
        }

        if (trackingMode === 'HOLD_BASED') {
            return [
                {value: 'HOLD_DURATION', label: 'Avg Hold Time', unit: 'seconds', icon: '⏱️'},
                {value: 'TOTAL_HOLD_TIME', label: 'Total Hold Time', unit: 'seconds', icon: '⏳'},
                {value: 'MAX_HOLD', label: 'Max Hold', unit: 'seconds', icon: '🏆'},
                {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'}
            ];
        }

        return [
            {value: 'WEIGHT', label: 'Max Weight', unit: weightUnit, icon: '🏋️'},
            {value: 'VOLUME', label: 'Total Volume', unit: weightUnit, icon: '📊'}
        ];
    };

    const availableMetrics = getAvailableMetrics();
    const currentMetric = availableMetrics.find(m => m.value === selectedMetric) || availableMetrics[0];

    useEffect(() => {
        loadChartData();
    }, [mode, selectedExercise, selectedMetric, externalPeriod, internalPeriod, offset]);

    useEffect(() => {
        const metrics = getAvailableMetrics();
        if (metrics.length > 0 && !metrics.find(m => m.value === selectedMetric)) {
            setSelectedMetric(metrics[0].value);
        }
    }, [mode, selectedExercise]);

    const loadChartData = async () => {
        const metrics = getAvailableMetrics();
        if (metrics.length === 0) {
            return;
        }

        const validMetric = metrics.find(m => m.value === selectedMetric);
        if (!validMetric) {
            setSelectedMetric(metrics[0].value);
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const currentPeriod = externalPeriod || internalPeriod;

            const response = await analyticsApi.getPerformanceTrackerData(
                selectedMetric,
                mapPeriodToBackend(currentPeriod),
                selectedExercise
            );

            const chartData: ChartDataPoint[] = response.dataPoints.map(point => ({
                date: point.date,
                value: point.value,
                displayValue: point.value,
                label: formatChartDate(point.date, currentPeriod),
                workoutCount: point.workoutCount,
                isCurrentPeriod: false
            }));

            setDataPoints(chartData);

            setSummary({
                average: response.summary.average,
                peak: response.summary.peak,
                low: response.summary.low,
                trend: response.summary.trend as 'UP' | 'DOWN' | 'STABLE',
                trendPercentage: response.summary.trendPercentage
            });

        } catch (err) {
            console.error('Failed to load chart data:', err);
            setError('Failed to load performance data');
        } finally {
            setLoading(false);
        }
    };

    const handleModeChange = (newMode: ChartMode) => {
        setMode(newMode);
        setSelectedExercise(null);
        setShowMetricDropdown(false);
    };

    const handleMetricChange = (metricValue: string) => {
        setSelectedMetric(metricValue);
        setShowMetricDropdown(false);
    };

    const handleMetricDropdownToggle = () => {
        setShowMetricDropdown(!showMetricDropdown);
    };

    const handlePeriodChange = (newPeriod: TimePeriod) => {
        setOffset(0);
        if (onPeriodChange) {
            onPeriodChange(newPeriod);
        } else {
            setInternalPeriod(newPeriod);
        }
    };

    const handleNavigateBack = () => {
        setOffset(offset - 1);
    };

    const handleNavigateForward = () => {
        if (offset < 0) {
            setOffset(offset + 1);
        }
    };

    const handleToday = () => {
        setOffset(0);
    };

    const handleExerciseChange = (exerciseId: number | null) => {
        setSelectedExercise(exerciseId);

        if (exerciseId) {
            const exerciseData = availableExercises.find(ex => ex.id === exerciseId);
            if (exerciseData) {
                let newMetrics: MetricOption[] = [];

                if (exerciseData.trackingMode === 'REP_BASED') {
                    newMetrics = [
                        {value: 'WEIGHT', label: 'Max Weight', unit: weightUnit, icon: '🏋️'},
                        {value: 'VOLUME', label: 'Total Volume', unit: weightUnit, icon: '📊'},
                        {value: 'REPS', label: 'Total Reps', unit: 'reps', icon: '🔢'},
                        {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'}
                    ];
                } else if (exerciseData.trackingMode === 'TIME_BASED') {
                    newMetrics = [
                        {value: 'DISTANCE', label: 'Distance', unit: distanceUnit, icon: '🏃'},
                        {value: 'PACE', label: 'Pace', unit: `min/${distanceUnit}`, icon: '⏱️'},
                        {value: 'SPEED', label: 'Speed', unit: `${distanceUnit}/hr`, icon: '⚡'},
                        {value: 'CALORIES', label: 'Calories', unit: 'cal', icon: '🔥'}
                    ];
                } else if (exerciseData.trackingMode === 'HOLD_BASED') {
                    newMetrics = [
                        {value: 'HOLD_DURATION', label: 'Avg Hold Time', unit: 'seconds', icon: '⏱️'},
                        {value: 'TOTAL_HOLD_TIME', label: 'Total Hold Time', unit: 'seconds', icon: '⏳'},
                        {value: 'MAX_HOLD', label: 'Max Hold', unit: 'seconds', icon: '🏆'},
                        {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'}
                    ];
                }

                const isCurrentMetricValid = newMetrics.some(m => m.value === selectedMetric);
                if (!isCurrentMetricValid && newMetrics.length > 0) {
                    setSelectedMetric(newMetrics[0].value);
                }
            }
        } else {
            setSelectedMetric('VOLUME');
        }
    };

    const {startDate, endDate} = getDateRange(period, offset);
    const periodLabel = getPeriodLabel(period, offset, season?.seasonName || 'Season', theme?.emoji || '🌟');
    const dateRangeLabel = formatDateRange(startDate, endDate);

    if (loading) {
        return (
            <div
                className={`relative max-w-5xl mx-auto bg-gradient-to-br ${theme.gradient} rounded-3xl shadow-2xl overflow-hidden border ${theme.border}`}>
                <div className="p-8 animate-pulse">
                    <div className={`h-8 ${theme.accentBg} bg-opacity-20 rounded-xl w-48 mb-6`}></div>
                    <div className="h-[45vh] bg-white/50 rounded-2xl"></div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div
                className={`relative max-w-5xl mx-auto bg-gradient-to-br ${theme.gradient} rounded-3xl shadow-2xl overflow-hidden border ${theme.border}`}>
                <div className="p-8">
                    <div className="text-center">
                        <div
                            className={`w-16 h-16 ${theme.accentBg} bg-opacity-20 rounded-2xl flex items-center justify-center mx-auto mb-4`}>
                            <span className="text-3xl">⚠️</span>
                        </div>
                        <p className={`font-bold text-lg mb-2 ${theme.textPrimary}`}>Error Loading Data</p>
                        <p className={`text-sm mb-6 ${theme.textSecondary}`}>{error}</p>
                        <button
                            onClick={() => loadChartData()}
                            className={`px-6 py-3 bg-gradient-to-r ${theme.buttonGradient} text-white rounded-xl font-bold shadow-lg hover:shadow-xl transition-all transform hover:scale-105`}
                        >
                            Retry
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="relative max-w-5xl mx-auto">
            {/* 🎨 Seasonal Floating Orbs */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none rounded-3xl -z-10">
                <div
                    className={`absolute -top-24 -right-24 w-96 h-96 ${theme.orb1} rounded-full blur-3xl opacity-40 animate-pulse`}></div>
                <div
                    className={`absolute -bottom-24 -left-24 w-96 h-96 ${theme.orb2} rounded-full blur-3xl opacity-30`}></div>
            </div>

            {/* 🎯 Main Card */}
            <div
                className={`relative bg-gradient-to-br ${theme.gradient} rounded-3xl shadow-2xl overflow-hidden border-2 ${theme.border}`}>

                {/* ✨ Header Section - Mobile-First Compact */}
                <div className="relative overflow-hidden">
                    <div className="absolute inset-0 bg-gradient-to-r from-white/60 via-white/40 to-transparent"></div>

                    <div className="relative px-3 md:px-6 py-3 md:py-4">
                        {/* Mode Tabs - Compact Mobile */}
                        <div className="flex gap-2 mb-3 md:mb-4">
                            <button
                                onClick={() => handleModeChange('EXERCISE')}
                                className={`flex-1 py-2.5 md:py-3 px-3 md:px-4 font-bold text-xs md:text-sm rounded-xl md:rounded-2xl transition-all ${
                                    mode === 'EXERCISE'
                                        ? `bg-gradient-to-r ${theme.buttonGradient} text-white shadow-lg`
                                        : `bg-white/70 backdrop-blur-sm ${theme.textSecondary} hover:bg-white hover:shadow-md`
                                }`}
                            >
                                <div className="flex items-center justify-center gap-1.5 md:gap-2">
                                    <TrendingUp className="w-3.5 h-3.5 md:w-4 md:h-4"/>
                                    <span className="hidden sm:inline">Exercise</span>
                                    <span className="sm:hidden">Exercise Metrics</span>
                                </div>
                            </button>
                            <button
                                onClick={() => handleModeChange('WORKOUT')}
                                className={`flex-1 py-2.5 md:py-3 px-3 md:px-4 font-bold text-xs md:text-sm rounded-xl md:rounded-2xl transition-all ${
                                    mode === 'WORKOUT'
                                        ? `bg-gradient-to-r ${theme.buttonGradient} text-white shadow-lg`
                                        : `bg-white/70 backdrop-blur-sm ${theme.textSecondary} hover:bg-white hover:shadow-md`
                                }`}
                            >
                                <div className="flex items-center justify-center gap-1.5 md:gap-2">
                                    <BarChart3 className="w-3.5 h-3.5 md:w-4 md:h-4"/>
                                    <span className="hidden sm:inline">Workout</span>
                                    <span className="sm:hidden">Workout Metrics</span>
                                </div>
                            </button>
                        </div>

                        {/* Period Navigation - Compact */}
                        <div className="flex items-center justify-between gap-2">
                            <button
                                onClick={handleNavigateBack}
                                className="p-2 md:p-2.5 hover:bg-white/60 rounded-lg md:rounded-xl transition-all active:scale-95"
                            >
                                <ChevronLeft className={`w-4 h-4 md:w-5 md:h-5 ${theme.textPrimary}`}/>
                            </button>

                            <div className="text-center flex-1 min-w-0">
                                <div className="flex items-center justify-center gap-1.5 md:gap-2 mb-0.5">
                                    <span className="text-lg md:text-2xl flex-shrink-0">{theme.emoji}</span>
                                    <h3 className={`text-sm md:text-lg font-black ${theme.textPrimary} truncate`}>
                                        {periodLabel}
                                    </h3>
                                </div>
                                <p className={`text-[10px] md:text-sm font-semibold ${theme.textSecondary}`}>
                                    {dateRangeLabel}
                                </p>
                            </div>

                            <button
                                onClick={handleNavigateForward}
                                disabled={offset >= 0}
                                className={`p-2 md:p-2.5 rounded-lg md:rounded-xl transition-all active:scale-95 ${
                                    offset >= 0
                                        ? 'text-gray-300 cursor-not-allowed'
                                        : `hover:bg-white/60 ${theme.textPrimary}`
                                }`}
                            >
                                <ChevronRight className="w-4 h-4 md:w-5 md:h-5"/>
                            </button>
                        </div>

                        {offset !== 0 && (
                            <div className="flex justify-center mt-2 md:mt-3">
                                <button
                                    onClick={handleToday}
                                    className={`px-4 py-1.5 md:px-5 md:py-2 text-xs md:text-sm font-bold rounded-full bg-gradient-to-r ${theme.buttonGradient} text-white shadow-lg hover:shadow-xl transition-all active:scale-95`}
                                >
                                    <div className="flex items-center gap-1.5 md:gap-2">
                                        <Sparkles className="w-3.5 h-3.5 md:w-4 md:h-4"/>
                                        <span className="hidden sm:inline">Back to Current</span>
                                        <span className="sm:hidden">Current</span>
                                    </div>
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                {/* 🎮 Controls Section - Compact Mobile */}
                <div className="px-3 md:px-6 py-3 md:py-4 bg-white/40 backdrop-blur-sm border-y border-white/50">
                    <div className="space-y-3 md:space-y-0 md:grid md:grid-cols-2 md:gap-4 mb-3 md:mb-4">
                        {/* Exercise Selector */}
                        {mode === 'EXERCISE' && availableExercises.length > 0 && (
                            <div className="relative dropdown-container">
                                <label
                                    className={`block text-[10px] md:text-xs font-bold ${theme.textSecondary} mb-1.5 md:mb-2 uppercase tracking-wide`}>
                                    💪 Exercise
                                </label>
                                <select
                                    value={selectedExercise || ''}
                                    onChange={(e) => handleExerciseChange(e.target.value ? Number(e.target.value) : null)}
                                    className={`w-full px-3 py-2.5 md:px-4 md:py-3 bg-white/90 backdrop-blur-sm border-2 ${theme.border} rounded-lg md:rounded-xl text-xs md:text-sm font-bold ${theme.textPrimary} hover:bg-white hover:shadow-lg transition-all focus:outline-none focus:ring-2 focus:ring-offset-2`}
                                >
                                    <option value="">All Exercises</option>
                                    {availableExercises.map(exercise => (
                                        <option key={exercise.id} value={exercise.id}>
                                            {exercise.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}

                        {/* Metric Selector */}
                        <div className="relative dropdown-container">
                            <label
                                className={`block text-[10px] md:text-xs font-bold ${theme.textSecondary} mb-1.5 md:mb-2 uppercase tracking-wide`}>
                                📊 Metric
                            </label>
                            <button
                                onClick={handleMetricDropdownToggle}
                                className={`w-full px-3 py-2.5 md:px-4 md:py-3 bg-white/90 backdrop-blur-sm border-2 ${theme.border} rounded-lg md:rounded-xl flex items-center justify-between hover:bg-white hover:shadow-lg transition-all text-xs md:text-sm font-bold ${theme.textPrimary}`}
                            >
                                <div className="flex items-center gap-2 md:gap-3">
                                    <span className="text-base md:text-lg">{currentMetric?.icon}</span>
                                    <span>{currentMetric?.label}</span>
                                </div>
                                <ChevronDown
                                    className={`w-4 h-4 md:w-5 md:h-5 transition-transform ${showMetricDropdown ? 'rotate-180' : ''}`}/>
                            </button>

                            {showMetricDropdown && (
                                <div
                                    className="absolute top-full left-0 right-0 mt-2 bg-white/95 backdrop-blur-md border-2 border-gray-200 rounded-xl md:rounded-2xl shadow-2xl z-50 max-h-64 md:max-h-72 overflow-y-auto">
                                    {availableMetrics.map((metric) => (
                                        <button
                                            key={metric.value}
                                            onClick={() => handleMetricChange(metric.value)}
                                            className={`w-full px-3 py-2.5 md:px-4 md:py-3 text-left transition-all flex items-center gap-2 md:gap-3 text-xs md:text-sm font-bold border-b border-gray-100 last:border-b-0 ${
                                                selectedMetric === metric.value
                                                    ? `${theme.messageBg} ${theme.textPrimary}`
                                                    : `text-gray-700 hover:${theme.messageBg}`
                                            }`}
                                        >
                                            <span className="text-lg md:text-xl">{metric.icon}</span>
                                            <span className="flex-1">{metric.label}</span>
                                            <span
                                                className={`text-[10px] md:text-xs px-1.5 py-0.5 md:px-2 md:py-1 rounded-lg ${theme.cardBg} ${theme.textTertiary}`}>
                                                {metric.unit}
                                            </span>
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Chart Type Toggle - Compact */}
                    <div className="flex justify-end">
                        <div
                            className="inline-flex gap-1.5 md:gap-2 bg-white/80 backdrop-blur-sm rounded-lg md:rounded-xl p-1 md:p-1.5 border-2 border-white/50 shadow-md">
                            <button
                                onClick={() => setChartType('LINE')}
                                className={`p-2 md:p-2.5 rounded-md md:rounded-lg transition-all ${
                                    chartType === 'LINE'
                                        ? `${theme.accentBg} bg-opacity-20 shadow-md`
                                        : 'hover:bg-white/60'
                                }`}
                                title="Line Chart"
                            >
                                <Activity
                                    className={`w-4 h-4 md:w-5 md:h-5 ${chartType === 'LINE' ? theme.textPrimary : 'text-gray-600'}`}/>
                            </button>
                            <button
                                onClick={() => setChartType('BAR')}
                                className={`p-2 md:p-2.5 rounded-md md:rounded-lg transition-all ${
                                    chartType === 'BAR'
                                        ? `${theme.accentBg} bg-opacity-20 shadow-md`
                                        : 'hover:bg-white/60'
                                }`}
                                title="Bar Chart"
                            >
                                <BarChart3
                                    className={`w-4 h-4 md:w-5 md:h-5 ${chartType === 'BAR' ? theme.textPrimary : 'text-gray-600'}`}/>
                            </button>
                        </div>
                    </div>
                </div>

                {/* 📊 Chart Section */}
                <div className="w-full h-[45vh] bg-white/30 backdrop-blur-sm p-4">
                    <ResponsiveContainer width="100%" height="100%">
                        {chartType === 'LINE' ? (
                            <LineChart data={dataPoints} margin={{top: 10, right: 10, left: -35, bottom: 5}}>
                                <defs>
                                    <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor={theme.chartColor} stopOpacity={0.3}/>
                                        <stop offset="95%" stopColor={theme.chartColor} stopOpacity={0}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false} opacity={0.5}/>
                                <XAxis
                                    dataKey="label"
                                    stroke="#9ca3af"
                                    style={{fontSize: '12px', fontWeight: 'bold'}}
                                    tick={{fill: '#6b7280'}}
                                    tickLine={false}
                                    axisLine={false}
                                />
                                <YAxis
                                    stroke="#9ca3af"
                                    style={{fontSize: '12px', fontWeight: 'bold'}}
                                    tick={{fill: '#6b7280'}}
                                    tickLine={false}
                                    axisLine={false}
                                    tickFormatter={(value) => formatMetricValue(value, selectedMetric)}
                                />
                                <Tooltip content={(props) => <CustomTooltip {...props} metric={currentMetric}
                                                                            theme={theme}/>}/>
                                <Line
                                    type="monotone"
                                    dataKey="displayValue"
                                    stroke={theme.chartColor}
                                    strokeWidth={3}
                                    dot={(props: any) => (
                                        <circle
                                            cx={props.cx}
                                            cy={props.cy}
                                            r={5}
                                            fill={theme.chartColor}
                                            stroke="white"
                                            strokeWidth={2}
                                            className="drop-shadow-lg"
                                        />
                                    )}
                                    activeDot={{r: 7, fill: theme.chartColor, stroke: 'white', strokeWidth: 3}}
                                    animationDuration={1000}
                                />
                            </LineChart>
                        ) : (
                            <BarChart data={dataPoints} margin={{top: 10, right: 10, left: -35, bottom: 5}}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false} opacity={0.5}/>
                                <XAxis
                                    dataKey="label"
                                    stroke="#9ca3af"
                                    style={{fontSize: '12px', fontWeight: 'bold'}}
                                    tick={{fill: '#6b7280'}}
                                    tickLine={false}
                                    axisLine={false}
                                />
                                <YAxis
                                    stroke="#9ca3af"
                                    style={{fontSize: '12px', fontWeight: 'bold'}}
                                    tick={{fill: '#6b7280'}}
                                    tickLine={false}
                                    axisLine={false}
                                    tickFormatter={(value) => formatMetricValue(value, selectedMetric)}
                                />
                                <Tooltip content={(props) => <CustomTooltip {...props} metric={currentMetric}
                                                                            theme={theme}/>}/>
                                <Bar
                                    dataKey="displayValue"
                                    fill={theme.chartColor}
                                    radius={[8, 8, 0, 0]}
                                    animationDuration={1000}
                                >
                                    {dataPoints.map((entry, index) => (
                                        <Cell
                                            key={`cell-${index}`}
                                            className="drop-shadow-md hover:drop-shadow-xl transition-all"
                                        />
                                    ))}
                                </Bar>
                            </BarChart>
                        )}
                    </ResponsiveContainer>
                </div>

                {/* 🎯 Time Period Pills - Mobile First */}
                <div className="px-4 md:px-6 py-3 bg-white/40 backdrop-blur-sm border-t border-white/50">
                    <div className="flex items-center justify-between md:justify-start md:gap-2">
                        {(['WEEK', 'MONTH', 'SEASON', 'YEAR'] as TimePeriod[]).map((p) => (
                            <button
                                key={p}
                                onClick={() => handlePeriodChange(p)}
                                className={`px-3 py-2 md:px-5 md:py-2.5 rounded-full font-bold text-xs md:text-sm whitespace-nowrap transition-all transform flex-shrink-0 flex items-center gap-1 ${
                                    period === p
                                        ? `bg-gradient-to-r ${theme.buttonGradient} text-white shadow-lg`
                                        : `bg-white/70 backdrop-blur-sm ${theme.textSecondary} hover:bg-white hover:shadow-md`
                                }`}
                            >
                                {p === 'SEASON' && <span className="text-sm md:text-base">{theme.emoji}</span>}
                                <span className="md:hidden">
                                    {p === 'WEEK' ? '1W' : p === 'MONTH' ? '1M' : p === 'SEASON' ? 'Season' : '1Y'}
                                </span>
                                <span className="hidden md:inline">
                                    {p === 'WEEK' ? 'Week' : p === 'MONTH' ? 'Month' : p === 'SEASON' ? 'Season' : 'Year'}
                                </span>
                            </button>
                        ))}
                    </div>
                </div>

                {/* 📈 Ultra-Compact Summary Stats */}
                {summary && (
                    <div className="px-4 py-3 bg-white/50 backdrop-blur-sm border-t border-white/50">
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                            <div
                                className={`text-center p-2 rounded-lg ${theme.cardBg} backdrop-blur-sm border ${theme.cardBorder} hover:scale-105 transition-all`}>
                                <p className={`text-[9px] font-bold ${theme.textSecondary} mb-0.5 uppercase tracking-wider`}>
                                    Avg
                                </p>
                                <p className={`text-base font-black ${theme.textPrimary} tabular-nums leading-tight`}>
                                    {formatMetricValue(summary.average, selectedMetric)}
                                </p>
                                <p className={`text-[9px] font-semibold ${theme.textTertiary}`}>{currentMetric?.unit}</p>
                            </div>

                            <div
                                className={`text-center p-2 rounded-lg ${theme.cardBg} backdrop-blur-sm border ${theme.cardBorder} hover:scale-105 transition-all`}>
                                <p className={`text-[9px] font-bold ${theme.textSecondary} mb-0.5 uppercase tracking-wider`}>
                                    Peak
                                </p>
                                <p className={`text-base font-black ${theme.textPrimary} tabular-nums leading-tight`}>
                                    {formatMetricValue(summary.peak, selectedMetric)}
                                </p>
                                <p className={`text-[9px] font-semibold ${theme.textTertiary}`}>{currentMetric?.unit}</p>
                            </div>

                            <div
                                className={`text-center p-2 rounded-lg ${theme.cardBg} backdrop-blur-sm border ${theme.cardBorder} hover:scale-105 transition-all`}>
                                <p className={`text-[9px] font-bold ${theme.textSecondary} mb-0.5 uppercase tracking-wider`}>
                                    Low
                                </p>
                                <p className={`text-base font-black ${theme.textPrimary} tabular-nums leading-tight`}>
                                    {formatMetricValue(summary.low, selectedMetric)}
                                </p>
                                <p className={`text-[9px] font-semibold ${theme.textTertiary}`}>{currentMetric?.unit}</p>
                            </div>

                            <div
                                className={`text-center p-2 rounded-lg ${theme.cardBg} backdrop-blur-sm border ${theme.cardBorder} hover:scale-105 transition-all`}>
                                <p className={`text-[9px] font-bold ${theme.textSecondary} mb-0.5 uppercase tracking-wider`}>
                                    Trend
                                </p>
                                <div className="flex items-center justify-center min-h-[20px]">
                                    {summary.trend === 'UP' ? (
                                        <p className="text-base font-black text-green-600 tabular-nums leading-tight">
                                            ↑{Math.abs(summary.trendPercentage).toFixed(0)}%
                                        </p>
                                    ) : summary.trend === 'DOWN' ? (
                                        <p className="text-base font-black text-red-600 tabular-nums leading-tight">
                                            ↓{Math.abs(summary.trendPercentage).toFixed(0)}%
                                        </p>
                                    ) : (
                                        <p className="text-base font-black text-gray-600 tabular-nums leading-tight">
                                            →{Math.abs(summary.trendPercentage).toFixed(0)}%
                                        </p>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

// ==================== CUSTOM TOOLTIP ====================

interface CustomTooltipProps {
    active?: boolean;
    payload?: Array<{ payload: ChartDataPoint; value: number; }>;
    label?: string | number;
    metric?: MetricOption;
    theme?: any;
}

const CustomTooltip: React.FC<CustomTooltipProps> = ({active, payload, label, metric, theme}) => {
    if (!active || !payload || !payload[0]) return null;
    const data = payload[0].payload;

    const fullDate = new Date(data.date).toLocaleDateString('en-US', {
        weekday: 'short',
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });

    const displayValue = isIntegerMetric(metric?.value || '')
        ? Math.round(data.displayValue).toString()
        : data.displayValue.toFixed(1);

    return (
        <div
            className={`bg-white/95 backdrop-blur-md border-2 ${theme?.border || 'border-gray-200'} rounded-2xl shadow-2xl p-4 min-w-[200px]`}>
            <p className={`text-xs font-bold ${theme?.textSecondary || 'text-gray-600'} mb-3 uppercase tracking-wide`}>
                {fullDate}
            </p>
            <div className="space-y-2">
                <div className={`flex items-center gap-3 p-3 rounded-xl ${theme?.cardBg || 'bg-gray-100'}`}>
                    <span className="text-2xl">{metric?.icon}</span>
                    <div>
                        <p className={`text-xs font-semibold ${theme?.textTertiary || 'text-gray-600'} mb-0.5`}>
                            {metric?.label}
                        </p>
                        <p className={`text-lg font-black ${theme?.textPrimary || 'text-gray-900'}`}>
                            {displayValue} {metric?.unit}
                        </p>
                    </div>
                </div>
                {data.workoutCount && (
                    <div className="flex items-center gap-2 pt-2 border-t border-gray-200">
                        <span className="text-sm">💪</span>
                        <p className={`text-sm font-semibold ${theme?.textSecondary || 'text-gray-600'}`}>
                            {data.workoutCount} workout{data.workoutCount > 1 ? 's' : ''}
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};