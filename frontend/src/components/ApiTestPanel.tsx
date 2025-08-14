// src/components/ApiTestPanel.tsx - Enhanced with comprehensive workout plan testing

import React, { useState } from 'react';
import { workoutPlanApi } from '../services/workoutPlanApi';

interface TestResult {
    test: string;
    status: 'pending' | 'success' | 'error';
    message: string;
    data?: any;
    timestamp?: string;
    duration?: number;
}

export const ApiTestPanel: React.FC = () => {
    const [tests, setTests] = useState<TestResult[]>([]);
    const [isRunning, setIsRunning] = useState(false);

    const addTestResult = (result: TestResult) => {
        setTests(prev => [...prev, { ...result, timestamp: new Date().toLocaleTimeString() }]);
    };

    const clearTests = () => {
        setTests([]);
    };

    const logTest = (testName: string, status: 'start' | 'success' | 'error', message?: string, data?: any) => {
        if (status === 'start') {
            console.log(`🧪 Starting ${testName}...`);
            addTestResult({ test: testName, status: 'pending', message: 'Testing...' });
        } else {
            console.log(`${status === 'success' ? '✅' : '❌'} ${testName} - ${message}`);
            addTestResult({
                test: testName,
                status,
                message: message || '',
                data
            });
        }
    };

    // ==================== BASIC CONNECTIVITY TESTS ====================

    const testBasicConnection = async () => {
        const startTime = Date.now();
        logTest('Basic Connection', 'start');

        try {
            const response = await fetch('http://localhost:8080/api/health', {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                signal: AbortSignal.timeout(5000)
            });

            if (response.ok) {
                const data = await response.json();
                const duration = Date.now() - startTime;
                logTest('Basic Connection', 'success', `Connected! Status: ${response.status} (${duration}ms)`, data);
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        } catch (error: any) {
            let message = 'Connection failed';
            if (error.name === 'AbortError') {
                message = 'Connection timeout - server not responding';
            } else if (error.message.includes('fetch')) {
                message = 'Cannot reach server - is Spring Boot running on port 8080?';
            } else {
                message = error.message;
            }
            logTest('Basic Connection', 'error', message);
        }
    };

    // ==================== EXERCISE API TESTS ====================

    const testExercisesEndpoint = async () => {
        logTest('Exercises Endpoint', 'start');

        try {
            const response = await fetch('http://localhost:8080/api/exercises/public', {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                signal: AbortSignal.timeout(5000)
            });

            if (response.ok) {
                const data = await response.json();
                const exercises = Array.isArray(data) ? data : (data.data || []);

                // Test workout tracking modes
                const cardioCount = exercises.filter((ex: any) => ex.isCardio).length;
                const isometricCount = exercises.filter((ex: any) => ex.isIsometric).length;
                const strengthCount = exercises.filter((ex: any) => !ex.isCardio && !ex.isIsometric).length;

                const hasIsCardio = exercises.length > 0 && 'isCardio' in exercises[0];
                const hasIsIsometric = exercises.length > 0 && 'isIsometric' in exercises[0];

                let message = `Found ${exercises.length} exercises`;
                if (hasIsCardio && hasIsIsometric) {
                    message += ` (❤️${cardioCount} cardio, 🛡️${isometricCount} isometric, 💪${strengthCount} strength)`;
                } else {
                    message += ` - Missing workout tracking fields!`;
                }

                logTest('Exercises Endpoint', hasIsCardio && hasIsIsometric ? 'success' : 'error', message, {
                    total: exercises.length,
                    cardio: cardioCount,
                    isometric: isometricCount,
                    strength: strengthCount,
                    hasIsCardio,
                    hasIsIsometric,
                    sample: exercises[0],
                    workoutTrackingSupport: hasIsCardio && hasIsIsometric ? 'Full Support' : 'Missing Fields'
                });
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        } catch (error: any) {
            logTest('Exercises Endpoint', 'error', error.message || 'Failed to fetch exercises');
        }
    };

    const testExerciseApiService = async () => {
        logTest('Exercise API Service', 'start');

        try {
            const { exerciseApi } = await import('../services/exerciseApi');
            const exercises = await exerciseApi.getPublicExercises();

            if (Array.isArray(exercises)) {
                const cardioExercises = exercises.filter(ex => ex.isCardio);
                const isometricExercises = exercises.filter(ex => ex.isIsometric);
                const strengthExercises = exercises.filter(ex => !ex.isCardio && !ex.isIsometric);

                const hasAllFields = exercises.length > 0 &&
                    'isCardio' in exercises[0] &&
                    'isIsometric' in exercises[0];

                const totalCategorized = cardioExercises.length + isometricExercises.length + strengthExercises.length;
                const isDataConsistent = totalCategorized === exercises.length;

                let message = `Service working! ${exercises.length} exercises loaded`;
                if (hasAllFields && isDataConsistent) {
                    message += ` with complete workout tracking support`;
                } else {
                    message += ` - workout tracking data issues detected`;
                }

                logTest('Exercise API Service', hasAllFields && isDataConsistent ? 'success' : 'error', message, {
                    total: exercises.length,
                    cardio: cardioExercises.length,
                    isometric: isometricExercises.length,
                    strength: strengthExercises.length,
                    hasIsCardioField: exercises.length > 0 && 'isCardio' in exercises[0],
                    hasIsIsometricField: exercises.length > 0 && 'isIsometric' in exercises[0],
                    dataConsistency: isDataConsistent ? 'Valid' : 'Invalid',
                    sample: exercises[0],
                    workoutTrackingModes: {
                        cardio: cardioExercises.slice(0, 2).map(ex => ex.name),
                        isometric: isometricExercises.slice(0, 2).map(ex => ex.name),
                        strength: strengthExercises.slice(0, 2).map(ex => ex.name)
                    }
                });
            } else {
                throw new Error('Exercise API did not return an array');
            }
        } catch (error: any) {
            logTest('Exercise API Service', 'error', error.message || 'Exercise API service failed');
        }
    };

    // ==================== WORKOUT PLAN API TESTS ====================

    const testWorkoutPlanEndpoints = async () => {
        logTest('Workout Plan Endpoints', 'start');

        const token = localStorage.getItem('auth_token');
        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };

        const endpoints = [
            { url: '/api/workout-plans', name: 'Public Plans', requiresAuth: false },
            { url: '/api/workout-plans/accessible', name: 'Accessible Plans', requiresAuth: true },
            { url: '/api/workout-plans/popular?limit=8', name: 'Popular Plans', requiresAuth: false },
            { url: '/api/workout-plans/trending?limit=6', name: 'Trending Plans', requiresAuth: false },
            { url: '/api/workout-plans/statistics', name: 'Statistics', requiresAuth: false }
        ];

        const results: any = {};
        let successCount = 0;
        let errorCount = 0;

        for (const endpoint of endpoints) {
            try {
                console.log(`🧪 Testing: ${endpoint.url}`);

                const requestHeaders = endpoint.requiresAuth ? headers : { 'Content-Type': 'application/json' };
                const response = await fetch(`http://localhost:8080${endpoint.url}`, { headers: requestHeaders });

                console.log(`📊 ${endpoint.name} Status: ${response.status}`);

                if (response.ok) {
                    const data = await response.json();
                    const count = Array.isArray(data) ? data.length :
                        Array.isArray(data.workoutPlans) ? data.workoutPlans.length :
                            'Not array';

                    results[endpoint.name] = {
                        status: 'success',
                        count: count,
                        sample: Array.isArray(data) && data.length > 0 ? data[0] :
                            data.workoutPlans && data.workoutPlans.length > 0 ? data.workoutPlans[0] :
                                data
                    };
                    successCount++;
                } else {
                    const errorText = await response.text();
                    results[endpoint.name] = {
                        status: 'error',
                        error: `${response.status} - ${errorText}`
                    };
                    errorCount++;
                }
            } catch (error: any) {
                results[endpoint.name] = {
                    status: 'error',
                    error: error.message
                };
                errorCount++;
            }
        }

        const message = `${successCount}/${endpoints.length} endpoints working. ${errorCount} failed.`;
        const overallStatus = successCount > errorCount ? 'success' : 'error';

        logTest('Workout Plan Endpoints', overallStatus, message, {
            summary: { successCount, errorCount, total: endpoints.length },
            endpointResults: results,
            authToken: !!token
        });
    };

    const testWorkoutPlanApiService = async () => {
        logTest('Workout Plan API Service', 'start');

        try {
            const { workoutPlanApi } = await import('../services/workoutPlanApi');

            // Test the main method your app uses
            const initialData = await workoutPlanApi.getInitialWorkoutPlanData();

            const allPlansCount = initialData.allPlans?.length || 0;
            const freePlansCount = initialData.freePlans?.length || 0;
            const popularPlansCount = initialData.popularPlans?.length || 0;
            const trendingPlansCount = initialData.trendingPlans?.length || 0;

            const message = `Loaded initial data: ${allPlansCount} all, ${freePlansCount} free, ${popularPlansCount} popular, ${trendingPlansCount} trending`;

            logTest('Workout Plan API Service', 'success', message, {
                counts: {
                    all: allPlansCount,
                    free: freePlansCount,
                    popular: popularPlansCount,
                    trending: trendingPlansCount
                },
                hasStatistics: !!initialData.statistics,
                samplePlan: initialData.allPlans?.[0] || initialData.freePlans?.[0] || null,
                dataStructure: {
                    hasAllPlans: !!initialData.allPlans,
                    hasFreePlans: !!initialData.freePlans,
                    hasPopularPlans: !!initialData.popularPlans,
                    hasTrendingPlans: !!initialData.trendingPlans,
                    hasStatistics: !!initialData.statistics
                }
            });
        } catch (error: any) {
            logTest('Workout Plan API Service', 'error', error.message || 'Workout Plan API service failed');
        }
    };

    const testIndividualWorkoutPlanEndpoints = async () => {
        logTest('Individual Workout Plan Methods', 'start');

        try {
            const { workoutPlanApi } = await import('../services/workoutPlanApi');

            const results: any = {};
            let successCount = 0;

            // Test individual methods
            const methods = [
                { name: 'getPublicWorkoutPlans', method: () => workoutPlanApi.getPublicWorkoutPlans() },
                { name: 'getPopularWorkoutPlans', method: () => workoutPlanApi.getPopularWorkoutPlans(5) },
                { name: 'getTrendingWorkoutPlans', method: () => workoutPlanApi.getTrendingWorkoutPlans(5) },
                { name: 'getWorkoutPlanStatistics', method: () => workoutPlanApi.getWorkoutPlanStatistics() }
            ];

            for (const { name, method } of methods) {
                try {
                    const result = await method();
                    const count = Array.isArray(result) ? result.length : 'Object';
                    results[name] = { status: 'success', count, sample: Array.isArray(result) ? result[0] : result };
                    successCount++;
                } catch (error: any) {
                    results[name] = { status: 'error', error: error.message };
                }
            }

            const message = `${successCount}/${methods.length} methods working`;
            logTest('Individual Workout Plan Methods', successCount > 0 ? 'success' : 'error', message, {
                successRate: `${successCount}/${methods.length}`,
                methodResults: results
            });
        } catch (error: any) {
            logTest('Individual Workout Plan Methods', 'error', error.message || 'Failed to test individual methods');
        }
    };

    // ==================== API CLIENT TESTS ====================

    const testApiClient = async () => {
        logTest('API Client Test', 'start');

        try {
            const { default: apiClient } = await import('../services/apiClient');
            const result = await apiClient.testConnection();

            if (result.status === 'success') {
                logTest('API Client Test', 'success', result.message, result);
            } else {
                throw new Error(result.message);
            }
        } catch (error: any) {
            logTest('API Client Test', 'error', error.message || 'API client not available or failed');
        }
    };

    // ==================== WORKOUT TRACKING MODE VALIDATION ====================

    const testWorkoutTrackingModes = async () => {
        logTest('Workout Tracking Modes', 'start');

        try {
            const { exerciseApi } = await import('../services/exerciseApi');
            const exercises = await exerciseApi.getPublicExercises();

            if (Array.isArray(exercises) && exercises.length > 0) {
                const testScenarios = {
                    cardioExercises: exercises.filter(ex => ex.isCardio && !ex.isIsometric),
                    isometricExercises: exercises.filter(ex => ex.isIsometric && !ex.isCardio),
                    strengthExercises: exercises.filter(ex => !ex.isCardio && !ex.isIsometric),
                    invalidExercises: exercises.filter(ex => ex.isCardio && ex.isIsometric)
                };

                const issues = [];
                if (testScenarios.invalidExercises.length > 0) {
                    issues.push(`${testScenarios.invalidExercises.length} exercises are both cardio AND isometric`);
                }

                if (testScenarios.cardioExercises.length === 0) {
                    issues.push('No cardio exercises found');
                }

                if (testScenarios.strengthExercises.length === 0) {
                    issues.push('No strength exercises found');
                }

                const isValid = issues.length === 0;

                logTest('Workout Tracking Modes', isValid ? 'success' : 'error',
                    isValid ? '✅ All workout tracking modes are properly configured!' : `⚠️ Issues: ${issues.join(', ')}`,
                    {
                        ...testScenarios,
                        totalExercises: exercises.length,
                        issues: issues,
                        validation: isValid ? 'PASSED' : 'FAILED'
                    }
                );
            } else {
                throw new Error('No exercises available for testing');
            }
        } catch (error: any) {
            logTest('Workout Tracking Modes', 'error', error.message || 'Workout tracking mode validation failed');
        }
    };

    // ==================== AUTHENTICATION TESTS ====================

    const testAuthentication = async () => {
        logTest('Authentication Status', 'start');

        try {
            const token = localStorage.getItem('auth_token');
            const hasToken = !!token;

            if (hasToken && token) {
                // Test token validity by trying an authenticated endpoint
                try {
                    // Test token validity using your API service instead of direct fetch
                    const accessiblePlans = await workoutPlanApi.getAccessibleWorkoutPlans();

                    logTest('Authentication Status', 'success',
                        `Valid authentication token (${accessiblePlans.length} accessible plans)`,
                        {
                            hasToken,
                            tokenLength: token.length,
                            accessiblePlansCount: accessiblePlans.length,
                            isValid: true
                        }
                    );
                    return; // Exit early on success
                } catch (error: any) {
                    const isAuthError = error.message.includes('401') || error.message.includes('403') || error.message.includes('auth');

                    logTest('Authentication Status', 'error',
                        isAuthError ? 'Invalid or expired token' : 'Authentication test failed',
                        {
                            hasToken,
                            tokenLength: token.length,
                            error: error.message,
                            isValid: false
                        }
                    );
                    return; // Exit early on error
                }
            } else {
                logTest('Authentication Status', 'error', 'No authentication token found', {
                    hasToken,
                    recommendation: 'Login required for full functionality'
                });
            }
        } catch (error: any) {
            logTest('Authentication Status', 'error', `Auth test failed: ${error.message}`);
        }
    };

    // ==================== TEST RUNNERS ====================

    const runAllTests = async () => {
        setIsRunning(true);
        clearTests();
        console.log('🚀 Starting comprehensive API tests...');

        try {
            // Core connectivity
            await testBasicConnection();
            await new Promise(resolve => setTimeout(resolve, 500));

            // Authentication
            await testAuthentication();
            await new Promise(resolve => setTimeout(resolve, 500));

            // Exercise tests
            await testExercisesEndpoint();
            await new Promise(resolve => setTimeout(resolve, 500));

            await testExerciseApiService();
            await new Promise(resolve => setTimeout(resolve, 500));

            await testWorkoutTrackingModes();
            await new Promise(resolve => setTimeout(resolve, 500));

            // Workout plan tests
            await testWorkoutPlanEndpoints();
            await new Promise(resolve => setTimeout(resolve, 500));

            await testWorkoutPlanApiService();
            await new Promise(resolve => setTimeout(resolve, 500));

            await testIndividualWorkoutPlanEndpoints();
            await new Promise(resolve => setTimeout(resolve, 500));

            // API client test
            await testApiClient();

        } catch (error) {
            console.error('❌ Test suite error:', error);
        } finally {
            setIsRunning(false);
            console.log('✅ All API tests completed');
        }
    };

    const runExerciseTests = async () => {
        setIsRunning(true);
        clearTests();
        console.log('🏃‍♂️ Running exercise-specific tests...');

        try {
            await testBasicConnection();
            await new Promise(resolve => setTimeout(resolve, 300));

            await testExercisesEndpoint();
            await new Promise(resolve => setTimeout(resolve, 300));

            await testExerciseApiService();
            await new Promise(resolve => setTimeout(resolve, 300));

            await testWorkoutTrackingModes();
        } finally {
            setIsRunning(false);
            console.log('✅ Exercise tests completed');
        }
    };

    const runWorkoutPlanTests = async () => {
        setIsRunning(true);
        clearTests();
        console.log('📋 Running workout plan-specific tests...');

        try {
            await testBasicConnection();
            await new Promise(resolve => setTimeout(resolve, 300));

            await testAuthentication();
            await new Promise(resolve => setTimeout(resolve, 300));

            await testWorkoutPlanEndpoints();
            await new Promise(resolve => setTimeout(resolve, 300));

            await testWorkoutPlanApiService();
            await new Promise(resolve => setTimeout(resolve, 300));

            await testIndividualWorkoutPlanEndpoints();
        } finally {
            setIsRunning(false);
            console.log('✅ Workout plan tests completed');
        }
    };

    // ==================== UI HELPERS ====================

    const getStatusIcon = (status: TestResult['status']) => {
        switch (status) {
            case 'success': return '✅';
            case 'error': return '❌';
            case 'pending': return '🔄';
            default: return '⚪';
        }
    };

    const getStatusColor = (status: TestResult['status']) => {
        switch (status) {
            case 'success': return '#d4edda';
            case 'error': return '#f8d7da';
            case 'pending': return '#fff3cd';
            default: return '#ffffff';
        }
    };

    const getTestSummary = () => {
        const total = tests.length;
        const successful = tests.filter(t => t.status === 'success').length;
        const failed = tests.filter(t => t.status === 'error').length;
        const pending = tests.filter(t => t.status === 'pending').length;

        return { total, successful, failed, pending };
    };

    const summary = getTestSummary();

    // ==================== RENDER ====================

    return (
        <div style={{
            padding: '20px',
            border: '2px solid #007bff',
            borderRadius: '12px',
            margin: '20px',
            backgroundColor: '#f8f9fa',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
        }}>
            {/* Header */}
            <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                <h2 style={{ margin: '0 0 10px 0', color: '#007bff' }}>
                    🧪 API Connectivity Test Panel
                </h2>
                <p style={{ margin: '0', color: '#6c757d', fontSize: '14px' }}>
                    Comprehensive testing for exercises and workout plans
                </p>
                {tests.length > 0 && (
                    <p style={{ margin: '10px 0 0 0', fontSize: '14px', fontWeight: 'bold' }}>
                        📊 Results: {summary.successful} ✅ | {summary.failed} ❌ | {summary.pending} 🔄
                    </p>
                )}
            </div>

            {/* Control Buttons */}
            <div style={{ marginBottom: '20px', textAlign: 'center', display: 'flex', flexWrap: 'wrap', gap: '8px', justifyContent: 'center' }}>
                <button
                    onClick={runAllTests}
                    disabled={isRunning}
                    style={{
                        padding: '12px 24px',
                        backgroundColor: isRunning ? '#6c757d' : '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: isRunning ? 'not-allowed' : 'pointer',
                        fontSize: '16px',
                        fontWeight: 'bold'
                    }}
                >
                    {isRunning ? '🔄 Running Tests...' : '🚀 Run All Tests'}
                </button>

                <button
                    onClick={runExerciseTests}
                    disabled={isRunning}
                    style={{
                        padding: '12px 20px',
                        backgroundColor: isRunning ? '#6c757d' : '#28a745',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: isRunning ? 'not-allowed' : 'pointer'
                    }}
                >
                    🏃‍♂️ Test Exercises
                </button>

                <button
                    onClick={runWorkoutPlanTests}
                    disabled={isRunning}
                    style={{
                        padding: '12px 20px',
                        backgroundColor: isRunning ? '#6c757d' : '#17a2b8',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: isRunning ? 'not-allowed' : 'pointer'
                    }}
                >
                    📋 Test Plans
                </button>

                <button
                    onClick={testBasicConnection}
                    disabled={isRunning}
                    style={{
                        padding: '12px 20px',
                        backgroundColor: isRunning ? '#6c757d' : '#ffc107',
                        color: 'black',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: isRunning ? 'not-allowed' : 'pointer'
                    }}
                >
                    🔗 Quick Test
                </button>

                <button
                    onClick={clearTests}
                    disabled={isRunning}
                    style={{
                        padding: '12px 20px',
                        backgroundColor: '#6c757d',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: isRunning ? 'not-allowed' : 'pointer'
                    }}
                >
                    🗑️ Clear
                </button>
            </div>

            {/* Results Display */}
            <div style={{
                maxHeight: '600px',
                overflowY: 'auto',
                border: '1px solid #dee2e6',
                backgroundColor: 'white',
                borderRadius: '8px',
                padding: '15px'
            }}>
                {tests.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '40px', color: '#6c757d' }}>
                        <p style={{ fontSize: '18px', margin: '0 0 10px 0' }}>🎯</p>
                        <p style={{ margin: '0' }}>No tests run yet.</p>
                        <p style={{ margin: '5px 0 0 0', fontSize: '14px' }}>
                            Choose a test suite to start testing your API connectivity.
                        </p>
                    </div>
                ) : (
                    tests.map((test, index) => (
                        <div
                            key={index}
                            style={{
                                marginBottom: '15px',
                                padding: '12px',
                                border: '1px solid #dee2e6',
                                borderRadius: '6px',
                                backgroundColor: getStatusColor(test.status)
                            }}
                        >
                            <div style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                marginBottom: '8px'
                            }}>
                                <div style={{ fontWeight: 'bold', fontSize: '16px' }}>
                                    {getStatusIcon(test.status)} {test.test}
                                </div>
                                {test.timestamp && (
                                    <span style={{ fontSize: '12px', color: '#6c757d' }}>
                                        {test.timestamp}
                                    </span>
                                )}
                            </div>

                            <div style={{ fontSize: '14px', color: '#495057', marginBottom: '8px' }}>
                                {test.message}
                            </div>

                            {test.data && (
                                <details style={{ marginTop: '8px' }}>
                                    <summary style={{
                                        cursor: 'pointer',
                                        color: '#007bff',
                                        fontSize: '14px',
                                        fontWeight: 'bold'
                                    }}>
                                        📊 Show Details
                                    </summary>
                                    <pre style={{
                                        fontSize: '12px',
                                        backgroundColor: '#f8f9fa',
                                        padding: '10px',
                                        borderRadius: '4px',
                                        overflow: 'auto',
                                        marginTop: '8px',
                                        border: '1px solid #dee2e6',
                                        maxHeight: '300px'
                                    }}>
                                        {JSON.stringify(test.data, null, 2)}
                                    </pre>
                                </details>
                            )}
                        </div>
                    ))
                )}
            </div>

            {/* Help Information */}
            <div style={{ marginTop: '20px', fontSize: '14px', color: '#6c757d' }}>
                <div style={{
                    backgroundColor: '#e7f3ff',
                    padding: '15px',
                    borderRadius: '6px',
                    border: '1px solid #b3d9ff'
                }}>
                    <p style={{ margin: '0 0 10px 0', fontWeight: 'bold', color: '#0056b3' }}>
                        🎯 What this tests:
                    </p>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '10px' }}>
                        <div>
                            <strong>🏃‍♂️ Exercise Tests:</strong>
                            <ul style={{ margin: '5px 0 0 0', paddingLeft: '20px', lineHeight: '1.4' }}>
                                <li>Basic connectivity to Spring Boot</li>
                                <li>Exercise endpoint functionality</li>
                                <li>Workout tracking modes (cardio/isometric/strength)</li>
                                <li>Exercise API service integration</li>
                            </ul>
                        </div>
                        <div>
                            <strong>📋 Workout Plan Tests:</strong>
                            <ul style={{ margin: '5px 0 0 0', paddingLeft: '20px', lineHeight: '1.4' }}>
                                <li>Authentication status</li>
                                <li>Public and authenticated endpoints</li>
                                <li>Popular and trending plans</li>
                                <li>API service integration</li>
                            </ul>
                        </div>
                    </div>
                    <p style={{ margin: '15px 0 5px 0', fontSize: '13px', fontStyle: 'italic' }}>
                        💡 Make sure your Spring Boot server is running on <code>http://localhost:8080</code>
                    </p>
                    <p style={{ margin: '5px 0 0 0', fontSize: '13px', fontStyle: 'italic' }}>
                        🔧 If workout plans fail with 403 errors, update your SecurityConfig.java to allow public access
                    </p>

                    {/* Quick Status Indicators */}
                    {tests.length > 0 && (
                        <div style={{ marginTop: '15px', padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px', border: '1px solid #dee2e6' }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '8px', color: '#495057' }}>📈 Quick Status:</div>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '8px', fontSize: '12px' }}>
                                <div style={{ color: summary.successful > 0 ? '#28a745' : '#6c757d' }}>
                                    ✅ Successful: {summary.successful}/{summary.total}
                                </div>
                                <div style={{ color: summary.failed > 0 ? '#dc3545' : '#6c757d' }}>
                                    ❌ Failed: {summary.failed}/{summary.total}
                                </div>
                                <div style={{ color: summary.pending > 0 ? '#ffc107' : '#6c757d' }}>
                                    🔄 Pending: {summary.pending}/{summary.total}
                                </div>
                                <div style={{ color: '#6c757d' }}>
                                    📊 Overall: {summary.total > 0 ? Math.round((summary.successful / summary.total) * 100) : 0}% success
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Common Issues & Solutions */}
                    <details style={{ marginTop: '15px' }}>
                        <summary style={{
                            cursor: 'pointer',
                            fontWeight: 'bold',
                            color: '#0056b3',
                            fontSize: '14px',
                            padding: '5px 0'
                        }}>
                            🛠️ Common Issues & Solutions
                        </summary>
                        <div style={{
                            marginTop: '10px',
                            padding: '12px',
                            backgroundColor: '#fff3cd',
                            borderRadius: '4px',
                            border: '1px solid #ffeaa7',
                            fontSize: '13px',
                            lineHeight: '1.4'
                        }}>
                            <div style={{ marginBottom: '10px' }}>
                                <strong>🔴 Backend Not Running:</strong>
                                <ul style={{ margin: '3px 0 0 0', paddingLeft: '20px' }}>
                                    <li>Start your Spring Boot server: <code>mvn spring-boot:run</code></li>
                                    <li>Check if port 8080 is available</li>
                                    <li>Verify database connection</li>
                                </ul>
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <strong>🔴 403 Forbidden on Workout Plans:</strong>
                                <ul style={{ margin: '3px 0 0 0', paddingLeft: '20px' }}>
                                    <li>Update SecurityConfig.java to allow public workout plan endpoints</li>
                                    <li>Add: <code>.requestMatchers("/api/workout-plans/**").permitAll()</code></li>
                                    <li>Restart your Spring Boot server</li>
                                </ul>
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <strong>🔴 Compilation Errors:</strong>
                                <ul style={{ margin: '3px 0 0 0', paddingLeft: '20px' }}>
                                    <li>Fix TestController.java: Add <code>SubscriptionTier.FREE</code> parameter</li>
                                    <li>Import: <code>import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;</code></li>
                                    <li>Run: <code>mvn clean compile</code></li>
                                </ul>
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <strong>🔴 Empty Workout Plans:</strong>
                                <ul style={{ margin: '3px 0 0 0', paddingLeft: '20px' }}>
                                    <li>Check if database migration ran: Look for V012__Add_Foundation_Exercises.sql</li>
                                    <li>Verify data: <code>SELECT COUNT(*) FROM workout_plans WHERE is_public = true;</code></li>
                                    <li>Run migration manually if needed</li>
                                </ul>
                            </div>

                            <div>
                                <strong>🔴 Authentication Issues:</strong>
                                <ul style={{ margin: '3px 0 0 0', paddingLeft: '20px' }}>
                                    <li>Login to get authentication token</li>
                                    <li>Check if token is expired</li>
                                    <li>Use public endpoints if not logged in</li>
                                </ul>
                            </div>
                        </div>
                    </details>

                    {/* Test Recommendations */}
                    {tests.length > 0 && summary.failed > 0 && (
                        <div style={{
                            marginTop: '15px',
                            padding: '12px',
                            backgroundColor: '#f8d7da',
                            borderRadius: '4px',
                            border: '1px solid #f5c6cb',
                            fontSize: '13px'
                        }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '8px', color: '#721c24' }}>
                                🚨 Action Required:
                            </div>
                            <div style={{ color: '#721c24' }}>
                                {summary.failed} test(s) failed. Check the detailed results above and follow the solutions in "Common Issues & Solutions".
                                <br />
                                💡 Tip: Start with "🔗 Quick Test" to verify basic connectivity first.
                            </div>
                        </div>
                    )}

                    {tests.length > 0 && summary.failed === 0 && summary.successful > 0 && (
                        <div style={{
                            marginTop: '15px',
                            padding: '12px',
                            backgroundColor: '#d4edda',
                            borderRadius: '4px',
                            border: '1px solid #c3e6cb',
                            fontSize: '13px'
                        }}>
                            <div style={{ fontWeight: 'bold', marginBottom: '8px', color: '#155724' }}>
                                🎉 All Tests Passed!
                            </div>
                            <div style={{ color: '#155724' }}>
                                Your API integration is working correctly. Both exercises and workout plans are functional.
                                <br />
                                🚀 Your frontend should now be able to load workout plans successfully!
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};