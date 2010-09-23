Weld currently comes with a number of examples:

* jsf/numberguess (a war example for JSF)
* jsf/login (another war example for JSF)
* jsf/translator (an EJB example for JSF)
* jsf/permalink (yet another war example for JSF)
* se/numberguess (the numberguess example for Java SE using Swing)
* se/helloworld (a simple example for Java SE)
* wicket/numberguess (the numberguess example for Wicket)

Before running the examples, you'll need to ensure your
server supports Weld.

The examples and RI are explained in detail in the reference guide, including
how to deploy the examples to JBoss 6.0.x, and how to deploy the examples to Tomcat


== Running the functional tests on the JSF examples

The functional tests can be run on an individual JSF examples or on all examples. JBoss AS 6 
must to be installed and running to run the functional tests. 

When running the testsuite against all the examples, use the following command in the examples
directory:

  mvn -Pftest-jboss-remote-6,jboss6 clean verify

When running the functional tests on an individual example, use the following command in
the example's directory:

  mvn -Pftest-jboss-remote-6,jboss6 clean verify

The jsf/numberguess example can be also tested in a cluster. Prior to executing this test
it is needed to start both JBoss AS instances manually.  Follow these steps:

1. Create a second all profile in your JBoss AS distribution

    cp -r server/all server/all2
    
2. Start both servers. The "all" profile is the *master* jboss instance (and is bound to
   default ports); the application is deployed to this profile.
     
     ./run.sh -c all -g DocsPartition -u 239.255.101.101 -b localhost \
       -Djboss.messaging.ServerPeerID=1 -Djboss.service.binding.set=ports-default
    
    ./run.sh -c all2 -g DocsPartition -u 239.255.101.101 -b localhost \
       -Djboss.messaging.ServerPeerID=2 -Djboss.service.binding.set=ports-01 
       
3. Make sure you have set the `JBOSS_HOME` environment property to point to your JBoss AS
   distribution
4. Run the test suite

    mvn -Pjboss6cluster,ftest-jboss-cluster-6 clean verify

The jsf/numberguess and jsf/permalink examples can be also tested with Tomcat and Jetty containers.

Before running the functional tests with Tomcat container, the Tomcat 6 container has to be installed
and running. Before starting Tomcat, add the following line to conf/tomcat-users.xml to allow 
the Cargo plugin to access the manager application, then start Tomcat:
  
  <user username="admin" password="" roles="manager"/> 
  
The following command will execute functional tests on standalone Tomcat container:

  mvn -Ptomcat,ftest-tomcat-6 clean verify 

An embedded Jetty 6 container is used when running the functional tests with Jetty container. 
The following command will execute functional tests with embedded Jetty container:

  mvn -Pjetty,ftest-jetty-6 clean verify
