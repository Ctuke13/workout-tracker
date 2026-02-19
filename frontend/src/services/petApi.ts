import {PetStats, MealType} from '../types/pet';
import authService from './authService';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export interface WeeklyStatsResponse {
    workoutsThisWeek: number;
    xpThisWeek: number;
    currentStreak: number;
    weeklyGoal: number | null;
    goalType: string | null;
    goalProgress: number | null;
    weekStartDate: string;
    weekEndDate: string;
    workoutsRemaining: number | null;
    goalAchieved: boolean | null;
    message: string | null;
}

export interface UserGoalResponse {
    weeklyWorkoutGoal: number | null;
    goalType: string;
    hasGoalSet: boolean;
    goalLevel: string | null;
}

export interface UserGoalRequest {
    weeklyWorkoutGoal: number | null;
    goalType?: string;
}

// ==================== NEW TYPES FOR XP SYSTEM ====================

export interface EvolutionRequirements {
    currentStage: string;
    currentStageDisplay: string;
    currentLevel: number;
    nextStage: string | null;
    nextStageDisplay: string | null;
    levelRequired: number | null;
    levelsRemaining: number | null;
    canEvolve: boolean;
    message: string;
}

export interface EvolutionResponse {
    success: boolean;
    oldStage: string;
    newStage: string;
    newStageDisplay: string;
    currentLevel: number;
    message: string;
}

export interface PetNameRequest {
    petName: string;
}

// ==================== PET API SERVICE ====================

class PetApiService {
    private baseURL = `${API_BASE_URL}/api/pet`;

    // ==================== GET PET STATS ====================

    async getStats(): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Getting pet stats`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/stats`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            console.log(`📡 [${requestId}] Get pet stats response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to get pet stats: ${response.status}`);
            }

            const data = await response.json();
            const petStats: PetStats = data.petStats || data;
            console.log(`✅ [${requestId}] Got pet stats - Level: ${petStats.level}, XP: ${petStats.xp}`);
            return petStats;
        } catch (error) {
            console.error(`💥 [${requestId}] Get pet stats error:`, error);
            throw error instanceof Error ? error : new Error('Failed to get pet stats');
        }
    }

    // ==================== 🆕 GET PET PROFILE ====================

    async getProfile(): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Getting pet profile`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/profile`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            console.log(`📡 [${requestId}] Get pet profile response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to get pet profile: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ [${requestId}] Got pet profile - Level ${data.level}, ${data.xp}/${data.xpToNextLevel} XP`);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Get pet profile error:`, error);
            throw error instanceof Error ? error : new Error('Failed to get pet profile');
        }
    }

    // ==================== 🆕 GET EVOLUTION REQUIREMENTS ====================

    async getEvolutionRequirements(): Promise<EvolutionRequirements> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Getting evolution requirements`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/evolution/requirements`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            console.log(`📡 [${requestId}] Get evolution requirements response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to get evolution requirements: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ [${requestId}] Evolution requirements - ${data.message}`);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Get evolution requirements error:`, error);
            throw error instanceof Error ? error : new Error('Failed to get evolution requirements');
        }
    }

    // ==================== 🆕 EVOLVE PET ====================

    async evolvePet(): Promise<EvolutionResponse> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Attempting to evolve pet`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/evolve`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log(`📡 [${requestId}] Evolve pet response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to evolve pet: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ [${requestId}] Evolution response - ${data.message}`);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Evolve pet error:`, error);
            throw error instanceof Error ? error : new Error('Failed to evolve pet');
        }
    }

    // ==================== 🆕 UPDATE PET NAME ====================

    async updatePetName(petName: string): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🏷️ [${requestId}] Updating pet name to: ${petName}`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/name`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({petName}),
            });

            console.log(`📡 [${requestId}] Update pet name response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || errorData.message || `Failed to update pet name: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ [${requestId}] Pet renamed to: ${data.petName}`);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Update pet name error:`, error);
            throw error instanceof Error ? error : new Error('Failed to update pet name');
        }
    }

    // ==================== FEED PET ====================

    async feed(mealType: MealType): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Feeding pet with:`, mealType);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/feed/${mealType.toLowerCase()}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log(`📡 [${requestId}] Feed pet response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to feed pet: ${response.status}`);
            }

            const data = await response.json();
            const petStats: PetStats = data.petStats || data;
            console.log(`✅ [${requestId}] Fed pet - New fuel: ${petStats.fuel}, Crystals: ${petStats.crystals}`);
            return petStats;
        } catch (error) {
            console.error(`💥 [${requestId}] Feed pet error:`, error);
            throw error instanceof Error ? error : new Error('Failed to feed pet');
        }
    }

    // ==================== MOTIVATE PET ====================

    async motivate(): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Motivating pet`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/motivate`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log(`📡 [${requestId}] Motivate pet response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to motivate pet: ${response.status}`);
            }

            const data = await response.json();
            const petStats: PetStats = data.petStats || data;
            console.log(`✅ [${requestId}] Motivated pet - New motivation: ${petStats.motivation}`);
            return petStats;
        } catch (error) {
            console.error(`💥 [${requestId}] Motivate pet error:`, error);
            throw error instanceof Error ? error : new Error('Failed to motivate pet');
        }
    }

    // ==================== BATHE PET ====================

    async bathe(): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Bathing pet`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/bathe`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log(`📡 [${requestId}] Bathe pet response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to bathe pet: ${response.status}`);
            }

            const data = await response.json();
            const petStats: PetStats = data.petStats || data;
            console.log(`✅ [${requestId}] Bathed pet - New cleanliness: ${petStats.cleanliness}`);
            return petStats;
        } catch (error) {
            console.error(`💥 [${requestId}] Bathe pet error:`, error);
            throw error instanceof Error ? error : new Error('Failed to bathe pet');
        }
    }

    // ==================== SLEEP PET ====================

    async sleep(): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Putting pet to sleep`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/sleep`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log(`📡 [${requestId}] Sleep pet response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to put pet to sleep: ${response.status}`);
            }

            const data = await response.json();
            const petStats: PetStats = data.petStats || data;
            console.log(`✅ [${requestId}] Pet sleeping - New fatigue: ${petStats.fatigue}`);
            return petStats;
        } catch (error) {
            console.error(`💥 [${requestId}] Sleep pet error:`, error);
            throw error instanceof Error ? error : new Error('Failed to put pet to sleep');
        }
    }

    // ==================== WAKE PET ====================

    async wake(): Promise<PetStats> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Waking pet`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/wake`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            console.log(`📡 [${requestId}] Wake pet response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to wake pet: ${response.status}`);
            }

            const data = await response.json();
            const petStats: PetStats = data.petStats || data;
            console.log(`✅ [${requestId}] Woke pet - New fatigue: ${petStats.fatigue}`);
            return petStats;
        } catch (error) {
            console.error(`💥 [${requestId}] Wake pet error:`, error);
            throw error instanceof Error ? error : new Error('Failed to wake pet');
        }
    }

    // ==================== CHECK MOTIVATE COOLDOWN ====================

    async getMotiveCooldown(): Promise<{ onCooldown: boolean; cooldownEndsAt: string | null }> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🐺 [${requestId}] Checking motivate cooldown`);

        try {
            const stats = await this.getStats();

            if (stats.lastMotivatedAt) {
                const lastMotivated = new Date(stats.lastMotivatedAt).getTime();
                const cooldownMs = 30 * 60 * 1000; // 30 minutes
                const cooldownEndsAt = new Date(lastMotivated + cooldownMs);

                if (cooldownEndsAt.getTime() > Date.now()) {
                    return {
                        onCooldown: true,
                        cooldownEndsAt: cooldownEndsAt.toISOString(),
                    };
                }
            }

            return {onCooldown: false, cooldownEndsAt: null};
        } catch (error) {
            console.error(`💥 [${requestId}] Check cooldown error:`, error);
            return {onCooldown: false, cooldownEndsAt: null};
        }
    }

    // ==================== 🆕 GET WEEKLY STATS ====================

    async getWeeklyStats(): Promise<WeeklyStatsResponse> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`📊 [${requestId}] Getting weekly stats`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/api/user/stats/weekly`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            console.log(`📡 [${requestId}] Get weekly stats response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to get weekly stats: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ [${requestId}] Weekly stats - ${data.workoutsThisWeek} workouts, ${data.currentStreak} day streak`);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Get weekly stats error:`, error);
            throw error instanceof Error ? error : new Error('Failed to get weekly stats');
        }
    }

    // ==================== 🆕 GET USER GOAL ====================

    async getUserGoal(): Promise<UserGoalResponse> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🎯 [${requestId}] Getting user goal`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/api/user/goal`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            console.log(`📡 [${requestId}] Get user goal response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to get user goal: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ [${requestId}] User goal - ${data.hasGoalSet ? `${data.weeklyWorkoutGoal} workouts (${data.goalLevel})` : 'No goal set'}`);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Get user goal error:`, error);
            throw error instanceof Error ? error : new Error('Failed to get user goal');
        }
    }

    // ==================== 🆕 UPDATE USER GOAL ====================

    async updateUserGoal(request: UserGoalRequest): Promise<UserGoalResponse> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🎯 [${requestId}] Updating user goal to: ${request.weeklyWorkoutGoal}`);

        try {
            const token = authService.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${API_BASE_URL}/api/user/goal`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(request),
            });

            console.log(`📡 [${requestId}] Update user goal response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to update user goal: ${response.status}`);
            }

            const data = await response.json();
            console.log(`✅ [${requestId}] Goal updated - ${data.hasGoalSet ? `${data.weeklyWorkoutGoal} workouts` : 'Goal removed'}`);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Update user goal error:`, error);
            throw error instanceof Error ? error : new Error('Failed to update user goal');
        }
    }


    // ==================== HELPER: CAN AFFORD MEAL ====================

    canAffordMeal(crystals: number, mealType: MealType): boolean {
        const costs: Record<MealType, number> = {
            SNACK: 1,
            MEAL: 3,
            FEAST: 6,
        };
        return crystals >= costs[mealType];
    }

    // ==================== HELPER: CAN BATHE ====================

    canBathe(motivation: number): boolean {
        return motivation >= 40;
    }
}

export const petApi = new PetApiService();
export default petApi;