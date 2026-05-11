import sys
import re
import json
import math
from typing import List, Optional, Any
from collections import deque

class ListNode:
    def __init__(self, val=0, next=None):
        self.val = val
        self.next = next

class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

{{USER_CODE}}

def smart_reader():
    input_data = sys.stdin.read()
    
    # Extract matrix [[...]]
    matrix_match = re.search(r'\[\s*\[.*?\]\s*\]', input_data, re.DOTALL)
    matrix = []
    if matrix_match:
        try:
            matrix = json.loads(matrix_match.group(0))
        except:
            pass
            
    # Extract integer (target) - usually after the matrix or with 'target ='
    # Find all numbers that are not inside brackets
    remaining = re.sub(r'\[.*?\]', ' ', input_data, flags=re.DOTALL)
    ints = re.findall(r'-?\d+', remaining)
    target = int(ints[0]) if ints else 0
    
    return matrix, target

if __name__ == "__main__":
    try:
        matrix, target = smart_reader()
        sol = Solution()
        result = sol.{{methodSignature}}(matrix, target)
        if isinstance(result, bool):
            print(str(result).lower())
        else:
            print(result)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)
