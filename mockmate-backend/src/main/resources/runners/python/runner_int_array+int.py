import sys

def print_result(result):
    if isinstance(result, list):
        print(' '.join(map(str, result)))
    elif isinstance(result, bool):
        print(str(result).lower())
    else:
        print(result)

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
nums = list(map(int, lines[0].strip().split())) if len(lines) > 0 else []
target = int(lines[1].strip()) if len(lines) > 1 else 0

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(nums, target)
print_result(result)
