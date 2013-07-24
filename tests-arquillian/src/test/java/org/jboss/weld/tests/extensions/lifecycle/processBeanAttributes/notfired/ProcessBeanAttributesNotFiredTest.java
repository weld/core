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

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Set;

import javax.enterprise.context.Conversation;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
        return ShrinkWrap.create(BeanArchive.class)
                .addClasses(Foo.class, MyExtension.class)
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Test
    public void testProcessBeanAttributesNotFiredForProgrammaticallyAddedBeans() {
        for (ProcessBeanAttributes<?> event : MyExtension.receivedProcessBeanAttributesEvents) {
            if (isProgrammaticallyAddedBean(event)) {
                fail("ProcessBeanAttributes fired for programmatically added bean");
            }
        }
    }

    @Test
    public void testProcessBeanAttributesNotFiredForBuiltInBeans() {
        for (ProcessBeanAttributes<?> event : MyExtension.receivedProcessBeanAttributesEvents) {
            if (isBuiltInBean(event)) {
                fail("ProcessBeanAttributes was called for built-in bean " + event.getAnnotated());
            }
        }
    }

    private boolean isProgrammaticallyAddedBean(ProcessBeanAttributes<?> event) {
        return MyExtension.PROGRAMMATICALLY_ADDED_BEAN_NAME.equals(event.getBeanAttributes().getName());
    }

    private boolean isBuiltInBean(ProcessBeanAttributes<?> event) {
        Set<Type> types = event.getBeanAttributes().getTypes();
        return types.contains(Instance.class)
            || types.contains(Bean.class)
            || types.contains(Event.class)
            || types.contains(EventMetadata.class)
            || types.contains(InjectionPoint.class)
            || types.contains(BeanManager.class)
            || types.contains(Decorator.class)
            || types.contains(Interceptor.class)
            || types.contains(UserTransaction.class)
            || types.contains(Principal.class)
            || types.contains(HttpServletRequest.class)
            || types.contains(HttpSession.class)
            || types.contains(Conversation.class)
            || types.contains(ServletContext.class);
    }
}
