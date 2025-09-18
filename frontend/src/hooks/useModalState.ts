import {useState} from 'react';

type SchedulingMode = 'exercise' | 'workout-plan';

export const useModalState = () => {
    // UI state
    const [showExerciseSelector, setShowExerciseSelector] = useState(false);
    const [showConfigModal, setShowConfigModal] = useState(false);
    const [showWorkoutPlanConfigModal, setShowWorkoutPlanConfigModal] = useState(false);
    const [showWorkoutDetailsModal, setShowWorkoutDetailsModal] = useState(false);
    const [schedulingMode, setSchedulingMode] = useState<SchedulingMode>('exercise');

    const openExerciseSelector = (mode: SchedulingMode = 'exercise') => {
        setSchedulingMode(mode);
        setShowExerciseSelector(true);
    };

    const closeExerciseSelector = () => {
        setShowExerciseSelector(false);
    };

    const openConfigModal = () => {
        setShowExerciseSelector(false);
        setShowConfigModal(true);
    };

    const closeConfigModal = () => {
        setShowConfigModal(false);
    };

    const openWorkoutPlanConfigModal = () => {
        setShowExerciseSelector(false);
        setShowWorkoutPlanConfigModal(true);
    };

    const closeWorkoutPlanConfigModal = () => {
        setShowWorkoutPlanConfigModal(false);
    };

    const openWorkoutDetailsModal = () => {
        setShowWorkoutDetailsModal(true);
    };

    const closeWorkoutDetailsModal = () => {
        setShowWorkoutDetailsModal(false);
    };

    const handleModeChange = (mode: SchedulingMode) => {
        setSchedulingMode(mode);
    };

    return {
        // State
        showExerciseSelector,
        showConfigModal,
        showWorkoutPlanConfigModal,
        showWorkoutDetailsModal,
        schedulingMode,

        // Setters (for direct access if needed)
        setShowExerciseSelector,
        setShowConfigModal,
        setShowWorkoutPlanConfigModal,
        setShowWorkoutDetailsModal,
        setSchedulingMode,

        // Handlers
        openExerciseSelector,
        closeExerciseSelector,
        openConfigModal,
        closeConfigModal,
        openWorkoutPlanConfigModal,
        closeWorkoutPlanConfigModal,
        openWorkoutDetailsModal,
        closeWorkoutDetailsModal,
        handleModeChange
    };
};