import axios from 'axios';
import { useAuthStore } from '../store/authStore';

export const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    headers: { 'Content-Type': 'application/json' },
    timeout: 120000,
});

// Request interceptor — attach JWT
api.interceptors.request.use((config) => {
    const token = useAuthStore.getState().accessToken;
    if (token) {
        config.headers.Authorization = 'Bearer ' + token;
    }
    return config;
});

// Response interceptor — handle 401 and 403
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            useAuthStore.getState().clearAuth();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);
