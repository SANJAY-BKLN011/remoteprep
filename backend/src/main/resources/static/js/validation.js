/**
 * Validation Utility Functions
 * 
 * Contains pure validation functions for forms, inputs, and business rules.
 */

(function () {
    const Validation = {
        /**
         * Validates student identification details for Page 1
         * @param {string} name 
         * @param {string} rollNumber 
         * @returns {Object} { isValid: boolean, errors: { name?: string, rollNumber?: string } }
         */
        validateStudentDetails: function (name, rollNumber) {
            const errors = {};
            const cleanName = (name || '').trim();
            const cleanRoll = (rollNumber || '').trim();

            if (!cleanName) {
                errors.name = 'Student Name is required.';
            } else if (cleanName.length < 2) {
                errors.name = 'Student Name must be at least 2 characters.';
            }

            if (!cleanRoll) {
                errors.rollNumber = 'Roll / Registration Number is required.';
            } else if (cleanRoll.length < 3) {
                errors.rollNumber = 'Roll Number must be at least 3 characters.';
            }

            return {
                isValid: Object.keys(errors).length === 0,
                errors: errors
            };
        },

        /**
         * Validates topic selection for Page 2
         * Requires at least 1 Aptitude topic and at least 1 DSA topic
         * @param {Array<string>} aptitudeTopics 
         * @param {Array<string>} dsaTopics 
         * @returns {Object} { isValid: boolean, message: string }
         */
        validateTopicSelection: function (aptitudeTopics, dsaTopics) {
            const hasAptitude = Array.isArray(aptitudeTopics) && aptitudeTopics.length > 0;
            const hasDsa = Array.isArray(dsaTopics) && dsaTopics.length > 0;

            if (!hasAptitude && !hasDsa) {
                return {
                    isValid: false,
                    message: 'Please select at least one Aptitude topic and at least one DSA topic.'
                };
            }

            if (!hasAptitude) {
                return {
                    isValid: false,
                    message: 'Please select at least one Aptitude topic.'
                };
            }

            if (!hasDsa) {
                return {
                    isValid: false,
                    message: 'Please select at least one DSA topic.'
                };
            }

            return {
                isValid: true,
                message: ''
            };
        }
    };

    // Expose to global window object
    window.Validation = Validation;
})();
