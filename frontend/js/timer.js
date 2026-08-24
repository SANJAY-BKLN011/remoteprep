/**
 * Examination Timer Module
 * 
 * Manages reliable countdown timers, visual threshold alerts,
 * and automatic completion callbacks without multiple interval duplicates.
 */

(function () {
    let timerInterval = null;
    let secondsRemaining = 0;
    let onTickCallback = null;
    let onExpireCallback = null;

    const ExamTimer = {
        /**
         * Starts a countdown timer
         * @param {number} totalSeconds - Duration in seconds (e.g., 1800 for 30 minutes)
         * @param {Function} onTick - Callback executed each second: (formattedTime, remainingSeconds, warningLevel)
         * @param {Function} onExpire - Callback executed when timer reaches 00:00
         */
        start: function (totalSeconds, onTick, onExpire) {
            // Clear any existing active interval to prevent duplicate timers
            this.stop();

            secondsRemaining = Math.max(0, totalSeconds);
            onTickCallback = onTick;
            onExpireCallback = onExpire;

            // Immediate initial tick
            this.tick();

            // Set single interval running every 1000ms
            timerInterval = setInterval(() => {
                secondsRemaining--;

                if (secondsRemaining <= 0) {
                    secondsRemaining = 0;
                    this.tick();
                    this.stop();

                    if (typeof onExpireCallback === 'function') {
                        onExpireCallback();
                    }
                } else {
                    this.tick();
                }
            }, 1000);
        },

        /**
         * Executes tick logic, calculates warning levels, and formats MM:SS
         */
        tick: function () {
            const formatted = this.formatTime(secondsRemaining);
            const warningLevel = this.getWarningLevel(secondsRemaining);

            if (typeof onTickCallback === 'function') {
                onTickCallback(formatted, secondsRemaining, warningLevel);
            }
        },

        /**
         * Formats seconds into MM:SS
         * @param {number} totalSec 
         * @returns {string} e.g. "30:00", "04:12"
         */
        formatTime: function (totalSec) {
            const minutes = Math.floor(totalSec / 60);
            const seconds = totalSec % 60;
            return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        },

        /**
         * Determines visual warning tier:
         * - 'normal'   : > 5 minutes (> 300s)
         * - 'warning'  : <= 5 minutes (<= 300s)
         * - 'critical' : <= 1 minute (<= 60s)
         */
        getWarningLevel: function (sec) {
            if (sec <= 60) return 'critical';
            if (sec <= 300) return 'warning';
            return 'normal';
        },

        /**
         * Stops and cleans up the active timer
         */
        stop: function () {
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
        },

        /**
         * Gets current seconds remaining
         */
        getRemainingSeconds: function () {
            return secondsRemaining;
        }
    };

    // Expose ExamTimer to global window object
    window.ExamTimer = ExamTimer;
})();
