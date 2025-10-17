import apiClient from './apiClient';

/**
 * User Preferences API Service
 * Handles user unit preferences (distance, weight)
 */

export interface UserPreferences {
    preferredDistanceUnit: 'km' | 'miles';
    preferredWeightUnit: 'kg' | 'lbs';
}

export const userPreferencesApi = {
    /**
     * Get user's current preferences
     */
    async getPreferences(): Promise<UserPreferences> {
        return apiClient.get<UserPreferences>('/api/users/preferences');
    },

    /**
     * Update all user preferences
     */
    async updatePreferences(preferences: UserPreferences): Promise<UserPreferences> {
        return apiClient.put<UserPreferences>('/api/users/preferences', preferences);
    },

    /**
     * Update only distance unit preference
     */
    async updateDistanceUnit(unit: 'km' | 'miles'): Promise<string> {
        return apiClient.patch<string>(`/api/users/preferences/distance-unit?unit=${unit}`);
    },

    /**
     * Update only weight unit preference
     */
    async updateWeightUnit(unit: 'kg' | 'lbs'): Promise<string> {
        return apiClient.patch<string>(`/api/users/preferences/weight-unit?unit=${unit}`);
    }
};