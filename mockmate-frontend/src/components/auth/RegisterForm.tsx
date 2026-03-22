import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { authService } from '../../services/authService';

type ExperienceLevel = 'FRESHER' | 'ONE_YEAR' | 'TWO_PLUS';

export function RegisterForm() {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [experienceLevel, setExperienceLevel] = useState<ExperienceLevel>('FRESHER');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await authService.register(name, email, password, experienceLevel);
            navigate('/profile', { replace: true });
        } catch (err: any) {
            setError(err.message || 'Registration failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const expLevels: { label: string; value: ExperienceLevel }[] = [
        { label: 'Fresher', value: 'FRESHER' },
        { label: '1 Year Exp', value: 'ONE_YEAR' },
        { label: '2+ Years', value: 'TWO_PLUS' },
    ];

    return (
        <form onSubmit={handleSubmit} className="flex flex-col space-y-4">
            {error && (
                <div className="bg-danger-light text-danger p-3 rounded-md text-sm text-center">
                    {error}
                </div>
            )}

            <div className="flex flex-col space-y-1.5">
                <label
                    htmlFor="register-name"
                    className="text-xs font-semibold text-text-secondary tracking-wider uppercase"
                >
                    Full Name
                </label>
                <input
                    id="register-name"
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="h-11 px-3 border border-border rounded-md focus:outline-none focus:border-border-focus focus:ring-1 focus:ring-border-focus transition-colors"
                    placeholder="Jane Doe"
                />
            </div>

            <div className="flex flex-col space-y-1.5">
                <label
                    htmlFor="register-email"
                    className="text-xs font-semibold text-text-secondary tracking-wider uppercase"
                >
                    Email Address
                </label>
                <input
                    id="register-email"
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="h-11 px-3 border border-border rounded-md focus:outline-none focus:border-border-focus focus:ring-1 focus:ring-border-focus transition-colors"
                    placeholder="name@example.com"
                />
            </div>

            <div className="flex flex-col space-y-1.5">
                <label
                    htmlFor="register-password"
                    className="text-xs font-semibold text-text-secondary tracking-wider uppercase"
                >
                    Password
                </label>
                <div className="relative">
                    <input
                        id="register-password"
                        type={showPassword ? 'text' : 'password'}
                        required
                        minLength={6}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full h-11 pl-3 pr-10 border border-border rounded-md focus:outline-none focus:border-border-focus focus:ring-1 focus:ring-border-focus transition-colors"
                        placeholder="••••••••"
                    />
                    <button
                        type="button"
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-text-tertiary hover:text-text-primary"
                        onClick={() => setShowPassword(!showPassword)}
                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                    >
                        {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                </div>
            </div>

            <div className="flex flex-col space-y-2 pt-1">
                <label className="text-xs font-semibold text-text-secondary tracking-wider uppercase">
                    Experience Level
                </label>
                <div className="grid grid-cols-3 gap-2">
                    {expLevels.map((lvl) => (
                        <button
                            key={lvl.value}
                            type="button"
                            onClick={() => setExperienceLevel(lvl.value)}
                            className={`py-2 text-sm text-center rounded-md border transition-colors ${experienceLevel === lvl.value
                                ? 'bg-violet border-violet text-white font-medium'
                                : 'bg-surface border-border text-text-secondary hover:bg-bg-subtle'
                                }`}
                        >
                            {lvl.label}
                        </button>
                    ))}
                </div>
            </div>

            <div className="pt-2">
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full h-11 bg-violet hover:bg-violet-dark text-white rounded-md font-medium transition-colors flex items-center justify-center disabled:opacity-70"
                >
                    {loading ? <Loader2 className="animate-spin" size={20} /> : 'Create Account'}
                </button>
            </div>

            <div className="text-center text-sm text-text-secondary pt-3">
                Already have an account?{' '}
                <Link to="/login" className="text-violet hover:text-violet-dark font-medium transition-colors">
                    Sign In &rarr;
                </Link>
            </div>
        </form>
    );
}
