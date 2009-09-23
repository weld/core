Web Beans Numberguess Example (Servlet Container)
=================================================

This example demonstrates the use of Web Beans in a Servlet container
environment (Tomcat 6 / Jetty 6) and in JBoss AS. No alterations are 
expected to be made to the Servlet container. All services are 
self-contained within the deployment.

This example uses a Maven 2 build. Execute the following command to build the
WAR. The WAR will will be located in the target directory after completion of
the build.

 mvn

Now you're ready to deploy.

== Deploying with an embedded servlet container

Run this command to execute the application in an embedded Jetty 6 container:

 mvn war:inplace jetty:run -Pjetty

You can also execute the application in an embedded Tomcat 6 container:

 mvn war:inplace tomcat:run -Ptomcat
 
You'll can access the app at http://localhost:9090

In both cases, any changes to assets in src/main/webapp take affect immediately. If
a change to a webapp configuration file is made, the application may
automatically redeploy. The redeploy behavior can be fined tuned in the plugin
configuration (at least for Jetty). If you make a change to a classpath
resource, you need to execute a build:

 mvn compile war:inplace {-Ptomcat,-Pjetty} 

Note that war:inplace copies the compiled classes and JARs inside WebContent,
under WEB-INF/classes and WEB-INF/lib, respectively, mixing source and compiled
files. However, the build does work around these temporary files by excluding
them from the packaged WAR and cleaning them during the Maven clean phase.
These folders are also ignored by SVN.

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

You can deploy it as an exploded archive
immediately after the war goal is finished assembling the exploded structure:

 mvn compile war:exploded tomcat:exploded -Ptomcat

Once the application is deployed, you can redeploy it using the following command:

 mvn tomcat:redeploy -Ptomcat

But likely you want to run one or more build goals first before you redeploy:

 mvn compile tomcat:redeploy -Ptomcat
 mvn war:exploded tomcat:redeploy -Ptomcat
 mvn compile war:exploded tomcat:redeploy -Ptomcat

The application is available at http://localhost:8080/webbeans-numberguess

== Launching Jetty embedded from Eclipse

First, set up the eclipse environment:

 mvn clean eclipse:clean eclipse:eclipse -Djetty-ide
 
Next, put all the needed resources into the src/main/webapp

 mvn war:inplace -Djetty-ide
 
Now, you are ready to run the server in Eclipse; find the Start class in src/main/jetty, and run it's
main method as a Java Application. The server will launch. You'll find the application at
http://localhost:8080


== Using Google App Engine

First, set up the eclipse environment:

 mvn clean eclipse:clean eclipse:eclipse -Dgae
 
Make sure you have the Google App Engine Eclipse plugin installed.

Next, put all the needed resources into the src/main/webapp

 mvn war:inplace -Dgae

Now, in Eclipse, you can either run the app locally, or deploy it to Google App Engine