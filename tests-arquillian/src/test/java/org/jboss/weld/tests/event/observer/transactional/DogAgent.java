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
package org.jboss.weld.tests.event.observer.transactional;

import java.lang.annotation.Annotation;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import static jakarta.ejb.TransactionManagementType.BEAN;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.UserTransaction;

@Stateless
@TransactionManagement(BEAN)
@Named
@Default
public class DogAgent implements Agent {

    public static String EVENT_FIRED = "afterEventFired";
    
    @Resource
    private UserTransaction userTransaction;
    
    @Inject
    private BeanManager jsr299Manager;

    @Override
    public void sendInTransaction(Object event, Annotation... annot) {
        try {
            userTransaction.begin();
            jsr299Manager.getEvent().select(annot).fire(event);
            Actions.add(EVENT_FIRED);
            userTransaction.commit();
        } catch (EJBException ejbException) {
            throw ejbException;
        } catch (Exception e) {
            throw new EJBException("Transaction failure", e);
        }
    }

    @Override
    public void sendInTransactionAndFail(Object event) throws Exception {
        userTransaction.begin();
        jsr299Manager.getEvent().fire(event);
        Actions.add(EVENT_FIRED);
        userTransaction.rollback();
    }

    @Override
    public void sendOutsideTransaction(Object event) {
        jsr299Manager.getEvent().fire(event);
        Actions.add(EVENT_FIRED);
    }
}
