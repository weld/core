/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.cluster;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequest;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.cluster.AbstractClusterTest;
import org.jboss.weld.test.util.Utils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NaiveClusterTest extends AbstractClusterTest {

    @BeforeMethod
    public void reset() {
        System.setProperty(ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION.get(),
                ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION.getDefaultValue()
                        .toString());
    }

    @Test(description = "A simple test to check session replication, doesn't carefully check if a bean ids are correct")
    public void testSimpleSessionReplication() throws Exception {

        TestContainer container1 = bootstrapContainer(1, Collections.singletonList(Foo.class));
        BeanManagerImpl beanManager1 = getBeanManager(container1);
        Bean<?> fooBean1 = beanManager1.resolve(beanManager1.getBeans(Foo.class));

        TestContainer container2 = bootstrapContainer(2, Collections.singletonList(Foo.class));
        BeanManagerImpl beanManager2 = getBeanManager(container2);
        Bean<?> fooBean2 = beanManager2.resolve(beanManager2.getBeans(Foo.class));

        use(1);
        // Set a value into Foo1
        Foo foo1 = (Foo) beanManager1.getReference(fooBean1, Foo.class, beanManager1.createCreationalContext(fooBean1));
        foo1.setName("container 1");

        replicateSession(1, container1, 2, container2);

        use(2);
        Foo foo2 = (Foo) beanManager2.getReference(fooBean2, Foo.class, beanManager2.createCreationalContext(fooBean2));
        assert foo2.getName().equals("container 1");
        use(2);
        container2.stopContainer();
        use(1);
        container1.stopContainer();
    }

    @Test(description = "A simple test to check conversation replication")
    public void testConversationReplication() throws Exception {

        TestContainer container1 = bootstrapContainer(1, Collections.singletonList(Baz.class));
        BeanManagerImpl beanManager1 = getBeanManager(container1);

        TestContainer container2 = bootstrapContainer(2, Collections.singletonList(Baz.class));
        BeanManagerImpl beanManager2 = getBeanManager(container2);

        use(1);

        // Set up the conversation context
        BoundRequest request1 = new BoundRequestImpl(container1.getSessionStore());
        BoundConversationContext conversationContext1 = Utils.getReference(beanManager1, BoundConversationContext.class);
        conversationContext1.associate(request1);
        conversationContext1.activate();

        // Set a value into Baz1
        Baz baz1 = Utils.getReference(beanManager1, Baz.class);
        baz1.setName("pete");

        // Begin the conversation
        Conversation conversation1 = Utils.getReference(beanManager1, Conversation.class);
        conversation1.begin();

        // refetch the test bean and check it has the right value
        baz1 = Utils.getReference(beanManager1, Baz.class);
        assert baz1.getName().equals("pete");

        // Simulate ending the request (from the POV of the conversation only!)
        assert !conversation1.isTransient();
        String cid = conversation1.getId();
        conversationContext1.invalidate();
        conversationContext1.deactivate();
        conversationContext1.dissociate(request1);

        // and start another, propagating the conversation
        request1 = new BoundRequestImpl(container1.getSessionStore());
        conversationContext1.associate(request1);
        conversationContext1.activate(cid);

        // refetch the test bean and check it has the right value
        baz1 = Utils.getReference(beanManager1, Baz.class);
        assert baz1.getName().equals("pete");
        assert !conversation1.isTransient();

        replicateSession(1, container1, 2, container2);

        use(2);

        // Set up the conversation context
        BoundRequest request2 = new BoundRequestImpl(container2.getSessionStore());
        BoundConversationContext conversationContext2 = Utils.getReference(beanManager2, BoundConversationContext.class);
        conversationContext2.associate(request2);
        conversationContext2.activate(cid);

        Baz baz2 = Utils.getReference(beanManager2, Baz.class);
        assert baz2.getName().equals("pete");

        Conversation conversation2 = Utils.getReference(beanManager2, Conversation.class);
        assert !conversation2.isTransient();

        use(2);
        container2.stopContainer();
        use(1);
        container1.stopContainer();
    }

    @Test
    public void testMultipleDependentObjectsSessionReplication() throws Exception {
        Collection<Class<?>> classes = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class);
        TestContainer container1 = bootstrapContainer(1, classes);
        BeanManagerImpl beanManager1 = getBeanManager(container1);
        Bean<?> stableBean1 = beanManager1.resolve(beanManager1.getBeans(Stable.class));

        TestContainer container2 = bootstrapContainer(2, classes);
        BeanManagerImpl beanManager2 = getBeanManager(container2);
        Bean<?> stableBean2 = beanManager2.resolve(beanManager2.getBeans(Stable.class));

        use(1);
        // Set a value into Foo1
        Stable stable1 = (Stable) beanManager1.getReference(stableBean1, Stable.class,
                beanManager1.createCreationalContext(stableBean1));
        stable1.getFodder().setAmount(10);
        stable1.getHorse().setName("George");

        replicateSession(1, container1, 2, container2);

        use(2);

        Stable stable2 = (Stable) beanManager2.getReference(stableBean2, Stable.class,
                beanManager2.createCreationalContext(stableBean2));
        assert stable2.getFodder().getAmount() == stable1.getFodder().getAmount();
        assert stable2.getHorse().getName() == null;

        use(1);
        assert stable1.getFodder().getAmount() == 10;
        assert stable1.getHorse().getName().equals("George");

        use(2);

        stable2.getFodder().setAmount(11);

        replicateSession(2, container2, 1, container1);

        use(1);

        stable1.getFodder().getAmount();

        assert stable1.getFodder().getAmount() == 11;
        use(1);
        container1.stopContainer();
        use(2);
        container2.stopContainer();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testVariableBeanDeploymentStructure() throws Exception {
        Collection<Class<?>> classes1 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class);
        Collection<Class<?>> classes2 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class, Foo.class);
        TestContainer container1 = bootstrapContainer(1, classes1);
        BeanManagerImpl beanManager1 = getBeanManager(container1);
        Bean<?> stableBean1 = beanManager1.resolve(beanManager1.getBeans(Stable.class));
        TestContainer container2 = bootstrapContainer(2, classes2);

        use(1);
        // Set a value into Foo1
        Stable stable1 = (Stable) beanManager1.getReference(stableBean1, Stable.class,
                beanManager1.createCreationalContext(stableBean1));
        stable1.getFodder().setAmount(10);
        stable1.getHorse().setName("George");

        try {
            replicateSession(1, container1, 2, container2);
        } finally {
            use(1);
            container1.stopContainer();
            use(2);
            container2.stopContainer();
        }
    }

    @Test
    public void testSessionReplication() throws Exception {
        Collection<Class<?>> classes1 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class);
        Collection<Class<?>> classes2 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class);
        TestContainer container1 = bootstrapContainer(1, classes1);
        BeanManagerImpl beanManager1 = getBeanManager(container1);
        Bean<?> stableBean1 = beanManager1.resolve(beanManager1.getBeans(Stable.class));

        TestContainer container2 = bootstrapContainer(2, classes2);
        BeanManagerImpl beanManager2 = getBeanManager(container2);
        Bean<?> stableBean2 = beanManager2.resolve(beanManager2.getBeans(Stable.class));

        use(1);
        // Set a value into Foo1
        Stable stable1 = (Stable) beanManager1.getReference(stableBean1, Stable.class,
                beanManager1.createCreationalContext(stableBean1));
        stable1.getFodder().setAmount(10);
        stable1.getHorse().setName("George");

        replicateSession(1, container1, 2, container2);

        use(2);

        Stable stable2 = (Stable) beanManager2.getReference(stableBean2, Stable.class,
                beanManager2.createCreationalContext(stableBean2));
        assert stable2.getFodder().getAmount() == stable1.getFodder().getAmount();
        assert stable2.getHorse().getName() == null;
        use(1);
        container1.stopContainer();
        use(2);
        container2.stopContainer();
    }

    @Test
    public void testVariableBeanDeploymentStructureNotVerified() throws Exception {
        Collection<Class<?>> classes1 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class);
        Collection<Class<?>> classes2 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class, Foo.class);
        System.setProperty(ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION.get(), "false");
        TestContainer container1 = bootstrapContainer(1, classes1);
        BeanManagerImpl beanManager1 = getBeanManager(container1);
        Bean<?> stableBean1 = beanManager1.resolve(beanManager1.getBeans(Stable.class));
        TestContainer container2 = bootstrapContainer(2, classes2);

        use(1);
        // Set a value into Foo1
        Stable stable1 = (Stable) beanManager1.getReference(stableBean1, Stable.class,
                beanManager1.createCreationalContext(stableBean1));
        stable1.getFodder().setAmount(10);
        stable1.getHorse().setName("George");

        try {
            replicateSession(1, container1, 2, container2);
        } finally {
            use(1);
            container1.stopContainer();
            use(2);
            container2.stopContainer();
        }
    }

    @Test
    public void testSessionReplicationWorksIfBeanIdIndexDisabled() throws Exception {
        Collection<Class<?>> classes1 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class);
        Collection<Class<?>> classes2 = Arrays.<Class<?>> asList(Stable.class, Horse.class, Fodder.class);
        System.setProperty(ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION.get(), "false");
        TestContainer container1 = bootstrapContainer(1, classes1);
        BeanManagerImpl beanManager1 = getBeanManager(container1);
        Bean<?> stableBean1 = beanManager1.resolve(beanManager1.getBeans(Stable.class));

        TestContainer container2 = bootstrapContainer(2, classes2);
        BeanManagerImpl beanManager2 = getBeanManager(container2);
        Bean<?> stableBean2 = beanManager2.resolve(beanManager2.getBeans(Stable.class));

        use(1);
        // Set a value into Foo1
        Stable stable1 = (Stable) beanManager1.getReference(stableBean1, Stable.class,
                beanManager1.createCreationalContext(stableBean1));
        stable1.getFodder().setAmount(10);
        stable1.getHorse().setName("George");

        replicateSession(1, container1, 2, container2);

        use(2);

        Stable stable2 = (Stable) beanManager2.getReference(stableBean2, Stable.class,
                beanManager2.createCreationalContext(stableBean2));
        assert stable2.getFodder().getAmount() == stable1.getFodder().getAmount();
        assert stable2.getHorse().getName() == null;
        use(1);
        container1.stopContainer();
        use(2);
        container2.stopContainer();
    }

}
