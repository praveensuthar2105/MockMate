import type { PhaseType } from '../../types';
import { motion } from 'framer-motion';

interface PhaseScoreBarProps {
    phase: PhaseType;
    score: number;
    delay?: number;
}

const PHASE_NAMES: Record<PhaseType, string> = {
    RESUME_SCREEN: 'Resume Screen',
    DSA: 'DSA Coding',
    SYSTEM_DESIGN: 'System Design',
    HR: 'HR Round'
};

const PHASE_COLORS: Record<PhaseType, string> = {
    RESUME_SCREEN: '#6C63FF',
    DSA: '#059669',
    SYSTEM_DESIGN: '#D97706',
    HR: '#DB2777'
};

export function PhaseScoreBar({ phase, score, delay = 0 }: PhaseScoreBarProps) {
    const color = PHASE_COLORS[phase];
    const name = PHASE_NAMES[phase];

    return (
        <div className="space-y-2">
            <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                    <div className="w-2 h-2 rounded-full" style={{ backgroundColor: color }} />
                    <span className="text-[14px] font-medium text-text-secondary">{name}</span>
                </div>
                <span className="font-display text-[16px] font-bold" style={{ color }}>
                    {score}
                </span>
            </div>

            <div className="h-2 w-full bg-bg-subtle rounded-full overflow-hidden">
                <motion.div
                    initial={{ width: 0 }}
                    animate={{ width: `${score}%` }}
                    transition={{ duration: 1, delay, ease: "easeOut" }}
                    className="h-full rounded-full"
                    style={{ backgroundColor: color }}
                />
            </div>
        </div>
    );
}
