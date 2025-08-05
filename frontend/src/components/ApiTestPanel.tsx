// src/components/ApiTestPanel.tsx - Enhanced with isIsometric testing

import React, { useState } from 'react';

interface TestResult {
    test: string;
    status: 'pending' | 'success' | 'error';
    message: string;
    data?: any;
    timestamp?: string;
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

    // Test 1: Basic Connection
    const testBasicConnection = async () => {
        addTestResult({ test: 'Basic Connection', status: 'pending', message: 'Testing...' });

        try {
            console.log('🧪 Testing basic connection to Spring Boot...');
            const response = await fetch('http://localhost:8080/api/health', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                },
                // Add timeout
                signal: AbortSignal.timeout(5000)
            });

            if (response.ok) {
                const data = await response.json();
                console.log('✅ Basic connection successful:', data);
                addTestResult({
                    test: 'Basic Connection',
                    status: 'success',
                    message: `Connected! Status: ${response.status}`,
                    data: data
                });
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        } catch (error: any) {
            console.error('❌ Basic connection failed:', error);
            let message = 'Connection failed';

            if (error.name === 'AbortError') {
                message = 'Connection timeout - server not responding';
            } else if (error.message.includes('fetch')) {
                message = 'Cannot reach server - is Spring Boot running on port 8080?';
            } else {
                message = error.message;
            }

            addTestResult({
                test: 'Basic Connection',
                status: 'error',
                message: message
            });
        }
    };

    // Test 2: Public Exercises Endpoint - ENHANCED with isIsometric testing
    const testExercisesEndpoint = async () => {
        addTestResult({ test: 'Exercises Endpoint', status: 'pending', message: 'Testing...' });

        try {
            console.log('🧪 Testing exercises endpoint...');
            const response = await fetch('http://localhost:8080/api/exercises/public', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                },
                signal: AbortSignal.timeout(5000)
            });

            if (response.ok) {
                const data = await response.json();
                console.log('✅ Exercises endpoint successful:', data);

                // Check if it's an array and has exercises
                const exercises = Array.isArray(data) ? data : (data.data || []);

                // ENHANCED: Test all three workout tracking modes
                const cardioCount = exercises.filter((ex: any) => ex.isCardio).length;
                const isometricCount = exercises.filter((ex: any) => ex.isIsometric).length;
                const strengthCount = exercises.filter((ex: any) => !ex.isCardio && !ex.isIsometric).length;

                // Check for required fields
                const hasIsCardio = exercises.length > 0 && 'isCardio' in exercises[0];
                const hasIsIsometric = exercises.length > 0 && 'isIsometric' in exercises[0];

                let message = `Found ${exercises.length} exercises`;
                if (hasIsCardio && hasIsIsometric) {
                    message += ` (❤️${cardioCount} cardio, 🛡️${isometricCount} isometric, 💪${strengthCount} strength)`;
                } else {
                    message += ` - Missing workout tracking fields!`;
                }

                addTestResult({
                    test: 'Exercises Endpoint',
                    status: hasIsCardio && hasIsIsometric ? 'success' : 'error',
                    message: message,
                    data: {
                        total: exercises.length,
                        cardio: cardioCount,
                        isometric: isometricCount,
                        strength: strengthCount,
                        hasIsCardio: hasIsCardio,
                        hasIsIsometric: hasIsIsometric,
                        sample: exercises[0],
                        workoutTrackingSupport: hasIsCardio && hasIsIsometric ? 'Full Support' : 'Missing Fields'
                    }
                });
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        } catch (error: any) {
            console.error('❌ Exercises endpoint failed:', error);
            addTestResult({
                test: 'Exercises Endpoint',
                status: 'error',
                message: error.message || 'Failed to fetch exercises'
            });
        }
    };

    // Test 3: Test with your API Client (if available)
    const testWithApiClient = async () => {
        addTestResult({ test: 'API Client Test', status: 'pending', message: 'Testing...' });

        try {
            console.log('🧪 Testing with API client...');

            // Dynamic import to avoid build errors if not available
            const { default: apiClient } = await import('../services/apiClient');
            const result = await apiClient.testConnection();

            if (result.status === 'success') {
                addTestResult({
                    test: 'API Client Test',
                    status: 'success',
                    message: result.message,
                    data: result
                });
            } else {
                throw new Error(result.message);
            }
        } catch (error: any) {
            console.error('❌ API client test failed:', error);
            addTestResult({
                test: 'API Client Test',
                status: 'error',
                message: error.message || 'API client not available or failed'
            });
        }
    };

    // Test 4: Test Exercise API Service - ENHANCED with workout tracking modes
    const testExerciseApiService = async () => {
        addTestResult({ test: 'Exercise API Service', status: 'pending', message: 'Testing...' });

        try {
            console.log('🧪 Testing Exercise API Service...');

            // Dynamic import to avoid build errors if not available
            const { exerciseApi } = await import('../services/exerciseApi');
            const exercises = await exerciseApi.getPublicExercises();

            if (Array.isArray(exercises)) {
                // ENHANCED: Test all workout tracking modes
                const cardioExercises = exercises.filter(ex => ex.isCardio);
                const isometricExercises = exercises.filter(ex => ex.isIsometric);
                const strengthExercises = exercises.filter(ex => !ex.isCardio && !ex.isIsometric);

                // Verify the fields exist and data makes sense
                const hasAllFields = exercises.length > 0 &&
                    'isCardio' in exercises[0] &&
                    'isIsometric' in exercises[0];

                // Check for data consistency
                const totalCategorized = cardioExercises.length + isometricExercises.length + strengthExercises.length;
                const isDataConsistent = totalCategorized === exercises.length;

                let message = `Service working! ${exercises.length} exercises loaded`;
                if (hasAllFields && isDataConsistent) {
                    message += ` with complete workout tracking support`;
                } else {
                    message += ` - workout tracking data issues detected`;
                }

                addTestResult({
                    test: 'Exercise API Service',
                    status: hasAllFields && isDataConsistent ? 'success' : 'error',
                    message: message,
                    data: {
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
                    }
                });
            } else {
                throw new Error('Exercise API did not return an array');
            }
        } catch (error: any) {
            console.error('❌ Exercise API service failed:', error);
            addTestResult({
                test: 'Exercise API Service',
                status: 'error',
                message: error.message || 'Exercise API service failed'
            });
        }
    };

    // NEW Test 5: Workout Tracking Mode Validation
    const testWorkoutTrackingModes = async () => {
        addTestResult({ test: 'Workout Tracking Modes', status: 'pending', message: 'Testing...' });

        try {
            console.log('🧪 Testing workout tracking mode validation...');

            const { exerciseApi } = await import('../services/exerciseApi');
            const exercises = await exerciseApi.getPublicExercises();

            if (Array.isArray(exercises) && exercises.length > 0) {
                // Test specific workout tracking scenarios
                const testScenarios = {
                    cardioExercises: exercises.filter(ex => ex.isCardio && !ex.isIsometric),
                    isometricExercises: exercises.filter(ex => ex.isIsometric && !ex.isCardio),
                    strengthExercises: exercises.filter(ex => !ex.isCardio && !ex.isIsometric),
                    invalidExercises: exercises.filter(ex => ex.isCardio && ex.isIsometric) // Should be empty
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

                addTestResult({
                    test: 'Workout Tracking Modes',
                    status: isValid ? 'success' : 'error',
                    message: isValid
                        ? `✅ All workout tracking modes are properly configured!`
                        : `⚠️ Issues found: ${issues.join(', ')}`,
                    data: {
                        ...testScenarios,
                        totalExercises: exercises.length,
                        issues: issues,
                        validation: isValid ? 'PASSED' : 'FAILED'
                    }
                });
            } else {
                throw new Error('No exercises available for testing');
            }
        } catch (error: any) {
            console.error('❌ Workout tracking mode test failed:', error);
            addTestResult({
                test: 'Workout Tracking Modes',
                status: 'error',
                message: error.message || 'Workout tracking mode validation failed'
            });
        }
    };

    const runAllTests = async () => {
        setIsRunning(true);
        clearTests();
        console.log('🚀 Starting comprehensive API tests...');

        await testBasicConnection();
        await new Promise(resolve => setTimeout(resolve, 500)); // Small delay between tests

        await testExercisesEndpoint();
        await new Promise(resolve => setTimeout(resolve, 500));

        await testWithApiClient();
        await new Promise(resolve => setTimeout(resolve, 500));

        await testExerciseApiService();
        await new Promise(resolve => setTimeout(resolve, 500));

        // NEW: Test workout tracking modes
        await testWorkoutTrackingModes();

        setIsRunning(false);
        console.log('✅ All API tests completed');
    };

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
            <div style={{ textAlign: 'center', marginBottom: '20px' }}>
                <h2 style={{ margin: '0 0 10px 0', color: '#007bff' }}>
                    🧪 API Connectivity Test Panel
                </h2>
                <p style={{ margin: '0', color: '#6c757d', fontSize: '14px' }}>
                    Test your Spring Boot backend integration with workout tracking modes
                </p>
            </div>

            <div style={{ marginBottom: '20px', textAlign: 'center' }}>
                <button
                    onClick={runAllTests}
                    disabled={isRunning}
                    style={{
                        padding: '12px 24px',
                        marginRight: '10px',
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
                    onClick={testBasicConnection}
                    disabled={isRunning}
                    style={{
                        padding: '12px 20px',
                        marginRight: '10px',
                        backgroundColor: isRunning ? '#6c757d' : '#28a745',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: isRunning ? 'not-allowed' : 'pointer'
                    }}
                >
                    🔗 Quick Test
                </button>

                <button
                    onClick={testWorkoutTrackingModes}
                    disabled={isRunning}
                    style={{
                        padding: '12px 20px',
                        marginRight: '10px',
                        backgroundColor: isRunning ? '#6c757d' : '#17a2b8',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: isRunning ? 'not-allowed' : 'pointer'
                    }}
                >
                    🎯 Test Tracking
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

            <div style={{
                maxHeight: '500px',
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
                            Click "Run All Tests" to start testing your API connectivity and workout tracking modes.
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
                                        border: '1px solid #dee2e6'
                                    }}>
                                        {JSON.stringify(test.data, null, 2)}
                                    </pre>
                                </details>
                            )}
                        </div>
                    ))
                )}
            </div>

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
                    <ul style={{ margin: '0', paddingLeft: '20px', lineHeight: '1.5' }}>
                        <li><strong>🔗 Basic Connection:</strong> Can we reach your Spring Boot server?</li>
                        <li><strong>💪 Exercises Endpoint:</strong> Does /api/exercises/public work with isIsometric field?</li>
                        <li><strong>🛠️ API Client:</strong> Is your apiClient.ts working?</li>
                        <li><strong>🎯 Exercise Service:</strong> Are isCardio and isIsometric fields working?</li>
                        <li><strong>🏃‍♂️ Workout Tracking:</strong> Are all three modes (cardio/isometric/strength) properly configured?</li>
                    </ul>
                    <p style={{ margin: '10px 0 0 0', fontSize: '13px', fontStyle: 'italic' }}>
                        💡 Make sure your Spring Boot server is running on <code>http://localhost:8080</code> with the V003 migration applied
                    </p>
                </div>
            </div>
        </div>
    );
};