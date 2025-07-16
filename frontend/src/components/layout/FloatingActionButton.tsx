import React from 'react';
import { PlusIcon, PlayIcon } from '@heroicons/react/24/solid';

interface FloatingActionButtonProps {
    onClick: () => void;
    isWorkoutMode: boolean;
}

const FloatingActionButton: React.FC<FloatingActionButtonProps> = ({
                                                                       onClick,
                                                                       isWorkoutMode
                                                                   }) => {
    return (
        <button
            onClick={onClick}
            className={`fixed bottom-20 right-4 w-14 h-14 rounded-full shadow-lg z-50 flex items-center justify-center transition-all duration-300 ${
                isWorkoutMode
                    ? 'bg-green-500 hover:bg-green-600'
                    : 'bg-gradient-to-r from-blue-600 to-green-500 hover:from-blue-700 hover:to-green-600'
            } hover:scale-110`}
        >
            {isWorkoutMode ? (
                <PlayIcon className="w-6 h-6 text-white" />
            ) : (
                <PlusIcon className="w-6 h-6 text-white" />
            )}
        </button>
    );
};

export default FloatingActionButton;