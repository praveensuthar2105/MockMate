import { api } from './api';
import type { AnalyticsResponse } from '../types';

export const analyticsService = {
    getOverview: async (): Promise<AnalyticsResponse> => {
        try {
            const response = await api.get('/api/analytics/overview');
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to fetch analytics');
        }
    }
};
