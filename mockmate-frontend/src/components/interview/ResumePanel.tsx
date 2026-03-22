import { FileText, Search, ShieldCheck, Zap } from 'lucide-react';
import { motion } from 'framer-motion';

export function ResumePanel() {
    const analysisSteps = [
        "Analyzing project complexity...",
        "Extracting tech stack (React, Node.js)...",
        "Identifying leadership markers...",
        "Cross-referencing with JD requirements..."
    ];

    return (
        <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex flex-col overflow-hidden">
            <div className="p-6 border-b border-border bg-bg-subtle/30">
                <h2 className="font-display font-semibold text-xl text-text-primary flex items-center gap-2">
                    <FileText className="text-violet" size={24} />
                    Resume Insights
                </h2>
                <p className="text-text-secondary text-xs mt-1">
                    AI-driven analysis of your professional background and project impact.
                </p>
            </div>

            <div className="flex-1 p-8 flex flex-col lg:flex-row gap-8 overflow-y-auto custom-scrollbar">
                {/* Visual Resume Representation */}
                <div className="flex-1 bg-white border border-border rounded-xl shadow-inner p-8 relative group">
                    <div className="absolute top-4 right-4 text-[10px] font-bold text-success bg-success-light px-2 py-0.5 rounded-full flex items-center gap-1">
                        <ShieldCheck size={10} /> Verified
                    </div>

                    <div className="w-24 h-4 bg-bg-subtle rounded-md mb-4" />
                    <div className="w-full h-2 bg-bg-subtle rounded-sm mb-2" />
                    <div className="w-4/5 h-2 bg-bg-subtle rounded-sm mb-8" />

                    <div className="space-y-6">
                        <div className="space-y-2">
                            <div className="w-16 h-3 bg-violet/10 rounded-md" />
                            <div className="w-full h-20 bg-bg-subtle/50 rounded-lg border border-border border-dashed" />
                        </div>
                        <div className="space-y-2">
                            <div className="w-20 h-3 bg-violet/10 rounded-md" />
                            <div className="w-full h-32 bg-bg-subtle/50 rounded-lg border border-border border-dashed" />
                        </div>
                    </div>

                    {/* Scanning Animation */}
                    <motion.div
                        initial={{ top: 0 }}
                        animate={{ top: '100%' }}
                        transition={{ duration: 4, repeat: Infinity, ease: "linear" }}
                        className="absolute left-0 right-0 h-0.5 bg-gradient-to-r from-transparent via-violet to-transparent opacity-50 z-10"
                    />
                </div>

                {/* Analysis Sidebar */}
                <div className="w-full lg:w-64 space-y-6">
                    <div className="bg-bg-subtle rounded-xl p-5 border border-border">
                        <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-widest mb-4 flex items-center gap-2">
                            <Search size={12} /> Live Scan
                        </h3>
                        <div className="space-y-4">
                            {analysisSteps.map((step, idx) => (
                                <motion.div
                                    key={idx}
                                    initial={{ opacity: 0, x: -10 }}
                                    animate={{ opacity: 1, x: 0 }}
                                    transition={{ delay: idx * 1.5 }}
                                    className="flex items-start gap-3"
                                >
                                    <div className="mt-1 w-1.5 h-1.5 rounded-full bg-violet shrink-0" />
                                    <span className="text-[12px] text-text-secondary leading-tight">{step}</span>
                                </motion.div>
                            ))}
                        </div>
                    </div>

                    <div className="bg-violet/5 rounded-xl p-5 border border-violet/10">
                        <h3 className="text-[10px] font-bold text-violet uppercase tracking-widest mb-3 flex items-center gap-2">
                            <Zap size={12} /> Focus Areas
                        </h3>
                        <div className="flex flex-wrap gap-2">
                            {['Scalability', 'Backend', 'React', 'Leadership'].map(tag => (
                                <span key={tag} className="text-[11px] px-2 py-0.5 bg-white border border-violet/20 rounded text-violet-dark font-medium font-mono">
                                    {tag}
                                </span>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
