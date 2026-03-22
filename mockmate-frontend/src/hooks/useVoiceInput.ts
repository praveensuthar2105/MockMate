import { useState, useCallback, useRef, useEffect } from 'react';

export const useVoiceInput = () => {
    const [isListening, setIsListening] = useState(false);
    const [transcript, setTranscript] = useState('');
    const [finalTranscript, setFinalTranscript] = useState('');
    const recognitionRef = useRef<any>(null);

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    const isSupported = !!SpeechRecognition;

    useEffect(() => {
        if (!isSupported) return;

        const recognition = new SpeechRecognition();
        recognition.lang = 'en-IN';
        recognition.interimResults = true;
        recognition.continuous = false;
        recognition.maxAlternatives = 1;

        recognition.onstart = () => {
            setIsListening(true);
        };

        recognition.onresult = (event: any) => {
            let interim = '';
            for (let i = event.resultIndex; i < event.results.length; ++i) {
                if (event.results[i].isFinal) {
                    setFinalTranscript(event.results[i][0].transcript);
                } else {
                    interim += event.results[i][0].transcript;
                }
            }
            setTranscript(interim);
        };

        recognition.onerror = (event: any) => {
            console.error('Speech recognition error:', event.error);
            setIsListening(false);
        };

        recognition.onend = () => {
            setIsListening(false);
        };

        recognitionRef.current = recognition;
    }, [isSupported, SpeechRecognition]);

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
            recognitionRef.current.stop();
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
