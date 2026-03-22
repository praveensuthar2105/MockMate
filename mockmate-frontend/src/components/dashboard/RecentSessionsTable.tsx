import { Link } from 'react-router-dom';
import { ArrowUpRight, Clock, Award } from 'lucide-react';
import type { InterviewSession } from '../../types';

interface RecentSessionsTableProps {
    sessions: InterviewSession[];
    title?: string;
    showViewAll?: boolean;
}

export function RecentSessionsTable({
    sessions,
    title = 'Recent Interviews',
    showViewAll = false
}: RecentSessionsTableProps) {
    if (!sessions || sessions.length === 0) {
        return (
            <div className="bg-bg-surface border border-border rounded-xl p-8 shadow-sm flex flex-col items-center justify-center text-center">
                <div className="w-12 h-12 bg-bg-subtle rounded-full flex items-center justify-center mb-3">
                    <Clock className="text-text-tertiary" size={24} />
                </div>
                <h3 className="text-text-primary font-medium mb-1">No recent interviews</h3>
                <p className="text-text-secondary text-sm max-w-[250px] mx-auto">
                    You haven't completed any mock interviews yet. Start one to see your history here.
                </p>
            </div>
        );
    }

    return (
        <div className="bg-bg-surface border border-border rounded-xl shadow-sm overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-border">
                <h3 className="font-display text-base font-semibold text-text-primary">{title}</h3>
                {showViewAll && (
                    <Link to="/history" className="text-sm text-violet hover:text-violet-dark font-medium transition-colors">
                        View all
                    </Link>
                )}
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-bg-subtle/50 text-text-secondary text-[13px] uppercase tracking-wider font-semibold">
                            <th className="px-5 py-3 font-medium">Role & Company</th>
                            <th className="px-5 py-3 font-medium">Date</th>
                            <th className="px-5 py-3 font-medium">Score</th>
                            <th className="px-5 py-3 font-medium">Status</th>
                            <th className="px-5 py-3 text-right font-medium">Action</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-border">
                        {sessions.map((session) => (
                            <tr key={session.id} className="hover:bg-bg-subtle/30 transition-colors">
                                <td className="px-5 py-4">
                                    <div className="font-medium text-text-primary">{session.jobRole}</div>
                                    <div className="text-[13px] text-text-tertiary">{session.companyName}</div>
                                </td>
                                <td className="px-5 py-4 text-[14px] text-text-secondary">
                                    {session.startedAt && !isNaN(new Date(session.startedAt).getTime()) ? (
                                        new Date(session.startedAt).toLocaleDateString(undefined, {
                                            month: 'short', day: 'numeric', year: 'numeric'
                                        })
                                    ) : (
                                        '-'
                                    )}
                                </td>
                                <td className="px-5 py-4">
                                    {session.overallScore !== null ? (
                                        <div className="flex items-center space-x-1">
                                            <Award size={16} className={session.overallScore >= 80 ? 'text-success' : 'text-violet'} />
                                            <span className="font-semibold text-text-primary">{session.overallScore}</span>
                                            <span className="text-text-tertiary text-[13px]">/100</span>
                                        </div>
                                    ) : (
                                        <span className="text-text-tertiary text-sm">-</span>
                                    )}
                                </td>
                                <td className="px-5 py-4">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-[12px] font-medium ${session.status === 'COMPLETED'
                                        ? 'bg-success-light text-success'
                                        : 'bg-warning-light text-warning'
                                        }`}>
                                        {session.status}
                                    </span>
                                </td>
                                <td className="px-5 py-4 text-right">
                                    {session.status === 'COMPLETED' ? (
                                        <Link
                                            to={`/interview/${session.id}/report`}
                                            className="inline-flex items-center justify-center text-text-secondary hover:text-violet transition-colors p-2"
                                            title="View Report"
                                        >
                                            <ArrowUpRight size={18} />
                                        </Link>
                                    ) : (
                                        <Link
                                            to={`/interview/${session.id}`}
                                            className="inline-flex items-center justify-center text-text-tertiary hover:text-violet transition-colors p-2"
                                            title="Resume Interview"
                                        >
                                            <Clock size={16} />
                                        </Link>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
