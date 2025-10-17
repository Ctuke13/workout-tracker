import React, {useState, useEffect} from 'react';
import {Dumbbell, Heart, Timer} from 'lucide-react';
import {useSeason} from '../../contexts/SeasonContext';
import {useUserPreferences} from '../../contexts/UserPreferencesContext';

interface WorkoutTypeBreakdownProps {
    period: 'WEEK' | 'MONTH' | 'YEAR' | 'ALL_TIME';
}

interface StrengthMetrics {
    totalVolume: number;      // In kg
    maxWeight: number;        // In kg
    totalReps: number;
    totalSets: number;
    workoutCount: number;
}

interface CardioMetrics {
    totalDistance: number;    // In km
    averagePace: number;      // Min per km
    topSpeed: number;         // Km/h
    totalCalories: number;
    workoutCount: number;
}

interface IsometricMetrics {
    totalHoldTime: number;    // In seconds
    longestHold: number;      // In seconds
    averageHoldTime: number;  // In seconds
    workoutCount: number;
}

interface BreakdownData {
    strength?: StrengthMetrics;
    cardio?: CardioMetrics;
    isometric?: IsometricMetrics;
}

export const WorkoutTypeBreakdown: React.FC<WorkoutTypeBreakdownProps> = ({period}) => {
    const {theme} = useSeason();
    const {
        distanceUnit,
        weightUnit,
        convertDistance,
        convertWeight,
        formatDistance,
        formatWeight
    } = useUserPreferences();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [breakdown, setBreakdown] = useState<BreakdownData>({});

    useEffect(() => {
        loadBreakdown();
    }, [period]);

    const loadBreakdown = async () => {
        try {
            setLoading(true);
            setError(null);

            // TODO: Replace with real API call
            // const data = await analyticsApi.getWorkoutTypeBreakdown(period);

            // Simulate API call
            await new Promise(resolve => setTimeout(resolve, 500));

            // Mock data
            const mockData = generateMockBreakdown();
            setBreakdown(mockData);

        } catch (err) {
            console.error('Failed to load breakdown:', err);
            setError('Failed to load workout breakdown');
        } finally {
            setLoading(false);
        }
    };

    // Format pace (min/km or min/mile)
    const formatPace = (minPerKm: number): string => {
        const pace = distanceUnit === 'miles' ? minPerKm * 1.60934 : minPerKm;
        const minutes = Math.floor(pace);
        const seconds = Math.round((pace - minutes) * 60);
        const unitLabel = distanceUnit === 'miles' ? 'mi' : 'km';
        return `${minutes}:${seconds.toString().padStart(2, '0')}/${unitLabel}`;
    };

    // Format speed
    const formatSpeed = (kmh: number): string => {
        const speed = distanceUnit === 'miles' ? convertDistance(kmh) : kmh;
        const unitLabel = distanceUnit === 'miles' ? 'mph' : 'km/h';
        return `${speed.toFixed(1)} ${unitLabel}`;
    };

    // Format time duration
    const formatDuration = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        if (mins === 0) return `${secs}s`;
        return `${mins}m ${secs}s`;
    };

    // Loading state
    if (loading) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg animate-pulse`}>
                <div className={`h-6 ${theme.accentLight} rounded w-48 mb-4`}></div>
                <div className="space-y-3">
                    <div className={`h-24 ${theme.accentLight} rounded`}></div>
                    <div className={`h-24 ${theme.accentLight} rounded`}></div>
                </div>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg`}>
                <p className="text-red-600 font-semibold text-center">⚠️ {error}</p>
            </div>
        );
    }

    // Check if user has any workout data
    const hasData = breakdown.strength || breakdown.cardio || breakdown.isometric;

    if (!hasData) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg`}>
                <div className="text-center py-8">
                    <Dumbbell className={`w-12 h-12 ${theme.textSecondary} mx-auto mb-4`}/>
                    <h3 className={`text-lg font-bold ${theme.textPrimary} mb-2`}>
                        No Workout Data Yet
                    </h3>
                    <p className={`text-sm ${theme.textSecondary}`}>
                        Complete workouts to see detailed breakdown by type
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg`}>
            {/* Header */}
            <div className="flex items-center gap-3 mb-6">
                <div className={`p-2 ${theme.accentGradient} rounded-lg shadow-md`}>
                    <Dumbbell className="w-5 h-5 text-white"/>
                </div>
                <div>
                    <h3 className={`text-base sm:text-lg font-black ${theme.textPrimary}`}>
                        Workout Type Breakdown
                    </h3>
                    <p className={`text-[10px] sm:text-xs ${theme.textSecondary} font-semibold`}>
                        Detailed metrics by workout type
                    </p>
                </div>
            </div>

            {/* Breakdown Sections */}
            <div className="space-y-4">

                {/* STRENGTH METRICS */}
                {breakdown.strength && (
                    <div className="bg-white rounded-lg p-4 border-2 border-gray-100">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center gap-2">
                                <div className="p-2 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg">
                                    <Dumbbell className="w-4 h-4 text-white"/>
                                </div>
                                <div>
                                    <h4 className="text-sm font-black text-gray-900">STRENGTH</h4>
                                    <p className="text-xs text-gray-600">{breakdown.strength.workoutCount} workouts</p>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Volume</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatVolume(breakdown.strength.totalVolume)}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Max Weight</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatWeight(breakdown.strength.maxWeight, 0)}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Total Reps</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {breakdown.strength.totalReps.toLocaleString()}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Total Sets</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {breakdown.strength.totalSets}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {/* CARDIO METRICS */}
                {breakdown.cardio && (
                    <div className="bg-white rounded-lg p-4 border-2 border-gray-100">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center gap-2">
                                <div className="p-2 bg-gradient-to-br from-red-500 to-red-600 rounded-lg">
                                    <Heart className="w-4 h-4 text-white"/>
                                </div>
                                <div>
                                    <h4 className="text-sm font-black text-gray-900">CARDIO</h4>
                                    <p className="text-xs text-gray-600">{breakdown.cardio.workoutCount} workouts</p>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Distance</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatDistance(breakdown.cardio.totalDistance, 1)}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Avg Pace</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatPace(breakdown.cardio.averagePace)}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Top Speed</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatSpeed(breakdown.cardio.topSpeed)}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Calories</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {breakdown.cardio.totalCalories.toLocaleString()}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {/* ISOMETRIC METRICS */}
                {breakdown.isometric && (
                    <div className="bg-white rounded-lg p-4 border-2 border-gray-100">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center gap-2">
                                <div className="p-2 bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg">
                                    <Timer className="w-4 h-4 text-white"/>
                                </div>
                                <div>
                                    <h4 className="text-sm font-black text-gray-900">ISOMETRIC</h4>
                                    <p className="text-xs text-gray-600">{breakdown.isometric.workoutCount} workouts</p>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Total Hold Time</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatDuration(breakdown.isometric.totalHoldTime)}
                                </p>
                            </div>
                            <div>
                                <p className="text-xs text-gray-600 mb-1">Longest Hold</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatDuration(breakdown.isometric.longestHold)}
                                </p>
                            </div>
                            <div className="col-span-2">
                                <p className="text-xs text-gray-600 mb-1">Avg Hold Time</p>
                                <p className="text-lg font-black text-gray-900 tabular-nums">
                                    {formatDuration(breakdown.isometric.averageHoldTime)}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

            </div>
        </div>
    );
};

// ==================== HELPER FUNCTIONS ====================

/**
 * Format volume with K suffix
 */
function formatVolume(volumeKg: number): string {
    // Convert to user's preferred unit
    const volume = volumeKg * 2.20462; // Convert kg to lbs for now (will use user pref later)

    if (volume >= 1000) {
        return `${(volume / 1000).toFixed(1)}K lbs`;
    }
    return `${volume.toFixed(0)} lbs`;
}

/**
 * Generate mock breakdown data
 * TODO: Replace with real API call
 */
function generateMockBreakdown(): BreakdownData {
    return {
        strength: {
            totalVolume: 20000, // 20,000 kg = ~44K lbs
            maxWeight: 102,     // 102 kg = ~225 lbs
            totalReps: 1234,
            totalSets: 45,
            workoutCount: 12
        },
        cardio: {
            totalDistance: 68.4, // 68.4 km = ~42.5 miles
            averagePace: 5.3,    // 5.3 min/km = ~8:30/mi
            topSpeed: 16.0,      // 16 km/h = ~10 mph
            totalCalories: 3450,
            workoutCount: 8
        },
        isometric: {
            totalHoldTime: 900,  // 15 minutes
            longestHold: 120,    // 2 minutes
            averageHoldTime: 45, // 45 seconds
            workoutCount: 5
        }
    };
}