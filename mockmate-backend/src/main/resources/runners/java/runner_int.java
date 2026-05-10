import java.util.*;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
        int n = 0;
            try {
                n = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.err.println("Invalid int input");
                System.exit(1);
            }
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(n);
        if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
