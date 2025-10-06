import React, {useState} from 'react';
import {Plus, Minus} from 'lucide-react';
import {Button} from '../ui/button';
import {Input} from '../ui/input';
import {Card, CardContent} from '../ui/card';
import {useWorkoutMode} from '../../hooks/useWorkoutMode';
import {RestTimerBanner} from './RestTimerBanner';

interface StrengthTrackerProps {
    currentSetData: any;
    onSetComplete: (setData: any) => void;
    onUpdateSetData: (setData: any) => void;
    isLastSet?: boolean;
    typeStyle: {
        bg: string;
        border: string;
        button: string;
        text: string;
    };
}

export const StrengthTracker: React.FC<StrengthTrackerProps> = ({
                                                                    currentSetData,
                                                                    isLastSet,
                                                                    onSetComplete,
                                                                    onUpdateSetData,
                                                                    typeStyle
                                                                }) => {

    const workoutMode = useWorkoutMode();
    const {restStartTime, setRestStartTime, currentRestSeconds, setCurrentRestSeconds} = workoutMode;

    const [reps, setReps] = useState(currentSetData?.reps || 0);
    const [weight, setWeight] = useState(currentSetData?.weight || 0);
    const [isResting, setIsResting] = useState(false);

    console.log('StrengthTracker - currentSetData:', {
        restSeconds: currentSetData?.restSeconds,
        fullSetData: currentSetData
    });

    const handleRepsChange = (value: string) => {
        const newReps = parseInt(value) || 0;
        setReps(newReps);
        updateSetData(newReps, weight);
    };

    const handleWeightChange = (value: string) => {
        const newWeight = parseFloat(value) || 0;
        setWeight(newWeight);
        updateSetData(reps, newWeight);
    };

    const updateSetData = (newReps: number, newWeight: number) => {
        onUpdateSetData({
            ...currentSetData,
            reps: newReps,
            weight: newWeight
        });
    };

    const adjustReps = (delta: number) => {
        const newReps = Math.max(0, reps + delta);
        setReps(newReps);
        updateSetData(newReps, weight);
    };

    const adjustWeight = (delta: number) => {
        const newWeight = Math.max(0, weight + delta);
        setWeight(newWeight);
        updateSetData(reps, newWeight);
    };

    const handleCompleteSet = () => {
        if (reps > 0) {
            onSetComplete({
                actualReps: reps,
                actualWeight: weight,
                actualRpe: currentSetData?.targetRpe || 7,
                restSeconds: currentSetData?.restSeconds || 90,
            });

            // Only start rest if NOT the last set
            if (!isLastSet) {
                setIsResting(true);
                setRestStartTime(new Date());
                setCurrentRestSeconds(0);
            }

            // Reset for next set
            setReps(0);
            setWeight(0);
        }
    };

    const handleEndRest = () => {
        console.log('🔥 END REST - Storing rest time:', currentRestSeconds);
        if (currentRestSeconds > 0) {
            workoutMode.setRestTimeForNextSet(currentRestSeconds);
            console.log('✅ Rest time stored successfully');
        }

        setIsResting(false);
        setRestStartTime(null);
    };

    const handleSkipRest = () => {
        setIsResting(false);
        setRestStartTime(null);
        setCurrentRestSeconds(0);
    };

    const quickRepButtons = [8, 10, 12, 15, 20];
    const weightIncrements = [2.5, 5, 10, 25];

    return (
        <>
            <RestTimerBanner
                isResting={isResting}
                currentRestSeconds={currentRestSeconds}
                targetRestSeconds={currentSetData?.restSeconds || 90}
                onEndRest={handleEndRest}
                onSkipRest={handleSkipRest}
                exerciseType="strength"
            />

            <Card className={`${typeStyle.bg} ${typeStyle.border} bg-gray-800`}>
                <CardContent className="p-6">
                    <div className="space-y-6">
                        {/* Main Input Grid */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {/* Reps Section */}
                            <div className="space-y-4">
                                <div className="text-center">
                                    <h3 className="text-2xl font-bold text-white mb-2">Reps</h3>
                                    <div className="text-5xl font-mono font-bold text-white mb-4">
                                        {reps}
                                    </div>
                                </div>

                                <div className="flex justify-center gap-2">
                                    <Button
                                        onClick={() => adjustReps(-1)}
                                        variant="outline"
                                        size="sm"
                                        className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                        disabled={reps <= 0 || isResting}
                                    >
                                        <Minus className="w-4 h-4"/>
                                    </Button>
                                    <Input
                                        type="number"
                                        value={reps}
                                        onChange={(e) => handleRepsChange(e.target.value)}
                                        className="w-20 text-center bg-gray-700 border-gray-600 text-white"
                                        min="0"
                                        disabled={isResting}
                                    />
                                    <Button
                                        onClick={() => adjustReps(1)}
                                        variant="outline"
                                        size="sm"
                                        className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                        disabled={isResting}
                                    >
                                        <Plus className="w-4 h-4"/>
                                    </Button>
                                </div>

                                <div className="grid grid-cols-3 gap-2">
                                    {quickRepButtons.map((quickRep) => (
                                        <Button
                                            key={quickRep}
                                            onClick={() => {
                                                setReps(quickRep);
                                                updateSetData(quickRep, weight);
                                            }}
                                            variant="outline"
                                            size="sm"
                                            className={`${
                                                reps === quickRep
                                                    ? typeStyle.button
                                                    : "bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                            }`}
                                            disabled={isResting}
                                        >
                                            {quickRep}
                                        </Button>
                                    ))}
                                </div>
                            </div>

                            {/* Weight Section */}
                            <div className="space-y-4">
                                <div className="text-center">
                                    <h3 className="text-2xl font-bold text-white mb-2">Weight (lbs)</h3>
                                    <div className="text-5xl font-mono font-bold text-white mb-4">
                                        {weight}
                                    </div>
                                </div>

                                <div className="flex justify-center gap-2">
                                    <Button
                                        onClick={() => adjustWeight(-5)}
                                        variant="outline"
                                        size="sm"
                                        className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                        disabled={weight <= 0 || isResting}
                                    >
                                        <Minus className="w-4 h-4"/>
                                    </Button>
                                    <Input
                                        type="number"
                                        value={weight}
                                        onChange={(e) => handleWeightChange(e.target.value)}
                                        className="w-24 text-center bg-gray-700 border-gray-600 text-white"
                                        min="0"
                                        step="2.5"
                                        disabled={isResting}
                                    />
                                    <Button
                                        onClick={() => adjustWeight(5)}
                                        variant="outline"
                                        size="sm"
                                        className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                        disabled={isResting}
                                    >
                                        <Plus className="w-4 h-4"/>
                                    </Button>
                                </div>

                                <div className="grid grid-cols-2 gap-2">
                                    {weightIncrements.map((increment) => (
                                        <Button
                                            key={increment}
                                            onClick={() => adjustWeight(increment)}
                                            variant="outline"
                                            size="sm"
                                            className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                            disabled={isResting}
                                        >
                                            +{increment}
                                        </Button>
                                    ))}
                                </div>

                                <div className="space-y-2">
                                    <label className="block text-sm font-medium text-gray-300">
                                        Quick Weight Selection
                                    </label>
                                    <div className="grid grid-cols-3 gap-1">
                                        {[45, 95, 135, 185, 225, 275].map((preset) => (
                                            <Button
                                                key={preset}
                                                onClick={() => {
                                                    setWeight(preset);
                                                    updateSetData(reps, preset);
                                                }}
                                                variant="outline"
                                                size="sm"
                                                className={`text-xs ${
                                                    weight === preset
                                                        ? typeStyle.button
                                                        : "bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                                }`}
                                                disabled={isResting}
                                            >
                                                {preset}
                                            </Button>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Set Summary */}
                        <div className="border-t border-gray-600 pt-4">
                            <div className="text-center space-y-2">
                                <p className="text-lg text-gray-300">
                                    Current Set: <span className="text-white font-semibold">
                                        {reps} reps @ {weight} lbs
                                    </span>
                                </p>
                                {weight > 0 && reps > 0 && (
                                    <p className="text-sm text-gray-400">
                                        Total Volume: {(reps * weight).toLocaleString()} lbs
                                    </p>
                                )}
                            </div>
                        </div>

                        {/* Complete Set Button */}
                        <div className="text-center">
                            <Button
                                onClick={handleCompleteSet}
                                className={`${typeStyle.button} text-lg px-8 py-3`}
                                disabled={reps === 0 || isResting}
                            >
                                Complete Set
                            </Button>
                        </div>

                        {/* Additional Options */}
                        <div className="border-t border-gray-600 pt-4">
                            <div className="grid grid-cols-2 gap-4">
                                <Button
                                    onClick={() => {
                                        setReps(0);
                                        setWeight(0);
                                        updateSetData(0, 0);
                                    }}
                                    variant="outline"
                                    className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                    disabled={isResting}
                                >
                                    Reset
                                </Button>
                                <Button
                                    onClick={() => onSetComplete({
                                        actualReps: 0,
                                        actualWeight: 0,
                                        actualRpe: 0,
                                        skipped: true
                                    })}
                                    variant="outline"
                                    className="bg-gray-700 border-gray-600 hover:bg-gray-600 text-white"
                                    disabled={isResting}
                                >
                                    Skip Set
                                </Button>
                            </div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </>
    );
};