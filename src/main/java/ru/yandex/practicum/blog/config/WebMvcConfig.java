package ru.yandex.practicum.blog.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Central MVC config for DispatcherServlet context.
 * Typical interview point: @EnableWebMvc activates Spring MVC infrastructure
 * (handler mappings, JSON converters, validation support, etc).
 */
@Configuration
@EnableWebMvc
@Import(DataConfig.class)
@ComponentScan(basePackages = "ru.yandex.practicum.blog")
public class WebMvcConfig {
}
