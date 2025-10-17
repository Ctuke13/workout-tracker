import React from 'react';
import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {AuthProvider} from './contexts/AuthContext';
import {WorkoutProvider} from './contexts/WorkoutContext';
import {SeasonProvider} from "./contexts/SeasonContext";
import {UserPreferencesProvider} from './contexts/UserPreferencesContext';
import MobileLayout from './components/layout/MobileLayout';

// Auth Pages (no layout)
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Main App Pages (with layout)
import WelcomePage from './pages/WelcomePage';
import ProgressPage from './pages/ProgressPage';
import AnalyticsPage from './pages/AnalyticsPage';
import CalendarPage from './pages/CalendarPage';
import CommunityPage from './pages/CommunityPage';
import MessagesPage from './pages/MessagesPage';
import ExercisesPage from './pages/ExercisesPage';

// Workout Mode (full-screen, no layout)
import WorkoutModePage from './pages/WorkoutModePage';

// Additional Pages
import NotificationsPage from './pages/NotificationsPage';
import SettingsPage from './pages/SettingsPage';
import HelpPage from './pages/HelpPage';
import BillingPage from './pages/BillingPage';

import {ApiTestPanel} from './components/ApiTestPanel';


const App: React.FC = () => {
    // @ts-ignore
    return (
        <BrowserRouter>
            <AuthProvider>
                <UserPreferencesProvider>
                    <WorkoutProvider>
                        <SeasonProvider>
                            <Routes>
                                {/* Public Routes - No Layout */}
                                <Route path="/" element={<LandingPage/>}/>
                                <Route path="/login" element={<LoginPage/>}/>
                                <Route path="/register" element={<RegisterPage/>}/>

                                {/* Workout Mode - Full Screen (No Layout) - Must come BEFORE other routes */}
                                <Route path="/workout" element={<WorkoutModePage/>}/>

                                {/* Protected Routes - With Mobile Layout */}
                                <Route path="/welcome" element={<MobileLayout><WelcomePage/></MobileLayout>}/>
                                <Route path="/progress" element={<MobileLayout><ProgressPage/></MobileLayout>}/>
                                <Route path="/analytics" element={<MobileLayout><AnalyticsPage/></MobileLayout>}/>
                                <Route path="/calendar" element={<MobileLayout><CalendarPage/></MobileLayout>}/>
                                <Route path="/community" element={<MobileLayout><CommunityPage/></MobileLayout>}/>
                                <Route path="/messages" element={<MobileLayout><MessagesPage/></MobileLayout>}/>
                                <Route path="/exercises" element={<MobileLayout><ExercisesPage/></MobileLayout>}/>
                                <Route path="/notifications"
                                       element={<MobileLayout><NotificationsPage/></MobileLayout>}/>
                                <Route path="/settings" element={<MobileLayout><SettingsPage/></MobileLayout>}/>
                                <Route path="/help" element={<MobileLayout><HelpPage/></MobileLayout>}/>
                                <Route path="/billing" element={<MobileLayout><BillingPage/></MobileLayout>}/>
                            </Routes>

                            {/*/!* API Test Panel - Shows on all pages in development *!/*/}
                            {/*{process.env.NODE_ENV === 'development' && <ApiTestPanel />}*/}
                        </SeasonProvider>
                    </WorkoutProvider>
                </UserPreferencesProvider>
            </AuthProvider>
        </BrowserRouter>
    );
};

export default App;