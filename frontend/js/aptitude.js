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
     * Starts the Aptitude Examination session by fetching authoritative questions from backend
     */
    async function startExam() {
        const assessmentId = window.AppState ? window.AppState.getAssessmentId() : null;
        if (!assessmentId) {
            alert('Assessment session has not been initialized. Please return to the first page.');
            if (window.Navigation) window.Navigation.navigateTo('page-student');
            return false;
        }

        // 1. Get selected topic IDs mapped to database numeric IDs
        let topicIds = [];
        if (window.TopicSelection && typeof window.TopicSelection.getBackendAptitudeTopicIds === 'function') {
            topicIds = window.TopicSelection.getBackendAptitudeTopicIds();
        } else {
            const selected = window.AppState ? window.AppState.getSelectedTopics().aptitude : [];
            topicIds = window.TopicData ? window.TopicData.mapAptitudeTopicIds(selected).slice(0, 3) : [1];
        }

        if (!topicIds || topicIds.length === 0) {
            topicIds = [1];
        }

        // 2. Fetch authoritative questions from backend POST /api/aptitude/exam
        const examResponse = await window.ApiClient.generateAptitudeExam({
            assessmentId: assessmentId,
            topicIds: topicIds
        });

        if (!examResponse || !examResponse.questions || examResponse.questions.length === 0) {
            throw new Error('No questions received from the assessment server.');
        }

        // 3. Normalize backend questions for UI
        // Backend provides: { id, topicId, questionText, optionA, optionB, optionC, optionD }
        const normalizedQuestions = examResponse.questions.map(q => ({
            id: q.id,
            topicId: q.topicId,
            questionText: q.questionText,
            question: q.questionText, // Backward compatibility
            options: [q.optionA, q.optionB, q.optionC, q.optionD],
            optionA: q.optionA,
            optionB: q.optionB,
            optionC: q.optionC,
            optionD: q.optionD
        }));

        // 4. Initialize exam state in AppState
        window.AppState.initAptitudeExam(normalizedQuestions);

        // 5. Switch to Aptitude page view and show active exam container
        const examContainer = document.getElementById('aptitude-exam-container');
        const summaryContainer = document.getElementById('aptitude-summary-container');
        if (examContainer) examContainer.classList.remove('hidden');
        if (summaryContainer) summaryContainer.classList.add('hidden');

        window.Navigation.navigateTo('page-aptitude');

        // 6. Render initial UI
        renderCurrentQuestion();
        renderPalette();

        // 7. Start 30-Minute Countdown Timer (1800 seconds)
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
     * Finalizes the Aptitude examination and submits answers to backend for authoritative scoring
     * @param {boolean} isAutoSubmit 
     */
    async function finishExam(isAutoSubmit = false) {
        // 1. Stop countdown timer
        window.ExamTimer.stop();

        // 2. Hide confirmation modal
        closeModal();

        const exam = window.AppState ? window.AppState.getAptitudeExam() : null;
        if (!exam) return;

        const assessmentId = window.AppState ? window.AppState.getAssessmentId() : null;
        if (!assessmentId) {
            alert('Assessment session has expired or is invalid. Please return to Page 1.');
            return;
        }

        // Format candidate answers for backend:
        // [{ questionId: number, selectedOption: "A"|"B"|"C"|"D"|null }]
        const optionLetters = ['A', 'B', 'C', 'D'];
        const answersPayload = [];
        exam.questions.forEach(q => {
            if (exam.answers.hasOwnProperty(q.id) && exam.answers[q.id] !== null && exam.answers[q.id] !== undefined) {
                const optIdx = exam.answers[q.id];
                answersPayload.push({
                    questionId: q.id,
                    selectedOption: optionLetters[optIdx] || null
                });
            } else {
                answersPayload.push({
                    questionId: q.id,
                    selectedOption: null
                });
            }
        });

        // Visual feedback
        if (btnNext) {
            btnNext.disabled = true;
            btnNext.textContent = 'Submitting...';
        }

        try {
            // Call backend POST /api/aptitude/submit
            const submitResponse = await window.ApiClient.submitAptitudeAnswers({
                assessmentId: assessmentId,
                answers: answersPayload
            });

            // Authoritative server-calculated results
            const resultsPayload = {
                totalQuestions: submitResponse.totalQuestions || 20,
                attempted: (submitResponse.correctAnswers || 0) + (submitResponse.wrongAnswers || 0),
                correct: submitResponse.correctAnswers || 0,
                wrong: submitResponse.wrongAnswers || 0,
                skipped: submitResponse.skippedAnswers !== undefined ? submitResponse.skippedAnswers : (20 - ((submitResponse.correctAnswers || 0) + (submitResponse.wrongAnswers || 0))),
                score: submitResponse.aptitudeScore !== undefined ? submitResponse.aptitudeScore : 0,
                isAutoSubmit: isAutoSubmit
            };

            // Save results to AppState
            window.AppState.completeAptitudeExam(resultsPayload);

            // Populate summary into Page 4 summary container
            displayAptitudeSummary(resultsPayload);
        } catch (error) {
            console.error('[Aptitude] Submission failed:', error);
            // On failure: remain on Page 4, show error, do not lose answers
            if (aptitudeErrorBanner) {
                aptitudeErrorBanner.textContent = error.message || 'Failed to submit aptitude examination. Your answers have been preserved. Please try again.';
                aptitudeErrorBanner.classList.remove('hidden');
                aptitudeErrorBanner.scrollIntoView({ behavior: 'smooth', block: 'center' });
            } else {
                alert(error.message || 'Failed to submit aptitude examination. Your answers have been preserved.');
            }
        } finally {
            if (btnNext) {
                btnNext.disabled = false;
                btnNext.textContent = 'Finish Aptitude \u2714';
            }
        }
    }

    /**
     * Renders Aptitude result summary on Page 4 transition container
     */
    function displayAptitudeSummary(results) {
        const examContainer = document.getElementById('aptitude-exam-container');
        const summaryContainer = document.getElementById('aptitude-summary-container');

        if (examContainer) {
            examContainer.classList.add('hidden');
        }

        if (summaryContainer) {
            summaryContainer.innerHTML = `
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
                        <strong>Next Stage:</strong> DSA Programming Assessment (2 Problems: 1 Easy [25 mins], 1 Medium [30 mins]).<br>
                        Make sure you are ready before clicking below to start the DSA timer.
                    </div>
                    <div style="margin-top: 1.5rem; text-align: right;">
                        <button type="button" id="btn-start-dsa-exam" class="btn btn-primary">
                            Start DSA Assessment &rarr;
                        </button>
                    </div>
                </div>
            `;
            summaryContainer.classList.remove('hidden');

            const btnStartDsa = document.getElementById('btn-start-dsa-exam');
            if (btnStartDsa) {
                btnStartDsa.addEventListener('click', async () => {
                    btnStartDsa.disabled = true;
                    btnStartDsa.textContent = 'Loading DSA Assessment...';
                    if (window.Dsa) {
                        try {
                            const success = await window.Dsa.startExam();
                            if (!success) {
                                btnStartDsa.disabled = false;
                                btnStartDsa.innerHTML = 'Start DSA Assessment &rarr;';
                            }
                        } catch (e) {
                            btnStartDsa.disabled = false;
                            btnStartDsa.innerHTML = 'Start DSA Assessment &rarr;';
                        }
                    }
                });
            }
        }
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
