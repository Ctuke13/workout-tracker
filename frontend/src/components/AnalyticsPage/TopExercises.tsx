import React, {useState, useEffect} from 'react';
import {BarChart3, TrendingUp} from 'lucide-react';
import {useSeason} from '../../contexts/SeasonContext';
import {analyticsApi, TopExercise} from '../../services/analyticsApi';
import {TimePeriod, mapPeriodToBackend} from '../../types/analytics';

interface TopExercisesProps {
    period: TimePeriod;
    limit?: number;
}

export const TopExercises: React.FC<TopExercisesProps> = ({period, limit = 10}) => {
    const {theme} = useSeason();

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [exercises, setExercises] = useState<TopExercise[]>([]);

    useEffect(() => {
        loadTopExercises();
    }, [period]);

    const loadTopExercises = async () => {
        try {
            setLoading(true);
            setError(null);

            // ✅ Call real API
            const data = await analyticsApi.getTopExercises(period, limit);
            setExercises(data);

        } catch (err) {
            console.error('Failed to load top exercises:', err);
            setError('Failed to load top exercises');
        } finally {
            setLoading(false);
        }
    };

    // Calculate max count for percentage bars
    const maxCount = exercises.length > 0 ? Math.max(...exercises.map(e => e.count)) : 0;

    // Get bar color based on position (gradient from most to least)
    const getBarOpacity = (index: number): number => {
        if (index === 0) return 1.0;      // #1 - Full opacity
        if (index === 1) return 0.85;     // #2 - Slightly dimmed
        if (index === 2) return 0.7;      // #3 - More dimmed
        return 0.5;                        // #4+ - Most dimmed
    };

    // Loading state
    if (loading) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg animate-pulse`}>
                <div className={`h-6 ${theme.accentLight} rounded w-48 mb-4`}></div>
                <div className="space-y-3">
                    {[1, 2, 3, 4, 5].map((i) => (
                        <div key={i} className={`h-12 ${theme.accentLight} rounded`}></div>
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
    if (exercises.length === 0) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-lg p-4 md:p-6 border ${theme.border} shadow-lg`}>
                <div className="text-center py-8">
                    <BarChart3 className={`w-12 h-12 ${theme.textSecondary} mx-auto mb-4`}/>
                    <h3 className={`text-lg font-bold ${theme.textPrimary} mb-2`}>
                        No Exercise Data Yet
                    </h3>
                    <p className={`text-sm ${theme.textSecondary}`}>
                        Complete workouts to see your most used exercises
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
                        <BarChart3 className="w-5 h-5 text-white"/>
                    </div>
                    <div>
                        <h3 className={`text-base sm:text-lg font-black ${theme.textPrimary}`}>
                            Most Used Exercises
                        </h3>
                        <p className={`text-[10px] sm:text-xs ${theme.textSecondary} font-semibold`}>
                            Your go-to movements
                        </p>
                    </div>
                </div>
            </div>

            {/* Exercise List with Bars */}
            <div className="space-y-4">
                {exercises.map((exercise, index) => {
                    const percentage = (exercise.count / maxCount) * 100;

                    return (
                        <div key={exercise.exerciseId} className="space-y-1.5">
                            {/* Exercise Name and Count */}
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-2">
                  <span className="text-lg font-black text-gray-400 w-6">
                    #{index + 1}
                  </span>
                                    <h4 className={`text-sm font-bold ${theme.textPrimary} truncate`}>
                                        {exercise.exerciseName}
                                    </h4>
                                </div>
                                <div className="flex items-center gap-3">
                  <span className={`text-sm font-black ${theme.textPrimary} tabular-nums`}>
                    {exercise.count}
                  </span>
                                    {index === 0 && (
                                        <TrendingUp className="w-4 h-4 text-green-500"/>
                                    )}
                                </div>
                            </div>

                            {/* Progress Bar */}
                            <div className="relative h-2 bg-gray-100 rounded-full overflow-hidden">
                                <div
                                    className={`absolute top-0 left-0 h-full bg-gradient-to-r ${theme.progressBar} rounded-full transition-all duration-500`}
                                    style={{
                                        width: `${percentage}%`,
                                        opacity: getBarOpacity(index)
                                    }}
                                />
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* View Exercise Details Button */}
            <div className="mt-6 text-center">
                <button
                    className={`px-6 py-2.5 bg-gradient-to-r ${theme.buttonGradient} text-white rounded-lg font-bold text-sm hover:opacity-90 transition shadow-md`}
                    onClick={() => {
                        // TODO: Navigate to Exercise Progression page
                        console.log('View exercise details clicked');
                    }}
                >
                    View Exercise Details →
                </button>
            </div>
        </div>
    );
};