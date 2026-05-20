import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            List<Integer> list = new ArrayList<>();
            while (sc.hasNext()) {
                String s = sc.next().replace(",", "");
                try {
                    list.add(Integer.parseInt(s));
                } catch(Exception ignored){}
            }
            int a = list.size() > 0 ? list.get(0) : 0;
            int b = list.size() > 1 ? list.get(1) : 0;
            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(a, b);
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
