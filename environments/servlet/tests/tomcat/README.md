Tomcat profiles
===============

Tomcat 10.1.x - the default profile
--------------------------------

By default **tomcat10** profile is used:

        mvn clean test -Dincontainer

To override the default Tomcat 10.1 version:

        mvn clean test -Dincontainer -Dtomcat.version=10.1.x
