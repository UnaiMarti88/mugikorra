-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 192.168.1.10    Database: tpv
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `erabiltzaileak`
--

DROP TABLE IF EXISTS `erabiltzaileak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erabiltzaileak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `erabiltzailea` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `pasahitza` varchar(255) NOT NULL,
  `rola_id` int NOT NULL,
  `ezabatua` tinyint DEFAULT '0',
  `chat` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `rola_id` (`rola_id`),
  KEY `rola_id_2` (`rola_id`),
  CONSTRAINT `erabiltzaileak_ibfk_1` FOREIGN KEY (`rola_id`) REFERENCES `rolak` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `FK_B553DCB6` FOREIGN KEY (`rola_id`) REFERENCES `rolak` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erabiltzaileak`
--

LOCK TABLES `erabiltzaileak` WRITE;
/*!40000 ALTER TABLE `erabiltzaileak` DISABLE KEYS */;
INSERT INTO `erabiltzaileak` VALUES (1,'Aitor','aitor@example.com','1234',1,0,0),(2,'Jon','jon@example.com','1234',2,0,0),(3,'fufy','s','1223',2,1,0),(4,'ff','ff','1234',1,1,0),(5,'ed','dede','aitor',2,1,1),(6,'wf','ere@','1233',2,0,0),(7,'Admin','Admin@gmail','12',1,0,1),(8,'Unai','Una@asa','1234',2,0,1);
/*!40000 ALTER TABLE `erabiltzaileak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eskaera_mahaiak`
--

DROP TABLE IF EXISTS `eskaera_mahaiak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eskaera_mahaiak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `eskaera_id` int NOT NULL,
  `mahaia_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_eskaera_mahaia` (`eskaera_id`,`mahaia_id`),
  KEY `fk_eskaera_mahaiak_mahaia` (`mahaia_id`),
  CONSTRAINT `fk_eskaera_mahaiak_eskaera` FOREIGN KEY (`eskaera_id`) REFERENCES `eskaerak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_eskaera_mahaiak_mahaia` FOREIGN KEY (`mahaia_id`) REFERENCES `mahaiak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eskaera_mahaiak`
--

LOCK TABLES `eskaera_mahaiak` WRITE;
/*!40000 ALTER TABLE `eskaera_mahaiak` DISABLE KEYS */;
INSERT INTO `eskaera_mahaiak` VALUES (1,2,1),(2,3,1);
/*!40000 ALTER TABLE `eskaera_mahaiak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eskaera_produktuak`
--

DROP TABLE IF EXISTS `eskaera_produktuak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eskaera_produktuak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `eskaera_id` int NOT NULL,
  `produktua_id` int NOT NULL,
  `kantitatea` int NOT NULL DEFAULT '1',
  `prezio_unitarioa` decimal(10,2) NOT NULL,
  `guztira` decimal(10,2) GENERATED ALWAYS AS ((`kantitatea` * `prezio_unitarioa`)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `eskaera_id_UNIQUE` (`eskaera_id`),
  UNIQUE KEY `produktua_id_UNIQUE` (`produktua_id`),
  KEY `eskaera_id` (`eskaera_id`),
  KEY `produktua_id` (`produktua_id`),
  CONSTRAINT `eskaera_produktuak_ibfk_1` FOREIGN KEY (`eskaera_id`) REFERENCES `eskaerak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `eskaera_produktuak_ibfk_2` FOREIGN KEY (`produktua_id`) REFERENCES `produktuak` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eskaera_produktuak`
--

LOCK TABLES `eskaera_produktuak` WRITE;
/*!40000 ALTER TABLE `eskaera_produktuak` DISABLE KEYS */;
/*!40000 ALTER TABLE `eskaera_produktuak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eskaerak`
--

DROP TABLE IF EXISTS `eskaerak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eskaerak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `mahaia_id` int DEFAULT NULL,
  `erabiltzaile_id` int NOT NULL,
  `komensalak` tinyint DEFAULT NULL,
  `egoera` enum('irekita','itxita','ordainketa_pendiente') DEFAULT 'irekita',
  `sukaldea_egoera` enum('zain','hasi','prest') DEFAULT 'zain',
  `sortze_data` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `itxiera_data` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mahaia_id` (`mahaia_id`),
  KEY `erabiltzaile_id` (`erabiltzaile_id`),
  CONSTRAINT `eskaerak_ibfk_1` FOREIGN KEY (`mahaia_id`) REFERENCES `mahaiak` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `eskaerak_ibfk_2` FOREIGN KEY (`erabiltzaile_id`) REFERENCES `erabiltzaileak` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eskaerak`
--

LOCK TABLES `eskaerak` WRITE;
/*!40000 ALTER TABLE `eskaerak` DISABLE KEYS */;
INSERT INTO `eskaerak` VALUES (2,1,2,4,'irekita',NULL,'2025-12-18 16:49:45',NULL),(3,1,2,4,'irekita',NULL,'2025-12-18 16:54:48',NULL);
/*!40000 ALTER TABLE `eskaerak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fakturak`
--

DROP TABLE IF EXISTS `fakturak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fakturak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `eskaera_id` int NOT NULL,
  `pdf_izena` varchar(255) NOT NULL,
  `data` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `guztira` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `eskaera_id` (`eskaera_id`),
  CONSTRAINT `fakturak_ibfk_1` FOREIGN KEY (`eskaera_id`) REFERENCES `eskaerak` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fakturak`
--

LOCK TABLES `fakturak` WRITE;
/*!40000 ALTER TABLE `fakturak` DISABLE KEYS */;
/*!40000 ALTER TABLE `fakturak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kategoriak`
--

DROP TABLE IF EXISTS `kategoriak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `kategoriak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `izena` (`izena`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kategoriak`
--

LOCK TABLES `kategoriak` WRITE;
/*!40000 ALTER TABLE `kategoriak` DISABLE KEYS */;
INSERT INTO `kategoriak` VALUES (4,'bigarren_platerak'),(7,'edariak_alkohol'),(8,'edariak_sinalkohol'),(1,'kafeak'),(3,'lehen_platerak'),(5,'pintxoak'),(2,'postreak'),(6,'razioak');
/*!40000 ALTER TABLE `kategoriak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mahaiak`
--

DROP TABLE IF EXISTS `mahaiak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mahaiak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `zenbakia` int NOT NULL,
  `kapazitatea` int NOT NULL,
  `egoera` enum('libre','okupatuta','ordainketa_pendiente') DEFAULT 'libre',
  PRIMARY KEY (`id`),
  UNIQUE KEY `zenbakia` (`zenbakia`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mahaiak`
--

LOCK TABLES `mahaiak` WRITE;
/*!40000 ALTER TABLE `mahaiak` DISABLE KEYS */;
INSERT INTO `mahaiak` VALUES (1,2,0,'libre');
/*!40000 ALTER TABLE `mahaiak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `osagaiak`
--

DROP TABLE IF EXISTS `osagaiak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `osagaiak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(150) NOT NULL,
  `unitatea` varchar(20) DEFAULT NULL,
  `stock_aktuala` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `izena` (`izena`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `osagaiak`
--

LOCK TABLES `osagaiak` WRITE;
/*!40000 ALTER TABLE `osagaiak` DISABLE KEYS */;
INSERT INTO `osagaiak` VALUES (1,'Azukrea','gr',10000.00),(2,'Esnea','gr',10000.00),(3,'Gatza','gr',10000.00),(4,'Gazta','gr',10000.00),(5,'Haragia','gr',10000.00),(6,'Irina','gr',10000.00),(7,'Kafea','gr',10000.00),(8,'Limoi','gr',10000.00),(9,'Ogitartekoa','gr',10000.00),(10,'Oliba olioa','gr',10000.00),(11,'Patatak','gr',10000.00),(12,'Tomatea','gr',10000.00),(13,'Txistorra','gr',10000.00),(14,'Txokolatea','gr',10000.00),(15,'Ura','gr',10000.00);
/*!40000 ALTER TABLE `osagaiak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produktu_osagaiak`
--

DROP TABLE IF EXISTS `produktu_osagaiak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produktu_osagaiak` (
  `produktua_id` int NOT NULL,
  `osagaia_id` int NOT NULL,
  `kantitatea` decimal(10,2) NOT NULL,
  `unitatea` varchar(20) NOT NULL,
  PRIMARY KEY (`produktua_id`,`osagaia_id`),
  KEY `produktu_osagaiak_ibfk_2` (`osagaia_id`),
  CONSTRAINT `produktu_osagaiak_ibfk_1` FOREIGN KEY (`produktua_id`) REFERENCES `produktuak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `produktu_osagaiak_ibfk_2` FOREIGN KEY (`osagaia_id`) REFERENCES `osagaiak` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produktu_osagaiak`
--

LOCK TABLES `produktu_osagaiak` WRITE;
/*!40000 ALTER TABLE `produktu_osagaiak` DISABLE KEYS */;
INSERT INTO `produktu_osagaiak` VALUES (1,1,1.00,'gr'),(1,7,50.00,'gr');
/*!40000 ALTER TABLE `produktu_osagaiak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produktuak`
--

DROP TABLE IF EXISTS `produktuak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produktuak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(200) NOT NULL,
  `kategoria_id` int NOT NULL,
  `prezioa` decimal(10,2) NOT NULL,
  `stock_aktuala` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `kategoria_id` (`kategoria_id`),
  CONSTRAINT `produktuak_ibfk_1` FOREIGN KEY (`kategoria_id`) REFERENCES `kategoriak` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produktuak`
--

LOCK TABLES `produktuak` WRITE;
/*!40000 ALTER TABLE `produktuak` DISABLE KEYS */;
INSERT INTO `produktuak` VALUES (1,'Kafe hutsa',1,1.20,0),(2,'Kafea esnearekin',1,1.40,0),(3,'Kafe ebakia',1,1.30,0),(4,'Kaputxinoa',1,1.80,0),(5,'Izoztutako kafea',1,2.00,0),(6,'Mokaccinoa',1,2.20,0),(7,'Kafe deskafeinatua',1,1.30,0),(8,'Txokolatezko tarta',2,3.00,0),(9,'Gazta tarta',2,4.20,0),(10,'Flana',2,3.00,0),(11,'Arroz-esnea',2,3.20,0),(12,'Natillak',2,3.50,0),(13,'Brownie beroa',2,4.50,0),(14,'Goxoki nahasketa',2,2.80,0),(15,'Fruta entsalada',2,3.10,0),(16,'Tiramisua',2,4.30,0),(17,'Banana split',2,4.80,0),(18,'Limoi-sorbetea',2,3.00,0),(19,'Banilla-izozkia',2,2.60,0),(20,'Txokolate-izozkia',2,2.60,0),(21,'Gatza eta ogi tarta',2,3.80,0),(22,'Azenario tarta',2,4.10,0),(23,'Entsalada mistoa',3,6.50,0),(24,'Caesar entsalada',3,7.00,0),(25,'Arrain-zopa',3,6.80,0),(26,'Barazki-krema',3,6.20,0),(27,'Makarrak boloñar erara',3,7.50,0),(28,'Hiru gozameneko arroza',3,6.90,0),(29,'Plantxan egindako barazkiak',3,6.40,0),(30,'Txuleta platerrean',4,18.00,0),(31,'Oilasko erreta',4,10.00,0),(32,'Legatza plantxan',4,12.50,0),(33,'Entrekota',4,16.00,0),(34,'Solomoa patatekin',4,9.50,0),(35,'Albondagak saltsan',4,10.20,0),(36,'Bakailaoa pil-pilean',4,14.00,0),(37,'Tortilla pintxoa',5,2.00,0),(38,'Gilda',5,1.50,0),(39,'Txaka pintxoa',5,2.20,0),(40,'Urdaiazpiko serranoa',5,2.50,0),(41,'Txistor pintxoa',5,2.10,0),(42,'Idiazabal gazta',5,2.40,0),(43,'Txorizoa sagardoan',5,2.80,0),(44,'Txalupa',5,2.50,0),(45,'Solomo pintxoa',5,3.20,0),(46,'Txanpiñoiak plantxan',5,2.20,0),(47,'Mini hanburgesa',5,3.00,0),(48,'Ganba irinztatua',5,2.70,0),(49,'Etxeko kroketak',5,1.80,0),(50,'Oilasko brotxeta',5,2.60,0),(51,'Hegaluze pintxoa',5,2.40,0),(52,'Raba errazioa',6,12.00,0),(53,'Patata brabak errazioa',6,7.00,0),(54,'Patata frijitu errazioa',6,5.00,0),(55,'Urdaiazpiko errazioa',6,14.00,0),(56,'Kroketa errazioa',6,9.00,0),(57,'Entsaladilla errazioa',6,7.50,0),(58,'Tripaki errazioa',6,10.00,0),(59,'Muskuilu errazioa',6,8.00,0),(60,'Oilasko hegalen errazioa',6,9.50,0),(61,'Gazta errazioa',6,10.00,0),(62,'Txorizo errazioa',6,8.50,0),(63,'Txanpiñoi errazioa',6,7.20,0),(64,'Tortilla errazioa',6,6.00,0),(65,'Oilasko ogi-arrautzeztatua',6,8.00,0),(66,'Nuggets errazioa',6,7.00,0),(67,'Garagardo txikia',7,2.20,0),(68,'Garagardo pitxerra',7,4.00,0),(69,'Ardo beltz kopa',7,2.50,0),(70,'Ardo zuri kopa',7,2.50,0),(71,'Kalimotxoa',7,3.50,0),(72,'Cuba libre',7,6.00,0),(73,'Vodka-laranja',7,5.50,0),(74,'Gin-tonic',7,6.50,0),(75,'Whisky kola',7,6.00,0),(76,'Garagardo beltza',7,2.50,0),(77,'Sagardoa',7,3.00,0),(78,'Txakolina',7,3.20,0),(79,'Vermuta',7,2.80,0),(80,'Mojitoa',7,6.50,0),(81,'Ezti-rona',7,3.00,0),(82,'Ura',8,1.20,0),(83,'Coca-Cola',8,2.20,0),(84,'Coca-Cola Zero',8,2.20,0),(85,'Laranja Fanta',8,2.00,0),(86,'Limoi Fanta',8,2.00,0),(87,'Nestea',8,2.20,0),(88,'Aquarius',8,2.20,0),(89,'Anana zukua',8,2.00,0),(90,'Laranja zukua',8,2.20,0),(91,'Tonika',8,2.10,0),(92,'Red Bull',8,3.00,0),(93,'Bitter Kas',8,2.20,0),(94,'Ur gasa',8,1.80,0),(95,'Mahats-zukua',8,1.50,0),(96,'Melokotoi zukua',8,2.10,0);
/*!40000 ALTER TABLE `produktuak` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rolak`
--

DROP TABLE IF EXISTS `rolak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rolak` (
  `id` int NOT NULL AUTO_INCREMENT,
  `izena` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rolak`
--

LOCK TABLES `rolak` WRITE;
/*!40000 ALTER TABLE `rolak` DISABLE KEYS */;
INSERT INTO `rolak` VALUES (1,'administratzailea'),(2,'zerbitzaria'),(3,'sukaldaria');
/*!40000 ALTER TABLE `rolak` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-23 13:54:34
