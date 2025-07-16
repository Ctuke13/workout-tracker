import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import MobileLayout from './components/layout/MobileLayout';

// Auth Pages (no layout)
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Main App Pages (with layout)
import WelcomePage from './pages/WelcomePage';
import ProgressPage from './pages/ProgressPage';
import CalendarPage from './pages/CalendarPage';
import CommunityPage from './pages/CommunityPage';
import MessagesPage from './pages/MessagesPage';
import { ExercisesPage } from './pages/ExercisesPage';

// Additional Pages
import NotificationsPage from './pages/NotificationsPage';
import SettingsPage from './pages/SettingsPage';
import HelpPage from './pages/HelpPage';
import BillingPage from './pages/BillingPage';

const App: React.FC = () => {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    {/* Public Routes - No Layout */}
                    <Route path="/" element={<LandingPage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />

                    {/* Protected Routes - With Mobile Layout */}
                    <Route path="/*" element={<MobileLayout />}>
                        <Route path="welcome" element={<WelcomePage />} />
                        <Route path="progress" element={<ProgressPage />} />
                        <Route path="calendar" element={<CalendarPage />} />
                        <Route path="community" element={<CommunityPage />} />
                        <Route path="messages" element={<MessagesPage />} />
                        <Route path="exercises" element={<ExercisesPage />} />
                        <Route path="notifications" element={<NotificationsPage />} />
                        <Route path="settings" element={<SettingsPage />} />
                        <Route path="help" element={<HelpPage />} />
                        <Route path="billing" element={<BillingPage />} />
                    </Route>
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
};

export default App;