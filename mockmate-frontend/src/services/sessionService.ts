import { api } from './api';
import { mapInterviewResponseToSession } from './sessionMapper';
import type { InterviewSession } from '../types';

export interface SessionRequest {
    company: string;
    jobRole: string;
    difficulty: string;
    resumeDurationMins?: number;
    dsaDurationMins?: number;
    systemDesignDurationMins?: number;
    hrDurationMins?: number;
    type?: string;
}

export const sessionService = {
    createSession: async (jobRole: string, company: string, difficulty: string, type: string = 'FULL_MOCK'): Promise<InterviewSession> => {
        try {
            const request: SessionRequest = {
                jobRole,
                company,
                difficulty,
                type
            };
            const response = await api.post('/api/sessions/create', request);
            return mapInterviewResponseToSession(response.data);
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to create session');
        }
    },
    start: async (sessionId: number): Promise<InterviewSession> => {
        try {
            const response = await api.post(`/api/sessions/${sessionId}/start`);
            return mapInterviewResponseToSession(response.data);
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to start session');
        }
    },
    getById: async (sessionId: number): Promise<InterviewSession> => {
        try {
            const response = await api.get(`/api/sessions/${sessionId}`);
            return mapInterviewResponseToSession(response.data);
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to fetch session');
        }
    }
};
