import React from 'react';
import {MagnifyingGlassIcon} from '@heroicons/react/24/outline';
import ExerciseCard from '../cards/ExerciseCard';
import {Exercise} from '../../types/exercise';

interface ExerciseGridProps {
    loading: boolean;
    filteredExercisesWithFavorites: (Exercise & { isFavorite: boolean })[];
    searchTerm: string;
    canAddToTargetDate: () => boolean;
    user: any;
    onExerciseSelect: (exercise: Exercise) => void;
    onDragStart?: (exercise: Exercise) => void;
    onToggleFavorite: (exercise: Exercise, event?: React.MouseEvent) => void;
}

const ExerciseGrid: React.FC<ExerciseGridProps> = ({
                                                       loading,
                                                       filteredExercisesWithFavorites,
                                                       searchTerm,
                                                       canAddToTargetDate,
                                                       user,
                                                       onExerciseSelect,
                                                       onDragStart,
                                                       onToggleFavorite
                                                   }) => {
    return (
        <div className="space-y-2">
            {loading ? (
                <div className="space-y-3">
                    {[1, 2, 3, 4].map((i) => (
                        <div key={i} className="animate-pulse">
                            <div className="bg-gradient-to-r from-gray-200 to-gray-300 h-24 rounded-2xl"></div>
                        </div>
                    ))}
                </div>
            ) : filteredExercisesWithFavorites.length > 0 ? (
                <div className="space-y-2">
                    {filteredExercisesWithFavorites.map((exercise) => (
                        <ExerciseCard
                            key={exercise.id}
                            exercise={exercise}
                            onSelect={() => onExerciseSelect(exercise)}
                            onDragStart={() => onDragStart?.(exercise)}
                            onToggleFavorite={onToggleFavorite}
                            disabled={!canAddToTargetDate()}
                            showFavoriteButton={user ? true : false}
                        />
                    ))}
                </div>
            ) : (
                <div className="text-center py-12">
                    <div
                        className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-blue-100 to-purple-100 rounded-full flex items-center justify-center">
                        <MagnifyingGlassIcon className="w-10 h-10 text-gray-400"/>
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">
                        {searchTerm ? 'No exercises found' : 'No exercises available'}
                    </h3>
                    <p className="text-gray-500 max-w-sm mx-auto">
                        {searchTerm ? 'Try a different search term' : 'Loading exercises...'}
                    </p>
                </div>
            )}
        </div>
    );
};

export default ExerciseGrid;