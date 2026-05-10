import sys

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
s = lines[0].strip() if len(lines) > 0 else ""
n = int(lines[1].strip()) if len(lines) > 1 else 0

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(s, n)
print(result)
