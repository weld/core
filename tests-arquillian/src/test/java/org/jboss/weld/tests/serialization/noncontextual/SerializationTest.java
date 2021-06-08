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
package org.jboss.weld.tests.serialization.noncontextual;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

@RunWith(Arquillian.class)
public class SerializationTest {

    @Inject
    BeanManager beanManager;

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SerializationTest.class)).addPackage(SerializationTest.class.getPackage());
    }

    /*
    * description =
    * "http://lists.jboss.org/pipermail/weld-dev/2010-February/002265.html"
    */
    @Test
    public void testSerializationOfEventInNonContextual() throws Exception {

        NonContextual instance = new NonContextual();
        beanManager.getInjectionTargetFactory(beanManager.createAnnotatedType(NonContextual.class))
                .createInjectionTarget(null).inject(
                instance, beanManager.createCreationalContext((Contextual<NonContextual>) null));
        new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(instance);
    }
}
