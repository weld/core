Weld's Examples
===============

Weld currently comes with a number of examples:

* `jsf/numberguess` (a simple war example for JSF)
* `jsf/login` (a simple war example for JSF)
* `jsf/translator` (a simple EJB example for JSF)
* `jsf/pastecode` (a more complex EJB example for JSF)
* `jsf/permalink` (a more complex war example for JSF)
* `se/numberguess` (the numberguess example for Java SE using Swing)
* `se/helloworld` (a simple example for Java SE)

Before running the examples, you'll need to ensure your server supports CDI (JBoss AS 6 and
GlassFish v3 both have built in support, and Weld provides support for Tomcat, Jetty and
Google App Engine). Weld also supports Java SE. 

The examples and Weld are explained in detail in the reference guide, including
how to deploy the examples to JBoss 6.0.x, and how to deploy the examples to Tomcat. Most
examples also have a README which explains how to run the example on all servers it supports. 


Running the functional tests for the JSF examples
------------------------------------------------

Weld's JSF examples come with "functional tests", which use Selenium to each flow a user can take
through the GUI of the example.

The functional tests can be run on an individual JSF examples or on all examples. JBoss AS 6 
must to be installed and running to run the functional tests. 

Make sure you have set the `JBOSS_HOME` environment property to point to your JBoss AS distribution.

To run the functional tests:

    mvn -Pftest-jboss-remote-6,jboss6 clean verify

You can run the functional tests against all examples (from the `examples` directory`) or against
an individual example (from it's sub-directory).

The jsf/numberguess example can be also tested in a cluster. Prior to executing this test
it is needed to start both JBoss AS instances manually.  Follow these steps:

1. Create a second all profile in your JBoss AS distribution

        cp -r server/all server/all2

2. Make sure you have set the `JBOSS_HOME` environment property to point to your JBoss AS
   distribution
    
3. Start both servers. The "all" profile is the *master* jboss instance (and is bound to
   default ports); the application is deployed to this profile.
     
        $JBOSS_HOME/bin/run.sh -c all -g DocsPartition -u 239.255.101.101 -b localhost \
           -Djboss.messaging.ServerPeerID=1 -Djboss.service.binding.set=ports-default
    
        $JBOSS_HOME/bin/run.sh -c all2 -g DocsPartition -u 239.255.101.101 -b localhost \
           -Djboss.messaging.ServerPeerID=2 -Djboss.service.binding.set=ports-01 
       
4. Run the test suite

        mvn -Pjboss6cluster,ftest-jboss-cluster-6 clean verify

The `jsf/numberguess` and `jsf/permalink` examples can be also tested with Tomcat and Jetty containers.

Before running the functional tests with Tomcat container, the Tomcat 6 container has to be installed
and running. 

1. Before starting Tomcat, add the following line to `conf/tomcat-users.xml`

        <user username="admin" password="" roles="manager"/> 

2. Start Tomcat
    
        $TOMCAT_HOME/bin/startup.sh
  
3. Run the testsuite

        mvn -Ptomcat,ftest-tomcat-6 clean verify 

An embedded Jetty 6 container is used when running the functional tests with Jetty container. 
The following command will execute functional tests with embedded Jetty container:

    mvn -Pjetty,ftest-jetty-6 clean verify
