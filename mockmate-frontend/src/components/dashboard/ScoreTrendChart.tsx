import { useId } from 'react';
import { Area, AreaChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

interface ScoreTrendChartProps {
    data: { date: string; score: number }[];
}

export function ScoreTrendChart({ data }: ScoreTrendChartProps) {
    const chartId = useId();
    const gradientId = `colorScore-${chartId}`;

    return (
        <div className="bg-bg-surface border border-border rounded-xl p-5 shadow-sm h-[320px]">
            <h3 className="font-display text-base font-semibold text-text-primary mb-6">Performance Trend</h3>
            <div className="w-full h-[220px] min-w-0">
                {data.length > 0 ? (
                    <ResponsiveContainer width="100%" height={220}>
                        <AreaChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                            <defs>
                                <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="var(--violet)" stopOpacity={0.3} />
                                    <stop offset="95%" stopColor="var(--violet)" stopOpacity={0} />
                                </linearGradient>
                            </defs>
                            <XAxis
                                dataKey="date"
                                axisLine={false}
                                tickLine={false}
                                tick={{ fontSize: 12, fill: 'var(--text-tertiary)' }}
                                dy={10}
                            />
                            <YAxis
                                axisLine={false}
                                tickLine={false}
                                tick={{ fontSize: 12, fill: 'var(--text-tertiary)' }}
                                domain={[0, 100]}
                            />
                            <Tooltip
                                contentStyle={{
                                    borderRadius: '8px',
                                    border: '1px solid var(--border)',
                                    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                                    fontSize: '13px'
                                }}
                            />
                            <Area
                                type="monotone"
                                dataKey="score"
                                stroke="var(--violet)"
                                strokeWidth={2}
                                fillOpacity={1}
                                fill={`url(#${gradientId})`}
                            />
                        </AreaChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="w-full h-full flex flex-col items-center justify-center text-text-tertiary">
                        <p className="text-sm">No trend data available yet.</p>
                        <p className="text-xs mt-1">Complete more interviews to see your progress.</p>
                    </div>
                )}
            </div>
        </div>
    );
}
