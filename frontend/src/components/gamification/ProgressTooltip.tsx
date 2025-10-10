// components/gamification/ProgressTooltip.tsx
import React from 'react';

interface ProgressTooltipProps {
    children: React.ReactNode;
    content: string;
    side?: 'top' | 'bottom' | 'left' | 'right';
}

export const ProgressTooltip: React.FC<ProgressTooltipProps> = ({
                                                                    children,
                                                                    content,
                                                                    side = 'bottom'
                                                                }) => {
    return (
        <div className="group relative inline-block">
            {children}
            <div className={`
                absolute hidden group-hover:block
                bg-gray-900 text-white text-xs px-3 py-2 rounded-lg
                whitespace-nowrap z-50 pointer-events-none
                ${side === 'bottom' ? 'top-full mt-2 left-1/2 -translate-x-1/2' : ''}
                ${side === 'top' ? 'bottom-full mb-2 left-1/2 -translate-x-1/2' : ''}
                ${side === 'left' ? 'right-full mr-2 top-1/2 -translate-y-1/2' : ''}
                ${side === 'right' ? 'left-full ml-2 top-1/2 -translate-y-1/2' : ''}
            `}>
                {content}
                {/* Arrow */}
                <div className={`
                    absolute w-2 h-2 bg-gray-900 rotate-45
                    ${side === 'bottom' ? 'bottom-full left-1/2 -translate-x-1/2 translate-y-1' : ''}
                    ${side === 'top' ? 'top-full left-1/2 -translate-x-1/2 -translate-y-1' : ''}
                    ${side === 'left' ? 'left-full top-1/2 -translate-y-1/2 -translate-x-1' : ''}
                    ${side === 'right' ? 'right-full top-1/2 -translate-y-1/2 translate-x-1' : ''}
                `}/>
            </div>
        </div>
    );
};