import { create } from 'zustand';
import { api } from '../services/api';
import type { InterviewSession, ChatMessage } from '../types';
import { mapInterviewResponseToSession } from '../services/sessionMapper';

interface SessionState {
    currentSession: InterviewSession | null;
    recentSessions: InterviewSession[];
    messages: ChatMessage[];
    isTyping: boolean;
    timeRemaining: number | null;
    loading: boolean;
    error: string | null;
    fetchRecentSessions: () => Promise<void>;
    fetchSession: (id: number) => Promise<void>;
    updateSession: (update: Partial<InterviewSession>) => void;
    updatePhase: (phase: string) => void;
    addMessage: (message: ChatMessage) => void;
    setTyping: (isTyping: boolean) => void;
    setTimeRemaining: (seconds: number | null) => void;
    clearSession: () => void;
}

export const useSessionStore = create<SessionState>((set) => ({
    currentSession: null,
    recentSessions: [],
    messages: [],
    isTyping: false,
    timeRemaining: null,
    loading: false,
    error: null,
    setTyping: (isTyping) => set({ isTyping }),
    setTimeRemaining: (seconds) => set({ timeRemaining: seconds }),
    addMessage: (message) => set((state) => ({
        messages: [...state.messages, message],
        isTyping: false
    })),
    clearSession: () => set({ currentSession: null, messages: [], isTyping: false, timeRemaining: null }),
    updateSession: (update) => set((state) => ({
        currentSession: state.currentSession ? { ...state.currentSession, ...update } : null
    })),
    updatePhase: (phase) => set((state) => ({
        currentSession: state.currentSession ? { ...state.currentSession, currentPhase: phase as any } : null
    })),
    fetchSession: async (id: number) => {
        set({ loading: true, error: null });
        try {
            const response = await api.get(`/api/interviews/${id}`);
            const session = mapInterviewResponseToSession(response.data);
            set({ currentSession: session, loading: false });
        } catch (error: any) {
            set({ error: error.message, loading: false });
        }
    },
    fetchRecentSessions: async () => {
        set({ loading: true, error: null });
        try {
            const response = await api.get('/api/interviews/me');
            const sessions = response.data.map(mapInterviewResponseToSession);
            set({ recentSessions: sessions, loading: false });
        } catch (error: any) {
            set({ error: error.message, loading: false });
        }
    },
}));
