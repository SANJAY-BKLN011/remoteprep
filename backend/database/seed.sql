-- =============================================================================
-- RemotePrep Assessment Platform - MySQL Seed Data
-- Database: remoteprep
-- Seeds: 32 Aptitude Topics & 10 DSA Topics
-- =============================================================================

USE remoteprep;

-- =============================================================================
-- 1. SEED APTITUDE TOPICS (32 Topics across 4 Categories)
-- =============================================================================

-- Quantitative Aptitude (12 topics)
INSERT INTO aptitude_topics (topic_code, category, topic_name) VALUES
('quant_number_system', 'Quantitative Aptitude', 'Number System & HCF/LCM'),
('quant_percentages', 'Quantitative Aptitude', 'Percentages'),
('quant_profit_loss', 'Quantitative Aptitude', 'Profit, Loss and Discount'),
('quant_interest', 'Quantitative Aptitude', 'Simple Interest and Compound Interest'),
('quant_ratio_proportion', 'Quantitative Aptitude', 'Ratio, Proportion and Partnership'),
('quant_averages_mixtures', 'Quantitative Aptitude', 'Averages, Mixtures and Alligation'),
('quant_time_work', 'Quantitative Aptitude', 'Time and Work, Pipes and Cisterns'),
('quant_time_speed_distance', 'Quantitative Aptitude', 'Time, Speed and Distance, Boats and Streams, Trains'),
('quant_permutation_probability', 'Quantitative Aptitude', 'Permutation, Combination and Probability'),
('quant_sequence_series', 'Quantitative Aptitude', 'Sequence, Series and Progressions'),
('quant_geometry_trig', 'Quantitative Aptitude', 'Geometry, Mensuration and Trigonometry'),
('quant_algebra_quadratics', 'Quantitative Aptitude', 'Algebra and Quadratic Equations');

-- Logical Reasoning (11 topics)
INSERT INTO aptitude_topics (topic_code, category, topic_name) VALUES
('logical_coding_decoding', 'Logical Reasoning', 'Coding-Decoding'),
('logical_blood_relations', 'Logical Reasoning', 'Blood Relations'),
('logical_direction_sense', 'Logical Reasoning', 'Direction Sense Test'),
('logical_seating_arrangements', 'Logical Reasoning', 'Seating Arrangements (Linear, Circular, Square)'),
('logical_puzzles', 'Logical Reasoning', 'Puzzles'),
('logical_syllogism', 'Logical Reasoning', 'Syllogism'),
('logical_clocks_calendars', 'Logical Reasoning', 'Clocks and Calendars'),
('logical_series', 'Logical Reasoning', 'Number, Letter and Alphanumeric Series'),
('logical_statement_reasoning', 'Logical Reasoning', 'Statement and Assumptions, Conclusions, Arguments'),
('logical_data_sufficiency', 'Logical Reasoning', 'Data Sufficiency'),
('logical_non_verbal', 'Logical Reasoning', 'Non-Verbal Reasoning (Mirror images, Paper folding, Cubes and dice)');

-- Verbal Ability (6 topics)
INSERT INTO aptitude_topics (topic_code, category, topic_name) VALUES
('verbal_reading_comprehension', 'Verbal Ability', 'Reading Comprehension'),
('verbal_spotting_errors', 'Verbal Ability', 'Spotting Errors and Sentence Correction'),
('verbal_para_jumbles', 'Verbal Ability', 'Para Jumbles and Sentence Completion'),
('verbal_synonyms_antonyms', 'Verbal Ability', 'Synonyms, Antonyms and Analogies'),
('verbal_idioms_phrases', 'Verbal Ability', 'Idioms, Phrases and One-Word Substitutes'),
('verbal_fill_in_blanks', 'Verbal Ability', 'Fill in the Blanks');

-- Data Interpretation (3 topics)
INSERT INTO aptitude_topics (topic_code, category, topic_name) VALUES
('di_tables_charts', 'Data Interpretation', 'Tables and Charts'),
('di_bar_pie_charts', 'Data Interpretation', 'Bar Graphs and Pie Charts'),
('di_line_caselets', 'Data Interpretation', 'Line Graphs and Caselets');

-- =============================================================================
-- 2. SEED DSA TOPICS (10 Topics)
-- =============================================================================

INSERT INTO dsa_topics (topic_code, topic_name) VALUES
('dsa_arrays', 'Arrays'),
('dsa_strings', 'Strings'),
('dsa_linked_lists', 'Linked Lists'),
('dsa_stack_queue', 'Stack & Queue'),
('dsa_binary_trees', 'Binary Trees'),
('dsa_bst', 'Binary Search Trees (BST)'),
('dsa_recursion_backtracking', 'Recursion & Backtracking'),
('dsa_dynamic_programming', 'Dynamic Programming (DP)'),
('dsa_graphs', 'Graphs'),
('dsa_heap_priority_queue', 'Heap / Priority Queue');
