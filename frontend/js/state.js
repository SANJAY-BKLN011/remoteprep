/**
 * Central State Management
 * 
 * Holds the single source of truth for the entire offline application session:
 * - Active student profile
 * - Selected topics (Aptitude & DSA)
 * - Exam progress, answers, and timers
 * - Final calculated results
 */

(function () {
    // Initial / Clean Default State
    const initialState = {
        student: {
            name: '',
            rollNumber: ''
        },
        selectedTopics: {
            aptitude: [], // Array of topic IDs
            dsa: []       // Array of topic IDs
        },
        instructionsAccepted: false,
        aptitudeExam: {
            questions: [],       // Selected 20 questions
            currentIndex: 0,     // 0 to 19
            answers: {},         // questionId -> selectedOptionIndex (or null)
            timeRemaining: 1800, // 30 minutes in seconds
            isCompleted: false
        },
        dsaExam: {
            easyProblem: null,
            mediumProblem: null,
            currentProblemIndex: 0, // 0 for Easy, 1 for Medium
            code: {
                easy: '',
                medium: ''
            },
            submissions: {
                easy: null,
                medium: null
            },
            timeRemaining: {
                easy: 1500,   // 25 minutes in seconds
                medium: 1800  // 30 minutes in seconds
            },
            isCompleted: false
        },
        results: {
            aptitude: null,
            dsa: null,
            summary: null
        }
    };

    // Deep clone helper for clean resets
    function deepClone(obj) {
        return JSON.parse(JSON.stringify(obj));
    }

    // Active mutable state
    let state = deepClone(initialState);

    // State Accessor and Mutator API
    const AppState = {
        /**
         * Get a read-only snapshot of the current state or specific slice
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
        },

        /**
         * Get active student details
         */
        getStudent: function () {
            return state.student;
        },

        /**
         * Update selected topics
         */
        setSelectedTopics: function (aptitudeTopics, dsaTopics) {
            state.selectedTopics.aptitude = [...aptitudeTopics];
            state.selectedTopics.dsa = [...dsaTopics];
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
        },

        /**
         * Reset state completely to default initial state (for next student)
         */
        resetState: function () {
            state = deepClone(initialState);
        }
    };

    // Expose to global window object
    window.AppState = AppState;
})();
