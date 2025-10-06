import React, {useState, useEffect} from 'react';
import {Play, Pause, Square} from 'lucide-react';
import {Button} from '../ui/button';
import {Input} from '../ui/input';
import {Card, CardContent} from '../ui/card';
import {useWorkoutMode} from '../../hooks/useWorkoutMode';
import {RestTimerBanner} from './RestTimerBanner';

interface IsometricTrackerProps {
    isActive: boolean;
    currentSetData: any;
    isLastSet?: boolean;
    onSetComplete: (setData: any) => void;
    onUpdateSetData: (setData: any) => void;
    typeStyle: {
        bg: string;
        border: string;
        button: string;
        text: string;
    };
}

export const IsometricTracker: React.FC<IsometricTrackerProps> = ({
                                                                      isActive,
                                                                      currentSetData,
                                                                      isLastSet,
                                                                      onSetComplete,
                                                                      onUpdateSetData,
                                                                      typeStyle
                                                                  }) => {
    const workoutMode = useWorkoutMode();
    const {restStartTime, setRestStartTime, currentRestSeconds, setCurrentRestSeconds} = workoutMode;
    const [isHolding, setIsHolding] = useState(false);
    const [isResting, setIsResting] = useState(false);
    const [holdTime, setHoldTime] = useState(currentSetData?.actualHoldSeconds || currentSetData?.holdTime || 0);
    const [manualHoldTime, setManualHoldTime] = useState((currentSetData?.actualHoldSeconds || currentSetData?.holdTime || 0).toString());

    useEffect(() => {
        const newHoldTime = currentSetData?.actualHoldSeconds || currentSetData?.holdTime || 0;
        setHoldTime(newHoldTime);
        setManualHoldTime(newHoldTime.toString());
        setIsHolding(false);
    }, [currentSetData?.id]);

    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (isHolding && isActive) {
            interval = setInterval(() => {
                setHoldTime((prev: number) => prev + 1);
            }, 1000);
        }
        return () => clearInterval(interval);
    }, [isHolding, isActive]);

    const formatTime = (seconds: number): string => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const handleStartHold = () => {
        setIsHolding(true);
    };

    const handlePauseHold = () => {
        setIsHolding(false);
    };

    const handleCompleteHold = () => {
        if (isHolding) {
            setIsHolding(false);
            setManualHoldTime(holdTime.toString());
        } else {
            if (holdTime > 0) {
                console.log('🔧 Completing isometric set with hold time:', holdTime);
                onSetComplete({
                    actualHoldSeconds: holdTime,
                    intensity: currentSetData?.intensity || 'Medium'
                });

                if (!isLastSet) {
                    setIsResting(true);
                    setRestStartTime(new Date());
                    setCurrentRestSeconds(0);
                }
            }
        }
    };

    const handleEndRest = () => {
        // ✅ Store rest time for next set
        if (currentRestSeconds > 0) {
            workoutMode.setRestTimeForNextSet(currentRestSeconds);
        }

        setIsResting(false);
        setRestStartTime(null);
    };

    const handleSkipRest = () => {
        setIsResting(false);
        setRestStartTime(null);
        setCurrentRestSeconds(0);
    };

    const handleManualHoldTimeChange = (value: string) => {
        setManualHoldTime(value);
        const numericValue = parseInt(value) || 0;
        setHoldTime(numericValue);
        onUpdateSetData({
            ...currentSetData,
            actualHoldSeconds: numericValue,
            intensity: currentSetData?.intensity || 'Medium'
        });
    };

    const handleIntensityChange = (intensity: string) => {
        onUpdateSetData({
            ...currentSetData,
            actualHoldSeconds: holdTime,
            intensity
        });
    };

    const intensityOptions = ['Light', 'Medium', 'Hard', 'Maximum'];
    const selectedIntensity = currentSetData?.intensity || 'Medium';

    return (
        <>
            <RestTimerBanner
                isResting={isResting}
                currentRestSeconds={currentRestSeconds}
                targetRestSeconds={currentSetData?.restSeconds || 30}
                onEndRest={handleEndRest}
                onSkipRest={handleSkipRest}
                exerciseType="isometric"
            />

            <Card className={`${typeStyle.bg} ${typeStyle.border} bg-gray-800`}>
                <CardContent className="p-6">
                    <div className="space-y-6">
                        <div className="text-center">
                            <div className="text-6xl font-mono font-bold text-white mb-4">
                                {formatTime(holdTime)}
                            </div>

                            <div className="flex justify-center gap-3">
                                {!isHolding ? (
                                    <Button
                                        onClick={handleStartHold}
                                        className={`${typeStyle.button} flex items-center gap-2`}
                                        disabled={isResting}
                                    >
                                        <Play className="w-5 h-5"/>
                                        Start Hold
                                    </Button>
                                ) : (
                                    <Button
                                        onClick={handlePauseHold}
                                        className="bg-yellow-600 hover:bg-yellow-700 flex items-center gap-2"
                                    >
                                        <Pause className="w-5 h-5"/>
                                        Pause
                                    </Button>
                                )}

                                <Button
                                    onClick={handleCompleteHold}
                                    className="bg-red-600 hover:bg-red-700 flex items-center gap-2"
                                    disabled={holdTime === 0 || isResting}
                                >
                                    <Square className="w-5 h-5"/>
                                    {isHolding ? 'Stop & Review' : 'Complete Set'}
                                </Button>
                            </div>
                        </div>

                        <div className="border-t border-gray-600 pt-6">
                            <h4 className="text-lg font-semibold text-white mb-4 text-center">
                                Hold Intensity
                            </h4>

                            <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                                {intensityOptions.map((intensity) => (
                                    <Button
                                        key={intensity}
                                        onClick={() => handleIntensityChange(intensity)}
                                        variant={selectedIntensity === intensity ? "default" : "outline"}
                                        className={`${
                                            selectedIntensity === intensity
                                                ? typeStyle.button
                                                : "bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                        }`}
                                        disabled={isResting}
                                    >
                                        {intensity}
                                    </Button>
                                ))}
                            </div>
                        </div>

                        <div className="border-t border-gray-600 pt-6">
                            <h4 className="text-lg font-semibold text-white mb-4 text-center">
                                Or Enter Manually
                            </h4>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-1">
                                        Hold Time (seconds) *
                                    </label>
                                    <Input
                                        type="number"
                                        value={manualHoldTime}
                                        onChange={(e) => handleManualHoldTimeChange(e.target.value)}
                                        placeholder="0"
                                        className="bg-gray-700 border-gray-600 text-white"
                                        min="0"
                                        disabled={isResting}
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-300 mb-1">
                                        Intensity Level
                                    </label>
                                    <select
                                        value={selectedIntensity}
                                        onChange={(e) => handleIntensityChange(e.target.value)}
                                        className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        disabled={isResting}
                                    >
                                        {intensityOptions.map((intensity) => (
                                            <option key={intensity} value={intensity}>
                                                {intensity}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            <div className="mt-4 text-center">
                                <Button
                                    onClick={() => {
                                        const holdSeconds = parseInt(manualHoldTime) || 0;
                                        console.log('🔧 Manually completing isometric set with hold time:', holdSeconds);
                                        onSetComplete({
                                            actualHoldSeconds: holdSeconds,
                                            intensity: selectedIntensity
                                        });

                                        if (!isLastSet) {
                                            setIsResting(true);
                                            setRestStartTime(new Date());
                                            setCurrentRestSeconds(0);
                                        }
                                    }}
                                    className={`${typeStyle.button} w-full md:w-auto`}
                                    disabled={!manualHoldTime || parseInt(manualHoldTime) === 0 || isResting}
                                >
                                    Complete Set Manually
                                </Button>
                            </div>
                        </div>

                        {holdTime > 0 && (
                            <div className="border-t border-gray-600 pt-4">
                                <div className="text-center space-y-2">
                                    <p className="text-sm text-gray-300">
                                        Current Hold: <span
                                        className="text-white font-semibold">{formatTime(holdTime)}</span>
                                    </p>
                                    <p className="text-sm text-gray-300">
                                        Intensity: <span className={`font-semibold ${
                                        selectedIntensity === 'Maximum' ? 'text-red-400' :
                                            selectedIntensity === 'Hard' ? 'text-orange-400' :
                                                selectedIntensity === 'Medium' ? 'text-yellow-400' :
                                                    'text-green-400'
                                    }`}>
                                            {selectedIntensity}
                                        </span>
                                    </p>
                                </div>
                            </div>
                        )}
                    </div>
                </CardContent>
            </Card>
        </>
    );
};