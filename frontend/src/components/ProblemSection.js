import React from 'react';

const ProblemSection = () => {
  const problems = [
    {
      icon: "🐛",
      title: "Cluttered & Buggy",
      description: "Ads everywhere, slow loading, crashes during workouts. Basic functionality that barely works."
    },
    {
      icon: "📊",
      title: "Poor Analytics",
      description: "Generic charts that don't help. No real insights into your progress or what's actually working."
    },
    {
      icon: "📉",
      title: "No Real Progress",
      description: "Logging workouts without meaningful tracking. You're working hard but can't see if you're improving."
    },
    {
      icon: "🎯",
      title: "Built for Everyone",
      description: "Generic fitness apps trying to serve everyone. Nothing built specifically for serious lifters."
    }
  ];

  return (
      <section id="problem" className="py-8 md:py-12 px-4 sm:px-6 lg:px-8 bg-gradient-to-b from-red-50 via-orange-50 to-light-bg-secondary animate-on-scroll">
        <div className="max-w-7xl mx-auto">

          {/* Section Header */}
          <div className="text-center mb-12 md:mb-16">
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-text-primary mb-6">
              Frustrated with{' '}
              <span className="text-transparent bg-gradient-to-r from-red-500 to-orange-500 bg-clip-text">
              Current Fitness Apps?
            </span>
            </h2>
            <p className="text-lg md:text-xl text-text-muted max-w-3xl mx-auto">
              We felt the same way. That's why we built something different.
            </p>
          </div>

          {/* Problems Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 md:gap-8">
            {problems.map((problem, index) => (
                <div
                    key={index}
                    className="group bg-light-card border border-light-border rounded-xl p-6 hover:border-red-300 hover:shadow-light-card-hover hover:shadow-red-200/20 transition-all duration-300 hover:transform hover:scale-105"
                >
                  {/* Icon */}
                  <div className="mb-4 p-3 bg-red-100 rounded-lg w-fit group-hover:bg-red-200 transition-colors">
                    <span className="text-2xl">{problem.icon}</span>
                  </div>

                  {/* Content */}
                  <h3 className="text-lg font-semibold text-text-primary mb-3 group-hover:text-red-600 transition-colors">
                    {problem.title}
                  </h3>
                  <p className="text-text-muted text-sm leading-relaxed group-hover:text-text-secondary transition-colors">
                    {problem.description}
                  </p>

                  {/* Frustrated Emoji */}
                  <div className="mt-4 text-2xl opacity-50 group-hover:opacity-100 transition-opacity">
                    😤
                  </div>
                </div>
            ))}
          </div>

          {/* Call-to-action hint */}
          <div className="text-center mt-12 md:mt-16">
            <p className="text-text-muted text-lg">
              Sound familiar?
              <span className="text-electric-blue font-semibold ml-2">
              There's a better way →
            </span>
            </p>
          </div>
        </div>
      </section>
  );
};

export default ProblemSection;