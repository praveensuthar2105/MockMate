import sys
import json
import re

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    matrices = []
    depth = 0
    start = -1
    for i, c in enumerate(input_data):
        if c == '[':
            if depth == 0 and input_data[i:i+2] == '[[':
                start = i
            depth += 1
        elif c == ']':
            depth -= 1
            if depth == 0 and start != -1:
                try:
                    matrices.append(json.loads(input_data[start:i+1]))
                except:
                    pass
                start = -1

    mat1 = matrices[0] if len(matrices) > 0 else []
    mat2 = matrices[1] if len(matrices) > 1 else []

    sol = Solution()
    result = sol.{{methodSignature}}(mat1, mat2)

    if isinstance(result, bool):
        print(str(result).lower())
    else:
        print(json.dumps(result).replace(' ', ''))

{{USER_CODE}}

if __name__ == '__main__':
    main()
