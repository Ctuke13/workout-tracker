import React from 'react';
import {Info, FileText, Shield, Mail, ExternalLink, Heart} from 'lucide-react';

const AboutSection: React.FC = () => {
    const aboutItems = [
        {
            icon: <Info className="w-5 h-5"/>,
            label: 'App Version',
            value: '1.0.0',
            color: 'bg-blue-100 text-blue-600',
        },
        {
            icon: <FileText className="w-5 h-5"/>,
            label: 'Terms of Service',
            value: 'View terms',
            color: 'bg-purple-100 text-purple-600',
            link: '/terms',
            external: true,
        },
        {
            icon: <Shield className="w-5 h-5"/>,
            label: 'Privacy Policy',
            value: 'View policy',
            color: 'bg-green-100 text-green-600',
            link: '/privacy',
            external: true,
        },
        {
            icon: <Mail className="w-5 h-5"/>,
            label: 'Contact Support',
            value: 'support@evopet.app',
            color: 'bg-pink-100 text-pink-600',
            link: 'mailto:support@evopet.app',
            external: true,
        },
    ];

    const handleClick = (item: typeof aboutItems[0]) => {
        if (item.link) {
            if (item.link.startsWith('mailto:')) {
                window.location.href = item.link;
            } else if (item.external) {
                window.open(item.link, '_blank');
            } else {
                // Navigate internally
                window.location.href = item.link;
            }
        }
    };

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900">About</h3>
                <p className="text-sm text-gray-500">App information and legal</p>
            </div>

            {/* About Items */}
            <div className="divide-y divide-gray-200">
                {aboutItems.map((item, index) => (
                    <div
                        key={index}
                        className={`px-6 py-4 transition-colors ${
                            item.link ? 'hover:bg-gray-50 cursor-pointer' : ''
                        }`}
                        onClick={() => handleClick(item)}
                    >
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                                <div className={`w-10 h-10 ${item.color} rounded-lg flex items-center justify-center`}>
                                    {item.icon}
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">{item.label}</p>
                                    <p className="text-base font-medium text-gray-900">{item.value}</p>
                                </div>
                            </div>
                            {item.link && <ExternalLink className="w-5 h-5 text-gray-400"/>}
                        </div>
                    </div>
                ))}
            </div>

            {/* Footer */}
            <div
                className="px-6 py-4 bg-gradient-to-r from-purple-50 to-pink-50 border-t border-gray-200 rounded-b-2xl">
                <div className="text-center">
                    <div className="flex items-center justify-center gap-2 mb-2">
                        <span className="text-2xl">🐺</span>
                        <p className="text-lg font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                            EvoPet
                        </p>
                    </div>
                    <p className="text-sm text-gray-600">
                        Made with <Heart className="w-4 h-4 inline text-red-500" fill="currentColor"/> for fitness
                        enthusiasts
                    </p>
                    <p className="text-xs text-gray-500 mt-1">
                        © 2026 EvoPet. All rights reserved.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default AboutSection;