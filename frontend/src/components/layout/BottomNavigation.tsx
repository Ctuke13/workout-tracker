import React from 'react';
import {NavLink} from 'react-router-dom';
import {
    HomeIcon,
    ChartBarIcon,
    CalendarIcon,
    UserGroupIcon,
    ChatBubbleLeftIcon
} from '@heroicons/react/24/outline';

const BottomNavigation: React.FC = () => {
    const navItems = [
        {
            path: '/pet',
            icon: HomeIcon,
            label: 'Pet',
            activeColor: 'text-amber-600'
        },
        {
            path: '/progress',
            icon: ChartBarIcon,
            label: 'Progress',
            activeColor: 'text-green-600'
        },
        {
            path: '/calendar',
            icon: CalendarIcon,
            label: 'Calendar',
            activeColor: 'text-purple-600'
        },
        {
            path: '/community',
            icon: UserGroupIcon,
            label: 'Community',
            activeColor: 'text-orange-600'
        },
        {
            path: '/messages',
            icon: ChatBubbleLeftIcon,
            label: 'Messages',
            activeColor: 'text-pink-600'
        }
    ];

    return (
        <nav className="bg-white border-t border-gray-200 fixed bottom-0 left-0 right-0 z-40">
            <div className="grid grid-cols-5 h-16">
                {navItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({isActive}) =>
                            `flex flex-col items-center justify-center space-y-1 transition-colors ${
                                isActive
                                    ? `${item.activeColor} bg-gray-50`
                                    : 'text-gray-500 hover:text-gray-700'
                            }`
                        }
                    >
                        {({isActive}) => (
                            <>
                                <item.icon className={`w-5 h-5 ${isActive ? 'scale-110' : ''} transition-transform`}/>
                                <span className="text-xs font-medium">{item.label}</span>
                            </>
                        )}
                    </NavLink>
                ))}
            </div>
        </nav>
    );
};

export default BottomNavigation;