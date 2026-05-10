import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String line = sc.hasNextLine() ? sc.nextLine().trim() : "";
        String[] parts = line.isEmpty() ? new String[0] : line.split("\\s+");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            nums[i] = Integer.parseInt(parts[i]);
        }

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(nums);
        printResult(result);
    }

    static void printResult(Object result) {
        if (result instanceof int[]) {
            int[] arr = (int[]) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(" ");
                sb.append(arr[i]);
            }
            System.out.println(sb.toString());
        } else if (result instanceof boolean[]) {
            boolean[] arr = (boolean[]) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(" ");
                sb.append(arr[i]);
            }
            System.out.println(sb.toString());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
