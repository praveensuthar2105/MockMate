import java.util.*;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
        String[] parts = sc.nextLine().trim().split("\\s+");
        int[] nums = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                try {
                    nums[i] = Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid integer token: " + parts[i] + " at index " + i);
                    throw new IllegalArgumentException(e);
                }
            }
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(nums);
        printResult(result);
        } finally {
            sc.close();
        }
    }

    static void printResult(Object result) {
        if (result instanceof int[]) {
            int[] arr = (int[]) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(arr[i]);
            }
            System.out.println(sb.toString().trim());
        } else if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
        } else if (result != null && result.getClass().isArray()) {
            if (result instanceof Object[]) { System.out.println(java.util.Arrays.toString((Object[]) result)); } else if (result instanceof double[]) { System.out.println(java.util.Arrays.toString((double[]) result)); } else if (result instanceof char[]) { System.out.println(java.util.Arrays.toString((char[]) result)); } else if (result instanceof long[]) { System.out.println(java.util.Arrays.toString((long[]) result)); } else if (result instanceof float[]) { System.out.println(java.util.Arrays.toString((float[]) result)); } else if (result != null && result.getClass().isArray()) {
            System.out.println(java.util.Arrays.toString((Object[]) result));
        } else {
            System.out.println(result);
        }
        } else if (result != null && result.getClass().isArray()) {
            System.out.println(java.util.Arrays.toString((Object[]) result));
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
