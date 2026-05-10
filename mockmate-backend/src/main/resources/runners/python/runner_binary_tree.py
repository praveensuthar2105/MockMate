import sys
from collections import deque

class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def build_tree(values):
    if not values or values[0] == 'null':
        return None
    root = TreeNode(int(values[0]))
    queue = deque([root])
    i = 1
    while queue and i < len(values):
        node = queue.popleft()
        if i < len(values) and values[i] != 'null':
            node.left = TreeNode(int(values[i]))
            queue.append(node.left)
        i += 1
        if i < len(values) and values[i] != 'null':
            node.right = TreeNode(int(values[i]))
            queue.append(node.right)
        i += 1
    return root

def serialize_tree(root):
    if not root:
        return 'null'
    result = []
    queue = deque([root])
    while queue:
        node = queue.popleft()
        if node:
            result.append(str(node.val))
            queue.append(node.left)
            queue.append(node.right)
        else:
            result.append('null')
    while result and result[-1] == 'null':
        result.pop()
    return ' '.join(result)

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
values = lines[0].strip().split()
root = build_tree(values)
sol = Solution()
result = sol.{{methodSignature}}(root)
if isinstance(result, TreeNode):
    print(serialize_tree(result))
elif isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
