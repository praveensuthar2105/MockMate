import { api } from './api';

export const resumeService = {
    upload: async (file: File): Promise<{ url: string; text: string }> => {
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await api.post('/api/resume/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            return response.data;
        } catch (error: any) {
            throw new Error(error.response?.data?.message || 'Failed to upload resume');
        }
    },
    getLatest: async (): Promise<{ url: string; text: string } | null> => {
        try {
            const response = await api.get('/api/resume/me');
            return response.data;
        } catch (error: any) {
            if (error.response?.status === 404) return null;
            throw new Error(error.response?.data?.message || 'Failed to fetch resume');
        }
    }
};
