import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import theme from './theme';
import LandingPage from './pages/LandingPage';
import './index.css';
import ExercisesPage from "./pages/ExercisesPage";

function App() {
    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Router>
                <div className="font-inter bg-light-bg text-text-primary min-h-screen">
                    <Routes>
                        <Route path="/" element={<LandingPage />} />
                        <Route path="/exercises" element={<ExercisesPage/>}/>
                        {/* Add more routes later for auth, dashboard, etc. */}
                    </Routes>
                </div>
            </Router>
        </ThemeProvider>
    );
}

export default App;