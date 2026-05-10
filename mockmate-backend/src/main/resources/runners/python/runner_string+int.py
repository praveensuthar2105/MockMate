import sys

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
s = lines[0].strip()
try:
    n = int(lines[1].strip())
except ValueError:
    print("ValueError", file=sys.stderr)
    sys.exit(1)
sol = Solution()
result = sol.{{methodSignature}}(s, n)
if isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
