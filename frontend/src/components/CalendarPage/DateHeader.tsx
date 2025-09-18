import React from 'react';
import {ChevronLeft, ChevronRight, RefreshCw} from 'lucide-react';
import {Button} from '../ui/button';

interface DateHeaderProps {
    viewingDate: Date;
    loading: boolean;
    viewingDateExercises: any[];
    onNavigateDay: (direction: 'prev' | 'next') => void;
    onGoToToday: () => void;
    onManualRefresh: () => void;
}

export const DateHeader: React.FC<DateHeaderProps> = ({
                                                          viewingDate,
                                                          loading,
                                                          viewingDateExercises,
                                                          onNavigateDay,
                                                          onGoToToday,
                                                          onManualRefresh
                                                      }) => {
    const getDateDisplayInfo = () => {
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(today.getDate() - 1);
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);

        if (viewingDate.toDateString() === today.toDateString()) {
            return {
                title: 'Today',
                subtitle: viewingDate.toLocaleDateString('en-US', {weekday: 'long', month: 'long', day: 'numeric'}),
                emoji: '🎯',
                bgColor: 'from-blue-500 to-green-500',
                textColor: 'text-white'
            };
        } else if (viewingDate.toDateString() === yesterday.toDateString()) {
            return {
                title: 'Yesterday',
                subtitle: viewingDate.toLocaleDateString('en-US', {weekday: 'long', month: 'long', day: 'numeric'}),
                emoji: '📅',
                bgColor: 'from-gray-400 to-gray-500',
                textColor: 'text-white'
            };
        } else if (viewingDate.toDateString() === tomorrow.toDateString()) {
            return {
                title: 'Tomorrow',
                subtitle: viewingDate.toLocaleDateString('en-US', {weekday: 'long', month: 'long', day: 'numeric'}),
                emoji: '✨',
                bgColor: 'from-purple-500 to-pink-500',
                textColor: 'text-white'
            };
        } else {
            return {
                title: viewingDate.toLocaleDateString('en-US', {weekday: 'long'}),
                subtitle: viewingDate.toLocaleDateString('en-US', {month: 'long', day: 'numeric', year: 'numeric'}),
                emoji: '📆',
                bgColor: 'from-gray-600 to-gray-700',
                textColor: 'text-white'
            };
        }
    };

    const isToday = () => {
        const today = new Date();
        return viewingDate.toDateString() === today.toDateString();
    };

    const dateInfo = getDateDisplayInfo();

    return (
        <div
            className={`bg-gradient-to-r ${dateInfo.bgColor} rounded-xl sm:rounded-2xl lg:rounded-3xl p-4 sm:p-6 lg:p-8 ${dateInfo.textColor} shadow-lg`}>
            <div className="text-center space-y-2 sm:space-y-3">
                <div className="text-3xl sm:text-4xl lg:text-6xl">{dateInfo.emoji}</div>
                <div>
                    <h1 className="text-xl sm:text-2xl lg:text-4xl font-bold">{dateInfo.title}</h1>
                    <p className="text-xs sm:text-sm lg:text-lg opacity-90 mt-1">{dateInfo.subtitle}</p>
                </div>

                {/* Day Navigation */}
                <div className="flex items-center justify-center gap-2 sm:gap-3 lg:gap-4 mt-4 sm:mt-6">
                    <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => onNavigateDay('prev')}
                        className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-2 sm:px-3 lg:px-4"
                    >
                        <ChevronLeft className="w-4 h-4"/>
                        <span className="hidden sm:inline ml-1">Yesterday</span>
                    </Button>

                    {!isToday() && (
                        <Button
                            variant="secondary"
                            size="sm"
                            onClick={onGoToToday}
                            className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-3 sm:px-4 lg:px-6"
                        >
                            Today
                        </Button>
                    )}

                    {/* Manual Refresh Button */}
                    <Button
                        variant="secondary"
                        size="sm"
                        onClick={onManualRefresh}
                        disabled={loading}
                        className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-2 sm:px-3 lg:px-4"
                        title="Refresh calendar data"
                    >
                        {loading ? (
                            <div
                                className="w-4 h-4 animate-spin rounded-full border-2 border-white border-t-transparent"/>
                        ) : (
                            <>
                                <RefreshCw className="w-4 h-4"/>
                                <span className="hidden sm:inline ml-1">Refresh</span>
                            </>
                        )}
                    </Button>

                    <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => onNavigateDay('next')}
                        className="bg-white/20 hover:bg-white/30 text-white border-white/20 px-2 sm:px-3 lg:px-4"
                    >
                        <span className="hidden sm:inline mr-1">Tomorrow</span>
                        <ChevronRight className="w-4 h-4"/>
                    </Button>
                </div>

                {/* Exercise Count Summary */}
                <div className="grid grid-cols-3 gap-3 sm:gap-4 lg:gap-6 mt-4 sm:mt-6 max-w-sm mx-auto">
                    <div className="text-center">
                        <div className="text-lg sm:text-xl lg:text-2xl font-bold">{viewingDateExercises.length}</div>
                        <div className="text-xs sm:text-sm opacity-80">Planned</div>
                    </div>
                    <div className="text-center">
                        <div
                            className="text-lg sm:text-xl lg:text-2xl font-bold">{viewingDateExercises.filter(ex => ex.completed).length}</div>
                        <div className="text-xs sm:text-sm opacity-80">Done</div>
                    </div>
                    <div className="text-center">
                        <div className="text-lg sm:text-xl lg:text-2xl font-bold">
                            {viewingDateExercises.reduce((total, ex) => total + (ex.exercise.estimatedDurationMinutes || 0), 0)}
                        </div>
                        <div className="text-xs sm:text-sm opacity-80">Minutes</div>
                    </div>
                </div>
            </div>
        </div>
    );
};