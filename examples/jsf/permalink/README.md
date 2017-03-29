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

Deploying to standalone Tomcat 8
--------------------------------
If you want to run the application on a standalone Tomcat, first download and
extract Tomcat. This build assumes you will be running Tomcat in its default
configuration, with a hostname of localhost and port 8080. Build example using 
command
                                                        
    mvn clean package -Ptomcat
                                                             
and deploy created WAR to Tomcat. 


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