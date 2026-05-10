import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String line = sc.hasNextLine() ? sc.nextLine().trim() : "";
        String[] words = line.isEmpty() ? new String[0] : line.split("\\s+");

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(words);
        System.out.println(result);
    }
}

{{USER_CODE}}
