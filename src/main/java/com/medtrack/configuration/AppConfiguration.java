package com.medtrack.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.NonNull;

@Configuration
public class AppConfiguration {

    @Bean
    public Thread generateThread(Runnable runnable) {
        return new Thread(runnable);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*") // Allow all origins or specify frontend URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}
// This configuration class defines a bean for creating a new thread.
// The thread is created using a Runnable instance passed as an argument to the
// threadBean method.
// This allows for the creation of a thread that can execute the specified
// Runnable task when started.
// The @Configuration annotation indicates that this class contains Spring
// configuration, and the @Bean annotation marks
// the method as a bean definition. This enables Spring to manage the lifecycle
// of the thread bean within the application context.