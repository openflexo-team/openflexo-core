package org.openflexo.foundation.lsp.websocket;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Web configuration class enabling CORS for API endpoints.
 * 
 * Draft configuration class required to support the API.
 * It is used by the DraftFileController class.
 */
@Configuration
public class DraftWebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS to allow all origins, headers, and GET/POST methods for /api/**.
     *
     * @param registry the {@link CorsRegistry} to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}