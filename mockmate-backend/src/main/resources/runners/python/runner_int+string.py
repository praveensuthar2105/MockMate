import sys

{{USER_CODE}}

if __name__ == "__main__":
    lines = [line.strip() for line in sys.stdin.readlines() if line.strip()]
    n = int(lines[0]) if len(lines) > 0 else 0
    s = lines[1] if len(lines) > 1 else ""
    
    sol = Solution()
    result = sol.{{methodSignature}}(n, s)
    print(result)
