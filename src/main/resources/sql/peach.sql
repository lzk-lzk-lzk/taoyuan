-- 创建数据库
CREATE DATABASE IF NOT EXISTS `taoyuan` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `taoyuan`;

-- 系统用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nick_name` varchar(50) NOT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `open_id` varchar(64) DEFAULT NULL COMMENT '微信小程序openid',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0正常 1停用',
  `user_type` varchar(20) NOT NULL COMMENT '用户类型 ADMIN/MINIAPP',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_user_username` (`username`),
  KEY `idx_sys_user_open_id` (`open_id`),
  KEY `idx_sys_user_user_type` (`user_type`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 品种信息表
DROP TABLE IF EXISTS `fruit_variety`;
CREATE TABLE `fruit_variety` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `variety_code` varchar(50) NOT NULL COMMENT '品种编码',
  `variety_name` varchar(100) NOT NULL COMMENT '品种名称',
  `category_name` varchar(100) NOT NULL COMMENT '种属类别',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `distribution_area` varchar(255) DEFAULT NULL COMMENT '分布地区',
  `fruit_traits` text COMMENT '果实性状',
  `cultivation_points` text COMMENT '栽培要点',
  `qr_code_url` varchar(255) DEFAULT NULL COMMENT '二维码图片地址',
  `qr_target_url` varchar(500) DEFAULT NULL COMMENT '二维码跳转地址',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0正常 1停用',
  `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记 0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_fruit_variety_name` (`variety_name`),
  KEY `idx_fruit_variety_code` (`variety_code`),
  KEY `idx_fruit_variety_category` (`category_name`),
  KEY `idx_fruit_variety_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='桃品种信息表';

-- 初始化账号数据
INSERT INTO `sys_user` (`id`, `username`, `password`, `nick_name`, `phone`, `avatar`, `open_id`, `status`, `user_type`, `last_login_time`, `remark`, `create_time`, `update_time`, `del_flag`)
VALUES
  (1, 'admin', '$2a$10$4Rpfwtwhft42fCRgGbDJhujJ4xUMrWz5DuIqdw5pMErW8J2FpfeGC', '系统管理员', '13800000000', NULL, NULL, 0, 'ADMIN', NULL, '初始管理员账号，密码 123456', NOW(), NOW(), 0),
  (2, 'miniapp01', '$2a$10$4Rpfwtwhft42fCRgGbDJhujJ4xUMrWz5DuIqdw5pMErW8J2FpfeGC', '小程序测试用户', '13900000000', NULL, NULL, 0, 'MINIAPP', NULL, '测试小程序账号，密码 123456', NOW(), NOW(), 0);

-- 初始化品种测试数据
INSERT INTO `fruit_variety` (`id`, `variety_code`, `variety_name`, `category_name`, `cover_image`, `distribution_area`, `fruit_traits`, `cultivation_points`, `qr_code_url`, `qr_target_url`, `status`, `sort_num`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES
  (1001, 'PTY-001', '春雪桃', '桃属', '/static/images/demo/chunxuetao.jpg', '山东、河北、河南', '果面洁白着红晕，肉质细脆，成熟期早', '注意花期防冻，幼果期及时疏果', NULL, 'https://example.com/miniapp/variety/detail?id=1001', 0, 1, '早熟白肉桃测试数据', 'admin', NOW(), 'admin', NOW(), 0),
  (1002, 'PTY-002', '中油蟠7号', '油蟠桃', '/static/images/demo/zhongyoupan7.jpg', '山东、江苏、安徽', '果形扁圆，香气足，糖度高，适合鲜食', '夏季注意控梢和水肥管理', NULL, 'https://example.com/miniapp/variety/detail?id=1002', 0, 2, '油蟠桃测试数据', 'admin', NOW(), 'admin', NOW(), 0),
  (1003, 'PTY-003', '锦绣黄桃', '黄桃', '/static/images/demo/jinxiu.jpg', '安徽、湖北、湖南', '果肉金黄，耐储运，适合鲜食和加工', '坐果后注意补钾，提高果实整齐度', NULL, 'https://example.com/miniapp/variety/detail?id=1003', 0, 3, '黄桃测试数据', 'admin', NOW(), 'admin', NOW(), 0);
