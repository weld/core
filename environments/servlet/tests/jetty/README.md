Jetty profiles
==============

Jetty 11.x profile - the default profile
----------------------------------------

To enable **jetty-embedded-11** profile:

        mvn clean test -Dincontainer

To override the default Jetty 11 version:

        mvn clean test -Dincontainer -Djetty.version=11.x.y.z