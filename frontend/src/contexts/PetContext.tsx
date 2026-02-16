import React, {createContext, useContext, useState, useEffect, useCallback, ReactNode} from 'react';
import {PetStats, MealType} from '../types/pet';
import petApi from '../services/petApi';
import {useAuth} from './AuthContext';

// ==================== ANIMATION TYPES ====================

type PetAnimation = 'feed' | 'bathe' | 'sleep' | 'wake' | 'play' | 'celebrate' | null;

// ==================== CONTEXT TYPES ====================

interface PetContextType {
    // State
    stats: PetStats | null;
    loading: boolean;
    error: string | null;
    actionLoading: boolean;
    lastAction: string | null;
    currentAnimation: PetAnimation;

    // Actions
    refreshStats: () => Promise<void>;
    feedPet: (mealType: MealType) => Promise<PetStats>;
    motivatePet: () => Promise<PetStats>;
    bathePet: () => Promise<PetStats>;
    sleepPet: () => Promise<PetStats>;
    wakePet: () => Promise<PetStats>;
    clearError: () => void;
    clearLastAction: () => void;
}

// ==================== CONTEXT ====================

const PetContext = createContext<PetContextType | undefined>(undefined);

// ==================== PROVIDER ====================

interface PetProviderProps {
    children: ReactNode;
}

export function PetProvider({children}: PetProviderProps) {
    const {isAuthenticated, user} = useAuth();

    const [stats, setStats] = useState<PetStats | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [actionLoading, setActionLoading] = useState(false);
    const [lastAction, setLastAction] = useState<string | null>(null);
    const [currentAnimation, setCurrentAnimation] = useState<PetAnimation>(null);

    // ==================== FETCH STATS ====================

    const refreshStats = useCallback(async () => {
        if (!isAuthenticated || !user?.onboardingCompleted) {
            setLoading(false);
            return;
        }

        try {
            setError(null);
            const petStats = await petApi.getStats();
            setStats(petStats);
            console.log('🐺 Pet stats refreshed:', petStats);
        } catch (err) {
            console.error('Failed to fetch pet stats:', err);
            setError(err instanceof Error ? err.message : 'Failed to load pet stats');
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated, user?.onboardingCompleted]);

    // Load stats on mount and when auth changes
    useEffect(() => {
        if (isAuthenticated && user?.onboardingCompleted) {
            refreshStats();
        }
    }, [isAuthenticated, user?.onboardingCompleted, refreshStats]);

    // ==================== ACTIONS ====================

    const feedPet = async (mealType: MealType): Promise<PetStats> => {
        setActionLoading(true);
        setError(null);

        try {
            // Trigger animation FIRST
            setCurrentAnimation('feed');

            const response = await petApi.feed(mealType);
            const prevFuel = stats?.fuel ?? 0;

            // Update stats
            setStats(response);

            const fuelGained = (response.fuel ?? 0) - prevFuel;
            setLastAction(`Fed ${mealType.toLowerCase()}! +${fuelGained > 0 ? fuelGained : 'some'} fuel`);
            console.log('🍖 Pet fed:', response);

            // Clear animation after Drink Milk duration (~4s)
            setTimeout(() => setCurrentAnimation(null), 3000);

            return response;
        } catch (err) {
            setCurrentAnimation(null);
            const message = err instanceof Error ? err.message : 'Failed to feed pet';
            setError(message);
            throw err;
        } finally {
            setActionLoading(false);
        }
    };

    const motivatePet = async (): Promise<PetStats> => {
        setActionLoading(true);
        setError(null);

        try {
            // Trigger play animation for motivation (ball bouncing interaction)
            setCurrentAnimation('play');

            const response = await petApi.motivate();
            const prevMotivation = stats?.motivation ?? 0;

            setStats(response);

            const motivationGained = (response.motivation ?? 0) - prevMotivation;
            setLastAction(`Motivated! +${motivationGained > 0 ? motivationGained : 'max'} motivation`);
            console.log('💪 Pet motivated:', response);

            // Clear animation after Play duration (~3s)
            setTimeout(() => setCurrentAnimation(null), 3000);

            return response;
        } catch (err) {
            setCurrentAnimation(null);
            const message = err instanceof Error ? err.message : 'Failed to motivate pet';
            setError(message);
            throw err;
        } finally {
            setActionLoading(false);
        }
    };

    const bathePet = async (): Promise<PetStats> => {
        setActionLoading(true);
        setError(null);

        try {
            // Trigger bathe animation FIRST (long animation ~10s)
            setCurrentAnimation('bathe');

            const response = await petApi.bathe();
            const prevCleanliness = stats?.cleanliness ?? 0;

            // Delay stats update so dirt fade animation plays first
            // Stats update at ~9s, animation handles dirt fading at 7-9.25s
            setTimeout(() => {
                setStats(response);
            }, 9000);

            const cleanlinessGained = (response.cleanliness ?? 0) - prevCleanliness;
            setLastAction(`Bathed! +${cleanlinessGained > 0 ? cleanlinessGained : 'sparkly clean'} cleanliness`);
            console.log('🛁 Pet bathed:', response);

            // Clear animation after Clean 1 duration (~12s)
            setTimeout(() => setCurrentAnimation(null), 12000);

            return response;
        } catch (err) {
            setCurrentAnimation(null);
            const message = err instanceof Error ? err.message : 'Failed to bathe pet';
            setError(message);
            throw err;
        } finally {
            setActionLoading(false);
        }
    };

    const sleepPet = async (): Promise<PetStats> => {
        setActionLoading(true);
        setError(null);

        try {
            // Trigger sleep animation - stays sleeping indefinitely
            setCurrentAnimation('sleep');

            const response = await petApi.sleep();
            const prevFatigue = stats?.fatigue ?? 0;

            setStats(response);

            const fatigueGained = (response.fatigue ?? 0) - prevFatigue;
            setLastAction(`Went to sleep! ${fatigueGained > 0 ? `+${fatigueGained}` : 'Resting...'} fatigue`);
            console.log('😴 Pet sleeping:', response);

            // NO setTimeout here - pet stays sleeping until explicitly woken

            return response;
        } catch (err) {
            setCurrentAnimation(null);
            const message = err instanceof Error ? err.message : 'Failed to put pet to sleep';
            setError(message);
            throw err;
        } finally {
            setActionLoading(false);
        }
    };

    const wakePet = async (): Promise<PetStats> => {
        setActionLoading(true);
        setError(null);

        try {
            // Trigger wake animation
            setCurrentAnimation('wake');

            const response = await petApi.wake();
            const prevFatigue = stats?.fatigue ?? 0;

            setStats(response);

            const fatigueReduced = prevFatigue - (response.fatigue ?? 0);
            setLastAction(`Woke up! ${fatigueReduced > 0 ? `-${fatigueReduced}` : 'Refreshed!'} fatigue`);
            console.log('⏰ Pet woken:', response);

            // Clear animation after Wake Up duration (~3s)
            setTimeout(() => setCurrentAnimation(null), 3000);

            return response;
        } catch (err) {
            setCurrentAnimation(null);
            const message = err instanceof Error ? err.message : 'Failed to wake pet';
            setError(message);
            throw err;
        } finally {
            setActionLoading(false);
        }
    };

    const clearError = () => setError(null);
    const clearLastAction = () => setLastAction(null);

    // ==================== CONTEXT VALUE ====================

    const value: PetContextType = {
        stats,
        loading,
        error,
        actionLoading,
        lastAction,
        currentAnimation,
        refreshStats,
        feedPet,
        motivatePet,
        bathePet,
        sleepPet,
        wakePet,
        clearError,
        clearLastAction,
    };

    return (
        <PetContext.Provider value={value}>
            {children}
        </PetContext.Provider>
    );
}

// ==================== HOOK ====================

export function usePet(): PetContextType {
    const context = useContext(PetContext);
    if (context === undefined) {
        throw new Error('usePet must be used within a PetProvider');
    }
    return context;
}

export default PetContext;