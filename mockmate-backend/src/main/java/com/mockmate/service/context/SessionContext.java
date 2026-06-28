package com.mockmate.service.context;

public class SessionContext {
    private static final ThreadLocal<Long> currentSessionId = new ThreadLocal<>();

    public static void setCurrentSessionId(Long id) {
        currentSessionId.set(id);
    }

    public static Long getCurrentSessionId() {
        return currentSessionId.get();
    }

    public static void clear() {
        currentSessionId.remove();
    }
}
