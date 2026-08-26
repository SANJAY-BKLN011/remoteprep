/**
 * Mock DSA Questions Repository
 * 
 * Provides a categorized repository of Easy and Medium coding problems
 * across the 10 frozen DSA topics with multi-language starter templates (Java, C++, C, Python).
 * 
 * Later this will be replaced by Spring Boot REST API calls to MySQL.
 */

(function () {
    const DSA_QUESTIONS = [
        {
                "id": "dsa_arr_01",
                "topicId": "dsa_arrays",
                "topicName": "Arrays",
                "difficulty": "easy",
                "title": "Two Sum",
                "description": "Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.",
                "examples": [
                        {
                                "input": "nums = [2, 7, 11, 15], target = 9",
                                "output": "[0, 1]",
                                "explanation": "Because nums[0] + nums[1] == 9, we return [0, 1]."
                        },
                        {
                                "input": "nums = [3, 2, 4], target = 6",
                                "output": "[1, 2]",
                                "explanation": "Because nums[1] + nums[2] == 6, we return [1, 2]."
                        }
                ],
                "constraints": [
                        "2 <= nums.length <= 10^4",
                        "-10^9 <= nums[i] <= 10^9",
                        "-10^9 <= target <= 10^9",
                        "Only one valid answer exists."
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int[] twoSum(int[] nums, int target) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\nusing namespace std;\n\nvector<int> twoSum(vector<int>& nums, int target) {\n    // Write your solution here\n    return {};\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nint* twoSum(int* nums, int numsSize, int target, int* returnSize) {\n    // Write your solution here\n    *returnSize = 0;\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def twoSum(nums, target):\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "nums = [2, 7, 11, 15], target = 9",
                                "expectedOutput": "[0, 1]"
                        },
                        {
                                "input": "nums = [3, 2, 4], target = 6",
                                "expectedOutput": "[1, 2]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "nums = [3, 3], target = 6",
                                "expectedOutput": "[0, 1]"
                        },
                        {
                                "input": "nums = [-1, -2, -3, -4, -5], target = -8",
                                "expectedOutput": "[2, 4]"
                        },
                        {
                                "input": "nums = [0, 4, 3, 0], target = 0",
                                "expectedOutput": "[0, 3]"
                        }
                ]
        },
        {
                "id": "dsa_arr_02",
                "topicId": "dsa_arrays",
                "topicName": "Arrays",
                "difficulty": "medium",
                "title": "Container With Most Water",
                "description": "You are given an integer array `height` of length `n`. There are `n` vertical lines drawn such that the two endpoints of the `i-th` line are `(i, 0)` and `(i, height[i])`.\n\nFind two lines that together with the x-axis form a container, such that the container contains the most water.\n\nReturn the maximum amount of water a container can store.",
                "examples": [
                        {
                                "input": "height = [1, 8, 6, 2, 5, 4, 8, 3, 7]",
                                "output": "49",
                                "explanation": "The vertical lines are at indices 1 and 8, width is 7, height is 7. Max Area = 7 * 7 = 49."
                        },
                        {
                                "input": "height = [1, 1]",
                                "output": "1",
                                "explanation": "Max Area = 1 * 1 = 1."
                        }
                ],
                "constraints": [
                        "n == height.length",
                        "2 <= n <= 10^5",
                        "0 <= height[i] <= 10^4"
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int maxArea(int[] height) {\n        // Write your solution here\n        return 0;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\nusing namespace std;\n\nint maxArea(vector<int>& height) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nint maxArea(int* height, int heightSize) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def maxArea(height):\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "height = [1, 8, 6, 2, 5, 4, 8, 3, 7]",
                                "expectedOutput": "49"
                        },
                        {
                                "input": "height = [1, 1]",
                                "expectedOutput": "1"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "height = [4, 3, 2, 1, 4]",
                                "expectedOutput": "16"
                        },
                        {
                                "input": "height = [1, 2, 1]",
                                "expectedOutput": "2"
                        },
                        {
                                "input": "height = [2, 3, 4, 5, 18, 17, 6]",
                                "expectedOutput": "17"
                        }
                ]
        },
        {
                "id": "dsa_str_01",
                "topicId": "dsa_strings",
                "topicName": "Strings",
                "difficulty": "easy",
                "title": "Valid Palindrome",
                "description": "A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward.\n\nGiven a string `s`, return `true` if it is a palindrome, or `false` otherwise.",
                "examples": [
                        {
                                "input": "s = \"A man, a plan, a canal: Panama\"",
                                "output": "true",
                                "explanation": "\"amanaplanacanalpanama\" is a palindrome."
                        },
                        {
                                "input": "s = \"race a car\"",
                                "output": "false",
                                "explanation": "\"raceacar\" is not a palindrome."
                        }
                ],
                "constraints": [
                        "1 <= s.length <= 2 * 10^5",
                        "`s` consists only of printable ASCII characters."
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static boolean isPalindrome(String s) {\n        // Write your solution here\n        return false;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <string>\nusing namespace std;\n\nbool isPalindrome(string s) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdbool.h>\n#include <string.h>\n\nbool isPalindrome(char* s) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def isPalindrome(s):\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "s = \"A man, a plan, a canal: Panama\"",
                                "expectedOutput": "true"
                        },
                        {
                                "input": "s = \"race a car\"",
                                "expectedOutput": "false"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "s = \" \"",
                                "expectedOutput": "true"
                        },
                        {
                                "input": "s = \"0P\"",
                                "expectedOutput": "false"
                        },
                        {
                                "input": "s = \"ab_a\"",
                                "expectedOutput": "true"
                        }
                ]
        },
        {
                "id": "dsa_str_02",
                "topicId": "dsa_strings",
                "topicName": "Strings",
                "difficulty": "medium",
                "title": "Longest Substring Without Repeating Characters",
                "description": "Given a string `s`, find the length of the longest substring without repeating characters.",
                "examples": [
                        {
                                "input": "s = \"abcabcbb\"",
                                "output": "3",
                                "explanation": "The answer is \"abc\", with the length of 3."
                        },
                        {
                                "input": "s = \"bbbbb\"",
                                "output": "1",
                                "explanation": "The answer is \"b\", with the length of 1."
                        }
                ],
                "constraints": [
                        "0 <= s.length <= 5 * 10^4",
                        "`s` consists of English letters, digits, symbols and spaces."
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int lengthOfLongestSubstring(String s) {\n        // Write your solution here\n        return 0;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <string>\nusing namespace std;\n\nint lengthOfLongestSubstring(string s) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <string.h>\n\nint lengthOfLongestSubstring(char* s) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def lengthOfLongestSubstring(s):\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "s = \"abcabcbb\"",
                                "expectedOutput": "3"
                        },
                        {
                                "input": "s = \"bbbbb\"",
                                "expectedOutput": "1"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "s = \"pwwkew\"",
                                "expectedOutput": "3"
                        },
                        {
                                "input": "s = \"\"",
                                "expectedOutput": "0"
                        },
                        {
                                "input": "s = \"au\"",
                                "expectedOutput": "2"
                        }
                ]
        },
        {
                "id": "dsa_ll_01",
                "topicId": "dsa_linked_lists",
                "topicName": "Linked Lists",
                "difficulty": "easy",
                "title": "Reverse Linked List",
                "description": "Given the head of a singly linked list, reverse the list, and return the reversed list values.",
                "examples": [
                        {
                                "input": "head = [1, 2, 3, 4, 5]",
                                "output": "[5, 4, 3, 2, 1]",
                                "explanation": "The list is completely reversed."
                        },
                        {
                                "input": "head = [1, 2]",
                                "output": "[2, 1]",
                                "explanation": "Nodes are swapped."
                        }
                ],
                "constraints": [
                        "The number of nodes in the list is in the range [0, 5000].",
                        "-5000 <= Node.val <= 5000"
                ],
                "starterCode": {
                        "java": "class ListNode {\n    int val;\n    ListNode next;\n    ListNode(int val) { this.val = val; }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static ListNode reverseList(ListNode head) {\n        // Write your solution here\n        return null;\n    }\n}",
                        "cpp": "#include <iostream>\nusing namespace std;\n\nstruct ListNode {\n    int val;\n    ListNode *next;\n    ListNode(int x) : val(x), next(NULL) {}\n};\n\nListNode* reverseList(ListNode* head) {\n    // Write your solution here\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nstruct ListNode {\n    int val;\n    struct ListNode *next;\n};\n\nstruct ListNode* reverseList(struct ListNode* head) {\n    // Write your solution here\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class ListNode:\n    def __init__(self, val=0, next=None):\n        self.val = val\n        self.next = next\n\ndef reverseList(head):\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "head = [1, 2, 3, 4, 5]",
                                "expectedOutput": "[5, 4, 3, 2, 1]"
                        },
                        {
                                "input": "head = [1, 2]",
                                "expectedOutput": "[2, 1]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "head = []",
                                "expectedOutput": "[]"
                        },
                        {
                                "input": "head = [1]",
                                "expectedOutput": "[1]"
                        }
                ]
        },
        {
                "id": "dsa_ll_02",
                "topicId": "dsa_linked_lists",
                "topicName": "Linked Lists",
                "difficulty": "medium",
                "title": "Remove N-th Node From End of List",
                "description": "Given the head of a linked list, remove the `n-th` node from the end of the list and return its head.",
                "examples": [
                        {
                                "input": "head = [1, 2, 3, 4, 5], n = 2",
                                "output": "[1, 2, 3, 5]",
                                "explanation": "The 2nd node from end is 4, which is removed."
                        },
                        {
                                "input": "head = [1], n = 1",
                                "output": "[]",
                                "explanation": "The only node is removed."
                        }
                ],
                "constraints": [
                        "The number of nodes in the list is `sz`.",
                        "1 <= sz <= 30",
                        "0 <= Node.val <= 100",
                        "1 <= n <= sz"
                ],
                "starterCode": {
                        "java": "class ListNode {\n    int val;\n    ListNode next;\n    ListNode(int val) { this.val = val; }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static ListNode removeNthFromEnd(ListNode head, int n) {\n        // Write your solution here\n        return null;\n    }\n}",
                        "cpp": "#include <iostream>\nusing namespace std;\n\nstruct ListNode {\n    int val;\n    ListNode *next;\n    ListNode(int x) : val(x), next(NULL) {}\n};\n\nListNode* removeNthFromEnd(ListNode* head, int n) {\n    // Write your solution here\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nstruct ListNode {\n    int val;\n    struct ListNode *next;\n};\n\nstruct ListNode* removeNthFromEnd(struct ListNode* head, int n) {\n    // Write your solution here\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class ListNode:\n    def __init__(self, val=0, next=None):\n        self.val = val\n        self.next = next\n\ndef removeNthFromEnd(head, n):\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "head = [1, 2, 3, 4, 5], n = 2",
                                "expectedOutput": "[1, 2, 3, 5]"
                        },
                        {
                                "input": "head = [1], n = 1",
                                "expectedOutput": "[]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "head = [1, 2], n = 1",
                                "expectedOutput": "[1]"
                        },
                        {
                                "input": "head = [1, 2], n = 2",
                                "expectedOutput": "[2]"
                        }
                ]
        },
        {
                "id": "dsa_sq_01",
                "topicId": "dsa_stack_queue",
                "topicName": "Stack & Queue",
                "difficulty": "easy",
                "title": "Valid Parentheses",
                "description": "Given a string `s` containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\nAn input string is valid if open brackets are closed by the same type of brackets and in the correct order.",
                "examples": [
                        {
                                "input": "s = \"()\"",
                                "output": "true",
                                "explanation": "The parentheses match."
                        },
                        {
                                "input": "s = \"()[]{}\"",
                                "output": "true",
                                "explanation": "All bracket pairs match."
                        },
                        {
                                "input": "s = \"(]\"",
                                "output": "false",
                                "explanation": "Mismatched bracket pair."
                        }
                ],
                "constraints": [
                        "1 <= s.length <= 10^4",
                        "`s` consists of parentheses only '()[]{}'."
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static boolean isValid(String s) {\n        // Write your solution here\n        return false;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <string>\n#include <stack>\nusing namespace std;\n\nbool isValid(string s) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdbool.h>\n#include <string.h>\n\nbool isValid(char* s) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def isValid(s):\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "s = \"()\"",
                                "expectedOutput": "true"
                        },
                        {
                                "input": "s = \"()[]{}\"",
                                "expectedOutput": "true"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "s = \"(]\"",
                                "expectedOutput": "false"
                        },
                        {
                                "input": "s = \"([)]\"",
                                "expectedOutput": "false"
                        },
                        {
                                "input": "s = \"{[]}\"",
                                "expectedOutput": "true"
                        }
                ]
        },
        {
                "id": "dsa_sq_02",
                "topicId": "dsa_stack_queue",
                "topicName": "Stack & Queue",
                "difficulty": "medium",
                "title": "Min Stack",
                "description": "Design a stack that supports push, pop, top, and retrieving the minimum element in constant time.\n\nImplement the MinStack class.",
                "examples": [
                        {
                                "input": "[\"MinStack\",\"push\",\"push\",\"push\",\"getMin\",\"pop\",\"top\",\"getMin\"], [[],[-2],[0],[-3],[],[],[],[]]",
                                "output": "[null,null,null,null,-3,null,0,-2]",
                                "explanation": "MinStack returns the minimum at any point."
                        }
                ],
                "constraints": [
                        "-2^31 <= val <= 2^31 - 1",
                        "Methods pop, top and getMin operations will always be called on non-empty stacks.",
                        "At most 3 * 10^4 calls will be made to push, pop, top, and getMin."
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\nclass MinStack {\n    public MinStack() {\n        // Initialize your data structure here\n    }\n    \n    public void push(int val) {\n        // Write your solution here\n    }\n    \n    public void pop() {\n        // Write your solution here\n    }\n    \n    public int top() {\n        // Write your solution here\n        return 0;\n    }\n    \n    public int getMin() {\n        // Write your solution here\n        return 0;\n    }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n}",
                        "cpp": "#include <iostream>\n#include <stack>\nusing namespace std;\n\nclass MinStack {\npublic:\n    MinStack() {\n        // Initialize your data structure here\n    }\n    \n    void push(int val) {\n        // Write your solution here\n    }\n    \n    void pop() {\n        // Write your solution here\n    }\n    \n    int top() {\n        // Write your solution here\n        return 0;\n    }\n    \n    int getMin() {\n        // Write your solution here\n        return 0;\n    }\n};\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\ntypedef struct {\n    // Define struct members\n} MinStack;\n\nMinStack* minStackCreate() {\n    // Initialize your data structure here\n    return NULL;\n}\n\nvoid minStackPush(MinStack* obj, int val) {\n    // Write your solution here\n}\n\nvoid minStackPop(MinStack* obj) {\n    // Write your solution here\n}\n\nint minStackTop(MinStack* obj) {\n    // Write your solution here\n    return 0;\n}\n\nint minStackGetMin(MinStack* obj) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class MinStack:\n    def __init__(self):\n        # Initialize your data structure here\n        pass\n\n    def push(self, val: int) -> None:\n        # Write your solution here\n        pass\n\n    def pop(self) -> None:\n        # Write your solution here\n        pass\n\n    def top(self) -> int:\n        # Write your solution here\n        return 0\n\n    def getMin(self) -> int:\n        # Write your solution here\n        return 0\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "[\"MinStack\",\"push\",\"push\",\"getMin\"], [[],[1],[2],[]]",
                                "expectedOutput": "[null,null,null,1]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "[\"MinStack\",\"push\",\"pop\",\"getMin\"], [[],[5],[],[]]",
                                "expectedOutput": "[null,null,null,0]"
                        },
                        {
                                "input": "[\"MinStack\",\"push\",\"push\",\"top\"], [[],[10],[20],[]]",
                                "expectedOutput": "[null,null,null,20]"
                        }
                ]
        },
        {
                "id": "dsa_bt_01",
                "topicId": "dsa_binary_trees",
                "topicName": "Binary Trees",
                "difficulty": "easy",
                "title": "Maximum Depth of Binary Tree",
                "description": "Given the root of a binary tree, return its maximum depth.\n\nA binary tree's maximum depth is the number of nodes along the longest path from the root node down to the farthest leaf node.",
                "examples": [
                        {
                                "input": "root = [3, 9, 20, null, null, 15, 7]",
                                "output": "3",
                                "explanation": "The depth is 3."
                        },
                        {
                                "input": "root = [1, null, 2]",
                                "output": "2",
                                "explanation": "The depth is 2."
                        }
                ],
                "constraints": [
                        "The number of nodes in the tree is in the range [0, 10^4].",
                        "-100 <= Node.val <= 100"
                ],
                "starterCode": {
                        "java": "class TreeNode {\n    int val;\n    TreeNode left;\n    TreeNode right;\n    TreeNode(int val) { this.val = val; }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int maxDepth(TreeNode root) {\n        // Write your solution here\n        return 0;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <algorithm>\nusing namespace std;\n\nstruct TreeNode {\n    int val;\n    TreeNode *left;\n    TreeNode *right;\n    TreeNode(int x) : val(x), left(NULL), right(NULL) {}\n};\n\nint maxDepth(TreeNode* root) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nstruct TreeNode {\n    int val;\n    struct TreeNode *left;\n    struct TreeNode *right;\n};\n\nint maxDepth(struct TreeNode* root) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class TreeNode:\n    def __init__(self, val=0, left=None, right=None):\n        self.val = val\n        self.left = left\n        self.right = right\n\ndef maxDepth(root):\n    # Write your solution here\n    return 0\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "root = [3, 9, 20, null, null, 15, 7]",
                                "expectedOutput": "3"
                        },
                        {
                                "input": "root = [1, null, 2]",
                                "expectedOutput": "2"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "root = []",
                                "expectedOutput": "0"
                        },
                        {
                                "input": "root = [0]",
                                "expectedOutput": "1"
                        }
                ]
        },
        {
                "id": "dsa_bt_02",
                "topicId": "dsa_binary_trees",
                "topicName": "Binary Trees",
                "difficulty": "medium",
                "title": "Binary Tree Level Order Traversal",
                "description": "Given the root of a binary tree, return the level order traversal of its nodes' values. (i.e., from left to right, level by level).",
                "examples": [
                        {
                                "input": "root = [3, 9, 20, null, null, 15, 7]",
                                "output": "[[3], [9, 20], [15, 7]]",
                                "explanation": "Values are grouped by tree levels."
                        },
                        {
                                "input": "root = [1]",
                                "output": "[[1]]",
                                "explanation": "Single root level."
                        }
                ],
                "constraints": [
                        "The number of nodes in the tree is in the range [0, 2000].",
                        "-1000 <= Node.val <= 1000"
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\nclass TreeNode {\n    int val;\n    TreeNode left;\n    TreeNode right;\n    TreeNode(int val) { this.val = val; }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static List<List<Integer>> levelOrder(TreeNode root) {\n        // Write your solution here\n        return new ArrayList<>();\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\n#include <queue>\nusing namespace std;\n\nstruct TreeNode {\n    int val;\n    TreeNode *left;\n    TreeNode *right;\n    TreeNode(int x) : val(x), left(NULL), right(NULL) {}\n};\n\nvector<vector<int>> levelOrder(TreeNode* root) {\n    // Write your solution here\n    return {};\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nstruct TreeNode {\n    int val;\n    struct TreeNode *left;\n    struct TreeNode *right;\n};\n\nint** levelOrder(struct TreeNode* root, int* returnSize, int** returnColumnSizes) {\n    // Write your solution here\n    *returnSize = 0;\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class TreeNode:\n    def __init__(self, val=0, left=None, right=None):\n        self.val = val\n        self.left = left\n        self.right = right\n\ndef levelOrder(root):\n    # Write your solution here\n    return []\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "root = [3, 9, 20, null, null, 15, 7]",
                                "expectedOutput": "[[3], [9, 20], [15, 7]]"
                        },
                        {
                                "input": "root = [1]",
                                "expectedOutput": "[[1]]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "root = []",
                                "expectedOutput": "[]"
                        }
                ]
        },
        {
                "id": "dsa_bst_01",
                "topicId": "dsa_bst",
                "topicName": "Binary Search Trees (BST)",
                "difficulty": "easy",
                "title": "Search in a Binary Search Tree",
                "description": "You are given the root of a binary search tree (BST) and an integer `val`.\n\nFind the node in the BST that the node's value equals `val` and return the subtree rooted with that node. If such a node does not exist, return `null`.",
                "examples": [
                        {
                                "input": "root = [4, 2, 7, 1, 3], val = 2",
                                "output": "[2, 1, 3]",
                                "explanation": "Node with value 2 is returned."
                        },
                        {
                                "input": "root = [4, 2, 7, 1, 3], val = 5",
                                "output": "[]",
                                "explanation": "Node 5 does not exist."
                        }
                ],
                "constraints": [
                        "The number of nodes in the tree is in the range [1, 5000].",
                        "1 <= Node.val <= 10^7",
                        "`root` is a valid binary search tree.",
                        "1 <= val <= 10^7"
                ],
                "starterCode": {
                        "java": "class TreeNode {\n    int val;\n    TreeNode left;\n    TreeNode right;\n    TreeNode(int val) { this.val = val; }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static TreeNode searchBST(TreeNode root, int val) {\n        // Write your solution here\n        return null;\n    }\n}",
                        "cpp": "#include <iostream>\nusing namespace std;\n\nstruct TreeNode {\n    int val;\n    TreeNode *left;\n    TreeNode *right;\n    TreeNode(int x) : val(x), left(NULL), right(NULL) {}\n};\n\nTreeNode* searchBST(TreeNode* root, int val) {\n    // Write your solution here\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nstruct TreeNode {\n    int val;\n    struct TreeNode *left;\n    struct TreeNode *right;\n};\n\nstruct TreeNode* searchBST(struct TreeNode* root, int val) {\n    // Write your solution here\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class TreeNode:\n    def __init__(self, val=0, left=None, right=None):\n        self.val = val\n        self.left = left\n        self.right = right\n\ndef searchBST(root, val):\n    # Write your solution here\n    return None\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "root = [4, 2, 7, 1, 3], val = 2",
                                "expectedOutput": "[2, 1, 3]"
                        },
                        {
                                "input": "root = [4, 2, 7, 1, 3], val = 5",
                                "expectedOutput": "[]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "root = [18, 2, 22, null, null, null, 63, null, 84], val = 63",
                                "expectedOutput": "[63, null, 84]"
                        }
                ]
        },
        {
                "id": "dsa_bst_02",
                "topicId": "dsa_bst",
                "topicName": "Binary Search Trees (BST)",
                "difficulty": "medium",
                "title": "Validate Binary Search Tree",
                "description": "Given the root of a binary tree, determine if it is a valid binary search tree (BST).\n\nA valid BST is defined as follows:\n- The left subtree of a node contains only nodes with keys less than the node's key.\n- The right subtree of a node contains only nodes with keys greater than the node's key.\n- Both the left and right subtrees must also be binary search trees.",
                "examples": [
                        {
                                "input": "root = [2, 1, 3]",
                                "output": "true",
                                "explanation": "Valid binary search tree."
                        },
                        {
                                "input": "root = [5, 1, 4, null, null, 3, 6]",
                                "output": "false",
                                "explanation": "Root's value is 5 but its right child's value is 4."
                        }
                ],
                "constraints": [
                        "The number of nodes in the tree is in the range [1, 10^4].",
                        "-2^31 <= Node.val <= 2^31 - 1"
                ],
                "starterCode": {
                        "java": "class TreeNode {\n    int val;\n    TreeNode left;\n    TreeNode right;\n    TreeNode(int val) { this.val = val; }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static boolean isValidBST(TreeNode root) {\n        // Write your solution here\n        return false;\n    }\n}",
                        "cpp": "#include <iostream>\nusing namespace std;\n\nstruct TreeNode {\n    int val;\n    TreeNode *left;\n    TreeNode *right;\n    TreeNode(int x) : val(x), left(NULL), right(NULL) {}\n};\n\nbool isValidBST(TreeNode* root) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdbool.h>\n\nstruct TreeNode {\n    int val;\n    struct TreeNode *left;\n    struct TreeNode *right;\n};\n\nbool isValidBST(struct TreeNode* root) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class TreeNode:\n    def __init__(self, val=0, left=None, right=None):\n        self.val = val\n        self.left = left\n        self.right = right\n\ndef isValidBST(root):\n    # Write your solution here\n    return False\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "root = [2, 1, 3]",
                                "expectedOutput": "true"
                        },
                        {
                                "input": "root = [5, 1, 4, null, null, 3, 6]",
                                "expectedOutput": "false"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "root = [2, 2, 2]",
                                "expectedOutput": "false"
                        },
                        {
                                "input": "root = [1]",
                                "expectedOutput": "true"
                        }
                ]
        },
        {
                "id": "dsa_rec_01",
                "topicId": "dsa_recursion_backtracking",
                "topicName": "Recursion & Backtracking",
                "difficulty": "easy",
                "title": "Fibonacci Number",
                "description": "The Fibonacci numbers, commonly denoted `F(n)` form a sequence, called the Fibonacci sequence, such that each number is the sum of the two preceding ones, starting from 0 and 1.\n\nGiven `n`, calculate `F(n)`.",
                "examples": [
                        {
                                "input": "n = 2",
                                "output": "1",
                                "explanation": "F(2) = F(1) + F(0) = 1 + 0 = 1."
                        },
                        {
                                "input": "n = 3",
                                "output": "2",
                                "explanation": "F(3) = F(2) + F(1) = 1 + 1 = 2."
                        },
                        {
                                "input": "n = 4",
                                "output": "3",
                                "explanation": "F(4) = F(3) + F(2) = 2 + 1 = 3."
                        }
                ],
                "constraints": [
                        "0 <= n <= 30"
                ],
                "starterCode": {
                        "java": "public class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int fib(int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                        "cpp": "#include <iostream>\nusing namespace std;\n\nint fib(int n) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n\nint fib(int n) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def fib(n):\n    # Write your solution here\n    return 0\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "n = 2",
                                "expectedOutput": "1"
                        },
                        {
                                "input": "n = 3",
                                "expectedOutput": "2"
                        },
                        {
                                "input": "n = 4",
                                "expectedOutput": "3"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "n = 0",
                                "expectedOutput": "0"
                        },
                        {
                                "input": "n = 1",
                                "expectedOutput": "1"
                        },
                        {
                                "input": "n = 10",
                                "expectedOutput": "55"
                        }
                ]
        },
        {
                "id": "dsa_rec_02",
                "topicId": "dsa_recursion_backtracking",
                "topicName": "Recursion & Backtracking",
                "difficulty": "medium",
                "title": "Generate Parentheses",
                "description": "Given `n` pairs of parentheses, write a function to generate all combinations of well-formed parentheses.",
                "examples": [
                        {
                                "input": "n = 3",
                                "output": "[\"((()))\", \"(()())\", \"(())()\", \"()(())\", \"()()()\"]",
                                "explanation": "All 5 valid combinations for 3 pairs."
                        },
                        {
                                "input": "n = 1",
                                "output": "[\"()\"]",
                                "explanation": "Single valid pair."
                        }
                ],
                "constraints": [
                        "1 <= n <= 8"
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static List<String> generateParenthesis(int n) {\n        // Write your solution here\n        return new ArrayList<>();\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\n#include <string>\nusing namespace std;\n\nvector<string> generateParenthesis(int n) {\n    // Write your solution here\n    return {};\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nchar** generateParenthesis(int n, int* returnSize) {\n    // Write your solution here\n    *returnSize = 0;\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def generateParenthesis(n):\n    # Write your solution here\n    return []\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "n = 3",
                                "expectedOutput": "[\"((()))\", \"(()())\", \"(())()\", \"()(())\", \"()()()\"]"
                        },
                        {
                                "input": "n = 1",
                                "expectedOutput": "[\"()\"]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "n = 2",
                                "expectedOutput": "[\"(())\", \"()()\"]"
                        }
                ]
        },
        {
                "id": "dsa_dp_01",
                "topicId": "dsa_dynamic_programming",
                "topicName": "Dynamic Programming (DP)",
                "difficulty": "easy",
                "title": "Climbing Stairs",
                "description": "You are climbing a staircase. It takes `n` steps to reach the top.\n\nEach time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?",
                "examples": [
                        {
                                "input": "n = 2",
                                "output": "2",
                                "explanation": "There are two ways to climb: (1 step + 1 step) or (2 steps)."
                        },
                        {
                                "input": "n = 3",
                                "output": "3",
                                "explanation": "Three ways: (1+1+1), (1+2), or (2+1)."
                        }
                ],
                "constraints": [
                        "1 <= n <= 45"
                ],
                "starterCode": {
                        "java": "public class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int climbStairs(int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                        "cpp": "#include <iostream>\nusing namespace std;\n\nint climbStairs(int n) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n\nint climbStairs(int n) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def climbStairs(n):\n    # Write your solution here\n    return 0\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "n = 2",
                                "expectedOutput": "2"
                        },
                        {
                                "input": "n = 3",
                                "expectedOutput": "3"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "n = 1",
                                "expectedOutput": "1"
                        },
                        {
                                "input": "n = 4",
                                "expectedOutput": "5"
                        },
                        {
                                "input": "n = 5",
                                "expectedOutput": "8"
                        }
                ]
        },
        {
                "id": "dsa_dp_02",
                "topicId": "dsa_dynamic_programming",
                "topicName": "Dynamic Programming (DP)",
                "difficulty": "medium",
                "title": "Coin Change",
                "description": "You are given an integer array `coins` representing coins of different denominations and an integer `amount` representing a total amount of money.\n\nReturn the fewest number of coins that you need to make up that amount. If that amount of money cannot be made up by any combination of the coins, return `-1`.",
                "examples": [
                        {
                                "input": "coins = [1, 2, 5], amount = 11",
                                "output": "3",
                                "explanation": "11 = 5 + 5 + 1 (3 coins)."
                        },
                        {
                                "input": "coins = [2], amount = 3",
                                "output": "-1",
                                "explanation": "Amount 3 cannot be made with only 2-cent coins."
                        },
                        {
                                "input": "coins = [1], amount = 0",
                                "output": "0",
                                "explanation": "0 coins needed for 0 amount."
                        }
                ],
                "constraints": [
                        "1 <= coins.length <= 12",
                        "1 <= coins[i] <= 2^31 - 1",
                        "0 <= amount <= 10^4"
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int coinChange(int[] coins, int amount) {\n        // Write your solution here\n        return -1;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\nusing namespace std;\n\nint coinChange(vector<int>& coins, int amount) {\n    // Write your solution here\n    return -1;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n\nint coinChange(int* coins, int coinsSize, int amount) {\n    // Write your solution here\n    return -1;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def coinChange(coins, amount):\n    # Write your solution here\n    return -1\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "coins = [1, 2, 5], amount = 11",
                                "expectedOutput": "3"
                        },
                        {
                                "input": "coins = [2], amount = 3",
                                "expectedOutput": "-1"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "coins = [1], amount = 0",
                                "expectedOutput": "0"
                        },
                        {
                                "input": "coins = [186, 419, 83, 408], amount = 6249",
                                "expectedOutput": "20"
                        }
                ]
        },
        {
                "id": "dsa_graph_01",
                "topicId": "dsa_graphs",
                "topicName": "Graphs",
                "difficulty": "easy",
                "title": "Find if Path Exists in Graph",
                "description": "There is a bi-directional graph with `n` vertices, where each vertex is labeled from `0` to `n - 1`.\n\nGiven edges and the vertices `source` and `destination`, return `true` if there is a valid path from `source` to `destination`, or `false` otherwise.",
                "examples": [
                        {
                                "input": "n = 3, edges = [[0,1],[1,2],[2,0]], source = 0, destination = 2",
                                "output": "true",
                                "explanation": "There is a path 0 -> 1 -> 2 as well as 0 -> 2."
                        },
                        {
                                "input": "n = 6, edges = [[0,1],[0,2],[3,5],[5,4],[4,3]], source = 0, destination = 5",
                                "output": "false",
                                "explanation": "There is no path from vertex 0 to vertex 5."
                        }
                ],
                "constraints": [
                        "1 <= n <= 2 * 10^5",
                        "0 <= edges.length <= 2 * 10^5",
                        "edges[i].length == 2",
                        "0 <= ui, vi <= n - 1",
                        "ui != vi",
                        "0 <= source, destination <= n - 1"
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static boolean validPath(int n, int[][] edges, int source, int destination) {\n        // Write your solution here\n        return false;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\nusing namespace std;\n\nbool validPath(int n, vector<vector<int>>& edges, int source, int destination) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdbool.h>\n\nbool validPath(int n, int** edges, int edgesSize, int* edgesColSize, int source, int destination) {\n    // Write your solution here\n    return false;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def validPath(n, edges, source, destination):\n    # Write your solution here\n    return False\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "n = 3, edges = [[0,1],[1,2],[2,0]], source = 0, destination = 2",
                                "expectedOutput": "true"
                        },
                        {
                                "input": "n = 6, edges = [[0,1],[0,2],[3,5],[5,4],[4,3]], source = 0, destination = 5",
                                "expectedOutput": "false"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "n = 1, edges = [], source = 0, destination = 0",
                                "expectedOutput": "true"
                        }
                ]
        },
        {
                "id": "dsa_graph_02",
                "topicId": "dsa_graphs",
                "topicName": "Graphs",
                "difficulty": "medium",
                "title": "Number of Islands",
                "description": "Given an `m x n` 2D binary grid `grid` which represents a map of '1's (land) and '0's (water), return the number of islands.\n\nAn island is surrounded by water and is formed by connecting adjacent lands horizontally or vertically.",
                "examples": [
                        {
                                "input": "grid = [[\"1\",\"1\",\"1\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"0\",\"0\"]]",
                                "output": "1",
                                "explanation": "All 1s are connected to form 1 island."
                        },
                        {
                                "input": "grid = [[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"1\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"1\",\"1\"]]",
                                "output": "3",
                                "explanation": "There are 3 separate islands."
                        }
                ],
                "constraints": [
                        "m == grid.length",
                        "n == grid[i].length",
                        "1 <= m, n <= 300",
                        "grid[i][j] is '0' or '1'."
                ],
                "starterCode": {
                        "java": "public class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int numIslands(char[][] grid) {\n        // Write your solution here\n        return 0;\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\nusing namespace std;\n\nint numIslands(vector<vector<char>>& grid) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n\nint numIslands(char** grid, int gridSize, int* gridColSize) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def numIslands(grid):\n    # Write your solution here\n    return 0\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "grid = [[\"1\",\"1\",\"1\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"0\",\"0\"]]",
                                "expectedOutput": "1"
                        },
                        {
                                "input": "grid = [[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"1\",\"1\",\"0\",\"0\",\"0\"],[\"0\",\"0\",\"1\",\"0\",\"0\"],[\"0\",\"0\",\"0\",\"1\",\"1\"]]",
                                "expectedOutput": "3"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "grid = [[\"1\"]]",
                                "expectedOutput": "1"
                        },
                        {
                                "input": "grid = [[\"0\"]]",
                                "expectedOutput": "0"
                        }
                ]
        },
        {
                "id": "dsa_heap_01",
                "topicId": "dsa_heap_priority_queue",
                "topicName": "Heap / Priority Queue",
                "difficulty": "easy",
                "title": "Kth Largest Element in a Stream",
                "description": "Design a class to find the `k-th` largest element in a stream. Note that it is the `k-th` largest element in the sorted order, not the `k-th` distinct element.\n\nImplement `KthLargest` class:\n- `KthLargest(int k, int[] nums)` Initializes the object with the integer `k` and the stream of integers `nums`.\n- `int add(int val)` Appends the integer `val` to the stream and returns the element representing the `k-th` largest element in the stream.",
                "examples": [
                        {
                                "input": "[\"KthLargest\", \"add\", \"add\", \"add\", \"add\", \"add\"], [[3, [4, 5, 8, 2]], [3], [5], [10], [9], [4]]",
                                "output": "[null, 4, 5, 5, 8, 8]",
                                "explanation": "The 3rd largest elements returned."
                        }
                ],
                "constraints": [
                        "1 <= k <= 10^4",
                        "0 <= nums.length <= 10^4",
                        "-10^4 <= nums[i] <= 10^4",
                        "-10^4 <= val <= 10^4",
                        "At most 10^4 calls will be made to add."
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\nclass KthLargest {\n    public KthLargest(int k, int[] nums) {\n        // Initialize your data structure here\n    }\n    \n    public int add(int val) {\n        // Write your solution here\n        return 0;\n    }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\n#include <queue>\nusing namespace std;\n\nclass KthLargest {\npublic:\n    KthLargest(int k, vector<int>& nums) {\n        // Initialize your data structure here\n    }\n    \n    int add(int val) {\n        // Write your solution here\n        return 0;\n    }\n};\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\ntypedef struct {\n    // Define struct members\n} KthLargest;\n\nKthLargest* kthLargestCreate(int k, int* nums, int numsSize) {\n    // Initialize your data structure here\n    return NULL;\n}\n\nint kthLargestAdd(KthLargest* obj, int val) {\n    // Write your solution here\n    return 0;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "class KthLargest:\n    def __init__(self, k: int, nums: list[int]):\n        # Initialize your data structure here\n        pass\n\n    def add(self, val: int) -> int:\n        # Write your solution here\n        return 0\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "[\"KthLargest\", \"add\", \"add\"], [[2, [0]], [-1], [1]]",
                                "expectedOutput": "[null, -1, 0]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "[\"KthLargest\", \"add\"], [[1, []], [-3]]",
                                "expectedOutput": "[null, -3]"
                        }
                ]
        },
        {
                "id": "dsa_heap_02",
                "topicId": "dsa_heap_priority_queue",
                "topicName": "Heap / Priority Queue",
                "difficulty": "medium",
                "title": "Top K Frequent Elements",
                "description": "Given an integer array `nums` and an integer `k`, return the `k` most frequent elements. You may return the answer in any order.",
                "examples": [
                        {
                                "input": "nums = [1,1,1,2,2,3], k = 2",
                                "output": "[1, 2]",
                                "explanation": "1 occurs 3 times, 2 occurs 2 times."
                        },
                        {
                                "input": "nums = [1], k = 1",
                                "output": "[1]",
                                "explanation": "Single element."
                        }
                ],
                "constraints": [
                        "1 <= nums.length <= 10^5",
                        "-10^4 <= nums[i] <= 10^4",
                        "`k` is in the range [1, the number of unique elements in the array].",
                        "It is guaranteed that the answer is unique."
                ],
                "starterCode": {
                        "java": "import java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n\n    public static int[] topKFrequent(int[] nums, int k) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                        "cpp": "#include <iostream>\n#include <vector>\n#include <queue>\n#include <unordered_map>\nusing namespace std;\n\nvector<int> topKFrequent(vector<int>& nums, int k) {\n    // Write your solution here\n    return {};\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "c": "#include <stdio.h>\n#include <stdlib.h>\n\nint* topKFrequent(int* nums, int numsSize, int k, int* returnSize) {\n    // Write your solution here\n    *returnSize = 0;\n    return NULL;\n}\n\nint main() {\n    // Write your solution here\n    return 0;\n}",
                        "python": "def topKFrequent(nums, k):\n    # Write your solution here\n    return []\n\nif __name__ == '__main__':\n    pass"
                },
                "sampleTestCases": [
                        {
                                "input": "nums = [1,1,1,2,2,3], k = 2",
                                "expectedOutput": "[1, 2]"
                        },
                        {
                                "input": "nums = [1], k = 1",
                                "expectedOutput": "[1]"
                        }
                ],
                "hiddenTestCases": [
                        {
                                "input": "nums = [4,1,-1,2,-1,2,3], k = 2",
                                "expectedOutput": "[-1, 2]"
                        }
                ]
        }
];

    const MockDsaQuestions = {
        /**
         * Get all available DSA questions
         */
        getAllQuestions: function () {
            return DSA_QUESTIONS;
        },

        /**
         * Get questions for a specific DSA topic
         */
        getQuestionsByTopic: function (topicId) {
            return DSA_QUESTIONS.filter(q => q.topicId === topicId);
        },

        /**
         * Get a specific question by ID
         */
        getQuestionById: function (id) {
            return DSA_QUESTIONS.find(q => q.id === id) || null;
        }
    };

    // Expose MockDsaQuestions globally
    window.MockDsaQuestions = MockDsaQuestions;
})();
