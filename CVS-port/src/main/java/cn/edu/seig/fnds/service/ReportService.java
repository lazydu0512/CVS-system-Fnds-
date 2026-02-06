package cn.edu.seig.fnds.service;

import cn.edu.seig.fnds.entity.*;
import cn.edu.seig.fnds.mapper.ReportMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报服务类
 * 
 * @author lazzydu
 */
@Service
public class ReportService extends ServiceImpl<ReportMapper, Report> {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CityManagerService cityManagerService;

    // 举报目标类型常量
    public static final int TARGET_TYPE_VIDEO = 0;
    public static final int TARGET_TYPE_COMMENT = 1;

    // 举报状态常量
    public static final int STATUS_PENDING = 0; // 待审核
    public static final int STATUS_APPROVED = 1; // 举报成功
    public static final int STATUS_REJECTED = 2; // 举报驳回

    /**
     * 提交举报
     */
    public boolean submitReport(Report report, Long currentUserId) {
        // 1. 验证目标是否存在，并获取被举报人ID和城市
        Long reportedUserId = null;
        String cityCode = null;

        if (report.getTargetType() == TARGET_TYPE_VIDEO) {
            // 举报视频
            Video video = videoService.getById(report.getTargetId());
            if (video == null) {
                throw new RuntimeException("被举报的视频不存在");
            }
            reportedUserId = video.getUploaderId();
            cityCode = video.getCity();
        } else if (report.getTargetType() == TARGET_TYPE_COMMENT) {
            // 举报评论
            Comment comment = commentService.getById(report.getTargetId());
            if (comment == null) {
                throw new RuntimeException("被举报的评论不存在");
            }
            reportedUserId = comment.getUserId();

            // 通过评论的视频ID获取城市
            Video video = videoService.getById(comment.getVideoId());
            if (video != null) {
                cityCode = video.getCity();
            }
        } else {
            throw new RuntimeException("无效的举报目标类型");
        }

        // 2. 验证不能举报自己
        if (currentUserId.equals(reportedUserId)) {
            throw new RuntimeException("不能举报自己的内容");
        }

        // 3. 检查是否已经举报过（防止重复举报）
        long existingReportCount = count(Wrappers.<Report>lambdaQuery()
                .eq(Report::getReporterId, currentUserId)
                .eq(Report::getTargetId, report.getTargetId())
                .eq(Report::getTargetType, report.getTargetType()));

        if (existingReportCount > 0) {
            throw new RuntimeException("您已经举报过该内容，请勿重复举报");
        }

        // 4. 填充举报信息
        report.setReporterId(currentUserId);
        report.setReportedUserId(reportedUserId);
        report.setCityCode(cityCode);
        report.setStatus(STATUS_PENDING);
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        // 5. 保存举报记录
        boolean saved = save(report);

        if (saved) {
            // 6. 通知管理员
            notifyAdmins(report, cityCode);
        }

        return saved;
    }

    /**
     * 通知城市管理员和系统管理员
     */
    private void notifyAdmins(Report report, String cityCode) {
        String targetType = report.getTargetType() == TARGET_TYPE_VIDEO ? "视频" : "评论";
        String content = "举报类型: " + report.getReasonCategory();

        System.out.println("=== 开始通知管理员 ===");
        System.out.println("举报ID: " + report.getId());
        System.out.println("城市代码: " + cityCode);
        System.out.println("举报人ID: " + report.getReporterId());

        // 1. 通知城市管理员（cityCode实际存储的是城市名称）
        if (cityCode != null && !cityCode.isEmpty()) {
            List<CityManager> cityManagers = cityManagerService.getManagersByCity(cityCode);
            System.out.println("找到城市管理员数量: " + (cityManagers != null ? cityManagers.size() : 0));

            if (cityManagers != null) {
                for (CityManager cm : cityManagers) {
                    System.out.println("通知城市管理员: " + cm.getUserId());
                    messageService.sendReportNotificationToAdmin(
                            cm.getUserId(),
                            report.getReporterId(), // 举报人作为发送者
                            report.getId(),
                            targetType,
                            content);
                }
            }
        } else {
            System.out.println("警告: 城市代码为空，无法通知城市管理员");
        }

        // 2. 通知系统管理员 (role = 1)
        List<User> systemAdmins = userService.list(Wrappers.<User>lambdaQuery()
                .eq(User::getRole, 1));
        System.out.println("找到系统管理员数量: " + (systemAdmins != null ? systemAdmins.size() : 0));

        if (systemAdmins != null) {
            for (User admin : systemAdmins) {
                System.out.println("通知系统管理员: " + admin.getId());
                messageService.sendReportNotificationToAdmin(
                        admin.getId(),
                        report.getReporterId(), // 举报人作为发送者
                        report.getId(),
                        targetType,
                        content);
            }
        }

        System.out.println("=== 通知管理员完成 ===");
    }

    /**
     * 获取举报列表（管理员）
     */
    public List<Report> getReportList(User reviewer, Integer status, String city) {
        var queryWrapper = Wrappers.<Report>lambdaQuery();

        // 根据状态过滤
        if (status != null) {
            queryWrapper.eq(Report::getStatus, status);
        }

        // 城市管理员只能看到自己管理的城市
        if (reviewer.getRole() == 2) {
            List<String> managedCities = cityManagerService.getManagedCityNames(reviewer.getId());
            if (managedCities.isEmpty()) {
                return List.of(); // 没有管理的城市
            }
            queryWrapper.in(Report::getCityCode, managedCities);
        }
        // 系统管理员可以看到所有举报
        else if (reviewer.getRole() == 1) {
            // 如果指定了城市，则过滤
            if (city != null && !city.isEmpty()) {
                queryWrapper.eq(Report::getCityCode, city);
            }
        } else {
            // 普通用户无权限
            return List.of();
        }

        queryWrapper.orderByDesc(Report::getCreateTime);
        List<Report> reports = list(queryWrapper);

        // 填充关联信息
        for (Report report : reports) {
            fillReportInfo(report);
        }

        return reports;
    }

    /**
     * 填充举报的关联信息
     */
    private void fillReportInfo(Report report) {
        // 填充举报者信息
        User reporter = userService.getById(report.getReporterId());
        if (reporter != null) {
            User safeReporter = new User();
            safeReporter.setId(reporter.getId());
            safeReporter.setNickname(reporter.getNickname());
            safeReporter.setAvatar(reporter.getAvatar());
            report.setReporter(safeReporter);
        }

        // 填充被举报者信息
        User reportedUser = userService.getById(report.getReportedUserId());
        if (reportedUser != null) {
            User safeReportedUser = new User();
            safeReportedUser.setId(reportedUser.getId());
            safeReportedUser.setNickname(reportedUser.getNickname());
            safeReportedUser.setAvatar(reportedUser.getAvatar());
            report.setReportedUser(safeReportedUser);
        }

        // 填充审核人信息
        if (report.getReviewerId() != null) {
            User reviewer = userService.getById(report.getReviewerId());
            if (reviewer != null) {
                User safeReviewer = new User();
                safeReviewer.setId(reviewer.getId());
                safeReviewer.setNickname(reviewer.getNickname());
                report.setReviewer(safeReviewer);
            }
        }

        // 填充目标内容
        if (report.getTargetType() == TARGET_TYPE_VIDEO) {
            Video video = videoService.getById(report.getTargetId());
            report.setTargetVideo(video);
        } else if (report.getTargetType() == TARGET_TYPE_COMMENT) {
            Comment comment = commentService.getById(report.getTargetId());
            report.setTargetComment(comment);
        }
    }

    /**
     * 审核通过举报
     */
    public boolean approveReport(Long reportId, Long reviewerId, String reviewComment) {
        Report report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if (report.getStatus() != STATUS_PENDING) {
            throw new RuntimeException("该举报已处理");
        }

        // 更新举报状态
        report.setStatus(STATUS_APPROVED);
        report.setReviewerId(reviewerId);
        report.setReviewTime(LocalDateTime.now());
        report.setReviewComment(reviewComment);
        report.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(report);

        if (updated) {
            // 根据举报类型执行相应操作
            if (report.getTargetType() == TARGET_TYPE_COMMENT) {
                // 评论软删除：修改内容为"该评论已删除"
                Comment comment = commentService.getById(report.getTargetId());
                if (comment != null) {
                    comment.setContent("该评论已删除");
                    comment.setStatus(1); // 设置为隐藏状态
                    comment.setUpdateTime(LocalDateTime.now());
                    commentService.updateById(comment);
                }
            } else if (report.getTargetType() == TARGET_TYPE_VIDEO) {
                // 视频下架
                Video video = videoService.getById(report.getTargetId());
                if (video != null) {
                    video.setOfflineStatus(2); // 管理员下架
                    video.setOfflineReason("违规内容被举报: " + report.getReasonCategory());
                    video.setOfflineTime(LocalDateTime.now());
                    video.setUpdateTime(LocalDateTime.now());
                    videoService.updateById(video);
                }
            }

            // 通知举报者
            String targetType = report.getTargetType() == TARGET_TYPE_VIDEO ? "视频" : "评论";
            messageService.sendReportResultNotification(
                    report.getReporterId(),
                    reviewerId, // 审核人作为发送者
                    reportId,
                    "您举报的" + targetType + "已处理成功，感谢您的反馈",
                    MessageService.TYPE_REPORT_APPROVED);

            // 通知被举报者
            messageService.sendReportResultNotification(
                    report.getReportedUserId(),
                    reviewerId, // 审核人作为发送者
                    reportId,
                    "您的" + targetType + "因违规被删除/下架。理由: " + report.getReasonCategory(),
                    MessageService.TYPE_CONTENT_REMOVED);
        }

        return updated;
    }

    /**
     * 驳回举报
     */
    public boolean rejectReport(Long reportId, Long reviewerId, String reviewComment) {
        Report report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if (report.getStatus() != STATUS_PENDING) {
            throw new RuntimeException("该举报已处理");
        }

        // 更新举报状态
        report.setStatus(STATUS_REJECTED);
        report.setReviewerId(reviewerId);
        report.setReviewTime(LocalDateTime.now());
        report.setReviewComment(reviewComment);
        report.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(report);

        if (updated) {
            // 通知举报者
            String targetType = report.getTargetType() == TARGET_TYPE_VIDEO ? "视频" : "评论";
            String rejectReason = reviewComment != null && !reviewComment.isEmpty()
                    ? "。理由: " + reviewComment
                    : "";

            messageService.sendReportResultNotification(
                    report.getReporterId(),
                    reviewerId, // 审核人作为发送者
                    reportId,
                    "您举报的" + targetType + "经审核未发现违规" + rejectReason,
                    MessageService.TYPE_REPORT_REJECTED);
        }

        return updated;
    }
}
