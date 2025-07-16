import React, { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import TopNavigation from './TopNavigation';
import BottomNavigation from './BottomNavigation';
import FloatingActionButton from './FloatingActionButton';
import WorkoutModeOverlay from './WorkoutModeOverlay';
import QuickWorkoutModal from "./QuickWorkoutModal";

interface MobileLayoutProps {
    children?: React.ReactNode;
}

const MobileLayout: React.FC<MobileLayoutProps> = ({ children }) => {
    const [isWorkoutMode, setIsWorkoutMode] = useState(false);
    const [showWorkoutModal, setShowWorkoutModal] = useState(false);
    const location = useLocation();

    // Hide bottom nav on certain pages (login, register, etc.)
    const hideBottomNav = ['/login', '/register', '/'].includes(location.pathname);

    return (
        <div className="flex flex-col h-screen bg-gray-50">
            {/* Top Navigation - Always visible */}
            <TopNavigation />

            {/* Main Content Area */}
            <main className={`flex-1 overflow-y-auto ${!hideBottomNav ? 'pb-16' : ''}`}>
                {children || <Outlet />}
            </main>

            {/* Bottom Navigation - Hidden on auth pages */}
            {!hideBottomNav && <BottomNavigation />}

            {/* Floating Action Button - Only on main app pages */}
            {!hideBottomNav && (
                <FloatingActionButton
                    onClick={() => setShowWorkoutModal(true)}
                    isWorkoutMode={isWorkoutMode}
                />
            )}

            {/* Workout Mode Overlay */}
            {isWorkoutMode && (
                <WorkoutModeOverlay
                    onClose={() => setIsWorkoutMode(false)}
                />
            )}

            {/* Quick Workout Modal */}
            {showWorkoutModal && (
                <QuickWorkoutModal
                    onClose={() => setShowWorkoutModal(false)}
                    onStartWorkout={() => {
                        setShowWorkoutModal(false);
                        setIsWorkoutMode(true);
                    }}
                />
            )}
        </div>
    );
};

export default MobileLayout;