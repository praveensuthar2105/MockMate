import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            List<String> list = new ArrayList<>();
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if(line.isEmpty()) continue;
                String[] parts = line.split("(?<=\"|')\\s*,\\s*(?=\"|')|\\s+");
                for(String p : parts) {
                    p = p.trim();
                    if(p.startsWith("\"") && p.endsWith("\"")) p = p.substring(1, p.length()-1);
                    else if(p.startsWith("'") && p.endsWith("'")) p = p.substring(1, p.length()-1);
                    list.add(p);
                }
            }
            String s1 = list.size() > 0 ? list.get(0) : "";
            String s2 = list.size() > 1 ? list.get(1) : "";
            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(s1, s2);
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
