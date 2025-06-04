import React, { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";
import { Button } from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import TimerIcon from '@mui/icons-material/Timer';

const HeroSection = () => {
    const [animatedStats, setAnimatedStats] = useState({
        setsCompleted: 0,
        sessionVolume: 0,
        exercisesDone: 0
    });

    const navigate = useNavigate();

    useEffect(() => {
        // Animate session-focused stats
        const timer = setTimeout(() => {
            setAnimatedStats({
                setsCompleted: 12,     // Sets completed today
                sessionVolume: 2850,   // Weight moved today
                exercisesDone: 4       // Exercises done today
            });
        }, 500);

        return () => clearTimeout(timer);
    }, []);

    const scrollToBeta = () => {
        document.getElementById('beta')?.scrollIntoView({ behavior: 'smooth' });
    };

    return (
        <section className="pt-20 pb-16 md:pb-24 px-4 sm:px-6 lg:px-8 bg-light-bg animate-on-scroll">
            <div className="max-w-7xl mx-auto">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 items-center">

                    {/* Left Side - Hero Content */}
                    <div className="text-center lg:text-left">
                        <div className="mb-6">
                            <span className="inline-block px-4 py-2 bg-electric-blue/10 border border-electric-blue/20 rounded-full text-electric-blue text-sm font-semibold mb-4">
                                🚀 Join the Beta - Limited Access
                            </span>
                        </div>

                        <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold leading-tight mb-6 text-text-primary">
                            Finally, a{' '}
                            <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                                Clean Fitness App
                            </span>{' '}
                            That Actually Works
                        </h1>

                        <p className="text-lg md:text-xl text-text-muted mb-8 max-w-2xl mx-auto lg:mx-0">
                            Track your workouts with precision. Get insights that matter.
                            No ads, no clutter, no BS - just results.
                        </p>

                        {/* CTA Buttons */}
                        <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
                            <Button
                                variant="contained"
                                size="large"
                                onClick={scrollToBeta}
                                className="bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end text-white px-8 py-4 rounded-lg font-semibold text-lg hover:shadow-lg transform hover:scale-105 transition-all"
                                startIcon={<PlayArrowIcon />}
                            >
                                Start Tracking FREE
                            </Button>

                            <Button
                                variant="outlined"
                                size="large"
                                onClick={() => navigate('/exercises')}
                                className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 px-8 py-4 rounded-lg font-semibold text-lg transition-all"
                            >
                                View Exercise Library
                            </Button>
                        </div>

                        {/* Trust Indicators */}
                        <div className="mt-8 pt-8 border-t border-light-border">
                            <div className="flex flex-wrap justify-center lg:justify-start gap-6 text-sm text-text-muted">
                                <span>✅ No credit card required</span>
                                <span>✅ Your data stays yours</span>
                                <span>✅ Cancel anytime</span>
                            </div>
                        </div>
                    </div>

                    {/* Right Side - Phone Frame with Light Theme "During Workout" Preview */}
                    <div className="relative flex justify-center">
                        <div className="relative mx-auto max-w-sm">
                            <div className="relative bg-gray-300 rounded-[2.5rem] p-2 shadow-2xl ring-1 ring-gray-400/20">
                                <div className="bg-light-card rounded-[2rem] overflow-hidden">
                                    {/* Status Bar */}
                                    <div className="bg-light-bg-secondary px-6 py-3 flex justify-between items-center text-xs text-text-muted border-b border-light-border">
                                        <div className="flex items-center space-x-1">
                                            <span className="font-medium text-text-primary">9:41</span>
                                        </div>
                                        <div className="flex items-center space-x-1">
                                            <span className="text-electric-blue font-semibold">WorkoutTracker</span>
                                        </div>
                                        <div className="flex items-center space-x-1">
                                            <span>📶</span>
                                            <span>🔋</span>
                                            <span className="font-medium text-text-primary">100%</span>
                                        </div>
                                    </div>

                                    {/* App Content - During Workout (Light Theme) */}
                                    <div className="p-6 relative">
                                        {/* Achievement Notification */}
                                        <div className="absolute top-1 right-1 bg-gradient-to-r from-neon-green to-electric-blue px-2 py-1 rounded-md shadow-lg animate-pulse z-10">
                                            <div className="text-white text-center">
                                                <div className="text-xs font-bold">🏆</div>
                                                <div className="text-[10px] font-semibold leading-tight">New PR!</div>
                                            </div>
                                        </div>

                                        {/* Workout Header */}
                                        <div className="flex items-center justify-between mb-6">
                                            <h3 className="text-lg font-semibold text-text-primary">Push Day Workout</h3>
                                            <span className="px-2 py-1 bg-orange-gradient-start/20 text-orange-gradient-start text-xs rounded-full">
                                                In Progress
                                            </span>
                                        </div>

                                        {/* Session Progress */}
                                        <div className="mb-6">
                                            <div className="flex items-center justify-between mb-2">
                                                <span className="text-text-muted">Exercise 4 of 6</span>
                                                <span className="text-neon-green font-semibold">67%</span>
                                            </div>
                                            <div className="bg-light-bg-secondary rounded-full h-2">
                                                <div
                                                    className="bg-gradient-to-r from-neon-green to-electric-blue h-2 rounded-full transition-all duration-2000 ease-out"
                                                    style={{ width: '67%' }}
                                                ></div>
                                            </div>
                                        </div>

                                        {/* Session Stats Grid */}
                                        <div className="grid grid-cols-2 gap-3 mb-6">
                                            <div className="bg-light-bg-secondary rounded-lg p-3">
                                                <div className="flex items-center mb-1">
                                                    <FitnessCenterIcon className="text-electric-blue w-4 h-4 mr-2" />
                                                    <span className="text-text-muted text-xs">Sets Done</span>
                                                </div>
                                                <div className="text-xl font-bold text-text-primary">
                                                    {animatedStats.setsCompleted}
                                                </div>
                                            </div>

                                            <div className="bg-light-bg-secondary rounded-lg p-3">
                                                <div className="flex items-center mb-1">
                                                    <TrendingUpIcon className="text-neon-green w-4 h-4 mr-2" />
                                                    <span className="text-text-muted text-xs">Volume Today</span>
                                                </div>
                                                <div className="text-xl font-bold text-text-primary">
                                                    {animatedStats.sessionVolume.toLocaleString()}
                                                </div>
                                            </div>

                                            <div className="bg-light-bg-secondary rounded-lg p-3">
                                                <div className="flex items-center mb-1">
                                                    <TimerIcon className="text-orange-gradient-start w-4 h-4 mr-2" />
                                                    <span className="text-text-muted text-xs">Session Time</span>
                                                </div>
                                                <div className="text-xl font-bold text-text-primary">1h 15m</div>
                                            </div>

                                            <div className="bg-light-bg-secondary rounded-lg p-3">
                                                <div className="flex items-center mb-1">
                                                    <TrendingUpIcon className="text-electric-blue w-4 h-4 mr-2" />
                                                    <span className="text-text-muted text-xs">Exercises</span>
                                                </div>
                                                <div className="text-xl font-bold text-neon-green">
                                                    {animatedStats.exercisesDone}
                                                </div>
                                            </div>
                                        </div>

                                        {/* Current Exercise */}
                                        <div className="bg-gradient-to-r from-electric-blue/10 to-neon-green/10 border border-electric-blue/20 rounded-lg p-4">
                                            <div className="flex items-center justify-between">
                                                <div>
                                                    <div className="text-text-primary font-semibold">Bench Press</div>
                                                    <div className="text-text-muted text-sm">Set 3 of 4 • 185 lbs</div>
                                                </div>
                                                <Button
                                                    variant="contained"
                                                    size="small"
                                                    className="bg-electric-blue hover:bg-electric-blue-dark text-white rounded-lg text-xs"
                                                >
                                                    Complete Set
                                                </Button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Preview Badge */}
                            <div className="absolute -bottom-6 left-1/2 transform -translate-x-1/2">
                                <span className="px-4 py-2 bg-electric-blue/20 text-gray-800 text-sm rounded-full border border-electric-blue/30 font-medium backdrop-blur-sm">
                                    📱 During Workout
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default HeroSection;