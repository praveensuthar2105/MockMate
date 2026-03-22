import { api } from './api';
import type { SessionReport, PageResponse, InterviewSession } from '../types';

export const reportService = {
    getReport: async (sessionId: number): Promise<SessionReport> => {
        try {
            const response = await api.get(`/api/sessions/${sessionId}/report`);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to fetch report');
        }
    },

    getHistory: async (page: number, size: number): Promise<PageResponse<InterviewSession>> => {
        try {
            const response = await api.get(`/api/sessions`, {
                params: { page, size }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to fetch history');
        }
    },

    downloadPdf: async (sessionId: number): Promise<void> => {
        try {
            const response = await api.get(`/api/sessions/${sessionId}/report/pdf`, {
                responseType: 'blob'
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.download = `MockMate-Report-${sessionId}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (error: any) {
            throw new Error('Failed to download PDF report');
        }
    }
};
