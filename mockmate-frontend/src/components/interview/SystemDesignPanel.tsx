import { PenTool, Maximize2, MousePointer2, GitBranch } from 'lucide-react';
import { motion } from 'framer-motion';

export function SystemDesignPanel() {
    return (
        <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex flex-col overflow-hidden">
            <div className="p-6 border-b border-border bg-bg-subtle/30 flex items-center justify-between">
                <div>
                    <h2 className="font-display font-semibold text-xl text-text-primary flex items-center gap-2">
                        <PenTool className="text-violet" size={24} />
                        Architecture Design
                    </h2>
                    <p className="text-text-secondary text-xs mt-1">
                        Map out scalable systems and discuss architectural trade-offs.
                    </p>
                </div>
                <div className="flex gap-2">
                    <button className="p-2 bg-white border border-border rounded-lg text-text-tertiary hover:text-violet transition-colors">
                        <Maximize2 size={18} />
                    </button>
                </div>
            </div>

            <div className="flex-1 bg-bg-page relative p-8 flex flex-col items-center justify-center overflow-hidden">
                {/* SVG Architecture Diagram Backdrop */}
                <svg className="absolute inset-0 w-full h-full opacity-[0.03] pointer-events-none" viewBox="0 0 100 100">
                    <pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse">
                        <path d="M 10 0 L 0 0 0 10" fill="none" stroke="currentColor" strokeWidth="0.5" />
                    </pattern>
                    <rect width="100%" height="100%" fill="url(#grid)" />
                </svg>

                {/* Simulated Diagram Components */}
                <div className="relative z-10 w-full max-w-2xl">
                    <div className="grid grid-cols-3 gap-12">
                        {/* Users */}
                        <motion.div
                            whileHover={{ scale: 1.05 }}
                            className="bg-white border border-border rounded-xl p-4 shadow-sm text-center"
                        >
                            <MousePointer2 className="mx-auto text-text-tertiary mb-2" size={20} />
                            <span className="text-[12px] font-bold text-text-primary uppercase tracking-tighter">Client Apps</span>
                        </motion.div>

                        {/* Load Balancer */}
                        <motion.div
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            className="bg-violet/10 border-2 border-dashed border-violet/30 rounded-xl p-4 shadow-sm text-center flex flex-col items-center justify-center"
                        >
                            <span className="text-[10px] font-bold text-violet uppercase mb-1">Load Balancer</span>
                            <div className="flex gap-1">
                                <div className="w-1.5 h-1.5 rounded-full bg-violet animate-pulse" />
                                <div className="w-1.5 h-1.5 rounded-full bg-violet animate-pulse delay-75" />
                            </div>
                        </motion.div>

                        {/* Services */}
                        <motion.div
                            whileHover={{ scale: 1.05 }}
                            className="bg-white border border-border rounded-xl p-4 shadow-sm text-center"
                        >
                            <GitBranch className="mx-auto text-text-tertiary mb-2" size={20} />
                            <span className="text-[12px] font-bold text-text-primary uppercase tracking-tighter">Microservices</span>
                        </motion.div>
                    </div>

                    {/* Connecting Lines (Simulated) */}
                    <div className="absolute top-1/2 left-0 right-0 h-px bg-gradient-to-r from-transparent via-border to-transparent -z-10" />
                </div>

                <div className="mt-20 text-center">
                    <div className="inline-flex items-center gap-2 px-4 py-2 bg-white border border-border rounded-full shadow-sm">
                        <span className="w-2 h-2 rounded-full bg-success animate-ping" />
                        <span className="text-sm font-semibold text-text-primary">Excalidraw Integration Ready</span>
                    </div>
                    <p className="text-text-tertiary text-xs mt-3 max-w-[300px] mx-auto">
                        In this phase, you are expected to use the chat to define components. Visual canvas mounts automatically.
                    </p>
                </div>

                {/* Component Library Sidebar Overlay */}
                <div className="absolute right-6 top-6 bottom-6 w-16 bg-white/80 backdrop-blur border border-border rounded-2xl p-2 flex flex-col gap-4 shadow-lg items-center py-6">
                    {/* Icons for components */}
                    {[1, 2, 3, 4].map(i => (
                        <div key={i} className="w-10 h-10 rounded-lg bg-bg-subtle border border-border hover:bg-violet-light/20 cursor-pointer transition-colors" />
                    ))}
                </div>
            </div>
        </div>
    );
}
