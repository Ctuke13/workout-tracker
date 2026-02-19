import React, {useState} from 'react';
import {X, Eye, EyeOff, Lock} from 'lucide-react';

interface PasswordChangeModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (currentPassword: string, newPassword: string) => Promise<void>;
}

const PasswordChangeModal: React.FC<PasswordChangeModalProps> = ({
                                                                     isOpen,
                                                                     onClose,
                                                                     onSave,
                                                                 }) => {
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showCurrent, setShowCurrent] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    if (!isOpen) return null;

    const validateForm = () => {
        if (!currentPassword) {
            setError('Current password is required');
            return false;
        }

        if (!newPassword) {
            setError('New password is required');
            return false;
        }

        if (newPassword.length < 8) {
            setError('New password must be at least 8 characters');
            return false;
        }

        if (newPassword === currentPassword) {
            setError('New password must be different from current password');
            return false;
        }

        if (newPassword !== confirmPassword) {
            setError('Passwords do not match');
            return false;
        }

        return true;
    };

    const handleSave = async () => {
        if (!validateForm()) return;

        try {
            setLoading(true);
            setError('');
            await onSave(currentPassword, newPassword);

            // Show success state
            setSuccess(true);
            setLoading(false);

            // Close after 1.5 seconds
            setTimeout(() => {
                handleClose();
            }, 1500);

        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to change password');
            setLoading(false);
        }
    };

    const handleClose = () => {
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
        setError('');
        setSuccess(false);
        setShowCurrent(false);
        setShowNew(false);
        setShowConfirm(false);
        onClose();
    };

    const getPasswordStrength = (password: string) => {
        if (password.length === 0) return null;
        if (password.length < 8) return {label: 'Too short', color: 'text-red-600'};
        if (password.length < 12) return {label: 'Weak', color: 'text-orange-600'};
        if (password.length < 16) return {label: 'Good', color: 'text-green-600'};
        return {label: 'Strong', color: 'text-green-700'};
    };

    const strength = getPasswordStrength(newPassword);

    return (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-xl max-w-md w-full overflow-hidden">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                            <Lock className="w-5 h-5 text-purple-600"/>
                        </div>
                        <h3 className="text-xl font-semibold text-gray-900">Change Password</h3>
                    </div>
                    <button
                        onClick={handleClose}
                        disabled={loading || success}
                        className="p-2 hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-50"
                    >
                        <X className="w-5 h-5 text-gray-500"/>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 space-y-4">
                    {/* Current Password */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Current Password
                        </label>
                        <div className="relative">
                            <input
                                type={showCurrent ? 'text' : 'password'}
                                value={currentPassword}
                                onChange={(e) => setCurrentPassword(e.target.value)}
                                disabled={loading || success}
                                className="w-full box-border px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent disabled:bg-gray-100"
                                placeholder="Enter current password"
                            />
                            <button
                                type="button"
                                onClick={() => setShowCurrent(!showCurrent)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                                disabled={loading || success}
                            >
                                {showCurrent ? <EyeOff className="w-5 h-5"/> : <Eye className="w-5 h-5"/>}
                            </button>
                        </div>
                    </div>

                    {/* New Password */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            New Password
                        </label>
                        <div className="relative">
                            <input
                                type={showNew ? 'text' : 'password'}
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                disabled={loading || success}
                                className="w-full box-border px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent disabled:bg-gray-100"
                                placeholder="Enter new password"
                            />
                            <button
                                type="button"
                                onClick={() => setShowNew(!showNew)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                                disabled={loading || success}
                            >
                                {showNew ? <EyeOff className="w-5 h-5"/> : <Eye className="w-5 h-5"/>}
                            </button>
                        </div>
                        {strength && !success && (
                            <p className={`mt-1 text-sm ${strength.color}`}>
                                Strength: {strength.label}
                            </p>
                        )}
                        <p className="mt-1 text-xs text-gray-500">
                            Must be at least 8 characters
                        </p>
                    </div>

                    {/* Confirm Password */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                            Confirm New Password
                        </label>
                        <div className="relative">
                            <input
                                type={showConfirm ? 'text' : 'password'}
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                disabled={loading || success}
                                className="w-full box-border px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent disabled:bg-gray-100"
                                placeholder="Confirm new password"
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter' && !loading && !success) {
                                        handleSave();
                                    }
                                }}
                            />
                            <button
                                type="button"
                                onClick={() => setShowConfirm(!showConfirm)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                                disabled={loading || success}
                            >
                                {showConfirm ? <EyeOff className="w-5 h-5"/> : <Eye className="w-5 h-5"/>}
                            </button>
                        </div>
                    </div>

                    {/* Success Message */}
                    {success && (
                        <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center gap-3">
                            <div
                                className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center flex-shrink-0">
                                <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor"
                                     viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                          d="M5 13l4 4L19 7"/>
                                </svg>
                            </div>
                            <p className="text-sm font-medium text-green-800">Password changed successfully!</p>
                        </div>
                    )}

                    {/* Error Message */}
                    {error && !success && (
                        <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                            <p className="text-sm text-red-600">{error}</p>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="flex gap-3 p-6 bg-gray-50 rounded-b-2xl">
                    <button
                        onClick={handleClose}
                        disabled={loading || success}
                        className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={loading || success || !currentPassword || !newPassword || !confirmPassword}
                        className="flex-1 px-4 py-2.5 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:from-purple-700 hover:to-pink-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                    >
                        {success ? 'Success!' : loading ? 'Changing...' : 'Change Password'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PasswordChangeModal;