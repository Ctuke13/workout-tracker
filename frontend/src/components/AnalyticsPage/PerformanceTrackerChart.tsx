import React, {useState, useEffect} from 'react';
import {TrendingUp, BarChart3, Activity, ChevronDown, ChevronLeft, ChevronRight} from 'lucide-react';
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

// Import your contexts
// import {useSeason} from '../../contexts/SeasonContext';
// import {useUserPreferences} from '../../contexts/UserPreferencesContext';

// ==================== TYPES ====================

type ChartMode = 'EXERCISE' | 'WORKOUT';
type ChartType = 'LINE' | 'BAR';

interface Exercise {
    id: number;
    name: string;
    emoji: string;
    trackingMode: 'REP_BASED' | 'TIME_BASED' | 'HOLD_BASED';
    usageCount: number;
}

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

// ==================== MOCK DATA ====================


// Mock season data (will be replaced by your SeasonContext)
const MOCK_SEASON = {
    seasonName: 'Fall 2025',
    emoji: '🍂',
    startDate: '2025-09-21',
    endDate: '2025-12-20',
    color: '#fb923c'
};

const MOCK_THEME = {
    emoji: '🍂',
    buttonGradient: 'from-amber-600 to-orange-600',
    textPrimary: 'text-amber-900',
    accentBg: 'bg-amber-500'
};

// Helper functions
const getBaseValueForMetric = (metric: string): number => {
    const baseValues: Record<string, number> = {
        'MAX_WEIGHT': 200,
        'TOTAL_VOLUME': 15000,
        'AVG_WEIGHT': 180,
        'ESTIMATED_1RM': 225,
        'TOTAL_REPS': 24,
        'TOTAL_DISTANCE': 5,
        'BEST_PACE': 7,
        'AVG_PACE': 8,
        'TOTAL_DURATION': 45,
        'CALORIES': 400,
        'MAX_HOLD': 60,
        'TOTAL_HOLD': 180,
        'AVG_HOLD': 55,
        'WORKOUT_COUNT': 4,
        'WORKOUT_DURATION': 60
    };
    return baseValues[metric] || 100;
};

const isIntegerMetric = (metric: string): boolean => {
    const integerMetrics = [
        'TOTAL_REPS', 'WORKOUT_COUNT', 'CALORIES',
        'MAX_HOLD', 'TOTAL_HOLD', 'AVG_HOLD',
        'TOTAL_DURATION', 'WORKOUT_DURATION'
    ];
    return integerMetrics.includes(metric);
};

const formatMetricValue = (value: number, metric: string): string => {
    if (isIntegerMetric(metric)) {
        return Math.round(value).toString();
    }
    return value.toFixed(1);
};

// Date calculation helpers
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
        // Mock season calculation (will use real SeasonContext)
        const seasonStart = new Date(MOCK_SEASON.startDate);
        const seasonEnd = new Date(MOCK_SEASON.endDate);

        // For now, just return current season
        // In real implementation, calculate based on offset
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

const getPeriodLabel = (period: TimePeriod, offset: number): string => {
    if (offset === 0) {
        if (period === 'WEEK') return 'This Week';
        if (period === 'MONTH') {
            const month = new Date().toLocaleDateString('en-US', {month: 'long', year: 'numeric'});
            return month;
        }
        if (period === 'SEASON') return `${MOCK_SEASON.emoji} ${MOCK_SEASON.seasonName}`;
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

const getStartOfWeek = (date: Date): Date => {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    d.setDate(diff);
    d.setHours(0, 0, 0, 0);
    return d;
};

const getEndOfWeek = (date: Date): Date => {
    const start = getStartOfWeek(date);
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    end.setHours(23, 59, 59, 999);
    return end;
};

const getDateLabelsForRange = (period: TimePeriod, startDate: Date, endDate: Date) => {
    const labels: { date: string; label: string }[] = [];

    if (period === 'WEEK') {
        for (let i = 0; i < 7; i++) {
            const date = new Date(startDate);
            date.setDate(startDate.getDate() + i);
            labels.push({
                date: date.toISOString().split('T')[0],
                label: date.toLocaleDateString('en-US', {weekday: 'short'})
            });
        }
    } else if (period === 'MONTH') {
        // Show weekly aggregates (4-5 points)
        const weeks = [];
        let current = new Date(startDate);
        while (current <= endDate) {
            weeks.push(new Date(current));
            current.setDate(current.getDate() + 7);
        }
        weeks.forEach(weekStart => {
            labels.push({
                date: weekStart.toISOString().split('T')[0],
                label: weekStart.toLocaleDateString('en-US', {month: 'short', day: 'numeric'})
            });
        });
    } else if (period === 'SEASON') {
        // Show weekly points (~12 points for 3 months)
        const weeks = [];
        let current = new Date(startDate);
        while (current <= endDate) {
            weeks.push(new Date(current));
            current.setDate(current.getDate() + 7);
        }
        weeks.forEach(weekStart => {
            labels.push({
                date: weekStart.toISOString().split('T')[0],
                label: weekStart.toLocaleDateString('en-US', {month: 'short', day: 'numeric'})
            });
        });
    } else if (period === 'YEAR') {
        for (let i = 0; i < 12; i++) {
            const date = new Date(startDate.getFullYear(), i, 1);
            labels.push({
                date: date.toISOString().split('T')[0],
                label: date.toLocaleDateString('en-US', {month: 'short'})
            });
        }
    }

    return labels;
};

// ==================== MAIN COMPONENT ====================

export default function PerformanceTrackerChart({
                                                    period: externalPeriod,
                                                    onPeriodChange
                                                }: PerformanceTrackerChartProps) {
    // Uncomment when integrating with your contexts:
    // const {theme, season} = useSeason();
    // const {distanceUnit, weightUnit} = useUserPreferences();

    // Mock theme/season (remove when using real contexts)
    const theme = MOCK_THEME;
    const season = MOCK_SEASON;
    const weightUnit = 'lbs';
    const distanceUnit = 'miles';

    const [mode, setMode] = useState<ChartMode>('EXERCISE');
    const [availableExercises, setAvailableExercises] = useState<{
        id: number;
        name: string;
        trackingMode: 'REP_BASED' | 'TIME_BASED' | 'HOLD_BASED';
    }[]>([]);
    const [selectedExercise, setSelectedExercise] = useState<number | null>(null);
    const [selectedMetric, setSelectedMetric] = useState<string>(() => {
        // Initialize with first metric from WORKOUT mode (most common)
        return 'VOLUME'; // Safe default that works for both modes
    });
    const [chartType, setChartType] = useState<ChartType>('LINE');
    const [internalPeriod, setInternalPeriod] = useState<TimePeriod>('MONTH');
    const [offset, setOffset] = useState(0);
    const [error, setError] = useState<string | null>(null);

    const [showExerciseDropdown, setShowExerciseDropdown] = useState(false);
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
                setShowExerciseDropdown(false);
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

        // Get selected exercise details
        const selectedExerciseData = availableExercises.find(e => e.id === selectedExercise);

        // ✅ ADD DEBUGGING
        console.log('🔍 Selected Exercise:', selectedExercise);
        console.log('🔍 Exercise Data:', selectedExerciseData);
        console.log('🔍 Tracking Mode:', selectedExerciseData?.trackingMode);
        console.log('🔍 Tracking Mode Type:', typeof selectedExerciseData?.trackingMode);

        // If no exercise selected, show all metrics
        if (!selectedExercise || !selectedExerciseData) {
            console.log('📊 Returning ALL metrics (no exercise selected)');
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

        // Filter metrics based on tracking mode
        const trackingMode = selectedExerciseData.trackingMode;

        if (trackingMode === 'REP_BASED') {
            console.log('💪 Returning REP_BASED metrics');
            return [
                {value: 'WEIGHT', label: 'Max Weight', unit: weightUnit, icon: '🏋️'},
                {value: 'VOLUME', label: 'Total Volume', unit: weightUnit, icon: '📊'},
                {value: 'REPS', label: 'Total Reps', unit: 'reps', icon: '🔢'},
                {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'}
            ];
        }

        if (trackingMode === 'TIME_BASED') {
            console.log('🏃 Returning TIME_BASED metrics');
            return [
                {value: 'DISTANCE', label: 'Distance', unit: distanceUnit, icon: '🏃'},
                {value: 'PACE', label: 'Pace', unit: `min/${distanceUnit}`, icon: '⏱️'},
                {value: 'SPEED', label: 'Speed', unit: `${distanceUnit}/hr`, icon: '⚡'},
                {value: 'CALORIES', label: 'Calories', unit: 'cal', icon: '🔥'}
            ];
        }

        if (trackingMode === 'HOLD_BASED') {
            console.log('🧘 Returning HOLD_BASED metrics');
            return [
                {value: 'HOLD_DURATION', label: 'Avg Hold Time', unit: 'seconds', icon: '⏱️'},
                {value: 'TOTAL_HOLD_TIME', label: 'Total Hold Time', unit: 'seconds', icon: '⏳'},
                {value: 'MAX_HOLD', label: 'Max Hold', unit: 'seconds', icon: '🏆'},
                {value: 'SETS', label: 'Total Sets', unit: 'sets', icon: '📋'}
            ];
        }

        // Default fallback
        console.log('⚠️ FALLING BACK TO DEFAULT - trackingMode did not match!');
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
        // ✅ Don't load if metrics aren't ready
        const metrics = getAvailableMetrics();
        if (metrics.length === 0) {
            console.log('⏳ Metrics not ready yet, skipping load');
            return;
        }

        // ✅ Ensure selected metric is valid
        const validMetric = metrics.find(m => m.value === selectedMetric);
        if (!validMetric) {
            console.log('⚠️ Invalid metric selected, using first available:', metrics[0].value);
            setSelectedMetric(metrics[0].value);
            return; // Will trigger re-load via useEffect
        }

        setLoading(true);
        setError(null);

        try {
            const currentPeriod = externalPeriod || internalPeriod;

            // ✅ Call real API
            const response = await analyticsApi.getPerformanceTrackerData(
                selectedMetric,
                mapPeriodToBackend(currentPeriod),
                selectedExercise
            );

            // Transform API data to chart format
            const chartData: ChartDataPoint[] = response.dataPoints.map(point => ({
                date: point.date,
                value: point.value,
                displayValue: point.value,
                label: formatChartDate(point.date, currentPeriod),
                workoutCount: point.workoutCount,
                isCurrentPeriod: false
            }));

            setDataPoints(chartData);

            // Set summary from API
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
        setShowExerciseDropdown(false);
    };

    const handlePeriodChange = (newPeriod: TimePeriod) => {
        setOffset(0); // Reset to current period
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

    const handleExerciseChange = (exerciseId: number | null) => {
        setSelectedExercise(exerciseId);

        // ✅ Reset metric to first valid one for this exercise type
        if (exerciseId) {
            const exerciseData = availableExercises.find(ex => ex.id === exerciseId);
            if (exerciseData) {
                // Get metrics for this exercise type
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

                // Check if current metric is valid for new exercise
                const isCurrentMetricValid = newMetrics.some(m => m.value === selectedMetric);
                if (!isCurrentMetricValid && newMetrics.length > 0) {
                    console.log(`🔄 Exercise changed to ${exerciseData.trackingMode}, resetting metric from ${selectedMetric} to ${newMetrics[0].value}`);
                    setSelectedMetric(newMetrics[0].value);
                }
            }
        } else {
            // If "All Exercises" selected, reset to a universal metric
            console.log('🔄 Switched to "All Exercises", resetting metric to VOLUME');
            setSelectedMetric('VOLUME');
        }
    };

    // Get chart color from season theme
    const chartColor = season.color || '#60a5fa';

    const {startDate, endDate} = getDateRange(period, offset);
    const periodLabel = getPeriodLabel(period, offset);
    const dateRangeLabel = formatDateRange(startDate, endDate);

    if (loading) {
        return (
            <div className="bg-white rounded-lg shadow-lg overflow-hidden">
                <div className="p-4 animate-pulse">
                    <div className="h-6 bg-gray-200 rounded w-32 mb-4"></div>
                    <div className="h-[45vh] bg-gray-100 rounded"></div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-white rounded-lg shadow-lg overflow-hidden">
                <div className="p-4">
                    <div className="text-red-600 text-center">
                        <p className="font-bold mb-2">Error Loading Data</p>
                        <p className="text-sm">{error}</p>
                        <button
                            onClick={() => loadChartData()}
                            className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                        >
                            Retry
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg shadow-lg overflow-hidden max-w-4xl mx-auto">
            {/* Mode Tabs */}
            <div className="flex border-b border-gray-200">
                <button
                    onClick={() => handleModeChange('EXERCISE')}
                    className={`flex-1 py-3 px-4 font-bold text-sm transition-all ${
                        mode === 'EXERCISE'
                            ? `border-b-2 border-${season.color?.replace('#', '')} ${theme.textPrimary} bg-gray-50`
                            : 'text-gray-600 hover:bg-gray-50'
                    }`}
                >
                    📈 Exercise Progress
                </button>
                <button
                    onClick={() => handleModeChange('WORKOUT')}
                    className={`flex-1 py-3 px-4 font-bold text-sm transition-all ${
                        mode === 'WORKOUT'
                            ? `border-b-2 border-${season.color?.replace('#', '')} ${theme.textPrimary} bg-gray-50`
                            : 'text-gray-600 hover:bg-gray-50'
                    }`}
                >
                    📊 Workout Stats
                </button>
            </div>

            {/* Period Navigation */}
            <div className="px-4 py-3 bg-gray-50 border-b border-gray-100">
                <div className="flex items-center justify-between mb-2">
                    <button
                        onClick={handleNavigateBack}
                        className="p-2 hover:bg-gray-200 rounded-lg transition"
                        aria-label="Previous period"
                    >
                        <ChevronLeft className="w-5 h-5 text-gray-700"/>
                    </button>

                    <div className="text-center flex-1">
                        <h3 className={`text-base font-black ${theme.textPrimary}`}>
                            {periodLabel}
                        </h3>
                        <p className="text-xs text-gray-600 font-medium mt-0.5">
                            {dateRangeLabel}
                        </p>
                    </div>

                    <button
                        onClick={handleNavigateForward}
                        disabled={offset >= 0}
                        className={`p-2 rounded-lg transition ${
                            offset >= 0
                                ? 'text-gray-300 cursor-not-allowed'
                                : 'hover:bg-gray-200 text-gray-700'
                        }`}
                        aria-label="Next period"
                    >
                        <ChevronRight className="w-5 h-5"/>
                    </button>
                </div>

                {offset !== 0 && (
                    <div className="flex justify-center">
                        <button
                            onClick={handleToday}
                            className={`px-3 py-1 text-xs font-bold rounded-full bg-gradient-to-r ${theme.buttonGradient} text-white hover:opacity-90 transition`}
                        >
                            Today
                        </button>
                    </div>
                )}
            </div>

            {/* Selectors */}
            <div className="px-4 py-3 bg-gray-50 border-b border-gray-100">
                <div className="space-y-2">
                    {mode === 'EXERCISE' && availableExercises.length > 0 && (
                        <div className="relative dropdown-container">
                            <label className="block text-xs font-semibold text-gray-600 mb-1">
                                Exercise
                            </label>
                            <select
                                value={selectedExercise || ''}
                                onChange={(e) => handleExerciseChange(e.target.value ? Number(e.target.value) : null)}
                                className="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg text-sm font-bold text-gray-900 hover:bg-gray-50 transition"
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

                    <div className="relative dropdown-container">
                        <label className="block text-xs font-semibold text-gray-600 mb-1">
                            Metric
                        </label>
                        <button
                            onClick={handleMetricDropdownToggle}
                            className="w-full px-3 py-2 bg-white border border-gray-200 rounded-lg flex items-center justify-between hover:bg-gray-50 transition text-sm font-bold text-gray-900"
                        >
                            <div className="flex items-center gap-2">
                                <span>{currentMetric?.icon}</span>
                                <span>{currentMetric?.label}</span>
                            </div>
                            <ChevronDown
                                className={`w-4 h-4 transition-transform ${showMetricDropdown ? 'rotate-180' : ''}`}/>
                        </button>

                        {showMetricDropdown && (
                            <div
                                className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-xl z-50 max-h-64 overflow-y-auto">
                                {availableMetrics.map((metric) => (
                                    <button
                                        key={metric.value}
                                        onClick={() => handleMetricChange(metric.value)}
                                        className={`w-full px-3 py-2 text-left hover:bg-gray-50 transition flex items-center gap-2 text-sm font-semibold ${
                                            selectedMetric === metric.value ? 'bg-blue-50 text-blue-600' : 'text-gray-700'
                                        }`}
                                    >
                                        <span>{metric.icon}</span>
                                        <span>{metric.label}</span>
                                        <span className="text-xs text-gray-500 ml-auto">({metric.unit})</span>
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                <div className="flex justify-end mt-3">
                    <div className="flex gap-1 bg-white rounded-lg p-1 border border-gray-200">
                        <button
                            onClick={() => setChartType('LINE')}
                            className={`p-1.5 rounded transition ${
                                chartType === 'LINE' ? theme.accentBg + ' bg-opacity-20' : 'hover:bg-gray-100'
                            }`}
                        >
                            <Activity
                                className={`w-4 h-4 ${chartType === 'LINE' ? theme.textPrimary : 'text-gray-600'}`}/>
                        </button>
                        <button
                            onClick={() => setChartType('BAR')}
                            className={`p-1.5 rounded transition ${
                                chartType === 'BAR' ? theme.accentBg + ' bg-opacity-20' : 'hover:bg-gray-100'
                            }`}
                        >
                            <BarChart3
                                className={`w-4 h-4 ${chartType === 'BAR' ? theme.textPrimary : 'text-gray-600'}`}/>
                        </button>
                    </div>
                </div>
            </div>

            {/* Chart */}
            <div className="w-full h-[45vh] bg-gray-50">
                <ResponsiveContainer width="100%" height="100%">
                    {chartType === 'LINE' ? (
                        <LineChart data={dataPoints} margin={{top: 20, right: -20, left: -20, bottom: 5}}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false}/>
                            <XAxis
                                dataKey="label"
                                stroke="#9ca3af"
                                style={{fontSize: '11px'}}
                                tick={{fill: '#6b7280'}}
                                tickLine={false}
                                axisLine={false}
                            />
                            <YAxis
                                stroke="#9ca3af"
                                style={{fontSize: '11px'}}
                                tick={{fill: '#6b7280'}}
                                tickLine={false}
                                axisLine={false}
                                tickFormatter={(value) => formatMetricValue(value, selectedMetric)}
                            />
                            <Tooltip content={(props) => <CustomTooltip {...props} metric={currentMetric}/>}/>
                            <Line
                                type="monotone"
                                dataKey="displayValue"
                                stroke={chartColor}
                                strokeWidth={2.5}
                                dot={(props: any) => {
                                    const isHighlighted = props.payload.isCurrentPeriod;
                                    return (
                                        <circle
                                            key={`dot-${props.index}`}
                                            cx={props.cx}
                                            cy={props.cy}
                                            r={isHighlighted ? 6 : 0}
                                            fill={chartColor}
                                            fillOpacity={isHighlighted ? 0.5 : 0}
                                            stroke={chartColor}
                                            strokeWidth={2}
                                        />
                                    );
                                }}
                                activeDot={{r: 5, fill: chartColor}}
                                animationDuration={800}
                            />
                        </LineChart>
                    ) : (
                        <BarChart data={dataPoints} margin={{top: 20, right: 10, left: -20, bottom: 5}}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" vertical={false}/>
                            <XAxis
                                dataKey="label"
                                stroke="#9ca3af"
                                style={{fontSize: '11px'}}
                                tick={{fill: '#6b7280'}}
                                tickLine={false}
                                axisLine={false}
                            />
                            <YAxis
                                stroke="#9ca3af"
                                style={{fontSize: '11px'}}
                                tick={{fill: '#6b7280'}}
                                tickLine={false}
                                axisLine={false}
                                tickFormatter={(value) => formatMetricValue(value, selectedMetric)}
                            />
                            <Tooltip content={(props) => <CustomTooltip {...props} metric={currentMetric}/>}/>
                            <Bar
                                dataKey="displayValue"
                                fill={chartColor}
                                radius={[6, 6, 0, 0]}
                                animationDuration={800}
                            >
                                {dataPoints.map((entry, index) => (
                                    <Cell
                                        key={`cell-${index}`}
                                        fillOpacity={entry.isCurrentPeriod ? 0.5 : 1}
                                    />
                                ))}
                            </Bar>
                        </BarChart>
                    )}
                </ResponsiveContainer>
            </div>

            {/* Time Period Pills */}
            <div className="px-4 py-3 border-t border-gray-100 bg-white">
                <div className="flex items-center gap-2 overflow-x-auto">
                    {(['WEEK', 'MONTH', 'SEASON', 'YEAR'] as TimePeriod[]).map((p) => (
                        <button
                            key={p}
                            onClick={() => handlePeriodChange(p)}
                            className={`px-4 py-2 rounded-full font-bold text-sm whitespace-nowrap transition-all flex-shrink-0 flex items-center gap-1 ${
                                period === p
                                    ? `bg-gradient-to-r ${theme.buttonGradient} text-white shadow-md`
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            {p === 'SEASON' && <span>{theme.emoji}</span>}
                            {p === 'WEEK' ? '1W' : p === 'MONTH' ? '1M' : p === 'SEASON' ? 'Season' : '1Y'}
                        </button>
                    ))}
                </div>
            </div>

            {/* Summary Stats */}
            {summary && (
                <div className="px-4 py-4 bg-gray-50 border-t border-gray-100">
                    <div className="grid grid-cols-3 gap-4">
                        <div className="text-center">
                            <p className="text-xs text-gray-600 font-semibold mb-1">Average</p>
                            <p className="text-lg font-black text-gray-900 tabular-nums">
                                {formatMetricValue(summary.average, selectedMetric)}
                            </p>
                            <p className="text-xs text-gray-500">{currentMetric?.unit}</p>
                        </div>
                        <div className="text-center">
                            <p className="text-xs text-gray-600 font-semibold mb-1">Peak</p>
                            <p className="text-lg font-black text-gray-900 tabular-nums">
                                {formatMetricValue(summary.peak, selectedMetric)}
                            </p>
                            <p className="text-xs text-gray-500">{currentMetric?.unit}</p>
                        </div>
                        <div className="text-center">
                            <p className="text-xs text-gray-600 font-semibold mb-1">Trend</p>
                            <p className={`text-lg font-black tabular-nums ${
                                summary.trend === 'UP' ? 'text-green-600' :
                                    summary.trend === 'DOWN' ? 'text-red-600' : 'text-gray-600'
                            }`}>
                                {summary.trend === 'UP' ? '↑' : summary.trend === 'DOWN' ? '↓' : '→'} {Math.abs(summary.trendPercentage).toFixed(1)}%
                            </p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

// ==================== CUSTOM TOOLTIP ====================

interface CustomTooltipProps {
    active?: boolean;
    payload?: Array<{ payload: ChartDataPoint; value: number; }>;
    label?: string | number;
    metric?: MetricOption;
}

const CustomTooltip: React.FC<CustomTooltipProps> = ({active, payload, label, metric}) => {
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
        <div className="bg-white border-2 border-gray-200 rounded-lg shadow-xl p-3 min-w-[180px]">
            <p className="text-xs font-bold text-gray-900 mb-2">{fullDate}</p>
            <div className="space-y-1">
                <p className="text-sm font-bold text-gray-900">
                    {displayValue} {metric?.unit}
                </p>
                <p className="text-xs text-gray-600">
                    {metric?.icon} {metric?.label}
                </p>
                {data.workoutCount && (
                    <p className="text-xs text-gray-500 mt-1.5 pt-1.5 border-t border-gray-100">
                        💪 {data.workoutCount} workout{data.workoutCount > 1 ? 's' : ''}
                    </p>
                )}
            </div>
        </div>
    );
};