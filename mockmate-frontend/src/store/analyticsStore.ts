import { create } from 'zustand';
import { analyticsService } from '../services/analyticsService';
import type { AnalyticsResponse } from '../types';

interface AnalyticsState {
    overview: AnalyticsResponse | null;
    loading: boolean;
    error: string | null;
    fetchOverview: () => Promise<void>;
}

export const useAnalyticsStore = create<AnalyticsState>((set) => ({
    overview: null,
    loading: false,
    error: null,
    fetchOverview: async () => {
        set({ loading: true, error: null });
        try {
            const overview = await analyticsService.getOverview();
            set({ overview, loading: false });
        } catch (error: any) {
            set({ error: error.message, loading: false });
        }
    },
}));
