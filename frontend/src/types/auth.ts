import {UserType, Gender, AccountStatus, ActivityLevel} from './enums';

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
    subscriptionTier: 'FREE' | 'PLUS' | 'PRO' | 'PRO_PROFESSIONAL';
    // ==================== ONBOARDING FIELDS ====================
    nickname: string | null;
    petName: string | null;
    onboardingCompleted: boolean;
    // ==================== TUTORIAL FIELDS ====================
    petTutorialCompleted: boolean;
    calendarTutorialCompleted: boolean;
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
    subscriptionTier: 'FREE' | 'PLUS' | 'PRO' | 'PRO_PROFESSIONAL';
    // ==================== ONBOARDING FIELDS ====================
    nickname: string | null;
    petName: string | null;
    onboardingCompleted: boolean;
    // ==================== TUTORIAL FIELDS ====================
    petTutorialCompleted: boolean;
    calendarTutorialCompleted: boolean;
}

// ==================== ONBOARDING TYPES ====================

export interface NicknameCheckResponse {
    available: boolean;
    message: string;
}

export interface PetNameCheckResponse {
    valid: boolean;
    message: string;
}

export interface CompleteOnboardingRequest {
    nickname?: string;
    petName?: string;
}

export interface OnboardingStatusResponse {
    onboardingCompleted: boolean;
    nickname: string | null;
    petName: string | null;
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
    refreshUser: () => Promise<void>;
    checkAvailability: (type: 'email' | 'username', value: string) => Promise<boolean>;
    checkNicknameAvailability: (nickname: string) => Promise<NicknameCheckResponse>;
    checkPetNameValidity: (petName: string) => Promise<PetNameCheckResponse>;
    completeOnboarding: (data: CompleteOnboardingRequest) => Promise<void>;
    getOnboardingStatus: () => Promise<OnboardingStatusResponse>;
    getCurrentUser: () => Promise<UserSummary>;
    clearError: () => void;
    // Helper for checking if user needs onboarding
    needsOnboarding: boolean;
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