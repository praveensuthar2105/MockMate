export function LoadingSkeleton() {
    return (
        <div className="w-full space-y-6">
            <div className="flex justify-between items-center">
                <div className="h-8 w-48 bg-bg-subtle rounded-md animate-pulse"></div>
                <div className="h-10 w-32 bg-bg-subtle rounded-md animate-pulse"></div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="h-32 bg-bg-subtle rounded-xl animate-pulse"></div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="h-80 bg-bg-subtle rounded-xl animate-pulse"></div>
                <div className="h-80 bg-bg-subtle rounded-xl animate-pulse"></div>
            </div>
        </div>
    );
}
