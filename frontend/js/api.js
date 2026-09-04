/**
 * Centralized API Client Module
 * 
 * Manages all HTTP communication between the RemotePrep frontend and the Spring Boot backend.
 * Provides uniform error handling, request/response lifecycle, and offline lab resilience.
 */
(function () {
    /**
     * Resolves the API base URL dynamically:
     * 1. If explicit window.REMOTEPREP_API_BASE is specified, use it.
     * 2. When served directly by Spring Boot (port 8080), use '' for same-origin relative requests.
     * 3. Fallback for separate development static servers (e.g. port 3000, 5500): 'http://localhost:8080'.
     */
    function resolveApiBaseUrl() {
        if (typeof window !== 'undefined' && window.REMOTEPREP_API_BASE) {
            return window.REMOTEPREP_API_BASE;
        }
        if (typeof window !== 'undefined' && window.location) {
            const port = window.location.port;
            // If served directly from Spring Boot embedded server on port 8080
            if (port === '8080' || window.location.host.endsWith(':8080')) {
                return '';
            }
        }
        return 'http://localhost:8080';
    }

    const API_BASE_URL = resolveApiBaseUrl();

    /**
     * Executes an HTTP request against the Spring Boot backend with uniform error handling.
     * 
     * @param {string} endpoint - Path relative to API_BASE_URL (e.g. '/api/students/start')
     * @param {Object} options - Fetch options (method, body, headers, etc.)
     * @returns {Promise<any>} Parsed response data
     */
    async function request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const config = {
            ...options,
            headers
        };

        let response;
        try {
            response = await fetch(url, config);
        } catch (networkError) {
            // Network failure: server down, offline mode, DNS/port resolution failure
            console.error(`[ApiClient] Network failure communicating with ${url}:`, networkError);
            const err = new Error('Unable to connect to the assessment server. Please check if the server is running or contact the lab administrator.');
            err.status = 0;
            err.isNetworkError = true;
            throw err;
        }

        // Parse response body
        let data = null;
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            try {
                data = await response.json();
            } catch (jsonError) {
                console.error('[ApiClient] Failed to parse JSON response:', jsonError);
                const err = new Error('Invalid response received from the assessment server.');
                err.status = response.status;
                throw err;
            }
        } else {
            const text = await response.text();
            data = text ? { message: text } : {};
        }

        if (!response.ok) {
            let userMessage = 'An unexpected server error occurred.';
            if (data && data.error) {
                userMessage = data.error;
            } else if (data && data.message) {
                userMessage = data.message;
            } else {
                switch (response.status) {
                    case 400:
                        userMessage = 'Invalid request. Please check the entered information.';
                        break;
                    case 404:
                        userMessage = 'The requested assessment resource was not found.';
                        break;
                    case 409:
                        userMessage = 'Conflict: The assessment is not in a valid state for this operation.';
                        break;
                    case 500:
                        userMessage = 'An internal server error occurred. Please contact the lab administrator.';
                        break;
                    default:
                        userMessage = `Server request failed with status code ${response.status}.`;
                        break;
                }
            }

            const error = new Error(userMessage);
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return data;
    }

    const ApiClient = {
        API_BASE_URL: API_BASE_URL,

        /**
         * Page 1: Initializes student assessment attempt.
         * POST /api/students/start
         * @param {{ name: string, rollNumber: string }} payload
         * @returns {Promise<{ studentId: number, assessmentId: number, name: string, rollNumber: string, status: string }>}
         */
        startAssessment: function (payload) {
            return request('/api/students/start', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        },

        /**
         * Page 3 -> 4: Generates authoritative 20-question aptitude exam.
         * POST /api/aptitude/exam
         * @param {{ assessmentId: number, topicIds: number[] }} payload
         * @returns {Promise<{ assessmentId: number, questions: Array }>}
         */
        generateAptitudeExam: function (payload) {
            return request('/api/aptitude/exam', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        },

        /**
         * Page 4: Submits candidate answers for aptitude examination and triggers server-side scoring.
         * POST /api/aptitude/submit
         * @param {{ assessmentId: number, answers: Array<{ questionId: number, selectedOption: string|null }> }} payload
         * @returns {Promise<{ assessmentId: number, aptitudeScore: number, totalQuestions: number, correctAnswers: number, wrongAnswers: number, skippedAnswers: number, status: string }>}
         */
        submitAptitudeAnswers: function (payload) {
            return request('/api/aptitude/submit', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        },

        /**
         * Page 5: Generates or retrieves 2 assigned DSA questions (1 Easy + 1 Medium).
         * POST /api/dsa/exam
         * @param {{ assessmentId: number, topicIds: number[] }} payload
         * @returns {Promise<{ assessmentId: number, questions: Array }>}
         */
        generateDsaExam: function (payload) {
            return request('/api/dsa/exam', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        },

        /**
         * Page 5: Executes code against demo test cases (Run feature).
         * POST /api/dsa/run
         * @param {{ assessmentId: number, questionId: number, language: string, sourceCode: string }} payload
         * @returns {Promise<{ assessmentId: number, questionId: number, language: string, testCases: Array }>}
         */
        runDsaCode: function (payload) {
            return request('/api/dsa/run', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        },

        /**
         * Page 5: Submits candidate code for final evaluation and persistent judging.
         * POST /api/dsa/submit
         * @param {{ assessmentId: number, questionId: number, language: string, sourceCode: string }} payload
         * @returns {Promise<{ submissionId: number, assessmentId: number, questionId: number, language: string, status: string, totalTestCases: number, passedTestCases: number, failedTestCases: number, executionTimeMs: number }>}
         */
        submitDsaCode: function (payload) {
            return request('/api/dsa/submit', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        },

        /**
         * Page 5 -> 6: Permanently completes assessment attempt and finalizes scores.
         * POST /api/assessment/{assessmentId}/complete
         * @param {number} assessmentId
         * @returns {Promise<{ assessmentId: number, studentId: number, studentName: string, rollNumber: string, aptitudeScore: number, aptitudeTotal: number, dsaScore: number, dsaTotal: number, totalScore: number, totalMarks: number, status: string, completedAt: string }>}
         */
        completeAssessment: function (assessmentId) {
            return request(`/api/assessment/${assessmentId}/complete`, {
                method: 'POST'
            });
        },

        /**
         * Page 6: Retrieves authoritative finalized result of an assessment.
         * GET /api/assessment/{assessmentId}/result
         * @param {number} assessmentId
         * @returns {Promise<{ assessmentId: number, studentId: number, studentName: string, rollNumber: string, aptitudeScore: number, aptitudeTotal: number, dsaScore: number, dsaTotal: number, totalScore: number, totalMarks: number, status: string, startedAt: string, completedAt: string }>}
         */
        getAssessmentResult: function (assessmentId) {
            return request(`/api/assessment/${assessmentId}/result`, {
                method: 'GET'
            });
        }
    };

    // Expose ApiClient globally
    window.ApiClient = ApiClient;
})();
