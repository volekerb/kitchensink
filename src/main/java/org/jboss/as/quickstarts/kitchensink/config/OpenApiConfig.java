package org.jboss.as.quickstarts.kitchensink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI documentation
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure OpenAPI documentation details
     */
    @Bean
    public OpenAPI kitchensinkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kitchensink API")
                        .description("Spring Boot migration of the JBoss Kitchensink application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("JBoss Kitchensink")
                                .url("https://github.com/jboss-developer/jboss-eap-quickstarts"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
