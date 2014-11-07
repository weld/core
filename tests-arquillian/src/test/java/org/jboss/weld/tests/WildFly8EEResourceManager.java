package org.jboss.weld.tests;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

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
     * Observe {@link org.jboss.arquillian.container.spi.event.StartContainer} and check/add required EE resources.
     *
     * @param eventContext
     */
    public void checkResources(@Observes EventContext<StartContainer> eventContext) {

        try {
            // First start the container
            eventContext.proceed();

            // Then check resources
            ModelControllerClient client = ModelControllerClient.Factory.create("localhost", 9990);
            checkJmsQueue(client);
            checkJmsTopic(client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check JMS topic and try to create one if it does not exist.
     *
     * @param client
     * @throws java.io.IOException
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
     * @throws java.io.IOException
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
}
