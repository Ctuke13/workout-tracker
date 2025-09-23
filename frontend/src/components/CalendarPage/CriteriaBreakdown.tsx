import React from 'react';
import {WorkoutAnalysisResult} from '../../utils/workoutPerformanceAnalyzer';
import {getStatusIcon, getStatusColor} from '../../utils/workoutDisplayHelpers';

interface CriteriaBreakdownProps {
    analysis: WorkoutAnalysisResult | null;
}

const CriteriaBreakdown: React.FC<CriteriaBreakdownProps> = ({analysis}) => {
    if (!analysis) return null;

    const analysisDetails = analysis.performance.criteria;

    return (
        <div>
            <h3 className="text-lg font-semibold mb-4">Target vs Actual Performance</h3>
            <div className="space-y-4">
                {analysisDetails.map((detail, index) => (
                    <div key={index} className="border border-gray-200 rounded-lg p-4">
                        <div className="flex items-center justify-between mb-2">
                            <div className="flex items-center gap-2">
                                {(() => {
                                    const IconComponent = getStatusIcon(detail.status);
                                    return <IconComponent className="w-4 h-4"/>;
                                })()}
                                <span className="font-medium text-gray-900">{detail.name}</span>
                            </div>
                            <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                getStatusColor(detail.status)
                            }`}>
                {detail.status.replace('_', ' ')}
              </span>
                        </div>

                        <div className="flex items-center justify-between mb-2">
                            <span className="text-sm text-gray-600">{detail.displayText}</span>
                            {detail.unit && (
                                <span className="text-xs text-gray-500">{detail.unit}</span>
                            )}
                        </div>

                        {detail.percentage && (
                            <div className="w-full bg-gray-200 rounded-full h-2">
                                <div
                                    className={`h-2 rounded-full transition-all duration-300 ${
                                        detail.status === 'EXCEEDED' ? 'bg-green-500' :
                                            detail.status === 'MET' ? 'bg-blue-500' :
                                                detail.status === 'PARTIAL' ? 'bg-yellow-500' :
                                                    'bg-red-500'
                                    }`}
                                    style={{width: `${Math.min(detail.percentage, 100)}%`}}
                                />
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CriteriaBreakdown;