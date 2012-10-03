package org.jboss.weld.tck.as7;

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
 * 
 * @author Martin Kouba
 */
public class JBossAS7EEResourceManager {

    private static final Logger logger = Logger.getLogger(JBossAS7EEResourceManager.class.getName());

    /**
     * Observe start of managed container and check/add required EE resources.  
     * 
     * @param eventContext
     */
    public void checkResources(@Observes EventContext<StartContainer> eventContext) {

        try {
            
            // Start the container
            eventContext.proceed();

            // Now check resources
            ModelControllerClient client = ModelControllerClient.Factory.create("localhost", 9999);

            // Check JMS topic and try to create one if it does not exist
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
                    throw new RuntimeException("Test JMS topic was not found and could not be created automatically: "
                            + response);
                }
                logger.log(Level.INFO, "Test JMS topic added");
            }

            // Check JMS queue and try to create one if it does not exist
            request = new ModelNode();
            request.get(ClientConstants.OP).set("read-resource");
            request.get("recursive").set(true);

            address = request.get(ClientConstants.OP_ADDR);
            address.add("subsystem", "messaging");
            address.add("hornetq-server", "default");
            address.add("jms-queue", "testQueue");

            response = client.execute(new OperationBuilder(request).build());

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
                    throw new RuntimeException("Test JMS topic was not found and could not be created automatically: "
                            + response);
                }
                logger.log(Level.INFO, "Test JMS queue added");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
