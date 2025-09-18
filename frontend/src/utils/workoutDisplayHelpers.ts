// src/utils/workoutDisplayHelpers.ts
import {
    TrendingUp,
    TrendingDown,
    Target,
    CheckCircle,
    XCircle,
    Minus,
    Activity
} from 'lucide-react';
import {WorkoutResults} from '../types/exercise';

/**
 * Get performance color classes for different performance ratings
 */
export const getPerformanceColor = (rating: string): string => {
    switch (rating) {
        case 'EXCEEDED':
            return 'text-green-600 bg-green-50 border-green-200';
        case 'MET':
            return 'text-blue-600 bg-blue-50 border-blue-200';
        case 'PARTIAL':
            return 'text-yellow-600 bg-yellow-50 border-yellow-200';
        case 'BELOW_TARGET':
            return 'text-red-600 bg-red-50 border-red-200';
        default:
            return 'text-gray-600 bg-gray-50 border-gray-200';
    }
};

/**
 * Get performance icon component for different performance ratings
 */
export const getPerformanceIcon = (rating: string) => {
    switch (rating) {
        case 'EXCEEDED':
            return TrendingUp;
        case 'MET':
            return Target;
        case 'PARTIAL':
            return Minus;
        case 'BELOW_TARGET':
            return TrendingDown;
        default:
            return Activity;
    }
};

/**
 * Get status icon for criteria evaluation
 */
export const getStatusIcon = (status: string) => {
    switch (status) {
        case 'EXCEEDED':
            return TrendingUp;
        case 'MET':
            return CheckCircle;
        case 'PARTIAL':
            return Minus;
        case 'BELOW_TARGET':
            return TrendingDown;
        default:
            return XCircle;
    }
};

/**
 * Get status color classes for criteria
 */
export const getStatusColor = (status: string): string => {
    switch (status) {
        case 'EXCEEDED':
            return 'text-green-600 bg-green-50 border-green-200';
        case 'MET':
            return 'text-blue-600 bg-blue-50 border-blue-200';
        case 'PARTIAL':
            return 'text-yellow-600 bg-yellow-50 border-yellow-200';
        case 'BELOW_TARGET':
            return 'text-red-600 bg-red-50 border-red-200';
        default:
            return 'text-gray-600 bg-gray-50 border-gray-200';
    }
};

/**
 * Get performance message for display
 */
export const getPerformanceMessage = (rating: string): string => {
    switch (rating) {
        case 'EXCEEDED':
            return 'Exceeded targets!';
        case 'MET':
            return 'All targets achieved';
        case 'PARTIAL':
            return 'Most targets achieved';
        case 'BELOW_TARGET':
            return 'Some targets missed';
        default:
            return 'Completed';
    }
};

/**
 * Format time in seconds to MM:SS format
 */
export const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
};

/**
 * Format pace for display (min/mile or min/km)
 */
export const formatPaceDisplay = (pace: number, unit: 'miles' | 'km' = 'miles'): string => {
    const minutes = Math.floor(pace);
    const seconds = Math.round((pace - minutes) * 60);
    const unitLabel = unit === 'miles' ? 'mi' : 'km';
    return `${minutes}:${seconds.toString().padStart(2, '0')} min/${unitLabel}`;
};

/**
 * Get difficulty color classes
 */
export const getDifficultyColor = (difficulty: string): string => {
    switch (difficulty.toLowerCase()) {
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

/**
 * Get exercise type color classes
 */
export const getExerciseTypeColor = (type: string): string => {
    switch (type.toLowerCase()) {
        case 'strength':
            return 'bg-blue-100 text-blue-800 border-blue-200';
        case 'cardio':
            return 'bg-red-100 text-red-800 border-red-200';
        case 'flexibility':
        case 'isometric':
            return 'bg-purple-100 text-purple-800 border-purple-200';
        default:
            return 'bg-gray-100 text-gray-800 border-gray-200';
    }
};

/**
 * Get progress bar color based on performance rating
 */
export const getProgressBarColor = (rating: string): string => {
    switch (rating) {
        case 'EXCEEDED':
            return 'bg-green-500';
        case 'MET':
            return 'bg-blue-500';
        case 'PARTIAL':
            return 'bg-yellow-500';
        case 'BELOW_TARGET':
            return 'bg-red-500';
        default:
            return 'bg-gray-500';
    }
};

/**
 * Format weight for display
 */
export const formatWeight = (weight: number, unit: string): string => {
    return `${weight} ${unit}`;
};

/**
 * Format distance for display
 */
export const formatDistance = (distance: number, unit: string): string => {
    return `${distance.toFixed(1)} ${unit}`;
};

/**
 * Get RPE description
 */
export const getRpeDescription = (rpe: number): string => {
    const descriptions: Record<number, string> = {
        1: 'Very easy - warm up pace',
        2: 'Easy - could do this all day',
        3: 'Moderate - comfortable effort',
        4: 'Somewhat hard - breathing harder',
        5: 'Hard - challenging but sustainable',
        6: 'Hard+ - difficult to maintain',
        7: 'Very hard - can speak a few words',
        8: 'Very hard+ - can barely speak',
        9: 'Extremely hard - maximal effort',
        10: 'Maximum - cannot continue'
    };
    return descriptions[rpe] || 'Unknown intensity';
};

/**
 * Get workout tracking badge configuration
 */
export const getWorkoutTrackingBadge = (isCardio: boolean, isIsometric: boolean) => {
    if (isCardio) {
        return {
            text: 'Cardio',
            icon: '❤️',
            className: 'bg-red-100 text-red-700 border-red-200'
        };
    }
    if (isIsometric) {
        return {
            text: 'Hold',
            icon: '🛡️',
            className: 'bg-purple-100 text-purple-700 border-purple-200'
        };
    }
    return {
        text: 'Reps',
        icon: '🔥',
        className: 'bg-blue-100 text-blue-700 border-blue-200'
    };
};