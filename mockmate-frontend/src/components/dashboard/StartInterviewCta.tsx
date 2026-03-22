import { Link } from 'react-router-dom';
import { ArrowRight, Zap } from 'lucide-react';

export function StartInterviewCta() {
    return (
        <div className="bg-violet-light border border-violet/20 rounded-xl p-6 relative overflow-hidden shadow-sm flex flex-col md:flex-row items-center justify-between gap-6">

            <div className="absolute top-0 right-0 p-4 opacity-10 pointer-events-none">
                <Zap size={140} className="text-violet" />
            </div>

            <div className="flex-1 relative z-10">
                <h3 className="font-display text-xl font-semibold text-text-primary mb-2">
                    Ready for your next session?
                </h3>
                <p className="text-text-secondary text-sm max-w-md">
                    Keep your streak alive. Start a new mock interview dynamically tailored to your desired company constraints and role difficulty.
                </p>
            </div>

            <Link
                to="/interview/new"
                className="relative z-10 shrink-0 bg-violet hover:bg-violet-dark text-white px-6 py-3 rounded-lg font-medium transition-colors shadow-md flex items-center space-x-2"
            >
                <span>Start Interview</span>
                <ArrowRight size={18} />
            </Link>

        </div>
    );
}
