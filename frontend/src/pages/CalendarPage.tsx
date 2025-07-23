// src/pages/CalendarPage.tsx - Complete Enhanced Calendar with Completion Status & Results Modal
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useWorkout } from '../contexts/WorkoutContext';
import { Exercise } from '../types/exercise';
import { useLocation, useNavigate } from 'react-router-dom';
import {
    ChevronLeftIcon,
    ChevronRightIcon,
    PlusIcon,
    CalendarDaysIcon,
    ClockIcon,
    XMarkIcon,
    PlayIcon,
    PencilIcon,
    Bars3Icon,
    TrashIcon,
    CheckBadgeIcon,
    EyeIcon,
    TrophyIcon,
    FireIcon,
    BoltIcon
} from '@heroicons/react/24/outline';

// Import mock data services and date utils
import { calendarMockApi, generateCalendarDays, CalendarDay, ScheduledExercise } from '../services/calendarMockData';
import { DateUtils } from '../utils/dateUtils';

// Import components
import ExerciseSelector from '../components/CalendarPage/ExerciseSelector';
import ExerciseConfigModal from '../components/CalendarPage/ExerciseConfigModal';

interface ExerciseConfiguration {
    sets: number;
    reps: string;
    weight?: number;
    restSeconds?: number;
    tempo?: string;
    targetRpe?: number;
    notes?: string;
}

const DEFAULT_CONFIG: ExerciseConfiguration = {
    sets: 3,
    reps: '10',
    weight: undefined,
    restSeconds: 90,
    tempo: '',
    targetRpe: 7,
    notes: ''
};

// Enhanced Exercise Card Component with Completion Status
const EnhancedExerciseCard: React.FC<{
    exercise: ScheduledExercise;
    onEdit: () => void;
    onRemove: () => void;
    onViewResults: () => void;
    isSelected: boolean;
    onTouchStart: () => void;
    onTouchEnd: () => void;
    onDoubleClick: () => void;
    onDragStart?: (e: React.DragEvent<HTMLDivElement>) => void;
    onDragEnd?: (e: React.DragEvent<HTMLDivElement>) => void;
    isMobile: boolean;
}> = ({
          exercise,
          onEdit,
          onRemove,
          onViewResults,
          isSelected,
          onTouchStart,
          onTouchEnd,
          onDoubleClick,
          onDragStart,
          onDragEnd,
          isMobile
      }) => {
    const isCompleted = exercise.completed;


    return (
        <div
            draggable={!isMobile}
            onDragStart={!isMobile ? onDragStart : undefined}
            onDragEnd={!isMobile ? onDragEnd : undefined}
            onDoubleClick={!isMobile ? onDoubleClick : undefined}
            onTouchStart={isMobile ? onTouchStart : undefined}
            onTouchEnd={isMobile ? onTouchEnd : undefined}
            className={`
                group relative rounded-lg p-3 border transition-all duration-200
                ${!isMobile ? 'hover:border-gray-300 cursor-move' : 'active:scale-[0.98]'}
                ${isSelected ? 'ring-2 ring-blue-500 bg-blue-50' : ''}
                ${isCompleted
                ? 'border-green-500 bg-gradient-to-r from-green-50 to-emerald-50'
                : 'border-gray-200 bg-gray-50'
            }
            `}
        >
            {/* Completion Badge */}
            {isCompleted && (
                <div className="absolute -top-2 -right-2 z-10">
                    <div className="bg-green-500 text-white rounded-full p-1 shadow-lg">
                        <CheckBadgeIcon className="w-4 h-4" />
                    </div>
                </div>
            )}

            {/* Exercise Header */}
            <div className="flex items-center justify-between mb-2">
                <div className="flex items-center min-w-0 flex-1">
                    <span className="text-base sm:text-lg mr-2 flex-shrink-0">
                        {exercise.exercise.emoji || '💪'}
                    </span>
                    <span className={`text-xs sm:text-sm font-medium truncate ${
                        isCompleted ? 'text-green-800' : 'text-gray-800'
                    }`}>
                        {exercise.exercise.exerciseName || exercise.exercise.name}
                    </span>
                </div>

                {/* Desktop Action Buttons */}
                {!isMobile && (
                    <div className="flex items-center space-x-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        {isCompleted && (
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onViewResults();
                                }}
                                className="p-1 text-green-600 hover:text-green-700 transition-colors"
                                title="View results"
                            >
                                <EyeIcon className="w-3 h-3" />
                            </button>
                        )}
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onEdit();
                            }}
                            className="p-1 text-gray-400 hover:text-blue-600 transition-colors"
                            title="Edit exercise"
                        >
                            <PencilIcon className="w-3 h-3" />
                        </button>
                    </div>
                )}
            </div>

            {/* Exercise Details */}
            <div className="text-xs text-gray-600 mb-2">
                <div className="flex items-center justify-between">
                    <span className={isCompleted ? 'text-green-700' : ''}>
                        {exercise.sets} × {exercise.reps}
                        {exercise.weight && ` @ ${exercise.weight}kg`}
                    </span>
                    {exercise.targetRpe && (
                        <span className={`font-medium ${
                            isCompleted ? 'text-green-600' : 'text-orange-600'
                        }`}>
                            RPE {exercise.targetRpe}
                        </span>
                    )}
                </div>

                {/* Completion Status */}
                {isCompleted && (
                    <div className="mt-1 flex items-center text-green-700">
                        <TrophyIcon className="w-3 h-3 mr-1" />
                        <span className="text-xs font-medium">Completed</span>
                    </div>
                )}
            </div>

            {/* Mobile Action Buttons */}
            {isMobile && isSelected && (
                <div className="absolute inset-x-0 -bottom-1 bg-white border border-gray-200 rounded-b-lg shadow-lg z-10">
                    <div className="flex divide-x divide-gray-200">
                        {isCompleted && (
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onViewResults();
                                }}
                                className="flex-1 flex items-center justify-center py-2 text-green-600 hover:bg-green-50 transition-colors"
                            >
                                <EyeIcon className="w-4 h-4 mr-1" />
                                <span className="text-xs font-medium">Results</span>
                            </button>
                        )}
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onEdit();
                            }}
                            className="flex-1 flex items-center justify-center py-2 text-blue-600 hover:bg-blue-50 transition-colors"
                        >
                            <PencilIcon className="w-4 h-4 mr-1" />
                            <span className="text-xs font-medium">Edit</span>
                        </button>
                        <button
                            onClick={(e) => {
                                e.stopPropagation();
                                onRemove();
                            }}
                            className="flex-1 flex items-center justify-center py-2 text-red-600 hover:bg-red-50 rounded-br-lg transition-colors"
                        >
                            <TrashIcon className="w-4 h-4 mr-1" />
                            <span className="text-xs font-medium">Delete</span>
                        </button>
                    </div>
                </div>
            )}

            {/* Desktop Drag Handle */}
            {!isMobile && (
                <div className="flex justify-center mt-1">
                    <Bars3Icon className="w-3 h-3 sm:w-4 sm:h-4 text-gray-300" />
                </div>
            )}
        </div>
    );
};

// Exercise Results Modal Component
const ExerciseResultsModal: React.FC<{
    exercise: ScheduledExercise | null;
    open: boolean;
    onClose: () => void;
}> = ({ exercise, open, onClose }) => {
    if (!open || !exercise) return null;

    // Mock workout results data (in real app, this would come from workout history)
    const mockResults = {
        completedSets: exercise.sets,
        totalReps: exercise.sets * parseInt(exercise.reps),
        averageRpe: exercise.targetRpe || 7,
        duration: 15, // minutes
        notes: "Great workout! Felt strong today."
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-md w-full p-6 max-h-[90vh] overflow-y-auto">
                {/* Header */}
                <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center">
                        <span className="text-2xl mr-3">
                            {exercise.exercise.emoji || '💪'}
                        </span>
                        <div>
                            <h2 className="text-xl font-bold text-gray-900">
                                {exercise.exercise.exerciseName || exercise.exercise.name}
                            </h2>
                            <p className="text-sm text-gray-500">
                                {DateUtils.formatDisplayDate(exercise.scheduledDate)}
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        <XMarkIcon className="w-5 h-5" />
                    </button>
                </div>

                {/* Completion Badge */}
                <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
                    <div className="flex items-center">
                        <CheckBadgeIcon className="w-6 h-6 text-green-600 mr-3" />
                        <div>
                            <h3 className="font-semibold text-green-800">Exercise Completed!</h3>
                            <p className="text-sm text-green-600">Great job on finishing this workout</p>
                        </div>
                    </div>
                </div>

                {/* Results Grid */}
                <div className="grid grid-cols-2 gap-4 mb-6">
                    <div className="bg-blue-50 rounded-lg p-4 text-center">
                        <div className="text-2xl font-bold text-blue-600">{mockResults.completedSets}</div>
                        <div className="text-sm text-blue-800">Sets Completed</div>
                    </div>
                    <div className="bg-green-50 rounded-lg p-4 text-center">
                        <div className="text-2xl font-bold text-green-600">{mockResults.totalReps}</div>
                        <div className="text-sm text-green-800">Total Reps</div>
                    </div>
                    <div className="bg-orange-50 rounded-lg p-4 text-center">
                        <div className="text-2xl font-bold text-orange-600">{mockResults.averageRpe}</div>
                        <div className="text-sm text-orange-800">Avg RPE</div>
                    </div>
                    <div className="bg-purple-50 rounded-lg p-4 text-center">
                        <div className="text-2xl font-bold text-purple-600">{mockResults.duration}m</div>
                        <div className="text-sm text-purple-800">Duration</div>
                    </div>
                </div>

                {/* Exercise Details */}
                <div className="border border-gray-200 rounded-lg p-4 mb-6">
                    <h4 className="font-semibold text-gray-900 mb-3">Exercise Details</h4>
                    <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                            <span className="text-gray-600">Planned Sets:</span>
                            <span className="font-medium">{exercise.sets}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">Planned Reps:</span>
                            <span className="font-medium">{exercise.reps}</span>
                        </div>
                        {exercise.weight && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">Weight:</span>
                                <span className="font-medium">{exercise.weight}kg</span>
                            </div>
                        )}
                        {exercise.targetRpe && (
                            <div className="flex justify-between">
                                <span className="text-gray-600">Target RPE:</span>
                                <span className="font-medium">{exercise.targetRpe}</span>
                            </div>
                        )}
                    </div>
                </div>

                {/* Notes */}
                {mockResults.notes && (
                    <div className="border border-gray-200 rounded-lg p-4 mb-6">
                        <h4 className="font-semibold text-gray-900 mb-2">Notes</h4>
                        <p className="text-sm text-gray-600">{mockResults.notes}</p>
                    </div>
                )}

                {/* Close Button */}
                <button
                    onClick={onClose}
                    className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
                >
                    Close
                </button>
            </div>
        </div>
    );
};

// Calendar Day Card Component
interface CalendarDayCardProps {
    day: CalendarDay;
    canAddExercise: boolean;
    remainingExercises: number;
    onAddExercise: () => void;
    onRemoveExercise: (exerciseId: string) => void;
    onEditExercise: (exercise: ScheduledExercise) => void;
    onViewResults: (exercise: ScheduledExercise) => void;
    onTouchStart: (exercise: ScheduledExercise) => void;
    onTouchEnd: (exercise: ScheduledExercise) => void;
    onDoubleClickExercise: (exercise: ScheduledExercise) => void;
    onDragStart: (e: React.DragEvent<HTMLDivElement>, exercise: ScheduledExercise) => void;
    onDragEnd: (e: React.DragEvent<HTMLDivElement>) => void;
    onDragOver: (e: React.DragEvent<HTMLDivElement>) => void;
    onDragLeave: (e: React.DragEvent<HTMLDivElement>) => void;
    onDrop: (e: React.DragEvent<HTMLDivElement>) => void;
    userTier: string;
    isProfessional: boolean;
    isDraggedOver: boolean;
    selectedExerciseForActions: string | null;
}

const CalendarDayCard: React.FC<CalendarDayCardProps> = ({
                                                             day,
                                                             canAddExercise,
                                                             remainingExercises,
                                                             onAddExercise,
                                                             onRemoveExercise,
                                                             onEditExercise,
                                                             onViewResults,
                                                             onTouchStart,
                                                             onTouchEnd,
                                                             onDoubleClickExercise,
                                                             onDragStart,
                                                             onDragEnd,
                                                             onDragOver,
                                                             onDragLeave,
                                                             onDrop,
                                                             userTier,
                                                             isProfessional,
                                                             isDraggedOver,
                                                             selectedExerciseForActions
                                                         }) => {
    const isFreeUser = userTier === 'REGULAR' && !isProfessional;
    const isMobile = window.innerWidth < 768;
    const completedCount = day.exercises.filter(ex => ex.completed).length;
    const totalCount = day.exercises.length;

    return (
        <div
            className={`
                bg-white rounded-lg p-2 sm:p-3 md:p-4 min-h-[160px] sm:min-h-[180px] md:min-h-[200px] border-2 border-dashed transition-all duration-200
                ${day.isToday ? 'border-blue-500 bg-blue-50' : 'border-gray-300'}
                ${day.isPast ? 'opacity-70' : ''}
                ${isDraggedOver ? 'border-green-500 bg-green-50' : ''}
                hover:shadow-md
            `}
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={onDrop}
        >
            {/* Day Header */}
            <div className="flex justify-between items-center mb-2 sm:mb-3">
                <span className="text-xs sm:text-sm text-gray-500">
                    {day.date.toLocaleDateString('en-US', { weekday: 'short' })}
                </span>
                <div className="flex items-center space-x-2">
                    <span className={`text-sm sm:text-base font-semibold ${
                        day.isToday ? 'text-blue-600' : 'text-gray-700'
                    }`}>
                        {day.date.getDate()}
                    </span>
                    {day.isToday && (
                        <span className="text-xs bg-blue-100 text-blue-600 px-1 rounded">
                            Today
                        </span>
                    )}
                    {/* Completion indicator */}
                    {totalCount > 0 && (
                        <div className="flex items-center">
                            {completedCount === totalCount ? (
                                <CheckBadgeIcon className="w-4 h-4 text-green-500" title="All exercises completed" />
                            ) : completedCount > 0 ? (
                                <div className="w-4 h-4 rounded-full bg-yellow-400 flex items-center justify-center">
                                    <span className="text-xs text-white font-bold">{completedCount}</span>
                                </div>
                            ) : null}
                        </div>
                    )}
                </div>
            </div>

            {/* Exercise List */}
            <div className="space-y-1 sm:space-y-2 mb-2 sm:mb-3 flex-1">
                {day.exercises.map((exercise) => (
                    <EnhancedExerciseCard
                        key={exercise.id}
                        exercise={exercise}
                        onEdit={() => onEditExercise(exercise)}
                        onRemove={() => onRemoveExercise(exercise.id)}
                        onViewResults={() => onViewResults(exercise)}
                        isSelected={selectedExerciseForActions === exercise.id}
                        onTouchStart={() => onTouchStart(exercise)}
                        onTouchEnd={() => onTouchEnd(exercise)}
                        onDoubleClick={() => onDoubleClickExercise(exercise)}
                        onDragStart={!isMobile ? (e) => onDragStart(e, exercise) : undefined}
                        onDragEnd={!isMobile ? onDragEnd : undefined}
                        isMobile={isMobile}
                    />
                ))}
            </div>

            {/* Add Exercise Button */}
            <div className="mt-auto">
                {canAddExercise ? (
                    <button
                        onClick={onAddExercise}
                        className="w-full py-2 px-3 sm:px-4 border-2 border-dashed border-gray-300 text-gray-500 rounded-lg hover:border-blue-400 hover:text-blue-600 transition-colors text-xs sm:text-sm font-medium active:scale-[0.98]"
                    >
                        <PlusIcon className="w-3 h-3 sm:w-4 sm:h-4 mx-auto mb-1" />
                        Add Exercise
                    </button>
                ) : (
                    <div className="w-full py-2 px-3 sm:px-4 text-center">
                        <button
                            disabled
                            className="w-full py-2 px-3 sm:px-4 border border-gray-300 text-gray-500 rounded-lg cursor-not-allowed text-xs sm:text-sm font-medium"
                        >
                            Day Full
                        </button>
                        {isFreeUser && (
                            <p className="text-xs text-orange-600 mt-1">
                                Free tier: {remainingExercises}/4 exercises
                            </p>
                        )}
                    </div>
                )}
            </div>

            {/* Exercise Count Display with Completion */}
            {day.exercises.length > 0 && (
                <div className="mt-2 text-center">
                    <span className="text-xs text-gray-500">
                        {completedCount === totalCount ? (
                            <span className="text-green-600 font-medium">
                                ✓ All {totalCount} completed
                            </span>
                        ) : (
                            <>
                                {completedCount}/{totalCount} completed
                                {isFreeUser && ` (${remainingExercises} remaining)`}
                            </>
                        )}
                    </span>
                </div>
            )}
        </div>
    );
};

// Mobile Horizontal Week View Component
interface HorizontalWeekViewProps {
    calendarDays: CalendarDay[];
    today: Date;
    canAddExercise: (dateString: string) => boolean;
    getRemainingExercises: (dateString: string) => number;
    onAddExercise: (dateString?: string) => void;
    onRemoveExercise: (dateString: string, exerciseId: string) => void;
    onEditExercise: (exercise: ScheduledExercise, dateString: string) => void;
    onViewResults: (exercise: ScheduledExercise) => void;
    onTouchStart: (exercise: ScheduledExercise, dateString: string) => void;
    onTouchEnd: (exercise: ScheduledExercise, dateString: string) => void;
    userTier: string;
    isProfessional: boolean;
    selectedExerciseForActions: string | null;
}

const HorizontalWeekView: React.FC<HorizontalWeekViewProps> = ({
                                                                   calendarDays,
                                                                   today,
                                                                   canAddExercise,
                                                                   getRemainingExercises,
                                                                   onAddExercise,
                                                                   onRemoveExercise,
                                                                   onEditExercise,
                                                                   onViewResults,
                                                                   onTouchStart,
                                                                   onTouchEnd,
                                                                   userTier,
                                                                   isProfessional,
                                                                   selectedExerciseForActions
                                                               }) => {
    const scrollContainerRef = useRef<HTMLDivElement>(null);

    // Auto-scroll to today on load
    useEffect(() => {
        if (scrollContainerRef.current && calendarDays.length > 0) {
            const todayIndex = calendarDays.findIndex(day => day.isToday);
            if (todayIndex !== -1) {
                const cardWidth = 280; // Approximate card width + gap
                const screenWidth = window.innerWidth;
                const scrollPosition = Math.max(0, (todayIndex * cardWidth) - (screenWidth / 2) + (cardWidth / 2));

                setTimeout(() => {
                    scrollContainerRef.current?.scrollTo({
                        left: scrollPosition,
                        behavior: 'smooth'
                    });
                }, 100);
            }
        }
    }, [calendarDays]);

    return (
        <>
            {/* Week Navigation Header */}
            <div className="flex items-center justify-between mb-3 px-3">
                <h3 className="text-sm font-medium text-gray-700">This Week</h3>
                <div className="flex items-center space-x-1">
                    {calendarDays.map((day, index) => (
                        <div
                            key={day.dateString}
                            className={`w-2 h-2 rounded-full transition-colors ${
                                day.isToday ? 'bg-blue-600' : 'bg-gray-300'
                            }`}
                        />
                    ))}
                </div>
            </div>

            {/* Horizontal Scrolling Container */}
            <div
                ref={scrollContainerRef}
                className="flex space-x-3 overflow-x-auto px-3 pb-2"
                style={{
                    scrollSnapType: 'x mandatory',
                    WebkitOverflowScrolling: 'touch',
                    scrollbarWidth: 'none',
                    msOverflowStyle: 'none'
                }}
            >
                {calendarDays.map((day) => (
                    <div
                        key={day.dateString}
                        className="flex-shrink-0 w-72"
                        style={{ scrollSnapAlign: 'center' }}
                    >
                        <MobileCalendarCard
                            day={day}
                            canAddExercise={canAddExercise(day.dateString)}
                            remainingExercises={getRemainingExercises(day.dateString)}
                            onAddExercise={() => onAddExercise(day.dateString)}
                            onRemoveExercise={(exerciseId) => onRemoveExercise(day.dateString, exerciseId)}
                            onEditExercise={(exercise) => onEditExercise(exercise, day.dateString)}
                            onViewResults={onViewResults}
                            onTouchStart={(exercise) => onTouchStart(exercise, day.dateString)}
                            onTouchEnd={(exercise) => onTouchEnd(exercise, day.dateString)}
                            userTier={userTier}
                            isProfessional={isProfessional}
                            selectedExerciseForActions={selectedExerciseForActions}
                        />
                    </div>
                ))}
            </div>

            {/* Scroll Hint */}
            <div className="text-center mt-2">
                <p className="text-xs text-gray-500">← Swipe to see other days →</p>
            </div>
        </>
    );
};

// Mobile-Optimized Calendar Card Component
interface MobileCalendarCardProps {
    day: CalendarDay;
    canAddExercise: boolean;
    remainingExercises: number;
    onAddExercise: () => void;
    onRemoveExercise: (exerciseId: string) => void;
    onEditExercise: (exercise: ScheduledExercise) => void;
    onViewResults: (exercise: ScheduledExercise) => void;
    onTouchStart: (exercise: ScheduledExercise) => void;
    onTouchEnd: (exercise: ScheduledExercise) => void;
    userTier: string;
    isProfessional: boolean;
    selectedExerciseForActions: string | null;
}

const MobileCalendarCard: React.FC<MobileCalendarCardProps> = ({
                                                                   day,
                                                                   canAddExercise,
                                                                   remainingExercises,
                                                                   onAddExercise,
                                                                   onRemoveExercise,
                                                                   onEditExercise,
                                                                   onViewResults,
                                                                   onTouchStart,
                                                                   onTouchEnd,
                                                                   userTier,
                                                                   isProfessional,
                                                                   selectedExerciseForActions
                                                               }) => {
    const isFreeUser = userTier === 'REGULAR' && !isProfessional;
    const completedCount = day.exercises.filter(ex => ex.completed).length;
    const totalCount = day.exercises.length;

    const handleActionClick = (e: React.MouseEvent | React.TouchEvent, action: () => void) => {
        e.preventDefault();
        e.stopPropagation();
        action();
    };

    return (
        <div className={`
            bg-white rounded-lg p-4 min-h-[300px] border-2 transition-all duration-200 shadow-sm
            ${day.isToday ? 'border-blue-500 bg-blue-50 shadow-md' : 'border-gray-200'}
            ${day.isPast ? 'opacity-70' : ''}
        `}>
            {/* Day Header */}
            <div className="flex items-center justify-between mb-4">
                <div>
                    <h4 className="text-sm font-medium text-gray-600">
                        {day.date.toLocaleDateString('en-US', { weekday: 'short' })}
                    </h4>
                    <p className="text-2xl font-bold text-gray-800">
                        {day.date.getDate()}
                    </p>
                </div>
                <div className="flex flex-col items-end space-y-1">
                    {day.isToday && (
                        <span className="px-2 py-1 bg-blue-600 text-white text-xs font-medium rounded-full">
                            Today
                        </span>
                    )}
                    {totalCount > 0 && (
                        <div className="flex items-center space-x-1">
                            <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs font-medium rounded-full">
                                {totalCount} exercise{totalCount !== 1 ? 's' : ''}
                            </span>
                            {completedCount === totalCount && totalCount > 0 && (
                                <CheckBadgeIcon className="w-4 h-4 text-green-500" />
                            )}
                        </div>
                    )}
                </div>
            </div>

            {/* Exercise List */}
            <div className="space-y-2 mb-4 flex-1">
                {day.exercises.map((exercise) => (
                    <div
                        key={exercise.id}
                        onTouchStart={() => onTouchStart(exercise)}
                        onTouchEnd={() => onTouchEnd(exercise)}
                        className={`
                            rounded-lg p-3 border transition-all duration-200 active:scale-[0.98] relative
                            ${selectedExerciseForActions === exercise.id ? 'ring-2 ring-blue-500 bg-blue-50' : ''}
                            ${exercise.completed
                            ? 'border-green-500 bg-gradient-to-r from-green-50 to-emerald-50'
                            : 'border-gray-200 bg-gray-50'
                        }
                        `}
                    >
                        {/* Completion Badge */}
                        {exercise.completed && (
                            <div className="absolute -top-1 -right-1">
                                <CheckBadgeIcon className="w-4 h-4 text-green-500" />
                            </div>
                        )}

                        <div className="flex items-center justify-between mb-1">
                            <div className="flex items-center min-w-0 flex-1">
                                <span className="text-lg mr-2 flex-shrink-0">
                                    {exercise.exercise.emoji || '💪'}
                                </span>
                                <span className={`text-sm font-medium truncate ${
                                    exercise.completed ? 'text-green-800' : 'text-gray-800'
                                }`}>
                                    {exercise.exercise.exerciseName || exercise.exercise.name}
                                </span>
                            </div>
                        </div>

                        <div className="text-xs text-gray-600 mb-2">
                            <div className="flex items-center justify-between">
                                <span className={exercise.completed ? 'text-green-700' : ''}>
                                    {exercise.sets} × {exercise.reps}
                                    {exercise.weight && ` @ ${exercise.weight}kg`}
                                </span>
                                {exercise.targetRpe && (
                                    <span className={`font-medium ${
                                        exercise.completed ? 'text-green-600' : 'text-orange-600'
                                    }`}>
                                        RPE {exercise.targetRpe}
                                    </span>
                                )}
                            </div>

                            {/* Completion Status */}
                            {exercise.completed && (
                                <div className="mt-1 flex items-center text-green-700">
                                    <TrophyIcon className="w-3 h-3 mr-1" />
                                    <span className="text-xs font-medium">Completed</span>
                                </div>
                            )}
                        </div>

                        {/* Mobile Action Buttons */}
                        {selectedExerciseForActions === exercise.id && (
                            <div className="flex space-x-2">
                                {exercise.completed && (
                                    <button
                                        onTouchStart={(e) => e.stopPropagation()}
                                        onTouchEnd={(e) => handleActionClick(e, () => onViewResults(exercise))}
                                        onClick={(e) => handleActionClick(e, () => onViewResults(exercise))}
                                        className="flex-1 flex items-center justify-center py-2 text-green-600 bg-green-50 rounded-md text-xs font-medium transition-colors"
                                    >
                                        <EyeIcon className="w-4 h-4 mr-1" />
                                        Results
                                    </button>
                                )}
                                <button
                                    onTouchStart={(e) => e.stopPropagation()}
                                    onTouchEnd={(e) => handleActionClick(e, () => onEditExercise(exercise))}
                                    onClick={(e) => handleActionClick(e, () => onEditExercise(exercise))}
                                    className="flex-1 flex items-center justify-center py-2 text-blue-600 bg-blue-50 rounded-md text-xs font-medium transition-colors"
                                >
                                    <PencilIcon className="w-4 h-4 mr-1" />
                                    Edit
                                </button>
                                <button
                                    onTouchStart={(e) => e.stopPropagation()}
                                    onTouchEnd={(e) => handleActionClick(e, () => onRemoveExercise(exercise.id))}
                                    onClick={(e) => handleActionClick(e, () => onRemoveExercise(exercise.id))}
                                    className="flex-1 flex items-center justify-center py-2 text-red-600 bg-red-50 rounded-md text-xs font-medium transition-colors"
                                >
                                    <TrashIcon className="w-4 h-4 mr-1" />
                                    Delete
                                </button>
                            </div>
                        )}
                    </div>
                ))}

                {/* Empty State */}
                {day.exercises.length === 0 && (
                    <div className="text-center py-8">
                        <div className="text-gray-400 text-3xl mb-2">📝</div>
                        <p className="text-gray-500 text-sm">No exercises planned</p>
                    </div>
                )}
            </div>

            {/* Add Exercise Button */}
            <div className="mt-auto">
                {canAddExercise ? (
                    <button
                        onClick={onAddExercise}
                        className="w-full py-3 px-4 border-2 border-dashed border-gray-300 text-gray-500 rounded-lg hover:border-blue-400 hover:text-blue-600 transition-colors text-sm font-medium active:scale-[0.98]"
                    >
                        <PlusIcon className="w-5 h-5 mx-auto mb-1" />
                        Add Exercise
                    </button>
                ) : (
                    <div className="w-full py-3 px-4 text-center">
                        <div className="text-sm text-gray-500 font-medium">Day Full</div>
                        {isFreeUser && (
                            <p className="text-xs text-orange-600 mt-1">
                                Free tier: {remainingExercises}/4 exercises
                            </p>
                        )}
                    </div>
                )}
            </div>

            {/* Exercise Count Display with Completion */}
            {totalCount > 0 && (
                <div className="mt-2 text-center">
                    <span className="text-xs text-gray-500">
                        {completedCount === totalCount ? (
                            <span className="text-green-600 font-medium">
                                ✓ All {totalCount} completed
                            </span>
                        ) : (
                            <>
                                {completedCount}/{totalCount} completed
                                {isFreeUser && ` (${remainingExercises} remaining)`}
                            </>
                        )}
                    </span>
                </div>
            )}
        </div>
    );
};

// Main Calendar Page Component
const CalendarPage: React.FC = () => {
    const { user } = useAuth();
    const { startWorkout } = useWorkout();
    const location = useLocation();
    const navigate = useNavigate();

    // Get current date using DateUtils for proper timezone handling
    const today = new Date();
    const [currentDate, setCurrentDate] = useState(today);
    const [calendarDays, setCalendarDays] = useState<CalendarDay[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Modal states
    const [exerciseSelectorOpen, setExerciseSelectorOpen] = useState(false);
    const [configModalOpen, setConfigModalOpen] = useState(false);
    const [selectedDate, setSelectedDate] = useState<string | null>(null);
    const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
    const [exerciseConfig, setExerciseConfig] = useState<ExerciseConfiguration>(DEFAULT_CONFIG);
    const [editingExerciseId, setEditingExerciseId] = useState<string | null>(null);

    // Results modal state
    const [showResultsModal, setShowResultsModal] = useState(false);
    const [selectedExerciseForResults, setSelectedExerciseForResults] = useState<ScheduledExercise | null>(null);

    // Mobile-specific states
    const [touchStartTime, setTouchStartTime] = useState<number>(0);
    const [selectedExerciseForActions, setSelectedExerciseForActions] = useState<string | null>(null);

    // Drag and drop state
    const [draggedExercise, setDraggedExercise] = useState<ScheduledExercise | null>(null);
    const [dragOverDay, setDragOverDay] = useState<string | null>(null);

    // Check if coming from WelcomePage with auto-open
    useEffect(() => {
        const state = location.state as any;
        if (state?.openExerciseSelector && state?.selectedDate) {
            setSelectedDate(state.selectedDate);
            setExerciseSelectorOpen(true);
            // Clear the state to prevent reopening
            navigate(location.pathname, { replace: true, state: {} });
        }

        // Check if returning from completed workout
        if (state?.workoutCompleted) {
            console.log('🎉 Workout completed! Updating exercise status...');
            // In a real app, you'd mark exercises as completed here
            // For now, we'll just clear the state
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [location.state, navigate, location.pathname]);

    // Load calendar data using mock API with fixed date handling
    const loadCalendarData = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            console.log('📅 Loading calendar data for week starting:', currentDate);

            // Generate week starting from Sunday of the current week using DateUtils
            const startOfWeek = DateUtils.getStartOfWeek(currentDate);

            // Load week data using mock API
            const weekDays = await generateCalendarDays(startOfWeek, 7);
            setCalendarDays(weekDays);

            console.log('✅ Calendar data loaded:', weekDays);
        } catch (err) {
            console.error('❌ Failed to load calendar data:', err);
            setError('Failed to load calendar. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [currentDate]);

    useEffect(() => {
        loadCalendarData();
    }, [loadCalendarData]);

    // Navigation
    const navigatePrevious = () => {
        const newDate = new Date(currentDate);
        newDate.setDate(newDate.getDate() - 7);
        setCurrentDate(newDate);
    };

    const navigateNext = () => {
        const newDate = new Date(currentDate);
        newDate.setDate(newDate.getDate() + 7);
        setCurrentDate(newDate);
    };

    const formatDateHeader = () => {
        const startOfWeek = calendarDays[0]?.date;
        const endOfWeek = calendarDays[6]?.date;
        if (startOfWeek && endOfWeek) {
            return {
                mobile: `${startOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} - ${endOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}`,
                desktop: `${startOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} - ${endOfWeek.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`
            };
        }
        return {
            mobile: currentDate.toLocaleDateString('en-US', { month: 'short', year: 'numeric' }),
            desktop: currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
        };
    };

    // Check user tier limits
    const canAddExercise = (dateString: string): boolean => {
        const dayExercises = calendarDays.find(day => day.dateString === dateString)?.exercises || [];

        // Free tier limit: 4 exercises per day
        if (user?.userType === 'REGULAR' && !user?.isProfessional) {
            return dayExercises.length < 4;
        }

        return true; // No limit for paid tiers
    };

    const getRemainingExercises = (dateString: string): number => {
        const dayExercises = calendarDays.find(day => day.dateString === dateString)?.exercises || [];

        if (user?.userType === 'REGULAR' && !user?.isProfessional) {
            return Math.max(0, 4 - dayExercises.length);
        }

        return 999; // Unlimited for paid tiers
    };

    // Exercise management using mock API
    const handleExerciseSelect = (exercise: Exercise) => {
        setSelectedExercise(exercise);
        setExerciseConfig(DEFAULT_CONFIG);
        setConfigModalOpen(true);
        setExerciseSelectorOpen(false);
    };

    const handleAddExercise = async () => {
        if (!selectedExercise || !selectedDate) return;

        setLoading(true);
        try {
            console.log('➕ Adding exercise to calendar:', {
                exercise: selectedExercise,
                date: selectedDate,
                config: exerciseConfig
            });

            if (editingExerciseId) {
                // Update existing exercise
                const updatedExercise = await calendarMockApi.updateScheduledExercise(editingExerciseId, {
                    sets: exerciseConfig.sets,
                    reps: exerciseConfig.reps,
                    weight: exerciseConfig.weight,
                    restSeconds: exerciseConfig.restSeconds,
                    tempo: exerciseConfig.tempo,
                    targetRpe: exerciseConfig.targetRpe,
                    notes: exerciseConfig.notes
                });

                // Update local state
                setCalendarDays(prevDays =>
                    prevDays.map(day => ({
                        ...day,
                        exercises: day.exercises.map(ex =>
                            ex.id === editingExerciseId ? updatedExercise : ex
                        )
                    }))
                );

                console.log('✅ Exercise updated successfully:', updatedExercise);
            } else {
                // Add new exercise
                const scheduledExercise = await calendarMockApi.scheduleExercise({
                    exerciseId: selectedExercise.id,
                    scheduledDate: selectedDate,
                    sets: exerciseConfig.sets,
                    reps: exerciseConfig.reps,
                    weight: exerciseConfig.weight,
                    restSeconds: exerciseConfig.restSeconds,
                    tempo: exerciseConfig.tempo,
                    targetRpe: exerciseConfig.targetRpe,
                    notes: exerciseConfig.notes
                });

                // Update local state
                setCalendarDays(prevDays =>
                    prevDays.map(day =>
                        day.dateString === selectedDate
                            ? { ...day, exercises: [...day.exercises, scheduledExercise] }
                            : day
                    )
                );

                console.log('✅ Exercise added successfully:', scheduledExercise);
            }

            // Reset state
            setConfigModalOpen(false);
            setSelectedExercise(null);
            setSelectedDate(null);
            setExerciseConfig(DEFAULT_CONFIG);
            setEditingExerciseId(null);

        } catch (err) {
            console.error('❌ Failed to add exercise:', err);
            setError('Failed to add exercise. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleRemoveExercise = async (dateString: string, exerciseId: string) => {
        try {
            console.log('🗑️ Removing exercise:', exerciseId);

            // Use mock API to remove exercise
            await calendarMockApi.removeScheduledExercise(exerciseId);

            // Update local state
            setCalendarDays(prevDays =>
                prevDays.map(day =>
                    day.dateString === dateString
                        ? {
                            ...day,
                            exercises: day.exercises.filter(ex => ex.id !== exerciseId)
                        }
                        : day
                )
            );

            // Clear selected exercise
            setSelectedExerciseForActions(null);

            console.log('✅ Exercise removed successfully');

        } catch (err) {
            console.error('❌ Failed to remove exercise:', err);
            setError('Failed to remove exercise. Please try again.');
        }
    };

    const handleEditExercise = (exercise: ScheduledExercise, dateString: string) => {
        setSelectedExercise(exercise.exercise);
        setSelectedDate(dateString);
        setExerciseConfig({
            sets: exercise.sets,
            reps: exercise.reps,
            weight: exercise.weight,
            restSeconds: exercise.restSeconds,
            tempo: exercise.tempo,
            targetRpe: exercise.targetRpe,
            notes: exercise.notes
        });
        setEditingExerciseId(exercise.id);
        setConfigModalOpen(true);
        setSelectedExerciseForActions(null);
    };

    // Handle viewing exercise results
    const handleViewResults = (exercise: ScheduledExercise) => {
        setSelectedExerciseForResults(exercise);
        setShowResultsModal(true);
        setSelectedExerciseForActions(null);
    };

    // Mobile touch handling for long press (replaces double-click)
    const handleTouchStart = (exercise: ScheduledExercise, dateString: string) => {
        setTouchStartTime(Date.now());
    };

    const handleTouchEnd = (exercise: ScheduledExercise, dateString: string) => {
        const touchDuration = Date.now() - touchStartTime;

        // Long press (500ms+) opens edit modal on mobile
        if (touchDuration >= 500) {
            console.log('📱 Long press detected - opening edit modal');
            handleEditExercise(exercise, dateString);
        } else if (touchDuration < 200) {
            // Quick tap shows/hides action buttons
            console.log('📱 Quick tap - toggle actions');
            setSelectedExerciseForActions(
                selectedExerciseForActions === exercise.id ? null : exercise.id
            );
        }
    };

    // NEW: Handle action button clicks (prevents deselection)
    const handleActionClick = (e: React.MouseEvent | React.TouchEvent, action: () => void) => {
        e.preventDefault();
        e.stopPropagation();
        action();
    };

    // Desktop double-click handling
    const handleDoubleClickExercise = (exercise: ScheduledExercise, dateString: string) => {
        console.log('🖱️ Double-click edit exercise:', exercise);
        if (exercise.completed) {
            handleViewResults(exercise);
        } else {
            handleEditExercise(exercise, dateString);
        }
    };

    const handleOpenExerciseSelector = (dateString?: string) => {
        const targetDate = dateString || DateUtils.getTodayString();

        if (!canAddExercise(targetDate)) {
            setError('You have reached the daily exercise limit for your subscription tier.');
            return;
        }

        setSelectedDate(targetDate);
        setExerciseSelectorOpen(true);
        setEditingExerciseId(null);
    };

    // Simplified drag and drop for desktop only
    const handleDragStart = (e: React.DragEvent<HTMLDivElement>, exercise: ScheduledExercise, fromDate: string) => {
        // Only enable drag on desktop
        if (window.innerWidth < 768) {
            e.preventDefault();
            return;
        }

        console.log('🔄 Starting drag:', exercise.exercise.name, 'from', fromDate);
        setDraggedExercise(exercise);
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', exercise.id);

        // Add visual feedback
        const target = e.currentTarget as HTMLDivElement;
        if (target) {
            target.style.opacity = '0.5';
        }
    };

    const handleDragEnd = (e: React.DragEvent<HTMLDivElement>) => {
        console.log('🔄 Drag ended');
        setDraggedExercise(null);
        setDragOverDay(null);

        // Restore visual feedback
        const target = e.currentTarget as HTMLDivElement;
        if (target) {
            target.style.opacity = '1';
        }
    };

    const handleDragOver = (e: React.DragEvent<HTMLDivElement>, dateString: string) => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        setDragOverDay(dateString);
    };

    const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
        // Only clear if we're leaving the drop zone entirely
        const currentTarget = e.currentTarget;
        const relatedTarget = e.relatedTarget as Node | null;
        if (!currentTarget.contains(relatedTarget)) {
            setDragOverDay(null);
        }
    };

    const handleDrop = async (e: React.DragEvent<HTMLDivElement>, toDate: string) => {
        e.preventDefault();
        setDragOverDay(null);

        if (!draggedExercise) {
            console.log('❌ No dragged exercise found');
            return;
        }

        // Don't allow drop on same date
        if (draggedExercise.scheduledDate === toDate) {
            console.log('❌ Cannot drop on same date');
            setDraggedExercise(null);
            return;
        }

        // Check if target date can accept exercises
        if (!canAddExercise(toDate)) {
            setError('Target day has reached the exercise limit.');
            setDraggedExercise(null);
            return;
        }

        try {
            console.log('📅 Moving exercise from', draggedExercise.scheduledDate, 'to', toDate);

            // Remove from old date
            await calendarMockApi.removeScheduledExercise(draggedExercise.id);

            // Add to new date
            const movedExercise = await calendarMockApi.scheduleExercise({
                exerciseId: draggedExercise.exerciseId,
                scheduledDate: toDate,
                sets: draggedExercise.sets,
                reps: draggedExercise.reps,
                weight: draggedExercise.weight,
                restSeconds: draggedExercise.restSeconds,
                tempo: draggedExercise.tempo,
                targetRpe: draggedExercise.targetRpe,
                notes: draggedExercise.notes
            });

            // Update local state
            setCalendarDays(prevDays =>
                prevDays.map(day => {
                    if (day.dateString === draggedExercise.scheduledDate) {
                        // Remove from old date
                        return {
                            ...day,
                            exercises: day.exercises.filter(ex => ex.id !== draggedExercise.id)
                        };
                    } else if (day.dateString === toDate) {
                        // Add to new date
                        return {
                            ...day,
                            exercises: [...day.exercises, movedExercise]
                        };
                    }
                    return day;
                })
            );

            console.log('✅ Exercise moved successfully');

        } catch (err) {
            console.error('❌ Failed to move exercise:', err);
            setError('Failed to move exercise. Please try again.');
        }

        setDraggedExercise(null);
    };

    // Start workout mode for today
    const handleStartWorkout = () => {
        const todayString = DateUtils.getTodayString();
        const todayExercises = calendarDays.find(day => day.dateString === todayString)?.exercises || [];

        if (todayExercises.length === 0) {
            setError('No exercises scheduled for today. Add some exercises first!');
            return;
        }

        startWorkout(todayExercises, todayString);
        navigate('/workout-mode');
    };

    if (loading && calendarDays.length === 0) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Loading your workout calendar...</p>
                </div>
            </div>
        );
    }

    const todayString = DateUtils.getTodayString();
    const todayExercises = calendarDays.find(day => day.dateString === todayString)?.exercises || [];
    const dateHeaders = formatDateHeader();

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Mobile-First Header */}
            <div className="bg-white border-b border-gray-200">
                <div className="max-w-7xl mx-auto px-3 sm:px-4 py-3 sm:py-4">
                    {/* Title Row - Mobile Optimized */}
                    <div className="flex items-center justify-between mb-3 sm:mb-4">
                        <div className="flex-1 min-w-0">
                            <h1 className="text-lg sm:text-xl md:text-2xl lg:text-3xl font-bold text-gray-900 truncate">
                                Workout Calendar
                            </h1>
                        </div>

                        {/* Mobile: Only essential button visible */}
                        <div className="sm:hidden">
                            <button
                                onClick={() => handleOpenExerciseSelector()}
                                className="p-2 bg-blue-600 text-white rounded-lg shadow-md active:scale-95 transition-transform"
                                title="Add Exercise"
                            >
                                <PlusIcon className="w-5 h-5" />
                            </button>
                        </div>
                    </div>

                    {/* Action Buttons Row - Desktop */}
                    <div className="hidden sm:flex items-center justify-between">
                        <div className="flex-1">
                            {/* Desktop shows more info */}
                        </div>

                        <div className="flex items-center space-x-3">
                            {/* Start Workout Button - Only show if today has exercises */}
                            {todayExercises.length > 0 && (
                                <button
                                    onClick={handleStartWorkout}
                                    className="flex items-center px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors shadow-md"
                                >
                                    <PlayIcon className="w-4 h-4 mr-2" />
                                    <span className="hidden md:inline">Start Workout</span>
                                    <span className="md:hidden">Start</span>
                                </button>
                            )}

                            <button
                                onClick={() => handleOpenExerciseSelector()}
                                className="flex items-center px-4 py-2 border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors"
                            >
                                <PlusIcon className="w-4 h-4 mr-2" />
                                <span className="hidden lg:inline">Add Exercise</span>
                                <span className="lg:hidden">Add</span>
                            </button>
                        </div>
                    </div>

                    {/* Mobile Action Row */}
                    <div className="sm:hidden">
                        {todayExercises.length > 0 && (
                            <button
                                onClick={handleStartWorkout}
                                className="w-full flex items-center justify-center px-4 py-3 bg-green-600 text-white rounded-lg font-medium shadow-md active:scale-[0.98] transition-transform"
                            >
                                <PlayIcon className="w-5 h-5 mr-2" />
                                Start Today's Workout
                            </button>
                        )}
                    </div>
                </div>
            </div>

            {/* Date Navigation */}
            <div className="bg-white border-b border-gray-200">
                <div className="max-w-7xl mx-auto px-3 sm:px-4 py-2 sm:py-3">
                    <div className="flex justify-between items-center">
                        <button
                            onClick={navigatePrevious}
                            className="p-2 rounded-lg hover:bg-gray-100 active:scale-95 transition-all"
                            disabled={loading}
                        >
                            <ChevronLeftIcon className="w-5 h-5 sm:w-6 sm:h-6" />
                        </button>

                        <div className="text-center">
                            <h2 className="text-sm sm:text-base md:text-lg lg:text-xl font-semibold text-gray-800">
                                <span className="sm:hidden">{dateHeaders.mobile}</span>
                                <span className="hidden sm:inline">{dateHeaders.desktop}</span>
                            </h2>
                        </div>

                        <button
                            onClick={navigateNext}
                            className="p-2 rounded-lg hover:bg-gray-100 active:scale-95 transition-all"
                            disabled={loading}
                        >
                            <ChevronRightIcon className="w-5 h-5 sm:w-6 sm:h-6" />
                        </button>
                    </div>
                </div>
            </div>

            {/* Content Area */}
            <div className="max-w-7xl mx-auto px-3 sm:px-4 py-3 sm:py-4 md:py-6">
                {/* Error Display */}
                {error && (
                    <div className="mb-3 sm:mb-4 p-3 sm:p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg relative">
                        <div className="flex justify-between items-start">
                            <span className="flex-1 text-sm sm:text-base">{error}</span>
                            <button
                                onClick={() => setError(null)}
                                className="text-red-500 hover:text-red-700 ml-2 active:scale-95 transition-transform"
                            >
                                <XMarkIcon className="w-4 h-4 sm:w-5 sm:h-5" />
                            </button>
                        </div>
                    </div>
                )}

                {/* Loading State */}
                {loading && (
                    <div className="mb-3 sm:mb-4 p-3 sm:p-4 bg-blue-100 border border-blue-400 text-blue-700 rounded-lg">
                        <div className="flex items-center">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600 mr-2"></div>
                            <span className="text-sm sm:text-base">Updating calendar...</span>
                        </div>
                    </div>
                )}

                {/* Calendar Grid - Mobile Horizontal, Desktop Grid */}

                {/* Mobile: Horizontal Scrolling Week View */}
                <div className="block sm:hidden mb-6">
                    <HorizontalWeekView
                        calendarDays={calendarDays}
                        today={today}
                        canAddExercise={canAddExercise}
                        getRemainingExercises={getRemainingExercises}
                        onAddExercise={handleOpenExerciseSelector}
                        onRemoveExercise={handleRemoveExercise}
                        onEditExercise={handleEditExercise}
                        onViewResults={handleViewResults}
                        onTouchStart={handleTouchStart}
                        onTouchEnd={handleTouchEnd}
                        userTier={user?.userType || 'REGULAR'}
                        isProfessional={user?.isProfessional || false}
                        selectedExerciseForActions={selectedExerciseForActions}
                    />
                </div>

                {/* Desktop: Traditional Grid */}
                <div className="hidden sm:grid sm:grid-cols-2 lg:grid-cols-7 gap-2 sm:gap-3 lg:gap-4 mb-4 sm:mb-6">
                    {calendarDays.map((day) => (
                        <CalendarDayCard
                            key={day.dateString}
                            day={day}
                            canAddExercise={canAddExercise(day.dateString)}
                            remainingExercises={getRemainingExercises(day.dateString)}
                            onAddExercise={() => handleOpenExerciseSelector(day.dateString)}
                            onRemoveExercise={(exerciseId) => handleRemoveExercise(day.dateString, exerciseId)}
                            onEditExercise={(exercise) => handleEditExercise(exercise, day.dateString)}
                            onViewResults={handleViewResults}
                            onTouchStart={(exercise) => handleTouchStart(exercise, day.dateString)}
                            onTouchEnd={(exercise) => handleTouchEnd(exercise, day.dateString)}
                            onDoubleClickExercise={(exercise) => handleDoubleClickExercise(exercise, day.dateString)}
                            onDragStart={(e, exercise) => handleDragStart(e, exercise, day.dateString)}
                            onDragEnd={handleDragEnd}
                            onDragOver={(e) => handleDragOver(e, day.dateString)}
                            onDragLeave={handleDragLeave}
                            onDrop={(e) => handleDrop(e, day.dateString)}
                            userTier={user?.userType || 'REGULAR'}
                            isProfessional={user?.isProfessional || false}
                            isDraggedOver={dragOverDay === day.dateString}
                            selectedExerciseForActions={selectedExerciseForActions}
                        />
                    ))}
                </div>

                {/* Quick Actions - Mobile Optimized */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 sm:gap-4 mb-6 sm:mb-8">
                    <div
                        className="bg-white rounded-xl shadow-lg p-3 sm:p-4 md:p-6 cursor-pointer hover:shadow-xl transition-all active:scale-[0.98]"
                        onClick={() => handleOpenExerciseSelector()}
                    >
                        <div className="flex items-center mb-2 sm:mb-3">
                            <PlusIcon className="w-5 h-5 sm:w-6 sm:h-6 text-blue-600 mr-2 sm:mr-3" />
                            <h3 className="text-sm sm:text-base md:text-lg font-semibold">Add Exercise</h3>
                        </div>
                        <p className="text-gray-600 text-xs sm:text-sm">
                            Search and add exercises to your calendar
                        </p>
                    </div>

                    <div className="bg-white rounded-xl shadow-lg p-3 sm:p-4 md:p-6 cursor-pointer hover:shadow-xl transition-all active:scale-[0.98]">
                        <div className="flex items-center mb-2 sm:mb-3">
                            <CalendarDaysIcon className="w-5 h-5 sm:w-6 sm:h-6 text-green-600 mr-2 sm:mr-3" />
                            <h3 className="text-sm sm:text-base md:text-lg font-semibold">Workout Plans</h3>
                        </div>
                        <p className="text-gray-600 text-xs sm:text-sm">
                            Add pre-made workout templates
                        </p>
                    </div>

                    <div className="bg-white rounded-xl shadow-lg p-3 sm:p-4 md:p-6 cursor-pointer hover:shadow-xl transition-all active:scale-[0.98]">
                        <div className="flex items-center mb-2 sm:mb-3">
                            <ClockIcon className="w-5 h-5 sm:w-6 sm:h-6 text-orange-600 mr-2 sm:mr-3" />
                            <h3 className="text-sm sm:text-base md:text-lg font-semibold">Schedule Program</h3>
                        </div>
                        <p className="text-gray-600 text-xs sm:text-sm">
                            Follow a multi-week program
                        </p>
                    </div>
                </div>
            </div>

            {/* Single FAB for Exercise Selection */}
            <button
                onClick={() => handleOpenExerciseSelector()}
                className="fixed bottom-16 sm:bottom-20 right-3 sm:right-4 bg-blue-600 text-white p-3 sm:p-4 rounded-full shadow-lg hover:bg-blue-700 transition-all active:scale-95 z-10"
                title="Add Exercise"
            >
                <PlusIcon className="w-5 h-5 sm:w-6 sm:h-6" />
            </button>

            {/* Exercise Selector Modal */}
            <ExerciseSelector
                open={exerciseSelectorOpen}
                onClose={() => setExerciseSelectorOpen(false)}
                onExerciseSelect={handleExerciseSelect}
                onDragStart={(exercise) => console.log('Drag started:', exercise)}
                selectedDate={selectedDate}
                canAddToSelectedDate={selectedDate ? canAddExercise(selectedDate) : true}
                title={editingExerciseId ? "Edit Exercise" : "Choose Exercise"}
                onDateChange={setSelectedDate}
            />

            {/* Exercise Configuration Modal */}
            <ExerciseConfigModal
                open={configModalOpen}
                onClose={() => {
                    setConfigModalOpen(false);
                    setEditingExerciseId(null);
                }}
                exercise={selectedExercise}
                config={exerciseConfig}
                onConfigChange={setExerciseConfig}
                onSave={handleAddExercise}
                selectedDate={selectedDate}
                loading={loading}
                isEditing={!!editingExerciseId}
                calendarDays={calendarDays}
                onDateChange={setSelectedDate}
            />

            {/* Exercise Results Modal */}
            <ExerciseResultsModal
                exercise={selectedExerciseForResults}
                open={showResultsModal}
                onClose={() => {
                    setShowResultsModal(false);
                    setSelectedExerciseForResults(null);
                }}
            />
        </div>
    );
};

export default CalendarPage;