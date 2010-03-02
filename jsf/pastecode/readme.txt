Weld PasteCode Example
======================

This example demonstrates the use of Weld in Java EE Environment. Contextual
state management and dependency injection are handled by JSR-299. Transaction
and persistence context management is handled by the EJB 3 container. No 
alterations are required to be made to the Servlet container. All services
are self-contained within the deployment.

-------Weld Features Covered (the list will increase)-------
- injecting into POJO, EJB (SFSB), Servlet
- @ApplicationScoped, @Model, @SessionScoped annotations
- producer named methods
- Decorators

This example uses a Maven 2 build. Execute the following command to build the
EAR. The EAR will be located in the target directory after completion of
the build.

 mvn

Now you are ready to deploy. A configuration is prepared for JBoss AS regarding
access to database. If you want to run in different Java EE container you will 
have to change configuration in persistence.xml and define your own database
connection.  

== Deploying to JBoss AS

If you run a normal Maven build, the artifact it produces is deployable to JBoss
AS by default:

 mvn package

Just copy target/weld-pastecode.ear to the JBoss AS deploy directory (since 
JBoss 6.0.0.M2 to server/all configuration) along with weld-pastecode-ds.xml 
datasource. 

Open this local URL to access the running application:

 http://localhost:8080/weld-pastecode
 
Alternatively, run "ant restart" to have the app copied to your ${jboss.home}.
The ant target will deploy datasource file as well.

= Importing the project into Eclipse

The recommended way to setup a Weld example in Eclipse is to use the m2eclipse
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

vim:tw=80
