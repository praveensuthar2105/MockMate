import sys
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
        nums = re.findall(r'-?\d+', remaining)
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
