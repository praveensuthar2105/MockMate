import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.hasNextLine() ? sc.nextLine().trim() : "";
        int n = sc.hasNextInt() ? sc.nextInt() : 0;

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(s, n);
        System.out.println(result);
    }
}

{{USER_CODE}}
