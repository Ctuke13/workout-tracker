import React, {useState} from 'react';
import {Bell, BellOff, Smartphone} from 'lucide-react';
import {useAuth} from '../../contexts/AuthContext';
import {useNotifications} from '../../hooks/useNotifications';
import apiClient from '../../services/apiClient';
import toast from 'react-hot-toast';

interface NotificationPref {
    key: keyof NotificationPrefs;
    label: string;
    description: string;
    defaultOn: boolean;
}

interface NotificationPrefs {
    notifPetHealth: boolean;
    notifStreakReminders: boolean;
    notifAchievements: boolean;
    notifRankSeason: boolean;
    notifWeeklySummary: boolean;
    notifSocialLeaderboard: boolean;
    notifReengagement: boolean;
}

const NOTIFICATION_PREFS: NotificationPref[] = [
    {
        key: 'notifPetHealth',
        label: 'Pet Health',
        description: 'Fuel, fatigue, cleanliness & motivation alerts',
        defaultOn: true,
    },
    {
        key: 'notifStreakReminders',
        label: 'Streak Reminders',
        description: 'Streak at risk and daily workout nudges',
        defaultOn: true,
    },
    {
        key: 'notifAchievements',
        label: 'Achievements & Milestones',
        description: 'Unlocks, level-ups, evolutions & personal records',
        defaultOn: true,
    },
    {
        key: 'notifRankSeason',
        label: 'Rank & Season',
        description: 'Rank changes and season start/end alerts',
        defaultOn: true,
    },
    {
        key: 'notifWeeklySummary',
        label: 'Weekly Summary',
        description: 'Your weekly workout recap every Monday',
        defaultOn: true,
    },
    {
        key: 'notifSocialLeaderboard',
        label: 'Social & Leaderboard',
        description: 'When someone passes you on the leaderboard',
        defaultOn: false,
    },
    {
        key: 'notifReengagement',
        label: 'Re-engagement',
        description: 'Reminders when you haven\'t logged in for a while',
        defaultOn: true,
    },
];

const NotificationsSection: React.FC = () => {
    const {user} = useAuth();
    const {permissionStatus, isRegistering, requestPermission} = useNotifications();
    const [saving, setSaving] = useState<string | null>(null);

    if (!user) return null;

    const browserPermission = 'Notification' in window
        ? Notification.permission
        : 'unsupported';

    const isGranted = browserPermission === 'granted' && permissionStatus !== 'unsupported';

    const handleToggle = async (key: keyof NotificationPrefs, currentValue: boolean) => {
        setSaving(key);
        try {
            await apiClient.patch('/api/users/notification-preferences', {
                [key]: !currentValue,
            });
            toast.success('Preference saved');
            // Note: user object will reflect change on next refreshUser()
        } catch (err) {
            toast.error('Failed to save preference');
        } finally {
            setSaving(null);
        }
    };

    const getPrefValue = (key: keyof NotificationPrefs): boolean => {
        const val = (user as any)[key];
        if (val === undefined || val === null) {
            return NOTIFICATION_PREFS.find(p => p.key === key)?.defaultOn ?? true;
        }
        return val as boolean;
    };

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900">Notifications</h3>
                <p className="text-sm text-gray-500">Choose what you want to be notified about</p>
            </div>

            {/* Browser Permission Banner */}
            <div className="px-6 pt-4">
                {browserPermission === 'denied' ? (
                    <div className="flex items-start gap-3 bg-red-50 border border-red-200 rounded-xl p-4 mb-4">
                        <BellOff className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5"/>
                        <div>
                            <p className="text-sm font-semibold text-red-800">Notifications blocked</p>
                            <p className="text-xs text-red-600 mt-0.5">
                                You've blocked notifications in your browser. To re-enable, click the lock icon in your
                                address bar and allow notifications.
                            </p>
                        </div>
                    </div>
                ) : browserPermission === 'default' || permissionStatus === 'idle' ? (
                    <div className="flex items-start gap-3 bg-purple-50 border border-purple-200 rounded-xl p-4 mb-4">
                        <Smartphone className="w-5 h-5 text-purple-500 flex-shrink-0 mt-0.5"/>
                        <div className="flex-1">
                            <p className="text-sm font-semibold text-purple-800">Enable push notifications</p>
                            <p className="text-xs text-purple-600 mt-0.5 mb-3">
                                Get notified about your pet's health, streaks, and achievements even when the app is
                                closed.
                            </p>
                            <button
                                onClick={requestPermission}
                                disabled={isRegistering}
                                className="px-4 py-2 bg-purple-600 text-white rounded-lg text-xs font-semibold hover:bg-purple-700 transition-colors disabled:opacity-50"
                            >
                                {isRegistering ? 'Enabling...' : 'Enable Notifications'}
                            </button>
                        </div>
                    </div>
                ) : isGranted ? (
                    <div className="flex items-center gap-2 bg-green-50 border border-green-200 rounded-xl px-4 py-3 mb-4">
                        <Bell className="w-4 h-4 text-green-600"/>
                        <p className="text-sm text-green-700 font-medium">Push notifications are enabled</p>
                    </div>
                ) : null}
            </div>

            {/* Preference Toggles */}
            <div className="divide-y divide-gray-100 px-2 pb-2">
                {NOTIFICATION_PREFS.map((pref) => {
                    const value = getPrefValue(pref.key);
                    const isSaving = saving === pref.key;

                    return (
                        <div key={pref.key} className="flex items-center justify-between px-4 py-4">
                            <div className="flex-1 min-w-0 pr-4">
                                <p className="text-sm font-medium text-gray-900">{pref.label}</p>
                                <p className="text-xs text-gray-500 mt-0.5">{pref.description}</p>
                            </div>
                            <button
                                onClick={() => handleToggle(pref.key, value)}
                                disabled={isSaving || !isGranted}
                                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-purple-400 focus:ring-offset-2 flex-shrink-0
                                    ${value && isGranted ? 'bg-purple-600' : 'bg-gray-200'}
                                    ${!isGranted ? 'opacity-40 cursor-not-allowed' : 'cursor-pointer'}
                                `}
                                title={!isGranted ? 'Enable notifications first' : undefined}
                            >
                                <span
                                    className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform
                                        ${value && isGranted ? 'translate-x-6' : 'translate-x-1'}
                                    `}
                                />
                            </button>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default NotificationsSection;