import React, { useState } from 'react';
import { ChevronDown, Filter, X } from 'lucide-react';
import { Goal, ExerciseTypeOption, ExerciseFilters, SortOption } from '../../types/exercise';
import { equipmentOptions, difficultyOptions, sortOptions } from '../../services/mockData';

interface DesktopFiltersProps {
    filters: ExerciseFilters;
    goals: Goal[];
    exerciseTypeOptions: ExerciseTypeOption[];
    onUpdateFilter: (key: keyof ExerciseFilters, value: any) => void;
    onClearFilters: () => void;
}

export const DesktopFilters: React.FC<DesktopFiltersProps> = ({
                                                                  filters,
                                                                  goals,
                                                                  exerciseTypeOptions,
                                                                  onUpdateFilter,
                                                                  onClearFilters
                                                              }) => {
    const [showAdvanced, setShowAdvanced] = useState(false);

    return (
        <div className="hidden sm:block">
            {/* Goal Pills */}
            <div className="flex gap-2 flex-wrap justify-center mb-6">
                {goals.slice(0, 6).map((goal: Goal) => (
                    <button
                        key={goal.id}
                        onClick={() => onUpdateFilter('activeGoal', goal.id)}
                        className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium transition-all ${
                            filters.activeGoal === goal.id
                                ? 'bg-blue-600 text-white shadow-md'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                    >
                        <span>{goal.emoji}</span>
                        <span>{goal.name}</span>
                        <span className="text-xs bg-white/20 px-2 py-0.5 rounded-full">
                            {goal.count}
                        </span>
                    </button>
                ))}
            </div>

            {/* Quick Filters */}
            <div className="flex gap-4 flex-wrap justify-center items-center mb-4">
                {/* Exercise Type */}
                <div className="flex items-center gap-2">
                    <label className="text-sm font-medium text-gray-700">Type:</label>
                    <div className="relative">
                        <select
                            value={filters.exerciseType}
                            onChange={(e) => onUpdateFilter('exerciseType', e.target.value)}
                            className="appearance-none bg-white border border-gray-300 rounded-lg px-3 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {exerciseTypeOptions.slice(0, 5).map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                        <ChevronDown className="absolute right-2 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                    </div>
                </div>

                {/* Difficulty */}
                <div className="flex items-center gap-2">
                    <label className="text-sm font-medium text-gray-700">Difficulty:</label>
                    <div className="relative">
                        <select
                            value={filters.difficulty}
                            onChange={(e) => onUpdateFilter('difficulty', e.target.value)}
                            className="appearance-none bg-white border border-gray-300 rounded-lg px-3 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {difficultyOptions.map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                        <ChevronDown className="absolute right-2 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                    </div>
                </div>

                {/* Equipment */}
                <div className="flex items-center gap-2">
                    <label className="text-sm font-medium text-gray-700">Equipment:</label>
                    <div className="relative">
                        <select
                            value={filters.equipment}
                            onChange={(e) => onUpdateFilter('equipment', e.target.value)}
                            className="appearance-none bg-white border border-gray-300 rounded-lg px-3 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {equipmentOptions.slice(0, 6).map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                        <ChevronDown className="absolute right-2 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                    </div>
                </div>

                {/* Sort */}
                <div className="flex items-center gap-2">
                    <label className="text-sm font-medium text-gray-700">Sort:</label>
                    <div className="relative">
                        <select
                            value={filters.sortBy}
                            onChange={(e) => onUpdateFilter('sortBy', e.target.value as SortOption)}
                            className="appearance-none bg-white border border-gray-300 rounded-lg px-3 py-2 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        >
                            {sortOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                        <ChevronDown className="absolute right-2 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                    </div>
                </div>

                {/* More Filters Button */}
                <button
                    onClick={() => setShowAdvanced(!showAdvanced)}
                    className="flex items-center gap-2 px-3 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-200 transition-colors"
                >
                    <Filter className="w-4 h-4" />
                    More Filters
                </button>

                {/* Clear Filters */}
                <button
                    onClick={onClearFilters}
                    className="flex items-center gap-2 px-3 py-2 text-gray-500 hover:text-gray-700 text-sm transition-colors"
                >
                    <X className="w-4 h-4" />
                    Clear
                </button>
            </div>

            {/* Advanced Filters */}
            {showAdvanced && (
                <div className="bg-gray-50 rounded-lg p-4 mb-4 space-y-4">
                    <h4 className="text-sm font-semibold text-gray-900 mb-3">Advanced Filters</h4>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        {/* Rating Filter */}
                        <div>
                            <label className="block text-sm text-gray-600 mb-2">
                                Minimum Rating: {filters.minRating}
                            </label>
                            <input
                                type="range"
                                min="0"
                                max="5"
                                step="0.1"
                                value={filters.minRating}
                                onChange={(e) => onUpdateFilter('minRating', parseFloat(e.target.value))}
                                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                            />
                            <div className="flex justify-between text-xs text-gray-500 mt-1">
                                <span>0</span>
                                <span>5</span>
                            </div>
                        </div>

                        {/* Duration Filter */}
                        <div>
                            <label className="block text-sm text-gray-600 mb-2">
                                Max Duration: {filters.maxDuration} min
                            </label>
                            <input
                                type="range"
                                min="5"
                                max="300"
                                step="5"
                                value={filters.maxDuration}
                                onChange={(e) => onUpdateFilter('maxDuration', parseInt(e.target.value))}
                                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                            />
                            <div className="flex justify-between text-xs text-gray-500 mt-1">
                                <span>5 min</span>
                                <span>300 min</span>
                            </div>
                        </div>

                        {/* Professional Only */}
                        <div className="flex items-center">
                            <input
                                type="checkbox"
                                id="professional-only-desktop"
                                checked={filters.professionalOnly}
                                onChange={(e) => onUpdateFilter('professionalOnly', e.target.checked)}
                                className="h-4 w-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
                            />
                            <label htmlFor="professional-only-desktop" className="ml-2 text-sm text-gray-600">
                                Professional trainers only
                            </label>
                        </div>
                    </div>

                    <div className="flex justify-end">
                        <button
                            onClick={() => setShowAdvanced(false)}
                            className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
                        >
                            Apply Advanced Filters
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};