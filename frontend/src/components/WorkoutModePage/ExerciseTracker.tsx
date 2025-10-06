import React from 'react';
import {CardioTracker} from './CardioTracker';
import {IsometricTracker} from './IsometricTracker';
import {StrengthTracker} from './StrengthTracker';

interface ExerciseTrackerProps {
    exerciseType: string;
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

export const ExerciseTracker: React.FC<ExerciseTrackerProps> = ({
                                                                    exerciseType,
                                                                    isActive,
                                                                    currentSetData,
                                                                    isLastSet,
                                                                    onSetComplete,
                                                                    onUpdateSetData,
                                                                    typeStyle
                                                                }) => {
    const isCardio = exerciseType?.toLowerCase() === 'cardio';
    const isIsometric = exerciseType?.toLowerCase() === 'isometric';

    if (isCardio) {
        return (
            <CardioTracker
                isActive={isActive}
                currentSetData={currentSetData}
                onSetComplete={onSetComplete}
                onUpdateSetData={onUpdateSetData}
                typeStyle={typeStyle}
            />
        );
    }

    if (isIsometric) {
        return (
            <IsometricTracker
                isActive={isActive}
                currentSetData={currentSetData}
                isLastSet={isLastSet}
                onSetComplete={onSetComplete}
                onUpdateSetData={onUpdateSetData}
                typeStyle={typeStyle}
            />
        );
    }

    // Default to strength training
    return (
        <StrengthTracker
            currentSetData={currentSetData}
            isLastSet={isLastSet}
            onSetComplete={onSetComplete}
            onUpdateSetData={onUpdateSetData}
            typeStyle={typeStyle}
        />
    );
};