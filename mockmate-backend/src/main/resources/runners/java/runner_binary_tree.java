import java.util.*;

public class Main {

    static class TreeNode {
        int val;
        TreeNode left, right;

        TreeNode(int val) {
            this.val = val;
        }
    }

    static TreeNode buildTree(String input) {
        if (input.isEmpty())
            return null;
        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty() || parts[0].equals("null"))
            return null;

        TreeNode root = new TreeNode(Integer.parseInt(parts[0]));
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        int i = 1;
        while (!queue.isEmpty() && i < parts.length) {
            TreeNode node = queue.poll();
            if (i < parts.length && !parts[i].equals("null")) {
                node.left = new TreeNode(Integer.parseInt(parts[i]));
                queue.offer(node.left);
            }
            i++;
            if (i < parts.length && !parts[i].equals("null")) {
                node.right = new TreeNode(Integer.parseInt(parts[i]));
                queue.offer(node.right);
            }
            i++;
        }
        return root;
    }

    static String serializeTree(TreeNode root) {
        if (root == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (sb.length() > 0)
                sb.append(" ");
            if (node == null) {
                sb.append("null");
            } else {
                sb.append(node.val);
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }
        // Trim trailing nulls
        String result = sb.toString();
        while (result.endsWith(" null")) {
            result = result.substring(0, result.length() - 5);
        }
        return result;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.hasNextLine() ? sc.nextLine().trim() : "";
        TreeNode root = buildTree(input);

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(root);

        if (result instanceof TreeNode) {
            System.out.println(serializeTree((TreeNode) result));
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
