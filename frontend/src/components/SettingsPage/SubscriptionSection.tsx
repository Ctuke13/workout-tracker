import React from 'react';
import {Crown, Zap, ArrowUpRight} from 'lucide-react';
import {useAuth} from '../../contexts/AuthContext';

const SubscriptionSection: React.FC = () => {
    const {user} = useAuth();

    if (!user) return null;

    const isFree = user.subscriptionTier === 'FREE';
    const isPlus = user.subscriptionTier === 'PLUS';
    const isPro = user.subscriptionTier === 'PRO';

    const getTierInfo = () => {
        if (isPro) {
            return {
                name: 'Pro',
                icon: <Crown className="w-6 h-6"/>,
                color: 'from-yellow-500 to-orange-500',
                bgColor: 'bg-gradient-to-br from-yellow-50 to-orange-50',
                borderColor: 'border-yellow-300',
                features: ['Unlimited workouts', 'Advanced analytics', 'Custom goals', 'Priority support'],
            };
        }
        if (isPlus) {
            return {
                name: 'Plus',
                icon: <Zap className="w-6 h-6"/>,
                color: 'from-purple-500 to-pink-500',
                bgColor: 'bg-gradient-to-br from-purple-50 to-pink-50',
                borderColor: 'border-purple-300',
                features: ['Extended data retention', 'Advanced stats', 'Custom pet skins', 'No ads'],
            };
        }
        return {
            name: 'Free',
            icon: <Crown className="w-6 h-6"/>,
            color: 'from-gray-500 to-gray-600',
            bgColor: 'bg-gradient-to-br from-gray-50 to-gray-100',
            borderColor: 'border-gray-300',
            features: ['30-day data retention', 'Basic stats', 'Standard pet', 'With ads'],
        };
    };

    const tierInfo = getTierInfo();

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900">Subscription</h3>
                <p className="text-sm text-gray-500">Manage your subscription plan</p>
            </div>

            {/* Current Plan */}
            <div className="p-6">
                <div className={`${tierInfo.bgColor} rounded-xl p-6 border-2 ${tierInfo.borderColor}`}>
                    <div className="flex items-start justify-between mb-4">
                        <div className="flex items-center gap-3">
                            <div
                                className={`w-12 h-12 bg-gradient-to-br ${tierInfo.color} rounded-xl flex items-center justify-center text-white shadow-lg`}>
                                {tierInfo.icon}
                            </div>
                            <div>
                                <h4 className="text-xl font-bold text-gray-900">{tierInfo.name} Plan</h4>
                                <p className="text-sm text-gray-600">Current subscription</p>
                            </div>
                        </div>
                        {!isPro && (
                            <button
                                className="px-4 py-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:from-purple-700 hover:to-pink-700 transition-all flex items-center gap-2 font-medium">
                                Upgrade
                                <ArrowUpRight className="w-4 h-4"/>
                            </button>
                        )}
                    </div>

                    {/* Features */}
                    <div className="space-y-2">
                        {tierInfo.features.map((feature, index) => (
                            <div key={index} className="flex items-center gap-2">
                                <div className="w-5 h-5 bg-white rounded-full flex items-center justify-center">
                                    <svg className="w-3 h-3 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                                        <path fillRule="evenodd"
                                              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                              clipRule="evenodd"/>
                                    </svg>
                                </div>
                                <p className="text-sm text-gray-700">{feature}</p>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Upgrade CTA for Free Users */}
                {isFree && (
                    <div
                        className="mt-4 p-4 bg-gradient-to-r from-purple-50 to-pink-50 rounded-xl border border-purple-200">
                        <p className="text-sm text-gray-700 mb-3">
                            <span className="font-semibold">Unlock unlimited features</span> with Plus or Pro
                        </p>
                        <div className="grid grid-cols-2 gap-3">
                            <button
                                className="px-4 py-2 bg-white border border-purple-300 text-purple-700 rounded-lg hover:bg-purple-50 transition-colors text-sm font-medium">
                                View Plans
                            </button>
                            <button
                                className="px-4 py-2 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:from-purple-700 hover:to-pink-700 transition-all text-sm font-medium">
                                Upgrade Now
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SubscriptionSection;