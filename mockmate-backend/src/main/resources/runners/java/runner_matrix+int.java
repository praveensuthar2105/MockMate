import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Parse line 1: rows cols
        int rows = 0, cols = 0;
        if (sc.hasNextLine()) {
            String dimsLine = sc.nextLine().trim();
            if (!dimsLine.isEmpty()) {
                String[] dims = dimsLine.split("\\s+");
                if (dims.length >= 2) {
                    rows = Integer.parseInt(dims[0]);
                    cols = Integer.parseInt(dims[1]);
                }
            }
        }

        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (sc.hasNextLine()) {
                String rowLine = sc.nextLine().trim();
                String[] parts = rowLine.split("\\s+");
                for (int j = 0; j < cols && j < parts.length; j++) {
                    matrix[i][j] = Integer.parseInt(parts[j]);
                }
            }
        }

        // Parse the target integer
        int target = 0;
        if (sc.hasNextLine()) {
            String targetLine = sc.nextLine().trim();
            if (!targetLine.isEmpty()) {
                target = Integer.parseInt(targetLine);
            }
        }

        // Call user solution
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(matrix, target);

        // Print output
        printResult(result);
    }

    static void printResult(Object result) {
        if (result instanceof int[]) {
            int[] arr = (int[]) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(" ");
                sb.append(arr[i]);
            }
            System.out.println(sb.toString());
        } else if (result instanceof boolean[]) {
            boolean[] arr = (boolean[]) result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0)
                    sb.append(" ");
                sb.append(arr[i]);
            }
            System.out.println(sb.toString());
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
