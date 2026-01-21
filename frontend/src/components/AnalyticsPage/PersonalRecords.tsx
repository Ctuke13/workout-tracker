import React, {useState, useEffect} from 'react';
import {Trophy, TrendingUp, Calendar, Timer} from 'lucide-react';
import {useSeason} from '../../contexts/SeasonContext';
import {useUserPreferences} from '../../contexts/UserPreferencesContext';
import {analyticsApi, PersonalRecord} from '../../services/analyticsApi';
import {TimePeriod} from '../../types/analytics';

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
    }, [period, limit]);

    // Map TimePeriod to days for the API
    const mapPeriodToDays = (period: TimePeriod): number => {
        switch (period) {
            case 'WEEK':
                return 7;
            case 'MONTH':
                return 30;
            case 'SEASON':
                return 90;
            case 'YEAR':
                return 365;
            default:
                return 30;
        }
    };

    const loadRecords = async () => {
        try {
            setLoading(true);
            setError(null);

            // Convert period to days
            const days = mapPeriodToDays(period);

            // Call real API
            const data = await analyticsApi.getRecentPersonalRecords(days);

            // Limit results
            setRecords(data.slice(0, limit));

            console.log(`✅ Loaded ${data.length} personal records for ${period} (${days} days)`);

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
            case 'MAX_VOLUME':
                return <TrendingUp className={iconClass}/>;
            case 'MAX_HOLD':  // ✅ ADD THIS
                return <Timer className={iconClass}/>; // Or use Clock icon
            case 'MAX_DISTANCE':
                return <TrendingUp className={iconClass}/>;
            case 'BEST_TIME':
                return <Calendar className={iconClass}/>;
            default:
                return <Trophy className={iconClass}/>;
        }
    };

    // Get background color for PR type
    const getIconBg = (type: string): string => {
        switch (type) {
            case 'MAX_WEIGHT':
                return 'from-yellow-500 to-yellow-600';
            case 'MAX_VOLUME':
                return 'from-purple-500 to-purple-600';
            case 'MAX_HOLD':  // ✅ ADD THIS
                return 'from-orange-500 to-orange-600'; // Orange for holds
            case 'MAX_DISTANCE':
                return 'from-blue-500 to-blue-600';
            case 'BEST_TIME':
                return 'from-green-500 to-green-600';
            default:
                return 'from-yellow-500 to-yellow-600';
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
                <div className="text-center py-8">
                    <p className="text-red-600 font-semibold mb-4">⚠️ {error}</p>
                    <button
                        onClick={loadRecords}
                        className={`px-6 py-2.5 bg-gradient-to-r ${theme.buttonGradient} text-white rounded-lg font-bold text-sm hover:opacity-90 transition shadow-md`}
                    >
                        Try Again
                    </button>
                </div>
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
                        Keep training to set your first PR in this period!
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
                            {period === 'WEEK' ? 'This week' :
                                period === 'MONTH' ? 'This month' :
                                    period === 'SEASON' ? 'This season' :
                                        'This year'} achievements
                        </p>
                    </div>
                </div>
            </div>

            {/* Records List */}
            <div className="space-y-3">
                {records.map((record, index) => (
                    <div
                        key={`${record.exerciseId}-${record.type}-${index}`}
                        className="bg-white rounded-lg p-3 sm:p-4 border-2 border-gray-100 hover:border-gray-200 transition-all hover:shadow-md"
                    >
                        <div className="flex items-center gap-3">
                            {/* Icon */}
                            <div className="flex-shrink-0">
                                <div
                                    className={`p-2 bg-gradient-to-br ${getIconBg(record.type)} rounded-lg shadow-md`}>
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
                                {record.type === 'MAX_VOLUME' && (
                                    <p className="text-xs text-gray-500">
                                        {record.weight}×{record.reps}
                                    </p>
                                )}
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