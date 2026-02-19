// ==================== USER API SERVICE ====================
// Create this file: frontend/src/services/userApi.ts

import api from './apiClient';

export interface UpdateNicknameRequest {
    nickname: string;
}

export interface UpdatePetNameRequest {
    petName: string;
}

export interface UpdatePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

export interface UserDataExport {
    userId: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    nickname: string | null;
    petName: string | null;
    createdAt: string;
    subscriptionTier: string;
    totalWorkouts: number;
    currentStreak: number;
    longestStreak: number;
    exportedAt: string;
    dataFormat: string;
}

class UserApiService {
    /**
     * Update user nickname
     */
    async updateNickname(nickname: string): Promise<void> {
        await api.put('/api/users/nickname', {nickname});
    }

    /**
     * Update pet name
     */
    async updatePetName(petName: string): Promise<void> {
        await api.put('/api/users/pet-name', {petName});
    }

    /**
     * Change user password
     */
    async changePassword(currentPassword: string, newPassword: string): Promise<void> {
        try {
            await api.put('/api/users/password', {
                currentPassword,
                newPassword
            });
        } catch (error: any) {
            if (error.response?.data?.message) {
                throw new Error(error.response.data.message);
            }
            throw new Error('Failed to change password. Please try again.');
        }
    }

    /**
     * Complete pet tutorial
     */
    async completePetTutorial(): Promise<void> {
        await api.put('/api/users/tutorial/pet/complete');
    }

    /**
     * Restart pet tutorial
     */
    async restartPetTutorial(): Promise<void> {
        await api.put('/api/users/tutorial/pet/restart');
    }

    /**
     * Complete calendar tutorial
     */
    async completeCalendarTutorial(): Promise<void> {
        await api.put('/api/users/tutorial/calendar/complete');
    }

    /**
     * Restart calendar tutorial
     */
    async restartCalendarTutorial(): Promise<void> {
        await api.put('/api/users/tutorial/calendar/restart');
    }

    /**
     * Update user preferences
     * Uses existing /api/users/preferences endpoint
     */
    async updatePreferences(preferences: {
        preferredDistanceUnit?: string;
        preferredWeightUnit?: string
    }): Promise<void> {
        await api.put('/api/users/preferences', preferences);
    }

    /**
     * Get user preferences
     */
    async getPreferences(): Promise<{ preferredDistanceUnit: string; preferredWeightUnit: string }> {
        const response = await api.get<{
            preferredDistanceUnit: string;
            preferredWeightUnit: string
        }>('/api/users/preferences');
        return response;
    }

    /**
     * Export user data
     */
    async exportData(): Promise<UserDataExport> {
        const response = await api.get<UserDataExport>('/api/users/export');
        return response;
    }

    /**
     * Delete user account
     */
    async deleteAccount(): Promise<void> {
        await api.delete('/api/users/profile');
    }


}

export const userApi = new UserApiService();
export default userApi;