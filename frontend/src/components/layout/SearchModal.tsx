// src/components/layout/SearchModal.tsx
import React, { useState, useEffect } from 'react';
import { XMarkIcon, MagnifyingGlassIcon, ClockIcon } from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';

interface SearchModalProps {
    onClose: () => void;
}

interface SearchResult {
    id: string;
    type: 'exercise' | 'user' | 'workout' | 'program';
    title: string;
    subtitle: string;
    icon: string;
    path: string;
}

interface RecentSearch {
    id: string;
    query: string;
    timestamp: Date;
}

const SearchModal: React.FC<SearchModalProps> = ({ onClose }) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
    const [recentSearches, setRecentSearches] = useState<RecentSearch[]>([
        { id: '1', query: 'Bench Press', timestamp: new Date() },
        { id: '2', query: 'Upper Body Workout', timestamp: new Date() },
        { id: '3', query: 'John Doe', timestamp: new Date() }
    ]);
    const [isSearching, setIsSearching] = useState(false);
    const navigate = useNavigate();

    // Mock search results - would come from your backend
    const mockResults: SearchResult[] = [
        {
            id: '1',
            type: 'exercise',
            title: 'Bench Press',
            subtitle: 'Chest, Triceps, Shoulders • Intermediate',
            icon: '🏋️‍♂️',
            path: '/exercises/bench-press'
        },
        {
            id: '2',
            type: 'exercise',
            title: 'Incline Bench Press',
            subtitle: 'Upper Chest, Triceps • Intermediate',
            icon: '🏋️‍♂️',
            path: '/exercises/incline-bench-press'
        },
        {
            id: '3',
            type: 'workout',
            title: 'Upper Body Strength',
            subtitle: 'Workout Plan • 45 minutes • 6 exercises',
            icon: '📋',
            path: '/workouts/upper-body-strength'
        },
        {
            id: '4',
            type: 'user',
            title: 'John Fitness',
            subtitle: '@johnfitness • 2.3k followers',
            icon: '👤',
            path: '/users/johnfitness'
        },
        {
            id: '5',
            type: 'program',
            title: 'Beginner Strength Program',
            subtitle: '12-week program • 347 enrolled',
            icon: '🎯',
            path: '/programs/beginner-strength'
        }
    ];

    // Simulate search API call
    useEffect(() => {
        if (searchQuery.trim().length > 0) {
            setIsSearching(true);
            const timeoutId = setTimeout(() => {
                const filtered = mockResults.filter(result =>
                    result.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                    result.subtitle.toLowerCase().includes(searchQuery.toLowerCase())
                );
                setSearchResults(filtered);
                setIsSearching(false);
            }, 300); // Simulate API delay

            return () => clearTimeout(timeoutId);
        } else {
            setSearchResults([]);
            setIsSearching(false);
        }
    }, [searchQuery]);

    const handleSearchSubmit = (query: string) => {
        // Add to recent searches
        const newSearch: RecentSearch = {
            id: Date.now().toString(),
            query,
            timestamp: new Date()
        };
        setRecentSearches(prev => [newSearch, ...prev.slice(0, 4)]); // Keep last 5

        // Navigate to search results page or specific result
        navigate(`/search?q=${encodeURIComponent(query)}`);
        onClose();
    };

    const handleResultClick = (result: SearchResult) => {
        handleSearchSubmit(result.title);
        navigate(result.path);
    };

    const handleRecentSearchClick = (query: string) => {
        setSearchQuery(query);
        handleSearchSubmit(query);
    };

    const clearRecentSearches = () => {
        setRecentSearches([]);
    };

    const getTypeColor = (type: string): string => {
        switch (type) {
            case 'exercise': return 'text-blue-600 bg-blue-50';
            case 'workout': return 'text-green-600 bg-green-50';
            case 'user': return 'text-purple-600 bg-purple-50';
            case 'program': return 'text-orange-600 bg-orange-50';
            default: return 'text-gray-600 bg-gray-50';
        }
    };

    const getTypeName = (type: string): string => {
        switch (type) {
            case 'exercise': return 'Exercise';
            case 'workout': return 'Workout';
            case 'user': return 'User';
            case 'program': return 'Program';
            default: return 'Result';
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-start justify-center pt-4 sm:pt-20">
            <div className="bg-white rounded-lg w-full max-w-2xl mx-4 max-h-[80vh] overflow-hidden shadow-2xl">

                {/* Search Header */}
                <div className="p-4 border-b border-gray-200">
                    <div className="flex items-center space-x-3">
                        <MagnifyingGlassIcon className="w-5 h-5 text-gray-400 flex-shrink-0" />
                        <input
                            type="text"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && searchQuery.trim()) {
                                    handleSearchSubmit(searchQuery.trim());
                                }
                            }}
                            placeholder="Search exercises, workouts, users, programs..."
                            className="flex-1 outline-none text-gray-900 placeholder-gray-500 text-lg"
                            autoFocus
                        />
                        {isSearching && (
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                        )}
                        <button
                            onClick={onClose}
                            className="p-1 text-gray-400 hover:text-gray-600 flex-shrink-0"
                        >
                            <XMarkIcon className="w-5 h-5" />
                        </button>
                    </div>
                </div>

                {/* Search Content */}
                <div className="max-h-96 overflow-y-auto">
                    {searchQuery.trim().length > 0 ? (
                        // Search Results
                        <div className="p-4">
                            {isSearching ? (
                                <div className="text-center py-8">
                                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
                                    <div className="text-gray-500">Searching...</div>
                                </div>
                            ) : searchResults.length > 0 ? (
                                <div className="space-y-1">
                                    <div className="text-sm font-medium text-gray-700 mb-3">
                                        {searchResults.length} result{searchResults.length !== 1 ? 's' : ''} for "{searchQuery}"
                                    </div>
                                    {searchResults.map((result) => (
                                        <button
                                            key={result.id}
                                            onClick={() => handleResultClick(result)}
                                            className="w-full text-left p-3 hover:bg-gray-50 rounded-lg transition-colors group"
                                        >
                                            <div className="flex items-center space-x-3">
                                                <div className="flex-shrink-0 text-2xl">
                                                    {result.icon}
                                                </div>
                                                <div className="flex-1 min-w-0">
                                                    <div className="flex items-center space-x-2">
                            <span className="font-medium text-gray-900 group-hover:text-blue-600 transition-colors">
                              {result.title}
                            </span>
                                                        <span className={`text-xs px-2 py-1 rounded-full ${getTypeColor(result.type)}`}>
                              {getTypeName(result.type)}
                            </span>
                                                    </div>
                                                    <div className="text-sm text-gray-500 truncate">
                                                        {result.subtitle}
                                                    </div>
                                                </div>
                                            </div>
                                        </button>
                                    ))}
                                </div>
                            ) : (
                                <div className="text-center py-8">
                                    <div className="text-gray-500 mb-2">No results found for "{searchQuery}"</div>
                                    <div className="text-sm text-gray-400">
                                        Try searching for exercises, workouts, or users
                                    </div>
                                </div>
                            )}
                        </div>
                    ) : (
                        // Recent Searches & Suggestions
                        <div className="p-4 space-y-4">

                            {/* Recent Searches */}
                            {recentSearches.length > 0 && (
                                <div>
                                    <div className="flex items-center justify-between mb-3">
                                        <div className="text-sm font-medium text-gray-700">Recent Searches</div>
                                        <button
                                            onClick={clearRecentSearches}
                                            className="text-xs text-gray-500 hover:text-gray-700"
                                        >
                                            Clear all
                                        </button>
                                    </div>
                                    <div className="space-y-1">
                                        {recentSearches.map((search) => (
                                            <button
                                                key={search.id}
                                                onClick={() => handleRecentSearchClick(search.query)}
                                                className="w-full text-left p-2 hover:bg-gray-50 rounded-lg transition-colors flex items-center space-x-3"
                                            >
                                                <ClockIcon className="w-4 h-4 text-gray-400 flex-shrink-0" />
                                                <span className="text-gray-700">{search.query}</span>
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Quick Access Categories */}
                            <div>
                                <div className="text-sm font-medium text-gray-700 mb-3">Quick Access</div>
                                <div className="grid grid-cols-2 gap-2">
                                    <button
                                        onClick={() => navigate('/exercises')}
                                        className="p-3 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors text-left"
                                    >
                                        <div className="text-lg mb-1">🏋️‍♂️</div>
                                        <div className="font-medium text-blue-900">Exercise Library</div>
                                        <div className="text-xs text-blue-600">650+ exercises</div>
                                    </button>

                                    <button
                                        onClick={() => navigate('/community')}
                                        className="p-3 bg-purple-50 hover:bg-purple-100 rounded-lg transition-colors text-left"
                                    >
                                        <div className="text-lg mb-1">👥</div>
                                        <div className="font-medium text-purple-900">Community</div>
                                        <div className="text-xs text-purple-600">Find users</div>
                                    </button>

                                    <button
                                        onClick={() => navigate('/calendar')}
                                        className="p-3 bg-green-50 hover:bg-green-100 rounded-lg transition-colors text-left"
                                    >
                                        <div className="text-lg mb-1">📋</div>
                                        <div className="font-medium text-green-900">Workout Plans</div>
                                        <div className="text-xs text-green-600">Pre-made routines</div>
                                    </button>

                                    <button
                                        onClick={() => navigate('/programs')}
                                        className="p-3 bg-orange-50 hover:bg-orange-100 rounded-lg transition-colors text-left"
                                    >
                                        <div className="text-lg mb-1">🎯</div>
                                        <div className="font-medium text-orange-900">Programs</div>
                                        <div className="text-xs text-orange-600">Multi-week plans</div>
                                    </button>
                                </div>
                            </div>

                            {/* Popular Searches */}
                            <div>
                                <div className="text-sm font-medium text-gray-700 mb-3">Popular This Week</div>
                                <div className="flex flex-wrap gap-2">
                                    {['Bench Press', 'Squats', 'Deadlift', 'Pull-ups', 'Push Day', 'Leg Day'].map((term) => (
                                        <button
                                            key={term}
                                            onClick={() => handleRecentSearchClick(term)}
                                            className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded-full text-sm text-gray-700 transition-colors"
                                        >
                                            {term}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* Search Footer */}
                <div className="p-3 border-t border-gray-100 bg-gray-50 text-center">
                    <div className="text-xs text-gray-500">
                        Press <kbd className="px-1 py-0.5 bg-gray-200 rounded text-xs">Enter</kbd> to search • <kbd className="px-1 py-0.5 bg-gray-200 rounded text-xs">Esc</kbd> to close
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SearchModal;