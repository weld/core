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
package org.jboss.weld.tests.activities.current;

import com.sun.el.ExpressionFactoryImpl;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.Utils;
import org.jboss.weld.test.el.EL;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Spec version: 20090519
 */
@RunWith(Arquillian.class)
public class ELCurrentActivityTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(ELCurrentActivityTest.class.getPackage())
                .addClasses(Utils.class, EL.class)
                .addPackages(true, ExpressionFactoryImpl.class.getPackage());
    }

    private static class DummyContext implements Context {
        private boolean active = true;

        public <T> T get(Contextual<T> contextual) {
            return null;
        }

        public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
            return null;
        }

        public Class<? extends Annotation> getScope() {
            return Dummy.class;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

    }

    private static class Daisy implements Bean<Cow> {

        private static final Set<Type> TYPES = new HashSet<Type>();

        private final static Set<Annotation> BINDING_TYPES = new HashSet<Annotation>();


        static {
            TYPES.add(Cow.class);
            TYPES.add(Object.class);
            BINDING_TYPES.add(new AnnotationLiteral<Tame>() {
            });
        }

        public Daisy(BeanManager beanManager) {
        }

        public Set<Annotation> getQualifiers() {
            return BINDING_TYPES;
        }

        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        public String getName() {
            return "daisy";
        }

        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        public Set<Type> getTypes() {
            return TYPES;
        }

        public boolean isNullable() {
            return true;
        }

        public boolean isSerializable() {
            return false;
        }

        public Cow create(CreationalContext<Cow> creationalContext) {
            return new Cow();
        }

        public void destroy(Cow instance, CreationalContext<Cow> creationalContext) {
            // TODO Auto-generated method stub

        }

        public Class<?> getBeanClass() {
            return Cow.class;
        }

        public boolean isAlternative() {
            return false;
        }

        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test
    public void testELEvaluationProcessedByCurrentActivty() {
        Context dummyContext = new DummyContext();
        beanManager.addContext(dummyContext);
        Assert.assertEquals(1, beanManager.getBeans(Cow.class).size());
        BeanManagerImpl childActivity = beanManager.createActivity();
        childActivity.addBean(new Daisy(childActivity));
        childActivity.setCurrent(dummyContext.getScope());
        Assert.assertNotNull(Utils.evaluateValueExpression(beanManager, "#{daisy}", Cow.class));
    }

}
