import { useEffect, useState } from 'react';
import { FileText, ShieldCheck } from 'lucide-react';
import { resumeService } from '../../services/resumeService';
import type { ResumeResponse } from '../../services/resumeService';

export function ResumePanel() {
    const [resume, setResume] = useState<ResumeResponse | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchResume = async () => {
            try {
                const res = await resumeService.getLatest();
                setResume(res);
            } catch (error) {
                console.error('Failed to fetch resume:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchResume();
    }, []);

    let parsedData: any = {};
    if (resume && resume.parsedJson) {
        try {
            parsedData = JSON.parse(resume.parsedJson);
        } catch (e) {
            console.error("Failed to parse resume json", e);
        }
    }

    const skills = Array.isArray(parsedData.skills) ? parsedData.skills : (resume?.skills ? resume.skills.split(',') : []);
    const projects = Array.isArray(parsedData.projects) ? parsedData.projects : [];
    const education = parsedData.education || "Not specified";

    if (loading) {
        return (
            <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex items-center justify-center">
                <div className="animate-pulse flex flex-col items-center">
                    <div className="w-12 h-12 bg-bg-subtle rounded-full mb-4"></div>
                    <div className="h-4 bg-bg-subtle rounded w-32"></div>
                </div>
            </div>
        );
    }

    return (
        <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex flex-col overflow-hidden">
            <div className="p-6 border-b border-border bg-bg-subtle/30">
                <h2 className="font-display font-semibold text-xl text-text-primary flex items-center gap-2">
                    <FileText className="text-violet" size={24} />
                    Your Resume
                </h2>
                <p className="text-text-secondary text-xs mt-1">
                    AI-driven analysis of your professional background and project impact.
                </p>
            </div>

            <div className="flex-1 p-8 flex flex-col lg:flex-row gap-8 overflow-y-auto custom-scrollbar">
                {/* Visual Resume Representation */}
                <div className="flex-1 bg-white border border-border rounded-xl shadow-inner p-8 relative group overflow-y-auto">
                    <div className="absolute top-4 right-4 text-[10px] font-bold text-success bg-success-light px-2 py-0.5 rounded-full flex items-center gap-1">
                        <ShieldCheck size={10} /> Verified
                    </div>

                    <div className="space-y-6">
                        {skills.length > 0 && (
                            <div>
                                <h4 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-2">Skills</h4>
                                <div className="flex flex-wrap gap-2">
                                    {skills.map((skill: string, idx: number) => (
                                        <span key={idx} className="bg-violet-light/30 text-violet px-3 py-1 rounded-full text-xs font-medium">
                                            {skill}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        )}

                        {projects.length > 0 && (
                            <div>
                                <h4 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-3">Projects</h4>
                                <div className="space-y-4">
                                    {projects.map((project: any, idx: number) => (
                                        <div key={idx} className="border border-border rounded-lg p-4 bg-bg-subtle/50">
                                            <h5 className="font-bold text-text-primary text-sm mb-1">{project.name}</h5>
                                            {project.tech && Array.isArray(project.tech) && (
                                                <div className="flex flex-wrap gap-1 mb-2">
                                                    {project.tech.map((tech: string, tidx: number) => (
                                                        <span key={tidx} className="bg-border text-text-secondary px-2 py-0.5 rounded text-[10px] font-medium">
                                                            {tech}
                                                        </span>
                                                    ))}
                                                </div>
                                            )}
                                            <p className="text-xs text-text-secondary line-clamp-2">{project.desc}</p>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {education && (
                            <div>
                                <h4 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-2">Education</h4>
                                <p className="text-sm text-text-primary">{education}</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}