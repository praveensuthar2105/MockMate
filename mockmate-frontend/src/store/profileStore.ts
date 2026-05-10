import { create } from 'zustand';
import { resumeService } from '../services/resumeService';
import type { ResumeResponse } from '../services/resumeService';
import { useAuthStore } from './authStore';

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
            // Update auth store as well so guards know the profile is complete
            try {
            useAuthStore.getState().updateUser({ profileComplete: true });
        } catch (err) {
            console.error("Failed to update user profile complete state", err);
        }
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