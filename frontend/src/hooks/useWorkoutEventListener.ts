import {useEffect} from 'react';


interface WorkoutEventDetail {
    date: string;
    exerciseIds: string[];
}

export const useWorkoutEventListener = (onWorkoutCompleted: (detail: WorkoutEventDetail) => void) => {
    useEffect(() => {
        const handleWorkoutCompleted = (event: CustomEvent<WorkoutEventDetail>) => {
            console.log('Workout completed event received:', event.detail);
            onWorkoutCompleted(event.detail);
        };

        // Type-safe event listener
        window.addEventListener('workoutCompleted', handleWorkoutCompleted as EventListener);

        return () => {
            window.removeEventListener('workoutCompleted', handleWorkoutCompleted as EventListener);
        };
    }, [onWorkoutCompleted]);
};