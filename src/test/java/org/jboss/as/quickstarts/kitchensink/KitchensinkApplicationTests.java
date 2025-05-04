package org.jboss.as.quickstarts.kitchensink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
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
}
