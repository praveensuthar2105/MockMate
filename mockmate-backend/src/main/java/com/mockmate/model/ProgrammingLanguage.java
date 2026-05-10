package com.mockmate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum ProgrammingLanguage {
    JAVA, PYTHON, JAVASCRIPT, CPP, CSHARP, GO, RUST;

    @JsonCreator
    public static ProgrammingLanguage fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Language cannot be null or blank");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "JAVA" -> JAVA;
            case "PYTHON", "PY" -> PYTHON;
            case "JAVASCRIPT", "JS", "NODE", "NODEJS" -> JAVASCRIPT;
            case "CPP", "C++" -> CPP;
            case "CSHARP", "C#", "DOTNET" -> CSHARP;
            case "GO", "GOLANG" -> GO;
            case "RUST" -> RUST;
            default -> throw new IllegalArgumentException("Unsupported programming language: " + value);
        };
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
