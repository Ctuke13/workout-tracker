import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@mui/material';

const Navigation = () => {
    const navigate = useNavigate();
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const scrollToSection = (sectionId) => {
        const element = document.getElementById(sectionId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
        }
        setIsMenuOpen(false);
    };

    const goToExercises = () => {
        navigate('/exercises');
        setIsMenuOpen(false);
    };

    return (
        <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-b border-light-border shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo */}
                    <div className="flex items-center">
                        <span
                            onClick={() => navigate('/')}
                            className="text-2xl font-bold text-electric-blue hover:text-electric-blue-dark transition-colors cursor-pointer"
                        >
                            💪 WorkoutTracker
                        </span>
                        <span className="ml-3 px-3 py-1 text-xs bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end text-white rounded-full font-semibold animate-pulse">
                            BETA
                        </span>
                    </div>

                    {/* Desktop Navigation */}
                    <div className="hidden md:flex items-center space-x-1">
                        <button
                            onClick={() => scrollToSection('problem')}
                            className="relative px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group"
                        >
                            Why We Built This
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-electric-blue transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>
                        <button
                            onClick={() => scrollToSection('pricing')}
                            className="relative px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group"
                        >
                            Pricing
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-electric-blue transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>
                        {/* Direct Exercise Library Link */}
                        <button
                            onClick={goToExercises}
                            className="relative px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group flex items-center space-x-1"
                        >
                            <span>📚</span>
                            <span>Exercise Library</span>
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-neon-green transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>
                        <button
                            onClick={() => scrollToSection('beta')}
                            className="relative px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group"
                        >
                            Early Access
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-neon-green transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>
                    </div>

                    {/* CTA Button */}
                    <div className="hidden md:flex items-center">
                        <Button
                            variant="contained"
                            onClick={() => scrollToSection('beta')}
                            className="bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end hover:from-orange-gradient-end hover:to-orange-gradient-start text-white px-6 py-2.5 rounded-lg font-semibold shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300"
                        >
                            Join Beta FREE
                        </Button>
                    </div>

                    {/* Custom Animated Hamburger Menu Button */}
                    <div className="md:hidden">
                        <button
                            onClick={() => setIsMenuOpen(!isMenuOpen)}
                            className="relative w-8 h-8 flex flex-col justify-center items-center space-y-1 group"
                            aria-label="Toggle menu"
                        >
                            <span
                                className={`block w-6 h-0.5 bg-text-primary transition-all duration-300 transform group-hover:bg-electric-blue ${
                                    isMenuOpen ? 'rotate-45 translate-y-1.5' : ''
                                }`}
                            ></span>
                            <span
                                className={`block w-6 h-0.5 bg-text-primary transition-all duration-300 group-hover:bg-electric-blue ${
                                    isMenuOpen ? 'opacity-0' : ''
                                }`}
                            ></span>
                            <span
                                className={`block w-6 h-0.5 bg-text-primary transition-all duration-300 transform group-hover:bg-electric-blue ${
                                    isMenuOpen ? '-rotate-45 -translate-y-1.5' : ''
                                }`}
                            ></span>
                        </button>
                    </div>
                </div>

                {/* Enhanced Mobile Menu */}
                <div className={`md:hidden transition-all duration-300 ease-in-out ${
                    isMenuOpen
                        ? 'max-h-96 opacity-100 visible'
                        : 'max-h-0 opacity-0 invisible overflow-hidden'
                }`}>
                    <div className="bg-light-card/95 backdrop-blur-md rounded-xl mt-3 mb-4 border border-light-border shadow-light-card">
                        <div className="flex flex-col p-4 space-y-1">
                            <button
                                onClick={() => scrollToSection('problem')}
                                className="text-left px-4 py-3 text-text-secondary hover:text-text-primary hover:bg-light-card-hover rounded-lg transition-all duration-200 flex items-center"
                            >
                                <span className="w-2 h-2 bg-electric-blue rounded-full mr-3 opacity-0 hover:opacity-100 transition-opacity"></span>
                                Why We Built This
                            </button>

                            {/* Direct Exercise Library Link for Mobile */}
                            <button
                                onClick={goToExercises}
                                className="text-left px-4 py-3 text-text-secondary hover:text-text-primary hover:bg-light-card-hover rounded-lg transition-all duration-200 flex items-center"
                            >
                                <span className="w-2 h-2 bg-neon-green rounded-full mr-3 opacity-0 hover:opacity-100 transition-opacity"></span>
                                <span className="mr-2">📚</span>
                                Exercise Library
                            </button>

                            <button
                                onClick={() => scrollToSection('pricing')}
                                className="text-left px-4 py-3 text-text-secondary hover:text-text-primary hover:bg-light-card-hover rounded-lg transition-all duration-200 flex items-center"
                            >
                                <span className="w-2 h-2 bg-electric-blue rounded-full mr-3 opacity-0 hover:opacity-100 transition-opacity"></span>
                                Pricing
                            </button>
                            <button
                                onClick={() => scrollToSection('beta')}
                                className="text-left px-4 py-3 text-text-secondary hover:text-text-primary hover:bg-light-card-hover rounded-lg transition-all duration-200 flex items-center"
                            >
                                <span className="w-2 h-2 bg-neon-green rounded-full mr-3 opacity-0 hover:opacity-100 transition-opacity"></span>
                                Early Access
                            </button>

                            {/* Mobile CTA Button */}
                            <div className="pt-4 mt-4 border-t border-light-border">
                                <Button
                                    variant="contained"
                                    fullWidth
                                    onClick={() => scrollToSection('beta')}
                                    className="bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end hover:from-orange-gradient-end hover:to-orange-gradient-start text-white py-3 rounded-lg font-semibold shadow-lg transform hover:scale-105 transition-all duration-300"
                                >
                                    🚀 Join Beta FREE
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navigation;