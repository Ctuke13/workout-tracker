import React from 'react';
import { LoginForm } from '../components/auth/LoginForm';

const LoginPage: React.FC = () => {
    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-pink-100 flex flex-col items-center justify-center py-12 px-4">
            {/* EvoPet Branding */}
            <div className="flex items-center gap-3 mb-8">
                <img
                    src="/assets/branding/EvoPet_icon.png"
                    alt="EvoPet"
                    className="w-14 h-14 rounded-2xl object-contain shadow-lg"
                />
                <img
                    src="/assets/branding/EvoPet_txt_lg.png"
                    alt="EvoPet"
                    className="h-10 w-auto object-contain"
                />
            </div>

            <LoginForm />
        </div>
    );
};

export default LoginPage;