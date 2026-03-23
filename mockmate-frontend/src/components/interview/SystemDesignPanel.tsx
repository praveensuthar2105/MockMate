import { PenTool } from 'lucide-react';
import { useState } from 'react';

export function SystemDesignPanel() {
    const [Excalidraw] = useState<any>(null);

    return (
        <div className="flex-1 h-full bg-bg-surface border border-border rounded-xl shadow-sm flex flex-col overflow-hidden">
            <div className="p-4 border-b border-border bg-bg-subtle/30 flex items-center justify-between">
                <div>
                    <h2 className="font-display font-semibold text-lg text-text-primary flex items-center gap-2">
                        <PenTool className="text-violet" size={20} />
                        Architecture Whiteboard
                    </h2>
                    <p className="text-text-secondary text-[11px] mt-0.5">
                        Design scalable systems and map out components.
                    </p>
                </div>
            </div>

            <div className="flex-1 relative overflow-hidden bg-[#f8f9fa]">
                {Excalidraw ? (
                    <Excalidraw
                        theme="light"
                        UIOptions={{
                            canvasActions: {
                                loadScene: false,
                                saveAsImage: true,
                                export: false,
                                clearCanvas: true,
                                toggleTheme: false,
                                changeViewBackgroundColor: true,
                            }
                        }}
                    />
                ) : (
                    <div className="flex items-center justify-center h-full text-text-tertiary animate-pulse">
                        Loading whiteboard environment...
                    </div>
                )}
            </div>
        </div>
    );
}
