package org.jboss.as.quickstarts.kitchensink.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration that is only activated when mongodb.enabled=false or not set
 */
@Configuration
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "false", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "org.jboss.as.quickstarts.kitchensink.data")
public class JpaConfig {
    // Configuration is handled by Spring Boot auto-configuration
}