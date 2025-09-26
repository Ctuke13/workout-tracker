import React, {useState, useEffect} from 'react';
import {Play, Pause, Square} from 'lucide-react';
import {Button} from '../ui/button';
import {Input} from '../ui/input';
import {Card, CardContent} from '../ui/card';

interface IsometricTrackerProps {
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

export const IsometricTracker: React.FC<IsometricTrackerProps> = ({
                                                                      isActive,
                                                                      currentSetData,
                                                                      onSetComplete,
                                                                      onUpdateSetData,
                                                                      typeStyle
                                                                  }) => {
    const [isHolding, setIsHolding] = useState(false);
    const [holdTime, setHoldTime] = useState(currentSetData?.actualHoldSeconds || currentSetData?.holdTime || 0);
    const [manualHoldTime, setManualHoldTime] = useState((currentSetData?.actualHoldSeconds || currentSetData?.holdTime || 0).toString());

    useEffect(() => {
        let interval: NodeJS.Timeout;
        if (isHolding && isActive) {
            interval = setInterval(() => {
                setHoldTime((prev: number) => {
                    const newHoldTime = prev + 1;
                    onUpdateSetData({
                        ...currentSetData,
                        actualHoldSeconds: newHoldTime, // 🔧 FIXED: Use actualHoldSeconds
                        intensity: currentSetData?.intensity || 'Medium'
                    });
                    return newHoldTime;
                });
            }, 1000);
        }
        return () => clearInterval(interval);
    }, [isHolding, isActive, currentSetData, onUpdateSetData]);

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
        setIsHolding(false);
        if (holdTime > 0) {
            console.log('🔧 Completing isometric set with hold time:', holdTime); // Debug log
            onSetComplete({
                actualHoldSeconds: holdTime, // 🔧 FIXED: Use actualHoldSeconds
                intensity: currentSetData?.intensity || 'Medium'
            });
        }
    };

    const handleManualHoldTimeChange = (value: string) => {
        setManualHoldTime(value);
        const numericValue = parseInt(value) || 0;
        setHoldTime(numericValue);
        onUpdateSetData({
            ...currentSetData,
            actualHoldSeconds: numericValue, // 🔧 FIXED: Use actualHoldSeconds
            intensity: currentSetData?.intensity || 'Medium'
        });
    };

    const handleIntensityChange = (intensity: string) => {
        onUpdateSetData({
            ...currentSetData,
            actualHoldSeconds: holdTime, // 🔧 FIXED: Use actualHoldSeconds
            intensity
        });
    };

    const intensityOptions = ['Light', 'Medium', 'Hard', 'Maximum'];
    const selectedIntensity = currentSetData?.intensity || 'Medium';

    return (
        <Card className={`${typeStyle.bg} ${typeStyle.border} bg-gray-800`}>
            <CardContent className="p-6">
                <div className="space-y-6">
                    {/* Hold Timer Display */}
                    <div className="text-center">
                        <div className="text-6xl font-mono font-bold text-white mb-4">
                            {formatTime(holdTime)}
                        </div>

                        {/* Timer Controls */}
                        <div className="flex justify-center gap-3">
                            {!isHolding ? (
                                <Button
                                    onClick={handleStartHold}
                                    className={`${typeStyle.button} flex items-center gap-2`}
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
                                disabled={holdTime === 0}
                            >
                                <Square className="w-5 h-5"/>
                                Complete Set
                            </Button>
                        </div>
                    </div>

                    {/* Intensity Selector */}
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
                                >
                                    {intensity}
                                </Button>
                            ))}
                        </div>
                    </div>

                    {/* Manual Input Section */}
                    <div className="border-t border-gray-600 pt-6">
                        <h4 className="text-lg font-semibold text-white mb-4 text-center">
                            Or Enter Manually
                        </h4>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* Hold Time Input */}
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
                                />
                            </div>

                            {/* Intensity Dropdown */}
                            <div>
                                <label className="block text-sm font-medium text-gray-300 mb-1">
                                    Intensity Level
                                </label>
                                <select
                                    value={selectedIntensity}
                                    onChange={(e) => handleIntensityChange(e.target.value)}
                                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                                >
                                    {intensityOptions.map((intensity) => (
                                        <option key={intensity} value={intensity}>
                                            {intensity}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        {/* Manual Complete Button */}
                        <div className="mt-4 text-center">
                            <Button
                                onClick={() => {
                                    const holdSeconds = parseInt(manualHoldTime) || 0;
                                    console.log('🔧 Manually completing isometric set with hold time:', holdSeconds); // Debug log
                                    onSetComplete({
                                        actualHoldSeconds: holdSeconds, // 🔧 FIXED: Use actualHoldSeconds
                                        intensity: selectedIntensity
                                    });
                                }}
                                className={`${typeStyle.button} w-full md:w-auto`}
                                disabled={!manualHoldTime || parseInt(manualHoldTime) === 0}
                            >
                                Complete Set Manually
                            </Button>
                        </div>
                    </div>

                    {/* Progress Indicators */}
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
    );
};