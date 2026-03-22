import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

export function ProtectedRoute() {
    const { isAuthenticated, user } = useAuthStore();
    const location = useLocation();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (user && user.profileComplete === false && !location.pathname.startsWith('/profile')) {
        console.warn("Please complete your profile first."); // Console notification instead of blocking alert
        return <Navigate to="/profile" replace />;
    }

    return <Outlet />;
}

export function AuthRedirect({ children }: { children: React.ReactNode }) {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    return isAuthenticated ? <Navigate to="/dashboard" replace /> : <>{children}</>;
}
