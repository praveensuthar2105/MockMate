import { api } from './api';
import type { AuthResponse } from '../types';
import { useAuthStore } from '../store/authStore';

export const authService = {
    register: async (name: string, email: string, password: string, experienceLevel: string): Promise<AuthResponse> => {
        try {
            const response = await api.post<AuthResponse>('/api/auth/register', {
                name,
                email,
                password,
                experienceLevel
            });
            useAuthStore.getState().setAuth(response.data);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Registration failed');
        }
    },

    login: async (email: string, password: string): Promise<AuthResponse> => {
        try {
            const response = await api.post<AuthResponse>('/api/auth/login', {
                email,
                password
            });
            useAuthStore.getState().setAuth(response.data);
            return response.data;
        } catch (error: any) {
            if (error.response?.status === 401) {
                throw new Error('Invalid email or password');
            }
            throw new Error(error.response?.data?.message || 'Login failed');
        }
    },

    refresh: async (refreshToken: string): Promise<AuthResponse> => {
        const response = await api.post<AuthResponse>('/api/auth/refresh', {
            refreshToken
        });
        return response.data;
    }
};
