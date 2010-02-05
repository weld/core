Weld Numberguess Example
========================

This example demonstrates the use of Weld in a Servlet container (Tomcat 6 or
Jetty 6) or as a non-EJB application in JBoss AS. No alterations are expected
to be made to the container. All services are self-contained within the
deployment.

You'll execute the Ant build script in this directory using the Ant command
(ant) to compile, assemble and deploy the example to JBoss AS. The Ant build is
using Maven under the covers, but you're not required to have Maven installed
on your path.  If you do have Maven installed, you can use the Maven command
(mvn) to compile and assemble a standalone artifact (WAR) and run the example
in an embedded Servlet container.

Execute the following command to build a WAR for a Java EE 6 container. The
WAR will be located in the target directory after completion of the build.

 mvn

Now you're ready to deploy to JBoss AS.

== Deploying to JBoss AS

Make sure you have assigned the path of your JBoss AS installation to the
jboss.home property key in the following file, relative to this directory:

../../build.properties

(This file is located in the examples directory of the Weld distribution).

If you haven't already, start JBoss AS. Then, deploy the application to JBoss AS
using this command:

 ant restart

Open this local URL to access the running application:

 http://localhost:8080/weld-numberguess

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

You can deploy it as an exploded archive immediately after the war goal is
finished assembling the exploded structure:

 mvn clean compile war:exploded tomcat:exploded -Ptomcat

Once the application is deployed, you can redeploy it using this command:

 mvn tomcat:redeploy -Ptomcat

But likely you want to run one or more build goals first before you redeploy:

 mvn compile tomcat:redeploy -Ptomcat
 mvn war:exploded tomcat:redeploy -Ptomcat
 mvn compile war:exploded tomcat:redeploy -Ptomcat

The application is available at the following local URL:

 http://localhost:8080/weld-numberguess

== Launching Jetty embedded from Eclipse

First, set up the Eclipse environment:

 mvn clean eclipse:clean eclipse:eclipse -Pjetty-ide
 
Next, put all the needed resources into the src/main/webapp

 mvn war:inplace -Pjetty-ide
 
Now, you are ready to run the server in Eclipse; find the Start class in
src/jetty/java, and run its main method as a Java Application. The server
will launch. You can view the application at the following local URL:

 http://localhost:8080

== Using Google App Engine

First, set up the Eclipse environment:

 mvn clean eclipse:clean eclipse:eclipse -Pgae
 
Make sure you have the Google App Engine Eclipse plugin installed.

Next, put all the needed resources into the src/main/webapp

 mvn war:inplace -Pgae

Now, in Eclipse, you can either run the app locally, or deploy it to Google App Engine.

vim:tw=80
