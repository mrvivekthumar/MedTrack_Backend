// Updated SwaggerConfig.java for Spring WebFlux
package com.medtrack.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3 configuration for MedTrack API (WebFlux Compatible)
 * Provides comprehensive API documentation with JWT authentication support
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI medTrackOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .addSecurityItem(createSecurityRequirement())
                .components(createComponents());
    }

    /**
     * Creates API information metadata
     */
    private Info createApiInfo() {
        return new Info()
                .title("MedTrack API")
                .description(
                        """
                                ## MedTrack - Medicine Tracking & Management System

                                A comprehensive RESTful API for managing medicines, tracking usage, and sending notifications.

                                ### Features:
                                - 👤 **User Management**: Registration, authentication, profile management
                                - 💊 **Medicine Management**: Add, update, delete, and track medicines
                                - 📊 **Usage Tracking**: Log medicine intake and view usage statistics
                                - 🔔 **Smart Notifications**: Expiry alerts, low stock warnings via Kafka
                                - 📈 **Analytics**: User statistics and adherence tracking
                                - 🔒 **Security**: JWT-based authentication and authorization

                                ### Authentication:
                                This API uses JWT (JSON Web Token) for authentication. Include the token in the Authorization header:
                                ```
                                Authorization: Bearer <your-jwt-token>
                                ```

                                ### Getting Started:
                                1. Register a new user account using `/api/v1/user/signup`
                                2. Sign in to get your JWT token using `/api/v1/user/signin`
                                3. Use the token to access protected endpoints

                                ### API Response Format:
                                All responses follow a consistent format with proper HTTP status codes.
                                """)
                .version("1.0.0")
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * Creates contact information
     */
    private Contact createContact() {
        return new Contact()
                .name("MedTrack Development Team")
                .email("support@medtrack.com")
                .url("https://medtrack.com");
    }

    /**
     * Creates license information
     */
    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Creates server configurations for different environments
     */
    private List<Server> createServers() {
        Server developmentServer = new Server()
                .url("http://localhost:8888")
                .description("Development Server");

        Server productionServer = new Server()
                .url("https://api.medtrack.com")
                .description("Production Server");

        return List.of(developmentServer, productionServer);
    }

    /**
     * Creates security requirements for JWT authentication
     */
    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }

    /**
     * Creates components including security schemes
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", createBearerAuthScheme());
    }

    /**
     * Creates JWT Bearer token security scheme
     */
    private SecurityScheme createBearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token obtained from login endpoint");
    }
}