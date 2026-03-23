import { api } from './api';
import type { InterviewSession, PhaseType, SessionReport } from '../types';
import { mapChatResponseToMessage, mapInterviewResponseToSession } from './sessionMapper';

interface BackendChatResponse {
    id: number;
    role: 'USER' | 'AI';
    content: string;
    phaseType: InterviewSession['currentPhase'] | null;
    createdAt: string;
}

export const reportService = {
    getReport: async (sessionId: number): Promise<SessionReport> => {
        const [sessionResponse, chatResponse] = await Promise.all([
            api.get(`/api/interviews/${sessionId}`),
            api.get<BackendChatResponse[]>(`/api/chat/${sessionId}`)
        ]);

        const session = mapInterviewResponseToSession(sessionResponse.data);
        session.messages = chatResponse.data.map(mapChatResponseToMessage);

        const phases: PhaseType[] = ['RESUME_SCREEN', 'DSA', 'SYSTEM_DESIGN', 'HR'];
        const hasPhaseMessages = (phase: PhaseType) => session.messages?.some((message) => message.phase === phase) ?? false;
        const completedPhases = phases.filter(hasPhaseMessages);
        const baseScore = session.overallScore ?? 0;

        return {
            overallScore: baseScore,
            phases: phases.map((phase) => ({
                phase,
                score: completedPhases.includes(phase) ? baseScore : 0,
                feedback: completedPhases.includes(phase)
                    ? `${phase.replace('_', ' ')} phase completed.`
                    : `${phase.replace('_', ' ')} phase has limited data.`
            })),
            strengths: completedPhases.length > 0 ? ['Completed interview phases successfully'] : ['Interview data captured'],
            weaknesses: baseScore < 70 ? ['Improve consistency and answer depth'] : ['Continue refining advanced answers'],
            nextSteps: ['Review transcript feedback', 'Practice weak phases', 'Attempt another mock interview']
        };
    },

    getHistory: async (): Promise<InterviewSession[]> => {
        const response = await api.get('/api/interviews/me');
        return response.data
            .map(mapInterviewResponseToSession)
            .sort((a: InterviewSession, b: InterviewSession) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime());
    },

    downloadPdf: async (sessionId: number): Promise<void> => {
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
    }
};
