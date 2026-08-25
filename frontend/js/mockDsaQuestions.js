/**
 * Mock DSA Questions Repository
 * 
 * Provides a categorized repository of Easy and Medium coding problems
 * across the 10 frozen DSA topics.
 * 
 * Later this will be replaced by Spring Boot REST API calls to MySQL.
 */

(function () {
    const DSA_QUESTIONS = [
        // =========================================================================
        // 1. ARRAYS (dsa_arrays)
        // =========================================================================
        {
            id: "dsa_arr_01",
            topicId: "dsa_arrays",
            topicName: "Arrays",
            difficulty: "easy",
            title: "Two Sum",
            description: "Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.",
            examples: [
                {
                    input: "nums = [2, 7, 11, 15], target = 9",
                    output: "[0, 1]",
                    explanation: "Because nums[0] + nums[1] == 9, we return [0, 1]."
                },
                {
                    input: "nums = [3, 2, 4], target = 6",
                    output: "[1, 2]",
                    explanation: "Because nums[1] + nums[2] == 6, we return [1, 2]."
                }
            ],
            constraints: [
                "2 <= nums.length <= 10^4",
                "-10^9 <= nums[i] <= 10^9",
                "-10^9 <= target <= 10^9",
                "Only one valid answer exists."
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n        int[] nums = {2, 7, 11, 15};\n        int target = 9;\n        int[] result = twoSum(nums, target);\n        System.out.println(\"[\" + result[0] + \", \" + result[1] + \"]\");\n    }\n\n    public static int[] twoSum(int[] nums, int target) {\n        // Your algorithm here\n        return new int[]{0, 1};\n    }\n}"
            },
            sampleTestCases: [
                { input: "nums = [2, 7, 11, 15], target = 9", expectedOutput: "[0, 1]" },
                { input: "nums = [3, 2, 4], target = 6", expectedOutput: "[1, 2]" }
            ],
            hiddenTestCases: [
                { input: "nums = [3, 3], target = 6", expectedOutput: "[0, 1]" },
                { input: "nums = [-1, -2, -3, -4, -5], target = -8", expectedOutput: "[2, 4]" },
                { input: "nums = [0, 4, 3, 0], target = 0", expectedOutput: "[0, 3]" }
            ]
        },
        {
            id: "dsa_arr_02",
            topicId: "dsa_arrays",
            topicName: "Arrays",
            difficulty: "medium",
            title: "Container With Most Water",
            description: "You are given an integer array `height` of length `n`. There are `n` vertical lines drawn such that the two endpoints of the `i-th` line are `(i, 0)` and `(i, height[i])`.\n\nFind two lines that together with the x-axis form a container, such that the container contains the most water.\n\nReturn the maximum amount of water a container can store.",
            examples: [
                {
                    input: "height = [1, 8, 6, 2, 5, 4, 8, 3, 7]",
                    output: "49",
                    explanation: "The vertical lines are at indices 1 and 8, width is 7, height is 7. Max Area = 7 * 7 = 49."
                },
                {
                    input: "height = [1, 1]",
                    output: "1",
                    explanation: "Max Area = 1 * 1 = 1."
                }
            ],
            constraints: [
                "n == height.length",
                "2 <= n <= 10^5",
                "0 <= height[i] <= 10^4"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        int[] height = {1, 8, 6, 2, 5, 4, 8, 3, 7};\n        System.out.println(maxArea(height));\n    }\n\n    public static int maxArea(int[] height) {\n        // Write your solution here\n        return 49;\n    }\n}"
            },
            sampleTestCases: [
                { input: "height = [1, 8, 6, 2, 5, 4, 8, 3, 7]", expectedOutput: "49" },
                { input: "height = [1, 1]", expectedOutput: "1" }
            ],
            hiddenTestCases: [
                { input: "height = [4, 3, 2, 1, 4]", expectedOutput: "16" },
                { input: "height = [1, 2, 1]", expectedOutput: "2" },
                { input: "height = [2, 3, 4, 5, 18, 17, 6]", expectedOutput: "17" }
            ]
        },

        // =========================================================================
        // 2. STRINGS (dsa_strings)
        // =========================================================================
        {
            id: "dsa_str_01",
            topicId: "dsa_strings",
            topicName: "Strings",
            difficulty: "easy",
            title: "Valid Palindrome",
            description: "A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward.\n\nGiven a string `s`, return `true` if it is a palindrome, or `false` otherwise.",
            examples: [
                {
                    input: 's = "A man, a plan, a canal: Panama"',
                    output: "true",
                    explanation: '"amanaplanacanalpanama" is a palindrome.'
                },
                {
                    input: 's = "race a car"',
                    output: "false",
                    explanation: '"raceacar" is not a palindrome.'
                }
            ],
            constraints: [
                "1 <= s.length <= 2 * 10^5",
                "`s` consists only of printable ASCII characters."
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        String s = \"A man, a plan, a canal: Panama\";\n        System.out.println(isPalindrome(s));\n    }\n\n    public static boolean isPalindrome(String s) {\n        // Write your solution here\n        return true;\n    }\n}"
            },
            sampleTestCases: [
                { input: 's = "A man, a plan, a canal: Panama"', expectedOutput: "true" },
                { input: 's = "race a car"', expectedOutput: "false" }
            ],
            hiddenTestCases: [
                { input: 's = " "', expectedOutput: "true" },
                { input: 's = "0P"', expectedOutput: "false" },
                { input: 's = "ab_a"', expectedOutput: "true" }
            ]
        },
        {
            id: "dsa_str_02",
            topicId: "dsa_strings",
            topicName: "Strings",
            difficulty: "medium",
            title: "Longest Substring Without Repeating Characters",
            description: "Given a string `s`, find the length of the longest substring without repeating characters.",
            examples: [
                {
                    input: 's = "abcabcbb"',
                    output: "3",
                    explanation: 'The answer is "abc", with the length of 3.'
                },
                {
                    input: 's = "bbbbb"',
                    output: "1",
                    explanation: 'The answer is "b", with the length of 1.'
                }
            ],
            constraints: [
                "0 <= s.length <= 5 * 10^4",
                "`s` consists of English letters, digits, symbols and spaces."
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        String s = \"abcabcbb\";\n        System.out.println(lengthOfLongestSubstring(s));\n    }\n\n    public static int lengthOfLongestSubstring(String s) {\n        // Write your solution here\n        return 3;\n    }\n}"
            },
            sampleTestCases: [
                { input: 's = "abcabcbb"', expectedOutput: "3" },
                { input: 's = "bbbbb"', expectedOutput: "1" }
            ],
            hiddenTestCases: [
                { input: 's = "pwwkew"', expectedOutput: "3" },
                { input: 's = ""', expectedOutput: "0" },
                { input: 's = "au"', expectedOutput: "2" }
            ]
        },

        // =========================================================================
        // 3. LINKED LISTS (dsa_linked_lists)
        // =========================================================================
        {
            id: "dsa_ll_01",
            topicId: "dsa_linked_lists",
            topicName: "Linked Lists",
            difficulty: "easy",
            title: "Reverse Linked List",
            description: "Given the head of a singly linked list, reverse the list, and return the reversed list values.",
            examples: [
                {
                    input: "head = [1, 2, 3, 4, 5]",
                    output: "[5, 4, 3, 2, 1]",
                    explanation: "The list is completely reversed."
                },
                {
                    input: "head = [1, 2]",
                    output: "[2, 1]",
                    explanation: "Nodes are swapped."
                }
            ],
            constraints: [
                "The number of nodes in the list is in the range [0, 5000].",
                "-5000 <= Node.val <= 5000"
            ],
            starterCode: {
                java: "class ListNode {\n    int val;\n    ListNode next;\n    ListNode(int val) { this.val = val; }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        // Write your test and solution here\n        System.out.println(\"[5, 4, 3, 2, 1]\");\n    }\n\n    public static ListNode reverseList(ListNode head) {\n        ListNode prev = null;\n        ListNode curr = head;\n        while (curr != null) {\n            ListNode nextTemp = curr.next;\n            curr.next = prev;\n            prev = curr;\n            curr = nextTemp;\n        }\n        return prev;\n    }\n}"
            },
            sampleTestCases: [
                { input: "head = [1, 2, 3, 4, 5]", expectedOutput: "[5, 4, 3, 2, 1]" },
                { input: "head = [1, 2]", expectedOutput: "[2, 1]" }
            ],
            hiddenTestCases: [
                { input: "head = []", expectedOutput: "[]" },
                { input: "head = [1]", expectedOutput: "[1]" }
            ]
        },
        {
            id: "dsa_ll_02",
            topicId: "dsa_linked_lists",
            topicName: "Linked Lists",
            difficulty: "medium",
            title: "Remove N-th Node From End of List",
            description: "Given the head of a linked list, remove the `n-th` node from the end of the list and return its head.",
            examples: [
                {
                    input: "head = [1, 2, 3, 4, 5], n = 2",
                    output: "[1, 2, 3, 5]",
                    explanation: "The 2nd node from end is 4, which is removed."
                },
                {
                    input: "head = [1], n = 1",
                    output: "[]",
                    explanation: "The only node is removed."
                }
            ],
            constraints: [
                "The number of nodes in the list is `sz`.",
                "1 <= sz <= 30",
                "0 <= Node.val <= 100",
                "1 <= n <= sz"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"[1, 2, 3, 5]\");\n    }\n}"
            },
            sampleTestCases: [
                { input: "head = [1, 2, 3, 4, 5], n = 2", expectedOutput: "[1, 2, 3, 5]" },
                { input: "head = [1], n = 1", expectedOutput: "[]" }
            ],
            hiddenTestCases: [
                { input: "head = [1, 2], n = 1", expectedOutput: "[1]" },
                { input: "head = [1, 2], n = 2", expectedOutput: "[2]" }
            ]
        },

        // =========================================================================
        // 4. STACK & QUEUE (dsa_stack_queue)
        // =========================================================================
        {
            id: "dsa_sq_01",
            topicId: "dsa_stack_queue",
            topicName: "Stack & Queue",
            difficulty: "easy",
            title: "Valid Parentheses",
            description: "Given a string `s` containing just the characters `'('`, `')'`, `'{'`, `'}'`, `'['` and `']'`, determine if the input string is valid.\n\nAn input string is valid if open brackets are closed by the same type of brackets in the correct order.",
            examples: [
                {
                    input: 's = "()"',
                    output: "true",
                    explanation: "Valid simple bracket pair."
                },
                {
                    input: 's = "()[]{}"',
                    output: "true",
                    explanation: "All brackets properly matched."
                },
                {
                    input: 's = "(]"',
                    output: "false",
                    explanation: "Mismatched bracket types."
                }
            ],
            constraints: [
                "1 <= s.length <= 10^4",
                "`s` consists of parentheses only '()[]{}'."
            ],
            starterCode: {
                java: "import java.util.Stack;\n\npublic class Main {\n    public static void main(String[] args) {\n        String s = \"()[]{}\";\n        System.out.println(isValid(s));\n    }\n\n    public static boolean isValid(String s) {\n        // Write your solution here\n        return true;\n    }\n}"
            },
            sampleTestCases: [
                { input: 's = "()"', expectedOutput: "true" },
                { input: 's = "()[]{}"', expectedOutput: "true" }
            ],
            hiddenTestCases: [
                { input: 's = "(]"', expectedOutput: "false" },
                { input: 's = "([)]"', expectedOutput: "false" },
                { input: 's = "{[]}"', expectedOutput: "true" }
            ]
        },
        {
            id: "dsa_sq_02",
            topicId: "dsa_stack_queue",
            topicName: "Stack & Queue",
            difficulty: "medium",
            title: "Daily Temperatures",
            description: "Given an array of integers `temperatures` represents the daily temperatures, return an array `answer` such that `answer[i]` is the number of days you have to wait after the `i-th` day to get a warmer temperature. If there is no future day, keep `answer[i] == 0`.",
            examples: [
                {
                    input: "temperatures = [73, 74, 75, 71, 69, 72, 76, 73]",
                    output: "[1, 1, 4, 2, 1, 1, 0, 0]",
                    explanation: "Monotonic stack evaluates future warmer days."
                },
                {
                    input: "temperatures = [30, 40, 50, 60]",
                    output: "[1, 1, 1, 0]",
                    explanation: "Each day is followed by warmer day."
                }
            ],
            constraints: [
                "1 <= temperatures.length <= 10^5",
                "30 <= temperatures[i] <= 100"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"[1, 1, 4, 2, 1, 1, 0, 0]\");\n    }\n}"
            },
            sampleTestCases: [
                { input: "temperatures = [73, 74, 75, 71, 69, 72, 76, 73]", expectedOutput: "[1, 1, 4, 2, 1, 1, 0, 0]" },
                { input: "temperatures = [30, 40, 50, 60]", expectedOutput: "[1, 1, 1, 0]" }
            ],
            hiddenTestCases: [
                { input: "temperatures = [30, 60, 90]", expectedOutput: "[1, 1, 0]" },
                { input: "temperatures = [90, 80, 70]", expectedOutput: "[0, 0, 0]" }
            ]
        },

        // =========================================================================
        // 5. BINARY TREES (dsa_binary_trees)
        // =========================================================================
        {
            id: "dsa_bt_01",
            topicId: "dsa_binary_trees",
            topicName: "Binary Trees",
            difficulty: "easy",
            title: "Maximum Depth of Binary Tree",
            description: "Given the `root` of a binary tree, return its maximum depth.\n\nA binary tree's maximum depth is the number of nodes along the longest path from the root node down to the farthest leaf node.",
            examples: [
                {
                    input: "root = [3, 9, 20, null, null, 15, 7]",
                    output: "3",
                    explanation: "The longest path is 3 -> 20 -> 7, depth is 3."
                },
                {
                    input: "root = [1, null, 2]",
                    output: "2",
                    explanation: "Depth is 2."
                }
            ],
            constraints: [
                "The number of nodes in the tree is in the range [0, 10^4].",
                "-100 <= Node.val <= 100"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(3);\n    }\n}"
            },
            sampleTestCases: [
                { input: "root = [3, 9, 20, null, null, 15, 7]", expectedOutput: "3" },
                { input: "root = [1, null, 2]", expectedOutput: "2" }
            ],
            hiddenTestCases: [
                { input: "root = []", expectedOutput: "0" },
                { input: "root = [0]", expectedOutput: "1" }
            ]
        },
        {
            id: "dsa_bt_02",
            topicId: "dsa_binary_trees",
            topicName: "Binary Trees",
            difficulty: "medium",
            title: "Binary Tree Level Order Traversal",
            description: "Given the `root` of a binary tree, return the level order traversal of its nodes' values (i.e., from left to right, level by level).",
            examples: [
                {
                    input: "root = [3, 9, 20, null, null, 15, 7]",
                    output: "[[3], [9, 20], [15, 7]]",
                    explanation: "Levels: Level 1 has 3, Level 2 has 9, 20, Level 3 has 15, 7."
                }
            ],
            constraints: [
                "The number of nodes in the tree is in the range [0, 2000].",
                "-1000 <= Node.val <= 1000"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"[[3], [9, 20], [15, 7]]\");\n    }\n}"
            },
            sampleTestCases: [
                { input: "root = [3, 9, 20, null, null, 15, 7]", expectedOutput: "[[3], [9, 20], [15, 7]]" },
                { input: "root = [1]", expectedOutput: "[[1]]" }
            ],
            hiddenTestCases: [
                { input: "root = []", expectedOutput: "[]" }
            ]
        },

        // =========================================================================
        // 6. BINARY SEARCH TREES (dsa_bst)
        // =========================================================================
        {
            id: "dsa_bst_01",
            topicId: "dsa_bst",
            topicName: "Binary Search Trees (BST)",
            difficulty: "easy",
            title: "Search in a Binary Search Tree",
            description: "You are given the root of a binary search tree (BST) and an integer `val`.\n\nFind the node in the BST that the node's value equals `val` and return the subtree rooted with that node. If such a node does not exist, return `null`.",
            examples: [
                {
                    input: "root = [4, 2, 7, 1, 3], val = 2",
                    output: "[2, 1, 3]",
                    explanation: "Subtree with value 2 is returned."
                }
            ],
            constraints: [
                "The number of nodes in the tree is in the range [1, 5000].",
                "1 <= Node.val <= 10^7",
                "root is a valid BST."
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"[2, 1, 3]\");\n    }\n}"
            },
            sampleTestCases: [
                { input: "root = [4, 2, 7, 1, 3], val = 2", expectedOutput: "[2, 1, 3]" },
                { input: "root = [4, 2, 7, 1, 3], val = 5", expectedOutput: "[]" }
            ],
            hiddenTestCases: [
                { input: "root = [18, 2, 22, null, null, null, 63], val = 22", expectedOutput: "[22, null, 63]" }
            ]
        },
        {
            id: "dsa_bst_02",
            topicId: "dsa_bst",
            topicName: "Binary Search Trees (BST)",
            difficulty: "medium",
            title: "Validate Binary Search Tree",
            description: "Given the root of a binary tree, determine if it is a valid binary search tree (BST).\n\nA valid BST is defined as follows:\n- The left subtree of a node contains only nodes with keys strictly less than the node's key.\n- The right subtree of a node contains only nodes with keys strictly greater than the node's key.",
            examples: [
                {
                    input: "root = [2, 1, 3]",
                    output: "true",
                    explanation: "Left child 1 < 2 < Right child 3."
                },
                {
                    input: "root = [5, 1, 4, null, null, 3, 6]",
                    output: "false",
                    explanation: "The root node's value is 5 but its right child's value is 4."
                }
            ],
            constraints: [
                "The number of nodes in the tree is in the range [1, 10^4].",
                "-2^31 <= Node.val <= 2^31 - 1"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(true);\n    }\n}"
            },
            sampleTestCases: [
                { input: "root = [2, 1, 3]", expectedOutput: "true" },
                { input: "root = [5, 1, 4, null, null, 3, 6]", expectedOutput: "false" }
            ],
            hiddenTestCases: [
                { input: "root = [2, 2, 2]", expectedOutput: "false" }
            ]
        },

        // =========================================================================
        // 7. RECURSION & BACKTRACKING (dsa_recursion_backtracking)
        // =========================================================================
        {
            id: "dsa_rec_01",
            topicId: "dsa_recursion_backtracking",
            topicName: "Recursion & Backtracking",
            difficulty: "easy",
            title: "Climbing Stairs",
            description: "You are climbing a staircase. It takes `n` steps to reach the top.\n\nEach time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?",
            examples: [
                {
                    input: "n = 2",
                    output: "2",
                    explanation: "There are two ways: 1 step + 1 step, or 2 steps."
                },
                {
                    input: "n = 3",
                    output: "3",
                    explanation: "Three ways: (1+1+1), (1+2), (2+1)."
                }
            ],
            constraints: [
                "1 <= n <= 45"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        int n = 3;\n        System.out.println(climbStairs(n));\n    }\n\n    public static int climbStairs(int n) {\n        if (n <= 2) return n;\n        int a = 1, b = 2;\n        for (int i = 3; i <= n; i++) {\n            int c = a + b;\n            a = b;\n            b = c;\n        }\n        return b;\n    }\n}"
            },
            sampleTestCases: [
                { input: "n = 2", expectedOutput: "2" },
                { input: "n = 3", expectedOutput: "3" }
            ],
            hiddenTestCases: [
                { input: "n = 4", expectedOutput: "5" },
                { input: "n = 5", expectedOutput: "8" }
            ]
        },
        {
            id: "dsa_rec_02",
            topicId: "dsa_recursion_backtracking",
            topicName: "Recursion & Backtracking",
            difficulty: "medium",
            title: "Subsets",
            description: "Given an integer array `nums` of unique elements, return all possible subsets (the power set).\n\nThe solution set must not contain duplicate subsets. Return the solution in any order.",
            examples: [
                {
                    input: "nums = [1, 2, 3]",
                    output: "[[], [1], [2], [1, 2], [3], [1, 3], [2, 3], [1, 2, 3]]",
                    explanation: "All 8 power set combinations."
                }
            ],
            constraints: [
                "1 <= nums.length <= 10",
                "-10 <= nums[i] <= 10"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"[[], [1], [2], [1, 2], [3], [1, 3], [2, 3], [1, 2, 3]]\");\n    }\n}"
            },
            sampleTestCases: [
                { input: "nums = [1, 2, 3]", expectedOutput: "[[], [1], [2], [1, 2], [3], [1, 3], [2, 3], [1, 2, 3]]" },
                { input: "nums = [0]", expectedOutput: "[[], [0]]" }
            ],
            hiddenTestCases: [
                { input: "nums = [1, 2]", expectedOutput: "[[], [1], [2], [1, 2]]" }
            ]
        },

        // =========================================================================
        // 8. DYNAMIC PROGRAMMING (dsa_dynamic_programming)
        // =========================================================================
        {
            id: "dsa_dp_01",
            topicId: "dsa_dynamic_programming",
            topicName: "Dynamic Programming (DP)",
            difficulty: "easy",
            title: "Fibonacci Number",
            description: "The Fibonacci numbers, commonly denoted `F(n)` form a sequence, called the Fibonacci sequence, such that each number is the sum of the two preceding ones, starting from 0 and 1.\n\nGiven `n`, calculate `F(n)`.",
            examples: [
                {
                    input: "n = 2",
                    output: "1",
                    explanation: "F(2) = F(1) + F(0) = 1 + 0 = 1."
                },
                {
                    input: "n = 4",
                    output: "3",
                    explanation: "F(4) = F(3) + F(2) = 2 + 1 = 3."
                }
            ],
            constraints: [
                "0 <= n <= 30"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        int n = 4;\n        System.out.println(fib(n));\n    }\n\n    public static int fib(int n) {\n        if (n <= 1) return n;\n        int a = 0, b = 1;\n        for (int i = 2; i <= n; i++) {\n            int c = a + b;\n            a = b;\n            b = c;\n        }\n        return b;\n    }\n}"
            },
            sampleTestCases: [
                { input: "n = 2", expectedOutput: "1" },
                { input: "n = 4", expectedOutput: "3" }
            ],
            hiddenTestCases: [
                { input: "n = 0", expectedOutput: "0" },
                { input: "n = 1", expectedOutput: "1" },
                { input: "n = 6", expectedOutput: "8" }
            ]
        },
        {
            id: "dsa_dp_02",
            topicId: "dsa_dynamic_programming",
            topicName: "Dynamic Programming (DP)",
            difficulty: "medium",
            title: "Coin Change",
            description: "You are given an integer array `coins` representing coins of different denominations and an integer `amount` representing a total amount of money.\n\nReturn the fewest number of coins that you need to make up that amount. If that amount of money cannot be made up by any combination of the coins, return -1.",
            examples: [
                {
                    input: "coins = [1, 2, 5], amount = 11",
                    output: "3",
                    explanation: "11 = 5 + 5 + 1 (3 coins)."
                },
                {
                    input: "coins = [2], amount = 3",
                    output: "-1",
                    explanation: "Cannot make 3 using only coin of 2."
                }
            ],
            constraints: [
                "1 <= coins.length <= 12",
                "1 <= coins[i] <= 2^31 - 1",
                "0 <= amount <= 10^4"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(3);\n    }\n}"
            },
            sampleTestCases: [
                { input: "coins = [1, 2, 5], amount = 11", expectedOutput: "3" },
                { input: "coins = [2], amount = 3", expectedOutput: "-1" }
            ],
            hiddenTestCases: [
                { input: "coins = [1], amount = 0", expectedOutput: "0" },
                { input: "coins = [186, 419, 83, 408], amount = 6249", expectedOutput: "20" }
            ]
        },

        // =========================================================================
        // 9. GRAPHS (dsa_graphs)
        // =========================================================================
        {
            id: "dsa_graph_01",
            topicId: "dsa_graphs",
            topicName: "Graphs",
            difficulty: "easy",
            title: "Find if Path Exists in Graph",
            description: "There is a bi-directional graph with `n` vertices, where each vertex is labeled from `0` to `n - 1`. Determine if there is a valid path that exists from vertex `source` to vertex `destination`.",
            examples: [
                {
                    input: "n = 3, edges = [[0,1],[1,2],[2,0]], source = 0, destination = 2",
                    output: "true",
                    explanation: "Two paths exist: 0 -> 2, and 0 -> 1 -> 2."
                }
            ],
            constraints: [
                "1 <= n <= 2 * 10^5",
                "0 <= edges.length <= 2 * 10^5"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(true);\n    }\n}"
            },
            sampleTestCases: [
                { input: "n = 3, edges = [[0,1],[1,2],[2,0]], source = 0, destination = 2", expectedOutput: "true" },
                { input: "n = 6, edges = [[0,1],[0,2],[3,5],[5,4],[4,3]], source = 0, destination = 5", expectedOutput: "false" }
            ],
            hiddenTestCases: [
                { input: "n = 1, edges = [], source = 0, destination = 0", expectedOutput: "true" }
            ]
        },
        {
            id: "dsa_graph_02",
            topicId: "dsa_graphs",
            topicName: "Graphs",
            difficulty: "medium",
            title: "Number of Islands",
            description: "Given an `m x n` 2D binary grid `grid` which represents a map of '1's (land) and '0's (water), return the number of islands.\n\nAn island is surrounded by water and is formed by connecting adjacent lands horizontally or vertically.",
            examples: [
                {
                    input: 'grid = [["1","1","1","1","0"],["1","1","0","1","0"],["1","1","0","0","0"],["0","0","0","0","0"]]',
                    output: "1",
                    explanation: "All connected 1s form 1 single island."
                }
            ],
            constraints: [
                "m == grid.length",
                "n == grid[i].length",
                "1 <= m, n <= 300"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(1);\n    }\n}"
            },
            sampleTestCases: [
                { input: 'grid = [["1","1","1","1","0"],["1","1","0","1","0"],["1","1","0","0","0"],["0","0","0","0","0"]]', expectedOutput: "1" },
                { input: 'grid = [["1","1","0","0","0"],["1","1","0","0","0"],["0","0","1","0","0"],["0","0","0","1","1"]]', expectedOutput: "3" }
            ],
            hiddenTestCases: [
                { input: 'grid = [["0"]]', expectedOutput: "0" },
                { input: 'grid = [["1"]]', expectedOutput: "1" }
            ]
        },

        // =========================================================================
        // 10. HEAP / PRIORITY QUEUE (dsa_heap_priority_queue)
        // =========================================================================
        {
            id: "dsa_hp_01",
            topicId: "dsa_heap_priority_queue",
            topicName: "Heap / Priority Queue",
            difficulty: "easy",
            title: "Last Stone Weight",
            description: "You are given an array of integers `stones` where `stones[i]` is the weight of the `i-th` stone.\n\nEach turn, we choose the heaviest two stones and smash them together. If `x == y`, both are destroyed. If `x != y`, the stone of weight `x` is destroyed, and the stone of weight `y` has new weight `y - x`.\n\nReturn the weight of the last remaining stone, or 0 if no stones are left.",
            examples: [
                {
                    input: "stones = [2, 7, 4, 1, 8, 1]",
                    output: "1",
                    explanation: "Smashing 7 & 8 leaves 1, and so on until 1 stone remains with weight 1."
                }
            ],
            constraints: [
                "1 <= stones.length <= 30",
                "1 <= stones[i] <= 1000"
            ],
            starterCode: {
                java: "import java.util.PriorityQueue;\n\npublic class Main {\n    public static void main(String[] args) {\n        int[] stones = {2, 7, 4, 1, 8, 1};\n        System.out.println(lastStoneWeight(stones));\n    }\n\n    public static int lastStoneWeight(int[] stones) {\n        // Write your solution here\n        return 1;\n    }\n}"
            },
            sampleTestCases: [
                { input: "stones = [2, 7, 4, 1, 8, 1]", expectedOutput: "1" },
                { input: "stones = [1]", expectedOutput: "1" }
            ],
            hiddenTestCases: [
                { input: "stones = [2, 2]", expectedOutput: "0" }
            ]
        },
        {
            id: "dsa_hp_02",
            topicId: "dsa_heap_priority_queue",
            topicName: "Heap / Priority Queue",
            difficulty: "medium",
            title: "K-th Largest Element in an Array",
            description: "Given an integer array `nums` and an integer `k`, return the `k-th` largest element in the array.\n\nNote that it is the `k-th` largest element in the sorted order, not the `k-th` distinct element.",
            examples: [
                {
                    input: "nums = [3, 2, 1, 5, 6, 4], k = 2",
                    output: "5",
                    explanation: "Sorted order is [1, 2, 3, 4, 5, 6], 2nd largest is 5."
                },
                {
                    input: "nums = [3, 2, 3, 1, 2, 4, 5, 5, 6], k = 4",
                    output: "4",
                    explanation: "4th largest element is 4."
                }
            ],
            constraints: [
                "1 <= k <= nums.length <= 10^5",
                "-10^4 <= nums[i] <= 10^4"
            ],
            starterCode: {
                java: "public class Main {\n    public static void main(String[] args) {\n        System.out.println(5);\n    }\n}"
            },
            sampleTestCases: [
                { input: "nums = [3, 2, 1, 5, 6, 4], k = 2", expectedOutput: "5" },
                { input: "nums = [3, 2, 3, 1, 2, 4, 5, 5, 6], k = 4", expectedOutput: "4" }
            ],
            hiddenTestCases: [
                { input: "nums = [1], k = 1", expectedOutput: "1" },
                { input: "nums = [7, 6, 5, 4, 3, 2, 1], k = 5", expectedOutput: "3" }
            ]
        }
    ];

    /**
     * Get all DSA questions filtered by topicId and/or difficulty
     */
    function getQuestionsByTopicAndDifficulty(topicId, difficulty) {
        return DSA_QUESTIONS.filter(q => {
            const matchTopic = !topicId || q.topicId === topicId;
            const matchDiff = !difficulty || q.difficulty === difficulty;
            return matchTopic && matchDiff;
        });
    }

    /**
     * Get all questions for a topic ID
     */
    function getQuestionsByTopic(topicId) {
        return DSA_QUESTIONS.filter(q => q.topicId === topicId);
    }

    /**
     * Get all available DSA questions
     */
    function getAllQuestions() {
        return [...DSA_QUESTIONS];
    }

    // Expose MockDsaQuestions to global window object
    window.MockDsaQuestions = {
        getAllQuestions: getAllQuestions,
        getQuestionsByTopic: getQuestionsByTopic,
        getQuestionsByTopicAndDifficulty: getQuestionsByTopicAndDifficulty,
        TOTAL_DSA_QUESTIONS: DSA_QUESTIONS.length
    };
})();
