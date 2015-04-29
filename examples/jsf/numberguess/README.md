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

    mvn verify -Darquillian=wildfly-managed-8

Deploying to WildFly Web
------------------------

WildFly Web is a lightweigh Servlet-only version of WildFly.

Make sure you have assigned the absolute path of your installation to the
`JBOSS_HOME` environment variable.

1. Open terminal and start the server by running

        $JBOSS_HOME/bin/standalone.sh

2. Build and deploy the example to the server you have started in step 1 using command

        mvn clean package -Pwildfly-web wildfly:deploy

3. Now you can view the application at <http://localhost:8080/weld-numberguess>.

Deploying to GlassFish
----------------------

Firstly, verify that the `GLASSFISH_HOME` environment variable points to your
GlassFish installation.

Then, create a new domain for the application:

    mvn glassfish:create-domain

Finally, deploy the application using:

    mvn package glassfish:deploy

The application becomes available at <http://localhost:7070/weld-numberguess>


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
tomcat-maven-plugin configuration in the `pom.xml`.

You can deploy it as an exploded archive immediately after the war goal is
finished assembling the exploded structure:

    mvn clean compile war:exploded tomcat7:deploy -Ptomcat

Once the application is deployed, you can redeploy it using this command:

    mvn tomcat7:redeploy -Ptomcat

But likely you want to run one or more build goals first before you redeploy:

    mvn compile tomcat7:redeploy -Ptomcat
    mvn war:exploded tomcat7:redeploy -Ptomcat
    mvn compile war:exploded tomcat7:redeploy -Ptomcat

Now you can view the application at <http://localhost:8080/weld-numberguess>.

To undeploy, use:

    mvn tomcat7:undeploy -Ptomcat

Deploying to embedded Jetty
---------------------------

Simply run:

    mvn war:inplace jetty:run -Pjetty

The application will be running at <http://localhost:9090/weld-numberguess>

NOTE: This configuration currently does not work on Maven 3.1.x due to class loading conflicts regarding JSR-330

Using Google App Engine
-----------------------

First, set up the Eclipse environment:

    mvn clean eclipse:clean eclipse:eclipse -Pgae

Make sure you have the Google App Engine Eclipse plugin installed.

Next, put all the needed resources into the `src/main/webapp`

    mvn war:inplace -Pgae

Now, in Eclipse, you can either run the app locally, or deploy it to Google App Engine.
