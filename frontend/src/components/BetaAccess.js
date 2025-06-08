import React, { useState } from 'react';
import { Button, TextField } from '@mui/material';

const BetaAccess = () => {
    const [email, setEmail] = useState('');
    const [isSubmitted, setIsSubmitted] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        // TODO: Connect to your backend API for email signup
        console.log('Beta signup:', email);
        setIsSubmitted(true);
        setEmail('');
    };

    const benefits = [
        {
            icon: "🚀",
            title: "First Access",
            description: "Be among the first to try new features before anyone else"
        },
        {
            icon: "💬",
            title: "Direct Feedback",
            description: "Your input directly shapes the product roadmap"
        },
        {
            icon: "💰",
            title: "Lifetime Discounts",
            description: "Founding member pricing locked in forever"
        },
        {
            icon: "🎯",
            title: "Beta Community",
            description: "Join exclusive Discord with other beta testers"
        }
    ];

    const stats = [
        { number: "500+", label: "Beta Testers" },
        { number: "12K+", label: "Workouts Logged" },
        { number: "4.9/5", label: "User Rating" },
        { number: "99%", label: "Would Recommend" }
    ];

    if (isSubmitted) {
        return (
            <section id="beta" className="py-4 md:py-8 px-4 sm:px-6 lg:px-8 bg-gradient-to-br from-electric-blue/5 via-light-bg-secondary to-neon-green/5 animate-on-scroll">
                <div className="max-w-4xl mx-auto text-center">
                    <div className="bg-gradient-to-r from-neon-green/20 to-electric-blue/20 border border-neon-green/30 rounded-2xl p-12">
                        <div className="text-6xl mb-6">🎉</div>
                        <h3 className="text-3xl font-bold text-text-primary mb-4">
                            Welcome to the Beta Community!
                        </h3>
                        <p className="text-lg text-text-muted mb-6">
                            Check your email for your beta access link and Discord invite.
                            You're now part of shaping the future of fitness tracking!
                        </p>
                        <div className="flex flex-col sm:flex-row gap-4 justify-center">
                            <Button
                                variant="contained"
                                className="bg-gradient-to-r from-electric-blue to-neon-green text-white px-8 py-3 rounded-lg font-semibold"
                            >
                                📱 Download Beta App
                            </Button>
                            <Button
                                variant="outlined"
                                className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 px-8 py-3 rounded-lg font-semibold"
                            >
                                💬 Join Discord Community
                            </Button>
                        </div>
                    </div>
                </div>
            </section>
        );
    }

    return (
        <section id="beta" className="py-4 md:py-8 px-4 sm:px-6 lg:px-8 bg-gradient-to-br from-light-bg-secondary via-light-bg-secondary to-electric-blue animate-on-scroll">
            <div className="max-w-7xl mx-auto">

                {/* Section Header */}
                <div className="text-center mb-12 md:mb-16">
                    <div className="mb-4">
                        <span className="inline-block px-4 py-2 bg-orange-gradient-start/20 border border-orange-gradient-start/30 rounded-full text-orange-gradient-start text-sm font-semibold animate-pulse">
                            🔥 Limited Beta Access - Join Now
                        </span>
                    </div>
                    <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-text-primary mb-6">
                        Join Our{' '}
                        <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                            Beta Community
                        </span>
                    </h2>
                    <p className="text-lg md:text-xl text-text-muted max-w-3xl mx-auto">
                        Be part of building the future of fitness tracking. Get early access,
                        influence features, and connect with other serious athletes.
                    </p>
                </div>

                {/* Beta Stats */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mb-12">
                    {stats.map((stat, index) => (
                        <div key={index} className="text-center">
                            <div className="text-3xl md:text-4xl font-bold text-electric-blue mb-2">
                                {stat.number}
                            </div>
                            <div className="text-text-muted text-sm">
                                {stat.label}
                            </div>
                        </div>
                    ))}
                </div>

                {/* Main Content Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">

                    {/* Left Side - Benefits */}
                    <div>
                        <h3 className="text-2xl font-bold text-text-primary mb-8">
                            Why Join the Beta?
                        </h3>

                        <div className="space-y-6">
                            {benefits.map((benefit, index) => (
                                <div key={index} className="flex items-start space-x-4">
                                    <div className="flex-shrink-0">
                                        <div className="w-12 h-12 bg-electric-blue/10 rounded-lg flex items-center justify-center">
                                            <span className="text-2xl">{benefit.icon}</span>
                                        </div>
                                    </div>
                                    <div>
                                        <h4 className="text-lg font-semibold text-text-primary mb-2">
                                            {benefit.title}
                                        </h4>
                                        <p className="text-text-muted">
                                            {benefit.description}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {/* Testimonial */}
                        <div className="mt-8 p-6 bg-light-card border border-light-border rounded-lg shadow-light-card">
                            <p className="text-text-muted italic mb-4">
                                "This beta has completely changed how I track my workouts.
                                The analytics are incredible and the community is amazing!"
                            </p>
                            <div className="flex items-center">
                                <div className="w-10 h-10 bg-gradient-to-r from-electric-blue to-neon-green rounded-full flex items-center justify-center text-white font-bold mr-3">
                                    M
                                </div>
                                <div>
                                    <div className="text-text-primary font-semibold">Mike R.</div>
                                    <div className="text-text-muted text-sm">Beta Tester since Day 1</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Right Side - Signup Form */}
                    <div>
                        <div className="bg-light-card border border-light-border rounded-2xl p-8 shadow-light-card">
                            <div className="text-center mb-8">
                                <h3 className="text-2xl font-bold text-text-primary mb-4">
                                    Get Your Beta Access
                                </h3>
                                <p className="text-text-muted">
                                    Join 500+ athletes already testing the future of fitness tracking
                                </p>
                            </div>

                            <form onSubmit={handleSubmit} className="space-y-6">
                                <div>
                                    <TextField
                                        fullWidth
                                        type="email"
                                        label="Email Address"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        required
                                        variant="outlined"
                                        sx={{
                                            '& .MuiOutlinedInput-root': {
                                                backgroundColor: '#FFFFFF',
                                                '& fieldset': {
                                                    borderColor: '#E2E8F0',
                                                },
                                                '&:hover fieldset': {
                                                    borderColor: '#00D2FF',
                                                },
                                                '&.Mui-focused fieldset': {
                                                    borderColor: '#00D2FF',
                                                },
                                            },
                                            '& .MuiInputLabel-root': {
                                                color: '#64748B',
                                            },
                                            '& .MuiOutlinedInput-input': {
                                                color: '#1E293B',
                                            },
                                        }}
                                    />
                                </div>

                                <Button
                                    type="submit"
                                    variant="contained"
                                    fullWidth
                                    size="large"
                                    className="bg-gradient-to-r from-electric-blue to-neon-green text-white py-4 rounded-lg font-semibold text-lg hover:shadow-lg transform hover:scale-105 transition-all"
                                    disabled={!email}
                                >
                                    🚀 Join Beta Community FREE
                                </Button>
                            </form>

                            {/* Trust Signals */}
                            <div className="mt-6 space-y-2 text-center text-sm text-text-muted">
                                <p>✅ Free forever • No spam, ever • Cancel anytime</p>
                                <p>✅ Instant access • Discord community • Founding member benefits</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default BetaAccess;