import sys

{{USER_CODE}}

if __name__ == "__main__":
    lines = [line.strip() for line in sys.stdin.readlines() if line.strip()]
    s1 = lines[0] if len(lines) > 0 else ""
    s2 = lines[1] if len(lines) > 1 else ""
    
    sol = Solution()
    result = sol.{{methodSignature}}(s1, s2)
    print(result)
