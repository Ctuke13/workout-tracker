/**
 * Analytics Helper Functions
 * Pure utility functions for formatting and transforming analytics data
 */

import {ChartMetric, TimePeriod} from '../types/analytics';

// ==================== DATE FORMATTING ====================

/**
 * Format date for chart display based on time period
 */
export function formatChartDate(dateString: string, period: TimePeriod): string {
    const date = new Date(dateString);

    switch (period) {
        case 'WEEK':
            return date.toLocaleDateString('en-US', {weekday: 'short'}); // "Mon"
        case 'MONTH':
            return date.toLocaleDateString('en-US', {month: 'short', day: 'numeric'}); // "Jan 15"
        case 'SEASON':
            return date.toLocaleDateString('en-US', {month: 'short', day: 'numeric'}); // "Jan 15"
        case 'YEAR':
            return date.toLocaleDateString('en-US', {month: 'short'}); // "Jan"
        default:
            return dateString;
    }
}

// ==================== METRIC FORMATTING ====================

/**
 * Get display information for a metric
 */
export function getMetricInfo(metric: ChartMetric): { label: string; icon: string } {
    const metricMap: Record<ChartMetric, { label: string; icon: string }> = {
        WEIGHT: {label: 'Max Weight', icon: '🏋️'},
        VOLUME: {label: 'Total Volume', icon: '📊'},
        REPS: {label: 'Total Reps', icon: '🔢'},
        SETS: {label: 'Total Sets', icon: '📋'},
        DISTANCE: {label: 'Distance', icon: '🏃'},
        PACE: {label: 'Pace', icon: '⏱️'},
        SPEED: {label: 'Speed', icon: '⚡'},
        CALORIES: {label: 'Calories', icon: '🔥'},
        HOLD_DURATION: {label: 'Avg Hold Time', icon: '⏱️'},      // ✅ ADD
        TOTAL_HOLD_TIME: {label: 'Total Hold Time', icon: '⏳'}, // ✅ ADD
        MAX_HOLD: {label: 'Max Hold', icon: '🏆'}                 // ✅ ADD
    };

    return metricMap[metric] || {label: metric, icon: '📈'};
}

// ==================== TREND HELPERS ====================

/**
 * Determine trend direction from percentage change
 */
export function getTrendDirection(percentage: number): 'UP' | 'DOWN' | 'STABLE' {
    if (percentage > 5) return 'UP';
    if (percentage < -5) return 'DOWN';
    return 'STABLE';
}

/**
 * Get emoji for trend direction
 */
export function getTrendEmoji(trend: 'UP' | 'DOWN' | 'STABLE'): string {
    switch (trend) {
        case 'UP':
            return '📈';
        case 'DOWN':
            return '📉';
        case 'STABLE':
            return '➡️';
    }
}

/**
 * Get Tailwind color class for trend
 */
export function getTrendColor(trend: 'UP' | 'DOWN' | 'STABLE'): string {
    switch (trend) {
        case 'UP':
            return 'text-green-600';
        case 'DOWN':
            return 'text-red-600';
        case 'STABLE':
            return 'text-gray-600';
    }
}