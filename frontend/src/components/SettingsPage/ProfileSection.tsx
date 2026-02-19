import React from 'react';
import {User, Edit} from 'lucide-react';
import {useAuth} from '../../contexts/AuthContext';

const ProfileSection: React.FC = () => {
    const {user} = useAuth();

    if (!user) return null;

    // Get initials for avatar
    const initials = `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();

    return (
        <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl shadow-sm border border-purple-200 p-6">
            <div className="flex items-center gap-4">
                {/* Avatar */}
                <div
                    className="w-20 h-20 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-lg flex-shrink-0">
                    {initials || <User className="w-10 h-10"/>}
                </div>

                {/* User Info */}
                <div className="flex-1 min-w-0">
                    <h2 className="text-xl font-bold text-gray-900 truncate">
                        {user.firstName} {user.lastName}
                    </h2>
                    <p className="text-sm text-gray-600 truncate">{user.email}</p>
                    <div className="flex items-center gap-2 mt-1">
                        <span
                            className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                            {user.subscriptionTier}
                        </span>
                        {user.nickname && (
                            <span
                                className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-pink-100 text-pink-800">
                                @{user.nickname}
                            </span>
                        )}
                    </div>
                </div>

                {/* Edit Button */}
                <button
                    onClick={() => {/* TODO: Navigate to profile edit */
                    }}
                    className="p-3 bg-white rounded-xl border border-gray-200 hover:bg-gray-50 transition-colors flex-shrink-0"
                    title="Edit Profile"
                >
                    <Edit className="w-5 h-5 text-gray-600"/>
                </button>
            </div>

            {/* Pet Info */}
            {user.petName && (
                <div className="mt-4 pt-4 border-t border-purple-200">
                    <div className="flex items-center gap-2">
                        <span className="text-2xl">🐺</span>
                        <div>
                            <p className="text-xs text-gray-600">Your Pet</p>
                            <p className="text-sm font-semibold text-gray-900">{user.petName}</p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProfileSection;