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
nums = list(map(int, lines[0].strip().split()))
sol = Solution()
result = sol.{{methodSignature}}(nums)
print_result(result)
