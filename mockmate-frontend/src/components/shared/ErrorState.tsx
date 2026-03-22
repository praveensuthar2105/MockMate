import { AlertCircle, RefreshCw } from 'lucide-react';

interface ErrorStateProps {
    message: string;
    onRetry?: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
    return (
        <div className="w-full p-6 flex flex-col items-center justify-center text-center bg-danger-light rounded-lg border border-danger/20">
            <AlertCircle className="text-danger w-10 h-10 mb-3" />
            <h3 className="text-danger font-semibold mb-1">Something went wrong</h3>
            <p className="text-danger/80 text-sm mb-4 max-w-md">{message}</p>
            {onRetry && (
                <button
                    onClick={onRetry}
                    className="flex items-center space-x-2 px-4 py-2 bg-white text-danger font-medium rounded-md shadow-sm border border-danger/10 hover:bg-danger/5 transition-colors"
                >
                    <RefreshCw size={16} />
                    <span>Try Again</span>
                </button>
            )}
        </div>
    );
}
