Weld Login Example
========================

Deploying to JBoss AS, JBoss EAP, WildFly
---------------------

Make sure you have assigned the absolute path of your installation to the
JBOSS_HOME environment variable.

1. Open terminal and start the server by running script:
	$JBOSS_HOME/bin/standalone.sh

2. Deploy example on the server you have run in the step 1 using command
	mvn jboss-as:deploy

3. Now you can view the application at <http://localhost:8080/weld-login>.


To run functional tests execute:
   mvn verify -Darquillian=wildfly-managed-8
