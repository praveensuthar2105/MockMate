import { useContext } from 'react';
import { InterviewRealtimeContext } from './interviewRealtimeContext';

export function useInterviewRealtime() {
    const context = useContext(InterviewRealtimeContext);

    if (!context) {
        throw new Error('useInterviewRealtime must be used inside InterviewRealtimeProvider');
    }

    return context;
}
