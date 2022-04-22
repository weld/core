Weld PasteCode Example
======================

This example demonstrates the use of Weld in Java EE Environment. Contextual
state management and dependency injection are handled by akarta Context Dependency Injection.
Transaction and persistence context management is handled by the EJB container. All services
are self-contained within the deployment.

Weld Features Covered
---------------------
- injecting into POJO, EJB (SFSB), Servlet
- `@ApplicationScoped`, `@Model`, `@SessionScoped` annotations
- producer named methods
- Decorators

Deploying to WildFly
--------------------
Make sure you have assigned the absolute path of your installation to the
`JBOSS_HOME` environment variable.

1. Open terminal and start the server by running script:

        $JBOSS_HOME/bin/standalone.sh

2. Deploy example to the server you have started in step 1 using command

        mvn wildfly:deploy

3. Now you can view the application at <http://localhost:8080/weld-pastecode>.


To run functional tests execute:

    mvn verify -Darquillian=wildfly-managed
