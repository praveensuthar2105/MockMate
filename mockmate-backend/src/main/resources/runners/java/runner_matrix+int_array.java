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
            int[][] matrix = parseNextMatrix();
            int[] nums = parseNextArray();

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(matrix, nums);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int[][] parseNextMatrix() {
        int start = fullInput.indexOf("[[", lastPos);
        if (start == -1) start = fullInput.indexOf('[', lastPos);
        if (start == -1) return new int[0][0];

        int end = findMatchingBracket(fullInput, start);
        if (end == -1) return new int[0][0];

        String content = fullInput.substring(start + 1, end);
        lastPos = end + 1;

        List<int[]> rows = new ArrayList<>();
        int rowStart = content.indexOf('[');
        while (rowStart != -1) {
            int rowEnd = content.indexOf(']', rowStart);
            if (rowEnd == -1) break;
            rows.add(parseArrayString(content.substring(rowStart + 1, rowEnd)));
            rowStart = content.indexOf('[', rowEnd);
        }
        return rows.toArray(new int[0][]);
    }

    static int[] parseNextArray() {
        int start = fullInput.indexOf('[', lastPos);
        if (start == -1) return new int[0];
        int end = fullInput.indexOf(']', start);
        if (end == -1) return new int[0];
        
        String content = fullInput.substring(start + 1, end);
        lastPos = end + 1;
        return parseArrayString(content);
    }

    static int findMatchingBracket(String s, int start) {
        int count = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '[') count++;
            else if (s.charAt(i) == ']') {
                count--;
                if (count == 0) return i;
            }
        }
        return -1;
    }

    static int[] parseArrayString(String s) {
        s = s.replace(",", " ").replace("[", "").replace("]", "").trim();
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
        } else if (result instanceof int[][]) {
            System.out.println(Arrays.deepToString((int[][]) result));
        } else if (result instanceof Boolean) {
            System.out.println(result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
