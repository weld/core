/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.builtinBeans.injectionPoint.disposer;

import static org.junit.Assert.assertTrue;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PropagationOfInjectionPointMetadataTest {

    @Inject
    private BeanManager manager;

    @Inject
    private FooExtension extension;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(PropagationOfInjectionPointMetadataTest.class.getPackage())
                .addAsServiceProvider(Extension.class, FooExtension.class).addClasses(Utils.class);
    }

    @Test
    public void testInjectionPointMetadataPropagatedToDisposerMethod() throws Exception {
        assertTrue(extension.isWrapped());
        Bean<Foo> bean = Reflections.cast(manager.resolve(manager.getBeans(Foo.class)));
        CreationalContext<Foo> ctx = manager.createCreationalContext(bean);
        Foo instance = bean.create(ctx);
        Foo instance2 = Utils.deserialize(Utils.serialize(instance));
        CreationalContext<Foo> ctx2 = Utils.deserialize(Utils.serialize(ctx));
        bean.destroy(instance2, ctx2);
        assertTrue(BarProducer.isDisposerCalled());
    }
}
