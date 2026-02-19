import React, {useState, useEffect} from 'react';
import {Bell, Scale, Ruler, Globe, ChevronRight} from 'lucide-react';
import userApi from '../../services/userApi';
import toast from 'react-hot-toast';

const PreferencesSection: React.FC = () => {
    const [pushEnabled, setPushEnabled] = useState(true);
    const [emailEnabled, setEmailEnabled] = useState(true);
    const [preferences, setPreferences] = useState<{
        preferredDistanceUnit: string;
        preferredWeightUnit: string
    } | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadPreferences();
    }, []);

    const loadPreferences = async () => {
        try {
            const prefs = await userApi.getPreferences();
            setPreferences(prefs);
        } catch (error) {
            console.error('Failed to load preferences:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleUpdatePreferences = async (updates: {
        preferredDistanceUnit?: string;
        preferredWeightUnit?: string
    }) => {
        try {
            await userApi.updatePreferences(updates);
            toast.success('Preferences updated!');
            loadPreferences();
        } catch (error) {
            toast.error('Failed to update preferences');
        }
    };

    const handleToggle = (type: 'push' | 'email') => {
        if (type === 'push') {
            setPushEnabled(!pushEnabled);
            toast.success(`Push notifications ${!pushEnabled ? 'enabled' : 'disabled'}`);
        } else {
            setEmailEnabled(!emailEnabled);
            toast.success(`Email notifications ${!emailEnabled ? 'enabled' : 'disabled'}`);
        }
    };

    if (loading) {
        return (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center justify-center py-8">
                    <div className="w-8 h-8 border-4 border-purple-500 border-t-transparent rounded-full animate-spin"/>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900">Preferences</h3>
                <p className="text-sm text-gray-500">Customize your app experience</p>
            </div>

            {/* Preference Items */}
            <div className="divide-y divide-gray-200">
                {/* Push Notifications */}
                <div className="px-6 py-4 hover:bg-gray-50 transition-colors">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 flex-1">
                            <div
                                className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600">
                                <Bell className="w-5 h-5"/>
                            </div>
                            <div className="flex-1">
                                <p className="text-base font-medium text-gray-900">Push Notifications</p>
                                <p className="text-sm text-gray-500">Get notified about workouts and achievements</p>
                            </div>
                        </div>
                        <button
                            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                                pushEnabled ? 'bg-purple-600' : 'bg-gray-300'
                            }`}
                            onClick={() => handleToggle('push')}
                        >
                            <span
                                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                    pushEnabled ? 'translate-x-6' : 'translate-x-1'
                                }`}
                            />
                        </button>
                    </div>
                </div>

                {/* Email Notifications */}
                <div className="px-6 py-4 hover:bg-gray-50 transition-colors">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 flex-1">
                            <div
                                className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600">
                                <Bell className="w-5 h-5"/>
                            </div>
                            <div className="flex-1">
                                <p className="text-base font-medium text-gray-900">Email Notifications</p>
                                <p className="text-sm text-gray-500">Receive weekly progress updates</p>
                            </div>
                        </div>
                        <button
                            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                                emailEnabled ? 'bg-purple-600' : 'bg-gray-300'
                            }`}
                            onClick={() => handleToggle('email')}
                        >
                            <span
                                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                                    emailEnabled ? 'translate-x-6' : 'translate-x-1'
                                }`}
                            />
                        </button>
                    </div>
                </div>

                {/* Weight Units */}
                <div
                    className="px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer"
                    onClick={() => {
                        const newUnit = preferences?.preferredWeightUnit === 'kg' ? 'lbs' : 'kg';
                        handleUpdatePreferences({preferredWeightUnit: newUnit});
                    }}
                >
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 flex-1">
                            <div
                                className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600">
                                <Scale className="w-5 h-5"/>
                            </div>
                            <div className="flex-1">
                                <p className="text-base font-medium text-gray-900">Weight Units</p>
                                <p className="text-sm text-gray-500">
                                    {preferences?.preferredWeightUnit === 'kg' ? 'Kilograms (kg)' : 'Pounds (lbs)'}
                                </p>
                            </div>
                        </div>
                        <ChevronRight className="w-5 h-5 text-gray-400"/>
                    </div>
                </div>

                {/* Distance Units */}
                <div
                    className="px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer"
                    onClick={() => {
                        const newUnit = preferences?.preferredDistanceUnit === 'km' ? 'miles' : 'km';
                        handleUpdatePreferences({preferredDistanceUnit: newUnit});
                    }}
                >
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 flex-1">
                            <div
                                className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600">
                                <Ruler className="w-5 h-5"/>
                            </div>
                            <div className="flex-1">
                                <p className="text-base font-medium text-gray-900">Distance Units</p>
                                <p className="text-sm text-gray-500">
                                    {preferences?.preferredDistanceUnit === 'km' ? 'Kilometers (km)' : 'Miles (mi)'}
                                </p>
                            </div>
                        </div>
                        <ChevronRight className="w-5 h-5 text-gray-400"/>
                    </div>
                </div>

                {/* Language */}
                <div className="px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 flex-1">
                            <div
                                className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center text-purple-600">
                                <Globe className="w-5 h-5"/>
                            </div>
                            <div className="flex-1">
                                <p className="text-base font-medium text-gray-900">Language</p>
                                <p className="text-sm text-gray-500">English</p>
                            </div>
                        </div>
                        <ChevronRight className="w-5 h-5 text-gray-400"/>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PreferencesSection;