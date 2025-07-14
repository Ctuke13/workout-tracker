// components/index.ts - Master barrel export

// Landing Page Components
export * from './LandingPage';

// Exercise Page Components
export * from './ExercisePage';

// Shared Components (add these as you create them)
// export * from './Shared';
// export * from './Layout';
// export * from './Forms';
// export * from './UI';

// Re-export commonly used components for convenience
export {
    Navigation,
    HeroSection,
    ProblemSection,
    SolutionSection,
    ExerciseLibrary,
    PricingSection,
    BetaAccess,
    FinalCTA
} from './LandingPage';

export {
    DesktopFilters,
    ExerciseCard,
    MobileFilterDrawer
} from './ExercisePage';