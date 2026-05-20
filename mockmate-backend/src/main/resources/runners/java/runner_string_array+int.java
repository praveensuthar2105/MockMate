import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString();

        try {
            int startBracket = fullInput.indexOf('[');
            int endBracket = fullInput.lastIndexOf(']');

            String[] arr;
            int k = 0;

            if (startBracket != -1 && endBracket != -1 && startBracket < endBracket) {
                String arrayPart = fullInput.substring(startBracket + 1, endBracket);
                List<String> list = new ArrayList<>();
                Matcher m = Pattern.compile("\"([^"]*)\"|'([^']*)'|([^\\s,]+)").matcher(arrayPart);
                while(m.find()) {
                    if(m.group(1) != null) list.add(m.group(1));
                    else if(m.group(2) != null) list.add(m.group(2));
                    else list.add(m.group(3));
                }
                arr = list.toArray(new String[0]);
                String remaining = fullInput.substring(endBracket + 1);
                Matcher mInt = Pattern.compile("-?\\d+").matcher(remaining);
                if(mInt.find()) k = Integer.parseInt(mInt.group());
            } else {
                arr = new String[0];
            }

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(arr, k);
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
