package org.jboss.weld.tck.wildfly8;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.cdi.tck.impl.ConfigurationFactory;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 * Assumptions:
 * <ul>
 * <li>WildFly 8 managed container adapter</li>
 * <li>only one container is used in the same time</li>
 * <li>H2 JDBC driver is properly configured</li>
 * </ul>
 *
 * @author Martin Kouba
 */
public class WildFly8EEResourceManager {

    private static final Logger logger = Logger.getLogger(WildFly8EEResourceManager.class.getName());

    /**
     * Observe {@link StartContainer} and check/add required EE resources.
     *
     * @param eventContext
     */
    public void checkResources(@Observes EventContext<StartContainer> eventContext) {

        try {
            // First start the container
            eventContext.proceed();

            // Then check resources
            ModelControllerClient client = ModelControllerClient.Factory.create("localhost", 9990);
            WildFlyMessaging messaging = WildFlyMessaging.get(client);
            if (messaging != null) {
                messaging.checkJmsQueue(client);
                messaging.checkJmsTopic(client);
            } else {
                /*
                 * JMS subsystem may not be installed (e.g. when debugging against standalone.xml) If this happens, do not
                 * attempt to install Queue/Topic as
                 * that always fails
                 */
                logger.log(Level.WARNING, "JMS subsystem not installed. Skipping test Queue/Topic installation.");
            }
            checkTestDataSource(client);
            checkEarSubdeploymentsIsolation(client);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check JMS topic and try to create one if it does not exist.
     *
     * @param client
     * @throws IOException
     */
    private void checkJmsTopic(ModelControllerClient client) throws IOException {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set("read-resource");
        request.get("recursive").set(true);

        ModelNode address = request.get(ClientConstants.OP_ADDR);
        address.add("subsystem", "messaging");
        address.add("hornetq-server", "default");
        address.add("jms-topic", "testTopic");

        ModelNode response = client.execute(new OperationBuilder(request).build());

        if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {

            request = new ModelNode();
            request.get(ClientConstants.OP).set("add");

            address = request.get(ClientConstants.OP_ADDR);
            address.add("subsystem", "messaging");
            address.add("hornetq-server", "default");
            address.add("jms-topic", "testTopic");

            ModelNode entries = request.get("entries");

            entries.add("topic/test");
            entries.add("java:jboss/exported/jms/topic/test");

            response = client.execute(new OperationBuilder(request).build());

            if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
                throw new RuntimeException("Test JMS topic was not found and could not be created automatically: " + response);
            }
            logger.log(Level.INFO, "Test JMS topic added");
        }
    }

    /**
     * Check JMS queue and try to create one if it does not exist.
     *
     * @param client
     * @throws IOException
     */
    private void checkJmsQueue(ModelControllerClient client) throws IOException {

        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set("read-resource");
        request.get("recursive").set(true);

        ModelNode address = request.get(ClientConstants.OP_ADDR);
        address.add("subsystem", "messaging");
        address.add("hornetq-server", "default");
        address.add("jms-queue", "testQueue");

        ModelNode response = client.execute(new OperationBuilder(request).build());

        if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {

            request = new ModelNode();
            request.get(ClientConstants.OP).set("add");

            address = request.get(ClientConstants.OP_ADDR);
            address.add("subsystem", "messaging");
            address.add("hornetq-server", "default");
            address.add("jms-queue", "testQueue");

            ModelNode entries = request.get("entries");
            entries.add("queue/test");
            entries.add("java:jboss/exported/jms/queue/test");

            response = client.execute(new OperationBuilder(request).build());

            if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
                throw new RuntimeException("Test JMS queue was not found and could not be created automatically: " + response);
            }
            logger.log(Level.INFO, "Test JMS queue added");
        }

    }

    /**
     * Check test data source and try to create one if it does not exist.
     *
     * @param client
     * @throws IOException
     */
    private void checkTestDataSource(ModelControllerClient client) throws IOException {

        // JNDI name has to be set via properties at the moment
        String testDataSourceJndiName = ConfigurationFactory.get().getTestDataSource();

        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set("read-resource");
        request.get("recursive").set("true");
        request.get(ClientConstants.OP_ADDR).get("subsystem").set("datasources");

        ModelNode response = client.execute(new OperationBuilder(request).build());

        if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            throw new RuntimeException("Unable to read existing data sources: " + response);
        }

        boolean found = false;
        if (response.get("result").hasDefined("data-source")) {
            for (Property dataSource : response.get("result").get("data-source").asPropertyList()) {
                if (dataSource.getValue().get("jndi-name").asString().equals(testDataSourceJndiName)) {
                    logger.log(Level.INFO, "Test data source found");
                    found = true;
                }
            }
        }

        if (!found) {

            String poolName = "TestTckDS";
            request = new ModelNode();
            request.get(ClientConstants.OP).set("add");

            ModelNode address = request.get(ClientConstants.OP_ADDR);
            address.add("subsystem", "datasources");
            address.add("data-source", poolName);

            request.get("jndi-name").set(testDataSourceJndiName);
            request.get("pool-name").set(poolName);
            request.get("enabled").set("true");
            request.get("use-java-context").set("true");
            request.get("connection-url").set("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            request.get("driver-name").set("h2");
            request.get("security").get("user-name").set("sa");
            request.get("security").get("password").set("sa");

            response = client.execute(new OperationBuilder(request).build());

            if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
                throw new RuntimeException(
                        "Test data source was not found and could not be created automatically: " + response);
            }

            // As of AS7 7.1 we have to enable DS
            ModelNode enableRequest = new ModelNode();
            enableRequest.get(ClientConstants.OP).set("enable");
            enableRequest.get("address").get("subsystem").set("datasources");
            enableRequest.get("address").get("data-source").set(poolName);

            ModelNode enableResult = client.execute(new OperationBuilder(enableRequest).build());

            if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
                throw new RuntimeException("Test data source could not be enabled automatically: " + enableResult);
            }

            logger.log(Level.INFO, "Test data source added: {0}", testDataSourceJndiName);
        }

    }

    private void checkEarSubdeploymentsIsolation(ModelControllerClient client) throws IOException {

        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set("read-attribute");
        request.get("name").set("ear-subdeployments-isolated");
        request.get(ClientConstants.OP_ADDR).get("subsystem").set("ee");

        ModelNode response = client.execute(new OperationBuilder(request).build());
        boolean isolated = response.get(ClientConstants.RESULT).asBoolean();

        if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            throw new RuntimeException("Unable to read ear-subdeployments-isolated attribute: " + response);
        }

        if (!isolated) {
            ModelNode writeRequest = new ModelNode();
            writeRequest.get(ClientConstants.OP).set("write-attribute");
            writeRequest.get("name").set("ear-subdeployments-isolated");
            writeRequest.get("value").set("true");
            writeRequest.get(ClientConstants.OP_ADDR).get("subsystem").set("ee");

            ModelNode writeResponse = client.execute(new OperationBuilder(writeRequest).build());

            if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
                throw new RuntimeException("Unable to write ear-subdeployments-isolated attribute: " + writeResponse);
            }
        }

    }

}
