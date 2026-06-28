import type {
    ChatMessage,
    InterviewSession,
    InterviewType,
    PhaseType,
} from '../types';

interface BackendChatResponse {
    id: number;
    role: ChatMessage['role'];
    content: string;
    timestamp: string;
    phase: PhaseType | null;
}

interface BackendInterviewResponse {
    id: number;
    company: string;
    jobRole: string;
    difficulty: string;
    interviewType: InterviewType;
    status: InterviewSession['status'];
    currentPhase: PhaseType;
    phaseEndTime: string | null;
    startedAt: string;
    endedAt: string | null;
    totalScore: number | null;
    resumeDurationMins?: number;
    dsaDurationMins?: number;
    systemDesignDurationMins?: number;
    hrDurationMins?: number;
    selectedPhases?: string[];
    messages?: BackendChatResponse[];
    reportJson?: string;
    dsaProblemJson?: string;
}

export function mapInterviewResponseToSession(
    response: BackendInterviewResponse,
): InterviewSession {
    return {
        id: response.id,
        jobRole: response.jobRole,
        companyName: response.company,
        difficulty:
            (response.difficulty?.toUpperCase() as InterviewSession['difficulty']) ||
            'MEDIUM',
        interviewType: response.interviewType,
        status: response.status,
        currentPhase: response.currentPhase,
        phaseEndTime: response.phaseEndTime,
        overallScore: response.totalScore,
        startedAt: response.startedAt,
        endedAt: response.endedAt,
        resumeDurationMins: response.resumeDurationMins,
        dsaDurationMins: response.dsaDurationMins,
        systemDesignDurationMins: response.systemDesignDurationMins,
        hrDurationMins: response.hrDurationMins,
        selectedPhases: response.selectedPhases,
        reportJson: response.reportJson,
        dsaProblemJson: response.dsaProblemJson,
        messages: response.messages?.map(mapChatResponseToMessage) ?? [],
    };
}

export function mapChatResponseToMessage(
    response: BackendChatResponse,
): ChatMessage {
    return {
        id: response.id,
        role: response.role,
        content: response.content,
        timestamp: response.timestamp,
        type: 'TEXT',
        phase: response.phase ?? undefined,
    };
}
