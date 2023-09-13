package org.jboss.weld.tests.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.event.StartContainer;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.as.controller.client.ModelControllerClient;

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

    public static final String TEST_QUEUE_DESTINATION = "java:jboss/exported/jms/queue/test";
    public static final String TEST_TOPIC_DESTINATION = "java:jboss/exported/jms/topic/test";
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
