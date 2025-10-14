// services/progressApi.ts
import apiClient from './apiClient';
import {Achievement, UnlockedAchievement, UserProgression} from '../types/gamification';
import {WorkoutCompletionResponse} from "@/services/workoutCompletionResponse";

export interface WorkoutCompletionRequest {
    durationMinutes: number;
    setsCompleted: number;
    volumeLifted: number;
    distanceKm?: number;
    holdSeconds?: number;
    uniqueExercisesCount: number;
    workoutType: 'STRENGTH' | 'CARDIO' | 'ISOMETRIC';
}

class ProgressApi {

    /**
     * Submit workout completion to progression system
     * Awards XP, checks achievements, updates streaks
     */
    async completeWorkout(data: WorkoutCompletionRequest): Promise<WorkoutCompletionResponse> {
        return apiClient.post<WorkoutCompletionResponse>('/api/progress/workout-completion', data);
    }

    /**
     * Get current user's progression data
     */
    async getUserProgression(): Promise<UserProgression> {
        return apiClient.get<UserProgression>('/api/progress/me');
    }

    /**
     * Get all available achievements
     */
    async getAllAchievements(): Promise<Achievement[]> {
        return apiClient.get<Achievement[]>('/api/progress/achievements');
    }

    /**
     * Get user's unlocked achievements
     */
    async getUnlockedAchievements(): Promise<UnlockedAchievement[]> {
        return apiClient.get<UnlockedAchievement[]>('/api/progress/achievements/unlocked');
    }

    /**
     * Get current season info
     */
    async getCurrentSeason(): Promise<any> {
        return apiClient.get<any>('/api/progress/seasons/current');
    }

    /**
     * Get seasonal leaderboard
     */
    async getSeasonalLeaderboard(limit?: number): Promise<any[]> {
        return apiClient.get<any[]>('/api/progress/leaderboard/seasonal', {limit});
    }

    /**
     * Get lifetime leaderboard
     */
    async getLifetimeLeaderboard(limit?: number): Promise<any[]> {
        return apiClient.get<any[]>('/api/progress/leaderboard/lifetime', {limit});
    }
}

export const progressApi = new ProgressApi();