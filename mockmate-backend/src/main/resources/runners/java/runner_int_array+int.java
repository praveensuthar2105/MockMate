import java.util.*;
import java.util.regex.*;

public class Main {
    static int lastPos = 0;
    static String fullInput = "";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        fullInput = sb.toString();
        
        try {
            // Find all numbers in the input
            List<String> tokens = new ArrayList<>();
            // Look for bracketed array first
            int startBracket = fullInput.indexOf('[');
            int endBracket = fullInput.lastIndexOf(']');
            
            int[] nums;
            int k;
            
            if (startBracket != -1 && endBracket != -1 && startBracket < endBracket) {
                // Bracketed format: [10, 20, 30] 35
                String arrayPart = fullInput.substring(startBracket + 1, endBracket);
                nums = parseArrayString(arrayPart);
                
                String remaining = fullInput.substring(endBracket + 1);
                k = parseFirstInt(remaining);
            } else {
                // Flat format: 10 20 30 35
                // The last number is likely 'k', everything else is 'nums'
                Pattern p = Pattern.compile("-?\\d+");
                Matcher m = p.matcher(fullInput);
                List<Integer> allInts = new ArrayList<>();
                while (m.find()) {
                    allInts.add(Integer.parseInt(m.group()));
                }
                
                if (allInts.isEmpty()) {
                    nums = new int[0];
                    k = 0;
                } else if (allInts.size() == 1) {
                    nums = new int[0];
                    k = allInts.get(0);
                } else {
                    k = allInts.get(allInts.size() - 1);
                    nums = new int[allInts.size() - 1];
                    for (int i = 0; i < nums.length; i++) nums[i] = allInts.get(i);
                }
            }

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(nums, k);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int parseFirstInt(String s) {
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(s);
        if (m.find()) return Integer.parseInt(m.group());
        return 0;
    }

    static int[] parseArrayString(String s) {
        s = s.replace(",", " ").trim();
        if (s.isEmpty()) return new int[0];
        String[] parts = s.split("\\s+");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            try {
                list.add(Integer.parseInt(p));
            } catch (Exception e) {}
        }
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) res[i] = list.get(i);
        return res;
    }

    static void printResult(Object result) {
        if (result instanceof int[]) {
            System.out.println(Arrays.toString((int[]) result));
        } else if (result instanceof Boolean) {
            System.out.println(result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
