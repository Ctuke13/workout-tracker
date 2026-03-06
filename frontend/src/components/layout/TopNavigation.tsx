// src/components/layout/TopNavigation.tsx - With Gamification Widget
import React, {useState, useEffect} from 'react';
import {useAuth} from '../../contexts/AuthContext';
import {usePet} from '../../contexts/PetContext';
import {useNavigate, useLocation} from 'react-router-dom';
import {
    Bars3Icon,
    BellIcon,
    Cog6ToothIcon,
    ArrowRightOnRectangleIcon
} from '@heroicons/react/24/outline';
import {MiniProgressWidget} from '../gamification/MiniProgressWidget';
import {useNotifications} from '../../hooks/useNotifications';

interface TopNavigationProps {
    scrollContainerRef?: React.RefObject<HTMLElement | null>;
}


const useScrollDirection = (scrollContainerRef?: React.RefObject<HTMLElement | null>) => {
    const [isVisible, setIsVisible] = useState(true);
    const [lastScrollY, setLastScrollY] = useState(0);

    useEffect(() => {
        const scrollElement = scrollContainerRef?.current || window;

        const handleScroll = () => {
            const currentScrollY = scrollContainerRef?.current
                ? scrollContainerRef.current.scrollTop
                : window.scrollY;

            const scrollThreshold = 30;

            if (Math.abs(currentScrollY - lastScrollY) < scrollThreshold) {
                return;
            }

            if (currentScrollY > lastScrollY && currentScrollY > 80) {
                setIsVisible(false);
            } else {
                setIsVisible(true);
            }

            setLastScrollY(currentScrollY);
        };

        scrollElement.addEventListener('scroll', handleScroll, {passive: true});
        return () => scrollElement.removeEventListener('scroll', handleScroll);
    }, [lastScrollY, scrollContainerRef]);

    return isVisible;
};

const TopNavigation: React.FC<TopNavigationProps> = ({scrollContainerRef}) => {
    const {user, logout} = useAuth();
    const {stats} = usePet();
    const navigate = useNavigate();
    const location = useLocation();
    const [showUserMenu, setShowUserMenu] = useState(false);
    const isWidgetVisible = useScrollDirection(scrollContainerRef);
    const {permissionStatus, requestPermission} = useNotifications();

    const browserPermission = 'Notification' in window ? Notification.permission : 'unsupported';
    const notificationsEnabled = browserPermission === 'granted' && permissionStatus !== 'unsupported';

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
            case '/community':
                return 'Community';
            case '/messages':
                return 'Messages';
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
            {/* Main Navigation Row */}
            <div className="max-w-7xl mx-auto px-3 sm:px-4 lg:px-6">
                <div className="flex justify-between items-center h-14 sm:h-16">
                    {/* Left Side - Logo/Title */}
                    <div className="flex items-center space-x-3 sm:space-x-4 min-w-0 flex-1">
                        {/* Mobile Menu Button - Hidden for now, can be added later */}
                        <div className="hidden">
                            <button className="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100">
                                <Bars3Icon className="w-5 h-5"/>
                            </button>
                        </div>

                        {/* Logo/Brand */}
                        <div className="flex items-center space-x-2 sm:space-x-3">
                            <div
                                className="w-8 h-8 sm:w-10 sm:h-10 bg-gradient-to-br from-blue-600 to-green-500 rounded-lg flex items-center justify-center">
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

                    {/* 🆕 CENTER: Desktop Progress Widget (hidden on mobile/tablet) */}
                    <div className="hidden lg:flex items-center justify-center mx-4 flex-shrink-0 gap-2">
                        <MiniProgressWidget/>
                        {/* Crystal Counter - After rank badge */}
                        <div
                            className="crystal-counter flex items-center gap-1.5 bg-gradient-to-r from-amber-100 to-orange-100 px-2.5 py-1.5 rounded-full border border-amber-200">
                            <span className="text-base">💎</span>
                            <span className="font-bold text-amber-700 text-sm">
                                {stats?.crystals ?? 0}
                            </span>
                        </div>
                    </div>

                    {/* Right Side - User Actions */}
                    <div className="flex items-center space-x-2 sm:space-x-3 flex-1 justify-end">
                        {/* Notifications - Desktop only */}
                        <div className="hidden sm:block">
                            <button
                                onClick={() => !notificationsEnabled && requestPermission()}
                                className="p-2 text-gray-400 hover:text-gray-500 hover:bg-gray-100 rounded-lg transition-colors relative"
                                title={notificationsEnabled ? 'Notifications enabled' : 'Click to enable notifications'}
                            >
                                <BellIcon className={`w-5 h-5 ${notificationsEnabled ? 'text-purple-500' : 'text-gray-400'}`}/>
                                {/* Red dot only when notifications are NOT enabled */}
                                {!notificationsEnabled && browserPermission !== 'denied' && (
                                    <span className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-red-500 rounded-full"/>
                                )}
                            </button>
                        </div>

                        {/* User Menu - Compact Avatar Only */}
                        <div className="relative">
                            <button
                                onClick={() => setShowUserMenu(!showUserMenu)}
                                className="p-1 rounded-full hover:ring-2 hover:ring-purple-300 transition-all"
                                title={`${user?.firstName} ${user?.lastName} - ${user?.userType === 'REGULAR' ? 'Free Plan' : 'Pro Plan'}`}
                            >
                                {/* User Avatar */}
                                <div
                                    className="w-9 h-9 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center shadow-md">
                                    <span className="text-white font-semibold text-sm">
                                        {getUserInitials()}
                                    </span>
                                </div>
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
                                    <div
                                        className="absolute right-0 mt-2 w-48 sm:w-56 bg-white rounded-lg shadow-lg border border-gray-200 py-2 z-20">
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
                                                    navigate('/settings');
                                                }}
                                                className="flex items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                                            >
                                                <Cog6ToothIcon className="w-4 h-4 mr-3"/>
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
                                                    <div
                                                        className="w-4 h-4 mr-3 bg-gradient-to-br from-blue-500 to-purple-500 rounded flex items-center justify-center">
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
                                                <ArrowRightOnRectangleIcon className="w-4 h-4 mr-3"/>
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

            {/* 🆕 MOBILE/TABLET: Progress Widget Row with SCROLL HIDE - Using max-height */}
            <div
                className={`lg:hidden border-t border-gray-100 bg-gradient-to-r from-blue-50/50 via-purple-50/30 to-pink-50/50 overflow-hidden transition-all duration-300 ease-in-out ${
                    isWidgetVisible
                        ? 'max-h-20 opacity-100'
                        : 'max-h-0 opacity-0'
                }`}
            >
                <div className="max-w-7xl mx-auto px-3 sm:px-4 py-2.5">
                    <div className="flex justify-center items-center gap-2">
                        <MiniProgressWidget/>
                        {/* Crystal Counter - After rank badge */}
                        <div
                            className="crystal-counter flex items-center gap-1.5 bg-gradient-to-r from-amber-100 to-orange-100 px-2.5 py-1.5 rounded-full border border-amber-200">
                            <span className="text-base">💎</span>
                            <span className="font-bold text-amber-700 text-sm">
                                {stats?.crystals ?? 0}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default TopNavigation;