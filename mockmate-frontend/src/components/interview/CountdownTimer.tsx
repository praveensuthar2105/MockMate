import { useEffect, useRef } from 'react';
import { Timer, AlertTriangle } from 'lucide-react';
import { useSessionStore } from '../../store/sessionStore';

export function CountdownTimer() {
    const { timeRemaining: seconds } = useSessionStore();

        const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

    useEffect(() => {
        if (seconds === null || seconds <= 0) {
            if (intervalRef.current) clearInterval(intervalRef.current);
            return;
        }

        if (intervalRef.current) clearInterval(intervalRef.current);
        intervalRef.current = setInterval(() => {
            const currentSeconds = useSessionStore.getState().timeRemaining;
            if (currentSeconds !== null && currentSeconds > 0) {
                useSessionStore.getState().setTimeRemaining(currentSeconds - 1);
            } else {
                if (intervalRef.current) clearInterval(intervalRef.current);
            }
        }, 1000);

        return () => {
            if (intervalRef.current) clearInterval(intervalRef.current);
        };
    }, [seconds]);

    const formatTime = (s: number | null) => {
        if (s === null) return '--:--';
        const m = Math.floor(s / 60);
        const remS = s % 60;
        return `${m.toString().padStart(2, '0')}:${remS.toString().padStart(2, '0')}`;
    };

    const getTimerColor = (s: number | null) => {
        if (s === null) return 'text-text-secondary border-border bg-bg-subtle';
        if (s < 60) return 'text-danger border-danger bg-danger-light animate-pulse';
        if (s <= 300) return 'text-warning border-warning bg-warning-light';
        return 'text-text-secondary border-border bg-bg-subtle';
    };

    return (
        <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full border transition-colors ${getTimerColor(seconds)}`}>
            {seconds !== null && seconds < 60 ? <AlertTriangle size={16} /> : <Timer size={16} />}
            <span className="font-mono text-sm font-semibold tracking-wide">
                {formatTime(seconds)}
            </span>
        </div>
    );
}
