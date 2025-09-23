import React from 'react';
import {StarIcon} from '@heroicons/react/24/outline';
import ExerciseCard from '../cards/ExerciseCard';
import {exerciseApi} from '../../services/exerciseApi';
import {Exercise} from '../../types/exercise';

interface FavoritesGridProps {
    user: any;
    loadingFavorites: boolean;
    filteredFavoriteExercises: (Exercise & { isFavorite: boolean })[];
    searchTerm: string;
    canAddToTargetDate: () => boolean;
    onExerciseSelect: (exercise: Exercise) => void;
    onDragStart?: (exercise: Exercise) => void;
    onToggleFavorite: (exercise: Exercise, event?: React.MouseEvent) => void;
    onSwitchToExercisesTab: () => void;
}

const FavoritesGrid: React.FC<FavoritesGridProps> = ({
                                                         user,
                                                         loadingFavorites,
                                                         filteredFavoriteExercises,
                                                         searchTerm,
                                                         canAddToTargetDate,
                                                         onExerciseSelect,
                                                         onDragStart,
                                                         onToggleFavorite,
                                                         onSwitchToExercisesTab
                                                     }) => {
    return (
        <div className="space-y-2">
            {!user ? (
                <div className="text-center py-12">
                    <div
                        className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-yellow-100 to-orange-100 rounded-full flex items-center justify-center">
                        <StarIcon className="w-10 h-10 text-yellow-500"/>
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">Sign in to see favorites</h3>
                    <p className="text-gray-500 max-w-sm mx-auto">
                        Create an account to save your favorite exercises
                    </p>
                </div>
            ) : loadingFavorites ? (
                <div className="space-y-3">
                    {[1, 2, 3, 4].map((i) => (
                        <div key={i} className="animate-pulse">
                            <div className="bg-gradient-to-r from-yellow-200 to-orange-300 h-24 rounded-2xl"></div>
                        </div>
                    ))}
                </div>
            ) : filteredFavoriteExercises.length > 0 ? (
                <div className="space-y-2">
                    <div className="flex items-center justify-between mb-4 px-2">
                        <p className="text-sm text-gray-600">
                            ⭐ {filteredFavoriteExercises.length} favorite
                            exercise{filteredFavoriteExercises.length !== 1 ? 's' : ''}
                        </p>
                        <button
                            onClick={async () => {
                                if (window.confirm('Remove all favorites? This cannot be undone.')) {
                                    try {
                                        await exerciseApi.clearAllFavorites();
                                    } catch (error) {
                                        console.error('Failed to clear favorites:', error);
                                    }
                                }
                            }}
                            className="text-xs text-red-600 hover:text-red-700 underline"
                        >
                            Clear All
                        </button>
                    </div>
                    {filteredFavoriteExercises.map((exercise) => (
                        <ExerciseCard
                            key={exercise.id}
                            exercise={{
                                ...exercise,
                                isFavorite: true // Force yellow star in favorites tab
                            }}
                            onSelect={() => onExerciseSelect(exercise)}
                            onDragStart={() => onDragStart?.(exercise)}
                            onToggleFavorite={onToggleFavorite}
                            disabled={!canAddToTargetDate()}
                            showFavoriteButton={true}
                            isFavoritesTab={true}
                        />
                    ))}
                </div>
            ) : (
                <div className="text-center py-12">
                    <div
                        className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-yellow-100 to-orange-100 rounded-full flex items-center justify-center">
                        <StarIcon className="w-10 h-10 text-yellow-500"/>
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">
                        {searchTerm ? 'No favorite exercises found' : 'No favorites yet'}
                    </h3>
                    <p className="text-gray-500 max-w-sm mx-auto">
                        {searchTerm ? 'Try a different search term' : 'Browse exercises and tap the ⭐ star to add favorites'}
                    </p>
                    <button
                        onClick={onSwitchToExercisesTab}
                        className="mt-4 px-4 py-2 bg-yellow-500 hover:bg-yellow-600 text-white rounded-lg font-medium transition-colors"
                    >
                        Browse Exercises
                    </button>
                </div>
            )}
        </div>
    );
};

export default FavoritesGrid;