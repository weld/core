/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.jta;

import javax.transaction.UserTransaction;

import org.jboss.weld.bean.builtin.ee.AbstractEEBean;
import org.jboss.weld.bean.builtin.ee.AbstractEECallable;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * @author pmuir
 */
class UserTransactionBean extends AbstractEEBean<UserTransaction> {

    private static class UserTransactionCallable extends AbstractEECallable<UserTransaction> {

        private static final long serialVersionUID = -6320641773968440920L;

        public UserTransactionCallable(BeanManagerImpl beanManager) {
            super(beanManager);
        }

        public UserTransaction call() throws Exception {
            final TransactionServices transactionServices = getBeanManager().getServices().get(TransactionServices.class);
            if (transactionServices != null) {
                return transactionServices.getUserTransaction();
            } else {
                throw BeanLogger.LOG.transactionServicesNotAvailable();
            }
        }

    }

    UserTransactionBean(BeanManagerImpl beanManager) {
        super(UserTransaction.class, new UserTransactionCallable(beanManager), beanManager);
    }

    @Override
    public String toString() {
        return "Built-in Bean [javax.transaction.UserTransaction] with qualifiers [@Default]";
    }

}
