import React from 'react';
import {SkipBack, SkipForward} from 'lucide-react';
import {Button} from '../ui/button';
import {Badge} from '../ui/badge';
import {Card, CardContent} from '../ui/card';

interface ExerciseNavigation {
    currentSetNumber: number;
    totalSets: number;
    canGoPrevious: boolean;
    canGoNext: boolean;
    onPreviousExercise: () => void;
    onNextExercise: () => void;
}

export const ExerciseNavigation: React.FC<ExerciseNavigation> = ({
                                                                     currentSetNumber,
                                                                     totalSets,
                                                                     canGoPrevious,
                                                                     canGoNext,
                                                                     onPreviousExercise,
                                                                     onNextExercise
                                                                 }) => {
    return (
        <Card className="bg-gray-800 border-gray-700">
            <CardContent className="p-4">
                <div className="flex items-center justify-between">
                    <Button
                        variant="outline"
                        onClick={onPreviousExercise}
                        disabled={!canGoPrevious}
                        className="flex-1 mr-2 bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                    >
                        <SkipBack className="w-4 h-4 mr-2"/>
                        Previous
                    </Button>

                    <div className="flex-1 text-center">
                        <Badge variant="secondary" className="text-sm bg-gray-700 text-white">
                            Set {currentSetNumber} of {totalSets}
                        </Badge>
                    </div>

                    <Button
                        variant="outline"
                        onClick={onNextExercise}
                        disabled={!canGoNext}
                        className="flex-1 ml-2 bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                    >
                        Next
                        <SkipForward className="w-4 h-4 ml-2"/>
                    </Button>
                </div>
            </CardContent>
        </Card>
    );
};