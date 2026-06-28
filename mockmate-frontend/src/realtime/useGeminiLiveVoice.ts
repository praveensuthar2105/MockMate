import { useCallback, useEffect, useRef, useState } from 'react';
import {
    GoogleGenAI,
    Modality,
    type LiveServerMessage,
    type Session,
} from '@google/genai';
import { voiceSessionService } from '../services/voiceSessionService';
import { useSessionStore } from '../store/sessionStore';
import { useInterviewStore } from '../store/interviewStore';

export type VoiceConnectionState =
    | 'IDLE'
    | 'CONNECTING'
    | 'LISTENING'
    | 'SPEAKING'
    | 'ERROR';

const INPUT_SAMPLE_RATE = 16_000;
const OUTPUT_SAMPLE_RATE = 24_000;

function bytesToBase64(bytes: Uint8Array): string {
    const chars: string[] = [];
    const len = bytes.length;
    for (let i = 0; i < len; i++) {
        chars.push(String.fromCharCode(bytes[i]));
    }
    return btoa(chars.join(''));
}

function base64ToBytes(data: string): Uint8Array {
    const binary = atob(data);
    const bytes = new Uint8Array(binary.length);
    for (let index = 0; index < binary.length; index += 1) {
        bytes[index] = binary.charCodeAt(index);
    }
    return bytes;
}

function resampleToPcm16(
    input: Float32Array,
    sourceRate: number,
    targetRate: number,
): Uint8Array {
    const ratio = sourceRate / targetRate;
    const outputLength = Math.max(1, Math.round(input.length / ratio));
    const pcm = new Int16Array(outputLength);

    for (let outputIndex = 0; outputIndex < outputLength; outputIndex += 1) {
        const sourceIndex = Math.min(
            input.length - 1,
            Math.round(outputIndex * ratio),
        );
        const sample = Math.max(-1, Math.min(1, input[sourceIndex]));
        pcm[outputIndex] = sample < 0 ? sample * 0x8000 : sample * 0x7fff;
    }

    return new Uint8Array(pcm.buffer);
}

export function useGeminiLiveVoice(sessionId: number) {
    const addMessage = useSessionStore((state) => state.addMessage);
    const [state, setState] = useState<VoiceConnectionState>('IDLE');
    const [isMuted, setMuted] = useState(false);
    const [partialTranscript, setPartialTranscript] = useState('');
    const [error, setError] = useState<string | null>(null);

    const liveSessionRef = useRef<Session | null>(null);
    const mediaStreamRef = useRef<MediaStream | null>(null);
    const inputContextRef = useRef<AudioContext | null>(null);
    const inputProcessorRef = useRef<ScriptProcessorNode | null>(null);
    const inputSourceRef = useRef<MediaStreamAudioSourceNode | null>(null);
    const silentGainRef = useRef<GainNode | null>(null);
    const outputContextRef = useRef<AudioContext | null>(null);
    const scheduledSourcesRef = useRef<AudioBufferSourceNode[]>([]);
    const nextPlaybackTimeRef = useRef(0);
    const inputTranscriptRef = useRef('');
    const outputTranscriptRef = useRef('');
    const mutedRef = useRef(false);

    const stopPlayback = useCallback(() => {
        for (const source of scheduledSourcesRef.current) {
            try {
                source.stop();
            } catch {
                // Source may already have completed.
            }
        }
        scheduledSourcesRef.current = [];
        nextPlaybackTimeRef.current = 0;
    }, []);

    const persistTranscript = useCallback(
        async (role: 'USER' | 'AI', content: string) => {
            const normalized = content.trim();
            if (!normalized) return;

            try {
                const message = await voiceSessionService.saveTranscript(
                    sessionId,
                    role,
                    normalized,
                );
                addMessage(message);
            } catch (persistError) {
                console.error('Failed to persist voice transcript', persistError);
            }
        },
        [addMessage, sessionId],
    );

    const playPcmAudio = useCallback(async (base64Audio: string) => {
        const context =
            outputContextRef.current ??
            new AudioContext({ sampleRate: OUTPUT_SAMPLE_RATE });
        outputContextRef.current = context;

        if (context.state === 'suspended') {
            await context.resume();
        }

        const bytes = base64ToBytes(base64Audio);
        const sampleCount = Math.floor(bytes.byteLength / 2);
        const audioBuffer = context.createBuffer(
            1,
            sampleCount,
            OUTPUT_SAMPLE_RATE,
        );
        const channel = audioBuffer.getChannelData(0);
        const view = new DataView(
            bytes.buffer,
            bytes.byteOffset,
            bytes.byteLength,
        );

        for (let index = 0; index < sampleCount; index += 1) {
            channel[index] = view.getInt16(index * 2, true) / 32768;
        }

        const source = context.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(context.destination);

        const startAt = Math.max(
            context.currentTime,
            nextPlaybackTimeRef.current,
        );
        source.start(startAt);
        nextPlaybackTimeRef.current = startAt + audioBuffer.duration;
        scheduledSourcesRef.current.push(source);
        source.onended = () => {
            scheduledSourcesRef.current =
                scheduledSourcesRef.current.filter((item) => item !== source);
        };
    }, []);

    const handleMessage = useCallback(
        (message: LiveServerMessage) => {
            // Handle toolCall requests from Gemini Live first
            if (message.toolCall) {
                const liveSession = liveSessionRef.current;
                if (liveSession) {
                    for (const fc of message.toolCall.functionCalls || []) {
                        if (fc.name === 'readCandidateCode') {
                            const currentCode = useInterviewStore.getState().code || '';
                            const currentLanguage = useInterviewStore.getState().language || '';
                            const result = `Language: ${currentLanguage}\nCode:\n${currentCode}`;

                            void liveSession.send({
                                toolResponse: {
                                    functionResponses: [
                                        {
                                            name: fc.name,
                                            id: fc.id,
                                            response: { output: result },
                                        },
                                    ],
                                },
                            });
                        }
                    }
                }
                return;
            }

            const serverContent = message.serverContent;
            if (!serverContent) return;

            if (serverContent.interrupted) {
                stopPlayback();
                outputTranscriptRef.current = '';
                setState('LISTENING');
            }

            const interimText = serverContent.interimInputTranscription?.text;
            if (interimText) {
                setPartialTranscript(interimText);
            }

            const inputTranscription = serverContent.inputTranscription;
            if (inputTranscription?.text) {
                inputTranscriptRef.current += inputTranscription.text;
                setPartialTranscript(inputTranscriptRef.current);
            }
            if (inputTranscription?.finished) {
                void persistTranscript('USER', inputTranscriptRef.current);
                inputTranscriptRef.current = '';
                setPartialTranscript('');
            }

            const outputTranscription = serverContent.outputTranscription;
            if (outputTranscription?.text) {
                // Robust Fallback: If AI is starting to speak/output text, User has finished speaking
                if (inputTranscriptRef.current.trim()) {
                    void persistTranscript('USER', inputTranscriptRef.current);
                    inputTranscriptRef.current = '';
                    setPartialTranscript('');
                }
                outputTranscriptRef.current += outputTranscription.text;
            }
            if (outputTranscription?.finished) {
                void persistTranscript('AI', outputTranscriptRef.current);
                outputTranscriptRef.current = '';
            }

            if (serverContent.modelTurn) {
                // Robust Fallback: If model turn is starting, User has finished speaking
                if (inputTranscriptRef.current.trim()) {
                    void persistTranscript('USER', inputTranscriptRef.current);
                    inputTranscriptRef.current = '';
                    setPartialTranscript('');
                }
            }

            for (const part of serverContent.modelTurn?.parts ?? []) {
                const inlineData = part.inlineData;
                if (
                    inlineData?.data &&
                    inlineData.mimeType?.startsWith('audio/')
                ) {
                    setState('SPEAKING');
                    void playPcmAudio(inlineData.data);
                }
            }

            if (serverContent.turnComplete) {
                // Robust Fallback: When turn is complete, persist the accumulated AI output transcription
                if (outputTranscriptRef.current.trim()) {
                    void persistTranscript('AI', outputTranscriptRef.current);
                    outputTranscriptRef.current = '';
                }
                setState('LISTENING');
            }
        },
        [persistTranscript, playPcmAudio, stopPlayback],
    );

    const stopMicrophone = useCallback(() => {
        inputProcessorRef.current?.disconnect();
        inputSourceRef.current?.disconnect();
        silentGainRef.current?.disconnect();
        mediaStreamRef.current?.getTracks().forEach((track) => track.stop());
        void inputContextRef.current?.close();

        inputProcessorRef.current = null;
        inputSourceRef.current = null;
        silentGainRef.current = null;
        mediaStreamRef.current = null;
        inputContextRef.current = null;
    }, []);

    const startMicrophone = useCallback(async () => {
        const mediaStream = await navigator.mediaDevices.getUserMedia({
            audio: {
                echoCancellation: true,
                noiseSuppression: true,
                autoGainControl: true,
                channelCount: 1,
            },
        });
        mediaStreamRef.current = mediaStream;

        const context = new AudioContext();
        if (context.state === 'suspended') {
            await context.resume();
        }
        const source = context.createMediaStreamSource(mediaStream);
        const processor = context.createScriptProcessor(4096, 1, 1);
        const silentGain = context.createGain();
        silentGain.gain.value = 0;

        processor.onaudioprocess = (event) => {
            const liveSession = liveSessionRef.current;
            if (!liveSession || mutedRef.current) return;

            const input = event.inputBuffer.getChannelData(0);
            const pcmBytes = resampleToPcm16(
                input,
                event.inputBuffer.sampleRate,
                INPUT_SAMPLE_RATE,
            );

            liveSession.sendRealtimeInput({
                audio: {
                    data: bytesToBase64(pcmBytes),
                    mimeType: `audio/pcm;rate=${INPUT_SAMPLE_RATE}`,
                },
            });
        };

        source.connect(processor);
        processor.connect(silentGain);
        silentGain.connect(context.destination);

        inputContextRef.current = context;
        inputSourceRef.current = source;
        inputProcessorRef.current = processor;
        silentGainRef.current = silentGain;
    }, []);

    const disconnect = useCallback(() => {
        stopMicrophone();
        stopPlayback();
        liveSessionRef.current?.close();
        liveSessionRef.current = null;
        void outputContextRef.current?.close();
        outputContextRef.current = null;
        inputTranscriptRef.current = '';
        outputTranscriptRef.current = '';
        setPartialTranscript('');
        setMuted(false);
        mutedRef.current = false;
        setState('IDLE');
    }, [stopMicrophone, stopPlayback]);

    const connect = useCallback(async () => {
        if (state !== 'IDLE' && state !== 'ERROR') return;

        setState('CONNECTING');
        setError(null);

        try {
            const token = await voiceSessionService.createToken(sessionId);
            const ai = new GoogleGenAI({
                apiKey: token.token,
                httpOptions: { apiVersion: 'v1alpha' },
            });

            const session = await ai.live.connect({
                model: token.model,
                config: {
                    responseModalities: [Modality.AUDIO],
                    inputAudioTranscription: {},
                    outputAudioTranscription: {},
                    sessionResumption: {},
                    tools: [
                        {
                            functionDeclarations: [
                                {
                                    name: 'readCandidateCode',
                                    description: "Retrieve the candidate's current programming code from the technical editor. Call this whenever the candidate mentions their code, refers to their implementation, or when you want to review and discuss their coding approach during the DSA coding round.",
                                    parameters: {
                                        type: 'OBJECT',
                                        properties: {},
                                    },
                                }
                            ]
                        }
                    ]
                },
                callbacks: {
                    onopen: () => setState('LISTENING'),
                    onmessage: handleMessage,
                    onerror: () => {
                        setError('The live voice connection encountered an error.');
                        setState('ERROR');
                    },
                    onclose: () => {
                        stopMicrophone();
                        setState('IDLE');
                    },
                },
            });

            liveSessionRef.current = session;
            await startMicrophone();
        } catch (connectError) {
            console.error('Failed to start Gemini Live voice', connectError);
            disconnect();
            setError(
                connectError instanceof Error
                    ? connectError.message
                    : 'Unable to start live voice.',
            );
            setState('ERROR');
        }
    }, [
        disconnect,
        handleMessage,
        sessionId,
        startMicrophone,
        state,
        stopMicrophone,
    ]);

    const toggleMute = useCallback(() => {
        setMuted((current) => {
            mutedRef.current = !current;
            return !current;
        });
    }, []);

    const currentPhase = useSessionStore((state) => state.currentSession?.currentPhase);
    const prevPhaseRef = useRef<string | null>(null);

    useEffect(() => {
        if (
            prevPhaseRef.current &&
            currentPhase &&
            prevPhaseRef.current !== currentPhase &&
            state !== 'IDLE' &&
            state !== 'ERROR'
        ) {
            console.log(
                `Phase changed from ${prevPhaseRef.current} to ${currentPhase}. Reconnecting voice to update system instructions.`,
            );
            void (async () => {
                disconnect();
                // Wait briefly before reconnecting to ensure clean teardown
                await new Promise((resolve) => setTimeout(resolve, 600));
                await connect();
            })();
        }
        prevPhaseRef.current = currentPhase || null;
    }, [currentPhase, state, connect, disconnect]);

    useEffect(() => disconnect, [disconnect]);

    return {
        voiceState: state,
        isVoiceActive: state !== 'IDLE' && state !== 'ERROR',
        isMuted,
        partialTranscript,
        voiceError: error,
        connectVoice: connect,
        disconnectVoice: disconnect,
        toggleMute,
    };
}
