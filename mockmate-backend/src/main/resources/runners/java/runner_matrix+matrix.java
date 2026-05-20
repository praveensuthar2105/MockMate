import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while (sc.hasNextLine()) sb.append(sc.nextLine()).append(" ");
        String fullInput = sb.toString().replaceAll("\\s", "");

        try {
            List<int[][]> matrices = new ArrayList<>();
            int idx = 0;
            while(idx < fullInput.length()) {
                int start = fullInput.indexOf("[[", idx);
                if(start == -1) break;
                int depth = 0;
                int end = start;
                for(int i = start; i < fullInput.length(); i++) {
                    if(fullInput.charAt(i) == '[') depth++;
                    else if(fullInput.charAt(i) == ']') depth--;
                    if(depth == 0) { end = i; break; }
                }
                String matrixStr = fullInput.substring(start, end + 1);
                matrices.add(parseMatrix(matrixStr));
                idx = end + 1;
            }

            int[][] mat1 = matrices.size() > 0 ? matrices.get(0) : new int[0][0];
            int[][] mat2 = matrices.size() > 1 ? matrices.get(1) : new int[0][0];

            Solution sol = new Solution();
            Object result = sol.{{methodSignature}}(mat1, mat2);
            if(result instanceof int[][]) {
                System.out.println(Arrays.deepToString((int[][])result));
            } else {
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            sc.close();
        }
    }

    static int[][] parseMatrix(String s) {
        if (s == null || s.length() < 2 || !s.startsWith("[[") || !s.endsWith("]]")) return new int[0][0];
        s = s.substring(2, s.length() - 2);
        if (s.isEmpty()) return new int[0][0];
        String[] rows = s.split("\\],\\[");
        int[][] res = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].isEmpty()) {
                res[i] = new int[0];
                continue;
            }
            String[] cols = rows[i].split(",");
            res[i] = new int[cols.length];
            for (int j = 0; j < cols.length; j++) {
                res[i][j] = Integer.parseInt(cols[j]);
            }
        }
        return res;
    }
}

{{USER_CODE}}
