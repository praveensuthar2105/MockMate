import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] dims = sc.nextLine().trim().split("\\s+");
        int rows = Integer.parseInt(dims[0]);
        int cols = Integer.parseInt(dims[1]);
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            String[] row = sc.nextLine().trim()
                .split("\\s+");
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = Integer.parseInt(row[j]);
            }
        }
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(matrix);
        if (result instanceof int[][]) {
            int[][] mat = (int[][]) result;
            for (int[] row : mat) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(" ");
                    sb.append(row[i]);
                }
                System.out.println(sb.toString().trim());
            }
        } else if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
