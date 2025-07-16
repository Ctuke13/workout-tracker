// src/components/layout/QuickWorkoutModal.tsx
import React from 'react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';

interface QuickWorkoutModalProps {
    onClose: () => void;
    onStartWorkout: () => void;
}

interface QuickOption {
    title: string;
    subtitle: string;
    color: string;
    icon: string;
    action: () => void;
    badge?: string;
}

const QuickWorkoutModal: React.FC<QuickWorkoutModalProps> = ({
                                                                 onClose,
                                                                 onStartWorkout
                                                             }) => {
    const navigate = useNavigate();

    const quickOptions: QuickOption[] = [
        {
            title: 'Continue Last Workout',
            subtitle: 'Upper Body Strength - 3 exercises left',
            color: 'bg-blue-500',
            icon: '🏋️‍♂️',
            action: onStartWorkout,
            badge: 'In Progress'
        },
        {
            title: 'Quick Cardio',
            subtitle: '15-minute high intensity session',
            color: 'bg-red-500',
            icon: '🏃‍♂️',
            action: () => {
                navigate('/workouts/quick-cardio');
                onClose();
            }
        },
        {
            title: 'Log Previous Workout',
            subtitle: 'Add a workout you did earlier',
            color: 'bg-green-500',
            icon: '📝',
            action: () => {
                navigate('/workouts/log-previous');
                onClose();
            }
        },
        {
            title: 'Browse Workout Plans',
            subtitle: 'Find a new routine to try',
            color: 'bg-purple-500',
            icon: '📚',
            action: () => {
                navigate('/calendar');
                onClose();
            }
        },
        {
            title: 'Free Workout',
            subtitle: 'Start with an empty session',
            color: 'bg-gray-500',
            icon: '✨',
            action: () => {
                navigate('/workouts/free');
                onClose();
            }
        },
        {
            title: 'Today\'s Scheduled',
            subtitle: 'Follow your planned workout',
            color: 'bg-orange-500',
            icon: '📅',
            action: () => {
                navigate('/calendar');
                onClose();
            }
        }
    ];

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-end justify-center sm:items-center">
            <div className="bg-white rounded-t-2xl sm:rounded-2xl w-full max-w-md mx-4 mb-0 sm:mb-0 max-h-[80vh] overflow-hidden">

                {/* Modal Header */}
                <div className="p-4 border-b border-gray-200 flex items-center justify-between">
                    <div>
                        <h3 className="text-lg font-semibold text-gray-900">Quick Workout</h3>
                        <p className="text-sm text-gray-500 mt-1">Choose how you want to work out</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors"
                    >
                        <XMarkIcon className="w-5 h-5" />
                    </button>
                </div>

                {/* Quick Options */}
                <div className="p-4 space-y-3 max-h-96 overflow-y-auto">
                    {quickOptions.map((option, index) => (
                        <button
                            key={index}
                            onClick={option.action}
                            className="w-full p-4 bg-gray-50 hover:bg-gray-100 rounded-xl transition-colors text-left group"
                        >
                            <div className="flex items-center space-x-4">
                                <div className={`w-12 h-12 ${option.color} rounded-xl flex items-center justify-center text-white text-xl flex-shrink-0 group-hover:scale-105 transition-transform`}>
                                    {option.icon}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center space-x-2">
                                        <div className="font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
                                            {option.title}
                                        </div>
                                        {option.badge && (
                                            <span className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded-full font-medium">
                        {option.badge}
                      </span>
                                        )}
                                    </div>
                                    <div className="text-sm text-gray-500 mt-1">
                                        {option.subtitle}
                                    </div>
                                </div>
                                <div className="text-gray-400 group-hover:text-gray-600 transition-colors">
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                    </svg>
                                </div>
                            </div>
                        </button>
                    ))}
                </div>

                {/* Footer */}
                <div className="p-4 border-t border-gray-100 bg-gray-50">
                    <div className="text-center">
                        <p className="text-xs text-gray-500 mb-2">
                            Tip: Use the + button anytime to quickly start a workout
                        </p>
                        <button
                            onClick={onClose}
                            className="text-sm text-gray-600 hover:text-gray-800 font-medium"
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default QuickWorkoutModal;