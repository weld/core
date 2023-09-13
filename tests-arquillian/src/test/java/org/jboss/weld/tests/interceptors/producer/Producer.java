/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.producer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InterceptionFactory;

@ApplicationScoped
public class Producer {

    static final List<String> INVOCATIONS = new ArrayList<>();

    @Produced
    @Dependent
    @Produces
    public Foo produceFoo(InterceptionFactory<Foo> interceptionFactory) {
        interceptionFactory.configure()
                .filterMethods((m) -> m.getJavaMember().getName().equals("ping") && m.getJavaMember().getParameterCount() == 0)
                .findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new Foo());
    }

    @Produced("classLevel")
    @ApplicationScoped
    @Produces
    public Foo produceFooWithClassLevelBinding(InterceptionFactory<Foo> interceptionFactory) {
        interceptionFactory.configure().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new Foo());
    }

    @Produced("ejbInterceptors")
    @ApplicationScoped
    @Produces
    public Foo produceFooWithEjbInterceptors(InterceptionFactory<Foo> interceptionFactory) {
        interceptionFactory.configure()
                .filterMethods((m) -> m.getJavaMember().getName().equals("ping") && m.getJavaMember().getParameterCount() == 0)
                .findFirst().get().add(new InterceptorsLiteral(HelloInterceptor.class));
        return interceptionFactory.createInterceptedInstance(new Foo());
    }

    @Produced("empty")
    @Produces
    public Foo produceFooWithNoBinding(InterceptionFactory<Foo> interceptionFactory) {
        return interceptionFactory.createInterceptedInstance(new Foo());
    }

    @Produced
    @Produces
    public Bar produceBar(InterceptionFactory<Bar> interceptionFactory, Bar bar) {
        // The binding should be ignored
        interceptionFactory.configure().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(bar);
    }

    @Produced
    @Dependent
    @Produces
    public Map<String, Object> produceMap(InterceptionFactory<HashMap<String, Object>> interceptionFactory) {
        interceptionFactory.ignoreFinalMethods().configure().filterMethods((m) -> {
            if (m.getJavaMember().getDeclaringClass().equals(HashMap.class) && m.getJavaMember().getName().equals("put")
                    && m.getJavaMember().getParameterCount() == 2) {
                return true;
            }
            return false;
        }).findFirst().get().add(Monitor.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new HashMap<>());
    }

    @Produced
    @Dependent
    @Produces
    public List<Object> produceList(InterceptionFactory<List<Object>> interceptionFactory) {
        interceptionFactory.ignoreFinalMethods().configure().filterMethods((m) -> {
            if (m.getJavaMember().getName().equals("add")
                    && m.getJavaMember().getParameterCount() == 1) {
                return true;
            }
            return false;
        }).findFirst().get().add(Monitor.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new ArrayList<>());
    }

    @Produces
    @Produced
    public FooParent produceFooParent(InterceptionFactory<FooParent> interceptionFactory) {
        interceptionFactory.configure().filterMethods((m) -> m.getJavaMember().getName().equals("ping")).findFirst().get()
                .add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new FooChild());
    }

    @Produces
    @Produced
    public AbstractBar produceAbstractBar(InterceptionFactory<AbstractBar> interceptionFactory) {
        interceptionFactory.configure().filterMethods((m) -> m.getJavaMember().getName().equals("ping")).findFirst().get()
                .add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new BarImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public SomeInterface produceBeanBasedOnInterface(InterceptionFactory<SomeInterface> interceptionFactory) {
        interceptionFactory.configure().add(Monitor.Literal.INSTANCE)
                .filterMethods((m) -> m.getJavaMember().getName().equals("ping")
                        && Arrays.asList(m.getJavaMember().getParameterTypes()).contains(Double.class))
                .findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new SomeImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public InterfaceWithDefaultMethod produceBeanBasedOnInterfaceWithDefaultMethod(
            InterceptionFactory<InterfaceWithDefaultMethod> interceptionFactory) {
        interceptionFactory.configure().add(Monitor.Literal.INSTANCE)
                .filterMethods((m) -> m.getJavaMember().getName().equals("ping")).findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new ImplOfInterfaceWithDefaultMethod());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public SomeGenericInterface<List<String>> produceBeanBasedOnGenericInterface(
            InterceptionFactory<SomeGenericInterface<List<String>>> interceptionFactory) {
        interceptionFactory.configure().add(Monitor.Literal.INSTANCE)
                .filterMethods((m) -> m.getJavaMember().getName().equals("ping")
                        && Arrays.asList(m.getJavaMember().getParameterTypes()).contains(Double.class))
                .findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new SomeGenericImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public ProxyableInterface produceBeanFromUnproxyableType(InterceptionFactory<ProxyableInterface> interceptionFactory) {
        interceptionFactory.configure().add(Monitor.Literal.INSTANCE)
                .filterMethods((m) -> m.getJavaMember().getName().equals("ping")).findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new UnproxyableImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public ProxyableInterfaceWithMethodAnnotation produceBeanFromUnproxyableTypeWithMethodAnnotationInInterface(
            InterceptionFactory<ProxyableInterfaceWithMethodAnnotation> interceptionFactory) {
        // already has method level annotation, add class level annotation as well
        interceptionFactory.configure().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new UnproxyableInterfaceWithMethodAnnotationImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public ProxyableInterfaceWithClassAnnotation produceBeanFromUnproxyableTypeWithClassAnnotationInInterface(
            InterceptionFactory<ProxyableInterfaceWithClassAnnotation> interceptionFactory) {
        // already has class level annotation, add method level annotation as well
        interceptionFactory.configure().filterMethods((m) -> m.getJavaMember().getName().equals("ping")).findFirst().get()
                .add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new UnproxyableInterfaceWithClassAnnotationImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public InterfaceWithGenericsB<String, Integer> produceBeanFromUnproxyableTypeWithGenericHierarchy(
            InterceptionFactory<InterfaceWithGenericsB<String, Integer>> interceptionFactory) {
        interceptionFactory.configure().add(Monitor.Literal.INSTANCE)
                .filterMethods((m) -> m.getJavaMember().getName().equals("pong")).findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new UnproxyableInterfaceWithGenericsChainImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public InterfaceWithAnnotation produceBeanFromUnproxyableTypeAndAddAlreadyPresentAnnotation(
            InterceptionFactory<InterfaceWithAnnotation> interceptionFactory) {
        // Hello is already present on class level, Monitor is on `pong` method
        interceptionFactory.configure().add(Hello.Literal.INSTANCE)
                .filterMethods(m -> m.getJavaMember().getName().equals("pong")).findFirst().get().add(Monitor.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new InterfaceWithAnnotationImpl());
    }

    @Produced
    @Produces
    @ApplicationScoped
    public InterfaceB produceBeanWithInterfaceHierarchy(InterceptionFactory<InterfaceB> interceptionFactory) {
        interceptionFactory.configure().add(Hello.Literal.INSTANCE)
                .filterMethods(m -> m.getJavaMember().getName().equals("pingB")).findFirst().get()
                .add(Monitor.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new NonGenericInterfaceChainImpl());
    }

    static class Foo {

        String ping() {
            return "pong";
        }

    }

    @ApplicationScoped
    static class Bar {

        String pong() {
            return "ping";
        }

    }

    static void reset() {
        INVOCATIONS.clear();
    }

    public static class FooParent {

        String ping() {
            return "Parent pong";
        }
    }

    public static class FooChild extends FooParent {

    }

    public static abstract class AbstractBar {
        abstract String ping();
    }

    public static class BarImpl extends AbstractBar {

        @Override
        String ping() {
            return "BarImpl pong";
        }
    }
}
