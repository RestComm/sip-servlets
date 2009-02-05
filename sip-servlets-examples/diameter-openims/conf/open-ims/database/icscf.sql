-- MySQL dump 10.11
--
-- Host: localhost    Database: icscf
-- ------------------------------------------------------
-- Server version	5.0.67-0ubuntu6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `nds_trusted_domains`
--

DROP TABLE IF EXISTS `nds_trusted_domains`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `nds_trusted_domains` (
  `id` int(11) NOT NULL auto_increment,
  `trusted_domain` varchar(83) NOT NULL default '',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `nds_trusted_domains`
--

LOCK TABLES `nds_trusted_domains` WRITE;
/*!40000 ALTER TABLE `nds_trusted_domains` DISABLE KEYS */;
INSERT INTO `nds_trusted_domains` VALUES (1,'open-ims.test');
/*!40000 ALTER TABLE `nds_trusted_domains` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_cscf`
--

DROP TABLE IF EXISTS `s_cscf`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `s_cscf` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(83) NOT NULL default '',
  `s_cscf_uri` varchar(83) NOT NULL default '',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `s_cscf`
--

LOCK TABLES `s_cscf` WRITE;
/*!40000 ALTER TABLE `s_cscf` DISABLE KEYS */;
INSERT INTO `s_cscf` VALUES (1,'First and only S-CSCF','sip:scscf.open-ims.test:6060');
/*!40000 ALTER TABLE `s_cscf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `s_cscf_capabilities`
--

DROP TABLE IF EXISTS `s_cscf_capabilities`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `s_cscf_capabilities` (
  `id` int(11) NOT NULL auto_increment,
  `id_s_cscf` int(11) NOT NULL default '0',
  `capability` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  KEY `idx_capability` (`capability`),
  KEY `idx_id_s_cscf` (`id_s_cscf`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `s_cscf_capabilities`
--

LOCK TABLES `s_cscf_capabilities` WRITE;
/*!40000 ALTER TABLE `s_cscf_capabilities` DISABLE KEYS */;
INSERT INTO `s_cscf_capabilities` VALUES (1,1,0),(2,1,1);
/*!40000 ALTER TABLE `s_cscf_capabilities` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-02-04 17:30:33
