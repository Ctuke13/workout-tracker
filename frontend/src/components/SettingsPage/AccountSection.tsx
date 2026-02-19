import React, {useState} from 'react';
import {User, Mail, AtSign, Heart, ChevronRight, Lock} from 'lucide-react';
import {useAuth} from '../../contexts/AuthContext';
import EditModal from './EditModal';
import PasswordChangeModal from './PasswordChangeModal';
import userApi from '../../services/userApi';
import toast from 'react-hot-toast';

const AccountSection: React.FC = () => {
    const {user} = useAuth();
    const [showNicknameModal, setShowNicknameModal] = useState(false);
    const [showPetNameModal, setShowPetNameModal] = useState(false);
    const [showPasswordModal, setShowPasswordModal] = useState(false);

    if (!user) return null;

    const handleUpdateNickname = async (nickname: string) => {
        try {
            await userApi.updateNickname(nickname);
            toast.success('Nickname updated successfully!');
            window.location.reload();
        } catch (error) {
            throw new Error('Failed to update nickname');
        }
    };

    const handleUpdatePetName = async (petName: string) => {
        try {
            await userApi.updatePetName(petName);
            toast.success('Pet name updated successfully!');
            window.location.reload();
        } catch (error) {
            throw new Error('Failed to update pet name');
        }
    };

    const handleChangePassword = async (currentPassword: string, newPassword: string) => {
        try {
            await userApi.changePassword(currentPassword, newPassword);
            toast.success('Password changed successfully!');
        } catch (error) {
            throw error; // Re-throw to let modal handle the error
        }
    };

    const accountItems = [
        {
            icon: <User className="w-5 h-5"/>,
            label: 'Username',
            value: user.username,
            action: undefined,
        },
        {
            icon: <Mail className="w-5 h-5"/>,
            label: 'Email',
            value: user.email,
            action: undefined,
        },
        {
            icon: <AtSign className="w-5 h-5"/>,
            label: 'Nickname',
            value: user.nickname || 'Not set',
            action: () => setShowNicknameModal(true),
        },
        {
            icon: <Heart className="w-5 h-5"/>,
            label: 'Pet Name',
            value: user.petName || 'Not set',
            action: () => setShowPetNameModal(true),
        },
    ];

    return (
        <>
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
                {/* Header */}
                <div className="px-6 py-4 border-b border-gray-200">
                    <h3 className="text-lg font-semibold text-gray-900">Account</h3>
                    <p className="text-sm text-gray-500">Manage your account information</p>
                </div>

                {/* Account Items */}
                <div className="divide-y divide-gray-200">
                    {accountItems.map((item, index) => (
                        <div
                            key={index}
                            className={`px-6 py-4 transition-colors ${
                                item.action ? 'hover:bg-gray-50 cursor-pointer' : ''
                            }`}
                            onClick={item.action}
                        >
                            <div className="flex items-center justify-between">
                                <div className="flex items-center gap-3">
                                    <div
                                        className="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-600">
                                        {item.icon}
                                    </div>
                                    <div>
                                        <p className="text-sm text-gray-600">{item.label}</p>
                                        <p className="text-base font-medium text-gray-900">{item.value}</p>
                                    </div>
                                </div>
                                {item.action && <ChevronRight className="w-5 h-5 text-gray-400"/>}
                            </div>
                        </div>
                    ))}

                    {/* Password Change */}
                    <div
                        className="px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer"
                        onClick={() => setShowPasswordModal(true)}
                    >
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                                <div
                                    className="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-600">
                                    <Lock className="w-5 h-5"/>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-600">Password</p>
                                    <p className="text-base font-medium text-gray-900">••••••••</p>
                                </div>
                            </div>
                            <ChevronRight className="w-5 h-5 text-gray-400"/>
                        </div>
                    </div>
                </div>
            </div>

            {/* Edit Nickname Modal */}
            <EditModal
                isOpen={showNicknameModal}
                onClose={() => setShowNicknameModal(false)}
                onSave={handleUpdateNickname}
                title="Edit Nickname"
                label="Nickname"
                currentValue={user.nickname || ''}
                placeholder="Enter your nickname"
                maxLength={20}
                validationPattern={/^[a-zA-Z0-9_]+$/}
                validationMessage="Nickname can only contain letters, numbers, and underscores"
            />

            {/* Edit Pet Name Modal */}
            <EditModal
                isOpen={showPetNameModal}
                onClose={() => setShowPetNameModal(false)}
                onSave={handleUpdatePetName}
                title="Edit Pet Name"
                label="Pet Name"
                currentValue={user.petName || ''}
                placeholder="Enter your pet's name"
                maxLength={50}
            />

            {/* Password Change Modal */}
            <PasswordChangeModal
                isOpen={showPasswordModal}
                onClose={() => setShowPasswordModal(false)}
                onSave={handleChangePassword}
            />
        </>
    );
};

export default AccountSection;