import { create } from 'zustand';
import type { DsaProblem, ExecutionResult, CodeEvaluation } from '../types';

interface InterviewState {
    dsaProblem: DsaProblem | null;
    executionResult: ExecutionResult | null;
    codeEvaluation: CodeEvaluation | null;
    hintsUsed: number;
    code: string;
    language: string;
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
    isRunning: false,
    isSubmitting: false,
    submitted: false,

    setProblem: (problem) => set({ dsaProblem: problem }),
    setExecutionResult: (result) => set({ executionResult: result }),
    setEvaluation: (evaluation) => set({ codeEvaluation: evaluation }),
    setCode: (code) => set({ code }),
    setLanguage: (language) => set({ language }),
    useHint: () => set((state) => ({ hintsUsed: state.hintsUsed + 1 })),
    setSubmitted: (submitted) => set({ submitted }),
    setRunning: (isRunning) => set({ isRunning }),
    setSubmitting: (isSubmitting) => set({ isSubmitting }),
}));