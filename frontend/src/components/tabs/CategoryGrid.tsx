import React from 'react';
import {ChevronRightIcon} from '@heroicons/react/24/outline';
import {Goal} from '../../types/exercise';

interface CategoryGridProps {
    categories: Goal[];
    onCategorySelect: (categoryId: string) => void;
}

const CategoryGrid: React.FC<CategoryGridProps> = ({
                                                       categories,
                                                       onCategorySelect
                                                   }) => {
    return (
        <div className="grid grid-cols-1 gap-3">
            {categories.map((category) => (
                <div
                    key={category.id}
                    className="group bg-gradient-to-r from-white to-gray-50 hover:from-blue-50 hover:to-purple-50 rounded-2xl border border-gray-200 hover:border-blue-300 p-4 hover:shadow-lg transition-all duration-300 cursor-pointer active:scale-[0.98]"
                    onClick={() => onCategorySelect(category.id)}
                >
                    <div className="flex items-center">
                        <div
                            className="w-12 h-12 bg-gradient-to-br from-blue-100 to-purple-100 rounded-xl flex items-center justify-center text-2xl mr-4 group-hover:scale-110 transition-transform duration-300">
                            {category.emoji}
                        </div>
                        <div className="flex-1">
                            <h3 className="font-bold text-gray-900 text-lg group-hover:text-blue-900 transition-colors">
                                {category.name}
                            </h3>
                            <p className="text-sm text-gray-500 group-hover:text-blue-600 transition-colors">
                                {category.count} exercises available
                            </p>
                        </div>
                        <ChevronRightIcon
                            className="w-5 h-5 text-gray-400 group-hover:text-blue-600 group-hover:translate-x-1 transition-all duration-300"/>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default CategoryGrid;