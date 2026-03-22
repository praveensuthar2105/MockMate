import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '../store/authStore';
import { useSessionStore } from '../store/sessionStore';
import type { WsEvent } from '../types';

export const useWebSocket = (sessionId: number | null) => {
    const { accessToken } = useAuthStore();
    const { addMessage, setTyping, updateSession, setTimeRemaining } = useSessionStore();
    const [events, setEvents] = useState<WsEvent[]>([]);
    const [isConnected, setIsConnected] = useState(false);
    const clientRef = useRef<Client | null>(null);

    const connect = useCallback(() => {
        if (!sessionId || !accessToken) return;

        const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

        const client = new Client({
            webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
            connectHeaders: {
                Authorization: `Bearer ${accessToken}`,
            },
            onConnect: () => {
                setIsConnected(true);
                client.subscribe(`/topic/session/${sessionId}`, (message) => {
                    const event = JSON.parse(message.body) as WsEvent;
                    setEvents((prev: WsEvent[]) => [...prev, event]);

                    if (event.type === 'MESSAGE' && event.content) {
                        addMessage({
                            id: Date.now(),
                            sender: event.role || 'AI',
                            content: event.content,
                            timestamp: event.timestamp || new Date().toISOString(),
                            type: 'TEXT',
                            phase: event.phase || undefined
                        });
                    } else if (event.type === 'TYPING') {
                        setTyping(true);
                        // Auto-hide after 3 seconds if no message arrives
                        setTimeout(() => setTyping(false), 3000);
                    } else if (event.type === 'PHASE_CHANGE') {
                        updateSession({ currentPhase: event.phase || undefined });
                    } else if (event.type === 'TIMER_UPDATE') {
                        setTimeRemaining(event.timeRemainingSeconds);
                    }
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
            onStompError: (frame) => {
                console.error('STOMP error', frame);
            },
        });

        client.activate();
        clientRef.current = client;
    }, [sessionId, accessToken]);

    const disconnect = useCallback(() => {
        if (clientRef.current) {
            clientRef.current.deactivate();
            clientRef.current = null;
            setIsConnected(false);
        }
    }, []);

    const sendMessage = useCallback((content: string) => {
        if (clientRef.current?.connected && sessionId) {
            clientRef.current.publish({
                destination: `/app/interview/${sessionId}/message`,
                body: JSON.stringify({ content }),
            });
        }
    }, [sessionId]);

    const sendPhaseComplete = useCallback(() => {
        if (clientRef.current?.connected && sessionId) {
            clientRef.current.publish({
                destination: `/app/interview/${sessionId}/phase-complete`,
                body: JSON.stringify({}),
            });
        }
    }, [sessionId]);

    useEffect(() => {
        if (sessionId) {
            connect();
        }
        return () => disconnect();
    }, [sessionId, connect, disconnect]);

    return {
        events,
        isConnected,
        sendMessage,
        sendPhaseComplete,
        clearEvents: () => setEvents([]),
    };
};
