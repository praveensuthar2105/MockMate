package com.mockmate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class RunnerTemplateService {

    private static final Map<String, String> FORMAT_TO_TEMPLATE = Map.ofEntries(
            Map.entry("int_array", "runner_int_array"),
            Map.entry("int", "runner_int"),
            Map.entry("string", "runner_string"),
            Map.entry("int_array+int", "runner_int_array+int"),
            Map.entry("int_array, int", "runner_int_array+int"),
            Map.entry("string+int", "runner_string+int"),
            Map.entry("string, int", "runner_string+int"),
            Map.entry("int+string", "runner_int+string"),
            Map.entry("int, string", "runner_int+string"),
            Map.entry("string+string", "runner_string+string"),
            Map.entry("string, string", "runner_string+string"),
            Map.entry("int+int", "runner_int+int"),
            Map.entry("int, int", "runner_int+int"),
            Map.entry("int_array+int_array", "runner_int_array+int_array"),
            Map.entry("int_array, int_array", "runner_int_array+int_array"),
            Map.entry("string_array", "runner_string_array"),
            Map.entry("string_array+int", "runner_string_array+int"),
            Map.entry("string_array, int", "runner_string_array+int"),
            Map.entry("string_array+string_array", "runner_string_array+string_array"),
            Map.entry("string_array, string_array", "runner_string_array+string_array"),
            Map.entry("matrix", "runner_matrix"),
            Map.entry("matrix+int", "runner_matrix+int"),
            Map.entry("matrix, int", "runner_matrix+int"),
            Map.entry("matrix+matrix", "runner_matrix+matrix"),
            Map.entry("matrix, matrix", "runner_matrix+matrix"),
            Map.entry("binary_tree", "runner_binary_tree"),
            Map.entry("linked_list", "runner_linked_list")
    );

    /**
     * Builds a complete Java Main.java by combining the runner template with user code.
     */
    public String buildJavaMain(String inputFormat, String methodSignature, String userCode) {
        String template = loadTemplate("runners/java/", inputFormat, ".java");
        return template
                .replace("{{methodSignature}}", methodSignature)
                .replace("{{USER_CODE}}", userCode);
    }

    /**
     * Builds a complete Python solution.py by combining the runner template with user code.
     */
    public String buildPythonRunner(String inputFormat, String methodSignature, String userCode) {
        String template = loadTemplate("runners/python/", inputFormat, ".py");
        return template
                .replace("{{methodSignature}}", methodSignature)
                .replace("{{USER_CODE}}", userCode);
    }

    private String loadTemplate(String prefix, String inputFormat, String suffix) {
        String normalizedFormat = inputFormat.toLowerCase().trim();
        String templateName = FORMAT_TO_TEMPLATE.get(normalizedFormat);

        if (templateName == null) {
            log.warn("Unknown inputFormat '{}' — falling back to int_array+int", inputFormat);
            templateName = "runner_int_array+int";
        }

        String path = prefix + templateName + suffix;

        try {
            ClassPathResource resource = new ClassPathResource(path);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load runner template from {}: {}", path, e.getMessage());
            throw new RuntimeException("Runner template not found: " + path);
        }
    }
}
