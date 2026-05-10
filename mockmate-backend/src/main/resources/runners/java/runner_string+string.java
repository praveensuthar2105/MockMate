import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s1 = sc.hasNextLine() ? sc.nextLine().trim() : "";
        String s2 = sc.hasNextLine() ? sc.nextLine().trim() : "";

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(s1, s2);
        System.out.println(result);
    }
}

{{USER_CODE}}
