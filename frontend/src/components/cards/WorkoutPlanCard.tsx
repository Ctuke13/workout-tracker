import React from 'react';
import {
    ClockIcon,
    StarIcon,
    UserGroupIcon,
    LockClosedIcon,
    CogIcon
} from '@heroicons/react/24/outline';
import {WorkoutPlanInfo} from '../../types/api';

interface WorkoutPlanCardProps {
    plan: WorkoutPlanInfo;
    onSelect: () => void;
    canAccess: boolean;
    userTier: string;
    disabled?: boolean;
    viewMode?: 'grid' | 'list';
}

const WorkoutPlanCard: React.FC<WorkoutPlanCardProps> = ({
                                                             plan,
                                                             onSelect,
                                                             canAccess,
                                                             userTier,
                                                             disabled = false,
                                                             viewMode = 'grid'
                                                         }) => {
    const isLocked = !canAccess;
    const isDisabled = disabled || isLocked;

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

    const getTierColor = (tier: string | undefined) => {
        const tierLevel = tier || 'FREE';
        switch (tierLevel) {
            case 'FREE':
                return 'bg-green-100 text-green-700 border-green-200';
            case 'PLUS':
                return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'PRO':
                return 'bg-purple-100 text-purple-700 border-purple-200';
            default:
                return 'bg-gray-100 text-gray-700 border-gray-200';
        }
    };

    const getCardLayout = () => {
        if (viewMode === 'list') {
            return 'flex items-center p-4 space-x-4';
        }
        return 'flex flex-col p-6 space-y-4';
    };

    return (
        <div
            className={`
                group bg-white rounded-2xl border border-gray-200 transition-all duration-300
                ${isDisabled
                ? 'opacity-50 cursor-not-allowed'
                : 'hover:shadow-lg hover:border-purple-300 cursor-pointer active:scale-[0.98] hover:-translate-y-1'
            }
                ${isLocked ? 'bg-gray-50' : ''}
            `}
            onClick={isDisabled ? undefined : onSelect}
        >
            <div className={getCardLayout()}>
                {/* Plan Icon/Image */}
                <div className={`
                    flex-shrink-0 rounded-xl flex items-center justify-center text-2xl font-bold
                    ${viewMode === 'list' ? 'w-16 h-16' : 'w-20 h-20 mx-auto'}
                    ${isLocked ? 'bg-gray-200 text-gray-500' : 'bg-gradient-to-br from-purple-100 to-blue-100 text-purple-600'}
                `}>
                    {isLocked ? <LockClosedIcon className="w-8 h-8"/> : '📋'}
                </div>

                {/* Plan Content */}
                <div className={`${viewMode === 'list' ? 'flex-1' : ''}`}>
                    <div className={`${viewMode === 'list' ? 'flex items-start justify-between' : 'text-center'}`}>
                        <div className={`${viewMode === 'list' ? 'flex-1' : ''}`}>
                            <h3 className={`font-bold text-gray-900 group-hover:text-purple-900 transition-colors
                                ${viewMode === 'list' ? 'text-lg mb-1' : 'text-xl mb-2'}
                            `}>
                                {plan.name || plan.workoutName || 'Unnamed Workout Plan'}
                            </h3>

                            {plan.description && (
                                <p className={`text-gray-600 group-hover:text-gray-700 transition-colors
                                    ${viewMode === 'list' ? 'text-sm line-clamp-2' : 'text-sm mb-4 line-clamp-3'}
                                `}>
                                    {viewMode === 'list' && plan.description.length > 100
                                        ? `${plan.description.substring(0, 100)}...`
                                        : plan.description
                                    }
                                </p>
                            )}
                        </div>

                        {/* Quick Stats for List View */}
                        {viewMode === 'list' && (
                            <div className="flex items-center space-x-4 text-sm text-gray-500 ml-4">
                                <div className="flex items-center">
                                    <ClockIcon className="w-4 h-4 mr-1"/>
                                    {plan.estimatedDurationMinutes || 0}min
                                </div>
                                <div className="flex items-center">
                                    <UserGroupIcon className="w-4 h-4 mr-1"/>
                                    {plan.exerciseCount || 0}
                                </div>
                                {plan.averageRating > 0 && (
                                    <div className="flex items-center">
                                        <StarIcon className="w-4 h-4 mr-1 text-yellow-500"/>
                                        {plan.averageRating.toFixed(1)}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    {/* Plan Tags */}
                    <div className={`flex flex-wrap gap-2 ${viewMode === 'list' ? 'mt-2' : 'mb-4'}`}>
                        <span
                            className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getTierColor(plan.subscriptionTierRequired)}`}>
                            {plan.subscriptionTierRequired || 'FREE'}
                        </span>
                        <span
                            className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${getDifficultyColor(plan.difficulty)}`}>
                            {plan.difficulty || plan.difficultyLevel || 'INTERMEDIATE'}
                        </span>
                        <span
                            className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-700 border border-gray-200">
                            <UserGroupIcon className="w-3 h-3 mr-1"/>
                            {plan.exerciseCount || 0} exercises
                        </span>
                    </div>

                    {/* Grid View Stats */}
                    {viewMode === 'grid' && (
                        <div className="flex items-center justify-center space-x-4 text-sm text-gray-500">
                            <div className="flex items-center">
                                <ClockIcon className="w-4 h-4 mr-1"/>
                                {plan.estimatedDurationMinutes || 0}min
                            </div>
                            {plan.averageRating && plan.averageRating > 0 && (
                                <div className="flex items-center">
                                    <StarIcon className="w-4 h-4 mr-1 text-yellow-500"/>
                                    {plan.averageRating.toFixed(1)}
                                </div>
                            )}
                        </div>
                    )}

                    {/* Action Hint */}
                    <div className={`${viewMode === 'list' ? 'text-right mt-2' : 'text-center mt-4'}`}>
                        {isLocked ? (
                            <span className="text-xs font-medium text-gray-500">
                                Upgrade to access →
                            </span>
                        ) : (
                            <div className="flex items-center justify-center gap-2">
                                <CogIcon className="w-4 h-4 text-purple-600"/>
                                <span
                                    className="text-xs font-medium text-gray-900 group-hover:text-purple-600 transition-colors">
                                    Configure & Schedule →
                                </span>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default WorkoutPlanCard;