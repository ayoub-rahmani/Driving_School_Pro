-- Java-based MySQL backup
-- Generated on: Wed Mar 26 17:45:47 CET 2025
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
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
(44, 'CREATE', 'BACKUP', NULL, 'Database backup created by sogut: driving_school_2025-03-26_17-44-59.sql', 1, 'sogut', '2025-03-26 17:44:59.0'),
(45, 'DELETE', 'CANDIDAT', 16, 'Suppression du candidat Ben Ali Ahmed', 1, 'sogut', '2025-03-26 17:45:16.0'),
(46, 'DELETE', 'CANDIDAT', 17, 'Suppression du candidat Haddad Fatma', 1, 'sogut', '2025-03-26 17:45:25.0');

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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `candidat`
INSERT INTO `candidat` (`id`, `nom`, `prenom`, `cin`, `date_naissance`, `telephone`, `adresse`, `email`, `date_inscription`, `categories_permis`, `chemin_photo_cin`, `chemin_photo_identite`, `chemin_certificat_medical`, `chemin_fiche_pdf`, `chemin_fiche_png`, `actif`) VALUES
(18, 'Trabelsi', 'Mohamed', '11223344', '1987-11-10 00:00:00.0', '98112231', 'Cité Olympique, Radès', 'mohamed.trabelsi@example.com', '2023-06-29 00:00:00.0', 'C', NULL, 'documentscandidats11223344_PHOTO_1742803806328.jpg', NULL, 'documents/generated/Fiche_Candidat_20250324.pdf', NULL, 1),
(19, 'Bouazzi', 'Sana', '14321055', '1995-06-05 00:00:00.0', '22556677', 'Route de la Marsa, Tunis', 'sana.bouazzi@example.com', '2024-07-22 00:00:00.0', 'B,C', NULL, 'documentscandidats14321055_PHOTO_1742935804563.jpg', NULL, 'documents/generated/Fiche_Candidat_20250325.pdf', NULL, 1),
(20, 'Jebalii', 'Khaled', '00001122', '1983-09-28 00:00:00.0', '71990011', 'Rue Charles de Gaulle, Bizerte', 'khaled.jebali@example.com', '2023-07-06 00:00:00.0', 'A', NULL, NULL, NULL, NULL, NULL, 1),
(21, 'Mrad', 'Leila', '13445566', '1998-02-14 00:00:00.0', '50334455', 'Avenue Kennedy, Sfax', 'leila.mrad@example.com', '2024-08-05 00:00:00.0', 'B', NULL, 'documentscandidats33445566_PHOTO_1742402042659.jpg', NULL, 'documents/generated/Fiche_Candidat_20250319.pdf', NULL, 0),
(22, 'Khlifi', 'Youssef', '17889900', '1991-05-01 00:00:00.0', '25778899', 'Rue Ibn Khaldoun, Kairouan', 'youssef.khlifi@example.com', '2024-08-12 00:00:00.0', 'A,B,C', NULL, NULL, NULL, 'documents/generated/Fiche_Candidat_20250321.pdf', NULL, 1),
(23, 'Ben Salem', 'Imen', '04556677', '1985-12-18 00:00:00.0', '05445566', 'Avenue 7 Novembre, Gabès', 'imen.bensalem@example.com', '2024-08-19 00:00:00.0', 'C', NULL, NULL, NULL, NULL, NULL, 1),
(26, 'Ben Amor', 'Walid', '66778899', '1989-10-25 00:00:00.0', '24667788', 'Rue Mongi Slim, Monastir', 'walid.benamor@example.com', '2024-09-09 00:00:00.0', 'A', NULL, NULL, NULL, NULL, NULL, 0),
(27, 'Dhaouadi', 'Nadia', '00112233', '1986-01-30 00:00:00.0', '51001122', 'Avenue de Carthage, Tunis', 'nadia.dhaouadi@example.com', '2024-09-16 00:00:00.0', 'B', NULL, NULL, NULL, NULL, NULL, 1),
(28, 'Gharbi', 'Fares', '12340987', '1994-09-17 00:00:00.0', '98123409', 'Route Sidi Dhrif, Sidi Bou Said', 'fares.gharbi@example.com', '2024-09-23 00:00:00.0', 'B,C', NULL, NULL, NULL, NULL, NULL, 0),
(29, 'Ayari', 'Rania', '11223355', '1991-06-02 00:00:00.0', '71456712', 'Avenue Farhat Hached, Kasserine', 'rania.ayari@example.com', '2024-09-30 00:00:00.0', 'B', NULL, NULL, NULL, 'documents/generated/Fiche_Candidat_20250319.pdf', NULL, 1);

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
  PRIMARY KEY (`id`),
  KEY `candidat_id` (`candidat_id`),
  CONSTRAINT `examen_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `examen`
INSERT INTO `examen` (`id`, `candidat_id`, `type_examen`, `date_examen`, `lieu_examen`, `frais_inscription`, `est_valide`) VALUES
(13, 27, 'Code', '2026-03-13 00:00:00.0', 'ygugy', 3300.0, 1),
(14, 27, 'Conduite', '2026-03-07 00:00:00.0', 'hjgjh', 3200.0, 0);

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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `moniteur`
INSERT INTO `moniteur` (`id`, `nom`, `prenom`, `cin`, `date_naissance`, `telephone`, `date_embauche`, `date_fin_contrat`, `num_permis`, `categories_permis`, `disponible`, `motif`, `salaire`, `experience`, `diplomes`, `notes`) VALUES
(2, 'Lambert', 'Paul', '07890123', '1985-02-20 00:00:00.0', '55577881', '2020-07-01 00:00:00.0', '2025-06-30 00:00:00.0', 'P202012345', 'A,B', 1, 'Fin de formation', 2500.0, 5.0, 'BEPECASER', 'Moniteur expérimenté et pédagogue'),
(3, 'Chevalier', 'Sophie', '12109876', '1982-09-14 00:00:00.0', '55599001', '2021-01-15 00:00:00.0', '2026-01-14 00:00:00.0', 'P202154321', 'B,C', 0, 'Mutation', 2800.0, 8.0, 'BEPECASER, Mention deux-roues', 'Spécialiste de la conduite en ville'),
(4, 'Leroy', 'David', '18901234', '1988-04-03 00:00:00.0', '55511222', '2022-03-04 00:00:00.0', '2016-02-04 00:00:00.0', 'P202298765', 'A', 0, 'Congé maternité', 2200.0, 3.0, 'BEPECASER', 'En congé de maternité jusqu''en septembre'),
(5, 'Girard', 'Céline', '03210987', '1980-11-27 00:00:00.0', '55533442', '2019-05-20 00:00:00.0', '2024-05-19 00:00:00.0', 'P201934567', 'B', 0, 'Déménagement', 3000.0, 10.0, 'BEPECASER, CS moto', 'Monitrice passionnée par la sécurité routière'),
(6, 'Dupuis', 'Thomas', '19012345', '1986-07-08 00:00:00.0', '55555665', '2023-09-01 00:00:00.0', '2020-08-06 00:00:00.0', 'P202376543', 'A,B,C', 0, '', 2700.0, 0.0, '', ''),
(7, 'Fournier', 'Isabelle', '14321098', '1983-03-16 00:00:00.0', '55577883', '2020-11-01 00:00:00.0', '2025-10-31 00:00:00.0', 'P202023456', 'C', 0, 'Arrêt maladie', 2400.0, 4.0, 'BEPECASER', 'En arrêt maladie depuis juillet'),
(8, 'Moreau', 'Antoine', '00123456', '1990-08-10 00:00:00.0', '55599008', '2021-05-15 00:00:00.0', '2026-05-14 00:00:00.0', 'P202165432', 'B', 1, 'Formation', 2900.0, 9.0, 'BEPECASER', 'Suit une formation complémentaire sur les nouvelles technologies'),
(9, 'Lemaire', 'Julie', '12345098', '1981-01-05 00:00:00.0', '55511221', '2022-07-01 00:00:00.0', '2027-06-30 00:00:00.0', 'P202245678', 'A,B', 1, 'Vacances', 2300.0, 2.0, 'BEPECASER', 'En vacances jusqu''à fin août'),
(10, 'Rousseau', 'Sébastien', '14325555', '1987-06-12 00:00:00.0', '55533447', '2018-09-20 00:00:00.0', '2023-09-19 00:00:00.0', 'P201887654', 'B,C', 1, 'Décès', 3100.0, 11.0, 'BEPECASER, Mention simulateur', 'Spécialiste de la formation sur simulateur'),
(11, 'Vincent', 'Aurélie', '34567891', '1984-05-01 00:00:00.0', '91333566', '2023-01-15 00:00:00.0', '2028-01-14 00:00:00.0', 'P202310987', 'A,C', 1, 'autre motif', 2600.0, 5.0, 'BEPECASER', 'Monitrice attentive et à l''écoute');

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
(1, 'sogut', 'sogut', 'administrateur', 'Administrator', 'zexnull@gmail.com', '92031886', '2025-03-23 07:34:37.0', '2025-03-26 17:44:38.0', 1),
(4, 'lotfi', 'lotfi', 'Secrétaire', 'lotfi rh', 'sogut93720710@gmail.com', '93720710', '2025-03-26 08:25:17.0', '2025-03-26 17:44:13.0', 1);

-- Table structure for table `vehicule`
DROP TABLE IF EXISTS `vehicule`;
CREATE TABLE `vehicule` (
  `id` int NOT NULL,
  `marque` varchar(300) DEFAULT NULL,
  `modele` varchar(300) DEFAULT NULL,
  `matricule` varchar(300) DEFAULT NULL,
  `type_permis` varchar(300) DEFAULT NULL,
  `kilometerage` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping data for table `vehicule`
INSERT INTO `vehicule` (`id`, `marque`, `modele`, `matricule`, `type_permis`, `kilometerage`) VALUES
(1, '1', '11', '1', 'Voiture', 0),
(11, '1', '11', '1', 'Voiture', 0),
(13, '1', '11', '1', 'Voiture', 0),
(17, '11', '11', '1', 'Voiture', 0),
(19, '11', '11', '1', 'Voiture', 0),
(198, '1', '11', '1', 'Voiture', 0),
(1987, '1', '11', '1', 'Voiture', 0),
(1988, '1', '11', '1', 'Voiture', 0),
(198745, '55', '11', '1', 'Voiture', 0),
(1987455, '55', '77', '1', 'Voiture', 0),
(19874555, '55', '77', '1', 'Voiture', 0);

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

