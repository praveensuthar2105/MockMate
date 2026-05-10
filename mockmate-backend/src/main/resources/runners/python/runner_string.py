import sys

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
s = lines[0].strip() if lines else ""

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(s)
print(result)
