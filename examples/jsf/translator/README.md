Weld Translator Example
=======================

This example demonstrates the use of Weld in a Servlet container (Tomcat or
Jetty) or as a non-EJB application for a Java EE server (JBoss AS or GlassFish). No alterations are expected
to be made to the container. All services are self-contained within the
deployment.

Deploying to JBoss AS
---------------------

Make sure you have assigned the absolute path of your JBoss AS installation to the
JBOSS_HOME environment variable.

Build the example by running:

   mvn package

To deploy the example run:

   mvn jboss-as:run -f ear/pom.xml

Now you can view the application at <http://localhost:8080/weld-translator>.

To run functional tests execute:
   mvn verify -Darquillian=jbossas-managed-7 -f ftest/pom.xml

Deploying to GlassFish
----------------------

Firstly, verify that the GLASSFISH_HOME environment variable points to your
GlassFish installation.

Build the example by running:

   mvn package

Then, create a new domain for the application:

   mvn glassfish:create-domain -f ear/pom.xml

Finally, deploy the application using:

   mvn package glassfish:deploy -f ear/pom.xml
   
The application becomes available at <http://localhost:7070/weld-translator>
   