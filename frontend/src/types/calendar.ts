import {ExerciseConfiguration} from "./exercise";

export interface WorkoutPlanConfiguration {
    workoutPlanId: number;
    scheduledDate: string;
    exerciseConfigs: WorkoutPlanExerciseConfig[];
    planNotes: string;
    estimatedDuration: number;
    reminderEnabled: boolean;
    reminderTime: string;
}

export interface WorkoutPlanExerciseConfig {
    exerciseId: number;
    configuration: ExerciseConfiguration;
    skip: boolean;
    substitute?: boolean;
    notes?: string;
}