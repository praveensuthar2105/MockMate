import { Link } from 'react-router-dom';
import { Sparkles, Trophy, Mic2, Code2, ArrowRight } from 'lucide-react';

export default function LandingPage() {
    return (
        <div className="min-h-screen bg-[#F8F9FA] text-slate-900 overflow-hidden relative selection:bg-violet/10">
            {/* Background Orbs - Softer for Light Mode */}
            <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-violet/5 blur-[120px] rounded-full" />
            <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-violet-dark/5 blur-[120px] rounded-full" />

            {/* Navbar (Mini) */}
            <nav className="fixed top-0 w-full z-50 backdrop-blur-xl bg-white/70 border-b border-slate-200">
                <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                        <div className="w-9 h-9 rounded-xl bg-gradient-to-br from-violet to-violet-dark flex items-center justify-center shadow-lg shadow-violet/20">
                            <Sparkles className="text-white w-5 h-5" />
                        </div>
                        <span className="font-display font-bold text-2xl tracking-tight text-slate-900">
                            MockMate
                        </span>
                    </div>
                    <div className="flex items-center space-x-6">
                        <Link to="/login" className="text-sm font-bold text-slate-600 hover:text-violet transition-colors">
                            Log In
                        </Link>
                        <Link to="/register" className="px-6 py-2.5 rounded-xl bg-violet hover:bg-violet-dark text-white text-sm font-bold transition-all shadow-lg shadow-violet/20 hover:scale-105 active:scale-95">
                            Get Started
                        </Link>
                    </div>
                </div>
            </nav>

            {/* Hero Section */}
            <main className="relative z-10 pt-40 pb-20 px-6 max-w-7xl mx-auto text-center">
                <div className="max-w-4xl mx-auto space-y-10 animate-in fade-in slide-in-from-bottom-8 duration-700">
                    <div className="inline-flex items-center space-x-2 px-4 py-1.5 rounded-full bg-violet/5 border border-violet/10 text-violet font-bold text-xs uppercase tracking-widest">
                        <Trophy size={14} />
                        <span>Practice with Gemini AI</span>
                    </div>

                    <h1 className="text-6xl md:text-8xl font-display font-bold leading-[1.1] tracking-tight text-slate-900">
                        The Smarter Way to <br />
                        <span className="text-violet">Technical Interviews</span>
                    </h1>

                    <p className="text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed font-medium">
                        Master coding problems, system design, and behavioral interviews with real-time AI feedback. Build confidence and land your dream job.
                    </p>

                    <div className="flex flex-wrap justify-center gap-5 pt-6">
                        <Link to="/register" className="flex items-center space-x-3 px-10 py-5 rounded-2xl bg-violet text-white font-bold text-lg hover:bg-violet-dark transition-all shadow-2xl shadow-violet/30 hover:shadow-violet/50 hover:-translate-y-1">
                            <span>Ready to Start?</span>
                            <ArrowRight size={22} />
                        </Link>
                        <Link to="/login" className="px-10 py-5 rounded-2xl bg-white border border-slate-200 text-slate-900 font-bold text-lg hover:bg-slate-50 transition-all shadow-sm">
                            Learn More
                        </Link>
                    </div>
                </div>

                {/* UI Preview Section */}
                <div className="mt-24 relative max-w-5xl mx-auto animate-in fade-in zoom-in-95 duration-1000 delay-300">
                    <div className="rounded-[40px] bg-white border border-slate-200 shadow-[0_32px_64px_-16px_rgba(0,0,0,0.1)] p-4 relative overflow-hidden">
                        <div className="aspect-[16/9] rounded-[28px] bg-slate-50 border border-slate-100 flex flex-col p-8 items-start text-left overflow-hidden">
                            <div className="flex items-center space-x-2 mb-8 border-b border-slate-200 w-full pb-6">
                                <div className="w-3 h-3 rounded-full bg-slate-300" />
                                <div className="w-3 h-3 rounded-full bg-slate-300" />
                                <div className="w-3 h-3 rounded-full bg-slate-300" />
                            </div>
                            <div className="w-full space-y-6">
                                <div className="h-6 w-1/3 bg-slate-200 rounded-lg animate-pulse" />
                                <div className="h-4 w-full bg-slate-100 rounded-lg" />
                                <div className="h-4 w-5/6 bg-slate-100 rounded-lg" />
                                <div className="grid grid-cols-2 gap-6 mt-12">
                                    <div className="h-32 rounded-3xl bg-white border border-slate-200 p-6 flex flex-col justify-between shadow-sm">
                                        <Code2 className="text-violet" size={24} />
                                        <div className="text-xs font-bold text-slate-400 uppercase tracking-widest">Coding Platform</div>
                                    </div>
                                    <div className="h-32 rounded-3xl bg-white border border-slate-200 p-6 flex flex-col justify-between shadow-sm">
                                        <Mic2 className="text-violet" size={24} />
                                        <div className="text-xs font-bold text-slate-400 uppercase tracking-widest">Voice Analytics</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    {/* Floating Stats */}
                    <div className="absolute -top-10 -right-10 p-6 rounded-[32px] bg-white border border-slate-200 shadow-2xl transform rotate-6 animate-float z-20">
                        <div className="text-xs text-slate-400 uppercase font-bold mb-1">Pass rate</div>
                        <div className="text-3xl font-display font-bold text-violet">98.4%</div>
                    </div>
                </div>

                {/* Features Grid */}
                <div className="mt-40 grid grid-cols-1 md:grid-cols-3 gap-10">
                    <FeatureCard
                        icon={<Code2 className="text-violet" />}
                        title="DSA Mastery"
                        description="Timed coding challenges with real-time AI logic analysis and complexity suggestions."
                    />
                    <FeatureCard
                        icon={<Sparkles className="text-violet" />}
                        title="System Design"
                        description="Design scalable architectures with AI-driven architectural reviews and feedback."
                    />
                    <FeatureCard
                        icon={<Mic2 className="text-violet" />}
                        title="Dynamic Voice"
                        description="Engaging voice interviews with transcription and emotional tone analysis."
                    />
                </div>
            </main>

            <footer className="py-16 border-t border-slate-200 text-center text-slate-400 font-bold text-sm bg-white">
                &copy; 2024 MockMate. Redefining interview preparation.
            </footer>
        </div>
    );
}

function FeatureCard({ icon, title, description }: { icon: React.ReactNode; title: string, description: string }) {
    return (
        <div className="p-10 rounded-[32px] bg-white border border-slate-200 hover:border-violet/30 hover:shadow-xl hover:shadow-violet/5 transition-all group cursor-default text-left">
            <div className="w-16 h-16 rounded-2xl bg-violet/5 flex items-center justify-center mb-8 group-hover:scale-110 transition-transform group-hover:bg-violet/10">
                {icon}
            </div>
            <h3 className="text-2xl font-bold mb-4 text-slate-900">{title}</h3>
            <p className="text-slate-600 leading-relaxed font-medium">{description}</p>
        </div>
    );
}
