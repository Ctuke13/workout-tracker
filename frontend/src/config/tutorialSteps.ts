// ==================================================
// src/config/tutorialSteps.ts
// Enhanced Tutorial step definitions with scroll & pre-actions
// ==================================================

export interface TutorialStep {
    id: number;
    title: string;
    description: string;
    highlightSelector?: string; // CSS selector for element to highlight
    position?: 'top' | 'bottom' | 'center';
    scrollIntoView?: boolean; // Auto-scroll to element
    preAction?: () => Promise<void> | void; // Action to run before showing step
}

// ==================================================
// PET PAGE TUTORIAL
// ==================================================

export const PET_TUTORIAL_STEPS: TutorialStep[] = [
    {
        id: 1,
        title: '👋 Welcome to EvoPet!',
        description: `This is your pet companion! Take care of them by keeping their stats healthy and they'll motivate you on your fitness journey.`,
        position: 'center',
        scrollIntoView: false, // Center modal, no scroll needed
    },
    {
        id: 2,
        title: '🐺 Your Pet',
        description: `This is your baby wolf! Watch them react to your actions with cute animations. Complete workouts to earn crystals and keep them happy!`,
        highlightSelector: '.pet-room-container',
        position: 'bottom',
        scrollIntoView: true,
    },
    {
        id: 3,
        title: '💎 Crystals',
        description: `Crystals are your currency! Earn them by completing workouts. Use them to feed your pet and keep them healthy.`,
        highlightSelector: '.crystal-counter',
        position: 'bottom',
        scrollIntoView: true,
    },
    {
        id: 4,
        title: '📊 This Week\'s Progress',
        description: `Track your weekly workout goals here. See your streak, XP earned, and progress toward your weekly target!`,
        highlightSelector: '.todays-activity-card',
        position: 'top',
        scrollIntoView: true,
    },
    {
        id: 5,
        title: '📈 Pet Stats',
        description: `Your pet has 4 needs: Fuel, Motivation, Fatigue, and Cleanliness. Keep them balanced to keep your pet happy!`,
        highlightSelector: '.compact-stats-container',
        position: 'top',
        scrollIntoView: true,
        preAction: async () => {
            // Expand CompactStats before highlighting
            const statsElement = document.querySelector('.compact-stats-container button');
            if (statsElement instanceof HTMLElement) {
                const isExpanded = document.querySelector('.compact-stats-container .animate-fadeIn');
                if (!isExpanded) {
                    statsElement.click();
                    // Wait for animation
                    await new Promise(resolve => setTimeout(resolve, 350));
                }
            }
        },
    },
    {
        id: 6,
        title: '✨ Actions',
        description: `Tap here to feed, motivate, or bathe your pet! Each action costs crystals or has cooldowns. Take good care of your companion!`,
        highlightSelector: '.floating-action-button',
        position: 'top',
        scrollIntoView: true,
    },
];

// ==================================================
// CALENDAR PAGE TUTORIAL
// ==================================================

export const CALENDAR_TUTORIAL_STEPS: TutorialStep[] = [
    {
        id: 1,
        title: '📅 Welcome to Your Workout Calendar!',
        description: `Plan and track your workouts here. Schedule exercises, view your progress, and stay organized on your fitness journey!`,
        position: 'center',
        scrollIntoView: false,
    },
    {
        id: 2,
        title: '📆 Date Navigation',
        description: `Navigate between days, jump to today, or refresh your calendar. See your daily workout count and duration at a glance!`,
        highlightSelector: '.date-header',
        position: 'bottom',
        scrollIntoView: true,
    },
    {
        id: 3,
        title: '🗓️ Week Overview',
        description: `See your entire week at a glance! Green dots show completed exercises, blue dots show planned ones. Tap any day to jump to it.`,
        highlightSelector: '.week-calendar',
        position: 'top',
        scrollIntoView: true,
    },
    {
        id: 4,
        title: '➕ Add Exercises',
        description: `Add exercises to your workout plan! You can add individual exercises or entire workout plans with multiple exercises.`,
        highlightSelector: '.add-exercise-button',
        position: 'top',
        scrollIntoView: true,
    },
    {
        id: 5,
        title: '💪 Exercise Cards',
        description: `Each card shows an exercise with sets, reps, and weight. Tap to see details, edit, or mark as complete during your workout!`,
        highlightSelector: '.exercise-card:first-child',
        position: 'top',
        scrollIntoView: true,
    },
    {
        id: 6,
        title: '▶️ Start Your Workout',
        description: `When you're ready to train, hit this button to enter workout mode! Track sets, reps, rest times, and earn crystals for your pet!`,
        highlightSelector: '.start-workout-button',
        position: 'top',
        scrollIntoView: true,
    },
];