import { UserType, Gender, AccountStatus, ActivityLevel } from './enums';

// ==================== AUTHENTICATION TYPES ====================

export interface LoginRequest {
    emailOrUsername: string;
    password: string;
    rememberMe?: boolean;
}

export interface RegisterRequest {
    email: string;
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string; // ISO date string
    gender?: Gender;
    zipcode: string;
    agreeToTerms: boolean;
}

export interface JwtResponse {
    token: string;
    type: string; // "Bearer"
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    userType: UserType;
    isProfessional: boolean;
}

export interface ApiResponse {
    success: boolean;
    message: string;
}

export interface UserSummary {
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    userType: UserType;
    accountStatus: AccountStatus;
    activityLevel?: ActivityLevel;
    isProfessional: boolean;
    isVerified: boolean;
}

// ==================== AUTH STATE ====================

export interface AuthState {
    isAuthenticated: boolean;
    user: JwtResponse | null;
    token: string | null;
    loading: boolean;
    error: string | null;
}

export interface AuthContextType extends AuthState {
    login: (credentials: LoginRequest) => Promise<void>;
    register: (userData: RegisterRequest) => Promise<void>;
    logout: () => void;
    refreshToken: () => Promise<void>;
    checkAvailability: (type: 'email' | 'username', value: string) => Promise<boolean>;
    getCurrentUser: () => Promise<UserSummary>;
    clearError: () => void;
}

// ==================== FORM VALIDATION ====================

export interface ValidationError {
    field: string;
    message: string;
}

export interface LoginFormData {
    emailOrUsername: string;
    password: string;
    rememberMe: boolean;
}

// Updated to match your backend exactly + frontend-only fields
export interface RegisterFormData {
    // Backend fields (these will be sent to API)
    email: string;
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    dateOfBirth: string;
    gender: Gender | '';
    zipcode: string;
    agreeToTerms: boolean;

    // Frontend-only fields (for validation/UX)
    confirmPassword: string;

    // Optional frontend preference fields (not sent to backend)
    fitnessGoal?: string;
    experienceLevel?: string;
    subscribeNewsletter?: boolean;
}

export interface FormErrors {
    [key: string]: string;
}