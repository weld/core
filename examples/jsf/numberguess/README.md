Weld Numberguess Example
========================

This example demonstrates the use of Weld in a Servlet container (Tomcat or
Jetty) or as a non-EJB application for a Java EE server (JBoss AS or GlassFish). No alterations are expected
to be made to the container. All services are self-contained within the
deployment.

Deploying to JBoss AS, JBoss EAP, WildFly
---------------------

Make sure you have assigned the absolute path of your installation to the
JBOSS_HOME environment variable.

1. Open terminal and start the server by running script:
	$JBOSS_HOME/bin/standalone.sh

2. Deploy example on the server you have run in step the 1 using command
	mvn jboss-as:deploy

3. Now you can view the application at <http://localhost:8080/weld-numberguess>.


To run functional tests execute:
   mvn verify -Darquillian=jbossas-managed-7

Deploying to GlassFish
----------------------

Firstly, verify that the GLASSFISH_HOME environment variable points to your
GlassFish installation.

Then, create a new domain for the application:

   mvn glassfish:create-domain

Finally, deploy the application using:

   mvn package glassfish:deploy

The application becomes available at <http://localhost:7070/weld-numberguess>


Deploying to standalone Tomcat
------------------------------

If you want to run the application on a standalone Tomcat, first download and
extract Tomcat. This build assumes you will be running Tomcat in its default
configuration, with a hostname of localhost and port 8080. Before starting
Tomcat, add the following line to `conf/tomcat-users.xml` to allow the Maven
Tomcat plugin to access the manager application, then start Tomcat:

    Tomcat 6:
    <user username="admin" password="" roles="manager"/>

    Tomcat 7:
    <user username="admin" password="" roles="manager-gui,manager-script"/>

To override this username and password, add a `<server>` with id `tomcat` in your
Maven `settings.xml` file, set the `<username>` and `<password>` elqements to the
appropriate values and uncomment the `<server>` element inside the
tomcat-maven-plugin configuration in the `pom.xml`.

You can deploy it as an exploded archive immediately after the war goal is
finished assembling the exploded structure:

    Tomcat 6:
    mvn clean compile war:exploded tomcat6:deploy -Ptomcat

    Tomcat 7:
    mvn clean compile war:exploded tomcat7:deploy -Ptomcat

Once the application is deployed, you can redeploy it using this command:

    Tomcat 6:
    mvn tomcat6:redeploy -Ptomcat

    Tomcat 7:
    mvn tomcat7:redeploy -Ptomcat

But likely you want to run one or more build goals first before you redeploy:

    Tomcat 6:
    mvn compile tomcat6:redeploy -Ptomcat
    mvn war:exploded tomcat6:redeploy -Ptomcat
    mvn compile war:exploded tomcat6:redeploy -Ptomcat

    Tomcat 7:
    mvn compile tomcat7:redeploy -Ptomcat
    mvn war:exploded tomcat7:redeploy -Ptomcat
    mvn compile war:exploded tomcat7:redeploy -Ptomcat

Now you can view the application at <http://localhost:8080/weld-numberguess>.

To undeploy, use:

    Tomcat 6:
    mvn tomcat6:undeploy -Ptomcat

    Tomcat 7:
    mvn tomcat7:undeploy -Ptomcat

Deploying to embedded Jetty
------------------------------

Simply run:

    mvn war:inplace jetty:run -Pjetty

The application will be running at the following local URL:

   http://localhost:9090/weld-numberguess


Launching Jetty embedded from Eclipse
-------------------------------------

First, set up the Eclipse environment:

    mvn clean eclipse:clean eclipse:eclipse -Pjetty-ide

and import the project into eclipse

Next, put all the needed resources into `src/main/webapp`

    mvn war:inplace -Pjetty-ide

Now, you are ready to run the server in Eclipse; find the Start class in
`src/jetty/java`, and run its main method as a Java Application. The server
will launch. Now you can view the application at <http://localhost:8080/weld-numberguess>.


Using Google App Engine
-----------------------

First, set up the Eclipse environment:

    mvn clean eclipse:clean eclipse:eclipse -Pgae

Make sure you have the Google App Engine Eclipse plugin installed.

Next, put all the needed resources into the src/main/webapp

    mvn war:inplace -Pgae

Now, in Eclipse, you can either run the app locally, or deploy it to Google App Engine.

vim:tw=80
