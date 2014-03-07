/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.event.lifecycle.phase;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test verifies, that a container lifecycle event method invocation throws {@link IllegalStateException} if performed outside of an extension observer
 * method.
 * 
 * @author Jozef Hartinger
 * 
 * @see WELD-1614
 */
@RunWith(Arquillian.class)
public class ContainerLifecycleEventInvocationOutsideObserverTest {

    @Inject
    private VerifyingExtension extension;
    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(ContainerLifecycleEventInvocationOutsideObserverTest.class.getPackage())
                .addAsServiceProvider(Extension.class, VerifyingExtension.class);
    }

    private static abstract class Invocation {
        void run() {
            try {
                execute();
                Assert.fail("Expected exception not thrown");
            } catch (IllegalStateException expected) {
            }
        }

        abstract void execute();
    }

    @Test
    public void testBeforeBeanDiscovery() {
        final BeforeBeanDiscovery event = extension.getBeforeBeanDiscovery();
        final AnnotatedType<?> type = manager.createAnnotatedType(ContainerLifecycleEventInvocationOutsideObserverTest.class);
        final AnnotatedType<? extends Annotation> annotation = manager.createAnnotatedType(SimpleAnnotation.class);

        new Invocation() {
            void execute() {
                event.addAnnotatedType(type);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addAnnotatedType(type, "foo");
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addInterceptorBinding(SimpleAnnotation.class);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addInterceptorBinding(annotation);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addQualifier(SimpleAnnotation.class);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addQualifier(annotation);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addScope(SimpleAnnotation.class, true, false);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addStereotype(SimpleAnnotation.class);
            }
        }.run();
    }

    @Test
    public void testAfterTypeDiscovery() {
        final AfterTypeDiscovery event = extension.getAfterTypeDiscovery();
        final AnnotatedType<?> type = manager.createAnnotatedType(ContainerLifecycleEventInvocationOutsideObserverTest.class);
        new Invocation() {
            void execute() {
                event.addAnnotatedType(type, "bar");
            }
        }.run();
        new Invocation() {
            void execute() {
                event.getAlternatives();
            }
        }.run();
        new Invocation() {
            void execute() {
                event.getDecorators();
            }
        }.run();
        new Invocation() {
            void execute() {
                event.getInterceptors();
            }
        }.run();
    }

    @Test
    public void testAfterBeanDiscovery() {
        final AfterBeanDiscovery event = extension.getAfterBeanDiscovery();
        new Invocation() {
            void execute() {
                event.addBean(null);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addContext(null);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addObserverMethod(null);
            }
        }.run();
        new Invocation() {
            void execute() {
                event.getAnnotatedType(ContainerLifecycleEventInvocationOutsideObserverTest.class, "foo");
            }
        }.run();
        new Invocation() {
            void execute() {
                event.getAnnotatedTypes(ContainerLifecycleEventInvocationOutsideObserverTest.class);
            }
        }.run();
    }
}
