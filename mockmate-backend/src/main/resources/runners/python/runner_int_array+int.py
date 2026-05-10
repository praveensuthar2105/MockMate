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
if len(lines) < 2:
    print("Missing lines", file=sys.stderr)
    sys.exit(1)
try:
    nums = list(map(int, lines[0].strip().split()))
    target = int(lines[1].strip())
except ValueError:
    print("Invalid format", file=sys.stderr)
    sys.exit(1)
try:
    sol = Solution()
    if not hasattr(sol, "{{methodSignature}}"):
        print("method {{methodSignature}} not found", file=sys.stderr)
        sys.exit(1)
    result = sol.{{methodSignature}}(nums, target)
except Exception as e:
    print(f"Error: {e}", file=sys.stderr)
    sys.exit(1)
print_result(result)
