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

Firstly, set the `JBOSS_HOME` environment property to point to your WildFly installation which already contains Weld 3 in older version:

> $ export JBOSS_HOME=/opt/wildfly

Then, run the upgrade script:

> $ mvn package -Pupdate-jboss-as -f jboss-as/pom.xml -Dweld.update.version=${weld.version}

In the above snippet, `${weld.version}` is the version of Weld you want to use.
Now you should have patched WildFly in `JBOSS_HOME`.

Creating a patch file for WildFly
---------------------------------

Apart from just patching a WildFly, there is an automated way to create reusable patch-file too.
To do that, three things are needed:
* Clean WildFly
  * A basis which you want the patch for
  * If you use `-Pdownload-wfly` profile, WildFly 12.0.0.Final will be automatically downloaded into `core/jboss-as/target` folder
  * If you already have WildFly, set the path to it throught property `-DwildflyOriginal=/path/to/clean/wfly`
* Patched WildFly with Weld 3 version you want
  * `-Pupdate-jboss-as` can be used to create patched WildFly, see the paragraph above
  * If you have this in advance, just set the path to it via `-DwildflyPatched=/path/to/patched/wfly`
* Patch XML descriptor
  * By default, this is automatically grabbed from our [repository](https://github.com/weld/build/tree/master/wildfly) for WildFly 12.0.0.Final
  * You can set different version via `-Dpatch.file.name="patch-config-wildfly-12-weld-3.0.xml"`

So, here are some commands:
* `mvn clean package -Pdownload-wfly,update-jboss-as,wfly-patch-gen`
  * Fully automated way of downloading pristine WildFly 12, patching it and creating a patch file, all in `jboss-as/target` folder
* `mvn clean install -Pupdate-jboss-as,download-wfly -f jboss-as/pom.xml`
  * This will download pristine WildFly, make a copy of it and patch it right away
  * No patch file will be generated
* `export JBOSS_HOME=/path/to/wfly; mvn clean install -Pwfly-patch-gen -DwildflyOriginal=/opt/myCleanWfly -DwildflyPatched=/opt/myPatchedWfly -Dpatch.file.name=patch-config-wildfly-12-weld-3.0.xml -f jboss-as/pom.xml`
  * This is a manual way where all variables are specified; here we generate patch file for Wildfly 12
  * Need to specify both WildFly instances - clean one and already patched one
  * You can also provide specific patch XML descriptor if the default one doesn't suit through `-DpatchConfig=patch-config-wildfly-12-weld-3.0.xml`

Running integration tests and the TCK on WildFly
----------------------------------------------------

Follow the steps above to set the JBOSS_HOME environment property and to upgrade Weld
within WildFly. Then, run:

> $ mvn clean verify -Dincontainer -f tests-arquillian/pom.xml

> $ mvn clean verify -Dincontainer -f jboss-tck-runner/pom.xml

If you want to run a specific test you can use the `-Dtest=<test_name>` flag. For example 

> $ mvn clean verify -Dincontainer -f jboss-tck-runner/pom.xml -Dtest=FireEventTest

Will run all the tests defined in `FireEventTest`.

> $ mvn clean verify -Dincontainer -f jboss-tck-runner/pom.xml -Dtest=FireEventTest#testInjectedEventAcceptsEventObject

Will only run the `FireEventTest.testInjectedEventAcceptsEventObject()` test method.
