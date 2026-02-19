import React, {useState} from 'react';
import {Download, Trash2, Shield, AlertTriangle} from 'lucide-react';
import {useAuth} from '../../contexts/AuthContext';
import userApi from '../../services/userApi';
import toast from 'react-hot-toast';

const DataPrivacySection: React.FC = () => {
    const {logout} = useAuth();
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [deleteConfirmText, setDeleteConfirmText] = useState('');
    const [exporting, setExporting] = useState(false);
    const [deleting, setDeleting] = useState(false);

    const handleExportData = async () => {
        try {
            setExporting(true);
            toast.loading('Preparing your data export...', {id: 'export'});

            const data = await userApi.exportData();

            // Create downloadable JSON file
            const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `evopet-data-export-${new Date().toISOString().split('T')[0]}.json`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);

            toast.success('Data exported successfully!', {id: 'export'});
        } catch (error) {
            toast.error('Export failed. Please try again.', {id: 'export'});
        } finally {
            setExporting(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (deleteConfirmText !== 'DELETE') {
            toast.error('Please type DELETE to confirm');
            return;
        }

        try {
            setDeleting(true);
            toast.loading('Deleting account...', {id: 'delete'});

            await userApi.deleteAccount();

            toast.success('Account deleted successfully', {id: 'delete'});

            // Logout and redirect after short delay
            setTimeout(() => {
                logout();
            }, 1000);
        } catch (error) {
            toast.error('Failed to delete account', {id: 'delete'});
            setDeleting(false);
        }
    };

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900">Privacy & Data</h3>
                <p className="text-sm text-gray-500">Manage your personal information</p>
            </div>

            <div className="p-6 space-y-4">
                {/* Export Data */}
                <div className="border border-gray-200 rounded-xl p-4">
                    <div className="flex items-start gap-3">
                        <div
                            className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center text-blue-600 flex-shrink-0">
                            <Download className="w-5 h-5"/>
                        </div>
                        <div className="flex-1">
                            <h4 className="text-base font-semibold text-gray-900 mb-1">Export Your Data</h4>
                            <p className="text-sm text-gray-600 mb-3">
                                Download a copy of your workout history, achievements, and profile data in JSON format.
                            </p>
                            <button
                                onClick={handleExportData}
                                disabled={exporting}
                                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {exporting ? 'Exporting...' : 'Download Data Export'}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Privacy Info */}
                <div className="border border-gray-200 rounded-xl p-4">
                    <div className="flex items-start gap-3">
                        <div
                            className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center text-green-600 flex-shrink-0">
                            <Shield className="w-5 h-5"/>
                        </div>
                        <div className="flex-1">
                            <h4 className="text-base font-semibold text-gray-900 mb-1">Your Privacy</h4>
                            <p className="text-sm text-gray-600">
                                We take your privacy seriously. Your data is encrypted and never sold to third parties.
                            </p>
                        </div>
                    </div>
                </div>

                {/* Delete Account */}
                <div className="border-2 border-red-200 rounded-xl p-4 bg-red-50">
                    <div className="flex items-start gap-3">
                        <div
                            className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center text-red-600 flex-shrink-0">
                            <Trash2 className="w-5 h-5"/>
                        </div>
                        <div className="flex-1">
                            <h4 className="text-base font-semibold text-gray-900 mb-1">Delete Account</h4>
                            <p className="text-sm text-gray-600 mb-3">
                                Permanently delete your account and all associated data. This action cannot be undone.
                            </p>

                            {!showDeleteConfirm ? (
                                <button
                                    onClick={() => setShowDeleteConfirm(true)}
                                    disabled={deleting}
                                    className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm font-medium"
                                >
                                    Delete Account
                                </button>
                            ) : (
                                <div className="bg-white rounded-lg p-4 border-2 border-red-300">
                                    <div className="flex items-start gap-2 mb-3">
                                        <AlertTriangle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5"/>
                                        <div>
                                            <p className="text-sm font-semibold text-gray-900">Are you absolutely
                                                sure?</p>
                                            <p className="text-sm text-gray-600 mt-1">
                                                This will permanently delete your account, workout history,
                                                achievements, and pet progress.
                                            </p>
                                        </div>
                                    </div>

                                    <div className="mb-3">
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            Type <span className="font-bold">DELETE</span> to confirm:
                                        </label>
                                        <input
                                            type="text"
                                            value={deleteConfirmText}
                                            onChange={(e) => setDeleteConfirmText(e.target.value)}
                                            placeholder="Type DELETE"
                                            disabled={deleting}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 disabled:bg-gray-100"
                                        />
                                    </div>

                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => {
                                                setShowDeleteConfirm(false);
                                                setDeleteConfirmText('');
                                            }}
                                            disabled={deleting}
                                            className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors text-sm font-medium disabled:opacity-50"
                                        >
                                            Cancel
                                        </button>
                                        <button
                                            onClick={handleDeleteAccount}
                                            disabled={deleting || deleteConfirmText !== 'DELETE'}
                                            className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            {deleting ? 'Deleting...' : 'Yes, Delete Forever'}
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DataPrivacySection;