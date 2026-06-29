-- Database initialization
CREATE DATABASE IF NOT EXISTS visitor_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE visitor_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. System Users
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT 'Username (Phone for visitor)',
  `password` varchar(100) NOT NULL COMMENT 'Encrypted Password',
  `real_name` varchar(50) DEFAULT NULL COMMENT 'Real Name',
  `phone` varchar(20) DEFAULT NULL COMMENT 'Phone Number',
  `id_card` varchar(20) DEFAULT NULL COMMENT 'ID Card / Student ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT 'Department ID',
  `status` tinyint(4) DEFAULT '1' COMMENT '1:Enable, 0:Disable',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Users';

-- 2. Roles
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(20) NOT NULL COMMENT 'ROLE_VISITOR, ROLE_HOST, etc.',
  `role_name` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Roles';

-- 3. User Roles
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Roles';

-- 4. Departments
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(50) NOT NULL,
  `parent_id` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Departments';

-- 5. Appointments
DROP TABLE IF EXISTS `vis_appointment`;
CREATE TABLE `vis_appointment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `visitor_id` bigint(20) NOT NULL COMMENT 'Visitor User ID',
  `host_id` bigint(20) NOT NULL COMMENT 'Host User ID',
  `visit_start_time` datetime NOT NULL,
  `visit_end_time` datetime NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `visitor_count` int(11) DEFAULT '1',
  `car_plate` varchar(20) DEFAULT NULL,
  `attachments` json DEFAULT NULL COMMENT 'URLs of health code, etc.',
  `status` tinyint(4) DEFAULT '0' COMMENT '0:Draft, 10:PendingHost, 20:PendingAdmin, 30:Approved, 40:Rejected, 50:Entered, 60:Completed, 80:Expired',
  `audit_remark` varchar(255) DEFAULT NULL COMMENT 'Reason for rejection',
  `qr_code` text DEFAULT NULL COMMENT 'QR Code Content',
  `qr_code_expire` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_visitor` (`visitor_id`),
  KEY `idx_host` (`host_id`),
  KEY `idx_time` (`visit_start_time`, `visit_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Appointments';

-- 6. Visit Logs (Entry/Exit)
DROP TABLE IF EXISTS `vis_visit_log`;
CREATE TABLE `vis_visit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint(20) NOT NULL,
  `entry_time` datetime DEFAULT NULL,
  `exit_time` datetime DEFAULT NULL,
  `guard_id` bigint(20) DEFAULT NULL COMMENT 'Operator',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_appt` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Visit Logs';

-- 7. System Config
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_key` varchar(50) NOT NULL,
  `config_value` varchar(255) DEFAULT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Config';

-- 8. System Logs (Operations)
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `operation` varchar(50) DEFAULT NULL,
  `method` varchar(200) DEFAULT NULL,
  `params` text,
  `ip` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Operation Logs';

-- Initial Data
INSERT INTO `sys_role` (id, role_code, role_name, description) VALUES 
(1, 'ROLE_ADMIN', '管理员', 'System Administrator'),
(2, 'ROLE_HOST', '被访人', 'Staff/Student'),
(3, 'ROLE_VISITOR', '访客', 'External Visitor'),
(4, 'ROLE_GUARD', '门岗', 'Security Guard');

-- Admin user (password: 123456 - encrypted needed in real app)
-- For dev purpose, we assume BCrypt will generate hash for '123456'
-- $2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkSQL/aJQplJpzgWn/K is '123456'
INSERT INTO `sys_user` (id, username, password, real_name, status) VALUES 
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnutj8iAt6ValmpBkSQL/aJQplJpzgWn/K', 'Super Admin', 1);

INSERT INTO `sys_user_role` (user_id, role_id) VALUES (1, 1);

INSERT INTO `sys_config` (config_key, config_value, description) VALUES 
('DAILY_LIMIT_PER_VISITOR', '2', 'Max appointments per visitor per day'),
('WEEKLY_LIMIT_PER_VISITOR', '5', 'Max appointments per visitor per week');

SET FOREIGN_KEY_CHECKS = 1;
