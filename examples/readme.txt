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

  mvn -Pftest-jboss-remote-60,jboss6 clean verify

When running the functional tests on an individual example, use the following command in
the example's directory (except for jsf/numberguess as explained below):

  mvn -U -Pftest-jboss-remote-60 clean verify

The jsf/numberguess example needs the jboss6 profile to be active, so, in the jsf/numberguess
directory, run:

  mvn -U -Pftest-jboss-remote-60,jboss6 clean verify

The jsf/numberguess example can be also tested in a cluster. Follow these steps:

1) start two JBossAS instances (read the guide in NumberGuessClusteringTest.java file under
   numberguess/src/ftest folder)
2) set up JBOSS_HOME environment property to point to JBossAS distribution
3) run the following command:

  mvn -U -Pjboss6cluster,ftest-jboss-cluster-60 clean verify  -Djboss.master.configuration=${env.JBOSS_HOME}/server/all
