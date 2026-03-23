import { api } from './api';
import type { InterviewSession } from '../types';
import { mapInterviewResponseToSession } from './sessionMapper';

export const sessionService = {
    createSession: async (jobRole: string, companyName: string, difficulty: string): Promise<InterviewSession> => {
        void jobRole;
        const response = await api.post('/api/interviews/start', {
            company: companyName,
            difficulty
        });
        return mapInterviewResponseToSession(response.data);
    },

    getSession: async (id: number): Promise<InterviewSession> => {
        const response = await api.get(`/api/interviews/${id}`);
        return mapInterviewResponseToSession(response.data);
    },

    getById: async (id: number): Promise<InterviewSession> => {
        const response = await api.get(`/api/interviews/${id}`);
        return mapInterviewResponseToSession(response.data);
    },

    endSession: async (id: number): Promise<InterviewSession> => {
        const response = await api.post(`/api/interviews/${id}/end`);
        return mapInterviewResponseToSession(response.data);
    }
};
