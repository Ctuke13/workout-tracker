import { createTheme } from '@mui/material/styles';

const theme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#00D2FF',
        },
        secondary: {
            main: '#FF6B35',
        },
        background: {
            default: '#FFFFFF',
            paper: '#F8F9FA',
        },
        text: {
            primary: '#1E293B',
            secondary: '#475569',
        },
        grey: {
            50: '#F8F9FA',
            100: '#F1F5F9',
            200: '#E2E8F0',
            300: '#CBD5E1',
            400: '#94A3B8',
            500: '#64748B',
            600: '#475569',
            700: '#334155',
            800: '#1E293B',
            900: '#0F172A',
        },
    },
    typography: {
        fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
    },
    components: {
        MuiButton: {
            styleOverrides: {
                contained: {
                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
                    '&:hover': {
                        boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
                    },
                },
                outlined: {
                    borderColor: '#00D2FF',
                    color: '#00D2FF',
                    '&:hover': {
                        backgroundColor: 'rgba(0, 210, 255, 0.04)',
                        borderColor: '#0099CC',
                    },
                },
            },
        },
    },
});

export default theme;