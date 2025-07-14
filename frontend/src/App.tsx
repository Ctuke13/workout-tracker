import React, { useEffect } from 'react';
import Navigation from '../src/components/LandingPage/Navigation';
import HeroSection from '../src/components/LandingPage/HeroSection';
import ProblemSection from '../src/components/LandingPage/ProblemSection';
import SolutionSection from '../src/components/LandingPage/SolutionSection';
import ExerciseLibrary from '../src/components/LandingPage/ExerciseLibrary';
import PricingSection from '../src/components/LandingPage/PricingSection';
import BetaAccess from '../src/components/LandingPage/BetaAccess';
import FinalCTA from '../src/components/LandingPage/FinalCTA';
import { BrowserRouter } from 'react-router-dom';

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
        <BrowserRouter>
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
        </BrowserRouter>
    );
};

export default LandingPage;