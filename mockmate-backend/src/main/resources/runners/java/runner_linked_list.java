import java.util.*;

class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}

{{USER_CODE}}

public class Main {
    static ListNode buildList(String line) {
        String[] parts = line.trim().split("\\s+");
        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;
        for (String p : parts) {
            if (!p.equals("null")) {
                curr.next = new ListNode(
                    Integer.parseInt(p));
                curr = curr.next;
            }
        }
        return dummy.next;
    }

    static String serializeList(ListNode head) {
        StringBuilder sb = new StringBuilder();
        while (head != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(head.val);
            head = head.next;
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ListNode head = buildList(sc.nextLine());
        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(head);
        if (result instanceof ListNode) {
            System.out.println(
                serializeList((ListNode) result));
        } else if (result instanceof Boolean) {
            System.out.println(
                result.toString().toLowerCase());
        } else {
            System.out.println(result);
        }
    }
}
