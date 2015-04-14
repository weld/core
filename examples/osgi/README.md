Weld Paint Example
========================

This example demonstrates the use of Weld in OSGi environment.

Deploying to Karaf
--------------------

Make sure you have Apache Karaf installed.

1. Build the Paint example. Run the following command in the examples/osgi directory

        mvn clean install

2. Open terminal and start the server by running the following command in the Karaf directory

        bin/karaf

3. Install Pax CDI and Weld by running the following command in the Karaf console:

        feature:install pax-cdi-weld

4. Deploy the Paint API artifact 

        bundle:install -s file:///path/to/weld/examples/osgi/paint-api/target/weld-osgi-paint-api.jar

5. Deploy the Paint Core artifact 

        bundle:install -s file:///path/to/weld/examples/osgi/paint-core/target/weld-osgi-paint-core.jar
This will start the Paint application. For now with only a single paintable shape - Circle

6. Deploy Square and Triangle bundles

        bundle:install -s file:///path/to/weld/examples/osgi/paint-square/target/weld-osgi-paint-square.jar
        bundle:install -s file:///path/to/weld/examples/osgi/paint-triangle/target/weld-osgi-paint-triangle.jar

