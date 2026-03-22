import { useState, useCallback } from 'react';

export const useVoiceOutput = () => {
    const [isSpeaking, setIsSpeaking] = useState(false);
    const isSupported = 'speechSynthesis' in window;

    const speak = useCallback((text: string) => {
        if (!isSupported) return;

        // Cancel any current speech
        window.speechSynthesis.cancel();

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'en-IN';
        utterance.rate = 0.92; // slightly slower for clarity
        utterance.pitch = 1.0;
        utterance.volume = 1.0;

        utterance.onstart = () => setIsSpeaking(true);
        utterance.onend = () => setIsSpeaking(false);
        utterance.onerror = () => setIsSpeaking(false);

        window.speechSynthesis.speak(utterance);
    }, [isSupported]);

    const stop = useCallback(() => {
        if (!isSupported) return;
        window.speechSynthesis.cancel();
        setIsSpeaking(false);
    }, [isSupported]);

    return {
        speak,
        stop,
        isSpeaking,
        isSupported
    };
};
