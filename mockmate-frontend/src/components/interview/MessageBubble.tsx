interface MessageBubbleProps {
    content: string;
    sender: 'USER' | 'AI' | 'SYSTEM';
    timestamp: string;
    type?: 'TEXT' | 'CODE' | 'FEEDBACK';
}

export function MessageBubble({ content, sender, timestamp, type = 'TEXT' }: MessageBubbleProps) {
    const isUser = sender === 'USER';
    const isSystem = sender === 'SYSTEM';

    if (isSystem) {
        return (
            <div className="flex justify-center my-4">
                <span className="bg-bg-subtle text-text-tertiary text-xs px-3 py-1 rounded-full font-medium tracking-wide shadow-sm border border-border">
                    {content}
                </span>
            </div>
        );
    }

    return (
        <div className={`flex w-full ${isUser ? 'justify-end' : 'justify-start'} mb-4 animate-in fade-in slide-in-from-bottom-2 duration-300`}>
            {!isUser && (
                <div className="w-8 h-8 rounded-full bg-violet flex items-center justify-center shrink-0 shadow-sm mr-3">
                    <span className="font-display font-semibold text-white text-xs">AI</span>
                </div>
            )}

            <div className={`max-w-[80%] lg:max-w-[70%] flex flex-col ${isUser ? 'items-end' : 'items-start'}`}>
                <div
                    className={`px-4 py-3 rounded-2xl shadow-sm overflow-hidden ${isUser
                            ? 'bg-violet text-white rounded-br-sm'
                            : 'bg-bg-surface border border-border text-text-primary rounded-bl-sm'
                        } ${type === 'CODE' ? 'font-mono text-[13px] whitespace-pre-wrap' : 'text-[15px] leading-relaxed'}`}
                >
                    {content}
                </div>

                <span className="text-[11px] font-medium text-text-tertiary mt-1 px-1">
                    {new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
            </div>

            {isUser && (
                <div className="w-8 h-8 rounded-full bg-bg-overlay border border-border flex items-center justify-center shrink-0 shadow-sm ml-3">
                    <span className="font-display font-medium text-text-secondary text-xs">You</span>
                </div>
            )}
        </div>
    );
}
