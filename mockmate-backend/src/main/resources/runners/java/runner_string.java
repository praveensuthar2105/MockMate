import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.hasNextLine() ? sc.nextLine().trim() : "";

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(input);
        System.out.println(result);
    }
}

{{USER_CODE}}
