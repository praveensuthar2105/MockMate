import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSessionStore } from '../store/sessionStore';
import { useWebSocket } from '../hooks/useWebSocket';
import { ChatPanel } from '../components/interview/ChatPanel';
import { PhaseProgressBar } from '../components/interview/PhaseProgressBar';
import { CountdownTimer } from '../components/interview/CountdownTimer';
import { DsaPanel } from '../components/interview/DsaPanel';
import { ResumePanel } from '../components/interview/ResumePanel';
import { HrPanel } from '../components/interview/HrPanel';
import { LoadingSkeleton } from '../components/shared/LoadingSkeleton';
import { ErrorState } from '../components/shared/ErrorState';
import { Volume2, VolumeX, Play } from 'lucide-react';
import { useRef } from 'react';

export default function InterviewRoomPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { currentSession, loading, error, fetchSession, clearSession } = useSessionStore();
    const { isConnected } = useWebSocket(id ? Number(id) : null);

    const containerRef = useRef<HTMLDivElement>(null);
    const chatRef = useRef<HTMLDivElement>(null);

    const [voiceEnabled, setVoiceEnabled] = useState(() => {
        const saved = localStorage.getItem('mockmate-voice-enabled');
        return saved === null ? true : saved === 'true';
    });

    const startResizeChat = (e: React.MouseEvent) => {
        e.preventDefault();
        const startX = e.clientX;
        const startWidth = chatRef.current?.getBoundingClientRect().width || 0;
        const containerWidth = containerRef.current?.getBoundingClientRect().width || 1;

        const handleMouseMove = (moveEvent: MouseEvent) => {
            if (!chatRef.current) return;
            const deltaX = moveEvent.clientX - startX;
            const newWidthPercent = ((startWidth + deltaX) / containerWidth) * 100;
            const clampedWidth = Math.min(Math.max(newWidthPercent, 20), 60);
            chatRef.current.style.width = `${clampedWidth}%`;
        };

        const handleMouseUp = () => {
            document.removeEventListener('mousemove', handleMouseMove);
            document.removeEventListener('mouseup', handleMouseUp);
        };

        document.addEventListener('mousemove', handleMouseMove);
        document.addEventListener('mouseup', handleMouseUp);
    };

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

        const endSessionOnLeave = () => {
            const state = useSessionStore.getState();
            if (state.currentSession?.status === 'IN_PROGRESS') {
                import('../services/api').then(({ api }) => {
                    api.post(`/api/sessions/${id}/end`).catch(() => {});
                });
            }
        };

        const handleBeforeUnload = () => {
            const state = useSessionStore.getState();
            if (state.currentSession?.status === 'IN_PROGRESS') {
                try {
                    const token = JSON.parse(localStorage.getItem('auth-storage') || '{}')?.state?.accessToken;
                    if (token) {
                        fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/sessions/${id}/end`, {
                            method: 'POST',
                            headers: {
                                'Authorization': `Bearer ${token}`
                            },
                            keepalive: true
                        }).catch(() => {});
                    }
                } catch (e) {
                    console.error("Failed to extract token for beforeunload beacon");
                }
            }
        };

        window.addEventListener('beforeunload', handleBeforeUnload);

        return () => {
            window.removeEventListener('beforeunload', handleBeforeUnload);
            endSessionOnLeave();
            clearSession();
        };
    }, [id, fetchSession, clearSession]);

    useEffect(() => {
        if (currentSession?.status === 'COMPLETED') {
            navigate(`/interview/${id}/report`, { replace: true });
        }
    }, [currentSession?.status, id, navigate]);

    if (loading && !currentSession) return <LoadingSkeleton />;
    if (error) return <ErrorState message={error} onRetry={() => id && fetchSession(Number(id))} />;
    if (!currentSession || currentSession.status === 'COMPLETED') return null;

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

                <div className="flex items-center justify-end min-w-[120px] space-x-4">
                    <button
                        onClick={() => id && useSessionStore.getState().nextPhase(Number(id))}
                        className="flex items-center space-x-1.5 px-3 py-1.5 bg-violet/10 border border-violet/30 text-violet rounded-md hover:bg-violet/20 transition-all group shadow-sm active:scale-95"
                        title="Skip to next round (Debug)"
                    >
                        <Play className="fill-current" size={12} />
                        <span className="text-[10px] font-bold uppercase tracking-wider">Next Round</span>
                    </button>
                    <CountdownTimer />
                </div>
            </div>

            {/* Main Content Area */}
            <div ref={containerRef} className="flex-1 flex flex-row overflow-hidden">
                {/* Left Panel: Chat */}
                <div ref={chatRef} style={{ width: '44%' }} className="h-full border-r border-border flex flex-col bg-bg-surface shrink-0">
                    <ChatPanel sessionId={Number(id)} />
                </div>

                {/* Resize Divider */}
                <div
                    onMouseDown={startResizeChat}
                    className="w-1.5 h-full cursor-col-resize hover:bg-violet/30 transition-colors flex items-center justify-center group shrink-0 z-20"
                >
                    <div className="w-0.5 h-8 bg-border group-hover:bg-violet/50 rounded-full" />
                </div>

                {/* Right Panel: Phase Content */}
                <div className="flex-1 h-full bg-bg-page overflow-y-auto">
                    {currentSession.currentPhase === 'RESUME_SCREEN' && <ResumePanel />}
                    {currentSession.currentPhase === 'DSA' && <DsaPanel sessionId={Number(id)} />}
                    {currentSession.currentPhase === 'HR' && <HrPanel />}

                    {!['RESUME_SCREEN', 'DSA', 'HR'].includes(currentSession.currentPhase) && (
                        <div className="h-full flex items-center justify-center">
                            <p className="text-text-tertiary">Waiting for next phase...</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
