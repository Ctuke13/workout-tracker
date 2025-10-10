// hooks/useUserProgression.ts
import {useState, useEffect} from 'react';
import {UserProgression} from '../types/gamification';
import {progressApi} from '../services/progressApi';
import {useAuth} from '../contexts/AuthContext';

interface UseUserProgressionResult {
    progression: UserProgression | null;
    loading: boolean;
    error: Error | null;
    refresh: () => Promise<void>;
}

export const useUserProgression = (): UseUserProgressionResult => {
    const {isAuthenticated, user} = useAuth();
    const [progression, setProgression] = useState<UserProgression | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<Error | null>(null);

    const fetchProgression = async () => {
        // Don't try to fetch if not authenticated
        if (!isAuthenticated) {
            setLoading(false);
            setProgression(null);
            return;
        }

        try {
            setLoading(true);
            setError(null);
            const data = await progressApi.getUserProgression();
            setProgression(data);
            console.log('✅ Loaded progression from backend:', data);
        } catch (err) {
            console.error('❌ Failed to fetch user progression:', err);
            setError(err as Error);
            setProgression(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProgression();
    }, [isAuthenticated]);

    return {
        progression,
        loading,
        error,
        refresh: fetchProgression
    };
};