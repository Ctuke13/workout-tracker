// src/components/CalendarPage/PerformanceStatsModal.tsx - Updated to use WorkoutResults
import React, {useState} from 'react';
import {
    X,
    TrendingUp,
    TrendingDown,
    Trophy,
    Target,
    Clock,
    Weight,
    Heart,
    Zap,
    Activity,
    Award,
    Star,
    Flame
} from 'lucide-react';
import {Button} from '../ui/button';
import {Card, CardContent, CardHeader, CardTitle} from '../ui/card';
import {Badge} from '../ui/badge';
import {Tabs, TabsContent, TabsList, TabsTrigger} from '../ui/tabs';
import {WorkoutResults} from '../../types/exercise'; // Using your existing interface

interface PerformanceStatsModalProps {
    isOpen: boolean;
    onClose: () => void;
    workoutResults: WorkoutResults;
    exerciseName: string;
}

const PerformanceStatsModal: React.FC<PerformanceStatsModalProps> = ({
                                                                         isOpen,
                                                                         onClose,
                                                                         workoutResults,
                                                                         exerciseName
                                                                     }) => {
    const [selectedTab, setSelectedTab] = useState('overview');

    if (!isOpen) return null;

    // Extract types from WorkoutResults for better type safety
    type PersonalRecord = NonNullable<WorkoutResults['personalRecords']>[0];
    type PerformanceImprovement = NonNullable<WorkoutResults['improvements']>[0];

    // Calculate summary stats with safe access
    const completedSets = workoutResults.sets.filter(set => set.completed);
    const totalReps = completedSets.reduce((sum, set) => sum + set.actualReps, 0);
    const totalVolume = workoutResults.strengthMetrics?.totalVolume || 0;
    const averageRpe = workoutResults.strengthMetrics?.averageRpe || 0;

    // Safe access to arrays
    const personalRecords = workoutResults.personalRecords || [];
    const improvements = workoutResults.improvements || [];

    const getPerformanceColor = (rating: string) => {
        switch (rating) {
            case 'EXCEEDED':
                return 'text-green-600 bg-green-100 border-green-300';
            case 'MET':
                return 'text-blue-600 bg-blue-100 border-blue-300';
            case 'PARTIAL':
                return 'text-yellow-600 bg-yellow-100 border-yellow-300';
            case 'BELOW_TARGET':
                return 'text-red-600 bg-red-100 border-red-300';
            default:
                return 'text-gray-600 bg-gray-100 border-gray-300';
        }
    };

    const getPerformanceIcon = (rating: string) => {
        switch (rating) {
            case 'EXCEEDED':
                return <TrendingUp className="w-4 h-4"/>;
            case 'MET':
                return <Target className="w-4 h-4"/>;
            case 'PARTIAL':
                return <Activity className="w-4 h-4"/>;
            case 'BELOW_TARGET':
                return <TrendingDown className="w-4 h-4"/>;
            default:
                return <Activity className="w-4 h-4"/>;
        }
    };

    const renderStrengthOverview = () => (
        <div className="space-y-6">
            {/* Performance Summary Cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <Card>
                    <CardContent className="p-4 text-center">
                        <Weight className="w-6 h-6 mx-auto mb-2 text-blue-600"/>
                        <div className="text-2xl font-bold text-gray-900">{totalVolume}</div>
                        <div className="text-sm text-gray-600">Total Volume</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="p-4 text-center">
                        <Target className="w-6 h-6 mx-auto mb-2 text-green-600"/>
                        <div className="text-2xl font-bold text-gray-900">{totalReps}</div>
                        <div className="text-sm text-gray-600">Total Reps</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="p-4 text-center">
                        <Zap className="w-6 h-6 mx-auto mb-2 text-purple-600"/>
                        <div className="text-2xl font-bold text-gray-900">{averageRpe.toFixed(1)}</div>
                        <div className="text-sm text-gray-600">Avg RPE</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="p-4 text-center">
                        <Clock className="w-6 h-6 mx-auto mb-2 text-orange-600"/>
                        <div className="text-2xl font-bold text-gray-900">{workoutResults.totalDurationMinutes}</div>
                        <div className="text-sm text-gray-600">Minutes</div>
                    </CardContent>
                </Card>
            </div>

            {/* Overall Performance Rating */}
            <Card>
                <CardContent className="p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <h3 className="text-lg font-semibold mb-1">Overall Performance</h3>
                            <p className="text-gray-600">How you performed vs your targets</p>
                        </div>
                        <div
                            className={`px-4 py-2 rounded-full border-2 flex items-center gap-2 ${getPerformanceColor(workoutResults.performanceRating)}`}>
                            {getPerformanceIcon(workoutResults.performanceRating)}
                            <span className="font-semibold capitalize">
                                {workoutResults.performanceRating.toLowerCase().replace('_', ' ')}
                            </span>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Personal Records & Improvements */}
            {(workoutResults.personalRecords?.length > 0 || workoutResults.improvements?.length > 0) && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {workoutResults.personalRecords?.length > 0 && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <Trophy className="w-5 h-5 text-yellow-600"/>
                                    Personal Records
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-3">
                                    {workoutResults.personalRecords.map((pr, index) => (
                                        <div key={index}
                                             className="flex items-center justify-between p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                                            <div>
                                                <div
                                                    className="font-semibold text-yellow-800">{pr.type.replace('_', ' ')}</div>
                                                <div className="text-sm text-yellow-600">New record!</div>
                                            </div>
                                            <div className="text-right">
                                                <div
                                                    className="text-lg font-bold text-yellow-800">{pr.newValue} {pr.unit}</div>
                                                {pr.previousValue && (
                                                    <div className="text-sm text-yellow-600">
                                                        +{(pr.newValue - pr.previousValue).toFixed(1)}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    )}

                    {workoutResults.improvements?.length > 0 && (
                        <Card>
                            <CardHeader>
                                <CardTitle className="flex items-center gap-2">
                                    <TrendingUp className="w-5 h-5 text-green-600"/>
                                    Improvements
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-3">
                                    {workoutResults.improvements.map((improvement, index) => (
                                        <div key={index}
                                             className="flex items-center justify-between p-3 bg-green-50 rounded-lg border border-green-200">
                                            <div>
                                                <div
                                                    className="font-semibold text-green-800 capitalize">{improvement.metric}</div>
                                                <div
                                                    className="text-sm text-green-600">vs {improvement.comparisonPeriod.replace('_', ' ')}</div>
                                            </div>
                                            <div className="text-right">
                                                <div className="text-lg font-bold text-green-800">
                                                    +{improvement.improvementPercentage.toFixed(1)}%
                                                </div>
                                                <div className="text-sm text-green-600">
                                                    {improvement.previousValue} → {improvement.currentValue}
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    )}
                </div>
            )}
        </div>
    );

    const renderCardioOverview = () => (
        <div className="space-y-6">
            {/* Cardio Performance Cards */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <Card>
                    <CardContent className="p-4 text-center">
                        <Clock className="w-6 h-6 mx-auto mb-2 text-red-600"/>
                        <div className="text-2xl font-bold text-gray-900">
                            {workoutResults.cardioMetrics?.totalDurationMinutes || workoutResults.totalDurationMinutes}
                        </div>
                        <div className="text-sm text-gray-600">Minutes</div>
                    </CardContent>
                </Card>

                {workoutResults.cardioMetrics?.totalDistanceKm && (
                    <Card>
                        <CardContent className="p-4 text-center">
                            <Activity className="w-6 h-6 mx-auto mb-2 text-blue-600"/>
                            <div className="text-2xl font-bold text-gray-900">
                                {(workoutResults.cardioMetrics.totalDistanceKm * 0.621371).toFixed(1)}
                            </div>
                            <div className="text-sm text-gray-600">Miles</div>
                        </CardContent>
                    </Card>
                )}

                {workoutResults.cardioMetrics?.averagePace && (
                    <Card>
                        <CardContent className="p-4 text-center">
                            <Zap className="w-6 h-6 mx-auto mb-2 text-purple-600"/>
                            <div className="text-2xl font-bold text-gray-900">
                                {Math.floor(workoutResults.cardioMetrics.averagePace)}:{((workoutResults.cardioMetrics.averagePace % 1) * 60).toFixed(0).padStart(2, '0')}
                            </div>
                            <div className="text-sm text-gray-600">Avg Pace</div>
                        </CardContent>
                    </Card>
                )}

                {workoutResults.cardioMetrics?.totalCaloriesBurned && (
                    <Card>
                        <CardContent className="p-4 text-center">
                            <Flame className="w-6 h-6 mx-auto mb-2 text-orange-600"/>
                            <div className="text-2xl font-bold text-gray-900">
                                {workoutResults.cardioMetrics.totalCaloriesBurned}
                            </div>
                            <div className="text-sm text-gray-600">Calories</div>
                        </CardContent>
                    </Card>
                )}
            </div>

            {/* Heart Rate Zone (if available) */}
            {workoutResults.cardioMetrics?.averageHeartRate && (
                <Card>
                    <CardContent className="p-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <h3 className="text-lg font-semibold mb-1">Heart Rate</h3>
                                <p className="text-gray-600">Average throughout workout</p>
                            </div>
                            <div className="flex items-center gap-2 text-pink-600">
                                <Heart className="w-6 h-6"/>
                                <span
                                    className="text-2xl font-bold">{workoutResults.cardioMetrics.averageHeartRate} bpm</span>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            )}
        </div>
    );

    const renderIsometricOverview = () => (
        <div className="space-y-6">
            {/* Isometric Performance Cards */}
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                <Card>
                    <CardContent className="p-4 text-center">
                        <Clock className="w-6 h-6 mx-auto mb-2 text-purple-600"/>
                        <div className="text-2xl font-bold text-gray-900">
                            {workoutResults.isometricMetrics?.totalHoldTimeSeconds}s
                        </div>
                        <div className="text-sm text-gray-600">Total Hold</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="p-4 text-center">
                        <Target className="w-6 h-6 mx-auto mb-2 text-indigo-600"/>
                        <div className="text-2xl font-bold text-gray-900">
                            {workoutResults.isometricMetrics?.averageHoldTimeSeconds?.toFixed(0)}s
                        </div>
                        <div className="text-sm text-gray-600">Avg Hold</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="p-4 text-center">
                        <Star className="w-6 h-6 mx-auto mb-2 text-blue-600"/>
                        <div className="text-2xl font-bold text-gray-900">
                            {workoutResults.isometricMetrics?.longestHoldSeconds}s
                        </div>
                        <div className="text-sm text-gray-600">Best Hold</div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );

    const renderSetBreakdown = () => (
        <Card>
            <CardHeader>
                <CardTitle>Set-by-Set Performance</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    {workoutResults.sets.map((set, index) => (
                        <div key={index} className="border rounded-lg p-4">
                            <div className="flex items-center justify-between mb-3">
                                <h4 className="font-semibold">Set {set.setNumber}</h4>
                                {set.performanceVsTarget && (
                                    <Badge className={getPerformanceColor(set.performanceVsTarget)}>
                                        {set.performanceVsTarget.toLowerCase().replace('_', ' ')}
                                    </Badge>
                                )}
                            </div>

                            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
                                <div>
                                    <div className="text-gray-600">Reps</div>
                                    <div className="font-medium">
                                        {set.actualReps}/{set.targetReps}
                                        {set.actualReps >= set.targetReps &&
                                            <span className="text-green-600 ml-1">✓</span>}
                                    </div>
                                </div>

                                {set.actualWeight && (
                                    <div>
                                        <div className="text-gray-600">Weight</div>
                                        <div className="font-medium">
                                            {set.actualWeight} {set.targetWeightUnit}
                                        </div>
                                    </div>
                                )}

                                {set.rpe && (
                                    <div>
                                        <div className="text-gray-600">RPE</div>
                                        <div className="font-medium">{set.rpe}/10</div>
                                    </div>
                                )}

                                {set.setDurationSeconds && (
                                    <div>
                                        <div className="text-gray-600">Duration</div>
                                        <div className="font-medium">{set.setDurationSeconds}s</div>
                                    </div>
                                )}
                            </div>

                            {set.notes && (
                                <div className="mt-3 p-2 bg-gray-50 rounded text-sm">
                                    <span className="font-medium">Notes:</span> {set.notes}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    );

    return (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
            <div className="bg-white rounded-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b">
                    <div>
                        <h2 className="text-2xl font-bold text-gray-900">{exerciseName}</h2>
                        <p className="text-gray-600">
                            Completed on {new Date(workoutResults.completedAt).toLocaleDateString()} at{' '}
                            {new Date(workoutResults.completedAt).toLocaleTimeString([], {
                                hour: '2-digit',
                                minute: '2-digit'
                            })}
                        </p>
                    </div>
                    <Button variant="outline" size="sm" onClick={onClose}>
                        <X className="w-4 h-4"/>
                    </Button>
                </div>

                {/* Content */}
                <div className="p-6">
                    <Tabs value={selectedTab} onValueChange={setSelectedTab}>
                        <TabsList className="grid w-full grid-cols-2">
                            <TabsTrigger value="overview">Overview</TabsTrigger>
                            <TabsTrigger value="details">Set Details</TabsTrigger>
                        </TabsList>

                        <TabsContent value="overview" className="mt-6">
                            {workoutResults.strengthMetrics && renderStrengthOverview()}
                            {workoutResults.cardioMetrics && renderCardioOverview()}
                            {workoutResults.isometricMetrics && renderIsometricOverview()}
                        </TabsContent>

                        <TabsContent value="details" className="mt-6">
                            {renderSetBreakdown()}
                        </TabsContent>
                    </Tabs>

                    {/* Workout Notes */}
                    {workoutResults.workoutNotes && (
                        <Card className="mt-6">
                            <CardHeader>
                                <CardTitle>Workout Notes</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-gray-700">{workoutResults.workoutNotes}</p>
                            </CardContent>
                        </Card>
                    )}
                </div>

                {/* Footer */}
                <div className="flex items-center justify-end gap-3 p-6 border-t bg-gray-50">
                    <Button variant="outline" onClick={onClose}>
                        Close
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default PerformanceStatsModal;