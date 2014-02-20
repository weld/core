Jetty profiles
==============

Jetty 7.x and 8.x - the default profile
---------------------------------------

By default **jetty-embedded-7** profile is used:

        mvn clean test -Dincontainer

To override the default Jetty version:

        mvn clean test -Dincontainer -Djetty.version=8.1.14.v20131031

Jetty 9.x profile
-----------------

To enable **jetty-embedded-9** profile:

        mvn clean test -Dincontainer -Djetty-embedded-9

To override the default Jetty 9 version:

        mvn clean test -Dincontainer -Djetty-embedded-9 -Djetty.version=9.0.7.v20131107