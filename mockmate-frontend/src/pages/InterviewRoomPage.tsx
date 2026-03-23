import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useSessionStore } from '../store/sessionStore';
import { useWebSocket } from '../hooks/useWebSocket';
import { ChatPanel } from '../components/interview/ChatPanel';
import { PhaseProgressBar } from '../components/interview/PhaseProgressBar';
import { CountdownTimer } from '../components/interview/CountdownTimer';
import { DsaPanel } from '../components/interview/DsaPanel';
import { ResumePanel } from '../components/interview/ResumePanel';
import { SystemDesignPanel } from '../components/interview/SystemDesignPanel';
import { HrPanel } from '../components/interview/HrPanel';
import { LoadingSkeleton } from '../components/shared/LoadingSkeleton';
import { ErrorState } from '../components/shared/ErrorState';
import { Volume2, VolumeX } from 'lucide-react';

export default function InterviewRoomPage() {
    const { id } = useParams<{ id: string }>();
    const { currentSession, loading, error, fetchSession, clearSession } = useSessionStore();
    const { isConnected } = useWebSocket(id ? Number(id) : null);

    const [voiceEnabled, setVoiceEnabled] = useState(() => {
        const saved = localStorage.getItem('mockmate-voice-enabled');
        return saved === null ? true : saved === 'true';
    });

    const toggleVoice = () => {
        const newValue = !voiceEnabled;
        setVoiceEnabled(newValue);
        localStorage.setItem('mockmate-voice-enabled', String(newValue));
        // Force a storage event so ChatPanel can sync if needed
        window.dispatchEvent(new Event('storage'));
    };

    useEffect(() => {
        if (!id) return;
        fetchSession(Number(id));
        return () => clearSession();
    }, [id, fetchSession, clearSession]);

    if (loading && !currentSession) return <LoadingSkeleton />;
    if (error) return <ErrorState message={error} onRetry={() => id && fetchSession(Number(id))} />;
    if (!currentSession) return null;

    return (
        <div className="flex flex-col h-screen w-screen overflow-hidden bg-bg-page fixed inset-0 z-50">
            {/* Top Bar (56px) */}
            <div className="h-[56px] bg-bg-surface border-b border-border flex items-center justify-between px-6 shrink-0 z-10">
                <div className="flex items-center space-x-4">
                    <h1 className="font-display font-semibold text-text-primary text-lg">
                        {currentSession.companyName || 'MockMate'} Interview
                    </h1>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-wider ${isConnected ? 'bg-success-light text-success' : 'bg-warning-light text-warning'}`}>
                        {isConnected ? 'Live' : 'Connecting...'}
                    </span>
                    <button
                        onClick={toggleVoice}
                        className={`flex items-center space-x-1.5 px-2.5 py-1 rounded-full transition-all border ${voiceEnabled
                                ? 'bg-violet/10 border-violet/30 text-violet'
                                : 'bg-bg-subtle border-border text-text-tertiary'
                            }`}
                    >
                        {voiceEnabled ? <Volume2 size={12} /> : <VolumeX size={12} />}
                        <span className="text-[10px] font-bold uppercase">Voice</span>
                    </button>
                </div>

                <div className="flex-1 flex justify-center max-w-2xl px-4">
                    <PhaseProgressBar currentPhase={currentSession.currentPhase} />
                </div>

                <div className="flex items-center justify-end min-w-[120px]">
                    <CountdownTimer />
                </div>
            </div>

            {/* Main Content Area */}
            <div className="flex-1 flex flex-row overflow-hidden">
                {/* Left Panel: Chat (44%) */}
                <div className="w-[44%] h-full border-r border-border flex flex-col bg-bg-surface shrink-0">
                    <ChatPanel sessionId={Number(id)} />
                </div>

                {/* Right Panel: Phase Content (56%) */}
                <div className="w-[56%] h-full bg-bg-page overflow-y-auto">
                    {currentSession.currentPhase === 'RESUME_SCREEN' && <ResumePanel />}
                    {currentSession.currentPhase === 'DSA' && <DsaPanel sessionId={Number(id)} />}
                    {currentSession.currentPhase === 'SYSTEM_DESIGN' && <SystemDesignPanel />}
                    {currentSession.currentPhase === 'HR' && <HrPanel />}

                    {!['RESUME_SCREEN', 'DSA', 'SYSTEM_DESIGN', 'HR'].includes(currentSession.currentPhase) && (
                        <div className="h-full flex items-center justify-center">
                            <p className="text-text-tertiary">Waiting for next phase...</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
