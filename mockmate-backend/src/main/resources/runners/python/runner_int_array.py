import sys, math, heapq, bisect, re
from typing import List, Optional, Dict, Set, Tuple, Any
from collections import deque, Counter, defaultdict, OrderedDict

def print_result(result):
    if isinstance(result, list):
        print(' '.join(map(str, result)))
    elif isinstance(result, bool):
        print(str(result).lower())
    else:
        print(result)

{{USER_CODE}}

import re

full_input = sys.stdin.read()
last_pos = 0

def get_next_array():
    global last_pos
    match = re.search(r'\[(.*?)\]', full_input[last_pos:])
    if match:
        content = match.group(1)
        last_pos += match.end()
        vals = re.findall(r'-?\d+', content)
        return [int(v) for v in vals]
    # Fallback to digits
    vals = re.findall(r'-?\d+', full_input)
    return [int(v) for v in vals]

try:
    nums = get_next_array()
except Exception as e:
    print(f"Parsing error: {e}", file=sys.stderr)
    sys.exit(1)
sol = Solution()
result = sol.{{methodSignature}}(nums)
print_result(result)
