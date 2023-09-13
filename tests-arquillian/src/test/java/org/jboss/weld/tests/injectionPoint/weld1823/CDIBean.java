/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.tests.injectionPoint.weld1823;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

/*
 *@author Emily Jiang
 */
public class CDIBean {

    CDIBean2 beanManagerInstance;

    public CDIBean(BeanManager manager) {
        Set<Bean<?>> beanList = manager.getBeans(CDIBean2.class);
        if (beanList != null && !beanList.isEmpty()) {
            Bean<?> bean = beanList.iterator().next();
            CreationalContext<?> context = manager.createCreationalContext(bean);
            beanManagerInstance = (CDIBean2) manager.getReference(bean, CDIBean2.class, context);
        }
    }

    public String getData() {
        return beanManagerInstance.getData();
    }

    public void setData(String data) {
        beanManagerInstance.setData(data);
    }
}