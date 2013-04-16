Weld PasteCode Example
======================

This example demonstrates the use of Weld in Java EE Environment. Contextual
state management and dependency injection are handled by JSR-299. Transaction
and persistence context management is handled by the EJB 3 container. All services
are self-contained within the deployment.

Weld Features Covered
---------------------
- injecting into POJO, EJB (SFSB), Servlet
- @ApplicationScoped, @Model, @SessionScoped annotations
- producer named methods
- Decorators

Deploying to JBoss AS
---------------------

Make sure you have assigned the absolute path of your JBoss AS installation to the
JBOSS_HOME environment variable.

To deploy the example run:

   mvn jboss-as:run

Now you can view the application at <http://localhost:8080/weld-pastecode>.

To run functional tests execute:
   mvn verify -Darquillian=jbossas-managed-7

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

 mvn eclipse:eclipse

Now go into Eclipse an import the project by selecting "Existing projects into
workspace" and selecting the project folder. Both approaches use the Eclipse
project configuration defined in the pom.xml file.

vim:tw=80
