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
        // Start on Page 1 (Student Details)
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
    }

    /**
     * Handles the Page 1 Student Details submission
     * @param {Event} e 
     */
    function handleStudentFormSubmit(e) {
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

        // 3. Update Application State
        window.AppState.setStudent(nameValue, rollValue);

        // 4. Navigate to Page 2 (Topic Selection)
        window.Navigation.navigateTo('page-topics');
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
