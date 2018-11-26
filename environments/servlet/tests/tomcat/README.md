Tomcat profiles
===============

Tomcat 9.x - the default profile
--------------------------------

By default **tomcat9** profile is used:

        mvn clean test -Dincontainer

To override the default Tomcat 9 version:

        mvn clean test -Dincontainer -Dtomcat.version=9.0.0
