import { Exercise, Goal, ExerciseTypeOption } from '../types/exercise';

export const mockExercises: Exercise[] = [
    {
        id: 1,
        name: "Push-ups",
        exerciseName: "Push-ups", // Backward compatibility
        emoji: "💪",
        description: "Classic upper body strength exercise that builds chest, shoulder, and tricep strength",
        exerciseType: "STRENGTH",
        isCardio: false,  // Strength exercise = sets/reps tracking interface
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
        published: true,
        // NEW REQUIRED FIELDS:
        isPopular: true,          // High usage count makes it popular
        isHighlyRated: true,      // 4.5+ rating makes it highly rated
        canDoAtHome: true,        // Bodyweight exercise can be done at home
        requiresEquipment: false, // No equipment needed
        createdBy: "Fitness Professional"
    },
    {
        id: 2,
        name: "Yoga Flow Sequence",
        exerciseName: "Yoga Flow Sequence",
        emoji: "🧘‍♀️",
        description: "Dynamic yoga flow to improve flexibility and mind-body connection",
        exerciseType: "FLEXIBILITY",
        isCardio: false,  // Flexibility exercise = sets/reps or duration tracking
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
        published: true,
        // NEW REQUIRED FIELDS:
        isPopular: true,          // High rating and good usage
        isHighlyRated: true,      // 4.6 rating is excellent
        canDoAtHome: true,        // Just needs a mat
        requiresEquipment: true,  // Requires yoga mat
        createdBy: "Certified Yoga Instructor"
    },
    {
        id: 3,
        name: "Single-Leg Balance Challenge",
        exerciseName: "Single-Leg Balance Challenge",
        emoji: "⚖️",
        description: "Progressive balance training to improve stability and coordination",
        exerciseType: "BALANCE",
        isCardio: false,  // Balance exercise = duration or reps tracking
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
        published: true,
        // NEW REQUIRED FIELDS:
        isPopular: false,         // Moderate usage count
        isHighlyRated: true,      // 4.3+ is good rating
        canDoAtHome: true,        // No equipment needed
        requiresEquipment: false, // Can use wall for support
        createdBy: "Physical Therapist"
    },
    {
        id: 4,
        name: "Tennis Serve Practice",
        exerciseName: "Tennis Serve Practice",
        emoji: "🎾",
        description: "Sport-specific drills to improve tennis serve technique and power",
        exerciseType: "SPORTS_SPECIFIC",
        isCardio: false,  // Sports-specific = reps/sets tracking
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
        published: true,
        // NEW REQUIRED FIELDS:
        isPopular: false,         // Lower usage (sport-specific)
        isHighlyRated: true,      // 4.4 is a good rating
        canDoAtHome: false,       // Needs court/space and equipment
        requiresEquipment: true,  // Requires racket and balls
        createdBy: "Tennis Pro"
    },
    {
        id: 5,
        name: "HIIT Sprint Intervals",
        exerciseName: "HIIT Sprint Intervals",
        emoji: "🏃‍♂️",
        description: "High-intensity cardiovascular training with sprint intervals",
        exerciseType: "CARDIO",
        isCardio: true,   // CRITICAL: Cardio exercise = time/distance/pace tracking interface
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
        published: true,
        // NEW REQUIRED FIELDS:
        isPopular: true,          // High usage for cardio
        isHighlyRated: true,      // 4.2+ is solid for high-intensity
        canDoAtHome: true,        // Can run in place or use small space
        requiresEquipment: false, // Bodyweight cardio
        createdBy: "HIIT Specialist"
    },
    {
        id: 6,
        name: "Box Jump Explosives",
        exerciseName: "Box Jump Explosives",
        emoji: "📦",
        description: "Plyometric training for explosive lower body power development",
        exerciseType: "PLYOMETRIC",
        isCardio: false,  // Plyometric = reps/sets tracking (explosive movements)
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
        published: true,
        // NEW REQUIRED FIELDS:
        isPopular: false,         // Requires equipment, so less popular
        isHighlyRated: true,      // 4.1+ is good
        canDoAtHome: false,       // Requires plyo box (safety concern)
        requiresEquipment: true,  // Needs plyo box
        createdBy: "Athletic Performance Coach"
    },
    {
        id: 7,
        name: "Post-Workout Recovery",
        exerciseName: "Post-Workout Recovery",
        emoji: "🛡️",
        description: "Gentle rehabilitation movements for muscle recovery and injury prevention",
        exerciseType: "REHABILITATION",
        isCardio: false,  // Rehabilitation = duration or gentle reps tracking
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
        published: true,
        // NEW REQUIRED FIELDS:
        isPopular: true,          // Highest usage count (everyone needs recovery)
        isHighlyRated: true,      // 4.7 is excellent rating
        canDoAtHome: true,        // Can be done at home with basic equipment
        requiresEquipment: true,  // Needs foam roller for best results
        createdBy: "Licensed Physical Therapist"
    }
];

// Helper function to create mock exercises with automatic isCardio determination
// This function demonstrates the logic that your backend will use
export const createMockExercise = (exerciseData: Omit<Exercise, 'isCardio'>): Exercise => {
    return {
        ...exerciseData,
        // Automatically set isCardio based on exerciseType - matches backend logic
        isCardio: exerciseData.exerciseType === 'CARDIO'
    };
};

// Update goals to include cardio-specific filtering capability
export const getMockGoals = (exerciseCount: number): Goal[] => [
    {id: 'all', name: 'All Goals', emoji: '🎯', count: exerciseCount},
    {id: 'fat-burn', name: 'Fat Burn', emoji: '🔥', count: 2}, // Primarily cardio exercises
    {id: 'muscle-building', name: 'Build Muscle', emoji: '💪', count: 2}, // Primarily strength exercises
    {id: 'endurance', name: 'Endurance', emoji: '⚡', count: 1}, // Mix of cardio and endurance
    {id: 'flexibility', name: 'Flexibility', emoji: '🧘‍♀️', count: 1}, // Non-cardio flexibility
    {id: 'recovery', name: 'Recovery', emoji: '🛡️', count: 1}, // Non-cardio recovery
    {id: 'balance', name: 'Balance', emoji: '⚖️', count: 1}, // Non-cardio balance
    {id: 'sport-performance', name: 'Sport Performance', emoji: '🏆', count: 1}, // Usually non-cardio
    {id: 'power', name: 'Explosive Power', emoji: '⚡', count: 1} // Non-cardio explosive movements
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
    { value: 'CARDIO', display: 'Cardiovascular', emoji: '❤️' },  // This is the cardio type
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

// NEW: Helper functions for cardio vs non-cardio filtering
export const getCardioExercises = (): Exercise[] => {
    return mockExercises.filter(exercise => exercise.isCardio);
};

export const getStrengthExercises = (): Exercise[] => {
    return mockExercises.filter(exercise => !exercise.isCardio);
};

export const getExercisesByTrackingType = (trackingType: 'cardio' | 'strength'): Exercise[] => {
    if (trackingType === 'cardio') {
        return getCardioExercises();
    }
    return getStrengthExercises();
};

// NEW: Enhanced filtering functions for the new fields
export const getPopularExercises = (): Exercise[] => {
    return mockExercises.filter(exercise => exercise.isPopular);
};

export const getHighlyRatedExercises = (): Exercise[] => {
    return mockExercises.filter(exercise => exercise.isHighlyRated);
};

export const getHomeExercises = (): Exercise[] => {
    return mockExercises.filter(exercise => exercise.canDoAtHome);
};

export const getNoEquipmentExercises = (): Exercise[] => {
    return mockExercises.filter(exercise => !exercise.requiresEquipment);
};

// Enhanced filtering function that supports all the new fields
export const getFilteredExercises = (filters: {
    trackingType?: 'cardio' | 'strength';
    isPopular?: boolean;
    isHighlyRated?: boolean;
    canDoAtHome?: boolean;
    requiresEquipment?: boolean;
    difficultyLevel?: string;
    exerciseType?: string;
}): Exercise[] => {
    return mockExercises.filter(exercise => {
        if (filters.trackingType && exercise.isCardio !== (filters.trackingType === 'cardio')) {
            return false;
        }
        if (filters.isPopular !== undefined && exercise.isPopular !== filters.isPopular) {
            return false;
        }
        if (filters.isHighlyRated !== undefined && exercise.isHighlyRated !== filters.isHighlyRated) {
            return false;
        }
        if (filters.canDoAtHome !== undefined && exercise.canDoAtHome !== filters.canDoAtHome) {
            return false;
        }
        if (filters.requiresEquipment !== undefined && exercise.requiresEquipment !== filters.requiresEquipment) {
            return false;
        }
        if (filters.difficultyLevel && exercise.difficultyLevel !== filters.difficultyLevel.toUpperCase()) {
            return false;
        }
        if (filters.exerciseType && exercise.exerciseType !== filters.exerciseType.toUpperCase()) {
            return false;
        }
        return true;
    });
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

// NEW: Fetch exercises filtered by tracking type (useful for workout planning)
export const fetchExercisesByTrackingType = async (trackingType: 'cardio' | 'strength'): Promise<Exercise[]> => {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 600));
    return getExercisesByTrackingType(trackingType);
};

// NEW: Fetch exercises with advanced filtering (simulates backend API capabilities)
export const fetchFilteredExercises = async (filters: Parameters<typeof getFilteredExercises>[0]): Promise<Exercise[]> => {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 500));
    return getFilteredExercises(filters);
};