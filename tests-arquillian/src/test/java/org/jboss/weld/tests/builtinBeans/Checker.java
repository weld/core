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

import java.security.Principal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class Checker {

    public static boolean checkPrincipal(Principal principal) {
        principal.getName();
        return true;
    }

    public static boolean checkBeanManager(BeanManager beanManager) {
        return beanManager != null && beanManager.isScope(ApplicationScoped.class);
    }

    public static boolean checkUserTransaction(UserTransaction userTransaction) {
        try {
            if (userTransaction != null) {
                userTransaction.getStatus();
                return true;
            }
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean checkInstance(Instance<Cow> cow) {
        if (cow != null && cow.get() != null) {
            return "Daisy".equals(cow.get().getName());
        } else {
            return false;
        }
    }

    public static boolean checkEvent(Event<Cow> cowEvent, CowEventObserver observer) {
        observer.reset();
        if (cowEvent != null) {
            cowEvent.fire(new Cow());
            return observer.isObserved();
        } else {
            return false;
        }
    }

    public static boolean checkInjectionPoint(InjectionPoint injectionPoint, Class<?> injectedClass) {
        if (injectionPoint != null) {
            return injectedClass.equals(injectionPoint.getBean().getBeanClass());
        } else {
            return false;
        }
    }

    public static boolean checkEquality(Object object1, Object object2) {
        return object1.equals(object2) && object1.hashCode() == object2.hashCode();
    }

}
