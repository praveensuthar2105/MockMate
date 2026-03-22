import { RegisterForm } from '../components/auth/RegisterForm';

export default function RegisterPage() {
    return (
        <div
            className="min-h-screen bg-bg-page flex items-center flex-col justify-center p-4 relative"
            style={{
                backgroundImage: 'radial-gradient(var(--border) 1px, transparent 1px)',
                backgroundSize: '24px 24px'
            }}
        >
            <div className="w-full max-w-md bg-bg-surface rounded-xl shadow-lg p-10 relative z-10 border border-border">

                <div className="text-center mb-8 flex flex-col items-center">
                    <div className="w-10 h-10 rounded-full bg-violet flex items-center justify-center mb-3">
                        <span className="font-display font-semibold text-white text-xl">M</span>
                    </div>
                    <h1 className="font-display text-[22px] text-text-primary font-semibold tracking-tight">
                        Create your account
                    </h1>
                    <p className="text-text-secondary text-sm mt-1">
                        Join MockMate to start practicing.
                    </p>
                </div>

                <RegisterForm />

            </div>
        </div>
    );
}
