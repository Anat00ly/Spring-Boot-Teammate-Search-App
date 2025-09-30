package com.example.research2.SpringBoot.config;

import com.example.research2.SpringBoot.ActivityInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    private final ActivityInterceptor activityInterceptor;

    public MvcConfig(ActivityInterceptor activityInterceptor) {
        this.activityInterceptor = activityInterceptor;
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // Create Path object properly handling Windows paths
            Path uploadDir = Paths.get(uploadPath);

            // Ensure directory exists
            uploadDir.toFile().mkdirs();

            // Convert to proper file URL format
            String absolutePath = uploadDir.toAbsolutePath().toString();
            if (!absolutePath.endsWith(File.separator)) {
                absolutePath += File.separator;
            }

            // Register resource handler
            registry.addResourceHandler("/avatars/**")
                    .addResourceLocations("file:" + absolutePath);

        } catch (Exception e) {
            // Log error and use fallback
            System.err.println("Error setting up resource handler: " + e.getMessage());
            // Fallback to a safe default path
            registry.addResourceHandler("/avatars/**")
                    .addResourceLocations("file:uploads/avatars/");
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(activityInterceptor);
    }
}