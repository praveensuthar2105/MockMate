import java.util.*;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
        String[] parts = sc.nextLine().trim().split("\\s+");
        int[] nums = new int[parts.length];
            try {
                for (int i = 0; i < parts.length; i++) {
                    nums[i] = Integer.parseInt(parts[i]);
                }
            } catch (Exception e) {
                System.err.println("Invalid array int input");
                System.exit(1);
            }
            int target = 0;
            try {
                target = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.err.println("Invalid target int input");
                System.exit(1);
            }
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(
            nums, target);
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
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
