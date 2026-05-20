import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            int k = sc.hasNextInt() ? sc.nextInt() : 0;
            String s = "";
            if (sc.hasNext()) {
                s = sc.nextLine().trim();
                // strip leading commas/spaces
                s = s.replaceFirst("^[,\\s]+", "");
                if(s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length()-1);
            }
            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(k, s);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
