import { api } from './api';
import type { ExecutionResult, DsaStatusResponse } from '../types';

export const codeService = {
    getProblem: async (sessionId: number): Promise<DsaStatusResponse> => {
        try {
            const response = await api.get(`/api/code/problem/${sessionId}`);
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to fetch problem');
        }
    },
    runCode: async (sessionId: number, _problemId: string, code: string, language: string, customInput?: string): Promise<ExecutionResult> => {
        try {
            const response = await api.post('/api/code/run', {
                sessionId,
                language: language.toUpperCase(),
                code,
                customInput
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to execute code');
        }
    },
    submitCode: async (sessionId: number, _problemId: string, code: string, language: string): Promise<import('../types').CodeEvaluation> => {
        try {
            const response = await api.post('/api/code/submit', {
                sessionId,
                language: language.toUpperCase(),
                code
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to evaluate code');
        }
    }
};
