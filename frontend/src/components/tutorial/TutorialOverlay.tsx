import React, {useState, useEffect, useCallback, useRef} from 'react';
import {X, ArrowRight, ArrowLeft} from 'lucide-react';
import {TutorialStep} from '../../config/tutorialSteps';

interface TutorialOverlayProps {
    steps: TutorialStep[];
    onComplete: () => void;
    onSkip: () => void;
    petName?: string;
}

const TutorialOverlay: React.FC<TutorialOverlayProps> = ({
                                                             steps,
                                                             onComplete,
                                                             onSkip,
                                                             petName
                                                         }) => {
    const [currentStepIndex, setCurrentStepIndex] = useState(0);
    const [highlightedElement, setHighlightedElement] = useState<HTMLElement | null>(null);
    const [isTransitioning, setIsTransitioning] = useState(false);
    const scrollPositionRef = useRef(0);

    const currentStep = steps[currentStepIndex];
    const isFirstStep = currentStepIndex === 0;
    const isLastStep = currentStepIndex === steps.length - 1;

    // Scroll to top FIRST, THEN lock scroll - prevents alignment issues when replaying tutorial
    useEffect(() => {
        // STEP 1: Scroll to top immediately (before any locking)
        window.scrollTo({
            top: 0,
            behavior: 'instant'
        });

        // STEP 2: Small delay to ensure scroll completes, then lock
        const lockTimeout = setTimeout(() => {
            // Now we're at top, save this position
            scrollPositionRef.current = 0;

            // Save original styles
            const originalOverflow = document.body.style.overflow;
            const originalPosition = document.body.style.position;
            const originalTop = document.body.style.top;
            const originalWidth = document.body.style.width;

            // Get scrollbar width
            const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;

            // Lock scroll with position fixed at top (0px)
            document.body.style.overflow = 'hidden';
            document.body.style.position = 'fixed';
            document.body.style.top = '0px'; // Always start at top
            document.body.style.width = '100%';
            document.body.style.paddingRight = `${scrollbarWidth}px`;
            document.body.style.minHeight = '100vh';
            document.body.style.height = '100vh';

            // Prevent wheel scroll
            const preventScroll = (e: WheelEvent) => {
                e.preventDefault();
            };

            // Prevent touch scroll
            const preventTouchScroll = (e: TouchEvent) => {
                if (e.touches.length > 1) return; // Allow pinch zoom
                e.preventDefault();
            };

            // Prevent keyboard scroll
            const preventKeyScroll = (e: KeyboardEvent) => {
                const scrollKeys = ['ArrowUp', 'ArrowDown', 'PageUp', 'PageDown', 'Home', 'End', ' '];
                if (scrollKeys.includes(e.key)) {
                    e.preventDefault();
                }
            };

            // Add event listeners
            window.addEventListener('wheel', preventScroll, {passive: false});
            window.addEventListener('touchmove', preventTouchScroll, {passive: false});
            window.addEventListener('keydown', preventKeyScroll, {passive: false});

            // Store cleanup function
            const cleanup = () => {
                // Remove event listeners
                window.removeEventListener('wheel', preventScroll);
                window.removeEventListener('touchmove', preventTouchScroll);
                window.removeEventListener('keydown', preventKeyScroll);

                // Restore styles
                document.body.style.overflow = originalOverflow;
                document.body.style.position = originalPosition;
                document.body.style.top = originalTop;
                document.body.style.width = originalWidth;
                document.body.style.paddingRight = '';

                // Restore scroll position (should stay at 0)
                window.scrollTo(0, scrollPositionRef.current);
            };

            // Return cleanup for this timeout
            return cleanup;
        }, 100); // Wait 100ms for scroll to complete

        // Cleanup on unmount
        return () => {
            clearTimeout(lockTimeout);
        };
    }, []);

    // Calculate optimal scroll to show FULL element
    const calculateOptimalScroll = useCallback((element: HTMLElement) => {
        const rect = element.getBoundingClientRect();
        const elementTop = rect.top + scrollPositionRef.current;
        const elementHeight = rect.height;
        const windowHeight = window.innerHeight;

        // Buffers
        const topBuffer = 120;  // Space for header + breathing room
        const bottomBuffer = 80; // Space at bottom + room for popup

        // Calculate ideal position: element centered with buffers
        const idealScroll = elementTop - topBuffer;

        // Check if element would be cut off at bottom
        const elementBottom = elementTop + elementHeight;
        const viewportBottom = idealScroll + windowHeight;

        if (elementBottom + bottomBuffer > viewportBottom) {
            // Element is cut off, scroll more to show it fully
            return Math.max(0, elementBottom + bottomBuffer - windowHeight);
        }

        return Math.max(0, idealScroll);
    }, []);

    // Scroll to position by updating body's top style
    const scrollToPosition = useCallback(async (targetScroll: number) => {
        scrollPositionRef.current = targetScroll;
        document.body.style.top = `-${targetScroll}px`;

        // Wait for paint
        await new Promise(resolve => setTimeout(resolve, 50));
    }, []);

    // Wait for scroll animation
    const waitForScroll = () => new Promise(resolve => setTimeout(resolve, 600));

    // Highlight element for current step
    const highlightStep = useCallback(async (step: TutorialStep) => {
        // Clear previous highlight
        setHighlightedElement(null);

        // If step has a selector, find and highlight the element
        if (step.highlightSelector) {
            // Run pre-action if it exists
            if (step.preAction) {
                try {
                    await step.preAction();
                } catch (error) {
                    console.error('Pre-action failed:', error);
                }
            }

            // Wait longer for content to fully render
            await new Promise(resolve => setTimeout(resolve, 400)); // Increased from 100ms to 400ms

// Find the element
            const element = document.querySelector(step.highlightSelector) as HTMLElement;

            if (element) {
                // Wait AGAIN for the element's content to paint
                await new Promise(resolve => setTimeout(resolve, 200));

                // Calculate optimal scroll position
                if (step.scrollIntoView !== false) {
                    const optimalScroll = calculateOptimalScroll(element);
                    await scrollToPosition(optimalScroll);
                    await waitForScroll();
                }

                // Highlight it
                setHighlightedElement(element);
            }
        }
    }, [calculateOptimalScroll, scrollToPosition]);

    // Handle step changes
    useEffect(() => {
        const initStep = async () => {
            setIsTransitioning(true);
            await highlightStep(currentStep);
            setIsTransitioning(false);
        };

        initStep();
    }, [currentStepIndex, currentStep, highlightStep]);

    // Get highlighted element position for cutout
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

    // Smart popup positioning
    const getPopupStyle = (): React.CSSProperties => {
        if (!highlightedElement || currentStep.position === 'center') {
            return {
                position: 'fixed',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                maxWidth: '90%',
                width: '400px',
                maxHeight: '80vh',
                overflowY: 'auto',
            };
        }

        const rect = highlightedElement.getBoundingClientRect();
        const popupWidth = 400;
        const popupHeight = 300;
        const spacing = 20;
        const windowHeight = window.innerHeight;
        const windowWidth = window.innerWidth;

        let style: React.CSSProperties = {
            position: 'fixed',
            maxWidth: '90%',
            width: `${popupWidth}px`,
            maxHeight: '80vh',
            overflowY: 'auto',
        };

        // Calculate available space
        const spaceBelow = windowHeight - rect.bottom;
        const spaceAbove = rect.top;

        // Determine best position
        if (currentStep.position === 'bottom' && spaceBelow > popupHeight + spacing) {
            // Below
            style.top = `${Math.min(rect.bottom + spacing, windowHeight - popupHeight - 20)}px`;
            style.left = '50%';
            style.transform = 'translateX(-50%)';
        } else if (currentStep.position === 'top' && spaceAbove > popupHeight + spacing) {
            // Above
            style.bottom = `${windowHeight - rect.top + spacing}px`;
            style.left = '50%';
            style.transform = 'translateX(-50%)';
        } else if (spaceBelow > popupHeight + spacing) {
            // Default to below if space available
            style.top = `${rect.bottom + spacing}px`;
            style.left = '50%';
            style.transform = 'translateX(-50%)';
        } else if (spaceAbove > popupHeight + spacing) {
            // Above if space available
            style.bottom = `${windowHeight - rect.top + spacing}px`;
            style.left = '50%';
            style.transform = 'translateX(-50%)';
        } else {
            // Fallback: centered
            style.top = '50%';
            style.left = '50%';
            style.transform = 'translate(-50%, -50%)';
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
        // Only handle tutorial navigation keys
        if (e.key === 'Escape') {
            onSkip();
        } else if (e.key === 'Enter') {
            e.preventDefault();
            handleNext();
        }
        // Note: Arrow keys are blocked by scroll prevention
    }, [onSkip, currentStepIndex]); // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        window.addEventListener('keydown', handleKeyPress);
        return () => window.removeEventListener('keydown', handleKeyPress);
    }, [handleKeyPress]);

    const highlightRect = getHighlightRect();

    return (
        <>
            {/* SVG overlay with cutout */}
            <svg
                className="fixed inset-0 pointer-events-none"
                style={{
                    zIndex: 9997,
                    width: '100vw',
                    height: '100vh',
                    top: 0,
                    left: 0
                }}
                preserveAspectRatio="none"
            >
                <defs>
                    <mask id="tutorial-mask">
                        <rect x="0" y="0" width="100%" height="100%" fill="white"/>
                        {highlightRect && (
                            <rect
                                x={highlightRect.left}
                                y={highlightRect.top}
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
                    fill="rgba(0, 0, 0, 0.7)"
                    mask="url(#tutorial-mask)"
                />
            </svg>

            {/* Highlight border */}
            {highlightRect && (
                <div
                    className="fixed pointer-events-none"
                    style={{
                        top: `${highlightRect.top}px`,
                        left: `${highlightRect.left}px`,
                        width: `${highlightRect.width}px`,
                        height: `${highlightRect.height}px`,
                        border: '3px solid rgb(168, 85, 247)',
                        borderRadius: '16px',
                        zIndex: 9998,
                        boxShadow: '0 0 0 4px rgba(168, 85, 247, 0.2), 0 0 20px rgba(168, 85, 247, 0.4)',
                        animation: 'pulse 2s ease-in-out infinite',
                    }}
                />
            )}

            {/* Tutorial popup */}
            <div
                style={{
                    ...getPopupStyle(),
                    zIndex: 9999,
                }}
                className={`bg-white rounded-2xl shadow-2xl p-6 ${isTransitioning ? 'opacity-0' : 'opacity-100'} transition-opacity duration-300`}
            >
                <div className="flex items-start justify-between mb-4">
                    <div className="flex-1">
                        <h3 className="text-xl font-bold text-gray-900 mb-1">
                            {currentStep.title}
                        </h3>
                        <p className="text-sm text-gray-600">
                            Step {currentStepIndex + 1} of {steps.length}
                        </p>
                    </div>
                    <button
                        onClick={onSkip}
                        className="text-gray-400 hover:text-gray-600 transition-colors p-1 flex-shrink-0"
                        aria-label="Skip tutorial"
                    >
                        <X className="w-5 h-5"/>
                    </button>
                </div>

                <p className="text-gray-700 mb-6 leading-relaxed">
                    {currentStep.description}
                </p>

                <div className="flex justify-center gap-2 mb-6">
                    {steps.map((_, index) => (
                        <div
                            key={index}
                            className={`h-2 rounded-full transition-all duration-300 ${
                                index === currentStepIndex
                                    ? 'w-8 bg-purple-500'
                                    : index < currentStepIndex
                                        ? 'w-2 bg-purple-300'
                                        : 'w-2 bg-gray-300'
                            }`}
                        />
                    ))}
                </div>

                <div className="flex items-center justify-between gap-3">
                    <button
                        onClick={handlePrevious}
                        disabled={isFirstStep}
                        className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-colors ${
                            isFirstStep
                                ? 'text-gray-400 cursor-not-allowed'
                                : 'text-purple-600 hover:bg-purple-50'
                        }`}
                    >
                        <ArrowLeft className="w-4 h-4"/>
                        Back
                    </button>

                    <button
                        onClick={onSkip}
                        className="text-sm text-gray-500 hover:text-gray-700 transition-colors whitespace-nowrap"
                    >
                        Skip Tutorial
                    </button>

                    <button
                        onClick={handleNext}
                        className="flex items-center gap-2 px-6 py-2 bg-purple-500 text-white rounded-lg font-medium hover:bg-purple-600 transition-colors"
                    >
                        {isLastStep ? 'Finish' : 'Next'}
                        {!isLastStep && <ArrowRight className="w-4 h-4"/>}
                    </button>
                </div>
            </div>

            <style dangerouslySetInnerHTML={{
                __html: `
                    @keyframes pulse {
                        0%, 100% { opacity: 1; transform: scale(1); }
                        50% { opacity: 0.8; transform: scale(1.02); }
                    }
                `
            }}/>
        </>
    );
};

export default TutorialOverlay;