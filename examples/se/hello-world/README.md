Weld SE Hello World example
===========================

Running the example
-------------------
As with all Weld SE applications, this example is executed
by starting Java with `org.jboss.weld.environment.se.StartMain`
as the main class. Of course you will need all of the relevant jar dependencies
on your classpath, which is most easily done by loading the project into your
favourite Maven-capable IDE and running it from there.

If you are using m2eclipse, and the application won't start, make sure you uncheck 
"Resolve dependencies from Workspace projects" in the Maven properties panel. Then
run a full build to ensure all classes are in the right place.

To run this example using Maven directly:

- Open a command line/terminal window in the `examples/se/hello-world` directory
- Ensure that Maven 3 is installed and in your `PATH`
- Ensure that the `JAVA_HOME` environment variable is pointing to your JDK installation
- Execute the following command

        mvn -Drun -Dname=Pete
