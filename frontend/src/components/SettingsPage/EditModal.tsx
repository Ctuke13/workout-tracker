import React, {useState} from 'react';
import {X} from 'lucide-react';

interface EditModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (value: string) => Promise<void>;
    title: string;
    label: string;
    currentValue: string;
    placeholder: string;
    maxLength?: number;
    validationPattern?: RegExp;
    validationMessage?: string;
}

const EditModal: React.FC<EditModalProps> = ({
                                                 isOpen,
                                                 onClose,
                                                 onSave,
                                                 title,
                                                 label,
                                                 currentValue,
                                                 placeholder,
                                                 maxLength = 50,
                                                 validationPattern,
                                                 validationMessage,
                                             }) => {
    const [value, setValue] = useState(currentValue);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    if (!isOpen) return null;

    const handleSave = async () => {
        // Validation
        if (!value.trim()) {
            setError(`${label} cannot be empty`);
            return;
        }

        if (validationPattern && !validationPattern.test(value)) {
            setError(validationMessage || 'Invalid format');
            return;
        }

        try {
            setLoading(true);
            setError('');
            await onSave(value.trim());
            onClose();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to save');
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setValue(currentValue);
        setError('');
        onClose();
    };

    return (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-xl max-w-md w-full overflow-hidden">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                    <h3 className="text-xl font-semibold text-gray-900">{title}</h3>
                    <button
                        onClick={handleClose}
                        disabled={loading}
                        className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        <X className="w-5 h-5 text-gray-500"/>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 overflow-hidden">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        {label}
                    </label>
                    <div className="w-full min-w-0">
                        <input
                            type="text"
                            value={value}
                            onChange={(e) => setValue(e.target.value)}
                            placeholder={placeholder}
                            maxLength={maxLength}
                            disabled={loading}
                            className="w-full box-border px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed"
                            autoFocus
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && !loading) {
                                    handleSave();
                                }
                            }}
                        />
                    </div>
                    {error && (
                        <p className="mt-2 text-sm text-red-600">{error}</p>
                    )}
                    <p className="mt-2 text-xs text-gray-500">
                        {value.length} / {maxLength} characters
                    </p>
                </div>

                {/* Footer */}
                <div className="flex gap-3 p-6 bg-gray-50 rounded-b-2xl">
                    <button
                        onClick={handleClose}
                        disabled={loading}
                        className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={loading || !value.trim()}
                        className="flex-1 px-4 py-2.5 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg hover:from-purple-700 hover:to-pink-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                    >
                        {loading ? 'Saving...' : 'Save Changes'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EditModal;