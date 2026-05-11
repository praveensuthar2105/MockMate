import sys
import re
import json
from typing import List, Optional, Any

{{USER_CODE}}

def smart_reader():
    input_data = sys.stdin.read()
    
    # Find all top-level lists [...]
    lists = []
    # Simplified regex for finding JSON-like lists
    pos = 0
    while True:
        start = input_data.find('[', pos)
        if start == -1: break
        
        # Find matching bracket
        count = 0
        end = -1
        for i in range(start, len(input_data)):
            if input_data[i] == '[': count += 1
            elif input_data[i] == ']':
                count -= 1
                if count == 0:
                    end = i
                    break
        if end != -1:
            try:
                lists.append(json.loads(input_data[start:end+1]))
                pos = end + 1
            except:
                pos = start + 1
        else:
            break
            
    matrix = lists[0] if len(lists) > 0 else []
    nums = lists[1] if len(lists) > 1 else []
    
    return matrix, nums

if __name__ == "__main__":
    try:
        matrix, nums = smart_reader()
        sol = Solution()
        result = sol.{{methodSignature}}(matrix, nums)
        if isinstance(result, bool):
            print(str(result).lower())
        else:
            print(result)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)
