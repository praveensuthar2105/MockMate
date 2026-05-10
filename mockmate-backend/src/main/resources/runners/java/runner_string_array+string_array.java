import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        String line1 = sc.hasNextLine() ? sc.nextLine().trim() : "";
        String[] arr1 = line1.isEmpty() ? new String[0] : line1.split("\\s+");

        String line2 = sc.hasNextLine() ? sc.nextLine().trim() : "";
        String[] arr2 = line2.isEmpty() ? new String[0] : line2.split("\\s+");

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(arr1, arr2);
        
        if (result instanceof String[]) {
            System.out.println(String.join(" ", (String[]) result));
        } else if (result instanceof List) {
            System.out.println(String.join(" ", (List<String>) result));
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
