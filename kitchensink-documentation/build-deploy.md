# Building and Deploying the Kitchensink Application

This document provides detailed instructions for building and deploying the Kitchensink application in different environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Building the Application](#building-the-application)
3. [Deployment Options](#deployment-options)
   - [JBoss EAP Deployment](#jboss-eap-deployment)
   - [OpenShift Deployment](#openshift-deployment)
   - [Docker Deployment](#docker-deployment)
4. [Configuration Options](#configuration-options)
5. [Troubleshooting](#troubleshooting)

## Prerequisites

Before building and deploying the Kitchensink application, ensure you have the following prerequisites installed:

- **Java Development Kit (JDK)**: JDK 11 or later
- **Maven**: Version 3.6.0 or later
- **JBoss EAP**: Version 8.0 or compatible application server
- **Git**: For source code management

## Building the Application

### Clone the Repository

```bash
git clone https://github.com/jboss-developer/jboss-eap-quickstarts.git
cd jboss-eap-quickstarts/kitchensink
```

### Configure Maven

Ensure Maven is properly configured to use JBoss repositories. You may need to add the following to your `~/.m2/settings.xml` file:

```xml
<settings>
  <profiles>
    <profile>
      <id>jboss-public-repository</id>
      <repositories>
        <repository>
          <id>jboss-public-repository-group</id>
          <name>JBoss Public Maven Repository Group</name>
          <url>https://repository.jboss.org/nexus/content/groups/public/</url>
          <layout>default</layout>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>jboss-public-repository-group</id>
          <name>JBoss Public Maven Repository Group</name>
          <url>https://repository.jboss.org/nexus/content/groups/public/</url>
          <layout>default</layout>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>jboss-public-repository</activeProfile>
  </activeProfiles>
</settings>
```

### Build the Application

To build the application, run:

```bash
mvn clean package
```

This command will:
1. Clean the project
2. Compile the source code
3. Run tests
4. Package the application as a WAR file

The resulting WAR file will be located in the `target` directory as `kitchensink.war`.

## Deployment Options

### JBoss EAP Deployment

#### Start JBoss EAP

```bash
$JBOSS_HOME/bin/standalone.sh
```

#### Deploy Using Maven

```bash
mvn wildfly:deploy
```

#### Manual Deployment

Copy the WAR file to the JBoss EAP deployment directory:

```bash
cp target/kitchensink.war $JBOSS_HOME/standalone/deployments/
```

#### Access the Application

Once deployed, the application will be available at:

```
http://localhost:8080/kitchensink
```

### OpenShift Deployment

The Kitchensink application can be deployed to OpenShift using the Source-to-Image (S2I) process or Helm charts.

#### Using Source-to-Image (S2I)

1. Log in to your OpenShift cluster:
   ```bash
   oc login <cluster-url>
   ```

2. Create a new project:
   ```bash
   oc new-project kitchensink
   ```

3. Create a new application using the JBoss EAP template:
   ```bash
   oc new-app --template=eap8-basic-s2i \
     --param=APPLICATION_NAME=kitchensink \
     --param=SOURCE_REPOSITORY_URL=https://github.com/jboss-developer/jboss-eap-quickstarts \
     --param=SOURCE_REPOSITORY_REF=8.0.x \
     --param=CONTEXT_DIR=kitchensink
   ```

4. Expose the service:
   ```bash
   oc expose service kitchensink
   ```

5. Get the route:
   ```bash
   oc get route kitchensink
   ```

#### Using Helm Charts

##### Original JBoss EAP Version

1. Add the JBoss EAP Helm repository:
   ```bash
   helm repo add jboss-eap https://jbossas.github.io/eap-charts/
   ```

2. Update the repositories:
   ```bash
   helm repo update
   ```

3. Install the chart:
   ```bash
   helm install kitchensink jboss-eap/eap8 \
     --set build.uri=https://github.com/jboss-developer/jboss-eap-quickstarts \
     --set build.ref=8.0.x \
     --set build.contextDir=kitchensink
   ```

##### Spring Boot Version

The Spring Boot version of Kitchensink includes a Helm chart for deployment to Kubernetes environments.

1. Build the Docker image:
   ```bash
   docker build -t kitchensink-spring:latest .
   ```

2. Install the Helm chart:
   ```bash
   helm install kitchensink-spring ./charts
   ```

3. To customize the deployment, you can override values:
   ```bash
   helm install kitchensink-spring ./charts \
     --set replicaCount=2 \
     --set image.repository=your-registry/kitchensink-spring \
     --set image.tag=1.0.0 \
     --set env.SPRING_PROFILES_ACTIVE=prod \
     --set persistence.enabled=true
   ```

4. Access the application:
   ```bash
   # Get the service URL
   kubectl get svc kitchensink-spring

   # If using Ingress, add the host to your /etc/hosts file
   echo "127.0.0.1 kitchensink-spring.local" | sudo tee -a /etc/hosts
   ```

### Docker Deployment

You can also deploy the Kitchensink application using Docker.

#### Create a Dockerfile

Create a file named `Dockerfile` with the following content:

```dockerfile
FROM registry.redhat.io/jboss-eap-8/eap8-openjdk17-openshift-rhel8:latest

COPY target/kitchensink.war $JBOSS_HOME/standalone/deployments/

USER root
RUN chown -R jboss:root $JBOSS_HOME/standalone/deployments/
USER jboss

EXPOSE 8080
CMD ["/opt/eap/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
```

#### Build and Run the Docker Image

```bash
# Build the application
mvn clean package

# Build the Docker image
docker build -t kitchensink:latest .

# Run the container
docker run -p 8080:8080 kitchensink:latest
```

Access the application at `http://localhost:8080/kitchensink`.

## Configuration Options

### Database Configuration

By default, the Kitchensink application uses an H2 in-memory database. To use a different database:

1. Modify the datasource configuration in `src/main/webapp/WEB-INF/kitchensink-quickstart-ds.xml`
2. Update the persistence.xml file in `src/main/resources/META-INF/persistence.xml`

#### Example PostgreSQL Configuration

**kitchensink-quickstart-ds.xml**:
```xml
<datasources>
    <datasource jndi-name="java:jboss/datasources/KitchensinkQuickstartDS"
        pool-name="kitchensink-quickstart" enabled="true"
        use-java-context="true">
        <connection-url>jdbc:postgresql://localhost:5432/kitchensink</connection-url>
        <driver>postgresql</driver>
        <security>
            <user-name>postgres</user-name>
            <password>postgres</password>
        </security>
    </datasource>
</datasources>
```

**persistence.xml**:
```xml
<persistence-unit name="primary">
   <jta-data-source>java:jboss/datasources/KitchensinkQuickstartDS</jta-data-source>
   <properties>
      <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
      <property name="hibernate.hbm2ddl.auto" value="create-drop" />
      <property name="hibernate.show_sql" value="false" />
   </properties>
</persistence-unit>
```

### Logging Configuration

To adjust logging levels, modify the JBoss EAP logging configuration in `$JBOSS_HOME/standalone/configuration/standalone.xml`:

```xml
<subsystem xmlns="urn:jboss:domain:logging:8.0">
    <console-handler name="CONSOLE">
        <level name="INFO"/>
        <formatter>
            <named-formatter name="COLOR-PATTERN"/>
        </formatter>
    </console-handler>
    <logger category="org.jboss.as.quickstarts.kitchensink">
        <level name="DEBUG"/>
    </logger>
</subsystem>
```

## Troubleshooting

### Common Issues

#### Build Failures

**Issue**: Maven build fails with dependency resolution errors.
**Solution**: Ensure your Maven settings are correctly configured to use JBoss repositories.

#### Deployment Failures

**Issue**: Application fails to deploy with "Address already in use" error.
**Solution**: Ensure no other application is using port 8080, or configure JBoss to use a different port.

**Issue**: Deployment fails with database connection errors.
**Solution**: Verify that the database server is running and accessible, and that the connection details in the datasource configuration are correct.

#### Runtime Errors

**Issue**: "ClassNotFoundException" or "NoClassDefFoundError".
**Solution**: Ensure all dependencies are correctly specified in the pom.xml file.

**Issue**: Validation errors when registering members.
**Solution**: Check that the input data meets the validation constraints defined in the Member entity.

### Getting Help

If you encounter issues not covered here:

1. Check the JBoss EAP server logs in `$JBOSS_HOME/standalone/log/server.log`
2. Consult the [JBoss EAP documentation](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/)
3. Visit the [JBoss Developer community](https://developer.jboss.org/) for support
