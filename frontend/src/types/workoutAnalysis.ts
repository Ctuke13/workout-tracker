export interface CriterionDetail {
    name: string;
    target: any;
    actual: any;
    status: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET' | 'NOT_SET';
    weight: number;
    displayText: string;
    unit?: string;
    percentage?: number;
}

export interface PerformanceEvaluation {
    overall: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET';
    criteria: CriterionDetail[];
    achievementScore: number;
}

export interface WorkoutAnalysisResult {
    performance: PerformanceEvaluation;
    quickStats: WorkoutQuickStats;
    summary: WorkoutSummary;
}

export interface WorkoutQuickStats {
    completedSets: number;
    totalReps?: number;
    totalDuration: number;
    maxWeight?: number;
    weightUnit?: string;
    totalDistance?: number;
    distanceUnit?: string;
    averagePace?: number;
    totalHoldTime?: number;
    averageHoldTime?: number;
    caloriesBurned?: number;
}

export interface WorkoutSummary {
    exerciseName: string;
    completedAt: string;
    exerciseType: 'strength' | 'cardio' | 'isometric';
    totalDuration: number;
    performanceRating: 'EXCEEDED' | 'MET' | 'PARTIAL' | 'BELOW_TARGET';
    achievementPercentage: number;
    hasPersonalRecords: boolean;
    hasImprovements: boolean;
}