import { Exercise, Goal, ExerciseTypeOption } from '../types/exercise';

export const mockExercises: Exercise[] = [
    {
        id: 1,
        name: "Push-ups",
        emoji: "💪",
        description: "Classic upper body strength exercise that builds chest, shoulder, and tricep strength",
        exerciseType: "STRENGTH",
        exerciseTypeDisplay: "Strength Training",
        difficultyLevel: "BEGINNER",
        estimatedDurationMinutes: 10,
        estimatedCalories: 85,
        targetMuscleGroups: ["CHEST", "SHOULDERS", "TRICEPS"],
        equipmentRequired: ["bodyweight"],
        benefits: ["Builds upper body strength", "Improves core stability", "No equipment needed"],
        tips: ["Keep your body in a straight line", "Lower chest to ground", "Push up explosively"],
        videoUrl: "https://example.com/pushups",
        createdByProfessional: true,
        usageCount: 2840,
        averageRating: 4.5,
        totalRatings: 1250,
        published: true
    },
    {
        id: 2,
        name: "Yoga Flow Sequence",
        emoji: "🧘‍♀️",
        description: "Dynamic yoga flow to improve flexibility and mind-body connection",
        exerciseType: "FLEXIBILITY",
        exerciseTypeDisplay: "Flexibility & Mobility",
        difficultyLevel: "INTERMEDIATE",
        estimatedDurationMinutes: 25,
        estimatedCalories: 150,
        targetMuscleGroups: ["FULL_BODY", "CORE"],
        equipmentRequired: ["yoga_mat"],
        benefits: ["Improves flexibility", "Enhances balance", "Reduces stress"],
        tips: ["Focus on breathing", "Move slowly", "Listen to your body"],
        videoUrl: "https://example.com/yoga-flow",
        createdByProfessional: true,
        usageCount: 1890,
        averageRating: 4.6,
        totalRatings: 920,
        published: true
    },
    {
        id: 3,
        name: "Single-Leg Balance Challenge",
        emoji: "⚖️",
        description: "Progressive balance training to improve stability and coordination",
        exerciseType: "BALANCE",
        exerciseTypeDisplay: "Balance & Stability",
        difficultyLevel: "INTERMEDIATE",
        estimatedDurationMinutes: 12,
        estimatedCalories: 60,
        targetMuscleGroups: ["LEGS", "CORE"],
        equipmentRequired: ["none"],
        benefits: ["Improves stability", "Prevents falls", "Enhances coordination"],
        tips: ["Start with eyes open", "Use a wall for support", "Progress gradually"],
        videoUrl: "https://example.com/balance",
        createdByProfessional: true,
        usageCount: 1340,
        averageRating: 4.3,
        totalRatings: 650,
        published: true
    },
    {
        id: 4,
        name: "Tennis Serve Practice",
        emoji: "🎾",
        description: "Sport-specific drills to improve tennis serve technique and power",
        exerciseType: "SPORTS_SPECIFIC",
        exerciseTypeDisplay: "Sports Specific",
        difficultyLevel: "ADVANCED",
        estimatedDurationMinutes: 30,
        estimatedCalories: 200,
        targetMuscleGroups: ["SHOULDERS", "CORE", "LEGS"],
        equipmentRequired: ["tennis_racket", "tennis_balls"],
        benefits: ["Improves serve power", "Better technique", "Sport performance"],
        tips: ["Focus on form first", "Use proper grip", "Follow through completely"],
        videoUrl: "https://example.com/tennis-serve",
        createdByProfessional: true,
        usageCount: 890,
        averageRating: 4.4,
        totalRatings: 340,
        published: true
    },
    {
        id: 5,
        name: "HIIT Sprint Intervals",
        emoji: "🏃‍♂️",
        description: "High-intensity cardiovascular training with sprint intervals",
        exerciseType: "CARDIO",
        exerciseTypeDisplay: "Cardiovascular",
        difficultyLevel: "ADVANCED",
        estimatedDurationMinutes: 18,
        estimatedCalories: 320,
        targetMuscleGroups: ["LEGS", "CARDIOVASCULAR"],
        equipmentRequired: ["none"],
        benefits: ["Burns calories fast", "Improves cardiovascular health", "Increases speed"],
        tips: ["Warm up properly", "Maintain form", "Cool down after"],
        videoUrl: "https://example.com/hiit-sprints",
        createdByProfessional: true,
        usageCount: 2100,
        averageRating: 4.2,
        totalRatings: 1100,
        published: true
    },
    {
        id: 6,
        name: "Box Jump Explosives",
        emoji: "📦",
        description: "Plyometric training for explosive lower body power development",
        exerciseType: "PLYOMETRIC",
        exerciseTypeDisplay: "Plyometric & Power",
        difficultyLevel: "ADVANCED",
        estimatedDurationMinutes: 15,
        estimatedCalories: 180,
        targetMuscleGroups: ["LEGS", "GLUTES"],
        equipmentRequired: ["plyo_box"],
        benefits: ["Builds explosive power", "Improves vertical jump", "Enhances athleticism"],
        tips: ["Land softly", "Step down carefully", "Start with lower boxes"],
        videoUrl: "https://example.com/box-jumps",
        createdByProfessional: true,
        usageCount: 1560,
        averageRating: 4.1,
        totalRatings: 780,
        published: true
    },
    {
        id: 7,
        name: "Post-Workout Recovery",
        emoji: "🛡️",
        description: "Gentle rehabilitation movements for muscle recovery and injury prevention",
        exerciseType: "REHABILITATION",
        exerciseTypeDisplay: "Rehabilitation",
        difficultyLevel: "BEGINNER",
        estimatedDurationMinutes: 20,
        estimatedCalories: 40,
        targetMuscleGroups: ["RECOVERY"],
        equipmentRequired: ["foam_roller"],
        benefits: ["Aids recovery", "Prevents injury", "Reduces soreness"],
        tips: ["Move gently", "Focus on problem areas", "Stay hydrated"],
        videoUrl: "https://example.com/recovery",
        createdByProfessional: true,
        usageCount: 3200,
        averageRating: 4.7,
        totalRatings: 1800,
        published: true
    }
];

export const getMockGoals = (exerciseCount: number): Goal[] => [
    {id: 'all', name: 'All Goals', emoji: '🎯', count: exerciseCount},
    {id: 'fat-burn', name: 'Fat Burn', emoji: '🔥', count: 2},
    {id: 'muscle-building', name: 'Build Muscle', emoji: '💪', count: 2},
    {id: 'endurance', name: 'Endurance', emoji: '⚡', count: 1},
    {id: 'flexibility', name: 'Flexibility', emoji: '🧘‍♀️', count: 1},
    {id: 'recovery', name: 'Recovery', emoji: '🛡️', count: 1},
    {id: 'balance', name: 'Balance', emoji: '⚖️', count: 1},
    {id: 'sport-performance', name: 'Sport Performance', emoji: '🏆', count: 1},
    {id: 'power', name: 'Explosive Power', emoji: '⚡', count: 1}
];

export const equipmentOptions: string[] = [
    'No Equipment',
    'Dumbbells',
    'Yoga Mat',
    'Jump Rope',
    'Foam Roller',
    'Plyo Box',
    'Tennis Racket'
];

export const difficultyOptions: string[] = [
    'Beginner',
    'Intermediate',
    'Advanced'
];

export const sortOptions = [
    { value: 'relevance', label: 'Relevance' },
    { value: 'rating', label: 'Rating' },
    { value: 'popularity', label: 'Popularity' },
    { value: 'duration', label: 'Duration' },
    { value: 'calories', label: 'Calories' },
    { value: 'newest', label: 'Newest' }
];

export const mockEquipmentOptions = equipmentOptions;
export const mockDifficultyOptions = difficultyOptions;

export const mockExerciseTypeOptions: ExerciseTypeOption[] = [
    { value: 'STRENGTH', display: 'Strength Training', emoji: '💪' },
    { value: 'CARDIO', display: 'Cardiovascular', emoji: '❤️' },
    { value: 'FLEXIBILITY', display: 'Flexibility & Mobility', emoji: '🤸‍♀️' },
    { value: 'BALANCE', display: 'Balance & Stability', emoji: '⚖️' },
    { value: 'PLYOMETRIC', display: 'Plyometric & Power', emoji: '⚡' },
    { value: 'REHABILITATION', display: 'Rehabilitation', emoji: '🛡️' },
    { value: 'SPORTS_SPECIFIC', display: 'Sports Specific', emoji: '🏆' }
];

export const getMockExerciseTypes = (): ExerciseTypeOption[] => {
    return mockExerciseTypeOptions.map(option => ({
        ...option,
        label: option.display, // Add label property for compatibility
        count: mockExercises.filter(ex => ex.exerciseType === option.value).length
    }));
};

// Simulate API calls for future backend integration
export const fetchExercises = async (): Promise<Exercise[]> => {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 800));
    return mockExercises;
};

export const fetchFilterOptions = async () => {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 300));
    return {
        equipmentOptions: mockEquipmentOptions,
        difficultyOptions: mockDifficultyOptions,
        exerciseTypeOptions: mockExerciseTypeOptions
    };
};