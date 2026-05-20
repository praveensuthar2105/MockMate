import os

missing_python_templates = {
    "runner_int+string.py": """import sys
import json

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return
    parts = input_data.split(maxsplit=1)
    if len(parts) == 0:
        return

    k = int(parts[0])
    s = parts[1].strip() if len(parts) > 1 else ""
    if s.startswith('"') and s.endswith('"'): s = s[1:-1]
    elif s.startswith("'") and s.endswith("'"): s = s[1:-1]

    sol = Solution()
    result = sol.{{methodSignature}}(k, s)

    if isinstance(result, bool):
        print(str(result).lower())
    else:
        print(result)

{{USER_CODE}}

if __name__ == '__main__':
    main()
""",
    "runner_string+string.py": """import sys
import json

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    import re
    # Match quoted strings or comma/space separated words
    parts = re.findall(r'"([^"]*)"|\'([^\']*)\'|([^\\s,]+)', input_data)
    parsed_parts = [p[0] or p[1] or p[2] for p in parts if p[0] or p[1] or p[2]]

    s1 = parsed_parts[0] if len(parsed_parts) > 0 else ""
    s2 = parsed_parts[1] if len(parsed_parts) > 1 else ""

    sol = Solution()
    result = sol.{{methodSignature}}(s1, s2)

    if isinstance(result, bool):
        print(str(result).lower())
    else:
        print(result)

{{USER_CODE}}

if __name__ == '__main__':
    main()
""",
    "runner_int+int.py": """import sys
import json

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    parts = input_data.replace(',', ' ').split()
    if len(parts) < 2:
        return

    a, b = int(parts[0]), int(parts[1])

    sol = Solution()
    result = sol.{{methodSignature}}(a, b)

    if isinstance(result, bool):
        print(str(result).lower())
    else:
        print(result)

{{USER_CODE}}

if __name__ == '__main__':
    main()
""",
    "runner_string_array+int.py": """import sys
import json
import re

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    start_idx = input_data.find('[')
    end_idx = input_data.rfind(']')

    if start_idx != -1 and end_idx != -1 and start_idx < end_idx:
        array_part = input_data[start_idx:end_idx+1]
        try:
            arr = json.loads(array_part.replace("'", '"'))
        except:
            arr = []
        remaining = input_data[end_idx+1:]
        nums = re.findall(r'-?\\d+', remaining)
        k = int(nums[0]) if nums else 0
    else:
        arr, k = [], 0

    sol = Solution()
    result = sol.{{methodSignature}}(arr, k)

    if isinstance(result, bool):
        print(str(result).lower())
    else:
        print(json.dumps(result).replace(' ', ''))

{{USER_CODE}}

if __name__ == '__main__':
    main()
""",
    "runner_string_array+string_array.py": """import sys
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
""",
    "runner_matrix+matrix.py": """import sys
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
""",
    "runner_int_array+int_array+int.py": """import sys
import json
import re

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    arrays = []
    depth = 0
    start = -1
    last_end = 0
    for i, c in enumerate(input_data):
        if c == '[':
            if depth == 0: start = i
            depth += 1
        elif c == ']':
            depth -= 1
            if depth == 0 and start != -1:
                try:
                    arrays.append(json.loads(input_data[start:i+1]))
                except:
                    pass
                last_end = i + 1
                start = -1

    remaining = input_data[last_end:]
    nums = re.findall(r'-?\\d+', remaining)
    k = int(nums[0]) if nums else 0

    arr1 = arrays[0] if len(arrays) > 0 else []
    arr2 = arrays[1] if len(arrays) > 1 else []

    sol = Solution()
    result = sol.{{methodSignature}}(arr1, arr2, k)

    if isinstance(result, bool):
        print(str(result).lower())
    else:
        print(json.dumps(result).replace(' ', ''))

{{USER_CODE}}

if __name__ == '__main__':
    main()
"""
}

for filename, content in missing_python_templates.items():
    path = os.path.join("mockmate-backend/src/main/resources/runners/python", filename)
    with open(path, "w") as f:
        f.write(content)

print("Generated missing Python runner templates.")
