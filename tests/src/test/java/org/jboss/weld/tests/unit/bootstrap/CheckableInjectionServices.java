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
package org.jboss.weld.tests.unit.bootstrap;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.injection.spi.InjectionContext;
import org.jboss.weld.injection.spi.InjectionServices;

/**
 * @author pmuir
 */
public class CheckableInjectionServices implements InjectionServices {

    private boolean before = false;
    private boolean after = false;
    private boolean injectedAfter = false;
    private boolean injectionTargetCorrect = false;
    private int aroundInjectForBarCalled;
    private int aroundInjectForFooCalled;

    @Override
    public <T> void aroundInject(InjectionContext<T> injectionContext) {
        before = true;
        if (injectionContext.getTarget() instanceof Bar) {
            aroundInjectForBarCalled++;
        }
        if (injectionContext.getTarget() instanceof Foo) {
            aroundInjectForFooCalled++;
            ((Foo) injectionContext.getTarget()).message = "hi!";
            if (injectionContext.getInjectionTarget().getInjectionPoints().size() == 1) {
                injectionTargetCorrect = injectionContext.getInjectionTarget().getInjectionPoints().iterator().next().getType()
                        .equals(Bar.class);
            }
        }
        injectionContext.proceed();
        after = true;
        if (injectionContext.getTarget() instanceof Foo) {
            Foo foo = (Foo) injectionContext.getTarget();
            injectedAfter = foo.getBar() instanceof Bar && foo.getMessage().equals("hi!");
        }
    }

    public void reset() {
        before = false;
        after = false;
        injectedAfter = false;
        injectionTargetCorrect = false;
        aroundInjectForFooCalled = 0;
        aroundInjectForBarCalled = 0;
    }

    public boolean isBefore() {
        return before;
    }

    public boolean isAfter() {
        return after;
    }

    public boolean isInjectedAfter() {
        return injectedAfter;
    }

    public boolean isInjectionTargetCorrect() {
        return injectionTargetCorrect;
    }

    public int getAroundInjectForBarCalled() {
        return aroundInjectForBarCalled;
    }

    public int getAroundInjectForFooCalled() {
        return aroundInjectForFooCalled;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public <T> void registerInjectionTarget(InjectionTarget<T> injectionTarget, AnnotatedType<T> annotatedType) {
    }
}
