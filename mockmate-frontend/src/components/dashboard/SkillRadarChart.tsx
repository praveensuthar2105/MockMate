import { PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart, ResponsiveContainer, Tooltip } from 'recharts';

interface SkillRadarChartProps {
    data: { skill: string; score: number; fullMark: number }[];
}

export function SkillRadarChart({ data }: SkillRadarChartProps) {
    return (
        <div className="bg-bg-surface border border-border rounded-xl p-5 shadow-sm h-[320px]">
            <h3 className="font-display text-base font-semibold text-text-primary mb-2">Skill Analysis</h3>
            <div className="w-full h-[240px] min-w-0">
                {data.length > 0 ? (
                    <ResponsiveContainer width="100%" height={240}>
                        <RadarChart cx="50%" cy="50%" outerRadius="70%" data={data}>
                            <PolarGrid stroke="var(--border)" />
                            <PolarAngleAxis
                                dataKey="skill"
                                tick={{ fill: 'var(--text-secondary)', fontSize: 11, fontWeight: 500 }}
                            />
                            <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                            <Tooltip
                                contentStyle={{
                                    borderRadius: '8px',
                                    border: '1px solid var(--border)',
                                    fontSize: '12px'
                                }}
                            />
                            <Radar
                                name="Score"
                                dataKey="score"
                                stroke="var(--violet)"
                                strokeWidth={2}
                                fill="var(--violet)"
                                fillOpacity={0.4}
                            />
                        </RadarChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="w-full h-full flex items-center justify-center text-text-tertiary">
                        <p className="text-sm">No analytical profile available yet.</p>
                    </div>
                )}
            </div>
        </div>
    );
}
