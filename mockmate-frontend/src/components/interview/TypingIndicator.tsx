export function TypingIndicator() {
    return (
        <div className="flex w-full justify-start mb-4 animate-in fade-in duration-300">
            <div className="w-8 h-8 rounded-full bg-violet flex items-center justify-center shrink-0 shadow-sm mr-3">
                <span className="font-display font-semibold text-white text-xs">AI</span>
            </div>

            <div className="bg-bg-surface border border-border px-4 py-3 rounded-2xl rounded-bl-sm flex items-center space-x-1.5 h-[42px] shadow-sm">
                <div className="w-2 h-2 rounded-full bg-border-strong animate-bounce" style={{ animationDelay: '0ms' }}></div>
                <div className="w-2 h-2 rounded-full bg-border-strong animate-bounce" style={{ animationDelay: '150ms' }}></div>
                <div className="w-2 h-2 rounded-full bg-border-strong animate-bounce" style={{ animationDelay: '300ms' }}></div>
            </div>
        </div>
    );
}
