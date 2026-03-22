import type { PhaseType } from '../../types';

interface PhaseProgressBarProps {
    currentPhase: PhaseType;
}

const PHASES = [
    { id: 'RESUME_SCREEN', label: 'Resume', color: 'var(--phase-resume)' },
    { id: 'DSA', label: 'Algorithms', color: 'var(--phase-dsa)' },
    { id: 'SYSTEM_DESIGN', label: 'Sys Design', color: 'var(--phase-system-design)' },
    { id: 'HR', label: 'HR', color: 'var(--phase-hr)' },
];

export function PhaseProgressBar({ currentPhase }: PhaseProgressBarProps) {
    const currentIndex = PHASES.findIndex((p) => p.id === currentPhase);

    return (
        <div className="flex flex-col space-y-2 w-full max-w-md">
            <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-text-secondary uppercase tracking-wider">
                    Current Phase
                </span>
                <span
                    className="text-xs font-bold px-2 py-0.5 rounded-md"
                    style={{
                        color: PHASES[currentIndex]?.color,
                        backgroundColor: `${PHASES[currentIndex]?.color}20`
                    }}
                >
                    {PHASES[currentIndex]?.label}
                </span>
            </div>

            <div className="flex items-center w-full h-2 bg-bg-overlay rounded-full overflow-hidden shadow-inner">
                {PHASES.map((phase, i) => {
                    const isCompleted = i < currentIndex;
                    const isActive = i === currentIndex;

                    return (
                        <div
                            key={phase.id}
                            className={`h-full transition-all duration-500 ease-in-out border-r border-bg-page last:border-0`}
                            style={{
                                width: '25%',
                                backgroundColor: isCompleted
                                    ? 'var(--success)'
                                    : isActive
                                        ? phase.color
                                        : 'transparent',
                                opacity: isCompleted ? 0.5 : 1
                            }}
                        />
                    );
                })}
            </div>
        </div>
    );
}
