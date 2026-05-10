import java.util.*;

public class Main {

    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }
    }

    static ListNode buildList(String input) {
        if (input.isEmpty())
            return null;
        String[] parts = input.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty())
            return null;
        ListNode dummy = new ListNode(0);
        ListNode curr = dummy;
        for (String p : parts) {
            curr.next = new ListNode(Integer.parseInt(p));
            curr = curr.next;
        }
        return dummy.next;
    }

    static String serializeList(ListNode head) {
        StringBuilder sb = new StringBuilder();
        while (head != null) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(head.val);
            head = head.next;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input = sc.hasNextLine() ? sc.nextLine().trim() : "";
        ListNode head = buildList(input);

        Solution sol = new Solution();
        Object result = sol.{{methodSignature}}(head);

        if (result instanceof ListNode) {
            System.out.println(serializeList((ListNode) result));
        } else {
            System.out.println(result);
        }
    }
}

{{USER_CODE}}
