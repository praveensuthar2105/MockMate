import type { Config } from 'tailwindcss'

export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                bg: {
                    page: 'var(--bg-page)',
                    surface: 'var(--bg-surface)',
                    subtle: 'var(--bg-subtle)',
                    overlay: 'var(--bg-overlay)',
                },
                border: {
                    DEFAULT: 'var(--border)',
                    strong: 'var(--border-strong)',
                    focus: 'var(--border-focus)',
                },
                violet: {
                    DEFAULT: 'var(--violet)',
                    light: 'var(--violet-light)',
                    dark: 'var(--violet-dark)',
                },
                success: {
                    DEFAULT: 'var(--success)',
                    light: 'var(--success-light)',
                },
                warning: {
                    DEFAULT: 'var(--warning)',
                    light: 'var(--warning-light)',
                },
                danger: {
                    DEFAULT: 'var(--danger)',
                    light: 'var(--danger-light)',
                },
                info: {
                    DEFAULT: 'var(--info)',
                    light: 'var(--info-light)',
                },
                text: {
                    primary: 'var(--text-primary)',
                    secondary: 'var(--text-secondary)',
                    tertiary: 'var(--text-tertiary)',
                },
                phase: {
                    resume: 'var(--phase-resume)',
                    resumeBg: 'var(--phase-resume-bg)',
                    dsa: 'var(--phase-dsa)',
                    dsaBg: 'var(--phase-dsa-bg)',
                    systemDesign: 'var(--phase-system-design)',
                    systemDesignBg: 'var(--phase-system-design-bg)',
                    hr: 'var(--phase-hr)',
                    hrBg: 'var(--phase-hr-bg)',
                }
            },
            fontFamily: {
                display: ['Fraunces', 'serif'],
                body: ['Geist Sans', 'sans-serif'],
                mono: ['Geist Mono', 'monospace'],
            },
            boxShadow: {
                sm: '0 1px 2px rgba(0,0,0,0.05)',
                md: '0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.04)',
                lg: '0 4px 6px rgba(0,0,0,0.06), 0 2px 4px rgba(0,0,0,0.04)',
            },
            borderRadius: {
                sm: '6px',
                md: '10px',
                lg: '14px',
                xl: '20px',
                full: '9999px',
            }
        },
    },
    plugins: [],
} satisfies Config
