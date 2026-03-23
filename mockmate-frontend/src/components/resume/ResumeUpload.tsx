import { useRef } from 'react';
import { Upload, FileText, CheckCircle } from 'lucide-react';
import { useProfileStore } from '../../store/profileStore';
import { ErrorState } from '../shared/ErrorState';

export function ResumeUpload() {
    const { resume, loading, error, uploadResume, fetchResume } = useProfileStore();
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            await uploadResume(file);
        }
    };

    return (
        <div className="bg-bg-surface border border-border rounded-xl shadow-sm p-8">
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h3 className="text-lg font-semibold text-text-primary flex items-center space-x-2">
                        <FileText size={20} className="text-violet" />
                        <span>Resume Management</span>
                    </h3>
                    <p className="text-sm text-text-tertiary mt-1">Upload your PDF resume. Our AI uses this to personalize your interview questions.</p>
                </div>
            </div>

            {error && (
                <div className="mb-4">
                    <ErrorState message={error} onRetry={() => fetchResume()} />
                </div>
            )}

            <div
                onClick={() => fileInputRef.current?.click()}
                className={`border-2 border-dashed rounded-xl p-10 flex flex-col items-center justify-center transition-all cursor-pointer ${loading
                    ? 'bg-bg-subtle border-border opacity-50 cursor-not-allowed'
                    : 'bg-bg-subtle/50 border-border hover:border-violet/50 hover:bg-violet/5'
                    }`}
            >
                <input
                    type="file"
                    ref={fileInputRef}
                    onChange={handleFileChange}
                    className="hidden"
                    accept=".pdf"
                    disabled={loading}
                />

                {loading ? (
                    <div className="animate-spin w-8 h-8 border-4 border-violet border-t-transparent rounded-full mb-3" />
                ) : (
                    <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center shadow-sm mb-4">
                        <Upload className="text-violet" size={24} />
                    </div>
                )}

                <h4 className="text-text-primary font-medium mb-1">
                    {resume ? 'Update your resume' : 'Upload your resume'}
                </h4>
                <p className="text-text-tertiary text-sm">Supported formats: PDF (Max 5MB)</p>
            </div>

            {resume && (
                <div className="mt-6 flex items-center p-4 bg-success-light/30 border border-success-light rounded-lg">
                    <div className="w-10 h-10 bg-success-light rounded-lg flex items-center justify-center text-success mr-4">
                        <FileText size={20} />
                    </div>
                    <div className="flex-1">
                        <p className="text-sm font-medium text-text-primary">Resume successfully analyzed</p>
                        <p className="text-xs text-text-tertiary">Your profile is now ready for mock interviews.</p>
                    </div>
                    <div className="text-success">
                        <CheckCircle size={20} />
                    </div>
                </div>
            )}
        </div>
    );
}
