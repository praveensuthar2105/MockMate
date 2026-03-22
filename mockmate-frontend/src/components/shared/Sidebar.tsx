import { Link, useLocation } from 'react-router-dom';
import { Home, PlusCircle, History, UserCircle, LogOut } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';

export function Sidebar() {
    const location = useLocation();
    const { user, clearAuth } = useAuthStore();

    const navItems = [
        { label: 'Dashboard', icon: Home, path: '/dashboard' },
        { label: 'New Interview', icon: PlusCircle, path: '/interview/new' },
        { label: 'History', icon: History, path: '/history' },
        { label: 'Profile', icon: UserCircle, path: '/profile' },
    ];

    const handleLogout = () => {
        clearAuth();
    };

    return (
        <aside className="w-[232px] fixed left-0 top-0 h-screen bg-bg-overlay border-r border-border flex flex-col z-40">

            <div className="flex items-center space-x-3 px-5 py-6">
                <div className="w-8 h-8 rounded-full bg-violet flex items-center justify-center shrink-0 shadow-sm">
                    <span className="font-display font-semibold text-white text-base">M</span>
                </div>
                <span className="font-display text-lg font-semibold tracking-tight text-text-primary">
                    MockMate
                </span>
            </div>

            <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto">
                <div className="px-2 pb-2">
                    <span className="text-[11px] font-semibold tracking-wider text-text-tertiary uppercase">Main Menu</span>
                </div>

                {navItems.map((item) => {
                    const isActive = location.pathname.startsWith(item.path);
                    return (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={`flex items-center space-x-3 px-3 py-2 rounded-md transition-colors ${isActive
                                ? 'bg-violet-light text-violet font-medium'
                                : 'text-text-secondary hover:bg-bg-subtle hover:text-text-primary'
                                }`}
                        >
                            <item.icon size={18} className={isActive ? 'text-violet' : 'text-text-tertiary'} />
                            <span className="text-[15px]">{item.label}</span>
                        </Link>
                    );
                })}
            </nav>

            <div className="p-4 border-t border-border mt-auto">
                <div className="flex items-center space-x-3 w-full bg-bg-subtle px-3 py-3 rounded-lg">
                    <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-text-primary truncate">{user?.name || 'User'}</p>
                        <p className="text-xs text-text-tertiary truncate">{user?.email}</p>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="text-text-tertiary hover:text-danger transition-colors p-1"
                        title="Log out"
                    >
                        <LogOut size={16} />
                    </button>
                </div>
            </div>
        </aside>
    );
}
