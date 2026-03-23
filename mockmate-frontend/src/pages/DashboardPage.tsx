import { useEffect, useState } from 'react';
import { Target, TrendingUp, Trophy, Star } from 'lucide-react';
import { StatCard } from '../components/dashboard/StatCard';
import { ScoreTrendChart } from '../components/dashboard/ScoreTrendChart';
import { SkillRadarChart } from '../components/dashboard/SkillRadarChart';
import { RecentSessionsTable } from '../components/dashboard/RecentSessionsTable';
import { StartInterviewCta } from '../components/dashboard/StartInterviewCta';
import { analyticsService } from '../services/analyticsService';
import type { DashboardMetrics } from '../services/analyticsService';
import { api } from '../services/api';
import type { InterviewSession } from '../types';
import { mapInterviewResponseToSession } from '../services/sessionMapper';

export default function DashboardPage() {
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [recentSessions, setRecentSessions] = useState<InterviewSession[]>([]);
    const [loading, setLoading] = useState(true);

    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);
                setError(null);
                const [metricsData, sessionsRes] = await Promise.all([
                    analyticsService.getOverview(),
                    api.get('/api/interviews/me')
                ]);
                setMetrics(metricsData);
                const mappedSessions = sessionsRes.data
                    .map(mapInterviewResponseToSession)
                    .sort((a: InterviewSession, b: InterviewSession) => new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime())
                    .slice(0, 5);
                setRecentSessions(mappedSessions);
            } catch (err) {
                console.error('Failed to load dashboard data', err);
                setError('Failed to load dashboard data. Please try again later.');
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    if (error) {
        return (
            <div className="w-full h-full flex flex-col items-center justify-center min-h-[60vh] space-y-4">
                <div className="p-3 bg-danger-light rounded-full text-danger">
                    <Star size={24} />
                </div>
                <h2 className="text-xl font-display font-semibold text-text-primary">Something went wrong</h2>
                <p className="text-text-secondary text-sm max-w-md text-center">{error}</p>
                <button
                    onClick={() => window.location.reload()}
                    className="px-4 py-2 bg-violet text-white rounded-md text-sm font-medium hover:bg-violet/90 transition-colors"
                >
                    Retry
                </button>
            </div>
        );
    }

    if (loading || !metrics) {
        return (
            <div className="w-full h-full flex items-center justify-center min-h-[60vh]">
                <div className="animate-pulse flex flex-col items-center">
                    <div className="w-10 h-10 border-4 border-violet border-t-transparent rounded-full animate-spin mb-4" />
                    <p className="text-text-tertiary text-sm">Loading your dashboard...</p>
                </div>
            </div>
        );
    }

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
                    value={metrics.totalInterviews}
                    icon={Target}
                />
                <StatCard
                    title="Avg. Overall Score"
                    value={`${metrics.averageScore}`}
                    icon={TrendingUp}
                    trend={metrics.averageScore >= 70 ? `+${(metrics.averageScore / 20).toFixed(1)}%` : undefined}
                />
                <StatCard
                    title="Highest Score"
                    value={`${metrics.highestScore}`}
                    icon={Trophy}
                />
                <StatCard
                    title="Percentile"
                    value={metrics.averageScore > 0 ? `Top ${Math.max(5, 100 - metrics.averageScore)}%` : 'N/A'}
                    icon={Star}
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
                <div className="lg:col-span-2">
                    <ScoreTrendChart data={metrics.recentTrends} />
                </div>
                <div>
                    <SkillRadarChart data={metrics.skillRadar} />
                </div>
            </div>

            <div>
                <RecentSessionsTable sessions={recentSessions} />
            </div>

        </div>
    );
}
