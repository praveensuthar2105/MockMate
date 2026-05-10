import sys

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
s = lines[0].strip()
n = int(lines[1].strip())
sol = Solution()
result = sol.{{methodSignature}}(s, n)
if isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
