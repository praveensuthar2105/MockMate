import sys

{{USER_CODE}}

if __name__ == "__main__":
    import sys
    input_data = sys.stdin.read().split()
    if not input_data:
        exit(0)
    
    idx = 0
    # Matrix 1
    rows1 = int(input_data[idx]); idx += 1
    cols1 = int(input_data[idx]); idx += 1
    matrix1 = []
    for _ in range(rows1):
        matrix1.append([int(x) for x in input_data[idx:idx+cols1]])
        idx += cols1
        
    # Matrix 2
    rows2 = int(input_data[idx]); idx += 1
    cols2 = int(input_data[idx]); idx += 1
    matrix2 = []
    for _ in range(rows2):
        matrix2.append([int(x) for x in input_data[idx:idx+cols2]])
        idx += cols2

    sol = Solution()
    result = sol.{{methodSignature}}(matrix1, matrix2)
    
    if isinstance(result, list) and result and isinstance(result[0], list):
        for row in result:
            print(" ".join(map(str, row)))
    else:
        print(result)
