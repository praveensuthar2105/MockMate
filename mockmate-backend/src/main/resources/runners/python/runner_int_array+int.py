import sys
import re
import json

{{USER_CODE}}

def smart_reader():
    input_data = sys.stdin.read().strip()
    
    # Try JSON parsing first for bracketed input
    try:
        # Look for something like [1,2,3] 10
        match = re.search(r'(\[.*?\])\s*(-?\d+)', input_data, re.DOTALL)
        if match:
            nums = json.loads(match.group(1))
            k = int(match.group(2))
            return nums, k
    except:
        pass
        
    # Fallback: Flat tokens
    tokens = re.findall(r'-?\d+', input_data)
    if not tokens:
        return [], 0
    if len(tokens) == 1:
        return [], int(tokens[0])
        
    # Last token is k, rest is nums
    k = int(tokens[-1])
    nums = [int(t) for t in tokens[:-1]]
    return nums, k

if __name__ == "__main__":
    try:
        nums, k = smart_reader()
        sol = Solution()
        result = sol.{{methodSignature}}(nums, k)
        if isinstance(result, bool):
            print(str(result).lower())
        else:
            print(result)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)
