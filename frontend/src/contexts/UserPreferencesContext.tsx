import React, {createContext, useContext, useState, useEffect, ReactNode} from 'react';
import {userPreferencesApi, UserPreferences} from '../services/userPreferencesApi';
import {useAuth} from './AuthContext';
import {
    convertDistance,
    convertWeight,
    convertPace,
    convertSpeed,
    formatDistanceWithUnit,
    formatWeightWithUnit,
    formatPaceWithUnit,
    formatSpeedWithUnit,
    DistanceUnit,
    WeightUnit
} from '../utils/unitConversions';

interface UserPreferencesContextType {
    // Preferences state
    preferences: UserPreferences;
    loading: boolean;
    error: string | null;

    // Update methods
    setDistanceUnit: (unit: DistanceUnit) => Promise<void>;
    setWeightUnit: (unit: WeightUnit) => Promise<void>;
    updatePreferences: (preferences: Partial<UserPreferences>) => Promise<void>;
    refreshPreferences: () => Promise<void>;

    // Conversion helpers
    convertDistance: (km: number) => number;
    convertWeight: (kg: number) => number;
    convertPace: (minPerKm: number) => number;
    convertSpeed: (kmh: number) => number;

    // Formatting helpers
    formatDistance: (km: number, decimals?: number) => string;
    formatWeight: (kg: number, decimals?: number) => string;
    formatPace: (minPerKm: number) => string;
    formatSpeed: (kmh: number, decimals?: number) => string;

    // Unit accessors
    distanceUnit: DistanceUnit;
    weightUnit: WeightUnit;
}

const UserPreferencesContext = createContext<UserPreferencesContextType | undefined>(undefined);

interface UserPreferencesProviderProps {
    children: ReactNode;
}

export const UserPreferencesProvider: React.FC<UserPreferencesProviderProps> = ({children}) => {
    const {isAuthenticated} = useAuth();

    const [preferences, setPreferences] = useState<UserPreferences>({
        preferredDistanceUnit: 'miles',
        preferredWeightUnit: 'lbs'
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Load preferences from backend only when authenticated
    useEffect(() => {
        if (isAuthenticated) {
            loadPreferences();
        } else {
            // Not authenticated - use defaults
            setLoading(false);
        }
    }, [isAuthenticated]);

    const loadPreferences = async () => {
        try {
            setLoading(true);
            setError(null);
            const prefs = await userPreferencesApi.getPreferences();
            setPreferences(prefs);
            console.log('✅ Loaded user preferences:', prefs);
        } catch (err: any) {
            // If 403 Forbidden, user is not authenticated - use defaults silently
            if (err?.response?.status === 403 || err?.status === 403) {
                console.log('ℹ️ User preferences require authentication - using defaults');
                setError(null); // Don't show error for expected 403
            } else {
                console.error('Failed to load user preferences:', err);
                setError('Failed to load preferences');
            }
            // Keep default values on error
        } finally {
            setLoading(false);
        }
    };

    const setDistanceUnitHandler = async (unit: DistanceUnit) => {
        try {
            await userPreferencesApi.updateDistanceUnit(unit);
            setPreferences(prev => ({...prev, preferredDistanceUnit: unit}));
            console.log('✅ Distance unit updated to:', unit);
        } catch (err) {
            console.error('Failed to update distance unit:', err);
            throw err;
        }
    };

    const setWeightUnitHandler = async (unit: WeightUnit) => {
        try {
            await userPreferencesApi.updateWeightUnit(unit);
            setPreferences(prev => ({...prev, preferredWeightUnit: unit}));
            console.log('✅ Weight unit updated to:', unit);
        } catch (err) {
            console.error('Failed to update weight unit:', err);
            throw err;
        }
    };

    const updatePreferencesHandler = async (updates: Partial<UserPreferences>) => {
        try {
            const updatedPrefs = {...preferences, ...updates};
            const result = await userPreferencesApi.updatePreferences(updatedPrefs as UserPreferences);
            setPreferences(result);
            console.log('✅ Preferences updated:', result);
        } catch (err) {
            console.error('Failed to update preferences:', err);
            throw err;
        }
    };

    // Conversion helpers (use current preferences)
    const convertDistanceHelper = (km: number): number => {
        return convertDistance(km, preferences.preferredDistanceUnit);
    };

    const convertWeightHelper = (kg: number): number => {
        return convertWeight(kg, preferences.preferredWeightUnit);
    };

    const convertPaceHelper = (minPerKm: number): number => {
        return convertPace(minPerKm, preferences.preferredDistanceUnit);
    };

    const convertSpeedHelper = (kmh: number): number => {
        return convertSpeed(kmh, preferences.preferredDistanceUnit);
    };

    // Formatting helpers (use current preferences)
    const formatDistanceHelper = (km: number, decimals: number = 1): string => {
        return formatDistanceWithUnit(km, preferences.preferredDistanceUnit, decimals);
    };

    const formatWeightHelper = (kg: number, decimals: number = 1): string => {
        return formatWeightWithUnit(kg, preferences.preferredWeightUnit, decimals);
    };

    const formatPaceHelper = (minPerKm: number): string => {
        return formatPaceWithUnit(minPerKm, preferences.preferredDistanceUnit);
    };

    const formatSpeedHelper = (kmh: number, decimals: number = 1): string => {
        return formatSpeedWithUnit(kmh, preferences.preferredDistanceUnit, decimals);
    };

    const value: UserPreferencesContextType = {
        // State
        preferences,
        loading,
        error,

        // Update methods
        setDistanceUnit: setDistanceUnitHandler,
        setWeightUnit: setWeightUnitHandler,
        updatePreferences: updatePreferencesHandler,
        refreshPreferences: loadPreferences,

        // Conversion helpers
        convertDistance: convertDistanceHelper,
        convertWeight: convertWeightHelper,
        convertPace: convertPaceHelper,
        convertSpeed: convertSpeedHelper,

        // Formatting helpers
        formatDistance: formatDistanceHelper,
        formatWeight: formatWeightHelper,
        formatPace: formatPaceHelper,
        formatSpeed: formatSpeedHelper,

        // Unit accessors
        distanceUnit: preferences.preferredDistanceUnit,
        weightUnit: preferences.preferredWeightUnit
    };

    return (
        <UserPreferencesContext.Provider value={value}>
            {children}
        </UserPreferencesContext.Provider>
    );
};

/**
 * Hook to use user preferences context
 */
export const useUserPreferences = (): UserPreferencesContextType => {
    const context = useContext(UserPreferencesContext);
    if (!context) {
        throw new Error('useUserPreferences must be used within UserPreferencesProvider');
    }
    return context;
};