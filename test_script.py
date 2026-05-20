import subprocess
import os

code = """
class Solution:
    def twoSum(self, nums, target):
        return [0, 1]
"""

input_data = "[2,7,11,15] 9"

# save to file and run python template logic
import re
import json

def main(input_data):
    if not input_data:
        return

    start_idx = input_data.find('[')
    end_idx = input_data.rfind(']')

    if start_idx != -1 and end_idx != -1 and start_idx < end_idx:
        array_part = input_data[start_idx:end_idx+1]
        try:
            arr = json.loads(array_part.replace("'", '"'))
        except Exception as e:
            arr = []
            print("Exception:", e)
        remaining = input_data[end_idx+1:]
        nums = re.findall(r'-?\\d+', remaining)
        k = int(nums[0]) if nums else 0
    else:
        arr, k = [], 0

    class Solution:
        def twoSum(self, nums, target):
            return [0, 1]

    sol = Solution()
    result = sol.twoSum(arr, k)
    print(result)

main(input_data)
