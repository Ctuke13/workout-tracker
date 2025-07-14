import React from 'react';
import { X, Filter } from 'lucide-react';
import { ExerciseFilters, SortOption, ExerciseTypeOption } from '../../types/exercise';
import {
    equipmentOptions,
    difficultyOptions,
    sortOptions
} from '../../services/mockData';

interface MobileFilterDrawerProps {
    isOpen: boolean;
    onClose: () => void;
    filters: ExerciseFilters;
    exerciseTypeOptions: ExerciseTypeOption[];
    onFilterChange: <K extends keyof ExerciseFilters>(key: K, value: ExerciseFilters[K]) => void;
}

export const MobileFilterDrawer: React.FC<MobileFilterDrawerProps> = ({
                                                                          isOpen,
                                                                          onClose,
                                                                          filters,
                                                                          exerciseTypeOptions,
                                                                          onFilterChange
                                                                      }) => {
    if (!isOpen) return null;

    const handleFilterChange = (filterKey: keyof ExerciseFilters, value: any) => {
        onFilterChange(filterKey, value);
    };

    const handleCheckboxChange = (filterKey: 'selectedEquipment' | 'selectedDifficulty', option: string) => {
        const currentValue = filters[filterKey];
        if (currentValue === option) {
            onFilterChange(filterKey, 'all');
        } else {
            onFilterChange(filterKey, option);
        }
    };

    return (
        <div className="fixed inset-0 z-50 lg:hidden">
            {/* Backdrop */}
            <div
                className="fixed inset-0 bg-black bg-opacity-50"
                onClick={onClose}
            />

            {/* Drawer */}
            <div className="fixed inset-y-0 right-0 w-80 bg-white shadow-xl overflow-y-auto">
                <div className="p-6">
                    {/* Header */}
                    <div className="flex items-center justify-between mb-6">
                        <div className="flex items-center gap-2">
                            <Filter className="w-5 h-5 text-blue-600" />
                            <h2 className="text-lg font-semibold">Filters</h2>
                        </div>
                        <button
                            onClick={onClose}
                            className="p-2 hover:bg-gray-100 rounded-full"
                        >
                            <X className="w-5 h-5" />
                        </button>
                    </div>

                    {/* Exercise Type Filter */}
                    <div className="mb-6">
                        <h3 className="font-medium mb-3">Exercise Type</h3>
                        <div className="space-y-2">
                            <label className="flex items-center gap-2">
                                <input
                                    type="radio"
                                    name="exerciseType"
                                    checked={filters.selectedExerciseType === 'all'}
                                    onChange={() => handleFilterChange('selectedExerciseType', 'all')}
                                    className="w-4 h-4 text-blue-600"
                                />
                                <span className="text-sm">All Types</span>
                            </label>
                            {exerciseTypeOptions.map((option) => (
                                <label key={option.value} className="flex items-center gap-2">
                                    <input
                                        type="radio"
                                        name="exerciseType"
                                        checked={filters.selectedExerciseType === option.value}
                                        onChange={() => handleFilterChange('selectedExerciseType', option.value)}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <span className="text-sm flex items-center gap-1">
                                        {option.emoji} {option.display}
                                        {option.count && <span className="text-xs text-gray-500">({option.count})</span>}
                                    </span>
                                </label>
                            ))}
                        </div>
                    </div>

                    {/* Difficulty Filter */}
                    <div className="mb-6">
                        <h3 className="font-medium mb-3">Difficulty</h3>
                        <div className="space-y-2">
                            <label className="flex items-center gap-2">
                                <input
                                    type="radio"
                                    name="difficulty"
                                    checked={filters.selectedDifficulty === 'all'}
                                    onChange={() => handleFilterChange('selectedDifficulty', 'all')}
                                    className="w-4 h-4 text-blue-600"
                                />
                                <span className="text-sm">All Levels</span>
                            </label>
                            {difficultyOptions.map((option) => (
                                <label key={option} className="flex items-center gap-2">
                                    <input
                                        type="radio"
                                        name="difficulty"
                                        checked={filters.selectedDifficulty === option.toLowerCase()}
                                        onChange={() => handleFilterChange('selectedDifficulty', option.toLowerCase())}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <span className="text-sm">{option}</span>
                                </label>
                            ))}
                        </div>
                    </div>

                    {/* Equipment Filter */}
                    <div className="mb-6">
                        <h3 className="font-medium mb-3">Equipment</h3>
                        <div className="space-y-2">
                            <label className="flex items-center gap-2">
                                <input
                                    type="radio"
                                    name="equipment"
                                    checked={filters.selectedEquipment === 'all'}
                                    onChange={() => handleFilterChange('selectedEquipment', 'all')}
                                    className="w-4 h-4 text-blue-600"
                                />
                                <span className="text-sm">All Equipment</span>
                            </label>
                            {equipmentOptions.map((option) => (
                                <label key={option} className="flex items-center gap-2">
                                    <input
                                        type="radio"
                                        name="equipment"
                                        checked={filters.selectedEquipment === option}
                                        onChange={() => handleFilterChange('selectedEquipment', option)}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <span className="text-sm">{option}</span>
                                </label>
                            ))}
                        </div>
                    </div>

                    {/* Professional Only Filter */}
                    <div className="mb-6">
                        <label className="flex items-center gap-2">
                            <input
                                type="checkbox"
                                checked={filters.showProfessionalOnly}
                                onChange={(e) => handleFilterChange('showProfessionalOnly', e.target.checked)}
                                className="w-4 h-4 text-blue-600"
                            />
                            <span className="text-sm">Professional Only</span>
                        </label>
                    </div>

                    {/* Rating Filter */}
                    <div className="mb-6">
                        <h3 className="font-medium mb-3">Minimum Rating</h3>
                        <div className="space-y-2">
                            {[0, 3, 4, 4.5].map((rating) => (
                                <label key={rating} className="flex items-center gap-2">
                                    <input
                                        type="radio"
                                        name="rating"
                                        checked={filters.minRating === rating}
                                        onChange={() => handleFilterChange('minRating', rating)}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <span className="text-sm">
                                        {rating === 0 ? 'All Ratings' : `${rating}+ Stars`}
                                    </span>
                                </label>
                            ))}
                        </div>
                    </div>

                    {/* Sort Filter */}
                    <div className="mb-6">
                        <h3 className="font-medium mb-3">Sort By</h3>
                        <div className="space-y-2">
                            {sortOptions.map((option) => (
                                <label key={option.value} className="flex items-center gap-2">
                                    <input
                                        type="radio"
                                        name="sort"
                                        checked={filters.sortBy === option.value}
                                        onChange={() => handleFilterChange('sortBy', option.value as SortOption)}
                                        className="w-4 h-4 text-blue-600"
                                    />
                                    <span className="text-sm">{option.label}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};