import java.util.*;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
        String s = sc.nextLine().trim();
        int n = Integer.parseInt(sc.nextLine().trim());
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(s, n);
        if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
    } else {
            System.out.println(result);
            }
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
