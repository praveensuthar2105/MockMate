import { create } from 'zustand';
import { resumeService } from '../services/resumeService';
import type { ResumeResponse } from '../services/resumeService';

interface ProfileState {
    resume: ResumeResponse | null;
    loading: boolean;
    error: string | null;
    uploadResume: (file: File) => Promise<void>;
    fetchResume: () => Promise<void>;
}

export const useProfileStore = create<ProfileState>((set) => ({
    resume: null,
    loading: false,
    error: null,
    uploadResume: async (file: File) => {
        set({ loading: true, error: null });
        try {
            const resume = await resumeService.upload(file);
            set({ resume, loading: false });
        } catch (error: any) {
            set({ error: error.message, loading: false });
        }
    },
    fetchResume: async () => {
        set({ loading: true, error: null });
        try {
            const resume = await resumeService.getLatest();
            set({ resume, loading: false });
        } catch (error: any) {
            set({ error: error.message, loading: false });
        }
    },
}));