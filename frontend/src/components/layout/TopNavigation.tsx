import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import { MagnifyingGlassIcon, BellIcon, ChatBubbleLeftIcon } from '@heroicons/react/24/outline';
import SearchModal from "./SearchModal";

const TopNavigation: React.FC = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [showProfileMenu, setShowProfileMenu] = useState(false);
    const [showSearchModal, setShowSearchModal] = useState(false);

    // Mock notification counts
    const notificationCount = 3;
    const messageCount = 2;

    const handleLogoClick = () => {
        navigate('/community'); // Logo takes you to Community feed
    };

    const profileMenuItems = [
        { label: 'Settings & Privacy', onClick: () => navigate('/settings') },
        { label: 'Help & Support', onClick: () => navigate('/help') },
        { label: 'Billing', onClick: () => navigate('/billing') },
        { label: 'Log Out', onClick: logout, className: 'text-red-600' }
    ];

    return (
        <header className="bg-white border-b border-gray-200 px-4 py-3 sticky top-0 z-50">
            <div className="flex items-center justify-between">
                {/* Left: Logo */}
                <button
                    onClick={handleLogoClick}
                    className="flex items-center space-x-2 hover:opacity-80 transition-opacity"
                >
                    <div className="w-8 h-8 bg-gradient-to-r from-blue-600 to-green-500 rounded-lg flex items-center justify-center">
                        <span className="text-white font-bold text-sm">WT</span>
                    </div>
                    <span className="font-bold text-gray-900 hidden sm:block">WorkoutTracker</span>
                </button>

                {/* Center: Search */}
                <div className="flex-1 max-w-md mx-4">
                    <button
                        onClick={() => setShowSearchModal(true)}
                        className="w-full bg-gray-100 hover:bg-gray-200 rounded-lg px-4 py-2 text-left text-gray-500 transition-colors"
                    >
                        <div className="flex items-center space-x-2">
                            <MagnifyingGlassIcon className="w-4 h-4" />
                            <span className="text-sm">Search exercises, users...</span>
                        </div>
                    </button>
                </div>

                {/* Right: Notifications, Messages, Profile */}
                <div className="flex items-center space-x-3">
                    {/* Notifications */}
                    <button
                        onClick={() => navigate('/notifications')}
                        className="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        <BellIcon className="w-5 h-5" />
                        {notificationCount > 0 && (
                            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                {notificationCount}
              </span>
                        )}
                    </button>

                    {/* Messages */}
                    <button
                        onClick={() => navigate('/messages')}
                        className="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        <ChatBubbleLeftIcon className="w-5 h-5" />
                        {messageCount > 0 && (
                            <span className="absolute -top-1 -right-1 bg-blue-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                {messageCount}
              </span>
                        )}
                    </button>

                    {/* Profile */}
                    <div className="relative">
                        <button
                            onClick={() => setShowProfileMenu(!showProfileMenu)}
                            className="flex items-center space-x-2 p-1 rounded-lg hover:bg-gray-100 transition-colors"
                        >
                            <div className="w-8 h-8 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
                <span className="text-white font-semibold text-sm">
                  {user?.firstName?.charAt(0)}{user?.lastName?.charAt(0)}
                </span>
                            </div>
                            <span className="text-sm text-gray-700 hidden sm:block">
                {user?.firstName}
              </span>
                        </button>

                        {/* Profile Dropdown */}
                        {showProfileMenu && (
                            <div className="absolute right-0 top-full mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-2 z-50">
                                {profileMenuItems.map((item, index) => (
                                    <button
                                        key={index}
                                        onClick={() => {
                                            item.onClick();
                                            setShowProfileMenu(false);
                                        }}
                                        className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-50 transition-colors ${item.className || 'text-gray-700'}`}
                                    >
                                        {item.label}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Search Modal */}
            {showSearchModal && (
                <SearchModal onClose={() => setShowSearchModal(false)} />
            )}
        </header>
    );
};

export default TopNavigation;