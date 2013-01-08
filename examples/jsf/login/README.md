Weld Login Example
========================

Deploying to JBoss AS
---------------------

Make sure you have assigned the absolute path of your JBoss AS installation to the
JBOSS_HOME environment variable.

To deploy the example run:

   mvn jboss-as:run

Now you can view the application at <http://localhost:8080/weld-login>.

To run functional tests execute:
   mvn verify -Darquillian=jbossas-managed-7
