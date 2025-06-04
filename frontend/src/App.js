import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import theme from './theme';
import LandingPage from './pages/LandingPage';
import './index.css';

function App() {
    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Router>
                <div className="font-inter bg-light-bg text-text-primary min-h-screen">
                    <Routes>
                        <Route path="/" element={<LandingPage />} />
                        {/* Add more routes later for auth, dashboard, etc. */}
                    </Routes>
                </div>
            </Router>
        </ThemeProvider>
    );
}

export default App;