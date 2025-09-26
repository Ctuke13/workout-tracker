import React from 'react';

interface WorkoutHeaderProps {
    exerciseName: string;
    exerciseIcon: string;
    currentExerciseIndex: number;
    totalExercises: number;
    completionPercentage: number;
    gradientClass: string;
}

export const WorkoutHeader: React.FC<WorkoutHeaderProps> = ({
                                                                exerciseName,
                                                                exerciseIcon,
                                                                currentExerciseIndex,
                                                                totalExercises,
                                                                completionPercentage,
                                                                gradientClass
                                                            }) => {
    return (
        <div className={`bg-gradient-to-r ${gradientClass} text-white p-4 shadow-lg`}>
            <div className="max-w-4xl mx-auto">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center gap-3">
                        <div className="text-2xl">{exerciseIcon}</div>
                        <div>
                            <h1 className="text-xl font-bold">
                                {exerciseName}
                            </h1>
                            <p className="text-sm opacity-90">
                                Exercise {currentExerciseIndex + 1} of {totalExercises}
                            </p>
                        </div>
                    </div>
                    <div className="text-right">
                        <div className="text-lg font-bold">{completionPercentage}%</div>
                        <div className="text-sm opacity-90">Complete</div>
                    </div>
                </div>

                {/* Progress bar */}
                <div className="w-full bg-white/20 rounded-full h-3">
                    <div
                        className="bg-white rounded-full h-3 transition-all duration-300"
                        style={{width: `${completionPercentage}%`}}
                    />
                </div>
            </div>
        </div>
    );
};