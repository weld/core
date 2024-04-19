Weld SE Numberguess example (Swing)
===================================

Running the Example
-------------------
To start this Weld SE application you can either choose to start it with `org.jboss.weld.environment.se.StartMain` or `org.jboss.weld.environment.se.example.numberguess.Main`.
The former is a well known default which attempts to discover beans on classpath and then boots the application.
The latter is a synthetic archive which has defined components and disables discovery leading to a minimal, quickly booting application.
Of course you will need all of the relevant jar dependencies
on your classpath, which is most easily done by loading the project into your favourite Maven-capable IDE and running it from there.

To run this example using Maven directly:

 - Ensure that Maven 3 is installed and in your `PATH`
 - Ensure that the `JAVA_HOME` environment variable is pointing to your JDK installation
 - Open a command line or terminal window in the `examples/se/numberguess` directory
 - Execute the following command

        mvn -Drun

Running the Example with Jandex
-------------------------------

Weld SE allows Jandex bytecode scanning utility to be used to speed up deployment.
To run the example application with Jandex, run:

        mvn clean package -Pjandex -Drun

Running the Example with build-time Jandex index creation
---------------------------------------------------------

The Jandex index may be created by Maven in the build phase. Weld then finds this
ready-made index and uses it to discover classes even faster.

To run the example in this configuration, run:

        mvn clean package -Pjandex,jandex-index dependency:copy-dependencies -Dmdep.stripVersion
        java -cp target/weld-se-numberguess.jar:target/dependency/weld-se-shaded.jar:target/dependency/jandex.jar org.jboss.weld.environment.se.StartMain

In the log, you should see a confirmation that existing Jandex index was found and used.
Note that in order for Jandex to be leveraged, you need to start your application with bean discovery enabled (e.g. via `org.jboss.weld.environment.se.StartMain`).

Running the Example with shaded maven plugin (fat-jar)
-----------------------------------------------------

This profile provides the capability to package the artifact in a fat-jar (single jar), including all dependencies:

        mvn clean package -Pshaded
        java -jar ./target/weld-se-numberguess.jar

Swing Example: Number Guess
---------------------------
Here's an example of a Swing application, Number Guess, similar to the example in chapter 6.
This example shows how to use the Weld SE extension in a Java SE based Swing application with no EJB or servlet dependencies.

In the Number Guess application you are given 10 attempts to guess a number between 1 and 100. After each attempt, you will be told whether you are too high, or too low. 

The game's main logic is located in `Game.java`. In this example, it differs from the web application version in several ways:

* the bean is application scoped rather than session scoped, since an instance
    of a Swing application typically represents a single 'session'.

* Notice that the bean is not named, since it doesn't need to be accessed via EL.

* In Java SE there is no JSF `FacesContext` to which messages can be added. Instead, the Game class provides additional information about the state of the current game including:

    * if the game has been won or lost,

    * if the most recent guess was invalid.

    This allows the Swing UI to query the state of the game, which it does indirectly
    via a class called `MessageGenerator`, in order to determine the appropriate messages
    to display to the user during the game.

* Since there is no dedicated validation phase, validation of user input is performed
    during the `check()` method.

* The `reset()` method makes a call to the injected `rndGenerator` in order to get
    the random number at the start of each game. Note that it cannot use
    `manager.getInstanceByType(Integer.class, new AnnotationLiteral<Random>(){})`
    as the JSF example does because there will not be any active contexts like there
    is during a JSF request.

For a deeper look into the SE Number Guess example, please refer to chapter 7.2 of the reference documentation.
