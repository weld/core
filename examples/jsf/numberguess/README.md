Weld Numberguess Example
========================

This example demonstrates the use of Weld in a Servlet container (Tomcat or
Jetty) or as a non-EJB application for a Java EE server (WildFly or GlassFish). No alterations are expected
to be made to the container. All services are self-contained within the
deployment.

Deploying to WildFly
--------------------

Make sure you have assigned the absolute path of your installation to the
`JBOSS_HOME` environment variable.

1. Open terminal and start the server by running

        $JBOSS_HOME/bin/standalone.sh

2. Build and deploy the example to the server you have started in step 1 using command

        mvn clean package wildfly:deploy

3. Now you can view the application at <http://localhost:8080/weld-numberguess>.


To run the functional tests, execute:

    mvn verify -Darquillian=wildfly-managed

Deploying to WildFly Web
------------------------

WildFly Web is a lightweight Servlet-only version of WildFly.

Make sure you have assigned the absolute path of your installation to the
`JBOSS_HOME` environment variable.

1. Open terminal and start the server by running

        $JBOSS_HOME/bin/standalone.sh

2. Build and deploy the example to the server you have started in step 1 using command

        mvn clean package -Pwildfly-web wildfly:deploy

3. Now you can view the application at <http://localhost:8080/weld-numberguess>.

Deploying to standalone Tomcat 9
--------------------------------

If you want to run the application on a standalone Tomcat, first download and
extract Tomcat. This build assumes you will be running Tomcat in its default
configuration, with a hostname of localhost and port 8080. Build example using
command

      mvn clean package -Ptomcat

and deploy the created WAR to Tomcat.

Deploying to standalone Jetty
-----------------------------

If you want to run the application on a standalone Jetty, first download and
extract the Jetty distribution. This build assumes you will be running Jetty in its default
configuration, with a hostname of localhost and port 8080. Build example using
command

      mvn clean package -Pjetty

and deploy the created WAR to Jetty (e.g. copy the artifact to webapps directory).

