/**
 * Topic Syllabus Data
 * 
 * Defines the finalized syllabus for Aptitude categories and DSA topics.
 * Each topic is identified by an immutable, unique topic ID string.
 */

(function () {
    const APTITUDE_CATEGORIES = [
        {
            categoryId: 'quant',
            categoryName: 'Quantitative Aptitude',
            topics: [
                { id: 'quant_number_system', name: 'Number System & HCF/LCM' },
                { id: 'quant_percentages', name: 'Percentages' },
                { id: 'quant_profit_loss', name: 'Profit, Loss & Discount' },
                { id: 'quant_interest', name: 'Simple Interest & Compound Interest' },
                { id: 'quant_ratio_proportion', name: 'Ratio, Proportion & Partnership' },
                { id: 'quant_averages_mixtures', name: 'Averages, Mixtures & Alligation' },
                { id: 'quant_time_work', name: 'Time & Work, Pipes & Cisterns' },
                { id: 'quant_time_speed_distance', name: 'Time, Speed & Distance, Boats & Streams, Trains' },
                { id: 'quant_permutation_probability', name: 'Permutation, Combination & Probability' },
                { id: 'quant_sequence_series', name: 'Sequence, Series & Progressions' },
                { id: 'quant_geometry_trig', name: 'Geometry, Mensuration & Trigonometry' },
                { id: 'quant_algebra_quadratics', name: 'Algebra & Quadratic Equations' }
            ]
        },
        {
            categoryId: 'logical',
            categoryName: 'Logical Reasoning',
            topics: [
                { id: 'logical_coding_decoding', name: 'Coding-Decoding' },
                { id: 'logical_blood_relations', name: 'Blood Relations' },
                { id: 'logical_direction_sense', name: 'Direction Sense Test' },
                { id: 'logical_seating_arrangements', name: 'Seating Arrangements (Linear, Circular, Square)' },
                { id: 'logical_puzzles', name: 'Puzzles' },
                { id: 'logical_syllogism', name: 'Syllogism' },
                { id: 'logical_clocks_calendars', name: 'Clocks & Calendars' },
                { id: 'logical_series', name: 'Number, Letter & Alphanumeric Series' },
                { id: 'logical_statement_reasoning', name: 'Statement and Assumptions, Conclusions, Arguments' },
                { id: 'logical_data_sufficiency', name: 'Data Sufficiency' },
                { id: 'logical_non_verbal', name: 'Non-Verbal Reasoning (Mirror Images, Paper Folding, Cubes and Dice)' }
            ]
        },
        {
            categoryId: 'verbal',
            categoryName: 'Verbal Ability',
            topics: [
                { id: 'verbal_reading_comprehension', name: 'Reading Comprehension' },
                { id: 'verbal_spotting_errors', name: 'Spotting Errors & Sentence Correction' },
                { id: 'verbal_para_jumbles', name: 'Para Jumbles & Sentence Completion' },
                { id: 'verbal_synonyms_antonyms', name: 'Synonyms, Antonyms & Analogies' },
                { id: 'verbal_idioms_phrases', name: 'Idioms, Phrases & One-Word Substitutes' },
                { id: 'verbal_fill_in_blanks', name: 'Fill in the Blanks' }
            ]
        },
        {
            categoryId: 'di',
            categoryName: 'Data Interpretation',
            topics: [
                { id: 'di_tables_charts', name: 'Tables & Charts' },
                { id: 'di_bar_pie_charts', name: 'Bar Graphs & Pie Charts' },
                { id: 'di_line_caselets', name: 'Line Graphs & Caselets' }
            ]
        }
    ];

    const DSA_TOPICS = [
        { id: 'dsa_arrays', name: 'Arrays' },
        { id: 'dsa_strings', name: 'Strings' },
        { id: 'dsa_linked_lists', name: 'Linked Lists' },
        { id: 'dsa_stack_queue', name: 'Stack & Queue' },
        { id: 'dsa_binary_trees', name: 'Binary Trees' },
        { id: 'dsa_bst', name: 'Binary Search Trees (BST)' },
        { id: 'dsa_recursion_backtracking', name: 'Recursion & Backtracking' },
        { id: 'dsa_dynamic_programming', name: 'Dynamic Programming (DP)' },
        { id: 'dsa_graphs', name: 'Graphs' },
        { id: 'dsa_heap_priority_queue', name: 'Heap / Priority Queue' }
    ];

    // Helper to get all Aptitude topic IDs as a flat array
    function getAllAptitudeTopicIds() {
        const ids = [];
        APTITUDE_CATEGORIES.forEach(category => {
            category.topics.forEach(topic => {
                ids.push(topic.id);
            });
        });
        return ids;
    }

    // Helper to get all DSA topic IDs as a flat array
    function getAllDsaTopicIds() {
        return DSA_TOPICS.map(topic => topic.id);
    }

    // Database ID Mappings (Aligning with seed.sql in MySQL)
    const APTITUDE_CODE_TO_ID = {
        'quant_number_system': 1,
        'quant_percentages': 2,
        'quant_profit_loss': 3,
        'quant_interest': 4,
        'quant_ratio_proportion': 5,
        'quant_averages_mixtures': 6,
        'quant_time_work': 7,
        'quant_time_speed_distance': 8,
        'quant_permutation_probability': 9,
        'quant_sequence_series': 10,
        'quant_geometry_trig': 11,
        'quant_algebra_quadratics': 12,
        'logical_coding_decoding': 13,
        'logical_blood_relations': 14,
        'logical_direction_sense': 15,
        'logical_seating_arrangements': 16,
        'logical_puzzles': 17,
        'logical_syllogism': 18,
        'logical_clocks_calendars': 19,
        'logical_series': 20,
        'logical_statement_reasoning': 21,
        'logical_data_sufficiency': 22,
        'logical_non_verbal': 23,
        'verbal_reading_comprehension': 24,
        'verbal_spotting_errors': 25,
        'verbal_para_jumbles': 26,
        'verbal_synonyms_antonyms': 27,
        'verbal_idioms_phrases': 28,
        'verbal_fill_in_blanks': 29,
        'di_tables_charts': 30,
        'di_bar_pie_charts': 31,
        'di_line_caselets': 32
    };

    const DSA_CODE_TO_ID = {
        'dsa_arrays': 1,
        'dsa_strings': 2,
        'dsa_linked_lists': 3,
        'dsa_stack_queue': 4,
        'dsa_binary_trees': 5,
        'dsa_bst': 6,
        'dsa_recursion_backtracking': 7,
        'dsa_dynamic_programming': 8,
        'dsa_graphs': 9,
        'dsa_heap_priority_queue': 10
    };

    function mapAptitudeTopicIds(topicCodes) {
        if (!Array.isArray(topicCodes)) return [];
        return topicCodes.map(code => {
            if (typeof code === 'number') return code;
            return APTITUDE_CODE_TO_ID[code] || parseInt(code, 10) || 1;
        });
    }

    function mapDsaTopicIds(topicCodes) {
        if (!Array.isArray(topicCodes)) return [];
        return topicCodes.map(code => {
            if (typeof code === 'number') return code;
            return DSA_CODE_TO_ID[code] || parseInt(code, 10) || 1;
        });
    }

    const TopicData = {
        APTITUDE_CATEGORIES: APTITUDE_CATEGORIES,
        DSA_TOPICS: DSA_TOPICS,
        getAllAptitudeTopicIds: getAllAptitudeTopicIds,
        getAllDsaTopicIds: getAllDsaTopicIds,
        TOTAL_APTITUDE_TOPICS: getAllAptitudeTopicIds().length,
        TOTAL_DSA_TOPICS: DSA_TOPICS.length,
        APTITUDE_CODE_TO_ID: APTITUDE_CODE_TO_ID,
        DSA_CODE_TO_ID: DSA_CODE_TO_ID,
        mapAptitudeTopicIds: mapAptitudeTopicIds,
        mapDsaTopicIds: mapDsaTopicIds
    };

    // Expose to window
    window.TopicData = TopicData;
})();
