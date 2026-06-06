-- Script d'altération de la base de données pour la gestion des assistants pédagogiques
-- Ce script ajoute la colonne `is_tronc_commun` à la table `filiere`
-- et la colonne `ordre` à la table `niveau`.

USE `appel_classe_db`;

-- 1. Ajout de la colonne is_tronc_commun à la table filiere
ALTER TABLE `filiere` ADD COLUMN `is_tronc_commun` TINYINT(1) NOT NULL DEFAULT 0;

-- 2. Ajout de la colonne ordre à la table niveau
-- Note : Si la table `niveau` n'existe pas encore sous ce nom exact, JPA la créera via ddl-auto=update.
-- Si la table existe déjà, ce script y ajoute la colonne d'ordre.
ALTER TABLE `niveau` ADD COLUMN `ordre` INT NOT NULL DEFAULT 1;
