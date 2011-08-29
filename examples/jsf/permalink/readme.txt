Weld Permalink Example (Servlet Environment)
============================================

This example demonstrates the use of Weld in a both a Java EE 6 container and a
Servlet environment (Tomcat 6 / Jetty 6). Contextual state management and
dependency injection are handled by JSR-299. For the purpose of demonstration,
data is only stored in-memory.  Additional setup is required to setup
transaction and persistence context management.  separately. No alterations are
required to be made to the Servlet container. All services are self-contained
within the deployment.

This example uses a Maven 2 build. Execute the following command to build the
WAR. The WAR will will be located in the target directory after completion of
the build.

 mvn

Now you are ready to deploy to a Java EE 6 container.

== Deploying to JBoss AS 6

If you run a normal Maven build, the artifact produced is deployable to JBoss AS
6 (or any Java EE 6-compliant container) by default:

 mvn package

Just copy target/weld-permalink.war to the JBoss AS deploy directory. Open this
local URL to access the running application:

 http://localhost:8080/weld-permalink
 
Alternatively, run ant restart to have the app copied to you ${jboss.home}

== Deploying to GlassFish 3

The application can also be deployed to GlassFish 3, the Java EE 6 reference
implementation.

First ensure that GlassFish has been started. You can start GlassFish from the
commandline using the asadmin provided by the GlassFish installation:

 asadmin start-domain domain1

Now you can deploy the target/weld-permalink.war through the web-based GlassFish
admininstration console or using asadmin:

 asadmin deploy target/weld-permalink.war

Once again, open this local URL to access the running application:

 http://localhost:8080/weld-permalink

To undeploy the application, run:

 asadmin undeploy weld-permalink

== Deploying to standalone Tomcat

If you want to run the application on a standalone Tomcat 6, first download and
extract Tomcat 6. This build assumes you will be running Tomcat in its default
configuration, with a hostname of localhost and port 8080. Before starting
Tomcat, add the following line to conf/tomcat-users.xml to allow the Maven
Tomcat plugin to access the manager application, then start Tomcat:

 <user username="admin" password="" roles="manager"/>

To override this username and password, add a <server> with id tomcat in your
Maven 2 settings.xml file, set the <username> and <password> elements to the
appropriate values and uncomment the <server> element inside the
tomcat-maven-plugin configuration in the pom.xml.

You can deploy the packaged archive to Tomcat via HTTP PUT using this command:

 mvn clean package tomcat:deploy -Ptomcat

Then you use this command to undeploy the application:

 mvn tomcat:undeploy

Instead of packaging the WAR, you can deploy it as an exploded archive
immediately after the war goal is finished assembling the exploded structure:

 mvn compile war:exploded tomcat:exploded -Ptomcat

Once the application is deployed, you can redeploy it using the following command:

 mvn tomcat:redeploy

But likely you want to run one or more build goals first before you redeploy:

 mvn compile tomcat:redeploy -Ptomcat
 mvn war:exploded tomcat:redeploy -Ptomcat
 mvn compile war:exploded tomcat:redeploy -Ptomcat

The application is available at the following local URL:

 http://localhost:8080/weld-permalink

== Deploying to embedded Jetty

You can deploy the application without moving any files around using the
embedded Jetty containers.

To run the application using embedded Jetty, execute this command:

 mvn jetty:run

The application will be running at the following local URL:
 
 http://localhost:9090/weld-permalink

To stop the application, terminate the running process using Ctrl+C.

== Importing the project into Eclipse

The recommended way to setup a Weld example in Eclipse is to use the m2eclipse
plugin. This plugin derives the build classpath from the dependencies listed in
the pom.xml file. It also has direct integration with Maven build commands.

To get started open Eclipse and import the project by selecting "Maven
Projects" and browsing to the project folder. You can now develop the project
in Eclipse just like any other project.

You could also prepare the Eclipse project before hand, then import the project
into Eclipse. First, transform the pom.xml into an m2eclipse Eclipse project
using this command:

 mvn eclipse:m2eclipse

Now go into Eclipse an import the project by selecting "Existing projects into
workspace" and selecting the project folder. Both approaches use the Eclipse
project configuration defined in the pom.xml file.

vim:tw=80
