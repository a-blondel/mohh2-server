-- password is always 'pass'
INSERT INTO ACCOUNT (NAME, PASS, MAIL, LOC, BORN, ZIP, GEND, SPAM, TOS, TICK, GAMECODE, VERS, SKU, SLUS, SDKVERS, BUILDDATE, CREATED_ON, UPDATED_ON) VALUES
('labeo', '$2a$10$6wND4Z9b3Nmz28Y5DAdGBen1LzQd0kyQI9rnRvb0qad3iQTxlypR6', 'labeo@ea.com', 'enCZ', '20070101', '12345', 'M', 'NN', 1, 'NDS/SVCLOC/TOKEN/90.45.123.44|dtrk+Wso4Notvo8JTXRPe2KU', '1380790872', 'WII/MOH08', 'MOHA', 'RM2X', '5.6.2.0', '"Sep  6 2007"', TIMESTAMP '2023-08-11 00:06:03.435237', TIMESTAMP '2023-08-11 00:06:44.596445'),
('jack', '$2a$10$6wND4Z9b3Nmz28Y5DAdGBen1LzQd0kyQI9rnRvb0qad3iQTxlypR6', 'jack@ea.com', 'frFR', '20070101', '12345', 'M', 'NN', 1, 'NDS/SVCLOC/TOKEN/90.45.123.44|dtrk+Wso4Notvo8JTXRPe2KU', '1380790872', 'WII/MOH08', 'MOHA', 'RM2X', '5.6.2.0', '"Sep  6 2007"', TIMESTAMP '2023-08-11 00:06:03.435237', TIMESTAMP '2023-08-11 00:06:44.596445'),
('bigguy', '$2a$10$6wND4Z9b3Nmz28Y5DAdGBen1LzQd0kyQI9rnRvb0qad3iQTxlypR6', 'bigguy@ea.com', 'enGB', '20070101', '12345', 'M', 'NN', 1, 'NDS/SVCLOC/TOKEN/90.45.123.44|dtrk+Wso4Notvo8JTXRPe2KU', '1380790872', 'WII/MOH08', 'MOHA', 'RM2X', '5.6.2.0', '"Sep  6 2007"', TIMESTAMP '2023-08-11 00:06:03.435237', TIMESTAMP '2023-08-11 00:06:44.596445'),
('gigi', '$2a$10$FORi89nE.KM8ULX0wc6p.uld0lg0iyIvNADkuxnO7hWmyGpZ4U2ve', 'gigi@ea.com', 'deDE', '20070101', '12345', 'M', 'YY', 1, 'NDS/SVCLOC/TOKEN/90.45.123.44|dtrk+Wso4Notvo8JTXRPe2KU', '1380790872', 'WII/MOH08', 'MOHA', 'RM2X', '5.6.2.0', '"Sep  6 2007"', TIMESTAMP '2023-08-12 01:26:11.260055', TIMESTAMP '2023-08-12 01:35:28.541726'),
('lchti', '$2a$10$FORi89nE.KM8ULX0wc6p.uld0lg0iyIvNADkuxnO7hWmyGpZ4U2ve', 'lchti@ea.com', 'frFR', '20070101', '12345', 'M', 'NN', 1, 'NDS/SVCLOC/TOKEN/90.45.123.44|dtrk+Wso4Notvo8JTXRPe2KU', '1380790872', 'WII/MOH08', 'MOHA', 'RM2X', '5.6.2.0', '"Sep  6 2007"', TIMESTAMP '2023-08-12 01:26:11.260055', TIMESTAMP '2023-08-12 01:35:28.541726');


INSERT INTO PERSONA (ACCOUNT_ID, PERS) VALUES
(1, 'Labeo'),
(2, 'jack041'),
(3, 'bigguyKid'),
(4, '"GIGI 88 OiOi"'),
(5, '"l ch ti"');

INSERT INTO PERSONA_STATS VALUES
(1, 186100, 39998, 120000, 337600, 662400, 3908751, 9120420, 1000000, 0, 0, 0, 0, 0, 0, 1000000, 0, 0, 0, 1000, 500, 0, 0, 558, 3497, 6503, 489, 2702, 7298, 286, 3245, 6755, 581, 5477, 4523, 18630, 3564, 6436, 1267, 7115, 2885, 558, 5909, 4091, 8316, 3329, 6671, 520, 3961, 6039, 1132, 2446, 7554, 363, 4459, 5541, 144404, 3373, 6627, 8945, 51),
(2, 187062, 59432, 125000, 224300, 775700, 2834272, 6613303, 0, 0, 0, 0, 1000000, 0, 1000000, 0, 0, 1200, 600, 0, 0, 0, 0, 7171, 2808, 7192, 25448, 2380, 7620, 12270, 2529, 7471, 2870, 3096, 6904, 7106, 3228, 6772, 5458, 6309, 3691, 35, 4128, 5872, 7476, 2778, 7222, 81850, 2071, 7929, 8423, 2591, 7409, 9505, 2555, 7445, 8128, 2975, 7025, 5377, 5945),
(3, 209921, 83263, 140000, 219400, 780600, 11525074, 4939317, 0, 0, 1000000, 0, 0, 0, 1000000, 0, 0, 1000, 500, 0, 0, 0, 0, 20815, 2479, 7521, 19942, 2062, 7938, 4111, 2286, 7714, 652, 2382, 7618, 6104, 3000, 7000, 5624, 5279, 4721, 5963, 5210, 4790, 24077, 2532, 7468, 25096, 1871, 8129, 5853, 2120, 7880, 1323, 2472, 7528, 27071, 3092, 6908, 53785, 9505),
(4, 164441, 50807, 115000, 248700, 751300, 2412337, 5628787, 0, 0, 0, 1000000, 0, 0, 0, 0, 1000000, 0, 0, 0, 0, 1500, 500, 2557, 3171, 6829, 7879, 2494, 7506, 954, 2696, 7304, 156, 3502, 6498, 1663, 3994, 6006, 2265, 6515, 3485, 3855, 7107, 2893, 11599, 2976, 7024, 39885, 2122, 7878, 1535, 2378, 7622, 1266, 4258, 5742, 68224, 3958, 6042, 21633, 970),
(5, 71794, 20674, 50000, 238400, 761600, 3228424, 1383610, 0, 0, 0, 1000000, 0, 0, 0, 0, 1000000, 0, 0, 0, 0, 1000, 250, 3159, 3007, 6993, 8603, 2101, 7899, 2393, 2248, 7752, 3261, 5650, 4350, 5290, 3187, 6813, 3605, 6008, 3992, 500, 5342, 4658, 3146, 2839, 7161, 6016, 1982, 8018, 2909, 2231, 7769, 9702, 3510, 6490, 5800, 3016, 6984, 9567, 7843);


INSERT INTO GAME (VERS, SLUS, USER_HOSTED, NAME, PARAMS, SYSFLAGS, PASS, MINSIZE, MAXSIZE, START_TIME, END_TIME) VALUES
('PSP/MOH07', 'ULUS10141', true, 'MOH', '8,b5,,1,-1,,,,1,e4a,e68,,114f0022', '262656', NULL, 1, 33, TIMESTAMP '2024-09-28 01:35:53.088243', '2024-09-28 01:35:53.088243'),
('WII/MOH08', 'RM2X', false, '"CTF Village"', '2,191,,,,,,,,-1,1,1,1,1,1,1,1,1,20,e49,e67,15f90,122d0022', '262656', NULL, 1, 33, TIMESTAMP '2023-08-12 01:35:53.088243', NULL),
('WII/MOH08', 'RM2X', false, '"TDM Port"', '7,65,,,a,,32,,,-1,1,1,1,1,1,1,1,,20,e49,e67,15f90,122d0022', '262656', NULL, 1, 33, TIMESTAMP '2023-08-12 01:35:53.088243', NULL),
('WII/MOH08', 'RM2X', false, '"DM Monastery"', '8,1f5,,,5,,14,,,-1,1,1,1,1,1,1,1,1,10,e49,e67,15f90,122d0022', '262656', NULL, 1, 17, TIMESTAMP '2023-08-12 01:35:53.088243', NULL),
('WII/MOH08', 'RM2X', false, '"DM City"', '8,c9,,,5,,14,,,-1,1,1,1,1,1,1,1,1,10,e49,e67,15f90,122d0022', '262656', NULL, 1, 17, TIMESTAMP '2023-08-12 01:35:53.088243', NULL),
('WII/MOH08', 'RM2X', false, '"DM Sewers"', '8,12d,,,5,,14,,,-1,1,1,1,1,1,1,1,1,10,e49,e67,15f90,122d0022', '262656', NULL, 1, 17, TIMESTAMP '2023-08-12 01:35:53.088243', NULL),
('WII/MOH08', 'RM2X', false, '"TDM Base"', '7,259,,,a,,32,,,-1,1,1,1,1,1,1,1,1,20,e49,e67,15f90,122d0022', '262656', NULL, 1, 33, TIMESTAMP '2023-08-12 01:35:53.088243', NULL);
