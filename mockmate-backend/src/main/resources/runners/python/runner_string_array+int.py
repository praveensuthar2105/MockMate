import sys

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []

# Parse line 1: string array
words = lines[0].strip().split() if len(lines) > 0 else []

# Parse line 2: int
k = int(lines[1].strip()) if len(lines) > 1 else 0

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(words, k)
if isinstance(result, list):
    print(" ".join(map(str, result)))
else:
    print(result)
