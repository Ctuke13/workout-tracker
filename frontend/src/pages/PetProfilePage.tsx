import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {ArrowLeft, Edit2, Check, X} from 'lucide-react';
import {PetStats} from '../types/pet';
import petApi, {EvolutionRequirements} from '../services/petApi';
import XpProgressBar from '../components/PetProfile/XpProgressBar';
import EvolutionCard from '../components/PetProfile/EvolutionCard';
import PetStatsCard from '../components/PetProfile/PetStatsCard';

const PetProfilePage: React.FC = () => {
    const navigate = useNavigate();

    // State
    const [stats, setStats] = useState<PetStats | null>(null);
    const [evolutionReqs, setEvolutionReqs] = useState<EvolutionRequirements | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    // Pet naming state
    const [isEditingName, setIsEditingName] = useState(false);
    const [newName, setNewName] = useState('');
    const [nameError, setNameError] = useState<string | null>(null);

    // ==================== FETCH DATA ====================

    const fetchData = async () => {
        try {
            setError(null);
            const [profileData, evolutionData] = await Promise.all([
                petApi.getProfile(),
                petApi.getEvolutionRequirements(),
            ]);

            setStats(profileData);
            setEvolutionReqs(evolutionData);
            setNewName(profileData.petName || '');
        } catch (err) {
            console.error('Failed to fetch pet data:', err);
            setError(err instanceof Error ? err.message : 'Failed to load pet data');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    // ==================== ACTIONS ====================

    const handleEvolve = async () => {
        if (!evolutionReqs?.canEvolve) return;

        try {
            setError(null);
            const result = await petApi.evolvePet();

            if (result.success) {
                setSuccessMessage(`🎉 ${result.message}`);
                // Refresh data
                await fetchData();
                // Clear message after 5 seconds
                setTimeout(() => setSuccessMessage(null), 5000);
            } else {
                setError(result.message);
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to evolve pet');
        }
    };

    const handleNameSave = async () => {
        const trimmedName = newName.trim();

        if (!trimmedName) {
            setNameError('Pet name cannot be empty');
            return;
        }

        if (trimmedName.length > 20) {
            setNameError('Pet name must be 20 characters or less');
            return;
        }

        try {
            setNameError(null);
            setError(null);

            const updated = await petApi.updatePetName(trimmedName);
            setStats(updated);
            setIsEditingName(false);
            setSuccessMessage(`Pet renamed to ${trimmedName}!`);
            setTimeout(() => setSuccessMessage(null), 3000);
        } catch (err) {
            setNameError(err instanceof Error ? err.message : 'Failed to update name');
        }
    };

    const handleNameCancel = () => {
        setNewName(stats?.petName || '');
        setIsEditingName(false);
        setNameError(null);
    };

    // ==================== LOADING STATE ====================

    if (loading) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 flex items-center justify-center">
                <div className="text-center">
                    <div
                        className="w-16 h-16 border-4 border-purple-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"/>
                    <p className="text-purple-700 font-medium">Loading profile...</p>
                </div>
            </div>
        );
    }

    // ==================== ERROR STATE ====================

    if (error && !stats) {
        return (
            <div
                className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-2xl shadow-xl p-6 max-w-md w-full text-center">
                    <div className="text-4xl mb-4">😢</div>
                    <h2 className="text-xl font-bold text-gray-900 mb-2">Couldn't Load Profile</h2>
                    <p className="text-gray-600 mb-4">{error}</p>
                    <div className="flex gap-3">
                        <button
                            onClick={fetchData}
                            className="flex-1 px-6 py-2 bg-purple-500 text-white rounded-lg font-medium hover:bg-purple-600 transition-colors"
                        >
                            Try Again
                        </button>
                        <button
                            onClick={() => navigate('/pet')}
                            className="flex-1 px-6 py-2 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300 transition-colors"
                        >
                            Go Back
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    if (!stats) return null;

    // ==================== MAIN RENDER ====================

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-purple-50">
            {/* Header */}
            <div className="bg-white/80 backdrop-blur-md border-b border-purple-200 sticky top-0 z-10">
                <div className="max-w-2xl mx-auto px-4 py-3 flex items-center justify-between">
                    <button
                        onClick={() => navigate('/pet')}
                        className="flex items-center gap-2 text-purple-700 hover:text-purple-900 transition-colors"
                    >
                        <ArrowLeft className="w-5 h-5"/>
                        <span className="font-medium">Back</span>
                    </button>
                    <h1 className="text-xl font-bold text-gray-900">Pet Profile</h1>
                    <div className="w-20"/>
                    {/* Spacer for centering */}
                </div>
            </div>

            {/* Success Message Toast */}
            {successMessage && (
                <div className="fixed top-20 left-1/2 -translate-x-1/2 z-50 animate-bounce">
                    <div className="bg-green-500 text-white px-6 py-3 rounded-full shadow-lg font-medium">
                        {successMessage}
                    </div>
                </div>
            )}

            {/* Error Toast */}
            {error && (
                <div className="fixed top-20 left-1/2 -translate-x-1/2 z-50 max-w-md w-full mx-4">
                    <div className="bg-red-50 border-2 border-red-300 rounded-xl p-4 text-red-700 shadow-lg">
                        <div className="flex items-start gap-3">
                            <span className="text-xl">⚠️</span>
                            <div className="flex-1">
                                <p className="font-medium">Error</p>
                                <p className="text-sm">{error}</p>
                            </div>
                            <button
                                onClick={() => setError(null)}
                                className="text-red-400 hover:text-red-600"
                            >
                                <X className="w-5 h-5"/>
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Main Content */}
            <div className="max-w-2xl mx-auto px-4 py-6 space-y-6">
                {/* Pet Stats Summary */}
                <PetStatsCard
                    level={stats.level ?? 1}
                    workoutsCompleted={stats.workoutsCompleted ?? 0}
                    crystals={stats.crystals ?? 0}
                    maxCrystals={stats.maxCrystals ?? 15}
                    petName={stats.petName ?? null}
                    evolutionStageDisplay={stats.evolutionStageDisplay ?? 'Baby Wolf'}
                />

                {/* Pet Name Editor */}
                <div className="bg-white rounded-2xl p-6 shadow-lg border border-gray-200">
                    <div className="flex items-center justify-between mb-4">
                        <h3 className="text-lg font-bold text-gray-900">Pet Name</h3>
                        {!isEditingName && (
                            <button
                                onClick={() => setIsEditingName(true)}
                                className="flex items-center gap-2 text-purple-600 hover:text-purple-700 font-medium text-sm"
                            >
                                <Edit2 className="w-4 h-4"/>
                                Edit
                            </button>
                        )}
                    </div>

                    {isEditingName ? (
                        <div className="space-y-3">
                            <input
                                type="text"
                                value={newName}
                                onChange={(e) => setNewName(e.target.value)}
                                maxLength={20}
                                placeholder="Enter pet name"
                                className="w-full px-4 py-2 border-2 border-purple-300 rounded-lg focus:outline-none focus:border-purple-500"
                            />
                            {nameError && (
                                <p className="text-sm text-red-600">{nameError}</p>
                            )}
                            <div className="flex gap-2">
                                <button
                                    onClick={handleNameSave}
                                    className="flex-1 flex items-center justify-center gap-2 py-2 bg-green-500 text-white rounded-lg font-medium hover:bg-green-600"
                                >
                                    <Check className="w-4 h-4"/>
                                    Save
                                </button>
                                <button
                                    onClick={handleNameCancel}
                                    className="flex-1 flex items-center justify-center gap-2 py-2 bg-gray-200 text-gray-700 rounded-lg font-medium hover:bg-gray-300"
                                >
                                    <X className="w-4 h-4"/>
                                    Cancel
                                </button>
                            </div>
                        </div>
                    ) : (
                        <p className="text-2xl font-bold text-purple-700">
                            {stats.petName || 'Unnamed'}
                        </p>
                    )}
                </div>

                {/* XP Progress */}
                <div className="bg-white rounded-2xl p-6 shadow-lg border border-gray-200">
                    <h3 className="text-lg font-bold text-gray-900 mb-4">Experience Progress</h3>
                    <XpProgressBar
                        currentXp={stats.xp ?? 0}
                        xpToNextLevel={stats.xpToNextLevel ?? 100}
                        level={stats.level ?? 1}
                    />
                </div>

                {/* Evolution Card */}
                {evolutionReqs && (
                    <EvolutionCard
                        currentStage={evolutionReqs.currentStage}
                        currentStageDisplay={evolutionReqs.currentStageDisplay}
                        nextStage={evolutionReqs.nextStage}
                        nextStageDisplay={evolutionReqs.nextStageDisplay}
                        currentLevel={evolutionReqs.currentLevel}
                        levelRequired={evolutionReqs.levelRequired}
                        levelsRemaining={evolutionReqs.levelsRemaining}
                        canEvolve={evolutionReqs.canEvolve}
                        message={evolutionReqs.message}
                        onEvolveClick={handleEvolve}
                    />
                )}

                {/* Quick Stats */}
                <div className="bg-white rounded-2xl p-6 shadow-lg border border-gray-200">
                    <h3 className="text-lg font-bold text-gray-900 mb-4">Quick Stats</h3>
                    <div className="grid grid-cols-2 gap-4">
                        <div className="bg-purple-50 rounded-lg p-3">
                            <p className="text-sm text-gray-600">Evolution Stage</p>
                            <p className="font-bold text-purple-700">{stats.evolutionStage}</p>
                        </div>
                        <div className="bg-blue-50 rounded-lg p-3">
                            <p className="text-sm text-gray-600">Total XP Earned</p>
                            <p className="font-bold text-blue-700">
                                {((stats.level ?? 1) - 1) * 100 + (stats.xp ?? 0)}
                            </p>
                        </div>
                        <div className="bg-pink-50 rounded-lg p-3">
                            <p className="text-sm text-gray-600">Pet Type</p>
                            <p className="font-bold text-pink-700">{stats.petType}</p>
                        </div>
                        <div className="bg-amber-50 rounded-lg p-3">
                            <p className="text-sm text-gray-600">Pet Color</p>
                            <p className="font-bold text-amber-700">{stats.petColor}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PetProfilePage;