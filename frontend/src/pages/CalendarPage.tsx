import React, {useState, useRef, useEffect} from 'react';
import {useNavigate, useLocation} from 'react-router-dom';
import {Card, CardContent, CardHeader, CardTitle} from '../components/ui/card';
import {Badge} from '../components/ui/badge';

// Import extracted hooks
import {useCalendarData} from '../hooks/useCalendarData';
import {useCalendarActions} from '../hooks/useCalendarActions';
import {useModalState} from '../hooks/useModalState';

// Import extracted components
import {DateHeader} from '../components/CalendarPage/DateHeader';
import {WeekCalendar} from '../components/CalendarPage/WeekCalendar';
import {ExerciseCard} from '../components/CalendarPage/ExerciseCard';
import WorkoutActions from '../components/CalendarPage/WorkoutActions';

// Import existing components
import FloatingActionButton from '../components/layout/FloatingActionButton';
import {ExerciseConfigModal} from '../components/CalendarPage/index';
import EnhancedExerciseSelector from '../components/CalendarPage/ExerciseSelector';
import WorkoutPlanConfigModal from '../components/CalendarPage/WorkoutPlanConfigModal';
import WorkoutDetailsModal from '../components/CalendarPage/WorkoutDetailsModal';

const CalendarPage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const previousLocation = useRef(location.pathname);

    // Core state
    const [viewingDate, setViewingDate] = useState(new Date());

    // Use extracted data management hook
    const {
        scheduledWorkouts,
        stats,
        userFavoriteIds,
        workoutResults,
        loading,
        viewingDateString,
        viewingDateExercises,
        loadDayData,
        refreshCalendarData,
        setScheduledWorkouts,
        setUserFavoriteIds
    } = useCalendarData(viewingDate);

    // Use extracted actions hook
    const {
        selectedExercise,
        exerciseConfig,
        selectedWorkoutPlan,
        editingExercise,
        isEditMode,
        selectedExerciseForDetails,
        selectedWorkoutResults,
        setSelectedExerciseForDetails,
        setSelectedWorkoutResults,
        handleExerciseSelect,
        handleConfigChange,
        handleSaveExercise,
        handleWorkoutPlanSelect,
        handleWorkoutPlanConfigure,
        handleWorkoutPlanConfigSave,
        handleEditExercise,
        handleFavoriteToggle,
        handleStartWorkout,
        handleStartFullWorkout,
        handleDeleteWorkout,
        handleViewWorkoutDetails,
        resetSchedulingState,
        resetEditingState
    } = useCalendarActions(
        viewingDate,
        viewingDateString,
        viewingDateExercises,
        workoutResults,
        loadDayData,
        refreshCalendarData,
        setScheduledWorkouts,
        setUserFavoriteIds,
        userFavoriteIds
    );

    // Use extracted modal state hook
    const {
        showExerciseSelector,
        showConfigModal,
        showWorkoutPlanConfigModal,
        showWorkoutDetailsModal,
        schedulingMode,
        openExerciseSelector,
        closeExerciseSelector,
        openConfigModal,
        closeConfigModal,
        openWorkoutPlanConfigModal,
        closeWorkoutPlanConfigModal,
        openWorkoutDetailsModal,
        closeWorkoutDetailsModal,
        handleModeChange
    } = useModalState();

    useEffect(() => {
        // Handle navigation back to calendar after workout completion
        if (location.pathname === '/calendar' && previousLocation.current === '/workout') {
            console.log('Returned to calendar from workout, refreshing data...');
            refreshCalendarData(true);
        }

        // Also check session storage for workout completion flag
        const workoutJustCompleted = sessionStorage.getItem('workoutJustCompleted');
        const completedWorkoutDate = sessionStorage.getItem('completedWorkoutDate');

        if (workoutJustCompleted === 'true' && completedWorkoutDate) {
            console.log('Workout completion detected, ensuring calendar is up to date...');
            refreshCalendarData(true, true); // showToast=true, forceCacheBust=true

            // If the completed workout was for the currently viewed date, ensure we show it
            if (completedWorkoutDate === viewingDateString) {
                loadDayData(true); // force reload
            }

            // Clear the flags
            sessionStorage.removeItem('workoutJustCompleted');
            sessionStorage.removeItem('completedWorkoutDate');
        }

        previousLocation.current = location.pathname;
    }, [location.pathname, refreshCalendarData, viewingDateString, loadDayData]);

    // Navigation handlers
    const navigateDay = (direction: 'prev' | 'next') => {
        setViewingDate(prev => {
            const newDate = new Date(prev);
            if (direction === 'prev') {
                newDate.setDate(prev.getDate() - 1);
            } else {
                newDate.setDate(prev.getDate() + 1);
            }
            return newDate;
        });
    };

    const goToToday = () => {
        setViewingDate(new Date());
    };

    // Enhanced handlers that integrate with modal state
    const handleExerciseSelectWithModal = (exercise: any) => {
        handleExerciseSelect(exercise);
        openConfigModal();
    };

    const handleWorkoutPlanConfigureWithModal = (workoutPlan: any) => {
        handleWorkoutPlanConfigure(workoutPlan);
        openWorkoutPlanConfigModal();
    };

    const handleEditExerciseWithModal = (scheduledExercise: any) => {
        handleEditExercise(scheduledExercise);
        openConfigModal();
    };

    const handleViewWorkoutDetailsWithModal = (exerciseId: string) => {
        handleViewWorkoutDetails(exerciseId);
        if (selectedExerciseForDetails && selectedWorkoutResults) {
            openWorkoutDetailsModal();
        }
    };

    const handleConfigModalClose = () => {
        closeConfigModal();
        if (!isEditMode) {
            resetSchedulingState();
        } else {
            resetEditingState();
        }
    };

    const handleWorkoutPlanConfigModalClose = () => {
        closeWorkoutPlanConfigModal();
        // selectedWorkoutPlan will be reset by the actions hook
    };

    const handleWorkoutDetailsModalClose = () => {
        closeWorkoutDetailsModal();
        setSelectedExerciseForDetails(null);
        setSelectedWorkoutResults(null);
    };

    // Utility functions
    const isToday = () => {
        const today = new Date();
        return viewingDate.toDateString() === today.toDateString();
    };

    return (
        <div className="w-full min-h-screen bg-gray-50 pb-20">
            <div className="px-3 sm:px-4 lg:px-6 py-3 sm:py-4 lg:py-6 space-y-4 sm:space-y-6 max-w-4xl mx-auto">

                {/* Date Header Component */}
                <DateHeader
                    viewingDate={viewingDate}
                    loading={loading}
                    viewingDateExercises={viewingDateExercises}
                    onNavigateDay={navigateDay}
                    onGoToToday={goToToday}
                    onManualRefresh={() => refreshCalendarData(true)}
                />

                {/* Workout Actions Component */}
                <WorkoutActions
                    isToday={isToday()}
                    hasExercises={viewingDateExercises.length > 0}
                    exerciseCount={viewingDateExercises.length}
                    onStartFullWorkout={handleStartFullWorkout}
                    onAddExercise={() => openExerciseSelector('exercise')}
                    onAddWorkoutPlan={() => openExerciseSelector('workout-plan')}
                />

                {/* Week Calendar Component */}
                <WeekCalendar
                    viewingDate={viewingDate}
                    scheduledWorkouts={scheduledWorkouts}
                    onDateSelect={setViewingDate}
                />

                {/* Exercises List */}
                {viewingDateExercises.length > 0 && (
                    <Card className="shadow-sm">
                        <CardHeader className="pb-3 sm:pb-4">
                            <CardTitle className="text-base sm:text-lg lg:text-xl flex items-center justify-between">
                                <span>Scheduled Exercises</span>
                                <Badge
                                    className={`text-xs sm:text-sm ${isToday() ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>
                                    {viewingDateExercises.filter(ex => ex.completed).length} / {viewingDateExercises.length} Complete
                                </Badge>
                            </CardTitle>
                        </CardHeader>
                        <CardContent className="pt-0">
                            <div className="space-y-3 sm:space-y-4">
                                {viewingDateExercises.map((exercise, index) => (
                                    <ExerciseCard
                                        key={exercise.id}
                                        exercise={exercise}
                                        index={index}
                                        workoutResults={workoutResults[exercise.id]}
                                        onStartWorkout={handleStartWorkout}
                                        onEditExercise={handleEditExerciseWithModal}
                                        onDeleteWorkout={handleDeleteWorkout}
                                        onViewDetails={handleViewWorkoutDetailsWithModal}
                                        onFavoriteToggle={handleFavoriteToggle}
                                    />
                                ))}
                            </div>
                        </CardContent>
                    </Card>
                )}

                {/* Quick Stats */}
                {(viewingDateExercises.length > 0 || isToday()) && stats && (
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 sm:gap-3 lg:gap-4">
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-blue-600">{stats.currentStreak}</div>
                                <div className="text-xs sm:text-sm text-gray-500">Day Streak</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-green-600">{stats.completedWorkouts}</div>
                                <div className="text-xs sm:text-sm text-gray-500">This Month</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-purple-600">{Math.round(stats.completionRate || 0)}%
                                </div>
                                <div className="text-xs sm:text-sm text-gray-500">Success</div>
                            </CardContent>
                        </Card>
                        <Card className="shadow-sm">
                            <CardContent className="p-3 sm:p-4 text-center">
                                <div
                                    className="text-base sm:text-lg lg:text-xl font-bold text-orange-600">{stats.averageWorkoutDuration}</div>
                                <div className="text-xs sm:text-sm text-gray-500">Avg Min</div>
                            </CardContent>
                        </Card>
                    </div>
                )}
            </div>

            {/* Floating Action Button */}
            <FloatingActionButton
                onClick={() => openExerciseSelector('exercise')}
                isWorkoutMode={false}
            />

            {/* Modals */}
            {showExerciseSelector && (
                <EnhancedExerciseSelector
                    open={showExerciseSelector}
                    onClose={closeExerciseSelector}
                    onExerciseSelect={handleExerciseSelectWithModal}
                    onWorkoutPlanSelect={handleWorkoutPlanSelect}
                    onWorkoutPlanConfigure={handleWorkoutPlanConfigureWithModal}
                    selectedDate={viewingDateString}
                    calendarDays={[]}
                    onDateChange={(dateString) => {
                        const newDate = new Date(dateString);
                        setViewingDate(newDate);
                    }}
                    title={`Add to ${isToday() ? 'Today' : 'Selected Date'}`}
                    initialTab={schedulingMode === 'workout-plan' ? 1 : 0}
                />
            )}

            {showConfigModal && selectedExercise && exerciseConfig && (
                <ExerciseConfigModal
                    isOpen={showConfigModal}
                    onClose={handleConfigModalClose}
                    exercise={selectedExercise}
                    config={exerciseConfig}
                    onConfigChange={handleConfigChange}
                    onSave={handleSaveExercise}
                    selectedDate={viewingDate}
                    loading={loading}
                    mode={schedulingMode}
                    onModeChange={handleModeChange}
                    onWorkoutPlanSelect={() => {
                    }} // Not used in this context
                    selectedWorkoutPlan={selectedWorkoutPlan}
                    isEditMode={isEditMode}
                    editingExercise={editingExercise}
                    onFavoriteToggle={handleFavoriteToggle}
                />
            )}

            {showWorkoutPlanConfigModal && selectedWorkoutPlan && (
                <WorkoutPlanConfigModal
                    isOpen={showWorkoutPlanConfigModal}
                    onClose={handleWorkoutPlanConfigModalClose}
                    workoutPlan={selectedWorkoutPlan}
                    selectedDate={viewingDate}
                    onSchedule={handleWorkoutPlanConfigSave}
                    loading={loading}
                />
            )}

            {showWorkoutDetailsModal && selectedExerciseForDetails && selectedWorkoutResults && (
                <WorkoutDetailsModal
                    isOpen={showWorkoutDetailsModal}
                    onClose={handleWorkoutDetailsModalClose}
                    exercise={selectedExerciseForDetails}
                    workoutResults={selectedWorkoutResults}
                />
            )}
        </div>
    );
};

export default CalendarPage;