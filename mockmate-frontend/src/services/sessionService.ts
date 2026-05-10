import { api } from './api';
import type { InterviewSession } from '../types';
import { mapInterviewResponseToSession } from './sessionMapper';

export const sessionService = {
    createSession: async (jobRole: string, companyName: string, difficulty: string): Promise<InterviewSession> => {
        const createResponse = await api.post('/api/sessions/create', {
            jobRole,
            company: companyName,
            difficulty,
            type: 'FULL_MOCK'
        });
        const sessionId = createResponse.data.id;
        const startResponse = await api.post(`/api/sessions/${sessionId}/start`);
        return mapInterviewResponseToSession(startResponse.data);
    },

    getSession: async (id: number): Promise<InterviewSession> => {
        const response = await api.get(`/api/sessions/${id}`);
        return mapInterviewResponseToSession(response.data);
    },

    getById: async (id: number): Promise<InterviewSession> => {
        const response = await api.get(`/api/sessions/${id}`);
        return mapInterviewResponseToSession(response.data);
    },

    endSession: async (id: number): Promise<InterviewSession> => {
        const response = await api.post(`/api/sessions/${id}/end`);
        return mapInterviewResponseToSession(response.data);
    }
};
