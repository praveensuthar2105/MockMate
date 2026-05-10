import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] dims = sc.nextLine().trim().split("\\s+");
        int rows = 0;
        int cols = 0;
        try {
            rows = Integer.parseInt(dims[0]);
            cols = Integer.parseInt(dims[1]);
            if (rows < 0 || cols < 0 || rows > 10000 || cols > 10000) {
                System.err.println("Invalid matrix dimensions");
                System.exit(1);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Failed to parse matrix dimensions");
            System.exit(1);
        }
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            String[] row = sc.nextLine().trim()
                .split("\\s+");
            if (row.length < cols) {
                System.err.println("Incomplete row " + i);
                System.exit(1);
            }
            for (int j = 0; j < cols; j++) {
                try {
                    matrix[i][j] = Integer.parseInt(row[j]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid integer at row " + i + " col " + j + ": " + row[j]);
                    System.exit(1);
                }
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
