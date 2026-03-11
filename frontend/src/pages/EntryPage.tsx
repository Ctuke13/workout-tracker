import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useRive } from '@rive-app/react-canvas';
import { useAuth } from '../contexts/AuthContext';
import { Gender } from '../types/enums';
import { ChevronRight, Mail, Lock, User, Sparkles, Heart } from 'lucide-react';

// ==================== TYPES ====================

type Step = 'splash' | 'name' | 'account' | 'personalInfo' | 'petName' | 'welcome';

interface EntryFormData {
    firstName: string;
    email: string;
    password: string;
    confirmPassword: string;
    dateOfBirth: string;
    zipcode: string;
    petName: string;
}

// ==================== MAIN COMPONENT ====================

const EntryPage: React.FC = () => {
    const navigate = useNavigate();
    const { register, completeOnboarding, isAuthenticated } = useAuth();

    const [currentStep, setCurrentStep] = useState<Step>('splash');
    const [formData, setFormData] = useState<EntryFormData>({
        firstName: '',
        email: '',
        password: '',
        confirmPassword: '',
        dateOfBirth: '',
        zipcode: '',
        petName: '',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Rive animation for the wolf
    const { RiveComponent } = useRive({
        src: '/assets/pet/rive/baby_wolf_idle.riv',
        artboard: 'Artboard',
        animations: ['Idle Natural', 'Idle Breathing'],
        autoplay: true,
    });

    // If already authenticated, redirect to pet page
    useEffect(() => {
        if (isAuthenticated) {
            navigate('/pet');
        }
    }, [isAuthenticated, navigate]);

    // ==================== STEP NAVIGATION ====================

    const goToNextStep = () => {
        const steps: Step[] = ['splash', 'name', 'account', 'personalInfo', 'petName', 'welcome'];
        const currentIndex = steps.indexOf(currentStep);
        if (currentIndex < steps.length - 1) {
            setCurrentStep(steps[currentIndex + 1]);
            setError(null);
        }
    };

    // ==================== FORM SUBMISSION ====================

    const handleSubmit = async () => {
        setLoading(true);
        setError(null);

        try {
            // Validate
            if (!formData.firstName.trim()) {
                throw new Error('Please enter your name');
            }
            if (!formData.email.trim() || !formData.email.includes('@')) {
                throw new Error('Please enter a valid email');
            }
            if (formData.password.length < 8) {
                throw new Error('Password must be at least 8 characters');
            }
            if (formData.password !== formData.confirmPassword) {
                throw new Error('Passwords do not match');
            }
            if (!formData.dateOfBirth) {
                throw new Error('Please enter your birthday');
            }
            if (!formData.zipcode || formData.zipcode.length !== 5) {
                throw new Error('Please enter a valid 5-digit zip code');
            }
            if (!formData.petName.trim()) {
                throw new Error('Please name your pet');
            }

            // Clean email prefix for username (remove special characters)
            const emailPrefix = formData.email.split('@')[0].replace(/[^a-zA-Z0-9]/g, '');
            const randomDigits = Math.floor(1000 + Math.random() * 9000);
            const username = `${emailPrefix}${randomDigits}`;

            console.log('🔍 Registration data:', {
                email: formData.email,
                username,
                firstName: formData.firstName,
                dateOfBirth: formData.dateOfBirth,
                zipcode: formData.zipcode,
                petName: formData.petName,
            });

            // Register with collected data
            const registrationData = {
                email: formData.email,
                username: username,
                password: formData.password,
                firstName: formData.firstName,
                lastName: 'Trainer',
                dateOfBirth: formData.dateOfBirth, // ✅ Real user data
                gender: 'OTHER' as any,
                zipcode: formData.zipcode, // ✅ Real user data
                agreeToTerms: true,
            };

            console.log('📤 Sending registration...');
            await register(registrationData);

            console.log('✅ Registration successful, completing onboarding...');

            // Complete onboarding with pet name
            await completeOnboarding({
                petName: formData.petName,
            });

            console.log('✅ Onboarding complete!');

            // Show success step
            setCurrentStep('welcome');

            // Auto-redirect after 2 seconds
            setTimeout(() => {
                navigate('/pet');
            }, 2000);

        } catch (err) {
            console.error('❌ Submission error:', err);
            const errorMessage = err instanceof Error ? err.message : 'Something went wrong';
            setError(errorMessage);
            setLoading(false);
        }
    };

    // ==================== PROGRESS BAR ====================

    const getProgress = (): number => {
        switch (currentStep) {
            case 'splash': return 0;
            case 'name': return 20;
            case 'account': return 40;
            case 'personalInfo': return 60;
            case 'petName': return 80;
            case 'welcome': return 100;
            default: return 0;
        }
    };

    // ==================== RENDER STEPS ====================

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-pink-100 flex flex-col">
            {/* Progress Bar */}
            <div className="w-full h-1 bg-white/30">
                <div
                    className="h-full bg-gradient-to-r from-purple-600 to-blue-600 transition-all duration-500"
                    style={{ width: `${getProgress()}%` }}
                />
            </div>

            {/* Content */}
            <div className="flex-1 flex items-center justify-center p-4">
                <div className="w-full max-w-md">
                    {currentStep === 'splash' && (
                        <SplashStep onContinue={goToNextStep} RiveComponent={RiveComponent} />
                    )}

                    {currentStep === 'name' && (
                        <NameStep
                            value={formData.firstName}
                            onChange={(value) => setFormData({ ...formData, firstName: value })}
                            onContinue={goToNextStep}
                            error={error}
                        />
                    )}

                    {currentStep === 'account' && (
                        <AccountStep
                            email={formData.email}
                            password={formData.password}
                            confirmPassword={formData.confirmPassword}
                            onChange={(field, value) => setFormData({ ...formData, [field]: value })}
                            onContinue={goToNextStep}
                            error={error}
                        />
                    )}

                    {currentStep === 'personalInfo' && (
                        <PersonalInfoStep
                            dateOfBirth={formData.dateOfBirth}
                            zipcode={formData.zipcode}
                            onChange={(field, value) => setFormData({ ...formData, [field]: value })}
                            onContinue={goToNextStep}
                            error={error}
                        />
                    )}

                    {currentStep === 'petName' && (
                        <PetNameStep
                            firstName={formData.firstName}
                            petName={formData.petName}
                            onChange={(value) => setFormData({ ...formData, petName: value })}
                            onSubmit={handleSubmit}
                            loading={loading}
                            error={error}
                            RiveComponent={RiveComponent}
                        />
                    )}

                    {currentStep === 'welcome' && (
                        <WelcomeStep firstName={formData.firstName} petName={formData.petName} />
                    )}
                </div>
            </div>
        </div>
    );
};

// ==================== STEP 1: SPLASH ====================

interface SplashStepProps {
    onContinue: () => void;
    RiveComponent: React.ComponentType;
}

const SplashStep: React.FC<SplashStepProps> = ({ onContinue, RiveComponent }) => {
    const [show, setShow] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        setTimeout(() => setShow(true), 100);
    }, []);

    return (
        <div
            className={`text-center transition-all duration-700 ${
                show ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'
            }`}
        >
            {/* EvoPet Logo */}
            <div className="flex items-center justify-center gap-3 mb-6">
                <img
                    src="/assets/branding/EvoPet_icon.png"
                    alt="EvoPet"
                    className="w-12 h-12 rounded-2xl object-contain shadow-lg"
                />
                <img
                    src="/assets/branding/EvoPet_txt_lg.png"
                    alt="EvoPet"
                    className="h-10 w-auto object-contain"
                />
            </div>

            {/* Wolf Animation */}
            <div className="relative w-56 h-56 mx-auto mb-4">
                <div className="absolute inset-0 bg-gradient-to-br from-purple-200/50 to-blue-200/50 rounded-full blur-3xl animate-pulse" />
                <div className="relative w-full h-full scale-[1.8] origin-center">
                    <RiveComponent />
                </div>
            </div>

            {/* Core Hook - the concept in one line */}
            <h1 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-2">
                Work out. Your wolf evolves.
            </h1>
            <p className="text-base text-gray-600 mb-6 max-w-xs mx-auto">
                EvoPet is a fitness tracker where every workout feeds, levels up, and evolves your virtual companion.
            </p>

            {/* Feature tiles — pet-specific */}
            <div className="grid grid-cols-3 gap-3 mb-6 max-w-sm mx-auto">
                <div className="bg-white/60 backdrop-blur-sm rounded-xl p-3 text-center">
                    <span className="text-2xl block mb-1">🏋️</span>
                    <p className="text-xs font-medium text-gray-700">Log Workouts</p>
                </div>
                <div className="bg-white/60 backdrop-blur-sm rounded-xl p-3 text-center">
                    <span className="text-2xl block mb-1">🐺</span>
                    <p className="text-xs font-medium text-gray-700">Feed Your Pet</p>
                </div>
                <div className="bg-white/60 backdrop-blur-sm rounded-xl p-3 text-center">
                    <span className="text-2xl block mb-1">⚡</span>
                    <p className="text-xs font-medium text-gray-700">Rank Up</p>
                </div>
            </div>

            {/* Primary CTA */}
            <button
                onClick={onContinue}
                className="w-full py-4 bg-gradient-to-r from-purple-600 to-pink-500 hover:from-purple-700 hover:to-pink-600 text-white text-lg font-bold rounded-2xl shadow-lg transition-all transform hover:scale-105 active:scale-95 flex items-center justify-center gap-2 mb-3"
            >
                Get Started — It's Free
                <ChevronRight className="w-6 h-6" />
            </button>

            {/* Sign in path for returning users */}
            <button
                onClick={() => navigate('/login')}
                className="w-full py-3 text-purple-600 font-semibold text-sm hover:text-purple-800 transition-colors"
            >
                Already have an account? <span className="underline">Sign in</span>
            </button>

            <p className="mt-2 text-xs text-gray-400">Takes about 3 minutes to set up</p>
        </div>
    );
};

// ==================== STEP 2: NAME ====================

interface NameStepProps {
    value: string;
    onChange: (value: string) => void;
    onContinue: () => void;
    error: string | null;
}

const NameStep: React.FC<NameStepProps> = ({ value, onChange, onContinue, error }) => {
    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && value.trim()) {
            onContinue();
        }
    };

    return (
        <div className="animate-fade-in">
            <div className="text-center mb-8">
                <div className="text-6xl mb-4 animate-bounce">👋</div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">
                    What should we call you?
                </h2>
                <p className="text-gray-600">Just your first name</p>
            </div>

            <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-6 shadow-xl overflow-hidden">
                {error && (
                    <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                        {error}
                    </div>
                )}

                <div className="relative mb-6">
                    <div className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none">
                        <User className="w-5 h-5 text-gray-400" />
                    </div>
                    <input
                        type="text"
                        value={value}
                        onChange={(e) => onChange(e.target.value)}
                        onKeyPress={handleKeyPress}
                        placeholder="Your name..."
                        className="w-10/12 pl-11 pr-4 py-2.5 border border-gray-200 rounded-lg text-base focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 transition-all text-gray-900 placeholder-gray-400"
                        autoFocus
                    />
                </div>

                <button
                    onClick={onContinue}
                    disabled={!value.trim()}
                    className="w-full py-4 bg-gradient-to-r from-purple-600 to-pink-500 text-white text-lg font-bold rounded-xl hover:from-purple-700 hover:to-pink-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed transform hover:scale-105 active:scale-95 shadow-md"
                >
                    Continue
                </button>
            </div>
        </div>
    );
};

// ==================== STEP 3: ACCOUNT ====================

interface AccountStepProps {
    email: string;
    password: string;
    confirmPassword: string;
    onChange: (field: string, value: string) => void;
    onContinue: () => void;
    error: string | null;
}

const AccountStep: React.FC<AccountStepProps> = ({
    email,
    password,
    confirmPassword,
    onChange,
    onContinue,
    error,
}) => {
    const isValid = email.includes('@') && password.length >= 8 && password === confirmPassword;

    return (
        <div className="animate-fade-in">
            <div className="text-center mb-8">
                <div className="text-6xl mb-4">🔐</div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">
                    Let's save your progress
                </h2>
                <p className="text-gray-600">Create your account</p>
            </div>

            <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-6 shadow-xl space-y-4 overflow-hidden">
                {error && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                        {error}
                    </div>
                )}

                {/* Email */}
                <div className="relative">
                    <div className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none">
                        <Mail className="w-5 h-5 text-gray-400" />
                    </div>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => onChange('email', e.target.value)}
                        placeholder="Email address..."
                        className="w-10/12 pl-11 pr-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 transition-all text-gray-900 placeholder-gray-400"
                    />
                </div>

                {/* Password */}
                <div className="relative">
                    <div className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none">
                        <Lock className="w-5 h-5 text-gray-400" />
                    </div>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => onChange('password', e.target.value)}
                        placeholder="Password (8+ characters)..."
                        className="w-10/12 pl-11 pr-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 transition-all text-gray-900 placeholder-gray-400"
                    />
                </div>

                {/* Confirm Password */}
                <div className="relative">
                    <div className="absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none">
                        <Lock className="w-5 h-5 text-gray-400" />
                    </div>
                    <input
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => onChange('confirmPassword', e.target.value)}
                        placeholder="Confirm password..."
                        className="w-10/12 pl-11 pr-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 transition-all text-gray-900 placeholder-gray-400"
                    />
                </div>

                <button
                    onClick={onContinue}
                    disabled={!isValid}
                    className="w-full py-4 bg-gradient-to-r from-purple-600 to-pink-500 text-white text-lg font-bold rounded-xl hover:from-purple-700 hover:to-pink-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed transform hover:scale-105 active:scale-95"
                >
                    Continue
                </button>

                <p className="text-xs text-center text-gray-500">
                    By continuing, you agree to our Terms & Privacy Policy
                </p>
            </div>
        </div>
    );
};

// ==================== STEP 4: PERSONAL INFO ====================

interface PersonalInfoStepProps {
    dateOfBirth: string;
    zipcode: string;
    onChange: (field: string, value: string) => void;
    onContinue: () => void;
    error: string | null;
}

const PersonalInfoStep: React.FC<PersonalInfoStepProps> = ({
    dateOfBirth,
    zipcode,
    onChange,
    onContinue,
    error,
}) => {
    const isValid = dateOfBirth && zipcode.length === 5;

    // Calculate max date (must be at least 13 years old)
    const today = new Date();
    const maxDate = new Date(today.getFullYear() - 13, today.getMonth(), today.getDate())
        .toISOString()
        .split('T')[0];

    // Min date (reasonable limit - 100 years ago)
    const minDate = new Date(today.getFullYear() - 100, 0, 1)
        .toISOString()
        .split('T')[0];

    return (
        <div className="animate-fade-in">
            <div className="text-center mb-8">
                <div className="text-6xl mb-4">📍</div>
                <h2 className="text-3xl font-bold text-gray-900 mb-2">
                    Just a bit more about you...
                </h2>
                <p className="text-gray-600">Helps us personalize your experience</p>
            </div>

            <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-6 shadow-xl space-y-4 overflow-hidden">
                {error && (
                    <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                        {error}
                    </div>
                )}

                {/* Birthday */}
                <div>
                    <label htmlFor="dateOfBirth" className="block text-sm font-medium text-gray-700 mb-2">
                        Your Birthday
                    </label>
                    <input
                        id="dateOfBirth"
                        type="date"
                        value={dateOfBirth}
                        onChange={(e) => onChange('dateOfBirth', e.target.value)}
                        min={minDate}
                        max={maxDate}
                        className="w-10/12 px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 transition-all text-gray-900 placeholder-gray-400"
                    />
                    <p className="mt-1 text-xs text-gray-500">Must be at least 13 years old</p>
                </div>

                {/* Zip Code */}
                <div>
                    <label htmlFor="zipcode" className="block text-sm font-medium text-gray-700 mb-2">
                        Zip Code
                    </label>
                    <input
                        id="zipcode"
                        type="text"
                        value={zipcode}
                        onChange={(e) => {
                            const value = e.target.value.replace(/\D/g, '').slice(0, 5);
                            onChange('zipcode', value);
                        }}
                        placeholder="12345"
                        maxLength={5}
                        className="w-10/12 px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 transition-all text-gray-900 placeholder-gray-400"
                    />
                    <p className="mt-1 text-xs text-gray-500">
                        {zipcode.length}/5 digits
                    </p>
                </div>

                {/* Info Box */}
                <div className="bg-blue-50 rounded-xl p-4">
                    <div className="flex items-start gap-3">
                        <span className="text-2xl">🔒</span>
                        <div className="text-sm text-blue-800">
                            <p className="font-medium mb-1">Privacy Note</p>
                            <p className="text-blue-600">
                                This helps us show you local features and age-appropriate content.
                                You can update this later in settings.
                            </p>
                        </div>
                    </div>
                </div>

                <button
                    onClick={onContinue}
                    disabled={!isValid}
                    className="w-10/12 py-4 bg-gradient-to-r from-purple-600 to-pink-500 text-white text-lg font-bold rounded-xl hover:from-purple-700 hover:to-pink-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed transform hover:scale-105 active:scale-95"
                >
                    Continue
                </button>
            </div>
        </div>
    );
};

// ==================== STEP 5: PET NAME ====================

interface PetNameStepProps {
    firstName: string;
    petName: string;
    onChange: (value: string) => void;
    onSubmit: () => void;
    loading: boolean;
    error: string | null;
    RiveComponent: React.ComponentType;
}

const PetNameStep: React.FC<PetNameStepProps> = ({
    firstName,
    petName,
    onChange,
    onSubmit,
    loading,
    error,
    RiveComponent,
}) => {
    const SUGGESTED_NAMES = ['Luna', 'Shadow', 'Max', 'Bella', 'Storm', 'Rocky', 'Nova', 'Zeus'];

    return (
        <div className="animate-fade-in">
            <div className="text-center mb-6">
                <h2 className="text-3xl font-bold text-gray-900 mb-2">
                    Name Your Wolf! ✨
                </h2>
                <p className="text-gray-600">Give your companion a special name</p>
            </div>

            {/* Wolf Preview */}
            <div className="relative w-56 h-56 mx-auto mb-6">
                <div className="absolute inset-0 bg-gradient-to-br from-purple-200/50 to-pink-200/50 rounded-full blur-2xl" />
                <div className="relative w-full h-full scale-[1.8] origin-center">
                    <RiveComponent />
                </div>
                {petName && (
                    <div className="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-white px-4 py-1 rounded-full shadow-lg border-2 border-purple-200">
                        <span className="font-bold text-purple-700">{petName}</span>
                    </div>
                )}
            </div>

            <div className="bg-white/80 backdrop-blur-sm rounded-2xl p-6 shadow-xl overflow-hidden">
                {error && (
                    <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                        {error}
                    </div>
                )}

                {/* Name Input */}
                <div className="mb-4">
                    <input
                        type="text"
                        value={petName}
                        onChange={(e) => onChange(e.target.value)}
                        placeholder="Enter a name..."
                        maxLength={50}
                        className="w-11/12 px-4 py-3 border-2 border-gray-300 rounded-xl text-lg focus:outline-none focus:border-purple-500 transition-colors"
                    />
                </div>

                {/* Suggested Names */}
                <div className="mb-6">
                    <p className="text-sm font-medium text-gray-600 mb-2">Or pick a suggestion:</p>
                    <div className="flex flex-wrap gap-2">
                        {SUGGESTED_NAMES.map((name) => (
                            <button
                                key={name}
                                onClick={() => onChange(name)}
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

                {/* Submit Button */}
                <button
                    onClick={onSubmit}
                    disabled={!petName.trim() || loading}
                    className="w-full py-4 bg-gradient-to-r from-purple-600 to-pink-600 text-white text-lg font-bold rounded-xl hover:from-purple-700 hover:to-pink-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed transform hover:scale-105 active:scale-95 shadow-lg"
                >
                    {loading ? (
                        <div className="flex items-center justify-center gap-2">
                            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                            <span>Creating your journey...</span>
                        </div>
                    ) : (
                        <span>Complete! 🎉</span>
                    )}
                </button>
            </div>
        </div>
    );
};

// ==================== STEP 6: WELCOME ====================

interface WelcomeStepProps {
    firstName: string;
    petName: string;
}

const WelcomeStep: React.FC<WelcomeStepProps> = ({ firstName, petName }) => {
    return (
        <div className="text-center animate-fade-in">
            {/* Confetti */}
            <div className="fixed inset-0 pointer-events-none">
                {[...Array(30)].map((_, i) => (
                    <div
                        key={i}
                        className="absolute text-2xl animate-confetti"
                        style={{
                            left: `${Math.random() * 100}%`,
                            top: '-10%',
                            animationDelay: `${Math.random() * 2}s`,
                            animationDuration: `${2 + Math.random() * 2}s`,
                        }}
                    >
                        {['🎉', '⭐', '💪', '🔥', '✨', '💎'][Math.floor(Math.random() * 6)]}
                    </div>
                ))}
            </div>

            <div className="relative z-10">
                <div className="text-8xl mb-6 animate-bounce">🎉</div>
                <h1 className="text-4xl font-bold text-gray-900 mb-4">
                    Welcome, {firstName}!
                </h1>
                <p className="text-2xl text-gray-700 mb-2">
                    {petName} is so excited to meet you!
                </p>
                <p className="text-lg text-gray-600 mb-8">
                    Your journey begins now...
                </p>

                <div className="inline-flex items-center gap-2 text-purple-600 font-semibold">
                    <div className="w-2 h-2 bg-purple-600 rounded-full animate-pulse" />
                    <div className="w-2 h-2 bg-purple-600 rounded-full animate-pulse" style={{ animationDelay: '0.2s' }} />
                    <div className="w-2 h-2 bg-purple-600 rounded-full animate-pulse" style={{ animationDelay: '0.4s' }} />
                </div>
            </div>

            <style>{`
                @keyframes confetti {
                    0% {
                        transform: translateY(0) rotate(0deg);
                        opacity: 1;
                    }
                    100% {
                        transform: translateY(100vh) rotate(720deg);
                        opacity: 0;
                    }
                }
                .animate-confetti {
                    animation: confetti linear forwards;
                }
                .animate-fade-in {
                    animation: fadeIn 0.5s ease-out forwards;
                }
                @keyframes fadeIn {
                    from {
                        opacity: 0;
                        transform: translateY(20px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
            `}</style>
        </div>
    );
};

export default EntryPage;