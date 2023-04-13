package ru.vsu.cs.picstorm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Arrays;
import java.util.Optional;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        Contact contactInfo = new Contact()
                .name("Kirill Skofenko")
                .email("tor505274@gmail.com")
                .url("https://vk.com/goosepusher");

        Info apiInfo = new Info().title("PicStorm API")
                .description("Full API documentation of PicStorm application")
                .version("v0.0.1")
                .contact(contactInfo);

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        Components apiComponents = new Components()
                .addSecuritySchemes("JWT Authentication", securityScheme);

        return new OpenAPI()
                .info(apiInfo)
                .components(apiComponents);
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            Optional<PreAuthorize> preAuthorizeAnnotation = Optional.ofNullable(handlerMethod.getMethodAnnotation(PreAuthorize.class));
            StringBuilder sb = new StringBuilder();
            if (preAuthorizeAnnotation.isPresent()) {
                String requiredAuthority = preAuthorizeAnnotation.get().value();
                for (String s : Arrays.asList("hasAuthority('", "')")) {
                    requiredAuthority = requiredAuthority.replace(s,"");
                }
                sb.append("Endpoint requires **")
                        .append(requiredAuthority)
                        .append("** permission.");
            } else {
                sb.append("Endpoint is **public**");
            }
            sb.append("<br/><br/>");
            if (operation.getDescription() != null) {
                sb.append(operation.getDescription());
            }
            operation.setDescription(sb.toString());
            return operation;
        };
    }
}