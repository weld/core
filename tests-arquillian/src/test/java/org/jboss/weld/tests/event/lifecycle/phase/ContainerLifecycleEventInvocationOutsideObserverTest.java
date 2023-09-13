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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test verifies, that a container lifecycle event method invocation throws {@link IllegalStateException} if performed
 * outside of an extension observer
 * method.
 *
 * @author Jozef Hartinger
 *
 *         See also WELD-1614
 */
@RunWith(Arquillian.class)
public class ContainerLifecycleEventInvocationOutsideObserverTest {

    @Inject
    private VerifyingExtension extension;
    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class,
                        Utils.getDeploymentNameAsHash(ContainerLifecycleEventInvocationOutsideObserverTest.class))
                .addPackage(ContainerLifecycleEventInvocationOutsideObserverTest.class.getPackage())
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
                event.addAnnotatedType(type, ContainerLifecycleEventInvocationOutsideObserverTest.class.getSimpleName());
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
                event.addBean(new DummyBean());
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addContext(new DummyContext());
            }
        }.run();
        new Invocation() {
            void execute() {
                event.addObserverMethod(new DummyObserverMethod());
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

    private static class DummyBean implements Bean<Object> {

        @Override
        public Object create(CreationalContext<Object> creationalContext) {
            return new Object();
        }

        @Override
        public void destroy(Object instance, CreationalContext<Object> creationalContext) {
        }

        @Override
        public Set<Type> getTypes() {
            return Collections.<Type> singleton(Object.class);
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return Collections.emptySet();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public Class<?> getBeanClass() {
            return Object.class;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }
    }

    private static class DummyContext implements Context {

        @Override
        public Class<? extends Annotation> getScope() {
            return RequestScoped.class;
        }

        @Override
        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            return null;
        }

        @Override
        public <T> T get(Contextual<T> contextual) {
            return null;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }

    private static class DummyObserverMethod implements ObserverMethod<Object> {

        @Override
        public Class<?> getBeanClass() {
            return Object.class;
        }

        @Override
        public Type getObservedType() {
            return Object.class;
        }

        @Override
        public Set<Annotation> getObservedQualifiers() {
            return Collections.emptySet();
        }

        @Override
        public Reception getReception() {
            return Reception.ALWAYS;
        }

        @Override
        public TransactionPhase getTransactionPhase() {
            return TransactionPhase.IN_PROGRESS;
        }

        @Override
        public void notify(Object event) {
            // noop
        }

    }
}
