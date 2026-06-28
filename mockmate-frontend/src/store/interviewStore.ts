import { create } from 'zustand';
import type { DsaProblem, ExecutionResult, CodeEvaluation } from '../types';

const getDefaultCode = (lang: string) => {
    if (lang === 'java') return 'class Solution {\n    // Implement your solution here\n}';
    if (lang === 'python') return 'class Solution:\n    def solve(self):\n        pass';
    return '// Start coding here...\n';
};

interface InterviewState {
    dsaProblem: DsaProblem | null;
    executionResult: ExecutionResult | null;
    codeEvaluation: CodeEvaluation | null;
    hintsUsed: number;
    code: string;
    language: string;
    codeBlocks: Record<string, string>;
    isRunning: boolean;
    isSubmitting: boolean;
    submitted: boolean;

    setProblem: (problem: DsaProblem | null) => void;
    setExecutionResult: (result: ExecutionResult | null) => void;
    setEvaluation: (evaluation: CodeEvaluation | null) => void;
    setCode: (code: string) => void;
    setLanguage: (lang: string) => void;
    useHint: () => void;
    setSubmitted: (submitted: boolean) => void;
    setRunning: (running: boolean) => void;
    setSubmitting: (submitting: boolean) => void;
}

export const useInterviewStore = create<InterviewState>((set) => ({
    dsaProblem: null,
    executionResult: null,
    codeEvaluation: null,
    hintsUsed: 0,
    code: '',
    language: 'java',
    codeBlocks: {},
    isRunning: false,
    isSubmitting: false,
    submitted: false,

    setProblem: (problem) => set((state) => {
        const nextCodeBlocks = { ...state.codeBlocks };
        if (problem) {
            if (!nextCodeBlocks['java'] && problem.javaStarterCode) {
                nextCodeBlocks['java'] = problem.javaStarterCode;
            }
            if (!nextCodeBlocks['python'] && problem.pythonStarterCode) {
                nextCodeBlocks['python'] = problem.pythonStarterCode;
            }
        }
        let currentCode = nextCodeBlocks[state.language];
        if (!currentCode && problem) {
            const langKey = `${state.language}StarterCode` as keyof DsaProblem;
            if (problem[langKey]) {
                currentCode = problem[langKey] as string;
            }
        }
        if (!currentCode) {
            currentCode = getDefaultCode(state.language);
        }
        nextCodeBlocks[state.language] = currentCode;

        return {
            dsaProblem: problem,
            code: currentCode,
            codeBlocks: nextCodeBlocks
        };
    }),
    setExecutionResult: (result) => set({ executionResult: result }),
    setEvaluation: (evaluation) => set({ codeEvaluation: evaluation }),
    setCode: (code) => set((state) => ({
        code,
        codeBlocks: {
            ...state.codeBlocks,
            [state.language]: code
        }
    })),
    setLanguage: (newLang) => set((state) => {
        const nextCodeBlocks = { ...state.codeBlocks };
        if (state.language) {
            nextCodeBlocks[state.language] = state.code;
        }
        let nextCode = nextCodeBlocks[newLang] || '';
        if (!nextCode && state.dsaProblem) {
            const langKey = `${newLang}StarterCode` as keyof DsaProblem;
            if (state.dsaProblem[langKey]) {
                nextCode = state.dsaProblem[langKey] as string;
            }
        }
        if (!nextCode) {
            nextCode = getDefaultCode(newLang);
        }
        nextCodeBlocks[newLang] = nextCode;

        return {
            language: newLang,
            code: nextCode,
            codeBlocks: nextCodeBlocks
        };
    }),
    useHint: () => set((state) => ({ hintsUsed: state.hintsUsed + 1 })),
    setSubmitted: (submitted) => set({ submitted }),
    setRunning: (isRunning) => set({ isRunning }),
    setSubmitting: (isSubmitting) => set({ isSubmitting }),
}));