import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../../contexts/AuthContext';
import {useRive} from '@rive-app/react-canvas';

// Suggested pet names
const SUGGESTED_NAMES = [
    'Luna', 'Shadow', 'Max', 'Bella', 'Storm', 'Rocky',
    'Nova', 'Zeus', 'Kira', 'Blaze', 'Echo', 'Titan'
];

const NameYourPetPage: React.FC = () => {
    const navigate = useNavigate();
    const {completeOnboarding, checkPetNameValidity, loading} = useAuth();

    const [petName, setPetName] = useState('');
    const [isChecking, setIsChecking] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [validationResult, setValidationResult] = useState<{
        valid: boolean;
        message: string;
    } | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [debounceTimer, setDebounceTimer] = useState<NodeJS.Timeout | null>(null);

    // Get nickname from previous step
    const savedNickname = sessionStorage.getItem('onboarding_nickname') || '';

    // Rive animation
    const {RiveComponent} = useRive({
        src: '/assets/pet/rive/baby_wolf_idle.riv',
        artboard: 'Artboard',
        animations: ['Idle Natural', 'Idle Breathing'],
        autoplay: true,
    });

    // Debounced pet name validation
    useEffect(() => {
        if (debounceTimer) {
            clearTimeout(debounceTimer);
        }

        if (petName.length === 0) {
            setValidationResult(null);
            return;
        }

        const timer = setTimeout(async () => {
            setIsChecking(true);
            try {
                const result = await checkPetNameValidity(petName);
                setValidationResult(result);
            } catch (error) {
                setValidationResult({valid: false, message: 'Failed to validate name'});
            } finally {
                setIsChecking(false);
            }
        }, 300);

        setDebounceTimer(timer);

        return () => {
            if (timer) clearTimeout(timer);
        };
    }, [petName]);

    const handleSelectSuggestion = (name: string) => {
        setPetName(name);
    };

    const handleComplete = async () => {
        if (petName.length === 0) {
            setError('Please give your pet a name!');
            return;
        }

        if (validationResult && !validationResult.valid) {
            setError(validationResult.message);
            return;
        }

        setIsSubmitting(true);
        setError(null);

        try {
            await completeOnboarding({
                nickname: savedNickname || undefined,
                petName: petName,
            });

            // Clear session storage
            sessionStorage.removeItem('onboarding_nickname');

            // Navigation handled by AuthContext
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to complete setup');
            setIsSubmitting(false);
        }
    };

    const handleBack = () => {
        navigate('/onboarding/meet-pet');
    };

    const isValid = petName.length > 0 && (validationResult?.valid !== false);

    return (
        <div
            className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-rose-50 flex flex-col overflow-hidden">
            {/* Progress Bar */}
            <div className="w-full h-2 bg-gray-200">
                <div
                    className="h-full bg-gradient-to-r from-blue-500 to-purple-500 w-full transition-all duration-500"/>
            </div>

            <div className="flex-1 flex flex-col items-center justify-center px-4 py-8">
                {/* Header */}
                <div className="text-center mb-4 animate-fade-in">
                    <h1 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-2">
                        Name Your Pet! ✨
                    </h1>
                    <p className="text-lg text-gray-600 max-w-md mx-auto">
                        Give your new companion a special name
                    </p>
                </div>

                {/* Pet Preview */}
                <div className="relative w-72 h-72 sm:w-80 sm:h-80 mb-4 animate-slide-up overflow-hidden">
                    <div
                        className="absolute inset-0 bg-gradient-to-br from-purple-200/50 to-pink-200/50 rounded-full blur-2xl"/>
                    <div className="relative w-full h-full scale-[1.8] origin-center">
                        <RiveComponent/>
                    </div>
                    {/* Name Display */}
                    {petName && (
                        <div
                            className="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-white px-4 py-1 rounded-full shadow-lg border-2 border-purple-200">
                            <span className="font-bold text-purple-700">{petName}</span>
                        </div>
                    )}
                </div>

                {/* Name Input Card */}
                <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 animate-slide-up">
                    {/* Error Display */}
                    {error && (
                        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                            {error}
                        </div>
                    )}

                    {/* Name Input */}
                    <div className="mb-4">
                        <label htmlFor="petName" className="block text-sm font-semibold text-gray-700 mb-2">
                            Pet Name
                        </label>
                        <div className="relative">
                            <input
                                id="petName"
                                type="text"
                                value={petName}
                                onChange={(e) => setPetName(e.target.value)}
                                placeholder="Enter a name..."
                                maxLength={50}
                                className={`w-full px-4 py-3 border-2 rounded-xl text-lg focus:outline-none transition-colors ${
                                    petName.length > 0
                                        ? validationResult?.valid
                                            ? 'border-green-400 focus:border-green-500'
                                            : validationResult?.valid === false
                                                ? 'border-red-400 focus:border-red-500'
                                                : 'border-gray-300 focus:border-purple-500'
                                        : 'border-gray-300 focus:border-purple-500'
                                }`}
                            />
                            {/* Status Icon */}
                            <div className="absolute right-3 top-1/2 -translate-y-1/2">
                                {isChecking ? (
                                    <div
                                        className="w-5 h-5 border-2 border-purple-500 border-t-transparent rounded-full animate-spin"/>
                                ) : petName.length > 0 && validationResult ? (
                                    validationResult.valid ? (
                                        <span className="text-green-500 text-xl">✓</span>
                                    ) : (
                                        <span className="text-red-500 text-xl">✗</span>
                                    )
                                ) : null}
                            </div>
                        </div>
                        {validationResult && !validationResult.valid && (
                            <p className="mt-2 text-sm text-red-600">{validationResult.message}</p>
                        )}
                    </div>

                    {/* Suggested Names */}
                    <div className="mb-6">
                        <p className="text-sm font-medium text-gray-600 mb-2">Or pick a suggestion:</p>
                        <div className="flex flex-wrap gap-2">
                            {SUGGESTED_NAMES.map((name) => (
                                <button
                                    key={name}
                                    onClick={() => handleSelectSuggestion(name)}
                                    className={`px-3 py-1.5 rounded-full text-sm font-medium transition-all ${
                                        petName === name
                                            ? 'bg-purple-500 text-white'
                                            : 'bg-purple-100 text-purple-700 hover:bg-purple-200'
                                    }`}
                                >
                                    {name}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Summary */}
                    {savedNickname && (
                        <div className="bg-gray-50 rounded-xl p-3 mb-4 text-sm">
                            <p className="text-gray-600">
                                <span className="font-medium">Your nickname:</span>{' '}
                                <span className="text-purple-600 font-semibold">{savedNickname}</span>
                            </p>
                        </div>
                    )}

                    {/* Complete Button */}
                    <button
                        onClick={handleComplete}
                        disabled={!isValid || isSubmitting || isChecking}
                        className="w-full py-4 bg-gradient-to-r from-purple-600 to-pink-600 text-white font-semibold rounded-xl hover:from-purple-700 hover:to-pink-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed transform hover:scale-[1.02] active:scale-[0.98] shadow-lg"
                    >
                        {isSubmitting ? (
                            <div className="flex items-center justify-center gap-2">
                                <div
                                    className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"/>
                                <span>Setting up your pet...</span>
                            </div>
                        ) : (
                            <span>Complete Setup! 🎉</span>
                        )}
                    </button>

                    <button
                        onClick={handleBack}
                        disabled={isSubmitting}
                        className="w-full mt-3 py-3 text-gray-500 font-medium hover:text-gray-700 transition-colors disabled:opacity-50"
                    >
                        ← Back
                    </button>
                </div>

                {/* Step Indicator */}
                <div className="mt-8 flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-blue-500"/>
                    <div className="w-3 h-3 rounded-full bg-blue-500"/>
                    <div className="w-3 h-3 rounded-full bg-blue-500"/>
                </div>
                <p className="mt-2 text-sm text-gray-500">Step 3 of 3</p>
            </div>
        </div>
    );
};

export default NameYourPetPage;