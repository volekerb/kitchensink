package org.jboss.as.quickstarts.kitchensink.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration that is only activated when mongodb.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
@EnableMongoRepositories(basePackages = "org.jboss.as.quickstarts.kitchensink.data.mongo")
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        // Extract database name from URI or use default
        String databaseName = "kitchensink";
        if (mongoUri.contains("/")) {
            String[] parts = mongoUri.split("/");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                // Handle query parameters if present
                if (lastPart.contains("?")) {
                    databaseName = lastPart.split("\\?")[0];
                } else {
                    databaseName = lastPart;
                }
            }
        }
        return new MongoTemplate(mongoClient(), databaseName);
    }
}
