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
package org.jboss.weld.tests.extensions.annotatedType.ejb;

import org.jboss.weld.test.util.annotated.TestAnnotatedTypeBuilder;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotatedTypeEjbExtension implements Extension {
    /**
     * Adds two ejb beans
     */
    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        TestAnnotatedTypeBuilder<Lathe> builder = new TestAnnotatedTypeBuilder<Lathe>(Lathe.class);
        builder.addToClass(new AnnotationLiteral<SmallLathe>() {
        });
        beforeBeanDiscovery.addAnnotatedType(builder.create(), Lathe.class.getSimpleName());
        builder = new TestAnnotatedTypeBuilder<Lathe>(Lathe.class);
        builder.addToClass(new AnnotationLiteral<BigLathe>() {
        });
        beforeBeanDiscovery.addAnnotatedType(builder.create(), BigLathe.class.getSimpleName());
    }

    /**
     * Adds annotations to an EJB
     */
    public void overrideLatheAnnotations(@Observes ProcessAnnotatedType<Lathe> event) throws SecurityException, NoSuchMethodException {
        if (!event.getAnnotatedType().isAnnotationPresent(SmallLathe.class) && !event.getAnnotatedType().isAnnotationPresent(BigLathe.class)) {
            TestAnnotatedTypeBuilder<Lathe> builder = new TestAnnotatedTypeBuilder<Lathe>(Lathe.class);
            for (Annotation a : event.getAnnotatedType().getAnnotations()) {
                builder.addToClass(a);
            }
            Method method = Lathe.class.getMethod("doWork");
            builder.addToMethod(method, new AnnotationLiteral<ConveyorShaft>() {
            });
            builder.addToMethod(method, new AnnotationLiteral<Produces>() {
            });
            event.setAnnotatedType(builder.create());
        }
    }
}
