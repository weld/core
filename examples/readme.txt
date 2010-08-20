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


***** How to run functional tests on JSF examples *****

Functional tests can be run separately on each of JSF examples or as a whole testsuite. JBoss AS 6 
has to be installed and running before it's possible to run functional tests. 

When running the whole testsuite, use the following command in the examples directory:

mvn -U -Pftest-jboss-remote-60,jboss5 clean verify

When running separate ftests, use the following command in respective example's folder under jsf
directory (except numberguess, explained below):

mvn -U -Pftest-jboss-remote-60 clean verify

Numberguess example needs its jboss5 profile to be executed so you have to use the same command as
for the whole testsuite, however, in the Numberguess director:

mvn -U -Pftest-jboss-remote-60,jboss5 clean verify

Numberguess can be aslo tested in a cluster. Follow these steps:
1) start two JBossAS instances (read the guide in NumberGuessClusteringTest.java file under
   numberguess/src/ftest folder)
2) set up JBOSS_HOME environment property to point to JBossAS distribution
3) run the following command:

mvn -U -Pjboss6cluster,ftest-jboss-cluster-60 clean verify 
  -Djboss.master.configuration=${env.JBOSS_HOME}/server/all

