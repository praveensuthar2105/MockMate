package com.mockmate.service.runner;

public enum ProblemType {
    INT_ARRAY("int_array", "int"), // e.g., max subarray, missing number
    INT_ARRAY_ARRAY("int_array", "int_array"), // e.g., sort array
    STRING("string", "int"), // e.g., length of longest substring
    STRING_STRING("string", "string"), // e.g., reverse string (if returning string)
    STRING_BOOLEAN("string", "boolean"), // e.g., valid parentheses
    INT_ARRAY_INT("int_array, int", "int_array"), // e.g., two sum
    INT_ARRAY_INT_INT("int_array, int", "int"), // e.g., search in rotated sorted array
    INT_ARRAY_INT_ARRAY("int_array, int_array", "int_array"), // e.g., merge sorted array
    INT_ARRAY_INT_ARRAY_INT("int_array, int_array", "int"), // e.g., median of two sorted arrays
    STRING_ARRAY("string_array", "string_array"), // e.g., group anagrams
    STRING_ARRAY_INT("string_array, int", "string_array"), // e.g., top k frequent words
    MATRIX("matrix", "int"), // e.g., number of islands
    MATRIX_INT("matrix, int", "int"), // e.g., min cost path with target
    MATRIX_MATRIX("matrix", "matrix"), // e.g., rotate image
    LINKED_LIST("linked_list", "linked_list"), // e.g., reverse linked list
    BINARY_TREE("binary_tree", "int"), // e.g., max depth of binary tree
    BINARY_TREE_TREE("binary_tree", "binary_tree"), // e.g., invert binary tree
    INT("int", "int"), // e.g., reverse integer
    INT_BOOLEAN("int", "boolean"), // e.g., palindrome number
    STRING_INT("string, int", "string"); // e.g., longest palindrome substring (if format uses int)

    private final String inputFormat;
    private final String outputFormat;

    ProblemType(String inputFormat, String outputFormat) {
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public static ProblemType from(String inputFormat, String outputFormat) {
        for (ProblemType type : values()) {
            if (type.inputFormat.equals(inputFormat) && type.outputFormat.equals(outputFormat)) {
                return type;
            }
        }
        return null;
    }
}
