import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Sparkles, LayoutDashboard, History, User, LogOut, PlusCircle } from 'lucide-react';

export default function Navbar() {
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        localStorage.removeItem('mockmate-token');
        navigate('/login');
    };

    const isActive = (path: string) => location.pathname === path;

    return (
        <nav className="fixed top-0 w-full z-50 backdrop-blur-md bg-bg-page/80 border-b border-white/5 h-16 shadow-lg shadow-black/20">
            <div className="max-w-7xl mx-auto px-6 h-full flex items-center justify-between">
                {/* Logo */}
                <Link to="/dashboard" className="flex items-center space-x-2 group">
                    <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet to-violet-dark flex items-center justify-center shadow-lg shadow-violet/20 group-hover:scale-110 transition-transform">
                        <Sparkles className="text-white w-5 h-5" />
                    </div>
                    <span className="font-display font-bold text-xl tracking-tight bg-gradient-to-r from-slate-900 to-slate-600 bg-clip-text text-transparent">
                        MockMate
                    </span>
                </Link>

                {/* Nav Links */}
                <div className="hidden md:flex items-center space-x-1">
                    <NavLink to="/dashboard" icon={<LayoutDashboard size={18} />} label="Dashboard" active={isActive('/dashboard')} />
                    <NavLink to="/history" icon={<History size={18} />} label="History" active={isActive('/history')} />
                    <NavLink to="/profile" icon={<User size={18} />} label="Profile" active={isActive('/profile')} />
                </div>

                {/* Right Actions */}
                <div className="flex items-center space-x-4">
                    <Link
                        to="/interview/new"
                        className="flex items-center space-x-2 px-4 py-2 rounded-xl bg-violet hover:bg-violet-dark text-white text-sm font-bold transition-all shadow-lg shadow-violet/20 hover:scale-105 active:scale-95"
                    >
                        <PlusCircle size={16} />
                        <span>New Interview</span>
                    </Link>

                    <button
                        onClick={handleLogout}
                        className="p-2 text-text-tertiary hover:text-danger transition-colors"
                        title="Logout"
                    >
                        <LogOut size={20} />
                    </button>
                </div>
            </div>
        </nav>
    );
}

function NavLink({ to, icon, label, active }: { to: string, icon: React.ReactNode, label: string, active: boolean }) {
    return (
        <Link
            to={to}
            className={`flex items-center space-x-2 px-4 py-1.5 rounded-xl text-sm font-bold transition-all ${active
                ? 'bg-violet/10 text-violet border border-violet/20'
                : 'text-text-secondary hover:text-violet hover:bg-violet/5'
                }`}
        >
            {icon}
            <span>{label}</span>
        </Link>
    );
}
