import React from 'react';
import { LoginForm } from '../components/auth/LoginForm';

const LoginPage: React.FC = () => {
    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-100 via-blue-100 to-pink-100 flex flex-col items-center justify-center py-12 px-4">
            {/* EvoPet Text Logo Only */}
            <div className="mb-4">
                <img
                    src="/assets/branding/EvoPet_txt_lg.png"
                    alt="EvoPet"
                    className="h-28 w-auto object-contain"
                />
            </div>

            {/* Constrain width so all form elements are consistent */}
            <div className="w-full max-w-md">
                <LoginForm />
            </div>
        </div>
    );
};

export default LoginPage;