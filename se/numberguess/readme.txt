====
    JBoss, Home of Professional Open Source
    Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
    contributors by the @authors tag. See the copyright.txt in the
    distribution for a full listing of individual contributors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

===========================
Swing Example: Number Guess
===========================

Here's an example of a Swing application, Number Guess, similar to the example in chapter 3.4.
In the Number Guess application you get given 10 attempts to guess a number between 1 and
100. After each attempt, you will be told whether you are too high, or too low. This example can
be found in the examples/se/numberguess folder of the Web Beans distribution.

There is an empty beans.xml file in the META-INF package (src/main/resources/META-INF/beans.xml), which
marks this application as a Web Beans application.

The game's main logic is located in Game.java. Some key points about this class:

     The bean is application scoped instead of session scoped, since an instance of the
     application represents a single 'session'.

     The bean is not named, since it doesn't need to be accessed via EL

     There is no JSF FacesContext to add messages to. Instead the Game class provides
     additional information about the state of the current game including:

     - If the game has been won or lost
     - If the most recent guess was invalid

     This allows the Swing UI to query the state of the game, which it does indirectly via a class
     called MessageGenerator, in order to determine the appropriate messages to display to the
     user during the game.

     Validation of user input is performed during the check() method, since there is no dedicated
     validation phase

     The reset() method makes a call to the injected rndGenerator in order
     to get the random number at the start of each game. It cannot use
     manager.getInstanceByType(Integer.class, new AnnotationLiteral<Random>(){})
     as the JSF example does because there will not be any active contexts like there is during
     a JSF request.

The MessageGenerator class depends on the current instance of Game, and queries its state in
order to determine the appropriate messages to provide as the prompt for the user's next guess
and the response to the previous guess. Some key points about this class:

    The instance of Game for the application is injected.

    The Game's state is interrogated to determine the appropriate challenge message.
    And again to determine whether to congratulate, console or encourage the user to continue.


Finally we come to the NumberGuessFrame class which provides the Swing front end to our
guessing game.

    The instance of Game for the application is injected.

    The message generator for UI messages is injected.

    This application is started in the usual Web Beans SE way, by observing the
    ContainerInitialized event.

    The initComponents method initializes all of the Swing components. Note the use of the msgGenerator.
    guessButtonActionPerformed is called when the 'Guess' button is clicked, and it does the
    following:

      - Gets the guess entered by the user and sets it as the current guess in the Game
      - Calls game.check() to validate and perform one 'turn' of the game
      - Calls refreshUI. If there were validation errors with the input, this will have been
         captured during game.check() and as such will be reflected in the messeges returned
         by MessageGenerator and subsequently presented to the user. If there are no validation
         errors then the user will be told to guess again (higher or lower) or that the game has ended
         either in a win (correct guess) or a loss (ran out of guesses).

   replayBtnActionPerformed simply calls game.reset() to start a new game and refreshes
   the messages in the UI.

   refreshUI uses the MessageGenerator to update the messages to the user based on the
   current state of the Game.

-------------------
Running the Example
-------------------

As with all Web Beans SE applications, this example is executed
by starting Java with org.jboss.webbeans.environment.se.StartMain
as the main class. Of course you will need all of the relevant jar dependencies
on your classpath, which is most easily done by loading the project into your
favourite Maven-capable IDE and running it from there..

To run this example using Maven directly:

- Open a command line/terminal window in the examples/se/numberguess directory
- Ensure that Maven 2 is installed and in your PATH
- Ensure that the JAVA_HOME environment variable is pointing to your JDK installation
- execute the following command

mvn -Drun
