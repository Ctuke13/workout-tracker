import React from 'react';
import {UserGroupIcon} from '@heroicons/react/24/outline';
import WorkoutPlanCard from '../cards/WorkoutPlanCard';
import {WorkoutPlanInfo} from '../../types/api';
import {CategoryWithDescription} from '../../types/exercise';

interface WorkoutPlanGridProps {
    loading: boolean;
    filteredWorkoutPlans: WorkoutPlanInfo[];
    searchTerm: string;
    workoutPlanCategories: CategoryWithDescription[];
    selectedPlanCategory: string;
    planView: 'grid' | 'list';
    userTier: string;
    canAddToTargetDate: () => boolean;
    onPlanCategoryFilter: (categoryId: string) => void;
    onPlanViewChange: (view: 'grid' | 'list') => void;
    onWorkoutPlanSelect: (plan: WorkoutPlanInfo) => void;
    canAccessPlan: (plan: WorkoutPlanInfo) => boolean;
}

const WorkoutPlanGrid: React.FC<WorkoutPlanGridProps> = ({
                                                             loading,
                                                             filteredWorkoutPlans,
                                                             searchTerm,
                                                             workoutPlanCategories,
                                                             selectedPlanCategory,
                                                             planView,
                                                             userTier,
                                                             canAddToTargetDate,
                                                             onPlanCategoryFilter,
                                                             onPlanViewChange,
                                                             onWorkoutPlanSelect,
                                                             canAccessPlan
                                                         }) => {
    return (
        <div className="space-y-4">
            {/* Category Filter Pills */}
            <div className="flex gap-2 pb-4 border-b border-gray-200 overflow-x-auto">
                {workoutPlanCategories.map((category) => (
                    <button
                        key={category.id}
                        onClick={() => onPlanCategoryFilter(category.id)}
                        className={`flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all ${
                            selectedPlanCategory === category.id
                                ? 'bg-purple-100 text-purple-700 border-2 border-purple-300'
                                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                        }`}
                    >
                        <span>{category.emoji}</span>
                        <span>{category.name}</span>
                        <span className="bg-white px-2 py-0.5 rounded-full text-xs">
                            {category.count}
                        </span>
                    </button>
                ))}
            </div>

            {/* View Toggle */}
            <div className="flex items-center justify-between">
                <p className="text-sm text-gray-600">
                    {filteredWorkoutPlans.length} plan{filteredWorkoutPlans.length !== 1 ? 's' : ''} found
                </p>
                <div className="flex bg-gray-100 rounded-lg p-1">
                    <button
                        onClick={() => onPlanViewChange('grid')}
                        className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                            planView === 'grid'
                                ? 'bg-white text-gray-900 shadow-sm'
                                : 'text-gray-600'
                        }`}
                    >
                        Grid
                    </button>
                    <button
                        onClick={() => onPlanViewChange('list')}
                        className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                            planView === 'list'
                                ? 'bg-white text-gray-900 shadow-sm'
                                : 'text-gray-600'
                        }`}
                    >
                        List
                    </button>
                </div>
            </div>

            {/* Workout Plans Display */}
            {loading ? (
                <div className={`grid gap-4 ${planView === 'grid' ? 'grid-cols-1 md:grid-cols-2' : 'grid-cols-1'}`}>
                    {[1, 2, 3, 4].map((i) => (
                        <div key={i} className="animate-pulse">
                            <div className="bg-gradient-to-r from-gray-200 to-gray-300 h-32 rounded-2xl"></div>
                        </div>
                    ))}
                </div>
            ) : filteredWorkoutPlans.length > 0 ? (
                <div className={`grid gap-4 ${planView === 'grid' ? 'grid-cols-1 md:grid-cols-2' : 'grid-cols-1'}`}>
                    {filteredWorkoutPlans.map((plan) => (
                        <WorkoutPlanCard
                            key={plan.id}
                            plan={plan}
                            onSelect={() => onWorkoutPlanSelect(plan)}
                            canAccess={canAccessPlan(plan)}
                            userTier={userTier}
                            disabled={!canAddToTargetDate()}
                            viewMode={planView}
                        />
                    ))}
                </div>
            ) : (
                <div className="text-center py-12">
                    <div
                        className="w-20 h-20 mx-auto mb-6 bg-gradient-to-br from-purple-100 to-pink-100 rounded-full flex items-center justify-center">
                        <UserGroupIcon className="w-10 h-10 text-gray-400"/>
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">
                        {searchTerm ? 'No workout plans found' : 'No workout plans available'}
                    </h3>
                    <p className="text-gray-500 max-w-sm mx-auto">
                        {searchTerm ? 'Try a different search term' : 'Loading workout plans...'}
                    </p>
                </div>
            )}
        </div>
    );
};

export default WorkoutPlanGrid;