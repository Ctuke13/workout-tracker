// src/components/layout/TopNavigation.tsx - Clean Mobile-First Design
import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    Bars3Icon,
    BellIcon,
    UserCircleIcon,
    Cog6ToothIcon,
    ArrowRightOnRectangleIcon,
    ChevronDownIcon
} from '@heroicons/react/24/outline';

const TopNavigation: React.FC = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [showUserMenu, setShowUserMenu] = useState(false);

    // Hide top nav on auth pages
    const hideOnPages = ['/login', '/register', '/'];
    if (hideOnPages.includes(location.pathname)) {
        return null;
    }

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/login');
        } catch (error) {
            console.error('Logout failed:', error);
        }
    };

    const getPageTitle = () => {
        switch (location.pathname) {
            case '/calendar':
                return 'Calendar';
            case '/exercises':
                return 'Exercises';
            case '/nutrition':
                return 'Nutrition';
            case '/progress':
                return 'Progress';
            case '/dashboard':
                return 'Dashboard';
            default:
                return 'Workout Tracker';
        }
    };

    const getUserInitials = () => {
        if (!user?.firstName || !user?.lastName) return 'U';
        return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
    };

    return (
        <header className="bg-white border-b border-gray-200 sticky top-0 z-40">
            <div className="max-w-7xl mx-auto px-3 sm:px-4 lg:px-6">
                <div className="flex justify-between items-center h-14 sm:h-16">
                    {/* Left Side - Logo/Title */}
                    <div className="flex items-center space-x-3 sm:space-x-4 min-w-0 flex-1">
                        {/* Mobile Menu Button - Hidden for now, can be added later */}
                        <div className="hidden">
                            <button className="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100">
                                <Bars3Icon className="w-5 h-5" />
                            </button>
                        </div>

                        {/* Logo/Brand */}
                        <div className="flex items-center space-x-2 sm:space-x-3">
                            <div className="w-8 h-8 sm:w-10 sm:h-10 bg-gradient-to-br from-blue-600 to-green-500 rounded-lg flex items-center justify-center">
                                <span className="text-white font-bold text-sm sm:text-base">W</span>
                            </div>
                            <div className="min-w-0">
                                <h1 className="text-base sm:text-lg font-bold text-gray-900 truncate">
                                    <span className="sm:hidden">{getPageTitle()}</span>
                                    <span className="hidden sm:inline">Workout Tracker</span>
                                </h1>
                                <div className="hidden sm:block">
                                    <p className="text-xs text-gray-500">
                                        {getPageTitle()}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Right Side - User Actions */}
                    <div className="flex items-center space-x-2 sm:space-x-3">
                        {/* Notifications - Desktop only */}
                        <div className="hidden sm:block">
                            <button
                                className="p-2 text-gray-400 hover:text-gray-500 hover:bg-gray-100 rounded-lg transition-colors relative"
                                title="Notifications"
                            >
                                <BellIcon className="w-5 h-5" />
                                {/* Notification badge */}
                                <span className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-red-500 rounded-full"></span>
                            </button>
                        </div>

                        {/* User Menu */}
                        <div className="relative">
                            <button
                                onClick={() => setShowUserMenu(!showUserMenu)}
                                className="flex items-center space-x-2 p-1 sm:p-2 rounded-lg hover:bg-gray-100 transition-colors"
                                title="User menu"
                            >
                                {/* User Avatar */}
                                <div className="w-8 h-8 sm:w-9 sm:h-9 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
                                    <span className="text-white font-semibold text-xs sm:text-sm">
                                        {getUserInitials()}
                                    </span>
                                </div>

                                {/* User Name - Desktop only */}
                                <div className="hidden lg:block text-left min-w-0">
                                    <p className="text-sm font-medium text-gray-900 truncate">
                                        {user?.firstName} {user?.lastName}
                                    </p>
                                    <p className="text-xs text-gray-500 truncate">
                                        {user?.userType === 'REGULAR' ? 'Free Plan' : 'Pro Plan'}
                                    </p>
                                </div>

                                {/* Dropdown Arrow - Desktop only */}
                                <ChevronDownIcon className={`hidden sm:block w-4 h-4 text-gray-400 transition-transform ${showUserMenu ? 'rotate-180' : ''}`} />
                            </button>

                            {/* User Dropdown Menu */}
                            {showUserMenu && (
                                <>
                                    {/* Backdrop */}
                                    <div
                                        className="fixed inset-0 z-10"
                                        onClick={() => setShowUserMenu(false)}
                                    />

                                    {/* Menu */}
                                    <div className="absolute right-0 mt-2 w-48 sm:w-56 bg-white rounded-lg shadow-lg border border-gray-200 py-2 z-20">
                                        {/* User Info - Mobile */}
                                        <div className="px-4 py-2 border-b border-gray-100 sm:hidden">
                                            <p className="text-sm font-medium text-gray-900">
                                                {user?.firstName} {user?.lastName}
                                            </p>
                                            <p className="text-xs text-gray-500">
                                                {user?.email}
                                            </p>
                                            <p className="text-xs text-blue-600 font-medium">
                                                {user?.userType === 'REGULAR' ? 'Free Plan' : 'Pro Plan'}
                                            </p>
                                        </div>

                                        {/* Menu Items */}
                                        <div className="py-1">
                                            <button
                                                onClick={() => {
                                                    setShowUserMenu(false);
                                                    navigate('/profile');
                                                }}
                                                className="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                                            >
                                                <UserCircleIcon className="w-4 h-4 mr-3" />
                                                Profile
                                            </button>

                                            <button
                                                onClick={() => {
                                                    setShowUserMenu(false);
                                                    navigate('/settings');
                                                }}
                                                className="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                                            >
                                                <Cog6ToothIcon className="w-4 h-4 mr-3" />
                                                Settings
                                            </button>

                                            {/* Upgrade to Pro - Free users only */}
                                            {user?.userType === 'REGULAR' && !user?.isProfessional && (
                                                <button
                                                    onClick={() => {
                                                        setShowUserMenu(false);
                                                        navigate('/upgrade');
                                                    }}
                                                    className="flex items-center w-full px-4 py-2 text-sm text-blue-600 hover:bg-blue-50 transition-colors"
                                                >
                                                    <div className="w-4 h-4 mr-3 bg-gradient-to-br from-blue-500 to-purple-500 rounded flex items-center justify-center">
                                                        <span className="text-white text-xs font-bold">✨</span>
                                                    </div>
                                                    Upgrade to Pro
                                                </button>
                                            )}

                                            <div className="border-t border-gray-100 my-1"></div>

                                            <button
                                                onClick={() => {
                                                    setShowUserMenu(false);
                                                    handleLogout();
                                                }}
                                                className="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                                            >
                                                <ArrowRightOnRectangleIcon className="w-4 h-4 mr-3" />
                                                Sign out
                                            </button>
                                        </div>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default TopNavigation;