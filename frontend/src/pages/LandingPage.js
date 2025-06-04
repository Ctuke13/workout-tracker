import React, { useEffect } from 'react';
import Navigation from '../components/Navigation';
import HeroSection from '../components/HeroSection';
import ProblemSection from '../components/ProblemSection';
import SolutionSection from '../components/SolutionSection';
import ExerciseLibrary from '../components/ExerciseLibrary';
import PricingSection from '../components/PricingSection';
import BetaAccess from '../components/BetaAccess';
import FinalCTA from '../components/FinalCTA';

const LandingPage = () => {
    useEffect(() => {
        // Scroll animations and progress bars
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animate-fade-in-up');
                }
            });
        }, observerOptions);

        // Observe all sections for animation
        document.querySelectorAll('.animate-on-scroll').forEach(el => {
            observer.observe(el);
        });

        return () => observer.disconnect();
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