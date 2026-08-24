/**
 * Aptitude Examination Controller (Page 4)
 * 
 * Handles:
 * - Mathematical question distribution across selected topics
 * - Fisher-Yates randomization
 * - Question rendering and single-choice answer selection
 * - Question navigation (Previous, Skip, Next, Direct Palette Jump)
 * - Visual status tracking (Current, Answered, Unanswered, Skipped)
 * - Timer synchronization & warning states
 * - Submission confirmation modal & final scoring calculation
 */

(function () {
    // DOM Elements Cache
    let questionNumberHeader;
    let questionTextEl;
    let optionsContainer;
    let btnPrev;
    let btnSkip;
    let btnNext;
    let paletteContainer;
    let timerDisplay;
    let timerBadge;
    let answeredCountBadge;
    let modalFinishConfirm;
    let modalStatAnswered;
    let modalStatUnanswered;
    let modalStatSkipped;
    let btnModalCancel;
    let btnModalConfirm;
    let aptitudeErrorBanner;

    /**
     * Initializes the Aptitude module and caches DOM references
     */
    function init() {
        cacheDOMElements();
        bindEvents();
    }

    /**
     * Cache DOM references
     */
    function cacheDOMElements() {
        questionNumberHeader = document.getElementById('apt-question-header');
        questionTextEl = document.getElementById('apt-question-text');
        optionsContainer = document.getElementById('apt-options-container');
        btnPrev = document.getElementById('btn-apt-prev');
        btnSkip = document.getElementById('btn-apt-skip');
        btnNext = document.getElementById('btn-apt-next');
        paletteContainer = document.getElementById('apt-palette-grid');
        timerDisplay = document.getElementById('apt-timer-display');
        timerBadge = document.getElementById('apt-timer-badge');
        answeredCountBadge = document.getElementById('apt-answered-badge');
        modalFinishConfirm = document.getElementById('modal-finish-confirm');
        modalStatAnswered = document.getElementById('modal-stat-answered');
        modalStatUnanswered = document.getElementById('modal-stat-unanswered');
        modalStatSkipped = document.getElementById('modal-stat-skipped');
        btnModalCancel = document.getElementById('btn-modal-cancel');
        btnModalConfirm = document.getElementById('btn-modal-confirm');
        aptitudeErrorBanner = document.getElementById('apt-error-banner');
    }

    /**
     * Bind UI event listeners
     */
    function bindEvents() {
        if (btnPrev) btnPrev.addEventListener('click', handlePrevious);
        if (btnSkip) btnSkip.addEventListener('click', handleSkip);
        if (btnNext) btnNext.addEventListener('click', handleNextOrFinish);

        if (paletteContainer) {
            paletteContainer.addEventListener('click', handlePaletteClick);
        }

        if (btnModalCancel) {
            btnModalCancel.addEventListener('click', closeModal);
        }

        if (btnModalConfirm) {
            btnModalConfirm.addEventListener('click', () => finishExam(false));
        }
    }

    /**
     * Fisher-Yates (Knuth) Shuffle Algorithm
     * Produces an unbiased random permutation without modifying the original array
     * @param {Array} array 
     * @returns {Array} Shuffled copy
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
     * Generates a 20-question examination distributed evenly across selected topics
     * @param {Array<string>} selectedTopicIds 
     * @returns {Object} { success: boolean, questions?: Array, error?: string }
     */
    function generateAptitudeExam(selectedTopicIds) {
        if (!Array.isArray(selectedTopicIds) || selectedTopicIds.length === 0) {
            return {
                success: false,
                error: 'No Aptitude topics selected. Please select at least one topic.'
            };
        }

        const totalRequired = 20;
        const numTopics = selectedTopicIds.length;
        const base = Math.floor(totalRequired / numTopics);
        const remainder = totalRequired % numTopics;

        const selectedQuestions = [];

        // Distribute questions evenly
        for (let i = 0; i < numTopics; i++) {
            const topicId = selectedTopicIds[i];
            const requiredCount = base + (i < remainder ? 1 : 0);

            if (requiredCount <= 0) continue;

            const availableQuestions = window.MockQuestions ? window.MockQuestions.getQuestionsByTopic(topicId) : [];

            if (!availableQuestions || availableQuestions.length < requiredCount) {
                return {
                    success: false,
                    error: `Unable to generate the examination because there are not enough questions available for selected topic: "${topicId}".`
                };
            }

            // Shuffle available questions for this topic and pick required amount
            const shuffledTopicQuestions = shuffleArray(availableQuestions);
            const picked = shuffledTopicQuestions.slice(0, requiredCount);

            selectedQuestions.push(...picked);
        }

        // Final verification: exactly 20 questions
        if (selectedQuestions.length !== totalRequired) {
            return {
                success: false,
                error: `Expected ${totalRequired} questions but generated ${selectedQuestions.length}.`
            };
        }

        // Final shuffle of the combined 20 questions
        const finalShuffledQuestions = shuffleArray(selectedQuestions);

        return {
            success: true,
            questions: finalShuffledQuestions
        };
    }

    /**
     * Starts the Aptitude Examination session
     */
    function startExam() {
        const selectedTopics = window.AppState ? window.AppState.getSelectedTopics().aptitude : [];

        // 1. Generate 20-question examination
        const generationResult = generateAptitudeExam(selectedTopics);

        if (!generationResult.success) {
            alert(generationResult.error);
            return false;
        }

        // 2. Initialize exam state in AppState
        window.AppState.initAptitudeExam(generationResult.questions);

        // 3. Switch to Aptitude page view
        window.Navigation.navigateTo('page-aptitude');

        // 4. Render initial UI
        renderCurrentQuestion();
        renderPalette();

        // 5. Start 30-Minute Countdown Timer (1800 seconds)
        window.ExamTimer.start(
            1800,
            handleTimerTick,
            handleTimerExpire
        );

        return true;
    }

    /**
     * Timer Tick Handler: updates UI and records remaining time in AppState
     */
    function handleTimerTick(formattedTime, remainingSeconds, warningLevel) {
        if (timerDisplay) {
            timerDisplay.textContent = formattedTime;
        }

        if (timerBadge) {
            timerBadge.className = `timer-badge timer-${warningLevel}`;
        }

        if (window.AppState) {
            window.AppState.setAptitudeTimeRemaining(remainingSeconds);
        }
    }

    /**
     * Timer Expire Handler: automatic submission at 00:00
     */
    function handleTimerExpire() {
        closeModal();
        alert('Time is up! Your Aptitude examination will now be automatically submitted.');
        finishExam(true);
    }

    /**
     * Renders the currently active question and its 4 options
     */
    function renderCurrentQuestion() {
        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!exam || !exam.questions || exam.questions.length === 0) return;

        const currentIndex = exam.currentIndex;
        const total = exam.questions.length;
        const q = exam.questions[currentIndex];
        const selectedAnswer = exam.answers[q.id];

        // Question header & text
        if (questionNumberHeader) {
            questionNumberHeader.textContent = `Question ${currentIndex + 1} of ${total}`;
        }

        if (questionTextEl) {
            questionTextEl.textContent = q.question;
        }

        // 4 MCQ Options
        if (optionsContainer) {
            const optionLetters = ['A', 'B', 'C', 'D'];
            let html = '';

            q.options.forEach((optText, optIdx) => {
                const isSelected = (selectedAnswer === optIdx);
                html += `
                    <div class="option-item ${isSelected ? 'selected' : ''}" data-option-index="${optIdx}">
                        <div class="option-indicator">${optionLetters[optIdx]}</div>
                        <div class="option-text">${escapeHtml(optText)}</div>
                    </div>
                `;
            });

            optionsContainer.innerHTML = html;

            // Bind option click listeners
            const optionCards = optionsContainer.querySelectorAll('.option-item');
            optionCards.forEach(card => {
                card.addEventListener('click', () => {
                    const optIndex = parseInt(card.getAttribute('data-option-index'), 10);
                    selectOption(q.id, optIndex);
                });
            });
        }

        // Update button states
        if (btnPrev) {
            btnPrev.disabled = (currentIndex === 0);
        }

        if (btnNext) {
            if (currentIndex === total - 1) {
                btnNext.textContent = 'Finish Aptitude \u2714';
                btnNext.classList.add('btn-finish-state');
            } else {
                btnNext.textContent = 'Next \u2192';
                btnNext.classList.remove('btn-finish-state');
            }
        }

        // Update answered count badge
        if (answeredCountBadge) {
            const answeredCount = Object.keys(exam.answers).length;
            answeredCountBadge.textContent = `Answered: ${answeredCount} / ${total}`;
        }

        // Update palette visual indicators
        renderPalette();
    }

    /**
     * Handles candidate selecting an option
     */
    function selectOption(questionId, optionIndex) {
        if (!window.AppState) return;

        window.AppState.setAptitudeAnswer(questionId, optionIndex);
        renderCurrentQuestion();
    }

    /**
     * Moves to Previous Question
     */
    function handlePrevious() {
        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!exam || exam.currentIndex <= 0) return;

        window.AppState.setAptitudeCurrentIndex(exam.currentIndex - 1);
        renderCurrentQuestion();
    }

    /**
     * Moves to Next Question or opens Finish Modal on last question
     */
    function handleNextOrFinish() {
        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!exam) return;

        if (exam.currentIndex >= exam.questions.length - 1) {
            openFinishModal();
        } else {
            window.AppState.setAptitudeCurrentIndex(exam.currentIndex + 1);
            renderCurrentQuestion();
        }
    }

    /**
     * Handles Skip action
     */
    function handleSkip() {
        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!exam) return;

        const currentQuestion = exam.questions[exam.currentIndex];
        window.AppState.skipAptitudeQuestion(currentQuestion.id);

        if (exam.currentIndex < exam.questions.length - 1) {
            window.AppState.setAptitudeCurrentIndex(exam.currentIndex + 1);
        }

        renderCurrentQuestion();
    }

    /**
     * Renders the 20-question navigation grid palette
     */
    function renderPalette() {
        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!paletteContainer || !exam || !exam.questions) return;

        let html = '';
        exam.questions.forEach((q, idx) => {
            const isCurrent = (idx === exam.currentIndex);
            const isAnswered = exam.answers.hasOwnProperty(q.id);
            const isSkipped = exam.skipped.includes(q.id);

            let stateClass = 'palette-unanswered';
            if (isAnswered) {
                stateClass = 'palette-answered';
            } else if (isSkipped) {
                stateClass = 'palette-skipped';
            }

            if (isCurrent) {
                stateClass += ' palette-current';
            }

            html += `
                <button type="button" class="palette-item ${stateClass}" data-index="${idx}" title="Go to Question ${idx + 1}">
                    ${idx + 1}
                </button>
            `;
        });

        paletteContainer.innerHTML = html;
    }

    /**
     * Direct jump to a question when clicking its palette number
     */
    function handlePaletteClick(e) {
        const target = e.target.closest('.palette-item');
        if (!target) return;

        const targetIndex = parseInt(target.getAttribute('data-index'), 10);
        if (!isNaN(targetIndex)) {
            window.AppState.setAptitudeCurrentIndex(targetIndex);
            renderCurrentQuestion();
        }
    }

    /**
     * Opens the Finish Aptitude confirmation modal
     */
    function openFinishModal() {
        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!exam || !modalFinishConfirm) return;

        const total = exam.questions.length;
        const answered = Object.keys(exam.answers).length;
        const skipped = exam.skipped.length;
        const unanswered = total - answered;

        if (modalStatAnswered) modalStatAnswered.textContent = answered;
        if (modalStatUnanswered) modalStatUnanswered.textContent = unanswered;
        if (modalStatSkipped) modalStatSkipped.textContent = skipped;

        modalFinishConfirm.classList.remove('hidden');
    }

    /**
     * Closes the confirmation modal
     */
    function closeModal() {
        if (modalFinishConfirm) {
            modalFinishConfirm.classList.add('hidden');
        }
    }

    /**
     * Finalizes the Aptitude examination and calculates score
     * @param {boolean} isAutoSubmit 
     */
    function finishExam(isAutoSubmit = false) {
        // 1. Stop countdown timer
        window.ExamTimer.stop();

        // 2. Hide confirmation modal
        closeModal();

        // 3. Compute score and statistics
        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!exam) return;

        const total = exam.questions.length;
        const answers = exam.answers;
        let correctCount = 0;

        exam.questions.forEach(q => {
            if (answers.hasOwnProperty(q.id) && answers[q.id] === q.correctAnswer) {
                correctCount++;
            }
        });

        const attemptedCount = Object.keys(answers).length;
        const wrongCount = attemptedCount - correctCount;
        const skippedCount = total - attemptedCount;
        const finalScore = correctCount; // 1 mark each, no negative marking

        const resultsPayload = {
            totalQuestions: total,
            attempted: attemptedCount,
            correct: correctCount,
            wrong: wrongCount,
            skipped: skippedCount,
            score: finalScore,
            isAutoSubmit: isAutoSubmit
        };

        // 4. Save results to AppState
        window.AppState.completeAptitudeExam(resultsPayload);

        // 5. Populate summary into Page 5 placeholder and navigate
        displayAptitudeSummaryOnPage5(resultsPayload);
        window.Navigation.navigateTo('page-dsa');
    }

    /**
     * Renders Aptitude result summary on Page 5 placeholder
     */
    function displayAptitudeSummaryOnPage5(results) {
        const dsaContainer = document.getElementById('dsa-container');
        if (!dsaContainer) return;

        dsaContainer.innerHTML = `
            <div class="apt-results-summary-card">
                <div class="summary-card-header">
                    <h3>\u2714 Aptitude Section Completed</h3>
                    <p>Your performance in the Aptitude Examination has been recorded.</p>
                </div>
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-value">${results.score} / ${results.totalQuestions}</div>
                        <div class="stat-label">Final Aptitude Score</div>
                    </div>
                    <div class="stat-card stat-correct">
                        <div class="stat-value">${results.correct}</div>
                        <div class="stat-label">Correct Answers</div>
                    </div>
                    <div class="stat-card stat-wrong">
                        <div class="stat-value">${results.wrong}</div>
                        <div class="stat-label">Wrong Answers</div>
                    </div>
                    <div class="stat-card stat-skipped">
                        <div class="stat-value">${results.skipped}</div>
                        <div class="stat-label">Skipped / Unanswered</div>
                    </div>
                </div>
                <div class="info-box" style="margin-top: 1.5rem;">
                    <strong>Next Stage:</strong> DSA Coding Assessment (1 Easy Problem &bull; 1 Medium Problem).<br>
                    <em>Note: The DSA Coding interface will be implemented in the next phase.</em>
                </div>
            </div>
        `;
    }

    /**
     * HTML entity escaping
     */
    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // Expose Aptitude controller
    window.Aptitude = {
        init: init,
        startExam: startExam,
        generateAptitudeExam: generateAptitudeExam,
        shuffleArray: shuffleArray,
        finishExam: finishExam
    };
})();
