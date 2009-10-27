Weld Permalink Example (Servlet Environment)
============================================

This example demonstrates the use of Weld in a Servlet environment (Tomcat 6
/ Jetty 6). Contextual state management and dependency injection are handled by
JSR-299. Transaction and persistence context management is handled by the EJB 3
container. No alterations are required to be made to the Servlet container. All
services are self-contained within the deployment.

This example uses a Maven 2 build. Execute the following command to build the
WAR. The WAR will will be located in the target directory after completion of
the build.

 mvn

Now you are ready to deploy.

== Deploying with an embedded servlet container

Run this command to execute the application in an embedded Jetty 6 container:

 mvn war:inplace jetty:run

You can also execute the application in an embedded Tomcat 6 container:

 mvn war:inplace tomcat:run

In both cases, any changes to assets in src/main/webapp take affect
immediately. If a change to a webapp configuration file is made, the
application may automatically redeploy. The redeploy behavior can be fined
tuned in the plugin configuration (at least for Jetty). If you make a change
to a classpath resource, you need to execute a build:

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

= Importing the project into Eclipse

The recommended way to setup a Seam example in Eclipse is to use the m2eclipse
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
