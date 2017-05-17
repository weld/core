Weld
====

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/weld/user)
[![Travis CI Build Status](https://img.shields.io/travis/weld/core/master.svg)](https://travis-ci.org/weld/core)

Weld is the reference implementation of CDI: Contexts and Dependency Injection for the Java EE Platform which is the Java standard for dependency injection and contextual lifecycle management and one of the most important and popular parts of the Java EE platform.

Weld is integrated into many Java EE application servers such as WildFly, JBoss Enterprise Application Platform, GlassFish, Oracle WebLogic and others. Weld can also be used in a Servlet-only environment (Tomcat, Jetty) or plain Java SE environment.

See http://weld.cdi-spec.org for more details.

Building Weld
-------------

To build Weld simply run

> $ mvn clean install

Upgrading Weld in WildFly
-------------------------

Firstly, set the JBOSS_HOME environment property to point to your WildFly installation

> $ export JBOSS_HOME=/opt/wildfly8

Then, run the upgrade script

> $ mvn package -Pupdate-jboss-as -f jboss-as/pom.xml -Dweld.update.version=${weld.version}

where ${weld.version} is the version of Weld you want to use

Running integration tests and the TCK on WildFly
----------------------------------------------------

Follow the steps above to set the JBOSS_HOME environment property and to upgrade Weld
within WildFly. Then, run:

> $ mvn clean verify -Dincontainer -f tests-arquillian/pom.xml

> $ mvn clean verify -Dincontainer -f jboss-tck-runner/pom.xml

If you want to run a specific test you can use the -Dtest=<test_name> flag. For tck 1.2 use -DtckTest=<test_name>. For example 

> $ mvn clean verify -Dincontainer -f jboss-tck-runner/pom.xml -DtckTest=FireEventTest

Will run all the tests defined in FireEventTest.java

> $ mvn clean verify -Dincontainer -f jboss-tck-runner/pom.xml -DtckTest=FireEventTest.testInjectedEventAcceptsEventObject 

Will only run testInjectedEventAcceptsEventObject.
