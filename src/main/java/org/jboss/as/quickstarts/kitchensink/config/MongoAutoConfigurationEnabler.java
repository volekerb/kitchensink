package org.jboss.as.quickstarts.kitchensink.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration class enables MongoDB auto-configuration when mongodb.enabled=true.
 * It overrides the default exclusion in application.properties and application-prod.properties.
 */
@Configuration
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
@EnableAutoConfiguration(exclude = {})
public class MongoAutoConfigurationEnabler {
    // This class doesn't need any methods, it just enables MongoDB auto-configuration
    // when mongodb.enabled=true through the @EnableAutoConfiguration annotation
}