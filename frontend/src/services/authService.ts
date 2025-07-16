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
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Starting login request for:`, credentials.emailOrUsername);

        try {
            const response = await fetch(`${this.baseURL}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(credentials),
            });

            console.log(`📡 [${requestId}] Login response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                console.log(`❌ [${requestId}] Login error response:`, errorData);
                throw new Error(errorData.message || `Login failed: ${response.status}`);
            }

            const data: JwtResponse = await response.json();
            console.log(`✅ [${requestId}] Login successful for user:`, data.username);
            this.setToken(data.token);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Login error:`, error);
            throw error instanceof Error ? error : new Error('Login failed');
        }
    }

    async register(userData: RegisterRequest): Promise<JwtResponse> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Starting registration request for:`, userData.email);

        try {
            const response = await fetch(`${this.baseURL}/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });

            console.log(`📡 [${requestId}] Registration response status:`, response.status);

            if (!response.ok) {
                const errorData = await response.text(); // Use text() first, then try to parse
                console.log(`❌ [${requestId}] Registration error response:`, errorData);

                // Try to parse as JSON, fallback to raw text
                let errorMessage = errorData;
                try {
                    const parsed = JSON.parse(errorData);
                    errorMessage = parsed.message || errorData;
                } catch {
                    // Use raw text if not valid JSON
                }

                throw new Error(errorMessage || `Registration failed: ${response.status}`);
            }

            const data: JwtResponse = await response.json();
            console.log(`✅ [${requestId}] Registration successful for user:`, data.username);
            this.setToken(data.token);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Registration error:`, error);
            throw error instanceof Error ? error : new Error('Registration failed');
        }
    }

    async refreshToken(): Promise<JwtResponse> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Starting token refresh`);

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

            console.log(`📡 [${requestId}] Token refresh response status:`, response.status);

            if (!response.ok) {
                throw new Error(`Token refresh failed: ${response.status}`);
            }

            const data: JwtResponse = await response.json();
            console.log(`✅ [${requestId}] Token refresh successful`);
            this.setToken(data.token);
            return data;
        } catch (error) {
            console.error(`💥 [${requestId}] Token refresh error:`, error);
            this.removeToken();
            throw error instanceof Error ? error : new Error('Token refresh failed');
        }
    }

    async getCurrentUser(): Promise<UserSummary> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Getting current user`);

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

            console.log(`📡 [${requestId}] Get user response status:`, response.status);

            if (!response.ok) {
                throw new Error(`Failed to get current user: ${response.status}`);
            }

            const userData = await response.json();
            console.log(`✅ [${requestId}] Got current user:`, userData.username);
            return userData;
        } catch (error) {
            console.error(`💥 [${requestId}] Get current user error:`, error);
            throw error instanceof Error ? error : new Error('Failed to get current user');
        }
    }

    logout(): void {
        console.log('🚪 Logging out user');
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
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Checking email availability for:`, email);

        try {
            const response = await fetch(`${this.baseURL}/check-email?email=${encodeURIComponent(email)}`);

            console.log(`📡 [${requestId}] Email check response status:`, response.status);

            if (!response.ok) {
                throw new Error(`Availability check failed: ${response.status}`);
            }

            const data: ApiResponse = await response.json();
            console.log(`✅ [${requestId}] Email availability result:`, data.success);
            return data.success;
        } catch (error) {
            console.error(`💥 [${requestId}] Email availability check error:`, error);
            return false;
        }
    }

    async checkUsernameAvailability(username: string): Promise<boolean> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Checking username availability for:`, username);

        try {
            const response = await fetch(`${this.baseURL}/check-username?username=${encodeURIComponent(username)}`);

            console.log(`📡 [${requestId}] Username check response status:`, response.status);

            if (!response.ok) {
                throw new Error(`Availability check failed: ${response.status}`);
            }

            const data: ApiResponse = await response.json();
            console.log(`✅ [${requestId}] Username availability result:`, data.success);
            return data.success;
        } catch (error) {
            console.error(`💥 [${requestId}] Username availability check error:`, error);
            return false;
        }
    }

    // ==================== TOKEN MANAGEMENT ====================

    getToken(): string | null {
        return localStorage.getItem('auth_token');
    }

    setToken(token: string): void {
        localStorage.setItem('auth_token', token);
        console.log('🔑 Token saved to localStorage');
    }

    removeToken(): void {
        localStorage.removeItem('auth_token');
        console.log('🗑️ Token removed from localStorage');
    }

    isTokenExpired(token: string): boolean {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            const isExpired = payload.exp < currentTime;
            console.log(`🕒 Token expiry check - Expired: ${isExpired}`);
            return isExpired;
        } catch {
            console.log('❌ Invalid token format');
            return true;
        }
    }

    // ==================== HTTP HELPERS ====================

    getAuthHeaders(): HeadersInit {
        const token = this.getToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    async authenticatedFetch(url: string, options: RequestInit = {}): Promise<Response> {
        const requestId = Math.random().toString(36).substr(2, 9);
        console.log(`🌟 [${requestId}] Making authenticated request to:`, url);

        const token = this.getToken();

        if (!token) {
            throw new Error('No authentication token available');
        }

        if (this.isTokenExpired(token)) {
            try {
                console.log(`🔄 [${requestId}] Token expired, refreshing...`);
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