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

    const TopicData = {
        APTITUDE_CATEGORIES: APTITUDE_CATEGORIES,
        DSA_TOPICS: DSA_TOPICS,
        getAllAptitudeTopicIds: getAllAptitudeTopicIds,
        getAllDsaTopicIds: getAllDsaTopicIds,
        TOTAL_APTITUDE_TOPICS: getAllAptitudeTopicIds().length,
        TOTAL_DSA_TOPICS: DSA_TOPICS.length
    };

    // Expose to window
    window.TopicData = TopicData;
})();
