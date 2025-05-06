package org.jboss.as.quickstarts.kitchensink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
    "mongodb.enabled=false"
})
class KitchensinkApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        // Verify that the application context loads successfully
        assertNotNull(context, "Application context should be available");
    }

    @Test
    void webConfigurationIsAvailable() {
        // Verify that the web configuration is available
        // There are multiple WebMvcConfigurer beans, so we need to get them as a map
        Map<String, WebMvcConfigurer> webMvcConfigurers = context.getBeansOfType(WebMvcConfigurer.class);
        assertNotNull(webMvcConfigurers, "WebMvcConfigurer beans should be available");
        assertFalse(webMvcConfigurers.isEmpty(), "At least one WebMvcConfigurer bean should be available");
    }

    @Test
    void serverPortLoggerHandlesEvent() {
        // Create an instance of the ServerPortLogger
        KitchensinkApplication.ServerPortLogger serverPortLogger = new KitchensinkApplication.ServerPortLogger();

        // Mock the event
        ServletWebServerInitializedEvent event = mock(ServletWebServerInitializedEvent.class);
        org.springframework.boot.web.server.WebServer webServer = mock(org.springframework.boot.web.server.WebServer.class);
        when(event.getWebServer()).thenReturn(webServer);
        when(webServer.getPort()).thenReturn(8080);

        // This should not throw any exceptions
        serverPortLogger.onApplicationEvent(event);
    }

    @Test
    void mongoAutoConfigurationIsDisabled() {
        // Verify that MongoDB auto-configuration beans are not present in the application context
        // when mongodb.enabled=false

        // MongoAutoConfiguration should not be present
        assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, 
            () -> context.getBean(MongoAutoConfiguration.class),
            "MongoAutoConfiguration bean should not be available when mongodb.enabled=false");

        // MongoDataAutoConfiguration should not be present
        assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, 
            () -> context.getBean(MongoDataAutoConfiguration.class),
            "MongoDataAutoConfiguration bean should not be available when mongodb.enabled=false");

        // MongoRepositoriesAutoConfiguration should not be present
        assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, 
            () -> context.getBean(MongoRepositoriesAutoConfiguration.class),
            "MongoRepositoriesAutoConfiguration bean should not be available when mongodb.enabled=false");

        // MongoTemplate should not be present
        assertThrows(org.springframework.beans.factory.NoSuchBeanDefinitionException.class, 
            () -> context.getBean(MongoTemplate.class),
            "MongoTemplate bean should not be available when mongodb.enabled=false");
    }
}
