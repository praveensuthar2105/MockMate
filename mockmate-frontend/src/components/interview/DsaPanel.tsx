import { useState, useEffect, lazy } from 'react';
import { Play, CheckCircle2, Loader2, Lightbulb } from 'lucide-react';
import { codeService } from '../../services/codeService';
import { useInterviewStore } from '../../store/interviewStore';

const Editor = lazy(() => import('@monaco-editor/react'));

interface DsaPanelProps {
    sessionId: number;
}

export function DsaPanel({ sessionId }: DsaPanelProps) {
    const {
        dsaProblem: problem,
        setProblem,
        code,
        setCode,
        language,
        setLanguage,
        executionResult: output,
        setExecutionResult: setOutput,
        codeEvaluation: evaluation,
        setEvaluation,
        hintsUsed,
        useHint,
        isRunning,
        setRunning: setIsRunning,
        isSubmitting,
        setSubmitting: setIsSubmitting,
        submitted: isSubmitted,
        setSubmitted: setIsSubmitted
    } = useInterviewStore();

    const [leftWidth, setLeftWidth] = useState(35); // percentage
    const [activeTab, setActiveTab] = useState<'output' | 'testcases' | 'evaluation'>('testcases');
    const [customTestCases, setCustomTestCases] = useState<string>('');
    const [isResizing, setIsResizing] = useState(false);
    const [hintText, setHintText] = useState<string | null>(null);
    const [isLoadingHint, setIsLoadingHint] = useState(false);

    useEffect(() => {
        if (isResizing) {
            const handleMouseMove = (e: MouseEvent) => {
                const newWidth = (e.clientX / window.innerWidth) * 100;
                if (newWidth > 20 && newWidth < 70) {
                    setLeftWidth(newWidth);
                }
            };
            const handleMouseUp = () => setIsResizing(false);
            window.addEventListener('mousemove', handleMouseMove);
            window.addEventListener('mouseup', handleMouseUp);
            return () => {
                window.removeEventListener('mousemove', handleMouseMove);
                window.removeEventListener('mouseup', handleMouseUp);
            };
        }
    }, [isResizing]);
    const [isFetching, setIsFetching] = useState(false);

    useEffect(() => {
        if (!problem && sessionId) {
            const fetchProblem = async () => {
                setIsFetching(true);
                try {
                    const data = await codeService.getProblem(sessionId);
                    setProblem(data.problem);
                    if (data.submitted) {
                        setIsSubmitted(true);
                        setEvaluation(data.evaluation);
                        setActiveTab('evaluation');
                    }
                } catch (err) {
                    console.error('Failed to fetch problem:', err);
                } finally {
                    setIsFetching(false);
                }
            };
            fetchProblem();
        }
    }, [sessionId, problem]);

    const handleLanguageChange = (newLang: string) => {
        setLanguage(newLang);
        if (newLang === 'java') {
            setCode('import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Read input using Scanner\n        Scanner sc = new Scanner(System.in);\n        // Write your solution here...\n        \n    }\n}');
        } else if (newLang === 'python') {
            setCode('import sys\n\ndef solve():\n    # Read input from sys.stdin\n    # Write your solution here...\n    pass\n\nif __name__ == "__main__":\n    solve()');
        } else if (newLang === 'javascript') {
            setCode('const readline = require("readline");\n\nconst rl = readline.createInterface({\n    input: process.stdin,\n    output: process.stdout,\n    terminal: false\n});\n\nrl.on("line", (line) => {\n    // Write your solution here...\n});');
        } else {
            setCode('// Start coding here...\n');
        }
    };

    const handleHint = async () => {
        if (hintsUsed >= 3) return;
        setIsLoadingHint(true);
        try {
            const nextLevel = hintsUsed + 1;
            const hint = await codeService.getHint(sessionId, nextLevel);
            setHintText(hint);
            useHint();
        } catch (error) {
            console.error('Failed to get hint:', error);
        } finally {
            setIsLoadingHint(false);
        }
    };

    const executeCode = async (isSubmit: boolean) => {
        if (!problem) return;
        const setStatus = isSubmit ? setIsSubmitting : setIsRunning;
        setStatus(true);
        try {
            if (isSubmit) {
                const confirmSubmit = window.confirm("Submit your final solution? You cannot change it after.");
                if (!confirmSubmit) {
                    setStatus(false);
                    return;
                }
                const res = await codeService.submitCode(sessionId, problem.title, code, language);
                setEvaluation(res);
                setIsSubmitted(true);
                setActiveTab('evaluation');
            } else {
                const res = await codeService.runCode(sessionId, problem.title, code, language, customTestCases);
                setOutput(res);
                setActiveTab('output');
            }
        } catch (err) {
            console.error('Code execution failed:', err);
            if (!isSubmit) {
                setOutput({
                    compiled: false,
                    compileError: 'A network or server error occurred during code execution.',
                    results: [],
                    passedCount: 0,
                    totalCount: 0,
                    allPassed: false
                });
            }
        } finally {
            setStatus(false);
        }
    };

    const handleRun = () => executeCode(false);
    const handleSubmit = () => executeCode(true);

    if (!problem || isFetching) {
        return (
            <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex items-center justify-center">
                <div className="text-center">
                    <Loader2 className="animate-spin text-violet mx-auto mb-2" size={24} />
                    <p className="text-text-tertiary text-sm">Loading problem context...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="flex-1 h-full flex flex-row bg-transparent overflow-hidden animate-in fade-in zoom-in-95 duration-500 relative">

            {/* Left Column: Problem Description */}
            <div
                style={{ width: `${leftWidth}%` }}
                className="flex flex-col bg-bg-surface border border-border rounded-xl shadow-sm overflow-hidden min-w-[250px]"
            >
                <div className="p-5 border-b border-border bg-bg-subtle shrink-0">
                    <h2 className="font-display font-bold text-lg text-text-primary mb-1">
                        {problem.title}
                    </h2>
                    <div className="flex items-center space-x-2">
                        <span className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${problem.difficulty === 'EASY' ? 'bg-success/10 text-success' :
                            problem.difficulty === 'MEDIUM' ? 'bg-warning/10 text-warning' :
                                'bg-danger/10 text-danger'
                            }`}>
                            {problem.difficulty}
                        </span>
                        <span className="text-text-tertiary text-[11px]">• DSA Round</span>
                    </div>
                </div>

                <div className="flex-1 overflow-y-auto p-5 custom-scrollbar">
                    <div className="prose prose-sm text-[14px] text-text-secondary max-w-none mb-6 leading-relaxed">
                        {problem.description}
                    </div>

                    {problem.constraints && problem.constraints.length > 0 && (
                        <div className="mb-6">
                            <h4 className="font-semibold text-xs text-text-primary uppercase tracking-wider mb-2">Constraints</h4>
                            <ul className="list-disc list-inside space-y-1">
                                {problem.constraints.map((c, i) => (
                                    <li key={i} className="text-sm text-text-secondary">{c}</li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {problem.examples && problem.examples.map((ex, i) => (
                        <div key={i} className="mb-4">
                            <h4 className="font-semibold text-xs text-text-secondary uppercase tracking-wider mb-2">Example {i + 1}</h4>
                            <div className="bg-bg-page p-3 border border-border rounded-lg font-mono text-xs">
                                <div className="mb-1"><span className="text-text-tertiary">Input:</span> <span className="text-text-primary">{ex.input}</span></div>
                                <div className="mb-1"><span className="text-text-tertiary">Output:</span> <span className="text-text-primary">{ex.output}</span></div>
                                {ex.explanation && (
                                    <div className="mt-2 text-text-tertiary italic">// {ex.explanation}</div>
                                )}
                            </div>
                        </div>
                    ))}

                    {hintText && (
                        <div className="mt-6 p-4 bg-violet/5 border border-violet/20 rounded-lg">
                            <h4 className="font-semibold text-xs text-violet uppercase tracking-wider mb-2 flex items-center">
                                <Lightbulb size={14} className="mr-1" /> Hint
                            </h4>
                            <p className="text-sm text-text-secondary">{hintText}</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Resize Divider */}
            <div
                onMouseDown={() => setIsResizing(true)}
                className={`w-1.5 h-full cursor-col-resize hover:bg-violet/30 transition-colors flex items-center justify-center group ${isResizing ? 'bg-violet/50' : ''}`}
            >
                <div className="w-0.5 h-8 bg-border group-hover:bg-violet/50 rounded-full" />
            </div>

            {/* Right Column: Editor & Output */}
            <div className="flex-1 flex flex-col bg-bg-surface border border-border rounded-xl shadow-sm overflow-hidden">
                {/* Editor Toolbar */}
                <div className="px-4 py-2 border-b border-border bg-bg-surface flex items-center justify-between shrink-0">
                    <div className="flex items-center space-x-3">
                        <div className="flex items-center space-x-2 px-2 py-1 bg-bg-subtle rounded-md border border-border">
                            <div className="w-2 h-2 rounded-full bg-violet animate-pulse" />
                            <span className="text-[11px] font-medium text-text-secondary uppercase tracking-tight">Technical Editor</span>
                        </div>
                        <select
                            value={language}
                            onChange={(e) => handleLanguageChange(e.target.value)}
                            className="text-xs bg-transparent border-none text-text-primary focus:outline-none focus:ring-0 cursor-pointer hover:text-violet transition-colors"
                        >
                            <option value="java">Java 22</option>
                            <option value="python">Python 3</option>
                            <option value="cpp">C++ 20</option>
                            <option value="javascript">Node.js</option>
                        </select>
                    </div>

                    <div className="flex items-center space-x-2">
                        <button
                            onClick={handleHint}
                            disabled={hintsUsed >= 3 || isSubmitted || isLoadingHint}
                            className="flex items-center space-x-1.5 px-3 py-1.5 bg-bg-subtle hover:bg-bg-overlay border border-border text-text-primary text-xs font-medium rounded-md transition-all disabled:opacity-50"
                        >
                            {isLoadingHint ? <Loader2 size={14} className="animate-spin" /> : <Lightbulb size={14} />}
                            <span>Hint ({3 - hintsUsed} left)</span>
                        </button>
                        <button
                            onClick={handleRun}
                            disabled={isRunning || isSubmitting || isSubmitted}
                            className="flex items-center space-x-1.5 px-3 py-1.5 bg-bg-subtle hover:bg-bg-overlay border border-border text-text-primary text-xs font-medium rounded-md transition-all disabled:opacity-50"
                        >
                            {isRunning ? <Loader2 size={14} className="animate-spin" /> : <Play size={14} />}
                            <span>Run Code</span>
                        </button>
                        <button
                            onClick={handleSubmit}
                            disabled={isRunning || isSubmitting || isSubmitted}
                            className={`flex items-center space-x-1.5 px-3 py-1.5 text-xs font-medium rounded-md transition-all shadow-sm disabled:opacity-50 ${isSubmitted ? 'bg-success text-white' : 'bg-violet hover:bg-violet-hover text-white'
                                }`}
                        >
                            {isSubmitting ? <Loader2 size={14} className="animate-spin" /> : isSubmitted ? <CheckCircle2 size={14} /> : <CheckCircle2 size={14} />}
                            <span>{isSubmitted ? 'Submitted' : 'Submit Solution'}</span>
                        </button>
                    </div>
                </div>

                {/* Editor Area */}
                <div className="flex-1 relative min-h-0 bg-[#0d0d0d]">
                    <Editor
                        height="100%"
                        language={language === 'java' ? 'java' : language === 'python' ? 'python' : 'javascript'}
                        theme="vs-dark"
                        value={code}
                        onChange={(val) => setCode(val || '')}
                        options={{
                            minimap: { enabled: false },
                            fontSize: 14,
                            fontFamily: 'JetBrains Mono, Menlo, Monaco, Consolas, Courier New, monospace',
                            padding: { top: 20, bottom: 20 },
                            scrollBeyondLastLine: false,
                            automaticLayout: true,
                            cursorBlinking: 'smooth',
                            smoothScrolling: true,
                            lineNumbersMinChars: 3,
                            glyphMargin: false,
                            folding: true,
                            renderLineHighlight: 'all',
                            lineHeight: 22,
                        }}
                    />
                </div>

                {/* Bottom Panel: Output & Test Cases */}
                <div className="flex-1 flex flex-col bg-[#161616] border-t border-border overflow-hidden min-h-[250px]">
                    {/* Tab Header */}
                    <div className="px-4 border-b border-white/5 bg-white/[0.02] flex items-center justify-between shrink-0">
                        <div className="flex items-center">
                            <button
                                onClick={() => setActiveTab('testcases')}
                                className={`px-4 py-3 text-[11px] font-bold uppercase tracking-widest transition-all border-b-2 ${activeTab === 'testcases' ? 'text-violet border-violet' : 'text-text-tertiary border-transparent hover:text-text-secondary'
                                    }`}
                            >
                                Test Cases
                            </button>
                            <button
                                onClick={() => setActiveTab('output')}
                                className={`px-4 py-3 text-[11px] font-bold uppercase tracking-widest transition-all border-b-2 ${activeTab === 'output' ? 'text-violet border-violet' : 'text-text-tertiary border-transparent hover:text-text-secondary'
                                    }`}
                            >
                                Console {output && <span className={`ml-1 w-1.5 h-1.5 rounded-full inline-block ${output.allPassed ? 'bg-success' : 'bg-warning'}`} />}
                            </button>
                            {isSubmitted && (
                                <button
                                    onClick={() => setActiveTab('evaluation')}
                                    className={`px-4 py-3 text-[11px] font-bold uppercase tracking-widest transition-all border-b-2 ${activeTab === 'evaluation' ? 'text-violet border-violet' : 'text-text-tertiary border-transparent hover:text-text-secondary'
                                        }`}
                                >
                                    Evaluation {evaluation && <span className="ml-1 w-1.5 h-1.5 rounded-full inline-block bg-violet" />}
                                </button>
                            )}
                        </div>
                        {activeTab === 'output' && output && (
                            <div className="flex items-center space-x-3 pr-2">
                                <span className="text-[10px] font-mono text-text-tertiary">{output.results?.[0]?.executionTimeMs || 0}ms</span>
                                {output.allPassed ? (
                                    <span className="text-[10px] font-bold text-success uppercase tracking-widest px-2 py-0.5 bg-success/10 rounded-full">Passed</span>
                                ) : (
                                    <span className="text-[10px] font-bold text-danger uppercase tracking-widest px-2 py-0.5 bg-danger/10 rounded-full">Failed</span>
                                )}
                            </div>
                        )}
                        {activeTab === 'evaluation' && evaluation && (
                            <div className="flex items-center space-x-3 pr-2">
                                <span className="text-[10px] font-bold text-violet uppercase tracking-widest px-2 py-0.5 bg-violet/10 rounded-full">Score: {evaluation.overallScore}/100</span>
                            </div>
                        )}
                    </div>

                    {/* Tab Content */}
                    <div className="flex-1 overflow-y-auto p-4 custom-scrollbar">
                        {activeTab === 'testcases' ? (
                            <div className="space-y-6">
                                {problem.examples && problem.examples.map((ex, i) => (
                                    <div key={i} className="animate-in slide-in-from-left-2 duration-300">
                                        <div className="flex items-center justify-between mb-2">
                                            <span className="text-[11px] font-bold text-text-tertiary uppercase">Case {i + 1}</span>
                                            {output && output.results?.[i] && (
                                                <span className={`text-[10px] font-bold ${output.results[i].passed ? 'text-success' : 'text-danger'}`}>
                                                    {output.results[i].passed ? '✓ Passed' : '✗ Failed'}
                                                </span>
                                            )}
                                        </div>
                                        <div className="space-y-2">
                                            <div className="bg-white/5 border border-white/10 rounded-lg p-3">
                                                <div className="text-[10px] text-text-tertiary mb-1 uppercase">Input</div>
                                                <div className="font-mono text-xs text-white/90">{ex.input}</div>
                                            </div>
                                            {output && output.results?.[i] && (
                                                <div className="bg-white/[0.02] border border-white/5 rounded-lg p-3">
                                                    <div className="text-[10px] text-text-tertiary mb-1 uppercase">Actual Output</div>
                                                    <div className={`font-mono text-xs ${output.results[i].passed ? 'text-success/90' : 'text-danger/90'}`}>
                                                        {output.results[i].actualOutput}
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))}

                                {/* Custom Test Case Input */}
                                <div className="mt-8 pt-6 border-t border-white/5">
                                    <h4 className="text-[11px] font-bold text-text-secondary uppercase tracking-widest mb-3">Custom Test Input</h4>
                                    <textarea
                                        value={customTestCases}
                                        onChange={(e) => setCustomTestCases(e.target.value)}
                                        placeholder="Paste your custom input here..."
                                        className="w-full h-32 bg-white/5 border border-white/10 rounded-xl p-4 font-mono text-xs text-white focus:outline-none focus:border-violet/50 transition-colors placeholder:text-text-tertiary/50"
                                    />
                                    <p className="mt-2 text-[10px] text-text-tertiary italic">
                                        // This input will be used for the first test case during "Run Code".
                                    </p>
                                </div>
                            </div>
                        ) : activeTab === 'evaluation' && evaluation ? (
                            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-2 duration-500">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <div className="bg-white/5 border border-white/10 rounded-xl p-4">
                                        <div className="text-[10px] text-text-tertiary mb-1 uppercase font-bold tracking-widest">Logic Correctness</div>
                                        <div className="text-xl font-display font-bold text-white">{evaluation.correctness}%</div>
                                    </div>
                                    <div className="bg-white/5 border border-white/10 rounded-xl p-4">
                                        <div className="text-[10px] text-text-tertiary mb-1 uppercase font-bold tracking-widest">Code Quality</div>
                                        <div className="text-xl font-display font-bold text-white">{evaluation.codeQuality}%</div>
                                    </div>
                                    <div className="bg-white/5 border border-white/10 rounded-xl p-4">
                                        <div className="text-[10px] text-text-tertiary mb-1 uppercase font-bold tracking-widest">Time Complexity</div>
                                        <div className="text-sm font-mono text-violet-light">{evaluation.timeComplexity}</div>
                                    </div>
                                    <div className="bg-white/5 border border-white/10 rounded-xl p-4">
                                        <div className="text-[10px] text-text-tertiary mb-1 uppercase font-bold tracking-widest">Space Complexity</div>
                                        <div className="text-sm font-mono text-violet-light">{evaluation.spaceComplexity}</div>
                                    </div>
                                </div>
                                <div className="bg-violet/5 border border-violet/20 rounded-xl p-4">
                                    <div className="text-[10px] text-violet-light mb-2 uppercase font-bold tracking-widest">Interviewer Feedback</div>
                                    <p className="text-[13px] text-text-secondary leading-relaxed italic">"{evaluation.feedback}"</p>
                                </div>
                                <div className="space-y-2">
                                    <div className="text-[10px] text-text-tertiary uppercase font-bold tracking-widest">Points for Improvement</div>
                                    {evaluation.improvements.map((imp: string, i: number) => (
                                        <div key={i} className="flex items-start space-x-2 text-[12px] text-text-secondary">
                                            <div className="w-1.5 h-1.5 rounded-full bg-violet mt-1.5 shrink-0" />
                                            <span>{imp}</span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        ) : (
                            <pre className="font-mono text-[13px] text-white/95 whitespace-pre-wrap leading-relaxed animate-in fade-in duration-300">
                                {output?.compileError || (output?.results?.[0]?.actualOutput ? (
                                    <div className="space-y-4">
                                        {output.results.map((res, i) => (
                                            <div key={i} className="pb-4 border-b border-white/5 last:border-0">
                                                <div className="flex items-center space-x-2 mb-1">
                                                    <span className="text-[10px] text-text-tertiary">#CASE {i + 1}: </span>
                                                    <span className={res.passed ? 'text-success' : 'text-danger'}>{res.passed ? 'PASSED' : 'FAILED'}</span>
                                                </div>
                                                <div className="grid grid-cols-1 gap-2">
                                                    <div><span className="text-text-tertiary opacity-50 text-[10px] mr-2">OUT:</span> <span className="text-white">{res.actualOutput}</span></div>
                                                    {!res.passed && <div><span className="text-text-tertiary opacity-50 text-[10px] mr-2">EXP:</span> <span className="text-text-tertiary">{res.expectedOutput}</span></div>}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                ) : isSubmitted ? "Code submitted successfully. View detailed evaluation in the 'Evaluation' tab." : "Click 'Run Code' to see execution output.")}
                            </pre>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
