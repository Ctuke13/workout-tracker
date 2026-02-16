import React, {createContext, useContext, useReducer, useEffect, ReactNode} from 'react';
import {useNavigate, useLocation} from 'react-router-dom';
import {
    AuthState,
    AuthContextType,
    LoginRequest,
    RegisterRequest,
    JwtResponse,
    UserSummary,
    NicknameCheckResponse,
    PetNameCheckResponse,
    CompleteOnboardingRequest,
    OnboardingStatusResponse,
} from '../types/auth';
import authService from '../services/authService';

// ==================== AUTH REDUCER ====================

type AuthAction =
    | { type: 'LOGIN_START' }
    | { type: 'LOGIN_SUCCESS'; payload: JwtResponse }
    | { type: 'LOGIN_FAILURE'; payload: string }
    | { type: 'REGISTER_START' }
    | { type: 'REGISTER_SUCCESS'; payload: JwtResponse }
    | { type: 'REGISTER_FAILURE'; payload: string }
    | { type: 'LOGOUT' }
    | { type: 'REFRESH_TOKEN_SUCCESS'; payload: JwtResponse }
    | { type: 'ONBOARDING_COMPLETE'; payload: JwtResponse }
    | { type: 'CLEAR_ERROR' }
    | { type: 'SET_LOADING'; payload: boolean }
    | { type: 'INIT_COMPLETE' };

const initialState: AuthState = {
    isAuthenticated: false,
    user: null,
    token: null,
    loading: true,
    error: null,
};

function authReducer(state: AuthState, action: AuthAction): AuthState {
    switch (action.type) {
        case 'LOGIN_START':
        case 'REGISTER_START':
            return {
                ...state,
                loading: true,
                error: null,
            };

        case 'LOGIN_SUCCESS':
        case 'REGISTER_SUCCESS':
        case 'REFRESH_TOKEN_SUCCESS':
        case 'ONBOARDING_COMPLETE':
            return {
                ...state,
                isAuthenticated: true,
                user: action.payload,
                token: action.payload.token,
                loading: false,
                error: null,
            };

        case 'LOGIN_FAILURE':
        case 'REGISTER_FAILURE':
            return {
                ...state,
                isAuthenticated: false,
                user: null,
                token: null,
                loading: false,
                error: action.payload,
            };

        case 'LOGOUT':
            return {
                ...initialState,
                loading: false,
            };

        case 'CLEAR_ERROR':
            return {
                ...state,
                error: null,
            };

        case 'SET_LOADING':
            return {
                ...state,
                loading: action.payload,
            };

        case 'INIT_COMPLETE':
            return {
                ...state,
                loading: false,
            };

        default:
            return state;
    }
}

// ==================== ONBOARDING ROUTES ====================

const ONBOARDING_ROUTES = [
    '/onboarding',
    '/onboarding/nickname',
    '/onboarding/meet-pet',
    '/onboarding/name-pet',
];

const PUBLIC_ROUTES = ['/', '/login', '/register'];

// ==================== AUTH CONTEXT ====================

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({children}: AuthProviderProps) {
    const [state, dispatch] = useReducer(authReducer, initialState);
    const navigate = useNavigate();
    const location = useLocation();

    // ==================== COMPUTED VALUES ====================

    const needsOnboarding = state.isAuthenticated && state.user?.onboardingCompleted === false;

    // ==================== AUTHENTICATION METHODS ====================

    const login = async (credentials: LoginRequest): Promise<void> => {
        dispatch({type: 'LOGIN_START'});
        try {
            const response = await authService.login(credentials);
            dispatch({type: 'LOGIN_SUCCESS', payload: response});

            // Redirect based on onboarding status
            if (!response.onboardingCompleted) {
                console.log('🐺 User needs onboarding, redirecting...');
                navigate('/onboarding/nickname');
            } else {
                console.log('✅ Login successful, redirecting to home');
                navigate('/welcome');
            }
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Login failed';
            dispatch({type: 'LOGIN_FAILURE', payload: message});
            throw error;
        }
    };

    const register = async (userData: RegisterRequest): Promise<void> => {
        dispatch({type: 'REGISTER_START'});
        try {
            const response = await authService.register(userData);
            dispatch({type: 'REGISTER_SUCCESS', payload: response});

            // New users always go to onboarding
            console.log('🐺 New registration, starting onboarding...');
            navigate('/onboarding/nickname');
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Registration failed';
            dispatch({type: 'REGISTER_FAILURE', payload: message});
            throw error;
        }
    };

    const logout = (): void => {
        authService.logout();
        dispatch({type: 'LOGOUT'});
        navigate('/');
        console.log('✅ Logout successful');
    };

    const refreshToken = async (): Promise<void> => {
        try {
            const response = await authService.refreshToken();
            dispatch({type: 'REFRESH_TOKEN_SUCCESS', payload: response});
        } catch (error) {
            logout();
            throw error;
        }
    };

    const checkAvailability = async (type: 'email' | 'username', value: string): Promise<boolean> => {
        try {
            if (type === 'email') {
                return await authService.checkEmailAvailability(value);
            } else {
                return await authService.checkUsernameAvailability(value);
            }
        } catch (error) {
            console.error(`Availability check failed for ${type}:`, error);
            return false;
        }
    };

    // ==================== ONBOARDING METHODS ====================

    const checkNicknameAvailability = async (nickname: string): Promise<NicknameCheckResponse> => {
        return authService.checkNicknameAvailability(nickname);
    };

    const checkPetNameValidity = async (petName: string): Promise<PetNameCheckResponse> => {
        return authService.checkPetNameValidity(petName);
    };

    const completeOnboarding = async (data: CompleteOnboardingRequest): Promise<void> => {
        try {
            const response = await authService.completeOnboarding(data);
            dispatch({type: 'ONBOARDING_COMPLETE', payload: response});

            console.log('🎉 Onboarding complete! Welcome to EvoPet!');
            navigate('/welcome');
        } catch (error) {
            console.error('Onboarding failed:', error);
            throw error;
        }
    };

    const getOnboardingStatus = async (): Promise<OnboardingStatusResponse> => {
        return authService.getOnboardingStatus();
    };

    const getCurrentUser = async (): Promise<UserSummary> => {
        return authService.getCurrentUser();
    };

    const clearError = (): void => {
        dispatch({type: 'CLEAR_ERROR'});
    };

    // ==================== INITIALIZATION ====================

    useEffect(() => {
        const initializeAuth = async () => {
            console.log('🔄 Initializing auth...');

            try {
                const token = authService.getToken();
                console.log('🔑 Token from localStorage:', token ? 'Found' : 'Not found');

                if (!token) {
                    console.log('❌ No token found, user not authenticated');
                    return;
                }

                if (authService.isTokenExpired(token)) {
                    console.log('⏰ Token expired, removing...');
                    authService.removeToken();
                    return;
                }

                console.log('✅ Valid token found, getting user data...');

                const user = await authService.getCurrentUser();
                console.log('👤 User data retrieved:', user.username);
                console.log('🐺 Onboarding completed:', user.onboardingCompleted);

                // Create JwtResponse object from stored token and user data
                const jwtResponse: JwtResponse = {
                    token,
                    type: 'Bearer',
                    id: user.id,
                    username: user.username,
                    email: user.email,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    userType: user.userType,
                    isProfessional: user.isProfessional,
                    subscriptionTier: user.subscriptionTier || 'FREE',
                    // Onboarding fields
                    nickname: user.nickname,
                    petName: user.petName,
                    onboardingCompleted: user.onboardingCompleted,
                };

                dispatch({type: 'LOGIN_SUCCESS', payload: jwtResponse});
                console.log('✅ Auth initialization successful');
            } catch (error) {
                console.error('❌ Auth initialization failed:', error);
                authService.removeToken();
            } finally {
                dispatch({type: 'INIT_COMPLETE'});
                console.log('🏁 Auth initialization complete');
            }
        };

        initializeAuth();
    }, []);

    // ==================== ONBOARDING ROUTE PROTECTION ====================

    useEffect(() => {
        // Don't redirect while loading
        if (state.loading) return;

        const currentPath = location.pathname;
        const isPublicRoute = PUBLIC_ROUTES.includes(currentPath);
        const isOnboardingRoute = ONBOARDING_ROUTES.some(route => currentPath.startsWith(route));

        // Not authenticated - can only access public routes
        if (!state.isAuthenticated) {
            if (!isPublicRoute && !isOnboardingRoute) {
                console.log('🚫 Not authenticated, redirecting to login');
                navigate('/login');
            }
            return;
        }

        // Authenticated but needs onboarding
        if (needsOnboarding) {
            if (!isOnboardingRoute) {
                console.log('🐺 Needs onboarding, redirecting...');
                navigate('/onboarding/nickname');
            }
            return;
        }

        // Authenticated and completed onboarding
        if (state.user?.onboardingCompleted) {
            // Redirect away from onboarding pages if already complete
            if (isOnboardingRoute) {
                console.log('✅ Already onboarded, redirecting to welcome');
                navigate('/welcome');
            }
            // Redirect away from public pages if logged in
            if (isPublicRoute && currentPath !== '/') {
                navigate('/welcome');
            }
        }
    }, [state.isAuthenticated, state.loading, state.user?.onboardingCompleted, location.pathname, navigate, needsOnboarding]);

    // ==================== TOKEN REFRESH INTERVAL ====================

    useEffect(() => {
        if (!state.isAuthenticated || !state.token) {
            return;
        }

        const refreshInterval = setInterval(async () => {
            try {
                if (authService.isTokenExpired(state.token!)) {
                    await refreshToken();
                }
            } catch (error) {
                console.error('Automatic token refresh failed:', error);
                logout();
            }
        }, 5 * 60 * 1000); // 5 minutes

        return () => clearInterval(refreshInterval);
    }, [state.isAuthenticated, state.token]);

    // ==================== CONTEXT VALUE ====================

    const contextValue: AuthContextType = {
        ...state,
        login,
        register,
        logout,
        refreshToken,
        checkAvailability,
        checkNicknameAvailability,
        checkPetNameValidity,
        completeOnboarding,
        getOnboardingStatus,
        getCurrentUser,
        clearError,
        needsOnboarding,
    };

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
}

// ==================== HOOK ====================

export function useAuth(): AuthContextType {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}

export default AuthContext;