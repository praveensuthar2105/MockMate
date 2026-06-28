import type { PropsWithChildren } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import { InterviewRealtimeContext } from './interviewRealtimeContext';
import { useGeminiLiveVoice } from './useGeminiLiveVoice';

interface InterviewRealtimeProviderProps extends PropsWithChildren {
    sessionId: number;
}

export function InterviewRealtimeProvider({
    sessionId,
    children,
}: InterviewRealtimeProviderProps) {
    const realtime = useWebSocket(sessionId);
    const voice = useGeminiLiveVoice(sessionId);

    return (
        <InterviewRealtimeContext.Provider value={{ ...realtime, ...voice }}>
            {children}
        </InterviewRealtimeContext.Provider>
    );
}
