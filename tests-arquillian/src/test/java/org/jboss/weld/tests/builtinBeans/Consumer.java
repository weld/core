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
package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import org.junit.Assert;

@SessionScoped
@SuppressWarnings("serial")
public class Consumer implements Serializable {

    // Not working incontainer as there is no principal
    //@Inject Principal principal;
    @Inject
    UserTransaction userTransaction;
    @Inject
    BeanManager beanManager;
    @Inject
    Instance<Cow> cow;
    @Inject
    Event<Cow> event;
    @Inject
    CowEventObserver observer;

    @PostConstruct
    public void postConstruct() {
        cow.get().setName("Daisy");
    }

    public Instance<Cow> getCow() {
        return cow;
    }

    public Event<Cow> getEvent() {
        return event;
    }

    public void check() {
        Assert.assertTrue(Checker.checkBeanManager(beanManager));

        // Not working incontainer as there is no principal
        //assert Checker.checkPrincipal(principal);
        Assert.assertTrue(Checker.checkUserTransaction(userTransaction));
        Assert.assertTrue(Checker.checkInstance(cow));
        Assert.assertTrue(Checker.checkEvent(event, observer));
    }

}
