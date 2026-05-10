import sys

class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

def build_list(values):
    dummy = ListNode(0)
    curr = dummy
    for v in values:
        if v != 'null':
            curr.next = ListNode(int(v))
            curr = curr.next
    return dummy.next

def serialize_list(head):
    result = []
    while head:
        result.append(str(head.val))
        head = head.next
    return ' '.join(result)

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
values = lines[0].strip().split()
head = build_list(values)
sol = Solution()
result = sol.{{methodSignature}}(head)
if isinstance(result, ListNode):
    print(serialize_list(result))
elif isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
