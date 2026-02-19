import React from 'react';
import {ArrowLeft} from 'lucide-react';
import {useNavigate} from 'react-router-dom';

const PrivacyPage: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-purple-50/30">
            {/* Header */}
            <div className="bg-white border-b border-gray-200 sticky top-0 z-10">
                <div className="max-w-4xl mx-auto px-4 py-4">
                    <button
                        onClick={() => navigate(-1)}
                        className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
                    >
                        <ArrowLeft className="w-5 h-5"/>
                        <span>Back</span>
                    </button>
                </div>
            </div>

            {/* Content */}
            <div className="max-w-4xl mx-auto px-4 py-8">
                <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">Privacy Policy</h1>
                    <p className="text-sm text-gray-600 mb-8">Last updated: February 17, 2026</p>

                    <div className="prose prose-gray max-w-none">
                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">1. Information We Collect</h2>
                        <p className="text-gray-700 mb-4">
                            We collect information you provide directly to us, including:
                        </p>
                        <ul className="list-disc list-inside text-gray-700 mb-4 space-y-1">
                            <li>Account information (name, email, username)</li>
                            <li>Workout data (exercises, sets, reps, weights)</li>
                            <li>Progress metrics (XP, achievements, streaks)</li>
                            <li>Pet customization data</li>
                            <li>Preferences and settings</li>
                        </ul>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">2. How We Use Your
                            Information</h2>
                        <p className="text-gray-700 mb-4">
                            We use the information we collect to:
                        </p>
                        <ul className="list-disc list-inside text-gray-700 mb-4 space-y-1">
                            <li>Provide, maintain, and improve our services</li>
                            <li>Track your fitness progress and achievements</li>
                            <li>Personalize your experience with EvoPet</li>
                            <li>Send you notifications and updates</li>
                            <li>Respond to your comments and questions</li>
                        </ul>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">3. Data Security</h2>
                        <p className="text-gray-700 mb-4">
                            We take reasonable measures to help protect your personal information from loss, theft,
                            misuse, unauthorized access, disclosure, alteration, and destruction. Your data is encrypted
                            both in transit and at rest.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">4. Data Sharing</h2>
                        <p className="text-gray-700 mb-4">
                            We do not sell your personal information to third parties. We may share your information
                            with:
                        </p>
                        <ul className="list-disc list-inside text-gray-700 mb-4 space-y-1">
                            <li>Service providers who assist in our operations</li>
                            <li>Other users (only if you choose to make data public)</li>
                            <li>Law enforcement when required by law</li>
                        </ul>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">5. Data Retention</h2>
                        <p className="text-gray-700 mb-4">
                            Free users: We retain your data for 30 days after your last activity.
                            Paid users: We retain your data for the duration of your subscription plus 90 days.
                            You can request deletion of your data at any time through the Settings page.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">6. Your Rights</h2>
                        <p className="text-gray-700 mb-4">
                            You have the right to:
                        </p>
                        <ul className="list-disc list-inside text-gray-700 mb-4 space-y-1">
                            <li>Access your personal data</li>
                            <li>Correct inaccurate data</li>
                            <li>Request deletion of your data</li>
                            <li>Export your data</li>
                            <li>Opt-out of marketing communications</li>
                        </ul>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">7. Cookies and Tracking</h2>
                        <p className="text-gray-700 mb-4">
                            We use cookies and similar tracking technologies to track activity on our Service and hold
                            certain information. You can instruct your browser to refuse all cookies or to indicate when
                            a cookie is being sent.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">8. Children's Privacy</h2>
                        <p className="text-gray-700 mb-4">
                            Our Service is not intended for children under 13 years of age. We do not knowingly collect
                            personal information from children under 13.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">9. Changes to This Policy</h2>
                        <p className="text-gray-700 mb-4">
                            We may update our Privacy Policy from time to time. We will notify you of any changes by
                            posting the new Privacy Policy on this page and updating the "Last updated" date.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">10. Contact Us</h2>
                        <p className="text-gray-700 mb-4">
                            If you have any questions about this Privacy Policy, please contact us at:
                            privacy@evopet.app
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PrivacyPage;