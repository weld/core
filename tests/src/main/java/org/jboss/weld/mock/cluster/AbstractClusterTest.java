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
package org.jboss.weld.mock.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.jboss.arquillian.container.weld.embedded.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.embedded.mock.FlatDeployment;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.util.WeldDefaultProxyServices;
import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.util.reflection.Reflections;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class AbstractClusterTest {

    private Singleton<Container> singleton;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void beforeClass() throws Exception {
        singleton = (Singleton) getInstanceField().get(null);
        getInstanceField().set(null, new SwitchableSingletonProvider().create(Container.class));
    }

    private static Field getInstanceField() throws Exception {
        Field field = Container.class.getDeclaredField("instance");
        field.setAccessible(true);
        return field;
    }

    @AfterClass
    public void afterClass() throws Exception {
        getInstanceField().set(null, singleton);
    }

    protected TestContainer bootstrapContainer(int id, Collection<Class<?>> classes) {
        // Bootstrap container
        SwitchableSingletonProvider.use(id);

        TestContainer container = new TestContainer(new FlatDeployment(new BeanDeploymentArchiveImpl(classes)));
        container.getDeployment().getServices().add(ProxyServices.class, new WeldDefaultProxyServices());
        container.startContainer();
        container.ensureRequestActive();

        return container;
    }

    protected static BeanManagerImpl getBeanManager(TestContainer container) {
        return (BeanManagerImpl) container
                .getBeanManager(container.getDeployment().getBeanDeploymentArchives().iterator().next());
    }

    protected void use(int id) {
        SwitchableSingletonProvider.use(id);
    }

    protected void replicateSession(int fromId, TestContainer fromContainer, int toId, TestContainer toContainer)
            throws Exception {
        // Mimic replicating the session - first serialize the objects
        byte[] bytes = serialize(fromContainer.getSessionStore());
        use(toId);

        // Deactivate the other store
        BoundSessionContext sessionContext = toContainer.instance().select(BoundSessionContext.class).get();
        sessionContext.deactivate();
        sessionContext.dissociate(toContainer.getSessionStore());

        // then copy them into the other session store
        toContainer.getSessionStore().putAll(Reflections.<Map<String, Object>> cast(deserialize(bytes)));
        sessionContext.associate(toContainer.getSessionStore());

        // then activate again
        sessionContext.activate();
        use(fromId);
    }

    protected byte[] serialize(Object instance) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(instance);
        return bytes.toByteArray();
    }

    protected Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return in.readObject();
    }
}
