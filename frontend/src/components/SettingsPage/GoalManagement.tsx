import React, {useState, useEffect} from 'react';
import {Target, TrendingUp, Award, X} from 'lucide-react';
import petApi, {UserGoalResponse} from '../../services/petApi';
import toast from 'react-hot-toast';

interface GoalPreset {
    label: string;
    value: number;
    description: string;
    icon: string;
}

const GOAL_PRESETS: GoalPreset[] = [
    {label: 'Beginner', value: 2, description: '2-3 workouts/week', icon: '🌱'},
    {label: 'Regular', value: 4, description: '3-4 workouts/week', icon: '💪'},
    {label: 'Dedicated', value: 6, description: '5-6 workouts/week', icon: '🔥'},
];

const GoalManagement: React.FC = () => {
    const [currentGoal, setCurrentGoal] = useState<UserGoalResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [customGoal, setCustomGoal] = useState<string>('');
    const [showCustomInput, setShowCustomInput] = useState(false);

    useEffect(() => {
        fetchCurrentGoal();
    }, []);

    const fetchCurrentGoal = async () => {
        try {
            setLoading(true);
            const goal = await petApi.getUserGoal();
            setCurrentGoal(goal);
        } catch (error) {
            console.error('Failed to fetch goal:', error);
            toast.error('Failed to load goal settings');
        } finally {
            setLoading(false);
        }
    };

    const handleSetGoal = async (goalValue: number | null) => {
        try {
            setUpdating(true);
            const response = await petApi.updateUserGoal({
                weeklyWorkoutGoal: goalValue,
                goalType: 'workouts',
            });

            setCurrentGoal(response);
            setShowCustomInput(false);
            setCustomGoal('');

            if (goalValue === null) {
                toast.success('Goal removed');
            } else {
                toast.success(`Goal set to ${goalValue} workouts/week!`);
            }
        } catch (error) {
            console.error('Failed to update goal:', error);
            toast.error('Failed to update goal');
        } finally {
            setUpdating(false);
        }
    };

    const handleCustomGoalSubmit = () => {
        const value = parseInt(customGoal);
        if (isNaN(value) || value < 1 || value > 7) {
            toast.error('Please enter a number between 1 and 7');
            return;
        }
        handleSetGoal(value);
    };

    const getGoalLevelEmoji = (level: string | null) => {
        if (!level) return '🎯';
        if (level === 'beginner') return '🌱';
        if (level === 'regular') return '💪';
        if (level === 'dedicated') return '🔥';
        return '⭐';
    };

    if (loading) {
        return (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                <div className="flex items-center justify-center py-8">
                    <div className="w-8 h-8 border-4 border-purple-500 border-t-transparent rounded-full animate-spin"/>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
            {/* Header */}
            <div className="px-6 py-4 border-b border-gray-200">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                        <Target className="w-5 h-5 text-purple-600"/>
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold text-gray-900">Weekly Workout Goal</h3>
                        <p className="text-sm text-gray-500">Set a target to stay motivated</p>
                    </div>
                </div>
            </div>

            {/* Current Goal Display */}
            {currentGoal?.hasGoalSet && (
                <div className="px-6 py-4 bg-gradient-to-r from-purple-50 to-pink-50 border-b border-gray-200">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <span className="text-3xl">{getGoalLevelEmoji(currentGoal.goalLevel)}</span>
                            <div>
                                <p className="text-sm text-gray-600">Current Goal</p>
                                <p className="text-xl font-bold text-gray-900">
                                    {currentGoal.weeklyWorkoutGoal} workouts/week
                                </p>
                                {currentGoal.goalLevel && (
                                    <p className="text-xs text-purple-600 capitalize">
                                        {currentGoal.goalLevel} Level
                                    </p>
                                )}
                            </div>
                        </div>
                        <button
                            onClick={() => handleSetGoal(null)}
                            disabled={updating}
                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Remove goal"
                        >
                            <X className="w-5 h-5"/>
                        </button>
                    </div>
                </div>
            )}

            {/* No Goal Set */}
            {!currentGoal?.hasGoalSet && (
                <div className="px-6 py-4 bg-gray-50 border-b border-gray-200">
                    <div className="text-center py-2">
                        <p className="text-gray-600">No goal set</p>
                        <p className="text-sm text-gray-500">Choose a preset below to get started</p>
                    </div>
                </div>
            )}

            {/* Goal Presets */}
            <div className="p-6">
                <h4 className="text-sm font-semibold text-gray-700 mb-3">Choose a Goal</h4>
                <div className="space-y-2">
                    {GOAL_PRESETS.map((preset) => (
                        <button
                            key={preset.value}
                            onClick={() => handleSetGoal(preset.value)}
                            disabled={updating}
                            className={`w-full p-4 rounded-xl border-2 transition-all text-left ${
                                currentGoal?.weeklyWorkoutGoal === preset.value
                                    ? 'border-purple-500 bg-purple-50'
                                    : 'border-gray-200 hover:border-purple-300 hover:bg-purple-50/50'
                            } ${updating ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
                        >
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-3">
                                    <span className="text-2xl">{preset.icon}</span>
                                    <div>
                                        <p className="font-semibold text-gray-900">{preset.label}</p>
                                        <p className="text-sm text-gray-600">{preset.description}</p>
                                    </div>
                                </div>
                                {currentGoal?.weeklyWorkoutGoal === preset.value && (
                                    <div
                                        className="w-6 h-6 bg-purple-500 rounded-full flex items-center justify-center">
                                        <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                                            <path fillRule="evenodd"
                                                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                                  clipRule="evenodd"/>
                                        </svg>
                                    </div>
                                )}
                            </div>
                        </button>
                    ))}

                    {/* Custom Goal */}
                    <div className="pt-2">
                        {!showCustomInput ? (
                            <button
                                onClick={() => setShowCustomInput(true)}
                                disabled={updating}
                                className="w-full p-4 rounded-xl border-2 border-dashed border-gray-300 hover:border-purple-400 hover:bg-purple-50/50 transition-all text-gray-600 hover:text-purple-600"
                            >
                                <div className="flex items-center gap-3">
                                    <span className="text-2xl">⭐</span>
                                    <div className="text-left">
                                        <p className="font-semibold">Custom Goal</p>
                                        <p className="text-sm">Set your own target</p>
                                    </div>
                                </div>
                            </button>
                        ) : (
                            <div className="p-4 rounded-xl border-2 border-purple-300 bg-purple-50">
                                <div className="flex items-center gap-2">
                                    <input
                                        type="number"
                                        min="1"
                                        max="7"
                                        value={customGoal}
                                        onChange={(e) => setCustomGoal(e.target.value)}
                                        placeholder="1-7"
                                        className="w-20 px-3 py-2 border border-gray-300 rounded-lg text-center focus:outline-none focus:ring-2 focus:ring-purple-500"
                                        disabled={updating}
                                    />
                                    <span className="text-gray-600">workouts/week</span>
                                    <div className="flex-1"/>
                                    <button
                                        onClick={handleCustomGoalSubmit}
                                        disabled={updating || !customGoal}
                                        className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                    >
                                        Set
                                    </button>
                                    <button
                                        onClick={() => {
                                            setShowCustomInput(false);
                                            setCustomGoal('');
                                        }}
                                        disabled={updating}
                                        className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Info Footer */}
            <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 rounded-b-xl">
                <div className="flex items-start gap-2 text-sm text-gray-600">
                    <TrendingUp className="w-4 h-4 mt-0.5 flex-shrink-0 text-purple-500"/>
                    <p>
                        Your progress is tracked on the Pet page. Complete workouts to build your streak and hit your
                        weekly goal!
                    </p>
                </div>
            </div>
        </div>
    );
};

export default GoalManagement;