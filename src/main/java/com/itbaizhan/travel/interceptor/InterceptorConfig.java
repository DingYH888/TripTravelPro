package com.itbaizhan.travel.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

//拦截器配置类
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    // 上传文件存储目录
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //创建拦截器对象并指定其拦截的路径
        registry.addInterceptor(new MemberInterceptor())
                .addPathPatterns("/frontdesk/favorite/**", "/frontdesk/my_favorite");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传的图片：优先从上传目录读取，同时兼容项目内置的示例图片
        String uploadLocation = Paths.get(uploadPath).toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation = uploadLocation + "/";
        }
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("classpath:/static/upload/", uploadLocation);
    }
}
