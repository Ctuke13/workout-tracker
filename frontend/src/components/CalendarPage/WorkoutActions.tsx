import React from 'react';
import {Play, Plus, Settings} from 'lucide-react';
import {Button} from '../ui/button';
import {Card, CardContent} from '../ui/card';

interface WorkoutActionsProps {
    isToday: boolean;
    hasExercises: boolean;
    exerciseCount: number;
    onStartFullWorkout: () => void;
    onAddExercise: () => void;
    onAddWorkoutPlan: () => void;
}

const WorkoutActions: React.FC<WorkoutActionsProps> = ({
                                                           isToday,
                                                           hasExercises,
                                                           exerciseCount,
                                                           onStartFullWorkout,
                                                           onAddExercise,
                                                           onAddWorkoutPlan
                                                       }) => {
    // Start Workout Button (only show if today and has exercises)
    const startWorkoutButton = isToday && hasExercises && (
        <Button
            className="w-full bg-green-600 hover:bg-green-700 text-white py-3 sm:py-4 lg:py-6 text-base sm:text-lg lg:text-xl font-bold flex items-center justify-center gap-2 sm:gap-3 shadow-lg rounded-xl sm:rounded-2xl"
            onClick={onStartFullWorkout}
        >
            <Play className="w-5 h-5 sm:w-6 sm:h-6 lg:w-8 lg:h-8"/>
            Start Today's Workout ({exerciseCount} exercises)
        </Button>
    );

    // Empty State (only show if no exercises)
    const emptyState = !hasExercises && (
        <Card className="shadow-sm border-dashed border-2 border-gray-300">
            <CardContent className="py-8 sm:py-12 lg:py-16 text-center">
                <div className="text-4xl sm:text-5xl lg:text-6xl mb-3 sm:mb-4 lg:mb-6">
                    {isToday ? '🎯' : '📅'}
                </div>
                <h3 className="text-base sm:text-lg lg:text-xl font-bold text-gray-900 mb-2">
                    {isToday ? 'No workouts planned for today' : 'No workouts planned'}
                </h3>
                <p className="text-sm sm:text-base text-gray-600 mb-4 sm:mb-6">
                    {isToday ? 'Ready to start your fitness journey?' : 'Plan ahead for a successful workout'}
                </p>
                <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center">
                    <Button
                        onClick={onAddExercise}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 sm:px-6 py-2 sm:py-3 text-sm sm:text-base font-semibold rounded-lg sm:rounded-xl flex items-center justify-center"
                    >
                        <Plus className="w-4 h-4 sm:w-5 sm:h-5 mr-2"/>
                        Add Exercise
                    </Button>
                    <Button
                        onClick={onAddWorkoutPlan}
                        className="bg-purple-600 hover:bg-purple-700 text-white px-4 sm:px-6 py-2 sm:py-3 text-sm sm:text-base font-semibold rounded-lg sm:rounded-xl flex items-center justify-center"
                    >
                        <Settings className="w-4 h-4 sm:w-5 sm:h-5 mr-2"/>
                        Add Workout Plan
                    </Button>
                </div>
            </CardContent>
        </Card>
    );

    return (
        <>
            {startWorkoutButton}
            {emptyState}
        </>
    );
};

export default WorkoutActions;