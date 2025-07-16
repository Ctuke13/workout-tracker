import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { validateLoginForm } from '../../utils/validation';
import { LoginFormData } from '../../types/auth';

export const LoginForm: React.FC = () => {
    const { login, loading, error } = useAuth();
    const [formData, setFormData] = useState<LoginFormData>({
        emailOrUsername: '',
        password: '',
        rememberMe: false
    });
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState<any>({});

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));

        // Clear error when user starts typing
        if (errors[name]) {
            setErrors((prev: any) => ({ ...prev, [name]: '' }));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        const validationErrors = validateLoginForm(formData);
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        try {
            await login({
                emailOrUsername: formData.emailOrUsername,
                password: formData.password,
                rememberMe: formData.rememberMe
            });
        } catch (err) {
            console.error('Login failed:', err);
        }
    };

    return (
        <div className="w-full max-w-md mx-auto px-4 sm:px-0">
            {/* Mobile-optimized header */}
            <div className="text-center mb-6 sm:mb-8">
                <div className="mb-4">
                    <span className="text-3xl sm:text-4xl">💪</span>
                </div>
                <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">
                    Welcome Back
                </h1>
                <p className="text-sm sm:text-base text-gray-600">
                    Sign in to continue your fitness journey
                </p>
            </div>

            {/* Error Alert - Mobile optimized */}
            {error && (
                <div className="mb-4 p-3 sm:p-4 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-red-700 text-sm">{error}</p>
                </div>
            )}

            {/* Login Form */}
            <form onSubmit={handleSubmit} className="space-y-4 sm:space-y-6">
                {/* Email/Username Field */}
                <div>
                    <label htmlFor="emailOrUsername" className="block text-sm font-medium text-gray-900 mb-2">
                        Email or Username
                    </label>
                    <input
                        id="emailOrUsername"
                        name="emailOrUsername"
                        type="text"
                        value={formData.emailOrUsername}
                        onChange={handleInputChange}
                        className="w-full px-3 sm:px-4 py-3 sm:py-3.5 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-gray-900 placeholder-gray-500"
                        placeholder="Enter your email or username"
                        autoComplete="username"
                    />
                    {errors.emailOrUsername && (
                        <p className="mt-1 text-sm text-red-600">{errors.emailOrUsername}</p>
                    )}
                </div>

                {/* Password Field */}
                <div>
                    <label htmlFor="password" className="block text-sm font-medium text-gray-900 mb-2">
                        Password
                    </label>
                    <div className="relative">
                        <input
                            id="password"
                            name="password"
                            type={showPassword ? 'text' : 'password'}
                            value={formData.password}
                            onChange={handleInputChange}
                            className="w-full px-3 sm:px-4 py-3 sm:py-3.5 pr-10 sm:pr-12 border border-gray-300 rounded-lg text-base focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white text-gray-900 placeholder-gray-500"
                            placeholder="Enter your password"
                            autoComplete="current-password"
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700 focus:outline-none"
                        >
                            {showPassword ? '👁️' : '👁️‍🗨️'}
                        </button>
                    </div>
                    {errors.password && (
                        <p className="mt-1 text-sm text-red-600">{errors.password}</p>
                    )}
                </div>

                {/* Remember Me & Forgot Password */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-2 sm:space-y-0">
                    <label className="flex items-center">
                        <input
                            type="checkbox"
                            name="rememberMe"
                            checked={formData.rememberMe}
                            onChange={handleInputChange}
                            className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                        />
                        <span className="ml-2 text-sm text-gray-600">Remember me</span>
                    </label>
                    <button
                        type="button"
                        className="text-sm text-blue-600 hover:text-blue-800 font-medium"
                    >
                        Forgot password?
                    </button>
                </div>

                {/* Submit Button - Full width on mobile */}
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-gradient-to-r from-blue-600 to-green-500 text-white py-3 sm:py-3.5 px-6 rounded-lg font-semibold text-base hover:shadow-lg transform hover:scale-[1.02] transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                >
                    {loading ? (
                        <div className="flex items-center justify-center">
                            <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                            Signing in...
                        </div>
                    ) : (
                        'Sign In'
                    )}
                </button>

                {/* Divider */}
                <div className="relative my-6">
                    <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-gray-300"></div>
                    </div>
                    <div className="relative flex justify-center text-sm">
                        <span className="px-2 bg-gray-50 text-gray-500">New to WorkoutTracker?</span>
                    </div>
                </div>

                {/* Sign Up Link */}
                <div className="text-center">
                    <button
                        type="button"
                        onClick={() => window.location.href = '/register'}
                        className="w-full sm:w-auto px-6 py-3 border-2 border-blue-600 text-blue-600 rounded-lg font-semibold hover:bg-blue-600 hover:text-white transition-all duration-200"
                    >
                        Create Account
                    </button>
                </div>
            </form>

            {/* Trust Signals - Mobile optimized */}
            <div className="mt-6 sm:mt-8 text-center">
                <div className="flex flex-col sm:flex-row justify-center items-center space-y-2 sm:space-y-0 sm:space-x-6 text-xs sm:text-sm text-gray-500">
          <span className="flex items-center">
            <span className="text-green-500 mr-1">✓</span>
            Secure login
          </span>
                    <span className="flex items-center">
            <span className="text-green-500 mr-1">✓</span>
            Your data stays yours
          </span>
                </div>
            </div>
        </div>
    );
};