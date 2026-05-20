import java.util.*;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString();

        try {
            List<int[]> arrays = new ArrayList<>();
            Matcher mBracket = Pattern.compile("\\[(.*?)\\]").matcher(fullInput);
            int lastEnd = 0;
            while(mBracket.find()) {
                String arrayPart = mBracket.group(1);
                arrays.add(parseArrayString(arrayPart));
                lastEnd = mBracket.end();
            }

            String remaining = fullInput.substring(lastEnd);
            Matcher mInt = Pattern.compile("-?\\d+").matcher(remaining);
            int k = 0;
            if(mInt.find()) k = Integer.parseInt(mInt.group());

            int[] arr1 = arrays.size() > 0 ? arrays.get(0) : new int[0];
            int[] arr2 = arrays.size() > 1 ? arrays.get(1) : new int[0];

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(arr1, arr2, k);
            if(result instanceof int[]) System.out.println(Arrays.toString((int[])result));
            else if(result instanceof Boolean) System.out.println(result.toString().toLowerCase());
            else System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int[] parseArrayString(String s) {
        s = s.replace(",", " ").trim();
        if (s.isEmpty()) return new int[0];
        String[] parts = s.split("\\s+");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            try { list.add(Integer.parseInt(p)); } catch(Exception ignored){}
        }
        int[] res = new int[list.size()];
        for (int i = 0; i < list.size(); i++) res[i] = list.get(i);
        return res;
    }
}

{{USER_CODE}}
