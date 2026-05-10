import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Parse line 1: string array
        String line1 = sc.hasNextLine() ? sc.nextLine().trim() : "";
        String[] words = line1.isEmpty() ? new String[0] : line1.split("\\s+");

        // Parse line 2: int
        int k = sc.hasNextLine() ? Integer.parseInt(sc.nextLine().trim()) : 0;

        // Call user solution
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(words, k);

        // Print output
        printResult(result);
    }

    static void printResult(Object result) {
        if (result instanceof String[]) {
            String[] arr = (String[]) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(" ");
                sb.append(arr[i]);
            }
            System.out.println(sb.toString());
        } else if (result instanceof List) {
            List<?> list = (List<?>) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0)
                    sb.append(" ");
                sb.append(list.get(i));
            }
            System.out.println(sb.toString());
        } else if (result instanceof int[]) {
            int[] arr = (int[]) result;
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
