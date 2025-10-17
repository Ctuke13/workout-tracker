import React, {useState} from 'react';
import {useSeason} from '../contexts/SeasonContext';
import {useUserPreferences} from '../contexts/UserPreferencesContext';
import {BarChart3} from 'lucide-react';
import {AnalyticsStats} from '../components/AnalyticsPage/AnalyticsStats';
import PerformanceTrackerChart from '../components/AnalyticsPage/PerformanceTrackerChart';
import {WorkoutTypeBreakdown} from '../components/AnalyticsPage/WorkoutTypeBreakdown';
import {PersonalRecords} from "../components/AnalyticsPage/PersonalRecords";
import {TopExercises} from '../components/AnalyticsPage/TopExercises';
import {TimePeriod} from '../types/analytics'; // ✅ Import shared type

// ❌ DELETE THIS LINE:
// type TimePeriod = 'WEEK' | 'MONTH' | 'SEASON' | 'YEAR';

const AnalyticsPage: React.FC = () => {
    const {theme} = useSeason();
    const {loading: prefsLoading} = useUserPreferences();
    const [selectedPeriod, setSelectedPeriod] = useState<TimePeriod>('MONTH');

    if (prefsLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
                    <p className="mt-4 text-gray-600">Loading analytics...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 pb-20">
            {/* Simplified Header */}
            <div className={`bg-gradient-to-r ${theme.gradient} border-b ${theme.border} shadow-lg`}>
                <div className="max-w-7xl mx-auto px-3 sm:px-4 lg:px-6 py-4">
                    <div className="flex items-center gap-3">
                        <div className={`p-2 ${theme.accentGradient} rounded-lg shadow-lg`}>
                            <BarChart3 className="w-5 h-5 sm:w-6 sm:h-6 text-white"/>
                        </div>
                        <div>
                            <h1 className={`text-xl sm:text-2xl font-black ${theme.textPrimary}`}>
                                Analytics
                            </h1>
                            <p className={`text-[10px] sm:text-xs ${theme.textSecondary} font-semibold`}>
                                Track your progress
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-3 sm:px-4 lg:px-6 py-4 sm:py-6 space-y-4 sm:space-y-6">
                <AnalyticsStats period={selectedPeriod}/>
                <PerformanceTrackerChart
                    period={selectedPeriod}
                    onPeriodChange={setSelectedPeriod}
                />
                {/* <WorkoutTypeBreakdown period={selectedPeriod}/> */}
                <PersonalRecords period={selectedPeriod} limit={8}/>
                <TopExercises period={selectedPeriod} limit={10}/>
            </div>
        </div>
    );
};

export default AnalyticsPage;