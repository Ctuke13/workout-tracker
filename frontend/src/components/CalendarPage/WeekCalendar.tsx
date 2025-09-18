import React from 'react';
import {Calendar} from 'lucide-react';
import {Card, CardContent, CardHeader, CardTitle} from '../ui/card';
import {ScheduledExercise} from '../../types/exercise';

interface WeekCalendarProps {
    viewingDate: Date;
    scheduledWorkouts: ScheduledExercise[];
    onDateSelect: (date: Date) => void;
}

export const WeekCalendar: React.FC<WeekCalendarProps> = ({
                                                              viewingDate,
                                                              scheduledWorkouts,
                                                              onDateSelect
                                                          }) => {
    const getWeekContext = () => {
        const startOfWeek = new Date(viewingDate);
        const day = startOfWeek.getDay();
        startOfWeek.setDate(viewingDate.getDate() - day);

        const weekDays = [];
        for (let i = 0; i < 7; i++) {
            const date = new Date(startOfWeek);
            date.setDate(startOfWeek.getDate() + i);
            const dateString = date.toISOString().split('T')[0];
            const dayExercises = scheduledWorkouts.filter(workout => workout.scheduledDate === dateString);

            weekDays.push({
                date,
                dateString,
                isViewing: date.toDateString() === viewingDate.toDateString(),
                isToday: date.toDateString() === new Date().toDateString(),
                exerciseCount: dayExercises.length,
                completedCount: dayExercises.filter(ex => ex.completed).length
            });
        }
        return weekDays;
    };

    return (
        <Card className="shadow-sm">
            <CardHeader className="pb-2 sm:pb-3">
                <CardTitle className="text-sm sm:text-base text-gray-600 flex items-center gap-2">
                    <Calendar className="w-4 h-4"/>
                    Week Overview
                </CardTitle>
            </CardHeader>
            <CardContent className="pt-0">
                <div className="grid grid-cols-7 gap-1 sm:gap-2">
                    {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((day, index) => (
                        <div key={day + index}
                             className="text-center text-xs sm:text-sm font-medium text-gray-500 pb-2">
                            <span className="sm:hidden">{day}</span>
                            <span className="hidden sm:inline">
                                {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][index]}
                            </span>
                        </div>
                    ))}
                    {getWeekContext().map((day, index) => (
                        <div
                            key={index}
                            className={`
                                p-2 sm:p-3 rounded-lg cursor-pointer transition-all duration-200 text-center min-h-[50px] sm:min-h-[60px] flex flex-col justify-center
                                ${day.isViewing ? 'bg-blue-500 text-white shadow-md scale-105' : 'bg-white hover:bg-gray-100'}
                                ${day.isToday && !day.isViewing ? 'border-2 border-blue-300' : 'border border-gray-200'}
                            `}
                            onClick={() => onDateSelect(day.date)}
                        >
                            <div
                                className={`text-sm sm:text-base font-bold ${day.isViewing ? 'text-white' : day.isToday ? 'text-blue-600' : 'text-gray-900'}`}>
                                {day.date.getDate()}
                            </div>
                            {day.exerciseCount > 0 && (
                                <div className="flex justify-center gap-0.5 mt-1">
                                    {Array.from({length: Math.min(day.exerciseCount, 4)}).map((_, idx) => (
                                        <div
                                            key={idx}
                                            className={`w-1 h-1 sm:w-1.5 sm:h-1.5 rounded-full ${
                                                day.isViewing
                                                    ? 'bg-white'
                                                    : idx < day.completedCount
                                                        ? 'bg-green-500'
                                                        : 'bg-blue-500'
                                            }`}
                                        />
                                    ))}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
};