import React, {useState, useEffect, useCallback} from 'react';
import {X, ArrowRight, ArrowLeft} from 'lucide-react';
import {TutorialStep} from '../../config/tutorialSteps';

interface GuidedTourProps {
    steps: TutorialStep[];
    onComplete: () => void;
    onSkip: () => void;
    petName?: string;
}

const GuidedTour: React.FC<GuidedTourProps> = ({
                                                   steps,
                                                   onComplete,
                                                   onSkip,
                                                   petName
                                               }) => {
    const [currentStepIndex, setCurrentStepIndex] = useState(0);
    const [highlightedElement, setHighlightedElement] = useState<HTMLElement | null>(null);
    const [isTransitioning, setIsTransitioning] = useState(false);
    const [isReady, setIsReady] = useState(false);

    const currentStep = steps[currentStepIndex];
    const isFirstStep = currentStepIndex === 0;
    const isLastStep = currentStepIndex === steps.length - 1;

    // Wait for page to fully load
    useEffect(() => {
        const prepare = async () => {
            await new Promise(resolve => setTimeout(resolve, 500));
            setIsReady(true);
        };
        prepare();
    }, []);

    // Highlight element and scroll it into view
    const highlightStep = useCallback(async (step: TutorialStep) => {
        setHighlightedElement(null);

        if (step.highlightSelector) {
            // Run pre-action if exists
            if (step.preAction) {
                try {
                    await step.preAction();
                    await new Promise(resolve => setTimeout(resolve, 400));
                } catch (error) {
                    console.error('Pre-action failed:', error);
                }
            }

            // Find the element
            const element = document.querySelector(step.highlightSelector) as HTMLElement;

            if (element) {
                console.log(`✅ Found element: ${step.highlightSelector}`);

                // Scroll element into view
                if (step.scrollIntoView !== false) {
                    // Special handling for FAB button - scroll to bottom!
                    if (step.highlightSelector === '.floating-action-button') {
                        window.scrollTo({
                            top: document.documentElement.scrollHeight,
                            behavior: 'smooth'
                        });
                        await new Promise(resolve => setTimeout(resolve, 800));
                    } else {
                        element.scrollIntoView({
                            behavior: 'smooth',
                            block: 'center',
                            inline: 'nearest'
                        });
                        await new Promise(resolve => setTimeout(resolve, 800));
                    }
                }

                setHighlightedElement(element);
            } else {
                console.warn(`❌ Element not found: ${step.highlightSelector}`);
            }
        }
    }, []);

    // Handle step changes
    useEffect(() => {
        if (!isReady) return;

        const initStep = async () => {
            setIsTransitioning(true);
            await highlightStep(currentStep);
            setIsTransitioning(false);
        };

        initStep();
    }, [currentStepIndex, currentStep, highlightStep, isReady]);

    // Get highlight position
    const getHighlightRect = () => {
        if (!highlightedElement) return null;

        const rect = highlightedElement.getBoundingClientRect();
        const padding = 8;

        return {
            top: rect.top - padding,
            left: rect.left - padding,
            width: rect.width + padding * 2,
            height: rect.height + padding * 2,
        };
    };

    // SMART CONTEXTUAL POSITIONING - Mobile-aware!
    const getModalStyle = (): React.CSSProperties => {
        if (!highlightedElement || currentStep.position === 'center') {
            return {
                position: 'fixed',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
            };
        }

        const rect = highlightedElement.getBoundingClientRect();
        const modalWidth = 360;
        const modalHeight = 300;
        const spacing = 16;
        const windowHeight = window.innerHeight;
        const windowWidth = window.innerWidth;
        const isMobile = windowWidth < 768;

        let style: React.CSSProperties = {
            position: 'fixed',
            maxWidth: '90vw',
        };

        // Calculate available space
        const spaceAbove = rect.top;
        const spaceBelow = windowHeight - rect.bottom;
        const spaceLeft = rect.left;
        const spaceRight = windowWidth - rect.right;

        // Calculate element's vertical position (0-1, where 0=top, 1=bottom)
        const elementVerticalPosition = (rect.top + rect.height / 2) / windowHeight;

        // MOBILE-SPECIFIC SMART POSITIONING
        if (isMobile) {
            // Simple rule: If element is in LOWER half → Modal at TOP
            //              If element is in UPPER half → Modal at BOTTOM
            if (elementVerticalPosition > 0.5) {
                // Element in lower half → Position modal at TOP
                style.top = '20px';
                style.left = '50%';
                style.transform = 'translateX(-50%)';
            } else {
                // Element in upper half → Position modal at BOTTOM
                style.bottom = '80px'; // Above bottom nav
                style.left = '50%';
                style.transform = 'translateX(-50%)';
            }
        }
        // DESKTOP POSITIONING - Try horizontal first
        else {
            if (spaceRight >= 300) {
                style.left = `${rect.right + spacing}px`;
                style.top = `${Math.max(20, Math.min(rect.top, windowHeight - modalHeight - 20))}px`;
            } else if (spaceLeft >= 300) {
                style.right = `${windowWidth - rect.left + spacing}px`;
                style.top = `${Math.max(20, Math.min(rect.top, windowHeight - modalHeight - 20))}px`;
            } else if (spaceBelow >= modalHeight + spacing) {
                style.top = `${rect.bottom + spacing}px`;
                style.left = '50%';
                style.transform = 'translateX(-50%)';
            } else if (spaceAbove >= modalHeight + spacing) {
                style.bottom = `${windowHeight - rect.top + spacing}px`;
                style.left = '50%';
                style.transform = 'translateX(-50%)';
            } else {
                style.top = '50%';
                style.left = '50%';
                style.transform = 'translate(-50%, -50%)';
            }
        }

        return style;
    };

    const handleNext = () => {
        if (isLastStep) {
            onComplete();
        } else {
            setCurrentStepIndex(prev => prev + 1);
        }
    };

    const handlePrevious = () => {
        if (!isFirstStep) {
            setCurrentStepIndex(prev => prev - 1);
        }
    };

    const handleKeyPress = useCallback((e: KeyboardEvent) => {
        if (e.key === 'Escape') {
            onSkip();
        } else if (e.key === 'Enter') {
            e.preventDefault();
            handleNext();
        }
    }, [onSkip, currentStepIndex]); // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        window.addEventListener('keydown', handleKeyPress);
        return () => window.removeEventListener('keydown', handleKeyPress);
    }, [handleKeyPress]);

    const highlightRect = getHighlightRect();

    if (!isReady) {
        return (
            <div className="fixed inset-0 bg-black/30 z-[9999] flex items-center justify-center backdrop-blur-sm">
                <div className="bg-white rounded-2xl p-6 shadow-2xl text-center">
                    <div
                        className="w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"/>
                    <p className="text-gray-700 font-medium">Starting tour...</p>
                </div>
            </div>
        );
    }

    return (
        <>
            {/* Dark overlay with SVG cutout - Starts below header! */}
            <svg
                className="fixed pointer-events-none"
                style={{
                    top: '64px',
                    left: 0,
                    right: 0,
                    bottom: 0,
                    width: '100%',
                    height: 'calc(100vh - 64px)',
                    zIndex: 9997
                }}
                preserveAspectRatio="none"
            >
                <defs>
                    <mask id="tour-spotlight">
                        <rect x="0" y="0" width="100%" height="100%" fill="white"/>
                        {highlightRect && (
                            <rect
                                x={highlightRect.left}
                                y={Math.max(0, highlightRect.top - 64)}
                                width={highlightRect.width}
                                height={highlightRect.height}
                                rx="16"
                                fill="black"
                            />
                        )}
                    </mask>
                </defs>
                <rect
                    x="0"
                    y="0"
                    width="100%"
                    height="100%"
                    fill="rgba(0, 0, 0, 0.6)"
                    mask="url(#tour-spotlight)"
                />
            </svg>

            {/* Highlight border */}
            {highlightRect && (
                <div
                    className="fixed pointer-events-none z-[9998] rounded-2xl border-4 border-purple-500 shadow-[0_0_0_4px_rgba(168,85,247,0.3),0_0_30px_rgba(168,85,247,0.6)]"
                    style={{
                        top: `${highlightRect.top}px`,
                        left: `${highlightRect.left}px`,
                        width: `${highlightRect.width}px`,
                        height: `${highlightRect.height}px`,
                        animation: 'pulse-border 2s ease-in-out infinite',
                    }}
                />
            )}

            {/* Tutorial Modal */}
            <div
                style={{
                    ...getModalStyle(),
                    zIndex: 9999,
                }}
                className={`
                    bg-white rounded-2xl shadow-2xl
                    p-3 sm:p-4
                    max-w-[90vw] w-[340px]
                    max-h-[70vh] sm:max-h-[85vh] overflow-y-auto
                    ${isTransitioning ? 'opacity-0 scale-95' : 'opacity-100 scale-100'}
                    transition-all duration-300
                `}
            >
                <div className="flex items-start justify-between mb-2 sm:mb-3">
                    <div className="flex-1">
                        <h3 className="text-lg sm:text-xl font-bold text-gray-900 mb-0.5 sm:mb-1">
                            {currentStep.title}
                        </h3>
                        <p className="text-xs sm:text-sm text-gray-600">
                            Step {currentStepIndex + 1} of {steps.length}
                        </p>
                    </div>
                    <button
                        onClick={onSkip}
                        className="text-gray-400 hover:text-gray-600 transition-colors p-1 flex-shrink-0 -mt-0.5"
                        aria-label="Exit tour"
                    >
                        <X className="w-4 h-4 sm:w-5 sm:h-5"/>
                    </button>
                </div>

                <p className="text-sm sm:text-base text-gray-700 mb-3 sm:mb-4 leading-snug sm:leading-relaxed">
                    {currentStep.description}
                </p>

                <div className="flex justify-center gap-1.5 sm:gap-2 mb-2 sm:mb-3">
                    {steps.map((_, index) => (
                        <div
                            key={index}
                            className={`h-1.5 sm:h-2 rounded-full transition-all duration-300 ${
                                index === currentStepIndex
                                    ? 'w-6 sm:w-8 bg-purple-500'
                                    : index < currentStepIndex
                                        ? 'w-1.5 sm:w-2 bg-purple-300'
                                        : 'w-1.5 sm:w-2 bg-gray-300'
                            }`}
                        />
                    ))}
                </div>

                <div className="flex items-center justify-between gap-2 sm:gap-3">
                    <button
                        onClick={handlePrevious}
                        disabled={isFirstStep}
                        className={`flex items-center gap-1 sm:gap-2 px-3 sm:px-4 py-1.5 sm:py-2 rounded-lg text-sm sm:text-base font-medium transition-colors ${
                            isFirstStep ? 'text-gray-400 cursor-not-allowed' : 'text-purple-600 hover:bg-purple-50'
                        }`}
                    >
                        <ArrowLeft className="w-3.5 h-3.5 sm:w-4 sm:h-4"/>
                        Back
                    </button>

                    <button
                        onClick={onSkip}
                        className="text-xs sm:text-sm text-gray-500 hover:text-gray-700 transition-colors px-1 sm:px-0"
                    >
                        Exit Tour
                    </button>

                    <button
                        onClick={handleNext}
                        className="flex items-center gap-1 sm:gap-2 px-4 sm:px-6 py-1.5 sm:py-2 bg-purple-500 text-white rounded-lg text-sm sm:text-base font-medium hover:bg-purple-600 transition-colors"
                    >
                        {isLastStep ? 'Finish' : 'Next'}
                        {!isLastStep && <ArrowRight className="w-3.5 h-3.5 sm:w-4 sm:h-4"/>}
                    </button>
                </div>
            </div>

            <style dangerouslySetInnerHTML={{
                __html: `
                    @keyframes pulse-border {
                        0%, 100% { opacity: 1; transform: scale(1); }
                        50% { opacity: 0.8; transform: scale(1.01); }
                    }
                `
            }}/>
        </>
    );
};

export default GuidedTour;