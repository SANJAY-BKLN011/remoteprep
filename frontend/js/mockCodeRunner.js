/**
 * Mock Code Execution Layer
 * 
 * Simulates deterministic compilation and test-case execution for Java solutions.
 * In future phases, this layer will be replaced with local Java process execution
 * or Spring Boot REST API code runners.
 * 
 * NO eval() or unsafe dynamic JavaScript execution is used.
 */

(function () {
    /**
     * Checks basic Java code structure
     */
    function analyzeCode(code) {
        const cleanCode = (code || '').trim();

        if (!cleanCode) {
            return {
                valid: false,
                errorType: 'COMPILATION_ERROR',
                message: 'Error: Source file is empty. Please write your Java solution.'
            };
        }

        // Basic brace balance check
        let openBraces = (cleanCode.match(/\{/g) || []).length;
        let closeBraces = (cleanCode.match(/\}/g) || []).length;

        if (openBraces !== closeBraces) {
            return {
                valid: false,
                errorType: 'COMPILATION_ERROR',
                message: `Compilation Error: Syntax error on token(s), mismatched braces { found: ${openBraces}, } found: ${closeBraces}`
            };
        }

        // Check for basic class definition
        if (!cleanCode.includes('class') || !cleanCode.includes('{')) {
            return {
                valid: false,
                errorType: 'COMPILATION_ERROR',
                message: 'Compilation Error: Class, interface, or enum expected in Java source file.'
            };
        }

        return {
            valid: true,
            errorType: null,
            message: null
        };
    }

    const MockCodeRunner = {
        /**
         * Simulates RUN action against visible sample test cases
         * @param {string} code - Candidate Java source code
         * @param {Object} question - Question object
         * @returns {Object} Execution result
         */
        run: function (code, question) {
            const analysis = analyzeCode(code);

            if (!analysis.valid) {
                return {
                    status: analysis.errorType,
                    totalTests: question.sampleTestCases ? question.sampleTestCases.length : 0,
                    passedTests: 0,
                    testResults: [],
                    errorMessage: analysis.message
                };
            }

            const sampleCases = question.sampleTestCases || [];
            const results = [];
            let passedCount = 0;

            sampleCases.forEach((tc, idx) => {
                // In mock simulation, valid code structure produces expected output
                const passed = true;
                if (passed) passedCount++;

                results.push({
                    caseNumber: idx + 1,
                    input: tc.input,
                    expectedOutput: tc.expectedOutput,
                    actualOutput: tc.expectedOutput,
                    passed: passed
                });
            });

            return {
                status: 'SUCCESS',
                totalTests: sampleCases.length,
                passedTests: passedCount,
                testResults: results,
                errorMessage: null
            };
        },

        /**
         * Simulates SUBMIT action against all test cases (sample + hidden)
         * @param {string} code - Candidate Java source code
         * @param {Object} question - Question object
         * @returns {Object} Submission result
         */
        submit: function (code, question) {
            const analysis = analyzeCode(code);

            const allCases = [
                ...(question.sampleTestCases || []),
                ...(question.hiddenTestCases || [])
            ];
            const totalCases = allCases.length;

            if (!analysis.valid) {
                return {
                    verdict: 'Compilation Error',
                    totalTestCases: totalCases,
                    testCasesPassed: 0,
                    executionTime: '0 ms',
                    memoryUsed: '0 MB',
                    errorMessage: analysis.message
                };
            }

            // If code is valid, simulate full acceptance across hidden test cases
            return {
                verdict: 'Accepted',
                totalTestCases: totalCases,
                testCasesPassed: totalCases,
                executionTime: '38 ms',
                memoryUsed: '39.2 MB',
                errorMessage: null
            };
        }
    };

    // Expose MockCodeRunner to global window object
    window.MockCodeRunner = MockCodeRunner;
})();
