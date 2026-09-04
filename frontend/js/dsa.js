/**
 * DSA Programming Examination Controller (Page 5)
 * 
 * Handles:
 * - Selection of 1 Easy and 1 Medium DSA problem from candidate's selected topics
 * - Two-column problem description and code editor layout
 * - Lightweight textarea editor with Tab indentation support
 * - Clean separation with MockCodeRunner for Run and Submit operations
 * - Question-specific countdown timers (25 min for Easy, 30 min for Medium)
 * - Submission verification, state persistence, and completion flow
 */

(function () {
    // DOM Elements Cache
    let problemHeaderNum;
    let problemDifficultyBadge;
    let problemTopicBadge;
    let problemTitleEl;
    let problemDescEl;
    let problemExamplesEl;
    let problemConstraintsEl;
    let languageSelect;
    let codeEditorTextarea;
    let consoleOutputEl;
    let timerBadge;
    let timerDisplay;
    let problemStatusPills;
    let btnPrev;
    let btnSkip;
    let btnNext;
    let btnReset;
    let btnRun;
    let btnSubmit;
    let modalSubmitConfirm;
    let modalProblemTitle;
    let btnModalCancel;
    let btnModalConfirm;

    /**
     * Initializes DSA module
     */
    function init() {
        cacheDOMElements();
        bindEvents();
    }

    /**
     * Cache DOM references
     */
    function cacheDOMElements() {
        problemHeaderNum = document.getElementById('dsa-problem-number');
        problemDifficultyBadge = document.getElementById('dsa-difficulty-badge');
        problemTopicBadge = document.getElementById('dsa-topic-badge');
        problemTitleEl = document.getElementById('dsa-problem-title');
        problemDescEl = document.getElementById('dsa-problem-desc');
        problemExamplesEl = document.getElementById('dsa-problem-examples');
        problemConstraintsEl = document.getElementById('dsa-problem-constraints');
        languageSelect = document.getElementById('dsa-language-select');
        codeEditorTextarea = document.getElementById('dsa-code-editor');
        consoleOutputEl = document.getElementById('dsa-console-output');
        timerBadge = document.getElementById('dsa-timer-badge');
        timerDisplay = document.getElementById('dsa-timer-display');
        problemStatusPills = document.getElementById('dsa-status-pills');
        btnPrev = document.getElementById('btn-dsa-prev');
        btnSkip = document.getElementById('btn-dsa-skip');
        btnNext = document.getElementById('btn-dsa-next');
        btnReset = document.getElementById('btn-dsa-reset');
        btnRun = document.getElementById('btn-dsa-run');
        btnSubmit = document.getElementById('btn-dsa-submit');
        modalSubmitConfirm = document.getElementById('modal-dsa-submit');
        modalProblemTitle = document.getElementById('modal-dsa-problem-title');
        btnModalCancel = document.getElementById('btn-modal-dsa-cancel');
        btnModalConfirm = document.getElementById('btn-modal-dsa-confirm');
    }

    /**
     * Bind UI event listeners
     */
    function bindEvents() {
        if (btnPrev) btnPrev.addEventListener('click', handlePrevious);
        if (btnSkip) btnSkip.addEventListener('click', handleSkip);
        if (btnNext) btnNext.addEventListener('click', handleNextOrFinish);
        if (btnReset) btnReset.addEventListener('click', handleReset);
        if (btnRun) btnRun.addEventListener('click', handleRun);
        if (btnSubmit) btnSubmit.addEventListener('click', openSubmitModal);

        if (btnModalCancel) btnModalCancel.addEventListener('click', closeSubmitModal);
        if (btnModalConfirm) btnModalConfirm.addEventListener('click', confirmSubmit);

        // Language selector change
        if (languageSelect) {
            languageSelect.addEventListener('change', handleLanguageChange);
        }

        // Auto-save code on input
        if (codeEditorTextarea) {
            codeEditorTextarea.addEventListener('input', handleCodeInput);
            codeEditorTextarea.addEventListener('keydown', handleEditorKeydown);
        }
    }

    /**
     * Handles programming language dropdown switch
     * @param {Event} e 
     */
    function handleLanguageChange(e) {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !exam.questions || exam.questions.length === 0) return;

        const currentQ = exam.questions[exam.currentIndex];
        if (!currentQ) return;

        const previousLang = window.AppState.getDsaLanguage(currentQ.id) || 'java';
        const newLang = e.target.value;

        // 1. Auto-save previous language code from editor
        if (codeEditorTextarea) {
            window.AppState.setDsaCode(currentQ.id, previousLang, codeEditorTextarea.value);
        }

        // 2. Update selected language in AppState
        window.AppState.setDsaLanguage(currentQ.id, newLang);

        // 3. Load code for new language into editor
        const targetCode = window.AppState.getDsaCode(currentQ.id, newLang);
        if (codeEditorTextarea) {
            codeEditorTextarea.value = targetCode;
        }
    }

    /**
     * Handles Tab key in code editor for 4-space indentation
     */
    function handleEditorKeydown(e) {
        if (e.key === 'Tab') {
            e.preventDefault();
            const start = this.selectionStart;
            const end = this.selectionEnd;

            // Insert 4 spaces
            this.value = this.value.substring(0, start) + '    ' + this.value.substring(end);
            this.selectionStart = this.selectionEnd = start + 4;

            handleCodeInput();
        }
    }

    /**
     * Saves code edits into AppState for current problem and language
     */
    function handleCodeInput() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !exam.questions || exam.questions.length === 0) return;

        const currentQ = exam.questions[exam.currentIndex];
        if (currentQ && codeEditorTextarea) {
            const currentLang = window.AppState.getDsaLanguage(currentQ.id) || 'java';
            window.AppState.setDsaCode(currentQ.id, currentLang, codeEditorTextarea.value);
        }
    }

    /**
     * Fisher-Yates shuffle helper
     */
    function shuffleArray(array) {
        const shuffled = [...array];
        for (let i = shuffled.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            const temp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = temp;
        }
        return shuffled;
    }

    /**
     * Generates 2 DSA questions: 1 Easy and 1 Medium STRICTLY from candidate's selected topics.
     * Never falls back to unselected topics.
     * @param {Array<string>} selectedTopicIds 
     * @returns {Object} { success: boolean, questions?: Array, error?: string }
     */
    function selectDsaQuestions(selectedTopicIds) {
        if (!Array.isArray(selectedTopicIds) || selectedTopicIds.length === 0) {
            return {
                success: false,
                error: 'Unable to generate the DSA examination because no DSA topics were selected.'
            };
        }

        const allQuestions = window.MockDsaQuestions ? window.MockDsaQuestions.getAllQuestions() : [];

        // 1. Filter questions strictly belonging to the candidate's selected topics
        const selectedTopicQuestions = allQuestions.filter(q => selectedTopicIds.includes(q.topicId));

        // 2. Pick 1 Easy Problem strictly from selected topics
        const easyPool = selectedTopicQuestions.filter(q => q.difficulty === 'easy');
        if (!easyPool || easyPool.length === 0) {
            return {
                success: false,
                error: 'Unable to generate the DSA examination because a required difficulty question is unavailable for the selected topics.'
            };
        }
        const shuffledEasy = shuffleArray(easyPool);
        const easyQuestion = shuffledEasy[0];

        // 3. Pick 1 Medium Problem strictly from selected topics (ensuring distinct question ID)
        const mediumPool = selectedTopicQuestions.filter(q => q.difficulty === 'medium' && q.id !== easyQuestion.id);
        if (!mediumPool || mediumPool.length === 0) {
            return {
                success: false,
                error: 'Unable to generate the DSA examination because a required difficulty question is unavailable for the selected topics.'
            };
        }
        const shuffledMedium = shuffleArray(mediumPool);
        const mediumQuestion = shuffledMedium[0];

        return {
            success: true,
            questions: [easyQuestion, mediumQuestion]
        };
    }

    /**
     * Starts the DSA Assessment session by fetching 2 assigned problems from backend
     */
    async function startExam() {
        const assessmentId = window.AppState ? window.AppState.getAssessmentId() : null;
        if (!assessmentId) {
            alert('Assessment session has expired or is invalid. Please return to Page 1.');
            if (window.Navigation) window.Navigation.navigateTo('page-student');
            return false;
        }

        let topicIds = [];
        if (window.TopicSelection && typeof window.TopicSelection.getBackendDsaTopicIds === 'function') {
            topicIds = window.TopicSelection.getBackendDsaTopicIds();
        } else {
            const selected = window.AppState ? window.AppState.getSelectedTopics().dsa : [];
            topicIds = window.TopicData ? window.TopicData.mapDsaTopicIds(selected) : [1];
        }

        if (!topicIds || topicIds.length === 0) {
            topicIds = [1];
        }

        let dsaResponse;
        try {
            dsaResponse = await window.ApiClient.generateDsaExam({
                assessmentId: assessmentId,
                topicIds: topicIds
            });
        } catch (err) {
            console.error('[DSA] Failed to generate exam:', err);
            alert(err.message || 'Unable to generate the DSA examination. Please contact the lab administrator.');
            return false;
        }

        if (!dsaResponse || !dsaResponse.questions || dsaResponse.questions.length !== 2) {
            alert('Unable to retrieve the 2 assigned DSA problems from the server.');
            return false;
        }

        // 1. Initialize DSA state in AppState with backend problems
        window.AppState.initDsaExam(dsaResponse.questions);

        // 2. Navigate to DSA Page view
        window.Navigation.navigateTo('page-dsa');

        // 3. Render Problem 1 (Easy)
        renderProblem();

        // 4. Start Easy Timer (25 minutes = 1500 seconds)
        const initialEasyTime = 1500;
        startProblemTimer(initialEasyTime);

        return true;
    }

    /**
     * Starts countdown timer for the active problem
     */
    function startProblemTimer(durationSeconds) {
        window.ExamTimer.start(
            durationSeconds,
            handleTimerTick,
            handleTimerExpire
        );
    }

    /**
     * Timer tick handler - updates UI and persists remaining seconds to AppState
     */
    function handleTimerTick(formattedTime, remainingSeconds, warningLevel) {
        if (timerDisplay) {
            timerDisplay.textContent = formattedTime;
        }

        if (timerBadge) {
            timerBadge.className = `timer-badge timer-${warningLevel}`;
        }

        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (exam) {
            const diffKey = exam.currentIndex === 0 ? 'easy' : 'medium';
            window.AppState.setDsaTimeRemaining(diffKey, remainingSeconds);
        }
    }

    /**
     * Timer expire handler
     */
    function handleTimerExpire() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        if (exam.currentIndex === 0) {
            window.AppState.setDsaTimeRemaining('easy', 0);
            alert('Time limit for Problem 1 (Easy - 25 mins) has expired! Moving automatically to Problem 2 (Medium).');
            switchToProblem(1);
        } else {
            window.AppState.setDsaTimeRemaining('medium', 0);
            alert('Time limit for Problem 2 (Medium - 30 mins) has expired! Finalizing DSA assessment.');
            finishExam(true);
        }
    }

    /**
     * Renders active DSA problem details, code editor, status, and console
     */
    function renderProblem() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !exam.questions || exam.questions.length === 0) return;

        const idx = exam.currentIndex;
        const total = exam.questions.length;
        const q = exam.questions[idx];

        // 1. Header Information
        if (problemHeaderNum) {
            problemHeaderNum.textContent = `Problem #${idx + 1} of ${total}`;
        }

        if (problemDifficultyBadge) {
            const diff = (q.difficulty || 'easy').toLowerCase();
            problemDifficultyBadge.textContent = diff.toUpperCase();
            problemDifficultyBadge.className = `difficulty-badge diff-${diff}`;
        }

        if (problemTopicBadge) {
            let topicName = q.topicName;
            if (!topicName && window.TopicData && window.TopicData.DSA_TOPICS) {
                const tMatch = window.TopicData.DSA_TOPICS.find(t => t.id === q.topicId || (window.TopicData.DSA_CODE_TO_ID && window.TopicData.DSA_CODE_TO_ID[t.id] === q.topicId));
                if (tMatch) topicName = tMatch.name;
            }
            problemTopicBadge.textContent = topicName || 'DSA Topic';
        }

        // 2. Title & Description
        if (problemTitleEl) {
            problemTitleEl.textContent = q.title || 'DSA Problem';
        }

        if (problemDescEl) {
            problemDescEl.innerHTML = formatTextWithLineBreaks(q.description || '');
        }

        // 3. Examples
        if (problemExamplesEl) {
            let examplesList = [];
            if (Array.isArray(q.examples)) {
                examplesList = q.examples;
            } else if (typeof q.examples === 'string') {
                try {
                    const parsed = JSON.parse(q.examples);
                    if (Array.isArray(parsed)) examplesList = parsed;
                    else examplesList = [{ input: q.examples, output: '' }];
                } catch (e) {
                    examplesList = [{ input: q.examples, output: '' }];
                }
            }

            let html = '';
            examplesList.forEach((ex, exIdx) => {
                html += `
                    <div class="example-box">
                        <div class="example-title">Example ${exIdx + 1}:</div>
                        <div class="example-row"><span class="lbl">Input:</span> <code>${escapeHtml(ex.input || '')}</code></div>
                        <div class="example-row"><span class="lbl">Output:</span> <code>${escapeHtml(ex.output || '')}</code></div>
                        ${ex.explanation ? `<div class="example-row"><span class="lbl">Explanation:</span> <span class="exp">${escapeHtml(ex.explanation)}</span></div>` : ''}
                    </div>
                `;
            });
            problemExamplesEl.innerHTML = html || '<div style="color: var(--color-text-muted); font-size: 0.8125rem;">No examples provided.</div>';
        }

        // 4. Constraints
        if (problemConstraintsEl) {
            let constraintsList = [];
            if (Array.isArray(q.constraints)) {
                constraintsList = q.constraints;
            } else if (typeof q.constraints === 'string') {
                try {
                    const parsed = JSON.parse(q.constraints);
                    if (Array.isArray(parsed)) {
                        constraintsList = parsed;
                    } else {
                        constraintsList = q.constraints.split('\n').map(s => s.trim()).filter(Boolean);
                    }
                } catch (e) {
                    constraintsList = q.constraints.split('\n').map(s => s.trim()).filter(Boolean);
                }
            }

            let html = '';
            constraintsList.forEach(c => {
                html += `<li><code>${escapeHtml(c)}</code></li>`;
            });
            problemConstraintsEl.innerHTML = html || '<li><code>Standard execution memory and time limits apply.</code></li>';
        }

        // 5. Code Editor & Language Selector
        const currentLang = window.AppState.getDsaLanguage(q.id) || 'java';
        if (languageSelect) {
            languageSelect.value = currentLang;
        }
        if (codeEditorTextarea) {
            let savedCode = window.AppState.getDsaCode(q.id, currentLang);
            if (!savedCode && q.starterCode) {
                if (typeof q.starterCode === 'object' && q.starterCode[currentLang]) {
                    savedCode = q.starterCode[currentLang];
                } else if (typeof q.starterCode === 'string') {
                    savedCode = q.starterCode;
                }
                window.AppState.setDsaCode(q.id, currentLang, savedCode);
            }
            codeEditorTextarea.value = savedCode || '';
        }

        // 6. Console initial message
        if (consoleOutputEl) {
            const submission = exam.submissions[q.id];
            if (submission) {
                const subLang = submission.language ? ` [${submission.language.toUpperCase()}]` : '';
                const verdict = submission.verdict || submission.rawStatus || 'Submitted';
                const isAcc = (verdict.toUpperCase() === 'ACCEPTED');
                consoleOutputEl.innerHTML = `<span class="log-info">Current Submission Verdict${subLang}:</span> <strong class="${isAcc ? 'text-success' : 'text-error'}">${escapeHtml(verdict)}</strong> (${submission.testCasesPassed ?? 0}/${submission.totalTestCases ?? 0} Passed)`;
            } else {
                consoleOutputEl.textContent = 'Run your code to see the output.';
            }
        }

        // 7. Navigation Buttons
        if (btnPrev) {
            btnPrev.disabled = (idx === 0);
        }

        if (btnNext) {
            if (idx === total - 1) {
                btnNext.textContent = 'Finish DSA \u2714';
                btnNext.classList.add('btn-finish-state');
            } else {
                btnNext.textContent = 'Next Problem \u2192';
                btnNext.classList.remove('btn-finish-state');
            }
        }

        // 8. Status Pills
        renderStatusPills();
    }

    /**
     * Renders Problem 1 & Problem 2 status tabs
     */
    function renderStatusPills() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!problemStatusPills || !exam || !exam.questions) return;

        let html = '';
        exam.questions.forEach((q, i) => {
            const isCurrent = (i === exam.currentIndex);
            const isSubmitted = exam.submissions.hasOwnProperty(q.id);
            const isSkipped = exam.skipped.includes(q.id);

            let statusText = 'Unanswered';
            let statusClass = 'pill-unanswered';

            if (isSubmitted) {
                statusText = 'Submitted';
                statusClass = 'pill-submitted';
            } else if (isSkipped) {
                statusText = 'Skipped';
                statusClass = 'pill-skipped';
            }

            html += `
                <button type="button" class="dsa-pill ${isCurrent ? 'active' : ''} ${statusClass}" data-index="${i}">
                    <span class="pill-title">Problem ${i + 1} (${q.difficulty.toUpperCase()})</span>
                    <span class="pill-status">${statusText}</span>
                </button>
            `;
        });

        problemStatusPills.innerHTML = html;

        // Attach click listeners to pills
        problemStatusPills.querySelectorAll('.dsa-pill').forEach(pill => {
            pill.addEventListener('click', (e) => {
                const targetIdx = parseInt(pill.getAttribute('data-index'), 10);
                if (targetIdx !== exam.currentIndex) {
                    switchToProblem(targetIdx);
                }
            });
        });
    }

    /**
     * Switches active problem between Problem 1 and Problem 2
     */
    function switchToProblem(newIndex) {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !exam.questions || exam.questions.length !== 2) {
            console.error("DSA exam is not initialized correctly.");
            return;
        }

        // Auto-save current code before switching
        handleCodeInput();

        // Stop current timer
        window.ExamTimer.stop();

        // Update active index
        window.AppState.setDsaCurrentIndex(newIndex);

        // Render target problem
        renderProblem();

        // Read the target problem's saved remaining time from AppState
        const diffKey = newIndex === 0 ? 'easy' : 'medium';
        const savedRemainingTime = (exam.timeRemaining && typeof exam.timeRemaining[diffKey] === 'number')
            ? exam.timeRemaining[diffKey]
            : (newIndex === 0 ? 1500 : 1800);

        if (savedRemainingTime > 0) {
            startProblemTimer(savedRemainingTime);
        } else {
            // If remaining time is 0, do not restart timer. Update display to 00:00 and critical state
            if (timerDisplay) timerDisplay.textContent = '00:00';
            if (timerBadge) timerBadge.className = 'timer-badge timer-critical';
        }
    }

    /**
     * Handles Reset Code button click - resets ONLY the active language of the active problem
     */
    function handleReset() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        const currentQ = exam.questions[exam.currentIndex];
        const currentLang = window.AppState.getDsaLanguage(currentQ.id) || 'java';
        let defaultCode = '';
        if (currentQ.starterCode && typeof currentQ.starterCode === 'object') {
            defaultCode = currentQ.starterCode[currentLang] || '';
        } else if (typeof currentQ.starterCode === 'string') {
            defaultCode = currentQ.starterCode;
        }

        if (codeEditorTextarea) {
            codeEditorTextarea.value = defaultCode;
        }

        window.AppState.setDsaCode(currentQ.id, currentLang, defaultCode);

        if (consoleOutputEl) {
            consoleOutputEl.innerHTML = `<span class="log-success">\u2714 ${currentLang.toUpperCase()} starter template restored successfully.</span>`;
        }
    }

    /**
     * Handles Run Code button click
     */
    async function handleRun() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !exam.questions || exam.questions.length === 0) return;

        const assessmentId = window.AppState ? window.AppState.getAssessmentId() : null;
        if (!assessmentId) {
            alert('Assessment session has expired or is invalid. Please return to Page 1.');
            return;
        }

        const currentQ = exam.questions[exam.currentIndex];
        const currentLang = window.AppState.getDsaLanguage(currentQ.id) || 'java';
        const code = codeEditorTextarea ? codeEditorTextarea.value : '';

        if (!code || !code.trim()) {
            alert('Please write some code before running sample test cases.');
            return;
        }

        if (btnRun) {
            btnRun.disabled = true;
            btnRun.textContent = 'Running...';
        }
        if (consoleOutputEl) {
            consoleOutputEl.innerHTML = `<span class="log-info">Compiling and executing against sample test cases (${currentLang.toUpperCase()})...</span>`;
        }

        try {
            const runResult = await window.ApiClient.runDsaCode({
                assessmentId: assessmentId,
                questionId: currentQ.id,
                language: currentLang.toUpperCase(),
                sourceCode: code
            });

            if (!consoleOutputEl) return;

            if (runResult && runResult.testCases && runResult.testCases.length > 0) {
                let logHtml = `<div class="log-header">\u2714 Sample Test Cases Execution Result (${currentLang.toUpperCase()})</div>\n`;
                let passedCount = 0;

                runResult.testCases.forEach(tc => {
                    const normExpected = (tc.expectedOutput || '').trim();
                    const normActual = (tc.actualOutput || '').trim();
                    const isPassed = (tc.status === 'SUCCESS' || tc.status === 'PASSED') && (normActual === normExpected);
                    if (isPassed) passedCount++;

                    logHtml += `
                        <div class="test-case-log ${isPassed ? 'tc-passed' : 'tc-failed'}">
                            <strong>Test Case ${tc.testCaseNumber}:</strong> ${isPassed ? 'Passed \u2714' : 'Failed \u2718'}
                            ${tc.status && tc.status !== 'SUCCESS' && tc.status !== 'PASSED' ? `<span class="verdict-tag tag-wrong" style="margin-left: 6px;">${escapeHtml(tc.status)}</span>` : ''}
                            <div class="tc-detail">Input: <code>${escapeHtml(tc.input || '')}</code></div>
                            <div class="tc-detail">Expected: <code>${escapeHtml(tc.expectedOutput || '')}</code></div>
                            <div class="tc-detail">Actual: <code>${escapeHtml(tc.actualOutput || '')}</code></div>
                            ${tc.error ? `<div class="tc-detail error-pre">${escapeHtml(tc.error)}</div>` : ''}
                            ${tc.executionTimeMs != null ? `<div class="tc-detail" style="font-size: 0.75rem; color: var(--color-text-muted);">Runtime: ${tc.executionTimeMs} ms</div>` : ''}
                        </div>
                    `;
                });

                logHtml += `\n<div class="log-summary">Sample Tests Passed: <strong>${passedCount} / ${runResult.testCases.length}</strong></div>`;
                consoleOutputEl.innerHTML = logHtml;
            } else {
                consoleOutputEl.innerHTML = `<div class="log-info">No sample test cases returned from server.</div>`;
            }
        } catch (err) {
            console.error('[DSA] Run failed:', err);
            if (consoleOutputEl) {
                consoleOutputEl.innerHTML = `
                    <div class="log-error">
                        <strong>\u2718 Execution Failed</strong>\n
                        <pre class="error-pre">${escapeHtml(err.message || 'An error occurred during code execution.')}</pre>
                    </div>
                `;
            }
        } finally {
            if (btnRun) {
                btnRun.disabled = false;
                btnRun.innerHTML = 'Run Sample Tests &#9654;';
            }
        }
    }

    /**
     * Opens submission confirmation modal
     */
    function openSubmitModal() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !modalSubmitConfirm) return;

        const currentQ = exam.questions[exam.currentIndex];
        const currentLang = window.AppState.getDsaLanguage(currentQ.id) || 'java';
        if (modalProblemTitle) {
            modalProblemTitle.textContent = `Problem #${exam.currentIndex + 1}: ${currentQ.title} (${currentQ.difficulty.toUpperCase()} - ${currentLang.toUpperCase()})`;
        }

        modalSubmitConfirm.classList.remove('hidden');
    }

    /**
     * Closes submit confirmation modal
     */
    function closeSubmitModal() {
        if (modalSubmitConfirm) {
            modalSubmitConfirm.classList.add('hidden');
        }
    }

    /**
     * Executes authoritative submit after modal confirmation
     */
    async function confirmSubmit() {
        closeSubmitModal();

        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !exam.questions || exam.questions.length === 0) return;

        const assessmentId = window.AppState ? window.AppState.getAssessmentId() : null;
        if (!assessmentId) {
            alert('Assessment session has expired or is invalid. Please return to Page 1.');
            return;
        }

        const currentQ = exam.questions[exam.currentIndex];
        const currentLang = window.AppState.getDsaLanguage(currentQ.id) || 'java';
        const code = codeEditorTextarea ? codeEditorTextarea.value : '';

        if (!code || !code.trim()) {
            alert('Please write code before submitting your solution.');
            return;
        }

        if (btnSubmit) {
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Submitting...';
        }
        if (btnRun) btnRun.disabled = true;

        if (consoleOutputEl) {
            consoleOutputEl.innerHTML = `<span class="log-info">Evaluating solution against all visible and hidden test cases (${currentLang.toUpperCase()})...</span>`;
        }

        try {
            const submitResult = await window.ApiClient.submitDsaCode({
                assessmentId: assessmentId,
                questionId: currentQ.id,
                language: currentLang.toUpperCase(),
                sourceCode: code
            });

            const rawStatus = submitResult.resultStatus || submitResult.status || 'PENDING';
            const isAccepted = (rawStatus.toUpperCase() === 'ACCEPTED');
            const displayVerdict = isAccepted ? 'Accepted' : rawStatus.replace(/_/g, ' ');

            const normalizedSubmission = {
                submissionId: submitResult.submissionId,
                verdict: displayVerdict,
                rawStatus: rawStatus,
                testCasesPassed: submitResult.passedTestCases != null ? submitResult.passedTestCases : 0,
                totalTestCases: submitResult.totalTestCases != null ? submitResult.totalTestCases : 0,
                failedTestCases: submitResult.failedTestCases != null ? submitResult.failedTestCases : 0,
                executionTime: submitResult.executionTimeMs != null ? `${submitResult.executionTimeMs} ms` : 'N/A',
                language: currentLang,
                code: code
            };

            window.AppState.recordDsaSubmission(currentQ.id, normalizedSubmission);

            if (consoleOutputEl) {
                if (isAccepted) {
                    consoleOutputEl.innerHTML = `
                        <div class="submission-success-card">
                            <div class="sub-verdict-title text-success">\u2714 Accepted (${currentLang.toUpperCase()})</div>
                            <div class="sub-meta">All Hidden & Sample Test Cases Passed: <strong>${normalizedSubmission.testCasesPassed} / ${normalizedSubmission.totalTestCases}</strong></div>
                            <div class="sub-meta">Execution Time: ${normalizedSubmission.executionTime}</div>
                        </div>
                    `;
                } else {
                    consoleOutputEl.innerHTML = `
                        <div class="submission-error-card">
                            <div class="sub-verdict-title text-error">\u2718 ${escapeHtml(displayVerdict)} (${currentLang.toUpperCase()})</div>
                            <div class="sub-meta">Test Cases Passed: <strong>${normalizedSubmission.testCasesPassed} / ${normalizedSubmission.totalTestCases}</strong></div>
                            <div class="sub-meta" style="margin-top: 4px; font-size: 0.8125rem; color: var(--color-text-muted);">Failed Test Cases: ${normalizedSubmission.failedTestCases}</div>
                        </div>
                    `;
                }
            }

            renderStatusPills();
        } catch (err) {
            console.error('[DSA] Submit failed:', err);
            if (consoleOutputEl) {
                consoleOutputEl.innerHTML = `
                    <div class="log-error">
                        <strong>\u2718 Submission Failed</strong>\n
                        <pre class="error-pre">${escapeHtml(err.message || 'An error occurred during code submission.')}</pre>
                    </div>
                `;
            }
        } finally {
            if (btnSubmit) {
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Submit Solution';
            }
            if (btnRun) btnRun.disabled = false;
        }
    }

    /**
     * Handles Previous button click
     */
    function handlePrevious() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || exam.currentIndex <= 0) return;

        switchToProblem(0);
    }

    /**
     * Handles Next or Finish button click
     */
    function handleNextOrFinish() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        if (exam.currentIndex === 0) {
            switchToProblem(1);
        } else {
            finishExam(false);
        }
    }

    /**
     * Handles Skip button click
     */
    function handleSkip() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        const currentQ = exam.questions[exam.currentIndex];
        window.AppState.skipDsaQuestion(currentQ.id);

        if (exam.currentIndex === 0) {
            switchToProblem(1);
        } else {
            finishExam(false);
        }
    }

    /**
     * Finalizes DSA examination, calls backend completion API, and transitions to Page 6
     * @param {boolean} isAutoSubmit 
     */
    async function finishExam(isAutoSubmit = false) {
        window.ExamTimer.stop();

        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        // Auto-save active code
        handleCodeInput();

        const assessmentId = window.AppState ? window.AppState.getAssessmentId() : null;
        if (!assessmentId) {
            alert('Assessment session has expired or is invalid.');
            return;
        }

        if (btnNext) btnNext.disabled = true;
        if (btnSubmit) btnSubmit.disabled = true;
        if (btnSkip) btnSkip.disabled = true;

        // Local summary computation for AppState
        let attemptedCount = 0;
        let acceptedCount = 0;
        let failedCount = 0;
        let skippedCount = 0;
        const problemDetails = [];

        exam.questions.forEach((q, idx) => {
            const sub = exam.submissions[q.id];
            const isSkipped = exam.skipped.includes(q.id);

            if (sub) {
                attemptedCount++;
                if (sub.verdict === 'Accepted' || sub.rawStatus === 'ACCEPTED') {
                    acceptedCount++;
                } else {
                    failedCount++;
                }
                problemDetails.push({
                    problemIndex: idx + 1,
                    questionId: q.id,
                    title: q.title,
                    difficulty: q.difficulty,
                    verdict: sub.verdict,
                    testCasesPassed: sub.testCasesPassed,
                    totalTestCases: sub.totalTestCases
                });
            } else {
                skippedCount++;
                problemDetails.push({
                    problemIndex: idx + 1,
                    questionId: q.id,
                    title: q.title,
                    difficulty: q.difficulty,
                    verdict: isSkipped ? 'Skipped' : 'Unanswered',
                    testCasesPassed: 0,
                    totalTestCases: 0
                });
            }
        });

        const dsaResults = {
            totalProblems: 2,
            attempted: attemptedCount,
            accepted: acceptedCount,
            failed: failedCount,
            skipped: skippedCount,
            problemDetails: problemDetails,
            isAutoSubmit: isAutoSubmit
        };

        window.AppState.completeDsaExam(dsaResults);

        try {
            // Complete Assessment on backend
            try {
                await window.ApiClient.completeAssessment(assessmentId);
            } catch (compErr) {
                console.warn('[DSA] Complete assessment returned warning (may already be completed):', compErr);
            }

            // Fetch finalized authoritative result
            const finalResult = await window.ApiClient.getAssessmentResult(assessmentId);
            window.AppState.setFinalResult(finalResult);

            // Render summary on Page 6
            renderFinalSummaryOnPage6(finalResult);
            window.Navigation.navigateTo('page-result');
        } catch (err) {
            console.error('[DSA] Failed to finalize assessment on server:', err);
            alert('Assessment finalized locally, but server sync reported: ' + (err.message || 'Unknown error'));

            const fallbackResult = window.AppState.getFinalResult() || {
                studentName: (window.AppState.getStudent() || {}).name || 'Candidate',
                rollNumber: (window.AppState.getStudent() || {}).rollNumber || 'N/A',
                assessmentId: assessmentId,
                status: 'COMPLETED',
                aptitudeScore: (window.AppState.getAptitudeResults() || {}).score || 0,
                aptitudeTotal: 20,
                dsaScore: acceptedCount * 10,
                dsaTotal: 20,
                totalScore: ((window.AppState.getAptitudeResults() || {}).score || 0) + (acceptedCount * 10),
                totalMarks: 40
            };
            renderFinalSummaryOnPage6(fallbackResult);
            window.Navigation.navigateTo('page-result');
        }
    }

    /**
     * Renders authoritative final summary on Page 6 from backend result
     * @param {Object} finalResult
     */
    function renderFinalSummaryOnPage6(finalResult) {
        const resultContainer = document.getElementById('result-container');
        if (!resultContainer) return;

        const aptResults = window.AppState ? window.AppState.getAptitudeResults() : null;
        const dsaExam = window.AppState ? window.AppState.getDsaExam() : null;
        const student = window.AppState ? window.AppState.getStudent() : null;

        const studentName = (finalResult && finalResult.studentName) || (student && student.name) || 'Candidate';
        const rollNumber = (finalResult && finalResult.rollNumber) || (student && student.rollNumber) || 'N/A';
        const assessmentId = (finalResult && finalResult.assessmentId) || (window.AppState ? window.AppState.getAssessmentId() : 'N/A');
        const status = (finalResult && finalResult.status) || 'COMPLETED';

        const aptitudeScore = (finalResult && typeof finalResult.aptitudeScore === 'number') ? finalResult.aptitudeScore : (aptResults ? aptResults.score : 0);
        const aptitudeTotal = (finalResult && typeof finalResult.aptitudeTotal === 'number') ? finalResult.aptitudeTotal : 20;

        const dsaScore = (finalResult && typeof finalResult.dsaScore === 'number') ? finalResult.dsaScore : 0;
        const dsaTotal = (finalResult && typeof finalResult.dsaTotal === 'number') ? finalResult.dsaTotal : 20;

        const totalScore = (finalResult && typeof finalResult.totalScore === 'number') ? finalResult.totalScore : (aptitudeScore + dsaScore);
        const totalMarks = (finalResult && typeof finalResult.totalMarks === 'number') ? finalResult.totalMarks : (aptitudeTotal + dsaTotal);

        let dsaProblemsHtml = '';
        if (dsaExam && dsaExam.questions && dsaExam.questions.length > 0) {
            dsaExam.questions.forEach((q, idx) => {
                const sub = dsaExam.submissions ? dsaExam.submissions[q.id] : null;
                const isSkipped = dsaExam.skipped && dsaExam.skipped.includes(q.id);
                const isAcc = sub && (sub.verdict === 'Accepted' || sub.rawStatus === 'ACCEPTED');
                const verdict = sub ? (sub.verdict || sub.rawStatus || 'Submitted') : (isSkipped ? 'Skipped' : 'Unanswered');
                const passed = sub ? (sub.testCasesPassed ?? 0) : 0;
                const totalCases = sub ? (sub.totalTestCases ?? 0) : 0;

                dsaProblemsHtml += `
                    <div class="dsa-res-item ${isAcc ? 'res-acc' : 'res-fail'}">
                        <div>
                            <strong>Problem ${idx + 1} (${q.difficulty ? q.difficulty.toUpperCase() : 'DSA'}): ${escapeHtml(q.title || '')}</strong>
                            <div style="font-size: 0.8125rem; color: var(--color-text-muted);">
                                Test Cases Passed: ${passed} / ${totalCases}
                            </div>
                        </div>
                        <div class="verdict-tag ${isAcc ? 'tag-accepted' : 'tag-wrong'}">${escapeHtml(verdict)}</div>
                    </div>
                `;
            });
        }

        resultContainer.innerHTML = `
            <div class="final-results-wrapper">
                <div class="results-header-box">
                    <h2>Assessment Session Final Result</h2>
                    <p style="margin-top: 6px; color: var(--color-text-main);">
                        Candidate: <strong>${escapeHtml(studentName)}</strong> &bull; Roll Number: <strong>${escapeHtml(rollNumber)}</strong> &bull; Assessment ID: <strong>#${escapeHtml(String(assessmentId))}</strong>
                    </p>
                    <div style="margin-top: 6px; font-size: 0.8125rem; color: var(--color-text-muted);">
                        Status: <span class="verdict-tag tag-accepted" style="font-size: 0.75rem;">${escapeHtml(status)}</span>
                        ${finalResult && finalResult.completedAt ? ` &bull; Completed: ${new Date(finalResult.completedAt).toLocaleString()}` : ''}
                    </div>
                </div>

                <!-- Total Overall Score Banner -->
                <div style="background: linear-gradient(135deg, #FFF8E1 0%, #FFE082 100%); border: 1.5px solid var(--color-primary); border-radius: var(--radius-md); padding: 1.25rem; text-align: center; margin-bottom: var(--space-lg);">
                    <div style="font-size: 0.875rem; font-weight: 700; color: var(--color-primary-dark); text-transform: uppercase; letter-spacing: 0.5px;">Overall Final Score</div>
                    <div style="font-size: 2.5rem; font-weight: 900; color: var(--color-primary-dark); line-height: 1.2; margin: 4px 0;">
                        ${totalScore} <span style="font-size: 1.25rem; font-weight: 600; color: var(--color-text-muted);">/ ${totalMarks}</span>
                    </div>
                    <div style="font-size: 0.8125rem; color: var(--color-text-muted);">Official Server-Verified Score</div>
                </div>

                <div class="summary-two-col">
                    <!-- Aptitude Results Column -->
                    <div class="result-section-box">
                        <h3 class="res-sec-title">Aptitude Assessment</h3>
                        <div class="stat-card" style="margin-bottom: 1rem;">
                            <div class="stat-value">${aptitudeScore} / ${aptitudeTotal}</div>
                            <div class="stat-label">Authoritative Score</div>
                        </div>
                        ${aptResults ? `
                            <div class="stat-mini-row"><span>Attempted:</span> <strong>${aptResults.attempted || 0} / 20</strong></div>
                            <div class="stat-mini-row"><span>Correct Answers:</span> <strong style="color: var(--color-success);">${aptResults.correct || 0}</strong></div>
                            <div class="stat-mini-row"><span>Incorrect Answers:</span> <strong style="color: var(--color-error);">${aptResults.wrong || 0}</strong></div>
                            <div class="stat-mini-row"><span>Skipped:</span> <strong>${aptResults.skipped || 0}</strong></div>
                        ` : `
                            <div class="stat-mini-row"><span>Score:</span> <strong>${aptitudeScore} / ${aptitudeTotal}</strong></div>
                        `}
                    </div>

                    <!-- DSA Results Column -->
                    <div class="result-section-box">
                        <h3 class="res-sec-title">DSA Coding Assessment</h3>
                        <div class="stat-card" style="margin-bottom: 1rem;">
                            <div class="stat-value">${dsaScore} / ${dsaTotal}</div>
                            <div class="stat-label">Authoritative Score</div>
                        </div>
                        <div class="dsa-details-list">
                            ${dsaProblemsHtml || '<div style="color: var(--color-text-muted); font-size: 0.875rem;">No problem details available.</div>'}
                        </div>
                    </div>
                </div>

                <div class="info-box" style="margin-top: 1.5rem;">
                    <strong>Assessment Complete:</strong> Your responses, execution logs, and scores have been authoritatively evaluated and stored by the RemotePrep evaluation engine.
                </div>
            </div>
        `;
    }

    /**
     * Helper to format text with newlines
     */
    function formatTextWithLineBreaks(str) {
        if (!str) return '';
        return str.split('\n').map(p => `<p style="margin-bottom: 0.75rem;">${escapeHtml(p)}</p>`).join('');
    }

    /**
     * HTML entity escaper
     */
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Expose Dsa controller
    window.Dsa = {
        init: init,
        startExam: startExam,
        selectDsaQuestions: selectDsaQuestions,
        renderProblem: renderProblem,
        finishExam: finishExam
    };
})();
