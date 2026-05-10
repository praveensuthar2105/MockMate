import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Line 1: rows cols
        String dimsLine = sc.hasNextLine() ? sc.nextLine().trim() : "";
        String[] dims = dimsLine.isEmpty() ? new String[]{"0", "0"} : dimsLine.split("\\s+");
        int rows = dims.length >= 2 ? Integer.parseInt(dims[0]) : 0;
        int cols = dims.length >= 2 ? Integer.parseInt(dims[1]) : 0;

        // Next rows lines: matrix data
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            String rowLine = sc.hasNextLine() ? sc.nextLine().trim() : "";
            String[] rowParts = rowLine.isEmpty() ? new String[0] : rowLine.split("\\s+");
            for (int j = 0; j < cols && j < rowParts.length; j++) {
                matrix[i][j] = Integer.parseInt(rowParts[j]);
            }
        }

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(matrix);
        System.out.println(result);
    }
}

{{USER_CODE}}
