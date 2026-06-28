import { useEffect, useState, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSessionStore } from '../store/sessionStore';
import { ChatPanel } from '../components/interview/ChatPanel';
import { PhaseProgressBar } from '../components/interview/PhaseProgressBar';
import { CountdownTimer } from '../components/interview/CountdownTimer';
import { DsaPanel } from '../components/interview/DsaPanel';
import { ResumePanel } from '../components/interview/ResumePanel';
import { HrPanel } from '../components/interview/HrPanel';
import { LoadingSkeleton } from '../components/shared/LoadingSkeleton';
import { ErrorState } from '../components/shared/ErrorState';
import { Volume2, VolumeX, Play, AlertCircle, MessageSquare } from 'lucide-react';
import { InterviewRealtimeProvider } from '../realtime/InterviewRealtimeProvider';
import { useInterviewRealtime } from '../realtime/useInterviewRealtime';

export default function InterviewRoomPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { currentSession, loading, error, fetchSession, clearSession } = useSessionStore();

    useEffect(() => {
        if (!id) return;
        fetchSession(Number(id));

        return () => {
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
        <InterviewRealtimeProvider sessionId={Number(id)}>
            <InterviewRoomContent />
        </InterviewRealtimeProvider>
    );
}

/* ─── Constants for chat overlay ───────────────────────────────────── */
const DEFAULT_CHAT_W = 400;
const DEFAULT_CHAT_H = 520;
const MIN_CHAT_W = 300;
const MIN_CHAT_H = 350;
const MAX_CHAT_W = 700;
const MAX_CHAT_H = 800;

function InterviewRoomContent() {
    const { id } = useParams<{ id: string }>();
    const { currentSession } = useSessionStore();
    const {
        isConnected,
        isVoiceActive,
        voiceState,
        voiceError,
        connectVoice,
        disconnectVoice
    } = useInterviewRealtime();

    /* ── Chat overlay state ─────────────────────────────────────────── */
    const [chatOpen, setChatOpen] = useState(true);
    const [chatSize, setChatSize] = useState({ w: DEFAULT_CHAT_W, h: DEFAULT_CHAT_H });
    const [chatPos, setChatPos] = useState({ x: 24, y: 24 }); // offset from bottom-right

    /* ── Auto-connect voice on mount ────────────────────────────────── */
    const voiceAttemptedRef = useRef(false);
    useEffect(() => {
        if (voiceAttemptedRef.current || !isConnected) return;
        voiceAttemptedRef.current = true;

        const timer = setTimeout(() => {
            connectVoice().catch((err) => {
                console.warn('[MockMate] Voice auto-connect failed, falling back to text chat:', err);
                // Voice failed — text chat is already available as fallback, no extra action needed
            });
        }, 1500); // small delay to let STOMP finish connecting

        return () => clearTimeout(timer);
    }, [isConnected, connectVoice]);

    /* ── Drag handler (move the chat panel) ─────────────────────────── */
    const dragRef = useRef<{ startX: number; startY: number; startPosX: number; startPosY: number } | null>(null);

    const onDragStart = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        dragRef.current = {
            startX: e.clientX,
            startY: e.clientY,
            startPosX: chatPos.x,
            startPosY: chatPos.y,
        };

        const onMove = (ev: MouseEvent) => {
            if (!dragRef.current) return;
            const dx = dragRef.current.startX - ev.clientX;
            const dy = dragRef.current.startY - ev.clientY;
            const newX = Math.max(0, dragRef.current.startPosX + dx);
            const newY = Math.max(0, dragRef.current.startPosY + dy);
            setChatPos({ x: newX, y: newY });
        };

        const onUp = () => {
            dragRef.current = null;
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
        };

        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    }, [chatPos]);

    /* ── Resize handler (resize the chat panel from corner) ─────────── */
    const resizeRef = useRef<{ startX: number; startY: number; startW: number; startH: number } | null>(null);

    const onResizeStart = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        resizeRef.current = {
            startX: e.clientX,
            startY: e.clientY,
            startW: chatSize.w,
            startH: chatSize.h,
        };

        const onMove = (ev: MouseEvent) => {
            if (!resizeRef.current) return;
            // Dragging top-left corner: moving left increases width, moving up increases height
            const dw = resizeRef.current.startX - ev.clientX;
            const dh = resizeRef.current.startY - ev.clientY;
            const newW = Math.min(MAX_CHAT_W, Math.max(MIN_CHAT_W, resizeRef.current.startW + dw));
            const newH = Math.min(MAX_CHAT_H, Math.max(MIN_CHAT_H, resizeRef.current.startH + dh));
            setChatSize({ w: newW, h: newH });
        };

        const onUp = () => {
            resizeRef.current = null;
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
        };

        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    }, [chatSize]);

    const toggleVoice = () => {
        if (isVoiceActive) {
            disconnectVoice();
        } else {
            void connectVoice();
        }
    };

    return (
        <div className="flex flex-col h-screen w-screen overflow-hidden bg-bg-page fixed inset-0 z-50">
            {/* Top Bar (56px) */}
            <div className="h-[56px] bg-bg-surface border-b border-border flex items-center justify-between px-6 shrink-0 z-10">
                <div className="flex items-center space-x-4">
                    <h1 className="font-display font-semibold text-text-primary text-lg">
                        {currentSession?.companyName || 'MockMate'} Interview
                    </h1>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase tracking-wider ${isConnected ? 'bg-success-light text-success' : 'bg-warning-light text-warning'}`}>
                        {isConnected ? 'Live' : 'Connecting...'}
                    </span>
                    <button
                        onClick={toggleVoice}
                        className={`flex items-center space-x-1.5 px-3 py-1 rounded-full transition-all border ${isVoiceActive
                            ? 'bg-violet/10 border-violet/30 text-violet shadow-sm'
                            : 'bg-bg-subtle border-border text-text-tertiary hover:border-violet/30 hover:text-violet/70'
                            }`}
                        title={isVoiceActive ? 'Disconnect real-time voice' : 'Connect real-time voice'}
                    >
                        {isVoiceActive ? <Volume2 size={12} className="animate-pulse" /> : <VolumeX size={12} />}
                        <span className="text-[10px] font-bold uppercase tracking-wide">
                            {isVoiceActive ? `Voice: ${voiceState}` : 'Voice Off'}
                        </span>
                    </button>
                </div>

                <div className="flex-1 flex justify-center max-w-2xl px-4">
                    <PhaseProgressBar currentPhase={currentSession?.currentPhase || 'RESUME_SCREEN'} />
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

            {/* Voice Error Banner */}
            {voiceError && (
                <div className="bg-danger/10 border-b border-danger/20 px-6 py-2 flex items-center space-x-2 text-danger text-xs animate-in slide-in-from-top duration-200">
                    <AlertCircle size={14} />
                    <span className="font-medium">{voiceError}</span>
                </div>
            )}

            {/* Main Content Area — full width for phase panel */}
            <div className="flex-1 overflow-hidden relative">
                {/* Full-width Phase Content */}
                <div className="h-full w-full bg-bg-page overflow-y-auto">
                    {currentSession?.currentPhase === 'RESUME_SCREEN' && <ResumePanel />}
                    {currentSession?.currentPhase === 'DSA' && <DsaPanel sessionId={Number(id)} />}
                    {currentSession?.currentPhase === 'HR' && <HrPanel />}

                    {currentSession && !['RESUME_SCREEN', 'DSA', 'HR'].includes(currentSession.currentPhase) && (
                        <div className="h-full flex items-center justify-center">
                            <p className="text-text-tertiary">Waiting for next phase...</p>
                        </div>
                    )}
                </div>

                {/* ── Floating Chat Overlay ──────────────────────────── */}
                {chatOpen ? (
                    <div
                        className="fixed z-[60] flex flex-col bg-bg-surface border border-border shadow-2xl rounded-2xl overflow-hidden backdrop-blur-xl"
                        style={{
                            width: chatSize.w,
                            height: chatSize.h,
                            right: chatPos.x,
                            bottom: chatPos.y,
                            transition: resizeRef.current || dragRef.current ? 'none' : 'box-shadow 0.2s',
                        }}
                    >
                        {/* Resize handle — top-left corner */}
                        <div
                            onMouseDown={onResizeStart}
                            className="absolute top-0 left-0 w-5 h-5 cursor-nw-resize z-[70] group"
                            title="Drag to resize"
                        >
                            <div className="absolute top-1 left-1 w-2.5 h-2.5 border-t-2 border-l-2 border-violet/40 group-hover:border-violet rounded-tl-sm transition-colors" />
                        </div>

                        {/* Draggable header */}
                        <div
                            onMouseDown={onDragStart}
                            className="px-4 py-3 border-b border-border bg-bg-surface flex items-center justify-between shrink-0 cursor-move select-none"
                        >
                            <div className="flex items-center space-x-2">
                                <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-success' : 'bg-warning animate-pulse'}`} />
                                <h2 className="font-display font-semibold text-text-primary m-0 text-sm">
                                    AI Interviewer
                                </h2>
                            </div>
                            <button
                                onClick={() => setChatOpen(false)}
                                className="p-1 text-text-tertiary hover:text-text-primary rounded-md hover:bg-bg-subtle transition-colors"
                                title="Minimize chat"
                            >
                                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M4 9H14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                                </svg>
                            </button>
                        </div>

                        {/* Chat content */}
                        <ChatPanel isFloating onCollapse={() => setChatOpen(false)} />
                    </div>
                ) : (
                    /* ── Collapsed FAB ──────────────────────────────── */
                    <button
                        onClick={() => setChatOpen(true)}
                        className="fixed bottom-6 right-6 z-[60] w-14 h-14 bg-violet text-white rounded-full shadow-lg flex items-center justify-center hover:scale-110 transition-transform active:scale-95"
                        title="Open chat"
                    >
                        <div className="relative">
                            <MessageSquare size={22} />
                            {/* Pulse indicator when typing */}
                            <div className="absolute -top-1 -right-1 w-3 h-3 bg-success rounded-full border-2 border-violet animate-pulse" />
                        </div>
                    </button>
                )}
            </div>
        </div>
    );
}
