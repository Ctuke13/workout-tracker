import React from 'react';
import {
    Bars3Icon,
    StarIcon,
    ClockIcon,
    HeartIcon,
    SparklesIcon,
    FireIcon
} from '@heroicons/react/24/outline';
import {StarIcon as StarIconSolid} from '@heroicons/react/24/solid';
import {Exercise} from '../../types/exercise';

interface ExerciseCardProps {
    exercise: Exercise;
    onSelect: () => void;
    onDragStart?: () => void;
    onToggleFavorite?: (exercise: Exercise, event?: React.MouseEvent) => void;
    disabled?: boolean;
    showPopularBadge?: boolean;
    showFavoriteButton?: boolean;
    isFavoritesTab?: boolean;
}

const ExerciseCard: React.FC<ExerciseCardProps> = ({
                                                       exercise,
                                                       onSelect,
                                                       onDragStart,
                                                       onToggleFavorite,
                                                       disabled = false,
                                                       showPopularBadge = false,
                                                       showFavoriteButton = false,
                                                       isFavoritesTab = false
                                                   }) => {
    const exerciseName = exercise.exerciseName || exercise.name || 'Unknown Exercise';
    const isMobile = window.innerWidth < 768;
    const isFavorited = exercise.isFavorite || false;

    const getDifficultyColor = (difficulty: string | undefined) => {
        const difficultyLevel = (difficulty || 'INTERMEDIATE').toLowerCase();
        switch (difficultyLevel) {
            case 'beginner':
                return 'bg-green-100 text-green-700 border-green-200';
            case 'intermediate':
                return 'bg-yellow-100 text-yellow-700 border-yellow-200';
            case 'advanced':
                return 'bg-red-100 text-red-700 border-red-200';
            default:
                return 'bg-gray-100 text-gray-700 border-gray-200';
        }
    };

    const getWorkoutTrackingBadge = () => {
        if (exercise.isCardio) {
            return (
                <span
                    className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-700 border border-red-200">
                    <HeartIcon className="w-3 h-3 mr-1"/>
                    Cardio
                </span>
            );
        }
        if (exercise.isIsometric) {
            return (
                <span
                    className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-700 border border-purple-200">
                    <SparklesIcon className="w-3 h-3 mr-1"/>
                    Hold
                </span>
            );
        }
        return (
            <span
                className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700 border border-blue-200">
                <FireIcon className="w-3 h-3 mr-1"/>
                Reps
            </span>
        );
    };

    return (
        <div
            className={`
                group bg-white rounded-2xl border border-gray-200 p-4 transition-all duration-300 relative
                ${disabled
                ? 'opacity-50 cursor-not-allowed'
                : 'hover:shadow-lg hover:border-blue-300 cursor-pointer active:scale-[0.98] hover:-translate-y-1'
            }
            `}
            draggable={!disabled && !isMobile}
            onDragStart={disabled || isMobile ? undefined : onDragStart}
            onClick={disabled ? undefined : onSelect}
        >
            {/* Favorite Star Button */}
            {showFavoriteButton && onToggleFavorite && (
                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        onToggleFavorite(exercise, e);
                    }}
                    className={`
                        absolute top-3 right-3 z-10 p-2 rounded-full transition-all duration-200
                        active:scale-95 shadow-sm hover:shadow-md
                        ${isFavorited
                        ? 'text-yellow-500 bg-yellow-100 hover:text-yellow-600 hover:bg-yellow-200 border border-yellow-300'
                        : 'text-gray-400 bg-gray-100 hover:text-yellow-500 hover:bg-yellow-100 border border-gray-300'
                    }
                    `}
                    title={isFavorited ? 'Remove from favorites' : 'Add to favorites'}
                >
                    {isFavorited ? (
                        <StarIconSolid className="w-5 h-5 text-yellow-500"/>
                    ) : (
                        <StarIcon className="w-5 h-5"/>
                    )}
                </button>
            )}

            <div className="flex items-start">
                {!isMobile && (
                    <div className="mr-3 text-gray-400 pt-1 group-hover:text-blue-500 transition-colors">
                        <Bars3Icon className="w-5 h-5"/>
                    </div>
                )}

                <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between mb-3">
                        <div className="flex items-center min-w-0 flex-1">
                            <h3 className="font-bold text-gray-900 mr-2 text-base group-hover:text-blue-900 transition-colors truncate">
                                {exercise.emoji || '💪'} {exerciseName}
                            </h3>
                            {showPopularBadge && (
                                <span
                                    className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gradient-to-r from-yellow-100 to-orange-100 text-yellow-800 border border-yellow-200 flex-shrink-0">
                                    <StarIcon className="w-3 h-3 mr-1"/>
                                    Popular
                                </span>
                            )}
                            {/* Show small favorite indicator when not showing the button */}
                            {isFavorited && !showFavoriteButton && (
                                <StarIconSolid className="w-4 h-4 text-yellow-500 ml-2 flex-shrink-0"/>
                            )}
                        </div>
                    </div>

                    {exercise.description && (
                        <p className="text-sm text-gray-600 mb-3 line-clamp-2 group-hover:text-gray-700 transition-colors">
                            {exercise.description.length > 80
                                ? `${exercise.description.substring(0, 80)}...`
                                : exercise.description
                            }
                        </p>
                    )}

                    <div className="flex flex-wrap gap-2 mb-3">
                        {getWorkoutTrackingBadge()}
                        <span
                            className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getDifficultyColor(exercise.difficultyLevel)}`}>
                            {exercise.difficultyLevel || 'INTERMEDIATE'}
                        </span>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3 text-xs text-gray-500">
                            {exercise.estimatedDurationMinutes && (
                                <div className="flex items-center">
                                    <ClockIcon className="w-3 h-3 mr-1"/>
                                    {exercise.estimatedDurationMinutes}min
                                </div>
                            )}
                            {exercise.averageRating && exercise.averageRating > 0 && (
                                <div className="flex items-center">
                                    <StarIcon className="w-3 h-3 mr-1 text-yellow-500"/>
                                    {exercise.averageRating.toFixed(1)}
                                </div>
                            )}
                        </div>
                        <div className="text-right">
                            <span
                                className="text-xs font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
                                Tap to add →
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ExerciseCard;