import sys

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
try:
    n = int(lines[0].strip())
except Exception as e:
    print(f"Invalid integer: {e}", file=sys.stderr)
    sys.exit(1)
try:
    sol = Solution()
    if not hasattr(sol, "{{methodSignature}}"):
        print("method {{methodSignature}} not found", file=sys.stderr)
        sys.exit(1)
    result = sol.{{methodSignature}}(n)
except Exception as e:
    print(f"Error: {e}", file=sys.stderr)
    sys.exit(1)
if isinstance(result, bool):
    print(str(result).lower())
else:
    print(result)
