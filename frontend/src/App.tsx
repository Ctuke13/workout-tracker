import React from 'react';
import {BrowserRouter, Routes, Route} from 'react-router-dom';
import {AuthProvider} from './contexts/AuthContext';
import {WorkoutProvider} from './contexts/WorkoutContext';
import {SeasonProvider} from "./contexts/SeasonContext";
import {UserPreferencesProvider} from './contexts/UserPreferencesContext';
import {PetProvider} from './contexts/PetContext';
import MobileLayout from './components/layout/MobileLayout';

// Auth Pages (no layout)
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

// Onboarding Pages (no layout)
import {
    NicknameSelectionPage,
    MeetYourPetPage,
    NameYourPetPage
} from './pages/onboarding';

// Pet Pages
import PetHomePage from './pages/PetHomePage';
import PetProfilePage from './pages/PetProfilePage';


// Main App Pages (with layout)
import WelcomePage from './pages/WelcomePage';
import ProgressPage from './pages/ProgressPage';
import AnalyticsPage from './pages/AnalyticsPage';
import CalendarPage from './pages/CalendarPage';
import CommunityPage from './pages/CommunityPage';
import MessagesPage from './pages/MessagesPage';
import ExercisesPage from './pages/ExercisesPage';
import AchievementsPage from './pages/AchievementsPage';

// Workout Mode (full-screen, no layout)
import WorkoutModePage from './pages/WorkoutModePage';

// Additional Pages
import NotificationsPage from './pages/NotificationsPage';
import SettingsPage from './pages/SettingsPage';
import HelpPage from './pages/HelpPage';
import BillingPage from './pages/BillingPage';

// import {ApiTestPanel} from './components/ApiTestPanel';


const App: React.FC = () => {
    return (
        <BrowserRouter>
            <AuthProvider>
                <UserPreferencesProvider>
                    <WorkoutProvider>
                        <SeasonProvider>
                            <PetProvider>
                                <Routes>
                                    {/* ==================== PUBLIC ROUTES ==================== */}
                                    <Route path="/" element={<LandingPage/>}/>
                                    <Route path="/login" element={<LoginPage/>}/>
                                    <Route path="/register" element={<RegisterPage/>}/>

                                    {/* ==================== ONBOARDING ROUTES ==================== */}
                                    {/* Protected by AuthContext - only for users who haven't completed onboarding */}
                                    <Route path="/onboarding/nickname" element={<NicknameSelectionPage/>}/>
                                    <Route path="/onboarding/meet-pet" element={<MeetYourPetPage/>}/>
                                    <Route path="/onboarding/name-pet" element={<NameYourPetPage/>}/>

                                    {/* ==================== PET ROUTES ==================== */}
                                    <Route path="/pet" element={<MobileLayout><PetHomePage/></MobileLayout>}/>
                                    <Route path="/pet/profile"
                                           element={<MobileLayout><PetProfilePage/></MobileLayout>}/>

                                    {/* ==================== WORKOUT MODE ==================== */}
                                    {/* Full Screen (No Layout) */}
                                    <Route path="/workout" element={<WorkoutModePage/>}/>

                                    {/* ==================== PROTECTED ROUTES ==================== */}
                                    {/* With Mobile Layout - Only accessible after onboarding complete */}
                                    <Route path="/welcome" element={<MobileLayout><WelcomePage/></MobileLayout>}/>
                                    <Route path="/progress" element={<MobileLayout><ProgressPage/></MobileLayout>}/>
                                    <Route path="/analytics" element={<MobileLayout><AnalyticsPage/></MobileLayout>}/>
                                    <Route path="/calendar" element={<MobileLayout><CalendarPage/></MobileLayout>}/>
                                    <Route path="/community" element={<MobileLayout><CommunityPage/></MobileLayout>}/>
                                    <Route path="/messages" element={<MobileLayout><MessagesPage/></MobileLayout>}/>
                                    <Route path="/exercises" element={<MobileLayout><ExercisesPage/></MobileLayout>}/>
                                    <Route path="/progress/achievements"
                                           element={<MobileLayout><AchievementsPage/></MobileLayout>}/>
                                    <Route path="/notifications"
                                           element={<MobileLayout><NotificationsPage/></MobileLayout>}/>
                                    <Route path="/settings" element={<MobileLayout><SettingsPage/></MobileLayout>}/>
                                    <Route path="/help" element={<MobileLayout><HelpPage/></MobileLayout>}/>
                                    <Route path="/billing" element={<MobileLayout><BillingPage/></MobileLayout>}/>
                                </Routes>

                                {/* API Test Panel - Shows on all pages in development */}
                                {/*{process.env.NODE_ENV === 'development' && <ApiTestPanel />}*/}
                            </PetProvider>
                        </SeasonProvider>
                    </WorkoutProvider>
                </UserPreferencesProvider>
            </AuthProvider>
        </BrowserRouter>
    );
};

export default App;