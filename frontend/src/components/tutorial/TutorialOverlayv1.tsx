// import React, {useState, useEffect, useCallback, useRef} from 'react';
// import {X, ArrowRight, ArrowLeft} from 'lucide-react';
// // import {TutorialStep} from '../../config/tutorialSteps';
//
// interface TutorialOverlayProps {
//     steps: TutorialStep[];
//     onComplete: () => void;
//     onSkip: () => void;
//     petName?: string;
// }
//
// const TutorialOverlayv1: React.FC<TutorialOverlayProps> = ({
//                                                              steps,
//                                                              onComplete,
//                                                              onSkip,
//                                                              petName
//                                                          }) => {
//     const [currentStepIndex, setCurrentStepIndex] = useState(0);
//     const [highlightedElement, setHighlightedElement] = useState<HTMLElement | null>(null);
//     const [isTransitioning, setIsTransitioning] = useState(false);
//     const [isReady, setIsReady] = useState(false);
//     const scrollPositionRef = useRef(0);
//     const cleanupFunctionRef = useRef<(() => void) | null>(null);
//
//     const currentStep = steps[currentStepIndex];
//     const isFirstStep = currentStepIndex === 0;
//     const isLastStep = currentStepIndex === steps.length - 1;
//
//     // Force full page render
//     useEffect(() => {
//         const preparePageForTutorial = async () => {
//             console.log('🎬 Preparing page for tutorial...');
//
//             // Scroll to bottom to trigger renders
//             window.scrollTo({
//                 top: document.documentElement.scrollHeight,
//                 behavior: 'instant'
//             });
//
//             await new Promise(resolve => setTimeout(resolve, 2500));
//
//             // Scroll back to top
//             window.scrollTo({
//                 top: 0,
//                 behavior: 'instant'
//             });
//
//             await new Promise(resolve => setTimeout(resolve, 200));
//
//             console.log('✅ Page ready!');
//             setIsReady(true);
//         };
//
//         preparePageForTutorial();
//     }, []);
//
//     // NEW: Scroll lock using OVERFLOW instead of position:fixed
//     useEffect(() => {
//         if (!isReady) return;
//
//         console.log('🔒 Locking scroll...');
//
//         const lockTimeout = setTimeout(() => {
//             // Save current position
//             scrollPositionRef.current = window.pageYOffset;
//
//             // Save original styles
//             const originalOverflow = document.documentElement.style.overflow;
//             const originalBodyOverflow = document.body.style.overflow;
//
//             // Lock scroll with overflow hidden (doesn't break rendering!)
//             document.documentElement.style.overflow = 'hidden';
//             document.body.style.overflow = 'hidden';
//
//             // Scroll to top
//             window.scrollTo(0, 0);
//             scrollPositionRef.current = 0;
//
//             const preventScroll = (e: WheelEvent) => {
//                 e.preventDefault();
//             };
//
//             const preventTouchScroll = (e: TouchEvent) => {
//                 if (e.touches.length > 1) return;
//                 e.preventDefault();
//             };
//
//             const preventKeyScroll = (e: KeyboardEvent) => {
//                 const scrollKeys = ['ArrowUp', 'ArrowDown', 'PageUp', 'PageDown', 'Home', 'End', ' '];
//                 if (scrollKeys.includes(e.key)) {
//                     e.preventDefault();
//                 }
//             };
//
//             window.addEventListener('wheel', preventScroll, {passive: false});
//             window.addEventListener('touchmove', preventTouchScroll, {passive: false});
//             window.addEventListener('keydown', preventKeyScroll, {passive: false});
//
//             cleanupFunctionRef.current = () => {
//                 console.log('🧹 Tutorial cleanup running...');
//
//                 window.removeEventListener('wheel', preventScroll);
//                 window.removeEventListener('touchmove', preventTouchScroll);
//                 window.removeEventListener('keydown', preventKeyScroll);
//
//                 document.documentElement.style.overflow = originalOverflow;
//                 document.body.style.overflow = originalBodyOverflow;
//
//                 window.scrollTo(0, scrollPositionRef.current);
//
//                 console.log('✅ Tutorial cleanup complete!');
//             };
//         }, 100);
//
//         return () => {
//             clearTimeout(lockTimeout);
//
//             if (cleanupFunctionRef.current) {
//                 cleanupFunctionRef.current();
//                 cleanupFunctionRef.current = null;
//             }
//         };
//     }, [isReady]);
//
//     const calculateOptimalScroll = useCallback((element: HTMLElement) => {
//         const rect = element.getBoundingClientRect();
//         const elementTop = rect.top + scrollPositionRef.current;
//         const elementHeight = rect.height;
//         const windowHeight = window.innerHeight;
//
//         const topBuffer = 120;
//         const bottomBuffer = 600;
//
//         const idealScroll = elementTop - topBuffer;
//         const elementBottom = elementTop + elementHeight;
//         const viewportBottom = idealScroll + windowHeight;
//
//         if (elementBottom + bottomBuffer > viewportBottom) {
//             return Math.max(0, elementBottom + bottomBuffer - windowHeight);
//         }
//
//         return Math.max(0, idealScroll);
//     }, []);
//
//     const scrollToPosition = useCallback(async (targetScroll: number) => {
//         scrollPositionRef.current = targetScroll;
//         // Smoothly scroll the actual window
//         window.scrollTo({
//             top: targetScroll,
//             behavior: 'smooth'
//         });
//         await new Promise(resolve => setTimeout(resolve, 600));
//     }, []);
//
//     const waitForScroll = () => new Promise(resolve => setTimeout(resolve, 100));
//
//     const highlightStep = useCallback(async (step: TutorialStep) => {
//         setHighlightedElement(null);
//
//         if (step.highlightSelector) {
//             if (step.preAction) {
//                 try {
//                     await step.preAction();
//                 } catch (error) {
//                     console.error('Pre-action failed:', error);
//                 }
//             }
//
//             await new Promise(resolve => setTimeout(resolve, 500));
//
//             const element = document.querySelector(step.highlightSelector) as HTMLElement;
//
//             if (element) {
//                 console.log(`✅ Found element: ${step.highlightSelector}`);
//
//                 await new Promise(resolve => setTimeout(resolve, 300));
//
//                 if (step.scrollIntoView !== false) {
//                     const optimalScroll = calculateOptimalScroll(element);
//                     console.log(`📏 Scrolling to position: ${optimalScroll}px`);
//                     await scrollToPosition(optimalScroll);
//                     await waitForScroll();
//                 }
//
//                 setHighlightedElement(element);
//             } else {
//                 console.warn(`❌ Element not found: ${step.highlightSelector}`);
//             }
//         }
//     }, [calculateOptimalScroll, scrollToPosition]);
//
//     useEffect(() => {
//         if (!isReady) return;
//
//         const initStep = async () => {
//             setIsTransitioning(true);
//             await highlightStep(currentStep);
//             setIsTransitioning(false);
//         };
//
//         initStep();
//     }, [currentStepIndex, currentStep, highlightStep, isReady]);
//
//     const getHighlightRect = () => {
//         if (!highlightedElement) return null;
//
//         const rect = highlightedElement.getBoundingClientRect();
//         const padding = 8;
//
//         return {
//             top: rect.top - padding,
//             left: rect.left - padding,
//             width: rect.width + padding * 2,
//             height: rect.height + padding * 2,
//         };
//     };
//
//     const getPopupStyle = (): React.CSSProperties => {
//         if (!highlightedElement || currentStep.position === 'center') {
//             return {
//                 position: 'fixed',
//                 top: '50%',
//                 left: '50%',
//                 transform: 'translate(-50%, -50%)',
//                 maxWidth: '90%',
//                 width: '400px',
//                 maxHeight: '80vh',
//                 overflowY: 'auto',
//             };
//         }
//
//         const rect = highlightedElement.getBoundingClientRect();
//         const popupHeight = 300;
//         const spacing = 20;
//         const windowHeight = window.innerHeight;
//
//         let style: React.CSSProperties = {
//             position: 'fixed',
//             maxWidth: '90%',
//             width: '400px',
//             maxHeight: '80vh',
//             overflowY: 'auto',
//         };
//
//         const spaceBelow = windowHeight - rect.bottom;
//         const spaceAbove = rect.top;
//
//         if (currentStep.position === 'bottom' && spaceBelow > popupHeight + spacing) {
//             style.top = `${Math.min(rect.bottom + spacing, windowHeight - popupHeight - 20)}px`;
//             style.left = '50%';
//             style.transform = 'translateX(-50%)';
//         } else if (currentStep.position === 'top' && spaceAbove > popupHeight + spacing) {
//             style.bottom = `${windowHeight - rect.top + spacing}px`;
//             style.left = '50%';
//             style.transform = 'translateX(-50%)';
//         } else if (spaceBelow > popupHeight + spacing) {
//             style.top = `${rect.bottom + spacing}px`;
//             style.left = '50%';
//             style.transform = 'translateX(-50%)';
//         } else if (spaceAbove > popupHeight + spacing) {
//             style.bottom = `${windowHeight - rect.top + spacing}px`;
//             style.left = '50%';
//             style.transform = 'translateX(-50%)';
//         } else {
//             style.top = '50%';
//             style.left = '50%';
//             style.transform = 'translate(-50%, -50%)';
//         }
//
//         return style;
//     };
//
//     const handleNext = () => {
//         if (isLastStep) {
//             onComplete();
//         } else {
//             setCurrentStepIndex(prev => prev + 1);
//         }
//     };
//
//     const handlePrevious = () => {
//         if (!isFirstStep) {
//             setCurrentStepIndex(prev => prev - 1);
//         }
//     };
//
//     const handleKeyPress = useCallback((e: KeyboardEvent) => {
//         if (e.key === 'Escape') {
//             onSkip();
//         } else if (e.key === 'Enter') {
//             e.preventDefault();
//             handleNext();
//         }
//     }, [onSkip, currentStepIndex]); // eslint-disable-line react-hooks/exhaustive-deps
//
//     useEffect(() => {
//         window.addEventListener('keydown', handleKeyPress);
//         return () => window.removeEventListener('keydown', handleKeyPress);
//     }, [handleKeyPress]);
//
//     const highlightRect = getHighlightRect();
//
//     if (!isReady) {
//         return (
//             <div className="fixed inset-0 bg-black/50 z-[9999] flex items-center justify-center">
//                 <div className="bg-white rounded-2xl p-6 shadow-2xl text-center">
//                     <div
//                         className="w-12 h-12 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"/>
//                     <p className="text-gray-700 font-medium">Preparing tutorial...</p>
//                     <p className="text-sm text-gray-500 mt-2">Loading all content...</p>
//                 </div>
//             </div>
//         );
//     }
//
//     return (
//         <>
//             {/* Simple SVG overlay */}
//             <svg
//                 className="fixed inset-0 pointer-events-none"
//                 style={{
//                     zIndex: 9997,
//                     width: '100vw',
//                     height: '100vh',
//                 }}
//                 preserveAspectRatio="none"
//             >
//                 <defs>
//                     <mask id="tutorial-mask">
//                         <rect x="0" y="0" width="100%" height="100%" fill="white"/>
//                         {highlightRect && (
//                             <rect
//                                 x={highlightRect.left}
//                                 y={highlightRect.top}
//                                 width={highlightRect.width}
//                                 height={highlightRect.height}
//                                 rx="16"
//                                 fill="black"
//                             />
//                         )}
//                     </mask>
//                 </defs>
//
//                 <rect
//                     x="0"
//                     y="0"
//                     width="100%"
//                     height="100%"
//                     fill="rgba(0, 0, 0, 0.7)"
//                     mask="url(#tutorial-mask)"
//                 />
//             </svg>
//
//             {/* Highlight border */}
//             {highlightRect && (
//                 <div
//                     className="fixed pointer-events-none"
//                     style={{
//                         top: `${highlightRect.top}px`,
//                         left: `${highlightRect.left}px`,
//                         width: `${highlightRect.width}px`,
//                         height: `${highlightRect.height}px`,
//                         border: '3px solid rgb(168, 85, 247)',
//                         borderRadius: '16px',
//                         zIndex: 9998,
//                         boxShadow: '0 0 0 4px rgba(168, 85, 247, 0.2), 0 0 20px rgba(168, 85, 247, 0.4)',
//                         animation: 'pulse 2s ease-in-out infinite',
//                     }}
//                 />
//             )}
//
//             {/* Tutorial popup */}
//             <div
//                 style={{
//                     ...getPopupStyle(),
//                     zIndex: 9999,
//                 }}
//                 className={`bg-white rounded-2xl shadow-2xl p-6 ${isTransitioning ? 'opacity-0' : 'opacity-100'} transition-opacity duration-300`}
//             >
//                 <div className="flex items-start justify-between mb-4">
//                     <div className="flex-1">
//                         <h3 className="text-xl font-bold text-gray-900 mb-1">
//                             {currentStep.title}
//                         </h3>
//                         <p className="text-sm text-gray-600">
//                             Step {currentStepIndex + 1} of {steps.length}
//                         </p>
//                     </div>
//                     <button
//                         onClick={onSkip}
//                         className="text-gray-400 hover:text-gray-600 transition-colors p-1 flex-shrink-0"
//                         aria-label="Skip tutorial"
//                     >
//                         <X className="w-5 h-5"/>
//                     </button>
//                 </div>
//
//                 <p className="text-gray-700 mb-6 leading-relaxed">
//                     {currentStep.description}
//                 </p>
//
//                 <div className="flex justify-center gap-2 mb-6">
//                     {steps.map((_, index) => (
//                         <div
//                             key={index}
//                             className={`h-2 rounded-full transition-all duration-300 ${
//                                 index === currentStepIndex
//                                     ? 'w-8 bg-purple-500'
//                                     : index < currentStepIndex
//                                         ? 'w-2 bg-purple-300'
//                                         : 'w-2 bg-gray-300'
//                             }`}
//                         />
//                     ))}
//                 </div>
//
//                 <div className="flex items-center justify-between gap-3">
//                     <button
//                         onClick={handlePrevious}
//                         disabled={isFirstStep}
//                         className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-colors ${
//                             isFirstStep
//                                 ? 'text-gray-400 cursor-not-allowed'
//                                 : 'text-purple-600 hover:bg-purple-50'
//                         }`}
//                     >
//                         <ArrowLeft className="w-4 h-4"/>
//                         Back
//                     </button>
//
//                     <button
//                         onClick={onSkip}
//                         className="text-sm text-gray-500 hover:text-gray-700 transition-colors whitespace-nowrap"
//                     >
//                         Skip Tutorial
//                     </button>
//
//                     <button
//                         onClick={handleNext}
//                         className="flex items-center gap-2 px-6 py-2 bg-purple-500 text-white rounded-lg font-medium hover:bg-purple-600 transition-colors"
//                     >
//                         {isLastStep ? 'Finish' : 'Next'}
//                         {!isLastStep && <ArrowRight className="w-4 h-4"/>}
//                     </button>
//                 </div>
//             </div>
//
//             <style dangerouslySetInnerHTML={{
//                 __html: `
//                     @keyframes pulse {
//                         0%, 100% { opacity: 1; transform: scale(1); }
//                         50% { opacity: 0.8; transform: scale(1.02); }
//                     }
//                 `
//             }}/>
//         </>
//     );
// };
//
// export default TutorialOverlayv1;