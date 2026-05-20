import sys
import json

def main():
    input_data = sys.stdin.read().strip()
    if not input_data:
        return

    import re
    # Match quoted strings or comma/space separated words
    parts = re.findall(r'"([^"]*)"|'([^']*)'|([^\s,]+)', input_data)
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
