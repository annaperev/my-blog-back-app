package ru.yandex.practicum.blog.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * Central MVC config for DispatcherServlet context.
 * Typical interview point: @EnableWebMvc activates Spring MVC infrastructure
 * (handler mappings, JSON converters, validation support, etc).
 */
@Configuration
@EnableWebMvc
@Import(DataConfig.class)
@ComponentScan(basePackages = "ru.yandex.practicum.blog")
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * CORS (Cross-Origin Resource Sharing) policy for browser clients.
     * Browser blocks JS access to responses from another origin unless server explicitly allows it.
     *
     * Frontend origin in this project: http://localhost
     * Backend origin in this project:  http://localhost:8080
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
