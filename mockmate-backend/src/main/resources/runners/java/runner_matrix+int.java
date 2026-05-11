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
            int target = parseNextInt();

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(matrix, target);
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

        if (rows.isEmpty() && !content.trim().isEmpty()) {
             // Fallback for flat matrix string
             rows.add(parseArrayString(content));
        }

        return rows.toArray(new int[0][]);
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

    static int parseNextInt() {
        int nextEqual = fullInput.indexOf('=', lastPos);
        int startSearch = (nextEqual != -1) ? nextEqual + 1 : lastPos;
        
        int start = -1;
        for (int i = startSearch; i < fullInput.length(); i++) {
            char c = fullInput.charAt(i);
            if (Character.isDigit(c) || c == '-') {
                start = i;
                break;
            }
        }
        if (start == -1) return 0;
        
        int end = start;
        while (end < fullInput.length() && (Character.isDigit(fullInput.charAt(end)) || fullInput.charAt(end) == '-')) {
            end++;
        }
        lastPos = end;
        return Integer.parseInt(fullInput.substring(start, end));
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
        } else if (result instanceof List) {
            System.out.println(result.toString());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
