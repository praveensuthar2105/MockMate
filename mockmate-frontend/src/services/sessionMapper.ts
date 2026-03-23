import type { ChatMessage, InterviewSession } from '../types';

interface BackendInterviewResponse {
    id: number;
    company: string;
    difficulty: string;
    status: InterviewSession['status'];
    currentPhase: InterviewSession['currentPhase'];
    phaseEndTime: string | null;
    startedAt: string;
    totalScore: number | null;
}

interface BackendChatResponse {
    id: number;
    role: 'USER' | 'AI';
    content: string;
    phaseType: ChatMessage['phase'] | null;
    createdAt: string;
}

export function mapInterviewResponseToSession(response: BackendInterviewResponse): InterviewSession {
    return {
        id: response.id,
        jobRole: 'Interview',
        companyName: response.company,
        difficulty: (response.difficulty?.toUpperCase() as InterviewSession['difficulty']) || 'MEDIUM',
        interviewType: 'FULL_MOCK',
        status: response.status,
        currentPhase: response.currentPhase,
        phaseEndTime: response.phaseEndTime,
        overallScore: response.totalScore,
        startedAt: response.startedAt,
        endedAt: null,
    };
}

export function mapChatResponseToMessage(response: BackendChatResponse): ChatMessage {
    return {
        id: response.id,
        role: response.role,
        content: response.content,
        timestamp: response.createdAt,
        type: 'TEXT',
        phase: response.phaseType || undefined,
    };
}