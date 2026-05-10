import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Matrix 1
        int rows1 = sc.hasNextInt() ? sc.nextInt() : 0;
        int cols1 = sc.hasNextInt() ? sc.nextInt() : 0;
        int[][] matrix1 = new int[rows1][cols1];
        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols1; j++) {
                matrix1[i][j] = sc.nextInt();
            }
        }

        // Matrix 2
        int rows2 = sc.hasNextInt() ? sc.nextInt() : 0;
        int cols2 = sc.hasNextInt() ? sc.nextInt() : 0;
        int[][] matrix2 = new int[rows2][cols2];
        for (int i = 0; i < rows2; i++) {
            for (int j = 0; j < cols2; j++) {
                matrix2[i][j] = sc.nextInt();
            }
        }

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(matrix1, matrix2);
        
        if (result instanceof int[][]) {
            int[][] res = (int[][]) result;
            for (int[] row : res) {
                for (int i = 0; i < row.length; i++) {
                    System.out.print(row[i] + (i == row.length - 1 ? "" : " "));
                }
                System.out.println();
            }
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
