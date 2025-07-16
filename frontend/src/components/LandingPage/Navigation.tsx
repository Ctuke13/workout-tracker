import React, {JSX, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@mui/material';

// Types
interface NavigationProps {}

interface NavItem {
    id: string;
    label: string;
    onClick: () => void;
    icon?: string;
}

const Navigation: React.FC<NavigationProps> = () => {
    const navigate = useNavigate();
    const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false);

    // Navigation handlers
    const goToLogin = (): void => {
        navigate('/login');
        setIsMenuOpen(false);
    };

    const goToRegister = (): void => {
        navigate('/register');
        setIsMenuOpen(false);
    };

    const scrollToSection = (sectionId: string): void => {
        const element = document.getElementById(sectionId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth' });
        }
        setIsMenuOpen(false);
    };

    const goToExercises = (): void => {
        navigate('/exercises');
        setIsMenuOpen(false);
    };

    const goToHome = (): void => {
        navigate('/');
        setIsMenuOpen(false);
    };

    const goToBeta = (): void => {
        scrollToSection('beta');
    };

    // Navigation items configuration
    const navigationItems: NavItem[] = [
        {
            id: 'problem',
            label: 'Why We Built This',
            onClick: () => scrollToSection('problem')
        },
        {
            id: 'pricing',
            label: 'Pricing',
            onClick: () => scrollToSection('pricing')
        },
        {
            id: 'exercises',
            label: 'Exercise Library',
            onClick: goToExercises,
            icon: '📚'
        },
        {
            id: 'beta',
            label: 'Early Access',
            onClick: () => scrollToSection('beta')
        }
    ];

    const renderNavButton = (item: NavItem, isMobile: boolean = false): JSX.Element => (
        <button
            key={item.id}
            onClick={item.onClick}
            className={`${
                isMobile
                    ? 'w-full text-left px-4 py-3 text-text-secondary hover:text-text-primary hover:bg-light-card-hover rounded-lg transition-all duration-200 flex items-center min-h-[48px] max-h-[48px]'
                    : 'relative px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group min-h-[40px] flex items-center'
            }`}
        >
            {isMobile && (
                <span className={`w-2 h-2 ${item.id === 'exercises' || item.id === 'beta' ? 'bg-neon-green' : 'bg-electric-blue'} rounded-full mr-3 opacity-0 hover:opacity-100 transition-opacity flex-shrink-0`}></span>
            )}
            {item.icon && <span className="mr-2 flex-shrink-0">{item.icon}</span>}
            <span className="text-sm sm:text-base font-medium whitespace-nowrap overflow-hidden text-ellipsis">{item.label}</span>
            {!isMobile && (
                <span className={`absolute bottom-0 left-1/2 w-0 h-0.5 ${item.id === 'exercises' || item.id === 'beta' ? 'bg-neon-green' : 'bg-electric-blue'} transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2`}></span>
            )}
        </button>
    );

    return (
        <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 backdrop-blur-md border-b border-light-border shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo */}
                    <div className="flex items-center">
                        <span
                            onClick={goToHome}
                            className="text-2xl font-bold text-electric-blue hover:text-electric-blue-dark transition-colors cursor-pointer"
                        >
                            💪 WorkoutTracker
                        </span>
                        <span className="ml-3 px-3 py-1 text-xs bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end text-white rounded-full font-semibold animate-pulse">
                            BETA
                        </span>
                    </div>

                    {/* Desktop Navigation */}
                    <div className="hidden md:flex items-center space-x-0 lg:space-x-1">
                        {/* Always show these core items */}
                        <button
                            onClick={() => scrollToSection('problem')}
                            className="relative px-2 lg:px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group min-h-[40px] flex items-center text-sm lg:text-base"
                        >
                            <span className="font-medium whitespace-nowrap">Why This</span>
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-electric-blue transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>

                        {/* Hide on medium, show on large+ */}
                        <button
                            onClick={() => scrollToSection('pricing')}
                            className="relative px-2 lg:px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group min-h-[40px] flex items-center text-sm lg:text-base hidden lg:flex"
                        >
                            <span className="font-medium whitespace-nowrap">Pricing</span>
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-electric-blue transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>

                        <button
                            onClick={goToExercises}
                            className="relative px-2 lg:px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group min-h-[40px] flex items-center text-sm lg:text-base"
                        >
                            <span className="mr-1 lg:mr-2 text-sm">📚</span>
                            <span className="font-medium whitespace-nowrap hidden lg:inline">Exercise Library</span>
                            <span className="font-medium whitespace-nowrap lg:hidden">Exercises</span>
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-neon-green transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>

                        {/* Hide on medium, show on large+ */}
                        <button
                            onClick={() => scrollToSection('beta')}
                            className="relative px-2 lg:px-4 py-2 text-text-secondary hover:text-text-primary hover:bg-light-bg-secondary hover:font-bold transition-all duration-300 rounded-lg group min-h-[40px] flex items-center text-sm lg:text-base hidden lg:flex"
                        >
                            <span className="font-medium whitespace-nowrap">Early Access</span>
                            <span className="absolute bottom-0 left-1/2 w-0 h-0.5 bg-neon-green transition-all duration-300 group-hover:w-3/4 transform -translate-x-1/2"></span>
                        </button>
                    </div>

                    {/* CTA Button */}
                    <div className="hidden md:flex items-center space-x-1 lg:space-x-3">
                        <button
                            onClick={goToLogin}
                            className="px-2 lg:px-4 py-2 text-text-secondary hover:text-text-primary rounded-lg transition-colors font-medium min-h-[40px] flex items-center text-sm lg:text-base whitespace-nowrap"
                        >
                            Login
                        </button>
                        <Button
                            variant="contained"
                            onClick={goToRegister}
                            className="bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end hover:from-orange-gradient-end hover:to-orange-gradient-start text-white rounded-lg font-semibold shadow-lg hover:shadow-xl transform hover:scale-105 transition-all duration-300"
                            sx={{
                                minHeight: '40px',
                                maxHeight: '40px',
                                fontSize: { md: '12px', lg: '16px' },
                                padding: { md: '8px 12px', lg: '10px 24px' },
                                whiteSpace: 'nowrap'
                            }}
                        >
                            <span className="hidden lg:inline">Join Beta FREE</span>
                            <span className="lg:hidden">Join Beta</span>
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
                            {navigationItems.map((item) => renderNavButton(item, true))}

                            {/* Mobile CTA Button */}
                            <div className="pt-4 mt-4 border-t border-light-border">
                                <div className="space-y-2">
                                    <Button
                                        variant="outlined"
                                        fullWidth
                                        onClick={goToLogin}
                                        className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 py-3 rounded-lg font-semibold transition-all"
                                    >
                                        Login
                                    </Button>
                                    <Button
                                        variant="contained"
                                        fullWidth
                                        onClick={goToRegister}
                                        className="bg-gradient-to-r from-orange-gradient-start to-orange-gradient-end hover:from-orange-gradient-end hover:to-orange-gradient-start text-white py-3 rounded-lg font-semibold shadow-lg transform hover:scale-105 transition-all duration-300"
                                    >
                                        🚀 Join Beta FREE
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navigation;