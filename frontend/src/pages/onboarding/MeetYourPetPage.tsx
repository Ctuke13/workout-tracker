import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {useRive, useStateMachineInput} from '@rive-app/react-canvas';

const MeetYourPetPage: React.FC = () => {
    const navigate = useNavigate();
    const [isLoaded, setIsLoaded] = useState(false);
    const [showContent, setShowContent] = useState(false);

    // Rive animation setup
    const {rive, RiveComponent} = useRive({
        src: '/assets/pet/rive/baby_wolf_idle.riv',
        artboard: 'Artboard',
        animations: ['Idle Natural', 'Idle Breathing'],
        autoplay: true,
        onLoad: () => {
            console.log('🐺 Wolf Rive animation loaded!');
            setIsLoaded(true);
        },
    });

    // Trigger entrance animation
    useEffect(() => {
        if (isLoaded) {
            const timer = setTimeout(() => setShowContent(true), 300);
            return () => clearTimeout(timer);
        }
    }, [isLoaded]);

    const handleContinue = () => {
        navigate('/onboarding/name-pet');
    };

    const handleBack = () => {
        navigate('/onboarding/nickname');
    };

    return (
        <div
            className="min-h-screen bg-gradient-to-br from-amber-50 via-orange-50 to-yellow-50 flex flex-col overflow-hidden">
            {/* Progress Bar */}
            <div className="w-full h-2 bg-gray-200">
                <div className="h-full bg-gradient-to-r from-blue-500 to-purple-500 w-2/3 transition-all duration-500"/>
            </div>

            <div className="flex-1 flex flex-col items-center justify-center px-4 py-8">
                {/* Header */}
                <div
                    className={`text-center mb-6 transition-all duration-700 ${showContent ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-4'}`}>
                    <h1 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-2">
                        Meet Your Pet Companion! 🎉
                    </h1>
                    <p className="text-lg text-gray-600 max-w-md mx-auto">
                        This is your fitness buddy. Take care of them, and they'll motivate you on your journey!
                    </p>
                </div>

                {/* Pet Display Area */}
                <div
                    className={`relative w-full max-w-sm aspect-square mb-6 transition-all duration-700 delay-200 overflow-hidden ${showContent ? 'opacity-100 scale-100' : 'opacity-0 scale-90'}`}>
                    {/* Glow Effect */}
                    <div
                        className="absolute inset-0 bg-gradient-to-br from-amber-200/50 to-orange-200/50 rounded-full blur-3xl animate-pulse"/>

                    {/* Pet Container */}
                    <div className="relative w-full h-full flex items-center justify-center">
                        {/* Loading State */}
                        {!isLoaded && (
                            <div className="absolute inset-0 flex items-center justify-center">
                                <div className="text-center">
                                    <div
                                        className="w-16 h-16 border-4 border-amber-500 border-t-transparent rounded-full animate-spin mb-4"/>
                                    <p className="text-amber-700 font-medium">Loading your pet...</p>
                                </div>
                            </div>
                        )}

                        {/* Rive Animation */}
                        <div
                            className={`w-full h-full transition-opacity duration-500 scale-[1.8] origin-center ${isLoaded ? 'opacity-100' : 'opacity-0'}`}>
                            <RiveComponent/>
                        </div>
                    </div>

                    {/* Sparkle Effects */}
                    {showContent && (
                        <>
                            <div className="absolute top-4 left-8 text-2xl animate-bounce"
                                 style={{animationDelay: '0s'}}>✨
                            </div>
                            <div className="absolute top-12 right-6 text-xl animate-bounce"
                                 style={{animationDelay: '0.5s'}}>⭐
                            </div>
                            <div className="absolute bottom-20 left-4 text-xl animate-bounce"
                                 style={{animationDelay: '1s'}}>✨
                            </div>
                            <div className="absolute bottom-16 right-10 text-2xl animate-bounce"
                                 style={{animationDelay: '0.3s'}}>💫
                            </div>
                        </>
                    )}
                </div>

                {/* Pet Info Card */}
                <div
                    className={`w-full max-w-md bg-white rounded-2xl shadow-xl p-6 mb-6 transition-all duration-700 delay-400 ${showContent ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'}`}>
                    <div className="text-center mb-4">
                        <span
                            className="inline-block px-4 py-1 bg-amber-100 text-amber-700 rounded-full text-sm font-semibold mb-2">
                            🐺 Baby Wolf
                        </span>
                        <h2 className="text-xl font-bold text-gray-900">Your New Companion</h2>
                    </div>

                    {/* Pet Features */}
                    <div className="grid grid-cols-2 gap-3 mb-4">
                        <div className="bg-blue-50 rounded-xl p-3 text-center">
                            <span className="text-2xl mb-1 block">💪</span>
                            <p className="text-xs font-medium text-blue-700">Workout Buddy</p>
                        </div>
                        <div className="bg-green-50 rounded-xl p-3 text-center">
                            <span className="text-2xl mb-1 block">🏆</span>
                            <p className="text-xs font-medium text-green-700">Earns Crystals</p>
                        </div>
                        <div className="bg-purple-50 rounded-xl p-3 text-center">
                            <span className="text-2xl mb-1 block">📈</span>
                            <p className="text-xs font-medium text-purple-700">Grows With You</p>
                        </div>
                        <div className="bg-pink-50 rounded-xl p-3 text-center">
                            <span className="text-2xl mb-1 block">❤️</span>
                            <p className="text-xs font-medium text-pink-700">Needs Care</p>
                        </div>
                    </div>

                    {/* Tip */}
                    <div className="bg-amber-50 rounded-xl p-3 text-sm text-amber-800 text-center">
                        <strong>Tip:</strong> Complete workouts to earn crystals and keep your pet happy!
                    </div>
                </div>

                {/* Buttons */}
                <div
                    className={`w-full max-w-md space-y-3 transition-all duration-700 delay-500 ${showContent ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'}`}>
                    <button
                        onClick={handleContinue}
                        className="w-full py-4 bg-gradient-to-r from-amber-500 to-orange-500 text-white font-semibold rounded-xl hover:from-amber-600 hover:to-orange-600 transition-all duration-200 transform hover:scale-[1.02] active:scale-[0.98] shadow-lg"
                    >
                        Let's Name Them! 🐺
                    </button>
                    <button
                        onClick={handleBack}
                        className="w-full py-3 text-gray-500 font-medium hover:text-gray-700 transition-colors"
                    >
                        ← Back
                    </button>
                </div>

                {/* Step Indicator */}
                <div className="mt-8 flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full bg-blue-500"/>
                    <div className="w-3 h-3 rounded-full bg-blue-500"/>
                    <div className="w-3 h-3 rounded-full bg-gray-300"/>
                </div>
                <p className="mt-2 text-sm text-gray-500">Step 2 of 3</p>
            </div>
        </div>
    );
};

export default MeetYourPetPage;