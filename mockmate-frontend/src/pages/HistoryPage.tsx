import { useEffect, useState } from 'react';
import { ChevronLeft, ChevronRight, AlertCircle, Loader2 } from 'lucide-react';
import { reportService } from '../services/reportService';
import { RecentSessionsTable } from '../components/dashboard/RecentSessionsTable';
import { LoadingSkeleton } from '../components/shared/LoadingSkeleton';
import type { InterviewSession } from '../types';

export default function HistoryPage() {
    const [sessions, setSessions] = useState<InterviewSession[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 10;

    const fetchHistory = async (pageNumber: number) => {
        setLoading(true);
        setError(null);
        try {
            const response = await reportService.getHistory(pageNumber, pageSize);
            setSessions(response.content || []);
            setTotalPages(response.totalPages || 0);
        } catch (err: any) {
            setError(err.message || 'Failed to fetch history');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchHistory(page);
    }, [page]);

    return (
        <div className="animate-in fade-in duration-500 pb-12">
            <div className="mb-8">
                <h1 className="text-3xl font-display font-semibold text-text-primary tracking-tight">Interview History</h1>
                <p className="text-text-secondary mt-1 text-[15px]">Review all your past sessions, transcripts, and score improvements over time.</p>
            </div>

            {loading && sessions.length === 0 ? (
                <LoadingSkeleton />
            ) : error ? (
                <div className="bg-bg-surface border border-border rounded-2xl p-12 flex flex-col items-center justify-center text-center">
                    <AlertCircle className="text-danger mb-4" size={48} />
                    <h3 className="text-lg font-bold text-text-primary mb-2">Error Loading History</h3>
                    <p className="text-text-secondary mb-6">{error}</p>
                    <button
                        onClick={() => fetchHistory(page)}
                        className="px-6 py-2 bg-violet text-white rounded-xl font-bold hover:bg-violet-dark transition-colors"
                    >
                        Try Again
                    </button>
                </div>
            ) : (
                <div className="space-y-6">
                    <div className="bg-bg-surface border border-border rounded-2xl shadow-sm overflow-hidden relative">
                        {loading && (
                            <div className="absolute inset-0 bg-white/50 backdrop-blur-[1px] z-10 flex items-center justify-center">
                                <Loader2 className="animate-spin text-violet" size={32} />
                            </div>
                        )}
                        <RecentSessionsTable sessions={sessions} title="Historical Sessions" showViewAll={false} />
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex items-center justify-center space-x-2">
                            <button
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0 || loading}
                                className="p-2 border border-border rounded-lg hover:bg-bg-subtle disabled:opacity-30 transition-colors"
                            >
                                <ChevronLeft size={20} />
                            </button>

                            <div className="flex items-center px-4 py-2 bg-bg-surface border border-border rounded-lg text-sm font-medium text-text-secondary">
                                Page {page + 1} of {totalPages}
                            </div>

                            <button
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page === totalPages - 1 || loading}
                                className="p-2 border border-border rounded-lg hover:bg-bg-subtle disabled:opacity-30 transition-colors"
                            >
                                <ChevronRight size={20} />
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
