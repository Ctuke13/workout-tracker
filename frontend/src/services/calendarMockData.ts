// src/services/calendarMockData.ts - Fixed date handling
import { mockExercises } from './mockData';
import { DateUtils } from '../utils/dateUtils';

// Calendar-specific types
export interface ScheduledExercise {
    id: string;
    exerciseId: number;
    exercise: any; // Will use Exercise from mockData
    scheduledDate: string; // ISO date string
    sets: number;
    reps: string;
    weight?: number;
    restSeconds?: number;
    tempo?: string;
    targetRpe?: number;
    notes?: string;
    completed: boolean;
    createdAt: string;
    userId: string;
}

export interface CalendarDay {
    date: Date;
    dateString: string;
    exercises: ScheduledExercise[];
    isToday: boolean;
    isPast: boolean;
    isFuture: boolean;
}

// Mock scheduled exercises storage (simulates backend)
let mockScheduledExercises: ScheduledExercise[] = [
    {
        id: 'sched_1',
        exerciseId: 1, // Push-ups
        exercise: mockExercises[0],
        scheduledDate: DateUtils.getTodayString(), // Fixed: Use local timezone
        sets: 3,
        reps: '12',
        weight: undefined,
        restSeconds: 90,
        targetRpe: 7,
        notes: 'Focus on form',
        completed: false,
        createdAt: new Date().toISOString(),
        userId: 'current_user'
    },
    {
        id: 'sched_2',
        exerciseId: 2, // Yoga Flow
        exercise: mockExercises[1],
        scheduledDate: DateUtils.getTodayString(), // Fixed: Use local timezone
        sets: 1,
        reps: '25 minutes',
        weight: undefined,
        restSeconds: 0,
        targetRpe: 5,
        notes: 'Morning routine',
        completed: true,
        createdAt: new Date().toISOString(),
        userId: 'current_user'
    }
];

// Mock API functions
export const calendarMockApi = {
    // Get exercises for calendar view (week/month)
    getCalendarExercises: async (startDate: string, endDate: string): Promise<ScheduledExercise[]> => {
        console.log(`📅 Mock API: Getting exercises from ${startDate} to ${endDate}`);

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 300));

        return mockScheduledExercises.filter(exercise =>
            exercise.scheduledDate >= startDate &&
            exercise.scheduledDate <= endDate &&
            exercise.userId === 'current_user'
        );
    },

    // Add exercise to calendar
    scheduleExercise: async (exerciseData: {
        exerciseId: number;
        scheduledDate: string;
        sets: number;
        reps: string;
        weight?: number;
        restSeconds?: number;
        tempo?: string;
        targetRpe?: number;
        notes?: string;
    }): Promise<ScheduledExercise> => {
        console.log('📅 Mock API: Scheduling exercise:', exerciseData);

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 500));

        // Find the exercise details
        const exercise = mockExercises.find(ex => ex.id === exerciseData.exerciseId);
        if (!exercise) {
            throw new Error('Exercise not found');
        }

        // Create new scheduled exercise
        const newScheduledExercise: ScheduledExercise = {
            id: `sched_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
            exerciseId: exerciseData.exerciseId,
            exercise: exercise,
            scheduledDate: exerciseData.scheduledDate, // Keep the date as provided
            sets: exerciseData.sets,
            reps: exerciseData.reps,
            weight: exerciseData.weight,
            restSeconds: exerciseData.restSeconds,
            tempo: exerciseData.tempo,
            targetRpe: exerciseData.targetRpe,
            notes: exerciseData.notes,
            completed: false,
            createdAt: new Date().toISOString(),
            userId: 'current_user'
        };

        // Add to mock storage
        mockScheduledExercises.push(newScheduledExercise);

        return newScheduledExercise;
    },

    // Remove exercise from calendar
    removeScheduledExercise: async (exerciseId: string): Promise<void> => {
        console.log('📅 Mock API: Removing exercise:', exerciseId);

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 200));

        mockScheduledExercises = mockScheduledExercises.filter(ex => ex.id !== exerciseId);
    },

    // Update exercise details
    updateScheduledExercise: async (exerciseId: string, updates: Partial<ScheduledExercise>): Promise<ScheduledExercise> => {
        console.log('📅 Mock API: Updating exercise:', exerciseId, updates);

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 300));

        const index = mockScheduledExercises.findIndex(ex => ex.id === exerciseId);
        if (index === -1) {
            throw new Error('Scheduled exercise not found');
        }

        mockScheduledExercises[index] = { ...mockScheduledExercises[index], ...updates };
        return mockScheduledExercises[index];
    },

    // Mark exercise as completed (NEW - for workout completion)
    markExerciseCompleted: async (exerciseId: string): Promise<ScheduledExercise> => {
        console.log('✅ Mock API: Marking exercise as completed:', exerciseId);

        const index = mockScheduledExercises.findIndex(ex => ex.id === exerciseId);
        if (index === -1) {
            throw new Error('Scheduled exercise not found');
        }

        mockScheduledExercises[index] = {
            ...mockScheduledExercises[index],
            completed: true
        };

        return mockScheduledExercises[index];
    },

    // Get exercises for a specific date (NEW - for workout history)
    getExercisesForDate: async (dateString: string): Promise<ScheduledExercise[]> => {
        console.log('📅 Mock API: Getting exercises for date:', dateString);

        await new Promise(resolve => setTimeout(resolve, 200));

        return mockScheduledExercises.filter(exercise =>
            exercise.scheduledDate === dateString &&
            exercise.userId === 'current_user'
        );
    },

    // Get available exercises (for search/selection)
    searchExercises: async (query: string = '', filters: any = {}): Promise<any[]> => {
        console.log('🔍 Mock API: Searching exercises:', query, filters);

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 400));

        let results = [...mockExercises];

        // Apply search filter
        if (query.trim()) {
            results = results.filter(exercise =>
                exercise.name.toLowerCase().includes(query.toLowerCase()) ||
                exercise.description.toLowerCase().includes(query.toLowerCase()) ||
                exercise.exerciseType.toLowerCase().includes(query.toLowerCase())
            );
        }

        // Apply additional filters (exerciseType, difficulty, etc.)
        if (filters.exerciseType && filters.exerciseType !== 'all') {
            results = results.filter(ex => ex.exerciseType === filters.exerciseType);
        }

        if (filters.difficultyLevel && filters.difficultyLevel !== 'all') {
            results = results.filter(ex => ex.difficultyLevel === filters.difficultyLevel);
        }

        return results;
    },

    // Get exercise categories/goals
    getExerciseGoals: async (): Promise<any[]> => {
        console.log('🎯 Mock API: Getting exercise goals');

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 200));

        return [
            { goal: 'fat-burn', count: 15 },
            { goal: 'muscle-building', count: 25 },
            { goal: 'endurance', count: 18 },
            { goal: 'flexibility', count: 12 },
            { goal: 'sport-specific', count: 8 },
            { goal: 'recovery', count: 10 }
        ];
    },

    // Get popular exercises
    getPopularExercises: async (limit: number = 10): Promise<any[]> => {
        console.log('⭐ Mock API: Getting popular exercises');

        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 250));

        return mockExercises
            .sort((a, b) => (b.usageCount || 0) - (a.usageCount || 0))
            .slice(0, limit);
    }
};

// Helper function to generate calendar days with mock data - FIXED DATE HANDLING
export const generateCalendarDays = async (startDate: Date, daysCount: number = 7): Promise<CalendarDay[]> => {
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + daysCount - 1);

    const startDateStr = DateUtils.getDateString(startDate); // Fixed: Use DateUtils
    const endDateStr = DateUtils.getDateString(endDate);     // Fixed: Use DateUtils

    // Get scheduled exercises for date range
    const scheduledExercises = await calendarMockApi.getCalendarExercises(startDateStr, endDateStr);

    // Generate calendar days
    const days: CalendarDay[] = [];
    const todayString = DateUtils.getTodayString(); // Fixed: Use DateUtils

    for (let i = 0; i < daysCount; i++) {
        const date = new Date(startDate);
        date.setDate(startDate.getDate() + i);

        const dateString = DateUtils.getDateString(date); // Fixed: Use DateUtils
        const dayExercises = scheduledExercises.filter(ex => ex.scheduledDate === dateString);

        days.push({
            date,
            dateString,
            exercises: dayExercises,
            isToday: DateUtils.isToday(dateString),    // Fixed: Use DateUtils
            isPast: DateUtils.isPast(dateString),      // Fixed: Use DateUtils
            isFuture: DateUtils.isFuture(dateString)   // Fixed: Use DateUtils
        });
    }

    return days;
};

// Export for easy backend migration later
export const BACKEND_ENDPOINTS = {
    SCHEDULE_EXERCISE: '/api/calendar/schedule',
    GET_CALENDAR: '/api/calendar',
    SEARCH_EXERCISES: '/api/exercises/public/search',
    GET_EXERCISES: '/api/exercises/public',
    GET_GOALS: '/api/exercises/goals',
    GET_POPULAR: '/api/exercises/popular'
};

export default calendarMockApi;