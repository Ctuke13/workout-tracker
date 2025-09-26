import React, {useState, useEffect} from 'react';
import {Play, Pause, Square} from 'lucide-react';
import {Button} from '../ui/button';
import {Input} from '../ui/input';
import {Card, CardContent} from '../ui/card';

interface CardioTrackerProps {
    isActive: boolean;
    currentSetData: any;
    onSetComplete: (setData: any) => void;
    onUpdateSetData: (setData: any) => void;
    typeStyle: {
        bg: string;
        border: string;
        button: string;
        text: string;
    };
}

export const CardioTracker: React.FC<CardioTrackerProps> = ({
                                                                isActive,
                                                                currentSetData,
                                                                onSetComplete,
                                                                onUpdateSetData,
                                                                typeStyle
                                                            }) => {
    const [isRunning, setIsRunning] = useState(false);
    const [duration, setDuration] = useState(currentSetData?.duration || 0);
    const [manualDuration, setManualDuration] = useState(currentSetData?.duration?.toString() || '');

    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (isRunning && isActive) {
            interval = setInterval(() => {
                setDuration((prev: number) => {
                    const newDuration = prev + 1;
                    onUpdateSetData({
                        ...currentSetData,
                        duration: newDuration,
                        distance: currentSetData?.distance || 0,
                        calories: currentSetData?.calories || 0
                    });
                    return newDuration;
                });
            }, 1000);
        }
        return () => clearInterval(interval);
    }, [isRunning, isActive, currentSetData, onUpdateSetData]);

    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const handleStart = () => {
        setIsRunning(true);
    };

    const handlePause = () => {
        setIsRunning(false);
    };

    const handleStop = () => {
        setIsRunning(false);
        if (duration > 0) {
            onSetComplete({
                duration,
                distance: currentSetData?.distance || 0,
                calories: currentSetData?.calories || 0
            });
        }
    };

    const handleManualDurationChange = (value: string) => {
        setManualDuration(value);
        const numericValue = parseInt(value) || 0;
        setDuration(numericValue * 60); // Convert minutes to seconds
        onUpdateSetData({
            ...currentSetData,
            duration: numericValue * 60,
            distance: currentSetData?.distance || 0,
            calories: currentSetData?.calories || 0
        });
    };

    const handleDistanceChange = (value: string) => {
        const distance = parseFloat(value) || 0;
        onUpdateSetData({
            ...currentSetData,
            duration,
            distance,
            calories: currentSetData?.calories || 0
        });
    };

    const handleCaloriesChange = (value: string) => {
        const calories = parseInt(value) || 0;
        onUpdateSetData({
            ...currentSetData,
            duration,
            distance: currentSetData?.distance || 0,
            calories
        });
    };

    return (
        <Card className={`${typeStyle.bg} ${typeStyle.border} bg-gray-800`}>
            <CardContent className="p-6">
                <div className="space-y-6">
                    {/* Timer Display */}
                    <div className="text-center">
                        <div className="text-6xl font-mono font-bold text-white mb-4">
                            {formatTime(duration)}
                        </div>

                        {/* Timer Controls */}
                        <div className="flex justify-center gap-3">
                            {!isRunning ? (
                                <Button
                                    onClick={handleStart}
                                    className={`${typeStyle.button} flex items-center gap-2`}
                                >
                                    <Play className="w-5 h-5"/>
                                    Start
                                </Button>
                            ) : (
                                <Button
                                    onClick={handlePause}
                                    className="bg-yellow-600 hover:bg-yellow-700 flex items-center gap-2"
                                >
                                    <Pause className="w-5 h-5"/>
                                    Pause
                                </Button>
                            )}

                            <Button
                                onClick={handleStop}
                                className="bg-red-600 hover:bg-red-700 flex items-center gap-2"
                                disabled={duration === 0}
                            >
                                <Square className="w-5 h-5"/>
                                Complete Set
                            </Button>
                        </div>
                    </div>

                    {/* Manual Input Section */}
                    <div className="border-t border-gray-600 pt-6">
                        <h4 className="text-lg font-semibold text-white mb-4 text-center">
                            Or Enter Manually
                        </h4>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            {/* Duration Input */}
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Duration (minutes) *
                                </label>
                                <Input
                                    type="number"
                                    value={manualDuration}
                                    onChange={(e) => handleManualDurationChange(e.target.value)}
                                    placeholder="0"
                                    className="bg-gray-700 border-gray-600 text-white"
                                    min="0"
                                    step="0.1"
                                />
                            </div>

                            {/* Distance Input */}
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Distance (optional)
                                </label>
                                <Input
                                    type="number"
                                    value={currentSetData?.distance || ''}
                                    onChange={(e) => handleDistanceChange(e.target.value)}
                                    placeholder="0.0"
                                    className="bg-gray-700 border-gray-600 text-white"
                                    min="0"
                                    step="0.1"
                                />
                            </div>

                            {/* Calories Input */}
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Calories (optional)
                                </label>
                                <Input
                                    type="number"
                                    value={currentSetData?.calories || ''}
                                    onChange={(e) => handleCaloriesChange(e.target.value)}
                                    placeholder="0"
                                    className="bg-gray-700 border-gray-600 text-white"
                                    min="0"
                                />
                            </div>
                        </div>

                        {/* Manual Complete Button */}
                        <div className="mt-4 text-center">
                            <Button
                                onClick={() => onSetComplete({
                                    duration: parseInt(manualDuration) * 60 || 0,
                                    distance: currentSetData?.distance || 0,
                                    calories: currentSetData?.calories || 0
                                })}
                                className={`${typeStyle.button} w-full md:w-auto`}
                                disabled={!manualDuration || parseInt(manualDuration) === 0}
                            >
                                Complete Set Manually
                            </Button>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
};