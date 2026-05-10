import sys

def print_result(result):
    if isinstance(result, list):
        print(' '.join(map(str, result)))
    elif isinstance(result, bool):
        print(str(result).lower())
    else:
        print(result)

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
try:
    nums = list(map(int, lines[0].strip().split()))
except ValueError as e:
    print("ValueError: " + str(e), file=sys.stderr)
    sys.exit(1)
sol = Solution()
result = sol.{{methodSignature}}(nums)
print_result(result)
