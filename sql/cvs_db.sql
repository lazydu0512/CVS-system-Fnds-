/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80100
 Source Host           : localhost:3306
 Source Schema         : cvs_db

 Target Server Type    : MySQL
 Target Server Version : 80100
 File Encoding         : 65001

 Date: 30/01/2026 23:48:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for city
-- ----------------------------
DROP TABLE IF EXISTS `city`;
CREATE TABLE `city`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市名称',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of city
-- ----------------------------
INSERT INTO `city` VALUES (1, '北京', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (2, '上海', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (3, '广州', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (4, '深圳', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (5, '杭州', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (6, '南京', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (7, '成都', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (8, '武汉', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (9, '西安', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (10, '重庆', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (11, '天津', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (12, '苏州', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (13, '青岛', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (14, '大连', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (15, '宁波', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (16, '厦门', '2026-01-22 21:28:37');
INSERT INTO `city` VALUES (17, '佛山', '2026-01-22 21:30:21');
INSERT INTO `city` VALUES (18, '澳门', '2026-01-29 23:38:37');
INSERT INTO `city` VALUES (19, '悉尼', '2026-01-29 23:45:46');
INSERT INTO `city` VALUES (20, '合肥', '2026-01-29 23:47:50');

-- ----------------------------
-- Table structure for city_manager
-- ----------------------------
DROP TABLE IF EXISTS `city_manager`;
CREATE TABLE `city_manager`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '城市管理员用户ID',
  `city_id` bigint(0) NOT NULL COMMENT '管理的城市ID',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_city`(`city_id`) USING BTREE COMMENT '一个城市只能有一个管理员',
  INDEX `idx_user`(`user_id`) USING BTREE,
  CONSTRAINT `city_manager_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `city_manager_ibfk_2` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of city_manager
-- ----------------------------
INSERT INTO `city_manager` VALUES (1, 5, 18, '2026-01-30 20:40:56');
INSERT INTO `city_manager` VALUES (2, 5, 17, '2026-01-30 21:01:04');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `video_id` bigint(0) NOT NULL COMMENT '视频ID',
  `user_id` bigint(0) NOT NULL COMMENT '评论用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint(0) NULL DEFAULT NULL COMMENT '父评论ID，null表示顶级评论',
  `status` int(0) NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-隐藏',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_video_id`(`video_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `fk_comment_video`(`video_id`) USING BTREE,
  INDEX `fk_comment_user`(`user_id`) USING BTREE,
  INDEX `fk_comment_parent`(`parent_id`) USING BTREE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (3, 8, 4, '好看！！！', NULL, 0, '2026-01-28 23:18:19', '2026-01-28 23:18:19');
INSERT INTO `comment` VALUES (4, 8, 5, '谢谢喜欢', 3, 0, '2026-01-28 23:18:38', '2026-01-28 23:18:38');
INSERT INTO `comment` VALUES (5, 12, 5, '外向的孤独患者自我拉扯~', NULL, 0, '2026-01-30 00:43:48', '2026-01-30 00:43:48');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `from_user_id` bigint(0) NOT NULL,
  `type` int(0) NOT NULL,
  `video_id` bigint(0) NULL DEFAULT NULL,
  `comment_id` bigint(0) NULL DEFAULT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_read` int(0) NOT NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 2, 4, 2, 1, 1, '发表我的第一条评论', 0, '2026-01-28 23:13:26');
INSERT INTO `message` VALUES (2, 4, 5, 3, 1, 2, '看到了', 1, '2026-01-28 23:14:04');
INSERT INTO `message` VALUES (3, 5, 4, 2, 8, 3, '好看！！！', 1, '2026-01-28 23:18:19');
INSERT INTO `message` VALUES (4, 4, 5, 3, 8, 4, '谢谢喜欢', 1, '2026-01-28 23:18:38');
INSERT INTO `message` VALUES (5, 5, 4, 1, 8, NULL, '赞了你的视频', 1, '2026-01-28 23:18:57');
INSERT INTO `message` VALUES (6, 5, 1, 4, 12, NULL, '您的视频《【官方ENCORE - 合肥站】《孤独患者》｜陈奕迅FEAR AND DREAMS 合肥站｜第六场 5 JAN 2025 ENCORE》审核通过。备注: 审核通过', 1, '2026-01-29 23:53:22');
INSERT INTO `message` VALUES (7, 5, 1, 4, 11, NULL, '您的视频《【官方ENCORE - 合肥站】《零下几分钟》｜陈奕迅FEAR AND DREAMS 合肥站｜第六场 5 JAN 2025 ENCORE》审核通过。备注: 审核通过', 1, '2026-01-29 23:53:23');
INSERT INTO `message` VALUES (8, 5, 1, 4, 10, NULL, '您的视频《【官方ENCORE - 悉尼站】《信任》｜陈奕迅FEAR AND DREAMS 悉尼站｜第二场 15 MAR 2025 ENCORE》审核通过。备注: 审核通过', 1, '2026-01-29 23:53:23');
INSERT INTO `message` VALUES (9, 5, 1, 4, 9, NULL, '您的视频《【官方ENCORE - 澳门站】《防不胜防》｜陈奕迅FEAR AND DREAMS 澳门站｜10 AUG 2025 ENCORE》审核通过。备注: 审核通过', 1, '2026-01-29 23:53:24');
INSERT INTO `message` VALUES (10, 4, 5, 4, 13, NULL, '您的视频《【官方ENCORE - 澳门站】《粤语残片》｜陈奕迅FEAR AND DREAMS 澳门站｜10 AUG 2025 ENCORE》审核通过', 0, '2026-01-30 21:59:06');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` int(0) NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户 1-城市管理员',
  `status` int(0) NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-禁用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email`) USING BTREE,
  INDEX `idx_role`(`role`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123456', '系统管理员', 'admin@cvs.com', NULL, 'http://localhost:8080/uploads/avatar/20260121232128_7fb19730.png', 1, 0, '2026-01-21 19:33:32', '2026-01-21 23:21:29');
INSERT INTO `user` VALUES (2, 'user1', '123456', '音乐爱好者', 'user1@example.com', '13800138001', NULL, 0, 0, '2026-01-21 19:33:32', '2026-01-21 19:33:32');
INSERT INTO `user` VALUES (3, 'user2', '123456', '演唱会粉丝', 'user2@example.com', '13800138002', NULL, 0, 0, '2026-01-21 19:33:32', '2026-01-21 19:33:32');
INSERT INTO `user` VALUES (4, 'user3', '123456', 'Lazydu', '', '', 'http://localhost:8080/uploads/avatar/20260122221136_abb7bdad.jpg', 0, 0, '2026-01-22 22:11:41', '2026-01-30 20:42:29');
INSERT INTO `user` VALUES (5, '2240708107', 'Lazydu0512', '渡', '1242698314@qq.com', '13392696613', 'http://localhost:8080/uploads/avatar/20260123001555_18d49557.jpg', 2, 0, '2026-01-22 23:26:48', '2026-01-23 00:15:55');

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS `video`;
CREATE TABLE `video`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '视频ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '视频描述',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `concert_date` date NOT NULL COMMENT '演唱会日期',
  `video_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频URL',
  `thumbnail_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缩略图URL',
  `duration` int(0) NULL DEFAULT NULL COMMENT '视频时长(秒)',
  `uploader_id` bigint(0) NOT NULL COMMENT '上传者ID',
  `status` int(0) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-审核通过 2-审核拒绝',
  `reviewer_id` bigint(0) NULL DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审核意见',
  `view_count` int(0) NOT NULL DEFAULT 0 COMMENT '观看次数',
  `like_count` int(0) NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `collect_count` int(0) NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `offline_status` int(0) NULL DEFAULT 0 COMMENT '下架状态 0:正常 1:用户下架 2:管理员下架',
  `offline_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下架理由',
  `offline_time` datetime(0) NULL DEFAULT NULL COMMENT '下架时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_uploader_id`(`uploader_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_city`(`city`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `fk_uploader`(`uploader_id`) USING BTREE,
  INDEX `fk_reviewer`(`reviewer_id`) USING BTREE,
  CONSTRAINT `fk_video_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_video_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video
-- ----------------------------
INSERT INTO `video` VALUES (8, '【官方ENCORE - 佛山站】《热带雨林》｜陈奕迅FEAR AND DREAMS 佛山站｜第四场 24 JAN 2025 ENCORE', '《热带雨林》\n曲: 孙伟明  \n词: 黄伟文  \n\n\n明明并非夏季 但我竟会觉得热 呼吸到 南美的暑气\n闲谈被关掉了 伴奏只有那烟味 听风扇 转得多诡秘\n\n句句我爱你 布满青苔\n别拥抱 懒得要死\n\n摇摇欲倒是你 像客厅里这腹地 将驶进 数辆推土机\n相恋的关系 犹像 热带雨林\n逐分钟 消失 4.8 哩\n\n最 茂盛的恋爱 静静盖满地\n是否 这样的韵律 也算自然定理\n这 静默的灾劫 没法可退避\n像相对没相对论 我闷还是你\n\n连绵绿色万里 静悄得鸟兽走避 只得我 蛮荒中等你\n当初的亲密 犹像 热带雨林\n逐分钟 消失 4.8哩\n\n最 茂盛的恋爱 静静盖满地\n是否 这样的韵律 也算自然定理\n这 静默的灾劫 没法可退避\n像相对没相对论 我闷还是你\n\n太 热烈的恋爱 用去的储备\n或者 以后的以后 永远亦难治理\n这 静默的灾劫 没法可退避\n像相对没相对论 我有病还是你', '佛山', '2025-01-24', 'http://localhost:8080/uploads/video/20260123010644_1fca05f1.mp4', 'http://localhost:8080/uploads/cover/20260122232833_0f8c3dc4.jpg', 261, 5, 1, 1, '2026-01-23 20:48:54', '', 3, 1, 1, '2026-01-22 23:28:34', '2026-01-29 23:53:52', 0, NULL, '2026-01-28 22:34:35');
INSERT INTO `video` VALUES (9, '【官方ENCORE - 澳门站】《防不胜防》｜陈奕迅FEAR AND DREAMS 澳门站｜10 AUG 2025 ENCORE', '为何喝过那咖啡杯 无故失踪了\n家里却彷佛 增添了数本新书\n为何你那床头玩具熊 再找不到\n花樽的花 偏偏天天转色\n\n也许这刻你仍然尚未发觉\n家中有这一个访客\n时时漏夜冒昧探你\n将琐碎东西带走\n然后又放低\n\n在你的唱机放低唱片是我\n算是暗中一起分享过首歌\n从你的套房带走被单是我\n你睡过的 至少我都睡过\n\n为何那个故障手机 无故修好了\n梳妆台怎么 这么快没有香水\n为何有雨门前就突然 有一把伞\n相簿的相 偏偏天天变少\n\n也许这刻你仍然尚未发觉\n家中有这一个访客\n时时漏夜冒昧探你\n将琐碎东西带走\n然后又放低\n\n在你的唱机放低唱片是我\n算是暗中一起分享过首歌\n从你的套房带走被单是我\n你睡过的 至少我都\n\n从你工作间带走废纸是我\n照着你的笔迹写封信给我\n在你抽屉中放低戒指是我\n你就算知 也不会想 是我', '澳门', '2025-08-10', 'http://localhost:8080/uploads/video/20260129234019_02901966.mp4', 'http://localhost:8080/uploads/cover/20260129234019_317f5eb1.jpg', 261, 5, 1, 1, '2026-01-29 23:53:24', '审核通过', 0, 0, 0, '2026-01-29 23:40:20', '2026-01-29 23:53:24', 0, NULL, NULL);
INSERT INTO `video` VALUES (10, '【官方ENCORE - 悉尼站】《信任》｜陈奕迅FEAR AND DREAMS 悉尼站｜第二场 15 MAR 2025 ENCORE', '你无谓迫我 打开刚才那一版\n早前便讲过 旧友聚餐\n你擒住不放 相中痴缠那一挽\n平凡活动社交也要防范\n\n肯相信我未 肯体恤我未\n双方要靠信任还是放饵\n请相信我哋 请珍惜我哋\n为何总不留余地\n\n你无谓偷看 手机中号码一闪\n私邮和短讯 逐个查验\n我门外经过 紧张得像过安检\n怀疑令实相会更快呈现\n\n肯相信我未 肯体恤我未\n双方要靠信任还是放饵\n请相信我哋 请珍惜我哋\n谣言没半点道理\n\n讲真厅中那扩音机\n奢侈价格不菲\n而我施展过 只此一次 谎言绝技\n只不过是 一点怕事\n撕走价钱 不讲你知\n不想有象征式意义\n成为你戒示\n\n终于肯相信我未 肯体恤我未\n双方要靠信任还是放饵\n请相信我哋 请珍惜我哋\n谣言是与非 瞎猜了还忌\n\n肯相信我未 肯体恤我未\n怎么砸破信任才是胜利\n请相信我哋 请珍惜我哋\n来年多点人情味', '悉尼', '2025-03-15', 'http://localhost:8080/uploads/video/20260129234656_d329170a.mp4', 'http://localhost:8080/uploads/cover/20260129234656_fe0fb176.jpg', 214, 5, 1, 1, '2026-01-29 23:53:23', '审核通过', 0, 0, 0, '2026-01-29 23:46:57', '2026-01-29 23:53:23', 0, NULL, NULL);
INSERT INTO `video` VALUES (11, '【官方ENCORE - 合肥站】《零下几分钟》｜陈奕迅FEAR AND DREAMS 合肥站｜第六场 5 JAN 2025 ENCORE', '想念像蚊子神出鬼没\n咬我一口但我没喊痛\n走过地铁白烟纽约寒冬\n走不过 心中的黑洞\n\n我呼吸 吸不到需要的热\n沿路上飘来爵士老歌\n老先生 带着孩子等车\n我在这\n\n零下几分钟 把时光暂停了\n把找不到路的我给冰冻\n这时间地点 对于我这游客 只等于寂寞\n\n可乐拌饭了几天以后\n忘了住这旅馆第几周\n混乱的世界加一公克寂寞\n成就了 最完美的心痛\n\n没关系 事情没那么严重\n不就是爱念了莫名的咒\n人的心 脆弱得像个气球 \n被狠狠戳破 \n\n零下几分钟 时代广场之中\n我数不清的记忆咆哮着\n冷风加音乐 是双倍的折磨 享受的人是我\n\n要或不要都没用 Oh Oh\n\n零下几分钟 把时光暂停了\n把找不到路的我给冰冻\n这时间地点 对于我这游客 只等于寂寞\n\n零下几分钟 时代广场之中\n数不清的记忆咆哮着\n冷风加音乐 天知道 享受的是我\n\n要或不要都没用 Oh Oh', '合肥', '2025-01-05', 'http://localhost:8080/uploads/video/20260129235016_cb35e35e.mp4', 'http://localhost:8080/uploads/cover/20260129235016_f782dcbc.jpg', 256, 5, 1, 1, '2026-01-29 23:53:23', '审核通过', 0, 0, 0, '2026-01-29 23:50:17', '2026-01-29 23:53:23', 0, NULL, NULL);
INSERT INTO `video` VALUES (12, '【官方ENCORE - 合肥站】《孤独患者》｜陈奕迅FEAR AND DREAMS 合肥站｜第六场 5 JAN 2025 ENCORE', '欢笑声　欢呼声\n炒热气氛　心却很冷\n聚光灯　是种蒙恩\n我却不能　喊等一等\n\n我真佩服我　还能幽默\n掉眼泪时　用笑掩过\n怕人看破　顾虑好多\n不谈寂寞　我们就都快活\n\n我不唱声嘶力竭的情歌\n不表示没有心碎的时刻\n我不曾摊开伤口任宰割\n愈合　就无人晓得　我内心挫折\n活像个孤独患者　自我拉扯\n外向的孤独患者　有何不可\n\n笑越大声　越是残忍\n挤满体温　室温更冷\n万一关灯　空虚扰人\n我却不能　喊等一等\n\n你说你爱我　却一直说\n说我不该　窝在角落\n策划逃脱　这也有错\n连我脆弱　的权利都掠夺\n\n我不唱声嘶力竭的情歌\n不表示没有心碎的时刻\n我不曾摊开伤口任宰割\n愈合　就无人晓得　我内心挫折\n活像个孤独患者　自我拉扯\n外向的孤独患者　有何不可\n\n我不要声嘶力竭的情歌\n来提示我需要你的时刻\n表面镇定并不是保护色\n反而　是要你懂得　我不知为何\n活像个孤独患者　自我拉扯\n外向的孤独患者　需要认可', '合肥', '2025-01-05', 'http://localhost:8080/uploads/video/20260129235241_ff491bb1.mp4', 'http://localhost:8080/uploads/cover/20260129235241_5ab06275.jpg', 269, 5, 1, 1, '2026-01-29 23:53:22', '审核通过', 3, 0, 0, '2026-01-29 23:52:42', '2026-01-30 01:01:44', 0, NULL, NULL);
INSERT INTO `video` VALUES (13, '【官方ENCORE - 澳门站】《粤语残片》｜陈奕迅FEAR AND DREAMS 澳门站｜10 AUG 2025 ENCORE', '乔迁那日打扫废物家居仿似开战\n无意发现当天穿返学夏季衬衣\n奇怪却是茄汁污垢 渗在这衬衣 布章外边\n极其大意 为何如此\n想那日初次约会心惊手震胆颤\n忙里泄露各种的丑态像丧尸\n而尴尬是快餐厅里 我误把浆汁四周乱溅\n骇人场面相当讽刺\n你及时递上餐纸 去为我清洗衬衣\n刹那间身体的触碰大件事\n今天看这段历史 像褪色午夜残片\n笑话情节 此刻变窝心故事\n现时大了 那种心跳难重演\n极灿烂时光一去难再遇上一次\n怎努力都想不起初恋怎会改变\n情侣数字我屈指一算大概知\n奇怪却是每恋一次 震撼总逐渐变得越浅\n令人动心只得那次\n有没捱坏了身子 会为哪位披嫁衣\n你有否挂念当天这丑小子\n今天看那段历史 像褪色午夜残片\n笑话情节 此刻变窝心故事\n现时大了 那种心跳难重演\n极灿烂时光一去难再遇上一次\n在混乱杂物当中找到失去的往事\n但现在杂物与我举家将会搬迁\n让记念成历史\n想想那旧时日子 像褪色午夜残片\n任何情节 今天多一种意义\n现时大了 那种心跳难重演\n极爆裂场面想再遇确实靠天意', '澳门', '2025-08-10', 'http://localhost:8080/uploads/video/20260130204318_d8026967.mp4', 'http://localhost:8080/uploads/cover/20260130204318_34e7e815.jpg', 273, 4, 1, 5, '2026-01-30 21:59:06', '', 0, 0, 0, '2026-01-30 20:43:19', '2026-01-30 21:59:06', 0, NULL, NULL);

-- ----------------------------
-- Table structure for video_collect
-- ----------------------------
DROP TABLE IF EXISTS `video_collect`;
CREATE TABLE `video_collect`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `video_id` bigint(0) NOT NULL COMMENT '视频ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_collect_video_user`(`video_id`, `user_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `fk_collect_video`(`video_id`) USING BTREE,
  INDEX `fk_collect_user`(`user_id`) USING BTREE,
  CONSTRAINT `fk_collect_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_collect_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2014305702247452675 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_collect
-- ----------------------------
INSERT INTO `video_collect` VALUES (2014305702247452674, 8, 4, '2026-01-28 23:18:57');

-- ----------------------------
-- Table structure for video_like
-- ----------------------------
DROP TABLE IF EXISTS `video_like`;
CREATE TABLE `video_like`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `video_id` bigint(0) NOT NULL COMMENT '视频ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_video_user`(`video_id`, `user_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `fk_like_video`(`video_id`) USING BTREE,
  INDEX `fk_like_user`(`user_id`) USING BTREE,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_like_video` FOREIGN KEY (`video_id`) REFERENCES `video` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2014004065586995204 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_like
-- ----------------------------
INSERT INTO `video_like` VALUES (2014004065586995203, 8, 4, '2026-01-28 23:18:57');

SET FOREIGN_KEY_CHECKS = 1;
