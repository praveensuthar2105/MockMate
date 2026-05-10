import sys

def print_result(result):
    if isinstance(result, list):
        print(' '.join(map(str, result)))
    else:
        print(result)

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
nums1 = list(map(int, lines[0].strip().split())) if len(lines) > 0 else []
nums2 = list(map(int, lines[1].strip().split())) if len(lines) > 1 else []

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(nums1, nums2)
print_result(result)
