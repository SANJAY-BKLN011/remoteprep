/**
 * Topic Selection Controller (Page 2)
 * 
 * Handles topic rendering, independent "All Topics" toggles,
 * individual topic selections, selection counters, and validation.
 */

(function () {
    // Local Selection Tracking (Sets of Topic IDs)
    const selectedAptitude = new Set();
    const selectedDsa = new Set();

    // DOM Elements Cache
    let aptitudeContainer;
    let dsaContainer;
    let toggleAllAptitude;
    let toggleAllDsa;
    let aptitudeBadge;
    let dsaBadge;
    let errorBanner;
    let summaryText;
    let btnContinue;
    let btnBack;

    /**
     * Initializes Topic Selection module
     */
    function init() {
        cacheDOMElements();
        renderAptitudeCategories();
        renderDsaTopics();
        bindEvents();
        updateUI();
    }

    /**
     * Cache DOM references
     */
    function cacheDOMElements() {
        aptitudeContainer = document.getElementById('aptitude-categories-container');
        dsaContainer = document.getElementById('dsa-topics-container');
        toggleAllAptitude = document.getElementById('toggle-all-aptitude');
        toggleAllDsa = document.getElementById('toggle-all-dsa');
        aptitudeBadge = document.getElementById('aptitude-count-badge');
        dsaBadge = document.getElementById('dsa-count-badge');
        errorBanner = document.getElementById('topics-error-banner');
        summaryText = document.getElementById('topics-selection-summary');
        btnContinue = document.getElementById('btn-topics-continue');
        btnBack = document.getElementById('btn-topics-back');
    }

    /**
     * Renders Aptitude categories and their respective topics
     */
    function renderAptitudeCategories() {
        if (!aptitudeContainer || !window.TopicData) return;

        let html = '';
        window.TopicData.APTITUDE_CATEGORIES.forEach(category => {
            html += `
                <div class="category-card" data-category-id="${category.categoryId}">
                    <div class="category-card-title">
                        <span>${escapeHtml(category.categoryName)}</span>
                        <span class="category-card-count" id="count-cat-${category.categoryId}">0 / ${category.topics.length}</span>
                    </div>
                    <div class="topic-list">
            `;

            category.topics.forEach(topic => {
                const isChecked = selectedAptitude.has(topic.id);
                html += `
                    <label class="topic-item-label ${isChecked ? 'selected' : ''}" for="chk-${topic.id}">
                        <input 
                            type="checkbox" 
                            id="chk-${topic.id}" 
                            class="topic-checkbox aptitude-topic-checkbox" 
                            data-topic-id="${topic.id}"
                            data-category-id="${category.categoryId}"
                            ${isChecked ? 'checked' : ''}
                        >
                        <span class="topic-item-text">${escapeHtml(topic.name)}</span>
                    </label>
                `;
            });

            html += `
                    </div>
                </div>
            `;
        });

        aptitudeContainer.innerHTML = html;
    }

    /**
     * Renders DSA topics
     */
    function renderDsaTopics() {
        if (!dsaContainer || !window.TopicData) return;

        let html = '<div class="topic-list">';
        window.TopicData.DSA_TOPICS.forEach(topic => {
            const isChecked = selectedDsa.has(topic.id);
            html += `
                <label class="topic-item-label ${isChecked ? 'selected' : ''}" for="chk-${topic.id}">
                    <input 
                        type="checkbox" 
                        id="chk-${topic.id}" 
                        class="topic-checkbox dsa-topic-checkbox" 
                        data-topic-id="${topic.id}"
                        ${isChecked ? 'checked' : ''}
                    >
                    <span class="topic-item-text">${escapeHtml(topic.name)}</span>
                </label>
            `;
        });
        html += '</div>';

        dsaContainer.innerHTML = html;
    }

    /**
     * Bind all user interactions and change events
     */
    function bindEvents() {
        // "ALL TOPICS" Toggle for Aptitude
        if (toggleAllAptitude) {
            toggleAllAptitude.addEventListener('change', handleToggleAllAptitude);
        }

        // "ALL TOPICS" Toggle for DSA
        if (toggleAllDsa) {
            toggleAllDsa.addEventListener('change', handleToggleAllDsa);
        }

        // Individual topic checkboxes (using event delegation for efficiency)
        if (aptitudeContainer) {
            aptitudeContainer.addEventListener('change', handleAptitudeCheckboxChange);
        }

        if (dsaContainer) {
            dsaContainer.addEventListener('change', handleDsaCheckboxChange);
        }

        // Action Buttons
        if (btnContinue) {
            btnContinue.addEventListener('click', handleContinue);
        }

        if (btnBack) {
            btnBack.addEventListener('click', handleBack);
        }
    }

    /**
     * Handles Aptitude All Topics toggle
     */
    function handleToggleAllAptitude(e) {
        const isChecked = e.target.checked;
        const allIds = window.TopicData.getAllAptitudeTopicIds();

        if (isChecked) {
            allIds.forEach(id => selectedAptitude.add(id));
        } else {
            selectedAptitude.clear();
        }

        // Update all Aptitude checkboxes in DOM
        const checkboxes = aptitudeContainer.querySelectorAll('.aptitude-topic-checkbox');
        checkboxes.forEach(cb => {
            cb.checked = isChecked;
            const parentLabel = cb.closest('.topic-item-label');
            if (parentLabel) {
                if (isChecked) {
                    parentLabel.classList.add('selected');
                } else {
                    parentLabel.classList.remove('selected');
                }
            }
        });

        clearErrorIfValid();
        updateUI();
    }

    /**
     * Handles DSA All Topics toggle
     */
    function handleToggleAllDsa(e) {
        const isChecked = e.target.checked;
        const allIds = window.TopicData.getAllDsaTopicIds();

        if (isChecked) {
            allIds.forEach(id => selectedDsa.add(id));
        } else {
            selectedDsa.clear();
        }

        // Update all DSA checkboxes in DOM
        const checkboxes = dsaContainer.querySelectorAll('.dsa-topic-checkbox');
        checkboxes.forEach(cb => {
            cb.checked = isChecked;
            const parentLabel = cb.closest('.topic-item-label');
            if (parentLabel) {
                if (isChecked) {
                    parentLabel.classList.add('selected');
                } else {
                    parentLabel.classList.remove('selected');
                }
            }
        });

        clearErrorIfValid();
        updateUI();
    }

    /**
     * Handles individual Aptitude topic checkbox changes
     */
    function handleAptitudeCheckboxChange(e) {
        if (!e.target.classList.contains('aptitude-topic-checkbox')) return;

        const checkbox = e.target;
        const topicId = checkbox.getAttribute('data-topic-id');
        const parentLabel = checkbox.closest('.topic-item-label');

        if (checkbox.checked) {
            selectedAptitude.add(topicId);
            if (parentLabel) parentLabel.classList.add('selected');
        } else {
            selectedAptitude.delete(topicId);
            if (parentLabel) parentLabel.classList.remove('selected');
        }

        // Synchronize All Topics toggle state
        if (toggleAllAptitude) {
            toggleAllAptitude.checked = (selectedAptitude.size === window.TopicData.TOTAL_APTITUDE_TOPICS);
        }

        clearErrorIfValid();
        updateUI();
    }

    /**
     * Handles individual DSA topic checkbox changes
     */
    function handleDsaCheckboxChange(e) {
        if (!e.target.classList.contains('dsa-topic-checkbox')) return;

        const checkbox = e.target;
        const topicId = checkbox.getAttribute('data-topic-id');
        const parentLabel = checkbox.closest('.topic-item-label');

        if (checkbox.checked) {
            selectedDsa.add(topicId);
            if (parentLabel) parentLabel.classList.add('selected');
        } else {
            selectedDsa.delete(topicId);
            if (parentLabel) parentLabel.classList.remove('selected');
        }

        // Synchronize All Topics toggle state
        if (toggleAllDsa) {
            toggleAllDsa.checked = (selectedDsa.size === window.TopicData.TOTAL_DSA_TOPICS);
        }

        clearErrorIfValid();
        updateUI();
    }

    /**
     * Updates header counters, category sub-counters, and summary text
     */
    function updateUI() {
        const aptCount = selectedAptitude.size;
        const totalApt = window.TopicData ? window.TopicData.TOTAL_APTITUDE_TOPICS : 32;
        const dsaCount = selectedDsa.size;
        const totalDsa = window.TopicData ? window.TopicData.TOTAL_DSA_TOPICS : 10;

        // Update badges
        if (aptitudeBadge) {
            aptitudeBadge.textContent = `${aptCount} / ${totalApt} Selected`;
        }
        if (dsaBadge) {
            dsaBadge.textContent = `${dsaCount} / ${totalDsa} Selected`;
        }

        // Update category sub-counters
        if (window.TopicData && window.TopicData.APTITUDE_CATEGORIES) {
            window.TopicData.APTITUDE_CATEGORIES.forEach(cat => {
                const countEl = document.getElementById(`count-cat-${cat.categoryId}`);
                if (countEl) {
                    let catSelected = 0;
                    cat.topics.forEach(t => {
                        if (selectedAptitude.has(t.id)) catSelected++;
                    });
                    countEl.textContent = `${catSelected} / ${cat.topics.length}`;
                }
            });
        }

        // Update bottom summary bar
        if (summaryText) {
            summaryText.innerHTML = `Selected: <strong>${aptCount}</strong> Aptitude topic${aptCount === 1 ? '' : 's'} &bull; <strong>${dsaCount}</strong> DSA topic${dsaCount === 1 ? '' : 's'}`;
        }
    }

    /**
     * Clears error banner if current selection satisfies both rules
     */
    function clearErrorIfValid() {
        if (!errorBanner || errorBanner.classList.contains('hidden')) return;

        const validation = window.Validation.validateTopicSelection(
            Array.from(selectedAptitude),
            Array.from(selectedDsa)
        );

        if (validation.isValid) {
            errorBanner.textContent = '';
            errorBanner.classList.add('hidden');
        }
    }

    /**
     * Handles "Continue to Instructions" button click
     */
    function handleContinue() {
        const aptitudeArray = Array.from(selectedAptitude);
        const dsaArray = Array.from(selectedDsa);

        // 1. Validate Selection
        const validation = window.Validation.validateTopicSelection(aptitudeArray, dsaArray);

        if (!validation.isValid) {
            if (errorBanner) {
                errorBanner.textContent = validation.message;
                errorBanner.classList.remove('hidden');
                errorBanner.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
            return;
        }

        // 2. Hide error banner if visible
        if (errorBanner) {
            errorBanner.textContent = '';
            errorBanner.classList.add('hidden');
        }

        // 3. Save to Application State
        window.AppState.setSelectedTopics(aptitudeArray, dsaArray);

        // 4. Navigate to Page 3 (Instructions)
        window.Navigation.navigateTo('page-instructions');
    }

    /**
     * Handles Back button click (returns to Page 1 while preserving inputs)
     */
    function handleBack() {
        if (errorBanner) {
            errorBanner.classList.add('hidden');
        }
        window.Navigation.navigateTo('page-student');
    }

    /**
     * HTML entity escaper
     */
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Expose controller
    window.TopicSelection = {
        init: init,
        getSelectedAptitude: () => Array.from(selectedAptitude),
        getSelectedDsa: () => Array.from(selectedDsa),
        getBackendAptitudeTopicIds: () => {
            const arr = Array.from(selectedAptitude);
            const mapped = window.TopicData ? window.TopicData.mapAptitudeTopicIds(arr) : arr;
            return mapped.slice(0, 3);
        },
        getBackendDsaTopicIds: () => {
            const arr = Array.from(selectedDsa);
            return window.TopicData ? window.TopicData.mapDsaTopicIds(arr) : arr;
        }
    };
})();
