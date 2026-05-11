import java.util.*;

public class Main {
    static int lastPos = 0;
    static String fullInput = "";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        fullInput = sb.toString();

        try {
            int[] nums1 = parseNextArray();
            int[] nums2 = parseNextArray();

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(nums1, nums2);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int[] parseNextArray() {
        int start = fullInput.indexOf('[', lastPos);
        int end = -1;
        if (start != -1) {
            end = fullInput.indexOf(']', start);
        }

        if (start != -1 && end != -1) {
            String content = fullInput.substring(start + 1, end);
            lastPos = end + 1;
            return parseArrayString(content);
        } else {
            int comma = fullInput.indexOf(',', lastPos);
            String segment = (comma != -1) ? fullInput.substring(lastPos, comma) : fullInput.substring(lastPos);
            lastPos = (comma != -1) ? comma + 1 : fullInput.length();
            return parseArrayString(segment);
        }
    }

    static int[] parseArrayString(String s) {
        if (s.contains("=")) s = s.substring(s.indexOf("=") + 1);
        s = s.replace("[", "").replace("]", "").replace(",", " ").trim();
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
            int[] arr = (int[]) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(arr[i]);
            }
            System.out.println(sb.toString().trim());
        } else if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
