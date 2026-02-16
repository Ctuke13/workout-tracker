import React from 'react';
import PetStatBar from './PetStatBar';
import {PetStats} from '../../types/pet';

interface PetStatBarsProps {
    stats: PetStats;
}

const PetStatBars: React.FC<PetStatBarsProps> = ({stats}) => {
    return (
        <div className="bg-white/90 backdrop-blur-sm rounded-2xl p-4 shadow-lg space-y-3">
            {/* Fuel */}
            <PetStatBar
                label="Fuel"
                value={stats.fuel}
                icon="🍖"
                colorClass="bg-gradient-to-r from-amber-400 to-orange-500"
                bgColorClass="bg-amber-100"
            />

            {/* Motivation */}
            <PetStatBar
                label="Motivation"
                value={stats.motivation}
                icon="💪"
                colorClass="bg-gradient-to-r from-pink-400 to-rose-500"
                bgColorClass="bg-pink-100"
            />

            {/* Fatigue (inverse - lower is better) */}
            <PetStatBar
                label="Fatigue"
                value={stats.fatigue}
                icon="😴"
                colorClass="bg-gradient-to-r from-blue-400 to-indigo-500"
                bgColorClass="bg-blue-100"
                inverse={true}
            />

            {/* Cleanliness */}
            <PetStatBar
                label="Cleanliness"
                value={stats.cleanliness}
                icon="✨"
                colorClass="bg-gradient-to-r from-cyan-400 to-teal-500"
                bgColorClass="bg-cyan-100"
            />
        </div>
    );
};

export default PetStatBars;