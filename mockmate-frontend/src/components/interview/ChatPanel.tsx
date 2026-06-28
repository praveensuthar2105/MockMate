import { useState, useRef, useEffect } from 'react';
import { MessageBubble } from './MessageBubble';
import { TypingIndicator } from './TypingIndicator';
import { SendHorizontal, Volume2, VolumeX } from 'lucide-react';
import { useSessionStore } from '../../store/sessionStore';
import { useVoiceOutput } from '../../hooks/useVoiceOutput';
import { useVoiceInput } from '../../hooks/useVoiceInput';
import { VoiceMicButton } from './VoiceMicButton';
import { useInterviewRealtime } from '../../realtime/useInterviewRealtime';

interface ChatPanelProps {
    isFloating?: boolean;
    onCollapse?: () => void;
}

export function ChatPanel({ isFloating = false, onCollapse }: ChatPanelProps) {
    const [input, setInput] = useState('');
    const [isVoiceEnabled, setIsVoiceEnabled] = useState(() => {
        const saved = localStorage.getItem('mockmate-voice-enabled');
        return saved === null ? true : saved === 'true';
    });
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const { messages, isTyping, addMessage } = useSessionStore();
    
    // Real-time context containing STOMP controls and Gemini Live controls
    const {
        sendMessage,
        isConnected,
        isVoiceActive,
        isMuted,
        partialTranscript: livePartialTranscript,
        toggleMute
    } = useInterviewRealtime();

    // Legacy Speech Synthesis (AI read-aloud)
    const { speak, stop: stopSpeaking } = useVoiceOutput();
    
    // Legacy Speech Recognition (browser mic to text box)
    const {
        isListening,
        transcript,
        finalTranscript,
        startListening,
        stopListening,
        isSupported,
        resetTranscript
    } = useVoiceInput();

    const baseTextRef = useRef('');

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages, isTyping]);

    // Persist legacy voice setting
    useEffect(() => {
        localStorage.setItem('mockmate-voice-enabled', String(isVoiceEnabled));
    }, [isVoiceEnabled]);

    // Sync with other components via storage event
    useEffect(() => {
        const handleStorage = () => {
            const saved = localStorage.getItem('mockmate-voice-enabled');
            if (saved !== null) setIsVoiceEnabled(saved === 'true');
        };
        window.addEventListener('storage', handleStorage);
        return () => window.removeEventListener('storage', handleStorage);
    }, []);

    // Speak AI messages when they arrive (ONLY in legacy mode when voice is enabled)
    useEffect(() => {
        if (!isVoiceActive && isVoiceEnabled && messages.length > 0) {
            const lastMessage = messages[messages.length - 1];
            if (lastMessage.role === 'AI' || lastMessage.role === 'SYSTEM') {
                speak(lastMessage.content);
            }
        }
        return () => stopSpeaking();
    }, [messages, isVoiceEnabled, isVoiceActive, speak, stopSpeaking]);

    // Sync speech transcript in real-time (ONLY in legacy mode)
    useEffect(() => {
        if (!isVoiceActive && isListening) {
            const speechText = [finalTranscript, transcript].filter(Boolean).join(' ');
            const newValue = baseTextRef.current
                ? `${baseTextRef.current} ${speechText}`
                : speechText;
            setInput(newValue);
        }
    }, [finalTranscript, transcript, isListening, isVoiceActive]);

    const handleInputChange = (val: string) => {
        setInput(val);
        if (val.trim()) stopSpeaking();
        if (!isVoiceActive && isListening) {
            stopListening();
            resetTranscript();
            baseTextRef.current = '';
        }
    };

    const handleMicClick = () => {
        if (isVoiceActive) {
            toggleMute();
        } else {
            if (isListening) {
                stopListening();
            } else {
                baseTextRef.current = input;
                startListening();
            }
        }
    };

    const handleSubmit = (e?: React.FormEvent) => {
        e?.preventDefault();
        const content = input.trim();
        if (!content || !isConnected) return;

        if (!isVoiceActive && isListening) {
            stopListening();
            resetTranscript();
        }
        baseTextRef.current = '';

        // Optimistic Update
        addMessage({
            id: crypto.randomUUID(),
            role: 'USER',
            content,
            timestamp: new Date().toISOString(),
        });

        sendMessage(content);
        setInput('');
    };

    /* When isFloating, the outer container (border, shadow, header, collapse)
       is managed by the parent (InterviewRoomPage floating overlay).
       ChatPanel only renders messages + input bar. */

    return (
        <div className={isFloating
            ? "flex flex-col flex-1 overflow-hidden"
            : "flex flex-col h-full bg-bg-surface border border-border rounded-xl shadow-sm overflow-hidden relative"
        }>
            {/* Header — only shown in non-floating (legacy) mode */}
            {!isFloating && (
                <div className="px-5 py-4 border-b border-border bg-bg-surface flex items-center justify-between shrink-0">
                    <div className="flex items-center space-x-2">
                        <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-success' : 'bg-warning animate-pulse'}`} />
                        <h2 className="font-display font-semibold text-text-primary m-0 text-sm">
                            Interviewer AI
                        </h2>
                    </div>
                    <div className="flex items-center space-x-2">
                        {!isVoiceActive && (
                            <button
                                onClick={() => setIsVoiceEnabled(!isVoiceEnabled)}
                                className={`p-1.5 rounded-lg transition-colors ${isVoiceEnabled ? 'text-violet bg-violet/10' : 'text-text-tertiary hover:text-text-secondary'}`}
                                title={isVoiceEnabled ? 'Disable AI Voice' : 'Enable AI Voice'}
                            >
                                {isVoiceEnabled ? <Volume2 size={16} /> : <VolumeX size={16} />}
                            </button>
                        )}
                    </div>
                </div>
            )}

            {/* Messages Scroll Area */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-bg-page/40">
                {messages.length === 0 && (
                    <div className="h-full flex flex-col items-center justify-center text-text-tertiary text-center">
                        <p className="text-xs">Say hello to start the session.</p>
                    </div>
                )}

                {messages.map((msg) => (
                    <MessageBubble
                        key={msg.id}
                        content={msg.content}
                        role={msg.role}
                        timestamp={msg.timestamp}
                    />
                ))}
                {isTyping && <TypingIndicator />}
                <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className={`p-3 bg-bg-surface border-t border-border shrink-0 ${isFloating ? 'pb-4' : ''}`}>
                {/* Legacy Listening State Status bar */}
                {!isVoiceActive && isListening && (
                    <div className="mb-2 px-3 py-1.5 bg-violet-light/50 border border-violet/10 rounded-xl flex items-center justify-between text-[11px] text-violet animate-in fade-in slide-in-from-bottom-1 duration-200">
                        <div className="flex items-center space-x-2">
                            <span className="relative flex h-2 w-2">
                                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-violet opacity-75"></span>
                                <span className="relative inline-flex rounded-full h-2 w-2 bg-violet"></span>
                            </span>
                            <span className="font-medium">Listening continuously... Speak your answer</span>
                        </div>
                        <div className="flex items-end space-x-[2px] h-3 pb-[1px]">
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-1 h-[8px]" />
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-2 h-[12px]" />
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-3 h-[6px]" />
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-4 h-[10px]" />
                        </div>
                    </div>
                )}

                {/* Real-time Voice Active Status bar */}
                {isVoiceActive && (
                    <div className="mb-2 px-3 py-1.5 bg-violet-light/50 border border-violet/10 rounded-xl flex items-center justify-between text-[11px] text-violet animate-in fade-in duration-200">
                        <div className="flex items-center space-x-2 overflow-hidden">
                            <span className="relative flex h-2 w-2 shrink-0">
                                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-violet opacity-75"></span>
                                <span className="relative inline-flex rounded-full h-2 w-2 bg-violet"></span>
                            </span>
                            <span className="font-medium truncate">
                                {isMuted
                                    ? 'Microphone muted (Real-time)'
                                    : livePartialTranscript
                                        ? `Transcribing: "${livePartialTranscript}"`
                                        : 'Real-time Voice Active... Speak anytime'}
                            </span>
                        </div>
                        <div className="flex items-end space-x-[2px] h-3 pb-[1px] shrink-0">
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-1 h-[8px]" />
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-2 h-[12px]" />
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-3 h-[6px]" />
                            <div className="w-[2px] bg-violet rounded-full animate-sound-wave-4 h-[10px]" />
                        </div>
                    </div>
                )}

                <form onSubmit={handleSubmit} className="flex items-end space-x-2">
                    <div className="flex-1 relative flex items-center bg-bg-page border border-border rounded-xl focus-within:ring-1 focus-within:ring-violet focus-within:border-violet transition-colors">
                        <textarea
                            value={input}
                            onChange={(e) => handleInputChange(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && !e.shiftKey) {
                                    e.preventDefault();
                                    if (input.trim()) handleSubmit();
                                }
                            }}
                            placeholder={isFloating ? "Type here..." : "Type or use voice..."}
                            className="w-full bg-transparent border-none focus:outline-none focus:ring-0 py-2 pl-3 pr-10 min-h-[40px] max-h-[80px] resize-none text-[13px]"
                            rows={1}
                        />
                    </div>

                    <VoiceMicButton
                        isListening={isVoiceActive ? !isMuted : isListening}
                        isSupported={isVoiceActive ? true : isSupported}
                        onClick={handleMicClick}
                        disabled={!isConnected}
                    />

                    <button
                        type="submit"
                        disabled={!input.trim() || !isConnected}
                        className="w-10 h-10 shrink-0 bg-violet hover:bg-violet-dark text-white rounded-xl flex items-center justify-center transition-colors disabled:opacity-50 shadow-sm"
                    >
                        <SendHorizontal size={16} />
                    </button>
                </form>
            </div>
        </div>
    );
}
