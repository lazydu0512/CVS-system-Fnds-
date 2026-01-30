package cn.edu.seig.fnds.config;

import cn.edu.seig.fnds.entity.User;
import cn.edu.seig.fnds.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        // 放行视频详情接口 /api/video/{数字id}（支持雪花算法生成的长ID）
        if (uri.matches("/api/video/\\d{1,20}")) {
            return true;
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        System.out.println("=== 拦截器调试 ===");
        System.out.println("请求路径: " + uri);
        System.out.println("Authorization Header: " + token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            System.out.println("提取的Token: " + token);
            User user = userService.getUserByToken(token);
            System.out.println("查询到的用户: " + (user != null ? user.getUsername() : "null"));
            if (user != null) {
                // 将当前用户存入request属性，方便Controller获取
                request.setAttribute("currentUser", user);
                return true;
            }
        }

        // Token无效或不存在，返回401
        System.out.println("认证失败，返回401");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
        return false;
    }
}
