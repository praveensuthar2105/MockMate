import { createContext } from 'react';
import type { VoiceConnectionState } from './useGeminiLiveVoice';

export interface InterviewRealtimeContextValue {
    isConnected: boolean;
    sendMessage: (content: string) => void;
    sendCodeDraft: (code: string, language: string) => void;
    voiceState: VoiceConnectionState;
    isVoiceActive: boolean;
    isMuted: boolean;
    partialTranscript: string;
    voiceError: string | null;
    connectVoice: () => Promise<void>;
    disconnectVoice: () => void;
    toggleMute: () => void;
}

export const InterviewRealtimeContext =
    createContext<InterviewRealtimeContextValue | null>(null);
