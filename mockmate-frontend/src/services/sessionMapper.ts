import type { InterviewSession } from '../types';

export const mapInterviewResponseToSession = (data: any): InterviewSession => {
    return {
        id: data.id,
        companyName: data.company,
        jobRole: data.jobRole,
        difficulty: data.difficulty,
        interviewType: data.interviewType || 'FULL_MOCK',
        status: data.status,
        currentPhase: data.currentPhase,
        phaseEndTime: data.phaseEndTime,
        startedAt: data.startedAt,
        endedAt: data.endedAt,
        overallScore: data.totalScore,
        resumeDurationMins: data.resumeDurationMins,
        dsaDurationMins: data.dsaDurationMins,
        systemDesignDurationMins: data.systemDesignDurationMins,
        hrDurationMins: data.hrDurationMins,
        messages: data.messages?.map((msg: any) => ({
            id: msg.id,
            role: msg.role === 'AI' ? 'AI' : 'USER',
            content: msg.content,
            timestamp: msg.timestamp,
            type: 'TEXT', // Default type
            phase: msg.phase
        }))
    };
};