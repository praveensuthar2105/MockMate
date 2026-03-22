import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Download,
    CheckCircle,
    AlertCircle,
    Calendar,
    Clock,
    Building2,
    BarChart3,
    Home,
    PlusCircle,
    Loader2
} from 'lucide-react';
import { reportService } from '../services/reportService';
import { sessionService } from '../services/sessionService';
import { ScoreRing } from '../components/report/ScoreRing';
import { PhaseScoreBar } from '../components/report/PhaseScoreBar';
import { TranscriptAccordion } from '../components/report/TranscriptAccordion';
import type { InterviewSession, SessionReport, PhaseType } from '../types';

export default function ReportPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [report, setReport] = useState<SessionReport | null>(null);
    const [session, setSession] = useState<InterviewSession | null>(null);
    const [loading, setLoading] = useState(true);
    const [isDownloading, setIsDownloading] = useState(false);

    useEffect(() => {
        if (!id) return;
        const fetchData = async () => {
            try {
                const sessionId = Number(id);
                const [reportData, sessionData] = await Promise.all([
                    reportService.getReport(sessionId),
                    sessionService.getById(sessionId)
                ]);
                setReport(reportData);
                setSession(sessionData);
            } catch (err) {
                console.error('Failed to load report data', err);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [id]);

    const handleDownload = async () => {
        if (!id) return;
        setIsDownloading(true);
        try {
            await reportService.downloadPdf(Number(id));
        } catch (err) {
            console.error('Download failed', err);
        } finally {
            setIsDownloading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex-1 h-[80vh] flex flex-col items-center justify-center">
                <Loader2 className="animate-spin text-violet mb-4" size={40} />
                <p className="text-text-tertiary font-medium animate-pulse">Generating your performance report...</p>
            </div>
        );
    }

    if (!report || !session) {
        return (
            <div className="flex-1 h-[80vh] flex flex-col items-center justify-center text-center p-6">
                <AlertCircle className="text-danger mb-4" size={48} />
                <h2 className="text-2xl font-display font-bold text-text-primary mb-2">Report Not Ready</h2>
                <p className="text-text-secondary max-w-md mb-8">
                    Your interview report is still being processed or could not be found.
                    Please ensure the interview is completed before viewing the report.
                </p>
                <button
                    onClick={() => navigate('/dashboard')}
                    className="px-6 py-2.5 bg-violet text-white rounded-xl font-bold hover:bg-violet-dark transition-colors"
                >
                    Back to Dashboard
                </button>
            </div>
        );
    }

    const durationMins = session.endedAt
        ? Math.round((new Date(session.endedAt).getTime() - new Date(session.startedAt).getTime()) / 60000)
        : 0;

    const phases: PhaseType[] = ['RESUME_SCREEN', 'DSA', 'SYSTEM_DESIGN', 'HR'];

    return (
        <div className="max-w-4xl mx-auto pb-32 animate-in fade-in slide-in-from-bottom-4 duration-700">
            {/* Header Card */}
            <div className="bg-bg-surface border border-border rounded-2xl p-8 mb-8 shadow-sm relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-violet/5 rounded-full -translate-y-1/2 translate-x-1/2 blur-2xl" />

                <h1 className="text-4xl font-display font-bold text-text-primary mb-6 tracking-tight">Interview Complete</h1>

                <div className="flex flex-wrap items-center gap-6">
                    <div className="flex items-center space-x-2 text-text-secondary">
                        <Building2 size={16} className="text-violet" />
                        <span className="text-sm font-medium">{session.companyName} Standard</span>
                    </div>
                    <div className="flex items-center space-x-2 text-text-secondary">
                        <BarChart3 size={16} className="text-success" />
                        <span className="text-sm font-medium capitalize">{session.difficulty.toLowerCase()} Level</span>
                    </div>
                    <div className="flex items-center space-x-2 text-text-secondary">
                        <Calendar size={16} className="text-warning" />
                        <span className="text-sm font-medium">{new Date(session.startedAt).toLocaleDateString()}</span>
                    </div>
                    <div className="flex items-center space-x-2 text-text-secondary">
                        <Clock size={16} className="text-danger" />
                        <span className="text-sm font-medium">{durationMins} min session</span>
                    </div>
                </div>
            </div>

            {/* Score Hero Section */}
            <div className="grid grid-cols-1 md:grid-cols-5 gap-8 mb-12">
                {/* Overall Score */}
                <div className="md:col-span-2 bg-bg-surface border border-border rounded-2xl p-8 shadow-sm flex flex-col items-center justify-center text-center">
                    <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.2em] mb-8">Overall Performance</h3>
                    <ScoreRing score={report.overallScore} />
                    <p className="mt-8 text-sm text-text-secondary font-medium italic opacity-80">
                        "{report.overallScore > 70 ? 'Excellent work! You are ready for the real thing.' : 'Solid effort! Focus on the suggested improvements below.'}"
                    </p>
                </div>

                {/* Phase Breakdown */}
                <div className="md:col-span-3 bg-bg-surface border border-border rounded-2xl p-8 shadow-sm">
                    <h3 className="text-[10px] font-bold text-text-tertiary uppercase tracking-[0.2em] mb-8">Phase Breakdown</h3>
                    <div className="space-y-6">
                        {(report.phases || []).map((p, idx) => (
                            <PhaseScoreBar
                                key={p.phase}
                                phase={p.phase}
                                score={p.score}
                                delay={idx * 0.2}
                            />
                        ))}
                    </div>
                </div>
            </div>

            {/* Strengths & Weaknesses */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-12">
                {/* Strengths */}
                <div className="bg-bg-surface border border-border border-l-[6px] border-l-success rounded-2xl p-8 shadow-sm">
                    <h3 className="text-lg font-display font-bold text-success flex items-center mb-6">
                        <CheckCircle className="mr-3" size={24} />
                        Key Strengths
                    </h3>
                    <ul className="space-y-4">
                        {(report.strengths || []).map((s, idx) => (
                            <li key={idx} className="flex items-start text-[14px] text-text-secondary leading-relaxed">
                                <span className="w-1.5 h-1.5 rounded-full bg-success/40 mt-2 mr-3 shrink-0" />
                                {s}
                            </li>
                        ))}
                    </ul>
                </div>

                {/* Areas to Improve */}
                <div className="bg-bg-surface border border-border border-l-[6px] border-l-danger rounded-2xl p-8 shadow-sm">
                    <h3 className="text-lg font-display font-bold text-danger flex items-center mb-6">
                        <AlertCircle className="mr-3" size={24} />
                        Areas to Improve
                    </h3>
                    <ul className="space-y-4">
                        {(report.weaknesses || []).map((w, idx) => (
                            <li key={idx} className="flex items-start text-[14px] text-text-secondary leading-relaxed">
                                <span className="w-1.5 h-1.5 rounded-full bg-danger/40 mt-2 mr-3 shrink-0" />
                                {w}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>

            {/* Next Steps */}
            <div className="mb-12">
                <h3 className="text-xl font-display font-bold text-text-primary mb-6">Recommended Actions</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {(report.nextSteps || []).map((step, idx) => (
                        <div key={idx} className="bg-bg-surface border border-border rounded-xl p-6 shadow-sm hover:border-violet/30 transition-colors group">
                            <div className="w-10 h-10 rounded-full bg-violet text-white flex items-center justify-center font-display font-bold text-lg mb-4 group-hover:scale-110 transition-transform">
                                {idx + 1}
                            </div>
                            <p className="text-[14px] text-text-secondary leading-relaxed font-medium">
                                {step}
                            </p>
                        </div>
                    ))}
                </div>
            </div>

            {/* Transcript Section */}
            <div className="mb-8">
                <div className="flex items-center justify-between mb-6">
                    <h3 className="text-xl font-display font-bold text-text-primary">Interview Transcript</h3>
                    <span className="text-xs text-text-tertiary font-medium bg-bg-subtle px-3 py-1 rounded-full uppercase tracking-wider">
                        Full Context
                    </span>
                </div>
                <TranscriptAccordion sessionId={Number(id)} phases={phases} />
            </div>

            {/* Sticky Action Bar */}
            <div className="fixed bottom-0 left-0 right-0 p-6 bg-white/80 backdrop-blur-lg border-t border-border z-40 flex justify-center">
                <div className="max-w-4xl w-full flex flex-col md:flex-row items-center justify-between gap-4">
                    <div className="flex items-center space-x-4">
                        <button
                            onClick={() => navigate('/dashboard')}
                            className="flex items-center space-x-2 px-5 py-2.5 text-text-secondary hover:text-text-primary transition-colors font-bold text-sm"
                        >
                            <Home size={18} />
                            <span>Dashboard</span>
                        </button>
                    </div>

                    <div className="flex items-center space-x-4 w-full md:w-auto">
                        <button
                            onClick={handleDownload}
                            disabled={isDownloading}
                            className="flex-1 md:flex-none flex items-center justify-center space-x-2 px-6 py-2.5 border-2 border-violet text-violet font-bold rounded-xl hover:bg-violet/5 transition-all disabled:opacity-50"
                        >
                            {isDownloading ? <Loader2 className="animate-spin" size={18} /> : <Download size={18} />}
                            <span>Download PDF</span>
                        </button>
                        <button
                            onClick={() => navigate('/interview/new')}
                            className="flex-1 md:flex-none flex items-center justify-center space-x-2 px-8 py-2.5 bg-violet text-white font-bold rounded-xl hover:bg-violet-dark transition-all shadow-lg shadow-violet/20"
                        >
                            <PlusCircle size={18} />
                            <span>Start New Interview</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
