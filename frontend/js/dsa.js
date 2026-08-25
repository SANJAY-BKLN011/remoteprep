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

        // Auto-save code on input
        if (codeEditorTextarea) {
            codeEditorTextarea.addEventListener('input', handleCodeInput);
            codeEditorTextarea.addEventListener('keydown', handleEditorKeydown);
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
     * Saves code edits into AppState
     */
    function handleCodeInput() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !exam.questions || exam.questions.length === 0) return;

        const currentQ = exam.questions[exam.currentIndex];
        if (currentQ && codeEditorTextarea) {
            window.AppState.setDsaCode(currentQ.id, codeEditorTextarea.value);
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
     * Starts the DSA Assessment session
     */
    function startExam() {
        const selectedTopics = window.AppState ? window.AppState.getSelectedTopics().dsa : [];
        const result = selectDsaQuestions(selectedTopics);

        if (!result.success || !result.questions || result.questions.length !== 2) {
            alert(result.error || 'Unable to generate the DSA examination because a required difficulty question is unavailable for the selected topics.');
            return false;
        }

        const questions = result.questions;

        // 1. Initialize DSA state in AppState
        window.AppState.initDsaExam(questions);

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
            problemDifficultyBadge.textContent = q.difficulty.toUpperCase();
            problemDifficultyBadge.className = `difficulty-badge diff-${q.difficulty}`;
        }

        if (problemTopicBadge) {
            problemTopicBadge.textContent = q.topicName || 'DSA Topic';
        }

        // 2. Title & Description
        if (problemTitleEl) {
            problemTitleEl.textContent = q.title;
        }

        if (problemDescEl) {
            problemDescEl.innerHTML = formatTextWithLineBreaks(q.description);
        }

        // 3. Examples
        if (problemExamplesEl) {
            let html = '';
            (q.examples || []).forEach((ex, exIdx) => {
                html += `
                    <div class="example-box">
                        <div class="example-title">Example ${exIdx + 1}:</div>
                        <div class="example-row"><span class="lbl">Input:</span> <code>${escapeHtml(ex.input)}</code></div>
                        <div class="example-row"><span class="lbl">Output:</span> <code>${escapeHtml(ex.output)}</code></div>
                        ${ex.explanation ? `<div class="example-row"><span class="lbl">Explanation:</span> <span class="exp">${escapeHtml(ex.explanation)}</span></div>` : ''}
                    </div>
                `;
            });
            problemExamplesEl.innerHTML = html;
        }

        // 4. Constraints
        if (problemConstraintsEl) {
            let html = '';
            (q.constraints || []).forEach(c => {
                html += `<li><code>${escapeHtml(c)}</code></li>`;
            });
            problemConstraintsEl.innerHTML = html;
        }

        // 5. Code Editor
        if (codeEditorTextarea) {
            const savedCode = window.AppState.getDsaCode(q.id);
            codeEditorTextarea.value = savedCode;
        }

        // 6. Console initial message
        if (consoleOutputEl) {
            const submission = exam.submissions[q.id];
            if (submission) {
                consoleOutputEl.innerHTML = `<span class="log-info">Current Submission Verdict:</span> <strong class="verdict-${submission.verdict.toLowerCase().replace(/\s+/g, '-')}">${escapeHtml(submission.verdict)}</strong> (${submission.testCasesPassed}/${submission.totalTestCases} Passed)`;
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
                statusText = 'Completed \u2714';
                statusClass = 'pill-completed';
            } else if (isSkipped) {
                statusText = 'Skipped';
                statusClass = 'pill-skipped';
            }

            if (isCurrent) {
                statusClass += ' pill-active';
            }

            html += `
                <button type="button" class="problem-status-pill ${statusClass}" data-index="${i}">
                    <strong>Problem ${i + 1} (${q.difficulty.toUpperCase()}):</strong> ${statusText}
                </button>
            `;
        });

        problemStatusPills.innerHTML = html;

        // Bind pill click for quick jump
        const pillButtons = problemStatusPills.querySelectorAll('.problem-status-pill');
        pillButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetIdx = parseInt(btn.getAttribute('data-index'), 10);
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
        if (!exam) return;

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
     * Handles Reset Code button click
     */
    function handleReset() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        const currentQ = exam.questions[exam.currentIndex];
        const defaultCode = (currentQ.starterCode && currentQ.starterCode.java) ? currentQ.starterCode.java : '';

        if (codeEditorTextarea) {
            codeEditorTextarea.value = defaultCode;
        }

        window.AppState.setDsaCode(currentQ.id, defaultCode);

        if (consoleOutputEl) {
            consoleOutputEl.innerHTML = '<span class="log-success">\u2714 Code reset successfully to original starter template.</span>';
        }
    }

    /**
     * Handles Run Code button click
     */
    function handleRun() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        const currentQ = exam.questions[exam.currentIndex];
        const code = codeEditorTextarea ? codeEditorTextarea.value : '';

        if (consoleOutputEl) {
            consoleOutputEl.textContent = 'Running sample test cases...';
        }

        // Mock execution run
        const runResult = window.MockCodeRunner ? window.MockCodeRunner.run(code, currentQ) : { status: 'COMPILATION_ERROR', errorMessage: 'Runner unavailable.' };

        if (!consoleOutputEl) return;

        if (runResult.status === 'SUCCESS') {
            let logHtml = `<div class="log-header">\u2714 Sample Test Cases Execution Result</div>\n`;
            runResult.testResults.forEach(tc => {
                logHtml += `
                    <div class="test-case-log ${tc.passed ? 'tc-passed' : 'tc-failed'}">
                        <strong>Test Case ${tc.caseNumber}:</strong> ${tc.passed ? 'Passed \u2714' : 'Failed \u2718'}
                        <div class="tc-detail">Input: <code>${escapeHtml(tc.input)}</code></div>
                        <div class="tc-detail">Expected: <code>${escapeHtml(tc.expectedOutput)}</code></div>
                        <div class="tc-detail">Actual: <code>${escapeHtml(tc.actualOutput)}</code></div>
                    </div>
                `;
            });
            logHtml += `\n<div class="log-summary">Sample Tests Passed: <strong>${runResult.passedTests} / ${runResult.totalTests}</strong></div>`;
            consoleOutputEl.innerHTML = logHtml;
        } else {
            consoleOutputEl.innerHTML = `
                <div class="log-error">
                    <strong>\u2718 ${escapeHtml(runResult.status)}</strong>\n
                    <pre class="error-pre">${escapeHtml(runResult.errorMessage || 'Unknown error occurred during mock execution.')}</pre>
                </div>
            `;
        }
    }

    /**
     * Opens submission confirmation modal
     */
    function openSubmitModal() {
        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam || !modalSubmitConfirm) return;

        const currentQ = exam.questions[exam.currentIndex];
        if (modalProblemTitle) {
            modalProblemTitle.textContent = `Problem #${exam.currentIndex + 1}: ${currentQ.title} (${currentQ.difficulty.toUpperCase()})`;
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
     * Executes mock submit after modal confirmation
     */
    function confirmSubmit() {
        closeSubmitModal();

        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        const currentQ = exam.questions[exam.currentIndex];
        const code = codeEditorTextarea ? codeEditorTextarea.value : '';

        // Run mock submit engine
        const submitResult = window.MockCodeRunner ? window.MockCodeRunner.submit(code, currentQ) : { verdict: 'Compilation Error', totalTestCases: 5, testCasesPassed: 0 };

        // Save submission in AppState
        window.AppState.recordDsaSubmission(currentQ.id, submitResult);

        // Update Console Output
        if (consoleOutputEl) {
            if (submitResult.verdict === 'Accepted') {
                consoleOutputEl.innerHTML = `
                    <div class="submission-success-card">
                        <div class="sub-verdict-title text-success">\u2714 Accepted</div>
                        <div class="sub-meta">All Hidden Test Cases Passed: <strong>${submitResult.testCasesPassed} / ${submitResult.totalTestCases}</strong></div>
                        <div class="sub-meta">Execution Time: ${submitResult.executionTime} &bull; Memory: ${submitResult.memoryUsed}</div>
                    </div>
                `;
            } else {
                consoleOutputEl.innerHTML = `
                    <div class="submission-error-card">
                        <div class="sub-verdict-title text-error">\u2718 ${escapeHtml(submitResult.verdict)}</div>
                        <div class="sub-meta">Test Cases Passed: <strong>${submitResult.testCasesPassed} / ${submitResult.totalTestCases}</strong></div>
                        <pre class="error-pre">${escapeHtml(submitResult.errorMessage || 'Failed hidden verification test cases.')}</pre>
                    </div>
                `;
            }
        }

        // Update status pill
        renderStatusPills();
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
     * Finalizes DSA examination and calculates scores
     * @param {boolean} isAutoSubmit 
     */
    function finishExam(isAutoSubmit = false) {
        window.ExamTimer.stop();

        const exam = window.AppState ? window.AppState.getDsaExam() : null;
        if (!exam) return;

        // Auto-save active code
        handleCodeInput();

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
                if (sub.verdict === 'Accepted') {
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
                    totalTestCases: (q.sampleTestCases ? q.sampleTestCases.length : 0) + (q.hiddenTestCases ? q.hiddenTestCases.length : 0)
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

        // Save to AppState
        window.AppState.completeDsaExam(dsaResults);

        // Render summary on Page 6 placeholder
        renderFinalSummaryOnPage6(dsaResults);
        window.Navigation.navigateTo('page-result');
    }

    /**
     * Renders combined Aptitude & DSA summary on Page 6 placeholder
     */
    function renderFinalSummaryOnPage6(dsaResults) {
        const resultContainer = document.getElementById('result-container');
        if (!resultContainer) return;

        const aptResults = window.AppState ? window.AppState.getAptitudeResults() : null;
        const student = window.AppState ? window.AppState.getStudent() : { name: 'Student', rollNumber: 'N/A' };

        let detailsHtml = '';
        dsaResults.problemDetails.forEach(p => {
            const isAcc = (p.verdict === 'Accepted');
            detailsHtml += `
                <div class="dsa-res-item ${isAcc ? 'res-acc' : 'res-fail'}">
                    <div>
                        <strong>Problem ${p.problemIndex} (${p.difficulty.toUpperCase()}): ${escapeHtml(p.title)}</strong>
                        <div style="font-size: 0.8125rem; color: var(--color-text-muted);">Test Cases: ${p.testCasesPassed} / ${p.totalTestCases}</div>
                    </div>
                    <div class="verdict-tag ${isAcc ? 'tag-accepted' : 'tag-wrong'}">${escapeHtml(p.verdict)}</div>
                </div>
            `;
        });

        resultContainer.innerHTML = `
            <div class="final-results-wrapper">
                <div class="results-header-box">
                    <h2>Assessment Session Summary</h2>
                    <p>Candidate: <strong>${escapeHtml(student.name)}</strong> &bull; Roll Number: <strong>${escapeHtml(student.rollNumber)}</strong></p>
                </div>

                <div class="summary-two-col">
                    <!-- Aptitude Results Column -->
                    <div class="result-section-box">
                        <h3 class="res-sec-title">Aptitude Assessment</h3>
                        <div class="stat-card" style="margin-bottom: 1rem;">
                            <div class="stat-value">${aptResults ? aptResults.score : 0} / 20</div>
                            <div class="stat-label">Final Aptitude Score</div>
                        </div>
                        <div class="stat-mini-row"><span>Attempted:</span> <strong>${aptResults ? aptResults.attempted : 0} / 20</strong></div>
                        <div class="stat-mini-row"><span>Correct:</span> <strong style="color: var(--color-success);">${aptResults ? aptResults.correct : 0}</strong></div>
                        <div class="stat-mini-row"><span>Wrong:</span> <strong style="color: var(--color-error);">${aptResults ? aptResults.wrong : 0}</strong></div>
                        <div class="stat-mini-row"><span>Skipped:</span> <strong>${aptResults ? aptResults.skipped : 0}</strong></div>
                    </div>

                    <!-- DSA Results Column -->
                    <div class="result-section-box">
                        <h3 class="res-sec-title">DSA Coding Assessment</h3>
                        <div class="stat-card" style="margin-bottom: 1rem;">
                            <div class="stat-value">${dsaResults.accepted} / 2</div>
                            <div class="stat-label">Problems Accepted</div>
                        </div>
                        <div class="dsa-details-list">
                            ${detailsHtml}
                        </div>
                    </div>
                </div>

                <div class="info-box" style="margin-top: 1.5rem;">
                    <strong>Prototype Assessment Complete:</strong> In future phases, permanent candidate submissions and verified compiler logs will be sent to the Spring Boot REST API and stored in MySQL.
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
