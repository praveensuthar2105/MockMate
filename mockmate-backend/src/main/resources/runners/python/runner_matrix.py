import sys

def print_result(result):
    if isinstance(result, list):
        if result and isinstance(result[0], list):
            for row in result:
                print(' '.join(map(str, row)))
        else:
            print(' '.join(map(str, result)))
    elif isinstance(result, bool):
        print(str(result).lower())
    else:
        print(result)

{{USER_CODE}}

lines = sys.stdin.read().strip().split('\n')
dims = lines[0].strip().split()
rows, cols = int(dims[0]), int(dims[1])
matrix = []
for i in range(1, rows + 1):
    row = list(map(int, lines[i].strip().split()))
    matrix.append(row)
sol = Solution()
result = sol.{{methodSignature}}(matrix)
print_result(result)
