Integration tests
=================

Located in src/test/java.

To run integration test on WildFly:

    mvn clean test -Dincontainer
    
To run integration test on Tomcat Embeded:

    mvn clean test -Dincontainer=tomcat
    
Functional tests
================

Located in src/ftest/java. Firefox browser must be installed to run Probe ftest (Currently default HtmlUnit driver doesn't work properly). 

To run functional test on WildFly:
 
    mvn clean test -Dincontainer -Pftest
    
To run functional test on Tomcat Embeded:

    mvn clean test -Dincontainer=tomcat -Pftest





