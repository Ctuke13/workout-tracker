import { FormErrors, LoginFormData, RegisterFormData } from '../types/auth';

// ==================== VALIDATION PATTERNS ====================

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const USERNAME_REGEX = /^[a-zA-Z0-9._-]+$/;
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
const ZIPCODE_REGEX = /^\d{5}$/;

// ==================== INDIVIDUAL VALIDATION FUNCTIONS ====================

export function validateEmail(email: string): string | null {
    if (!email) return 'Email is required';
    if (email.length > 100) return 'Email cannot exceed 100 characters';
    if (!EMAIL_REGEX.test(email)) return 'Please enter a valid email address';
    return null;
}

export function validateUsername(username: string): string | null {
    if (!username) return 'Username is required';
    if (username.length < 3) return 'Username must be at least 3 characters';
    if (username.length > 30) return 'Username cannot exceed 30 characters';
    if (!USERNAME_REGEX.test(username)) {
        return 'Username can only contain letters, numbers, dots, underscores, and hyphens';
    }
    return null;
}

export function validatePassword(password: string): string | null {
    if (!password) return 'Password is required';
    if (password.length < 8) return 'Password must be at least 8 characters';
    if (password.length > 128) return 'Password cannot exceed 128 characters';
    if (!PASSWORD_REGEX.test(password)) {
        return 'Password must contain at least one lowercase letter, one uppercase letter, and one number';
    }
    return null;
}

export function validateConfirmPassword(password: string, confirmPassword: string): string | null {
    if (!confirmPassword) return 'Please confirm your password';
    if (password !== confirmPassword) return 'Passwords do not match';
    return null;
}

export function validateFirstName(firstName: string): string | null {
    if (!firstName) return 'First name is required';
    if (firstName.length < 2) return 'First name must be at least 2 characters';
    if (firstName.length > 50) return 'First name cannot exceed 50 characters';
    return null;
}

export function validateLastName(lastName: string): string | null {
    if (!lastName) return 'Last name is required';
    if (lastName.length < 2) return 'Last name must be at least 2 characters';
    if (lastName.length > 50) return 'Last name cannot exceed 50 characters';
    return null;
}

export function validateDateOfBirth(dateOfBirth: string): string | null {
    if (!dateOfBirth) return 'Date of birth is required';

    const date = new Date(dateOfBirth);
    const today = new Date();

    if (isNaN(date.getTime())) return 'Please enter a valid date';
    if (date >= today) return 'Date of birth must be in the past';

    // Check if user is at least 13 years old
    const minAge = new Date();
    minAge.setFullYear(minAge.getFullYear() - 13);
    if (date > minAge) return 'You must be at least 13 years old';

    return null;
}