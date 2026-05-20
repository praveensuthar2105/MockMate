import sys
import json
import re

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    arrays = []
    # Find all top level arrays
    depth = 0
    start = -1
    for i, c in enumerate(input_data):
        if c == '[':
            if depth == 0: start = i
            depth += 1
        elif c == ']':
            depth -= 1
            if depth == 0 and start != -1:
                try:
                    arrays.append(json.loads(input_data[start:i+1].replace("'", '"')))
                except:
                    pass
                start = -1

    arr1 = arrays[0] if len(arrays) > 0 else []
    arr2 = arrays[1] if len(arrays) > 1 else []

    sol = Solution()
    result = sol.{{methodSignature}}(arr1, arr2)

    if isinstance(result, bool):
        print(str(result).lower())
    else:
        print(json.dumps(result).replace(' ', ''))

{{USER_CODE}}

if __name__ == '__main__':
    main()
