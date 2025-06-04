import React from 'react';
import { Button } from '@mui/material';

const PricingSection = () => {
    const plans = [
        {
            name: "FREE",
            price: "$0 ",
            period: "forever",
            description: "Perfect for getting started with workout tracking",
            badge: null,
            features: [
                "✅ Unlimited workout logging",
                "✅ Basic progress tracking",
                "✅ Exercise library access",
                "✅ No ads, ever",
                "❌ Advanced analytics",
                "❌ Workout scheduling",
                "❌ AI recommendations"
            ],
            cta: "Start Free",
            popular: false,
            color: "border"
        },
        {
            name: "PLUS",
            price: "$4.99",
            period: "/month",
            description: "For serious athletes who want detailed insights",
            badge: "⭐ Most Popular",
            features: [
                "✅ Everything in FREE",
                "✅ Advanced analytics dashboard",
                "✅ Volume & strength analysis",
                "✅ Personal record tracking",
                "✅ Workout scheduling",
                "✅ Export your data",
                "❌ AI workout generation"
            ],
            cta: "Upgrade to PLUS",
            popular: true,
            color: "border-electric-blue"
        },
        {
            name: "PRO",
            price: "$14.99",
            period: "/month",
            description: "Maximum performance with AI-powered insights",
            badge: "🚀 Elite",
            features: [
                "✅ Everything in PLUS",
                "✅ AI workout generation",
                "✅ Sport-specific programs",
                "✅ Advanced progression tracking",
                "✅ Priority support",
                "✅ Beta feature access",
                "✅ Custom integrations"
            ],
            cta: "Go Pro",
            popular: false,
            color: "border-neon-green"
        }
    ];

    return (
        <section id="pricing" className="py-16 md:py-24 px-4 sm:px-6 lg:px-8 bg-light-bg animate-on-scroll">
            <div className="max-w-7xl mx-auto">

                {/* Section Header */}
                <div className="text-center mb-12 md:mb-16">
                    <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-text-primary mb-6">
                        Choose Your{' '}
                        <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                            Training Level
                        </span>
                    </h2>
                    <p className="text-lg md:text-xl text-text-muted max-w-3xl mx-auto mb-8">
                        Start free and upgrade when you're ready for advanced features.
                        No hidden fees, cancel anytime.
                    </p>

                    {/* Beta Pricing Notice */}
                    <div className="inline-block px-6 py-3 bg-orange-gradient-start/20 border border-orange-gradient-start/30 rounded-lg">
                        <span className="text-orange-gradient-start font-semibold">
                            🎉 Beta Pricing - 50% off for founding members!
                        </span>
                    </div>
                </div>

                {/* Pricing Cards */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
                    {plans.map((plan, index) => (
                        <div
                            key={index}
                            className={`relative bg-light-card border-2 ${plan.color} rounded-2xl p-8 shadow-light-card transition-all duration-300 hover:transform hover:scale-105 hover:shadow-light-card-hover ${
                                plan.popular
                                    ? 'shadow-3xl shadow-electric-blue/40 transform scale-105 ring-2 ring-electric-blue/30 hover:shadow-electric-blue'
                                    : 'shadow-2xl shadow-gray-700 hover:shadow-neon-green'
                            }`}
                        >
                            {/* Popular Badge */}
                            {plan.badge && (
                                <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                                    <span className={`px-4 py-2 rounded-full text-sm font-bold text-white ${
                                        plan.popular
                                            ? 'bg-gradient-to-r from-electric-blue to-neon-green'
                                            : 'bg-gradient-to-r from-neon-green to-electric-blue'
                                    }`}>
                                        {plan.badge}
                                    </span>
                                </div>
                            )}

                            {/* Plan Header */}
                            <div className="text-center mb-8">
                                <h3 className="text-2xl font-bold text-text-primary mb-2">{plan.name}</h3>
                                <div className="mb-4">
                                    <span className="text-4xl md:text-5xl font-bold text-text-primary">{plan.price}</span>
                                    <span className="text-text-muted">{plan.period}</span>
                                </div>
                                <p className="text-text-muted text-sm">{plan.description}</p>
                            </div>

                            {/* Features List */}
                            <div className="space-y-3 mb-8">
                                {plan.features.map((feature, featureIndex) => (
                                    <div key={featureIndex} className="flex items-start space-x-3">
                                        <span className="text-sm text-text-secondary">{feature}</span>
                                    </div>
                                ))}
                            </div>

                            {/* CTA Button */}
                            <Button
                                variant={plan.popular ? "contained" : "outlined"}
                                fullWidth
                                size="large"
                                className={`py-3 rounded-lg font-semibold transition-all ${
                                    plan.popular
                                        ? 'bg-gradient-to-r from-electric-blue to-neon-green text-white hover:shadow-lg transform hover:scale-105'
                                        : 'border-electric-blue text-electric-blue hover:bg-electric-blue/10'
                                }`}
                            >
                                {plan.cta}
                            </Button>
                        </div>
                    ))}
                </div>

                {/* Trust Signals */}
                <div className="text-center">
                    <div className="flex flex-wrap justify-center gap-8 text-sm text-text-muted">
                        <span className="flex items-center">
                            <span className="text-neon-green mr-2">✅</span>
                            No credit card required for FREE
                        </span>
                        <span className="flex items-center">
                            <span className="text-neon-green mr-2">✅</span>
                            Cancel anytime, keep your data
                        </span>
                        <span className="flex items-center">
                            <span className="text-neon-green mr-2">✅</span>
                            14-day free trial for paid plans
                        </span>
                        <span className="flex items-center">
                            <span className="text-neon-green mr-2">✅</span>
                            Your data stays yours
                        </span>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default PricingSection;