import sys

content = """import { api } from './api';
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
            api.get(`/api/sessions/${sessionId}`),
            api.get<BackendChatResponse[]>(`/api/chat/${sessionId}`)
        ]);

        const session = mapInterviewResponseToSession(sessionResponse.data);
        session.messages = chatResponse.data.map(mapChatResponseToMessage);

        const phases: PhaseType[] = ['RESUME_SCREEN', 'DSA', 'SYSTEM_DESIGN', 'HR'];
        const hasPhaseMessages = (phase: PhaseType) => session.messages?.some((message) => message.phase === phase) ?? false;
        const completedPhases = phases.filter(hasPhaseMessages);
        const baseScore = session.overallScore ?? 0;

        // NEW LOGIC: Use reportJson if available
        if (session.reportJson) {
            try {
                const parsedReport = JSON.parse(session.reportJson);
                // Mapped structure from backend: map of PhaseType -> phase details
                // e.g. { "RESUME_SCREEN": { "score": 80, "feedback": "...", "strengths": [...], "weaknesses": [...] } }

                const mappedPhases = phases.map(phase => {
                    const phaseData = parsedReport[phase];
                    if (phaseData) {
                        return {
                            phase,
                            score: phaseData.score || 0,
                            feedback: phaseData.feedback || `${phase.replace('_', ' ')} phase completed.`
                        };
                    } else {
                        return {
                            phase,
                            score: 0,
                            feedback: `${phase.replace('_', ' ')} phase has limited data.`
                        };
                    }
                });

                // Aggregate strengths and weaknesses from all phases, or use top level if provided
                let allStrengths: string[] = parsedReport.strengths || [];
                let allWeaknesses: string[] = parsedReport.weaknesses || [];
                let nextSteps: string[] = parsedReport.nextSteps || ['Review transcript feedback', 'Practice weak phases', 'Attempt another mock interview'];

                if (allStrengths.length === 0 || allWeaknesses.length === 0) {
                     Object.values(parsedReport).forEach((data: any) => {
                         if (data && Array.isArray(data.strengths)) allStrengths.push(...data.strengths);
                         if (data && Array.isArray(data.weaknesses)) allWeaknesses.push(...data.weaknesses);
                     });
                     // Dedup and limit
                     allStrengths = Array.from(new Set(allStrengths)).slice(0, 5);
                     allWeaknesses = Array.from(new Set(allWeaknesses)).slice(0, 5);
                }

                if (allStrengths.length === 0) allStrengths = completedPhases.length > 0 ? ['Completed interview phases successfully'] : ['Interview data captured'];
                if (allWeaknesses.length === 0) allWeaknesses = baseScore < 70 ? ['Improve consistency and answer depth'] : ['Continue refining advanced answers'];

                return {
                    overallScore: baseScore,
                    phases: mappedPhases,
                    strengths: allStrengths,
                    weaknesses: allWeaknesses,
                    nextSteps: nextSteps
                };

            } catch (e) {
                console.warn("Failed to parse session.reportJson. Falling back to default.");
            }
        }

        // FALLBACK LOGIC for legacy sessions without reportJson
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
"""

with open("mockmate-frontend/src/services/reportService.ts", "w") as f:
    f.write(content)
