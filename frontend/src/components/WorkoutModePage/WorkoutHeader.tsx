import React from 'react';
import {Clock} from 'lucide-react';

interface WorkoutHeaderProps {
    exerciseName: string;
    exerciseIcon: string;
    currentExerciseIndex: number;
    totalExercises: number;
    completionPercentage: number;
    gradientClass: string;
    workoutDuration?: number;  // ✅ NEW
}

const formatDuration = (seconds: number): string => {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hrs > 0) {
        return `${hrs}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${mins}:${secs.toString().padStart(2, '0')}`;
};

export const WorkoutHeader: React.FC<WorkoutHeaderProps> = ({
                                                                exerciseName,
                                                                exerciseIcon,
                                                                currentExerciseIndex,
                                                                totalExercises,
                                                                completionPercentage,
                                                                gradientClass,
                                                                workoutDuration = 0  // ✅ NEW - Must be in the parameter list!
                                                            }) => {
    return (
        <div className={`bg-gradient-to-r ${gradientClass} text-white p-4 shadow-lg`}>
            <div className="max-w-4xl mx-auto space-y-4">
                {/* Top Row - Exercise Info and Stats */}
                <div className="flex items-start justify-between gap-4">
                    {/* Left - Exercise Name */}
                    <div className="flex items-center gap-3 flex-1 min-w-0">
                        <div className="text-2xl flex-shrink-0">{exerciseIcon}</div>
                        <div className="min-w-0">
                            <h1 className="text-xl font-bold truncate">
                                {exerciseName}
                            </h1>
                            <p className="text-sm opacity-90">
                                Exercise {currentExerciseIndex + 1} of {totalExercises}
                            </p>
                        </div>
                    </div>

                    {/* Right - Timer and Completion */}
                    <div className="flex items-center gap-3 sm:gap-6 flex-shrink-0">
                        {/* ✅ WORKOUT TIMER - NEW! */}
                        <div className="flex items-center gap-2 bg-white/20 px-3 py-2 rounded-lg">
                            <Clock className="w-5 h-5"/>
                            <div className="text-right">
                                <div className="text-xl sm:text-2xl font-mono font-bold leading-none">
                                    {formatDuration(workoutDuration)}
                                </div>
                                <div className="text-[10px] sm:text-xs opacity-75 mt-0.5">
                                    Duration
                                </div>
                            </div>
                        </div>

                        {/* Completion Percentage */}
                        <div className="text-right">
                            <div className="text-xl sm:text-2xl font-bold leading-none">
                                {completionPercentage}%
                            </div>
                            <div className="text-[10px] sm:text-xs opacity-75 mt-0.5">
                                Complete
                            </div>
                        </div>
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