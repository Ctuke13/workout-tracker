import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import {
    validateEmail,
    validateFirstName, validateLastName,
    validatePassword,
    validateRegisterForm,
    validateUsername, validateZipcode
} from '../../utils/validation';
import {FormErrors, RegisterFormData} from '../../types/auth';
import { Gender } from '../../types/enums';

export const RegisterForm: React.FC = () => {
    const { register, loading, error, checkAvailability } = useAuth();
    const [currentStep, setCurrentStep] = useState(1);
    const [formData, setFormData] = useState<RegisterFormData>({
        email: '',
        username: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: '',
        dateOfBirth: '',
        gender: '',
        zipcode: '',
        agreeToTerms: false,
        fitnessGoal: '',
        experienceLevel: '',
        subscribeNewsletter: true
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [errors, setErrors] = useState<FormErrors>({});
    const [availability, setAvailability] = useState({ email: true, username: true });

    const fitnessGoals = [
        { id: 'weight_loss', label: 'Lose Weight', emoji: '🔥' },
        { id: 'muscle_gain', label: 'Build Muscle', emoji: '💪' },
        { id: 'endurance', label: 'Improve Endurance', emoji: '🏃‍♂️' },
        { id: 'strength', label: 'Get Stronger', emoji: '🏋️‍♂️' },
        { id: 'general_fitness', label: 'General Fitness', emoji: '⚡' },
        { id: 'sports_performance', label: 'Sports Performance', emoji: '🏆' }
    ];

    const experienceLevels = [
        { id: 'beginner', label: 'Beginner', description: '0-6 months' },
        { id: 'intermediate', label: 'Intermediate', description: '6 months - 2 years' },
        { id: 'advanced', label: 'Advanced', description: '2+ years' },
        { id: 'expert', label: 'Expert/Coach', description: 'Professional level' }
    ];

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value, type } = e.target;
        const checked = type === 'checkbox' ? (e.target as HTMLInputElement).checked : undefined;

        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));

        // Clear error when user starts typing
        if (errors[name]) {
            setErrors((prev: FormErrors) => ({ ...prev, [name]: '' }));
        }

        // Real-time validation for better UX
        if (value && name !== 'confirmPassword') {
            let fieldError: string | null = null;

            switch (name) {
                case 'email':
                    fieldError = validateEmail(value);
                    break;
                case 'username':
                    fieldError = validateUsername(value);
                    break;
                case 'password':
                    fieldError = validatePassword(value);
                    // Also check confirm password if it exists
                    if (formData.confirmPassword && formData.confirmPassword !== value) {
                        setErrors((prev: FormErrors) => ({ ...prev, confirmPassword: 'Passwords do not match' }));
                    }
                    break;
                case 'firstName':
                    fieldError = validateFirstName(value);
                    break;
                case 'lastName':
                    fieldError = validateLastName(value);
                    break;
                case 'zipcode':
                    fieldError = validateZipcode(value);
                    break;
            }

            if (fieldError) {
                setErrors((prev: FormErrors) => ({ ...prev, [name]: fieldError }));
            }
        }

        // Check availability for email/username
        if ((name === 'email' || name === 'username') && value.length > 2) {
            checkAvailability(name as 'email' | 'username', value).then(available => {
                setAvailability(prev => ({ ...prev, [name]: available }));
            }).catch(err => {
                console.error('Availability check failed:', err);
            });
        }
    };

    const handleNext = () => {
        const stepErrors = validateCurrentStep();
        if (Object.keys(stepErrors).length === 0) {
            setCurrentStep(prev => prev + 1);
        } else {
            setErrors(stepErrors);
        }
    };

    const handleBack = () => {
        setCurrentStep(prev => prev - 1);
        setErrors({});
    };

    const validateCurrentStep = () => {
        const stepErrors: FormErrors = {};

        if (currentStep === 1) {
            if (!formData.email) stepErrors.email = 'Email is required';
            if (!formData.username) stepErrors.username = 'Username is required';
            if (!formData.password) stepErrors.password = 'Password is required';
            if (!formData.confirmPassword) stepErrors.confirmPassword = 'Please confirm password';
            if (formData.password !== formData.confirmPassword) {
                stepErrors.confirmPassword = 'Passwords do not match';
            }
        } else if (currentStep === 2) {
            if (!formData.firstName) stepErrors.firstName = 'First name is required';
            if (!formData.lastName) stepErrors.lastName = 'Last name is required';
            if (!formData.dateOfBirth) stepErrors.dateOfBirth = 'Date of birth is required';
            if (!formData.gender) stepErrors.gender = 'Please select your gender';
            if (!formData.zipcode) stepErrors.zipcode = 'Zipcode is required';
        } else if (currentStep === 3) {
            if (!formData.agreeToTerms) stepErrors.agreeToTerms = 'You must agree to the terms';
        }

        return stepErrors;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const validationErrors = validateRegisterForm(formData);
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);

            // Scroll to first error for better UX
            const firstErrorField = Object.keys(validationErrors)[0];
            const errorElement = document.getElementById(firstErrorField);
            if (errorElement) {
                errorElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                errorElement.focus();
            }

            return;
        }

        try {
            const registerPayload = {
                email: formData.email,
                username: formData.username,
                password: formData.password,
                firstName: formData.firstName,
                lastName: formData.lastName,
                dateOfBirth: formData.dateOfBirth,
                gender: formData.gender as Gender,
                zipcode: formData.zipcode,
                agreeToTerms: formData.agreeToTerms
            };

            console.log('🚀 Attempting registration with payload:', registerPayload);
            await register(registerPayload);
            console.log('✅ Registration completed successfully!');

        } catch (err) {
            console.error('❌ Registration failed:', err);
        }
    };

    const renderStepIndicator = () => (
        <div className="flex items-center justify-center mb-6 sm:mb-8">
            {[1, 2, 3].map((step) => (
                <div key={step} className="flex items-center">
                    <div
                        className={`w-8 h-8 sm:w-10 sm:h-10 rounded-full flex items-center justify-center text-sm font-medium transition-colors ${
                            step <= currentStep
                                ? 'bg-blue-600 text-white'
                                : 'bg-gray-200 text-gray-600'
                        }`}
                    >
                        {step}
                    </div>
                    {step < 3 && (
                        <div
                            className={`w-8 sm:w-12 h-1 mx-2 transition-colors ${
                                step < currentStep ? 'bg-blue-600' : 'bg-gray-200'
                            }`}
                        />
                    )}
                </div>
            ))}
        </div>
    );

    const renderStep1 = () => (
        <div className="space-y-4 sm:space-y-6 w-full">
            <div className="text-center mb-6">
                <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-2">
                    Create Your Account
                </h2>
                <p className="text-sm sm:text-base text-gray-600">
                    Start your fitness journey with WorkoutTracker
                </p>
            </div>

            <div className="w-full">
                <label htmlFor="email" className="block text-sm font-medium text-gray-900 mb-2">
                    Email Address
                </label>
                <input
                    id="email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                    placeholder="your@email.com"
                    autoComplete="email"
                />
                <div className="mt-1 text-xs text-gray-500">
                    We'll never share your email with anyone else
                </div>
                {!availability.email && formData.email && (
                    <p className="mt-1 text-sm text-red-600">Email already taken</p>
                )}
                {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email}</p>}
            </div>

            <div className="w-full">
                <label htmlFor="username" className="block text-sm font-medium text-gray-900 mb-2">
                    Username
                </label>
                <input
                    id="username"
                    name="username"
                    type="text"
                    value={formData.username}
                    onChange={handleInputChange}
                    className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                    placeholder="your_username"
                    autoComplete="username"
                />
                <div className="mt-1 text-xs text-gray-500">
                    3-30 characters, letters, numbers, dots, underscores, hyphens only
                </div>
                {!availability.username && formData.username && (
                    <p className="mt-1 text-sm text-red-600">Username already taken</p>
                )}
                {errors.username && <p className="mt-1 text-sm text-red-600">{errors.username}</p>}
            </div>

            <div className="w-full">
                <label htmlFor="password" className="block text-sm font-medium text-gray-900 mb-2">
                    Password
                </label>
                <div className="relative w-full">
                    <input
                        id="password"
                        name="password"
                        type={showPassword ? 'text' : 'password'}
                        value={formData.password}
                        onChange={handleInputChange}
                        className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 pr-10 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                        placeholder="Create a strong password"
                        autoComplete="new-password"
                    />
                    <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 flex-shrink-0"
                    >
                        {showPassword ? '👁️' : '👁️‍🗨️'}
                    </button>
                </div>
                <div className="mt-1 text-xs text-gray-500">
                    Must be at least 8 characters with uppercase, lowercase, and number
                </div>
                {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password}</p>}
            </div>

            <div className="w-full">
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-900 mb-2">
                    Confirm Password
                </label>
                <div className="relative w-full">
                    <input
                        id="confirmPassword"
                        name="confirmPassword"
                        type={showConfirmPassword ? 'text' : 'password'}
                        value={formData.confirmPassword}
                        onChange={handleInputChange}
                        className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 pr-10 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                        placeholder="Confirm your password"
                        autoComplete="new-password"
                    />
                    <button
                        type="button"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 flex-shrink-0"
                    >
                        {showConfirmPassword ? '👁️' : '👁️‍🗨️'}
                    </button>
                </div>
                {errors.confirmPassword && <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>}
            </div>
        </div>
    );

    const renderStep2 = () => (
        <div className="space-y-4 sm:space-y-6 w-full">
            <div className="text-center mb-6">
                <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-2">
                    Personal Information
                </h2>
                <p className="text-sm sm:text-base text-gray-600">
                    Help us personalize your experience
                </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full">
                <div className="w-full min-w-0">
                    <label htmlFor="firstName" className="block text-sm font-medium text-gray-900 mb-2">
                        First Name
                    </label>
                    <input
                        id="firstName"
                        name="firstName"
                        type="text"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                        placeholder="John"
                    />
                    {errors.firstName && <p className="mt-1 text-sm text-red-600">{errors.firstName}</p>}
                </div>

                <div className="w-full min-w-0">
                    <label htmlFor="lastName" className="block text-sm font-medium text-gray-900 mb-2">
                        Last Name
                    </label>
                    <input
                        id="lastName"
                        name="lastName"
                        type="text"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                        placeholder="Doe"
                    />
                    {errors.lastName && <p className="mt-1 text-sm text-red-600">{errors.lastName}</p>}
                </div>
            </div>

            <div className="w-full">
                <label htmlFor="dateOfBirth" className="block text-sm font-medium text-gray-900 mb-2">
                    Date of Birth
                </label>
                <input
                    id="dateOfBirth"
                    name="dateOfBirth"
                    type="date"
                    value={formData.dateOfBirth}
                    onChange={handleInputChange}
                    className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                />
                <div className="mt-1 text-xs text-gray-500">
                    You must be at least 13 years old to create an account
                </div>
                {errors.dateOfBirth && <p className="mt-1 text-sm text-red-600">{errors.dateOfBirth}</p>}
            </div>

            <div className="w-full">
                <label htmlFor="gender" className="block text-sm font-medium text-gray-900 mb-2">
                    Gender
                </label>
                <select
                    id="gender"
                    name="gender"
                    value={formData.gender}
                    onChange={handleInputChange}
                    className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border bg-white"
                >
                    <option value="">Select your gender</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                    <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
                </select>
                {errors.gender && <p className="mt-1 text-sm text-red-600">{errors.gender}</p>}
            </div>

            <div className="w-full">
                <label htmlFor="zipcode" className="block text-sm font-medium text-gray-900 mb-2">
                    Zipcode
                </label>
                <input
                    id="zipcode"
                    name="zipcode"
                    type="text"
                    value={formData.zipcode}
                    onChange={handleInputChange}
                    className="w-full max-w-full min-w-0 px-3 sm:px-4 py-3 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent box-border"
                    placeholder="12345"
                    maxLength={5}
                />
                <div className="mt-1 text-xs text-gray-500">
                    5-digit US zipcode required
                </div>
                {errors.zipcode && <p className="mt-1 text-sm text-red-600">{errors.zipcode}</p>}
            </div>
        </div>
    );

    const renderStep3 = () => (
        <div className="space-y-6 w-full">
            <div className="text-center mb-6">
                <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-2">
                    Almost Done!
                </h2>
                <p className="text-sm sm:text-base text-gray-600">
                    Optional preferences to personalize your experience
                </p>
            </div>

            <div className="w-full">
                <label className="block text-sm font-medium text-gray-900 mb-3">
                    What's your primary fitness goal? (Optional)
                </label>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full">
                    {fitnessGoals.map((goal) => (
                        <button
                            key={goal.id}
                            type="button"
                            onClick={() => handleInputChange({ target: { name: 'fitnessGoal', value: goal.id } } as any)}
                            className={`w-full p-3 sm:p-4 rounded-lg border-2 text-left transition-all ${
                                formData.fitnessGoal === goal.id
                                    ? 'border-blue-500 bg-blue-50 text-blue-700'
                                    : 'border-gray-300 hover:border-gray-400'
                            }`}
                        >
                            <div className="flex items-center">
                                <span className="text-xl mr-3 flex-shrink-0">{goal.emoji}</span>
                                <span className="font-medium">{goal.label}</span>
                            </div>
                        </button>
                    ))}
                </div>
            </div>

            <div className="w-full">
                <label className="block text-sm font-medium text-gray-900 mb-3">
                    What's your experience level? (Optional)
                </label>
                <div className="space-y-3 w-full">
                    {experienceLevels.map((level) => (
                        <button
                            key={level.id}
                            type="button"
                            onClick={() => handleInputChange({ target: { name: 'experienceLevel', value: level.id } } as any)}
                            className={`w-full p-3 sm:p-4 rounded-lg border-2 text-left transition-all ${
                                formData.experienceLevel === level.id
                                    ? 'border-blue-500 bg-blue-50 text-blue-700'
                                    : 'border-gray-300 hover:border-gray-400'
                            }`}
                        >
                            <div className="flex justify-between items-center">
                                <span className="font-medium">{level.label}</span>
                                <span className="text-sm text-gray-500 flex-shrink-0 ml-2">{level.description}</span>
                            </div>
                        </button>
                    ))}
                </div>
            </div>

            <div className="space-y-3 pt-4 border-t border-gray-300 w-full">
                <label className="flex items-start">
                    <input
                        type="checkbox"
                        name="agreeToTerms"
                        checked={formData.agreeToTerms}
                        onChange={handleInputChange}
                        className="mt-1 mr-3 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded flex-shrink-0"
                    />
                    <span className="text-sm text-gray-600">
                        I agree to the <button type="button" className="text-blue-600 hover:underline">Terms of Service</button> and{' '}
                        <button type="button" className="text-blue-600 hover:underline">Privacy Policy</button>
                    </span>
                </label>
                {errors.agreeToTerms && <p className="text-sm text-red-600">{errors.agreeToTerms}</p>}

                <label className="flex items-start">
                    <input
                        type="checkbox"
                        name="subscribeNewsletter"
                        checked={formData.subscribeNewsletter || false}
                        onChange={handleInputChange}
                        className="mt-1 mr-3 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded flex-shrink-0"
                    />
                    <span className="text-sm text-gray-600">
                        Send me workout tips and product updates (optional)
                    </span>
                </label>
            </div>
        </div>
    );

    return (
        <div className="w-full max-w-2xl mx-auto px-4 sm:px-6">
            {/* Header */}
            <div className="text-center mb-6 sm:mb-8">
                <div className="mb-4">
                    <span className="text-3xl sm:text-4xl">💪</span>
                </div>
                <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">
                    Join WorkoutTracker
                </h1>
                <p className="text-sm sm:text-base text-gray-600">
                    Free forever • No credit card required
                </p>
            </div>

            {/* Step Indicator */}
            {renderStepIndicator()}

            {/* Error Alert */}
            {error && (
                <div className="mb-4 p-3 sm:p-4 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-red-700 text-sm">{error}</p>
                </div>
            )}

            {/* Form */}
            <div className="bg-white rounded-xl shadow-lg p-4 sm:p-6 lg:p-8 w-full overflow-hidden">
                <form onSubmit={handleSubmit} className="w-full">
                    {currentStep === 1 && renderStep1()}
                    {currentStep === 2 && renderStep2()}
                    {currentStep === 3 && renderStep3()}

                    {/* Navigation Buttons */}
                    <div className="flex flex-col sm:flex-row justify-between gap-3 mt-6 sm:mt-8 w-full">
                        <button
                            type="button"
                            onClick={handleBack}
                            className={`px-6 py-3 rounded-lg font-medium transition-all ${
                                currentStep === 1
                                    ? 'invisible'
                                    : 'border border-gray-300 text-gray-600 hover:bg-gray-50'
                            }`}
                        >
                            Back
                        </button>

                        {currentStep < 3 ? (
                            <button
                                type="button"
                                onClick={handleNext}
                                className="px-8 py-3 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-all"
                            >
                                Continue
                            </button>
                        ) : (
                            <button
                                type="submit"
                                disabled={loading}
                                className="px-8 py-3 bg-gradient-to-r from-blue-600 to-green-500 text-white rounded-lg font-medium hover:shadow-lg transform hover:scale-[1.02] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                            >
                                {loading ? (
                                    <div className="flex items-center justify-center">
                                        <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                                        Creating Account...
                                    </div>
                                ) : (
                                    'Create Account'
                                )}
                            </button>
                        )}
                    </div>
                </form>
            </div>

            {/* Sign In Link */}
            {currentStep === 1 && (
                <div className="text-center mt-6">
                    <p className="text-sm text-gray-500">
                        Already have an account?{' '}
                        <button
                            type="button"
                            onClick={() => window.location.href = '/login'}
                            className="text-blue-600 hover:text-blue-800 font-medium"
                        >
                            Sign in
                        </button>
                    </p>
                </div>
            )}
        </div>
    );
};