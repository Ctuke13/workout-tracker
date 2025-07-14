import React, { useEffect, useState } from 'react';
import { Button } from '@mui/material';

// Types
interface Solution {
    icon: string;
    title: string;
    description: string;
    highlight: string;
}

interface SolutionCardProps {
    solution: Solution;
    index: number;
}

interface Achievement {
    icon: string;
    title: string;
    subtitle: string;
    bgColor: string;
    borderColor: string;
}

interface Metric {
    value: string;
    label: string;
    color: string;
}

const SolutionSection: React.FC = () => {
    const [chartProgress, setChartProgress] = useState<number>(0);

    useEffect(() => {
        const timer = setTimeout(() => {
            setChartProgress(100);
        }, 800);

        return (): void => {
            clearTimeout(timer);
        };
    }, []);

    // Solutions configuration
    const solutions: Solution[] = [
        {
            icon: "⚡",
            title: "Clean & Fast",
            description: "No ads, no clutter. Built with modern tech that actually works. Your workouts, nothing else.",
            highlight: "0 ads"
        },
        {
            icon: "📈",
            title: "Real Analytics",
            description: "Meaningful insights that help you improve. Track what matters, see your progress clearly.",
            highlight: "Smart insights"
        },
        {
            icon: "🎯",
            title: "Measure Progress",
            description: "See your strength gains, volume increases, and performance trends. Data that drives results.",
            highlight: "Real progress"
        },
        {
            icon: "💻",
            title: "Built by Developers",
            description: "Quality code, reliable performance. Built by people who understand both fitness and technology.",
            highlight: "Quality first"
        }
    ];

    // Key metrics configuration
    const keyMetrics: Metric[] = [
        { value: "+23%", label: "Strength Gain", color: "text-electric-blue" },
        { value: "187K", label: "Total Volume", color: "text-neon-green" },
        { value: "5", label: "New PRs", color: "text-orange-gradient-start" }
    ];

    // Recent achievements configuration
    const achievements: Achievement[] = [
        {
            icon: "🏆",
            title: "New Bench Press PR!",
            subtitle: "185 lbs • 2 days ago",
            bgColor: "bg-neon-green/10",
            borderColor: "border-neon-green/20"
        },
        {
            icon: "🔥",
            title: "7-Day Streak Complete",
            subtitle: "Consistency is key!",
            bgColor: "bg-electric-blue/10",
            borderColor: "border-electric-blue/20"
        }
    ];

    // Chart data points for progress visualization
    const chartHeights: number[] = [65, 78, 82, 88, 92, 95, 100];

    // Reusable Solution Card Component
    const SolutionCard: React.FC<SolutionCardProps> = ({ solution, index }) => (
        <div className="flex items-start space-x-4">
            <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-electric-blue/10 rounded-lg flex items-center justify-center text-2xl">
                    {solution.icon}
                </div>
            </div>
            <div>
                <div className="flex items-center space-x-3 mb-2">
                    <h4 className="text-lg font-semibold text-gray-900">{solution.title}</h4>
                    <span className="px-2 py-1 bg-neon-green/20 text-neon-green text-xs font-semibold rounded-full">
                        {solution.highlight}
                    </span>
                </div>
                <p className="text-gray-600">{solution.description}</p>
            </div>
        </div>
    );

    // Key Metrics Component
    const MetricCard: React.FC<{ metric: Metric }> = ({ metric }) => (
        <div className="bg-gray-50 rounded-lg p-4 text-center">
            <div className={`text-2xl font-bold ${metric.color}`}>{metric.value}</div>
            <div className="text-xs text-gray-600">{metric.label}</div>
        </div>
    );

    // Achievement Component
    const AchievementCard: React.FC<{ achievement: Achievement }> = ({ achievement }) => (
        <div className={`flex items-center space-x-3 p-3 ${achievement.bgColor} rounded-lg border ${achievement.borderColor}`}>
            <span className="text-lg">{achievement.icon}</span>
            <div>
                <div className="font-medium text-gray-900">{achievement.title}</div>
                <div className="text-sm text-gray-600">{achievement.subtitle}</div>
            </div>
        </div>
    );

    return (
        <section className="py-4 md:py-8 px-4 sm:px-6 lg:px-8 bg-gradient-to-b from-gray-50 to-blue-50 animate-on-scroll">
            <div className="max-w-7xl mx-auto">

                {/* Section Header */}
                <div className="text-center mb-12 md:mb-16">
                    <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-6">
                        What We{' '}
                        <span className="text-transparent bg-gradient-to-r from-electric-blue to-neon-green bg-clip-text">
                            Built Instead
                        </span>
                    </h2>
                    <p className="text-lg md:text-xl text-gray-600 max-w-3xl mx-auto mb-8">
                        While other apps give you basic logging, we give you insights that actually help you improve.
                    </p>
                </div>

                {/* Main Content Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 items-center mb-16">

                    {/* Left Side - Analytics Dashboard Preview */}
                    <div className="relative">
                        {/* Desktop/Tablet Frame */}
                        <div className="bg-gray-800 rounded-lg p-1 shadow-2xl">
                            {/* Browser Chrome */}
                            <div className="bg-gray-700 rounded-t-lg px-4 py-2 flex items-center space-x-2">
                                <div className="flex space-x-2">
                                    <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                                    <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                                    <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                                </div>
                                <div className="flex-1 text-center text-gray-400 text-sm">
                                    app.workouttracker.com/analytics
                                </div>
                            </div>

                            {/* Dashboard Content */}
                            <div className="bg-white rounded-b-lg p-6">
                                {/* Dashboard Header */}
                                <div className="flex items-center justify-between mb-6">
                                    <h3 className="text-xl font-bold text-gray-900">Your Progress Analytics</h3>
                                    <span className="px-3 py-1 bg-electric-blue/20 text-electric-blue text-sm rounded-full font-medium">
                                        Last 30 Days
                                    </span>
                                </div>

                                {/* Key Metrics Row */}
                                <div className="grid grid-cols-3 gap-4 mb-6">
                                    {keyMetrics.map((metric: Metric, index: number) => (
                                        <MetricCard key={index} metric={metric} />
                                    ))}
                                </div>

                                {/* Strength Progress Chart */}
                                <div className="mb-6">
                                    <div className="flex items-center justify-between mb-3">
                                        <h4 className="font-semibold text-gray-900">Bench Press Progress</h4>
                                        <span className="text-sm text-neon-green">↗ +15 lbs this month</span>
                                    </div>
                                    <div className="h-24 bg-gray-50 rounded-lg relative overflow-hidden">
                                        {/* Chart bars */}
                                        <div className="absolute bottom-0 left-0 w-full h-full flex items-end justify-between px-4 py-2">
                                            {chartHeights.map((height: number, i: number) => (
                                                <div
                                                    key={i}
                                                    className="bg-gradient-to-t from-electric-blue to-neon-green rounded-t w-6 transition-all duration-1000 ease-out"
                                                    style={{
                                                        height: chartProgress ? `${height}%` : '0%',
                                                        transitionDelay: `${i * 100}ms`
                                                    }}
                                                ></div>
                                            ))}
                                        </div>
                                        {/* Chart line overlay */}
                                        <div className="absolute top-2 right-2 text-xs text-gray-600">
                                            185 lbs
                                        </div>
                                    </div>
                                </div>

                                {/* Recent Achievements */}
                                <div className="space-y-2">
                                    <h4 className="font-semibold text-gray-900">Recent Achievements</h4>
                                    {achievements.map((achievement: Achievement, index: number) => (
                                        <AchievementCard key={index} achievement={achievement} />
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Analytics Preview Badge */}
                        <div className="absolute -bottom-4 left-1/2 transform -translate-x-1/2">
                            <span className="px-3 py-2 bg-electric-blue/20 text-gray-800 text-xs sm:text-sm rounded-full border border-electric-blue/30 font-medium backdrop-blur-sm">
                                <span className="hidden sm:inline">📊 Analytics Dashboard</span>
                                <span className="sm:hidden">📊 Analytics</span>
                            </span>
                        </div>
                    </div>

                    {/* Right Side - Solutions Content */}
                    <div>
                        <h3 className="text-2xl md:text-3xl font-bold text-gray-900 mb-8">
                            Analytics That Actually Help You Improve
                        </h3>

                        <div className="space-y-6">
                            {solutions.map((solution: Solution, index: number) => (
                                <SolutionCard
                                    key={index}
                                    solution={solution}
                                    index={index}
                                />
                            ))}
                        </div>
                    </div>
                </div>

                {/* Beta Community CTA */}
                <div className="text-center">
                    <div className="bg-gradient-to-r from-electric-blue/10 to-neon-green/10 border border-electric-blue/20 rounded-2xl p-8 max-w-4xl mx-auto">
                        <h3 className="text-2xl font-bold text-gray-900 mb-4">
                            Ready to See Your Real Progress?
                        </h3>
                        <p className="text-gray-600 mb-6">
                            Join our beta community and experience analytics that actually help you get stronger.
                        </p>
                        <Button
                            variant="contained"
                            size="large"
                            className="bg-gradient-to-r from-electric-blue to-neon-green text-white px-8 py-3 rounded-lg font-semibold"
                        >
                            🚀 Join Beta Community
                        </Button>
                    </div>
                </div>
            </div>
        </section>
    );
};

export default SolutionSection;