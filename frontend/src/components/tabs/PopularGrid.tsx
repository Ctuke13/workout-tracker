import React from 'react';
import {TrophyIcon} from '@heroicons/react/24/outline';
import ExerciseCard from '../cards/ExerciseCard';
import {Exercise} from '../../types/exercise';

interface PopularGridProps {
    loading: boolean;
    popularExercises: Exercise[];
    userFavoriteIds: Set<number>;
    canAddToTargetDate: () => boolean;
    user: any;
    onExerciseSelect: (exercise: Exercise) => void;
    onDragStart?: (exercise: Exercise) => void;
    onToggleFavorite: (exercise: Exercise, event?: React.MouseEvent) => void;
}

const PopularGrid: React.FC<PopularGridProps> = ({
                                                     loading,
                                                     popularExercises,
                                                     userFavoriteIds,
                                                     canAddToTargetDate,
                                                     user,
                                                     onExerciseSelect,
                                                     onDragStart,
                                                     onToggleFavorite
                                                 }) => {
    return (
        <div className="space-y-2">
            {popularExercises.length > 0 ? (
                popularExercises.map((exercise) => (
                    <ExerciseCard
                        key={exercise.id}
                        exercise={{
                            ...exercise,
                            isFavorite: userFavoriteIds.has(exercise.id)
                        }}
                        onSelect={() => onExerciseSelect(exercise)}
                        onDragStart={() => onDragStart?.(exercise)}
                        onToggleFavorite={onToggleFavorite}
                        disabled={!canAddToTargetDate()}
                        showPopularBadge
                        showFavoriteButton={user ? true : false}
                    />
                ))
            ) : (
                <div className="text-center py-12">
                    <div
                        className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-yellow-100 to-orange-100 rounded-full flex items-center justify-center">
                        <TrophyIcon className="w-10 h-10 text-yellow-500"/>
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">
                        {loading ? 'Loading popular exercises...' : 'No popular exercises found'}
                    </h3>
                    <p className="text-gray-500 max-w-sm mx-auto">
                        {loading ? 'Fetching from backend...' : 'Popular exercises will appear here when available'}
                    </p>
                </div>
            )}
        </div>
    );
};

export default PopularGrid;