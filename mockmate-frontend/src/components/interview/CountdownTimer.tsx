import { useEffect, useState } from 'react';
import { Timer, AlertTriangle } from 'lucide-react';
import { useSessionStore } from '../../store/sessionStore';

export function CountdownTimer() {
    const { timeRemaining: seconds, setTimeRemaining } = useSessionStore();
    const [isWarning, setIsWarning] = useState(false);

    useEffect(() => {
        if (seconds === null || seconds <= 0) return;

        const interval = setInterval(() => {
            setTimeRemaining(seconds - 1);
        }, 1000);

        return () => clearInterval(interval);
    }, [seconds, setTimeRemaining]);

    useEffect(() => {
        setIsWarning(seconds !== null && seconds < 300); // 5 minutes remaining
    }, [seconds]);

    const formatTime = (s: number | null) => {
        if (s === null) return '--:--';
        const m = Math.floor(s / 60);
        const remS = s % 60;
        return `${m.toString().padStart(2, '0')}:${remS.toString().padStart(2, '0')}`;
    };

    return (
        <div className={`flex items-center space-x-2 px-3 py-1.5 rounded-full border transition-colors ${isWarning
            ? 'bg-danger-light border-danger text-danger'
            : 'bg-bg-subtle border-border text-text-secondary'
            }`}>
            {isWarning ? <AlertTriangle size={16} /> : <Timer size={16} />}
            <span className="font-mono text-sm font-semibold tracking-wide">
                {formatTime(seconds)}
            </span>
        </div>
    );
}
