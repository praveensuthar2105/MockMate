import sys

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
n = int(lines[0].strip())
sol = Solution()
result = sol.{{methodSignature}}(n)
if isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
