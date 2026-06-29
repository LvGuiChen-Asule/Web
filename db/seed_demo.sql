USE visitor_db;

INSERT INTO sys_dept (id, dept_name, parent_id) VALUES
(1, '教学部', 0),
(2, '行政部', 0),
(3, '安全保卫处', 0);

INSERT INTO sys_user (id, username, password, real_name, phone, id_card, dept_id, status) VALUES
(10, 'host_zhang', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '张老师', '13900000001', '110101199001010011', 1, 1),
(11, 'host_li', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '李老师', '13900000002', '110101199002020022', 2, 1),
(20, 'visitor_wang', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '王五', '13800000001', '330102199303030033', NULL, 1),
(21, 'visitor_zhao', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '赵六', '13800000002', '330102199404040044', NULL, 1),
(22, 'visitor_sun', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '孙七', '13800000003', '330102199505050055', NULL, 1),
(30, 'guard_01', '$2a$10$b8QdVhReanWyDj9ps1a8q.P49kExUKQDkmw0Qb7gW575i7mBlYrQa', '门岗一号', '13700000001', '440103199606060066', 3, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(10, 2),
(11, 2),
(20, 3),
(21, 3),
(22, 3),
(30, 4);

INSERT INTO area (id, area_name, description, is_active) VALUES
(2, '教学楼A区', '教学楼A区', 1),
(3, '实验楼B区', '实验楼B区', 1),
(4, '图书馆', '图书馆区域', 1);

INSERT INTO blacklist (id, visitor_id_card, visitor_name, reason, operator_id, expire_time) VALUES
(1, '330102199505050055', '孙七', '测试黑名单（临时）', 1, '2026-12-31 23:59:59'),
(2, '999999199001010000', '永久黑名单', '测试黑名单（永久）', 1, NULL);

INSERT INTO appointment
(id, visitor_id, visitor_name, visitor_phone, visitor_id_card, visited_user_id, area_id, start_time, end_time, reason, status, qrcode_url, create_time, update_time) VALUES
(1001, 20, '王五', '13800000001', '330102199303030033', 10, 2, '2026-04-10 09:00:00', '2026-04-10 10:00:00', '来访交流', 'PENDING', NULL, '2026-04-02 10:00:00', '2026-04-02 10:00:00'),
(1002, 21, '赵六', '13800000002', '330102199404040044', 10, 3, '2026-04-10 10:30:00', '2026-04-10 11:30:00', '项目沟通', 'FIRST_APPROVED', NULL, '2026-04-02 10:05:00', '2026-04-02 10:20:00'),
(1003, 20, '王五', '13800000001', '330102199303030033', 11, 4, '2026-04-10 14:00:00', '2026-04-10 16:00:00', '参观访问', 'APPROVED', NULL, '2026-04-02 10:10:00', '2026-04-02 10:40:00'),
(1004, 21, '赵六', '13800000002', '330102199404040044', 11, 2, '2026-04-11 09:00:00', '2026-04-11 10:00:00', '测试拒绝', 'REJECTED', NULL, '2026-04-02 10:15:00', '2026-04-02 10:25:00'),
(1005, 20, '王五', '13800000001', '330102199303030033', 10, 2, '2026-04-12 09:00:00', '2026-04-12 10:00:00', '测试取消', 'CANCELLED', NULL, '2026-04-02 10:16:00', '2026-04-02 10:26:00'),
(1006, 20, '王五', '13800000001', '330102199303030033', 10, 3, '2026-04-01 09:00:00', '2026-04-01 10:00:00', '已入校未离校', 'CHECKED_IN', NULL, '2026-04-01 08:00:00', '2026-04-01 09:05:00'),
(1007, 21, '赵六', '13800000002', '330102199404040044', 11, 4, '2026-03-31 14:00:00', '2026-03-31 16:00:00', '已离校', 'CHECKED_OUT', NULL, '2026-03-31 13:00:00', '2026-03-31 16:10:00');

INSERT INTO approval_record (id, appointment_id, approver_id, approver_role, action, comment, create_time) VALUES
(2001, 1002, 10, 'FIRST', 'APPROVE', '同意', '2026-04-02 10:20:00'),
(2002, 1003, 11, 'FIRST', 'APPROVE', '同意', '2026-04-02 10:30:00'),
(2003, 1003, 1, 'SECOND', 'APPROVE', '同意', '2026-04-02 10:40:00'),
(2004, 1004, 11, 'FIRST', 'REJECT', '时间不合适', '2026-04-02 10:25:00');

INSERT INTO access_record (id, appointment_id, visitor_name, entry_time, exit_time, duration_minutes, status) VALUES
(3001, 1006, '王五', '2026-04-01 09:05:00', NULL, NULL, 'IN_CAMPUS'),
(3002, 1007, '赵六', '2026-03-31 14:05:00', '2026-03-31 16:10:00', 125, 'LEFT');

INSERT INTO vehicle (id, appointment_id, license_plate, visitor_name, visitor_phone, entry_time, exit_time, status) VALUES
(4001, 1003, '浙A12345', '王五', '13800000001', NULL, NULL, 'OUT'),
(4002, 1007, '浙B99887', '赵六', '13800000002', '2026-03-31 14:10:00', '2026-03-31 16:00:00', 'OUT');

INSERT INTO appointment_item (id, appointment_id, item_name, quantity, note, create_time) VALUES
(7001, 1003, '笔记本电脑', 1, '工作设备', '2026-04-02 10:12:00'),
(7002, 1003, '相机', 1, '拍摄记录', '2026-04-02 10:12:00'),
(7003, 1006, '工具箱', 1, '维修工具', '2026-04-01 08:30:00');

INSERT INTO visitor_feedback (id, appointment_id, visitor_id, visitor_name, approval_speed, guard_attitude, environment, overall, comment, is_anonymous, is_featured, create_time) VALUES
(8001, 1007, 21, '赵六', 5, 5, 5, 5, '审批很快，门岗很专业，校园环境很好。', 1, 1, '2026-03-31 18:00:00');

INSERT INTO message (id, receiver_id, type, title, content, is_read, create_time) VALUES
(5001, 20, 'APPROVAL_RESULT', '预约审批更新', '被访人已通过，等待管理员审批', 0, '2026-04-02 10:20:05'),
(5002, 20, 'APPROVAL_RESULT', '预约审批结果', '管理员已通过', 0, '2026-04-02 10:40:05'),
(5003, 10, 'APPROVAL_RESULT', '预约审批结果', '管理员已通过', 0, '2026-04-02 10:40:05'),
(5004, 21, 'APPROVAL_RESULT', '预约审批结果', '被访人已拒绝', 1, '2026-04-02 10:25:05'),
(5005, 10, 'SYSTEM', '系统通知', '这是一条测试系统通知', 0, '2026-04-02 11:00:00');

INSERT INTO operation_log (id, username, operation, method, params, execution_time, ip_address, create_time) VALUES
(6001, 'admin', 'ConfigController.update', 'PUT /api/v1/config/daily_max_appointments', '{\"key\":\"daily_max_appointments\",\"value\":\"3\"}', 12, '127.0.0.1', '2026-04-02 09:00:00'),
(6002, 'host_zhang', 'ApprovalController.first', 'POST /api/v1/approval/first', '{\"id\":1002,\"passed\":true}', 25, '127.0.0.1', '2026-04-02 10:20:00'),
(6003, 'admin', 'ApprovalController.second', 'POST /api/v1/approval/second', '{\"id\":1003,\"passed\":true}', 31, '127.0.0.1', '2026-04-02 10:40:00');
