/**
 * Rules and Instructions Controller (Page 3)
 * 
 * Manages instruction acceptance state, enables/disables the start button,
 * and controls navigation between Topic Selection (Page 2) and Aptitude Exam (Page 4).
 */

(function () {
    // DOM Elements Cache
    let chkAccept;
    let acceptancePanel;
    let btnStart;
    let btnPrevious;

    /**
     * Initializes Instructions module
     */
    function init() {
        cacheDOMElements();
        bindEvents();
        syncUIWithState();
    }

    /**
     * Cache DOM references
     */
    function cacheDOMElements() {
        chkAccept = document.getElementById('chk-accept-instructions');
        acceptancePanel = document.getElementById('acceptance-panel');
        btnStart = document.getElementById('btn-instructions-start');
        btnPrevious = document.getElementById('btn-instructions-previous');
    }

    /**
     * Bind user interaction events
     */
    function bindEvents() {
        if (chkAccept) {
            chkAccept.addEventListener('change', handleAcceptanceChange);
        }

        if (btnStart) {
            btnStart.addEventListener('click', handleStartExamination);
        }

        if (btnPrevious) {
            btnPrevious.addEventListener('click', handlePrevious);
        }
    }

    /**
     * Handles changes to the instruction acceptance checkbox
     */
    function handleAcceptanceChange(e) {
        const isAccepted = Boolean(e.target.checked);

        // 1. Update centralized AppState
        if (window.AppState && typeof window.AppState.setInstructionsAccepted === 'function') {
            window.AppState.setInstructionsAccepted(isAccepted);
        }

        // 2. Update visual state
        updateAcceptanceVisuals(isAccepted);
    }

    /**
     * Updates button enabled/disabled state and panel styling
     */
    function updateAcceptanceVisuals(isAccepted) {
        if (btnStart) {
            btnStart.disabled = !isAccepted;
        }

        if (acceptancePanel) {
            if (isAccepted) {
                acceptancePanel.classList.add('accepted');
            } else {
                acceptancePanel.classList.remove('accepted');
            }
        }
    }

    /**
     * Synchronizes UI with AppState (e.g. on navigation return)
     */
    function syncUIWithState() {
        const state = window.AppState ? window.AppState.getState() : null;
        const isAccepted = state ? Boolean(state.instructionsAccepted) : false;

        if (chkAccept) {
            chkAccept.checked = isAccepted;
        }

        updateAcceptanceVisuals(isAccepted);
    }

    /**
     * Handles "Start Examination →" button click
     */
    function handleStartExamination() {
        const state = window.AppState ? window.AppState.getState() : null;
        const isAccepted = (chkAccept && chkAccept.checked) || (state && state.instructionsAccepted);

        // Guard clause: ensure acceptance is mandatory
        if (!isAccepted) {
            return;
        }

        // Confirm state persistence
        if (window.AppState && typeof window.AppState.setInstructionsAccepted === 'function') {
            window.AppState.setInstructionsAccepted(true);
        }

        // Navigate to Page 4 (Aptitude Examination)
        if (window.Navigation && typeof window.Navigation.navigateTo === 'function') {
            window.Navigation.navigateTo('page-aptitude');
        }
    }

    /**
     * Handles "← Previous" button click (returns to Topic Selection while preserving selections)
     */
    function handlePrevious() {
        if (window.Navigation && typeof window.Navigation.navigateTo === 'function') {
            window.Navigation.navigateTo('page-topics');
        }
    }

    // Expose controller
    window.Instructions = {
        init: init,
        syncUIWithState: syncUIWithState
    };
})();
