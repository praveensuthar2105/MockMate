import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, Loader2, Clock } from 'lucide-react';
import { api } from '../services/api';

export default function SessionConfigPage() {
    const [companyName, setCompanyName] = useState('');
    const [difficulty, setDifficulty] = useState('');

    // Default duration in minutes
    const [resumeDurationMins, setResumeDurationMins] = useState(5);
    const [dsaDurationMins, setDsaDurationMins] = useState(30);
    const [systemDesignDurationMins, setSystemDesignDurationMins] = useState(15);
    const [hrDurationMins, setHrDurationMins] = useState(10);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const navigate = useNavigate();

    const companies = [
        'Google', 'Amazon', 'Microsoft',
        'Flipkart', 'Zepto', 'Swiggy',
        'Razorpay', 'CRED', 'Other'
    ];

    const difficulties = ['EASY', 'MEDIUM', 'HARD'];

    const totalDuration = resumeDurationMins + dsaDurationMins + systemDesignDurationMins + hrDurationMins;

    const handleStart = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!companyName) {
            setError('Please select a target company.');
            return;
        }

        if (!difficulty) {
            setError('Please select a difficulty level.');
            return;
        }

        setLoading(true);

        try {
            const createRes = await api.post('/api/sessions/create', {
                company: companyName,
                difficulty,
                jobRole: 'Software Engineer', // Defaulting as not requested to be dynamic in this iteration
                type: 'FULL_MOCK',
                resumeDurationMins,
                dsaDurationMins,
                systemDesignDurationMins,
                hrDurationMins
            });

            const sessionId = createRes.data.id;
            await api.post(`/api/sessions/${sessionId}/start`);

            navigate(`/interview/${sessionId}`);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to initialize session. Please try again.');
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

                    <div className="space-y-3 pt-2">
                        <label className="text-sm font-semibold text-text-secondary uppercase tracking-wider">Target Company</label>
                        <div className="grid grid-cols-3 gap-3">
                            {companies.map((company) => (
                                <button
                                    key={company}
                                    type="button"
                                    onClick={() => setCompanyName(company)}
                                    className={`py-3 rounded-lg border text-sm font-medium transition-colors ${companyName === company
                                        ? 'bg-violet/10 border-violet text-violet shadow-sm'
                                        : 'bg-bg-page border-border text-text-secondary hover:border-text-tertiary'
                                        }`}
                                >
                                    {company}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="space-y-3 pt-2">
                        <label className="text-sm font-semibold text-text-secondary uppercase tracking-wider">Interview Difficulty</label>
                        <div className="grid grid-cols-3 gap-3">
                            {difficulties.map((level) => (
                                <button
                                    key={level}
                                    type="button"
                                    onClick={() => setDifficulty(level)}
                                    className={`py-3 rounded-lg border text-sm font-medium transition-colors flex flex-col items-center justify-center ${difficulty === level
                                        ? 'bg-violet/10 border-violet text-violet shadow-sm'
                                        : 'bg-bg-page border-border text-text-secondary hover:border-text-tertiary'
                                        }`}
                                >
                                    {level.charAt(0) + level.slice(1).toLowerCase()}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="space-y-4 pt-2">
                        <label className="text-sm font-semibold text-text-secondary uppercase tracking-wider">Duration Configuration</label>

                        <div className="space-y-4">
                            <div>
                                <div className="flex justify-between items-center mb-1">
                                    <span className="text-sm text-text-primary">Resume</span>
                                    <span className="text-sm text-text-secondary font-medium">{resumeDurationMins} min</span>
                                </div>
                                <input
                                    type="range"
                                    min="3" max="15"
                                    value={resumeDurationMins}
                                    onChange={(e) => setResumeDurationMins(parseInt(e.target.value))}
                                    className="w-full"
                                />
                            </div>

                            <div>
                                <div className="flex justify-between items-center mb-1">
                                    <span className="text-sm text-text-primary">DSA</span>
                                    <span className="text-sm text-text-secondary font-medium">{dsaDurationMins} min</span>
                                </div>
                                <input
                                    type="range"
                                    min="15" max="60"
                                    value={dsaDurationMins}
                                    onChange={(e) => setDsaDurationMins(parseInt(e.target.value))}
                                    className="w-full"
                                />
                            </div>

                            <div>
                                <div className="flex justify-between items-center mb-1">
                                    <span className="text-sm text-text-primary">System Design</span>
                                    <span className="text-sm text-text-secondary font-medium">{systemDesignDurationMins} min</span>
                                </div>
                                <input
                                    type="range"
                                    min="10" max="30"
                                    value={systemDesignDurationMins}
                                    onChange={(e) => setSystemDesignDurationMins(parseInt(e.target.value))}
                                    className="w-full"
                                />
                            </div>

                            <div>
                                <div className="flex justify-between items-center mb-1">
                                    <span className="text-sm text-text-primary">HR</span>
                                    <span className="text-sm text-text-secondary font-medium">{hrDurationMins} min</span>
                                </div>
                                <input
                                    type="range"
                                    min="5" max="20"
                                    value={hrDurationMins}
                                    onChange={(e) => setHrDurationMins(parseInt(e.target.value))}
                                    className="w-full"
                                />
                            </div>
                        </div>

                        <div className="flex justify-between items-center bg-bg-subtle p-4 rounded-lg border border-border mt-4">
                            <span className="text-sm font-medium text-text-secondary flex items-center space-x-2">
                                <Clock size={16} />
                                <span>Total Duration</span>
                            </span>
                            <span className="text-lg font-bold text-violet">{totalDuration} minutes</span>
                        </div>
                    </div>

                    <div className="pt-6 border-t border-border mt-6">
                        <button
                            type="submit"
                            disabled={loading || !companyName || !difficulty}
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
