// types/index.ts
export * from './exercise';
// export * from './api';

// Common shared types
export interface ApiResponse<T> {
    data: T;
    success: boolean;
    message?: string;
    errors?: string[];
}

export interface PaginationParams {
    page?: number;
    limit?: number;
    offset?: number;
}

export interface SearchParams {
    query?: string;
    filters?: Record<string, any>;
    sort?: string;
    order?: 'asc' | 'desc';
}

// UI State types
export interface LoadingState {
    isLoading: boolean;
    error: string | null;
}

export interface FormState<T> {
    data: T;
    errors: Record<keyof T, string>;
    isSubmitting: boolean;
    isDirty: boolean;
}

// Navigation types
export interface NavigationItem {
    id: string;
    label: string;
    href: string;
    icon?: string;
    isActive?: boolean;
}

// Component Props helpers
export interface BaseComponentProps {
    className?: string;
    id?: string;
    'data-testid'?: string;
}