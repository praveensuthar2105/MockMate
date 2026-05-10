import sys

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
words = lines[0].strip().split() if lines else []

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(words)
print(result)
