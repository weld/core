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
package org.jboss.weld.tests.specialization.weld802;

import org.jboss.weld.introspector.ForwardingWeldClass;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * @author Ales Justin
 */
public class CustomExtension implements Extension {
    public void registerBeans(@Observes BeforeBeanDiscovery event, final BeanManager manager) {
        final WeldClass<Foo> foo = getFooWeldClass(manager);
        final WeldClass<Bar> bar = new ForwardingWeldClass<Bar>() {
            @Override
            protected WeldClass<Bar> delegate() {
                return (WeldClass<Bar>) manager.createAnnotatedType(Bar.class);
            }

            @Override
            public WeldClass<? super Bar> getWeldSuperclass() {
                return foo;
            }
        };
        event.addAnnotatedType(foo);
        event.addAnnotatedType(bar);
    }

    protected WeldClass<Foo> getFooWeldClass(BeanManager manager) {
        if (manager instanceof BeanManagerImpl) {
            BeanManagerImpl bmi = (BeanManagerImpl) manager;
            ClassTransformer ct = bmi.getServices().get(ClassTransformer.class);
            return WeldClassImpl.of(Foo.class, ct);
        } else {
            return (WeldClass<Foo>) manager.createAnnotatedType(Foo.class);
        }
    }
}
