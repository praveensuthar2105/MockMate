import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            String s = sc.hasNextLine() ? sc.nextLine().trim() : "";
            if (s.contains("=")) s = s.substring(s.indexOf("=") + 1).replace("\"", "").replace("'", "").trim();

            String nLine = sc.hasNextLine() ? sc.nextLine().trim() : "0";
            if (nLine.contains("=")) nLine = nLine.substring(nLine.indexOf("=") + 1).trim();
            int n = Integer.parseInt(nLine.replace(",", ""));

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(s, n);
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
