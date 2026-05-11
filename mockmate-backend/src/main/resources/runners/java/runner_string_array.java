import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            String line = sc.hasNextLine() ? sc.nextLine() : "";
            String[] words = parseStringArray(line);
            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(words);
            if (result instanceof String[]) {
                System.out.println(String.join(" ", (String[]) result));
            } else {
                System.out.println(result);
            }
        } finally {
            sc.close();
        }
    }

    static String[] parseStringArray(String line) {
        if (line.contains("=")) line = line.substring(line.indexOf("=") + 1);
        line = line.replace("[", "").replace("]", "").replace(",", " ").trim();
        if (line.isEmpty()) return new String[0];
        return line.split("\\s+");
    }
        if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
