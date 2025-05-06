package org.jboss.as.quickstarts.kitchensink;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.jboss.as.quickstarts.kitchensink.config.OpenApiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "mongodb.enabled=false"
})
class OpenApiConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private OpenAPI openAPI;

    @Test
    void contextLoads() {
        // Verify that the OpenApiConfig bean is created
        OpenApiConfig openApiConfig = context.getBean(OpenApiConfig.class);
        assertNotNull(openApiConfig, "OpenApiConfig bean should be available");
    }

    @Test
    void openApiConfigurationIsCorrect() {
        // Verify OpenAPI bean is available
        assertNotNull(openAPI, "OpenAPI bean should be available");

        // Verify info properties
        Info info = openAPI.getInfo();
        assertNotNull(info, "Info should not be null");
        assertEquals("Kitchensink API", info.getTitle());
        assertEquals("Spring Boot migration of the JBoss Kitchensink application", info.getDescription());
        assertEquals("1.0.0", info.getVersion());

        // Verify contact information
        Contact contact = info.getContact();
        assertNotNull(contact, "Contact should not be null");
        assertEquals("JBoss Kitchensink", contact.getName());
        assertEquals("https://github.com/jboss-developer/jboss-eap-quickstarts", contact.getUrl());

        // Verify license information
        License license = info.getLicense();
        assertNotNull(license, "License should not be null");
        assertEquals("Apache 2.0", license.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0.html", license.getUrl());
    }
}
