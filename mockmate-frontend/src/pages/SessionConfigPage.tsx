import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Briefcase, Building, AlertCircle, Loader2 } from 'lucide-react';
import { sessionService } from '../services/sessionService';

export default function SessionConfigPage() {
    const [jobRole, setJobRole] = useState('');
    const [companyName, setCompanyName] = useState('');
    const [difficulty, setDifficulty] = useState('MEDIUM');
    const [type, setType] = useState('FULL_MOCK');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const navigate = useNavigate();

    const handleStart = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        const trimmedRole = jobRole.trim();
        const trimmedCompany = companyName.trim();

        if (!trimmedRole || !trimmedCompany) {
            setError('Please provide both a job role and target company.');
            return;
        }

        setLoading(true);

        try {
            const session = await sessionService.createSession(trimmedRole, trimmedCompany, difficulty, type);
            navigate(`/interview/${session.id}`);
        } catch (err: unknown) {
            const error = err as { response?: { data?: { message?: string } } };
            setError(error.response?.data?.message || 'Failed to initialize session. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-xl mx-auto mt-10 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="mb-8">
                <h1 className="text-3xl font-display font-semibold text-text-primary tracking-tight">Configure Interview</h1>
                <p className="text-text-secondary mt-2">Tailor your mock interview session to match the exact role and company you are targeting.</p>
            </div>

            <div className="bg-bg-surface border border-border rounded-xl shadow-sm overflow-hidden">
                <form onSubmit={handleStart} className="p-8 flex flex-col space-y-6">

                    {error && (
                        <div role="alert" className="bg-danger-light text-danger p-4 rounded-lg text-[14px] flex items-start space-x-3">
                            <AlertCircle size={18} className="shrink-0 mt-0.5" />
                            <p>{error}</p>
                        </div>
                    )}

                    <div className="space-y-2">
                        <label htmlFor="jobRole" className="text-sm font-semibold text-text-secondary uppercase tracking-wider">Target Job Role</label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-text-tertiary">
                                <Briefcase size={18} />
                            </div>
                            <input
                                id="jobRole"
                                type="text"
                                required
                                value={jobRole}
                                onChange={(e) => setJobRole(e.target.value)}
                                className="w-full h-12 pl-10 pr-4 border border-border rounded-lg focus:outline-none focus:border-border-focus focus:ring-1 focus:ring-border-focus transition-colors bg-bg-page"
                                placeholder="e.g. Senior Frontend Engineer"
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label htmlFor="companyName" className="text-sm font-semibold text-text-secondary uppercase tracking-wider">Target Company</label>
                        <div className="relative">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-text-tertiary">
                                <Building size={18} />
                            </div>
                            <input
                                id="companyName"
                                type="text"
                                required
                                value={companyName}
                                onChange={(e) => setCompanyName(e.target.value)}
                                className="w-full h-12 pl-10 pr-4 border border-border rounded-lg focus:outline-none focus:border-border-focus focus:ring-1 focus:ring-border-focus transition-colors bg-bg-page"
                                placeholder="e.g. Google, Stripe, Notion"
                            />
                        </div>
                    </div>

                    <div className="space-y-3 pt-2">
                        <label className="text-sm font-semibold text-text-secondary uppercase tracking-wider">Interview Focus</label>
                        <div className="grid grid-cols-2 gap-3">
                            {[
                                { id: 'FULL_MOCK', label: 'Full Mock' },
                                { id: 'DSA_ONLY', label: 'DSA Only' },
                                { id: 'SYSTEM_DESIGN_ONLY', label: 'System Design' },
                                { id: 'HR_ONLY', label: 'Behavioral/HR' }
                            ].map((focus) => (
                                <button
                                    key={focus.id}
                                    type="button"
                                    onClick={() => setType(focus.id)}
                                    className={`py-3 px-4 rounded-lg border text-sm font-medium transition-all text-left flex flex-col ${type === focus.id
                                        ? 'bg-violet/5 border-violet text-violet shadow-sm ring-1 ring-violet/20'
                                        : 'bg-bg-page border-border text-text-secondary hover:border-text-tertiary'
                                        }`}
                                >
                                    <span className={type === focus.id ? 'text-violet' : 'text-text-primary'}>{focus.label}</span>
                                    <span className="text-[10px] opacity-70 mt-0.5">
                                        {focus.id === 'FULL_MOCK' && 'All phases in sequence'}
                                        {focus.id === 'DSA_ONLY' && 'Skip to Data Structures'}
                                        {focus.id === 'SYSTEM_DESIGN_ONLY' && 'Skip to Architecture'}
                                        {focus.id === 'HR_ONLY' && 'Skip to Culture Fit'}
                                    </span>
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="space-y-3 pt-2">
                        <label className="text-sm font-semibold text-text-secondary uppercase tracking-wider">Interview Difficulty</label>
                        <div className="grid grid-cols-3 gap-3">
                            {['EASY', 'MEDIUM', 'HARD'].map((level) => (
                                <button
                                    key={level}
                                    type="button"
                                    onClick={() => setDifficulty(level)}
                                    className={`py-3 rounded-lg border text-sm font-medium transition-colors ${difficulty === level
                                        ? 'bg-violet/10 border-violet text-violet shadow-sm'
                                        : 'bg-bg-page border-border text-text-secondary hover:border-text-tertiary'
                                        }`}
                                >
                                    {level.charAt(0) + level.slice(1).toLowerCase()}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="pt-6 border-t border-border mt-6">
                        <button
                            type="submit"
                            disabled={loading || !jobRole || !companyName}
                            className="w-full h-12 bg-violet hover:bg-violet-dark text-white rounded-lg font-medium transition-all flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed shadow-md"
                        >
                            {loading ? (
                                <span className="flex items-center space-x-2">
                                    <Loader2 className="animate-spin" size={18} />
                                    <span>Preparing Environments...</span>
                                </span>
                            ) : (
                                'Launch Interview Room →'
                            )}
                        </button>
                    </div>

                </form>
            </div>
        </div>
    );
}
