/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tck.wildfly8;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.weld.util.collections.ImmutableList;

abstract class WildFlyMessaging {

    private static final Logger LOGGER = Logger.getLogger(WildFly8EEResourceManager.class.getName());

    static WildFlyMessaging get(ModelControllerClient client) throws IOException {
        for (WildFlyMessaging messaging : WildFlyMessaging.INSTANCES) {
            if (messaging.isActive(client)) {
                return messaging;
            }
        }
        return null;
    }

    private static final List<WildFlyMessaging> INSTANCES = ImmutableList.of(new ActiveMQ(), new HornetQ());

    boolean isActive(ModelControllerClient client) throws IOException {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
        ModelNode address = request.get(ClientConstants.OP_ADDR);
        address.add(getSubsystem());

        ModelNode response = client.execute(new OperationBuilder(request).build());
        return response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS);
    }

    void checkJmsQueue(ModelControllerClient client) throws IOException {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);

        ModelNode address = request.get(ClientConstants.OP_ADDR);
        address.add(getSubsystem());
        address.add(getJmsServer());
        address.add("jms-queue", "testQueue");

        ModelNode response = client.execute(new OperationBuilder(request).build());
        if (response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            return;
        }

        request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);

        address = request.get(ClientConstants.OP_ADDR);
        address.add(getSubsystem());
        address.add(getJmsServer());
        address.add("jms-queue", "testQueue");

        ModelNode entries = request.get("entries");
        entries.add("queue/test");
        entries.add("java:jboss/exported/jms/queue/test");

        response = client.execute(new OperationBuilder(request).build());

        if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            throw new RuntimeException("Test JMS queue was not found and could not be created automatically: " + response);
        }
        LOGGER.log(Level.INFO, "Test JMS queue added");
    }

    void checkJmsTopic(ModelControllerClient client) throws IOException {
        ModelNode request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);

        ModelNode address = request.get(ClientConstants.OP_ADDR);
        address.add(getSubsystem());
        address.add(getJmsServer());
        address.add("jms-topic", "testTopic");

        ModelNode response = client.execute(new OperationBuilder(request).build());

        if (response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            return;
        }

        request = new ModelNode();
        request.get(ClientConstants.OP).set(ClientConstants.ADD);

        address = request.get(ClientConstants.OP_ADDR);
        address.add(getSubsystem());
        address.add(getJmsServer());
        address.add("jms-topic", "testTopic");

        ModelNode entries = request.get("entries");

        entries.add("topic/test");
        entries.add("java:jboss/exported/jms/topic/test");

        response = client.execute(new OperationBuilder(request).build());

        if (!response.get(ClientConstants.OUTCOME).asString().equals(ClientConstants.SUCCESS)) {
            throw new RuntimeException("Test JMS topic was not found and could not be created automatically: " + response);
        }
        LOGGER.log(Level.INFO, "Test JMS topic added");
    }

    abstract Property getSubsystem();

    abstract Property getJmsServer();

    private static class HornetQ extends WildFlyMessaging {

        private static final String SUBSYSTEM_NAME = "messaging";
        private static final String SERVER_ATTRIBUTE_NAME = "hornetq-server";
        private static final String SERVER_NAME = "default";

        @Override
        Property getSubsystem() {
            return new Property(ClientConstants.SUBSYSTEM, new ModelNode(SUBSYSTEM_NAME));
        }

        @Override
        Property getJmsServer() {
            return new Property(SERVER_ATTRIBUTE_NAME, new ModelNode(SERVER_NAME));
        }
    }

    private static class ActiveMQ extends WildFlyMessaging {

        private static final String SUBSYSTEM_NAME = "messaging-activemq";
        private static final String SERVER_ATTRIBUTE_NAME = "server";
        private static final String SERVER_NAME = "default";

        @Override
        Property getSubsystem() {
            return new Property(ClientConstants.SUBSYSTEM, new ModelNode(SUBSYSTEM_NAME));
        }

        @Override
        Property getJmsServer() {
            return new Property(SERVER_ATTRIBUTE_NAME, new ModelNode(SERVER_NAME));
        }
    }
}
