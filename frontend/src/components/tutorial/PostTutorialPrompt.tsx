import React from 'react';
import {Calendar, X} from 'lucide-react';

interface PostTutorialPromptProps {
    onPlanWorkout: () => void;
    onDismiss: () => void;
    petName?: string;
}

const PostTutorialPrompt: React.FC<PostTutorialPromptProps> = ({
                                                                   onPlanWorkout,
                                                                   onDismiss,
                                                                   petName,
                                                               }) => {
    return (
        <>
            {/* Backdrop */}
            <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50"/>

            {/* Modal */}
            <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-[51] w-full max-w-md mx-4">
                <div className="bg-white rounded-2xl shadow-2xl overflow-hidden animate-slideIn">
                    {/* Header */}
                    <div className="bg-gradient-to-r from-green-500 to-emerald-600 px-6 py-4 relative">
                        <button
                            onClick={onDismiss}
                            className="absolute top-4 right-4 text-white/80 hover:text-white transition-colors"
                        >
                            <X className="w-5 h-5"/>
                        </button>
                        <div className="text-center">
                            <div className="text-5xl mb-2">🎉</div>
                            <h2 className="text-2xl font-bold text-white">Great Job!</h2>
                        </div>
                    </div>

                    {/* Content */}
                    <div className="p-6 text-center">
                        <p className="text-lg text-gray-700 mb-2">
                            You've learned how to care for {petName || 'your pet'}!
                        </p>
                        <p className="text-gray-600 mb-6">
                            Now let's plan your first workout so you can start earning crystals.
                        </p>

                        {/* CTA Buttons */}
                        <div className="space-y-3">
                            <button
                                onClick={onPlanWorkout}
                                className="w-full flex items-center justify-center gap-2 px-6 py-4 bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white font-semibold rounded-xl transition-all shadow-lg hover:shadow-xl transform hover:scale-[1.02]"
                            >
                                <Calendar className="w-5 h-5"/>
                                Plan First Workout
                            </button>

                            <button
                                onClick={onDismiss}
                                className="w-full px-6 py-3 text-gray-600 hover:text-gray-800 font-medium transition-colors"
                            >
                                I'll Do It Later
                            </button>
                        </div>
                    </div>

                    {/* Footer Tip */}
                    <div className="bg-gray-50 px-6 py-4 text-center border-t border-gray-200">
                        <p className="text-sm text-gray-600">
                            💡 <strong>Tip:</strong> You can access your calendar anytime from the "Plan Workout" card on
                            your pet page!
                        </p>
                    </div>
                </div>
            </div>

            <style dangerouslySetInnerHTML={{
                __html: `
                    @keyframes slideIn {
                        from {
                            opacity: 0;
                            transform: scale(0.9) translateY(20px);
                        }
                        to {
                            opacity: 1;
                            transform: scale(1) translateY(0);
                        }
                    }
                    .animate-slideIn {
                        animation: slideIn 0.3s ease-out;
                    }
                `
            }}/>
        </>
    );
};

export default PostTutorialPrompt;