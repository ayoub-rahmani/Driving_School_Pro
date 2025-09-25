-- Java-based MySQL backup
-- Generated on: Mon Apr 07 12:53:57 CET 2025
-- Database: driving_school

-- Create database
CREATE DATABASE IF NOT EXISTS `driving_school`;
USE `driving_school`;

-- Table structure for table `audit_logs`
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `action` varchar(50) NOT NULL,
  `entity_type` varchar(50) NOT NULL,
  `entity_id` bigint DEFAULT NULL,
  `details` text,
  `user_id` int DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `audit_logs`
INSERT INTO `audit_logs` (`id`, `action`, `entity_type`, `entity_id`, `details`, `user_id`, `username`, `timestamp`) VALUES
(1, 'UPDATE', 'CANDIDAT', 20, 'UPDATE candidat Jebali Khaled', 1, 'sogut', '2025-03-26 05:45:12.0'),
(2, 'CREATE', 'USER', 2, 'CREATE utilisateur tarek', 1, 'sogut', '2025-03-26 06:09:37.0'),
(3, 'UPDATE', 'USER', 2, 'UPDATE utilisateur tarek', 1, 'sogut', '2025-03-26 06:14:52.0'),
(4, 'UPDATE', 'CANDIDAT', 19, 'UPDATE candidat Bouazzi Sana', 1, 'sogut', '2025-03-26 06:16:44.0'),
(5, 'UPDATE', 'CANDIDAT', 18, 'UPDATE candidat Trabelsi Mohamed', 1, 'sogut', '2025-03-26 06:18:55.0'),
(6, 'UPDATE', 'USER', 2, 'UPDATE utilisateur tarek', 1, 'sogut', '2025-03-26 06:21:17.0'),
(7, 'UPDATE', 'USER', 2, 'UPDATE utilisateur tarek', 1, 'sogut', '2025-03-26 06:21:44.0'),
(8, 'UPDATE', 'CANDIDAT', 20, 'UPDATE candidat Jebalii Khaled', 1, 'sogut', '2025-03-26 06:32:21.0'),
(9, 'DELETE', 'USER', 2, 'Suppression de l''utilisateur tarek', 1, 'sogut', '2025-03-26 06:34:02.0'),
(10, 'CREATE', 'USER', 3, 'CREATE utilisateur tarek', 1, 'sogut', '2025-03-26 06:34:49.0'),
(13, 'UPDATE', 'USER', 3, 'UPDATE utilisateur tarek', 1, 'sogut', '2025-03-26 07:45:19.0'),
(14, 'DELETE', 'AUDIT_LOG', 12, 'Suppression du log #12', 1, 'sogut', '2025-03-26 08:21:42.0'),
(15, 'DELETE', 'AUDIT_LOG', 11, 'Suppression du log #11', 1, 'sogut', '2025-03-26 08:21:49.0'),
(16, 'UPDATE', 'USER', 3, 'UPDATE utilisateur tarek', 1, 'sogut', '2025-03-26 08:22:28.0'),
(17, 'UPDATE', 'USER', 3, 'UPDATE utilisateur tarek', 1, 'sogut', '2025-03-26 08:22:38.0'),
(18, 'DELETE', 'USER', 3, 'Suppression de l''utilisateur tarek', 1, 'sogut', '2025-03-26 08:24:33.0'),
(19, 'CREATE', 'USER', 4, 'CREATE utilisateur lotfi', 1, 'sogut', '2025-03-26 08:25:18.0'),
(20, 'DELETE', 'CANDIDAT', 30, 'Suppression du candidat Zouari Karim', 1, 'sogut', '2025-03-26 08:28:01.0'),
(21, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-43-28.sql', 1, 'sogut', '2025-03-26 09:57:47.0'),
(22, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-46-31.sql', 1, 'sogut', '2025-03-26 09:57:51.0'),
(23, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-49-19.sql', 1, 'sogut', '2025-03-26 09:57:54.0'),
(24, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-53-19.sql', 1, 'sogut', '2025-03-26 09:57:56.0'),
(25, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-53-22.sql', 1, 'sogut', '2025-03-26 09:57:59.0'),
(26, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-57-34.sql', 1, 'sogut', '2025-03-26 09:58:02.0'),
(27, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-03-26_10-01-17.sql', 1, 'sogut', '2025-03-26 10:01:18.0'),
(28, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-58-04.sql', 1, 'sogut', '2025-03-26 10:01:23.0'),
(29, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-58-07.sql', 1, 'sogut', '2025-03-26 10:01:26.0'),
(30, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-58-10.sql', 1, 'sogut', '2025-03-26 10:01:29.0'),
(31, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_09-58-14.sql', 1, 'sogut', '2025-03-26 10:01:38.0'),
(32, 'CREATE', 'BACKUP', NULL, 'Database backup created by auto: driving_school_2025-03-26_10-02-55.sql', 1, 'sogut', '2025-03-26 10:02:55.0'),
(33, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_10-01-17.sql', 1, 'sogut', '2025-03-26 10:27:18.0'),
(34, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_10-02-55.sql', 1, 'sogut', '2025-03-26 10:27:22.0'),
(35, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-03-26_10-27-27.sql', 1, 'sogut', '2025-03-26 10:27:28.0'),
(36, 'UPDATE', 'CANDIDAT', 16, 'UPDATE candidat Ben Ali Ahmed', 1, 'sogut', '2025-03-26 10:28:06.0'),
(37, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-03-26_10-28-51.sql', 1, 'sogut', '2025-03-26 10:28:52.0'),
(38, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_10-28-51.sql', 1, 'sogut', '2025-03-26 10:35:05.0'),
(39, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_10-27-27.sql', 1, 'sogut', '2025-03-26 10:35:12.0'),
(40, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_10-36-38.sql', 1, 'sogut', '2025-03-26 10:38:39.0'),
(41, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_10-36-32.sql', 1, 'sogut', '2025-03-26 10:38:43.0'),
(42, 'DELETE', 'BACKUP', NULL, 'Database backup deleted by sogut: driving_school_2025-03-26_10-35-16.sql', 1, 'sogut', '2025-03-26 10:38:46.0'),
(43, 'RESTORE', 'BACKUP', NULL, 'Database restored from backup by sogut: driving_school_2025-03-26_10-40-29.sql', 1, 'sogut', '2025-03-26 10:41:15.0'),
(44, 'RESTORE', 'BACKUP', NULL, 'Database restored from backup by sogut: driving_school_2025-03-26_17-44-59.sql', 1, 'sogut', '2025-03-26 17:45:48.0'),
(45, 'DELETE', 'CANDIDAT', 16, 'Suppression du candidat Ben Ali Ahmed', 1, 'sogut', '2025-03-26 17:48:01.0'),
(46, 'UPDATE', 'EXAMEN', 16, 'UPDATE examen de Code d''ID : 16 de 26', 1, 'sogut', '2025-03-26 21:56:43.0'),
(48, 'DELETE', 'EXAMEN', 16, 'Suppression d''examen de Code d''ID :  16', 1, 'sogut', '2025-03-26 23:03:08.0'),
(49, 'UPDATE', 'EXAMEN', 17, 'UPDATE examen de Code d''ID : 17 de Ayari Rania', 1, 'sogut', '2025-03-26 23:05:29.0'),
(50, 'UPDATE', 'EXAMEN', 17, 'UPDATE examen de Code d''ID : 17 de Ayari Rania', 1, 'sogut', '2025-03-26 23:07:00.0'),
(51, 'UPDATE', 'EXAMEN', 18, 'UPDATE examen de Conduite d''ID : 18 de Ayari Rania', 1, 'sogut', '2025-03-26 23:07:18.0'),
(52, 'DELETE', 'AUDIT_LOG', 47, 'Suppression du log #47', 1, 'sogut', '2025-03-26 23:08:36.0'),
(53, 'UPDATE', 'MONITEUR', 3, 'UPDATE moniteur Chevalier Sophie', 1, 'sogut', '2025-04-01 06:40:46.0'),
(54, 'UPDATE', 'MONITEUR', 4, 'UPDATE moniteur Leroy David', 1, 'sogut', '2025-04-01 06:41:13.0'),
(55, 'UPDATE', 'MONITEUR', 11, 'UPDATE moniteur Vincent Aurélie', 1, 'sogut', '2025-04-01 06:41:24.0'),
(56, 'UPDATE', 'VEHICULE', 3, 'UPDATE vehicule Honda CBR (789 TUN 1234)', 1, 'sogut', '2025-04-01 16:25:13.0'),
(57, 'UPDATE', 'REPARATION', 2, 'UPDATE reparation pour véhicule ID: 1 - Vidange et changement filtres', 1, 'sogut', '2025-04-01 17:39:20.0'),
(58, 'UPDATE', 'REPARATION', 2, 'UPDATE reparation pour véhicule ID: 1 - Vidange et changement filtres', 1, 'sogut', '2025-04-01 17:39:28.0'),
(59, 'UPDATE', 'REPARATION', 2, 'UPDATE reparation pour véhicule ID: 1 - Vidange et changement filtres', 1, 'sogut', '2025-04-01 17:39:49.0'),
(60, 'UPDATE', 'CANDIDAT', 21, 'UPDATE candidat Mrad Leila', 1, 'sogut', '2025-04-01 17:43:05.0'),
(61, 'UPDATE', 'REPARATION', 5, 'UPDATE reparation pour véhicule ID: 4 - Remplacement embrayage', 1, 'sogut', '2025-04-01 17:44:48.0'),
(62, 'CREATE', 'Reparation', 5, 'Création de la réparation: Remplacement embrayage', 1, 'sogut', '2025-04-01 17:44:48.0'),
(63, 'UPDATE', 'REPARATION', 6, 'UPDATE reparation pour véhicule ID: 4 - changement des roues', 4, 'lotfi', '2025-04-01 17:50:23.0'),
(64, 'CREATE', 'REPARATION', 6, 'Création de la réparation: changement des roues', 4, 'lotfi', '2025-04-01 17:50:23.0'),
(65, 'UPDATE', 'VEHICULE', 4, 'UPDATE vehicule Mercedes Actros (321 TUN 6543)', 1, 'sogut', '2025-04-02 08:46:21.0'),
(66, 'UPDATE', 'VEHICULE', 4, 'UPDATE vehicule Mercedes Actros (321 TUN 6543)', 1, 'sogut', '2025-04-02 09:00:12.0'),
(67, 'UPDATE', 'VEHICULE', 4, 'UPDATE vehicule Mercedes Actros (321 TUN 6543)', 1, 'sogut', '2025-04-02 09:05:43.0'),
(68, 'UPDATE', 'VEHICULE', 3, 'UPDATE vehicule Honda CBR (789 TUN 1234)', 1, 'sogut', '2025-04-02 09:07:19.0'),
(69, 'UPDATE', 'SEANCE', 1, 'UPDATE séance de Conduite (ID: 1)', 1, 'sogut', '2025-04-02 12:29:48.0'),
(70, 'DELETE', 'SEANCE', 1, 'Suppression de la séance #1 de type Conduite', 1, 'sogut', '2025-04-02 12:39:58.0'),
(71, 'UPDATE', 'SEANCE', 2, 'UPDATE séance de Conduite (ID: 2)', 1, 'sogut', '2025-04-02 12:40:11.0'),
(72, 'DELETE', 'SEANCE', 2, 'Suppression de la séance #2 de type Conduite', 1, 'sogut', '2025-04-02 12:48:45.0'),
(73, 'UPDATE', 'SEANCE', 3, 'UPDATE séance de Conduite (ID: 3)', 1, 'sogut', '2025-04-02 13:09:11.0'),
(74, 'UPDATE', 'SEANCE', 4, 'UPDATE séance de Conduite (ID: 4)', 1, 'sogut', '2025-04-03 03:29:32.0'),
(75, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-04-03_06-12-36.sql', 1, 'sogut', '2025-04-03 06:12:36.0'),
(76, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-04-03_06-26-35.sql', 1, 'sogut', '2025-04-03 06:26:36.0'),
(77, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-04-03_13-05-57.sql', 1, 'sogut', '2025-04-03 13:05:57.0'),
(78, 'UPDATE', 'MONITEUR', 7, 'UPDATE moniteur Fournier Isabelle', 1, 'sogut', '2025-04-03 19:11:38.0'),
(79, 'UPDATE', 'CANDIDAT', 17, 'UPDATE candidat Haddad Fatma', 1, 'sogut', '2025-04-04 01:31:48.0'),
(80, 'UPDATE', 'CANDIDAT', 18, 'UPDATE candidat Trabelsi Mohamed', 1, 'sogut', '2025-04-04 01:33:09.0'),
(81, 'UPDATE', 'CANDIDAT', 19, 'UPDATE candidat Bouazzi Sana', 1, 'sogut', '2025-04-04 01:33:20.0'),
(82, 'UPDATE', 'CANDIDAT', 20, 'UPDATE candidat Jebalii Khaled', 1, 'sogut', '2025-04-04 01:33:35.0'),
(83, 'UPDATE', 'CANDIDAT', 21, 'UPDATE candidat Mrad Leila', 1, 'sogut', '2025-04-04 01:33:47.0'),
(84, 'UPDATE', 'CANDIDAT', 22, 'UPDATE candidat Khlifi Youssef', 1, 'sogut', '2025-04-04 01:34:00.0'),
(85, 'UPDATE', 'CANDIDAT', 23, 'UPDATE candidat Ben Salem Imen', 1, 'sogut', '2025-04-04 01:34:54.0'),
(86, 'UPDATE', 'CANDIDAT', 26, 'UPDATE candidat Ben Amor Walid', 1, 'sogut', '2025-04-04 01:35:15.0'),
(87, 'UPDATE', 'CANDIDAT', 27, 'UPDATE candidat Dhaouadi Nadia', 1, 'sogut', '2025-04-04 01:35:28.0'),
(88, 'UPDATE', 'CANDIDAT', 28, 'UPDATE candidat Gharbi Fares', 1, 'sogut', '2025-04-04 01:35:50.0'),
(89, 'UPDATE', 'CANDIDAT', 33, 'UPDATE candidat Bouzid Kamel', 1, 'sogut', '2025-04-04 01:36:14.0'),
(90, 'UPDATE', 'EXAMEN', 49, 'UPDATE examen de Code d''ID : 49 de Haddad Fatma', 1, 'sogut', '2025-04-04 05:02:11.0'),
(91, 'UPDATE', 'EXAMEN', 50, 'UPDATE examen de Conduite d''ID : 50 de Haddad Fatma', 1, 'sogut', '2025-04-04 05:02:26.0'),
(92, 'CREATE', 'PAIEMENT', NULL, 'Création d''un nouveau paiement pour Haddad Fatma', 1, 'sogut', '2025-04-06 15:38:29.0'),
(93, 'CREATE', 'PAIEMENT', NULL, 'Création d''un nouveau paiement pour Haddad Fatma', 1, 'sogut', '2025-04-06 15:40:05.0'),
(94, 'CREATE', 'PAIEMENT', NULL, 'Création d''un nouveau paiement pour Haddad Fatma', 1, 'sogut', '2025-04-06 15:41:13.0'),
(95, 'CREATE', 'PAIEMENT', NULL, 'Création d''un nouveau paiement pour Haddad Fatma', 1, 'sogut', '2025-04-06 15:43:10.0'),
(96, 'CREATE', 'PAIEMENT', NULL, 'Création d''un nouveau paiement pour Haddad Fatma', 1, 'sogut', '2025-04-06 15:52:07.0'),
(97, 'CREATE', 'PAIEMENT', NULL, 'Création d''un nouveau paiement pour Haddad Fatma', 1, 'sogut', '2025-04-06 15:55:53.0'),
(98, 'CREATE', 'PAIEMENT', 1, 'Création d''un nouveau paiement pour Haddad Fatma', 1, 'sogut', '2025-04-06 16:00:43.0'),
(99, 'CREATE', 'PAIEMENT', 2, 'Création d''un nouveau paiement pour Trabelsi Mohamed', 1, 'sogut', '2025-04-06 16:15:43.0'),
(100, 'CREATE', 'DEPENSE', 1, 'Création d''une nouvelle dépense de catégorie MONITEUR', 1, 'sogut', '2025-04-06 18:04:55.0'),
(101, 'CREATE', 'DEPENSE', 2, 'Paiement groupé pour Vincent Aurélie', 1, 'sogut', '2025-04-06 18:05:13.0'),
(102, 'DELETE', 'PAIEMENT', 2, 'Suppression du paiement ID: 2', 1, 'sogut', '2025-04-07 06:40:52.0'),
(103, 'UPDATE', 'CANDIDAT', 27, 'UPDATE candidat Dhaouadi Nadia', 4, 'lotfi', '2025-04-07 12:50:04.0');
INSERT INTO `audit_logs` (`id`, `action`, `entity_type`, `entity_id`, `details`, `user_id`, `username`, `timestamp`) VALUES
(104, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-04-07_12-53-30.sql', 1, 'sogut', '2025-04-07 12:53:30.0'),
(105, 'UPDATE', 'CANDIDAT', 17, 'UPDATE candidat Haddaddddddddd Fatma', 1, 'sogut', '2025-04-07 12:53:46.0');

-- Table structure for table `candidat`
DROP TABLE IF EXISTS `candidat`;
CREATE TABLE `candidat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `prenom` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `cin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `date_naissance` date NOT NULL,
  `telephone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `adresse` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `date_inscription` date NOT NULL,
  `categories_permis` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `chemin_photo_cin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `chemin_photo_identite` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `chemin_certificat_medical` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `chemin_fiche_pdf` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `chemin_fiche_png` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `actif` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cin_UNIQUE` (`cin`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `candidat`
INSERT INTO `candidat` (`id`, `nom`, `prenom`, `cin`, `date_naissance`, `telephone`, `adresse`, `email`, `date_inscription`, `categories_permis`, `chemin_photo_cin`, `chemin_photo_identite`, `chemin_certificat_medical`, `chemin_fiche_pdf`, `chemin_fiche_png`, `actif`) VALUES
(17, 'Haddaddddddddd', 'Fatma', '18765432', '2001-03-22 00:00:00.0', '55987654', 'Avenue Habib Bourguiba, Sousse', 'fatma.haddad@example.com', '2023-06-09 00:00:00.0', 'A,B', NULL, 'documents\candidats\18765432_PHOTO_1743726704496.jpg', NULL, 'documents/generated/Fiche_Candidat_20250324.pdf', NULL, 0),
(18, 'Trabelsi', 'Mohamed', '11223344', '1987-11-10 00:00:00.0', '98112231', 'Cité Olympique, Radès', 'mohamed.trabelsi@example.com', '2023-06-29 00:00:00.0', 'C', NULL, 'documents\candidats\11223344_PHOTO_1743726785957.jpg', NULL, 'documents/generated/Fiche_Candidat_20250324.pdf', NULL, 1),
(19, 'Bouazzi', 'Sana', '14321055', '1995-06-05 00:00:00.0', '22556677', 'Route de la Marsa, Tunis', 'sana.bouazzi@example.com', '2024-07-22 00:00:00.0', 'B,C', NULL, 'documents\candidats\14321055_PHOTO_1743726797069.jpg', NULL, 'documents/generated/Fiche_Candidat_20250325.pdf', NULL, 1),
(20, 'Jebalii', 'Khaled', '00001122', '1983-09-28 00:00:00.0', '71990011', 'Rue Charles de Gaulle, Bizerte', 'khaled.jebali@example.com', '2023-07-06 00:00:00.0', 'A', NULL, 'documents\candidats\00001122_PHOTO_1743726812156.jpg', NULL, NULL, NULL, 1),
(21, 'Mrad', 'Leila', '13445566', '1998-02-14 00:00:00.0', '50334455', 'Avenue Kennedy, Sfax', 'leila.mrad@example.com', '2024-08-05 00:00:00.0', 'B', NULL, 'documents\candidats\13445566_PHOTO_1743726824776.jpg', NULL, 'documents/generated/Fiche_Candidat_20250319.pdf', NULL, 0),
(22, 'Khlifi', 'Youssef', '17889900', '1991-05-01 00:00:00.0', '25778899', 'Rue Ibn Khaldoun, Kairouan', 'youssef.khlifi@example.com', '2024-08-12 00:00:00.0', 'A,B,C', NULL, 'documents\candidats\17889900_PHOTO_1743726837527.jpg', NULL, 'documents/generated/Fiche_Candidat_20250321.pdf', NULL, 1),
(23, 'Ben Salem', 'Imen', '04556677', '1985-12-18 00:00:00.0', '15445561', 'Avenue 7 Novembre, Gabès', 'imen.bensalem@example.com', '2024-08-19 00:00:00.0', 'C', NULL, 'documents\candidats\04556677_PHOTO_1743726848124.jpg', NULL, NULL, NULL, 1),
(26, 'Ben Amor', 'Walid', '16778899', '1989-10-25 00:00:00.0', '24667788', 'Rue Mongi Slim, Monastir', 'walid.benamor@example.com', '2024-09-09 00:00:00.0', 'A', NULL, 'documents\candidats\66778899_PHOTO_1743726904157.jpg', NULL, NULL, NULL, 0),
(27, 'Dhaouadi', 'Nadia', '00112233', '1986-01-30 00:00:00.0', '51001122', 'Avenue de Carthage, Tunisss', 'nadia.dhaouadi@example.com', '2024-09-16 00:00:00.0', 'B', NULL, 'documents\candidats\00112233_PHOTO_1743726924452.jpg', NULL, NULL, NULL, 1),
(28, 'Gharbi', 'Fares', '12340987', '1994-09-17 00:00:00.0', '98123409', 'Route Sidi Dhrif, Sidi Bou Said', 'fares.gharbi@example.com', '2024-09-23 00:00:00.0', 'B,C', NULL, 'documents\candidats\12340987_PHOTO_1743726948037.jpg', NULL, NULL, NULL, 0),
(29, 'Ayari', 'Rania', '11223355', '1991-06-02 00:00:00.0', '71456712', 'Avenue Farhat Hached, Kasserine', 'rania.ayari@example.com', '2024-09-30 00:00:00.0', 'B', NULL, NULL, NULL, 'documents/generated/Fiche_Candidat_20250319.pdf', NULL, 1),
(32, 'Mejri', 'Amira', '10293847', '1993-05-12 00:00:00.0', '20123456', '15 Rue de Marseille, Tunis', 'amira.mejri@example.com', '2024-10-05 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 1),
(33, 'Bouzid', 'Kamel', '00394857', '1990-08-23 00:00:00.0', '57234567', '7 Avenue Habib Bourguiba, Sousse', 'kamel.bouzid@example.com', '2024-10-08 00:00:00.0', 'A,B', NULL, 'documents\candidats\20394857_PHOTO_1743726957557.jpg', NULL, NULL, NULL, 1),
(34, 'Chaabane', 'Leila', '30485968', '1995-02-17 00:00:00.0', '95345678', '22 Rue Ibn Khaldoun, Sfax', 'leila.chaabane@example.com', '2024-10-12 00:00:00.0', 'C', NULL, NULL, NULL, NULL, NULL, 1),
(35, 'Dridi', 'Sami', '40596079', '1988-11-30 00:00:00.0', '22456789', '9 Avenue de Carthage, Bizerte', 'sami.dridi@example.com', '2024-10-15 00:00:00.0', 'A,B,C', NULL, NULL, NULL, NULL, NULL, 1),
(36, 'Ferchichi', 'Nour', '50607182', '1997-07-05 00:00:00.0', '50567890', '3 Rue de la Liberté, Nabeul', 'nour.ferchichi@example.com', '2024-10-18 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 1),
(37, 'Gharsallah', 'Yassine', '60718293', '1992-04-20 00:00:00.0', '25678901', '18 Avenue Farhat Hached, Monastir', 'yassine.gharsallah@example.com', '2024-10-22 00:00:00.0', 'A', NULL, NULL, NULL, NULL, NULL, 1),
(38, 'Hamdi', 'Rania', '70829304', '1994-09-15 00:00:00.0', '98789012', '5 Rue Ali Bach Hamba, Mahdia', 'rania.hamdi@example.com', '2024-10-25 00:00:00.0', 'B,C', NULL, NULL, NULL, NULL, NULL, 1),
(39, 'Jebali', 'Tarek', '80930415', '1989-01-08 00:00:00.0', '22890123', '12 Avenue Mohamed V, Gabès', 'tarek.jebali@example.com', '2024-10-29 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 1),
(40, 'Khelifi', 'Mariem', '90041526', '1996-06-25 00:00:00.0', '50901234', '27 Rue Mongi Slim, Kairouan', 'mariem.khelifi@example.com', '2024-11-02 00:00:00.0', 'A,B', NULL, NULL, NULL, NULL, NULL, 1),
(41, 'Laabidi', 'Bilel', '10152637', '1991-03-14 00:00:00.0', '25012345', '8 Avenue de la République, Gafsa', 'bilel.laabidi@example.com', '2024-11-05 00:00:00.0', 'C', NULL, NULL, NULL, NULL, NULL, 1),
(42, 'Maaloul', 'Ines', '11263748', '1998-12-03 00:00:00.0', '98123456', '14 Rue Ibn Rachiq, Béja', 'ines.maaloul@example.com', '2024-11-08 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 1),
(43, 'Nasri', 'Omar', '12374859', '1987-10-19 00:00:00.0', '22234567', '6 Avenue Habib Thameur, Jendouba', 'omar.nasri@example.com', '2024-11-12 00:00:00.0', 'A,B,C', NULL, NULL, NULL, NULL, NULL, 1),
(44, 'Oueslati', 'Fatma', '13485960', '1993-08-07 00:00:00.0', '50345678', '19 Rue Alain Savary, Médenine', 'fatma.oueslati@example.com', '2024-11-15 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 1),
(45, 'Riahi', 'Mehdi', '14596071', '1990-05-22 00:00:00.0', '25456789', '11 Avenue de Paris, Tataouine', 'mehdi.riahi@example.com', '2024-11-19 00:00:00.0', 'A', NULL, NULL, NULL, NULL, NULL, 1),
(46, 'Saidi', 'Asma', '15607182', '1995-11-11 00:00:00.0', '98567890', '23 Rue Hédi Chaker, Kef', 'asma.saidi@example.com', '2024-11-22 00:00:00.0', 'B,C', NULL, NULL, NULL, NULL, NULL, 1),
(47, 'Toumi', 'Hichem', '16718293', '1989-07-28 00:00:00.0', '22678901', '4 Avenue Bourguiba, Kasserine', 'hichem.toumi@example.com', '2024-11-26 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 0),
(48, 'Yahyaoui', 'Sirine', '17829304', '1997-04-16 00:00:00.0', '50789012', '16 Rue de la Mosquée, Siliana', 'sirine.yahyaoui@example.com', '2024-11-29 00:00:00.0', 'A,B', NULL, NULL, NULL, NULL, NULL, 0),
(49, 'Zaidi', 'Nabil', '18930415', '1992-01-09 00:00:00.0', '25890123', '7 Avenue de l''Environnement, Zaghouan', 'nabil.zaidi@example.com', '2024-12-03 00:00:00.0', 'C', NULL, NULL, NULL, NULL, NULL, 0),
(50, 'Abidi', 'Lamia', '19041526', '1994-06-30 00:00:00.0', '98901234', '20 Rue des Orangers, Tozeur', 'lamia.abidi@example.com', '2024-12-06 00:00:00.0', 'A,B,C', NULL, NULL, NULL, NULL, NULL, 0),
(51, 'Belhadj', 'Karim', '20152637', '1988-03-25 00:00:00.0', '22012345', '9 Avenue de la Liberté, Kebili', 'karim.belhadj@example.com', '2024-12-10 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 0),
(52, 'Chatti', 'Manel', '21263748', '1996-10-14 00:00:00.0', '50123456', '13 Rue Ibn Sina, Sidi Bouzid', 'manel.chatti@example.com', '2024-12-13 00:00:00.0', 'A', NULL, NULL, NULL, NULL, NULL, 0),
(53, 'Dallagi', 'Firas', '22374859', '1991-05-07 00:00:00.0', '25234567', '5 Avenue de la République, Ariana', 'firas.dallagi@example.com', '2024-12-17 00:00:00.0', 'B,C', NULL, NULL, NULL, NULL, NULL, 0),
(54, 'Elloumi', 'Salma', '23485960', '1993-12-22 00:00:00.0', '98345678', '17 Rue de Palestine, Ben Arous', 'salma.elloumi@example.com', '2024-12-20 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 0),
(55, 'Ferjani', 'Anis', '24596071', '1990-09-18 00:00:00.0', '22456789', '8 Avenue Habib Bourguiba, Manouba', 'anis.ferjani@example.com', '2024-12-24 00:00:00.0', 'A,B', NULL, NULL, NULL, NULL, NULL, 0),
(56, 'Guizani', 'Rim', '25607182', '1995-04-03 00:00:00.0', '50567890', '21 Rue des Jasmins, La Marsa', 'rim.guizani@example.com', '2024-12-27 00:00:00.0', 'C', NULL, NULL, NULL, NULL, NULL, 0),
(57, 'Haddad', 'Zied', '26718293', '1987-01-26 00:00:00.0', '25678901', '10 Avenue de Carthage, Sidi Bou Said', 'zied.haddad@example.com', '2024-12-31 00:00:00.0', 'A,B,C', NULL, NULL, NULL, NULL, NULL, 0),
(58, 'Issaoui', 'Nesrine', '27829304', '1998-08-11 00:00:00.0', '98789012', '24 Rue des Palmiers, Gammarth', 'nesrine.issaoui@example.com', '2025-01-03 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 0),
(59, 'Jouini', 'Wassim', '28930415', '1992-03-29 00:00:00.0', '22890123', '6 Avenue de la Plage, Hammamet', 'wassim.jouini@example.com', '2025-01-07 00:00:00.0', 'A', NULL, NULL, NULL, NULL, NULL, 0),
(60, 'Kouki', 'Yasmine', '29041526', '1994-10-12 00:00:00.0', '50901234', '15 Rue des Oliviers, Korba', 'yasmine.kouki@example.com', '2025-01-10 00:00:00.0', 'B,C', NULL, NULL, NULL, NULL, NULL, 0);

-- Table structure for table `depenses`
DROP TABLE IF EXISTS `depenses`;
CREATE TABLE `depenses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categorie` varchar(50) NOT NULL,
  `montant` double NOT NULL,
  `date_depense` date NOT NULL,
  `description` text,
  `vehicule_id` bigint DEFAULT NULL,
  `moniteur_id` bigint DEFAULT NULL,
  `paye` tinyint(1) DEFAULT '0',
  `type_vehicule_depense` varchar(50) DEFAULT NULL,
  `reparation_id` bigint DEFAULT NULL,
  `type_autre_depense` varchar(100) DEFAULT NULL,
  `date_creation` date DEFAULT NULL,
  `date_modification` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_depenses_date` (`date_depense`),
  KEY `idx_depenses_categorie` (`categorie`),
  KEY `idx_depenses_vehicule` (`vehicule_id`),
  KEY `idx_depenses_moniteur` (`moniteur_id`),
  KEY `idx_depenses_paye` (`paye`),
  KEY `idx_depenses_reparation_id` (`reparation_id`),
  CONSTRAINT `depenses_ibfk_1` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicule` (`id`) ON DELETE SET NULL,
  CONSTRAINT `depenses_ibfk_2` FOREIGN KEY (`moniteur_id`) REFERENCES `moniteur` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `depenses`
INSERT INTO `depenses` (`id`, `categorie`, `montant`, `date_depense`, `description`, `vehicule_id`, `moniteur_id`, `paye`, `type_vehicule_depense`, `reparation_id`, `type_autre_depense`, `date_creation`, `date_modification`) VALUES
(1, 'MONITEUR', 3500.0, '2025-04-06 00:00:00.0', '', NULL, 11, 0, NULL, NULL, NULL, '2025-04-06 00:00:00.0', '2025-04-06 00:00:00.0'),
(2, 'MONITEUR', 3500.0, '2025-04-06 00:00:00.0', 'Paiement groupé pour Vincent Aurélie', NULL, 11, 0, NULL, NULL, NULL, '2025-04-06 00:00:00.0', NULL);

-- Table structure for table `examen`
DROP TABLE IF EXISTS `examen`;
CREATE TABLE `examen` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `candidat_id` bigint NOT NULL,
  `type_examen` enum('Code','Conduite') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `date_examen` date NOT NULL,
  `lieu_examen` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `frais_inscription` double NOT NULL,
  `est_valide` tinyint(1) NOT NULL,
  `latitude` float DEFAULT '0',
  `longitude` float DEFAULT '0',
  `adresse` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `candidat_id` (`candidat_id`),
  CONSTRAINT `examen_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `examen`
INSERT INTO `examen` (`id`, `candidat_id`, `type_examen`, `date_examen`, `lieu_examen`, `frais_inscription`, `est_valide`, `latitude`, `longitude`, `adresse`) VALUES
(13, 27, 'Code', '2026-03-13 00:00:00.0', 'ygugy', 3300.0, 1, 0.0, 0.0, NULL),
(14, 27, 'Conduite', '2026-03-07 00:00:00.0', 'hjgjh', 3200.0, 0, 0.0, 0.0, NULL),
(17, 29, 'Code', '2025-06-13 00:00:00.0', 'aaa', 1220.0, 1, 0.0, 0.0, NULL),
(18, 29, 'Conduite', '2028-03-18 00:00:00.0', 'aasssssss', 3000.0, 1, 0.0, 0.0, NULL),
(19, 32, 'Code', '2025-04-15 00:00:00.0', 'Centre d''examen Tunis', 250.0, 1, 0.0, 0.0, NULL),
(20, 33, 'Code', '2025-04-18 00:00:00.0', 'Centre d''examen Sousse', 250.0, 1, 0.0, 0.0, NULL),
(21, 34, 'Code', '2025-04-22 00:00:00.0', 'Centre d''examen Sfax', 250.0, 1, 0.0, 0.0, NULL),
(22, 35, 'Code', '2025-04-25 00:00:00.0', 'Centre d''examen Bizerte', 250.0, 1, 0.0, 0.0, NULL),
(23, 36, 'Code', '2025-04-29 00:00:00.0', 'Centre d''examen Nabeul', 250.0, 1, 0.0, 0.0, NULL),
(24, 37, 'Code', '2025-05-02 00:00:00.0', 'Centre d''examen Monastir', 250.0, 1, 0.0, 0.0, NULL),
(25, 38, 'Code', '2025-05-06 00:00:00.0', 'Centre d''examen Mahdia', 250.0, 1, 0.0, 0.0, NULL),
(26, 39, 'Code', '2025-05-09 00:00:00.0', 'Centre d''examen Gabès', 250.0, 1, 0.0, 0.0, NULL),
(27, 40, 'Code', '2025-05-13 00:00:00.0', 'Centre d''examen Kairouan', 250.0, 1, 0.0, 0.0, NULL),
(28, 41, 'Code', '2025-05-16 00:00:00.0', 'Centre d''examen Gafsa', 250.0, 1, 0.0, 0.0, NULL),
(29, 42, 'Code', '2025-05-20 00:00:00.0', 'Centre d''examen Béja', 250.0, 0, 0.0, 0.0, NULL),
(30, 43, 'Code', '2025-05-23 00:00:00.0', 'Centre d''examen Jendouba', 250.0, 0, 0.0, 0.0, NULL),
(31, 44, 'Code', '2025-05-27 00:00:00.0', 'Centre d''examen Médenine', 250.0, 0, 0.0, 0.0, NULL),
(32, 45, 'Code', '2025-05-30 00:00:00.0', 'Centre d''examen Tataouine', 250.0, 0, 0.0, 0.0, NULL),
(33, 46, 'Code', '2025-06-03 00:00:00.0', 'Centre d''examen Kef', 250.0, 0, 0.0, 0.0, NULL),
(34, 32, 'Conduite', '2025-06-16 00:00:00.0', 'Circuit de conduite 5', 350.0, 1, 0.0, 0.0, NULL),
(35, 33, 'Conduite', '2025-06-17 00:00:00.0', 'Circuit de conduite 2', 350.0, 1, 0.0, 0.0, NULL),
(36, 34, 'Conduite', '2025-06-18 00:00:00.0', 'Circuit de conduite 4', 350.0, 0, 0.0, 0.0, NULL),
(37, 35, 'Conduite', '2025-06-19 00:00:00.0', 'Circuit de conduite 2', 350.0, 0, 0.0, 0.0, NULL),
(38, 36, 'Conduite', '2025-06-20 00:00:00.0', 'Circuit de conduite 3', 350.0, 0, 0.0, 0.0, NULL),
(39, 37, 'Conduite', '2025-06-21 00:00:00.0', 'Circuit de conduite 4', 350.0, 1, 0.0, 0.0, NULL),
(40, 38, 'Conduite', '2025-06-22 00:00:00.0', 'Circuit de conduite 3', 350.0, 1, 0.0, 0.0, NULL),
(41, 39, 'Conduite', '2025-06-23 00:00:00.0', 'Circuit de conduite 1', 350.0, 1, 0.0, 0.0, NULL),
(42, 40, 'Conduite', '2025-06-24 00:00:00.0', 'Circuit de conduite 4', 350.0, 0, 0.0, 0.0, NULL),
(43, 41, 'Conduite', '2025-06-25 00:00:00.0', 'Circuit de conduite 2', 350.0, 1, 0.0, 0.0, NULL),
(49, 17, 'Code', '2025-04-10 00:00:00.0', 'route bizzetre', 2332.0, 1, 0.0, 0.0, NULL),
(50, 17, 'Conduite', '2025-04-17 00:00:00.0', 'aaaaaa', 2100.0, 1, 0.0, 0.0, NULL);

-- Table structure for table `moniteur`
DROP TABLE IF EXISTS `moniteur`;
CREATE TABLE `moniteur` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  `prenom` varchar(255) NOT NULL,
  `cin` varchar(255) NOT NULL,
  `date_naissance` date DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `date_embauche` date DEFAULT NULL,
  `date_fin_contrat` date DEFAULT NULL,
  `num_permis` varchar(255) DEFAULT NULL,
  `categories_permis` varchar(255) DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT NULL,
  `motif` varchar(300) DEFAULT NULL,
  `salaire` double DEFAULT NULL,
  `experience` double DEFAULT NULL,
  `diplomes` varchar(300) DEFAULT NULL,
  `notes` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `moniteur`
INSERT INTO `moniteur` (`id`, `nom`, `prenom`, `cin`, `date_naissance`, `telephone`, `date_embauche`, `date_fin_contrat`, `num_permis`, `categories_permis`, `disponible`, `motif`, `salaire`, `experience`, `diplomes`, `notes`) VALUES
(2, 'Lambert', 'Paul', '07890122', '1985-02-20 00:00:00.0', '55577881', '2020-07-01 00:00:00.0', '2025-06-30 00:00:00.0', 'P202012345', 'A,B', 1, 'Fin de formation', 2500.0, 5.0, 'BEPECASER', 'Moniteur expérimenté et pédagogue'),
(3, 'Chevalier', 'Sophie', '02109876', '1982-09-14 00:00:00.0', '55599001', '2021-01-15 00:00:00.0', '2026-01-14 00:00:00.0', 'P202154321', 'B,C', 0, 'Mutation', 2800.0, 8.0, 'BEPECASER, Mention deux-roues', 'Spécialiste de la conduite en ville'),
(4, 'Leroy', 'David', '08901234', '1988-04-03 00:00:00.0', '55511222', '2022-03-04 00:00:00.0', '2016-02-04 00:00:00.0', 'P202298765', 'A', 0, 'Congé maternité', 2200.0, 3.0, 'BEPECASER', 'En congé de maternité jusqu''en septembre'),
(5, 'Girard', 'Céline', '03210987', '1980-11-27 00:00:00.0', '55533442', '2019-05-20 00:00:00.0', '2024-05-19 00:00:00.0', 'P201934567', 'B', 0, 'Déménagement', 3000.0, 10.0, 'BEPECASER, CS moto', 'Monitrice passionnée par la sécurité routière'),
(6, 'Dupuis', 'Thomas', '19012345', '1986-07-08 00:00:00.0', '55555665', '2023-09-01 00:00:00.0', '2020-08-06 00:00:00.0', 'P202376543', 'A,B,C', 0, '', 2700.0, 0.0, '', ''),
(7, 'Fournier', 'Isabelle', '14321098', '1983-03-16 00:00:00.0', '55577883', '2020-11-01 00:00:00.0', '2025-10-31 00:00:00.0', 'P202023456', 'C', 1, 'Arrêt maladie', 2400.0, 4.0, 'BEPECASER', 'En arrêt maladie depuis juillet'),
(8, 'Moreau', 'Antoine', '00123456', '1990-08-10 00:00:00.0', '55599008', '2021-05-15 00:00:00.0', '2026-05-14 00:00:00.0', 'P202165432', 'B', 1, 'Formation', 2900.0, 9.0, 'BEPECASER', 'Suit une formation complémentaire sur les nouvelles technologies'),
(9, 'Lemaire', 'Julie', '12345098', '1981-01-05 00:00:00.0', '55511221', '2022-07-01 00:00:00.0', '2027-06-30 00:00:00.0', 'P202245678', 'A,B', 1, 'Vacances', 2300.0, 2.0, 'BEPECASER', 'En vacances jusqu''à fin août'),
(10, 'Rousseau', 'Sébastien', '14325555', '1987-06-12 00:00:00.0', '55533447', '2018-09-20 00:00:00.0', '2023-09-19 00:00:00.0', 'P201887654', 'B,C', 1, 'Décès', 3100.0, 11.0, 'BEPECASER, Mention simulateur', 'Spécialiste de la formation sur simulateur'),
(11, 'Vincent', 'Aurélie', '14567891', '1984-05-01 00:00:00.0', '91333566', '2023-01-15 00:00:00.0', '2028-01-14 00:00:00.0', 'P202310987', 'A,C', 1, 'autre motif', 2600.0, 5.0, 'BEPECASER', 'Monitrice attentive et à l''écoute'),
(12, 'Mejri', 'Sofien', '30152637', '1985-07-18 00:00:00.0', '50012345', '2022-05-15 00:00:00.0', '2027-05-14 00:00:00.0', 'P202245678', 'A,B', 1, NULL, 2700.0, 7.0, 'BEPECASER', 'Spécialiste de la conduite en ville'),
(13, 'Bouzidi', 'Amina', '31263748', '1988-04-03 00:00:00.0', '25123456', '2023-08-01 00:00:00.0', '2028-07-31 00:00:00.0', 'P202356789', 'B,C', 1, NULL, 2500.0, 5.0, 'BEPECASER', 'Excellente pédagogue'),
(14, 'Chabbi', 'Hamza', '32374859', '1983-11-22 00:00:00.0', '98234567', '2021-03-10 00:00:00.0', '2026-03-09 00:00:00.0', 'P202167890', 'A,B,C', 1, NULL, 2900.0, 9.0, 'BEPECASER, Mention deux-roues', 'Formateur expérimenté'),
(15, 'Driss', 'Sarra', '33485960', '1990-06-09 00:00:00.0', '22345678', '2024-01-15 00:00:00.0', '2029-01-14 00:00:00.0', 'P202478901', 'B', 1, NULL, 2300.0, 3.0, 'BEPECASER', 'Nouvelle recrue prometteuse'),
(16, 'Fakhfakh', 'Nizar', '34596071', '1986-03-27 00:00:00.0', '50456789', '2022-09-20 00:00:00.0', '2027-09-19 00:00:00.0', 'P202289012', 'A', 1, NULL, 2600.0, 6.0, 'BEPECASER, CS moto', 'Spécialiste de la conduite moto'),
(17, 'Ghorbel', 'Yosra', '35607182', '1989-10-14 00:00:00.0', '25567890', '2023-04-05 00:00:00.0', '2028-04-04 00:00:00.0', 'P202390123', 'B,C', 1, NULL, 2800.0, 8.0, 'BEPECASER', 'Très appréciée des élèves'),
(18, 'Hamrouni', 'Riadh', '36718293', '1984-05-31 00:00:00.0', '98678901', '2021-11-15 00:00:00.0', '2026-11-14 00:00:00.0', 'P202101234', 'A,B', 1, NULL, 3000.0, 10.0, 'BEPECASER, Mention simulateur', 'Expert en formation sur simulateur'),
(19, 'Jlassi', 'Taoufik', '37829304', '1987-02-19 00:00:00.0', '22789012', '2022-07-01 00:00:00.0', '2027-06-30 00:00:00.0', 'P202212345', 'C', 0, 'Congé maladie', 2400.0, 4.0, 'BEPECASER', 'En arrêt maladie jusqu''au 15 mai'),
(20, 'Khelifi', 'Mouna', '38930415', '1991-09-08 00:00:00.0', '50890123', '2023-10-10 00:00:00.0', '2028-10-09 00:00:00.0', 'P202323456', 'A,B', 0, 'Formation', 2700.0, 7.0, 'BEPECASER', 'En formation complémentaire'),
(21, 'Laabidi', 'Bassem', '39041526', '1985-04-25 00:00:00.0', '25901234', '2021-06-20 00:00:00.0', '2026-06-19 00:00:00.0', 'P202134567', 'B', 0, 'Congé parental', 2500.0, 5.0, 'BEPECASER', 'En congé parental jusqu''en juin'),
(22, 'Maalej', 'Imen', '40152637', '1988-01-12 00:00:00.0', '98012345', '2022-12-05 00:00:00.0', '2027-12-04 00:00:00.0', 'P202245678', 'A,B,C', 0, 'Mutation', 2900.0, 9.0, 'BEPECASER', 'En cours de mutation vers une autre agence'),
(23, 'Nasraoui', 'Omar', '41263748', '1983-08-29 00:00:00.0', '22123456', '2023-02-15 00:00:00.0', '2028-02-14 00:00:00.0', 'P202356789', 'B,C', 0, 'Vacances', 2600.0, 6.0, 'BEPECASER', 'En vacances jusqu''à fin avril'),
(24, 'Ouerghi', 'Fatma', '42374859', '1990-03-16 00:00:00.0', '50234567', '2021-09-01 00:00:00.0', '2026-08-31 00:00:00.0', 'P202167890', 'A', 0, 'Arrêt maladie', 2400.0, 4.0, 'BEPECASER, CS moto', 'En arrêt maladie longue durée'),
(25, 'Riahi', 'Mehdi', '43485960', '1986-12-03 00:00:00.0', '25345678', '2022-04-10 00:00:00.0', '2027-04-09 00:00:00.0', 'P202278901', 'B', 0, 'Formation', 2800.0, 8.0, 'BEPECASER', 'En formation sur les nouvelles réglementations'),
(26, 'Saidi', 'Asma', '44596071', '1989-07-20 00:00:00.0', '98456789', '2023-11-20 00:00:00.0', '2028-11-19 00:00:00.0', 'P202389012', 'A,B', 0, 'Congé sans solde', 2700.0, 7.0, 'BEPECASER', 'En congé sans solde pour 3 mois');

-- Table structure for table `paiements`
DROP TABLE IF EXISTS `paiements`;
CREATE TABLE `paiements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `candidat_id` bigint DEFAULT NULL COMMENT 'Reference to candidats table',
  `date_paiement` date DEFAULT NULL COMMENT 'Date when payment was made',
  `montant` decimal(10,2) DEFAULT NULL COMMENT 'Payment amount',
  `methode_paiement` varchar(50) DEFAULT NULL COMMENT 'Payment method (ESPECES, CARTE, CHEQUE, VIREMENT)',
  `reference` varchar(100) DEFAULT NULL COMMENT 'Unique payment reference number',
  `statut` varchar(50) DEFAULT NULL COMMENT 'Payment status (COMPLET, PARTIEL, REMBOURSEMENT)',
  `notes` text COMMENT 'Additional notes about the payment',
  `remise` decimal(5,2) DEFAULT NULL COMMENT 'Discount percentage applied to payment',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_candidat` (`candidat_id`),
  KEY `idx_date` (`date_paiement`),
  KEY `idx_methode` (`methode_paiement`),
  KEY `idx_statut` (`statut`),
  CONSTRAINT `paiements_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Stores all payment records for candidates';

-- Dumping data for table `paiements`
INSERT INTO `paiements` (`id`, `candidat_id`, `date_paiement`, `montant`, `methode_paiement`, `reference`, `statut`, `notes`, `remise`, `created_at`, `updated_at`) VALUES
(1, 17, '2025-04-06 00:00:00.0', 30.00, 'ESPECES', 'PAY-1641236-32', 'PARTIEL', '', 0.00, '2025-04-06 16:00:42.0', '2025-04-06 16:00:42.0');

-- Table structure for table `reparation`
DROP TABLE IF EXISTS `reparation`;
CREATE TABLE `reparation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicule_id` bigint NOT NULL,
  `facture_id` bigint DEFAULT NULL,
  `description` text NOT NULL,
  `date_reparation` date NOT NULL,
  `cout` decimal(10,2) NOT NULL DEFAULT '0.00',
  `prestataire` varchar(100) DEFAULT NULL,
  `facture_path` varchar(255) DEFAULT NULL,
  `notes` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_vehicule_id` (`vehicule_id`),
  KEY `idx_facture_id` (`facture_id`),
  KEY `idx_date_reparation` (`date_reparation`),
  CONSTRAINT `reparation_ibfk_1` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `reparation`
INSERT INTO `reparation` (`id`, `vehicule_id`, `facture_id`, `description`, `date_reparation`, `cout`, `prestataire`, `facture_path`, `notes`, `created_at`, `updated_at`) VALUES
(1, 1, 1001, 'Remplacement plaquettes de frein', '2024-03-15 00:00:00.0', 180.50, 'Garage Central', NULL, NULL, '2025-04-01 16:25:47.0', '2025-04-01 16:25:47.0'),
(2, 1, 1002, 'Vidange et changement filtres', '2024-01-20 00:00:00.0', 120.00, 'Garage Central', NULL, NULL, '2025-04-01 16:25:47.0', '2025-04-01 17:39:49.0'),
(3, 2, 1003, 'Réparation climatisation', '2024-02-10 00:00:00.0', 350.75, 'Auto Clim', NULL, NULL, '2025-04-01 16:25:47.0', '2025-04-01 16:25:47.0'),
(4, 3, 1004, 'Changement pneus', '2024-03-05 00:00:00.0', 420.00, 'Pneu Express', NULL, NULL, '2025-04-01 16:25:47.0', '2025-04-01 16:25:47.0'),
(5, 4, 1005, 'Remplacement embrayage', '2024-02-15 00:00:00.0', 850.00, 'Garage Poids Lourds', NULL, NULL, '2025-04-01 16:25:47.0', '2025-04-01 17:44:48.0'),
(6, 4, NULL, 'changement des roues', '2022-04-07 00:00:00.0', 50.00, 'garage de medenine', 'documents\reparations\1743526205451_Capture.PNG', '', '2025-04-01 17:50:23.0', '2025-04-01 17:50:23.0');

-- Table structure for table `seance`
DROP TABLE IF EXISTS `seance`;
CREATE TABLE `seance` (
  `id_seance` int NOT NULL AUTO_INCREMENT,
  `id_moniteur` bigint NOT NULL,
  `id_candidat` bigint NOT NULL,
  `id_vehicule` bigint DEFAULT NULL,
  `date_debut` datetime NOT NULL,
  `typeseance` varchar(50) NOT NULL,
  `typepermis` varchar(50) NOT NULL,
  `longtitude` float DEFAULT '0',
  `latitude` float DEFAULT '0',
  `adresse` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_seance`),
  KEY `idx_seance_date` (`date_debut`),
  KEY `idx_seance_moniteur` (`id_moniteur`),
  KEY `idx_seance_candidat` (`id_candidat`),
  KEY `idx_seance_vehicule` (`id_vehicule`),
  KEY `idx_seance_type` (`typeseance`,`typepermis`),
  CONSTRAINT `fk_seance_candidat` FOREIGN KEY (`id_candidat`) REFERENCES `candidat` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_seance_moniteur` FOREIGN KEY (`id_moniteur`) REFERENCES `moniteur` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_seance_vehicule` FOREIGN KEY (`id_vehicule`) REFERENCES `vehicule` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `seance`
INSERT INTO `seance` (`id_seance`, `id_moniteur`, `id_candidat`, `id_vehicule`, `date_debut`, `typeseance`, `typepermis`, `longtitude`, `latitude`, `adresse`) VALUES
(6, 2, 18, 1, '2025-04-03T09:00', 'Conduite', 'Voiture', 10.0268, 36.8184, 'Avenue des Sanhajites, صنهاجة, معتمدية وادي اللیل‬, Manouba, 2021, Tunisia'),
(7, 2, 17, NULL, '2025-04-03T13:00', 'Code', 'Voiture', 0.0, 0.0, ''),
(8, 2, 17, 1, '2025-04-17T08:00', 'Conduite', 'Voiture', 10.1843, 36.7653, 'الوردية, معتمدية الوردية, Tunis, 2066, Tunisia'),
(9, 13, 26, 80, '2025-04-30T09:00', 'Conduite', 'Camion', 10.1877, 36.7929, 'نهج إيطاليا, بحيرة تونس, معتمدية باب بحر, Tunis, 1151, Tunisia'),
(10, 2, 18, 1, '2025-04-06T08:00', 'Conduite', 'Voiture', 10.1906, 36.8154, 'الطريق الوطنية تونس - بنزرت, البحيرة, معتمدية حي الخضراء, Tunis, 1073, Tunisia');

-- Table structure for table `tarifs`
DROP TABLE IF EXISTS `tarifs`;
CREATE TABLE `tarifs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_service` varchar(50) NOT NULL,
  `montant` double NOT NULL,
  `description` text,
  `actif` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_tarifs_type_service` (`type_service`),
  KEY `idx_tarifs_actif` (`actif`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `tarifs`
INSERT INTO `tarifs` (`id`, `type_service`, `montant`, `description`, `actif`) VALUES
(1, 'SEANCE_CODE', 20.0, 'Tarif pour une séance de code', 1),
(2, 'SEANCE_CONDUITE', 40.0, 'Tarif pour une séance de conduite', 1),
(3, 'EXAMEN_CODE', 30.0, 'Tarif pour un examen de code', 1),
(4, 'EXAMEN_CONDUITE', 60.0, 'Tarif pour un examen de conduite', 1);

-- Table structure for table `users`
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` timestamp NULL DEFAULT NULL,
  `active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `users`
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `phone_number`, `created_at`, `last_login`, `active`) VALUES
(1, 'sogut', 'sogut', 'administrateur', 'Administrator', 'zexnull@gmail.com', '92031886', '2025-03-23 07:34:37.0', '2025-04-07 12:52:25.0', 1),
(4, 'lotfi', 'lotfi', 'Secrétaire', 'lotfi rh', 'sogut93720710@gmail.com', '93720710', '2025-03-26 08:25:17.0', '2025-04-07 12:49:06.0', 1);

-- Table structure for table `vehicule`
DROP TABLE IF EXISTS `vehicule`;
CREATE TABLE `vehicule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `marque` varchar(100) NOT NULL,
  `modele` varchar(100) NOT NULL,
  `matricule` varchar(20) NOT NULL,
  `kilometrage` int NOT NULL,
  `type_permis` enum('Moto','Voiture','Camion') NOT NULL,
  `date_mise_en_service` date NOT NULL,
  `date_prochain_entretien` date DEFAULT NULL,
  `date_vignette` date DEFAULT NULL,
  `date_assurance` date DEFAULT NULL,
  `date_visite_technique` date DEFAULT NULL,
  `papiers` text,
  `disponible` tinyint(1) NOT NULL DEFAULT '1',
  `motif_indisponibilite` varchar(255) DEFAULT NULL,
  `notes` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `matricule` (`matricule`),
  KEY `idx_type_permis` (`type_permis`),
  KEY `idx_disponible` (`disponible`),
  KEY `idx_date_vignette` (`date_vignette`),
  KEY `idx_date_assurance` (`date_assurance`),
  KEY `idx_date_visite_technique` (`date_visite_technique`),
  KEY `idx_date_prochain_entretien` (`date_prochain_entretien`)
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `vehicule`
INSERT INTO `vehicule` (`id`, `marque`, `modele`, `matricule`, `kilometrage`, `type_permis`, `date_mise_en_service`, `date_prochain_entretien`, `date_vignette`, `date_assurance`, `date_visite_technique`, `papiers`, `disponible`, `motif_indisponibilite`, `notes`, `created_at`, `updated_at`) VALUES
(1, 'Renault', 'Clio', '123 TUN 4567', 25000, 'Voiture', '2022-01-15 00:00:00.0', '2025-06-30 00:00:00.0', '2025-12-31 00:00:00.0', '2025-10-15 00:00:00.0', '2025-08-20 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-01 16:08:22.0', '2025-04-01 16:08:22.0'),
(2, 'Peugeot', '208', '456 TUN 7890', 18500, 'Voiture', '2023-03-10 00:00:00.0', '2025-09-15 00:00:00.0', '2025-11-30 00:00:00.0', '2025-09-22 00:00:00.0', '2025-07-18 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-01 16:08:22.0', '2025-04-01 16:08:22.0'),
(3, 'Honda', 'CBR', '789 TUN 1234', 12001, 'Moto', '2023-05-20 00:00:00.0', '2025-08-10 00:00:00.0', '2025-10-25 00:00:00.0', '2025-05-01 00:00:00.0', '2025-06-15 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-01 16:08:22.0', '2025-04-02 09:07:19.0'),
(4, 'Mercedes', 'Actros', '321 TUN 6543', 45000, 'Camion', '2021-11-05 00:00:00.0', '2025-04-26 00:00:00.0', '2025-04-05 00:00:00.0', '2025-07-25 00:00:00.0', '2025-03-01 00:00:00.0', NULL, 0, NULL, NULL, '2025-04-01 16:08:22.0', '2025-04-02 09:05:43.0'),
(74, 'Toyota', 'Yaris', '345 TUN 6789', 22000, 'Voiture', '2022-08-15 00:00:00.0', '2025-08-15 00:00:00.0', '2025-12-15 00:00:00.0', '2025-08-15 00:00:00.0', '2025-10-15 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(75, 'Ford', 'Fiesta', '678 TUN 9012', 18000, 'Voiture', '2023-02-10 00:00:00.0', '2025-08-10 00:00:00.0', '2025-12-10 00:00:00.0', '2025-07-10 00:00:00.0', '2025-09-10 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(76, 'Suzuki', 'Swift', '901 TUN 2345', 15000, 'Voiture', '2023-05-20 00:00:00.0', '2025-11-20 00:00:00.0', '2026-01-20 00:00:00.0', '2025-10-20 00:00:00.0', '2025-12-20 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(77, 'Kawasaki', 'Z650', '234 TUN 5678', 5000, 'Moto', '2023-09-05 00:00:00.0', '2025-09-05 00:00:00.0', '2025-12-05 00:00:00.0', '2025-09-05 00:00:00.0', '2025-11-05 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(78, 'Ducati', 'Monster', '567 TUN 8901', 3500, 'Moto', '2024-01-15 00:00:00.0', '2025-07-15 00:00:00.0', '2025-12-15 00:00:00.0', '2025-07-15 00:00:00.0', '2025-09-15 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(79, 'BMW', 'F800', '890 TUN 1234', 7000, 'Moto', '2023-06-10 00:00:00.0', '2025-06-10 00:00:00.0', '2025-12-10 00:00:00.0', '2025-06-10 00:00:00.0', '2025-08-10 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(80, 'Scania', 'R450', '123 TUN 4568', 50000, 'Camion', '2022-03-20 00:00:00.0', '2025-03-20 00:00:00.0', '2025-12-20 00:00:00.0', '2025-03-20 00:00:00.0', '2025-05-20 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(81, 'Volvo', 'FH16', '456 TUN 7891', 65000, 'Camion', '2021-11-10 00:00:00.0', '2025-05-10 00:00:00.0', '2025-11-10 00:00:00.0', '2025-05-10 00:00:00.0', '2025-07-10 00:00:00.0', NULL, 1, NULL, NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(82, 'Citroen', 'C3', '789 TUN 0123', 28000, 'Voiture', '2022-04-25 00:00:00.0', '2025-04-25 00:00:00.0', '2025-10-25 00:00:00.0', '2025-04-25 00:00:00.0', '2025-06-25 00:00:00.0', NULL, 0, 'En réparation', NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(83, 'Hyundai', 'i20', '012 TUN 3456', 32000, 'Voiture', '2021-09-15 00:00:00.0', '2025-03-15 00:00:00.0', '2025-09-15 00:00:00.0', '2025-03-15 00:00:00.0', '2025-05-15 00:00:00.0', NULL, 0, 'Problème moteur', NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(84, 'Yamaha', 'MT-09', '345 TUN 6781', 9000, 'Moto', '2022-12-05 00:00:00.0', '2025-06-05 00:00:00.0', '2025-12-05 00:00:00.0', '2025-06-05 00:00:00.0', '2025-08-05 00:00:00.0', NULL, 0, 'Révision complète', NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(85, 'KTM', 'Duke', '678 TUN 9017', 6500, 'Moto', '2023-03-20 00:00:00.0', '2025-09-20 00:00:00.0', '2026-03-20 00:00:00.0', '2025-09-20 00:00:00.0', '2025-11-20 00:00:00.0', NULL, 0, 'Changement embrayage', NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(86, 'MAN', 'TGX', '901 TUN 2343', 75000, 'Camion', '2021-06-10 00:00:00.0', '2025-06-10 00:00:00.0', '2025-06-10 00:00:00.0', '2025-06-10 00:00:00.0', '2025-08-10 00:00:00.0', NULL, 0, 'Problème boîte de vitesses', NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(87, 'DAF', 'XF', '234 TUN 5670', 80000, 'Camion', '2020-10-15 00:00:00.0', '2025-04-15 00:00:00.0', '2025-10-15 00:00:00.0', '2025-04-15 00:00:00.0', '2025-06-15 00:00:00.0', NULL, 0, 'Révision complète', NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0'),
(88, 'Fiat', 'Tipo', '567 TUN 8951', 40000, 'Voiture', '2021-05-01 00:00:00.0', '2025-05-01 00:00:00.0', '2025-11-01 00:00:00.0', '2025-05-01 00:00:00.0', '2025-07-01 00:00:00.0', NULL, 0, 'Accident', NULL, '2025-04-03 13:17:05.0', '2025-04-03 13:17:05.0');

-- Table structure for table `verification_codes`
DROP TABLE IF EXISTS `verification_codes`;
CREATE TABLE `verification_codes` (
  `user_id` int NOT NULL,
  `code` varchar(10) NOT NULL,
  `expiry_time` timestamp NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `verification_codes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `verification_codes`

