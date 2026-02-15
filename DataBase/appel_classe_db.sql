CREATE DATABASE  IF NOT EXISTS `appel_classe_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `appel_classe_db`;
-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: appel_classe_db
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `administrateur`
--

DROP TABLE IF EXISTS `administrateur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `administrateur` (
  `niveau_acces` varchar(255) DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`utilisateur_id`),
  CONSTRAINT `FKap8vk068oh8c0qxcjbdsoewd0` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `administrateur`
--

LOCK TABLES `administrateur` WRITE;
/*!40000 ALTER TABLE `administrateur` DISABLE KEYS */;
/*!40000 ALTER TABLE `administrateur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `appel`
--

DROP TABLE IF EXISTS `appel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date_heure_debut` datetime(6) DEFAULT NULL,
  `date_heure_fin` datetime(6) DEFAULT NULL,
  `id_appel` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL,
  `seance_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKck1atj7mjv6hotaq2jw21bogl` (`seance_id`),
  CONSTRAINT `FKck1atj7mjv6hotaq2jw21bogl` FOREIGN KEY (`seance_id`) REFERENCES `seance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appel`
--

LOCK TABLES `appel` WRITE;
/*!40000 ALTER TABLE `appel` DISABLE KEYS */;
/*!40000 ALTER TABLE `appel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assistant_pedagogique`
--

DROP TABLE IF EXISTS `assistant_pedagogique`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assistant_pedagogique` (
  `departement` varchar(255) DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`utilisateur_id`),
  CONSTRAINT `FK1h6kniagyxvc8ay9vhp5x0i2e` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assistant_pedagogique`
--

LOCK TABLES `assistant_pedagogique` WRITE;
/*!40000 ALTER TABLE `assistant_pedagogique` DISABLE KEYS */;
/*!40000 ALTER TABLE `assistant_pedagogique` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classe`
--

DROP TABLE IF EXISTS `classe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classe` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `annee_academique` varchar(255) DEFAULT NULL,
  `id_classe` varchar(255) DEFAULT NULL,
  `niveau` varchar(255) DEFAULT NULL,
  `nom_classe` varchar(255) DEFAULT NULL,
  `emploi_du_temps_id` bigint DEFAULT NULL,
  `etablissement` bigint DEFAULT NULL,
  `filiere_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm5j80kxclcjkyko2pf2j8s594` (`emploi_du_temps_id`),
  KEY `FK6wfy7wfb6nxr3abenj5d0kuwg` (`etablissement`),
  KEY `FK5dlr7nj4ia58qex7s483y59l6` (`filiere_id`),
  CONSTRAINT `FK5dlr7nj4ia58qex7s483y59l6` FOREIGN KEY (`filiere_id`) REFERENCES `filiere` (`id`),
  CONSTRAINT `FK6wfy7wfb6nxr3abenj5d0kuwg` FOREIGN KEY (`etablissement`) REFERENCES `etablissement` (`id`),
  CONSTRAINT `FKcy4aok5e2jg9mq7uayl69948e` FOREIGN KEY (`emploi_du_temps_id`) REFERENCES `emploi_du_temps` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classe`
--

LOCK TABLES `classe` WRITE;
/*!40000 ALTER TABLE `classe` DISABLE KEYS */;
/*!40000 ALTER TABLE `classe` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classe_cours`
--

DROP TABLE IF EXISTS `classe_cours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classe_cours` (
  `classe_id` bigint NOT NULL,
  `cours_id` bigint NOT NULL,
  KEY `FKgmdswbv88tbkhblsdcjpiidio` (`cours_id`),
  KEY `FKp3dop16rbl46nfarunijty274` (`classe_id`),
  CONSTRAINT `FKgmdswbv88tbkhblsdcjpiidio` FOREIGN KEY (`cours_id`) REFERENCES `cours_ue` (`id`),
  CONSTRAINT `FKp3dop16rbl46nfarunijty274` FOREIGN KEY (`classe_id`) REFERENCES `classe` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classe_cours`
--

LOCK TABLES `classe_cours` WRITE;
/*!40000 ALTER TABLE `classe_cours` DISABLE KEYS */;
/*!40000 ALTER TABLE `classe_cours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cours_ue`
--

DROP TABLE IF EXISTS `cours_ue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cours_ue` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codeue` varchar(255) DEFAULT NULL,
  `id_cours` varchar(255) DEFAULT NULL,
  `intitule` varchar(255) DEFAULT NULL,
  `volume_horaire` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cours_ue`
--

LOCK TABLES `cours_ue` WRITE;
/*!40000 ALTER TABLE `cours_ue` DISABLE KEYS */;
/*!40000 ALTER TABLE `cours_ue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emploi_du_temps`
--

DROP TABLE IF EXISTS `emploi_du_temps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emploi_du_temps` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creneaux` varchar(255) DEFAULT NULL,
  `id_emploi` varchar(255) DEFAULT NULL,
  `periode` varchar(255) DEFAULT NULL,
  `gestionnaire_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaabqxmnsvcfowv50gcqygr0o3` (`gestionnaire_id`),
  CONSTRAINT `FKaabqxmnsvcfowv50gcqygr0o3` FOREIGN KEY (`gestionnaire_id`) REFERENCES `assistant_pedagogique` (`utilisateur_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emploi_du_temps`
--

LOCK TABLES `emploi_du_temps` WRITE;
/*!40000 ALTER TABLE `emploi_du_temps` DISABLE KEYS */;
/*!40000 ALTER TABLE `emploi_du_temps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enseignant`
--

DROP TABLE IF EXISTS `enseignant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enseignant` (
  `grade` varchar(255) DEFAULT NULL,
  `specialite` varchar(255) DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`utilisateur_id`),
  CONSTRAINT `FKtow190o3rq4pjniqv1lef49rr` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enseignant`
--

LOCK TABLES `enseignant` WRITE;
/*!40000 ALTER TABLE `enseignant` DISABLE KEYS */;
/*!40000 ALTER TABLE `enseignant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enseignant_cours`
--

DROP TABLE IF EXISTS `enseignant_cours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enseignant_cours` (
  `enseignant_id` bigint NOT NULL,
  `cours_id` bigint NOT NULL,
  KEY `FKo1w2i2py52wq7m9o7h4psgu9d` (`cours_id`),
  KEY `FK4i3ubyyl4kla67crl1e8p1dol` (`enseignant_id`),
  CONSTRAINT `FK4i3ubyyl4kla67crl1e8p1dol` FOREIGN KEY (`enseignant_id`) REFERENCES `enseignant` (`utilisateur_id`),
  CONSTRAINT `FKo1w2i2py52wq7m9o7h4psgu9d` FOREIGN KEY (`cours_id`) REFERENCES `cours_ue` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enseignant_cours`
--

LOCK TABLES `enseignant_cours` WRITE;
/*!40000 ALTER TABLE `enseignant_cours` DISABLE KEYS */;
/*!40000 ALTER TABLE `enseignant_cours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `etablissement`
--

DROP TABLE IF EXISTS `etablissement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `etablissement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `annee_academique_courante` varchar(255) DEFAULT NULL,
  `email_contact` varchar(255) DEFAULT NULL,
  `id_etablissement` varchar(255) DEFAULT NULL,
  `nom_etablissement` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `etablissement`
--

LOCK TABLES `etablissement` WRITE;
/*!40000 ALTER TABLE `etablissement` DISABLE KEYS */;
/*!40000 ALTER TABLE `etablissement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `etudiant`
--

DROP TABLE IF EXISTS `etudiant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `etudiant` (
  `date_naissance` varchar(255) DEFAULT NULL,
  `matricule` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  `classe_id` bigint DEFAULT NULL,
  PRIMARY KEY (`utilisateur_id`),
  KEY `FK4f9xvhnh2rx4gl5l1sbrtflmh` (`classe_id`),
  CONSTRAINT `FK4f9xvhnh2rx4gl5l1sbrtflmh` FOREIGN KEY (`classe_id`) REFERENCES `classe` (`id`),
  CONSTRAINT `FKk1k5fhsjak7v0uk031i5geqj9` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `etudiant`
--

LOCK TABLES `etudiant` WRITE;
/*!40000 ALTER TABLE `etudiant` DISABLE KEYS */;
/*!40000 ALTER TABLE `etudiant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `filiere`
--

DROP TABLE IF EXISTS `filiere`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `filiere` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `id_filiere` varchar(255) DEFAULT NULL,
  `nom_filiere` varchar(255) DEFAULT NULL,
  `etablissement_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK18y5i45gjdm02dha3vwutfgv3` (`etablissement_id`),
  CONSTRAINT `FK18y5i45gjdm02dha3vwutfgv3` FOREIGN KEY (`etablissement_id`) REFERENCES `etablissement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `filiere`
--

LOCK TABLES `filiere` WRITE;
/*!40000 ALTER TABLE `filiere` DISABLE KEYS */;
/*!40000 ALTER TABLE `filiere` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `justificatif`
--

DROP TABLE IF EXISTS `justificatif`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `justificatif` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date_soumission` date DEFAULT NULL,
  `fichier` varchar(255) DEFAULT NULL,
  `id_justificatif` varchar(255) DEFAULT NULL,
  `motif` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL,
  `document_url` varchar(255) DEFAULT NULL,
  `statut_validation` varchar(255) DEFAULT NULL,
  `presence_id` bigint DEFAULT NULL,
  `validateur_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs4ap2phtkkge6u50qxstjhyo4` (`presence_id`),
  KEY `FK2p2n02vurr6pbh1cd5dwj3k2t` (`validateur_id`),
  CONSTRAINT `FK2p2n02vurr6pbh1cd5dwj3k2t` FOREIGN KEY (`validateur_id`) REFERENCES `assistant_pedagogique` (`utilisateur_id`),
  CONSTRAINT `FK7ymewtadw0iyekb78x0emhdch` FOREIGN KEY (`presence_id`) REFERENCES `presence` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `justificatif`
--

LOCK TABLES `justificatif` WRITE;
/*!40000 ALTER TABLE `justificatif` DISABLE KEYS */;
/*!40000 ALTER TABLE `justificatif` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `destinataire` varchar(255) DEFAULT NULL,
  `id_notification` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parent`
--

DROP TABLE IF EXISTS `parent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parent` (
  `enfants_associes` varchar(255) DEFAULT NULL,
  `lien_parental` varchar(255) DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`utilisateur_id`),
  CONSTRAINT `FKnt7w406eout30y7t69p27anj5` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parent`
--

LOCK TABLES `parent` WRITE;
/*!40000 ALTER TABLE `parent` DISABLE KEYS */;
/*!40000 ALTER TABLE `parent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parent_etudiant`
--

DROP TABLE IF EXISTS `parent_etudiant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parent_etudiant` (
  `parent_id` bigint NOT NULL,
  `etudiant_id` bigint NOT NULL,
  KEY `FK4gix4k8pcbsfyet4b0fbv9bio` (`etudiant_id`),
  KEY `FKseul2ypm9yfubbwrptjff49mq` (`parent_id`),
  CONSTRAINT `FK4gix4k8pcbsfyet4b0fbv9bio` FOREIGN KEY (`etudiant_id`) REFERENCES `etudiant` (`utilisateur_id`),
  CONSTRAINT `FKseul2ypm9yfubbwrptjff49mq` FOREIGN KEY (`parent_id`) REFERENCES `parent` (`utilisateur_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parent_etudiant`
--

LOCK TABLES `parent_etudiant` WRITE;
/*!40000 ALTER TABLE `parent_etudiant` DISABLE KEYS */;
/*!40000 ALTER TABLE `parent_etudiant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(500) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpnvtwliis6p05pn6i3ndjrqt2` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permissions`
--

LOCK TABLES `permissions` WRITE;
/*!40000 ALTER TABLE `permissions` DISABLE KEYS */;
INSERT INTO `permissions` VALUES (1,'Voir les cours','COURSE_VIEW'),(2,'Gérer les cours','COURSE_MANAGE'),(3,'Voir les utilisateurs','USER_VIEW'),(4,'Gérer les utilisateurs','USER_MANAGE'),(5,'Voir les étudiants','STUDENT_VIEW'),(6,'Voir les présences','ATTENDANCE_VIEW'),(7,'Gérer les présences','ATTENDANCE_MANAGE'),(8,'Voir les notes','GRADE_VIEW'),(9,'Soumettre les devoirs','ASSIGNMENT_SUBMIT'),(10,'Valider les justificatifs','JUSTIFICATION_VALIDATE'),(11,'Voir les statistiques','STATISTICS_VIEW');
/*!40000 ALTER TABLE `permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presence`
--

DROP TABLE IF EXISTS `presence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `horodatage` datetime(6) DEFAULT NULL,
  `id_presence` varchar(255) DEFAULT NULL,
  `localisation` varchar(255) DEFAULT NULL,
  `mode_enregistrement` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL,
  `appel_id` bigint DEFAULT NULL,
  `etudiant_id` bigint DEFAULT NULL,
  `seance_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKga6h3ww1n2pts9o7e64g2udg6` (`appel_id`),
  KEY `FKksx89i2kcy5ydha9cl7jgovbs` (`etudiant_id`),
  KEY `FKrilwqu23ivg6slm088wnmqbvh` (`seance_id`),
  CONSTRAINT `FKga6h3ww1n2pts9o7e64g2udg6` FOREIGN KEY (`appel_id`) REFERENCES `appel` (`id`),
  CONSTRAINT `FKksx89i2kcy5ydha9cl7jgovbs` FOREIGN KEY (`etudiant_id`) REFERENCES `etudiant` (`utilisateur_id`),
  CONSTRAINT `FKrilwqu23ivg6slm088wnmqbvh` FOREIGN KEY (`seance_id`) REFERENCES `seance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presence`
--

LOCK TABLES `presence` WRITE;
/*!40000 ALTER TABLE `presence` DISABLE KEYS */;
/*!40000 ALTER TABLE `presence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permissions` (
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `FKegdk29eiy7mdtefy5c7eirr6e` (`permission_id`),
  CONSTRAINT `FKegdk29eiy7mdtefy5c7eirr6e` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  CONSTRAINT `FKn5fotdgk8d1xvo8nav9uv3muc` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role_permissions`
--

LOCK TABLES `role_permissions` WRITE;
/*!40000 ALTER TABLE `role_permissions` DISABLE KEYS */;
INSERT INTO `role_permissions` VALUES (1,1),(2,1),(3,1),(4,1),(1,2),(2,2),(1,3),(1,4),(1,5),(2,5),(4,5),(5,5),(1,6),(2,6),(3,6),(4,6),(5,6),(1,7),(2,7),(5,7),(1,8),(2,8),(3,8),(3,9),(1,10),(4,10),(1,11);
/*!40000 ALTER TABLE `role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(500) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Administrateur système','ADMIN'),(2,'Enseignant','ENSEIGNANT'),(3,'Étudiant','ETUDIANT'),(4,'Assistant pédagogique','ASSISTANT_PEDAGOGIQUE'),(5,'Surveillant','SURVEILLANT');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seance`
--

DROP TABLE IF EXISTS `seance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `heure_debut` time(6) DEFAULT NULL,
  `heure_fin` time(6) DEFAULT NULL,
  `id_seance` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL,
  `cours_id` bigint DEFAULT NULL,
  `emploi_du_temps_id` bigint DEFAULT NULL,
  `enseignant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrfxjmrcsbq5xs26jdghgywmp0` (`cours_id`),
  KEY `FKc7wj666ypniqwg385sq9mxv6f` (`emploi_du_temps_id`),
  KEY `FK2o5dfw98jhfa4jcarkc6apqh2` (`enseignant_id`),
  CONSTRAINT `FK2o5dfw98jhfa4jcarkc6apqh2` FOREIGN KEY (`enseignant_id`) REFERENCES `enseignant` (`utilisateur_id`),
  CONSTRAINT `FKc7wj666ypniqwg385sq9mxv6f` FOREIGN KEY (`emploi_du_temps_id`) REFERENCES `emploi_du_temps` (`id`),
  CONSTRAINT `FKrfxjmrcsbq5xs26jdghgywmp0` FOREIGN KEY (`cours_id`) REFERENCES `cours_ue` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seance`
--

LOCK TABLES `seance` WRITE;
/*!40000 ALTER TABLE `seance` DISABLE KEYS */;
/*!40000 ALTER TABLE `seance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seance_surveillant`
--

DROP TABLE IF EXISTS `seance_surveillant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seance_surveillant` (
  `seance_id` bigint NOT NULL,
  `surveillant_id` bigint NOT NULL,
  KEY `FK4jj412ulrc3y8ja3ip5wj5ab` (`surveillant_id`),
  KEY `FK9nackreu8fr0d8o4bp5vfo3qx` (`seance_id`),
  CONSTRAINT `FK4jj412ulrc3y8ja3ip5wj5ab` FOREIGN KEY (`surveillant_id`) REFERENCES `surveillant` (`utilisateur_id`),
  CONSTRAINT `FK9nackreu8fr0d8o4bp5vfo3qx` FOREIGN KEY (`seance_id`) REFERENCES `seance` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seance_surveillant`
--

LOCK TABLES `seance_surveillant` WRITE;
/*!40000 ALTER TABLE `seance_surveillant` DISABLE KEYS */;
/*!40000 ALTER TABLE `seance_surveillant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `surveillant`
--

DROP TABLE IF EXISTS `surveillant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `surveillant` (
  `affectation` varchar(255) DEFAULT NULL,
  `utilisateur_id` bigint NOT NULL,
  PRIMARY KEY (`utilisateur_id`),
  CONSTRAINT `FK7tkbuyj22iodoe9fn6vfhi5o4` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `surveillant`
--

LOCK TABLES `surveillant` WRITE;
/*!40000 ALTER TABLE `surveillant` DISABLE KEYS */;
/*!40000 ALTER TABLE `surveillant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateur` (
  `type_utilisateur` varchar(31) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `prenom` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `statut` varchar(255) DEFAULT NULL,
  `account_non_locked` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `premiere_connexion` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateur`
--

LOCK TABLES `utilisateur` WRITE;
/*!40000 ALTER TABLE `utilisateur` DISABLE KEYS */;
INSERT INTO `utilisateur` VALUES ('Utilisateur',1,'','','','',NULL,'ACTIF',_binary '','2026-02-14 13:15:06.507174',_binary '',NULL),('Utilisateur',2,'mbidayohan33@gmail.com','Mbida','mot2passe@','Yohan',NULL,'ACTIF',_binary '','2026-02-14 13:57:57.699999',_binary '',NULL);
/*!40000 ALTER TABLE `utilisateur` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `utilisateur_roles`
--

DROP TABLE IF EXISTS `utilisateur_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `utilisateur_roles` (
  `utilisateur_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`utilisateur_id`,`role_id`),
  KEY `FKpvb9dvcn9icrukism9we0knub` (`role_id`),
  CONSTRAINT `FK9lop304xtodorgho9w56lpjhn` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateur` (`id`),
  CONSTRAINT `FKpvb9dvcn9icrukism9we0knub` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `utilisateur_roles`
--

LOCK TABLES `utilisateur_roles` WRITE;
/*!40000 ALTER TABLE `utilisateur_roles` DISABLE KEYS */;
/*!40000 ALTER TABLE `utilisateur_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'appel_classe_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-14 23:19:30
