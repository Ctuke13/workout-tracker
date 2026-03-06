import React, {useRef, useState} from 'react';
import {User, Camera, Loader2} from 'lucide-react';
import {useAuth} from '../../contexts/AuthContext';
import apiClient from '../../services/apiClient';

interface UploadResponse {
    imageUrl: string;
    crystalsAwarded: number;
    firstUpload: boolean;
}

const ProfileSection: React.FC = () => {
    const {user, refreshUser} = useAuth();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [uploading, setUploading] = useState(false);
    const [crystalToast, setCrystalToast] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    if (!user) return null;

    const initials = `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();

    const handlePhotoClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        // Basic client-side validation
        if (!file.type.startsWith('image/')) {
            setError('Please select an image file.');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            setError('Image must be under 5MB.');
            return;
        }

        setError(null);
        setUploading(true);

        try {
            const formData = new FormData();
            formData.append('photo', file);

            const response = await apiClient.postForm<UploadResponse>(
                '/api/users/profile-photo',
                formData
            );

            // Refresh auth context so the new photo URL is reflected everywhere
            if (refreshUser) await refreshUser();

            // Show crystal reward toast on first upload
            if (response.crystalsAwarded > 0) {
                setCrystalToast(`+${response.crystalsAwarded} 💎 crystals for adding your photo!`);
                setTimeout(() => setCrystalToast(null), 4000);
            }
        } catch (err) {
            console.error('Photo upload failed:', err);
            setError('Upload failed. Please try again.');
        } finally {
            setUploading(false);
            // Reset input so the same file can be re-selected after an error
            if (fileInputRef.current) fileInputRef.current.value = '';
        }
    };

    return (
        <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl shadow-sm border border-purple-200 p-6">

            {/* Crystal reward toast */}
            {crystalToast && (
                <div className="mb-4 bg-amber-50 border border-amber-300 rounded-xl px-4 py-3 text-amber-700 text-sm font-semibold text-center animate-pulse">
                    {crystalToast}
                </div>
            )}

            <div className="flex items-center gap-4">
                {/* Avatar — tappable to upload */}
                <div className="relative flex-shrink-0">
                    <button
                        onClick={handlePhotoClick}
                        disabled={uploading}
                        className="relative w-20 h-20 rounded-full overflow-hidden focus:outline-none focus:ring-2 focus:ring-purple-400 focus:ring-offset-2 group"
                        title="Change profile photo"
                    >
                        {user.profileImageUrl ? (
                            <img
                                src={user.profileImageUrl}
                                alt="Profile"
                                className="w-full h-full object-cover"
                            />
                        ) : (
                            <div className="w-full h-full bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center text-white text-2xl font-bold">
                                {initials || <User className="w-10 h-10"/>}
                            </div>
                        )}

                        {/* Overlay shown on hover / while uploading */}
                        <div className={`absolute inset-0 bg-black/40 flex flex-col items-center justify-center transition-opacity rounded-full
                            ${uploading ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'}`}>
                            {uploading
                                ? <Loader2 className="w-6 h-6 text-white animate-spin"/>
                                : <Camera className="w-6 h-6 text-white"/>
                            }
                        </div>
                    </button>

                    {/* First-upload nudge badge — only when no photo yet */}
                    {!user.profileImageUrl && !uploading && (
                        <div className="absolute -bottom-1 -right-1 bg-amber-400 text-white text-xs font-bold rounded-full w-6 h-6 flex items-center justify-center shadow-md border-2 border-white"
                             title="Add a photo to earn 2 crystals!">
                            +2
                        </div>
                    )}
                </div>

                {/* User Info */}
                <div className="flex-1 min-w-0">
                    <h2 className="text-xl font-bold text-gray-900 truncate">
                        {user.firstName} {user.lastName}
                    </h2>
                    <p className="text-sm text-gray-600 truncate">{user.email}</p>
                    <div className="flex items-center gap-2 mt-1">
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                            {user.subscriptionTier}
                        </span>
                        {user.nickname && (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-pink-100 text-pink-800">
                                @{user.nickname}
                            </span>
                        )}
                    </div>

                    {/* First-upload prompt text */}
                    {!user.profileImageUrl && (
                        <button
                            onClick={handlePhotoClick}
                            disabled={uploading}
                            className="mt-1.5 text-xs text-purple-600 hover:text-purple-800 font-medium underline underline-offset-2"
                        >
                            Add a photo · earn 2 💎
                        </button>
                    )}
                </div>
            </div>

            {/* Error message */}
            {error && (
                <p className="mt-3 text-xs text-red-600 text-center">{error}</p>
            )}

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

            {/* Hidden file input */}
            <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleFileChange}
            />
        </div>
    );
};

export default ProfileSection;