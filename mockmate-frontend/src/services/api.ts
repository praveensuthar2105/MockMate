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

let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

// Response interceptor — handle 401 and 403
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then((token) => {
                        originalRequest.headers.Authorization = 'Bearer ' + token;
                        return api(originalRequest);
                    })
                    .catch((err) => {
                        return Promise.reject(err);
                    });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            const refreshToken = useAuthStore.getState().refreshToken;
            if (refreshToken) {
                try {
                    const response = await axios.post(
                        (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080') + '/api/auth/refresh',
                        { refreshToken }
                    );
                    const { accessToken, refreshToken: newRefreshToken } = response.data;
                    useAuthStore.setState({ accessToken, refreshToken: newRefreshToken });

                    processQueue(null, accessToken);
                    isRefreshing = false;

                    originalRequest.headers.Authorization = 'Bearer ' + accessToken;
                    return api(originalRequest);
                } catch (refreshError) {
                    processQueue(refreshError, null);
                    isRefreshing = false;
                    useAuthStore.getState().clearAuth();
                    window.location.href = '/login';
                    return Promise.reject(refreshError);
                }
            } else {
                useAuthStore.getState().clearAuth();
                window.location.href = '/login';
            }
        }

        if (error.response?.status === 403) {
            useAuthStore.getState().clearAuth();
            window.location.href = '/login';
        }

        return Promise.reject(error);
    }
);
