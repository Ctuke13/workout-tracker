// src/services/authService.ts
import {
    LoginRequest,
    RegisterRequest,
    JwtResponse,
    ApiResponse,
    UserSummary
} from '../types/auth';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

class AuthService {
    private baseURL = `${API_BASE_URL}/api/auth`;

    // ==================== AUTHENTICATION METHODS ====================

    async login(credentials: LoginRequest): Promise<JwtResponse> {
        try {
            const response = await fetch(`${this.baseURL}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(credentials),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Login failed: ${response.status}`);
            }

            const data: JwtResponse = await response.json();
            this.setToken(data.token);
            return data;
        } catch (error) {
            console.error('Login error:', error);
            throw error instanceof Error ? error : new Error('Login failed');
        }
    }

    async register(userData: RegisterRequest): Promise<JwtResponse> {
        try {
            const response = await fetch(`${this.baseURL}/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Registration failed: ${response.status}`);
            }

            const data: JwtResponse = await response.json();
            this.setToken(data.token);
            return data;
        } catch (error) {
            console.error('Registration error:', error);
            throw error instanceof Error ? error : new Error('Registration failed');
        }
    }

    async refreshToken(): Promise<JwtResponse> {
        try {
            const token = this.getToken();
            if (!token) {
                throw new Error('No token available for refresh');
            }

            const response = await fetch(`${this.baseURL}/refresh-token`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error(`Token refresh failed: ${response.status}`);
            }

            const data: JwtResponse = await response.json();
            this.setToken(data.token);
            return data;
        } catch (error) {
            console.error('Token refresh error:', error);
            this.removeToken();
            throw error instanceof Error ? error : new Error('Token refresh failed');
        }
    }

    async getCurrentUser(): Promise<UserSummary> {
        try {
            const token = this.getToken();
            if (!token) {
                throw new Error('No authentication token');
            }

            const response = await fetch(`${this.baseURL}/me`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to get current user: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Get current user error:', error);
            throw error instanceof Error ? error : new Error('Failed to get current user');
        }
    }

    logout(): void {
        this.removeToken();
        // Optional: Call logout endpoint for server-side cleanup
        const token = this.getToken();
        if (token) {
            fetch(`${this.baseURL}/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            }).catch(() => {
                // Ignore errors - token is already being removed
            });
        }
    }

    // ==================== AVAILABILITY CHECKS ====================

    async checkEmailAvailability(email: string): Promise<boolean> {
        try {
            const response = await fetch(`${this.baseURL}/check-email?email=${encodeURIComponent(email)}`);

            if (!response.ok) {
                throw new Error(`Availability check failed: ${response.status}`);
            }

            const data: ApiResponse = await response.json();
            return data.success;
        } catch (error) {
            console.error('Email availability check error:', error);
            return false;
        }
    }

    async checkUsernameAvailability(username: string): Promise<boolean> {
        try {
            const response = await fetch(`${this.baseURL}/check-username?username=${encodeURIComponent(username)}`);

            if (!response.ok) {
                throw new Error(`Availability check failed: ${response.status}`);
            }

            const data: ApiResponse = await response.json();
            return data.success;
        } catch (error) {
            console.error('Username availability check error:', error);
            return false;
        }
    }

    // ==================== TOKEN MANAGEMENT ====================

    getToken(): string | null {
        return localStorage.getItem('auth_token');
    }

    setToken(token: string): void {
        localStorage.setItem('auth_token', token);
    }

    removeToken(): void {
        localStorage.removeItem('auth_token');
    }

    isTokenExpired(token: string): boolean {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            return payload.exp < currentTime;
        } catch {
            return true;
        }
    }

    // ==================== HTTP HELPERS ====================

    getAuthHeaders(): HeadersInit {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    async authenticatedFetch(url: string, options: RequestInit = {}): Promise<Response> {
        const token = this.getToken();

        if (!token) {
            throw new Error('No authentication token available');
        }

        if (this.isTokenExpired(token)) {
            try {
                await this.refreshToken();
            } catch {
                throw new Error('Authentication expired');
            }
        }

        return fetch(url, {
            ...options,
            headers: {
                ...options.headers,
                ...this.getAuthHeaders(),
            },
        });
    }
}

export const authService = new AuthService();
export default authService;