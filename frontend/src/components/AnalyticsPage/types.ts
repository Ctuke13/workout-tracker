// ==================== TIME PERIOD TYPES ====================

export type TimePeriod = 'WEEK' | 'MONTH' | 'SEASON' | 'YEAR';

// Helper interfaces for components that accept period props
export interface PeriodProps {
    period: TimePeriod;
}

export interface PeriodChangeProps {
    period?: TimePeriod;
    onPeriodChange?: (period: TimePeriod) => void;
}

// Map frontend TimePeriod to backend API period
export const mapPeriodToBackend = (period: TimePeriod): string => {
    if (period === 'SEASON') {
        return 'YEAR'; // Backend uses YEAR for seasonal data
    }
    return period;
};