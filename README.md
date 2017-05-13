Weld
====

[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/weld/user)
[![Travis CI Build Status](https://img.shields.io/travis/weld/core/master.svg)](https://travis-ci.org/weld/core)
[![Maven Central](http://img.shields.io/maven-central/v/org.jboss.weld.se/weld-se-shaded.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22weld-core-impl%22)
[![License](https://img.shields.io/badge/license-Apache%20License%202.0-yellow.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

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





