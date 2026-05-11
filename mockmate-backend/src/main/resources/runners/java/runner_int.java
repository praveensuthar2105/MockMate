import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            String line = sc.hasNextLine() ? sc.nextLine() : "0";
            int n = parseSingleInt(line);

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(n);
            if (result instanceof Boolean) {
                System.out.println(result.toString().toLowerCase());
            } else {
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int parseSingleInt(String line) {
        if (line.contains("=")) line = line.substring(line.indexOf("=") + 1);
        line = line.replace(",", "").trim();
        return Integer.parseInt(line);
    }
}

{{USER_CODE}}
