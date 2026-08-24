/**
 * Navigation Module
 * 
 * Controls Single-Page Application (SPA) view switching,
 * ensuring clean transitions and header state updates.
 */

(function () {
    const pages = [
        'page-student',
        'page-topics',
        'page-instructions',
        'page-aptitude',
        'page-dsa',
        'page-result'
    ];

    const Navigation = {
        /**
         * Switches the active view to the specified target page ID
         * @param {string} targetPageId - DOM element ID of target page
         */
        navigateTo: function (targetPageId) {
            // Hide all registered pages
            pages.forEach(id => {
                const el = document.getElementById(id);
                if (el) {
                    el.classList.add('hidden');
                }
            });

            // Show target page
            const targetEl = document.getElementById(targetPageId);
            if (targetEl) {
                targetEl.classList.remove('hidden');
                window.scrollTo(0, 0);
            } else {
                console.error(`Page element with ID "${targetPageId}" not found.`);
            }

            // Update header student info display
            this.updateHeaderInfo();
        },

        /**
         * Updates the top navigation bar with the active student details
         */
        updateHeaderInfo: function () {
            const studentInfoEl = document.getElementById('header-student-info');
            if (!studentInfoEl) return;

            const student = window.AppState ? window.AppState.getStudent() : null;
            if (student && student.name && student.rollNumber) {
                studentInfoEl.innerHTML = `Student: <span>${escapeHtml(student.name)} (${escapeHtml(student.rollNumber)})</span>`;
                studentInfoEl.classList.remove('hidden');
            } else {
                studentInfoEl.innerHTML = '';
                studentInfoEl.classList.add('hidden');
            }
        }
    };

    /**
     * Basic HTML escaping to prevent XSS during text injection
     */
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Expose to global window object
    window.Navigation = Navigation;
})();
