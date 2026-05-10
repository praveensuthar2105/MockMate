import sys

{{USER_CODE}}

if __name__ == "__main__":
    input_data = sys.stdin.read().split()
    a = int(input_data[0]) if len(input_data) > 0 else 0
    b = int(input_data[1]) if len(input_data) > 1 else 0
    
    sol = Solution()
    result = sol.{{methodSignature}}(a, b)
    print(result)
