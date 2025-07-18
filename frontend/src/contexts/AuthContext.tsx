import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    AuthState,
    AuthContextType,
    LoginRequest,
    RegisterRequest,
    JwtResponse,
    UserSummary
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
    | { type: 'CLEAR_ERROR' }
    | { type: 'SET_LOADING'; payload: boolean }
    | { type: 'INIT_COMPLETE' }; // NEW: Separate action for initialization completion

const initialState: AuthState = {
    isAuthenticated: false,
    user: null,
    token: null,
    loading: true, // Start with loading true for initialization
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
                loading: false, // Don't show loading after logout
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

        case 'INIT_COMPLETE': // NEW: Always finish initialization
            return {
                ...state,
                loading: false,
            };

        default:
            return state;
    }
}

// ==================== AUTH CONTEXT ====================

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [state, dispatch] = useReducer(authReducer, initialState);
    const navigate = useNavigate();

    // ==================== AUTHENTICATION METHODS ====================

    const login = async (credentials: LoginRequest): Promise<void> => {
        dispatch({ type: 'LOGIN_START' });
        try {
            const response = await authService.login(credentials);
            dispatch({ type: 'LOGIN_SUCCESS', payload: response });

            navigate('/welcome');
            console.log('✅ Login successful, redirecting to dashboard')
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Login failed';
            dispatch({ type: 'LOGIN_FAILURE', payload: message });
            throw error;
        }
    };

    const register = async (userData: RegisterRequest): Promise<void> => {
        dispatch({ type: 'REGISTER_START' });
        try {
            const response = await authService.register(userData);
            dispatch({ type: 'REGISTER_SUCCESS', payload: response });

            navigate('/welcome', { state: { fromRegistration: true } });
            console.log('✅ Registration successful, redirecting to dashboard');
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Registration failed';
            dispatch({ type: 'REGISTER_FAILURE', payload: message });
            throw error;
        }
    };

    const logout = (): void => {
        authService.logout();
        dispatch({ type: 'LOGOUT' });

        navigate('/');
        console.log('✅ Logout successful, redirecting to home');
    };

    const refreshToken = async (): Promise<void> => {
        try {
            const response = await authService.refreshToken();
            dispatch({ type: 'REFRESH_TOKEN_SUCCESS', payload: response });
        } catch (error) {
            logout();
            throw error;
        }
    };

    const checkAvailability = async (type: 'email' | 'username', value: string): Promise<boolean> => {
        // IMPORTANT: Don't affect main loading state for availability checks
        try {
            if (type === 'email') {
                return await authService.checkEmailAvailability(value);
            } else {
                return await authService.checkUsernameAvailability(value);
            }
        } catch (error) {
            console.error(`Availability check failed for ${type}:`, error);
            return false; // Assume available on error to not block user
        }
    };

    const getCurrentUser = async (): Promise<UserSummary> => {
        return authService.getCurrentUser();
    };

    const clearError = (): void => {
        dispatch({ type: 'CLEAR_ERROR' });
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
                    return; // Will hit finally block
                }

                if (authService.isTokenExpired(token)) {
                    console.log('⏰ Token expired, removing...');
                    authService.removeToken();
                    return; // Will hit finally block
                }

                console.log('✅ Valid token found, getting user data...');

                // Try to get current user to validate token
                const user = await authService.getCurrentUser();
                console.log('👤 User data retrieved:', user.username);

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
                };

                dispatch({ type: 'LOGIN_SUCCESS', payload: jwtResponse });
                console.log('✅ Auth initialization successful');
            } catch (error) {
                console.error('❌ Auth initialization failed:', error);
                authService.removeToken();
                // Don't dispatch failure here - just let initialization complete
            } finally {
                // CRITICAL: Always finish initialization regardless of outcome
                dispatch({ type: 'INIT_COMPLETE' });
                console.log('🏁 Auth initialization complete');
            }
        };

        initializeAuth();
    }, []);

    // ==================== TOKEN REFRESH INTERVAL ====================

    useEffect(() => {
        if (!state.isAuthenticated || !state.token) {
            return;
        }

        // Refresh token 5 minutes before expiry
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
        getCurrentUser,
        clearError,
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