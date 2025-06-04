/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Light theme backgrounds
        'light-bg': '#FFFFFF',
        'light-bg-secondary': '#F8F9FA',
        'light-bg-tertiary': '#F1F5F9',

        // Light theme cards
        'light-card': '#FFFFFF',
        'light-card-hover': '#F8F9FA',
        'light-border': '#E2E8F0',
        'light-border-hover': '#CBD5E1',

        // Keep existing accent colors (they work great on light backgrounds)
        'electric-blue': '#00D2FF',
        'electric-blue-dark': '#0099CC',
        'neon-green': '#00FF94',
        'neon-green-dark': '#00CC77',
        'orange-gradient-start': '#FF6B35',
        'orange-gradient-end': '#FF8E53',

        // Light theme text colors
        'text-primary': '#1E293B',
        'text-secondary': '#475569',
        'text-muted': '#64748B',
        'text-light': '#94A3B8',

        // Keep dark colors for phone previews and specific use cases
        'dark-bg': '#0A0A0B',
        'dark-card': '#1A1A1B',
        'dark-card-hover': '#2A2A2B',
      },
      fontFamily: {
        'inter': ['Inter', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
      },
      animation: {
        'fade-in-up': 'fadeInUp 0.8s ease-out',
        'grow-progress': 'growProgress 2s ease-out',
      },
      boxShadow: {
        'light-card': '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
        'light-card-hover': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        'light-section': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
      }
    }
  },
  plugins: [],
  corePlugins: {
    preflight: false, // Prevents conflicts with MUI
  }
}