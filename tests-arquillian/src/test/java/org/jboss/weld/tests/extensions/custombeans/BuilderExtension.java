/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.custombeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.TypeLiteral;

/**
 *
 * @author Martin Kouba
 */
public class BuilderExtension implements Extension {

    static final AtomicBoolean DISPOSED = new AtomicBoolean(false);

    public void processAnnotatedType(@Observes ProcessAnnotatedType<? extends VetoedBean> event) {
        event.veto();
    }

    @SuppressWarnings("serial")
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {

        AnnotatedType<Foo> annotatedType = beanManager.createAnnotatedType(Foo.class);

        // Read from bean attributes, change the name and remove @Model stereotype
        // Note that we have to set the scope manually as it's initialized to @RequestScoped through the bean attributes
        event.addBean().beanClass(Foo.class).read(beanManager.createBeanAttributes(annotatedType)).name("bar")
                .stereotypes(Collections.emptySet()).scope(Dependent.class).produceWith((i) -> {
                    Foo foo = new Foo();
                    foo.postConstruct();
                    return foo;
                });

        // Read from AT, add qualifier, set id
        event.addBean().read(annotatedType).id("BAZinga").addQualifier(Juicy.Literal.INSTANCE);

        // Read from AT, set the scope
        event.addBean().read(beanManager.createAnnotatedType(Bar.class)).scope(Dependent.class);

        // Test simple produceWith callback
        event.addBean().addType(Integer.class).addQualifier(Random.Literal.INSTANCE)
                .produceWith((i) -> {
                    i.select(DependentBean.class).get(); // create dependent instance
                    return new java.util.Random().nextInt(1000);
                })
                .disposeWith((beanInstance, instance) -> {
                    instance.select(DependentBean.class).get(); // create dependent instance
                    DISPOSED.set(true);
                });

        // Test produceWith callback with Instance<Object> param
        event.addBean().addType(Long.class).addQualifier(AnotherRandom.Literal.INSTANCE)
                .produceWith((i) -> i.select(Foo.class, Juicy.Literal.INSTANCE).get().getId() * 2);

        // Test TypeLiteral
        List<String> list = new ArrayList<String>();
        list.add("FOO");
        event.addBean().addType(new TypeLiteral<List<String>>() {
        }).addQualifier(Juicy.Literal.INSTANCE).produceWith((i) -> list);

        // Test transitive type closure
        event.addBean().addTransitiveTypeClosure(Foo.class).addQualifier(Random.Literal.INSTANCE)
                .produceWith((i) -> new Foo(-1l));

        // Test default qualifiers
        event.addBean().addType(Configuration.class).produceWith((i) -> new Configuration(1));

        // Test default scopes
        event.addBean().addQualifier(Bla.Literal.of("dependent")).addType(Integer.class).createWith((ctx) -> 1);
        event.addBean().addQualifier(Bla.Literal.of("model")).addStereotype(Model.class).addType(Integer.class).createWith((ctx) -> 2);
        event.addBean().addQualifier(Bla.Literal.of("more")).addStereotype(Model.class).addStereotype(SuperCoolStereotype.class).addType(Integer.class).createWith((ctx) -> 3);

        // add a bean testing that when a bean has @Named and @Any, @Default will be added automatically
        event.addBean()
                .beanClass(String.class)
                .addType(String.class)
                .addQualifiers(NamedLiteral.of("string"), Any.Literal.INSTANCE)
                .createWith(a -> "foo")
                .scope(ApplicationScoped.class);
    }
}
