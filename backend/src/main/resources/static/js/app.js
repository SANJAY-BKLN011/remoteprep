/**
 * Main Application Orchestrator
 * 
 * Initializes DOM listeners, binds Page 1 interaction logic,
 * and coordinates between state, validation, and navigation.
 */

(function () {
    // DOM Elements Cache
    let studentForm;
    let nameInput;
    let rollInput;
    let nameError;
    let rollError;
    let formErrorBanner;
    let btnContinue;

    /**
     * Initializes all event listeners and starting view
     */
    function init() {
        cacheDOMElements();
        bindEvents();
        // Initialize Topic Selection component
        if (window.TopicSelection && typeof window.TopicSelection.init === 'function') {
            window.TopicSelection.init();
        }
        // Initialize Rules & Instructions component
        if (window.Instructions && typeof window.Instructions.init === 'function') {
            window.Instructions.init();
        }
        // Initialize Aptitude Assessment component
        if (window.Aptitude && typeof window.Aptitude.init === 'function') {
            window.Aptitude.init();
        }
        // Initialize DSA Assessment component
        if (window.Dsa && typeof window.Dsa.init === 'function') {
            window.Dsa.init();
        }

        // Restore active session on reload or start fresh
        restoreSessionOrStart();
    }

    /**
     * Resumes an existing active assessment session across page reloads
     */
    function restoreSessionOrStart() {
        const hasSession = window.AppState && typeof window.AppState.loadFromSession === 'function' && window.AppState.loadFromSession();
        if (hasSession) {
            const student = window.AppState.getStudent();
            if (student && student.name && nameInput) {
                nameInput.value = student.name;
            }
            if (student && student.rollNumber && rollInput) {
                rollInput.value = student.rollNumber;
            }

            const activePage = window.AppState.getActivePage();
            const finalResult = window.AppState.getFinalResult();
            const dsaExam = window.AppState.getDsaExam();
            const aptExam = window.AppState.getAptitudeExam();

            if (finalResult || activePage === 'page-result') {
                if (window.Dsa && typeof window.Dsa.renderFinalSummaryOnPage6 === 'function') {
                    window.Dsa.renderFinalSummaryOnPage6(finalResult);
                }
                window.Navigation.navigateTo('page-result');
                return;
            } else if (activePage === 'page-dsa' && dsaExam && dsaExam.questions && dsaExam.questions.length > 0) {
                window.Navigation.navigateTo('page-dsa');
                if (window.Dsa && typeof window.Dsa.renderProblem === 'function') {
                    window.Dsa.renderProblem(dsaExam.currentIndex || 0);
                }
                return;
            } else if (activePage === 'page-aptitude' && aptExam && aptExam.questions && aptExam.questions.length > 0) {
                window.Navigation.navigateTo('page-aptitude');
                if (window.Aptitude && typeof window.Aptitude.renderCurrentQuestion === 'function') {
                    window.Aptitude.renderCurrentQuestion();
                }
                return;
            } else if (activePage === 'page-instructions') {
                window.Navigation.navigateTo('page-instructions');
                return;
            } else if (activePage === 'page-topics') {
                window.Navigation.navigateTo('page-topics');
                return;
            }
        }

        // Default start on Page 1 (Student Details)
        window.Navigation.navigateTo('page-student');
    }

    /**
     * Cache frequently accessed DOM elements
     */
    function cacheDOMElements() {
        studentForm = document.getElementById('form-student-details');
        nameInput = document.getElementById('input-student-name');
        rollInput = document.getElementById('input-student-roll');
        nameError = document.getElementById('error-student-name');
        rollError = document.getElementById('error-student-roll');
        formErrorBanner = document.getElementById('student-error-banner');
        btnContinue = document.getElementById('btn-student-continue');
    }

    /**
     * Bind user interaction events
     */
    function bindEvents() {
        if (studentForm) {
            studentForm.addEventListener('submit', handleStudentFormSubmit);
        }

        // Clear error highlights as user types
        if (nameInput) {
            nameInput.addEventListener('input', () => clearFieldError(nameInput, nameError));
        }

        if (rollInput) {
            rollInput.addEventListener('input', () => clearFieldError(rollInput, rollError));
        }

        // Warning when leaving or refreshing during active exam
        window.addEventListener('beforeunload', function (e) {
            const activePage = window.AppState ? window.AppState.getActivePage() : '';
            if (activePage === 'page-aptitude' || activePage === 'page-dsa') {
                e.preventDefault();
                e.returnValue = 'An assessment is currently in progress. Refreshing or leaving the page may disrupt your exam session.';
                return e.returnValue;
            }
        });
    }

    /**
     * Handles the Page 1 Student Details submission
     * @param {Event} e 
     */
    async function handleStudentFormSubmit(e) {
        e.preventDefault();

        const nameValue = nameInput.value.trim();
        const rollValue = rollInput.value.trim();

        // 1. Validate Input
        const validationResult = window.Validation.validateStudentDetails(nameValue, rollValue);

        if (!validationResult.isValid) {
            displayValidationErrors(validationResult.errors);
            return;
        }

        // 2. Clear any lingering errors
        resetErrors();

        // Prevent accidental duplicate assessment creation if active session exists for same student
        const existingStudent = window.AppState ? window.AppState.getStudent() : null;
        if (existingStudent && existingStudent.assessmentId &&
            existingStudent.rollNumber.toUpperCase() === rollValue.toUpperCase() &&
            existingStudent.name.toUpperCase() === nameValue.toUpperCase() &&
            existingStudent.status === 'IN_PROGRESS') {
            window.Navigation.navigateTo('page-topics');
            return;
        }

        // Prevent double submission and provide visual feedback
        if (btnContinue) {
            btnContinue.disabled = true;
            btnContinue.textContent = 'Connecting to Server...';
        }

        try {
            // 3. Call backend POST /api/students/start
            const response = await window.ApiClient.startAssessment({
                name: nameValue,
                rollNumber: rollValue
            });

            // 4. Update Application State with authoritative backend identifiers
            window.AppState.setStudentDetails({
                studentId: response.studentId,
                assessmentId: response.assessmentId,
                name: response.name || nameValue,
                rollNumber: response.rollNumber || rollValue,
                status: response.status || 'IN_PROGRESS'
            });

            // 5. Navigate to Page 2 (Topic Selection)
            window.Navigation.navigateTo('page-topics');
        } catch (error) {
            console.error('[Page 1] Error starting assessment:', error);
            if (formErrorBanner) {
                formErrorBanner.textContent = error.message || 'Unable to start assessment session. Please try again or contact the lab administrator.';
                formErrorBanner.classList.remove('hidden');
                formErrorBanner.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        } finally {
            if (btnContinue) {
                btnContinue.disabled = false;
                btnContinue.innerHTML = 'Continue to Topic Selection &rarr;';
            }
        }
    }

    /**
     * Displays field-specific and banner errors
     * @param {Object} errors 
     */
    function displayValidationErrors(errors) {
        let firstInvalidInput = null;

        if (errors.name) {
            nameInput.classList.add('input-error');
            nameError.textContent = errors.name;
            nameError.classList.remove('hidden');
            if (!firstInvalidInput) firstInvalidInput = nameInput;
        } else {
            clearFieldError(nameInput, nameError);
        }

        if (errors.rollNumber) {
            rollInput.classList.add('input-error');
            rollError.textContent = errors.rollNumber;
            rollError.classList.remove('hidden');
            if (!firstInvalidInput) firstInvalidInput = rollInput;
        } else {
            clearFieldError(rollInput, rollError);
        }

        if (formErrorBanner) {
            formErrorBanner.textContent = 'Please fill in all required fields correctly to continue.';
            formErrorBanner.classList.remove('hidden');
        }

        if (firstInvalidInput) {
            firstInvalidInput.focus();
        }
    }

    /**
     * Clears error indicator for a specific field
     */
    function clearFieldError(inputEl, errorEl) {
        if (inputEl) inputEl.classList.remove('input-error');
        if (errorEl) {
            errorEl.textContent = '';
            errorEl.classList.add('hidden');
        }
        if (formErrorBanner && !nameInput.classList.contains('input-error') && !rollInput.classList.contains('input-error')) {
            formErrorBanner.classList.add('hidden');
        }
    }

    /**
     * Resets all validation visual errors
     */
    function resetErrors() {
        if (nameInput) nameInput.classList.remove('input-error');
        if (rollInput) rollInput.classList.remove('input-error');
        if (nameError) {
            nameError.textContent = '';
            nameError.classList.add('hidden');
        }
        if (rollError) {
            rollError.textContent = '';
            rollError.classList.add('hidden');
        }
        if (formErrorBanner) {
            formErrorBanner.textContent = '';
            formErrorBanner.classList.add('hidden');
        }
    }

    // Initialize application when DOM is fully loaded
    document.addEventListener('DOMContentLoaded', init);

    // Expose orchestrator
    window.App = {
        init: init
    };
})();
