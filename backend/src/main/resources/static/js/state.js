/**
 * Central State Management
 * 
 * Holds the single source of truth for the entire offline application session:
 * - Active student profile
 * - Selected topics (Aptitude & DSA)
 * - Exam progress, answers, timers, code submissions, and skipped questions
 * - Final calculated results
 */

(function () {
    const SESSION_KEY = 'remoteprep_session_state';

    // Initial / Clean Default State
    const initialState = {
        activePage: 'page-student',
        student: {
            name: '',
            rollNumber: '',
            studentId: null,
            assessmentId: null,
            status: null
        },
        selectedTopics: {
            aptitude: [], // Array of topic IDs
            dsa: []       // Array of topic IDs
        },
        instructionsAccepted: false,
        aptitudeExam: {
            questions: [],       // Selected 20 questions
            currentIndex: 0,     // 0 to 19
            answers: {},         // questionId -> selectedOptionIndex (0-3)
            skipped: [],         // Array of questionIds explicitly skipped
            startTime: null,     // ISO timestamp
            endTime: null,       // ISO timestamp
            timeRemaining: 1800, // 30 minutes in seconds
            isCompleted: false
        },
        dsaExam: {
            questions: [],          // Array of 2 questions: [easyQuestion, mediumQuestion]
            currentIndex: 0,        // 0 for Easy (Problem 1), 1 for Medium (Problem 2)
            selectedLanguage: {},   // questionId -> 'java' | 'cpp' | 'c' | 'python'
            code: {},               // questionId -> { java: '...', cpp: '...', c: '...', python: '...' }
            runOutput: {},          // questionId -> run output data
            submissions: {},        // questionId -> { verdict, testCasesPassed, totalTestCases, details, timestamp, language }
            skipped: [],            // Array of skipped questionIds
            timeRemaining: {
                easy: 1500,         // 25 minutes in seconds
                medium: 1800        // 30 minutes in seconds
            },
            startTime: null,
            endTime: null,
            isCompleted: false
        },
        results: {
            aptitude: null,
            dsa: null,
            summary: null,
            finalResult: null
        }
    };

    // Deep clone helper for clean resets
    function deepClone(obj) {
        return JSON.parse(JSON.stringify(obj));
    }

    // Active mutable state
    let state = deepClone(initialState);

    function saveSession() {
        try {
            if (typeof window !== 'undefined' && window.sessionStorage) {
                window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(state));
            }
        } catch (e) {
            console.warn('[AppState] Failed to save state to sessionStorage:', e);
        }
    }

    function clearSession() {
        try {
            if (typeof window !== 'undefined' && window.sessionStorage) {
                window.sessionStorage.removeItem(SESSION_KEY);
            }
        } catch (e) {
            console.warn('[AppState] Failed to clear sessionStorage:', e);
        }
    }

    // State Accessor and Mutator API
    const AppState = {
        /**
         * Get a read-only snapshot of the current state
         */
        getState: function () {
            return state;
        },

        /**
         * Set student identification details
         */
        setStudent: function (name, rollNumber) {
            state.student.name = (name || '').trim();
            state.student.rollNumber = (rollNumber || '').trim().toUpperCase();
            saveSession();
        },

        /**
         * Set authoritative student and assessment session details from backend
         */
        setStudentDetails: function (details = {}) {
            if (details.name !== undefined) state.student.name = (details.name || '').trim();
            if (details.rollNumber !== undefined) state.student.rollNumber = (details.rollNumber || '').trim().toUpperCase();
            if (details.studentId !== undefined) state.student.studentId = details.studentId;
            if (details.assessmentId !== undefined) state.student.assessmentId = details.assessmentId;
            if (details.status !== undefined) state.student.status = details.status;
            saveSession();
        },

        /**
         * Get active assessment ID
         */
        getAssessmentId: function () {
            return state.student.assessmentId;
        },

        /**
         * Get active student ID
         */
        getStudentId: function () {
            return state.student.studentId;
        },

        /**
         * Get active student details
         */
        getStudent: function () {
            return state.student;
        },

        /**
         * Set authoritative final result retrieved from backend
         */
        setFinalResult: function (result) {
            state.results.finalResult = result ? { ...result } : null;
            saveSession();
        },

        /**
         * Get authoritative final result
         */
        getFinalResult: function () {
            return state.results.finalResult;
        },

        /**
         * Update selected topics
         */
        setSelectedTopics: function (aptitudeTopics, dsaTopics) {
            state.selectedTopics.aptitude = [...aptitudeTopics];
            state.selectedTopics.dsa = [...dsaTopics];
            saveSession();
        },

        /**
         * Get selected topics
         */
        getSelectedTopics: function () {
            return state.selectedTopics;
        },

        /**
         * Mark instructions as accepted
         */
        setInstructionsAccepted: function (accepted) {
            state.instructionsAccepted = Boolean(accepted);
            saveSession();
        },

        // ==========================================
        // APTITUDE EXAM STATE METHODS
        // ==========================================

        /**
         * Initialize Aptitude exam with generated questions
         */
        initAptitudeExam: function (questions) {
            state.aptitudeExam.questions = [...questions];
            state.aptitudeExam.currentIndex = 0;
            state.aptitudeExam.answers = {};
            state.aptitudeExam.skipped = [];
            state.aptitudeExam.startTime = new Date().toISOString();
            state.aptitudeExam.endTime = null;
            state.aptitudeExam.timeRemaining = 1800; // 30 minutes
            state.aptitudeExam.isCompleted = false;
            saveSession();
        },

        /**
         * Record or update answer for a question
         */
        setAptitudeAnswer: function (questionId, optionIndex) {
            state.aptitudeExam.answers[questionId] = optionIndex;
            state.aptitudeExam.skipped = state.aptitudeExam.skipped.filter(id => id !== questionId);
            saveSession();
        },

        /**
         * Mark question as skipped (clears answer if present)
         */
        skipAptitudeQuestion: function (questionId) {
            if (state.aptitudeExam.answers.hasOwnProperty(questionId)) {
                delete state.aptitudeExam.answers[questionId];
            }
            if (!state.aptitudeExam.skipped.includes(questionId)) {
                state.aptitudeExam.skipped.push(questionId);
            }
            saveSession();
        },

        /**
         * Update active question index
         */
        setAptitudeCurrentIndex: function (index) {
            if (index >= 0 && index < state.aptitudeExam.questions.length) {
                state.aptitudeExam.currentIndex = index;
                saveSession();
            }
        },

        /**
         * Update remaining time in seconds
         */
        setAptitudeTimeRemaining: function (seconds) {
            state.aptitudeExam.timeRemaining = Math.max(0, seconds);
        },

        /**
         * Finalize Aptitude examination and save score results
         */
        completeAptitudeExam: function (results) {
            state.aptitudeExam.isCompleted = true;
            state.aptitudeExam.endTime = new Date().toISOString();
            state.results.aptitude = { ...results };
            saveSession();
        },

        /**
         * Get Aptitude exam state slice
         */
        getAptitudeExam: function () {
            return state.aptitudeExam;
        },

        /**
         * Get Aptitude results
         */
        getAptitudeResults: function () {
            return state.results.aptitude;
        },

        // ==========================================
        // DSA EXAM STATE METHODS
        // ==========================================

        /**
         * Initialize DSA exam with 2 selected problems [Easy, Medium]
         */
        initDsaExam: function (questions) {
            state.dsaExam.questions = [...questions];
            state.dsaExam.currentIndex = 0;
            state.dsaExam.selectedLanguage = {};
            state.dsaExam.code = {};
            state.dsaExam.runOutput = {};
            state.dsaExam.submissions = {};
            state.dsaExam.skipped = [];
            state.dsaExam.timeRemaining = {
                easy: 1500,   // 25 minutes
                medium: 1800  // 30 minutes
            };
            state.dsaExam.startTime = new Date().toISOString();
            state.dsaExam.endTime = null;
            state.dsaExam.isCompleted = false;

            const supportedLangs = ['java', 'cpp', 'c', 'python'];

            // Pre-populate initial starter code and default language ('java') for all questions
            questions.forEach(q => {
                state.dsaExam.selectedLanguage[q.id] = 'java';
                state.dsaExam.code[q.id] = {};
                supportedLangs.forEach(lang => {
                    if (q.starterCode && typeof q.starterCode === 'object' && q.starterCode[lang]) {
                        state.dsaExam.code[q.id][lang] = q.starterCode[lang];
                    } else if (typeof q.starterCode === 'string') {
                        state.dsaExam.code[q.id][lang] = q.starterCode;
                    } else {
                        state.dsaExam.code[q.id][lang] = '';
                    }
                });
            });
            saveSession();
        },

        /**
         * Get selected programming language for a problem ID
         */
        getDsaLanguage: function (questionId) {
            return (state.dsaExam.selectedLanguage && state.dsaExam.selectedLanguage[questionId]) || 'java';
        },

        /**
         * Set selected programming language for a problem ID
         */
        setDsaLanguage: function (questionId, language) {
            if (!state.dsaExam.selectedLanguage) {
                state.dsaExam.selectedLanguage = {};
            }
            state.dsaExam.selectedLanguage[questionId] = language || 'java';
            saveSession();
        },

        /**
         * Save candidate code for a problem ID and language
         * Supports both setDsaCode(questionId, code) and setDsaCode(questionId, language, code)
         */
        setDsaCode: function (questionId, langOrCode, maybeCode) {
            if (!state.dsaExam.code[questionId]) {
                state.dsaExam.code[questionId] = {};
            }
            if (typeof maybeCode === 'string') {
                const lang = langOrCode || 'java';
                state.dsaExam.code[questionId][lang] = maybeCode;
            } else {
                const currentLang = this.getDsaLanguage(questionId);
                if (typeof state.dsaExam.code[questionId] === 'string') {
                    state.dsaExam.code[questionId] = { [currentLang]: langOrCode };
                } else {
                    state.dsaExam.code[questionId][currentLang] = langOrCode;
                }
            }
            saveSession();
        },

        /**
         * Retrieve candidate code for a problem ID and language
         * Supports both getDsaCode(questionId) and getDsaCode(questionId, language)
         */
        getDsaCode: function (questionId, language) {
            if (!state.dsaExam.code || !state.dsaExam.code[questionId]) {
                return '';
            }
            const lang = language || this.getDsaLanguage(questionId);
            if (typeof state.dsaExam.code[questionId] === 'string') {
                return state.dsaExam.code[questionId];
            }
            return state.dsaExam.code[questionId][lang] || '';
        },

        /**
         * Set active problem index (0 for Easy, 1 for Medium)
         */
        setDsaCurrentIndex: function (index) {
            if (index === 0 || index === 1) {
                state.dsaExam.currentIndex = index;
                saveSession();
            }
        },

        /**
         * Skip current DSA problem
         */
        skipDsaQuestion: function (questionId) {
            if (!state.dsaExam.skipped.includes(questionId)) {
                state.dsaExam.skipped.push(questionId);
                saveSession();
            }
        },

        /**
         * Record submission result for a DSA problem
         */
        recordDsaSubmission: function (questionId, submissionResult) {
            state.dsaExam.submissions[questionId] = {
                ...submissionResult,
                timestamp: new Date().toISOString()
            };
            state.dsaExam.skipped = state.dsaExam.skipped.filter(id => id !== questionId);
            saveSession();
        },

        /**
         * Store execution output for a DSA problem run
         */
        setDsaRunOutput: function (questionId, outputData) {
            if (!state.dsaExam.runOutput) {
                state.dsaExam.runOutput = {};
            }
            state.dsaExam.runOutput[questionId] = outputData;
            saveSession();
        },

        /**
         * Retrieve cached execution output for a DSA problem
         */
        getDsaRunOutput: function (questionId) {
            return (state.dsaExam.runOutput && state.dsaExam.runOutput[questionId]) || null;
        },

        /**
         * Update remaining time for Easy or Medium problem
         */
        setDsaTimeRemaining: function (difficulty, seconds) {
            if (state.dsaExam.timeRemaining.hasOwnProperty(difficulty)) {
                state.dsaExam.timeRemaining[difficulty] = Math.max(0, seconds);
            }
        },

        /**
         * Finalize DSA examination and save score results
         */
        completeDsaExam: function (results) {
            state.dsaExam.isCompleted = true;
            state.dsaExam.endTime = new Date().toISOString();
            state.results.dsa = { ...results };
            saveSession();
        },

        /**
         * Get DSA exam state slice
         */
        getDsaExam: function () {
            return state.dsaExam;
        },

        /**
         * Get DSA results
         */
        getDsaResults: function () {
            return state.results.dsa;
        },

        /**
         * Track active page identifier
         */
        setActivePage: function (pageId) {
            state.activePage = pageId || 'page-student';
            saveSession();
        },

        /**
         * Retrieve active page identifier
         */
        getActivePage: function () {
            return state.activePage || 'page-student';
        },

        /**
         * Explicitly trigger session save
         */
        saveToSession: function () {
            saveSession();
        },

        /**
         * Restore state from sessionStorage if available
         * @returns {boolean} true if restored, false otherwise
         */
        loadFromSession: function () {
            try {
                if (typeof window !== 'undefined' && window.sessionStorage) {
                    const raw = window.sessionStorage.getItem(SESSION_KEY);
                    if (raw) {
                        const parsed = JSON.parse(raw);
                        if (parsed && typeof parsed === 'object' && parsed.student && parsed.student.assessmentId) {
                            state = Object.assign(deepClone(initialState), parsed);
                            return true;
                        }
                    }
                }
            } catch (e) {
                console.warn('[AppState] Failed to load state from sessionStorage:', e);
            }
            return false;
        },

        /**
         * Clear sessionStorage
         */
        clearSession: function () {
            clearSession();
        },

        /**
         * Reset state completely to default initial state (for next student)
         */
        resetState: function () {
            state = deepClone(initialState);
            clearSession();
        }
    };

    // Expose to global window object
    window.AppState = AppState;
})();
