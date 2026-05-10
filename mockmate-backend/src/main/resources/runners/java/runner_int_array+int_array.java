import java.util.*;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
        String line1 = sc.nextLine().trim();
            String[] p1 = line1.isEmpty() ? new String[0] : line1.split("\\s+");
            int[] nums1 = new int[p1.length];
            for (int i = 0; i < p1.length; i++) {
                nums1[i] = Integer.parseInt(p1[i]);
            }
        String line2 = sc.nextLine().trim();
            String[] p2 = line2.isEmpty() ? new String[0] : line2.split("\\s+");
            int[] nums2 = new int[p2.length];
            for (int i = 0; i < p2.length; i++) {
                nums2[i] = Integer.parseInt(p2[i]);
            }
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(
            nums1, nums2);
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
