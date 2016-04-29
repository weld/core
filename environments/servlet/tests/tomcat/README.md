Tomcat profiles
===============

Tomcat 7.x - the default profile
--------------------------------

By default **tomcat7** profile is used:

        mvn clean test -Dincontainer

To override the default Tomcat 7 version:

        mvn clean test -Dincontainer -Dtomcat.version=7.0.52

Tomcat 8.x profile
------------------

To enable **tomcat8** profile:

        mvn clean test -Dincontainer -Dtomcat8

To override the default Tomcat 8 version:

        mvn clean test -Dincontainer -Dtomcat8 -Dtomcat.version=8.0.1