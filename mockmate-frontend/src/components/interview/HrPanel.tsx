import { HeartHandshake, Video, Mic, ShieldAlert, MonitorCheck } from 'lucide-react';
import { motion } from 'framer-motion';

export function HrPanel() {
    return (
        <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex flex-col overflow-hidden">
            <div className="p-6 border-b border-border bg-bg-subtle/30">
                <h2 className="font-display font-semibold text-xl text-text-primary flex items-center gap-2">
                    <HeartHandshake className="text-phase-hr" size={24} />
                    Behavioral Assessment
                </h2>
                <p className="text-text-secondary text-xs mt-1">
                    Focused on cultural alignment, personality markers, and soft skills.
                </p>
            </div>

            <div className="flex-1 p-10 flex flex-col items-center justify-center bg-bg-page/50 text-center">
                {/* Simulated Camera Feed Container */}
                <div className="relative w-full max-w-md aspect-video bg-black rounded-3xl overflow-hidden shadow-2xl border-4 border-white/10 group">
                    {/* Placeholder for camera */}
                    <div className="absolute inset-0 flex flex-col items-center justify-center bg-gradient-to-tr from-[#1a1c2c] to-[#4a192c]">
                        <Video size={48} className="text-white/20 mb-4" />
                        <span className="text-white/40 text-sm font-medium">Camera Feed Standby</span>
                    </div>

                    {/* Camera Active Indicator */}
                    <div className="absolute top-4 left-4 flex items-center gap-2 px-3 py-1 bg-black/40 backdrop-blur-md rounded-full border border-white/10">
                        <div className="w-2 h-2 rounded-full bg-danger animate-pulse" />
                        <span className="text-[10px] font-bold text-white uppercase tracking-widest">Live Analysis</span>
                    </div>

                    {/* Audio Visualizer (Simulated) */}
                    <div className="absolute bottom-6 left-1/2 -translate-x-1/2 flex items-end gap-1 h-8">
                        {[1, 2, 3, 4, 3, 2, 1, 3, 5, 2].map((h, i) => (
                            <motion.div
                                key={i}
                                initial={{ height: 4 }}
                                animate={{ height: [4, h * 6, 4] }}
                                transition={{ duration: 0.8, repeat: Infinity, delay: i * 0.1 }}
                                className="w-1 bg-violet rounded-full opacity-60"
                            />
                        ))}
                    </div>

                    {/* Overlays */}
                    <div className="absolute inset-0 border-2 border-white/5 pointer-events-none rounded-3xl" />
                </div>

                <div className="mt-12 grid grid-cols-2 gap-6 w-full max-w-md">
                    <div className="bg-white border border-border rounded-2xl p-4 shadow-sm hover:border-phase-hr/30 transition-colors">
                        <MonitorCheck size={20} className="text-phase-hr mb-2" />
                        <h4 className="text-[11px] font-bold text-text-tertiary uppercase mb-1">AI Eye Tracking</h4>
                        <p className="text-[10px] text-text-secondary">Confidence marker active</p>
                    </div>
                    <div className="bg-white border border-border rounded-2xl p-4 shadow-sm hover:border-phase-hr/30 transition-colors">
                        <ShieldAlert size={20} className="text-phase-hr mb-2" />
                        <h4 className="text-[11px] font-bold text-text-tertiary uppercase mb-1">Tone Analysis</h4>
                        <p className="text-[10px] text-text-secondary">Sentiment detection live</p>
                    </div>
                </div>

                <div className="mt-12">
                    <p className="text-text-secondary text-sm font-medium">
                        "Your verbal cues and sentiment analysis are being processed natively."
                    </p>
                    <div className="flex items-center justify-center gap-3 mt-4">
                        <div className="w-8 h-8 rounded-full bg-bg-subtle border border-border flex items-center justify-center">
                            <Mic size={14} className="text-text-tertiary" />
                        </div>
                        <span className="text-xs text-text-tertiary">Natural conversation mode enabled</span>
                    </div>
                </div>
            </div>
        </div>
    );
}
