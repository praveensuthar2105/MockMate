import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextLine() ? Integer.parseInt(sc.nextLine().trim()) : 0;

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(n);
        System.out.println(result);
    }
}

{{USER_CODE}}
