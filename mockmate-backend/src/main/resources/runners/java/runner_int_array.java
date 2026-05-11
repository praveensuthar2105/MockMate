import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            String line = sc.hasNextLine() ? sc.nextLine() : "";
            int[] nums = parseArray(line);

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(nums);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int[] parseArray(String line) {
        if (line.contains("=")) line = line.substring(line.indexOf("=") + 1);
        line = line.replace("[", "").replace("]", "").replace(",", " ").trim();
        if (line.isEmpty()) return new int[0];
        String[] parts = line.split("\\s+");
        int[] res = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            res[i] = Integer.parseInt(parts[i]);
        }
        return res;
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
