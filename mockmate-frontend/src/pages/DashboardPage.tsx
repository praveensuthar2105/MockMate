import { useEffect } from 'react';
import { Target, TrendingUp, Trophy, Star } from 'lucide-react';
import { StatCard } from '../components/dashboard/StatCard';
import { ScoreTrendChart } from '../components/dashboard/ScoreTrendChart';
import { SkillRadarChart } from '../components/dashboard/SkillRadarChart';
import { RecentSessionsTable } from '../components/dashboard/RecentSessionsTable';
import { StartInterviewCta } from '../components/dashboard/StartInterviewCta';
import { useAnalyticsStore } from '../store/analyticsStore';
import { useSessionStore } from '../store/sessionStore';
import { LoadingSkeleton } from '../components/shared/LoadingSkeleton';
import { ErrorState } from '../components/shared/ErrorState';

export default function DashboardPage() {
    const { overview, loading: analyticsLoading, error: analyticsError, fetchOverview } = useAnalyticsStore();
    const { recentSessions, loading: sessionsLoading, error: sessionsError, fetchRecentSessions } = useSessionStore();

    useEffect(() => {
        fetchOverview();
        fetchRecentSessions();
    }, [fetchOverview, fetchRecentSessions]);

    const isLoading = analyticsLoading || sessionsLoading;
    const error = analyticsError || sessionsError;

    if (isLoading && !overview) {
        return <LoadingSkeleton />;
    }

    if (error) {
        return (
            <div className="py-10">
                <ErrorState
                    message={error}
                    onRetry={() => {
                        fetchOverview();
                        fetchRecentSessions();
                    }}
                />
            </div>
        );
    }

    if (!overview) return null;

    return (
        <div className="flex flex-col space-y-8 animate-in fade-in duration-500">
            <div>
                <h1 className="text-3xl font-display font-semibold text-text-primary tracking-tight">Overview</h1>
                <p className="text-text-secondary mt-1 text-[15px]">Welcome back. Here's a summary of your recent interview performances.</p>
            </div>

            <StartInterviewCta />

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
                <StatCard
                    title="Total Interviews"
                    value={overview?.totalSessions ?? 0}
                    icon={Target}
                />
                <StatCard
                    title="Avg. Overall Score"
                    value={overview?.averageScore ?? 0}
                    icon={TrendingUp}
                    trend={overview?.averageScore >= 70 ? `+${((overview?.averageScore || 0) / 20).toFixed(1)}%` : undefined}
                />
                <StatCard
                    title="Best Score"
                    value={overview?.bestScore ?? 0}
                    icon={Trophy}
                />
                <StatCard
                    title="Current Streak"
                    value={`${overview?.currentStreak ?? 0} days`}
                    icon={Star}
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
                <div className="lg:col-span-2">
                    {overview?.scoreHistory?.length ? (
                        <ScoreTrendChart data={overview.scoreHistory} />
                    ) : (
                        <div className="h-[300px] bg-bg-surface border border-border rounded-xl flex items-center justify-center text-text-tertiary italic">
                            Complete your first interview to see your score trend
                        </div>
                    )}
                </div>
                <div>
                    {overview?.phaseAverages ? (
                        <SkillRadarChart
                            data={Object.entries(overview.phaseAverages || {}).map(([phase, score]) => ({
                                skill: phase.replace('_', ' '),
                                score,
                                fullMark: 100
                            }))}
                        />
                    ) : (
                        <div className="h-[300px] bg-bg-surface border border-border rounded-xl flex items-center justify-center text-text-tertiary italic">
                            No skills data yet
                        </div>
                    )}
                </div>
            </div>

            <div>
                <RecentSessionsTable sessions={recentSessions} showViewAll={true} />
            </div>
        </div>
    );
}
