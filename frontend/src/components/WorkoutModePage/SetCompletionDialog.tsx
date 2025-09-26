import React from 'react';
import {Plus, Trophy, ArrowRight} from 'lucide-react';
import {Button} from '../ui/button';
import {Card, CardContent, CardHeader, CardTitle} from '../ui/card';

interface SetCompletionDialogProps {
    show: boolean;
    isLastSet: boolean;
    isLastExercise: boolean;
    exerciseName: string;
    onNextExercise: () => void;
    onAddSet: () => void;
    onCompleteWorkout: () => void;
    onClose: () => void;
}

export const SetCompletionDialog: React.FC<SetCompletionDialogProps> = ({
                                                                            show,
                                                                            isLastSet,
                                                                            isLastExercise,
                                                                            exerciseName,
                                                                            onNextExercise,
                                                                            onAddSet,
                                                                            onCompleteWorkout,
                                                                            onClose
                                                                        }) => {
    if (!show) return null;

    return (
        <div className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
            <Card className="w-full max-w-md bg-gray-800 border-gray-700 text-white">
                <CardHeader className="pb-3">
                    <CardTitle className="text-center">
                        {isLastSet ? (
                            <div className="space-y-2">
                                <div className="text-2xl">🎯</div>
                                <div>Set Complete!</div>
                            </div>
                        ) : (
                            <div className="space-y-2">
                                <div className="text-2xl">✅</div>
                                <div>Great job!</div>
                            </div>
                        )}
                    </CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    {isLastSet ? (
                        <div className="space-y-4">
                            <p className="text-center text-gray-300">
                                You've completed all sets for <span
                                className="font-bold text-white">{exerciseName}</span>
                            </p>

                            <div className="space-y-3">
                                <Button
                                    onClick={onAddSet}
                                    variant="outline"
                                    className="w-full bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                >
                                    <Plus className="w-4 h-4 mr-2"/>
                                    Add Another Set
                                </Button>

                                {isLastExercise ? (
                                    <Button
                                        onClick={onCompleteWorkout}
                                        className="w-full bg-green-600 hover:bg-green-700"
                                    >
                                        <Trophy className="w-4 h-4 mr-2"/>
                                        Complete Workout!
                                    </Button>
                                ) : (
                                    <Button
                                        onClick={onNextExercise}
                                        className="w-full bg-blue-600 hover:bg-blue-700"
                                    >
                                        <ArrowRight className="w-4 h-4 mr-2"/>
                                        Next Exercise
                                    </Button>
                                )}

                                <Button
                                    onClick={onClose}
                                    variant="outline"
                                    className="w-full bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                >
                                    Continue Here
                                </Button>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center">
                            <p className="text-gray-300 mb-4">Set completed successfully!</p>
                            <Button
                                onClick={onClose}
                                className="w-full bg-blue-600 hover:bg-blue-700"
                            >
                                Continue
                            </Button>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};