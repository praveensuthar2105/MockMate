import sys

class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

def build_list(values):
    dummy = ListNode(0)
    curr = dummy
    for v in values:
        curr.next = ListNode(int(v))
        curr = curr.next
    return dummy.next

def serialize_list(head):
    result = []
    while head:
        result.append(str(head.val))
        head = head.next
    return ' '.join(result)

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
values = lines[0].strip().split() if lines else []
head = build_list(values)

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(head)
print(serialize_list(result))
