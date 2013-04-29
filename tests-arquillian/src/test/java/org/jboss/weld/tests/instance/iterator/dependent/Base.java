/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.instance.iterator.dependent;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.jboss.weld.test.util.ActionSequence;

public abstract class Base implements Ping {

    @Inject
    InjectionPoint injectionPoint;

    @PostConstruct
    public void init() {
        ActionSequence.addAction("init", getId());
    }

    @PreDestroy
    public void destroy() {
        ActionSequence.addAction("destroy", getId());
    }

    @Override
    public Type pong() {
        assertNotNull(injectionPoint);
        return injectionPoint.getType();
    }

    protected abstract String getId();

}
