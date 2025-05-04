package org.jboss.as.quickstarts.kitchensink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@SpringBootApplication
public class KitchensinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(KitchensinkApplication.class, args);
	}

	@Component
	public static class ServerPortLogger implements ApplicationListener<WebServerInitializedEvent> {
		private static final Logger LOG = Logger.getLogger(ServerPortLogger.class.getName());

		@Override
		public void onApplicationEvent(WebServerInitializedEvent event) {
			int port = event.getWebServer().getPort();
			LOG.info("=======================================================");
			LOG.info("  JBoss Kitchensink Spring Boot Migration is running!");
			LOG.info("  Access the application at: http://localhost:" + port);
			LOG.info("  H2 Database console: http://localhost:" + port + "/h2-console");
			LOG.info("=======================================================");
		}
	}
}
