import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {GraduationCap, RefreshCw} from 'lucide-react';
import {useAuth} from '../../contexts/AuthContext';
import userApi from '../../services/userApi';

const TutorialSection: React.FC = () => {
    const navigate = useNavigate();
    const {user, refreshUser} = useAuth();
    const [loading, setLoading] = useState<'pet' | 'calendar' | null>(null);

    const handleReplayPetTutorial = async () => {
        if (window.confirm('This will show the Pet page tutorial again. Continue?')) {
            setLoading('pet');
            try {
                await userApi.restartPetTutorial();
                await refreshUser();
                navigate('/pet');
            } catch (error) {
                console.error('Failed to restart pet tutorial:', error);
                alert('Failed to restart tutorial. Please try again.');
            } finally {
                setLoading(null);
            }
        }
    };

    const handleReplayCalendarTutorial = async () => {
        if (window.confirm('This will show the Calendar page tutorial again. Continue?')) {
            setLoading('calendar');
            try {
                await userApi.restartCalendarTutorial();
                await refreshUser();
                navigate('/calendar');
            } catch (error) {
                console.error('Failed to restart calendar tutorial:', error);
                alert('Failed to restart tutorial. Please try again.');
            } finally {
                setLoading(null);
            }
        }
    };

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
            {/* Header */}
            <div className="px-4 py-3 border-b border-gray-100">
                <div className="flex items-center gap-2">
                    <GraduationCap className="w-5 h-5 text-purple-600"/>
                    <h2 className="font-semibold text-gray-900">Tutorials</h2>
                </div>
                <p className="text-sm text-gray-600 mt-1">Replay feature tutorials</p>
            </div>

            {/* Tutorial Options */}
            <div className="divide-y divide-gray-100">
                {/* Pet Tutorial */}
                <button
                    onClick={handleReplayPetTutorial}
                    disabled={loading !== null}
                    className="w-full px-4 py-4 flex items-center justify-between hover:bg-purple-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-purple-100 flex items-center justify-center">
                            <span className="text-xl">🐺</span>
                        </div>
                        <div className="text-left">
                            <h3 className="font-medium text-gray-900">Pet Page Tutorial</h3>
                            <p className="text-sm text-gray-600">
                                {user?.petTutorialCompleted
                                    ? 'Completed'
                                    : 'Not started yet'}
                            </p>
                        </div>
                    </div>
                    {loading === 'pet' ? (
                        <div
                            className="w-5 h-5 border-2 border-purple-500 border-t-transparent rounded-full animate-spin"/>
                    ) : (
                        <RefreshCw className="w-5 h-5 text-gray-400"/>
                    )}
                </button>

                {/* Calendar Tutorial */}
                <button
                    onClick={handleReplayCalendarTutorial}
                    disabled={loading !== null}
                    className="w-full px-4 py-4 flex items-center justify-between hover:bg-blue-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                            <span className="text-xl">📅</span>
                        </div>
                        <div className="text-left">
                            <h3 className="font-medium text-gray-900">Calendar Tutorial</h3>
                            <p className="text-sm text-gray-600">
                                {user?.calendarTutorialCompleted
                                    ? 'Completed'
                                    : 'Not started yet'}
                            </p>
                        </div>
                    </div>
                    {loading === 'calendar' ? (
                        <div
                            className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"/>
                    ) : (
                        <RefreshCw className="w-5 h-5 text-gray-400"/>
                    )}
                </button>
            </div>

            {/* Help Text */}
            <div className="px-4 py-3 bg-gray-50 border-t border-gray-100">
                <p className="text-xs text-gray-600">
                    💡 Replaying a tutorial will navigate you to that page and show the guided walkthrough again.
                </p>
            </div>
        </div>
    );
};

export default TutorialSection;