import React from 'react';
import {HeroStatsCard} from '../components/ProgressPage/HeroStatsCard';
import {CurrentSeasonCard} from "../components/ProgressPage/CurrentSeasonCard";
import {AchievementGalleryPreview} from "../components/ProgressPage/AchievementGalleryPreview";
import {LeaderboardPreview} from "../components/ProgressPage/LeaderboardPreview";
import {AnalyticsPreview} from '../components/ProgressPage/AnalyticsPreview';

const ProgressPage: React.FC = () => {
    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            {/* Changed from bg-gray-900 to bg-gray-50 */}
            <div className="container mx-auto px-4 py-6 space-y-6 max-w-7xl">
                {/* Hero Stats Card */}
                <HeroStatsCard/>

                {/* Current Season Card */}
                <CurrentSeasonCard/>

                <AnalyticsPreview/>

                <AchievementGalleryPreview/>

                <LeaderboardPreview/>


                {/* TODO: Add remaining components */}
                {/* <PersonalRecordsPreview /> */}

            </div>
        </div>
    );
};

export default ProgressPage;