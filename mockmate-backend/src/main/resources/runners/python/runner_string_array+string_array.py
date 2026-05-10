import sys

{{USER_CODE}}

if __name__ == "__main__":
    lines = [line.strip() for line in sys.stdin.readlines() if line.strip()]
    
    arr1 = lines[0].split() if len(lines) > 0 else []
    arr2 = lines[1].split() if len(lines) > 1 else []
    
    sol = Solution()
    result = sol.{{methodSignature}}(arr1, arr2)
    
    if isinstance(result, list):
        print(" ".join(map(str, result)))
    else:
        print(result)
