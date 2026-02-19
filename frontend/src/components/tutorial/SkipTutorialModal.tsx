import React from 'react';
import {X, AlertCircle} from 'lucide-react';

interface SkipTutorialModalProps {
    isOpen: boolean;
    onConfirm: () => void;
    onCancel: () => void;
    tutorialName: string;
}

const SkipTutorialModal: React.FC<SkipTutorialModalProps> = ({
                                                                 isOpen,
                                                                 onConfirm,
                                                                 onCancel,
                                                                 tutorialName
                                                             }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center p-4 animate-fadeIn">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/50 backdrop-blur-sm"
                onClick={onCancel}
            />

            {/* Modal */}
            <div className="relative bg-white rounded-2xl shadow-2xl max-w-sm w-full p-6 animate-slideUp">
                {/* Close Button */}
                <button
                    onClick={onCancel}
                    className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
                >
                    <X className="w-5 h-5"/>
                </button>

                {/* Icon */}
                <div className="w-12 h-12 rounded-full bg-amber-100 flex items-center justify-center mb-4">
                    <AlertCircle className="w-6 h-6 text-amber-600"/>
                </div>

                {/* Content */}
                <h3 className="text-lg font-bold text-gray-900 mb-2">
                    Skip {tutorialName} Tutorial?
                </h3>
                <p className="text-sm text-gray-600 mb-6">
                    You can always replay this tutorial later from Settings if you need a refresher.
                </p>

                {/* Actions */}
                <div className="flex gap-3">
                    <button
                        onClick={onCancel}
                        className="flex-1 px-4 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg font-medium transition-colors"
                    >
                        Continue Tutorial
                    </button>
                    <button
                        onClick={onConfirm}
                        className="flex-1 px-4 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg font-medium transition-colors"
                    >
                        Skip
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SkipTutorialModal;