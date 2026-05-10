import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        if (sc.hasNextLine()) sc.nextLine(); // consume newline
        String s = sc.hasNextLine() ? sc.nextLine().trim() : "";

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(n, s);
        System.out.println(result);
    }
}

{{USER_CODE}}
