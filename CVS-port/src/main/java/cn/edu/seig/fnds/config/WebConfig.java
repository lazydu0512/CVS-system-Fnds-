package cn.edu.seig.fnds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置类
 * 
 * @author lazzydu
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

        @Autowired
        private LoginInterceptor loginInterceptor;

        /**
         * 配置跨域
         */
        @Override
        public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                                .allowedOriginPatterns("*")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*")
                                .allowCredentials(true)
                                .maxAge(3600);

                // 为上传的静态资源配置跨域
                registry.addMapping("/uploads/**")
                                .allowedOriginPatterns("*")
                                .allowedMethods("GET", "OPTIONS")
                                .allowedHeaders("*")
                                .allowCredentials(true)
                                .maxAge(3600);
        }

        /**
         * 配置拦截器
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(loginInterceptor)
                                .addPathPatterns("/api/**")
                                .excludePathPatterns(
                                                "/api/user/login",
                                                "/api/user/register",
                                                "/api/video/list",
                                                "/api/video/user/**",
                                                "/api/comment/video/**",
                                                "/api/city", // 放行城市列表接口
                                                "/api/file/**", // 放行文件上传接口
                                                "/uploads/**" // 放行上传的静态资源
                                );
                // 注意：/api/video/{id} 不再排除，因为它会匹配 /api/video/upload
                // 视频详情接口改为在Controller中处理（不需要登录）
        }

        /**
         * 配置静态资源映射
         */
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 获取当前工作目录下的uploads目录
                String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

                // 使用Paths.get().toUri()确保路径格式正确 (例如处理Windows的反斜杠)
                String uploadUri = java.nio.file.Paths.get(uploadPath).toUri().toString();

                System.out.println("=== 静态资源映射配置 ===");
                System.out.println("uploads目录路径: " + uploadPath);
                System.out.println("uploads URI: " + uploadUri);

                // 映射 /uploads/** 到本地文件系统
                // 确保路径以 / 结尾，否则可能导致无法访问
                if (!uploadUri.endsWith("/")) {
                        uploadUri += "/";
                }
                registry.addResourceHandler("/uploads/**")
                                .addResourceLocations(uploadUri);

                // 映射 /static/** 到 classpath:/static/
                registry.addResourceHandler("/static/**")
                                .addResourceLocations("classpath:/static/");
        }
}