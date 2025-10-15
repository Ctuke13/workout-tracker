// contexts/SeasonContext.tsx
import React, {createContext, useContext, useEffect, useState, ReactNode} from 'react';
import {progressApi} from '../services/progressApi';

// Types
interface Season {
    seasonId: number;
    seasonName: string;
    seasonType: string;
    startDate: string;
    endDate: string;
    isActive: boolean;
}

interface SeasonTheme {
    name: string;
    emoji: string;
    gradient: string;
    border: string;
    textPrimary: string;
    textSecondary: string;
    textTertiary: string;
    progressBar: string;
    progressBg: string;
    progressBorder: string;
    cardBg: string;
    cardBorder: string;
    buttonGradient: string;
    messageBg: string;
    messageBorder: string;
    messageText: string;
    orb1: string;
    orb2: string;
    accentLight: string;
    accentGradient: string;
    accentBg: string;
}

interface SeasonContextType {
    season: Season | null;
    theme: SeasonTheme;
    loading: boolean;
    error: string | null;
    refetch: () => Promise<void>;
}

// Default theme (fallback)
const defaultTheme: SeasonTheme = {
    name: 'Default',
    emoji: '🌟',
    gradient: 'from-slate-50 via-gray-50 to-slate-100',
    border: 'border-slate-300',
    textPrimary: 'text-slate-900',
    textSecondary: 'text-slate-700',
    textTertiary: 'text-slate-600',
    progressBar: 'from-slate-500 via-gray-400 to-slate-600',
    progressBg: 'bg-slate-200',
    progressBorder: 'border-slate-300',
    cardBg: 'bg-slate-100/80',
    cardBorder: 'border-slate-300',
    buttonGradient: 'from-slate-600 to-gray-600 hover:from-slate-500 hover:to-gray-500',
    messageBg: 'bg-slate-100',
    messageBorder: 'border-slate-300',
    messageText: 'text-slate-800',
    orb1: 'bg-slate-300/40',
    orb2: 'bg-gray-300/40',
    accentLight: 'bg-slate-300/40',
    accentGradient: 'bg-gradient-to-br from-slate-500 to-gray-600',
    accentBg: 'bg-slate-500'
};

// Get season theme based on season name
const getSeasonTheme = (seasonName: string): SeasonTheme => {
    const lowerName = seasonName.toLowerCase();

    if (lowerName.includes('winter')) {
        return {
            name: 'Winter',
            emoji: '❄️',
            gradient: 'from-blue-50 via-cyan-50 to-blue-100',
            border: 'border-blue-300',
            textPrimary: 'text-blue-900',
            textSecondary: 'text-blue-700',
            textTertiary: 'text-blue-600',
            progressBar: 'from-blue-500 via-cyan-400 to-blue-600',
            progressBg: 'bg-blue-200',
            progressBorder: 'border-blue-300',
            cardBg: 'bg-blue-100/80',
            cardBorder: 'border-blue-300',
            buttonGradient: 'from-blue-600 to-cyan-600 hover:from-blue-500 hover:to-cyan-500',
            messageBg: 'bg-blue-100',
            messageBorder: 'border-blue-300',
            messageText: 'text-blue-800',
            orb1: 'bg-blue-300/40',
            orb2: 'bg-cyan-300/40',
            accentLight: 'bg-blue-300/40',
            accentGradient: 'bg-gradient-to-br from-blue-500 to-cyan-600',
            accentBg: 'bg-blue-500'
        };
    } else if (lowerName.includes('spring')) {
        return {
            name: 'Spring',
            emoji: '🌸',
            gradient: 'from-green-50 via-emerald-50 to-teal-50',
            border: 'border-green-300',
            textPrimary: 'text-green-900',
            textSecondary: 'text-green-700',
            textTertiary: 'text-green-600',
            progressBar: 'from-green-500 via-emerald-400 to-green-600',
            progressBg: 'bg-green-200',
            progressBorder: 'border-green-300',
            cardBg: 'bg-green-100/80',
            cardBorder: 'border-green-300',
            buttonGradient: 'from-green-600 to-emerald-600 hover:from-green-500 hover:to-emerald-500',
            messageBg: 'bg-green-100',
            messageBorder: 'border-green-300',
            messageText: 'text-green-800',
            orb1: 'bg-green-300/40',
            orb2: 'bg-emerald-300/40',
            accentLight: 'bg-green-300/40',
            accentGradient: 'bg-gradient-to-br from-green-500 to-emerald-600',
            accentBg: 'bg-green-500'
        };
    } else if (lowerName.includes('summer')) {
        return {
            name: 'Summer',
            emoji: '☀️',
            gradient: 'from-orange-50 via-amber-50 to-yellow-50',
            border: 'border-orange-300',
            textPrimary: 'text-orange-900',
            textSecondary: 'text-orange-700',
            textTertiary: 'text-orange-600',
            progressBar: 'from-orange-500 via-amber-400 to-orange-600',
            progressBg: 'bg-orange-200',
            progressBorder: 'border-orange-300',
            cardBg: 'bg-orange-100/80',
            cardBorder: 'border-orange-300',
            buttonGradient: 'from-orange-600 to-amber-600 hover:from-orange-500 hover:to-amber-500',
            messageBg: 'bg-orange-100',
            messageBorder: 'border-orange-300',
            messageText: 'text-orange-800',
            orb1: 'bg-orange-300/40',
            orb2: 'bg-amber-300/40',
            accentLight: 'bg-orange-300/40',
            accentGradient: 'bg-gradient-to-br from-orange-500 to-amber-600',
            accentBg: 'bg-orange-500'
        };
    } else { // Fall/Autumn (default)
        return {
            name: 'Fall',
            emoji: '🍂',
            gradient: 'from-amber-50 via-orange-50 to-red-50',
            border: 'border-amber-300',
            textPrimary: 'text-amber-900',
            textSecondary: 'text-amber-700',
            textTertiary: 'text-amber-600',
            progressBar: 'from-amber-500 via-orange-400 to-red-500',
            progressBg: 'bg-amber-200',
            progressBorder: 'border-amber-300',
            cardBg: 'bg-amber-100/80',
            cardBorder: 'border-amber-300',
            buttonGradient: 'from-amber-600 to-orange-600 hover:from-amber-500 hover:to-orange-500',
            messageBg: 'bg-amber-100',
            messageBorder: 'border-amber-300',
            messageText: 'text-amber-800',
            orb1: 'bg-amber-300/40',
            orb2: 'bg-orange-300/40',
            accentLight: 'bg-amber-300/40',
            accentGradient: 'bg-gradient-to-br from-amber-500 to-orange-600',
            accentBg: 'bg-amber-500'
        };
    }
};

// Create context
const SeasonContext = createContext<SeasonContextType | undefined>(undefined);

// Provider component
interface SeasonProviderProps {
    children: ReactNode;
}

export const SeasonProvider: React.FC<SeasonProviderProps> = ({children}) => {
    const [season, setSeason] = useState<Season | null>(null);
    const [theme, setTheme] = useState<SeasonTheme>(defaultTheme);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchSeason = async () => {
        try {
            setLoading(true);
            setError(null);

            const seasonData = await progressApi.getCurrentSeason();
            setSeason(seasonData);
            setTheme(getSeasonTheme(seasonData.seasonName));

            console.log('✅ Season loaded:', seasonData.seasonName);
        } catch (err) {
            console.error('❌ Failed to load season:', err);
            setError('Failed to load season data');
            // Keep default theme on error
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSeason();
    }, []);

    const value: SeasonContextType = {
        season,
        theme,
        loading,
        error,
        refetch: fetchSeason
    };

    return (
        <SeasonContext.Provider value={value}>
            {children}
        </SeasonContext.Provider>
    );
};

// Custom hook to use season context
export const useSeason = (): SeasonContextType => {
    const context = useContext(SeasonContext);
    if (context === undefined) {
        throw new Error('useSeason must be used within a SeasonProvider');
    }
    return context;
};

export default SeasonContext;