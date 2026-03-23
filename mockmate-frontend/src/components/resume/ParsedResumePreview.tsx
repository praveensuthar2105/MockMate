import { useProfileStore } from '../../store/profileStore';

export function ParsedResumePreview() {
    const { resume } = useProfileStore();

    if (!resume) return null;

    let parsedData: any = {};
    try {
        parsedData = JSON.parse(resume.parsedJson);
    } catch (e) {
        // Fallback or handle invalid JSON
        parsedData = {
            summary: resume.summary,
            skills: resume.skills ? resume.skills.split(',') : [],
            projects: [],
            education: "Not specified"
        };
    }

    const { summary, skills, projects, education } = parsedData;

    return (
        <div className="bg-bg-surface border border-border rounded-xl shadow-sm p-8 mt-6">
            <h3 className="text-lg font-semibold text-text-primary mb-4 border-b border-border pb-2">
                Parsed Resume Preview
            </h3>

            <div className="space-y-6">
                {summary && (
                    <div>
                        <h4 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-2">Summary</h4>
                        <p className="text-sm text-text-primary leading-relaxed">{summary}</p>
                    </div>
                )}

                {skills && Array.isArray(skills) && skills.length > 0 && (
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

                {projects && Array.isArray(projects) && projects.length > 0 && (
                    <div>
                        <h4 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-2">Projects</h4>
                        <div className="grid gap-4 md:grid-cols-2">
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
    );
}
