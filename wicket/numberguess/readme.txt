This example is similar to its sibling "numberguess" example, but implemented 
with wicket and the wicket-weld integration.  It also follows the wicket 
standard usage of running from within eclipse with jetty.  So, for example, to 
run the app, right-click on Start.java in the project and choose "Run as Java 
Application," which will launch jetty with the example.  Then hit 
http://localhost:8080/

Make sure to use the jetty profile when building your workspace or running maven goals. 
If you hit problems with classes not found, make sure to clean the project.

Alternatively, you can deploy to JBoss AS using the build.xml file as usual.
