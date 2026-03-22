import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';

interface ScoreRingProps {
    score: number;
}

export function ScoreRing({ score }: ScoreRingProps) {
    const [animatedScore, setAnimatedScore] = useState(0);
    const radius = 64;
    const circumference = 2 * Math.PI * radius;

    useEffect(() => {
        const timer = setTimeout(() => {
            setAnimatedScore(score);
        }, 100);
        return () => clearTimeout(timer);
    }, [score]);

    const offset = circumference - (animatedScore / 100) * circumference;

    const getColor = (s: number) => {
        if (s >= 70) return '#16A34A'; // success
        if (s >= 40) return '#D97706'; // warning
        return '#DC2626'; // danger
    };

    const color = getColor(score);

    return (
        <div className="relative flex items-center justify-center w-40 h-40">
            <svg width="160" height="160" className="transform -rotate-90">
                {/* Track */}
                <circle
                    cx="80"
                    cy="80"
                    r={radius}
                    stroke="currentColor"
                    strokeWidth="10"
                    fill="none"
                    className="text-border"
                />
                {/* Progress Arc */}
                <motion.circle
                    cx="80"
                    cy="80"
                    r={radius}
                    stroke={color}
                    strokeWidth="10"
                    fill="none"
                    strokeLinecap="round"
                    initial={{ strokeDasharray: circumference, strokeDashoffset: circumference }}
                    animate={{ strokeDashoffset: offset }}
                    transition={{ duration: 1.5, ease: "easeOut" }}
                />
            </svg>

            <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="font-display text-4xl font-bold text-text-primary">
                    {Math.round(animatedScore)}
                </span>
                <span className="font-sans text-xs font-medium text-text-tertiary uppercase tracking-widest mt-0.5">
                    / 100
                </span>
            </div>
        </div>
    );
}
