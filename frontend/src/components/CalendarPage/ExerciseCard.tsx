import React from 'react';
import {Play, Clock, Target, CheckCircle, Settings, Weight} from 'lucide-react';
import {StarIcon, StarIcon as StarIconSolid} from "@heroicons/react/24/outline";
import {Button} from '../ui/button';
import {Badge} from '../ui/badge';
import {toast} from 'react-hot-toast';
import {ScheduledExercise, Exercise} from '../../types/exercise';
import {exerciseApi} from '../../services/exerciseApi';
import CompletedWorkoutDisplay from './CompletedWorkoutDisplay';
import {WorkoutResults} from '../../types/exercise';

interface ExerciseCardProps {
    exercise: ScheduledExercise;
    index: number;
    workoutResults?: WorkoutResults;
    onStartWorkout: (exerciseId: string) => void;
    onEditExercise: (exercise: ScheduledExercise) => void;
    onDeleteWorkout: (workoutId: string) => void;
    onViewDetails?: (exerciseId: string) => void;
    onFavoriteToggle: (exercise: Exercise) => void;
}

export const ExerciseCard: React.FC<ExerciseCardProps> = ({
                                                              exercise,
                                                              index,
                                                              workoutResults,
                                                              onStartWorkout,
                                                              onEditExercise,
                                                              onDeleteWorkout,
                                                              onViewDetails,
                                                              onFavoriteToggle
                                                          }) => {
    const getConfigurationDisplay = (exercise: ScheduledExercise) => {
        const exerciseType = exercise.exercise;

        if (exerciseType.isCardio) {
            const details = [];
            const duration = exercise.targetDurationMinutes || exercise.exercise.estimatedDurationMinutes;
            if (duration) details.push(`⏱️ ${duration} min`);

            if (exercise.targetDistance) {
                const unit = exercise.targetDistanceUnit === 'km' ? 'km' : 'mi';
                details.push(`📏 ${exercise.targetDistance}${unit}`);
            } else if (exercise.targetDistanceKm) {
                details.push(`📏 ${exercise.targetDistanceKm}km`);
            }

            if (exercise.targetPace) {
                const paceUnit = exercise.targetDistanceUnit === 'km' ? '/km' : '/mi';
                const minutes = Math.floor(exercise.targetPace);
                const seconds = Math.round((exercise.targetPace - minutes) * 60);
                const paceDisplay = `${minutes}:${seconds.toString().padStart(2, '0')}`;
                details.push(`⚡ ${paceDisplay}${paceUnit}`);
            }

            if (exercise.targetSets && exercise.targetSets > 1) {
                details.push(`🔄 ${exercise.targetSets} rounds`);
                if (exercise.restSeconds && exercise.restSeconds > 0) {
                    details.push(`💤 ${exercise.restSeconds}s rest`);
                }
            }

            return {
                text: details.length > 0 ? details.join(' • ') : '❤️ Cardio workout',
                bgColor: 'bg-red-50',
                textColor: 'text-red-700',
                borderColor: 'border-red-200',
                iconColor: 'text-red-600'
            };
        } else if (exerciseType.isIsometric) {
            const details = [];
            if (exercise.targetSets) details.push(`🔄 ${exercise.targetSets} sets`);
            if (exercise.holdDurationSeconds) details.push(`⏱️ ${exercise.holdDurationSeconds}s hold`);
            if (exercise.restSeconds) details.push(`💤 ${exercise.restSeconds}s rest`);

            return {
                text: details.length > 0 ? details.join(' • ') : '🛡️ Isometric holds',
                bgColor: 'bg-purple-50',
                textColor: 'text-purple-700',
                borderColor: 'border-purple-200',
                iconColor: 'text-purple-600'
            };
        } else {
            const details = [];
            if (exercise.targetSets) details.push(`🔄 ${exercise.targetSets} sets`);
            if (exercise.targetReps) details.push(`🎯 ${exercise.targetReps} reps`);

            if (exercise.targetWeight) {
                const unit = exercise.targetWeightUnit || 'lbs';
                details.push(`⚖️ ${exercise.targetWeight}${unit}`);
            }

            if (exercise.restSeconds) details.push(`⏱️ ${exercise.restSeconds}s rest`);
            if (exercise.targetRpe) details.push(`💪 RPE ${exercise.targetRpe}`);
            if (exercise.tempo) details.push(`🎵 ${exercise.tempo}`);

            return {
                text: details.length > 0 ? details.join(' • ') : '💪 Strength training',
                bgColor: 'bg-blue-50',
                textColor: 'text-blue-700',
                borderColor: 'border-blue-200',
                iconColor: 'text-blue-600'
            };
        }
    };

    const getStatusIcon = (exercise: ScheduledExercise) => {
        if (exercise.completed) {
            return <CheckCircle className="w-4 h-4 sm:w-5 sm:h-5 text-green-500"/>;
        }
        return <Clock className="w-4 h-4 sm:w-5 sm:h-5 text-blue-500"/>;
    };

    // If exercise is completed and has results, show the completed workout display
    if (exercise.completed && workoutResults) {
        return (
            <CompletedWorkoutDisplay
                exercise={exercise}
                workoutResults={workoutResults}
                onViewDetails={() => onViewDetails?.(exercise.id)}
            />
        );
    }

    // If exercise is completed but no results available
    if (exercise.completed && !workoutResults) {
        return (
            <div
                className="border-2 border-yellow-200 bg-yellow-50 rounded-lg sm:rounded-xl p-3 sm:p-4 lg:p-6 transition-all duration-200">
                <div className="flex items-start gap-3">
                    <div className="flex-shrink-0 mt-0.5">
                        <div
                            className="w-6 h-6 sm:w-8 sm:h-8 lg:w-10 lg:h-10 rounded-full flex items-center justify-center font-bold text-xs sm:text-sm lg:text-base bg-yellow-500 text-white">
                            ⚠️
                        </div>
                    </div>
                    <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-2">
                            <CheckCircle className="w-4 h-4 sm:w-5 sm:h-5 text-yellow-500"/>
                            <h3 className="font-bold text-sm sm:text-base lg:text-lg text-gray-900 truncate">
                                {exercise.exercise.name || exercise.exercise.exerciseName}
                            </h3>
                        </div>
                        <p className="text-sm text-yellow-700 mb-3">
                            Workout completed but detailed results are not available yet.
                        </p>
                        <div className="flex items-center text-yellow-600 text-sm font-medium">
                            <CheckCircle className="w-4 h-4 mr-1"/>
                            <span>Completed</span>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // Regular scheduled exercise card
    const configDisplay = getConfigurationDisplay(exercise);

    return (
        <div className={`
            border-2 rounded-lg sm:rounded-xl p-3 sm:p-4 lg:p-6 transition-all duration-200
            ${exercise.completed
            ? 'border-green-200 bg-green-50 hover:bg-green-100'
            : 'border-gray-200 bg-white hover:bg-gray-50 hover:border-blue-300'
        }
        `}>
            <div className="flex items-start gap-3">
                <div className="flex-shrink-0 mt-0.5">
                    <div className={`
                        w-6 h-6 sm:w-8 sm:h-8 lg:w-10 lg:h-10 rounded-full flex items-center justify-center font-bold text-xs sm:text-sm lg:text-base
                        ${exercise.completed ? 'bg-green-500 text-white' : 'bg-blue-500 text-white'}
                    `}>
                        {index + 1}
                    </div>
                </div>

                <div className="flex-1 min-w-0">
                    {/* Header with Exercise Title and Favorite Star */}
                    <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2 flex-1 min-w-0">
                            {getStatusIcon(exercise)}
                            <h3 className="font-bold text-sm sm:text-base lg:text-lg text-gray-900 truncate">
                                {exercise.exercise.name || exercise.exercise.exerciseName}
                            </h3>
                        </div>

                        {/* Favorite star */}
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onFavoriteToggle(exercise.exercise);
                            }}
                            className={`
                                ml-2 p-1.5 rounded-full transition-all duration-200 flex-shrink-0
                                active:scale-95 shadow-sm hover:shadow-md border
                                ${exercise.exercise.isFavorite
                                ? 'text-yellow-500 bg-yellow-100 hover:bg-yellow-200 border-yellow-300'
                                : 'text-gray-400 bg-gray-50 hover:bg-yellow-100 border-gray-200'
                            }
                            `}
                            title={exercise.exercise.isFavorite ? 'Remove from favorites' : 'Add to favorites'}
                        >
                            {exercise.exercise.isFavorite ? (
                                <StarIconSolid className="w-4 h-4 text-yellow-500"/>
                            ) : (
                                <StarIcon className="w-4 h-4"/>
                            )}
                        </button>
                    </div>

                    {/* Configuration Details */}
                    <div className={`${configDisplay.bgColor} rounded-lg p-3 mb-3 border ${configDisplay.borderColor}`}>
                        <div className="flex items-center gap-2 mb-2">
                            <Weight className={`w-4 h-4 ${configDisplay.iconColor}`}/>
                            <span className={`text-sm font-medium ${configDisplay.textColor}`}>
                                Configuration
                            </span>
                        </div>
                        <p className={`text-sm ${configDisplay.textColor} font-medium`}>
                            {configDisplay.text}
                        </p>
                        {exercise.notes && (
                            <p className={`text-xs ${configDisplay.textColor} mt-1 italic opacity-80`}>
                                "{exercise.notes}"
                            </p>
                        )}
                    </div>

                    {/* Exercise Description */}
                    {exercise.exercise.description && (
                        <p className="text-xs sm:text-sm text-gray-600 mb-3 line-clamp-2">
                            {exercise.exercise.description}
                        </p>
                    )}

                    {/* Badges */}
                    <div className="flex flex-wrap gap-1 sm:gap-2 mb-3">
                        <Badge variant="secondary" className="text-xs">
                            {exercise.exercise.exerciseType}
                        </Badge>
                        <Badge variant="outline" className="text-xs">
                            {exercise.exercise.difficultyLevel}
                        </Badge>
                        {exercise.exercise.isCardio && (
                            <Badge variant="outline" className="text-xs text-red-700 bg-red-50">
                                ❤️ Cardio
                            </Badge>
                        )}
                        {exercise.exercise.isIsometric && (
                            <Badge variant="outline" className="text-xs text-purple-700 bg-purple-50">
                                🛡️ Hold
                            </Badge>
                        )}
                    </div>

                    {/* Exercise Stats */}
                    <div className="flex items-center gap-3 sm:gap-4 text-xs sm:text-sm text-gray-600 mb-4">
                        <div className="flex items-center gap-1">
                            <Clock className="w-3 h-3 sm:w-4 sm:h-4"/>
                            <span>{exercise.exercise.estimatedDurationMinutes} min</span>
                        </div>
                        <div className="flex items-center gap-1">
                            <Target className="w-3 h-3 sm:w-4 sm:h-4"/>
                            <span>{exercise.exercise.estimatedCalories} cal</span>
                        </div>
                        {exercise.exercise.averageRating > 0 && (
                            <div className="flex items-center gap-1">
                                <span>⭐</span>
                                <span>{exercise.exercise.averageRating.toFixed(1)}</span>
                            </div>
                        )}
                    </div>

                    {/* Action Buttons */}
                    <div className="flex flex-wrap gap-2 pt-2 border-t border-gray-100">
                        {exercise.completed ? (
                            <div className="flex items-center text-green-600 text-sm font-medium">
                                <CheckCircle className="w-4 h-4 mr-1"/>
                                <span className="hidden sm:inline">Completed</span>
                                <span className="sm:hidden">✓ Done</span>
                            </div>
                        ) : (
                            <>
                                {/* Primary Action: Start Workout */}
                                <Button
                                    size="sm"
                                    onClick={() => onStartWorkout(exercise.id)}
                                    className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 text-xs sm:text-sm font-medium"
                                >
                                    <Play className="w-4 h-4 mr-1"/>
                                    <span className="hidden sm:inline">Start Workout</span>
                                    <span className="sm:hidden">▶️</span>
                                </Button>

                                {/* Secondary: Edit Configuration */}
                                <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => onEditExercise(exercise)}
                                    className="text-blue-600 hover:bg-blue-50 border-blue-200 px-3 py-2 text-xs sm:text-sm"
                                >
                                    <Settings className="w-4 h-4 mr-1"/>
                                    <span className="hidden sm:inline">Edit</span>
                                    <span className="sm:hidden">⚙️</span>
                                </Button>
                            </>
                        )}

                        <Button
                            size="sm"
                            variant="outline"
                            onClick={() => onDeleteWorkout(exercise.id)}
                            className="text-red-600 hover:bg-red-50 border-red-200 px-3 py-2 text-xs sm:text-sm"
                        >
                            <span className="hidden sm:inline mr-1">🗑️</span>
                            <span className="hidden sm:inline">Delete</span>
                            <span className="sm:hidden">✗</span>
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};