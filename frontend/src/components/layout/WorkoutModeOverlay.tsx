// src/components/layout/WorkoutModeOverlay.tsx
import React, { useState, useEffect } from 'react';
import { XMarkIcon, PlayIcon, PauseIcon } from '@heroicons/react/24/outline';

interface WorkoutModeOverlayProps {
  onClose: () => void;
}

interface CurrentExercise {
  id: string;
  name: string;
  currentSet: number;
  totalSets: number;
  targetReps: number;
  targetWeight?: number;
  actualReps?: number;
  actualWeight?: number;
}

const WorkoutModeOverlay: React.FC<WorkoutModeOverlayProps> = ({ onClose }) => {
  const [currentExercise, setCurrentExercise] = useState<CurrentExercise>({
    id: '1',
    name: 'Bench Press',
    currentSet: 2,
    totalSets: 3,
    targetReps: 8,
    targetWeight: 185,
    actualReps: 8,
    actualWeight: 185
  });

  const [restTimer, setRestTimer] = useState(150); // 2:30 in seconds
  const [isResting, setIsResting] = useState(true);
  const [workoutDuration, setWorkoutDuration] = useState(1245); // 20:45 in seconds

  // Timer effects
  useEffect(() => {
    const interval = setInterval(() => {
      setWorkoutDuration(prev => prev + 1);
      if (isResting && restTimer > 0) {
        setRestTimer(prev => prev - 1);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [isResting, restTimer]);

  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const completeSet = () => {
    if (currentExercise.currentSet < currentExercise.totalSets) {
      setCurrentExercise(prev => ({
        ...prev,
        currentSet: prev.currentSet + 1
      }));
      setRestTimer(90); // 1:30 rest
      setIsResting(true);
    } else {
      // Move to next exercise or complete workout
      alert('Exercise complete! Moving to next exercise...');
    }
  };

  const skipRest = () => {
    setRestTimer(0);
    setIsResting(false);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-95 z-50 flex flex-col">
      {/* Workout Mode Header */}
      <div className="bg-gray-900 text-white p-4 flex items-center justify-between border-b border-gray-700">
        <div className="flex items-center space-x-3">
          <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse"></div>
          <span className="font-semibold">Workout Mode</span>
          <span className="text-gray-400">•</span>
          <span className="text-sm text-gray-400">{formatTime(workoutDuration)}</span>
        </div>
        <button
          onClick={onClose}
          className="p-2 hover:bg-gray-800 rounded-lg transition-colors"
        >
          <XMarkIcon className="w-5 h-5" />
        </button>
      </div>

      {/* Main Workout Content */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-md mx-auto p-4 space-y-6">

          {/* Current Workout Info */}
          <div className="text-center text-white">
            <h2 className="text-2xl font-bold mb-2">Upper Body Strength</h2>
            <div className="text-gray-400">Exercise 2 of 5</div>
          </div>

          {/* Current Exercise Card */}
          <div className="bg-gray-800 rounded-xl p-6 text-white">
            <div className="text-center mb-4">
              <h3 className="text-xl font-bold mb-2">{currentExercise.name}</h3>
              <div className="text-gray-300">
                Set {currentExercise.currentSet} of {currentExercise.totalSets}
              </div>
            </div>

            {/* Target vs Actual */}
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div className="bg-gray-700 rounded-lg p-3 text-center">
                <div className="text-xs text-gray-400 mb-1">Target</div>
                <div className="font-bold">
                  {currentExercise.targetWeight}lbs × {currentExercise.targetReps}
                </div>
              </div>
              <div className="bg-gray-700 rounded-lg p-3 text-center">
                <div className="text-xs text-gray-400 mb-1">Previous</div>
                <div className="font-bold text-blue-400">
                  185lbs × 8
                </div>
              </div>
            </div>

            {/* Weight/Reps Input */}
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div>
                <label className="block text-sm text-gray-400 mb-2">Weight (lbs)</label>
                <input
                  type="number"
                  className="w-full bg-gray-700 text-white rounded-lg px-3 py-3 text-center text-lg font-bold focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  value={currentExercise.actualWeight || ''}
                  onChange={(e) => setCurrentExercise(prev => ({
                    ...prev,
                    actualWeight: parseInt(e.target.value) || undefined
                  }))}
                  placeholder="185"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">Reps</label>
                <input
                  type="number"
                  className="w-full bg-gray-700 text-white rounded-lg px-3 py-3 text-center text-lg font-bold focus:ring-2 focus:ring-blue-500 focus:outline-none"
                  value={currentExercise.actualReps || ''}
                  onChange={(e) => setCurrentExercise(prev => ({
                    ...prev,
                    actualReps: parseInt(e.target.value) || undefined
                  }))}
                  placeholder="8"
                />
              </div>
            </div>

            {/* RPE/Effort Rating */}
            <div className="mb-4">
              <label className="block text-sm text-gray-400 mb-2">How hard was this set? (RPE)</label>
              <div className="flex space-x-2">
                {[6, 7, 8, 9, 10].map((rpe) => (
                  <button
                    key={rpe}
                    className="flex-1 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm font-medium transition-colors"
                  >
                    {rpe}
                  </button>
                ))}
              </div>
            </div>

            {/* Complete Set Button */}
            <button
              onClick={completeSet}
              disabled={!currentExercise.actualReps || !currentExercise.actualWeight}
              className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white py-4 rounded-lg font-bold text-lg transition-colors"
            >
              Complete Set
            </button>
          </div>

          {/* Rest Timer */}
          {isResting && restTimer > 0 && (
            <div className="bg-gray-800 rounded-xl p-6 text-center text-white">
              <div className="text-sm text-gray-400 mb-2">Rest Time</div>
              <div className="text-4xl font-bold mb-2">{formatTime(restTimer)}</div>
              <div className="text-sm text-gray-400 mb-4">
                90 seconds recommended
              </div>

              <div className="flex space-x-3">
                <button
                  onClick={skipRest}
                  className="flex-1 bg-gray-700 hover:bg-gray-600 text-white py-3 rounded-lg font-medium transition-colors"
                >
                  Skip Rest
                </button>
                <button
                  onClick={() => setRestTimer(prev => prev + 30)}
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg font-medium transition-colors"
                >
                  +30 sec
                </button>
              </div>
            </div>
          )}

          {/* Workout Progress */}
          <div className="bg-gray-800 rounded-xl p-4 text-white">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-gray-400">Workout Progress</span>
              <span className="text-sm text-gray-400">2 of 5 exercises</span>
            </div>
            <div className="w-full bg-gray-700 rounded-full h-2">
              <div className="bg-blue-600 h-2 rounded-full w-2/5 transition-all duration-300"></div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="grid grid-cols-2 gap-3">
            <button className="bg-gray-800 hover:bg-gray-700 text-white py-3 rounded-lg font-medium transition-colors">
              📝 Add Note
            </button>
            <button className="bg-gray-800 hover:bg-gray-700 text-white py-3 rounded-lg font-medium transition-colors">
              🔄 Replace Exercise
            </button>
          </div>
        </div>
      </div>

      {/* Bottom Actions */}
      <div className="bg-gray-900 border-t border-gray-700 p-4">
        <div className="max-w-md mx-auto flex space-x-3">
          <button className="flex-1 bg-red-600 hover:bg-red-700 text-white py-3 rounded-lg font-semibold transition-colors">
            End Workout
          </button>
          <button className="flex-1 bg-gray-700 hover:bg-gray-600 text-white py-3 rounded-lg font-semibold transition-colors">
            Pause
          </button>
        </div>
      </div>
    </div>
  );
};

export default WorkoutModeOverlay;