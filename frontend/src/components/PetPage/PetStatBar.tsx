import React from 'react';

interface PetStatBarProps {
    label: string;
    value: number;
    maxValue?: number;
    icon: string;
    colorClass: string;
    bgColorClass: string;
    inverse?: boolean; // For fatigue where lower is better
}

const PetStatBar: React.FC<PetStatBarProps> = ({
                                                   label,
                                                   value,
                                                   maxValue = 100,
                                                   icon,
                                                   colorClass,
                                                   bgColorClass,
                                                   inverse = false,
                                               }) => {
    // Handle null/undefined values
    const safeValue = value ?? 0;
    const percentage = Math.min(100, Math.max(0, (safeValue / maxValue) * 100));

    // Determine status color based on value (or inverse for fatigue)
    const getStatusColor = () => {
        const effectiveValue = inverse ? 100 - percentage : percentage;

        if (effectiveValue >= 70) return 'text-green-600';
        if (effectiveValue >= 40) return 'text-yellow-600';
        if (effectiveValue >= 20) return 'text-orange-600';
        return 'text-red-600';
    };

    return (
        <div className="flex items-center gap-3">
            {/* Icon */}
            <div className="text-xl w-6 text-center flex-shrink-0">
                {icon}
            </div>

            {/* Label & Bar */}
            <div className="flex-1 min-w-0">
                <div className="flex justify-between items-center mb-1">
                    <span className="text-sm font-medium text-gray-700">{label}</span>
                    <span className={`text-sm font-bold ${getStatusColor()}`}>
                        {Math.round(safeValue)}/{maxValue}
                    </span>
                </div>

                {/* Progress Bar */}
                <div className={`h-3 rounded-full ${bgColorClass} overflow-hidden shadow-inner`}>
                    <div
                        className={`h-full rounded-full ${colorClass} transition-all duration-500 ease-out`}
                        style={{width: `${percentage}%`}}
                    />
                </div>
            </div>
        </div>
    );
};

export default PetStatBar;