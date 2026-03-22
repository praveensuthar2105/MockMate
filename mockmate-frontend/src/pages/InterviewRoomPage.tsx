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
        <div className="flex flex-col h-[calc(100vh-64px)] -m-8 p-4 md:p-6 lg:p-8 overflow-hidden">

            {/* Header Area */}
            <div className="flex flex-col md:flex-row md:items-end justify-between mb-6 shrink-0 gap-4">
                <div>
                    <div className="flex items-center space-x-3 mb-1">
                        <h1 className="text-2xl font-display font-semibold text-text-primary tracking-tight">
                            {currentSession.jobRole} Interview
                        </h1>
                        <span className={`text-[11px] font-bold px-2 py-0.5 rounded uppercase tracking-wider ${isConnected ? 'bg-success-light text-success' : 'bg-warning-light text-warning'
                            }`}>
                            {isConnected ? 'Live' : 'Connecting...'}
                        </span>
                    </div>
                    <p className="text-text-secondary text-sm">
                        {currentSession.companyName} company standards applied.
                    </p>
                </div>

                <div className="flex items-center space-x-6 md:space-x-8">
                    {/* Voice Toggle */}
                    <button
                        onClick={toggleVoice}
                        className={`flex items-center space-x-2 px-3 py-1.5 rounded-full transition-all duration-300 border ${voiceEnabled
                                ? 'bg-violet/10 border-violet/30 text-violet'
                                : 'bg-bg-subtle border-border text-text-tertiary'
                            }`}
                        title={voiceEnabled ? 'AI Voice On' : 'AI Voice Off'}
                    >
                        {voiceEnabled ? <Volume2 size={14} /> : <VolumeX size={14} />}
                        <span className="text-[11px] font-bold uppercase tracking-wider">Voice</span>
                    </button>

                    <PhaseProgressBar currentPhase={currentSession.currentPhase} />
                    <CountdownTimer />
                </div>
            </div>

            {/* Main Workspace Area */}
            <div className="flex-1 flex flex-col lg:flex-row gap-6 min-h-0 overflow-hidden relative">

                {/* Left Side: Chat Panel (only if not DSA) */}
                {currentSession.currentPhase !== 'DSA' && (
                    <div className="w-full lg:w-[400px] h-full shrink-0 flex flex-col">
                        <ChatPanel sessionId={Number(id)} />
                    </div>
                )}

                {/* Right Side: Workspace Area (Dynamic based on phase) */}
                <div className="flex-1 h-full min-h-0">
                    {currentSession.currentPhase === 'RESUME_SCREEN' && <ResumePanel />}
                    {currentSession.currentPhase === 'DSA' && (
                        <>
                            <DsaPanel
                                sessionId={Number(id)}
                                problem={null}
                            />
                            <ChatPanel sessionId={Number(id)} isFloating={true} />
                        </>
                    )}
                    {currentSession.currentPhase === 'SYSTEM_DESIGN' && <SystemDesignPanel />}
                    {currentSession.currentPhase === 'HR' && <HrPanel />}

                    {/* Neutral state/Fallback */}
                    {!['RESUME_SCREEN', 'DSA', 'SYSTEM_DESIGN', 'HR'].includes(currentSession.currentPhase) && (
                        <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex items-center justify-center">
                            <p className="text-text-tertiary">Waiting for next phase...</p>
                        </div>
                    )}
                </div>

            </div>
        </div>
    );
}
