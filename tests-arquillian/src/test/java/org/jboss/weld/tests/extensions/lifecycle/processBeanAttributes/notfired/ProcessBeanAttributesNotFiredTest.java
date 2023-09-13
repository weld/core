/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.extensions.lifecycle.processBeanAttributes.notfired;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.Type;
import java.security.Principal;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class ProcessBeanAttributesNotFiredTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProcessBeanAttributesNotFiredTest.class))
                .addClasses(Foo.class, MyExtension.class)
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Test
    public void testProcessBeanAttributesNotFiredForProgrammaticallyAddedBeans() {
        assertFalse("ProcessBeanAttributes was called for built-in bean",
                MyExtension.observedNames.contains(MyExtension.PROGRAMMATICALLY_ADDED_BEAN_NAME));
    }

    @Test
    public void testProcessBeanAttributesNotFiredForBuiltInBeans() {
        final Type[] types = new Type[] { Instance.class, Bean.class, Event.class, EventMetadata.class, InjectionPoint.class,
                BeanManager.class, Decorator.class,
                Interceptor.class, UserTransaction.class, Principal.class, HttpServletRequest.class, HttpSession.class,
                Conversation.class, ServletContext.class };
        for (Type type : types) {
            assertFalse("ProcessBeanAttributes was called for built-in bean " + type, MyExtension.observedTypes.contains(type));
        }
    }
}
