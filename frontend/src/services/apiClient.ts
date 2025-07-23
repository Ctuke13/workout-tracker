// src/services/apiClient.ts - API Client that leverages your existing AuthService

import axios, { AxiosInstance, AxiosResponse, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { authService } from './authService';
import { ApiResponse } from '../types/api';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

class ApiClient {
    private client: AxiosInstance;
    private isRefreshingToken = false;
    private refreshTokenPromise: Promise<void> | null = null;

    constructor() {
        this.client = axios.create({
            baseURL: API_BASE_URL,
            timeout: 10000,
            headers: {
                'Content-Type': 'application/json',
            },
        });

        this.setupInterceptors();
    }

    private setupInterceptors(): void {
        // Request interceptor - automatically add auth token
        this.client.interceptors.request.use(
            (config: InternalAxiosRequestConfig) => {
                const token = authService.getToken();
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }

                if (process.env.NODE_ENV === 'development') {
                    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`);
                    if (config.params) console.log('📋 Query params:', config.params);
                }

                return config;
            },
            (error) => {
                console.error('❌ Request interceptor error:', error);
                return Promise.reject(error);
            }
        );

        // Response interceptor - handle auth errors and auto-retry
        this.client.interceptors.response.use(
            (response: AxiosResponse) => {
                if (process.env.NODE_ENV === 'development') {
                    console.log(`✅ API Response: ${response.status} ${response.config.url}`);
                }
                return response;
            },
            async (error: AxiosError) => {
                const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

                // Handle 401 unauthorized errors with automatic token refresh
                if (error.response?.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;

                    try {
                        await this.handleTokenRefresh();

                        // Retry the original request with new token
                        const token = authService.getToken();
                        if (token) {
                            originalRequest.headers.Authorization = `Bearer ${token}`;
                            return this.client.request(originalRequest);
                        }
                    } catch (refreshError) {
                        console.error('❌ Token refresh failed, redirecting to login');
                        authService.logout();
                        // You might want to redirect to login page here
                        return Promise.reject(refreshError);
                    }
                }

                // Log other errors for debugging
                if (process.env.NODE_ENV === 'development') {
                    console.error('❌ API Error:', {
                        url: error.config?.url,
                        status: error.response?.status,
                        message: error.message,
                        data: error.response?.data
                    });
                }

                return Promise.reject(error);
            }
        );
    }

    private async handleTokenRefresh(): Promise<void> {
        // Prevent multiple simultaneous token refresh attempts
        if (this.isRefreshingToken) {
            return this.refreshTokenPromise || Promise.resolve();
        }

        this.isRefreshingToken = true;
        this.refreshTokenPromise = authService.refreshToken().then(() => {
            console.log('🔄 Token refreshed successfully');
        }).finally(() => {
            this.isRefreshingToken = false;
            this.refreshTokenPromise = null;
        });

        return this.refreshTokenPromise;
    }

    // ==================== HTTP METHODS ====================

    /**
     * Generic GET request
     */
    public async get<T>(endpoint: string, params?: any): Promise<T> {
        try {
            const response = await this.client.get<ApiResponse<T> | T>(endpoint, { params });
            return this.extractData<T>(response.data);
        } catch (error) {
            throw this.handleError(error, 'GET', endpoint);
        }
    }

    /**
     * Generic POST request
     */
    public async post<T>(endpoint: string, data?: any): Promise<T> {
        try {
            const response = await this.client.post<ApiResponse<T> | T>(endpoint, data);
            return this.extractData<T>(response.data);
        } catch (error) {
            throw this.handleError(error, 'POST', endpoint);
        }
    }

    /**
     * Generic PUT request
     */
    public async put<T>(endpoint: string, data?: any): Promise<T> {
        try {
            const response = await this.client.put<ApiResponse<T> | T>(endpoint, data);
            return this.extractData<T>(response.data);
        } catch (error) {
            throw this.handleError(error, 'PUT', endpoint);
        }
    }

    /**
     * Generic DELETE request
     */
    public async delete<T>(endpoint: string): Promise<T> {
        try {
            const response = await this.client.delete<ApiResponse<T> | T>(endpoint);
            return this.extractData<T>(response.data);
        } catch (error) {
            throw this.handleError(error, 'DELETE', endpoint);
        }
    }

    /**
     * Health check method to verify backend connectivity
     */
    public async healthCheck(): Promise<{ status: string; timestamp: string }> {
        try {
            const response = await this.client.get('/api/health');
            return response.data;
        } catch (error) {
            console.error('❌ Health check failed:', error);
            throw new Error('Backend server is not available');
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Extract data from API response, handling both wrapped and direct responses
     */
    private extractData<T>(responseData: ApiResponse<T> | T): T {
        // Check if response is wrapped in ApiResponse format
        if (responseData && typeof responseData === 'object' && 'success' in responseData) {
            const apiResponse = responseData as ApiResponse<T>;
            if (apiResponse.success && apiResponse.data !== undefined) {
                return apiResponse.data;
            } else {
                throw new Error(apiResponse.message || 'API request failed');
            }
        }

        // Return direct data if not wrapped
        return responseData as T;
    }

    /**
     * Centralized error handling
     */
    private handleError(error: any, method: string, endpoint: string): Error {
        if (axios.isAxiosError(error)) {
            const message = error.response?.data?.message ||
                error.response?.data?.error ||
                error.message ||
                'An unexpected error occurred';

            console.error(`❌ ${method} ${endpoint} failed:`, {
                status: error.response?.status,
                message: message,
                url: error.config?.url
            });

            return new Error(message);
        }

        console.error(`❌ ${method} ${endpoint} failed with non-Axios error:`, error);
        return error instanceof Error ? error : new Error('An unexpected error occurred');
    }

    /**
     * Get the underlying Axios instance for advanced usage
     */
    // ==================== TESTING METHODS ====================

    /**
     * Simple connectivity test - doesn't require authentication
     */
    public async testConnection(): Promise<{ status: string; message: string }> {
        try {
            console.log('🧪 Testing basic connectivity to:', API_BASE_URL);
            const response = await this.client.get('/api/health', { timeout: 5000 });
            console.log('✅ Connection test successful:', response.status);
            return {
                status: 'success',
                message: `Connected to ${API_BASE_URL} (Status: ${response.status})`
            };
        } catch (error: any) {
            console.error('❌ Connection test failed:', error.message);
            if (error.code === 'ECONNREFUSED') {
                return {
                    status: 'error',
                    message: 'Backend server is not running or unreachable'
                };
            } else if (error.code === 'ENOTFOUND') {
                return {
                    status: 'error',
                    message: 'Cannot resolve backend server address'
                };
            } else {
                return {
                    status: 'error',
                    message: `Connection failed: ${error.message}`
                };
            }
        }
    }

    /**
     * Test public endpoints that don't require authentication
     */
    public async testPublicEndpoints(): Promise<{ endpoint: string; status: string; error?: string }[]> {
        const publicEndpoints = [
            '/api/health',
            '/api/exercises/public',
            '/api/exercises/public/filters'
        ];

        const results = [];

        for (const endpoint of publicEndpoints) {
            try {
                console.log(`🧪 Testing endpoint: ${endpoint}`);
                const response = await this.client.get(endpoint, { timeout: 5000 });
                console.log(`✅ ${endpoint} - Status: ${response.status}`);
                results.push({
                    endpoint,
                    status: 'success',
                });
            } catch (error: any) {
                console.error(`❌ ${endpoint} - Error:`, error.message);
                results.push({
                    endpoint,
                    status: 'error',
                    error: error.message
                });
            }
        }

        return results;
    }

    /**
     * Test exercise data retrieval specifically
     */
    public async testExerciseData(): Promise<{ success: boolean; data?: any; error?: string }> {
        try {
            console.log('🧪 Testing exercise data retrieval...');
            const exercises = await this.get('/api/exercises/public');
            console.log('✅ Exercise data test successful:', {
                count: Array.isArray(exercises) ? exercises.length : 'Unknown',
                sample: Array.isArray(exercises) && exercises.length > 0 ? exercises[0] : null
            });
            return {
                success: true,
                data: exercises
            };
        } catch (error: any) {
            console.error('❌ Exercise data test failed:', error.message);
            return {
                success: false,
                error: error.message
            };
        }
    }

    public getAxiosInstance(): AxiosInstance {
        return this.client;
    }
}

// Export singleton instance
export default new ApiClient();