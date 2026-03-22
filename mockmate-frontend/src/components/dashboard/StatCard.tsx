import type { LucideIcon } from 'lucide-react';

interface StatCardProps {
    title: string;
    value: string | number;
    icon: LucideIcon;
    trend?: string;
}

export function StatCard({ title, value, icon: Icon, trend }: StatCardProps) {
    return (
        <div className="bg-bg-surface border border-border rounded-xl p-5 shadow-sm flex flex-col justify-between h-[130px]">
            <div className="flex items-center justify-between">
                <span className="text-text-secondary text-sm font-medium tracking-wide">{title}</span>
                <div className="w-10 h-10 rounded-full bg-violet-light flex items-center justify-center shrink-0">
                    <Icon className="text-violet" size={20} />
                </div>
            </div>

            <div className="flex items-end justify-between mt-auto">
                <span className="font-display text-3xl font-semibold text-text-primary tracking-tight">
                    {value}
                </span>
                {trend && (
                    <span className="text-success text-xs font-semibold bg-success-light px-2 py-0.5 rounded-full mb-1">
                        {trend}
                    </span>
                )}
            </div>
        </div>
    );
}
