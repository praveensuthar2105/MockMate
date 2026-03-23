export type ExperienceLevel = 'FRESHER' | 'ONE_YEAR' | 'TWO_PLUS'

export interface User {
    id: number
    email: string
    name: string
    experienceLevel: ExperienceLevel
    profileComplete: boolean
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    userId: number;
    email: string;
    name: string;
    experienceLevel: ExperienceLevel;
    profileComplete: boolean;
}

export interface Resume {
    id: number
    filePath: string
    originalFileName: string
    rawText: string
    parsedJson: string
    skills: string
    summary: string
    uploadedAt: string
}

export type PhaseType = 'RESUME_SCREEN' | 'DSA' | 'SYSTEM_DESIGN' | 'HR'
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'
export type InterviewType = 'FULL_MOCK' | 'DSA_ONLY' | 'SYSTEM_DESIGN_ONLY' | 'HR_ONLY'
export type SessionStatus = 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED'
export type MessageRole = 'USER' | 'AI'

export interface InterviewSession {
    id: number
    companyName?: string
    jobRole: string
    difficulty: Difficulty
    interviewType: InterviewType
    status: SessionStatus
    currentPhase: PhaseType
    phaseEndTime: string | null
    overallScore: number | null
    startedAt: string
    endedAt: string | null
    messages?: ChatMessage[]
    resumeDurationMins?: number
    dsaDurationMins?: number
    systemDesignDurationMins?: number
    hrDurationMins?: number
}

export interface ChatMessage {
    id: number
    role: 'USER' | 'AI' | 'SYSTEM'
    content: string
    timestamp: string
    type?: 'TEXT' | 'CODE' | 'FEEDBACK'
    phase?: PhaseType
}

export interface WsEvent {
    type: 'MESSAGE' | 'TYPING' | 'PHASE_CHANGE' | 'TIMER_UPDATE' | 'ERROR'
    content: string | null
    role: MessageRole | null
    phase: PhaseType | null
    timeRemainingSeconds: number | null
    timestamp: string
}

export interface DsaProblem {
    title: string
    description: string
    constraints: string[]
    examples: ProblemExample[]
    hints: ProblemHint[]
    difficulty: Difficulty
    timeComplexityExpected: string
    spaceComplexityExpected: string
    javaStarterCode?: string
    javaTestRunner?: string
    pythonStarterCode?: string
    pythonTestRunner?: string
    javascriptStarterCode?: string
    javascriptTestRunner?: string
}

export interface ProblemExample {
    input: string
    output: string
    explanation: string
}

export interface ProblemHint {
    level: number
    hint: string
}

export interface ExecutionResult {
    compiled: boolean
    compileError: string | null
    results: TestCaseResult[]
    passedCount: number
    totalCount: number
    allPassed: boolean
}

export interface TestCaseResult {
    passed: boolean
    input: string
    expectedOutput: string
    actualOutput: string
    error: string | null
    timedOut: boolean
    executionTimeMs: number
}

export interface CodeEvaluation {
    timeComplexity: string
    spaceComplexity: string
    correctness: number
    codeQuality: number
    naming: number
    edgeCases: number
    overallScore: number
    feedback: string
    improvements: string[]
    hintsUsed: number
    testsPassed: number
    testsTotal: number
}

export interface SessionReport {
    overallScore: number
    phases: PhaseScore[]
    strengths: string[]
    weaknesses: string[]
    nextSteps: string[]
}

export interface PhaseScore {
    phase: PhaseType
    score: number
    feedback: string
}

export interface AnalyticsResponse {
    totalSessions: number
    averageScore: number
    bestScore: number
    currentStreak: number
    scoreHistory: ScorePoint[]
    phaseAverages: Record<PhaseType, number>
}

export interface ScorePoint {
    date: string
    score: number
    company: string
}

export interface PageResponse<T> {
    content: T[]
    totalElements: number
    totalPages: number
    size: number
    number: number
}
export interface DsaStatusResponse {
    problem: DsaProblem;
    submitted: boolean;
    evaluation: CodeEvaluation | null;
}
