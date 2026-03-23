import { Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { Mail, Briefcase, Award, CheckCircle, AlertCircle } from 'lucide-react';
import { LoadingSkeleton } from '../components/shared/LoadingSkeleton';
import { ResumeUpload } from '../components/resume/ResumeUpload';
import { ParsedResumePreview } from '../components/resume/ParsedResumePreview';

export default function ProfilePage() {
    const { user } = useAuthStore();

    if (!user) return <LoadingSkeleton />;

    return (
        <div className="max-w-3xl mx-auto animate-in fade-in duration-500 mt-4">
            <div className="mb-8">
                <h1 className="text-3xl font-display font-semibold text-text-primary tracking-tight">Your Profile</h1>
                <p className="text-text-secondary mt-1">Manage your professional identity and resume for AI-powered interviews.</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-6">
                    {/* Basic Info Card */}
                    <div className="bg-bg-surface border border-border rounded-xl shadow-sm overflow-hidden">
                        <div className="h-24 bg-gradient-to-r from-violet-light to-violet/10 border-b border-border relative">
                            <div className="absolute -bottom-10 left-8">
                                <div className="w-20 h-20 bg-violet border-4 border-bg-surface rounded-full flex items-center justify-center shadow-sm">
                                    <span className="font-display font-semibold text-3xl text-white">
                                        {user?.name?.charAt(0) || 'U'}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="pt-14 px-8 pb-8 flex flex-col space-y-6">
                            <div>
                                <h2 className="text-xl font-bold text-text-primary">{user?.name}</h2>
                                <div className="flex items-center text-text-secondary mt-1 space-x-2">
                                    <Mail size={16} />
                                    <span className="text-sm">{user?.email}</span>
                                </div>
                            </div>

                            <div className="border-t border-border pt-6 grid grid-cols-2 gap-6">
                                <div className="space-y-1">
                                    <span className="text-xs font-semibold text-text-tertiary uppercase tracking-wider flex items-center space-x-1.5"><Briefcase size={14} /> <span>Experience Level</span></span>
                                    <p className="text-text-primary font-medium">{user?.experienceLevel?.replace('_', ' ') || 'FRESHER'}</p>
                                </div>
                                <div className="space-y-1">
                                    <span className="text-xs font-semibold text-text-tertiary uppercase tracking-wider flex items-center space-x-1.5"><Award size={14} /> <span>Profile Status</span></span>
                                    <div className="flex items-center space-x-1.5">
                                        {user?.profileComplete ? (
                                            <>
                                                <CheckCircle size={14} className="text-success" />
                                                <p className="text-text-primary font-medium text-success">Complete</p>
                                            </>
                                        ) : (
                                            <>
                                                <AlertCircle size={14} className="text-warning" />
                                                <p className="text-text-primary font-medium text-warning">Action Required</p>
                                            </>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <ResumeUpload />
                    <ParsedResumePreview />
                </div>

                <div className="space-y-6">
                    <div className="bg-bg-surface border border-border rounded-xl shadow-sm p-6">
                        <h3 className="font-semibold text-text-primary mb-4">Quick Links</h3>
                        <div className="space-y-3">
                            <Link to="/dashboard" className="block w-full px-4 py-2 text-left bg-bg-subtle hover:bg-bg-overlay border border-border rounded-lg text-sm font-medium transition-colors text-text-primary">
                                Dashboard
                            </Link>
                            <Link to="/history" className="block w-full px-4 py-2 text-left bg-bg-subtle hover:bg-bg-overlay border border-border rounded-lg text-sm font-medium transition-colors text-text-primary">
                                History
                            </Link>
                        </div>
                    </div>

                    <div className="bg-bg-surface border border-border rounded-xl shadow-sm p-6">
                        <h3 className="font-semibold text-text-primary mb-4">Interview Stats</h3>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center">
                                <span className="text-xs text-text-tertiary uppercase font-bold">Resumes Scanned</span>
                                <span className="text-sm font-bold text-text-primary">1</span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-xs text-text-tertiary uppercase font-bold">Skills Extracted</span>
                                <span className="text-sm font-bold text-text-primary">12</span>
                            </div>
                            <div className="w-full bg-bg-subtle rounded-full h-1.5 mt-2">
                                <div className="bg-violet h-1.5 rounded-full w-3/4" />
                            </div>
                            <p className="text-[10px] text-text-tertiary text-center italic">Profile 75% complete</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
