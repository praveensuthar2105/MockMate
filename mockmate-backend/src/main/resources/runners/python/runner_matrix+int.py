import sys

raw_input = sys.stdin.read().strip()
lines = raw_input.split('\n') if raw_input else []
dims = lines[0].strip().split() if lines else ["0", "0"]
rows, cols = int(dims[0]), int(dims[1])

matrix = []
for i in range(1, rows + 1):
    if i < len(lines):
        row = list(map(int, lines[i].strip().split()))
        matrix.append(row)

target = 0
if rows + 1 < len(lines):
    target = int(lines[rows + 1].strip())

{{USER_CODE}}

sol = Solution()
result = sol.{{methodSignature}}(matrix, target)
print(result)
