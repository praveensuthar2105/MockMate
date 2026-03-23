interface MessageBubbleProps {
    content: string;
    role: string;
    timestamp: string;
}

export function MessageBubble({ content, role, timestamp }: MessageBubbleProps) {
    const isUser = role === 'USER';
    const isSystem = role === 'SYSTEM';

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
                    className={`px-4 py-3 rounded-2xl shadow-sm overflow-hidden text-[15px] leading-relaxed whitespace-pre-wrap ${isUser
                            ? 'bg-violet text-white rounded-br-sm'
                            : 'bg-bg-surface border border-border text-text-primary rounded-bl-sm'
                        }`}
                >
                    {content}
                </div>

                <span className="text-[11px] font-medium text-text-tertiary mt-1 px-1">
                    {new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
            </div>
        </div>
    );
}
