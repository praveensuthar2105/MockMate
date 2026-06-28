import { Mic, MicOff } from 'lucide-react';

interface VoiceMicButtonProps {
    isListening: boolean;
    isSupported: boolean;
    onClick: () => void;
    disabled?: boolean;
}

export function VoiceMicButton({ isListening, isSupported, onClick, disabled = false }: VoiceMicButtonProps) {
    if (!isSupported) {
        return (
            <button
                type="button"
                disabled
                className="w-11 h-11 rounded-xl flex items-center justify-center bg-bg-subtle text-text-tertiary cursor-not-allowed opacity-50"
                title="Voice input requires Chrome or Edge"
            >
                <Mic size={20} />
            </button>
        );
    }

    return (
        <div className="relative">
            <button
                type="button"
                onClick={onClick}
                disabled={disabled}
                className={`w-11 h-11 rounded-xl flex items-center justify-center transition-all duration-300 relative z-10 ${isListening
                        ? 'bg-danger text-white shadow-lg shadow-danger/20'
                        : 'bg-bg-page border border-border text-text-secondary hover:border-violet hover:text-violet'
                    } ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
                title={isListening ? 'Stop Listening' : 'Voice Input'}
            >
                {isListening ? (
                    <>
                        <MicOff size={20} />
                        <span className="absolute inset-[-4px] rounded-full border-2 border-danger animate-ping opacity-75" />
                    </>
                ) : (
                    <Mic size={20} />
                )}
            </button>
        </div>
    );
}
