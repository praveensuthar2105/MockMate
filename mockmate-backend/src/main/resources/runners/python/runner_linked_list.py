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
            try:
                curr.next = ListNode(int(v))
            except ValueError:
                raise ValueError("Invalid integer token: " + str(v))
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
try:
    sol = Solution()
    if not hasattr(sol, "{{methodSignature}}"):
        print("method {{methodSignature}} not found", file=sys.stderr)
        sys.exit(1)
    result = sol.{{methodSignature}}(head)
except Exception as e:
    print(f"Error: {e}", file=sys.stderr)
    sys.exit(1)
if isinstance(result, ListNode):
    print(serialize_list(result))
elif isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
