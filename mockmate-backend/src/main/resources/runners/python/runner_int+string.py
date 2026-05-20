import sys
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
