import sys
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
