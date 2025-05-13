package org.example.configmapping;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Spring MVC pour la fonctionnalité de visualisation des mappings
 */
@Configuration
public class MappingVisualizationConfig implements WebMvcConfigurer {

    /**
     * Configure les gestionnaires de ressources statiques
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/mapping-visualizer/**")
                .addResourceLocations("classpath:/static/mapping-visualizer/");
    }

    /**
     * Configure les contrôleurs de vue pour les pages HTML
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/static/mapping-visualizer").setViewName("forward:/mapping-visualizer/index.html");
        registry.addViewController("/static/mapping-visualizer/").setViewName("forward:/mapping-visualizer/index.html");
    }

    /**
     * Configure CORS pour permettre les appels depuis différentes origines (utile en développement)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/mappings/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }

    /**
     * Configurateur Web MVC pour l'application
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/mapping-visualizer/**")
                        .addResourceLocations("classpath:/static/mapping-visualizer/");
            }
        };
    }

}