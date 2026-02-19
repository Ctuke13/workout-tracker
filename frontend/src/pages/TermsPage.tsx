import React from 'react';
import {ArrowLeft} from 'lucide-react';
import {useNavigate} from 'react-router-dom';

const TermsPage: React.FC = () => {
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
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">Terms of Service</h1>
                    <p className="text-sm text-gray-600 mb-8">Last updated: February 17, 2026</p>

                    <div className="prose prose-gray max-w-none">
                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">1. Acceptance of Terms</h2>
                        <p className="text-gray-700 mb-4">
                            By accessing and using EvoPet ("the Service"), you accept and agree to be bound by the terms
                            and provision of this agreement.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">2. Use of Service</h2>
                        <p className="text-gray-700 mb-4">
                            EvoPet provides a fitness tracking platform with gamification elements. You are responsible
                            for maintaining the confidentiality of your account and password and for restricting access
                            to your device.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">3. User Account</h2>
                        <p className="text-gray-700 mb-4">
                            You must provide accurate and complete information when creating an account. You are
                            responsible for all activities that occur under your account.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">4. Subscription and Billing</h2>
                        <p className="text-gray-700 mb-4">
                            Some features may require a paid subscription. Subscription fees are non-refundable except
                            as required by law. You may cancel your subscription at any time.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">5. Content and Conduct</h2>
                        <p className="text-gray-700 mb-4">
                            You retain all rights to the content you upload. You agree not to upload content that is
                            illegal, offensive, or infringes on others' rights.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">6. Privacy</h2>
                        <p className="text-gray-700 mb-4">
                            Your privacy is important to us. Please review our Privacy Policy to understand how we
                            collect, use, and protect your information.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">7. Disclaimer</h2>
                        <p className="text-gray-700 mb-4">
                            EvoPet is not a substitute for professional medical advice. Always consult with a healthcare
                            professional before starting any fitness program. We do not guarantee specific fitness
                            results.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">8. Limitation of Liability</h2>
                        <p className="text-gray-700 mb-4">
                            EvoPet shall not be liable for any indirect, incidental, special, consequential, or punitive
                            damages resulting from your use of the Service.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">9. Changes to Terms</h2>
                        <p className="text-gray-700 mb-4">
                            We reserve the right to modify these terms at any time. We will notify users of any material
                            changes via email or through the Service.
                        </p>

                        <h2 className="text-xl font-semibold text-gray-900 mt-6 mb-3">10. Contact</h2>
                        <p className="text-gray-700 mb-4">
                            For questions about these Terms, please contact us at: support@evopet.app
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TermsPage;
