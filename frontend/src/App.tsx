import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import {ExercisesPage} from './pages/ExercisesPage';
import WelcomePage from './pages/WelcomePage';

const App: React.FC = () => {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    <Route path="/" element={<LandingPage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/exercises" element={<ExercisesPage />} />
                    <Route path="/welcome" element={<WelcomePage />} />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
};

export default App;