import React, { JSX } from 'react';
import { Award, Clock, Flame, Heart, TrendingUp, Users } from "lucide-react";
import { Exercise } from '../../types/exercise';
import {
    getDifficultyColor,
    formatMuscleGroups,
    formatNumberWithCommas,
    getPopularityLevel
} from '../../utils/exerciseFormatters';

interface ExerciseCardProps {
    exercise: Exercise;
    index: number;
    isExpanded: boolean;
    isFavorite: boolean;
    onToggleExpand: (index: number) => void;
    onToggleFavorite: (exerciseId: number) => void;
    onTrackWorkout?: (exerciseId: number) => void;
}

export const ExerciseCard: React.FC<ExerciseCardProps> = ({
                                                              exercise,
                                                              index,
                                                              isExpanded,
                                                              isFavorite,
                                                              onToggleExpand,
                                                              onToggleFavorite,
                                                              onTrackWorkout
                                                          }) => {
    const renderStars = (rating: number, totalRatings: number): JSX.Element => {
        return (
            <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                    <span
                        key={star}
                        className={`text-sm ${
                            star <= rating ? 'text-yellow-400' : 'text-gray-300'
                        }`}
                    >
                        ⭐
                    </span>
                ))}
                <span className="text-xs text-gray-600 ml-1">
                    {rating.toFixed(1)} ({totalRatings})
                </span>
            </div>
        );
    };

    return (
        <div className="group bg-white border border-gray-200 rounded-xl overflow-hidden hover:border-blue-300 hover:shadow-lg transition-all duration-300">
            <div className="p-4 sm:p-6 pb-4">
                {/* Header */}
                <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-2 sm:gap-3 flex-1">
                        <div className="text-xl sm:text-2xl group-hover:scale-110 transition-transform">
                            {exercise.emoji}
                        </div>
                        <div className="flex-1 min-w-0">
                            <h3 className="font-semibold text-gray-900 text-base sm:text-lg group-hover:text-blue-600 transition-colors truncate">
                                {exercise.name}
                            </h3>
                            <div className="flex items-center gap-1 sm:gap-2 mt-1 flex-wrap">
                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${getDifficultyColor(exercise.difficultyLevel)}`}>
                                    {exercise.difficultyLevel.toLowerCase()}
                                </span>
                                {exercise.createdByProfessional && (
                                    <div className="flex items-center gap-1 bg-blue-50 px-2 py-1 rounded-full">
                                        <Award className="w-3 h-3 text-blue-600" />
                                        <span className="text-xs text-blue-600 font-medium">Pro</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                    <button
                        onClick={() => onToggleFavorite(exercise.id)}
                        className={`p-2 rounded-full transition-colors flex-shrink-0 ${
                            isFavorite
                                ? 'text-red-500 bg-red-50'
                                : 'text-gray-400 hover:text-red-500 hover:bg-red-50'
                        }`}
                    >
                        <Heart className={`w-4 h-4 sm:w-5 sm:h-5 ${isFavorite ? 'fill-current' : ''}`} />
                    </button>
                </div>

                {/* Rating and Stats Row */}
                <div className="grid grid-cols-2 gap-2 sm:gap-4 mb-3 sm:mb-4">
                    <div>
                        {renderStars(exercise.averageRating, exercise.totalRatings)}
                    </div>
                    <div className="flex items-center gap-1 text-gray-600">
                        <Users className="w-3 h-3 sm:w-4 sm:h-4" />
                        <span className="text-xs">{formatNumberWithCommas(exercise.usageCount)} used</span>
                    </div>
                </div>

                {/* Description */}
                <p className="text-gray-600 text-sm mb-3 sm:mb-4 line-clamp-2">
                    {exercise.description}
                </p>

                {/* Stats Grid */}
                <div className="grid grid-cols-3 gap-2 sm:gap-3 mb-3 sm:mb-4">
                    <div className="text-center">
                        <div className="flex items-center justify-center gap-1 text-orange-500 mb-1">
                            <Flame className="w-3 h-3 sm:w-4 sm:h-4" />
                            <span className="font-semibold text-xs sm:text-sm">{exercise.estimatedCalories}</span>
                        </div>
                        <div className="text-xs text-gray-500">calories</div>
                    </div>
                    <div className="text-center">
                        <div className="flex items-center justify-center gap-1 text-blue-600 mb-1">
                            <Clock className="w-3 h-3 sm:w-4 sm:h-4" />
                            <span className="font-semibold text-xs sm:text-sm">{exercise.estimatedDurationMinutes}</span>
                        </div>
                        <div className="text-xs text-gray-500">minutes</div>
                    </div>
                    <div className="text-center">
                        <div className="flex items-center justify-center gap-1 text-green-500 mb-1">
                            <TrendingUp className="w-3 h-3 sm:w-4 sm:h-4" />
                            <span className="font-semibold text-xs sm:text-sm">
                                {getPopularityLevel(exercise.usageCount)}
                            </span>
                        </div>
                        <div className="text-xs text-gray-500">popularity</div>
                    </div>
                </div>

                {/* Muscle Groups */}
                {exercise.targetMuscleGroups && exercise.targetMuscleGroups.length > 0 && (
                    <div className="mb-3 sm:mb-4">
                        <h4 className="text-sm font-medium text-gray-900 mb-2">Target Muscles:</h4>
                        <div className="flex flex-wrap gap-1">
                            {formatMuscleGroups(exercise.targetMuscleGroups).slice(0, 3).map((muscle: string, i: number) => (
                                <span
                                    key={i}
                                    className="px-2 py-1 bg-green-50 text-green-700 text-xs rounded-full font-medium"
                                >
                                    {muscle}
                                </span>
                            ))}
                            {exercise.targetMuscleGroups.length > 3 && (
                                <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                                    +{exercise.targetMuscleGroups.length - 3}
                                </span>
                            )}
                        </div>
                    </div>
                )}

                {/* Benefits */}
                <div className="mb-3 sm:mb-4">
                    <h4 className="text-sm font-medium text-gray-900 mb-2">Key Benefits:</h4>
                    <div className="space-y-1">
                        {exercise.benefits.slice(0, 2).map((benefit: string, i: number) => (
                            <div key={i} className="flex items-start text-xs text-gray-600">
                                <span className="text-green-500 mr-2 mt-0.5 flex-shrink-0">✓</span>
                                <span className="line-clamp-1">{benefit}</span>
                            </div>
                        ))}
                        {exercise.benefits.length > 2 && (
                            <div className="text-xs text-blue-600">
                                +{exercise.benefits.length - 2} more benefits
                            </div>
                        )}
                    </div>
                </div>

                {/* Expandable Section */}
                {isExpanded && (
                    <div className="mb-3 sm:mb-4 p-3 sm:p-4 bg-gray-50 rounded-lg border border-gray-200">
                        <h4 className="text-sm font-semibold text-gray-900 mb-2">Form Tips:</h4>
                        <div className="space-y-2 mb-3 sm:mb-4">
                            {exercise.tips.map((tip: string, i: number) => (
                                <div key={i} className="flex items-start text-xs text-gray-700">
                                    <span className="text-blue-600 mr-2 mt-0.5 flex-shrink-0">•</span>
                                    <span>{tip}</span>
                                </div>
                            ))}
                        </div>

                        {exercise.videoUrl && (
                            <div className="p-3 bg-orange-50 rounded-lg border border-orange-200">
                                <div className="flex items-center text-xs text-orange-700 font-medium">
                                    <span className="mr-2">📹</span>
                                    HD Video Guide Available
                                </div>
                            </div>
                        )}
                    </div>
                )}

                {/* Action Buttons */}
                <div className="space-y-2">
                    <button
                        onClick={() => onToggleExpand(index)}
                        className="w-full px-3 sm:px-4 py-2 border border-blue-600 text-blue-600 hover:bg-blue-50 text-xs sm:text-sm rounded-lg transition-colors"
                    >
                        {isExpanded ? 'Hide Details' : 'View Form Tips'}
                    </button>
                    <button
                        onClick={() => onTrackWorkout?.(exercise.id)}
                        className="w-full px-3 sm:px-4 py-2 bg-gradient-to-r from-blue-600 to-green-500 text-white text-xs sm:text-sm font-semibold rounded-lg hover:shadow-lg transform hover:scale-105 transition-all"
                    >
                        📱 Track This Workout
                    </button>
                </div>
            </div>
        </div>
    );
};