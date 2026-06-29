
CREATE DATABASE IF NOT EXISTS visitor_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE visitor_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `vehicle`;
DROP TABLE IF EXISTS `access_record`;
DROP TABLE IF EXISTS `visitor_feedback`;
DROP TABLE IF EXISTS `appointment_item`;
DROP TABLE IF EXISTS `approval_record`;
DROP TABLE IF EXISTS `appointment`;
DROP TABLE IF EXISTS `blacklist`;
DROP TABLE IF EXISTS `area`;
DROP TABLE IF EXISTS `system_config`;

DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `id_card` varchar(20) DEFAULT NULL,
  `dept_id` bigint(20) DEFAULT NULL,
  `status` tinyint(4) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Users';

CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(20) NOT NULL,
  `role_name` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Roles';

CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Roles';

CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(50) NOT NULL,
  `parent_id` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Departments';

CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(64) NOT NULL,
  `config_value` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Config';

CREATE TABLE `area` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `area_name` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_name` (`area_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Campus Areas';

CREATE TABLE `blacklist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `visitor_id_card` varchar(20) NOT NULL,
  `visitor_name` varchar(50) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `operator_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `expire_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_black_id_card` (`visitor_id_card`),
  KEY `idx_black_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Blacklist';

CREATE TABLE `appointment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `visitor_id` bigint(20) NOT NULL,
  `visitor_name` varchar(50) DEFAULT NULL,
  `visitor_phone` varchar(20) DEFAULT NULL,
  `visitor_id_card` varchar(20) DEFAULT NULL,
  `visited_user_id` bigint(20) NOT NULL,
  `area_id` bigint(20) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `qrcode_url` longtext DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_visited_time` (`visited_user_id`,`start_time`,`end_time`),
  KEY `idx_visitor_time` (`visitor_id`,`start_time`,`end_time`),
  KEY `idx_visitor_id_card_time` (`visitor_id_card`,`start_time`,`end_time`),
  KEY `idx_status` (`status`),
  KEY `idx_area` (`area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Appointments';

CREATE TABLE `appointment_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint(20) NOT NULL,
  `item_name` varchar(100) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '1',
  `note` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_item_appt` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Appointment Items';

CREATE TABLE `visitor_feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint(20) NOT NULL,
  `visitor_id` bigint(20) NOT NULL,
  `visitor_name` varchar(50) DEFAULT NULL,
  `approval_speed` int(11) NOT NULL,
  `guard_attitude` int(11) NOT NULL,
  `environment` int(11) NOT NULL,
  `overall` int(11) NOT NULL,
  `comment` text,
  `is_anonymous` tinyint(1) NOT NULL DEFAULT '1',
  `is_featured` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feedback_appt` (`appointment_id`),
  KEY `idx_featured_time` (`is_featured`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Visitor Feedback';

CREATE TABLE `approval_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint(20) NOT NULL,
  `approver_id` bigint(20) NOT NULL,
  `approver_role` varchar(10) NOT NULL,
  `action` varchar(10) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_appt_role_time` (`appointment_id`,`approver_role`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Approval Records';

CREATE TABLE `access_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint(20) NOT NULL,
  `visitor_name` varchar(50) DEFAULT NULL,
  `entry_time` datetime DEFAULT NULL,
  `exit_time` datetime DEFAULT NULL,
  `duration_minutes` int(11) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_appt_status_time` (`appointment_id`,`status`,`entry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Access Records';

CREATE TABLE `vehicle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint(20) NOT NULL,
  `license_plate` varchar(20) NOT NULL,
  `visitor_name` varchar(50) DEFAULT NULL,
  `visitor_phone` varchar(20) DEFAULT NULL,
  `entry_time` datetime DEFAULT NULL,
  `exit_time` datetime DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicle_appt` (`appointment_id`),
  KEY `idx_vehicle_plate` (`license_plate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Vehicle Records';

CREATE TABLE `message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `receiver_id` bigint(20) NOT NULL,
  `type` varchar(30) NOT NULL,
  `title` varchar(100) NOT NULL,
  `content` text,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_receiver_read_time` (`receiver_id`,`is_read`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Messages';

CREATE TABLE `operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `operation` varchar(100) DEFAULT NULL,
  `method` varchar(200) DEFAULT NULL,
  `params` longtext DEFAULT NULL,
  `execution_time` bigint(20) DEFAULT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_log_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Operation Logs';

INSERT INTO `sys_role` (id, role_code, role_name, description) VALUES
(1, 'ROLE_ADMIN', '管理员', 'System Administrator'),
(2, 'ROLE_HOST', '被访人', 'Staff/Student'),
(3, 'ROLE_VISITOR', '访客', 'External Visitor'),
(4, 'ROLE_GUARD', '门岗', 'Security Guard');

INSERT INTO `sys_user` (id, username, password, real_name, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkSQL/aJQplJpzgWn/K', 'Super Admin', 1);

INSERT INTO `sys_user_role` (user_id, role_id) VALUES (1, 1);

INSERT INTO `system_config` (config_key, config_value, description) VALUES
('daily_max_appointments', '3', '每日最大预约次数'),
('appointment_enabled', 'true', '是否允许预约'),
('max_duration_hours', '4', '单次最大访问时长(小时)');

INSERT INTO `area` (area_name, description, is_active) VALUES
('默认区域', '默认可访问区域', 1);

SET FOREIGN_KEY_CHECKS = 1;
