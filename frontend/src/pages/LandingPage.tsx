import React, { useEffect } from 'react';
import {
    Navigation,
    HeroSection,
    ProblemSection,
    SolutionSection,
    ExerciseLibrary,
    PricingSection,
    BetaAccess,
    FinalCTA
} from '../components/LandingPage';

// Temporary test - add to any existing component
import authService from '../services/authService';



// Types for Intersection Observer
interface IntersectionObserverOptions {
    threshold: number;
    rootMargin: string;
}

const LandingPage: React.FC = () => {
    useEffect(() => {
        // Scroll animations and progress bars
        const observerOptions: IntersectionObserverOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries: IntersectionObserverEntry[]) => {
            entries.forEach((entry: IntersectionObserverEntry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-fade-in-up');
                }
            });
        }, observerOptions);

        // Observe all sections for animation
        const animatedElements = document.querySelectorAll('.animate-on-scroll');
        animatedElements.forEach((el: Element) => {
            observer.observe(el);
        });

        // Cleanup function
        return (): void => {
            observer.disconnect();
        };
    }, []);

    return (
        <div className="min-h-screen bg-dark-bg">
            <Navigation />
            <HeroSection />
            <ProblemSection />
            <SolutionSection />
            <ExerciseLibrary />
            <PricingSection />
            <BetaAccess />
            <FinalCTA />
        </div>
    );
};

export default LandingPage;