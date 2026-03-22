import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { authService } from '../../services/authService';

export function LoginForm() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await authService.login(email, password);
            navigate('/dashboard', { replace: true });
        } catch (err: any) {
            setError(err.message || 'Login failed. Please check your credentials.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col space-y-4">
            {error && (
                <div className="bg-danger-light text-danger p-3 rounded-md text-sm text-center">
                    {error}
                </div>
            )}

            <div className="flex flex-col space-y-1.5">
                <label
                    htmlFor="login-email"
                    className="text-xs font-semibold text-text-secondary tracking-wider uppercase"
                >
                    Email Address
                </label>
                <input
                    id="login-email"
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
                    htmlFor="login-password"
                    className="text-xs font-semibold text-text-secondary tracking-wider uppercase"
                >
                    Password
                </label>
                <div className="relative">
                    <input
                        id="login-password"
                        type={showPassword ? 'text' : 'password'}
                        required
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

            <div className="pt-2">
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full h-11 bg-violet hover:bg-violet-dark text-white rounded-md font-medium transition-colors flex items-center justify-center disabled:opacity-70"
                >
                    {loading ? <Loader2 className="animate-spin" size={20} /> : 'Sign In'}
                </button>
            </div>

            <div className="flex items-center space-x-2 my-4">
                <div className="h-px bg-border flex-1" />
                <span className="text-text-tertiary text-sm px-2">or</span>
                <div className="h-px bg-border flex-1" />
            </div>

            <div className="text-center text-sm text-text-secondary">
                Don&apos;t have an account?{' '}
                <Link to="/register" className="text-violet hover:text-violet-dark font-medium transition-colors">
                    Register &rarr;
                </Link>
            </div>
        </form>
    );
}
