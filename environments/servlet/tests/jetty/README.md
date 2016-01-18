Jetty profiles
==============

Jetty 9.x profile - the default profile
----------------------------------------

To enable **jetty-embedded-9** profile:

        mvn clean test -Dincontainer

To override the default Jetty 9 version:

        mvn clean test -Dincontainer -Djetty.version=9.3.6.v20151106