import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString();

        try {
            List<String[]> arrays = new ArrayList<>();
            Matcher mBracket = Pattern.compile("\\[(.*?)\\]").matcher(fullInput);
            while(mBracket.find()) {
                String arrayPart = mBracket.group(1);
                List<String> list = new ArrayList<>();
                Matcher m = Pattern.compile("\"([^"]*)\"|'([^']*)'|([^\\s,]+)").matcher(arrayPart);
                while(m.find()) {
                    if(m.group(1) != null) list.add(m.group(1));
                    else if(m.group(2) != null) list.add(m.group(2));
                    else list.add(m.group(3));
                }
                arrays.add(list.toArray(new String[0]));
            }
            String[] arr1 = arrays.size() > 0 ? arrays.get(0) : new String[0];
            String[] arr2 = arrays.size() > 1 ? arrays.get(1) : new String[0];

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(arr1, arr2);
            if(result instanceof String[]) System.out.println(Arrays.toString((String[])result));
            else System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }
}

{{USER_CODE}}
