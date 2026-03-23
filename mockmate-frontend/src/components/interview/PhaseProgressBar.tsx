import { Check } from 'lucide-react';

interface PhaseProgressBarProps {
    currentPhase: string;
}

const PHASES = [
    { id: 'RESUME_SCREEN', label: 'Resume' },
    { id: 'DSA', label: 'DSA' },
    { id: 'SYSTEM_DESIGN', label: 'System Design' },
    { id: 'HR', label: 'HR' },
];

export function PhaseProgressBar({ currentPhase }: PhaseProgressBarProps) {
    const currentIndex = PHASES.findIndex((p) => p.id === currentPhase);

    return (
        <div className="flex items-center w-full max-w-[500px] justify-between relative px-4">
            {/* Connector Line */}
            <div className="absolute top-3 left-8 right-8 h-[2px] bg-border z-0" />

            {/* Active/Completed Connector Line Overlay */}
            <div
                className="absolute top-3 left-8 h-[2px] bg-violet z-0 transition-all duration-500 ease-in-out"
                style={{
                    width: currentIndex > 0
                        ? `calc(${(currentIndex / (PHASES.length - 1)) * 100}% - 2rem)`
                        : '0%'
                }}
            />

            {PHASES.map((phase, i) => {
                const isCompleted = i < currentIndex;
                const isActive = i === currentIndex;
                const isFuture = i > currentIndex;

                return (
                    <div key={phase.id} className="relative z-10 flex flex-col items-center">
                        <div
                            className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-300
                                ${isCompleted ? 'bg-success text-white ring-2 ring-white' : ''}
                                ${isActive ? 'bg-violet text-white ring-4 ring-violet/20 shadow-lg' : ''}
                                ${isFuture ? 'bg-bg-page border-2 border-border text-text-tertiary' : ''}
                            `}
                        >
                            {isCompleted ? (
                                <Check size={14} strokeWidth={3} />
                            ) : isActive ? (
                                <div className="absolute inset-0 rounded-full border border-violet animate-ping opacity-20" />
                            ) : null}
                            {!isCompleted && <span>{i + 1}</span>}
                        </div>
                        <span
                            className={`mt-2 text-[10px] font-semibold tracking-wider uppercase whitespace-nowrap absolute top-full
                                ${isActive ? 'text-text-primary' : 'text-text-tertiary'}
                            `}
                        >
                            {phase.label}
                        </span>
                    </div>
                );
            })}
        </div>
    );
}