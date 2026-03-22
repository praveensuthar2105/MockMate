import { useState, useRef, useEffect } from 'react';
import { MessageBubble } from './MessageBubble';
import { TypingIndicator } from './TypingIndicator';
import { SendHorizontal, Volume2, VolumeX } from 'lucide-react';
import { useSessionStore } from '../../store/sessionStore';
import { useWebSocket } from '../../hooks/useWebSocket';
import { ArrowRight } from 'lucide-react';
import { useVoiceOutput } from '../../hooks/useVoiceOutput';
import { VoiceMicButton } from './VoiceMicButton';

interface ChatPanelProps {
    sessionId: number;
    isFloating?: boolean;
}

export function ChatPanel({ sessionId, isFloating = false }: ChatPanelProps) {
    const [input, setInput] = useState('');
    const [isVoiceEnabled, setIsVoiceEnabled] = useState(() => {
        const saved = localStorage.getItem('mockmate-voice-enabled');
        return saved === null ? true : saved === 'true';
    });
    const [isMinimized, setIsMinimized] = useState(isFloating);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const { messages, isTyping, addMessage } = useSessionStore();
    const { sendMessage, sendPhaseComplete, isConnected } = useWebSocket(sessionId);

    const { speak, stop: stopSpeaking } = useVoiceOutput();

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages, isTyping]);

    // Persist voice setting
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

    // Speak AI messages when they arrive if voice is enabled
    useEffect(() => {
        if (isVoiceEnabled && messages.length > 0) {
            const lastMessage = messages[messages.length - 1];
            if (lastMessage.sender === 'AI' || lastMessage.sender === 'SYSTEM') {
                speak(lastMessage.content);
            }
        }
        return () => stopSpeaking();
    }, [messages, isVoiceEnabled, speak, stopSpeaking]);

    const handleInputChange = (val: string) => {
        setInput(val);
        if (val.trim()) stopSpeaking();
    };

    const handleTranscriptComplete = (transcript: string) => {
        setInput(transcript);
        // Auto-send after 1.5s if no edits
        setTimeout(() => {
            setInput((current: string) => {
                if (current === transcript && current.trim()) {
                    // Trigger manual submit logic
                    const content = current.trim();
                    if (content && isConnected) {
                        addMessage({
                            id: Date.now(),
                            sender: 'USER',
                            content,
                            timestamp: new Date().toISOString(),
                            type: 'TEXT'
                        });
                        sendMessage(content);
                        return '';
                    }
                }
                return current;
            });
        }, 1500);
    };

    const handleSubmit = (e?: React.FormEvent) => {
        e?.preventDefault();
        const content = input.trim();
        if (!content || !isConnected) return;

        // Optimistic Update
        addMessage({
            id: Date.now(),
            sender: 'USER',
            content,
            timestamp: new Date().toISOString(),
            type: 'TEXT'
        });

        sendMessage(content);
        setInput('');
    };

    const floatingClasses = isFloating
        ? `fixed bottom-6 right-6 z-50 transition-all duration-300 bg-bg-surface border border-border shadow-2xl flex flex-col ${isMinimized ? 'w-14 h-14 rounded-full flex items-center justify-center overflow-hidden' : 'w-[380px] h-[550px] rounded-2xl overflow-hidden backdrop-blur-xl'}`
        : "flex flex-col h-full bg-bg-surface border border-border rounded-xl shadow-sm overflow-hidden relative";

    if (isFloating && isMinimized) {
        return (
            <button
                onClick={() => setIsMinimized(false)}
                className="fixed bottom-6 right-6 w-14 h-14 bg-violet text-white rounded-full shadow-lg flex items-center justify-center hover:scale-110 transition-transform z-50 animate-bounce-subtle"
            >
                <div className="relative">
                    <SendHorizontal size={24} />
                    {isTyping && <div className="absolute -top-1 -right-1 w-3 h-3 bg-success rounded-full border-2 border-violet" />}
                </div>
            </button>
        );
    }

    return (
        <div className={floatingClasses}>

            {/* Header */}
            <div className={`px-5 py-4 border-b border-border bg-bg-surface flex items-center justify-between shrink-0 ${isFloating ? 'cursor-move' : ''}`}>
                <div className="flex items-center space-x-2">
                    <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-success' : 'bg-warning animate-pulse'}`} />
                    <h2 className="font-display font-semibold text-text-primary m-0 text-sm">
                        {isFloating ? 'AI Interviewer' : 'Interviewer AI'}
                    </h2>
                </div>
                <div className="flex items-center space-x-2">
                    {isFloating && (
                        <button
                            onClick={() => setIsMinimized(true)}
                            className="p-1 text-text-tertiary hover:text-text-primary"
                        >
                            <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M4 10H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                            </svg>
                        </button>
                    )}
                    <button
                        onClick={() => setIsVoiceEnabled(!isVoiceEnabled)}
                        className={`p-1.5 rounded-lg transition-colors ${isVoiceEnabled ? 'text-violet bg-violet/10' : 'text-text-tertiary hover:text-text-secondary'}`}
                        title={isVoiceEnabled ? 'Disable AI Voice' : 'Enable AI Voice'}
                    >
                        {isVoiceEnabled ? <Volume2 size={16} /> : <VolumeX size={16} />}
                    </button>
                    {isConnected && messages.length > 2 && (
                        <button
                            onClick={() => sendPhaseComplete()}
                            className={`flex items-center space-x-1.5 bg-violet text-white font-bold rounded-lg hover:bg-violet-dark transition-colors shadow-sm shadow-violet/20 ${isFloating ? 'px-2 py-1 text-[10px]' : 'px-3 py-1.5 text-[11px]'}`}
                        >
                            <span>Next</span>
                            <ArrowRight size={12} />
                        </button>
                    )}
                </div>
            </div>

            {/* Messages Scroll Area */}
            <div className="flex-1 overflow-y-auto p-5 space-y-4 bg-bg-page/40">
                {messages.length === 0 && (
                    <div className="h-full flex flex-col items-center justify-center text-text-tertiary text-center">
                        <p className="text-xs">Say hello to start the session.</p>
                    </div>
                )}

                {messages.map((msg) => (
                    <MessageBubble
                        key={msg.id}
                        content={msg.content}
                        sender={msg.sender}
                        timestamp={msg.timestamp}
                        type={msg.type}
                    />
                ))}
                {isTyping && <TypingIndicator />}
                <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className={`p-4 bg-bg-surface border-t border-border shrink-0 ${isFloating ? 'pb-6' : ''}`}>
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

                    <VoiceMicButton onTranscriptComplete={handleTranscriptComplete} disabled={!isConnected} />

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
