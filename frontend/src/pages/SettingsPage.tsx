import React from 'react';
import {LogOut} from 'lucide-react';
import {useAuth} from '../contexts/AuthContext';
import ProfileSection from '../components/SettingsPage/ProfileSection';
import AccountSection from '../components/SettingsPage/AccountSection';
import GoalManagement from '../components/SettingsPage/GoalManagement';
import TutorialSection from '../components/SettingsPage/TutorialSection.jsx';
import PreferencesSection from '../components/SettingsPage/PreferencesSection';
import SubscriptionSection from '../components/SettingsPage/SubscriptionSection';
import DataPrivacySection from '../components/SettingsPage/DataPrivacySection';
import AboutSection from '../components/SettingsPage/AboutSection';

const SettingsPage: React.FC = () => {
    const {logout} = useAuth();

    const handleLogout = () => {
        if (window.confirm('Are you sure you want to sign out?')) {
            logout();
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-purple-50/30 pb-24">
            {/* Header */}
            <div className="bg-white border-b border-gray-200 sticky top-0 z-10">
                <div className="max-w-2xl mx-auto px-4 py-4">
                    <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
                    <p className="text-sm text-gray-600">Manage your account and preferences</p>
                </div>
            </div>

            {/* Content */}
            <div className="max-w-2xl mx-auto px-4 py-6 space-y-6">
                {/* Profile */}
                <ProfileSection/>

                {/* Account */}
                <AccountSection/>

                {/* Weekly Goals */}
                <GoalManagement/>

                {/* Tutorials */}
                <TutorialSection/>

                {/* Preferences */}
                <PreferencesSection/>

                {/* Subscription */}
                <SubscriptionSection/>

                {/* Privacy & Data */}
                <DataPrivacySection/>

                {/* About */}
                <AboutSection/>

                {/* Sign Out Button */}
                <button
                    onClick={handleLogout}
                    className="w-full bg-white rounded-2xl shadow-sm border border-red-200 hover:border-red-300 hover:bg-red-50 transition-all p-4 flex items-center justify-center gap-3 text-red-600 font-semibold"
                >
                    <LogOut className="w-5 h-5"/>
                    Sign Out
                </button>

                {/* Bottom Spacing for Mobile Nav */}
                <div className="h-6"/>
            </div>
        </div>
    );
};

export default SettingsPage;