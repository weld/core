Weld Permalink Example (Servlet Environment)
============================================

This example demonstrates the use of Weld in a both a Java EE 6 container and a
Servlet environment (Tomcat or Jetty). Contextual state management and
dependency injection are handled by CDI. For the purpose of demonstration,
data is only stored in-memory. Additional configuration is required to set up
transaction and persistence context management. No alterations are
required to be made to the Servlet container. All services are self-contained
within the deployment.

Deploying to WildFly
--------------------
Make sure you have assigned the absolute path of your installation to the
`JBOSS_HOME` environment variable.

1. Open terminal and start the server by running script:

        $JBOSS_HOME/bin/standalone.sh

2. Deploy example to the server you have started in step 1 using command

        mvn wildfly:deploy

3. Now you can view the application at <http://localhost:8080/weld-permalink>.

To run functional tests execute:

    mvn verify -Darquillian=wildfly-managed-8

Deploying to GlassFish
----------------------
Firstly, verify that the `GLASSFISH_HOME` environment variable points to your
GlassFish installation.

Then, create a new domain for the application:

    mvn glassfish:create-domain

Finally, deploy the application using:

    mvn package glassfish:deploy

The application becomes available at <http://localhost:7070/weld-permalink>.

Deploying to standalone Tomcat 7
--------------------------------
If you want to run the application on a standalone Tomcat, first download and
extract Tomcat. This build assumes you will be running Tomcat in its default
configuration, with a hostname of localhost and port 8080. Before starting
Tomcat, add the following line to `conf/tomcat-users.xml` to allow the Maven
Tomcat plugin to access the manager application, then start Tomcat:

    <user username="admin" password="" roles="standard,manager-script"/>

To override this username and password, add a `<server>` with id `tomcat` in your
Maven `settings.xml` file, set the `<username>` and `<password>` elqements to the
appropriate values and uncomment the `<server>` element inside the
`tomcat-maven-plugin` configuration in the `pom.xml`.

You can deploy it as an exploded archive immediately after the war goal is
finished assembling the exploded structure:

    mvn clean compile war:exploded tomcat7:deploy -Ptomcat

Once the application is deployed, you can redeploy it using this command:

    mvn tomcat7:redeploy -Ptomcat

But likely you want to run one or more build goals first before you redeploy:

    mvn compile tomcat7:redeploy -Ptomcat
    mvn war:exploded tomcat7:redeploy -Ptomcat
    mvn compile war:exploded tomcat7:redeploy -Ptomcat

Now you can view the application at <http://localhost:8080/weld-permalink>.

To undeploy, use:

    mvn tomcat7:undeploy -Ptomcat

Deploying to embedded Jetty
---------------------------
Simply run:

    mvn war:inplace jetty:run -Pjetty

The application will be running at <http://localhost:9090/weld-permalink>.

NOTE: This configuration currently does not work on Maven 3.1.x due to class loading conflicts regarding JSR-330

Importing the project into Eclipse
----------------------------------
The recommended way to setup a Weld example in Eclipse is to use the m2eclipse
plugin. This plugin derives the build classpath from the dependencies listed in
the pom.xml file. It also has direct integration with Maven build commands.

To get started open Eclipse and import the project by selecting "Maven
Projects" and browsing to the project folder. You can now develop the project
in Eclipse just like any other project.

You could also prepare the Eclipse project before hand, then import the project
into Eclipse. First, transform the pom.xml into an m2eclipse Eclipse project
using this command:

    mvn eclipse:eclipse

Now go into Eclipse an import the project by selecting "Existing projects into
workspace" and selecting the project folder. Both approaches use the Eclipse
project configuration defined in the pom.xml file.