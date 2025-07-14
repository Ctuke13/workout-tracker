import React from 'react';
import { Button } from '@mui/material';

// Types
interface LinkItem {
    name: string;
    href: string;
}

interface LinkSection {
    title: string;
    links: LinkItem[];
}

interface FeatureItem {
    text: string;
}

const FinalCTA: React.FC = () => {
    // Navigation handlers
    const scrollToBeta = (): void => {
        const betaElement = document.getElementById('beta');
        if (betaElement) {
            betaElement.scrollIntoView({ behavior: 'smooth' });
        }
    };

    const scrollToTop = (): void => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    // Features configuration
    const features: FeatureItem[] = [
        { text: "🏋️‍♂️ Comprehensive exercise library" },
        { text: "📊 Advanced analytics dashboard" },
        { text: "📱 Clean, ad-free interface" },
        { text: "🔒 Your data stays yours" },
        { text: "💻 Built by developers" },
        { text: "🚀 Beta community access" }
    ];

    // Links configuration
    const linkSections: Record<string, LinkSection> = {
        company: {
            title: "Company",
            links: [
                { name: "About", href: "#" },
                { name: "Blog", href: "#" },
                { name: "Careers", href: "#" },
                { name: "Press", href: "#" }
            ]
        },
        product: {
            title: "Product",
            links: [
                { name: "Exercise Library", href: "/exercises" },
                { name: "Pricing", href: "#pricing" },
                { name: "API", href: "#" },
                { name: "Changelog", href: "#" }
            ]
        },
        support: {
            title: "Support",
            links: [
                { name: "Help Center", href: "#" },
                { name: "Discord", href: "#" },
                { name: "Contact", href: "#" },
                { name: "Status", href: "#" }
            ]
        }
    };

    // Social links configuration
    const socialIcons: string[] = ["📧", "💬", "🐙"];

    // Footer links configuration
    const footerLinks: LinkItem[] = [
        { name: "Privacy Policy", href: "#" },
        { name: "Terms of Service", href: "#" },
        { name: "Cookie Policy", href: "#" }
    ];

    // Link Section Component
    const LinkSectionComponent: React.FC<{ section: LinkSection }> = ({ section }) => (
        <div>
            <h4 className="text-text-primary font-semibold mb-4">{section.title}</h4>
            <ul className="space-y-3">
                {section.links.map((link: LinkItem, index: number) => (
                    <li key={index}>
                        <a
                            href={link.href}
                            className="text-text-secondary hover:text-electric-blue transition-colors cursor-pointer font-medium"
                        >
                            {link.name}
                        </a>
                    </li>
                ))}
            </ul>
        </div>
    );

    return (
        <>
            {/* Final CTA Section */}
            <section className="py-4 md:py-8 px-4 sm:px-6 lg:px-8 bg-gradient-to-r from-blue-50 to-cyan-50 animate-on-scroll">
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
                        {features.map((feature: FeatureItem, index: number) => (
                            <div
                                key={index}
                                className="flex items-center justify-center sm:justify-start space-x-2 text-text-secondary font-medium"
                            >
                                <span>{feature.text}</span>
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
                                {socialIcons.map((icon: string, index: number) => (
                                    <span
                                        key={index}
                                        className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center cursor-pointer hover:bg-blue-200 transition-colors"
                                    >
                                        {icon}
                                    </span>
                                ))}
                            </div>
                        </div>

                        {/* Links Columns */}
                        <LinkSectionComponent section={linkSections.company} />
                        <LinkSectionComponent section={linkSections.product} />
                        <LinkSectionComponent section={linkSections.support} />
                    </div>

                    {/* Bottom Footer */}
                    <div className="border-t border-gray-200 mt-12 pt-8 flex flex-col md:flex-row justify-between items-center">
                        <div className="text-text-secondary text-sm mb-4 md:mb-0 font-medium">
                            © 2025 WorkoutTracker. Built with ❤️ for serious athletes.
                        </div>
                        <div className="flex space-x-6 text-sm">
                            {footerLinks.map((link: LinkItem, index: number) => (
                                <a
                                    key={index}
                                    href={link.href}
                                    className="text-text-secondary hover:text-electric-blue transition-colors font-medium"
                                >
                                    {link.name}
                                </a>
                            ))}
                        </div>
                    </div>
                </div>
            </footer>
        </>
    );
};

export default FinalCTA;