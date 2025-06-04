import React, { useState } from 'react';
import { Button } from '@mui/material';
import { useNavigate } from "react-router-dom";

const ExerciseLibrary = () => {
    const [activeGoal, setActiveGoal] = useState('fat-burn');
    const navigate = useNavigate();

    // Goal-based exercise preview (2 exercises per goal for teaser)
    const exercises = {
        'fat-burn': [
            {
                name: "HIIT Circuit",
                duration: "20 mins",
                difficulty: "Intermediate",
                emoji: "🔥",
                caloriesBurn: "400-600/hr",
                benefits: ["Burns calories fast", "Boosts metabolism"]
            },
            {
                name: "Tabata Training",
                duration: "15 mins",
                difficulty: "Advanced",
                emoji: "⚡",
                caloriesBurn: "350-500/hr",
                benefits: ["Maximum intensity", "Time efficient"]
            }
        ],
        'muscle-building': [
            {
                name: "Push Day Routine",
                duration: "45 mins",
                difficulty: "Intermediate",
                emoji: "💪",
                caloriesBurn: "250-400/hr",
                benefits: ["Builds upper body", "Increases strength"]
            },
            {
                name: "Compound Movements",
                duration: "60 mins",
                difficulty: "Advanced",
                emoji: "🏋️‍♂️",
                caloriesBurn: "300-450/hr",
                benefits: ["Full body strength", "Functional power"]
            }
        ],
        'endurance': [
            {
                name: "Steady-State Cardio",
                duration: "30-60 mins",
                difficulty: "Beginner",
                emoji: "🏃‍♂️",
                caloriesBurn: "300-500/hr",
                benefits: ["Improves cardio", "Low impact"]
            },
            {
                name: "CrossFit WOD",
                duration: "25 mins",
                difficulty: "Advanced",
                emoji: "⚡",
                caloriesBurn: "400-700/hr",
                benefits: ["Total body endurance", "Functional fitness"]
            }
        ],
        'flexibility': [
            {
                name: "Dynamic Flow Yoga",
                duration: "30 mins",
                difficulty: "Beginner",
                emoji: "🧘‍♀️",
                caloriesBurn: "150-250/hr",
                benefits: ["Improves mobility", "Stress relief"]
            },
            {
                name: "Active Recovery",
                duration: "20 mins",
                difficulty: "Beginner",
                emoji: "🤸‍♂️",
                caloriesBurn: "100-200/hr",
                benefits: ["Aids recovery", "Prevents injury"]
            }
        ]
    };

    // Goal categories with updated theming
    const goals = [
        { id: 'fat-burn', name: 'Fat Burn', emoji: '🔥', color: 'red', count: '45+' },
        { id: 'muscle-building', name: 'Muscle Building', emoji: '💪', color: 'blue', count: '120+' },
        { id: 'endurance', name: 'Endurance', emoji: '⚡', color: 'orange', count: '65+' },
        { id: 'flexibility', name: 'Flexibility', emoji: '🧘‍♀️', color: 'green', count: '40+' }
    ];

    const getDifficultyColor = (difficulty) => {
        switch (difficulty.toLowerCase()) {
            case 'beginner':
                return 'text-green-600';
            case 'intermediate':
                return 'text-amber-400';
            case 'advanced':
                return 'text-red-500';
            default:
                return 'text-text-muted';
        }
    };

    return (
        <section id="exercises" className="py-8 md:py-12 px-4 sm:px-6 lg:px-8 bg-gradient-to-b from-blue-50 to-gray-100 animate-on-scroll">
            <div className="max-w-7xl mx-auto">

                {/* Section Header */}
                <div className="text-center mb-12 md:mb-16">
                    <div className="mb-4">
                        <span className="inline-block px-4 py-2 bg-neon-green/10 border border-neon-green/20 rounded-full text-green-500 text-sm font-semibold">
                            🆓 Free Access - No Signup Required
                        </span>
                    </div>
                    <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-text-primary mb-6">
                        Find Workouts for{' '}
                        <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                            Your Goals
                        </span>
                    </h2>
                    <p className="text-lg md:text-xl text-text-muted max-w-3xl mx-auto">
                        Discover exercises tailored to your fitness goals. From fat burning to muscle building -
                        we have the workouts that get results.
                    </p>
                </div>

                {/* Goal Tabs */}
                <div className="flex flex-wrap justify-center gap-2 md:gap-4 mb-12">
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
                            <span className="hidden sm:inline">{goal.name}</span>
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

                {/* Exercise Preview Grid (Only 2 per goal) */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-12 max-w-4xl mx-auto">
                    {exercises[activeGoal].map((exercise, index) => (
                        <div
                            key={index}
                            className="group bg-light-card border border-light-border rounded-xl p-6 hover:border-electric-blue/30 hover:shadow-light-card-hover hover:shadow-electric-blue/10 transition-all duration-300 hover:transform hover:scale-105"
                        >
                            {/* Exercise Header */}
                            <div className="flex items-center justify-between mb-4">
                                <div className="text-3xl">{exercise.emoji}</div>
                                <span className={`text-xs px-2 py-1 rounded-full font-semibold ${getDifficultyColor(exercise.difficulty)} bg-gray-100`}>
                                    {exercise.difficulty}
                                </span>
                            </div>

                            {/* Exercise Name */}
                            <h3 className="text-lg font-semibold text-text-primary mb-3 group-hover:text-electric-blue transition-colors">
                                {exercise.name}
                            </h3>

                            {/* Quick Stats */}
                            <div className="space-y-2 mb-4">
                                <div className="flex justify-between text-sm">
                                    <span className="text-text-muted">Duration:</span>
                                    <span className="text-text-secondary font-medium">{exercise.duration}</span>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-text-muted">Calories:</span>
                                    <span className="text-electric-blue font-medium">{exercise.caloriesBurn}</span>
                                </div>
                            </div>

                            {/* Benefits */}
                            <div className="mb-4">
                                {exercise.benefits.map((benefit, i) => (
                                    <div key={i} className="flex items-center text-xs text-text-muted mb-1">
                                        <span className="text-neon-green mr-2">✓</span>
                                        {benefit}
                                    </div>
                                ))}
                            </div>

                            {/* Hover Action */}
                            <div className="opacity-0 group-hover:opacity-100 transition-opacity">
                                <Button
                                    size="small"
                                    variant="outlined"
                                    onClick={() => navigate(`/exercises/${exercise.category}#${exercise.name}`)}
                                    className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 text-xs w-full"
                                >
                                    View Full Workout
                                </Button>
                            </div>
                        </div>
                    ))}
                </div>

                {/* Enhanced CTA Section */}
                <div className="text-center">
                    <div className="bg-gradient-to-r from-electric-blue/10 to-neon-green/10 border border-electric-blue/20 rounded-2xl p-8 max-w-4xl mx-auto">
                        <h3 className="text-2xl font-bold text-text-primary mb-4">
                            Ready to Explore 500+ Goal-Based Workouts?
                        </h3>
                        <p className="text-text-muted mb-6">
                            Get access to our complete exercise library with HD video guides, detailed form tips,
                            and progressive workout plans for every fitness goal.
                        </p>

                        {/* Goal Summary */}
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
                            {goals.map((goal) => (
                                <div key={goal.id} className="text-center">
                                    <div className="text-2xl mb-2">{goal.emoji}</div>
                                    <div className="text-sm text-text-secondary font-medium">{goal.count} workouts</div>
                                </div>
                            ))}
                        </div>

                        <div className="flex flex-col sm:flex-row gap-4 justify-center">
                            <Button
                                variant="contained"
                                size="large"
                                onClick={() => navigate('/exercises')}
                                className="bg-gradient-to-r from-electric-blue to-neon-green text-white px-8 py-3 rounded-lg font-semibold hover:shadow-lg transform hover:scale-105 transition-all"
                            >
                                🔍 Browse Full Exercise Library
                            </Button>
                            <Button
                                variant="outlined"
                                size="large"
                                className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 px-8 py-3 rounded-lg font-semibold transition-all"
                            >
                                📱 Start Tracking Workouts
                            </Button>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default ExerciseLibrary;