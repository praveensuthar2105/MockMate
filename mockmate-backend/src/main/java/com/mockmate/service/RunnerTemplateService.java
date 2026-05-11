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
            Map.entry("matrix+int_array", "runner_matrix+int_array"),
            Map.entry("matrix, int_array", "runner_matrix+int_array"),
            Map.entry("matrix+matrix", "runner_matrix+matrix"),
            Map.entry("matrix, matrix", "runner_matrix+matrix"),
            Map.entry("binary_tree", "runner_binary_tree"),
            Map.entry("linked_list", "runner_linked_list"),
            Map.entry("int_array+int_array+int", "runner_int_array+int_array+int"),
            Map.entry("int_array, int_array, int", "runner_int_array+int_array+int")
    );

    /**
     * Builds a complete Java Main.java by combining the runner template with user code.
     */
    public String buildJavaMain(String inputFormat, String methodSignature, String userCode) {
        if (methodSignature == null || userCode == null) throw new IllegalArgumentException("methodSignature and userCode cannot be null");
        
        String finalFormat = inputFormat;
        // Smart Recovery: If the format is int_array but the code expects a matrix, switch to matrix template
        if (("int_array+int".equals(inputFormat) || "int_array".equals(inputFormat)) && 
            (userCode.contains("int[][]") || userCode.contains("Integer[][]"))) {
            log.info("Smart Recovery: Switching template from {} to matrix equivalent based on user code signature", inputFormat);
            finalFormat = inputFormat.replace("int_array", "matrix");
        }

        String template = loadTemplate("runners/java/", finalFormat, ".java");
        return template
                .replace("{{methodSignature}}", methodSignature)
                .replace("{{USER_CODE}}", userCode);
    }

    /**
     * Builds a complete Python solution.py by combining the runner template with user code.
     */
    public String buildPythonRunner(String inputFormat, String methodSignature, String userCode) {
        if (methodSignature == null || userCode == null) throw new IllegalArgumentException("methodSignature and userCode cannot be null");
        
        String finalFormat = inputFormat;
        // Python Smart Recovery: If the code uses List[List[int]], switch to matrix template
        if (("int_array+int".equals(inputFormat) || "int_array".equals(inputFormat)) && 
            (userCode.contains("List[List[int]]") || userCode.contains("List[List]"))) {
            log.info("Smart Recovery (Python): Switching template to matrix equivalent");
            finalFormat = inputFormat.replace("int_array", "matrix");
        }

        String template = loadTemplate("runners/python/", finalFormat, ".py");
        return template
                .replace("{{methodSignature}}", methodSignature)
                .replace("{{USER_CODE}}", userCode);
    }

    private String loadTemplate(String prefix, String inputFormat, String suffix) {
        if (inputFormat == null) throw new IllegalArgumentException("inputFormat cannot be null");
        String normalizedFormat = inputFormat.toLowerCase().trim();
        String templateName = FORMAT_TO_TEMPLATE.get(normalizedFormat);

        if (templateName == null) {
            log.warn("Unknown inputFormat '{}' — falling back to int_array+int", inputFormat);
            templateName = "runner_int_array+int";
        }

        String path = prefix + templateName + suffix;

        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (java.io.InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Failed to load runner template from {}: {}", path, e.getMessage());
            throw new RuntimeException("Runner template not found: " + path);
        }
    }
}
