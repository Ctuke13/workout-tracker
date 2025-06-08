import React, { useState, useEffect } from "react";
import { Button, TextField, InputAdornment, CircularProgress, Alert } from "@mui/material";
import { useNavigate } from "react-router-dom";

const ExercisesPage = () => {
    const navigate = useNavigate();

    // State for API data
    const [exercises, setExercises] = useState([]);
    const [goals, setGoals] = useState([]);
    const [equipmentOptions, setEquipmentOptions] = useState([]);
    const [difficultyOptions, setDifficultyOptions] = useState([]);

    // State for filters
    const [activeGoal, setActiveGoal] = useState('all');
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedEquipment, setSelectedEquipment] = useState('all');
    const [selectedDifficulty, setSelectedDifficulty] = useState('all');

    // UI State
    const [expandedCard, setExpandedCard] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

    // API base URL - adjust if your backend runs on a different port
    const API_BASE = 'http://localhost:8080/api/public/exercises';

    // Fetch initial data on component mount
    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                setLoading(true);
                setError(null);

                // Fetch all data in parallel
                const [exercisesRes, goalsRes, filterRes] = await Promise.all([
                    fetch(API_BASE),
                    fetch(`${API_BASE}/goals`),
                    fetch(`${API_BASE}/filters`)
                ]);

                if (!exercisesRes.ok || !goalsRes.ok || !filterRes.ok){
                    throw new Error('Failed to fetch data from server');
                }

                const [exerciseData, goalsData, filtersData] = await Promise.all([
                    exercisesRes.json(),
                    goalsRes.json(),
                    filterRes.json()
                ]);

                setExercises(exerciseData);

                // Add "All" option to goals and set proper format
                const formattedGoals = [
                    {id: 'all', name: 'All Goals', emoji: '🎯',  count: exerciseData.length},
                    ...goalsData.map(goal => ({
                        id: goal.goal,
                        name: formatGoalName(goal.goal),
                        emoji: getGoalEmoji(goal.goal),
                        count: goal.count
                    }))
                ];
                setGoals(formattedGoals);

                setEquipmentOptions(filtersData.equipment || []);
                setDifficultyOptions(filtersData.difficulties || []);

            } catch (err) {
                console.error("Error fetching data:", err);
                setError('Failed to load exercises. Please try again later.')
            } finally {
                setLoading(false);
            }
        };

        fetchInitialData();
    }, []);

    // Fetch filtered exercises when filters change
    useEffect(() => {
        const fetchFilteredExercises = async () => {
            if (loading) return;

            try {
                setError(null);
                let url = API_BASE;
                const params = new URLSearchParams();

                // Handle search
                if (searchTerm.trim()) {
                    url = `${API_BASE}/search`;
                    params.append('q', searchTerm.trim());

                    // Add filters to search
                    if (activeGoal !== 'all'){
                        params.append('goal', activeGoal);
                    }
                    if (selectedDifficulty !== 'all') {
                        params.append('difficulty', selectedDifficulty);
                    }
                    if (selectedEquipment !== 'all') {
                        params.append('equipment', selectedEquipment)
                    }
                } else {
                    // Handle filters only (no search)
                    if (activeGoal !== 'all'){
                        params.append('goal', activeGoal)
                    }
                    if (selectedDifficulty !== 'all') {
                        params.append('difficulty', selectedDifficulty);
                    }
                    if (selectedEquipment !== 'all') {
                        params.append('equipment', selectedEquipment);
                    }
                }

                if (params.toString()) {
                    url += `?${params.toString()}`
                }

                const response = await fetch(url);
                if (!response.ok) {
                    throw new Error('Failed to fetch filtered exercises');
                }

                const data = await response.json();
                setExercises(data);

            } catch (err) {
                console.error('Error fetching filtered exercises:', err);
                setError('Failed to load filtered exercises.');
            }
        };

        const timeoutId = setTimeout(fetchFilteredExercises, 300);
        return () => clearTimeout(timeoutId);
    }, [activeGoal, searchTerm, selectedEquipment, selectedDifficulty, loading]);


    // Helper functions
    const formatGoalName = (goal) => {
        const goalMap = {
            'fat-burn': 'Fat Burn',
            'muscle-building': 'Muscle Building',
            'endurance': 'Endurance',
            'flexibility': 'Flexibility',
            'sport-specific': 'Sport-Specific',
            'recovery': 'Recovery'
        };
        return goalMap[goal] || goal;
    };

    const getGoalEmoji = (goal) => {
        const emojiMap = {
            'fat-burn': '🔥',
            'muscle-building': '💪',
            'endurance': '⚡',
            'flexibility': '🧘‍♀️',
            'sport-specific': '🎯',
            'recovery': '🛡️'
        };
        return emojiMap[goal] || '🎯';
    };

    const getDifficultyColor = (difficulty) => {
        switch (difficulty.toLowerCase()) {
            case 'beginner':
                return 'text-neon-green bg-green-100';
            case 'intermediate':
                return 'text-orange-gradient-start bg-orange-100';
            case 'advanced':
                return 'text-red-500 bg-red-100';
            default:
                return 'text-text-muted bg-gray-100';
        }
    };

    const toggleExpanded = (index) => {
        setExpandedCard(expandedCard === index ? null : index);
    }

    // Clear all filters
    const clearFilters = () => {
        setActiveGoal('all');
        setSearchTerm('');
        setSelectedEquipment('all');
        setSelectedDifficulty('all');
    };

    // Add this helper function after your existing helper functions (around line 150)

    const formatEquipmentName = (equipment) => {
        if (equipment === 'None') {
            return 'No Equipment';
        }
        return equipment;
    };

// Update your generateResultsSummary function (around line 185)

    const generateResultsSummary = () => {
        const count = exercises.length;
        const parts = [];

        // Base text
        parts.push(`Showing <span class="font-semibold text-electric-blue">${count}</span>`);

        // Add difficulty if selected
        if (selectedDifficulty !== 'all') {
            parts.push(`<span class="text-orange-gradient-start font-semibold">${selectedDifficulty}</span>`);
        }

        // Add "exercises"
        parts.push(`exercise${count !== 1 ? 's' : ''}`);

        // Add equipment if selected (FIXED)
        if (selectedEquipment !== 'all') {
            parts.push(`using <span class="text-purple-600 font-semibold">${formatEquipmentName(selectedEquipment)}</span>`);
        }

        // Add goal if selected
        if (activeGoal !== 'all') {
            parts.push(`for <span class="text-electric-blue font-semibold">${formatGoalName(activeGoal)}</span>`);
        }

        // Add search term if present
        if (searchTerm.trim()) {
            parts.push(`matching <span class="text-neon-green font-semibold">"${searchTerm.trim()}"</span>`);
        }

        return parts.join(' ');
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-light-bg flex items-center justify-center">
                <div className="text-center">
                    <CircularProgress size={60} className="text-electric-blue" />
                    <p className="mt-4 text-text-muted">Loading Exercise Library...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-light-bg">
            {/* Navigation Header - Mobile Optimized */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-b border-light-border shadow-sm">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">
                        {/* Logo - Mobile Optimized */}
                        <div className="flex items-center">
                            <span className="text-xl sm:text-2xl font-bold text-electric-blue hover:text-electric-blue-dark transition-colors cursor-pointer">
                                💪 <span className="hidden xs:inline">WorkoutTracker</span>
                            </span>
                            <span className="ml-2 sm:ml-3 px-2 sm:px-3 py-1 text-xs bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end text-white rounded-full font-semibold animate-pulse">
                                BETA
                            </span>
                        </div>

                        {/* Mobile Menu Button */}
                        <div className="sm:hidden">
                            <button
                                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                                className="text-text-secondary hover:text-text-primary transition-colors p-2"
                            >
                                <span className="text-xl">☰</span>
                            </button>
                        </div>

                        {/* Desktop Navigation */}
                        <div className="hidden sm:flex items-center space-x-4">
                            <button
                                onClick={() => navigate('/')}
                                className="text-text-secondary hover:text-text-primary transition-colors"
                            >
                                ← Back to Home
                            </button>
                            <Button
                                variant="contained"
                                onClick={() => navigate('/#beta')}
                                className="bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end text-white px-6 py-2.5 rounded-lg font-semibold shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300"
                            >
                                Start Tracking FREE
                            </Button>
                        </div>
                    </div>

                    {/* Mobile Menu Dropdown */}
                    {isMobileMenuOpen && (
                        <div className="sm:hidden absolute top-16 left-0 right-0 bg-white border-b border-light-border shadow-lg">
                            <div className="px-4 py-4 space-y-3">
                                <button
                                    onClick={() => {
                                        navigate('/');
                                        setIsMobileMenuOpen(false);
                                    }}
                                    className="block w-full text-left text-text-secondary hover:text-text-primary transition-colors py-2"
                                >
                                    ← Back to Home
                                </button>
                                <Button
                                    variant="contained"
                                    fullWidth
                                    onClick={() => {
                                        navigate('/#beta');
                                        setIsMobileMenuOpen(false);
                                    }}
                                    className="bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end text-white py-3 rounded-lg font-semibold"
                                >
                                    🚀 Start Tracking FREE
                                </Button>
                            </div>
                        </div>
                    )}
                </div>
            </nav>

            {/* Main Content */}
            <main className="pt-20 pb-16">
                {/* Hero Section */}
                <section className="py-4 px-4 sm:px-6 lg:px-8 bg-light-bg">
                    <div className="max-w-7xl mx-auto text-center">
                        <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold text-text-primary mb-6">
                            Complete{' '}
                            <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                                Exercise Library
                            </span>
                        </h1>
                        <p className="text-base sm:text-lg md:text-xl text-text-muted max-w-3xl mx-auto">
                            Discover goal-based workouts with detailed instructions, form tips, and video guides.
                            Find the perfect exercises for your fitness journey.
                        </p>
                    </div>
                </section>

                {/* Error Alert */}
                {error && (
                    <section className="py-4 px-4 sm:px-6 lg:px-8">
                        <div className="max-w-7xl mx-auto">
                            <Alert
                                severity="error"
                                onClose={() => setError(null)}
                                className="mb-4"
                            >
                                {error}
                            </Alert>
                        </div>
                    </section>
                )}

                {/* Goal Selection - Mobile Optimized */}
                <section className="py-8 px-4 sm:px-6 lg:px-8 bg-light-bg-secondary">
                    <div className="max-w-7xl mx-auto">
                        <h2 className="text-lg sm:text-xl font-semibold text-text-primary mb-6 text-center">Choose Your Fitness Goal</h2>

                        {/* Mobile: Stacked Goal Buttons */}
                        <div className="sm:hidden space-y-2 mb-8">
                            {goals.map((goal) => (
                                <button
                                    key={goal.id}
                                    onClick={() => setActiveGoal(goal.id)}
                                    className={`w-full flex items-center justify-between px-4 py-3 rounded-lg font-semibold transition-all duration-300 ${
                                        activeGoal === goal.id
                                            ? 'bg-electric-blue text-white shadow-lg shadow-electric-blue/20'
                                            : 'bg-light-card text-text-secondary hover:text-text-primary hover:bg-light-card-hover border border-light-border'
                                    }`}
                                >
                                    <div className="flex items-center space-x-3">
                                        <span className="text-xl">{goal.emoji}</span>
                                        <span className="text-sm font-medium">{goal.name}</span>
                                    </div>
                                    <span className={`text-xs px-2 py-1 rounded-full font-semibold ${
                                        activeGoal === goal.id
                                            ? 'bg-white/20 text-white'
                                            : 'bg-electric-blue/10 text-electric-blue'
                                    }`}>
                                        {goal.count}
                                    </span>
                                </button>
                            ))}
                        </div>

                        {/* Desktop: Horizontal Goal Buttons */}
                        <div className="hidden sm:flex flex-wrap justify-center gap-2 md:gap-4 mb-8">
                            {goals.map((goal) => (
                                <button
                                    key={goal.id}
                                    onClick={() => setActiveGoal(goal.id)}
                                    className={`flex items-center space-x-2 px-4 md:px-6 py-3 rounded-lg font-semibold transition-all duration-300 transform hover:scale-105 ${
                                        activeGoal === goal.id
                                            ? 'bg-electric-blue text-white shadow-lg shadow-electric-blue/20'
                                            : 'bg-light-card text-text-secondary hover:text-text-primary hover:bg-light-card-hover border border-light-border'
                                    }`}
                                >
                                    <span className="text-xl">{goal.emoji}</span>
                                    <span>{goal.name}</span>
                                    <span className={`text-xs px-2 py-1 rounded-full ${
                                        activeGoal === goal.id
                                            ? 'bg-white/20 text-white'
                                            : 'bg-electric-blue/10 text-electric-blue'
                                    }`}>
                                        {goal.count}
                                    </span>
                                </button>
                            ))}
                        </div>

                        {/* Quick Filters - Mobile Optimized */}
                        <div className="bg-light-card border border-light-border rounded-xl p-4 sm:p-6 mb-8">
                            <div className="flex justify-between items-center mb-4">
                                <h3 className="text-base sm:text-lg font-semibold text-text-primary">Filter Exercises</h3>
                                <button
                                    onClick={clearFilters}
                                    className="text-xs sm:text-sm text-electric-blue hover:text-electric-blue-dark transition-colors"
                                >
                                    Clear All
                                </button>
                            </div>
                            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3 sm:gap-4">
                                {/* Search */}
                                <TextField
                                    fullWidth
                                    placeholder="Find exercises..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    variant="outlined"
                                    size="small"
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <span>🔍</span>
                                            </InputAdornment>
                                        )
                                    }}
                                />

                                {/* Equipment Filter */}
                                <select
                                    value={selectedEquipment}
                                    onChange={(e) => setSelectedEquipment(e.target.value)}
                                    className="px-3 py-2 border border-light-border rounded-lg bg-light-card text-text-primary focus:border-electric-blue focus:outline-none text-sm"
                                >
                                    <option value="all">All Equipment</option>
                                    {equipmentOptions.map(equipment => (
                                        <option key={equipment} value={equipment}>
                                            {equipment}
                                        </option>
                                    ))}
                                </select>

                                {/* Difficulty Filter */}
                                <select
                                    value={selectedDifficulty}
                                    onChange={(e) => setSelectedDifficulty(e.target.value)}
                                    className="px-3 py-2 border border-light-border rounded-lg bg-light-card text-text-primary focus:border-electric-blue focus:outline-none text-sm"
                                >
                                    <option value="all">All Difficulties</option>
                                    {difficultyOptions.map(difficulty => (
                                        <option key={difficulty} value={difficulty}>
                                            {difficulty}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Exercise Grid */}
                <section className="py-8 px-4 sm:px-6 lg:px-8 bg-light-bg">
                    <div className="max-w-7xl mx-auto">
                        {/* Results Summary */}
                        <div className="mb-6 text-center">
                            <p
                                className="text-sm sm:text-base text-text-muted"
                                dangerouslySetInnerHTML={{ __html: generateResultsSummary() }}
                            />
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 sm:gap-6">
                            {exercises.map((exercise, index) => (
                                <div
                                    key={exercise.id}
                                    className="group bg-light-bg-secondary border-2 border-dark-card rounded-xl p-4 sm:p-6 hover:border-electric-blue/30 hover:shadow-light-card-hover hover:shadow-electric-blue/90 transition-all duration-300"
                                >
                                    {/* Exercise Header */}
                                    <div className="flex items-center justify-between mb-4">
                                        <span className="text-2xl sm:text-3xl group-hover:scale-110 transition-transform">{exercise.emoji}</span>
                                        <span className={`text-xs px-2 py-1 rounded-full font-semibold ${getDifficultyColor(exercise.difficulty)}`}>
                                            {exercise.difficulty}
                                        </span>
                                    </div>

                                    {/* Exercise Name */}
                                    <h3 className="text-base sm:text-lg font-semibold text-text-primary mb-3 group-hover:text-electric-blue transition-colors">
                                        {exercise.name}
                                    </h3>

                                    {/* Quick Stats */}
                                    <div className="grid grid-cols-2 gap-3 mb-4 text-xs sm:text-sm">
                                        <div>
                                            <span className="text-text-muted">Duration:</span>
                                            <div className="font-medium text-text-secondary">{exercise.duration}</div>
                                        </div>
                                        <div>
                                            <span className="text-text-muted">Equipment:</span>
                                            <div className="font-medium text-text-secondary">{exercise.equipment}</div>
                                        </div>
                                        <div>
                                            <span className="text-text-muted">Calories:</span>
                                            <div className="font-medium text-electric-blue">{exercise.calories}</div>
                                        </div>
                                        <div>
                                            <span className="text-text-muted">Video:</span>
                                            <div className="font-medium text-orange-gradient-start">
                                                {exercise.videoUrl ? 'Available' : 'Coming Soon'}
                                            </div>
                                        </div>
                                    </div>

                                    {/* Benefits */}
                                    <div className="mb-4">
                                        <h4 className="text-xs sm:text-sm font-semibold text-text-primary mb-2">Key Benefits:</h4>
                                        <div className="space-y-1">
                                            {exercise.benefits.map((benefit, i) => (
                                                <div key={i} className="flex items-center text-xs text-text-muted">
                                                    <span className="text-neon-green mr-2">✓</span>
                                                    {benefit}
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    {/* Expandable Section */}
                                    {expandedCard === index && (
                                        <div className="mb-4 p-3 sm:p-4 bg-light-bg-secondary rounded-lg border border-light-border">
                                            <h4 className="text-xs sm:text-sm font-semibold text-text-primary mb-2">Form Tips:</h4>
                                            <div className="space-y-2">
                                                {exercise.tips.map((tip, i) => (
                                                    <div key={i} className="flex items-start text-xs text-text-secondary">
                                                        <span className="text-electric-blue mr-2 mt-0.5">•</span>
                                                        {tip}
                                                    </div>
                                                ))}
                                            </div>
                                            {exercise.description && (
                                                <div className="mt-3 p-3 bg-electric-blue/10 rounded-lg border border-electric-blue/20">
                                                    <p className="text-xs text-text-secondary">{exercise.description}</p>
                                                </div>
                                            )}
                                            <div className="mt-3 p-3 bg-orange-gradient-start/10 rounded-lg border border-orange-gradient-start/20">
                                                <div className="flex items-center text-xs text-orange-gradient-start font-medium">
                                                    <span className="mr-2">📹</span>
                                                    {exercise.videoUrl ? 'HD Video Guide Available' : 'HD Video Guide Coming Soon'}
                                                </div>
                                            </div>
                                        </div>
                                    )}

                                    {/* Action Buttons */}
                                    <div className="space-y-2">
                                        <Button
                                            variant="outlined"
                                            fullWidth
                                            size="small"
                                            onClick={() => toggleExpanded(index)}
                                            className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 text-xs sm:text-sm"
                                        >
                                            {expandedCard === index ? 'Hide Details' : 'View Form Tips'}
                                        </Button>
                                        <Button
                                            variant="contained"
                                            fullWidth
                                            size="small"
                                            onClick={() => navigate('/#beta')}
                                            className="bg-gradient-to-r from-electric-blue to-neon-green text-white text-xs sm:text-sm font-semibold hover:shadow-lg transform hover:scale-105 transition-all"
                                        >
                                            📱 Track This Workout
                                        </Button>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* No Results */}
                        {exercises.length === 0 && !loading && (
                            <div className="text-center py-12">
                                <div className="text-4xl mb-4">🔍</div>
                                <h3 className="text-lg sm:text-xl font-semibold text-text-primary mb-2">No exercises found</h3>
                                <p className="text-sm sm:text-base text-text-muted mb-4">Try adjusting your search or filters</p>
                                <Button
                                    variant="outlined"
                                    onClick={clearFilters}
                                    className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 text-sm"
                                >
                                    Clear All Filters
                                </Button>
                            </div>
                        )}
                    </div>
                </section>

                {/* Final CTA - Mobile Optimized */}
                <section className="py-12 sm:py-16 px-4 sm:px-6 lg:px-8 bg-gradient-to-r from-electric-blue/10 to-neon-green/10">
                    <div className="max-w-4xl mx-auto text-center">
                        <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-text-primary mb-6">
                            Ready to Start{' '}
                            <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                                Tracking Progress?
                            </span>
                        </h2>
                        <p className="text-base sm:text-lg text-text-secondary mb-8">
                            Join thousands of users already tracking their workouts and seeing real results with WorkoutTracker.
                        </p>
                        <div className="flex flex-col sm:flex-row gap-4 justify-center">
                            <Button
                                variant="contained"
                                size="large"
                                onClick={() => navigate('/#beta')}
                                className="bg-gradient-to-r from-electric-blue to-neon-green text-white px-8 sm:px-10 py-3 sm:py-4 rounded-lg font-semibold text-base sm:text-lg hover:shadow-xl transform hover:scale-105 transition-all"
                            >
                                🚀 Start Tracking FREE
                            </Button>
                            <Button
                                variant="outlined"
                                size="large"
                                onClick={() => navigate('/')}
                                className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 px-8 sm:px-10 py-3 sm:py-4 rounded-lg font-semibold text-base sm:text-lg transition-all"
                            >
                                ← Back to Home
                            </Button>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
};

export default ExercisesPage;