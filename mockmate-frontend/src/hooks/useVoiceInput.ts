import { useState, useCallback, useRef, useEffect } from 'react';

const SpeechRecognition = typeof window !== 'undefined'
    ? ((window as any).SpeechRecognition || (window as any).webkitSpeechRecognition)
    : null;

const isSupported = !!SpeechRecognition;

export const useVoiceInput = () => {
    const [isListening, setIsListening] = useState(false);
    const [transcript, setTranscript] = useState('');
    const [finalTranscript, setFinalTranscript] = useState('');
    const recognitionRef = useRef<any>(null);

    useEffect(() => {
        if (!isSupported || !SpeechRecognition) return;

        const recognition = new SpeechRecognition();
        recognition.lang = 'en-IN';
        recognition.interimResults = true;
        recognition.continuous = true;
        recognition.maxAlternatives = 1;

        recognition.onstart = () => {
            setIsListening(true);
        };

        recognition.onresult = (event: any) => {
            let interim = '';
            let final = '';
            for (let i = 0; i < event.results.length; ++i) {
                const result = event.results[i];
                if (result.isFinal) {
                    final += result[0].transcript + ' ';
                } else {
                    interim += result[0].transcript;
                }
            }
            setFinalTranscript(final.trim());
            setTranscript(interim.trim());
        };

        recognition.onerror = (event: any) => {
            console.error('Speech recognition error:', event.error);
            setIsListening(false);
        };

        recognition.onend = () => {
            setIsListening(false);
        };

        recognitionRef.current = recognition;

        return () => {
            if (recognitionRef.current) {
                try {
                    recognitionRef.current.abort();
                } catch (e) {
                    // Ignore errors on abort
                }
            }
        };
    }, []);

    const startListening = useCallback(() => {
        if (recognitionRef.current) {
            setTranscript('');
            setFinalTranscript('');
            try {
                recognitionRef.current.start();
            } catch (err) {
                console.error('STT Start Error:', err);
            }
        }
    }, []);

    const stopListening = useCallback(() => {
        if (recognitionRef.current) {
            try {
                recognitionRef.current.stop();
            } catch (err) {
                console.error('STT Stop Error:', err);
            }
        }
    }, []);

    const resetTranscript = useCallback(() => {
        setTranscript('');
        setFinalTranscript('');
    }, []);

    return {
        isListening,
        transcript,
        finalTranscript,
        startListening,
        stopListening,
        isSupported,
        resetTranscript
    };
};
