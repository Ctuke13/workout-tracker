/**
 * Core Analytics UI Types
 * Shared types for UI components, not tied to backend API structure
 */

// ==================== TIME PERIOD TYPES ====================

export type TimePeriod = 'WEEK' | 'MONTH' | 'SEASON' | 'YEAR';

export interface PeriodProps {
    period: TimePeriod;
}

export interface PeriodChangeProps {
    period?: TimePeriod;
    onPeriodChange?: (period: TimePeriod) => void;
}

/**
 * Map frontend TimePeriod to backend API period
 * Handles the SEASON → YEAR mapping
 */
export const mapPeriodToBackend = (period: TimePeriod): string => {
    if (period === 'SEASON') {
        return 'YEAR'; // Backend uses YEAR for seasonal data
    }
    return period;
};

// ==================== CHART TYPES ====================

export type ChartMetric =
    | 'WEIGHT'           // Max weight lifted
    | 'VOLUME'           // Total volume (weight × reps)
    | 'REPS'             // Total reps
    | 'SETS'             // Total sets
    | 'DISTANCE'         // Distance (cardio)
    | 'PACE'             // Pace (cardio)
    | 'SPEED'            // Speed (cardio)
    | 'CALORIES'         // Calories burned
    | 'HOLD_DURATION'    // Average hold time (isometric)
    | 'TOTAL_HOLD_TIME'  // Total hold time (isometric)
    | 'MAX_HOLD';        // Max hold duration (isometric)

export type ChartType = 'LINE' | 'BAR';

export interface ChartDataPoint {
    date: string;           // ISO date string
    value: number;          // The metric value
    label: string;          // Display label (formatted)
    workoutCount?: number;  // Optional workout count for tooltip
}

export interface MetricOption {
    value: ChartMetric;
    label: string;
    unit: string;
    available: boolean;
    icon: string;
}

// ==================== PERFORMANCE TYPES ====================

export interface PerformanceData {
    metric: ChartMetric;
    dataPoints: ChartDataPoint[];
    average: number;
    peak: number;
    trend: 'UP' | 'DOWN' | 'STABLE';
    trendPercentage: number;
}

export interface WorkoutMetrics {
    hasStrengthData: boolean;
    hasCardioData: boolean;
    hasIsometricData: boolean;
    availableMetrics: ChartMetric[];
}

// ==================== WORKOUT TYPE BREAKDOWN TYPES ====================

export interface StrengthMetrics {
    totalVolume: number;      // In kg
    maxWeight: number;        // In kg
    totalReps: number;
    totalSets: number;
    averageWeight: number;    // In kg
}

export interface CardioMetrics {
    totalDistance: number;    // In km
    averagePace: number;      // Min per km
    topSpeed: number;         // Km/h
    totalCalories: number;
    averageHeartRate?: number;
}

export interface IsometricMetrics {
    totalHoldTime: number;    // In seconds
    longestHold: number;      // In seconds
    averageHoldTime: number;  // In seconds
    totalSessions: number;
}

export interface WorkoutTypeBreakdown {
    strength?: StrengthMetrics;
    cardio?: CardioMetrics;
    isometric?: IsometricMetrics;
}

// ==================== API RESPONSE TYPES ====================

export interface TimePeriodSummary {
    period: string;
    startDate: string;
    endDate: string;
    workouts: number;
    minutes: number;
    volume: number;
    workoutChange: number;
    minutesChange: number;
    volumeChange: number;
    averageMinutesPerWorkout?: number;
}

export interface AllPeriodSummaries {
    week: TimePeriodSummary;
    month: TimePeriodSummary;
    year: TimePeriodSummary;
    allTime: TimePeriodSummary;
}

export interface PersonalRecord {
    type: string;
    exerciseName: string;
    exerciseId: number;
    value: number;
    reps?: number;
    weight?: number;
    date: string;
    unit: string;
}

export interface TopExercise {
    exerciseId: number;
    exerciseName: string;
    trackingMode: 'REP_BASED' | 'TIME_BASED' | 'HOLD_BASED';
    count: number;
    volume: number;
}

export interface ExerciseProgressionPoint {
    date: string;
    weight: number;
    reps: number;
    volume: number;
    setNumber: number;
}

export interface PerformanceTrackerResponse {
    metric: ChartMetric;
    period: TimePeriod;
    dataPoints: Array<{
        date: string;
        value: number;
        workoutCount: number;
    }>;
    summary: {
        average: number;
        peak: number;
        low: number;
        trend: 'UP' | 'DOWN' | 'STABLE';
        trendPercentage: number;
        totalDataPoints: number;
    };
}
