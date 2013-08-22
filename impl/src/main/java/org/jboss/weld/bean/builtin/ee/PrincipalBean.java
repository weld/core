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
package org.jboss.weld.bean.builtin.ee;

import java.security.Principal;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.security.spi.SecurityServices;

/**
 * @author pmuir
 */
public class PrincipalBean extends AbstractEEBean<Principal> {

    private static class PrincipalCallable extends AbstractEECallable<Principal> {

        private static final long serialVersionUID = -6603676793378907096L;

        public PrincipalCallable(BeanManagerImpl beanManager) {
            super(beanManager);
        }

        public Principal call() throws Exception {
            final SecurityServices securityServices = getBeanManager().getServices().get(SecurityServices.class);
            if (securityServices != null) {
                return securityServices.getPrincipal();
            } else {
                throw BeanLogger.LOG.securityServicesNotAvailable();
            }
        }

    }

    public PrincipalBean(BeanManagerImpl beanManager) {
        super(Principal.class, new PrincipalCallable(beanManager), beanManager);
    }

    @Override
    public String toString() {
        return "Built-in Bean [java.security.Principal] with qualifiers [@Default]";
    }

}
