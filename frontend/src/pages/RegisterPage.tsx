import React from 'react';
import { RegisterForm } from '../components/auth/RegisterForm';

const RegisterPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4">
      <RegisterForm />
    </div>
  );
};

export default RegisterPage;