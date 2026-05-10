import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.hasNextInt() ? sc.nextInt() : 0;
        int b = sc.hasNextInt() ? sc.nextInt() : 0;

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(a, b);
        System.out.println(result);
    }
}

{{USER_CODE}}
