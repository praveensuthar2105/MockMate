import sys

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
s = lines[0].strip()
sol = Solution()
result = sol.{{methodSignature}}(s)
if isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
