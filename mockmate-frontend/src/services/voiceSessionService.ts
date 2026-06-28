import { api } from './api';
import type { ChatMessage } from '../types';

export interface VoiceSessionToken {
    token: string;
    model: string;
    expiresAt: string;
}

export const voiceSessionService = {
    async createToken(sessionId: number): Promise<VoiceSessionToken> {
        const response = await api.post<VoiceSessionToken>(
            `/api/sessions/${sessionId}/voice/token`,
        );
        return response.data;
    },

    async saveTranscript(
        sessionId: number,
        role: 'USER' | 'AI',
        content: string,
    ): Promise<ChatMessage> {
        const response = await api.post(
            `/api/sessions/${sessionId}/voice/transcript`,
            { role, content },
        );

        return {
            id: response.data.id,
            role: response.data.role,
            content: response.data.content,
            timestamp: response.data.createdAt,
            phase: response.data.phaseType,
            type: 'TEXT',
        };
    },
};
