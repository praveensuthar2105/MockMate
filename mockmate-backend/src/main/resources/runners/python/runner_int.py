import sys

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
n = int(lines[0].strip()) if lines else 0

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(n)
print(result)
