import { DifficultyLevel } from '../types/exercise';

// Equipment name formatting
export const formatEquipmentName = (equipment: string): string => {
    const equipmentMap: Record<string, string> = {
        'dumbbells': 'Dumbbells',
        'dumbbell': 'Dumbbells',
        'resistance_bands': 'Resistance Bands',
        'kettlebell': 'Kettlebell',
        'yoga_mat': 'Yoga Mat',
        'bodyweight': 'No Equipment',
        'none': 'No Equipment',
        'jump_rope': 'Jump Rope',
        'foam_roller': 'Foam Roller',
        'plyo_box': 'Plyo Box',
        'tennis_racket': 'Tennis Racket',
        'tennis_balls': 'Tennis Balls'
    };
    return equipmentMap[equipment.toLowerCase()] || equipment;
};

// Difficulty level color classes
export const getDifficultyColor = (difficulty: DifficultyLevel): string => {
    switch (difficulty.toLowerCase()) {
        case 'beginner':
            return 'text-green-600 bg-green-100';
        case 'intermediate':
            return 'text-orange-600 bg-orange-100';
        case 'advanced':
            return 'text-red-600 bg-red-100';
        default:
            return 'text-gray-600 bg-gray-100';
    }
};

// Format muscle groups for display
export const formatMuscleGroups = (muscleGroups: string[]): string[] => {
    if (!muscleGroups || muscleGroups.length === 0) return [];
    return muscleGroups.map((group: string) => {
        return group.toLowerCase().replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase());
    });
};

// Format numbers with commas
export const formatNumberWithCommas = (num: number): string => {
    return num.toLocaleString();
};

// Format duration for display
export const formatDuration = (minutes: number): string => {
    if (minutes < 60) {
        return `${minutes} min`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return remainingMinutes > 0
        ? `${hours}h ${remainingMinutes}m`
        : `${hours}h`;
};

// Format popularity level
export const getPopularityLevel = (usageCount: number): string => {
    if (usageCount > 2000) return 'High';
    if (usageCount > 1000) return 'Med';
    return 'Low';
};

// Truncate text with ellipsis
export const truncateText = (text: string, maxLength: number): string => {
    if (text.length <= maxLength) return text;
    return text.slice(0, maxLength) + '...';
};