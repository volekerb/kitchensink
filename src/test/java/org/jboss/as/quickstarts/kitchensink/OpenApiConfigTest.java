package org.jboss.as.quickstarts.kitchensink;

import org.jboss.as.quickstarts.kitchensink.config.OpenApiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class OpenApiConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        // Verify that the OpenApiConfig bean is created
        OpenApiConfig openApiConfig = context.getBean(OpenApiConfig.class);
        assertNotNull(openApiConfig, "OpenApiConfig bean should be available");
    }
}
