import java.util.*;

class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

{{USER_CODE}}

public class Main {
    static TreeNode buildTree(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0
            || parts[0].equals("null")) return null;
        TreeNode root = new TreeNode(
            Integer.parseInt(parts[0]));
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        int i = 1;
        while (!queue.isEmpty() && i < parts.length) {
            TreeNode node = queue.poll();
            if (i < parts.length
                && !parts[i].equals("null")) {
                node.left = new TreeNode(
                    Integer.parseInt(parts[i]));
                queue.offer(node.left);
            }
            i++;
            if (i < parts.length
                && !parts[i].equals("null")) {
                node.right = new TreeNode(
                    Integer.parseInt(parts[i]));
                queue.offer(node.right);
            }
            i++;
        }
        return root;
    }

    static String serializeTree(TreeNode root) {
        if (root == null) return "null";
        StringBuilder sb = new StringBuilder();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (sb.length() > 0) sb.append(" ");
            if (node == null) {
                sb.append("null");
            } else {
                sb.append(node.val);
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }
        String result = sb.toString().trim();
        while (result.endsWith(" null")) {
            result = result.substring(
                0, result.length() - 5);
        }
        return result.trim();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TreeNode root = buildTree(sc.nextLine());
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(root);
        if (result instanceof TreeNode) {
            System.out.println(
                serializeTree((TreeNode) result));
        } else if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
}
