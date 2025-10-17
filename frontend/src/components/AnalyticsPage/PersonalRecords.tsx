import React, {useState, useEffect} from 'react';
import {Trophy, TrendingUp, Calendar} from 'lucide-react';
import {useSeason} from '../../contexts/SeasonContext';
import {useUserPreferences} from '../../contexts/UserPreferencesContext';
import {analyticsApi, PersonalRecord} from '../../services/analyticsApi';
import {TimePeriod, mapPeriodToBackend} from '../../types/analytics';

interface PersonalRecordsProps {
    period: TimePeriod;
    limit?: number;
}

export const PersonalRecords: React.FC<PersonalRecordsProps> = ({period, limit = 8}) => {
    const {theme} = useSeason();
    const {weightUnit, distanceUnit, convertWeight, convertDistance} = useUserPreferences();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [records, setRecords] = useState<PersonalRecord[]>([]);

    useEffect(() => {
        loadRecords();
    }, [period]);

    const loadRecords = async () => {
        try {
            setLoading(true);
            setError(null);

            const backendPeriod = mapPeriodToBackend(period);

            // TODO: Update API to accept period parameter
            // For now, get recent records (30 days)
            // const data = await analyticsApi.getRecentPersonalRecords(30);

            // Simulate API call
            await new Promise(resolve => setTimeout(resolve, 500));

            // Mock data
            const mockData = generateMockPRs();
            setRecords(mockData.slice(0, limit));

        } catch (err) {
            console.error('Failed to load personal records:', err);
            setError('Failed to load personal records');
        } finally {
            setLoading(false);
        }
    };

    // Convert value based on unit
    const convertValue = (value: number, unit: string): number => {
        if (unit === 'kg' || unit === 'lbs') {
            return weightUnit === 'lbs' ? convertWeight(value) : value;
        }
        if (unit === 'km' || unit === 'miles') {
            return distanceUnit === 'miles' ? convertDistance(value) : value;
        }
        return value;
    };

    // Format value with unit
    const formatValue = (value: number, unit: string): string => {
        const converted = convertValue(value, unit);

        if (unit === 'kg' || unit === 'lbs') {
            return `${converted.toFixed(0)} ${weightUnit}`;
        }
        if (unit === 'km' || unit === 'miles') {
            return `${converted.toFixed(1)} ${distanceUnit}`;
        }
        if (unit === 'time') {
            // Format as MM:SS
            const totalSeconds = Math.round(value);
            const minutes = Math.floor(totalSeconds / 60);
            const seconds = totalSeconds % 60;
            return `${minutes}:${seconds.toString().padStart(2, '0')}`;
        }
        return `${value} ${unit}`;
    };

    // Format date
    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        const now = new Date();
        const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));

        if (diffDays === 0) return 'Today';
        if (diffDays === 1) return 'Yesterday';
        if (diffDays < 7) return `${diffDays} days ago`;

        return date.toLocaleDateString('en-US', {month: 'short', day: 'numeric', year: 'numeric'});
    };

    // Get icon for PR type
    const getIcon = (type: string) => {
        const iconClass = "w-4 h-4 text-white";
        switch (type) {
            case 'MAX_WEIGHT':
                return <Trophy className={iconClass}/>;
            case 'MAX_DISTANCE':
                return <TrendingUp className={iconClass}/>;
            case 'BEST_TIME':
                return <Calendar className={iconClass}/>;
            default:
                return <Trophy className={iconClass}/>;
        }
    };

    // Loading state
    if (loading) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg animate-pulse`}>
                <div className={`h-6 ${theme.accentLight} rounded w-48 mb-4`}></div>
                <div className="space-y-3">
                    {[1, 2, 3].map((i) => (
                        <div key={i} className={`h-16 ${theme.accentLight} rounded`}></div>
                    ))}
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

    // Empty state
    if (records.length === 0) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg`}>
                <div className="text-center py-8">
                    <Trophy className={`w-12 h-12 ${theme.textSecondary} mx-auto mb-4`}/>
                    <h3 className={`text-lg font-bold ${theme.textPrimary} mb-2`}>
                        No Personal Records Yet
                    </h3>
                    <p className={`text-sm ${theme.textSecondary}`}>
                        Keep training to set your first PR!
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg`}>
            {/* Header */}
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className={`p-2 ${theme.accentGradient} rounded-lg shadow-md`}>
                        <Trophy className="w-5 h-5 text-white"/>
                    </div>
                    <div>
                        <h3 className={`text-base sm:text-lg font-black ${theme.textPrimary}`}>
                            Personal Records
                        </h3>
                        <p className={`text-[10px] sm:text-xs ${theme.textSecondary} font-semibold`}>
                            Recent achievements
                        </p>
                    </div>
                </div>
            </div>

            {/* Records List */}
            <div className="space-y-3">
                {records.map((record, index) => (
                    <div
                        key={index}
                        className="bg-white rounded-lg p-3 sm:p-4 border-2 border-gray-100 hover:border-gray-200 transition-all hover:shadow-md"
                    >
                        <div className="flex items-center gap-3">
                            {/* Icon */}
                            <div className="flex-shrink-0">
                                <div
                                    className={`p-2 bg-gradient-to-br from-yellow-500 to-yellow-600 rounded-lg shadow-md`}>
                                    {getIcon(record.type)}
                                </div>
                            </div>

                            {/* Exercise Info */}
                            <div className="flex-1 min-w-0">
                                <h4 className="text-sm font-black text-gray-900 truncate">
                                    {record.exerciseName}
                                </h4>
                                <div className="flex items-center gap-2 mt-0.5">
                                    <p className="text-xs text-gray-600">
                                        {formatDate(record.date)}
                                    </p>
                                    {record.reps && (
                                        <span className="text-xs text-gray-400">
                      • {record.reps} reps
    </span>
                                    )}
                                </div>
                            </div>

                            {/* Value */}
                            <div className="text-right flex-shrink-0">
                                <p className={`text-lg sm:text-xl font-black ${theme.textPrimary} tabular-nums`}>
                                    {formatValue(record.value, record.unit)}
                                </p>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* View All Link (if more records exist) */}
            {records.length >= limit && (
                <div className="mt-6 text-center">
                    <button
                        className={`px-6 py-2.5 bg-gradient-to-r ${theme.buttonGradient} text-white rounded-lg font-bold text-sm hover:opacity-90 transition shadow-md`}
                        onClick={() => {
                            // TODO: Navigate to full PR page or expand
                            console.log('View all records clicked');
                        }}
                    >
                        View All Records →
                    </button>
                </div>
            )}
        </div>
    );
};

// ==================== MOCK DATA GENERATOR ====================

function generateMockPRs(): PersonalRecord[] {
    const exercises = [
        {name: 'Deadlift', type: 'MAX_WEIGHT', value: 315, unit: 'lbs', reps: 1},
        {name: 'Bench Press', type: 'MAX_WEIGHT', value: 225, unit: 'lbs', reps: 1},
        {name: 'Squat', type: 'MAX_WEIGHT', value: 275, unit: 'lbs', reps: 1},
        {name: 'Pull-ups', type: 'MAX_WEIGHT', value: 45, unit: 'lbs', reps: 10},
        {name: '5K Run', type: 'BEST_TIME', value: 1350, unit: 'time'}, // 22:30
        {name: '10K Run', type: 'BEST_TIME', value: 2820, unit: 'time'}, // 47:00
        {name: 'Running', type: 'MAX_DISTANCE', value: 10.2, unit: 'km'},
        {name: 'Overhead Press', type: 'MAX_WEIGHT', value: 135, unit: 'lbs', reps: 5},
    ];

    return exercises.map((ex, index) => ({
        type: ex.type,
        exerciseName: ex.name,
        exerciseId: index + 1,
        value: ex.value,
        unit: ex.unit,
        reps: ex.reps,
        weight: ex.type === 'MAX_WEIGHT' ? ex.value : undefined,
        date: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    }));
}