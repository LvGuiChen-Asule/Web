USE visitor_db;

INSERT INTO sys_user (id, username, password, real_name, phone, id_card, dept_id, status, create_time, update_time) VALUES
(12, 'host_wu', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '吴老师', '13900000003', '110101199003030033', 1, 1, NOW(), NOW()),
(13, 'host_chen', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '陈老师', '13900000004', '110101199004040044', 2, 1, NOW(), NOW()),
(23, 'visitor_liu', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '刘一', '13800000004', '320102199101010011', NULL, 1, NOW(), NOW()),
(24, 'visitor_qian', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '钱二', '13800000005', '320102199202020022', NULL, 1, NOW(), NOW()),
(25, 'visitor_zhou', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '周三', '13800000006', '320102199303030033', NULL, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
password = VALUES(password),
real_name = VALUES(real_name),
phone = VALUES(phone),
id_card = VALUES(id_card),
dept_id = VALUES(dept_id),
status = VALUES(status),
update_time = NOW();

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(12, 2),
(13, 2),
(23, 3),
(24, 3),
(25, 3);

INSERT INTO area (id, area_name, description, is_active) VALUES
(2, '教学楼A区', '教学楼A区', 1),
(3, '实验楼B区', '实验楼B区', 1),
(4, '图书馆', '图书馆区域', 1)
ON DUPLICATE KEY UPDATE
area_name = VALUES(area_name),
description = VALUES(description),
is_active = VALUES(is_active);

DELETE FROM appointment
WHERE reason LIKE '统计测试%'
  AND start_time >= '2026-03-01 00:00:00'
  AND start_time <  '2026-04-11 00:00:00';

INSERT INTO appointment
(visitor_id, visitor_name, visitor_phone, visitor_id_card, visited_user_id, area_id, start_time, end_time, reason, status, qrcode_url, create_time, update_time)
SELECT
  visitorId,
  visitorName,
  visitorPhone,
  visitorIdCard,
  hostId,
  areaId,
  startTime,
  endTime,
  CONCAT('统计测试-来访', DATE_FORMAT(startTime, '%Y%m%d'), '-', slot),
  status,
  NULL,
  DATE_SUB(startTime, INTERVAL 2 DAY),
  DATE_SUB(startTime, INTERVAL 2 DAY)
FROM (
  SELECT
    d.dayStart,
    d.i,
    s.slot,
    CASE s.slot
      WHEN 0 THEN 20
      WHEN 1 THEN 21
      WHEN 2 THEN 23
      WHEN 3 THEN 24
      WHEN 4 THEN 25
      ELSE 20
    END AS visitorId,
    CASE s.slot
      WHEN 0 THEN '王五'
      WHEN 1 THEN '赵六'
      WHEN 2 THEN '刘一'
      WHEN 3 THEN '钱二'
      WHEN 4 THEN '周三'
      ELSE '王五'
    END AS visitorName,
    CASE s.slot
      WHEN 0 THEN '13800000001'
      WHEN 1 THEN '13800000002'
      WHEN 2 THEN '13800000004'
      WHEN 3 THEN '13800000005'
      WHEN 4 THEN '13800000006'
      ELSE '13800000001'
    END AS visitorPhone,
    CASE s.slot
      WHEN 0 THEN '330102199303030033'
      WHEN 1 THEN '330102199404040044'
      WHEN 2 THEN '320102199101010011'
      WHEN 3 THEN '320102199202020022'
      WHEN 4 THEN '320102199303030033'
      ELSE '330102199303030033'
    END AS visitorIdCard,
    CASE
      WHEN s.slot IN (0,1,2) THEN 10
      WHEN s.slot IN (3,4) THEN 11
      ELSE CASE WHEN MOD(d.i, 5) = 0 THEN 13 ELSE 12 END
    END AS hostId,
    CASE MOD(d.i + s.slot, 3)
      WHEN 0 THEN 2
      WHEN 1 THEN 3
      ELSE 4
    END AS areaId,
    CASE s.slot
      WHEN 0 THEN DATE_ADD(d.dayStart, INTERVAL 9 HOUR)
      WHEN 1 THEN DATE_ADD(DATE_ADD(d.dayStart, INTERVAL 10 HOUR), INTERVAL 30 MINUTE)
      WHEN 2 THEN DATE_ADD(d.dayStart, INTERVAL 14 HOUR)
      WHEN 3 THEN DATE_ADD(DATE_ADD(d.dayStart, INTERVAL 15 HOUR), INTERVAL 30 MINUTE)
      WHEN 4 THEN DATE_ADD(d.dayStart, INTERVAL 16 HOUR)
      ELSE DATE_ADD(d.dayStart, INTERVAL 19 HOUR)
    END AS startTime,
    CASE s.slot
      WHEN 0 THEN DATE_ADD(d.dayStart, INTERVAL 10 HOUR)
      WHEN 1 THEN DATE_ADD(DATE_ADD(d.dayStart, INTERVAL 11 HOUR), INTERVAL 30 MINUTE)
      WHEN 2 THEN DATE_ADD(d.dayStart, INTERVAL 15 HOUR)
      WHEN 3 THEN DATE_ADD(DATE_ADD(d.dayStart, INTERVAL 16 HOUR), INTERVAL 30 MINUTE)
      WHEN 4 THEN DATE_ADD(d.dayStart, INTERVAL 17 HOUR)
      ELSE DATE_ADD(d.dayStart, INTERVAL 20 HOUR)
    END AS endTime,
    CASE MOD(d.i + s.slot, 3)
      WHEN 0 THEN 'APPROVED'
      WHEN 1 THEN 'CHECKED_IN'
      ELSE 'CHECKED_OUT'
    END AS status
  FROM (
    SELECT
      DATE_ADD('2026-03-01 00:00:00', INTERVAL n DAY) AS dayStart,
      n AS i
    FROM (
      SELECT ones.n + tens.n * 10 AS n
      FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) ones
      CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) tens
    ) t
    WHERE n <= 40
  ) d
  CROSS JOIN (
    SELECT 0 AS slot UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
  ) s
  WHERE s.slot <= (MOD(d.i, 4) + 2)
) rows;
