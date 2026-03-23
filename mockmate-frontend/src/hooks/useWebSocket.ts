import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useSessionStore } from '../store/sessionStore';

export const useWebSocket = (sessionId: number | null) => {
    const { accessToken } = useAuthStore();
    const { addMessage, setTyping, updatePhase, setTimeRemaining } = useSessionStore();
    const [isConnected, setIsConnected] = useState(false);
    const clientRef = useRef<Client | null>(null);
    const navigate = useNavigate();

    const connect = useCallback(() => {
        if (!sessionId || !accessToken) return;

        const baseUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws/websocket';

        const client = new Client({
            brokerURL: baseUrl,
            connectHeaders: {
                Authorization: `Bearer ${accessToken}`,
            },
            reconnectDelay: 3000,
            onConnect: () => {
                setIsConnected(true);
                client.subscribe(`/topic/session/${sessionId}`, (message) => {
                    const event = JSON.parse(message.body);

                    if (event.type === 'MESSAGE') {
                        setTyping(false);
                        if (event.content) {
                            addMessage({
                                id: Date.now(),
                                role: event.role || 'AI',
                                content: event.content,
                                timestamp: event.timestamp || new Date().toISOString(),
                                phase: event.phase || undefined
                            });
                        }
                    } else if (event.type === 'TYPING') {
                        setTyping(true);
                    } else if (event.type === 'PHASE_CHANGE') {
                        if (event.phase === null) {
                            navigate(`/interview/${sessionId}/report`);
                        } else {
                            updatePhase(event.phase);
                        }
                    } else if (event.type === 'TIMER_UPDATE') {
                        setTimeRemaining(event.timeRemainingSeconds);
                    } else if (event.type === 'ERROR') {
                        console.error('WebSocket Error Event:', event.content);
                    }
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        client.activate();
        clientRef.current = client;
    }, [sessionId, accessToken, navigate, addMessage, setTyping, updatePhase, setTimeRemaining]);

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

    useEffect(() => {
        if (sessionId) {
            connect();
        }
        return () => disconnect();
    }, [sessionId, connect, disconnect]);

    return {
        isConnected,
        sendMessage
    };
};