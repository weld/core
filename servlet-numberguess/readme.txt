Web Beans Numberguess Example (Servlet Container)
=================================================

This example demonstrates the use of Web Beans in a Servlet container
environment (Tomcat 6 / Jetty 6). No alterations are expected to be made to the
Servlet container. All services are self-contained within the deployment.

This example uses a Maven 2 build. Execute the following command to build the
WAR. The WAR will will be located in the target directory after completion of
the build.

 mvn

Now you're ready to deploy.

== Deploying with an embedded servlet container

Run this command to execute the application in an embedded Jetty 6 container:

 mvn war:inplace jetty:run

You can also execute the application in an embedded Tomcat 6 container:

 mvn war:inplace tomcat:run

In both cases, any changes to assets in WebContent take affect immediately. If
a change to a webapp configuration file is made, the application may
automatically redeploy. The redeploy behavior can be fined tuned in the plugin
configuration (at least for Jetty). If you make a change to a classpath
resource, you need to execute a build:

 mvn compile war:inplace

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

You can deploy the packaged archive to Tomcat via HTTP PUT using this command:

 mvn package tomcat:deploy

Then you use this command to undeploy the application:

 mvn tomcat:undeploy

Instead of packaging the WAR, you can deploy it as an exploded archive
immediately after the war goal is finished assembling the exploded structure:

 mvn compile war:exploded tomcat:exploded

Once the application is deployed, you can redeploy it using the following command:

 mvn tomcat:redeploy

But likely you want to run one or more build goals first before you redeploy:

 mvn compile tomcat:redeploy
 mvn war:exploded tomcat:redeploy
 mvn compile war:exploded tomcat:redeploy

