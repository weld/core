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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InterceptionFactory;

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
        return interceptionFactory.createInterceptedInstance(new ArrayList<>(Arrays.asList("ping", "pong")));
    }


    @Produces
    @Produced
    public FooParent produceFooParent(InterceptionFactory<FooParent> interceptionFactory) {
        interceptionFactory.configure().filterMethods((m) ->
            m.getJavaMember().getName().equals("ping")
        ).findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new FooChild());
    }
    
    @Produces
    @Produced
    public AbstractBar produceAbstractBar(InterceptionFactory<AbstractBar> interceptionFactory) {
        interceptionFactory.configure().filterMethods((m) ->
            m.getJavaMember().getName().equals("ping")
        ).findFirst().get().add(Hello.Literal.INSTANCE);
        return interceptionFactory.createInterceptedInstance(new BarImpl());
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

    public static class BarImpl extends AbstractBar{

        @Override
        String ping() {
            return "BarImpl pong";
        }
    }
}
