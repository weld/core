Weld Numberguess Example
========================

This example demonstrates the use of Weld in a Servlet container (Tomcat or
Jetty) or as a non-EJB application for a Jakarta EE server (WildFly or GlassFish). No alterations are expected
to be made to the container. All services are self-contained within the
deployment.

Deploying to WildFly
--------------------

Make sure you have assigned the absolute path of your installation to the
`JBOSS_HOME` environment variable.

1. Open terminal and start the server by running

```shell
$JBOSS_HOME/bin/standalone.sh
```

2. Build and deploy the example to the server you have started in step 1 using command

```shell
mvn clean package wildfly:deploy
```

3. Now you can view the application at <http://localhost:8080/weld-numberguess>.

If you do not have WildFly server downloaded, you can use a Maven profile to provision the server and deploy the app onto it.

1. Provision server using the following command. Replace `XYZ` with the version of WildFly you need.

```shell
mvn clean package -Pprovision-wildfly-server -Dversion.wfly.server=XYZ
```

2. The server is now prepared under `/target/server` and already contains Numberguess example in its `standalone/deployments` directory. Execute the server startup script as follows:

```shell
./target/server/bin/standalone.sh
``` 

Deploying to standalone Tomcat
--------------------------------

If you want to run the application on a standalone Tomcat, first download and
extract Tomcat. This build assumes you will be running Tomcat in its default
configuration, with a hostname of localhost and port 8080. Build example using
command

```shell
mvn clean package -Ptomcat
```

and deploy the created WAR to Tomcat.

Deploying to standalone Jetty
-----------------------------

If you want to run the application on a standalone Jetty, first download and extract the Jetty distribution. This build assumes you will be running Jetty in its default configuration, with a hostname of localhost and port 8080.

Note that Jetty needs to have its CDI module (`ee10-cdi`) and websockets module (`ee10-websocket-jakarta`) enabled for Numberguess to work. Here's how to do it:

```shell
$ export JETTY_HOME=/path/to/jetty-home
$ mkdir /path/to/jetty-base
$ cd /path/to/jetty-base
$ java -jar $JETTY_HOME/start.jar --add-modules=server,http,ee10-deploy,ee10-cdi,ee10-websocket-jakarta
```
Build example using command

```shell
mvn clean package -Pjetty
```

then deploy the created WAR to Jetty (e.g. copy the artifact to webapps directory) and start Jetty. You should see the app under <http://localhost:8080/weld-numberguess>.

Executing tests
-----------------------------

Tests can be executed against any of the above containers.
Once the server/servlet is started and Numberguess is deployed, run the following command:

```shell
mvn clean verify -Pintegration-testing
```