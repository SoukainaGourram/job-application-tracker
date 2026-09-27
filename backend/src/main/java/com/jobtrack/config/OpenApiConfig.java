package com.jobtrack.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "JobTrack API",
                version = "1.0",
                description = "API REST pour la plateforme de suivi de candidatures JobTrack",
                contact = @Contact(
                        name = "JobTrack",
                        email = "contact@jobtrack.io"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Serveur de développement")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer Authentication — saisissez votre token sans le préfixe 'Bearer'",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
