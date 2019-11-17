CREATE TABLE `browser_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `verified` tinyint(1) NOT NULL DEFAULT '0',
  `idp_user_email` varchar(100) NOT NULL,
  `idp_user_name` varchar(100) NOT NULL,
  `user_agent` text NOT NULL,
  `ip_address` varchar(43) NOT NULL,
  `retry_cnt` int(10) NOT NULL DEFAULT '0',
  `error_cnt` int(10) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_for_browser_auth_idp_user_email_created_at` (`idp_user_email`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `browser_auth_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `browser_auth_id` bigint(20) NOT NULL,
  `verified` tinyint(1) NOT NULL DEFAULT '0',
  `auth_code` varchar(50) NOT NULL,
  `expired_at` datetime NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_for_browser_auth_id_created_at` (`browser_auth_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `browser_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `browser_auth_id` bigint(20) NOT NULL,
  `browser_session_value` varchar(500) NOT NULL,
  `expired_at` datetime NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `index_for_browser_auth_browser_session_expired_at` (`browser_session_value`,`expired_at`),
  KEY `fk_browser_session_for_browser_auth_id` (`browser_auth_id`),
  CONSTRAINT `fk_browser_session_for_browser_auth_id` FOREIGN KEY (`browser_auth_id`) REFERENCES `browser_auth` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
