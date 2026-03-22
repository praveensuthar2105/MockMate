import { useEffect } from 'react';
import { Mic, MicOff } from 'lucide-react';
import { useVoiceInput } from '../../hooks/useVoiceInput';

interface VoiceMicButtonProps {
    onTranscriptComplete: (text: string) => void;
    disabled?: boolean;
}

export function VoiceMicButton({ onTranscriptComplete, disabled = false }: VoiceMicButtonProps) {
    const {
        isListening,
        transcript,
        finalTranscript,
        startListening,
        stopListening,
        isSupported,
        resetTranscript
    } = useVoiceInput();

    useEffect(() => {
        if (finalTranscript) {
            onTranscriptComplete(finalTranscript);
            resetTranscript();
        }
    }, [finalTranscript, onTranscriptComplete, resetTranscript]);

    if (!isSupported) {
        return (
            <button
                disabled
                className="p-2 rounded-xl bg-bg-subtle text-text-tertiary cursor-not-allowed opacity-50"
                title="Voice input requires Chrome or Edge"
            >
                <Mic size={20} />
            </button>
        );
    }

    const handleClick = () => {
        if (isListening) {
            stopListening();
        } else {
            startListening();
        }
    };

    return (
        <div className="relative">
            <button
                type="button"
                onClick={handleClick}
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

            {/* Interim Transcript Overlay */}
            {isListening && transcript && (
                <div className="absolute bottom-full mb-4 right-0 w-[300px] animate-in slide-in-from-bottom-2 duration-300">
                    <div className="bg-bg-surface/90 backdrop-blur-md border border-border p-3 rounded-xl shadow-xl">
                        <p className="text-[13px] text-text-tertiary italic leading-relaxed">
                            {transcript}...
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
}
