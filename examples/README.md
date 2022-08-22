Weld's Examples
===============

Weld currently comes with a number of examples:

* `jsf/numberguess` (a simple war example for JSF; can be deployed to various servers/servlets)
* `jsf/login` (a simple war example for JSF)
* `jsf/translator` (a simple EJB example for JSF; uses EAR)
* `se/numberguess` (the numberguess example for Java SE using Swing)
* `se/groovy-numberguess` (the numberguess in Groovy leveraging Swing)

Before running the examples, you'll need to ensure your environment supports CDI.
As for EE servers WildFly and GlassFish both have built in support.
If it is servlets you need, Weld provides support for Tomcat and Jetty.
Last but not least, you can run Weld in plain Java SE where you don't really need any prerequisites. 

The examples and Weld are explained in detail in the reference guide, including
how to deploy the examples to WildFly, and how to deploy the examples to Tomcat. Most
examples also have a README which explains how to run the example on all servers it supports. 


Running the functional tests for the JSF examples
------------------------------------------------

Weld's JSF examples come with functional tests, which use Selenium to test each flow a user can 
take through the GUI of the example.

The functional tests can be run on an individual JSF examples or on all examples.
WildFly (latest Final release recommended) must be installed to run the functional tests. 

Make sure you have set the `JBOSS_HOME` environment property to point to your WildFly distribution.

To run the functional tests:

    mvn -Darquillian=wildfly-managed clean verify

You can run the functional tests against all examples (from the `examples` directory) or against
an individual example (from its sub-directory).

The` jsf/numberguess` example can also be tested in a cluster. Follow these steps for a default configuration:

1. Create two WildFly distributions, so you have, e.g.

        /home/foo/testing/node1/wildfly/

    and

        /home/foo/testing/node2/wildfly/

2. Configure each of the installations' `standalone/configuration/standalone-ha.xml` files

    Edit the `<interfaces/>` element to bind each instance to a different loopback IP address, e.g.

        <loopback-address value="127.0.1.1"/>

    and

        <loopback-address value="127.0.2.1"/>
       
3. Run the test suite, modify the `node{1,2}.jbossHome` properties to match your configuration

        mvn clean verify -Pwildfly-cluster -Darquillian=wildfly-cluster -Dnode1.jbossHome=/home/foo/testing/node1/wildfly/ -Dnode2.jbossHome=/home/foo/testing/node2/wildfly/

   If you have set up a different addresses in the previous step, you also need to add the following system properties:

        -Dnode1.contextPath=http://127.0.1.1:8080/weld-numberguess -Dnode2.contextPath=http://127.0.2.1:8080/weld-numberguess



