import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

const NicknameSelectionPage: React.FC = () => {
    const navigate = useNavigate();
    const { user, checkNicknameAvailability } = useAuth();
    
    const [nickname, setNickname] = useState('');
    const [isChecking, setIsChecking] = useState(false);
    const [validationResult, setValidationResult] = useState<{
        available: boolean;
        message: string;
    } | null>(null);
    const [debounceTimer, setDebounceTimer] = useState<NodeJS.Timeout | null>(null);

    // Debounced nickname check
    useEffect(() => {
        if (debounceTimer) {
            clearTimeout(debounceTimer);
        }

        if (nickname.length < 3) {
            setValidationResult(null);
            return;
        }

        const timer = setTimeout(async () => {
            setIsChecking(true);
            try {
                const result = await checkNicknameAvailability(nickname);
                setValidationResult(result);
            } catch (error) {
                setValidationResult({ available: false, message: 'Failed to check nickname' });
            } finally {
                setIsChecking(false);
            }
        }, 500);

        setDebounceTimer(timer);

        return () => {
            if (timer) clearTimeout(timer);
        };
    }, [nickname]);

    const handleContinue = () => {
        // Store nickname in sessionStorage for later
        sessionStorage.setItem('onboarding_nickname', nickname || '');
        navigate('/onboarding/meet-pet');
    };

    const handleSkip = () => {
        sessionStorage.setItem('onboarding_nickname', '');
        navigate('/onboarding/meet-pet');
    };

    const isValid = nickname.length === 0 || (nickname.length >= 3 && validationResult?.available);

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 flex flex-col">
            {/* Progress Bar */}
            <div className="w-full h-2 bg-gray-200">
                <div className="h-full bg-gradient-to-r from-blue-500 to-purple-500 w-1/3 transition-all duration-500" />
            </div>

            <div className="flex-1 flex flex-col items-center justify-center px-4 py-8">
                {/* Welcome Header */}
                <div className="text-center mb-8 animate-fade-in">
                    <div className="text-6xl mb-4">👋</div>
                    <h1 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-2">
                        Welcome, {user?.firstName || 'Trainer'}!
                    </h1>
                    <p className="text-lg text-gray-600 max-w-md">
                        Let's set up your profile. Choose a nickname for the leaderboards!
                    </p>
                </div>

                {/* Nickname Input Card */}
                <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 sm:p-8 animate-slide-up">
                    <div className="mb-6">
                        <label htmlFor="nickname" className="block text-sm font-semibold text-gray-700 mb-2">
                            Nickname (Optional)
                        </label>
                        <div className="relative">
                            <input
                                id="nickname"
                                type="text"
                                value={nickname}
                                onChange={(e) => setNickname(e.target.value.replace(/[^a-zA-Z0-9_]/g, ''))}
                                placeholder="e.g., FitnessPro123"
                                maxLength={20}
                                className={`w-full px-4 py-3 border-2 rounded-xl text-lg focus:outline-none transition-colors ${
                                    nickname.length > 0
                                        ? validationResult?.available
                                            ? 'border-green-400 focus:border-green-500'
                                            : validationResult?.available === false
                                                ? 'border-red-400 focus:border-red-500'
                                                : 'border-gray-300 focus:border-blue-500'
                                        : 'border-gray-300 focus:border-blue-500'
                                }`}
                            />
                            {/* Status Icon */}
                            <div className="absolute right-3 top-1/2 -translate-y-1/2">
                                {isChecking ? (
                                    <div className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
                                ) : nickname.length >= 3 && validationResult ? (
                                    validationResult.available ? (
                                        <span className="text-green-500 text-xl">✓</span>
                                    ) : (
                                        <span className="text-red-500 text-xl">✗</span>
                                    )
                                ) : null}
                            </div>
                        </div>

                        {/* Validation Message */}
                        {nickname.length > 0 && nickname.length < 3 && (
                            <p className="mt-2 text-sm text-amber-600">
                                Nickname must be at least 3 characters
                            </p>
                        )}
                        {validationResult && nickname.length >= 3 && (
                            <p className={`mt-2 text-sm ${validationResult.available ? 'text-green-600' : 'text-red-600'}`}>
                                {validationResult.message}
                            </p>
                        )}

                        {/* Character Count */}
                        <div className="mt-2 flex justify-between text-xs text-gray-400">
                            <span>Letters, numbers, and underscores only</span>
                            <span>{nickname.length}/20</span>
                        </div>
                    </div>

                    {/* Info Box */}
                    <div className="bg-blue-50 rounded-xl p-4 mb-6">
                        <div className="flex items-start gap-3">
                            <span className="text-2xl">💡</span>
                            <div className="text-sm text-blue-800">
                                <p className="font-medium mb-1">Why a nickname?</p>
                                <p className="text-blue-600">
                                    Your nickname appears on leaderboards and when you share achievements. 
                                    You can skip this and add one later!
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Buttons */}
                    <div className="space-y-3">
                        <button
                            onClick={handleContinue}
                            disabled={!isValid || isChecking}
                            className="w-full py-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white font-semibold rounded-xl hover:from-blue-700 hover:to-purple-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed transform hover:scale-[1.02] active:scale-[0.98]"
                        >
                            Continue
                        </button>
                        <button
                            onClick={handleSkip}
                            className="w-full py-3 text-gray-500 font-medium hover:text-gray-700 transition-colors"
                        >
                            Skip for now
                        </button>
                    </div>
                </div>

                {/* Step Indicator */}
                <div className="mt-8 flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-blue-500" />
                    <div className="w-3 h-3 rounded-full bg-gray-300" />
                    <div className="w-3 h-3 rounded-full bg-gray-300" />
                </div>
                <p className="mt-2 text-sm text-gray-500">Step 1 of 3</p>
            </div>
        </div>
    );
};

export default NicknameSelectionPage;
