package org.jboss.as.quickstarts.kitchensink.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration class explicitly disables MongoDB auto-configuration when mongodb.enabled=false.
 * It ensures that MongoDB-related beans are not created when MongoDB is not enabled.
 */
@Configuration
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "false", matchIfMissing = true)
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    MongoRepositoriesAutoConfiguration.class
})
public class MongoAutoConfigurationDisabler {
    // This class doesn't need any methods, it just disables MongoDB auto-configuration
    // when mongodb.enabled=false through the @EnableAutoConfiguration annotation
}