import React from 'react';
import { Button } from '@mui/material';

const FinalCTA = () => {
    const scrollToBeta = () => {
        document.getElementById('beta')?.scrollIntoView({ behavior: 'smooth' });
    };

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const features = [
        "🏋️‍♂️ Comprehensive exercise library",
        "📊 Advanced analytics dashboard",
        "📱 Clean, ad-free interface",
        "🔒 Your data stays yours",
        "💻 Built by developers",
        "🚀 Beta community access"
    ];

    const links = {
        company: [
            { name: "About", href: "#" },
            { name: "Blog", href: "#" },
            { name: "Careers", href: "#" },
            { name: "Press", href: "#" }
        ],
        product: [
            { name: "Exercise Library", href: "/exercises" },
            { name: "Pricing", href: "#pricing" },
            { name: "API", href: "#" },
            { name: "Changelog", href: "#" }
        ],
        support: [
            { name: "Help Center", href: "#" },
            { name: "Discord", href: "#" },
            { name: "Contact", href: "#" },
            { name: "Status", href: "#" }
        ]
    };

    return (
        <>
            {/* Final CTA Section */}
            <section className="py-16 md:py-24 px-4 sm:px-6 lg:px-8 bg-gradient-to-r from-blue-50 to-cyan-50 animate-on-scroll">
                <div className="max-w-4xl mx-auto text-center">
                    <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-text-primary mb-6">
                        Ready to Transform{' '}
                        <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                            Your Training?
                        </span>
                    </h2>
                    <p className="text-lg md:text-xl text-text-secondary mb-8 max-w-2xl mx-auto">
                        Join the beta community and be part of building the future of fitness tracking.
                        No ads, no BS, just results.
                    </p>

                    {/* Feature Summary */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-12">
                        {features.map((feature, index) => (
                            <div
                                key={index}
                                className="flex items-center justify-center sm:justify-start space-x-2 text-text-secondary font-medium"
                            >
                                <span>{feature}</span>
                            </div>
                        ))}
                    </div>

                    {/* CTA Buttons */}
                    <div className="flex flex-col sm:flex-row gap-4 justify-center mb-8">
                        <Button
                            variant="contained"
                            size="large"
                            onClick={scrollToBeta}
                            className="bg-gradient-to-r from-electric-blue to-neon-green text-white px-10 py-4 rounded-lg font-semibold text-lg hover:shadow-xl transform hover:scale-105 transition-all"
                        >
                            🚀 Start Your Beta Journey
                        </Button>
                        <Button
                            variant="outlined"
                            size="large"
                            onClick={scrollToTop}
                            className="border-electric-blue text-electric-blue hover:bg-electric-blue/10 px-10 py-4 rounded-lg font-semibold text-lg transition-all"
                        >
                            📚 Learn More
                        </Button>
                    </div>

                    {/* Trust Signals */}
                    <div className="text-center text-sm text-text-secondary font-medium">
                        <p>Join 500+ beta testers • Free forever plan • No credit card required</p>
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className="bg-gray-50 border-t border-gray-200 py-12 px-4 sm:px-6 lg:px-8">
                <div className="max-w-7xl mx-auto">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-8">

                        {/* Brand Column */}
                        <div className="md:col-span-1">
                            <div className="flex items-center mb-4">
                                <span className="text-2xl font-bold text-electric-blue">
                                    💪 WorkoutTracker
                                </span>
                            </div>
                            <p className="text-text-secondary mb-4 leading-relaxed">
                                The fitness tracker built by developers, for people who want real results.
                            </p>
                            <div className="flex space-x-4">
                                <span className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center cursor-pointer hover:bg-blue-200 transition-colors">
                                    📧
                                </span>
                                <span className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center cursor-pointer hover:bg-blue-200 transition-colors">
                                    💬
                                </span>
                                <span className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center cursor-pointer hover:bg-blue-200 transition-colors">
                                    🐙
                                </span>
                            </div>
                        </div>

                        {/* Links Columns */}
                        <div>
                            <h4 className="text-text-primary font-semibold mb-4">Company</h4>
                            <ul className="space-y-3">
                                {links.company.map((link, index) => (
                                    <li key={index}>
                                        <a href={link.href} className="text-text-secondary hover:text-electric-blue transition-colors cursor-pointer font-medium">
                                            {link.name}
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </div>

                        <div>
                            <h4 className="text-text-primary font-semibold mb-4">Product</h4>
                            <ul className="space-y-3">
                                {links.product.map((link, index) => (
                                    <li key={index}>
                                        <a href={link.href} className="text-text-secondary hover:text-electric-blue transition-colors cursor-pointer font-medium">
                                            {link.name}
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </div>

                        <div>
                            <h4 className="text-text-primary font-semibold mb-4">Support</h4>
                            <ul className="space-y-3">
                                {links.support.map((link, index) => (
                                    <li key={index}>
                                        <a href={link.href} className="text-text-secondary hover:text-electric-blue transition-colors cursor-pointer font-medium">
                                            {link.name}
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </div>

                    {/* Bottom Footer */}
                    <div className="border-t border-gray-200 mt-12 pt-8 flex flex-col md:flex-row justify-between items-center">
                        <div className="text-text-secondary text-sm mb-4 md:mb-0 font-medium">
                            © 2025 WorkoutTracker. Built with ❤️ for serious athletes.
                        </div>
                        <div className="flex space-x-6 text-sm">
                            <a href="#" className="text-text-secondary hover:text-electric-blue transition-colors font-medium">
                                Privacy Policy
                            </a>
                            <a href="#" className="text-text-secondary hover:text-electric-blue transition-colors font-medium">
                                Terms of Service
                            </a>
                            <a href="#" className="text-text-secondary hover:text-electric-blue transition-colors font-medium">
                                Cookie Policy
                            </a>
                        </div>
                    </div>
                </div>
            </footer>
        </>
    );
};

export default FinalCTA;