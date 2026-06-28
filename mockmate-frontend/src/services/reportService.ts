import { api } from './api';
import type { InterviewSession, PhaseType, SessionReport } from '../types';
import { mapInterviewResponseToSession } from './sessionMapper';

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
            api.get(`/api/sessions/${sessionId}`),
            api.get<BackendChatResponse[]>(`/api/chat/${sessionId}`)
        ]);

        const session = mapInterviewResponseToSession(sessionResponse.data);
        session.messages = chatResponse.data.map((msg) => ({
            id: msg.id,
            role: msg.role === 'USER' ? 'USER' : 'AI',
            content: msg.content,
            timestamp: msg.createdAt,
            type: 'TEXT',
            phase: msg.phaseType ?? undefined
        }));

        const phases: PhaseType[] = ['RESUME_SCREEN', 'DSA', 'SYSTEM_DESIGN', 'HR'];
        const hasPhaseMessages = (phase: PhaseType) => session.messages?.some((message) => message.phase === phase) ?? false;
        const completedPhases = phases.filter(hasPhaseMessages);
        const baseScore = session.overallScore ?? 0;

        let backendReport: Record<string, any> = {};
        if (session.reportJson) {
            try {
                backendReport = JSON.parse(session.reportJson);
            } catch (e) {
                console.error('Failed to parse backend reportJson', e);
            }
        }

        const strengthsList: string[] = [];
        const weaknessesList: string[] = [];
        const nextStepsList: string[] = [];

        Object.keys(backendReport).forEach((key) => {
            const phaseEval = backendReport[key];
            if (phaseEval) {
                if (Array.isArray(phaseEval.strengths)) {
                    strengthsList.push(...phaseEval.strengths);
                }
                if (Array.isArray(phaseEval.weaknesses)) {
                    weaknessesList.push(...phaseEval.weaknesses);
                }
                if (Array.isArray(phaseEval.recommendations)) {
                    nextStepsList.push(...phaseEval.recommendations);
                }
            }
        });

        const strengths = Array.from(new Set(strengthsList)).filter(Boolean);
        const weaknesses = Array.from(new Set(weaknessesList)).filter(Boolean);
        const nextSteps = Array.from(new Set(nextStepsList)).filter(Boolean);

        const finalStrengths = strengths.length > 0 
            ? strengths 
            : (completedPhases.length > 0 
                ? ['Completed interview phases successfully'] 
                : ['Interview data captured']);
                
        const finalWeaknesses = weaknesses.length > 0 
            ? weaknesses 
            : (baseScore < 70 ? ['Improve consistency and answer depth'] : ['Continue refining advanced answers']);
            
        const finalNextSteps = nextSteps.length > 0 
            ? nextSteps 
            : ['Review transcript feedback', 'Practice weak phases', 'Attempt another mock interview'];

        return {
            overallScore: baseScore,
            phases: phases.map((phase) => {
                const phaseEval = backendReport[phase];
                return {
                    phase,
                    score: phaseEval ? (phaseEval.score ?? 0) : (completedPhases.includes(phase) ? baseScore : 0),
                    feedback: phaseEval 
                        ? (phaseEval.feedback ?? `${phase.replace('_', ' ')} phase completed.`)
                        : (completedPhases.includes(phase) 
                            ? `${phase.replace('_', ' ')} phase completed.` 
                            : `${phase.replace('_', ' ')} phase has limited data.`)
                };
            }),
            strengths: finalStrengths,
            weaknesses: finalWeaknesses,
            nextSteps: finalNextSteps
        };
    },

    getHistory: async (): Promise<InterviewSession[]> => {
        const response = await api.get('/api/sessions/me');
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
