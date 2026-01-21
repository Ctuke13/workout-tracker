import React, {useState} from 'react';
import {HeroStatsCard} from '../components/ProgressPage/HeroStatsCard';
import {CurrentSeasonCard} from "../components/ProgressPage/CurrentSeasonCard";
import {AchievementGalleryPreview} from "../components/ProgressPage/AchievementGalleryPreview";
import {LeaderboardPreview} from "../components/ProgressPage/LeaderboardPreview";
import {AnalyticsPreview} from '../components/ProgressPage/AnalyticsPreview';
import {SeasonHistoryModal} from '../components/ProgressPage/SeasonHistoryModal';
import {LeaderboardModal} from '../components/ProgressPage/LeaderboardModal';

const ProgressPage: React.FC = () => {
    const [showSeasonHistory, setShowSeasonHistory] = useState(false);
    const [showLeaderboard, setShowLeaderboard] = useState(false);

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <div className="container mx-auto px-4 py-6 space-y-6 max-w-7xl">
                <HeroStatsCard/>
                <CurrentSeasonCard onViewHistory={() => setShowSeasonHistory(true)}/>
                <AnalyticsPreview/>
                <AchievementGalleryPreview/>
                <LeaderboardPreview onViewFull={() => setShowLeaderboard(true)}/>
            </div>

            {/* Modals */}
            <SeasonHistoryModal
                isOpen={showSeasonHistory}
                onClose={() => setShowSeasonHistory(false)}
            />
            <LeaderboardModal
                isOpen={showLeaderboard}
                onClose={() => setShowLeaderboard(false)}
            />
        </div>
    );
};

export default ProgressPage;