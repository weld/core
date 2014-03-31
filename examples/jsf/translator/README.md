Weld Translator Example
=======================

This example demonstrates the use of Weld in a Servlet container (Tomcat or
Jetty) or as a non-EJB application for a Java EE server (WildFly or GlassFish). 
No alterations are expected to be made to the container. All services are 
self-contained within the deployment.

Deploying to WildFly
--------------------

Make sure you have assigned the absolute path of your installation to the
`JBOSS_HOME` environment variable.

1. Open terminal and start the server by running script:

        $JBOSS_HOME/bin/standalone.sh

2. Install the parent using command:

        mvn install -f ../../pom.xml

3. Deploy this example to the server you have started in step 1 using command

        mvn wildfly:deploy -f ear/pom.xml

4. Now you can view the application at <http://localhost:8080/weld-translator>.


To run functional tests execute:

    mvn verify -Darquillian=wildfly-managed-8 -f ftest/pom.xml

Deploying to GlassFish
----------------------
Firstly, verify that the `GLASSFISH_HOME` environment variable points to your
GlassFish installation.

Build the example by running:

    mvn package

Then, create a new domain for the application:

    mvn glassfish:create-domain -f ear/pom.xml

Finally, deploy the application using:

    mvn package glassfish:deploy -f ear/pom.xml
   
The application becomes available at <http://localhost:7070/weld-translator>.