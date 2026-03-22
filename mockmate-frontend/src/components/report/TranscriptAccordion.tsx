import { useState } from 'react';
import { ChevronDown } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { MessageBubble } from '../interview/MessageBubble';
import { useSessionStore } from '../../store/sessionStore';
import type { PhaseType } from '../../types';

interface TranscriptAccordionProps {
    sessionId: number;
    phases: PhaseType[];
}

export function TranscriptAccordion({ phases }: TranscriptAccordionProps) {
    const [openPhase, setOpenPhase] = useState<PhaseType | null>(null);
    const { messages } = useSessionStore();

    const togglePhase = (phase: PhaseType) => {
        setOpenPhase(openPhase === phase ? null : phase);
    };

    const getPhaseMessages = (phase: PhaseType) => {
        return messages.filter(m => m.phase === phase);
    };

    return (
        <div className="space-y-4">
            {phases.map((phase) => {
                const phaseMessages = getPhaseMessages(phase);
                const isOpen = openPhase === phase;

                return (
                    <div key={phase} className="border border-border rounded-xl overflow-hidden bg-bg-surface shadow-sm">
                        <button
                            onClick={() => togglePhase(phase)}
                            className="w-full px-5 py-4 flex items-center justify-between hover:bg-bg-page transition-colors"
                        >
                            <div className="flex items-center space-x-4">
                                <span className="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 bg-bg-subtle border border-border rounded-full text-text-tertiary">
                                    {phase.replace('_', ' ')}
                                </span>
                                <h3 className="text-[15px] font-semibold text-text-primary capitalize">
                                    {phase.toLowerCase().replace('_', ' ')} Interview
                                </h3>
                            </div>
                            <div className="flex items-center space-x-3">
                                <motion.div
                                    animate={{ rotate: isOpen ? 180 : 0 }}
                                    transition={{ duration: 0.3 }}
                                >
                                    <ChevronDown size={20} className="text-text-tertiary" />
                                </motion.div>
                            </div>
                        </button>

                        <AnimatePresence>
                            {isOpen && (
                                <motion.div
                                    initial={{ height: 0, opacity: 0 }}
                                    animate={{ height: 'auto', opacity: 1 }}
                                    exit={{ height: 0, opacity: 0 }}
                                    transition={{ duration: 0.3 }}
                                >
                                    <div className="px-5 pb-5 pt-2 border-t border-border bg-bg-page/20 space-y-4 max-h-[500px] overflow-y-auto custom-scrollbar">
                                        {phaseMessages.length === 0 ? (
                                            <p className="text-center text-text-tertiary text-sm py-4 italic">
                                                No messages recorded for this phase.
                                            </p>
                                        ) : (
                                            phaseMessages.map((msg) => (
                                                <MessageBubble
                                                    key={msg.id}
                                                    content={msg.content}
                                                    sender={msg.sender}
                                                    timestamp={msg.timestamp}
                                                    type={msg.type}
                                                />
                                            ))
                                        )}
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                );
            })}
        </div>
    );
}
